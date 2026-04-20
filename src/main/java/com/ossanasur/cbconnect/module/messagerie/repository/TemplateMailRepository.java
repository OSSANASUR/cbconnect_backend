package com.ossanasur.cbconnect.module.messagerie.repository;

import com.ossanasur.cbconnect.module.messagerie.entity.TemplateMail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateMailRepository extends JpaRepository<TemplateMail, Integer> {

    @Query("SELECT t FROM TemplateMail t WHERE t.actif = true AND t.deletedData = false ORDER BY t.typeTemplate, t.nom")
    List<TemplateMail> findAllActifs();

    Optional<TemplateMail> findByTrackingId(UUID trackingId);
}