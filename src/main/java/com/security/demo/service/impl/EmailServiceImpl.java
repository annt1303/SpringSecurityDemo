package com.security.demo.service.impl;

import com.security.demo.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendOtp(String email, String otp) {
        try {
            if (otp == null || otp.length() != 6) {
                throw new IllegalArgumentException("OTP must be 6 digits");
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                            "UTF-8"
                    );

            Context context = new Context();
            context.setVariable("expireMinutes", 10);

            // map từng số OTP vào template
            context.setVariable("code1", otp.charAt(0));
            context.setVariable("code2", otp.charAt(1));
            context.setVariable("code3", otp.charAt(2));
            context.setVariable("code4", otp.charAt(3));
            context.setVariable("code5", otp.charAt(4));
            context.setVariable("code6", otp.charAt(5));

            String html = templateEngine.process("mail/otp-email", context);

            helper.setTo(email);
            helper.setSubject("Verify Your Email");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

}

