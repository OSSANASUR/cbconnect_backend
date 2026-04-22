package com.ossanasur.cbconnect.module.attestation.service;
import java.util.UUID;
public interface FacturePdfService {
    byte[] genererPdf(UUID factureTrackingId);
    String nomFichier(UUID factureTrackingId);
}
