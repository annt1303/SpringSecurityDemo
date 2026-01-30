package com.security.demo.repository;

import com.security.demo.entity.User;
import com.security.demo.entity.UserAuthProvider;
import com.security.demo.entity.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthProviderRepository
        extends JpaRepository<UserAuthProvider, Long> {

    Optional<UserAuthProvider> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    Optional<UserAuthProvider> findByUserAndProvider(
            User user,
            AuthProvider provider
    );
}

