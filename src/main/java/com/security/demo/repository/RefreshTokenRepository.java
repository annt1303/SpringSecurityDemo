package com.security.demo.repository;

import com.security.demo.entity.RefreshToken;
import com.security.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByUserAndDeviceIdAndRevokedFalse(User user, String deviceId);
}

