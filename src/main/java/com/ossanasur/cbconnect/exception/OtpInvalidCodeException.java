package com.ossanasur.cbconnect.exception;

import lombok.Getter;

@Getter
public class OtpInvalidCodeException extends RuntimeException {
    private final int attemptsRemaining;
    public OtpInvalidCodeException(int attemptsRemaining) {
        super("Code incorrect. Tentatives restantes : " + attemptsRemaining);
        this.attemptsRemaining = attemptsRemaining;
    }
}
