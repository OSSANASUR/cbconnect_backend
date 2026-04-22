package com.ossanasur.cbconnect.exception;

import lombok.Getter;

@Getter
public class OtpResendCooldownException extends RuntimeException {
    private final long secondsRemaining;
    public OtpResendCooldownException(long secondsRemaining) {
        super("Veuillez attendre " + secondsRemaining + " seconde(s) avant de demander un nouveau code.");
        this.secondsRemaining = secondsRemaining;
    }
}
