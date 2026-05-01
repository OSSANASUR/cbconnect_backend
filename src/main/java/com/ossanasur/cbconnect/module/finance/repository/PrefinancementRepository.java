package com.ossanasur.cbconnect.module.finance.repository;
import com.ossanasur.cbconnect.module.finance.entity.Prefinancement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional; import java.util.UUID;
@Repository
public interface PrefinancementRepository extends JpaRepository<Prefinancement, Integer> {
    @Query("SELECT p FROM Prefinancement p WHERE p.sinistre.sinistreTrackingId=:sid AND p.activeData=true AND p.deletedData=false")
    Optional<Prefinancement> findBySinistre(@Param("sid") UUID sinistreId);

    /**
     * Compteur global par préfixe (PF-YYYY-NUMSIN-). Pas de filtre sinistre_id
     * (cf. spec 2026-04-28 option A — SEQ partagée par num_sin).
     */
    @Query("SELECT COUNT(p) FROM Prefinancement p " +
           "WHERE p.numeroPrefinancement LIKE :prefixPattern")
    long countSeqForPrefinancement(@Param("prefixPattern") String prefixPattern);

    @Query("SELECT p FROM Prefinancement p WHERE p.prefinancementTrackingId = :id " +
           "AND p.activeData = true AND p.deletedData = false")
    java.util.Optional<Prefinancement> findActiveByTrackingId(@Param("id") UUID id);

    /** Liste des préfinancements actifs d'un sinistre, triés date demande desc. */
    @Query("SELECT p FROM Prefinancement p " +
           "WHERE p.sinistre.sinistreTrackingId = :sid " +
           "AND p.activeData = true AND p.deletedData = false " +
           "ORDER BY p.datePrefinancement DESC, p.historiqueId DESC")
    List<Prefinancement> findActiveBySinistre(@Param("sid") UUID sinistreTrackingId);

    /**
     * Vrai si au moins un préfinancement actif (statut VALIDE ou REMBOURSE_PARTIEL)
     * existe sur ce sinistre. Utilisé par EncaissementGuardService.regleA étendue.
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Prefinancement p " +
           "WHERE p.sinistre.sinistreTrackingId = :sid " +
           "AND p.statut IN (com.ossanasur.cbconnect.common.enums.StatutPrefinancement.VALIDE, " +
           "                 com.ossanasur.cbconnect.common.enums.StatutPrefinancement.REMBOURSE_PARTIEL) " +
           "AND p.activeData = true AND p.deletedData = false")
    boolean existsActifBySinistre(@Param("sid") UUID sinistreTrackingId);

    /** Préfinancements non totalement remboursés sur un sinistre (= candidats à imputation). */
    @Query("SELECT p FROM Prefinancement p " +
           "WHERE p.sinistre.sinistreTrackingId = :sid " +
           "AND p.statut IN (com.ossanasur.cbconnect.common.enums.StatutPrefinancement.VALIDE, " +
           "                 com.ossanasur.cbconnect.common.enums.StatutPrefinancement.REMBOURSE_PARTIEL) " +
           "AND p.activeData = true AND p.deletedData = false " +
           "ORDER BY p.dateValidation ASC")
    List<Prefinancement> findRembourssablesBySinistre(@Param("sid") UUID sinistreTrackingId);

    /** Total préfinancé (montantPrefinance) actif pour un sinistre. */
    @Query("SELECT COALESCE(SUM(p.montantPrefinance), 0) FROM Prefinancement p " +
           "WHERE p.sinistre.sinistreTrackingId = :sid " +
           "AND p.statut <> com.ossanasur.cbconnect.common.enums.StatutPrefinancement.ANNULE " +
           "AND p.activeData = true AND p.deletedData = false")
    BigDecimal sumMontantActifBySinistre(@Param("sid") UUID sinistreTrackingId);
}
