package com.example.sneakershop.checkout;

import com.example.sneakershop.user.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutController {
    private final CheckoutService service;

    public CheckoutController(CheckoutService service) {
        this.service = service;
    }

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> create(
            @Valid @RequestBody SessionRequest request,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(service.create(request, user, authorization != null));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<StatusResponse> status(
            @PathVariable String sessionId,
            @AuthenticationPrincipal User user,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Checkout-Guest-Token", required = false) String guestToken) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(service.status(sessionId, user, authorization != null, guestToken));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody byte[] body,
            @RequestHeader("Stripe-Signature") String signature) {
        service.handleWebhook(new String(body, StandardCharsets.UTF_8), signature);
        return ResponseEntity.noContent().build();
    }

    public record SessionRequest(
            @NotNull UUID requestId,
            @NotNull @Size(min = 1, max = 100) List<@NotNull @Valid Item> items) {}

    public record Item(
            @NotNull @Positive Long productId,
            @NotNull @Min(36) @Max(41) Integer size,
            @NotNull @Min(1) @Max(99) Integer quantity) {}

    public record SessionResponse(String sessionId, String url, String guestAccessToken) {}

    public record StatusResponse(long orderId, String status) {}
}
