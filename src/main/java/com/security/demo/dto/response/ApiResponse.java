package com.security.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int status;          // HTTP status code
    private String message;
    private T data;
    private LocalDateTime timestamp;

    /* ================= SUCCESS ================= */

    public static <T> ApiResponse<T> success(
            int status,
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ApiResponse<Void> success(
            int status,
            String message
    ) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /* ================= ERROR ================= */

    public static ApiResponse<Void> error(
            int status,
            String message
    ) {
        return ApiResponse.<Void>builder()
                .status(status)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
