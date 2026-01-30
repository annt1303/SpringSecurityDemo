package com.security.demo.security;

import com.security.demo.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        int status = HttpStatus.UNAUTHORIZED.value();
        String message = "Unauthorized";

        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
        }
        else if (ex instanceof DisabledException) {
            status = HttpStatus.FORBIDDEN.value();
            message = "Account is not verified. Please verify OTP.";
        }

        response.setStatus(status);

        ApiResponse<Void> apiResponse = ApiResponse.error(
                status,
                message
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );
    }
}
