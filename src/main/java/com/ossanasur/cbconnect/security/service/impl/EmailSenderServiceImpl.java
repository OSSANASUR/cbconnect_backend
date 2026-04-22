package com.ossanasur.cbconnect.security.service.impl;

import com.ossanasur.cbconnect.security.service.EmailSenderService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:poolapps@ossanasur.com}")
    private String fromAddress;

    @Value("${app.mail.from-name:CBConnect}")
    private String fromName;

    @Override
    public void sendTemplated(String toEmail, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            variables.forEach(ctx::setVariable);
            String html = templateEngine.process(templateName, ctx);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);
            try {
                helper.setFrom(fromAddress, fromName);
            } catch (java.io.UnsupportedEncodingException e) {
                helper.setFrom(fromAddress);
            }
            mailSender.send(msg);
            log.info("Email '{}' envoyé à {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Échec envoi email '{}' à {} : {}", subject, toEmail, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email : " + e.getMessage(), e);
        }
    }
}
