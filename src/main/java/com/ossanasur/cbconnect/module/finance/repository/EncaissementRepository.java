package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.module.finance.entity.Encaissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EncaissementRepository extends JpaRepository<Encaissement, Integer> {
    @Query("SELECT e FROM Encaissement e WHERE e.encaissementTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<Encaissement> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT e FROM Encaissement e WHERE e.sinistre.sinistreTrackingId=:sid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateReception DESC")
    List<Encaissement> findBySinistre(@Param("sid") UUID sinistreId);

    /**
     * État II — Encaissements par pays émetteur du sinistre.
     * Colonnes : [0]=bureau, [1]=codePays,
     * [2]=nbN1, [3]=montantN1, [4]=pgN1,
     * [5]=nbN, [6]=montantN, [7]=pgN
     */
    @Query(value = """
            SELECT
                p.libelle           AS bureau,
                p.code_carte_brune  AS code_pays,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1)                       AS nb_n1,
                COALESCE(SUM(e.montant_cheque)        FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1), 0)      AS montant_n1,
                COALESCE(SUM(e.produit_frais_gestion) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1), 0)      AS pg_n1,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN)                        AS nb_n,
                COALESCE(SUM(e.montant_cheque)        FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN),  0)      AS montant_n,
                COALESCE(SUM(e.produit_frais_gestion) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN),  0)      AS pg_n
            FROM encaissement e
            JOIN sinistre s ON s.historique_id = e.sinistre_id
            JOIN pays p     ON p.historique_id = s.pays_emetteur_id
            WHERE e.deleted_data = FALSE AND e.active_data = TRUE
              AND s.deleted_data = FALSE AND s.active_data = TRUE
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) IN (:anneeN1, :anneeN)
            GROUP BY p.libelle, p.code_carte_brune
            ORDER BY p.libelle
            """, nativeQuery = true)
    List<Object[]> statEncaissementParPays(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

    /**
     * Détail Togo — Encaissements par compagnie (organisme émetteur)
     * pour les sinistres gérés par le Togo.
     * Colonnes : [0]=compagnie, [1]=code, [2]=nbN1, [3]=montantN1, [4]=nbN,
     * [5]=montantN
     */
    @Query(value = """
            SELECT
                o.raison_sociale    AS compagnie,
                o.code              AS code,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1)                  AS nb_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1), 0)       AS montant_n1,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN)                   AS nb_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN),  0)       AS montant_n
            FROM encaissement e
            JOIN organisme o ON o.historique_id = e.organisme_emetteur_id
            JOIN sinistre  s ON s.historique_id = e.sinistre_id
            JOIN pays      p ON p.historique_id = s.pays_gestionnaire_id
            WHERE e.deleted_data = FALSE AND e.active_data = TRUE
              AND s.deleted_data = FALSE AND s.active_data = TRUE
              AND p.code_carte_brune = 'TG'
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) IN (:anneeN1, :anneeN)
            GROUP BY o.raison_sociale, o.code
            ORDER BY o.raison_sociale
            """, nativeQuery = true)
    List<Object[]> statEncaissementDontTogo(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

    /**
     * Vérifie si un encaissement existe déjà pour ce chèque et cet organisme.
     * Clé de doublon : même numeroCheque + même organismeEmetteur.
     */
    boolean existsByNumeroChequeAndOrganismeEmetteurAndActiveDataTrueAndDeletedDataFalse(
            String numeroCheque,
            com.ossanasur.cbconnect.module.auth.entity.Organisme organismeEmetteur);

    // Alias court utilisé dans RepriseService
    default boolean existsByNumeroChequeAndOrganismeEmetteur(
            String numeroCheque,
            com.ossanasur.cbconnect.module.auth.entity.Organisme organismeEmetteur) {
        return existsByNumeroChequeAndOrganismeEmetteurAndActiveDataTrueAndDeletedDataFalse(
                numeroCheque, organismeEmetteur);
    }

    /**
     * Statut reprise — compte les encaissements importés via reprise historique.
     */
    long countByRepriseHistoriqueAndActiveDataTrueAndDeletedDataFalse(boolean repriseHistorique);

}
