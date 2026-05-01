package com.ossanasur.cbconnect.module.finance.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ossanasur.cbconnect.common.enums.TypeMotif;
import com.ossanasur.cbconnect.module.finance.entity.ParamMotif;

@Repository
public interface ParamMotifRepository extends JpaRepository<ParamMotif, Integer> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM param_motifs
        WHERE param_motif_tracking_id = :id
          AND active_data = TRUE AND deleted_data = FALSE
        """)
    Optional<ParamMotif> findActiveByTrackingId(@Param("id") UUID id);

    @Query(nativeQuery = true, value = """
        SELECT * FROM param_motifs
        WHERE type = :type
          AND actif = TRUE
          AND active_data = TRUE
          AND deleted_data = FALSE
        ORDER BY libelle_motif ASC
        """)
    List<ParamMotif> findActiveByType(@Param("type") String type);

    @Query(nativeQuery = true,
        value = """
            SELECT * FROM param_motifs
            WHERE active_data = TRUE AND deleted_data = FALSE
            ORDER BY type ASC, libelle_motif ASC
            """,
        countQuery = """
            SELECT COUNT(*) FROM param_motifs
            WHERE active_data = TRUE AND deleted_data = FALSE
            """)
    Page<ParamMotif> findAllActive(Pageable pageable);

    boolean existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalseAndParamMotifTrackingIdNot(
            String libelleMotif, TypeMotif type, UUID excludeId);

    // Variante pour création (pas d'exclusion)
    boolean existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalse(
            String libelleMotif, TypeMotif type);

    @Query(nativeQuery = true, value = """
        SELECT * FROM param_motifs
        WHERE libelle_motif = :libelle
          AND type = :type
          AND actif = TRUE
          AND active_data = TRUE
          AND deleted_data = FALSE
        """)
    Optional<ParamMotif> findActiveByLibelleAndType(
            @Param("libelle") String libelle,
            @Param("type") String type);
}
