-- ============================================================
--  V11 — Bureaux Homologues CEDEAO (13 pays hors Togo)
--
--  Le BUREAU_NATIONAL du Togo (BNCB-TG) est déjà créé en V3.
--  On crée ici les 13 bureaux des pays partenaires qui seront
--  liés comme organismeHomologue sur les sinistres lors de
--  la reprise historique 2025.
--
--  Nomenclature :
--    code        → BCB-{code_carte_brune}   ex: BCB-BJ
--    email       → bcb-{code}@cedeao.local  (synthétique, à corriger)
--    type        → BUREAU_HOMOLOGUE
-- ============================================================

INSERT INTO
    organisme (
        organisme_tracking_id,
        type_organisme,
        raison_sociale,
        code,
        email,
        responsable,
        code_pays,
        code_pays_bcb,
        pays_id,
        active,
        reprise_historique,
        created_at,
        created_by,
        active_data,
        deleted_data
    )
SELECT
    gen_random_uuid (),
    'BUREAU_HOMOLOGUE',
    bh.raison_sociale,
    bh.code,
    bh.email,
    'Directeur Général',
    p.code_iso,
    p.code_carte_brune,
    p.historique_id,
    true,
    false, -- ces bureaux ne sont PAS des imports de reprise, ce sont des référentiels
    NOW(),
    'SYSTEM',
    true,
    false
FROM (
        VALUES (
                'Bureau National Carte Brune CEDEAO – Bénin', 'BCB-BJ', 'bcb-bj@cedeao.local', 'BJ'
            ), (
                'Bureau National Carte Brune CEDEAO – Burkina Faso', 'BCB-BF', 'bcb-bf@cedeao.local', 'BF'
            ), (
                'Bureau National Carte Brune CEDEAO – Cap-Vert', 'BCB-CV', 'bcb-cv@cedeao.local', 'CV'
            ), (
                'Bureau National Carte Brune CEDEAO – Côte d''Ivoire', 'BCB-CI', 'bcb-ci@cedeao.local', 'CI'
            ), (
                'Bureau National Carte Brune CEDEAO – Gambie', 'BCB-GM', 'bcb-gm@cedeao.local', 'GM'
            ), (
                'Bureau National Carte Brune CEDEAO – Ghana', 'BCB-GH', 'bcb-gh@cedeao.local', 'GH'
            ), (
                'Bureau National Carte Brune CEDEAO – Guinée', 'BCB-GN', 'bcb-gn@cedeao.local', 'GN'
            ), (
                'Bureau National Carte Brune CEDEAO – Liberia', 'BCB-LR', 'bcb-lr@cedeao.local', 'LR'
            ), (
                'Bureau National Carte Brune CEDEAO – Mali', 'BCB-ML', 'bcb-ml@cedeao.local', 'ML'
            ), (
                'Bureau National Carte Brune CEDEAO – Niger', 'BCB-NE', 'bcb-ne@cedeao.local', 'NE'
            ), (
                'Bureau National Carte Brune CEDEAO – Nigeria', 'BCB-NG', 'bcb-ng@cedeao.local', 'NG'
            ), (
                'Bureau National Carte Brune CEDEAO – Sénégal', 'BCB-SN', 'bcb-sn@cedeao.local', 'SN'
            ), (
                'Bureau National Carte Brune CEDEAO – Sierra Leone', 'BCB-SL', 'bcb-sl@cedeao.local', 'SL'
            )
    ) AS bh (
        raison_sociale, code, email, code_bcb
    )
    JOIN pays p ON p.code_carte_brune = bh.code_bcb
    AND p.active_data = true
ON CONFLICT DO NOTHING;