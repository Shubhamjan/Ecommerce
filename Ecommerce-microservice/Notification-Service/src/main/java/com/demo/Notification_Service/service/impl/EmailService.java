package com.demo.Notification_Service.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:ShopApp}")
    private String appName;

    public void sendHtmlEmail(String toEmail, String subject, String templateName,
                              Map<String, Object> variables) throws MessagingException {
        // build thymeleaf context with variables
        Context context = new Context();
        context.setVariables(variables);
        context.setVariable("appName", appName);

        // process template to HTML string
        String htmlContent = templateEngine.process(templateName, context);

        // build mime message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent,true);// true = HTML

        mailSender.send(message);
        log.info("Email sent to: {} subject: {}", toEmail, subject);
    }


}
