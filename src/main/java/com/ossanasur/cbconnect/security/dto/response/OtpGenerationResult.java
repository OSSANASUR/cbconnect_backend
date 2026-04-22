package com.ossanasur.cbconnect.security.dto.response;

import java.util.UUID;

public record OtpGenerationResult(UUID otpTrackingId, String maskedEmail) {}
