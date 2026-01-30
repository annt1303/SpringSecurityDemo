package com.security.demo.util;

import com.security.demo.config.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties props;

    public void setRefreshToken(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(props.getRefresh().getName(), token)
                .httpOnly(true)
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path("/")
                .maxAge(Duration.ofDays(props.getRefresh().getMaxAgeDays()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshToken(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(props.getRefresh().getName(), "")
                .httpOnly(true)
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public String getDeviceId(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(c -> props.getDevice().getName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public void setDeviceId(HttpServletResponse response, String deviceId) {
        ResponseCookie cookie = ResponseCookie.from(props.getDevice().getName(), deviceId)
                .httpOnly(false)
                .secure(props.isSecure())
                .sameSite(props.getSameSite())
                .path("/")
                .maxAge(Duration.ofDays(props.getDevice().getMaxAgeDays()))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}