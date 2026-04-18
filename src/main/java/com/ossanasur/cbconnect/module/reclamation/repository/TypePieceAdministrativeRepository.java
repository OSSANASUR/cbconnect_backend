package com.ossanasur.cbconnect.module.reclamation.repository;

import com.ossanasur.cbconnect.common.enums.TypeDommage;
import com.ossanasur.cbconnect.module.reclamation.entity.TypePieceAdministrative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TypePieceAdministrativeRepository extends JpaRepository<TypePieceAdministrative, Integer> {

    @Query("SELECT t FROM TypePieceAdministrative t WHERE t.actif = true AND t.deletedData = false ORDER BY t.ordre, t.libelle")
    List<TypePieceAdministrative> findAllActif();

    /**
     * Pièces applicables pour un TypeDommage donné :
     * - pièces COMMUNES (typeDommage IS NULL)
     * - pièces spécifiques au typeDommage passé
     * Triées par ordre puis libellé.
     */
    @Query("SELECT t FROM TypePieceAdministrative t " +
            "WHERE t.actif = true AND t.deletedData = false " +
            "AND (t.typeDommage IS NULL OR t.typeDommage = :td) " +
            "ORDER BY CASE WHEN t.typeDommage IS NULL THEN 0 ELSE 1 END, t.ordre, t.libelle")
    List<TypePieceAdministrative> findApplicablesPour(@Param("td") TypeDommage typeDommage);

    Optional<TypePieceAdministrative> findByTrackingId(UUID trackingId);
}