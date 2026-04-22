-- ── Coordonnées physiques et numériques des organismes ───────────────────────
-- Permet de stocker l'adresse complète d'un Bureau / Compagnie / Homologue
-- pour affichage dans les PDF (factures, courriers) et fiches organisme.
ALTER TABLE organisme
    ADD COLUMN IF NOT EXISTS adresse              VARCHAR(255),
    ADD COLUMN IF NOT EXISTS boite_postale        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS ville                VARCHAR(100),
    ADD COLUMN IF NOT EXISTS telephone_principal  VARCHAR(30),
    ADD COLUMN IF NOT EXISTS fax                  VARCHAR(30),
    ADD COLUMN IF NOT EXISTS site_web             VARCHAR(255);
