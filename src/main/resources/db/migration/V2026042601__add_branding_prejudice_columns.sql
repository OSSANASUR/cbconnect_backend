-- ────────────────────────────────────────────────────────────
-- Branding par organisme : entête, pied, signataires
-- ────────────────────────────────────────────────────────────
ALTER TABLE organisme
    ADD COLUMN header_image_url varchar(500),
    ADD COLUMN footer_image_url varchar(500),
    ADD COLUMN titre_responsable varchar(150),
    ADD COLUMN afficher_deux_signatures boolean NOT NULL DEFAULT false,
    ADD COLUMN responsable2 varchar(200),
    ADD COLUMN titre_responsable2 varchar(150);

-- ────────────────────────────────────────────────────────────
-- N° de sinistre côté assureur étranger
-- ────────────────────────────────────────────────────────────
ALTER TABLE sinistre
    ADD COLUMN numero_sinistre_assureur varchar(100);

-- ────────────────────────────────────────────────────────────
-- Type de préjudice + motif complémentaire sur paiement
-- ────────────────────────────────────────────────────────────
ALTER TABLE paiement
    ADD COLUMN type_prejudice varchar(20),
    ADD COLUMN motif_complement varchar(255);

-- Backfill : tous les paiements existants en MATERIEL par défaut
UPDATE paiement SET type_prejudice = 'MATERIEL' WHERE type_prejudice IS NULL;

ALTER TABLE paiement ALTER COLUMN type_prejudice SET NOT NULL;
