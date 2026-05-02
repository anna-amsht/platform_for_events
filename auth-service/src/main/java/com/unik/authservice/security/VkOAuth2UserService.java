package com.unik.authservice.security;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class VkOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final String VK_USER_INFO_URL = "https://api.vk.com/method/users.get";
    private static final String VK_API_VERSION = "5.131";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", userRequest.getAdditionalParameters().get("email"));

        Map<String, Object> userInfo = fetchUserInfo(userRequest);
        attributes.putAll(userInfo);

        Object id = attributes.get("id");
        if (id == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_vk_user"), "VK user id is missing");
        }

        return new DefaultOAuth2User(List.of(), attributes, "id");
    }

    private Map<String, Object> fetchUserInfo(OAuth2UserRequest userRequest) {
        String uri = UriComponentsBuilder.fromHttpUrl(VK_USER_INFO_URL)
                .queryParam("fields", "first_name,last_name")
                .queryParam("v", VK_API_VERSION)
                .queryParam("access_token", userRequest.getAccessToken().getTokenValue())
                .toUriString();

        try {
            Map<String, Object> body = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            ).getBody();

            Object response = Objects.requireNonNull(body, "VK user info response is empty").get("response");
            if (response instanceof List<?> users && !users.isEmpty() && users.getFirst() instanceof Map<?, ?> user) {
                Map<String, Object> result = new HashMap<>();
                user.forEach((key, value) -> result.put(String.valueOf(key), value));
                return result;
            }
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_vk_user_info"),
                    "VK user info response does not contain a user"
            );
        } catch (RestClientException | NullPointerException ex) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("vk_user_info_error"),
                    "Failed to load VK user info",
                    ex
            );
        }
    }
}
