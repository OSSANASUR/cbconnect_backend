-- V26 — Extension des détails "engin" pour les adversaires (table victime)
-- Champs ajoutés suite aux retours métier : l'adversaire doit être décrit au même
-- niveau que l'assuré (genre, modèle, couleur, châssis, capacité, propriétaire, remorque).

ALTER TABLE victime
    ADD COLUMN IF NOT EXISTS modele_vehicule       VARCHAR(255),
    ADD COLUMN IF NOT EXISTS genre_vehicule        VARCHAR(30),
    ADD COLUMN IF NOT EXISTS couleur_vehicule      VARCHAR(50),
    ADD COLUMN IF NOT EXISTS numero_chassis        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS capacite_vehicule     INTEGER,
    ADD COLUMN IF NOT EXISTS proprietaire_vehicule VARCHAR(255),
    ADD COLUMN IF NOT EXISTS a_remorque            BOOLEAN NOT NULL DEFAULT FALSE;

-- Index léger sur immatriculation pour accélérer les recherches récurrentes côté back-office
CREATE INDEX IF NOT EXISTS idx_victime_immatriculation ON victime (immatriculation)
    WHERE immatriculation IS NOT NULL AND deleted_data = FALSE;
