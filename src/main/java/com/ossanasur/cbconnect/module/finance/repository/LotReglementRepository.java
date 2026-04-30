package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.common.enums.StatutLotReglement;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
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

    @Query("""
        SELECT l FROM LotReglement l
        WHERE l.lotTrackingId = :id
          AND l.activeData = true AND l.deletedData = false
        """)
    Optional<LotReglement> findActiveByTrackingId(@Param("id") UUID id);

    @Query("""
        SELECT l FROM LotReglement l
        WHERE (:expert IS NULL OR l.expert = :expert)
          AND (:statut IS NULL OR l.statut = :statut)
          AND l.activeData = true AND l.deletedData = false
        ORDER BY l.createdAt DESC
        """)
    Page<LotReglement> findActiveFiltered(
            @Param("expert") Expert expert,
            @Param("statut") StatutLotReglement statut,
            Pageable pageable);
}
