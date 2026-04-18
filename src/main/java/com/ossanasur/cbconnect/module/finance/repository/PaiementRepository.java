package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.module.finance.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {
    @Query("SELECT p FROM Paiement p WHERE p.paiementTrackingId=:id AND p.activeData=true AND p.deletedData=false")
    Optional<Paiement> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT p FROM Paiement p WHERE p.sinistre.sinistreTrackingId=:sid AND p.activeData=true AND p.deletedData=false ORDER BY p.dateEmission DESC")
    List<Paiement> findBySinistre(@Param("sid") UUID sinistreId);

    /**
     * État II — Paiements par pays émetteur du sinistre.
     * Colonnes : [0]=beneficiaire, [1]=codePays,
     * [2]=nbN1, [3]=montantN1, [4]=nbN, [5]=montantN
     */
    @Query(value = """
            SELECT
                p.libelle           AS beneficiaire,
                p.code_carte_brune  AS code_pays,
                COUNT(pm.historique_id) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1) AS nb_n1,
                COALESCE(SUM(pm.montant) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1), 0) AS montant_n1,
                COUNT(pm.historique_id) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)  AS nb_n,
                COALESCE(SUM(pm.montant) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN),  0) AS montant_n
            FROM paiement pm
            JOIN sinistre s ON s.historique_id = pm.sinistre_id
            JOIN pays p     ON p.historique_id = s.pays_emetteur_id
            WHERE pm.deleted_data = FALSE AND pm.active_data = TRUE
              AND s.deleted_data  = FALSE AND s.active_data  = TRUE
              AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
            GROUP BY p.libelle, p.code_carte_brune
            ORDER BY p.libelle
            """, nativeQuery = true)
    List<Object[]> statPaiementParPays(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

    /**
     * Détail Togo — Paiements par compagnie bénéficiaire (organisme)
     * pour les sinistres gérés par le Togo.
     * Colonnes : [0]=compagnie, [1]=code, [2]=nbN1, [3]=montantN1, [4]=nbN,
     * [5]=montantN
     */
    @Query(value = """
            SELECT
                o.raison_sociale AS compagnie,
                o.code           AS code,
                COUNT(pm.historique_id) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1) AS nb_n1,
                COALESCE(SUM(pm.montant) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1), 0) AS montant_n1,
                COUNT(pm.historique_id) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)  AS nb_n,
                COALESCE(SUM(pm.montant) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN),  0) AS montant_n
            FROM paiement pm
            JOIN sinistre  s ON s.historique_id = pm.sinistre_id
            JOIN pays      p ON p.historique_id = s.pays_gestionnaire_id
            LEFT JOIN organisme o ON o.historique_id = pm.beneficiaire_organisme_id
            WHERE pm.deleted_data = FALSE AND pm.active_data = TRUE
              AND s.deleted_data  = FALSE AND s.active_data  = TRUE
              AND p.code_carte_brune = 'TG'
              AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
              AND o.historique_id IS NOT NULL
            GROUP BY o.raison_sociale, o.code
            ORDER BY o.raison_sociale
            """, nativeQuery = true)
    List<Object[]> statPaiementDontTogo(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

}
