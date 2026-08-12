package com.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpMail(String toEmail,String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp +
                "\n\nThis OTP is valid for 10 minutes." +
                "\nDo not share this with anyone.");
        mailSender.send(message);
    }
}
