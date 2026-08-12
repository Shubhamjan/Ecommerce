package com.demo.service.impl;

import com.demo.dto.ForgetPasswordReset;
import com.demo.dto.ResetPasswordRequest;
import com.demo.dto.VerifyOtpRequest;
import com.demo.entity.PasswordResetOtp;
import com.demo.entity.User;
import com.demo.repository.PasswordResetOtpRepository;
import com.demo.repository.UserRepository;
import com.demo.service.ForgetPasswordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ForgetPasswordServiceImpl implements ForgetPasswordService {


    private final PasswordResetOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendOtp(ForgetPasswordReset request) {

        // Check user exists
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(request.getEmail());
        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));


        // Save OTP
        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(request.getEmail())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10)) // valid 10 mins
                .used(false)
                .build();
        otpRepository.save(resetOtp);

        // Send email
        emailService.sendOtpMail(request.getEmail(), otp);
        log.info("OTP sent to email: {}", request.getEmail());

    }

    @Override
    public void verifyOtp(VerifyOtpRequest request) {

        PasswordResetOtp resetOtp = otpRepository
                .findByEmailAndOtpAndUsedFalse(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetOtp resetOtp = otpRepository
                .findByEmailAndOtpAndUsedFalse(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        // Update password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        resetOtp.setUsed(true);
        otpRepository.save(resetOtp);

        log.info("Password reset successful for: {}", request.getEmail());
    }
}
