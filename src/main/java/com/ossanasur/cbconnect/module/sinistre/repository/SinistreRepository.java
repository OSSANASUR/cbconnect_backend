package com.ossanasur.cbconnect.module.sinistre.repository;
import com.ossanasur.cbconnect.common.enums.StatutSinistre;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate; import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Integer> {
    @Query("SELECT s FROM Sinistre s WHERE s.sinistreTrackingId=:id AND s.activeData=true AND s.deletedData=false")
    Optional<Sinistre> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT s FROM Sinistre s WHERE s.numeroSinistreLocal=:num AND s.activeData=true AND s.deletedData=false")
    Optional<Sinistre> findByNumeroSinistreLocal(@Param("num") String numero);
    @Query("SELECT s FROM Sinistre s WHERE s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
    Page<Sinistre> findAllActive(Pageable pageable);
    @Query("SELECT s FROM Sinistre s WHERE s.statut=:statut AND s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
    List<Sinistre> findAllByStatut(@Param("statut") StatutSinistre statut);
    @Query("SELECT s FROM Sinistre s WHERE s.redacteur.utilisateurTrackingId=:uid AND s.activeData=true AND s.deletedData=false ORDER BY s.dateDeclaration DESC")
    List<Sinistre> findAllByRedacteur(@Param("uid") UUID utilisateurTrackingId);
    @Query("SELECT s FROM Sinistre s WHERE s.activeData=true AND s.deletedData=false AND s.dateProchaineAudience BETWEEN :debut AND :fin")
    List<Sinistre> findAudiencesBetween(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);
    boolean existsByNumeroSinistreLocalAndActiveDataTrueAndDeletedDataFalse(String numero);
    @Query("SELECT COUNT(s) FROM Sinistre s WHERE YEAR(s.dateDeclaration)=:annee AND s.typeSinistre=:type AND s.activeData=true AND s.deletedData=false")
    long countByAnneeAndType(@Param("annee") int annee, @Param("type") com.ossanasur.cbconnect.common.enums.TypeSinistre type);
}
