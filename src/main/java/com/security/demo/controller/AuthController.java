package com.security.demo.controller;

import com.security.demo.dto.request.SetPasswordRequest;
import com.security.demo.dto.request.*;
import com.security.demo.dto.response.ApiResponse;
import com.security.demo.dto.response.AuthResponse;
import com.security.demo.exception.BadRequestException;
import com.security.demo.exception.UnauthorizedException;
import com.security.demo.service.AuthService;
import com.security.demo.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @RequestBody RegisterRequest req
    ) {
        authService.register(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        "Register successful. Please verify OTP"
                ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verify(
            @RequestBody VerifyOtpRequest req
    ) {
        authService.verifyOtp(req);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Verify OTP successful"
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String deviceId = cookieUtil.getDeviceId(request);
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
            cookieUtil.setDeviceId(response, deviceId);
        }

        AuthResponse authResponse = authService.login(req, deviceId);

        cookieUtil.setRefreshToken(response, authResponse.refreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Login successful",
                        authResponse.accessToken()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is required");
        }

        String deviceId = cookieUtil.getDeviceId(request);

        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
            cookieUtil.setDeviceId(response, deviceId);
        }

        AuthResponse tokens = authService.refreshToken(refreshToken, deviceId);

        cookieUtil.setRefreshToken(response, tokens.refreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Refresh successful",
                        tokens.accessToken()
                )
        );
    }


    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String deviceId = cookieUtil.getDeviceId(request);
        if (deviceId != null) {
            authService.logout(deviceId);
        }

        cookieUtil.clearRefreshToken(response);

        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Logout successful"
                )
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @RequestBody ResendOtpRequest req
    ) {
        authService.resendOtp(req.email());
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "OTP resent successfully"
                )
        );
    }

    @PostMapping("/set-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> setPassword(
            @RequestBody SetPasswordRequest req
    ) {
        authService.setPassword(req);
        return ResponseEntity.ok(
                ApiResponse.success(
                        HttpStatus.OK.value(),
                        "Set password successful"
                )
        );
    }

}




