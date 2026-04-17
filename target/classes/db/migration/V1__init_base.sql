-- CBConnect – Migration initiale
-- Table de base pour toutes les entites (chaque entite a sa propre table car @MappedSuperclass)

-- ── Pays ──────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS pays (
    historique_id   SERIAL PRIMARY KEY,
    pays_tracking_id UUID UNIQUE NOT NULL DEFAULT gen_random_uuid(),
    code_iso        VARCHAR(5)   UNIQUE NOT NULL,
    code_carte_brune VARCHAR(5)  UNIQUE NOT NULL,
    libelle         VARCHAR(100) NOT NULL,
    smig_mensuel    DECIMAL(15,2) NOT NULL DEFAULT 0,
    monnaie         VARCHAR(10)  NOT NULL,
    taux_change_xof DECIMAL(10,4) NOT NULL DEFAULT 1,
    age_retraite    INTEGER NOT NULL DEFAULT 60,
    actif           BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      VARCHAR(150),
    updated_by      VARCHAR(150),
    deleted_by      VARCHAR(150),
    libelle_h       VARCHAR(255),
    active_data     BOOLEAN NOT NULL DEFAULT true,
    parent_code_id  VARCHAR(255),
    deleted_data    BOOLEAN NOT NULL DEFAULT false,
    from_table      VARCHAR(50) DEFAULT 'PAYS',
    excel           BOOLEAN NOT NULL DEFAULT false
);
CREATE UNIQUE INDEX uk_pays_tracking_actif ON pays (pays_tracking_id) WHERE active_data = true AND deleted_data = false;
COMMENT ON TABLE pays IS '14 Etats membres CEDEAO. SMIG utilise comme base pour tous les calculs CIMA.';

-- Seed pays CEDEAO initiaux
INSERT INTO pays (pays_tracking_id, code_iso, code_carte_brune, libelle, smig_mensuel, monnaie, taux_change_xof, age_retraite, actif, created_at, created_by, active_data, deleted_data, from_table)
VALUES
  (gen_random_uuid(), 'TGO', 'TG', 'Togo',        35000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'BFA', 'BF', 'Burkina Faso', 40000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'SEN', 'SN', 'Senegal',      80000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'CIV', 'CI', 'Cote d Ivoire',75000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'MLI', 'ML', 'Mali',          40000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'GIN', 'GN', 'Guinee',        450000,'GNF', 135.8, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'NER', 'NE', 'Niger',         36898, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'BEN', 'BJ', 'Benin',         52000, 'XOF', 1, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'GHA', 'GH', 'Ghana',         624,'GHS', 0.6, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'NGA', 'NG', 'Nigeria',       30000,'NGN', 0.65, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'GMB', 'GM', 'Gambie',        3000, 'GMD', 10.5, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'LBR', 'LR', 'Liberia',       300,'LRD', 5.2, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'SLE', 'SL', 'Sierra Leone',  800000,'SLL', 0.04, 60, true, NOW(), 'SYSTEM', true, false, 'PAYS'),
  (gen_random_uuid(), 'CPV', 'CV', 'Cap-Vert',      14000,'CVE', 6.1, 65, true, NOW(), 'SYSTEM', true, false, 'PAYS')
ON CONFLICT DO NOTHING;
