package com.ossanasur.cbconnect.module.reclamation.repository;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal; import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface DossierReclamationRepository extends JpaRepository<DossierReclamation, Integer> {
    @Query("SELECT d FROM DossierReclamation d WHERE d.dossierTrackingId=:id AND d.activeData=true AND d.deletedData=false")
    Optional<DossierReclamation> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT d FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false ORDER BY d.dateOuverture DESC")
    List<DossierReclamation> findByVictime(@Param("vid") UUID victimeId);

    @Query("SELECT COALESCE(SUM(d.montantTotalRetenu),0) FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false")
    Optional<BigDecimal> findMontantRetenuByVictime(@Param("vid") UUID victimeId);

    /**
     * Liste paginée de tous les dossiers actifs avec filtres optionnels.
     * @param statut  null = tous statuts
     * @param search  recherche insensible à la casse sur : numeroDossier, numéro sinistre, nom victime
     */
    @Query("SELECT d FROM DossierReclamation d "
            + "LEFT JOIN d.sinistre s LEFT JOIN d.victime v "
            + "WHERE d.activeData=true AND d.deletedData=false "
            + "AND (:statut IS NULL OR d.statut = :statut) "
            + "AND (:search IS NULL OR :search = '' "
            + "     OR LOWER(d.numeroDossier) LIKE LOWER(CONCAT('%',:search,'%')) "
            + "     OR LOWER(s.numeroSinistreLocal) LIKE LOWER(CONCAT('%',:search,'%')) "
            + "     OR LOWER(v.nom) LIKE LOWER(CONCAT('%',:search,'%')) "
            + "     OR LOWER(v.prenoms) LIKE LOWER(CONCAT('%',:search,'%'))) "
            + "ORDER BY d.dateOuverture DESC, d.historiqueId DESC")
    Page<DossierReclamation> search(@Param("statut") StatutDossierReclamation statut,
                                    @Param("search") String search,
                                    Pageable pageable);
}
