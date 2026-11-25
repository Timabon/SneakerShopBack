package com.example.sneakershop.security;

import com.example.sneakershop.security.jwt.JwtService;
import com.example.sneakershop.user.User;
import com.example.sneakershop.user.UserRepository;
import com.example.sneakershop.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleSignIn {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
    private static final Pattern CODE = Pattern.compile("[A-Za-z0-9_-]{43}");

    private final JdbcClient jdbc;
    private final UserService accounts;
    private final UserRepository users;
    private final String frontend;

    GoogleSignIn(JdbcClient jdbc, UserService accounts, UserRepository users,
            @Value("${checkout.frontend-url}") String frontend) {
        this.jdbc = jdbc;
        this.accounts = accounts;
        this.users = users;
        this.frontend = frontend.replaceFirst("/+$", "");
    }

    void success(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        String challenge = (String) request.getAttribute(GoogleAuthorizationRequests.BROWSER_CHALLENGE);
        if (!(authentication.getPrincipal() instanceof OidcUser profile) || !isCode(challenge)
                || !Boolean.TRUE.equals(profile.getEmailVerified())
                || !validClaim(profile.getSubject()) || !validClaim(profile.getEmail())) {
            failure(response, "google_sign_in_failed");
            return;
        }

        String email = profile.getEmail().strip().toLowerCase(Locale.ROOT);
        Optional<User> account = findAccount(profile.getSubject(), email, profile.getFullName());
        if (account.isEmpty()) {
            failure(response, "account_exists");
            return;
        }

        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String code = BASE64_URL.encodeToString(random);
        jdbc.sql("DELETE FROM google_sign_in_codes WHERE expires_at <= now()").update();
        jdbc.sql("""
                INSERT INTO google_sign_in_codes (code_hash, browser_challenge, user_id, expires_at)
                VALUES (:hash, :challenge, :userId, now() + interval '2 minutes')
                """)
                .param("hash", hash(code)).param("challenge", challenge)
                .param("userId", account.get().getId()).update();

        redirect(response, "code=" + code);
    }

    Optional<String> exchange(String code, String verifier) {
        if (!isCode(code) || !isCode(verifier)) {
            return Optional.empty();
        }
        return jdbc.sql("""
                DELETE FROM google_sign_in_codes
                WHERE code_hash = :hash AND browser_challenge = :challenge AND expires_at > now()
                RETURNING user_id
                """)
                .param("hash", hash(code)).param("challenge", hash(verifier)).query(Long.class).optional()
                .flatMap(users::findById)
                .map(user -> JwtService.generateToken(user.getEmail()));
    }

    void failure(HttpServletResponse response, String reason) throws IOException {
        redirect(response, "error=" + reason);
    }

    private void redirect(HttpServletResponse response, String fragment) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.sendRedirect(frontend + "/oauth-success#" + fragment);
    }

    private Optional<User> findAccount(String subject, String email, String name) {
        String displayName = StringUtils.hasText(name) ? name.strip() : email;
        int length = Math.min(displayName.codePointCount(0, displayName.length()), 255);
        try {
            return accounts.findOrCreateGoogleUser(
                    subject, email, displayName.substring(0, displayName.offsetByCodePoints(0, length)));
        } catch (DataIntegrityViolationException ex) {
            // A concurrent signup can win either the Google subject or the email constraint.
            Optional<User> existing = users.findByGoogleSubject(subject);
            if (existing.isPresent() || users.existsByEmailIgnoreCase(email)) {
                return existing;
            }
            throw ex;
        }
    }

    static boolean isCode(String value) {
        return value != null && CODE.matcher(value).matches();
    }

    private static boolean validClaim(String value) {
        return StringUtils.hasText(value) && value.length() <= 255;
    }

    private static String hash(String value) {
        try {
            return BASE64_URL.encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
