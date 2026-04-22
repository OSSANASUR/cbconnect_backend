package com.ossanasur.cbconnect.security.dto.response;

import java.util.Map;
import java.util.UUID;

public record LoginResponse(
        Map<String, Object> tokens,
        UserInfoResponse user,
        boolean requires2FA,
        UUID otpTrackingId,
        String maskedEmail
) {
    public static LoginResponse direct(Map<String, Object> tokens, UserInfoResponse user) {
        return new LoginResponse(tokens, user, false, null, null);
    }

    public static LoginResponse requires2FA(UUID otpTrackingId, String maskedEmail) {
        return new LoginResponse(null, null, true, otpTrackingId, maskedEmail);
    }
}
