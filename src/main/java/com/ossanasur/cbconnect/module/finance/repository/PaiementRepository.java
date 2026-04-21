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

  /** Doublon : même N° chèque de paiement */
  boolean existsByNumeroChequeEmisAndActiveDataTrueAndDeletedDataFalse(String numeroChequeEmis);

  /** Comptage reprise */
  long countByRepriseHistoriqueAndActiveDataTrueAndDeletedDataFalse(boolean repriseHistorique);

  /**
   * Reporting mensuel Paiements — Tableau I : PAR PAYS BÉNÉFICIAIRE.
   *
   * Groupe les paiements (non annulés) par pays émetteur du sinistre
   * (= pays qui reçoit le paiement).
   *
   * Colonnes renvoyées (index) :
   * [0] pays [1] code_pays
   * [2] nb_mois_n1 [3] mt_mois_n1
   * [4] nb_mois_n [5] mt_mois_n
   * [6] nb_cumul_n1 [7] mt_cumul_n1
   * [8] nb_cumul_n [9] mt_cumul_n
   * [10] nb_fda_n [11] mt_fda_n
   */
  @Query(value = """
      SELECT
          p.libelle                                                                                       AS pays,
          p.code_carte_brune                                                                              AS code_pays,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)              AS nb_mois_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0)          AS mt_mois_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)              AS nb_mois_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0)          AS mt_mois_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)             AS nb_cumul_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0)         AS mt_cumul_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)             AS nb_cumul_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0)         AS mt_cumul_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)                AS nb_fda_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN), 0)            AS mt_fda_n
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays p ON p.historique_id = s.pays_emetteur_id
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut       <> 'ANNULE'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY p.libelle
      """, nativeQuery = true)
  List<Object[]> reportingMensuelPaiParPays(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

  /**
   * Reporting mensuel Paiements — Tableau II : MARCHÉ TOGOLAIS.
   *
   * Groupe les paiements par compagnie membre togolaise bénéficiaire
   * (type_organisme = COMPAGNIE_MEMBRE, code_pays_bcb = 'TG').
   * Les paiements sans organisme togolais → "VICTIMES" (bénéficiaires directs).
   *
   * Colonnes renvoyées : mêmes indices que reportingMensuelPaiParPays.
   */
  @Query(value = """
      SELECT
          COALESCE(om.raison_sociale, 'VICTIMES')                                                        AS compagnie,
          NULL                                                                                            AS code_compagnie,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)              AS nb_mois_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0)          AS mt_mois_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)              AS nb_mois_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0)          AS mt_mois_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)             AS nb_cumul_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0)         AS mt_cumul_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)             AS nb_cumul_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0)         AS mt_cumul_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)                AS nb_fda_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN), 0)            AS mt_fda_n
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays p ON p.historique_id = s.pays_gestionnaire_id
      -- Compagnie membre togolaise bénéficiaire (peut être NULL → paiement victime)
      LEFT JOIN organisme om ON om.historique_id = pm.beneficiaire_organisme_id
          AND om.active_data    = TRUE
          AND om.deleted_data   = FALSE
          AND om.type_organisme = 'COMPAGNIE_MEMBRE'
          AND om.code_pays_bcb  = 'TG'
      WHERE pm.deleted_data  = FALSE
        AND pm.active_data   = TRUE
        AND pm.statut        <> 'ANNULE'
        AND p.code_carte_brune = 'TG'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
      GROUP BY COALESCE(om.raison_sociale, 'VICTIMES')
      ORDER BY COALESCE(om.raison_sociale, 'VICTIMES')
      """, nativeQuery = true)
  List<Object[]> reportingMensuelPaiParCompagnie(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

  // ══════════════════════════════════════════════════════════════════════════
  // R4 — CADENCE DE SURVENANCE PAR RAPPORT AU PAIEMENT
  // ══════════════════════════════════════════════════════════════════════════

  /**
   * Cadence TOTAL — tous sinistres, tous pays.
   *
   * Colonnes : [0]=annee_survenance [1]=annee_paiement [2]=nb [3]=montant
   */
  @Query(value = """
      SELECT
          EXTRACT(YEAR FROM s.date_accident)::INT                              AS annee_survenance,
          EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::INT AS annee_paiement,
          COUNT(pm.historique_id)                                               AS nb,
          COALESCE(SUM(pm.montant), 0)                                          AS montant
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
                     AND s.active_data   = TRUE AND s.deleted_data = FALSE
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut       <> 'ANNULE'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :anneeMax
      GROUP BY annee_survenance, annee_paiement
      ORDER BY annee_survenance, annee_paiement
      """, nativeQuery = true)
  List<Object[]> cadenceTotal(@Param("anneeMax") int anneeMax);

  /**
   * Cadence par COMPAGNIE MEMBRE TOGOLAISE (organisme_membre).
   *
   * Filtre : pays_gestionnaire = TG.
   *
   * Colonnes : [0]=compagnie [1]=annee_survenance [2]=annee_paiement [3]=nb
   * [4]=montant
   */
  @Query(value = """
      SELECT
          COALESCE(o.raison_sociale, 'AUTRES')                                 AS compagnie,
          EXTRACT(YEAR FROM s.date_accident)::INT                              AS annee_survenance,
          EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::INT AS annee_paiement,
          COUNT(pm.historique_id)                                               AS nb,
          COALESCE(SUM(pm.montant), 0)                                          AS montant
      FROM paiement pm
      JOIN sinistre  s  ON s.historique_id = pm.sinistre_id
                       AND s.active_data   = TRUE AND s.deleted_data = FALSE
      JOIN pays      pg ON pg.historique_id = s.pays_gestionnaire_id
      LEFT JOIN organisme o ON o.historique_id = s.organisme_membre_id
          AND o.active_data    = TRUE
          AND o.deleted_data   = FALSE
          AND o.type_organisme = 'COMPAGNIE_MEMBRE'
          AND o.code_pays_bcb  = 'TG'
      WHERE pm.deleted_data     = FALSE
        AND pm.active_data      = TRUE
        AND pm.statut           <> 'ANNULE'
        AND pg.code_carte_brune  = 'TG'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :anneeMax
      GROUP BY COALESCE(o.raison_sociale, 'AUTRES'), annee_survenance, annee_paiement
      ORDER BY COALESCE(o.raison_sociale, 'AUTRES'), annee_survenance, annee_paiement
      """, nativeQuery = true)
  List<Object[]> cadenceParCompagnieTogo(@Param("anneeMax") int anneeMax);

  /**
   * Cadence — paiements croisés (survenus en × payés en), par pays.
   *
   * Le "pays" est le pays_emetteur du sinistre (pays de l'organisme homologue
   * qui a déclaré le sinistre).
   *
   * Colonnes renvoyées :
   * [0] pays_libelle [1] code_pays
   * [2] annee_surv (-1 = regroupement "ant" pour toute année < anneeMin)
   * [3] annee_pay (-1 idem)
   * [4] nb COUNT(paiements)
   * [5] montant SUM(paiement.montant)
   */
  @Query(value = """
      SELECT
          p.libelle                                                                   AS pays_libelle,
          p.code_carte_brune                                                         AS code_pays,
          CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
               THEN EXTRACT(YEAR FROM s.date_accident)::int
               ELSE -1 END                                                           AS annee_surv,
          CASE WHEN EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::int >= :anneeMin
               THEN EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::int
               ELSE -1 END                                                           AS annee_pay,
          COUNT(pm.historique_id)                                                    AS nb,
          COALESCE(SUM(pm.montant), 0)                                               AS montant
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays p ON p.historique_id = s.pays_emetteur_id
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut      <> 'ANNULE'
      GROUP BY p.libelle, p.code_carte_brune, annee_surv, annee_pay
      ORDER BY p.libelle, annee_surv DESC, annee_pay DESC
      """, nativeQuery = true)
  List<Object[]> cadenceParPays(@Param("anneeMin") int anneeMin);

  /**
   * Cadence — paiements croisés (survenus en × payés en), par compagnie membre
   * TG.
   *
   * La compagnie est l'organisme_membre du sinistre
   * (compagnie togolaise ayant déclaré le sinistre).
   * Filtre : uniquement les sinistres gérés par le Togo (pays_gestionnaire = TG).
   * Les sinistres sans compagnie membre → libellé "AUTRES".
   *
   * Colonnes : mêmes indices que cadenceParPays, [0]=compagnie, [1]=null.
   */
  @Query(value = """
      SELECT
          COALESCE(o.raison_sociale, 'AUTRES')                                       AS compagnie,
          NULL                                                                        AS code_compagnie,
          CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
               THEN EXTRACT(YEAR FROM s.date_accident)::int
               ELSE -1 END                                                           AS annee_surv,
          CASE WHEN EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::int >= :anneeMin
               THEN EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission))::int
               ELSE -1 END                                                           AS annee_pay,
          COUNT(pm.historique_id)                                                    AS nb,
          COALESCE(SUM(pm.montant), 0)                                               AS montant
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays pg ON pg.historique_id = s.pays_gestionnaire_id
          AND pg.code_carte_brune = 'TG'
      LEFT JOIN organisme o ON o.historique_id = s.organisme_membre_id
          AND o.active_data    = TRUE
          AND o.deleted_data   = FALSE
          AND o.type_organisme = 'COMPAGNIE_MEMBRE'
          AND o.code_pays_bcb  = 'TG'
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut      <> 'ANNULE'
      GROUP BY COALESCE(o.raison_sociale, 'AUTRES'), annee_surv, annee_pay
      ORDER BY COALESCE(o.raison_sociale, 'AUTRES'), annee_surv DESC, annee_pay DESC
      """, nativeQuery = true)
  List<Object[]> cadenceParCompagnie(@Param("anneeMin") int anneeMin);

  /**
   * Reporting mensuel Paiements — Tableau I : par PAYS émetteur.
   *
   * Structure identique à reportingMensuelEncParPays mais sur la table paiement.
   * Date pivot = COALESCE(pm.date_paiement, pm.date_emission).
   * Montant = pm.montant.
   *
   * Colonnes : [0]=pays_libelle [1]=code_pays [2-11] nb/mt mois/cumul/fda
   */
  @Query(value = """
      SELECT
          p.libelle                                                                              AS pays_libelle,
          p.code_carte_brune                                                                     AS code_pays,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)     AS nb_mois_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0) AS mt_mois_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)     AS nb_mois_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0) AS mt_mois_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)    AS nb_cumul_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0) AS mt_cumul_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)    AS nb_cumul_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0) AS mt_cumul_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)       AS nb_fda_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN), 0)   AS mt_fda_n
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays p ON p.historique_id = s.pays_emetteur_id
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut       <> 'ANNULE'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY p.libelle
      """, nativeQuery = true)
  List<Object[]> reportingMensuelPayParPays(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

  /**
   * Reporting mensuel Paiements — Tableau II : par compagnie membre togolaise.
   * Filtre : paiements liés à des sinistres gérés par le Togo (pays_gestionnaire
   * = TG).
   */
  @Query(value = """
      SELECT
          COALESCE(om.raison_sociale, 'AUTRES')                                              AS compagnie,
          NULL                                                                               AS code_compagnie,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)     AS nb_mois_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0) AS mt_mois_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois)     AS nb_mois_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) = :mois), 0) AS mt_mois_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)    AS nb_cumul_n1,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN1
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0) AS mt_cumul_n1,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois)    AS nb_cumul_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN
              AND EXTRACT(MONTH FROM COALESCE(pm.date_paiement, pm.date_emission)) <= :mois), 0) AS mt_cumul_n,
          COUNT(pm.historique_id) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN)       AS nb_fda_n,
          COALESCE(SUM(pm.montant) FILTER (WHERE
              EXTRACT(YEAR  FROM COALESCE(pm.date_paiement, pm.date_emission)) = :anneeN), 0)   AS mt_fda_n
      FROM paiement pm
      JOIN sinistre s ON s.historique_id = pm.sinistre_id
          AND s.active_data = TRUE AND s.deleted_data = FALSE
      JOIN pays pg ON pg.historique_id = s.pays_gestionnaire_id
          AND pg.code_carte_brune = 'TG'
      LEFT JOIN organisme om ON om.historique_id = s.organisme_membre_id
          AND om.active_data    = TRUE
          AND om.deleted_data   = FALSE
          AND om.type_organisme = 'COMPAGNIE_MEMBRE'
          AND om.code_pays_bcb  = 'TG'
      WHERE pm.deleted_data = FALSE
        AND pm.active_data  = TRUE
        AND pm.statut       <> 'ANNULE'
        AND EXTRACT(YEAR FROM COALESCE(pm.date_paiement, pm.date_emission)) IN (:anneeN1, :anneeN)
      GROUP BY COALESCE(om.raison_sociale, 'AUTRES')
      ORDER BY COALESCE(om.raison_sociale, 'AUTRES')
      """, nativeQuery = true)
  List<Object[]> reportingMensuelPayParCompagnie(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

}
