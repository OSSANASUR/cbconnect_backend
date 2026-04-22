-- ── Table des banques partenaires ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS banque (
    historique_id      SERIAL PRIMARY KEY,
    banque_tracking_id UUID        NOT NULL,
    nom                VARCHAR(255) NOT NULL,
    code               VARCHAR(20)  NOT NULL,
    code_bic           VARCHAR(15),
    agence             VARCHAR(255),
    ville              VARCHAR(100),
    code_pays          VARCHAR(5),
    telephone          VARCHAR(30),

    -- colonnes InternalHistorique
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    deleted_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    deleted_by         VARCHAR(255),
    libelle            VARCHAR(255),
    active_data        BOOLEAN NOT NULL DEFAULT TRUE,
    parent_code_id     VARCHAR(255),
    deleted_data       BOOLEAN NOT NULL DEFAULT FALSE,
    from_table         VARCHAR(100),
    excel              BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_banque_code_actif
    ON banque (code)
    WHERE active_data = TRUE AND deleted_data = FALSE;

CREATE INDEX IF NOT EXISTS idx_banque_tracking
    ON banque (banque_tracking_id);

-- ── Données de référence — principales banques du Togo ────────────────────────
INSERT INTO banque (banque_tracking_id, nom, code, code_bic, agence, ville, code_pays,
                    created_at, created_by, active_data, deleted_data)
VALUES
  (gen_random_uuid(), 'ECOBANK TOGO',                   'ECOBKTG',  'ECOCBJTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'ORABANK TOGO',                   'ORABKTG',  'ORABSNTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'SOCIÉTÉ GÉNÉRALE TOGO',          'SGTG',     'SOGETGTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'BANQUE ATLANTIQUE TOGO',         'BATG',     'ATLBTGTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'CORIS BANK INTERNATIONAL TOGO',  'CORISTG',  'CORBTGTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'UNITED BANK FOR AFRICA TOGO',    'UBATG',    'UNAFTGTG', NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'BANQUE POPULAIRE POUR L''EPARGNE ET LE CRÉDIT', 'BPECTG', NULL, NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE),
  (gen_random_uuid(), 'DIAMOND BANK TOGO',              'DIAMBKTG', NULL,       NULL, 'Lomé', 'TGO', NOW(), 'SYSTEM', TRUE, FALSE);
