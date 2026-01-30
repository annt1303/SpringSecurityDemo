package com.security.demo.dto.request;

public record RegisterRequest(
        String name,
        String email,
        String password
) {}

