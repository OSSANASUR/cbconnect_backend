-- V10__2fa_email_otp.sql
-- Adds organisme.two_factor_enabled + OTP tracking columns (attempts, last_sent_at)

ALTER TABLE organisme
    ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE otp
    ADD COLUMN IF NOT EXISTS attempts INTEGER NOT NULL DEFAULT 0;

ALTER TABLE otp
    ADD COLUMN IF NOT EXISTS last_sent_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_otp_tracking_id_active
    ON otp (otp_tracking_id)
    WHERE used = false;

CREATE INDEX IF NOT EXISTS idx_otp_utilisateur_purpose_active
    ON otp (utilisateur_id, purpose)
    WHERE used = false;
