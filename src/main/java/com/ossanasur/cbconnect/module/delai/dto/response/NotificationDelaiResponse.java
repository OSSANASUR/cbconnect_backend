package com.ossanasur.cbconnect.module.delai.dto.response;
import com.ossanasur.cbconnect.common.enums.*;
import java.time.LocalDate;
public record NotificationDelaiResponse(
    Integer id, String codeDelai, String libelleDelai, String referenceJuridique,
    String sinistreNumeroLocal, String victimeNomPrenom, String responsableNomPrenom,
    LocalDate dateDebut, LocalDate dateEcheance, long joursRestants,
    StatutNotificationDelai statut, NiveauAlerteDelai niveauAlerte, Integer nombreAlertes
) {}
