package com.ossanasur.cbconnect.security.service.impl;

import com.ossanasur.cbconnect.common.enums.TypeTable;
import com.ossanasur.cbconnect.exception.OtpExpiredException;
import com.ossanasur.cbconnect.exception.OtpInvalidCodeException;
import com.ossanasur.cbconnect.exception.OtpMaxAttemptsException;
import com.ossanasur.cbconnect.exception.OtpResendCooldownException;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.security.dto.response.OtpGenerationResult;
import com.ossanasur.cbconnect.security.entity.OTP;
import com.ossanasur.cbconnect.security.repository.OtpRepository;
import com.ossanasur.cbconnect.security.service.EmailSenderService;
import com.ossanasur.cbconnect.security.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private static final int CODE_LENGTH = 6;
    private static final int VALIDITY_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final String PURPOSE_LOGIN = "LOGIN";

    private final OtpRepository otpRepository;
    private final EmailSenderService emailSenderService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.name:CBConnect}")
    private String appName;

    @Override
    @Transactional
    public OtpGenerationResult generateAndSendOtp(Utilisateur user) {
        String code = generateCode();
        UUID trackingId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OTP otp = OTP.builder()
                .otpTrackingId(trackingId)
                .code(code)
                .purpose(PURPOSE_LOGIN)
                .used(false)
                .expiresAt(now.plusMinutes(VALIDITY_MINUTES))
                .attempts(0)
                .lastSentAt(now)
                .utilisateur(user)
                .createdBy(user.getUsername() != null ? user.getUsername() : user.getEmail())
                .activeData(true)
                .deletedData(false)
                .fromTable(TypeTable.UTILISATEUR)
                .build();

        otpRepository.save(otp);
        sendOtpEmail(user, code);

        log.info("OTP 2FA généré pour user={} trackingId={}", user.getEmail(), trackingId);
        return new OtpGenerationResult(trackingId, maskEmail(user.getEmail()));
    }

    @Override
    @Transactional
    public Utilisateur verifyOtp(UUID otpTrackingId, String code) {
        OTP otp = otpRepository.findActiveByTrackingId(otpTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("OTP introuvable ou déjà utilisé"));

        if (otp.getExpiresAt() != null && otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expiré trackingId={}", otpTrackingId);
            throw new OtpExpiredException("Code expiré. Veuillez en demander un nouveau.");
        }

        if (otp.getAttempts() >= MAX_ATTEMPTS) {
            otp.setUsed(true);
            otpRepository.save(otp);
            log.warn("OTP bloqué (max tentatives) trackingId={}", otpTrackingId);
            throw new OtpMaxAttemptsException();
        }

        boolean match = constantTimeEquals(otp.getCode(), code);
        if (!match) {
            otp.setAttempts(otp.getAttempts() + 1);
            otpRepository.save(otp);
            int remaining = MAX_ATTEMPTS - otp.getAttempts();
            if (remaining <= 0) {
                otp.setUsed(true);
                otpRepository.save(otp);
                throw new OtpMaxAttemptsException();
            }
            log.info("OTP incorrect trackingId={} remaining={}", otpTrackingId, remaining);
            throw new OtpInvalidCodeException(remaining);
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        log.info("OTP validé trackingId={} user={}", otpTrackingId, otp.getUtilisateur().getEmail());
        return otp.getUtilisateur();
    }

    @Override
    @Transactional
    public OtpGenerationResult resendOtp(UUID otpTrackingId) {
        OTP otp = otpRepository.findActiveByTrackingId(otpTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Session OTP introuvable"));

        if (otp.getLastSentAt() != null) {
            long seconds = Duration.between(otp.getLastSentAt(), LocalDateTime.now()).getSeconds();
            if (seconds < RESEND_COOLDOWN_SECONDS) {
                throw new OtpResendCooldownException(RESEND_COOLDOWN_SECONDS - seconds);
            }
        }

        Utilisateur user = otp.getUtilisateur();
        otp.setUsed(true);
        otpRepository.save(otp);

        return generateAndSendOtp(user);
    }

    @Override
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "****";
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String head = local.substring(0, 1);
        return head + "***" + domain;
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) sb.append(secureRandom.nextInt(10));
        return sb.toString();
    }

    private boolean constantTimeEquals(String expected, String provided) {
        if (expected == null || provided == null) return false;
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private void sendOtpEmail(Utilisateur user, String code) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("nom", (user.getPrenoms() != null ? user.getPrenoms() + " " : "") +
                (user.getNom() != null ? user.getNom() : ""));
        vars.put("otpCode", code);
        vars.put("expirationMinutes", VALIDITY_MINUTES);
        vars.put("appName", appName);
        vars.put("year", Year.now().getValue());

        String subject = "Votre code de connexion " + appName + " : " + code;
        emailSenderService.sendTemplated(user.getEmail(), subject, "otp_mail", vars);
    }
}
