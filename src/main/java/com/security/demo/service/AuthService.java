package com.security.demo.service;

import com.security.demo.dto.request.SetPasswordRequest;
import com.security.demo.dto.request.LoginRequest;
import com.security.demo.dto.request.RegisterRequest;
import com.security.demo.dto.request.VerifyOtpRequest;
import com.security.demo.dto.response.AuthResponse;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request, String deviceId);
    AuthResponse refreshToken(String refreshToken, String deviceId);
    void logout(String deviceId);
    void resendOtp(String email);
    void setPassword(SetPasswordRequest req);
}

