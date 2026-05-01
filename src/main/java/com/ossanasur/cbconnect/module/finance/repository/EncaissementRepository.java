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

    @Query("SELECT (COUNT(e) > 0) FROM Encaissement e " +
           "WHERE e.sinistre.sinistreTrackingId = :sid " +
           "AND e.statutCheque <> com.ossanasur.cbconnect.common.enums.StatutCheque.ANNULE " +
           "AND e.activeData = true AND e.deletedData = false")
    boolean existsNonAnnuleBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT COALESCE(SUM(e.montantCheque), 0) FROM Encaissement e " +
           "WHERE e.sinistre.sinistreTrackingId = :sid " +
           "AND e.statutCheque = com.ossanasur.cbconnect.common.enums.StatutCheque.ENCAISSE " +
           "AND e.activeData = true AND e.deletedData = false")
    java.math.BigDecimal sumMontantEncaisseBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT e FROM Encaissement e WHERE e.sinistre.sinistreTrackingId=:sid AND e.activeData=true AND e.deletedData=false ORDER BY e.dateReception DESC")
    List<Encaissement> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT e FROM Encaissement e " +
           "WHERE e.sinistre.sinistreTrackingId = :sid " +
           "AND e.statutCheque <> com.ossanasur.cbconnect.common.enums.StatutCheque.ANNULE " +
           "AND e.activeData = true AND e.deletedData = false " +
           "ORDER BY e.dateEncaissement ASC")
    List<Encaissement> findActifsBySinistre(@Param("sid") UUID sinistreTrackingId);

    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(e.montant_cheque), 0) FROM encaissement e
        JOIN sinistre s ON s.historique_id = e.sinistre_id
        WHERE s.sinistre_tracking_id = :sid
          AND e.statut_cheque <> 'ANNULE'
          AND e.active_data = TRUE AND e.deleted_data = FALSE
        """)
    java.math.BigDecimal sumMontantActifBySinistre(@Param("sid") UUID sid);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Encaissement e " +
           "WHERE e.sinistre.sinistreTrackingId = :sid " +
           "AND e.statutCheque <> com.ossanasur.cbconnect.common.enums.StatutCheque.ANNULE " +
           "AND e.activeData = true AND e.deletedData = false")
    boolean existsActifNonAnnuleBySinistre(@Param("sid") UUID sid);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Encaissement e " +
           "WHERE e.sinistre.sinistreTrackingId = :sid " +
           "AND e.statutCheque = com.ossanasur.cbconnect.common.enums.StatutCheque.ENCAISSE " +
           "AND e.activeData = true AND e.deletedData = false")
    boolean existsEncaisseBySinistre(@Param("sid") UUID sid);

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
                COALESCE(om.raison_sociale, 'AUTRES')   AS compagnie,
                COALESCE(om.code, '')                   AS code,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1)                  AS nb_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1), 0)       AS montant_n1,
                COUNT(e.historique_id)  FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN)                   AS nb_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN),  0)       AS montant_n
            FROM encaissement e
            JOIN sinistre  s  ON s.historique_id = e.sinistre_id
                AND s.active_data = TRUE AND s.deleted_data = FALSE
            JOIN pays      pg ON pg.historique_id = s.pays_gestionnaire_id
                AND pg.code_carte_brune = 'TG'
            LEFT JOIN organisme om ON om.historique_id = s.organisme_membre_id
                AND om.active_data    = TRUE
                AND om.deleted_data   = FALSE
                AND om.type_organisme = 'COMPAGNIE_MEMBRE'
                AND om.code_pays_bcb  = 'TG'
            WHERE e.deleted_data = FALSE AND e.active_data = TRUE
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) IN (:anneeN1, :anneeN)
            GROUP BY COALESCE(om.raison_sociale, 'AUTRES'), COALESCE(om.code, '')
            ORDER BY CASE WHEN COALESCE(om.raison_sociale, 'AUTRES') = 'AUTRES' THEN 1 ELSE 0 END, COALESCE(om.raison_sociale, 'AUTRES')
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

    /**
     * Retrouve un encaissement par son N° chèque et son organisme émetteur.
     * Utilisé par la reprise paiements pour lier le paiement à son encaissement.
     */
    @Query("SELECT e FROM Encaissement e " +
            "WHERE e.numeroCheque = :num " +
            "AND e.organismeEmetteur = :org " +
            "AND e.activeData = true AND e.deletedData = false")
    Optional<Encaissement> findByNumeroChequeAndOrganismeEmetteur(
            @Param("num") String numeroCheque,
            @Param("org") com.ossanasur.cbconnect.module.auth.entity.Organisme organismeEmetteur);

    /**
     * Reporting mensuel Encaissements — Tableau I : par PAYS (non plus par
     * compagnie).
     *
     * Groupement par pays du sinistre (pays_emetteur) — identique à la
     * structure Excel R2 : BENIN, BURKINA, CI, GHANA, MALI, NIGER, NIGERIA, TOGO*.
     *
     * Colonnes renvoyées : [0]=pays_libelle [1]=code_pays
     * [2-3] nb/mt mois N-1, [4-5] nb/mt mois N,
     * [6-7] nb/mt cumul N-1, [8-9] nb/mt cumul N,
     * [10-11] nb/mt fda N
     */
    @Query(value = """
            SELECT
                p.libelle                                                                            AS pays_libelle,
                p.code_carte_brune                                                                   AS code_pays,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois) AS nb_mois_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois), 0) AS mt_mois_n1,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois) AS nb_mois_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois), 0) AS mt_mois_n,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois) AS nb_cumul_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois), 0) AS mt_cumul_n1,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois) AS nb_cumul_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois), 0) AS mt_cumul_n,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN) AS nb_fda_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN), 0) AS mt_fda_n
            FROM encaissement e
            JOIN sinistre s ON s.historique_id = e.sinistre_id
                AND s.active_data = TRUE AND s.deleted_data = FALSE
            JOIN pays p ON p.historique_id = s.pays_emetteur_id
            WHERE e.deleted_data = FALSE
              AND e.active_data  = TRUE
              AND e.statut_cheque <> 'ANNULE'
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) IN (:anneeN1, :anneeN)
            GROUP BY p.libelle, p.code_carte_brune
            ORDER BY p.libelle
            """, nativeQuery = true)
    List<Object[]> reportingMensuelEncParPays(
            @Param("anneeN") int anneeN,
            @Param("anneeN1") int anneeN1,
            @Param("mois") int mois);

    /**
     * Reporting mensuel Encaissements — Tableau II : MARCHÉ TOGOLAIS.
     *
     * Groupe les encaissements par compagnie membre togolaise
     * (type_organisme = COMPAGNIE_MEMBRE, code_pays_bcb = 'TG').
     * Les encaissements sans membre togolais → "AUTRES".
     *
     * Colonnes renvoyées : mêmes indices que reportingMensuelEncParPays.
     */
    @Query(value = """
            SELECT
                COALESCE(om.raison_sociale, 'AUTRES')                                                      AS compagnie,
                NULL                                                                                        AS code_compagnie,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois)       AS nb_mois_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois), 0)   AS mt_mois_n1,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois)       AS nb_mois_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) = :mois), 0)   AS mt_mois_n,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois)      AS nb_cumul_n1,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN1
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois), 0)  AS mt_cumul_n1,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois)      AS nb_cumul_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN
                    AND EXTRACT(MONTH FROM COALESCE(e.date_encaissement, e.date_reception)) <= :mois), 0)  AS mt_cumul_n,
                COUNT(e.historique_id) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN)         AS nb_fda_n,
                COALESCE(SUM(e.montant_cheque) FILTER (WHERE
                    EXTRACT(YEAR  FROM COALESCE(e.date_encaissement, e.date_reception)) = :anneeN), 0)     AS mt_fda_n
            FROM encaissement e
            JOIN sinistre s ON s.historique_id = e.sinistre_id
                AND s.active_data = TRUE AND s.deleted_data = FALSE
            -- Jointure conditionnelle : uniquement compagnies membres TOGOLAISES
            LEFT JOIN organisme om ON om.historique_id = s.organisme_membre_id
                AND om.active_data    = TRUE
                AND om.deleted_data   = FALSE
                AND om.type_organisme = 'COMPAGNIE_MEMBRE'
                AND om.code_pays_bcb  = 'TG'
            WHERE e.deleted_data = FALSE
              AND e.active_data  = TRUE
              AND e.statut_cheque <> 'ANNULE'
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception)) IN (:anneeN1, :anneeN)
            GROUP BY COALESCE(om.raison_sociale, 'AUTRES')
            ORDER BY CASE WHEN COALESCE(om.raison_sociale, 'AUTRES') = 'AUTRES' THEN 1 ELSE 0 END, COALESCE(om.raison_sociale, 'AUTRES')
            """, nativeQuery = true)
    List<Object[]> reportingMensuelEncParCompagnie(
            @Param("anneeN") int anneeN,
            @Param("anneeN1") int anneeN1,
            @Param("mois") int mois);

    /**
     * Cadence encaissements par PAYS — triangle (survenus en × encaissés en).
     *
     * Axe survenance = EXTRACT(YEAR FROM s.date_accident).
     * Axe encaissement = EXTRACT(YEAR FROM COALESCE(e.date_encaissement,
     * e.date_reception)).
     * Groupement par pays_emetteur du sinistre.
     *
     * Colonnes : [0]=pays_libelle [1]=code_pays [2]=annee_surv [3]=annee_enc [4]=nb
     * [5]=montant
     */
    @Query(value = """
            SELECT
                p.libelle                                                                          AS pays_libelle,
                p.code_carte_brune                                                                 AS code_pays,
                CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
                     THEN EXTRACT(YEAR FROM s.date_accident)::int
                     ELSE -1 END                                                                   AS annee_surv,
                CASE WHEN EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))::int >= :anneeMin
                     THEN EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))::int
                     ELSE -1 END                                                                   AS annee_enc,
                COUNT(e.historique_id)                                                             AS nb,
                COALESCE(SUM(e.montant_cheque), 0)                                                 AS montant
            FROM encaissement e
            JOIN sinistre s ON s.historique_id = e.sinistre_id
                AND s.active_data = TRUE AND s.deleted_data = FALSE
            JOIN pays p ON p.historique_id = s.pays_emetteur_id
            WHERE e.deleted_data = FALSE
              AND e.active_data  = TRUE
              AND e.statut_cheque <> 'ANNULE'
            GROUP BY p.libelle, p.code_carte_brune, annee_surv, annee_enc
            ORDER BY p.libelle, annee_surv DESC, annee_enc DESC
            """, nativeQuery = true)
    List<Object[]> cadenceEncParPays(@Param("anneeMin") int anneeMin);

    /**
     * Cadence encaissements par COMPAGNIE MEMBRE TG.
     * Colonnes : mêmes indices que cadenceEncParPays, [0]=compagnie [1]=null.
     */
    @Query(value = """
            SELECT
                COALESCE(om.raison_sociale, 'AUTRES')                                              AS compagnie,
                NULL                                                                               AS code_compagnie,
                CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
                     THEN EXTRACT(YEAR FROM s.date_accident)::int
                     ELSE -1 END                                                                   AS annee_surv,
                CASE WHEN EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))::int >= :anneeMin
                     THEN EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))::int
                     ELSE -1 END                                                                   AS annee_enc,
                COUNT(e.historique_id)                                                             AS nb,
                COALESCE(SUM(e.montant_cheque), 0)                                                 AS montant
            FROM encaissement e
            JOIN sinistre s ON s.historique_id = e.sinistre_id
                AND s.active_data = TRUE AND s.deleted_data = FALSE
            JOIN pays pg ON pg.historique_id = s.pays_gestionnaire_id
                AND pg.code_carte_brune = 'TG'
            LEFT JOIN organisme om ON om.historique_id = s.organisme_membre_id
                AND om.active_data    = TRUE
                AND om.deleted_data   = FALSE
                AND om.type_organisme = 'COMPAGNIE_MEMBRE'
                AND om.code_pays_bcb  = 'TG'
            WHERE e.deleted_data = FALSE
              AND e.active_data  = TRUE
              AND e.statut_cheque <> 'ANNULE'
            GROUP BY COALESCE(om.raison_sociale, 'AUTRES'), annee_surv, annee_enc
            ORDER BY CASE WHEN COALESCE(om.raison_sociale, 'AUTRES') = 'AUTRES' THEN 1 ELSE 0 END, COALESCE(om.raison_sociale, 'AUTRES'), annee_surv DESC, annee_enc DESC
            """, nativeQuery = true)
    List<Object[]> cadenceEncParCompagnie(@Param("anneeMin") int anneeMin);

    /**
     * Graphique pluriannuel — Encaissements par année.
     * Colonnes : [0]=annee [1]=nb [2]=montant
     */
    @Query(value = """
            SELECT
                EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))::INT AS annee,
                COUNT(e.historique_id)                                                    AS nb,
                COALESCE(SUM(e.montant_cheque), 0)                                        AS montant
            FROM encaissement e
            WHERE e.deleted_data = FALSE AND e.active_data = TRUE
              AND EXTRACT(YEAR FROM COALESCE(e.date_encaissement, e.date_reception))
                  BETWEEN :anneeDebut AND :anneeFin
            GROUP BY annee
            ORDER BY annee
            """, nativeQuery = true)
    List<Object[]> encaissementsParAnnee(@Param("anneeDebut") int anneeDebut, @Param("anneeFin") int anneeFin);

}