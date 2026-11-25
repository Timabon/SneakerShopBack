package com.example.sneakershop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

final class GoogleAuthorizationRequests implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    static final String BROWSER_CHALLENGE = GoogleAuthorizationRequests.class.getName() + ".browserChallenge";

    private final HttpSessionOAuth2AuthorizationRequestRepository requests =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return requests.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorization, HttpServletRequest request, HttpServletResponse response) {
        String challenge = request.getParameter("challenge");
        if (authorization != null && GoogleSignIn.isCode(challenge)) {
            authorization = OAuth2AuthorizationRequest.from(authorization)
                    .attributes(attributes -> attributes.put(BROWSER_CHALLENGE, challenge))
                    .build();
        }
        requests.saveAuthorizationRequest(authorization, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorization = requests.removeAuthorizationRequest(request, response);
        if (authorization != null) {
            request.setAttribute(BROWSER_CHALLENGE, authorization.getAttribute(BROWSER_CHALLENGE));
        }
        return authorization;
    }
}
