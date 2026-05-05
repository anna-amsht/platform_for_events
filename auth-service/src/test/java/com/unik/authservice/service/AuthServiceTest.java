package com.unik.authservice.service;

import com.unik.authservice.client.UserServiceClient;
import com.unik.authservice.config.AppProperties;
import com.unik.authservice.dto.internal.UpsertUserProfileRequest;
import com.unik.authservice.dto.request.LoginRequest;
import com.unik.authservice.dto.request.RegisterRequest;
import com.unik.authservice.entity.AccountRole;
import com.unik.authservice.entity.OAuthAccount;
import com.unik.authservice.entity.OAuthProvider;
import com.unik.authservice.entity.User;
import com.unik.authservice.exception.EmailAlreadyExistsException;
import com.unik.authservice.repository.OAuthAccountRepository;
import com.unik.authservice.repository.UserRepository;
import com.unik.authservice.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OAuthAccountRepository oAuthAccountRepository = mock(OAuthAccountRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final AuthService service = new AuthService(
            userRepository,
            oAuthAccountRepository,
            passwordEncoder,
            jwtService,
            new AppProperties(
                    new AppProperties.Jwt("01234567890123456789012345678901", 1500),
                    new AppProperties.Frontend("http://localhost:5173"),
                    new AppProperties.Oauth2("http://localhost:8080"),
                    new AppProperties.Github("client", "secret"),
                    new AppProperties.Internal("internal")
            ),
            userServiceClient
    );

    @Test
    void registerCreatesParticipantByDefaultAndProfilesUser() {
        when(userRepository.existsByEmailIgnoreCase("User@Example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createToken(any(User.class))).thenReturn("jwt-token");

        var response = service.register(new RegisterRequest("User@Example.com", "password123", null));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.expiresInSeconds()).isEqualTo(1);
        assertThat(response.role()).isEqualTo("PARTICIPANT");

        ArgumentCaptor<UpsertUserProfileRequest> profileCaptor = ArgumentCaptor.forClass(UpsertUserProfileRequest.class);
        verify(userServiceClient).upsertUserProfile(profileCaptor.capture());
        assertThat(profileCaptor.getValue().email()).isEqualTo("user@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("user@example.com", "password123", AccountRole.PARTICIPANT)))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(userServiceClient, never()).upsertUserProfile(any());
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = User.local("user@example.com", "encoded", AccountRole.PARTICIPANT);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("user@example.com", "bad")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginReturnsTokenForValidPassword() {
        User user = User.local("user@example.com", "encoded", AccountRole.ORGANIZER);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded")).thenReturn(true);
        when(jwtService.createToken(user)).thenReturn("jwt-token");

        var response = service.login(new LoginRequest("user@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.role()).isEqualTo("ORGANIZER");
    }

    @Test
    void loginRejectsUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "password123")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void loginWithOAuthReturnsTokenForLinkedAccount() {
        User user = User.oauth("oauth@example.com");
        var account = OAuthAccount.link(user, OAuthProvider.GITHUB, "12345");
        when(oAuthAccountRepository.findByProviderAndExternalUserId(OAuthProvider.GITHUB, "12345"))
                .thenReturn(Optional.of(account));
        when(jwtService.createToken(user)).thenReturn("oauth-token");

        var response = service.loginWithOAuth(OAuthProvider.GITHUB, "12345", "ignored@example.com");

        assertThat(response.accessToken()).isEqualTo("oauth-token");
        assertThat(response.role()).isEqualTo("PARTICIPANT");
        verify(userRepository, never()).save(any());
        verify(userServiceClient, never()).upsertUserProfile(any());
    }

    @Test
    void loginWithOAuthCreatesUserWhenAccountIsNew() {
        when(oAuthAccountRepository.findByProviderAndExternalUserId(OAuthProvider.GITHUB, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("oauth@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createToken(any(User.class))).thenReturn("oauth-token");

        var response = service.loginWithOAuth(OAuthProvider.GITHUB, "12345", " OAuth@Example.com ");

        assertThat(response.accessToken()).isEqualTo("oauth-token");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("oauth@example.com");
        verify(oAuthAccountRepository).save(any(OAuthAccount.class));
        verify(userServiceClient).upsertUserProfile(any(UpsertUserProfileRequest.class));
    }

    @Test
    void loginWithOAuthUsesSyntheticEmailWhenProviderDoesNotReturnEmail() {
        when(oAuthAccountRepository.findByProviderAndExternalUserId(OAuthProvider.GITHUB, "12345"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmailIgnoreCase("github-12345@oauth.local")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.createToken(any(User.class))).thenReturn("oauth-token");

        service.loginWithOAuth(OAuthProvider.GITHUB, "12345", " ");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("github-12345@oauth.local");
    }
}
