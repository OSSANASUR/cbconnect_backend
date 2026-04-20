package com.ossanasur.cbconnect.module.messagerie.service;

import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import com.ossanasur.cbconnect.module.courrier.repository.CourrierRepository;
import com.ossanasur.cbconnect.module.messagerie.dto.request.EnvoyerMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.PreviewTemplateRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.CorpsEtPj;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageApercu;
import com.ossanasur.cbconnect.module.messagerie.dto.response.MessageComplet;
import com.ossanasur.cbconnect.module.messagerie.dto.response.PieceJointe;
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
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.io.ByteArrayOutputStream;

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
    private static final int PAGE_SIZE = 25;

    // ─── Boîte de réception (IMAP) ──────────────────────────────

    /**
     * @param page      page 0-based (0 = les plus récents)
     * @param recherche filtre texte sur sujet/expéditeur (null = pas de filtre)
     */
    public DataResponse<InboxPageResponse> getInbox(String loginAuteur, int page, String recherche) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        if (!config.isEstConfiguree())
            throw new IllegalStateException("Messagerie non configurée");

        List<MessageApercu> messages = new ArrayList<>();
        int totalMessages = 0;
        int nonLus = 0;

        try {
            Store store = connectImap(config);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            totalMessages = inbox.getMessageCount();
            nonLus = inbox.getUnreadMessageCount();

            // ── FetchProfile : headers uniquement — évite de dl les corps ──
            // Réduit le temps de réponse de ~25s à ~2s
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE); // From, To, Subject, Date
            fp.add(FetchProfile.Item.FLAGS); // SEEN, ANSWERED, etc.

            if (recherche != null && !recherche.isBlank()) {
                // Recherche côté serveur IMAP
                jakarta.mail.search.SearchTerm term = new jakarta.mail.search.OrTerm(
                        new jakarta.mail.search.SubjectTerm(recherche),
                        new jakarta.mail.search.FromStringTerm(recherche));
                Message[] found = inbox.search(term);
                inbox.fetch(found, fp);
                Arrays.sort(found, (a, b) -> {
                    try {
                        return b.getSentDate().compareTo(a.getSentDate());
                    } catch (Exception e) {
                        return 0;
                    }
                });
                for (Message m : found) {
                    MessageApercu a = toApercu(m);
                    if (a != null)
                        messages.add(a);
                }
            } else {
                // Pagination : messages IMAP indexés 1..N, le + récent = N
                int end = totalMessages - (page * PAGE_SIZE);
                int start = Math.max(1, end - PAGE_SIZE + 1);
                if (end >= 1) {
                    Message[] msgs = inbox.getMessages(start, end);
                    inbox.fetch(msgs, fp);
                    for (int i = msgs.length - 1; i >= 0; i--) {
                        MessageApercu a = toApercu(msgs[i]);
                        if (a != null)
                            messages.add(a);
                    }
                }
            }
            inbox.close(false);
            store.close();

        } catch (Exception e) {
            log.warn("[MESSAGERIE] Erreur lecture inbox : {}", e.getMessage());
        }

        int totalPages = totalMessages == 0 ? 1 : (int) Math.ceil((double) totalMessages / PAGE_SIZE);
        return DataResponse.success("Inbox",
                new InboxPageResponse(messages, page, totalPages, totalMessages, nonLus));
    }

    /** Réponse paginée inbox */
    public record InboxPageResponse(
            List<MessageApercu> messages,
            int page, int totalPages,
            int totalMessages, int nonLus) {
    }

    /** Nombre de messages non lus — léger, pour le badge sidebar */
    public DataResponse<Integer> getNonLus(String loginAuteur) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        if (!config.isEstConfiguree())
            return DataResponse.success("Non lus", 0);
        try {
            Store store = connectImap(config);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            int count = inbox.getUnreadMessageCount();
            inbox.close(false);
            store.close();
            return DataResponse.success("Non lus", count);
        } catch (Exception e) {
            log.warn("[MESSAGERIE] Erreur getNonLus : {}", e.getMessage());
            return DataResponse.success("Non lus", 0);
        }
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

    public DataResponse<UUID> envoyer(EnvoyerMailRequest req, List<MultipartFile> fichiers, String loginAuteur) {
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
            System.out.println("Destinataire: " + req.destinataire());
            messageId = envoyerSmtp(config, req.destinataire(), req.cc(), req.sujet(),
                    corpsAvecSignature, fichiers); // fichiers = param ajouté à envoyer()
            statut = StatutMailEnvoye.ENVOYE;
            log.info("[MESSAGERIE] Mail envoyé à {} — sujet: {}", req.destinataire(), req.sujet());
        } catch (Exception e) {
            statut = StatutMailEnvoye.ECHEC;
            String detail = e.getMessage();
            if (e instanceof jakarta.mail.SendFailedException sfe) {
                StringBuilder sb = new StringBuilder(detail == null ? "" : detail);
                if (sfe.getInvalidAddresses() != null && sfe.getInvalidAddresses().length > 0)
                    sb.append(" | invalides=").append(Arrays.toString(sfe.getInvalidAddresses()));
                if (sfe.getValidUnsentAddresses() != null && sfe.getValidUnsentAddresses().length > 0)
                    sb.append(" | non-envoyés=").append(Arrays.toString(sfe.getValidUnsentAddresses()));
                Exception next = sfe.getNextException();
                while (next != null) {
                    sb.append(" → ").append(next.getMessage());
                    next = (next instanceof jakarta.mail.MessagingException me) ? me.getNextException() : null;
                }
                detail = sb.toString();
            }
            log.error("[MESSAGERIE] Échec envoi : {}", detail, e);
            throw new RuntimeException("Échec envoi mail : " + simplifierErreur(detail));
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
            String sujet, String corpsHtml,
            List<MultipartFile> fichiers) throws Exception {
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

        if (fichiers != null && !fichiers.isEmpty()) {
            // Corps HTML + pièces jointes → MimeMultipart
            MimeMultipart multipart = new MimeMultipart("mixed");

            // Part 1 : corps HTML
            jakarta.mail.internet.MimeBodyPart htmlPart = new jakarta.mail.internet.MimeBodyPart();
            htmlPart.setContent(corpsHtml, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            // Parts suivants : pièces jointes
            for (MultipartFile f : fichiers) {
                if (f.isEmpty())
                    continue;
                jakarta.mail.internet.MimeBodyPart attachPart = new jakarta.mail.internet.MimeBodyPart();
                attachPart.setFileName(jakarta.mail.internet.MimeUtility.encodeText(
                        f.getOriginalFilename(), "UTF-8", "B"));
                attachPart.setContent(f.getBytes(), f.getContentType());
                attachPart.setDisposition(Part.ATTACHMENT);
                multipart.addBodyPart(attachPart);
            }

            msg.setContent(multipart);
        } else {
            // Pas de PJ — envoi simple HTML
            msg.setContent(corpsHtml, "text/html; charset=UTF-8");
        }

        msg.saveChanges();
        Transport.send(msg);
        return msg.getMessageID();
    }

    /**
     * Lit un message IMAP complet par son UID.
     * Le messageIdImap retourné par getInbox() est l'UID IMAP (ex: "7105").
     * On utilise UIDFolder.getMessageByUID() pour le retrouver directement.
     *
     * GET /v1/messagerie/imap/{uid}
     */
    public DataResponse<MessageComplet> getMessageImap(String uid, String loginAuteur) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        if (!config.isEstConfiguree())
            throw new IllegalStateException("Messagerie non configurée");

        try {
            Store store = connectImap(config);
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE); // READ_WRITE pour marquer SEEN

            if (!(folder instanceof UIDFolder)) {
                folder.close(false);
                store.close();
                throw new RuntimeException("Le serveur IMAP ne supporte pas les UID");
            }

            UIDFolder uidFolder = (UIDFolder) folder;
            long uidLong;
            try {
                uidLong = Long.parseLong(uid);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("UID IMAP invalide : " + uid);
            }

            Message m = uidFolder.getMessageByUID(uidLong);
            if (m == null) {
                folder.close(false);
                store.close();
                throw new RessourceNotFoundException("Message IMAP introuvable (UID=" + uid + ")");
            }

            // ── Marquer comme lu ──────────────────────────────────────────
            m.setFlag(Flags.Flag.SEEN, true);

            // ── Extraire les infos de base ────────────────────────────────
            String de = m.getFrom() != null && m.getFrom().length > 0
                    ? m.getFrom()[0].toString()
                    : "";
            List<String> a = m.getRecipients(Message.RecipientType.TO) != null
                    ? Arrays.stream(m.getRecipients(Message.RecipientType.TO))
                            .map(Object::toString).toList()
                    : List.of();
            List<String> cc = m.getRecipients(Message.RecipientType.CC) != null
                    ? Arrays.stream(m.getRecipients(Message.RecipientType.CC))
                            .map(Object::toString).toList()
                    : List.of();
            String sujet = m.getSubject() != null
                    ? jakarta.mail.internet.MimeUtility.decodeText(m.getSubject())
                    : "(sans sujet)";
            LocalDateTime date = m.getSentDate() != null
                    ? m.getSentDate().toInstant()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
                    : LocalDateTime.now();

            // ── Extraire corps + pièces jointes ──────────────────────────
            CorpsEtPj corpsEtPj = extraireCorpsEtPj(m);

            folder.close(false);
            store.close();

            return DataResponse.success("Message IMAP",
                    new MessageComplet(
                            null, // pas de courrierTrackingId (non en base)
                            uid, // messageIdImap = l'UID
                            de, a, cc, sujet,
                            corpsEtPj.corpsHtml(),
                            corpsEtPj.corpsTexte(),
                            date,
                            true, // on vient de le marquer lu
                            false, null, null, null,
                            corpsEtPj.piecesJointes()));

        } catch (RessourceNotFoundException | IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MESSAGERIE] Erreur lecture message IMAP UID={} : {}", uid, e.getMessage());
            throw new RuntimeException("Erreur lecture message : " + simplifierErreur(e.getMessage()));
        }
    }

    public DataResponse<PieceJointe> telechargerPieceJointe(
            String messageIdImap, int partIndex, String loginAuteur) {
        ConfigurationMail config = getConfigOuException(loginAuteur);
        try {
            Store store = connectImap(config);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            // Recherche par Message-ID
            jakarta.mail.search.SearchTerm term = new jakarta.mail.search.MessageIDTerm(messageIdImap);
            Message[] found = inbox.search(term);
            if (found.length == 0)
                throw new RessourceNotFoundException("Message IMAP introuvable");

            Message m = found[0];
            PieceJointe pj = extrairePieceJointe(m, partIndex);
            inbox.close(false);
            store.close();
            return DataResponse.success("Pièce jointe", pj);
        } catch (RessourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("[MESSAGERIE] Erreur téléchargement PJ : {}", e.getMessage());
            throw new RuntimeException("Impossible de récupérer la pièce jointe");
        }
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

    private MessageApercu toApercu(Message m) {
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

    public CorpsEtPj extraireCorpsEtPj(Message m) throws Exception {
        String corpsHtml = null;
        String corpsTexte = null;
        List<PieceJointe> pj = new ArrayList<>();
        extraireRecursif(m, new int[] { 0 }, pj, new StringBuilder(), new StringBuilder());
        Object content = m.getContent();
        if (content instanceof String s) {
            if (m.getContentType().toLowerCase().contains("html"))
                corpsHtml = s;
            else
                corpsTexte = s;
        } else if (content instanceof MimeMultipart) {
            CorpsEtPj r = extraireMultipart((MimeMultipart) content, new int[] { 0 });
            corpsHtml = r.corpsHtml();
            corpsTexte = r.corpsTexte();
            pj = r.piecesJointes();
        }
        return new CorpsEtPj(corpsHtml, corpsTexte, pj);
    }

    private CorpsEtPj extraireMultipart(MimeMultipart mp, int[] idx) throws Exception {
        String html = null;
        String texte = null;
        List<PieceJointe> pjs = new ArrayList<>();

        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);
            String disp = part.getDisposition();
            String ct = part.getContentType().toLowerCase();

            if (Part.ATTACHMENT.equalsIgnoreCase(disp) || Part.INLINE.equalsIgnoreCase(disp)
                    && part.getFileName() != null) {
                // Pièce jointe — on ne stocke PAS le contenu dans la liste (trop lourd)
                String nom = part.getFileName() != null
                        ? jakarta.mail.internet.MimeUtility.decodeText(part.getFileName())
                        : "piece-jointe-" + idx[0];
                pjs.add(new PieceJointe(idx[0], nom, part.getContentType(),
                        part.getSize(), null));
                idx[0]++;
            } else if (ct.startsWith("text/html")) {
                html = (String) part.getContent();
            } else if (ct.startsWith("text/plain") && html == null) {
                texte = (String) part.getContent();
            } else if (part.getContent() instanceof MimeMultipart) {
                CorpsEtPj sub = extraireMultipart((MimeMultipart) part.getContent(), idx);
                if (sub.corpsHtml() != null)
                    html = sub.corpsHtml();
                if (sub.corpsTexte() != null)
                    texte = sub.corpsTexte();
                pjs.addAll(sub.piecesJointes());
            }
        }
        return new CorpsEtPj(html, texte, pjs);
    }

    private PieceJointe extrairePieceJointe(Message m, int targetIndex) throws Exception {
        int[] idx = { 0 };
        if (m.getContent() instanceof MimeMultipart mp) {
            return trouverPj(mp, idx, targetIndex);
        }
        throw new RessourceNotFoundException("Pièce jointe introuvable");
    }

    private PieceJointe trouverPj(MimeMultipart mp, int[] idx, int target) throws Exception {
        for (int i = 0; i < mp.getCount(); i++) {
            BodyPart part = mp.getBodyPart(i);
            String disp = part.getDisposition();
            if (Part.ATTACHMENT.equalsIgnoreCase(disp)
                    || (Part.INLINE.equalsIgnoreCase(disp) && part.getFileName() != null)) {
                if (idx[0] == target) {
                    // Lire le contenu en Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try (InputStream is = part.getInputStream()) {
                        is.transferTo(baos);
                    }
                    String nom = part.getFileName() != null
                            ? jakarta.mail.internet.MimeUtility.decodeText(part.getFileName())
                            : "piece-jointe";
                    return new PieceJointe(idx[0], nom, part.getContentType(),
                            part.getSize(), Base64.getEncoder().encodeToString(baos.toByteArray()));
                }
                idx[0]++;
            } else if (part.getContent() instanceof MimeMultipart) {
                PieceJointe found = trouverPj((MimeMultipart) part.getContent(), idx, target);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    public void extraireRecursif(Object o, int[] idx,
            List<PieceJointe> pjs,
            StringBuilder txt, StringBuilder html) {
        // placeholder — la vraie logique est dans extraireMultipart()
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
        return new MessageComplet(
                c.getCourrierTrackingId(), c.getMessageIdMail(),
                c.getExpediteur(), List.of(c.getDestinataire()), List.of(),
                c.getObjet(), c.getCorpsHtml(), null,
                c.getDateEnvoi(), true, c.isTraite(),
                c.getSinistre() != null ? c.getSinistre().getNumeroSinistreLocal() : null,
                c.getSinistre() != null ? c.getSinistre().getSinistreTrackingId() : null,
                c.getTemplate() != null ? c.getTemplate().getTypeTemplate() : null,
                List.of() // ← pièces jointes vides pour courriers CBConnect
        );
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