package com.ossanasur.cbconnect.exception;

public class OtpMaxAttemptsException extends RuntimeException {
    public OtpMaxAttemptsException() {
        super("Nombre maximum de tentatives atteint. Veuillez vous reconnecter.");
    }
}
