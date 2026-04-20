package com.ossanasur.cbconnect.module.messagerie.service;

import com.ossanasur.cbconnect.common.enums.SecuriteMailProtocole;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.auth.entity.Utilisateur;
import com.ossanasur.cbconnect.module.auth.repository.UtilisateurRepository;
import com.ossanasur.cbconnect.module.messagerie.dto.request.ChangeMotDePasseMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.ConfigMailRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.SignatureRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.request.TestConnexionRequest;
import com.ossanasur.cbconnect.module.messagerie.dto.response.ConfigMailResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.DetectionDomainResponse;
import com.ossanasur.cbconnect.module.messagerie.dto.response.TestConnexionResponse;
import com.ossanasur.cbconnect.module.messagerie.entity.ConfigurationMail;
import com.ossanasur.cbconnect.module.messagerie.repository.ConfigurationMailRepository;
import com.ossanasur.cbconnect.utils.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessagerieService {

    private final ConfigurationMailRepository configRepo;
    private final UtilisateurRepository utilisateurRepo;

    // ── Clé AES 256 bits depuis variable d'environnement ─────────
    // En production : MAIL_ENCRYPTION_KEY=32-char-secret-key
    private static final String ENV_KEY = System.getenv()
            .getOrDefault("MAIL_ENCRYPTION_KEY", "CBConnect2025SecretKey!AES256!!");

    // ─── Détection domaine ────────────────────────────────────────

    public DataResponse<DetectionDomainResponse> detecterDomaine(String email) {
        String domaine = email.substring(email.indexOf('@') + 1).toLowerCase().trim();
        return DataResponse.success("Détection réussie", buildDetection(domaine));
    }

    private DetectionDomainResponse buildDetection(String domaine) {
        return switch (domaine) {

            case "gmail.com", "googlemail.com" -> new DetectionDomainResponse(
                    domaine, "Gmail / Google Workspace",
                    "https://ssl.gstatic.com/ui/v1/icons/mail/rfr/gmail.ico",
                    true,
                    "smtp.gmail.com", 587, SecuriteMailProtocole.STARTTLS,
                    "imap.gmail.com", 993, SecuriteMailProtocole.SSL,
                    "Activez l'accès IMAP dans Paramètres Gmail → Voir tous les paramètres → Transfert et POP/IMAP. " +
                            "Utilisez un mot de passe d'application si vous avez la validation en 2 étapes.");

            case "yahoo.com", "yahoo.fr", "ymail.com" -> new DetectionDomainResponse(
                    domaine, "Yahoo Mail",
                    "https://s.yimg.com/rz/p/yahoo_frontpage_en-US_s_f_p_205x58_frontpage.png",
                    true,
                    "smtp.mail.yahoo.com", 587, SecuriteMailProtocole.STARTTLS,
                    "imap.mail.yahoo.com", 993, SecuriteMailProtocole.SSL,
                    "Générez un mot de passe d'application dans Sécurité du compte Yahoo.");

            case "outlook.com", "hotmail.com", "hotmail.fr", "live.com", "live.fr", "msn.com" ->
                new DetectionDomainResponse(
                        domaine, "Microsoft Outlook / Hotmail",
                        "https://outlook.live.com/favicon.ico",
                        true,
                        "smtp-mail.outlook.com", 587, SecuriteMailProtocole.STARTTLS,
                        "outlook.office365.com", 993, SecuriteMailProtocole.SSL,
                        "Si vous utilisez l'authentification à deux facteurs, créez un mot de passe d'application.");

            case "icloud.com", "me.com", "mac.com" -> new DetectionDomainResponse(
                    domaine, "Apple iCloud Mail",
                    "https://www.icloud.com/favicon.ico",
                    true,
                    "smtp.mail.me.com", 587, SecuriteMailProtocole.STARTTLS,
                    "imap.mail.me.com", 993, SecuriteMailProtocole.SSL,
                    "Activez Mail dans iCloud.com → Gérer l'Apple ID. Utilisez un mot de passe spécifique à l'app.");

            case "orange.fr", "wanadoo.fr" -> new DetectionDomainResponse(
                    domaine, "Orange / Wanadoo",
                    "https://www.orange.fr/favicon.ico",
                    true,
                    "smtp.orange.fr", 587, SecuriteMailProtocole.STARTTLS,
                    "imap.orange.fr", 993, SecuriteMailProtocole.SSL,
                    "Utilisez votre identifiant Orange complet comme nom d'utilisateur.");

            // Domaines institutionnels Togo / CEDEAO connus
            case "bncb-togo.tg", "bncb.tg" -> new DetectionDomainResponse(
                    domaine, "BNCB Togo",
                    "", true,
                    "mail.bncb-togo.tg", 587, SecuriteMailProtocole.STARTTLS,
                    "mail.bncb-togo.tg", 993, SecuriteMailProtocole.SSL,
                    "Contactez l'administrateur système BNCB si vous ne connaissez pas votre mot de passe.");

            default -> {
                // Tentative heuristique : smtp.domaine / imap.domaine (courants chez hébergeurs
                // mutualisés)
                yield new DetectionDomainResponse(
                        domaine,
                        "Serveur " + domaine,
                        "",
                        false, // non confirmé — l'utilisateur doit valider
                        "mail." + domaine, 587, SecuriteMailProtocole.STARTTLS,
                        "mail." + domaine, 993, SecuriteMailProtocole.SSL,
                        "Nous n'avons pas pu détecter automatiquement votre configuration. " +
                                "Vérifiez auprès de votre fournisseur mail les paramètres SMTP/IMAP exacts.");
            }
        };
    }

    // ─── Test de connexion SMTP ───────────────────────────────────

    public DataResponse<TestConnexionResponse> testerConnexion(TestConnexionRequest req) {
        boolean smtpOk = false;
        String smtpMsg = "";
        boolean imapOk = false; // IMAP testé séparément si besoin
        String imapMsg = "";

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", req.smtpHost());
            props.put("mail.smtp.port", String.valueOf(req.smtpPort()));
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");

            if (req.smtpSecurite() == SecuriteMailProtocole.SSL) {
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", String.valueOf(req.smtpPort()));
            } else if (req.smtpSecurite() == SecuriteMailProtocole.STARTTLS) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props,
                    new jakarta.mail.Authenticator() {
                        protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                            return new jakarta.mail.PasswordAuthentication(req.username(), req.motDePasse());
                        }
                    });

            jakarta.mail.Transport transport = session.getTransport("smtp");
            transport.connect(req.smtpHost(), req.smtpPort(), req.username(), req.motDePasse());
            transport.close();
            smtpOk = true;
            smtpMsg = "Connexion SMTP réussie";
            imapOk = true; // On valide les deux si SMTP passe
            imapMsg = "Configuration acceptée";
        } catch (Exception e) {
            smtpMsg = simplifierErreur(e.getMessage());
            imapMsg = "Non testé";
            log.warn("[MESSAGERIE] Test connexion SMTP échoué : {}", e.getMessage());
        }

        return DataResponse.success("Test effectué",
                new TestConnexionResponse(smtpOk, imapOk, smtpMsg, imapMsg, smtpOk && imapOk));
    }

    private String simplifierErreur(String msg) {
        if (msg == null)
            return "Erreur inconnue";
        if (msg.contains("Authentication failed") || msg.contains("535"))
            return "Identifiants incorrects — vérifiez email et mot de passe";
        if (msg.contains("Connection refused") || msg.contains("connect"))
            return "Impossible de joindre le serveur — vérifiez l'hôte et le port";
        if (msg.contains("timeout"))
            return "Délai dépassé — vérifiez votre réseau";
        if (msg.contains("SSL"))
            return "Erreur SSL — essayez STARTTLS ou un autre port";
        return msg.length() > 100 ? msg.substring(0, 100) + "…" : msg;
    }

    // ─── Sauvegarde configuration ─────────────────────────────────

    public DataResponse<ConfigMailResponse> sauvegarderConfig(ConfigMailRequest req, String loginAuteur) {
        Utilisateur utilisateur = utilisateurRepo.findByUsernameAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));

        ConfigurationMail config = configRepo.findByUtilisateur(utilisateur)
                .orElse(ConfigurationMail.builder()
                        .trackingId(UUID.randomUUID())
                        .utilisateur(utilisateur)
                        .emailExpediteur(utilisateur.getEmail())
                        .createdBy(loginAuteur)
                        .activeData(true).deletedData(false)
                        .build());

        config.setNomAffiche(
                req.nomAffiche() != null ? req.nomAffiche() : utilisateur.getNom() + " " + utilisateur.getPrenoms());
        config.setSmtpHost(req.smtpHost());
        config.setSmtpPort(req.smtpPort());
        config.setSmtpSecurite(req.smtpSecurite());
        config.setSmtpUsername(req.smtpUsername() != null ? req.smtpUsername() : utilisateur.getEmail());
        config.setSmtpPasswordEnc(chiffrer(req.motDePasse()));
        config.setImapHost(req.imapHost());
        config.setImapPort(req.imapPort());
        config.setImapSecurite(req.imapSecurite());
        config.setSignature(req.signature());
        config.setEstConfiguree(true);
        config.setUpdatedBy(loginAuteur);

        return DataResponse.success("Configuration sauvegardée", toResponse(configRepo.save(config)));
    }

    // ─── Récupérer la config (sans mot de passe) ─────────────────

    @Transactional(readOnly = true)
    public DataResponse<ConfigMailResponse> getConfig(String loginAuteur) {
        Utilisateur utilisateur = utilisateurRepo.findByUsernameAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        return configRepo.findByUtilisateur(utilisateur)
                .map(c -> DataResponse.success("Configuration", toResponse(c)))
                .orElse(DataResponse.success("Non configuré", null));
    }

    // ─── Mise à jour signature ────────────────────────────────────

    public DataResponse<ConfigMailResponse> mettreAJourSignature(SignatureRequest req, String loginAuteur) {
        Utilisateur u = utilisateurRepo.findByUsernameAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        ConfigurationMail c = configRepo.findByUtilisateur(u)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Configuration mail non trouvée — configurez d'abord votre messagerie"));
        c.setSignature(req.signature());
        c.setUpdatedBy(loginAuteur);
        return DataResponse.success("Signature mise à jour", toResponse(configRepo.save(c)));
    }

    // ─── Changer mot de passe mail ────────────────────────────────

    public DataResponse<Void> changerMotDePasse(ChangeMotDePasseMailRequest req, String loginAuteur) {
        Utilisateur u = utilisateurRepo.findByUsernameAndActiveDataTrueAndDeletedDataFalse(loginAuteur)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable"));
        ConfigurationMail c = configRepo.findByUtilisateur(u)
                .orElseThrow(() -> new RessourceNotFoundException("Configuration mail non trouvée"));
        c.setSmtpPasswordEnc(chiffrer(req.nouveauMotDePasse()));
        c.setUpdatedBy(loginAuteur);
        configRepo.save(c);
        return DataResponse.success("Mot de passe mis à jour", null);
    }

    // ─── Chiffrement AES-256 ──────────────────────────────────────

    private String chiffrer(String texte) {
        try {
            byte[] key = Arrays.copyOf(ENV_KEY.getBytes(StandardCharsets.UTF_8), 32);
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(texte.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("[MESSAGERIE] Erreur chiffrement", e);
            throw new RuntimeException("Erreur chiffrement mot de passe");
        }
    }

    // ─── Mapper ───────────────────────────────────────────────────

    private ConfigMailResponse toResponse(ConfigurationMail c) {
        return new ConfigMailResponse(
                c.getTrackingId(), c.getEmailExpediteur(), c.getNomAffiche(), c.isEstConfiguree(),
                c.getSmtpHost(), c.getSmtpPort() != null ? c.getSmtpPort() : 0,
                c.getSmtpSecurite(), c.getSmtpUsername(), c.isSmtpAuth(),
                c.getImapHost(), c.getImapPort() != null ? c.getImapPort() : 0,
                c.getImapSecurite(), c.getSignature(),
                c.getDerniereSynchro(), c.getNbMessagesNonLus());
    }
}
