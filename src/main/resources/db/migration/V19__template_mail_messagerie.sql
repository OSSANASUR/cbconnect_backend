-- ============================================================
--  V19 — Templates mail + champs messagerie sur Courrier
-- ============================================================

-- ── 1. Table des templates mail ───────────────────────────────────
CREATE TABLE IF NOT EXISTS template_mail (
    historique_id   SERIAL PRIMARY KEY,
    tracking_id     UUID         NOT NULL UNIQUE DEFAULT gen_random_uuid(),

    -- Identité
    type_template   VARCHAR(50)  NOT NULL,   -- TypeTemplateMailEnum
    nom             VARCHAR(200) NOT NULL,
    description     TEXT,

    -- Contenu
    sujet           VARCHAR(500) NOT NULL,   -- peut contenir {{numero_sinistre}}, {{organisme}}, etc.
    corps_html      TEXT         NOT NULL,   -- HTML avec variables {{...}}

    -- Langue / actif
    actif           BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit
    created_at      TIMESTAMPTZ  DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100),
    deleted_by      VARCHAR(100),
    active_data     BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_data    BOOLEAN      NOT NULL DEFAULT FALSE,
    parent_code_id  VARCHAR(100),
    from_table      VARCHAR(100),
    libelle         VARCHAR(255),
    excel           BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ── 2. Enrichissement table courrier pour lien messagerie ────────
ALTER TABLE courrier
    ADD COLUMN IF NOT EXISTS message_id_mail  VARCHAR(500),  -- Message-ID SMTP (pour threading)
    ADD COLUMN IF NOT EXISTS corps_html       TEXT,          -- Corps HTML du mail
    ADD COLUMN IF NOT EXISTS envoye_par_mail  BOOLEAN        NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS statut_envoi     VARCHAR(20),   -- EN_ATTENTE | ENVOYE | ECHEC
    ADD COLUMN IF NOT EXISTS date_envoi       TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS template_id      INT REFERENCES template_mail(historique_id);

-- ── 3. Seed — templates de base BNCB ─────────────────────────────
INSERT INTO template_mail (tracking_id, type_template, nom, description, sujet, corps_html, actif, created_by, active_data, deleted_data)
VALUES

-- RELANCE
(gen_random_uuid(), 'RELANCE', 'Relance homologue', 'Relance standard envoyée à un bureau homologue',
 'Relance — Sinistre n° {{numero_sinistre}} — {{organisme_homologue}}',
 '<p>Bonjour,</p>
<p>Nous vous contactons concernant le sinistre n° <strong>{{numero_sinistre}}</strong> survenu le <strong>{{date_accident}}</strong>, impliquant M./Mme <strong>{{assure_nom}}</strong>.</p>
<p>Sauf erreur de notre part, ce dossier est toujours en attente de règlement. Nous vous saurions gré de bien vouloir nous faire parvenir votre position dans les meilleurs délais.</p>
<p>Dans l''attente de votre retour, nous restons à votre disposition pour toute information complémentaire.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- DEMANDE DE PIECES
(gen_random_uuid(), 'DEMANDE_PIECES', 'Demande de pièces', 'Demande de pièces administratives manquantes',
 'Demande de pièces — Sinistre n° {{numero_sinistre}}',
 '<p>Bonjour,</p>
<p>Dans le cadre du traitement du sinistre n° <strong>{{numero_sinistre}}</strong>, nous avons besoin des pièces complémentaires suivantes :</p>
<ul>{{liste_pieces}}</ul>
<p>Merci de nous faire parvenir ces documents dans un délai de <strong>15 jours</strong>.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- DEMANDE D''OFFRE
(gen_random_uuid(), 'DEMANDE_OFFRE', 'Demande d''offre d''indemnisation', 'Demande d''offre à un organisme homologue',
 'Demande d''offre d''indemnisation — Sinistre n° {{numero_sinistre}}',
 '<p>Bonjour,</p>
<p>Nous vous transmettons le dossier relatif au sinistre n° <strong>{{numero_sinistre}}</strong> survenu le <strong>{{date_accident}}</strong> à <strong>{{lieu_accident}}</strong>.</p>
<p>Nous vous saurions gré de bien vouloir nous faire parvenir votre offre d''indemnisation pour les victimes concernées.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- TRANSMISSION EXPERTISE
(gen_random_uuid(), 'TRANSMISSION_EXPERTISE', 'Transmission d''expertise', 'Envoi du rapport d''expertise',
 'Transmission expertise — Sinistre n° {{numero_sinistre}}',
 '<p>Bonjour,</p>
<p>Veuillez trouver ci-joint le rapport d''expertise relatif au sinistre n° <strong>{{numero_sinistre}}</strong>.</p>
<p>Merci de nous accuser réception.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- CONFIRMATION GARANTIE
(gen_random_uuid(), 'CONFIRMATION_GARANTIE', 'Confirmation de garantie', 'Confirmation de prise en charge',
 'Confirmation de garantie — Sinistre n° {{numero_sinistre}}',
 '<p>Bonjour,</p>
<p>Nous accusons réception de votre dossier concernant le sinistre n° <strong>{{numero_sinistre}}</strong> et confirmons la prise en charge de ce dossier par notre bureau.</p>
<p>Nous reviendrons vers vous dès que possible pour la suite du traitement.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- ACCORD VICTIME
(gen_random_uuid(), 'ACCORD_VICTIME', 'Accord victime', 'Notification d''accord avec la victime',
 'Accord victime — Sinistre n° {{numero_sinistre}}',
 '<p>Bonjour,</p>
<p>Nous avons le plaisir de vous informer qu''un accord a été trouvé avec la victime concernant le sinistre n° <strong>{{numero_sinistre}}</strong>.</p>
<p>Nous vous transmettrons les documents de clôture dans les prochains jours.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- BAP
(gen_random_uuid(), 'BAP', 'Bon à payer', 'Émission d''un bon à payer',
 'Bon à payer — Sinistre n° {{numero_sinistre}} — {{montant}} FCFA',
 '<p>Bonjour,</p>
<p>Suite à l''accord intervenu concernant le sinistre n° <strong>{{numero_sinistre}}</strong>, nous vous prions de bien vouloir procéder au règlement de la somme de <strong>{{montant}} FCFA</strong> en faveur de <strong>{{beneficiaire}}</strong>.</p>
<p>Cordialement,</p>
<p><strong>{{expediteur_nom}}</strong><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE),

-- LIBRE
(gen_random_uuid(), 'LIBRE', 'Message libre', 'Email libre sans template prédéfini',
 '{{sujet}}',
 '<p>{{corps}}</p><p><br>{{signature}}</p>',
 TRUE, 'SYSTEM', TRUE, FALSE)

ON CONFLICT DO NOTHING;
