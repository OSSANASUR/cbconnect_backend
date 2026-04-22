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

}
