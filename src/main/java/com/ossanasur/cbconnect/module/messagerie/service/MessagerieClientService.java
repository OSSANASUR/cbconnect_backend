package com.ossanasur.cbconnect.module.messagerie.service;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.messagerie.dto.request.EnvoyerMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.PreviewTemplateRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageApercu;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageComplet;
import com.ossanasur.cbconnect.module.messagerie.dto.response.PreviewTemplateResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.TemplateMailResponse;
import com.ossanasur.cbconnect.module.messagerie.entity.ConfigurationMail;
import com.ossanasur.cbconnect.module.messagerie.entity.TemplateMail;
import com.ossanasur.cbconnect.module.messagerie.repository.ConfigurationMailRepository;
import com.ossanasur.cbconnect.module.messagerie.repository.TemplateMailRepository;
import com.ossanasur.cbconnect.module.sinistre.entity.Sinistre;
import com.ossanasur.cbconnect.module.sinistre.repository.SinistreRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessagerieClientService {

    private final ConfigurationMailRepository configRepo;
    private final TemplateMailRepository templateRepo;
    private final CourrierRepository courrierRepo;
    private final SinistreRepository sinistreRepo;
    private final UtilisateurRepository utilisateurRepo;

    private static final String ENV_KEY = System.getenv()
            .getOrDefault("MAIL_ENCRYPTION_KEY", "CBConnect2025SecretKey!AES256!!");
    private static final int MAX_MESSAGES = 50;

    // ─── Boîte de réception (IMAP) ──────────────────────────────

    @Transactional(readOnly = true)
    public DataResponse<List<MessageApercu>> getInbox(String loginAuteur) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        if (!config.isEstConfiguree())
            throw new IllegalStateException("Messagerie non configurée");

        List<MessageApercu> messages = new ArrayList<>();
        try {
            Store store = connectImap(config);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] msgs = inbox.getMessages();
            int start = Math.max(0, msgs.length - MAX_MESSAGES);
            for (int i = msgs.length - 1; i >= start; i--) {
                Message m = msgs[i];
                messages.add(toApercu(m, false));
            }
            inbox.close(false);
            store.close();
        } catch (Exception e) {
            log.warn("[MESSAGERIE] Erreur lecture inbox : {}", e.getMessage());
            return DataResponse.success("Inbox (erreur connexion)", messages);
        }
        return DataResponse.success("Inbox", messages);
    }

    // ─── Boîte d'envoi (IMAP SENT / courriers CBConnect) ────────

    @Transactional(readOnly = true)
    public DataResponse<List<MessageApercu>> getSent(String loginAuteur) {
        // On combine : dossier IMAP Sent + courriers CBConnect envoyés par mail
        // Utilisateur u = getUtilisateur(loginAuteur);
        List<MessageApercu> messages = new ArrayList<>();

        // Courriers enregistrés en base (envoyés via CBConnect)
        courrierRepo.findSortantsEnvoyesParMail().forEach(c -> messages.add(courrierToApercu(c)));

        // Trier par date décroissante
        messages.sort(Comparator.comparing(MessageApercu::date, Comparator.nullsLast(Comparator.reverseOrder())));
        return DataResponse.success("Envoyés", messages);
    }

    // ─── Lire un message complet ─────────────────────────────────

    @Transactional(readOnly = true)
    public DataResponse<MessageComplet> getMessage(UUID courrierTrackingId, String loginAuteur) {
        Courrier c = courrierRepo.findActiveByTrackingId(courrierTrackingId)
                .orElseThrow(() -> new RessourceNotFoundException("Message introuvable"));
        return DataResponse.success("Message", courrierToComplet(c));
    }

    // ─── Envoyer un mail ─────────────────────────────────────────

    public DataResponse<UUID> envoyer(EnvoyerMailRequest req, String loginAuteur) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        // Utilisateur u = getUtilisateur(loginAuteur);

        // 1. Résolution sinistre
        Sinistre sinistre = req.sinistreTrackingId() != null
                ? sinistreRepo.findActiveByTrackingId(req.sinistreTrackingId()).orElse(null)
                : null;

        // 2. Corps final avec signature
        String corpsAvecSignature = appendSignature(req.corpsHtml(), config);

        // 3. Envoi SMTP
        String messageId = null;
        StatutMailEnvoye statut = StatutMailEnvoye.EN_ATTENTE;
        try {
            messageId = envoyerSmtp(config, req.destinataire(), req.cc(),
                    req.sujet(), corpsAvecSignature);
            statut = StatutMailEnvoye.ENVOYE;
            log.info("[MESSAGERIE] Mail envoyé à {} — sujet: {}", req.destinataire(), req.sujet());
        } catch (Exception e) {
            statut = StatutMailEnvoye.ECHEC;
            log.error("[MESSAGERIE] Échec envoi : {}", e.getMessage());
            throw new RuntimeException("Échec envoi mail : " + simplifierErreur(e.getMessage()));
        }

        // 4. Enregistrement en base comme Courrier (traçabilité)
        TemplateMail template = req.templateTrackingId() != null
                ? templateRepo.findByTrackingId(req.templateTrackingId()).orElse(null)
                : null;

        long seq = courrierRepo.count() + 1;
        String ref = "LKS/" + LocalDate.now().getYear() + "/" + String.format("%05d", seq);

        Courrier courrier = Courrier.builder()
                .courrierTrackingId(UUID.randomUUID())
                .referenceCourrier(ref)
                .typeCourrier(TypeCourrier.SORTANT)
                .nature(req.nature() != null ? req.nature() : NatureCourrier.AUTRE)
                .expediteur(config.getEmailExpediteur())
                .destinataire(req.destinataire())
                .objet(req.sujet())
                .dateCourrier(LocalDate.now())
                .canal(CanalCourrier.MAIL)
                .corpsHtml(corpsAvecSignature)
                .messageIdMail(messageId)
                .envoyeParMail(true)
                .statutEnvoi(statut)
                .dateEnvoi(LocalDateTime.now())
                .template(template)
                .sinistre(sinistre)
                .traite(true)
                .dateTraitement(LocalDateTime.now())
                .createdBy(loginAuteur)
                .activeData(true).deletedData(false)
                .build();

        Courrier saved = courrierRepo.save(courrier);
        return DataResponse.success("Mail envoyé", saved.getCourrierTrackingId());
    }

    // ─── Templates ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DataResponse<List<TemplateMailResponse>> getTemplates() {
        return DataResponse.success("Templates",
                templateRepo.findAllActifs().stream().map(this::toTemplateResponse).toList());
    }

    @Transactional(readOnly = true)
    public DataResponse<PreviewTemplateResponse> previewTemplate(PreviewTemplateRequest req) {
        TemplateMail t = templateRepo.findByTrackingId(req.templateTrackingId())
                .orElseThrow(() -> new RessourceNotFoundException("Template introuvable"));

        // Substitution des variables
        Map<String, String> vars = new HashMap<>(req.variablesSupp() != null ? req.variablesSupp() : Map.of());

        if (req.sinistreTrackingId() != null) {
            sinistreRepo.findActiveByTrackingId(req.sinistreTrackingId()).ifPresent(s -> {
                vars.put("numero_sinistre", s.getNumeroSinistreLocal());
                vars.put("date_accident", s.getDateAccident() != null ? s.getDateAccident().toString() : "");
                vars.put("lieu_accident", s.getLieuAccident() != null ? s.getLieuAccident() : "");
                vars.put("assure_nom", s.getAssure() != null ? s.getAssure().getNomComplet() : "");
                vars.put("organisme_homologue",
                        s.getOrganismeHomologue() != null ? s.getOrganismeHomologue().getRaisonSociale() : "");
            });
        }

        String sujetRendu = substituer(t.getSujet(), vars);
        String corpsRendu = substituer(t.getCorpsHtml(), vars);

        return DataResponse.success("Preview", new PreviewTemplateResponse(sujetRendu, corpsRendu));
    }

    // ─── Helpers privés ──────────────────────────────────────────

    private String substituer(String template, Map<String, String> vars) {
        if (template == null)
            return "";
        String result = template;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            result = result.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
        }
        return result;
    }

    private String appendSignature(String corps, ConfigurationMail config) {
        if (config.getSignature() == null || config.getSignature().isBlank())
            return corps;
        return corps + "<br><br><hr style='border:none;border-top:1px solid #e2e8f0;margin:16px 0'>"
                + config.getSignature();
    }

    private String envoyerSmtp(ConfigurationMail config, String to, String cc,
            String sujet, String corpsHtml) throws Exception {
        String mdp = dechiffrer(config.getSmtpPasswordEnc());
        Properties props = buildSmtpProps(config);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUsername(), mdp);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new jakarta.mail.internet.InternetAddress(
                config.getEmailExpediteur(), config.getNomAffiche()));
        msg.addRecipients(Message.RecipientType.TO,
                jakarta.mail.internet.InternetAddress.parse(to));
        if (cc != null && !cc.isBlank())
            msg.addRecipients(Message.RecipientType.CC,
                    jakarta.mail.internet.InternetAddress.parse(cc));
        msg.setSubject(sujet, "UTF-8");
        msg.setContent(corpsHtml, "text/html; charset=UTF-8");
        msg.saveChanges();

        Transport.send(msg);
        return msg.getMessageID();
    }

    private Properties buildSmtpProps(ConfigurationMail config) {
        Properties p = new Properties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.host", config.getSmtpHost());
        p.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        p.put("mail.smtp.connectiontimeout", "10000");
        p.put("mail.smtp.timeout", "10000");
        if (config.getSmtpSecurite() == SecuriteMailProtocole.SSL) {
            p.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            p.put("mail.smtp.socketFactory.port", String.valueOf(config.getSmtpPort()));
        } else if (config.getSmtpSecurite() == SecuriteMailProtocole.STARTTLS) {
            p.put("mail.smtp.starttls.enable", "true");
        }
        return p;
    }

    private Store connectImap(ConfigurationMail config) throws Exception {
        String mdp = dechiffrer(config.getSmtpPasswordEnc());
        Properties p = new Properties();
        String proto = config.getImapSecurite() == SecuriteMailProtocole.SSL ? "imaps" : "imap";
        p.put("mail." + proto + ".host", config.getImapHost());
        p.put("mail." + proto + ".port", String.valueOf(config.getImapPort()));
        p.put("mail." + proto + ".connectiontimeout", "10000");
        if (config.getImapSecurite() == SecuriteMailProtocole.STARTTLS)
            p.put("mail.imap.starttls.enable", "true");

        Session session = Session.getInstance(p);
        Store store = session.getStore(proto);
        store.connect(config.getImapHost(), config.getImapPort(),
                config.getSmtpUsername(), mdp);
        return store;
    }

    private MessageApercu toApercu(Message m, boolean sent) {
        try {
            String from = m.getFrom() != null && m.getFrom().length > 0
                    ? m.getFrom()[0].toString()
                    : "";
            String to = m.getRecipients(Message.RecipientType.TO) != null
                    ? m.getRecipients(Message.RecipientType.TO)[0].toString()
                    : "";
            String sujet = m.getSubject() != null ? m.getSubject() : "(sans sujet)";
            LocalDateTime date = m.getSentDate() != null
                    ? m.getSentDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now();
            boolean lu = m.isSet(Flags.Flag.SEEN);
            return new MessageApercu(null, String.valueOf(((UIDFolder) m.getFolder()).getUID(m)),
                    from, to, sujet, sujet.substring(0, Math.min(sujet.length(), 100)),
                    date, lu, false, null, null, StatutMailEnvoye.ENVOYE, null);
        } catch (Exception e) {
            return null;
        }
    }

    private MessageApercu courrierToApercu(Courrier c) {
        return new MessageApercu(c.getCourrierTrackingId(), c.getMessageIdMail(),
                c.getExpediteur(), c.getDestinataire(), c.getObjet(),
                c.getCorpsHtml() != null
                        ? c.getCorpsHtml().replaceAll("<[^>]+>", "").substring(0, Math.min(
                                c.getCorpsHtml().replaceAll("<[^>]+>", "").length(), 120))
                        : c.getObjet(),
                c.getDateEnvoi(), true, c.isTraite(),
                c.getSinistre() != null ? c.getSinistre().getNumeroSinistreLocal() : null,
                c.getSinistre() != null ? c.getSinistre().getSinistreTrackingId() : null,
                c.getStatutEnvoi(),
                c.getTemplate() != null ? c.getTemplate().getTypeTemplate() : null);
    }

    private MessageComplet courrierToComplet(Courrier c) {
        return new MessageComplet(c.getCourrierTrackingId(), c.getMessageIdMail(),
                c.getExpediteur(), List.of(c.getDestinataire()), List.of(),
                c.getObjet(), c.getCorpsHtml(), null,
                c.getDateEnvoi(), true, c.isTraite(),
                c.getSinistre() != null ? c.getSinistre().getNumeroSinistreLocal() : null,
                c.getSinistre() != null ? c.getSinistre().getSinistreTrackingId() : null,
                c.getTemplate() != null ? c.getTemplate().getTypeTemplate() : null);
    }

    private TemplateMailResponse toTemplateResponse(TemplateMail t) {
        return new TemplateMailResponse(t.getTrackingId(), t.getTypeTemplate(),
                t.getNom(), t.getDescription(), t.getSujet(), t.getCorpsHtml(), t.isActif());
    }

    private ConfigurationMail getConfigOuException(String login) {
        return configRepo.findByUtilisateur(getUtilisateur(login))
                .orElseThrow(() -> new IllegalStateException(
                        "Messagerie non configurée — configurez d'abord votre boîte mail"));
    }

    private Utilisateur getUtilisateur(String login) {
        return utilisateurRepo.findByUsernameAndActiveDataTrueAndDeletedDataFalse(login)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
    }

    private String dechiffrer(String enc) {
        try {
            byte[] key = Arrays.copyOf(ENV_KEY.getBytes(StandardCharsets.UTF_8), 32);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return new String(cipher.doFinal(Base64.getDecoder().decode(enc)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erreur déchiffrement mot de passe");
        }
    }

    private String simplifierErreur(String msg) {
        if (msg == null)
            return "Erreur inconnue";
        if (msg.contains("Authentication"))
            return "Identifiants incorrects";
        if (msg.contains("refused") || msg.contains("connect"))
            return "Serveur inaccessible";
        return msg.length() > 80 ? msg.substring(0, 80) + "…" : msg;
    }
}
