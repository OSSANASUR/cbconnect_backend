package com.ossanasur.cbconnect.module.pv.repository;
import com.ossanasur.cbconnect.module.pv.entity.PvSinistre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface PvSinistreRepository extends JpaRepository<PvSinistre, Integer> {
    @Query("SELECT p FROM PvSinistre p WHERE p.pvTrackingId=:id AND p.activeData=true AND p.deletedData=false")
    Optional<PvSinistre> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT p FROM PvSinistre p WHERE p.sinistre.sinistreTrackingId=:sid AND p.activeData=true AND p.deletedData=false")
    List<PvSinistre> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT p FROM PvSinistre p WHERE p.sinistre IS NULL AND p.activeData=true AND p.deletedData=false ORDER BY p.dateReceptionBncb DESC")
    List<PvSinistre> findNonAssocies();

    @Query("SELECT p FROM PvSinistre p "
         + "WHERE p.activeData=true AND p.deletedData=false "
         + "  AND (:estComplet IS NULL OR p.estComplet = :estComplet) "
         + "ORDER BY p.dateReceptionBncb DESC")
    Page<PvSinistre> findAllActive(@Param("estComplet") Boolean estComplet, Pageable pageable);

    boolean existsByNumeroPvAndActiveDataTrueAndDeletedDataFalse(String numeroPv);
}
