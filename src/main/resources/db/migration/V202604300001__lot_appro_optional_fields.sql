-- Rendre prix_unitaire_achat et date_commande optionnels sur lot_approvisionnement.
-- Le prix d'achat est désormais saisi ailleurs ; la date de commande est facultative.
ALTER TABLE lot_approvisionnement ALTER COLUMN prix_unitaire_achat DROP NOT NULL;
ALTER TABLE lot_approvisionnement ALTER COLUMN date_commande DROP NOT NULL;
