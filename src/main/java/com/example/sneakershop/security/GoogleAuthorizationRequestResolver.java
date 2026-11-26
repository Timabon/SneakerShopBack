package com.example.sneakershop.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

final class GoogleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    private final DefaultOAuth2AuthorizationRequestResolver login;
    private final DefaultOAuth2AuthorizationRequestResolver signup;

    GoogleAuthorizationRequestResolver(ClientRegistrationRepository clients) {
        String baseUri = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;
        this.login = new DefaultOAuth2AuthorizationRequestResolver(clients, baseUri);
        this.signup = new DefaultOAuth2AuthorizationRequestResolver(clients, baseUri);
        this.signup.setAuthorizationRequestCustomizer(builder ->
                builder.additionalParameters(parameters -> parameters.put("prompt", "select_account")));
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return resolverFor(request).resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        return resolverFor(request).resolve(request, registrationId);
    }

    private DefaultOAuth2AuthorizationRequestResolver resolverFor(HttpServletRequest request) {
        return "signup".equals(request.getParameter("intent")) ? signup : login;
    }
}
