package com.unik.authservice.controller;

import com.unik.authservice.dto.request.LoginRequest;
import com.unik.authservice.dto.request.RegisterRequest;
import com.unik.authservice.dto.response.TokenResponse;
import com.unik.authservice.dto.response.UserMeResponse;
import com.unik.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserMeResponse me(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(Object::toString)
                .filter(authority -> authority.equals("ROLE_ORGANIZER") || authority.equals("ROLE_PARTICIPANT"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("PARTICIPANT");
        return new UserMeResponse((UUID) authentication.getDetails(), authentication.getName(), role);
    }
}
