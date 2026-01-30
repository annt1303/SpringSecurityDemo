package com.security.demo.repository;

import com.security.demo.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OtpVerificationRepository
        extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpCode(
            String email, String otpCode
    );

    Optional<OtpVerification>
    findTopByEmailAndVerifiedFalseOrderBySentAtDesc(String email);

    List<OtpVerification> findByEmailAndVerifiedFalse(String email);


}

