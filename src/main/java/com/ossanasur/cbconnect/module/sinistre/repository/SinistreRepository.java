package com.ossanasur.cbconnect.module.sinistre.repository;

import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
import com.ossanasur.cbconnect.common.enums.PositionRc;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Integer> {
  @Query("SELECT s FROM Sinistre s WHERE s.sinistreTrackingId=:id AND s.activeData=true AND s.deletedData=false")
  Optional<Sinistre> findActiveByTrackingId(@Param("id") UUID id);

  @Query("SELECT s FROM Sinistre s WHERE s.numeroSinistreLocal=:num AND s.activeData=true AND s.deletedData=false")
  Optional<Sinistre> findByNumeroSinistreLocal(@Param("num") String numero);

        @Query("SELECT s FROM Sinistre s WHERE s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
        Page<Sinistre> findAllActive(Pageable pageable);

        @Query("SELECT s FROM Sinistre s "
                        + "LEFT JOIN s.assure a "
                        + "WHERE s.activeData=true AND s.deletedData=false "
                        + "  AND ("
                        + "       LOWER(s.numeroSinistreLocal)     LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(s.numeroSinistreManuel)    LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(s.numeroSinistreHomologue) LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(a.nomAssure)               LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(a.prenomAssure)            LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(a.nomComplet)              LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(a.immatriculation)         LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "    OR LOWER(a.numeroPolice)            LIKE LOWER(CONCAT('%', :q, '%')) "
                        + "  ) "
                        + "ORDER BY s.dateDeclaration DESC")
        Page<Sinistre> search(@Param("q") String query, Pageable pageable);

        @Query("""
                        SELECT s FROM Sinistre s
                        LEFT JOIN s.assure a
                        LEFT JOIN s.organismeMembre om
                        LEFT JOIN s.paysEmetteur pe
                        WHERE s.activeData=true AND s.deletedData=false
                          AND (:q IS NULL OR :q = '' OR (
                               LOWER(s.numeroSinistreLocal)     LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(s.numeroSinistreManuel)    LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(s.numeroSinistreHomologue) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.nomAssure)               LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.prenomAssure)            LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.nomComplet)              LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.immatriculation)         LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(a.numeroPolice)            LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(om.raisonSociale)          LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(pe.libelle)                LIKE LOWER(CONCAT('%', :q, '%'))
                          ))
                          AND (:statut IS NULL OR s.statut = :statut)
                          AND (:dateDebut IS NULL OR s.dateDeclaration >= :dateDebut)
                          AND (:dateFin IS NULL OR s.dateDeclaration <= :dateFin)
                          AND (:positionRc IS NULL OR s.positionRc = :positionRc)
                          AND (:rcManquante = false OR (
                              s.statut IN :statutsRcAttendue
                              AND (s.positionRc IS NULL OR s.positionRc <> :tranchee)
                          ))
                          AND (:rcPct IS NULL OR (
                              s.positionRc = :tranchee
                              AND s.tauxRc IS NOT NULL
                              AND (
                                  (:rcPct = '<50' AND s.tauxRc < 50)
                                  OR (:rcPct = '50-80' AND s.tauxRc >= 50 AND s.tauxRc <= 80)
                                  OR (:rcPct = '>80' AND s.tauxRc > 80)
                              )
                          ))
                          AND (:litige IS NULL
                              OR (:litige = 'EN_LITIGE' AND (s.statut IN :statutsLitige OR s.estContentieux = true))
                              OR (:litige = 'HORS_LITIGE' AND (s.statut NOT IN :statutsLitige AND s.estContentieux = false))
                              OR (:litige = 'CONTENTIEUX' AND s.statut = :statutContentieux)
                              OR (:litige = 'ARBITRAGE' AND s.statut = :statutArbitrage)
                          )
                        ORDER BY s.dateDeclaration DESC
                        """)
        Page<Sinistre> findAllFiltered(
                        @Param("q") String query,
                        @Param("statut") StatutSinistre statut,
                        @Param("positionRc") PositionRc positionRc,
                        @Param("rcManquante") boolean rcManquante,
                        @Param("rcPct") String rcPct,
                        @Param("litige") String litige,
                        @Param("dateDebut") LocalDate dateDebut,
                        @Param("dateFin") LocalDate dateFin,
                        @Param("statutsRcAttendue") List<StatutSinistre> statutsRcAttendue,
                        @Param("statutsLitige") List<StatutSinistre> statutsLitige,
                        @Param("statutContentieux") StatutSinistre statutContentieux,
                        @Param("statutArbitrage") StatutSinistre statutArbitrage,
                        @Param("tranchee") PositionRc tranchee,
                        Pageable pageable);

  @Query("SELECT s FROM Sinistre s WHERE s.statut=:statut AND s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
  List<Sinistre> findAllByStatut(@Param("statut") StatutSinistre statut);

  @Query("SELECT s FROM Sinistre s WHERE s.redacteur.utilisateurTrackingId=:uid AND s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
  List<Sinistre> findAllByRedacteur(@Param("uid") UUID utilisateurTrackingId);

  @Query("SELECT s FROM Sinistre s WHERE s.activeData=true AND s.deletedData=false AND s.dateProchaineAudience BETWEEN :debut AND :fin")
  List<Sinistre> findAudiencesBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

  boolean existsByNumeroSinistreLocalAndActiveDataTrueAndDeletedDataFalse(String numero);

  @Query("SELECT COUNT(s) FROM Sinistre s WHERE YEAR(s.dateDeclaration)=:annee AND s.typeSinistre=:type AND s.activeData=true AND s.deletedData=false")
  long countByAnneeAndType(@Param("annee") int annee,
      @Param("type") com.ossanasur.cbconnect.common.enums.TypeSinistre type);

  // existsByNumeroSinistreManuel
  @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Sinistre s WHERE s.numeroSinistreManuel=:num AND s.activeData=true AND s.deletedData=false")
  boolean existsByNumeroSinistreManuel(@Param("num") String numeroSinistreManuel);

  // countByRepriseHistorique
  @Query("SELECT COUNT(s) FROM Sinistre s WHERE s.repriseHistorique=:reprise AND s.activeData=true AND s.deletedData=false")
  long countByRepriseHistorique(@Param("reprise") boolean reprise);

  @Query("SELECT COUNT(s) FROM Sinistre s WHERE s.typeSinistre=:type AND s.repriseHistorique=:reprise AND s.activeData=true AND s.deletedData=false")
  long countByTypeSinistreAndRepriseHistorique(@Param("type") TypeSinistre typeSinistre,
      @Param("reprise") boolean reprise);

  /**
   * État I — Sinistres déclarés par pays partenaire (hors Togo), comparaison N-1 vs N.
   * ET (SURVENU_TOGO)     → pays partenaire = pays_emetteur    (véhicule étranger entré au Togo)
   * TE (SURVENU_ETRANGER) → pays partenaire = pays_gestionnaire (sinistre togolais à l'étranger)
   * Le Togo (code_carte_brune = 'TG') est exclu des lignes.
   * Colonnes retournées : [0]=bureau, [1]=codePays, [2]=nbN1, [3]=nbN
   */
  @Query(value = """
      SELECT
          p.libelle                                                                               AS bureau,
          p.code_carte_brune                                                                      AS code_pays,
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN1) AS nb_n1,
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN)  AS nb_n
      FROM sinistre s
      JOIN pays p ON p.historique_id = CASE
          WHEN s.type_sinistre = 'SURVENU_TOGO'     THEN s.pays_emetteur_id
          WHEN s.type_sinistre = 'SURVENU_ETRANGER' THEN s.pays_gestionnaire_id
          ELSE s.pays_emetteur_id
      END
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND p.code_carte_brune <> 'TG'
        AND EXTRACT(YEAR FROM s.date_declaration) IN (:anneeN1, :anneeN)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY nb_n DESC, nb_n1 DESC
      """, nativeQuery = true)
  List<Object[]> statSinistreParPays(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

  /**
   * Évolution pluriannuelle — total, ET et TE par année civile.
   * Colonnes retournées : [0]=annee, [1]=total, [2]=et, [3]=te
   */
  @Query(value = """
      SELECT
          EXTRACT(YEAR FROM s.date_declaration)::INTEGER                            AS annee,
          COUNT(s.historique_id)                                                    AS total,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO')   AS et,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER') AS te
      FROM sinistre s
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND EXTRACT(YEAR FROM s.date_declaration) BETWEEN :anneeDebut AND :anneeFin
      GROUP BY 1
      ORDER BY 1
      """, nativeQuery = true)
  List<Object[]> evolutionSinistresParAnnee(@Param("anneeDebut") int anneeDebut, @Param("anneeFin") int anneeFin);

  /**
   * Cherche un sinistre actif par son numéro manuel (ET ou TE).
   * Utilisé par la reprise encaissements pour lier l'encaissement au bon
   * sinistre.
   */
  @Query("SELECT s FROM Sinistre s WHERE s.numeroSinistreManuel = :num " +
      "AND s.activeData = true AND s.deletedData = false")
  Optional<Sinistre> findByNumeroSinistreManuel(@Param("num") String numeroSinistreManuel);

  /**
   * Reporting mensuel — par pays partenaire CEDEAO.
   *
   * Pour chaque pays étranger (pays émetteur des ET, pays gestionnaire des TE) :
   * Retourne les comptages TE et ET pour :
   * - le mois courant (années N et N-1)
   * - le cumul jan→mois (années N et N-1)
   * - la fin d'année N (jan→dec)
   *
   * Colonnes renvoyées (index) :
   * [0] pays_libelle [1] code_pays
   * [2] te_mois_n1 [3] te_mois_n
   * [4] et_mois_n1 [5] et_mois_n
   * [6] te_cumul_n1 [7] te_cumul_n
   * [8] et_cumul_n1 [9] et_cumul_n
   * [10] tot_fda_n
   */
  @Query(value = """
      SELECT
          p.libelle                                                                                                          AS pays_libelle,
          p.code_carte_brune                                                                                                 AS code_pays,
          -- MOIS N-1
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS te_mois_n1,
          -- MOIS N
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS te_mois_n,
          -- ET MOIS N-1
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS et_mois_n1,
          -- ET MOIS N
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS et_mois_n,
          -- CUMUL TE N-1
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS te_cumul_n1,
          -- CUMUL TE N
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS te_cumul_n,
          -- CUMUL ET N-1
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS et_cumul_n1,
          -- CUMUL ET N
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS et_cumul_n,
          -- FIN D'ANNÉE N
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN)                            AS tot_fda_n
      FROM sinistre s
      -- Pour les ET : pays émetteur = pays étranger d'origine
      -- Pour les TE : pays gestionnaire = pays où l'accident s'est produit
      JOIN pays p ON p.historique_id = CASE
          WHEN s.type_sinistre = 'SURVENU_TOGO'     THEN s.pays_emetteur_id
          WHEN s.type_sinistre = 'SURVENU_ETRANGER' THEN s.pays_gestionnaire_id
      END
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND p.code_carte_brune <> 'TG'
        AND EXTRACT(YEAR FROM s.date_declaration) IN (:anneeN1, :anneeN)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY p.libelle
      """, nativeQuery = true)
  List<Object[]> reportingMensuelParPays(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

  /**
   * Reporting mensuel — par compagnie membre togolaise (CIMA RC Auto).
   *
   * Colonnes renvoyées (mêmes indices que reportingMensuelParPays) :
   * [0] raison_sociale [1] null (pas de code)
   * [2..10] mêmes comptages TE/ET/cumul/fda
   */
  @Query(value = """
      SELECT
          COALESCE(o.raison_sociale, 'AUTRES')                                                                              AS raison_sociale,
          NULL                                                                                                               AS code_compagnie,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS te_mois_n1,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS te_mois_n,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS et_mois_n1,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) = :mois)                                                           AS et_mois_n,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS te_cumul_n1,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_ETRANGER'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS te_cumul_n,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN1
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS et_cumul_n1,
          COUNT(s.historique_id) FILTER (WHERE s.type_sinistre = 'SURVENU_TOGO'
              AND EXTRACT(YEAR  FROM s.date_declaration) = :anneeN
              AND EXTRACT(MONTH FROM s.date_declaration) <= :mois)                                                          AS et_cumul_n,
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN)                            AS tot_fda_n
      FROM sinistre s
      -- Jointure uniquement sur les compagnies membres TOGOLAISES (CIMA RC Auto)
      -- type_organisme = COMPAGNIE_MEMBRE  ET  code_pays_bcb = 'TG'
      -- Les sinistres sans membre togolais sont regroupés sous "AUTRES"
      LEFT JOIN organisme o ON o.historique_id = s.organisme_membre_id
          AND o.active_data      = TRUE
          AND o.deleted_data     = FALSE
          AND o.type_organisme   = 'COMPAGNIE_MEMBRE'
          AND o.code_pays_bcb    = 'TG'
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND EXTRACT(YEAR FROM s.date_declaration) IN (:anneeN1, :anneeN)
      GROUP BY COALESCE(o.raison_sociale, 'AUTRES')
      ORDER BY COALESCE(o.raison_sociale, 'AUTRES')
      """, nativeQuery = true)
  List<Object[]> reportingMensuelParCompagnie(
      @Param("anneeN") int anneeN,
      @Param("anneeN1") int anneeN1,
      @Param("mois") int mois);

  /**
   * Cadence — sinistres déclarés par (pays_emetteur, année de survenance).
   * Sert à calculer le Taux Payé = paiements / déclarés.
   *
   * Colonnes : [0]=pays_libelle, [1]=code_pays, [2]=annee_surv, [3]=nb_declares
   */
  @Query(value = """
      SELECT
          p.libelle                                                                   AS pays_libelle,
          p.code_carte_brune                                                         AS code_pays,
          CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
               THEN EXTRACT(YEAR FROM s.date_accident)::int
               ELSE -1 END                                                           AS annee_surv,
          COUNT(s.historique_id)                                                     AS nb_declares
      FROM sinistre s
      JOIN pays p ON p.historique_id = s.pays_emetteur_id
      WHERE s.deleted_data = FALSE AND s.active_data = TRUE
      GROUP BY p.libelle, p.code_carte_brune, annee_surv
      ORDER BY p.libelle, annee_surv DESC
      """, nativeQuery = true)
  List<Object[]> sinistresDeclaresParPays(@Param("anneeMin") int anneeMin);

  /**
   * Cadence — sinistres déclarés par (compagnie membre TG, année de survenance).
   *
   * Colonnes : [0]=compagnie, [1]=null, [2]=annee_surv, [3]=nb_declares
   */
  @Query(value = """
      SELECT
          COALESCE(o.raison_sociale, 'AUTRES')                                       AS compagnie,
          NULL                                                                        AS code_compagnie,
          CASE WHEN EXTRACT(YEAR FROM s.date_accident)::int >= :anneeMin
               THEN EXTRACT(YEAR FROM s.date_accident)::int
               ELSE -1 END                                                           AS annee_surv,
          COUNT(s.historique_id)                                                     AS nb_declares
      FROM sinistre s
      JOIN pays pg ON pg.historique_id = s.pays_gestionnaire_id
          AND pg.code_carte_brune = 'TG'
      LEFT JOIN organisme o ON o.historique_id = s.organisme_membre_id
          AND o.active_data    = TRUE
          AND o.deleted_data   = FALSE
          AND o.type_organisme = 'COMPAGNIE_MEMBRE'
          AND o.code_pays_bcb  = 'TG'
      WHERE s.deleted_data = FALSE AND s.active_data = TRUE
      GROUP BY COALESCE(o.raison_sociale, 'AUTRES'), annee_surv
      ORDER BY COALESCE(o.raison_sociale, 'AUTRES'), annee_surv DESC
      """, nativeQuery = true)
  List<Object[]> sinistresDeclaresParCompagnie(@Param("anneeMin") int anneeMin);

  /**
   * Renvoie les sinistres pour lesquels un expert donné est éligible au
   * paiement d'honoraires, c'est-à-dire :
   * 1. Une affectation VALIDE lie l'expert au sinistre.
   * 2. Au moins un rapport d'expertise (médicale ou matérielle) a été déposé
   *    (dateRapport IS NOT NULL) par cet expert pour ce sinistre.
   * 3. Au moins un encaissement actif (statut_cheque != ANNULE) est rattaché
   *    au sinistre.
   *
   * Note technique — ExpertiseMedicale n'a pas de FK directe vers sinistre :
   * la jointure passe par la table victime (em.victime_id → v.historique_id,
   * v.sinistre_id = s.historique_id).
   */
  /**
   * État litiges — Récapitulatif par pays partenaire.
   * annee=0 → tous les exercices ; sinon filtre sur EXTRACT(YEAR FROM date_declaration).
   * Colonnes : [0]=bureau, [1]=code_pays, [2]=nb_contentieux, [3]=nb_arbitrage, [4]=nb_total
   */
  @Query(value = """
      SELECT
          p.libelle              AS bureau,
          p.code_carte_brune    AS code_pays,
          COUNT(s.historique_id) FILTER (WHERE s.statut = 'CONTENTIEUX') AS nb_contentieux,
          COUNT(s.historique_id) FILTER (WHERE s.statut = 'ARBITRAGE')   AS nb_arbitrage,
          COUNT(s.historique_id)                                          AS nb_total
      FROM sinistre s
      JOIN pays p ON p.historique_id = CASE
          WHEN s.type_sinistre = 'SURVENU_TOGO'     THEN s.pays_emetteur_id
          WHEN s.type_sinistre = 'SURVENU_ETRANGER' THEN s.pays_gestionnaire_id
          ELSE s.pays_emetteur_id
      END
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND s.statut IN ('CONTENTIEUX', 'ARBITRAGE')
        AND (:annee = 0 OR EXTRACT(YEAR FROM s.date_declaration) = :annee)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY nb_total DESC
      """, nativeQuery = true)
  List<Object[]> litigeParPays(@Param("annee") int annee);

  /**
   * État litiges — Liste détaillée des dossiers en contentieux ou arbitrage.
   * Colonnes : [0]=numero, [1]=date_declaration, [2]=assure, [3]=type_sinistre,
   *            [4]=pays_partenaire, [5]=statut, [6]=niveau_juridiction, [7]=date_prochaine_audience
   */
  @Query(value = """
      SELECT
          s.numero_sinistre_local                                                              AS numero,
          TO_CHAR(s.date_declaration, 'DD/MM/YYYY')                                          AS date_declaration,
          COALESCE(a.nom_complet, CONCAT(a.nom_assure, ' ', COALESCE(a.prenom_assure, ''))) AS assure,
          s.type_sinistre,
          p.libelle                                                                            AS pays_partenaire,
          s.statut,
          s.niveau_juridiction,
          TO_CHAR(s.date_prochaine_audience, 'DD/MM/YYYY')                                  AS date_prochaine_audience
      FROM sinistre s
      LEFT JOIN assure a ON a.historique_id = s.assure_id
          AND a.active_data = TRUE AND a.deleted_data = FALSE
      JOIN pays p ON p.historique_id = CASE
          WHEN s.type_sinistre = 'SURVENU_TOGO'     THEN s.pays_emetteur_id
          WHEN s.type_sinistre = 'SURVENU_ETRANGER' THEN s.pays_gestionnaire_id
          ELSE s.pays_emetteur_id
      END
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND s.statut IN ('CONTENTIEUX', 'ARBITRAGE')
        AND (:annee = 0 OR EXTRACT(YEAR FROM s.date_declaration) = :annee)
      ORDER BY s.statut, s.date_prochaine_audience ASC NULLS LAST, s.date_declaration DESC
      """, nativeQuery = true)
  List<Object[]> dossiersEnLitige(@Param("annee") int annee);

  @Query(nativeQuery = true, value = """
      SELECT s.* FROM sinistre s
      WHERE EXISTS (
              SELECT 1 FROM affectation_expert a
              WHERE a.sinistre_id = s.historique_id
                AND a.expert_id = :expertId
                AND a.statut = 'VALIDE'
                AND a.active_data = TRUE AND a.deleted_data = FALSE)
        AND (
              EXISTS (SELECT 1 FROM expertise_medicale em
                      JOIN victime v ON v.historique_id = em.victime_id
                      WHERE v.sinistre_id = s.historique_id
                        AND em.expert_id = :expertId
                        AND em.date_rapport IS NOT NULL
                        AND em.active_data = TRUE AND em.deleted_data = FALSE)
           OR EXISTS (SELECT 1 FROM expertise_materielle ema
                      WHERE ema.sinistre_id = s.historique_id
                        AND ema.expert_id = :expertId
                        AND ema.date_rapport IS NOT NULL
                        AND ema.active_data = TRUE AND ema.deleted_data = FALSE)
            )
        AND EXISTS (SELECT 1 FROM encaissement e
                    WHERE e.sinistre_id = s.historique_id
                      AND e.statut_cheque <> 'ANNULE'
                      AND e.active_data = TRUE AND e.deleted_data = FALSE)
        AND s.active_data = TRUE AND s.deleted_data = FALSE
      """)
  List<Sinistre> findPayablesForExpert(@Param("expertId") Integer expertId);

  /**
   * Type d'expertise (INITIALE / CONTRE_EXPERTISE / AMIABLE / TIERCE_EXPERTISE)
   * de l'affectation active de cet expert sur ce sinistre.
   */
  @Query(nativeQuery = true, value = """
      SELECT a.type_expertise FROM affectation_expert a
      WHERE a.sinistre_id = :sinistreId
        AND a.expert_id = :expertId
        AND a.active_data = TRUE AND a.deleted_data = FALSE
      ORDER BY a.date_affectation DESC
      LIMIT 1
      """)
  String findTypeExpertiseForExpert(
      @Param("sinistreId") Integer sinistreId,
      @Param("expertId") Integer expertId);

  /**
   * Date du rapport d'expertise (médicale via victime, ou matérielle via sinistre)
   * pour cet expert sur ce sinistre. Renvoie la plus récente si plusieurs.
   */
  @Query(nativeQuery = true, value = """
      SELECT MAX(date_rapport) FROM (
          SELECT em.date_rapport
          FROM expertise_medicale em
          JOIN victime v ON v.historique_id = em.victime_id
          WHERE v.sinistre_id = :sinistreId
            AND em.expert_id = :expertId
            AND em.date_rapport IS NOT NULL
            AND em.active_data = TRUE AND em.deleted_data = FALSE
          UNION ALL
          SELECT ema.date_rapport
          FROM expertise_materielle ema
          WHERE ema.sinistre_id = :sinistreId
            AND ema.expert_id = :expertId
            AND ema.date_rapport IS NOT NULL
            AND ema.active_data = TRUE AND ema.deleted_data = FALSE
      ) AS toutes_dates
      """)
  java.time.LocalDate findDateRapportForExpert(
      @Param("sinistreId") Integer sinistreId,
      @Param("expertId") Integer expertId);

}
