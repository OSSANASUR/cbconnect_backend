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

    @Query("SELECT p FROM ParamMotif p WHERE p.paramMotifTrackingId = :id AND p.activeData = true AND p.deletedData = false")
    Optional<ParamMotif> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT p FROM ParamMotif p WHERE p.type = :type AND p.actif = true AND p.activeData = true AND p.deletedData = false ORDER BY p.libelleMotif ASC")
    List<ParamMotif> findActiveByType(@Param("type") TypeMotif type);

    @Query("SELECT p FROM ParamMotif p WHERE p.activeData = true AND p.deletedData = false ORDER BY p.type ASC, p.libelleMotif ASC")
    Page<ParamMotif> findAllActive(Pageable pageable);

    boolean existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalseAndParamMotifTrackingIdNot(
            String libelleMotif, TypeMotif type, UUID excludeId);

    // Variante pour création (pas d'exclusion)
    boolean existsByLibelleMotifAndTypeAndActiveDataTrueAndDeletedDataFalse(
            String libelleMotif, TypeMotif type);

    @Query("""
        SELECT p FROM ParamMotif p
        WHERE p.libelleMotif = :libelle
          AND p.type = :type
          AND p.actif = true
          AND p.activeData = true
          AND p.deletedData = false
        """)
    Optional<ParamMotif> findActiveByLibelleAndType(
            @Param("libelle") String libelle,
            @Param("type") TypeMotif type);
}
