package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.module.finance.entity.LotReglement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LotReglementRepository extends JpaRepository<LotReglement, Integer> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM lot_reglement
        WHERE lot_tracking_id = :id
          AND active_data = TRUE AND deleted_data = FALSE
        """)
    Optional<LotReglement> findActiveByTrackingId(@Param("id") UUID id);

    @Query(nativeQuery = true,
        value = """
            SELECT * FROM lot_reglement
            WHERE (CAST(:expertId AS INTEGER) IS NULL OR expert_id = :expertId)
              AND (CAST(:statut AS VARCHAR) IS NULL OR statut = :statut)
              AND active_data = TRUE AND deleted_data = FALSE
            ORDER BY created_at DESC
            """,
        countQuery = """
            SELECT COUNT(*) FROM lot_reglement
            WHERE (CAST(:expertId AS INTEGER) IS NULL OR expert_id = :expertId)
              AND (CAST(:statut AS VARCHAR) IS NULL OR statut = :statut)
              AND active_data = TRUE AND deleted_data = FALSE
            """)
    Page<LotReglement> findActiveFiltered(
            @Param("expertId") Integer expertId,
            @Param("statut") String statut,
            Pageable pageable);

    @Query(nativeQuery = true, value = """
        SELECT COUNT(*) FROM lot_reglement
        WHERE EXTRACT(YEAR FROM created_at) = :annee
        """)
    long countByYear(@Param("annee") int annee);

    /**
     * Lots distincts contenant au moins un paiement actif sur le sinistre donné.
     */
    @Query(nativeQuery = true, value = """
        SELECT DISTINCT lr.* FROM lot_reglement lr
        JOIN paiement p ON p.lot_reglement_id = lr.historique_id
        JOIN sinistre s ON s.historique_id = p.sinistre_id
        WHERE s.sinistre_tracking_id = :sinistreTrackingId
          AND lr.active_data = TRUE AND lr.deleted_data = FALSE
          AND p.active_data = TRUE AND p.deleted_data = FALSE
        ORDER BY lr.created_at DESC
        """)
    java.util.List<LotReglement> findBySinistreTrackingId(
            @Param("sinistreTrackingId") UUID sinistreTrackingId);
}
