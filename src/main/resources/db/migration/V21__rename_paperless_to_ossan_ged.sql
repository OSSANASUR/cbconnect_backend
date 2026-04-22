-- CBConnect V20 — Rebranding Paperless → OssanGED by OSSANASUR
--
-- Renomme toutes les tables, colonnes, index et valeurs from_table référençant
-- "paperless" pour utiliser le préfixe "ossan_ged".
-- Zéro perte de données : uniquement des RENAME (contraintes FK suivent le rename).

-- ─── 1. Table paperless_dossier ───────────────────────────────────────────────
ALTER TABLE paperless_dossier RENAME TO ossan_ged_dossier;
ALTER TABLE ossan_ged_dossier RENAME COLUMN paperless_dossier_tracking_id TO ossan_ged_dossier_tracking_id;
ALTER TABLE ossan_ged_dossier RENAME COLUMN paperless_storage_path_id    TO ossan_ged_storage_path_id;
ALTER TABLE ossan_ged_dossier RENAME COLUMN paperless_correspondent_id   TO ossan_ged_correspondent_id;
ALTER INDEX uk_paperless_dossier_tracking_actif RENAME TO uk_ossan_ged_dossier_tracking_actif;
UPDATE ossan_ged_dossier SET from_table = 'OSSAN_GED_DOSSIER' WHERE from_table = 'PAPERLESS_DOSSIER';
ALTER TABLE ossan_ged_dossier ALTER COLUMN from_table SET DEFAULT 'OSSAN_GED_DOSSIER';

-- ─── 2. Table paperless_document ──────────────────────────────────────────────
ALTER TABLE paperless_document RENAME TO ossan_ged_document;
ALTER TABLE ossan_ged_document RENAME COLUMN paperless_document_tracking_id TO ossan_ged_document_tracking_id;
ALTER TABLE ossan_ged_document RENAME COLUMN paperless_document_id          TO ossan_ged_document_id;
ALTER TABLE ossan_ged_document RENAME COLUMN paperless_tag_id               TO ossan_ged_tag_id;
ALTER TABLE ossan_ged_document RENAME COLUMN paperless_doc_type_id          TO ossan_ged_doc_type_id;
ALTER INDEX uk_paperless_doc_tracking_actif RENAME TO uk_ossan_ged_doc_tracking_actif;
UPDATE ossan_ged_document SET from_table = 'OSSAN_GED_DOCUMENT' WHERE from_table = 'PAPERLESS_DOCUMENT';
ALTER TABLE ossan_ged_document ALTER COLUMN from_table SET DEFAULT 'OSSAN_GED_DOCUMENT';

-- ─── 3. Colonnes FK externes (id du document côté OssanGED) ───────────────────
ALTER TABLE sinistre                 RENAME COLUMN paperless_dossier_id      TO ossan_ged_dossier_id;
ALTER TABLE victime                  RENAME COLUMN paperless_correspondent_id TO ossan_ged_correspondent_id;
ALTER TABLE pv_sinistre              RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE expertise_medicale       RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE expertise_materielle     RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE facture_reclamation      RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE facture_attestation      RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE courrier                 RENAME COLUMN paperless_document_id     TO ossan_ged_document_id;
ALTER TABLE piece_dossier_reclamation RENAME COLUMN paperless_document_id    TO ossan_ged_document_id;

-- ─── 4. Description du module GED (seed V3) ──────────────────────────────────
UPDATE module_entity
   SET description = 'Gestion Electronique des Documents (OssanGED by OSSANASUR)'
 WHERE nom_module = 'GED';

-- ─── 5. Valeurs enum TypeTable en base (from_table pour traçabilité) ──────────
-- Si d'autres tables pointent vers ces valeurs dans des colonnes from_table spécifiques,
-- leur @DiscriminatorValue Java sera mis à jour via le renommage côté code.
