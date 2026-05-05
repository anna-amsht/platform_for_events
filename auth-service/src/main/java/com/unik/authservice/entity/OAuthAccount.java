package com.unik.authservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "auth", name = "oauth_accounts")
public class OAuthAccount {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OAuthProvider provider;

    @Column(name = "external_user_id", nullable = false, length = 64)
    private String externalUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OAuthAccount() {
    }

    public static OAuthAccount link(User user, OAuthProvider provider, String externalUserId) {
        OAuthAccount account = new OAuthAccount();
        account.user = user;
        account.provider = provider;
        account.externalUserId = externalUserId;
        return account;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = Instant.now();
    }

    public User getUser() {
        return user;
    }
}
