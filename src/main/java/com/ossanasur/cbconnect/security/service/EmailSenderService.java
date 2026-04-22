package com.ossanasur.cbconnect.security.service;

import java.util.Map;

public interface EmailSenderService {
    void sendTemplated(String toEmail, String subject, String templateName, Map<String, Object> variables);
}
