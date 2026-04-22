package com.ossanasur.cbconnect.module.ged.repository;

import com.ossanasur.cbconnect.module.ged.entity.OssanGedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OssanGedDocumentRepository extends JpaRepository<OssanGedDocument, Integer> {

    Optional<OssanGedDocument> findByOssanGedDocumentTrackingId(UUID trackingId);

    @Query("SELECT d FROM OssanGedDocument d WHERE d.sinistre.sinistreTrackingId=:sid AND d.activeData=true AND d.deletedData=false ORDER BY d.dateDocument DESC NULLS LAST")
    List<OssanGedDocument> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT d FROM OssanGedDocument d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false ORDER BY d.dateDocument DESC NULLS LAST")
    List<OssanGedDocument> findByVictime(@Param("vid") UUID victimeId);
}
