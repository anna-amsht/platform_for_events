package com.unik.authservice.security;

import com.unik.authservice.config.AppProperties;
import com.unik.authservice.dto.response.TokenResponse;
import com.unik.authservice.entity.OAuthProvider;
import com.unik.authservice.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final AppProperties appProperties;

    public OAuth2LoginSuccessHandler(AuthService authService, AppProperties appProperties) {
        this.authService = authService;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuthProvider provider = OAuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
        OAuth2User principal = oauthToken.getPrincipal();

        String externalUserId = String.valueOf(principal.getAttribute("id"));
        String email = principal.getAttribute("email");
        TokenResponse token = authService.loginWithOAuth(provider, externalUserId, email);

        String redirectUrl = UriComponentsBuilder.fromUriString(appProperties.frontend().url())
                .path("/oauth2/callback")
                .queryParam("access_token", token.accessToken())
                .queryParam("token_type", token.tokenType())
                .queryParam("expires_in", token.expiresInSeconds())
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
