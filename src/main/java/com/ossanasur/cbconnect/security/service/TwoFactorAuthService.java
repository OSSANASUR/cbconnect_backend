package com.ossanasur.cbconnect.security.service;

import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.security.dto.response.OtpGenerationResult;

import java.util.UUID;

public interface TwoFactorAuthService {
    OtpGenerationResult generateAndSendOtp(Utilisateur user);
    Utilisateur verifyOtp(UUID otpTrackingId, String code);
    OtpGenerationResult resendOtp(UUID otpTrackingId);
    String maskEmail(String email);
}
