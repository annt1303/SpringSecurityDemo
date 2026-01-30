package com.security.demo.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}

