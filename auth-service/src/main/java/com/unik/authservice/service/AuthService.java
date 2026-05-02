package com.unik.authservice.service;

import com.unik.authservice.client.UserServiceClient;
import com.unik.authservice.config.AppProperties;
import com.unik.authservice.dto.internal.UpsertUserProfileRequest;
import com.unik.authservice.dto.request.LoginRequest;
import com.unik.authservice.dto.request.RegisterRequest;
import com.unik.authservice.dto.response.TokenResponse;
import com.unik.authservice.entity.AccountRole;
import com.unik.authservice.entity.OAuthAccount;
import com.unik.authservice.entity.OAuthProvider;
import com.unik.authservice.entity.User;
import com.unik.authservice.exception.EmailAlreadyExistsException;
import com.unik.authservice.repository.OAuthAccountRepository;
import com.unik.authservice.repository.UserRepository;
import com.unik.authservice.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final UserServiceClient userServiceClient;

    public AuthService(
            UserRepository userRepository,
            OAuthAccountRepository oAuthAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AppProperties appProperties,
            UserServiceClient userServiceClient
    ) {
        this.userRepository = userRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = userRepository.save(User.local(request.email(), passwordEncoder.encode(request.password()),
                normalizeRole(request.role())));
        userServiceClient.upsertUserProfile(new UpsertUserProfileRequest(user.getId(), user.getEmail()));
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return tokenFor(user);
    }

    @Transactional
    public TokenResponse loginWithOAuth(OAuthProvider provider, String externalUserId, String email) {
        return oAuthAccountRepository.findByProviderAndExternalUserId(provider, externalUserId)
                .map(OAuthAccount::getUser)
                .map(this::tokenFor)
                .orElseGet(() -> createOAuthUser(provider, externalUserId, email));
    }

    private TokenResponse createOAuthUser(OAuthProvider provider, String externalUserId, String email) {
        String normalizedEmail = normalizeEmail(email, provider, externalUserId);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> userRepository.save(User.oauth(normalizedEmail)));
        oAuthAccountRepository.save(OAuthAccount.link(user, provider, externalUserId));
        userServiceClient.upsertUserProfile(new UpsertUserProfileRequest(user.getId(), user.getEmail()));
        return tokenFor(user);
    }

    private TokenResponse tokenFor(User user) {
        return new TokenResponse(jwtService.createToken(user), "Bearer", expiresInSeconds(),
                user.getAccountRole().name());
    }

    private long expiresInSeconds() {
        return Math.max(1L, appProperties.jwt().expirationMs() / 1000);
    }

    private AccountRole normalizeRole(AccountRole role) {
        return role == null ? AccountRole.PARTICIPANT : role;
    }

    private static String normalizeEmail(String email, OAuthProvider provider, String externalUserId) {
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase(Locale.ROOT);
        }
        return provider.name().toLowerCase(Locale.ROOT) + "-" + externalUserId + "@oauth.local";
    }
}
