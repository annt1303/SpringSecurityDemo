package com.security.demo.security;

import com.security.demo.entity.User;
import com.security.demo.repository.UserRepository;
import com.security.demo.service.RefreshTokenService;
import com.security.demo.service.impl.JwtService;
import com.security.demo.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2SuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User =
                (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new RuntimeException("Email not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String deviceId = cookieUtil.getDeviceId(request);

        if (deviceId == null || deviceId.isBlank()) {
            deviceId = UUID.randomUUID().toString();
            cookieUtil.setDeviceId(response, deviceId);
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken =
                refreshTokenService.createForUser(user, deviceId);

        // Save refresh token in cookie
        cookieUtil.setRefreshToken(response, refreshToken);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write("""
            {
              "accessToken": "%s"
            }
        """.formatted(accessToken));
    }
}