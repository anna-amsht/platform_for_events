package com.unik.authservice.repository;

import com.unik.authservice.entity.OAuthAccount;
import com.unik.authservice.entity.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, UUID> {

    Optional<OAuthAccount> findByProviderAndExternalUserId(OAuthProvider provider, String externalUserId);
}
