-- ============================================================
--  V2026042901 — Mapping TypeDocumentOssanGed ↔ TypePieceAdministrative
--
--  Ajoute la colonne type_document_ged sur type_piece_administrative.
--  Quand renseignée, l'upload d'un document GED de ce type vers un dossier
--  déclenche l'auto-association : la pièce ATTENDUE passe automatiquement RECUE.
--  NULL = association manuelle uniquement (comportement précédent inchangé).
-- ============================================================

ALTER TABLE type_piece_administrative
    ADD COLUMN IF NOT EXISTS type_document_ged VARCHAR(30);

COMMENT ON COLUMN type_piece_administrative.type_document_ged IS
    'Valeur de TypeDocumentOssanGed. Si renseignée, un upload GED de ce type '
    'vers le dossier auto-associe la pièce (ATTENDUE → RECUE). NULL = manuel.';

-- ── Backfill des pièces du seed V14 (par libellé exact) ─────────
-- Ces UPDATE n'affectent que les lignes dont le libellé n'a pas été modifié.
-- Les pièces renommées par l'admin conservent type_document_ged = NULL
-- et peuvent être configurées manuellement via /parametres/pieces.

UPDATE type_piece_administrative SET type_document_ged = 'CNI'
    WHERE libelle = 'Pièce d''identité / CNI'       AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'PV'
    WHERE libelle = 'PV de Police / Constat amiable' AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'CMI'
    WHERE libelle = 'Certificat médical initial'     AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'EXPERTISE_MED'
    WHERE libelle = 'Rapport d''expertise médicale'  AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'FACTURE_MED'
    WHERE libelle = 'Factures médicales'             AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'ORDONNANCE'
    WHERE libelle = 'Ordonnances médicales'          AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'ACTE_DECES'
    WHERE libelle = 'Certificat de décès'            AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'ACTE_NAISSANCE'
    WHERE libelle = 'Acte de naissance'              AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'ACTE_MARIAGE'
    WHERE libelle = 'Acte de mariage'                AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'EXPERTISE_AUTO'
    WHERE libelle = 'Rapport d''expertise automobile' AND type_document_ged IS NULL;

UPDATE type_piece_administrative SET type_document_ged = 'FACTURE_RECLAMATION'
    WHERE libelle IN ('Factures de réparation', 'Facture de réclamation')
      AND type_document_ged IS NULL;
