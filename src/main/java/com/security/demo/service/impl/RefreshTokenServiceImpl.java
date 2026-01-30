package com.security.demo.service.impl;

import com.security.demo.entity.RefreshToken;
import com.security.demo.entity.User;
import com.security.demo.exception.UnauthorizedException;
import com.security.demo.repository.RefreshTokenRepository;
import com.security.demo.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshRepo;

    @Override
    @Transactional
    public String createForUser(User user, String deviceId) {

        revokeByUserAndDevice(user, deviceId);

        String token = UUID.randomUUID().toString();

        refreshRepo.save(
                RefreshToken.builder()
                        .user(user)
                        .deviceId(deviceId)
                        .token(token)
                        .expiryDate(LocalDateTime.now().plusDays(7))
                        .revoked(false)
                        .build()
        );

        return token;
    }

    @Override
    public void revokeByUserAndDevice(User user, String deviceId) {
        refreshRepo.findByUserAndDeviceIdAndRevokedFalse(user, deviceId)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    token.setExpiryDate(LocalDateTime.now());
                    refreshRepo.save(token);
                });
    }

    @Override
    public RefreshToken verify(String token, String deviceId) {

        RefreshToken rt = refreshRepo
                .findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (rt.isRevoked()
                || rt.getExpiryDate().isBefore(LocalDateTime.now())
                || !rt.getDeviceId().equals(deviceId)) {
            throw new UnauthorizedException("Refresh token invalid");
        }

        return rt;
    }


    @Override
    @Transactional
    public String rotate(RefreshToken oldToken, String deviceId) {
        oldToken.setRevoked(true);
        refreshRepo.save(oldToken);

        return createForUser(oldToken.getUser(), deviceId);
    }

    @Override
    public void revokeByUser(User user) {
        refreshRepo.findByUser(user)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    rt.setExpiryDate(LocalDateTime.now());
                });
    }
}


