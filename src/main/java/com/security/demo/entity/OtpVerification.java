package com.security.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification extends BaseEntity {

    private String email;

    private String otpCode;

    private LocalDateTime expiryTime;

    private boolean verified;

    private LocalDateTime sentAt;
}


