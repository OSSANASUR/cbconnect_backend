package com.ossanasur.cbconnect.module.expertise.repository;

import com.ossanasur.cbconnect.module.expertise.entity.AffectationExpert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AffectationExpertRepository extends JpaRepository<AffectationExpert, Integer> {

    @Query("SELECT a FROM AffectationExpert a WHERE a.affectationTrackingId=:id AND a.activeData=true AND a.deletedData=false")
    Optional<AffectationExpert> findByTrackingId(@Param("id") UUID id);

    @Query("SELECT a FROM AffectationExpert a WHERE a.sinistre.sinistreTrackingId=:sid AND a.activeData=true AND a.deletedData=false ORDER BY a.dateAffectation DESC")
    List<AffectationExpert> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT a FROM AffectationExpert a WHERE a.victime.victimeTrackingId=:vid AND a.activeData=true AND a.deletedData=false ORDER BY a.dateAffectation DESC")
    List<AffectationExpert> findByVictime(@Param("vid") UUID victimeId);

    @Query("SELECT a FROM AffectationExpert a WHERE a.statut='EN_ATTENTE' AND a.dateLimiteRapport < CURRENT_DATE AND a.activeData=true AND a.deletedData=false")
    List<AffectationExpert> findEnRetard();

    @Query(nativeQuery = true, value = """
        SELECT COUNT(*) > 0 FROM affectation_expert a
        JOIN expert e ON e.historique_id = a.expert_id
        JOIN sinistre s ON s.historique_id = a.sinistre_id
        WHERE e.expert_tracking_id = :expertTrackingId
          AND s.sinistre_tracking_id = :sinistreTrackingId
          AND a.active_data = TRUE
          AND a.deleted_data = FALSE
        """)
    boolean existsActiveByExpertAndSinistre(
            @Param("expertTrackingId") UUID expertTrackingId,
            @Param("sinistreTrackingId") UUID sinistreTrackingId);
}