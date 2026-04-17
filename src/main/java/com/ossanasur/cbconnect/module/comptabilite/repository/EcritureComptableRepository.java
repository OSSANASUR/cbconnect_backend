package com.ossanasur.cbconnect.module.comptabilite.repository;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import com.ossanasur.cbconnect.module.comptabilite.entity.EcritureComptable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate; import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface EcritureComptableRepository extends JpaRepository<EcritureComptable, Integer> {
    Optional<EcritureComptable> findByEcritureTrackingId(UUID id);
    @Query("SELECT e FROM EcritureComptable e WHERE e.sinistre.sinistreTrackingId=:sid ORDER BY e.dateEcriture DESC")
    List<EcritureComptable> findBySinistre(@Param("sid") UUID sinistreId);
    @Query("SELECT e FROM EcritureComptable e WHERE e.dateEcriture BETWEEN :debut AND :fin ORDER BY e.dateEcriture DESC")
    Page<EcritureComptable> findByPeriode(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin, Pageable pageable);
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.numeroEcriture, 9) AS int)),0) FROM EcritureComptable e WHERE e.numeroEcriture LIKE CONCAT('ECR-', :annee, '-%')")
    long findMaxSeq(@Param("annee") int annee);
}
