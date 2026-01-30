package com.security.demo.service;

import com.security.demo.entity.RefreshToken;
import com.security.demo.entity.User;

public interface RefreshTokenService {

    String createForUser(User user, String deviceId);

    RefreshToken verify(String token, String deviceId);

    String rotate(RefreshToken oldToken, String deviceId);

    void revokeByUser(User user);

    void revokeByUserAndDevice(User user, String deviceId);
}

