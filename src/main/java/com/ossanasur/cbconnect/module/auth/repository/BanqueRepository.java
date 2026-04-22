package com.ossanasur.cbconnect.module.auth.repository;

import com.ossanasur.cbconnect.module.auth.entity.Banque;
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
public interface BanqueRepository extends JpaRepository<Banque, Integer> {

    @Query("SELECT b FROM Banque b WHERE b.banqueTrackingId = :id AND b.activeData = true AND b.deletedData = false")
    Optional<Banque> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT b FROM Banque b WHERE b.activeData = true AND b.deletedData = false ORDER BY b.nom ASC")
    List<Banque> findAllActive();

    @Query("SELECT b FROM Banque b WHERE b.banqueTrackingId = :id ORDER BY b.createdAt DESC")
    Page<Banque> findHistoryByTrackingId(@Param("id") UUID id, Pageable pageable);

    boolean existsByCodeAndActiveDataTrueAndDeletedDataFalse(String code);
}
