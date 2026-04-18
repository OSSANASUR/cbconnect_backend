package com.ossanasur.cbconnect.module.auth.repository;

import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganismeRepository extends JpaRepository<Organisme, Integer> {
    @Query("SELECT o FROM Organisme o WHERE o.organismeTrackingId = :id AND o.activeData = true AND o.deletedData = false")
    Optional<Organisme> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT o FROM Organisme o WHERE o.activeData = true AND o.deletedData = false ORDER BY o.createdAt DESC")
    List<Organisme> findAllActive();

    @Query("SELECT o FROM Organisme o WHERE o.activeData = true AND o.deletedData = false AND o.typeOrganisme = :type ORDER BY o.createdAt DESC")
    List<Organisme> findAllActiveByType(@Param("type") com.ossanasur.cbconnect.common.enums.TypeOrganisme type);

    @Query("SELECT o FROM Organisme o WHERE o.organismeTrackingId = :id ORDER BY o.createdAt DESC")
    Page<Organisme> findHistoryByTrackingId(@Param("id") UUID id, Pageable pageable);

    boolean existsByCodeAndActiveDataTrueAndDeletedDataFalse(String code);

    boolean existsByEmailAndActiveDataTrueAndDeletedDataFalse(String email);

    boolean existsByCode(String code);

    long countByRepriseHistorique(boolean reprise);

    Optional<Organisme> findByCodeIgnoreCase(String code);

    // Recherche floue par raison sociale — fallback utilisé par la reprise
    // historique lorsque le code assureur (ex: "SUNU BJ") ne matche aucun code
    // exact. `findFirst` car plusieurs organismes peuvent matcher (LIMIT 1).
    Optional<Organisme> findFirstByRaisonSocialeContainingIgnoreCase(String raisonSociale);
}
