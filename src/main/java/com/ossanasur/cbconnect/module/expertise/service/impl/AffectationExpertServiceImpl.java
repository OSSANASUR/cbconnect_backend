// ═══════════════════════════════════════════════════════════════
//  AffectationExpertServiceImpl.java
//  Chemin : module/expertise/service/impl/AffectationExpertServiceImpl.java
// ═══════════════════════════════════════════════════════════════
package com.ossanasur.cbconnect.module.expertise.service.impl;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.expertise.dto.request.AffectationExpertRequest;
import com.ossanasur.cbconnect.module.expertise.dto.response.AffectationExpertResponse;
import com.ossanasur.cbconnect.module.expertise.entity.AffectationExpert;
import com.ossanasur.cbconnect.module.expertise.repository.AffectationExpertRepository;
import com.ossanasur.cbconnect.module.expertise.repository.ExpertRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.module.sinistre.repository.VictimeRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AffectationExpertServiceImpl {

    private final AffectationExpertRepository affectationRepo;
    private final ExpertRepository expertRepo;
    private final VictimeRepository victimeRepo;
    private final SinistreRepository sinistreRepo;
    private final CourrierRepository courrierRepo;

    /**
     * Crée une affectation et génère automatiquement 2 courriers :
     * 1. Note de mission → expert
     * 2. Lettre de prévenance → victime
     */
    @Transactional
    public DataResponse<AffectationExpertResponse> affecter(
            AffectationExpertRequest req, String loginAuteur) {

        var expert = expertRepo.findActiveByTrackingId(req.expertTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Expert introuvable"));
        var victime = victimeRepo.findActiveByTrackingId(req.victimeTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Victime introuvable"));
        var sinistre = sinistreRepo.findActiveByTrackingId(req.sinistreTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Sinistre introuvable"));

        // ── Générer les courriers ──────────────────────────────────
        String refBase = "AFF-" + sinistre.getNumeroSinistreLocal()
                + "-" + req.typeExpertise().name().substring(0, 3)
                + "-" + LocalDate.now();

        // Note de mission → expert
        Courrier mission = Courrier.builder()
                .courrierTrackingId(UUID.randomUUID())
                .referenceCourrier(refBase + "-MISSION")
                .typeCourrier(TypeCourrier.SORTANT)
                .nature(NatureCourrier.TRANSMISSION_EXPERTISE)
                .expediteur("BNCB-TOGO")
                .destinataire(expert.getNomComplet())
                .objet("Note de mission — Expertise " + req.typeExpertise().name()
                        + " — Sinistre n° " + sinistre.getNumeroSinistreLocal())
                .dateCourrier(LocalDate.now())
                .canal(CanalCourrier.PHYSIQUE)
                .sinistre(sinistre)
                .corpsHtml(genererCorpsNoteMission(expert, victime, sinistre,
                        req.typeExpertise(), req.dateLimiteRapport()))
                .envoyeParMail(false)
                .activeData(true).deletedData(false)
                .fromTable(TypeTable.COURRIER).createdBy(loginAuteur)
                .build();
        mission = courrierRepo.save(mission);

        // Lettre de prévenance → victime
        Courrier lettreVictime = Courrier.builder()
                .courrierTrackingId(UUID.randomUUID())
                .referenceCourrier(refBase + "-PREVENANCE")
                .typeCourrier(TypeCourrier.SORTANT)
                .nature(NatureCourrier.AUTRE)
                .expediteur("BNCB-TOGO")
                .destinataire(victime.getPrenoms() + " " + victime.getNom())
                .objet("Convocation expertise " + req.typeExpertise().name()
                        + " — Sinistre n° " + sinistre.getNumeroSinistreLocal())
                .dateCourrier(LocalDate.now())
                .canal(CanalCourrier.PHYSIQUE)
                .sinistre(sinistre)
                .corpsHtml(genererCorpsLettreVictime(expert, victime, sinistre,
                        req.typeExpertise(), req.dateAffectation()))
                .envoyeParMail(false)
                .activeData(true).deletedData(false)
                .fromTable(TypeTable.COURRIER).createdBy(loginAuteur)
                .build();
        lettreVictime = courrierRepo.save(lettreVictime);

        // ── Créer l'affectation ────────────────────────────────────
        AffectationExpert affectation = AffectationExpert.builder()
                .affectationTrackingId(UUID.randomUUID())
                .expert(expert).victime(victime).sinistre(sinistre)
                .typeExpertise(req.typeExpertise())
                .dateAffectation(req.dateAffectation())
                .dateLimiteRapport(req.dateLimiteRapport())
                .statut("EN_ATTENTE")
                .courrierMission(mission)
                .courrierVictime(lettreVictime)
                .observations(req.observations())
                .activeData(true).deletedData(false)
                .fromTable(TypeTable.AFFECTATION_EXPERT).createdBy(loginAuteur)
                .build();

        return DataResponse.created("Affectation créée — courriers générés",
                toResponse(affectationRepo.save(affectation)));
    }

    @Transactional(readOnly = true)
    public DataResponse<List<AffectationExpertResponse>> getBySinistre(UUID sinistreId) {
        return DataResponse.success(
                affectationRepo.findBySinistre(sinistreId).stream()
                        .map(this::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public DataResponse<List<AffectationExpertResponse>> getByVictime(UUID victimeId) {
        return DataResponse.success(
                affectationRepo.findByVictime(victimeId).stream()
                        .map(this::toResponse).toList());
    }

    @Transactional
    public DataResponse<AffectationExpertResponse> mettreAJourStatut(
            UUID affectationId, String statut, String loginAuteur) {
        var aff = affectationRepo.findByTrackingId(affectationId)
                .orElseThrow(() -> new RessourceNotFoundException("Affectation introuvable"));
        aff.setStatut(statut);
        aff.setUpdatedBy(loginAuteur);
        return DataResponse.success("Statut mis à jour", toResponse(affectationRepo.save(aff)));
    }

    @Transactional
    public DataResponse<AffectationExpertResponse> marquerMailExpertEnvoye(
            UUID affectationId, String loginAuteur) {
        var aff = affectationRepo.findByTrackingId(affectationId)
                .orElseThrow(() -> new RessourceNotFoundException("Affectation introuvable"));
        aff.setMailExpertEnvoye(true);
        aff.setUpdatedBy(loginAuteur);
        return DataResponse.success("Mail expert marqué comme envoyé", toResponse(affectationRepo.save(aff)));
    }

    // ── Génération corps courriers (HTML minimal imprimable) ──────

    private String genererCorpsNoteMission(
            com.ossanasur.cbconnect.module.expertise.entity.Expert expert,
            com.ossanasur.cbconnect.module.sinistre.entity.Victime victime,
            com.ossanasur.cbconnect.module.sinistre.entity.Sinistre sinistre,
            TypeExpertise type, LocalDate dateLimite) {

        String limite = dateLimite != null
                ? dateLimite.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "à convenir";

        return """
                <div style="font-family:Arial,sans-serif;max-width:700px;margin:auto;padding:20px">
                  <div style="text-align:center;margin-bottom:30px">
                    <strong style="font-size:18px">BNCB — BUREAU NATIONAL DE LA CARTE BRUNE — TOGO</strong><br/>
                    <span style="font-size:13px">NOTE DE MISSION D'EXPERTISE</span>
                  </div>
                  <p>Lomé, le %s</p>
                  <p><strong>À :</strong> %s</p>
                  <p><strong>Objet :</strong> Mission d'expertise %s — Sinistre n° %s</p>
                  <hr/>
                  <p>Monsieur/Madame,</p>
                  <p>
                    Nous avons l'honneur de vous confier une mission d'expertise <strong>%s</strong>
                    concernant la victime <strong>%s %s</strong>, dans le cadre du sinistre
                    n° <strong>%s</strong> (accident du <strong>%s</strong>).
                  </p>
                  <p>Nous vous prions de bien vouloir procéder à l'examen de la victime et de nous
                    faire parvenir votre rapport au plus tard le <strong>%s</strong>.</p>
                  <p>
                    <strong>Honoraires :</strong> Selon barème CIMA en vigueur.
                    Taux de retenue à la source applicable : <strong>%s%%</strong>.
                  </p>
                  <p>Veuillez agréer, Monsieur/Madame, l'expression de nos salutations distinguées.</p>
                  <br/>
                  <p>Le Directeur Général<br/>BNCB-TOGO</p>
                </div>
                """.formatted(
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                expert.getNomComplet(),
                type.name(),
                sinistre.getNumeroSinistreLocal(),
                type.name(),
                victime.getPrenoms(), victime.getNom(),
                sinistre.getNumeroSinistreLocal(),
                sinistre.getDateAccident().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                limite,
                expert.getTauxRetenue() != null ? expert.getTauxRetenue().getValeur().toPlainString() : "0");
    }

    private String genererCorpsLettreVictime(
            com.ossanasur.cbconnect.module.expertise.entity.Expert expert,
            com.ossanasur.cbconnect.module.sinistre.entity.Victime victime,
            com.ossanasur.cbconnect.module.sinistre.entity.Sinistre sinistre,
            TypeExpertise type, LocalDate dateAffectation) {

        return """
                <div style="font-family:Arial,sans-serif;max-width:700px;margin:auto;padding:20px">
                  <div style="text-align:center;margin-bottom:30px">
                    <strong style="font-size:18px">BNCB — BUREAU NATIONAL DE LA CARTE BRUNE — TOGO</strong><br/>
                    <span style="font-size:13px">CONVOCATION À UNE EXPERTISE</span>
                  </div>
                  <p>Lomé, le %s</p>
                  <p><strong>À :</strong> %s %s</p>
                  <p><strong>Objet :</strong> Convocation expertise médicale — Sinistre n° %s</p>
                  <hr/>
                  <p>Madame/Monsieur,</p>
                  <p>
                    Nous vous informons que dans le cadre du traitement du sinistre
                    n° <strong>%s</strong> (accident du %s), votre dossier nécessite
                    une <strong>expertise %s</strong>.
                  </p>
                  <p>
                    Un expert a été désigné pour procéder à cet examen :
                    <strong>%s</strong>%s.
                    Celui-ci vous contactera directement pour fixer le rendez-vous.
                  </p>
                  <p>
                    Nous vous remercions de bien vouloir vous munir de toutes les pièces médicales
                    (ordonnances, examens, comptes-rendus) lors de cet examen.
                  </p>
                  <p>Veuillez agréer, Madame/Monsieur, l'expression de nos salutations distinguées.</p>
                  <br/>
                  <p>Le Directeur Général<br/>BNCB-TOGO</p>
                </div>
                """.formatted(
                LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                victime.getPrenoms(), victime.getNom(),
                sinistre.getNumeroSinistreLocal(),
                sinistre.getNumeroSinistreLocal(),
                sinistre.getDateAccident().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                type.name(),
                expert.getNomComplet(),
                expert.getSpecialite() != null ? " (" + expert.getSpecialite() + ")" : "");
    }

    private AffectationExpertResponse toResponse(AffectationExpert a) {
        var e = a.getExpert();
        var v = a.getVictime();
        var s = a.getSinistre();
        return new AffectationExpertResponse(
                a.getAffectationTrackingId(),
                e.getExpertTrackingId(), e.getNomComplet(), e.getEmail(), e.getTelephone(),
                e.getTypeExpert().name(),
                e.getMontExpertise(), e.getTauxRetenue(),
                v.getVictimeTrackingId(),
                v.getPrenoms() + " " + v.getNom(),
                null, // victimeEmail à mapper si champ existe
                s.getSinistreTrackingId(), s.getNumeroSinistreLocal(),
                a.getTypeExpertise(), a.getDateAffectation(), a.getDateLimiteRapport(),
                a.getStatut(),
                a.getCourrierMission() != null ? a.getCourrierMission().getCourrierTrackingId() : null,
                a.getCourrierVictime() != null ? a.getCourrierVictime().getCourrierTrackingId() : null,
                a.isMailExpertEnvoye(), a.isMailVictimeEnvoye(),
                a.getObservations());
    }
}
