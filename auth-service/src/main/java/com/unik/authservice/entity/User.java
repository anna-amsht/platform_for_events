package com.unik.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(schema = "auth", name = "users")
public class User {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_role", nullable = false, length = 32)
    private AccountRole accountRole;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public static User local(String email, String passwordHash, AccountRole accountRole) {
        User user = new User();
        user.email = email.trim().toLowerCase(Locale.ROOT);
        user.passwordHash = passwordHash;
        user.accountRole = accountRole;
        return user;
    }

    public static User oauth(String email) {
        User user = new User();
        user.email = email.trim().toLowerCase(Locale.ROOT);
        user.accountRole = AccountRole.PARTICIPANT;
        return user;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (accountRole == null) {
            accountRole = AccountRole.PARTICIPANT;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }
}
