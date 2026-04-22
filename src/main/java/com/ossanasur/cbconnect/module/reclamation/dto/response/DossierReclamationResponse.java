package com.ossanasur.cbconnect.module.reclamation.dto.response;
import com.ossanasur.cbconnect.common.enums.StatutDossierReclamation;
import com.ossanasur.cbconnect.common.enums.TypeDommage;
import com.ossanasur.cbconnect.common.enums.TypeVictime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DossierReclamationResponse(
        UUID dossierTrackingId,
        String numeroDossier,
        LocalDate dateOuverture,
        LocalDate dateCloture,
        StatutDossierReclamation statut,
        BigDecimal montantTotalReclame,
        BigDecimal montantTotalRetenu,
        String notesRedacteur,
        // Sinistre
        UUID sinistreTrackingId,
        String numeroSinistreLocal,
        TypeDommage typeDommage,
        LocalDate dateAccident,
        // Victime
        UUID victimeTrackingId,
        String victimeNomComplet,
        TypeVictime victimeType,
        LocalDate victimeDateNaissance,
        // Rédacteur
        String redacteurNomComplet,
        // Compteurs pièces
        Long nbPiecesRequises,
        Long nbPiecesRecues,
        Long nbPiecesAttendues,
        Long nbPiecesRejetees,
        Boolean estMur,
        // Compteurs factures
        Integer nbFactures,
        Integer nbPiecesAdmin,
        // Factures (null pour la liste, renseigné pour le détail)
        List<FactureReclamationResponse> factures
) {}
