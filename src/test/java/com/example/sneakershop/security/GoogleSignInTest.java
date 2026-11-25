package com.example.sneakershop.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.sneakershop.dto.UserDTO;
import com.example.sneakershop.security.jwt.JwtService;
import com.example.sneakershop.user.UserRepository;
import com.example.sneakershop.user.UserService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class GoogleSignInTest {
    private static final String VERIFIER = "a".repeat(43);

    @Autowired private GoogleSignIn google;
    @Autowired private JwtService jwt;
    @Autowired private UserService accounts;
    @Autowired private UserRepository users;

    @Test
    void createsAccountAndExchangesCodeOnceForTheInitiatingBrowser() throws Exception {
        String subject = UUID.randomUUID().toString();
        String email = subject + "@example.test";
        String code = signIn(subject, email, true).getRedirectedUrl().split("#code=", 2)[1];
        var account = users.findByGoogleSubject(subject).orElseThrow();

        assertThat(account.getBasket()).isNotNull();
        assertThat(account.getPassword()).isNull();
        assertThat(google.exchange(code, "b".repeat(43))).isEmpty();
        assertThat(google.exchange(code, VERIFIER).map(jwt::extractEmail)).contains(email);
        assertThat(google.exchange(code, VERIFIER)).isEmpty();

        String changedEmailCode = signIn(subject, "updated-" + email, true)
                .getRedirectedUrl().split("#code=", 2)[1];
        assertThat(google.exchange(changedEmailCode, VERIFIER).map(jwt::extractEmail)).contains(email);
        assertThat(users.findByGoogleSubject(subject)).get().extracting(user -> user.getId()).isEqualTo(account.getId());
    }

    @Test
    void leavesExistingPasswordAccountsSeparateAndRequiresVerifiedEmail() throws Exception {
        String subject = UUID.randomUUID().toString();
        String email = "Signup-" + subject + "@example.test";
        accounts.createUser(new UserDTO("Existing user", "ExampleOnly123", email));

        assertThat(signIn(subject, email, true).getRedirectedUrl()).endsWith("#error=account_exists");
        assertThat(signIn(subject, "other-" + email, false).getRedirectedUrl()).endsWith("#error=google_sign_in_failed");
        assertThat(users.findByGoogleSubject(subject)).isEmpty();
    }

    @Test
    void carriesBrowserChallengeOnlyOnMatchingOAuthState() throws Exception {
        var requests = new GoogleAuthorizationRequests();
        var start = new MockHttpServletRequest();
        start.setParameter("challenge", challenge());
        var request = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("client").state("expected-state").build();
        var response = new MockHttpServletResponse();
        requests.saveAuthorizationRequest(request, start, response);

        var callback = new MockHttpServletRequest();
        callback.setSession(start.getSession());
        callback.setParameter("state", "wrong-state");
        assertThat(requests.removeAuthorizationRequest(callback, response)).isNull();
        assertThat(callback.getAttribute(GoogleAuthorizationRequests.BROWSER_CHALLENGE)).isNull();

        callback.setParameter("state", "expected-state");
        assertThat(requests.removeAuthorizationRequest(callback, response)).isNotNull();
        assertThat(callback.getAttribute(GoogleAuthorizationRequests.BROWSER_CHALLENGE)).isEqualTo(challenge());
        assertThat(requests.removeAuthorizationRequest(callback, response)).isNull();
    }

    private MockHttpServletResponse signIn(String subject, String email, boolean verified) throws Exception {
        var profile = mock(OidcUser.class);
        when(profile.getSubject()).thenReturn(subject);
        when(profile.getEmail()).thenReturn(email);
        when(profile.getEmailVerified()).thenReturn(verified);
        when(profile.getFullName()).thenReturn("Google shopper");

        var callback = new MockHttpServletRequest();
        callback.setAttribute(GoogleAuthorizationRequests.BROWSER_CHALLENGE, challenge());
        var response = new MockHttpServletResponse();
        google.success(callback, response, UsernamePasswordAuthenticationToken.authenticated(profile, "", List.of()));
        return response;
    }

    private static String challenge() throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(VERIFIER.getBytes(StandardCharsets.US_ASCII)));
    }
}
