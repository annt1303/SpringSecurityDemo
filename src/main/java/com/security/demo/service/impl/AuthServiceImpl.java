package com.security.demo.service.impl;

import com.security.demo.dto.request.SetPasswordRequest;
import com.security.demo.dto.request.LoginRequest;
import com.security.demo.dto.request.RegisterRequest;
import com.security.demo.dto.request.VerifyOtpRequest;
import com.security.demo.dto.response.AuthResponse;
import com.security.demo.entity.OtpVerification;
import com.security.demo.entity.RefreshToken;
import com.security.demo.entity.Role;
import com.security.demo.entity.User;
import com.security.demo.entity.enums.RoleName;
import com.security.demo.exception.BadRequestException;
import com.security.demo.exception.UnauthorizedException;
import com.security.demo.repository.OtpVerificationRepository;
import com.security.demo.repository.RoleRepository;
import com.security.demo.repository.UserRepository;
import com.security.demo.service.AuthService;
import com.security.demo.service.EmailService;
import com.security.demo.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final OtpVerificationRepository otpRepo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${app.otp.resend-cooldown-seconds}")
    private int resendCooldown;

    @Override
    public void register(RegisterRequest req) {

        if (userRepo.existsByEmail(req.email())) {
            throw new BadRequestException("Email already exists");
        }

        Role userRole = roleRepo.findByName(RoleName.ROLE_USER)
                .orElseThrow();

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(encoder.encode(req.password()))
                .enabled(false)
                .roles(Set.of(userRole))
                .build();

        userRepo.save(user);

        generateAndSendOtp(req.email());
    }


    @Override
    public void verifyOtp(VerifyOtpRequest req) {
        OtpVerification otp = otpRepo
                .findByEmailAndOtpCode(req.email(), req.otp())
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (otp.isVerified()) {
            throw new BadRequestException("OTP already used");
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }


        User user = userRepo.findByEmail(req.email()).orElseThrow();
        user.setEnabled(true);

        otp.setVerified(true);
    }

    @Override
    public AuthResponse login(LoginRequest req, String deviceId) {

        User user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        if (req.password() == null) {
            throw new BadRequestException(
                    "Please update your password to log in."
            );
        }

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email(), req.password()
                )
        );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createForUser(user, deviceId);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken, String deviceId) {

        RefreshToken oldToken =
                refreshTokenService.verify(refreshToken, deviceId);

        // create new tokens
        String newAccessToken = jwtService.generateAccessToken(oldToken.getUser());
        String newRefreshToken = refreshTokenService.rotate(oldToken, deviceId);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String deviceId) {
        User user = getCurrentUser();
        refreshTokenService.revokeByUserAndDevice(user, deviceId);
    }


    @Override
    public void resendOtp(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.isEnabled()) {
            throw new BadRequestException("Account already verified");
        }

        // CHECK COOLDOWN
        otpRepo.findTopByEmailAndVerifiedFalseOrderBySentAtDesc(email)
                .ifPresent(lastOtp -> {
                    if (lastOtp.getSentAt()
                            .plusSeconds(resendCooldown)
                            .isAfter(LocalDateTime.now())) {
                        throw new BadRequestException(
                                "Please wait 3 minutes before resending OTP"
                        );
                    }
                });

        // revoke Old OTPs
        otpRepo.findByEmailAndVerifiedFalse(email)
                .forEach(otp -> otp.setVerified(true));

        generateAndSendOtp(email);
    }

    @Override
    public void setPassword(SetPasswordRequest req){
        User user = getCurrentUser();

        if (user.getPassword() != null) {
            throw new BadRequestException("Password already set");
        }

        user.setPassword(encoder.encode(req.password()));

    };

    private void generateAndSendOtp(String email) {

        String otp = String.valueOf(
                new SecureRandom().nextInt(900000) + 100000
        );

        otpRepo.save(OtpVerification.builder()
                .email(email)
                .otpCode(otp)
                .sentAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verified(false)
                .build()
        );

        emailService.sendOtp(email, otp);
    }

    private User getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }


}

