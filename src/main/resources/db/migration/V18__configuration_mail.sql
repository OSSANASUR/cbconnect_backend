-- ============================================================
--  V18 — Configuration mail par utilisateur
--
--  Stocke la config SMTP/IMAP propre à chaque utilisateur afin
--  d'envoyer et recevoir des mails depuis sa propre boîte.
--  Le mot de passe SMTP est chiffré AES-256 (colonne smtp_password_enc).
-- ============================================================

CREATE TABLE IF NOT EXISTS configuration_mail (
    historique_id        SERIAL PRIMARY KEY,
    tracking_id          UUID UNIQUE DEFAULT gen_random_uuid(),

    utilisateur_id       INTEGER     NOT NULL UNIQUE REFERENCES utilisateur(historique_id),

    -- ── Identité expéditeur ──────────────────────────────
    email_expediteur     VARCHAR(200) NOT NULL,
    nom_affiche          VARCHAR(200),

    -- ── SMTP ─────────────────────────────────────────────
    smtp_host            VARCHAR(200),
    smtp_port            INTEGER,
    smtp_securite        VARCHAR(20),   -- NONE | SSL | STARTTLS
    smtp_username        VARCHAR(200),
    smtp_password_enc    TEXT,
    smtp_auth            BOOLEAN      NOT NULL DEFAULT TRUE,

    -- ── IMAP ─────────────────────────────────────────────
    imap_host            VARCHAR(200),
    imap_port            INTEGER,
    imap_securite        VARCHAR(20),   -- NONE | SSL | STARTTLS

    -- ── État ─────────────────────────────────────────────
    est_configuree       BOOLEAN      NOT NULL DEFAULT FALSE,
    derniere_synchro     TIMESTAMP,
    nb_messages_non_lus  INTEGER      NOT NULL DEFAULT 0,

    -- ── Signature ────────────────────────────────────────
    signature            TEXT,

    -- ── Audit InternalHistorique ─────────────────────────
    created_at           TIMESTAMP,
    updated_at           TIMESTAMP,
    deleted_at           TIMESTAMP,
    created_by           VARCHAR(150),
    updated_by           VARCHAR(150),
    deleted_by           VARCHAR(150),
    libelle              VARCHAR(255),
    active_data          BOOLEAN      NOT NULL DEFAULT TRUE,
    parent_code_id       VARCHAR(255),
    deleted_data         BOOLEAN      NOT NULL DEFAULT FALSE,
    from_table           VARCHAR(50)  DEFAULT 'CONFIGURATION_MAIL_UTILISATEUR',
    excel                BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX uk_configuration_mail_tracking_actif
    ON configuration_mail (tracking_id)
    WHERE active_data = TRUE AND deleted_data = FALSE;

CREATE UNIQUE INDEX uk_configuration_mail_utilisateur_actif
    ON configuration_mail (utilisateur_id)
    WHERE active_data = TRUE AND deleted_data = FALSE;
