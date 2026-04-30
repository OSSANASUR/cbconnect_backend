-- ============================================================
--  V2026042902 — Backfill type_document_ged sur les pièces existantes
--
--  Utilise ILIKE pour être tolérant aux variantes de libellé.
--  Idempotent : n'écrit que si type_document_ged IS NULL.
--  Les pièces déjà configurées via l'UI ne sont pas touchées.
-- ============================================================

-- CNI
UPDATE type_piece_administrative
SET type_document_ged = 'CNI'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%cni%'
    OR libelle ILIKE '%carte%identit%'
    OR libelle ILIKE '%identit%');

-- PV / Constat
UPDATE type_piece_administrative
SET type_document_ged = 'PV'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%proc%s%verbal%'
    OR libelle ILIKE '% pv %'
    OR libelle = 'PV'
    OR libelle ILIKE 'pv%'
    OR libelle ILIKE '%constat%');

-- CMI — Certificat Médical Initial
UPDATE type_piece_administrative
SET type_document_ged = 'CMI'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%cmi%'
    OR libelle ILIKE '%certificat%m%dic%initial%'
    OR libelle ILIKE '%certificat%initial%');

-- CMC — Certificat Médical de Consolidation
UPDATE type_piece_administrative
SET type_document_ged = 'CMC'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%cmc%'
    OR libelle ILIKE '%consolidation%'
    OR libelle ILIKE '%certificat%m%dic%consolid%');

-- Rapport expertise médicale
UPDATE type_piece_administrative
SET type_document_ged = 'EXPERTISE_MED'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%expertise%m%d%'
    OR libelle ILIKE '%rapport%expert%m%d%');

-- Rapport expertise automobile / matérielle
UPDATE type_piece_administrative
SET type_document_ged = 'EXPERTISE_AUTO'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%expertise%auto%'
    OR libelle ILIKE '%expertise%mat%r%'
    OR libelle ILIKE '%rapport%auto%');

-- Factures médicales
UPDATE type_piece_administrative
SET type_document_ged = 'FACTURE_MED'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%facture%m%d%'
    OR libelle ILIKE '%factures%m%d%');

-- Facture de réclamation / réparation
UPDATE type_piece_administrative
SET type_document_ged = 'FACTURE_RECLAMATION'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%facture%r%clamation%'
    OR libelle ILIKE '%factures%r%paration%'
    OR libelle ILIKE '%facture%r%par%');

-- Ordonnance
UPDATE type_piece_administrative
SET type_document_ged = 'ORDONNANCE'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND libelle ILIKE '%ordonnance%';

-- Acte / certificat de décès
UPDATE type_piece_administrative
SET type_document_ged = 'ACTE_DECES'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%acte%d%c%s%'
    OR libelle ILIKE '%certificat%d%c%s%');

-- Acte de naissance
UPDATE type_piece_administrative
SET type_document_ged = 'ACTE_NAISSANCE'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND libelle ILIKE '%naissance%';

-- Acte de mariage
UPDATE type_piece_administrative
SET type_document_ged = 'ACTE_MARIAGE'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND libelle ILIKE '%mariage%';

-- Frais funéraires
UPDATE type_piece_administrative
SET type_document_ged = 'FRAIS_FUNERAIRES'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%fun%raire%'
    OR libelle ILIKE '%obseque%');

-- Certificat de scolarité
UPDATE type_piece_administrative
SET type_document_ged = 'SCOLARITE'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND libelle ILIKE '%sco%larit%';

-- Certificat de vie
UPDATE type_piece_administrative
SET type_document_ged = 'CERTIFICAT_VIE'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND libelle ILIKE '%certificat%vie%';

-- Rapport médical générique (en dernier pour ne pas écraser les précédents)
UPDATE type_piece_administrative
SET type_document_ged = 'RAPPORT_MEDICAL'
WHERE type_document_ged IS NULL AND deleted_data = FALSE
  AND (libelle ILIKE '%rapport%m%dical%'
    OR libelle ILIKE '%rapport%sant%');
