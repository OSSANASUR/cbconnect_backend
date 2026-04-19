package com.ossanasur.cbconnect.module.ged.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ossanasur.cbconnect.module.ged.entity.PaperlessDocument;

public interface PaperlessDocumentRepository extends JpaRepository<PaperlessDocument, Integer> {
    Optional<PaperlessDocument> findByPaperlessDocumentTrackingId(UUID trackingId);
}
