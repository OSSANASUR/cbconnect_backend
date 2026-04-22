package com.ossanasur.cbconnect.module.ged.repository;

import com.ossanasur.cbconnect.common.enums.TypeDossierOssanGed;
import com.ossanasur.cbconnect.module.ged.entity.OssanGedDossier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OssanGedDossierRepository extends JpaRepository<OssanGedDossier, Integer> {

    @Query("SELECT d FROM OssanGedDossier d WHERE d.sinistre.sinistreTrackingId=:sid AND d.typeDossier='SINISTRE' AND d.activeData=true AND d.deletedData=false")
    Optional<OssanGedDossier> findRootBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT d FROM OssanGedDossier d WHERE d.victime.victimeTrackingId=:vid AND d.typeDossier='VICTIME' AND d.activeData=true AND d.deletedData=false")
    Optional<OssanGedDossier> findByVictime(@Param("vid") UUID victimeId);

    @Query("SELECT d FROM OssanGedDossier d WHERE d.parentDossier.historiqueId=:parentId AND d.typeDossier=:type AND d.titre=:titre AND d.activeData=true AND d.deletedData=false")
    Optional<OssanGedDossier> findByParentAndTypeAndTitre(@Param("parentId") Integer parentId,
                                                          @Param("type") TypeDossierOssanGed type,
                                                          @Param("titre") String titre);
}
