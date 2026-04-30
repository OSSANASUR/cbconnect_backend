package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.module.finance.entity.PrefinancementRemboursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrefinancementRemboursementRepository
        extends JpaRepository<PrefinancementRemboursement, Integer> {

    @Query("SELECT r FROM PrefinancementRemboursement r " +
           "WHERE r.remboursementTrackingId = :id " +
           "AND r.activeData = true AND r.deletedData = false")
    Optional<PrefinancementRemboursement> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT r FROM PrefinancementRemboursement r " +
           "WHERE r.prefinancement.prefinancementTrackingId = :pid " +
           "AND r.activeData = true AND r.deletedData = false " +
           "ORDER BY r.dateRemboursement ASC")
    List<PrefinancementRemboursement> findByPrefinancement(@Param("pid") UUID prefinancementTrackingId);

    /** Somme des remboursements actifs imputés à un préfinancement (par historique_id). */
    @Query("SELECT COALESCE(SUM(r.montant), 0) FROM PrefinancementRemboursement r " +
           "WHERE r.prefinancement.historiqueId = :prefinId " +
           "AND r.activeData = true AND r.deletedData = false")
    BigDecimal sumMontantByPrefinancement(@Param("prefinId") Integer prefinancementHistoriqueId);

    /** Somme des imputations actives sur un encaissement (= consommation déjà appliquée). */
    @Query("SELECT COALESCE(SUM(r.montant), 0) FROM PrefinancementRemboursement r " +
           "WHERE r.encaissementSource.historiqueId = :encId " +
           "AND r.activeData = true AND r.deletedData = false")
    BigDecimal sumMontantByEncaissement(@Param("encId") Integer encaissementHistoriqueId);
}
