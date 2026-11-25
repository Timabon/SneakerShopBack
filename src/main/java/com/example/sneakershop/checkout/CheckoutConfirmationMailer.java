package com.example.sneakershop.checkout;

import com.example.sneakershop.checkout.CheckoutOrderStore.CheckoutOrder;
import com.example.sneakershop.checkout.CheckoutOrderStore.LineItem;
import com.example.sneakershop.checkout.CheckoutOrderStore.PendingConfirmation;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class CheckoutConfirmationMailer {
    private static final Logger log = LoggerFactory.getLogger(CheckoutConfirmationMailer.class);

    private final CheckoutOrderStore orders;
    private final RestClient resend;
    private final URI endpoint;
    private final String apiKey;
    private final String sender;
    private final String testRecipient;

    CheckoutConfirmationMailer(
            CheckoutOrderStore orders, RestClient.Builder clientBuilder,
            @Value("${checkout.confirmation.api-url}") URI endpoint,
            @Value("${checkout.confirmation.api-key}") String apiKey,
            @Value("${checkout.confirmation.from}") String sender,
            @Value("${checkout.confirmation.test-recipient}") String testRecipient) {
        this.orders = orders;
        this.endpoint = endpoint;
        this.apiKey = apiKey.strip();
        this.sender = sender.strip();
        this.testRecipient = testRecipient.strip();

        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.resend = clientBuilder.requestFactory(factory).build();
    }

    String recipientFor(String checkoutEmail) {
        return enabled() && testRecipient.equalsIgnoreCase(checkoutEmail.strip()) ? testRecipient : null;
    }

    @Scheduled(fixedDelay = 5_000)
    @Transactional
    public void deliverNext() {
        if (enabled()) {
            orders.nextConfirmation(testRecipient).ifPresent(this::deliver);
        }
    }

    private void deliver(PendingConfirmation confirmation) {
        CheckoutOrder order = confirmation.order();
        Email email = new Email(
                sender, List.of(confirmation.recipient()), "SneakerShop test order #" + order.id() + " confirmed",
                confirmationText(order));
        try {
            SendResponse response = resend.post().uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(apiKey))
                    .header("Idempotency-Key", "checkout-confirmation/" + order.stripeRequestKey())
                    .body(email).retrieve().body(SendResponse.class);
            if (response == null || response.id() == null || response.id().isBlank()) {
                throw new RestClientException("Missing email delivery reference");
            }
        } catch (RestClientException ex) {
            int delaySeconds = Math.min(3_600, 30 * (1 << Math.min(confirmation.attempts(), 7)));
            orders.retryConfirmation(order.id(), delaySeconds);
            String reason = ex instanceof RestClientResponseException failure
                    ? failure.getStatusCode().toString() : ex.getClass().getSimpleName();
            log.warn("Confirmation email request failed for order {} ({})", order.id(), reason);
            return;
        }
        orders.markConfirmationSent(order.id());
    }

    private boolean enabled() {
        return !apiKey.isBlank() && !sender.isBlank() && !testRecipient.isBlank();
    }

    private static String confirmationText(CheckoutOrder order) {
        StringBuilder text = new StringBuilder("Thanks for shopping with SneakerShop.\n\n")
                .append("Your test payment for order #").append(order.id()).append(" is confirmed.\n\n");
        for (LineItem line : order.items()) {
            text.append(line.item().quantity()).append(" × ").append(line.name())
                    .append(" (EU size ").append(line.item().size()).append(")\n");
        }
        return text.append("\nTotal paid: ").append(BigDecimal.valueOf(order.totalCents(), 2).toPlainString())
                .append(" EUR\n\nSneakerShop").toString();
    }

    private record Email(String from, List<String> to, String subject, String text) {}

    private record SendResponse(String id) {}
}
