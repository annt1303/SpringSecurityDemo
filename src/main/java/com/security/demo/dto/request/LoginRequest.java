package com.security.demo.dto.request;

public record LoginRequest(
        String email,
        String password
) {}

