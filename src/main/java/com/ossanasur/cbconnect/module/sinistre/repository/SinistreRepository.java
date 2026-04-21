package com.ossanasur.cbconnect.module.sinistre.repository;

import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.common.enums.TypeSinistre;
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
   * État I — Sinistres déclarés par pays émetteur, comparaison N-1 vs N.
   * Colonnes retournées : [0]=bureau, [1]=codePays, [2]=nbN1, [3]=nbN
   */
  @Query(value = """
      SELECT
          p.libelle                                                                               AS bureau,
          p.code_carte_brune                                                                      AS code_pays,
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN1) AS nb_n1,
          COUNT(s.historique_id) FILTER (WHERE EXTRACT(YEAR FROM s.date_declaration) = :anneeN)  AS nb_n
      FROM sinistre s
      JOIN pays p ON p.historique_id = s.pays_emetteur_id
      WHERE s.deleted_data = FALSE
        AND s.active_data  = TRUE
        AND EXTRACT(YEAR FROM s.date_declaration) IN (:anneeN1, :anneeN)
      GROUP BY p.libelle, p.code_carte_brune
      ORDER BY p.libelle
      """, nativeQuery = true)
  List<Object[]> statSinistreParPays(@Param("anneeN") int anneeN, @Param("anneeN1") int anneeN1);

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

}
