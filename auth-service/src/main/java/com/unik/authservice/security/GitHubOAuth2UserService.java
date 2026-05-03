package com.unik.authservice.security;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GitHubOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String EMAILS_URL = "https://api.github.com/user/emails";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = delegate.loadUser(userRequest);
        Map<String, Object> attributes = new HashMap<>(user.getAttributes());

        Object id = attributes.get("id");
        if (id == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_github_user"), "GitHub user id is missing");
        }

        if (attributes.get("email") == null) {
            attributes.put("email", fetchPrimaryEmail(userRequest));
        }

        return new DefaultOAuth2User(user.getAuthorities(), attributes, "id");
    }

    private String fetchPrimaryEmail(OAuth2UserRequest userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userRequest.getAccessToken().getTokenValue());

        try {
            List<Map<String, Object>> emails = restTemplate.exchange(
                    EMAILS_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }
            ).getBody();

            if (emails == null) {
                return null;
            }

            return emails.stream()
                    .filter(email -> Boolean.TRUE.equals(email.get("primary")) && Boolean.TRUE.equals(email.get("verified")))
                    .map(email -> email.get("email"))
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .findFirst()
                    .orElse(null);
        } catch (RestClientException ex) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("github_email_error"),
                    "Failed to load GitHub user email",
                    ex
            );
        }
    }
}
