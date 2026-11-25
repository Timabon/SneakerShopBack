package com.example.sneakershop.checkout;

import com.example.sneakershop.checkout.CheckoutController.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
class CheckoutOrderStore {
    private final JdbcClient jdbc;
    private final ObjectMapper json;

    CheckoutOrderStore(JdbcClient jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    Optional<CheckoutOrder> findByRequest(String scope, String requestHash) {
        return jdbc.sql("""
                SELECT * FROM checkout_orders WHERE request_scope = :scope AND request_hash = :hash
                """)
                .param("scope", scope).param("hash", requestHash).query(this::mapOrder).optional();
    }

    Optional<CheckoutOrder> findBySession(String sessionId) {
        return jdbc.sql("SELECT * FROM checkout_orders WHERE stripe_session_id = :sessionId")
                .param("sessionId", sessionId).query(this::mapOrder).optional();
    }

    Optional<CheckoutOrder> findByStripeRequestKey(UUID key) {
        return jdbc.sql("SELECT * FROM checkout_orders WHERE stripe_request_key = :key")
                .param("key", key).query(this::mapOrder).optional();
    }

    void insert(Long userId, String scope, String requestHash, UUID salt, String tokenHash,
                String frontendUrl, List<LineItem> items, long totalCents) {
        jdbc.sql("""
                INSERT INTO checkout_orders
                    (user_id, request_scope, request_hash, guest_token_salt, guest_token_hash,
                     stripe_request_key, frontend_url, line_items, total_cents)
                VALUES (:userId, :scope, :hash, :salt, :tokenHash, :key, :frontendUrl,
                        CAST(:items AS jsonb), :totalCents)
                ON CONFLICT (request_scope, request_hash) DO NOTHING
                """)
                .param("userId", userId, Types.BIGINT)
                .param("scope", scope)
                .param("hash", requestHash)
                .param("salt", salt, Types.OTHER)
                .param("tokenHash", tokenHash, Types.CHAR)
                .param("key", UUID.randomUUID())
                .param("frontendUrl", frontendUrl)
                .param("items", serialize(items))
                .param("totalCents", totalCents)
                .update();
    }

    void attachSession(long orderId, String sessionId, String url) {
        jdbc.sql("""
                UPDATE checkout_orders SET stripe_session_id = :sessionId, stripe_checkout_url = :url
                WHERE order_id = :orderId AND stripe_session_id IS NULL
                """)
                .param("sessionId", sessionId).param("url", url).param("orderId", orderId).update();
    }

    void updatePaymentStatus(long orderId, String status) {
        jdbc.sql("""
                UPDATE checkout_orders SET status = :status
                WHERE order_id = :orderId AND status <> 'paid' AND status <> :status
                """)
                .param("orderId", orderId).param("status", status).update();
    }

    void markPaid(long orderId, String recipient) {
        jdbc.sql("""
                WITH paid_order AS (
                    UPDATE checkout_orders SET status = 'paid'
                    WHERE order_id = :orderId AND status <> 'paid'
                    RETURNING order_id
                )
                INSERT INTO checkout_confirmations (order_id, recipient)
                SELECT order_id, :recipient FROM paid_order WHERE :recipient IS NOT NULL
                """)
                .param("orderId", orderId).param("recipient", recipient, Types.VARCHAR).update();
    }

    Optional<PendingConfirmation> nextConfirmation(String testRecipient) {
        return jdbc.sql("""
                SELECT orders.*, confirmation.recipient, confirmation.attempts
                FROM checkout_confirmations confirmation
                JOIN checkout_orders orders USING (order_id)
                WHERE confirmation.sent_at IS NULL AND confirmation.recipient = :recipient
                  AND confirmation.next_attempt_at <= now() AND confirmation.deadline > now()
                ORDER BY confirmation.next_attempt_at, confirmation.order_id
                LIMIT 1 FOR UPDATE OF confirmation SKIP LOCKED
                """)
                .param("recipient", testRecipient)
                .query((row, number) -> new PendingConfirmation(
                        mapOrder(row, number), row.getString("recipient"), row.getInt("attempts")))
                .optional();
    }

    void markConfirmationSent(long orderId) {
        jdbc.sql("UPDATE checkout_confirmations SET sent_at = now() WHERE order_id = :orderId")
                .param("orderId", orderId).update();
    }

    void retryConfirmation(long orderId, int delaySeconds) {
        jdbc.sql("""
                UPDATE checkout_confirmations
                SET attempts = attempts + 1, next_attempt_at = now() + make_interval(secs => :delay)
                WHERE order_id = :orderId
                """)
                .param("orderId", orderId).param("delay", delaySeconds).update();
    }

    private CheckoutOrder mapOrder(ResultSet row, int rowNumber) throws SQLException {
        try {
            return new CheckoutOrder(
                    row.getLong("order_id"),
                    row.getObject("user_id", Long.class),
                    row.getObject("guest_token_salt", UUID.class),
                    row.getString("guest_token_hash"),
                    row.getObject("stripe_request_key", UUID.class),
                    row.getString("stripe_session_id"),
                    row.getString("stripe_checkout_url"),
                    row.getString("frontend_url"),
                    json.readValue(row.getString("line_items"), new TypeReference<List<LineItem>>() {}),
                    row.getLong("total_cents"),
                    row.getString("status"),
                    row.getObject("created_at", OffsetDateTime.class).toInstant());
        } catch (JsonProcessingException ex) {
            throw new SQLException("Could not read checkout line items", ex);
        }
    }

    private String serialize(List<LineItem> items) {
        try {
            return json.writeValueAsString(items);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not write checkout line items", ex);
        }
    }

    record LineItem(Item item, String name, long unitCents) {}

    record PendingConfirmation(CheckoutOrder order, String recipient, int attempts) {}

    record CheckoutOrder(
            long id, Long userId, UUID guestSalt, String guestTokenHash, UUID stripeRequestKey,
            String sessionId, String url, String frontendUrl, List<LineItem> items, long totalCents,
            String status, Instant createdAt) {}
}
