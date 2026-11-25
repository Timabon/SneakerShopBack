package com.example.sneakershop.checkout;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.example.sneakershop.checkout.CheckoutController.Item;
import com.example.sneakershop.checkout.CheckoutController.SessionRequest;
import com.example.sneakershop.checkout.CheckoutController.SessionResponse;
import com.example.sneakershop.checkout.CheckoutController.StatusResponse;
import com.example.sneakershop.checkout.CheckoutOrderStore.CheckoutOrder;
import com.example.sneakershop.checkout.CheckoutOrderStore.LineItem;
import com.example.sneakershop.product.Product;
import com.example.sneakershop.product.ProductRepository;
import com.example.sneakershop.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
class CheckoutService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    private static final String CURRENCY = "eur";
    private static final long MAX_TOTAL_CENTS = 99_999_999;
    private static final Pattern SESSION_ID = Pattern.compile("cs_test_[A-Za-z0-9]+");
    private static final Pattern GUEST_TOKEN = Pattern.compile("[a-f0-9]{64}");

    private final CheckoutOrderStore orders;
    private final ProductRepository products;
    private final ObjectMapper json;
    private final CheckoutConfirmationMailer confirmationMailer;
    private final StripeClient stripe;
    private final String webhookSecret;
    private final String frontendUrl;

    CheckoutService(
            CheckoutOrderStore orders, ProductRepository products, ObjectMapper json,
            CheckoutConfirmationMailer confirmationMailer,
            @Value("${checkout.stripe.secret-key}") String stripeKey,
            @Value("${checkout.stripe.webhook-secret}") String webhookSecret,
            @Value("${checkout.frontend-url}") String frontendUrl) {
        this.orders = orders;
        this.products = products;
        this.json = json;
        this.confirmationMailer = confirmationMailer;
        this.stripe = stripeKey.startsWith("sk_test_") || stripeKey.startsWith("rk_test_")
                ? StripeClient.builder().setApiKey(stripeKey).setConnectTimeout(5_000)
                        .setReadTimeout(10_000).setMaxNetworkRetries(1).build()
                : null;
        this.webhookSecret = webhookSecret;
        this.frontendUrl = frontendUrl.replaceFirst("/+$", "");
    }

    SessionResponse create(SessionRequest request, User user, boolean hasAuthorization) {
        requireValidAuthorization(user, hasAuthorization);
        if (stripe == null || !webhookSecret.startsWith("whsec_")) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Stripe sandbox is not configured");
        }
        if (request.requestId().version() != 4 || request.requestId().variant() != 2) {
            throw new ResponseStatusException(BAD_REQUEST, "A random request ID is required");
        }

        List<Item> items = request.items().stream()
                .sorted(Comparator.comparing(Item::productId).thenComparing(Item::size))
                .toList();
        String scope = user == null ? "guest" : "user:" + user.getId();
        String requestHash = digest("checkout-request:" + request.requestId());
        CheckoutOrder order = orders.findByRequest(scope, requestHash)
                .orElseGet(() -> saveOrder(user, scope, requestHash, request.requestId(), items));

        if (!items.equals(order.items().stream().map(LineItem::item).toList())) {
            throw new ResponseStatusException(CONFLICT, "Checkout request ID already used for a different cart");
        }
        // Stripe may prune idempotency keys after 24 hours. Never recreate an old attempt.
        if (!"pending".equals(order.status())
                || order.createdAt().plus(Duration.ofHours(23)).isBefore(Instant.now())) {
            throw new ResponseStatusException(CONFLICT, "Start a new checkout for this cart");
        }

        if (order.sessionId() == null) {
            Session session = openStripeSession(order);
            orders.attachSession(order.id(), session.getId(), session.getUrl());
            order = orders.findByRequest(scope, requestHash).orElseThrow();
        }
        String token = order.guestSalt() == null ? null : guestToken(request.requestId(), order.guestSalt());
        return new SessionResponse(order.sessionId(), order.url(), token);
    }

    StatusResponse status(String sessionId, User user, boolean hasAuthorization, String guestToken) {
        requireValidAuthorization(user, hasAuthorization);
        if (user == null && guestToken == null) {
            throw new ResponseStatusException(UNAUTHORIZED);
        }
        if (!SESSION_ID.matcher(sessionId).matches()) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid Checkout Session ID");
        }
        CheckoutOrder order = orders.findBySession(sessionId)
                .filter(found -> canView(found, user, guestToken))
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN));
        return new StatusResponse(order.id(), order.status());
    }

    void handleWebhook(String payload, String signature) {
        if (!webhookSecret.startsWith("whsec_")) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Stripe webhook is not configured");
        }
        Event event;
        JsonNode session;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
            session = json.readTree(event.getDataObjectDeserializer().getRawJson());
        } catch (SignatureVerificationException | JsonProcessingException | RuntimeException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid Stripe webhook");
        }
        if (!Boolean.FALSE.equals(event.getLivemode()) || !"checkout.session".equals(session.path("object").asText())
                || !"payment".equals(session.path("mode").asText())) {
            return;
        }
        boolean paid = "paid".equals(session.path("payment_status").asText());
        String status = switch (event.getType()) {
            case "checkout.session.completed", "checkout.session.async_payment_succeeded" -> paid ? "paid" : null;
            case "checkout.session.async_payment_failed", "checkout.session.expired" -> paid ? null : "failed";
            default -> null;
        };
        if (status == null) {
            return;
        }

        UUID reference;
        try {
            reference = UUID.fromString(session.path("client_reference_id").asText());
        } catch (IllegalArgumentException ex) {
            return;
        }
        orders.findByStripeRequestKey(reference).ifPresent(order -> {
            if (order.sessionId() == null) {
                throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Checkout Session is still being saved");
            }
            if (order.sessionId().equals(session.path("id").asText())
                    && CURRENCY.equals(session.path("currency").asText())
                    && order.totalCents() == session.path("amount_total").asLong(-1)) {
                if (paid) {
                    String email = session.path("customer_details").path("email").asText("");
                    orders.markPaid(order.id(), confirmationMailer.recipientFor(email));
                } else {
                    orders.updatePaymentStatus(order.id(), status);
                }
            }
        });
    }

    private CheckoutOrder saveOrder(User user, String scope, String requestHash, UUID requestId, List<Item> items) {
        if (new HashSet<>(items.stream().map(item -> item.productId() + ":" + item.size()).toList())
                .size() != items.size()) {
            throw new ResponseStatusException(BAD_REQUEST, "Combine quantities for the same product and size");
        }
        Map<Long, Product> catalog = new HashMap<>();
        products.findAllById(items.stream().map(Item::productId).distinct().toList())
                .forEach(product -> catalog.put(product.getProductId(), product));
        List<LineItem> snapshot = items.stream().map(item -> price(item, catalog)).toList();
        long total = snapshot.stream().mapToLong(line -> line.unitCents() * line.item().quantity()).sum();
        if (total < 50 || total > MAX_TOTAL_CENTS) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart total is outside Stripe's payment limits");
        }

        UUID salt = user == null ? UUID.randomUUID() : null;
        String tokenHash = salt == null ? null : digest(guestToken(requestId, salt));
        orders.insert(user == null ? null : user.getId(), scope, requestHash, salt, tokenHash,
                frontendUrl, snapshot, total);
        return orders.findByRequest(scope, requestHash).orElseThrow();
    }

    private LineItem price(Item item, Map<Long, Product> catalog) {
        Product product = catalog.get(item.productId());
        if (product == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Cart contains an unknown product");
        }
        try {
            long cents = product.getProductPrice().movePointRight(2).longValueExact();
            if (cents > 0 && cents <= MAX_TOTAL_CENTS) {
                return new LineItem(item, product.getProductName(), cents);
            }
        } catch (ArithmeticException ex) {
            log.warn("Product {} has a price that cannot be represented in EUR cents", item.productId());
        }
        throw new ResponseStatusException(BAD_REQUEST, "Cart contains a product with an invalid price");
    }

    private Session openStripeSession(CheckoutOrder order) {
        String requestKey = order.stripeRequestKey().toString();
        var params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setIntegrationIdentifier("sneakershop-hosted-vnjfkhqg")
                .setClientReferenceId(requestKey)
                .setSuccessUrl(order.frontendUrl() + "/checkout/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(order.frontendUrl() + "/checkout?canceled=1")
                .setAdaptivePricing(SessionCreateParams.AdaptivePricing.builder().setEnabled(false).build())
                .setShippingAddressCollection(SessionCreateParams.ShippingAddressCollection.builder()
                        .addAllowedCountry(SessionCreateParams.ShippingAddressCollection.AllowedCountry.AT)
                        .build());
        for (LineItem line : order.items()) {
            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(line.item().quantity().longValue())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(CURRENCY)
                            .setUnitAmount(line.unitCents())
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(line.name())
                                    .setDescription("EU size " + line.item().size())
                                    .build())
                            .build())
                    .build());
        }
        try {
            Session session = stripe.v1().checkout().sessions().create(
                    params.build(), RequestOptions.builder().setIdempotencyKey(requestKey).build());
            if (!isHostedSession(session)) {
                throw new ResponseStatusException(BAD_GATEWAY, "Stripe did not return a hosted test Checkout Session");
            }
            return session;
        } catch (StripeException ex) {
            log.warn("Stripe Checkout creation failed for order {} (Stripe request {}, code {})",
                    order.id(), ex.getRequestId(), ex.getCode());
            throw new ResponseStatusException(BAD_GATEWAY, "Stripe Checkout is temporarily unavailable");
        }
    }

    private static boolean isHostedSession(Session session) {
        if (session.getId() == null || !SESSION_ID.matcher(session.getId()).matches() || session.getUrl() == null) {
            return false;
        }
        URI url = URI.create(session.getUrl());
        String path = url.getRawPath();
        return "https".equals(url.getScheme()) && "checkout.stripe.com".equals(url.getRawAuthority())
                && (("/c/pay/" + session.getId()).equals(path) || ("/g/pay/" + session.getId()).equals(path));
    }

    private static boolean canView(CheckoutOrder order, User user, String token) {
        if (order.guestTokenHash() == null) {
            return user != null && Objects.equals(order.userId(), user.getId());
        }
        return token != null && GUEST_TOKEN.matcher(token).matches()
                && MessageDigest.isEqual(order.guestTokenHash().getBytes(StandardCharsets.US_ASCII),
                        digest(token).getBytes(StandardCharsets.US_ASCII));
    }

    private static void requireValidAuthorization(User user, boolean hasAuthorization) {
        if (hasAuthorization && user == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid shopper sign-in");
        }
    }

    private static String guestToken(UUID requestId, UUID salt) {
        return digest("checkout-guest:" + salt + ":" + requestId);
    }

    private static String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
