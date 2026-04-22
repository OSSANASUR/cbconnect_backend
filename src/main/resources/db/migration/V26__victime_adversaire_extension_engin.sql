-- V26 — Extension des détails "engin" pour les adversaires (table victime)

ALTER TABLE victime
    ADD COLUMN IF NOT EXISTS immatriculation        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS modele_vehicule        VARCHAR(255),
    ADD COLUMN IF NOT EXISTS genre_vehicule         VARCHAR(30),
    ADD COLUMN IF NOT EXISTS couleur_vehicule       VARCHAR(50),
    ADD COLUMN IF NOT EXISTS numero_chassis         VARCHAR(50),
    ADD COLUMN IF NOT EXISTS capacite_vehicule      INTEGER,
    ADD COLUMN IF NOT EXISTS proprietaire_vehicule  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS est_adversaire BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS a_remorque             BOOLEAN NOT NULL DEFAULT FALSE;

-- Index sur immatriculation pour accélérer les recherches côté back-office
CREATE INDEX IF NOT EXISTS idx_victime_immatriculation ON victime (immatriculation)
    WHERE immatriculation IS NOT NULL AND deleted_data = FALSE;