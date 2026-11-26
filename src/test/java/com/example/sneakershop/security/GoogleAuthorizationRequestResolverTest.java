package com.example.sneakershop.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.util.UriComponentsBuilder;

class GoogleAuthorizationRequestResolverTest {
    @Test
    void requestsGoogleAccountSelectionOnlyForSignup() {
        var client = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId("test-client").clientSecret("test-secret").build();
        var resolver = new GoogleAuthorizationRequestResolver(new InMemoryClientRegistrationRepository(client));
        String path = "/oauth2/authorization/google";
        var request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);

        var login = UriComponentsBuilder.fromUriString(resolver.resolve(request).getAuthorizationRequestUri())
                .build().getQueryParams();
        assertThat(login).doesNotContainKey("prompt");

        request.setParameter("intent", "signup");
        var signup = UriComponentsBuilder.fromUriString(resolver.resolve(request).getAuthorizationRequestUri())
                .build().getQueryParams();
        assertThat(signup.getFirst("prompt")).isEqualTo("select_account");
        assertThat(signup.getFirst("state")).isNotBlank();
        assertThat(signup.getFirst("nonce")).isNotBlank();
    }
}
