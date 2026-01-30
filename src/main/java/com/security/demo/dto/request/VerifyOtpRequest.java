package com.security.demo.dto.request;

public record VerifyOtpRequest(
        String email,
        String otp
) {}

