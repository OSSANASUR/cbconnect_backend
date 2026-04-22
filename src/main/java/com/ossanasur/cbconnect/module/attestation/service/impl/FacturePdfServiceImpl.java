package com.ossanasur.cbconnect.module.attestation.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.ossanasur.cbconnect.common.enums.TypeFactureAttestation;
import com.ossanasur.cbconnect.common.enums.TypeOrganisme;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.attestation.entity.CommandeAttestation;
import com.ossanasur.cbconnect.module.attestation.entity.FactureAttestation;
import com.ossanasur.cbconnect.module.attestation.entity.TrancheLivraisonAttestation;
import com.ossanasur.cbconnect.module.attestation.repository.FactureAttestationRepository;
import com.ossanasur.cbconnect.module.attestation.repository.TrancheLivraisonAttestationRepository;
import com.ossanasur.cbconnect.module.attestation.service.FacturePdfService;
import com.ossanasur.cbconnect.module.attestation.util.MontantEnLettresFr;
import com.ossanasur.cbconnect.module.auth.entity.Organisme;
import com.ossanasur.cbconnect.module.auth.repository.OrganismeRepository;
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacturePdfServiceImpl implements FacturePdfService {

    private final FactureAttestationRepository factureRepository;
    private final TrancheLivraisonAttestationRepository trancheRepository;
    private final ParametreRepository parametreRepository;
    private final OrganismeRepository organismeRepository;

    // ── Charte graphique CBConnect ─────────────────────────────────────────────
    private static final Color VERT_BNCB     = new Color(26, 107, 60);     // #1a6b3c
    private static final Color VERT_FONCE    = new Color(20, 83, 45);      // #14532d
    private static final Color VERT_CLAIR    = new Color(240, 253, 244);   // #f0fdf4
    private static final Color GRIS_BORDURE  = new Color(229, 231, 235);   // #e5e7eb
    private static final Color GRIS_FOND     = new Color(249, 250, 251);   // #f9fafb
    private static final Color GRIS_TEXTE    = new Color(107, 114, 128);   // #6b7280
    private static final Color GRIS_LABEL    = new Color(75, 85, 99);      // #4b5563
    private static final Color BLANC         = Color.WHITE;
    private static final Color NOIR_TITRE    = new Color(17, 24, 39);      // #111827

    // Polices
    private static final Font F_TITLE_HUGE   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, NOIR_TITRE);
    private static final Font F_TITLE_NUM    = FontFactory.getFont(FontFactory.HELVETICA, 12, GRIS_TEXTE);
    private static final Font F_HEADER_WHITE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BLANC);
    private static final Font F_HEADER_LINE  = FontFactory.getFont(FontFactory.HELVETICA, 9, BLANC);
    private static final Font F_LABEL_SMALL  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, GRIS_LABEL);
    private static final Font F_NORMAL       = FontFactory.getFont(FontFactory.HELVETICA, 10, NOIR_TITRE);
    private static final Font F_NORMAL_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 9, NOIR_TITRE);
    private static final Font F_BOLD         = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NOIR_TITRE);
    private static final Font F_BOLD_SMALL   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, NOIR_TITRE);
    private static final Font F_TABLE_HEAD   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BLANC);
    private static final Font F_TABLE_CELL   = FontFactory.getFont(FontFactory.HELVETICA, 10, NOIR_TITRE);
    private static final Font F_TOTAL        = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, VERT_FONCE);
    private static final Font F_LETTRES      = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, VERT_FONCE);
    private static final Font F_SIGNATURE    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, NOIR_TITRE);
    private static final Font F_FOOTER       = FontFactory.getFont(FontFactory.HELVETICA, 8, GRIS_TEXTE);

    private final DecimalFormat NUM = new DecimalFormat("#,##0",
        DecimalFormatSymbols.getInstance(Locale.FRANCE));
    private final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    // ────────────────────────────────────────────────────────────────────────────

    private String param(String cle, String defaut) {
        return parametreRepository.findByCle(cle).map(p -> p.getValeur()).orElse(defaut);
    }

    /** Tente de retrouver l'organisme BNCB pour exposer ses coordonnees physiques (adresse, BP...). */
    private Optional<Organisme> chercherBureauNational() {
        var raisonAttendue = param("BNCB_RAISON_SOCIALE", null);
        var bureaux = organismeRepository.findAllActiveByType(TypeOrganisme.BUREAU_NATIONAL);
        if (bureaux.isEmpty()) return Optional.empty();
        if (raisonAttendue != null) {
            var match = bureaux.stream()
                .filter(o -> raisonAttendue.equalsIgnoreCase(o.getRaisonSociale())).findFirst();
            if (match.isPresent()) return match;
        }
        return Optional.of(bureaux.get(0));
    }

    @Override
    public String nomFichier(UUID factureTrackingId) {
        var f = factureRepository.findActiveByTrackingId(factureTrackingId)
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        String prefix = TypeFactureAttestation.PROFORMA.equals(f.getTypeFacture()) ? "Proforma" : "Facture";
        return prefix + "_" + f.getNumeroFacture().replace('/', '-') + ".pdf";
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] genererPdf(UUID factureTrackingId) {
        FactureAttestation f = factureRepository.findActiveByTrackingId(factureTrackingId)
            .orElseThrow(() -> new RessourceNotFoundException("Facture introuvable"));
        var commande = f.getCommande();
        if (commande == null) throw new RessourceNotFoundException("Commande de la facture introuvable");
        var destinataire = commande.getOrganisme();
        var bnOpt = chercherBureauNational();
        boolean proforma = TypeFactureAttestation.PROFORMA.equals(f.getTypeFacture());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new FooterEvent());
            doc.open();

            ajouterEnTete(doc, bnOpt);
            ajouterTitre(doc, proforma, f);
            ajouterBlocsParties(doc, bnOpt, destinataire, f, commande);
            ajouterTableau(doc, commande, f);
            ajouterMontantEnLettres(doc, proforma, f);
            ajouterEncartFinal(doc, proforma, commande, f);
            ajouterSignature(doc, f);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Echec generation PDF facture : " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    // ── 1. Bandeau d'en-tete vert avec identite BNCB ──────────────────────────
    private void ajouterEnTete(Document doc, Optional<Organisme> bnOpt) throws DocumentException {
        String raison = param("BNCB_RAISON_SOCIALE", "BUREAU NATIONAL TOGOLAIS CARTE BRUNE CEDEAO");
        String ville = param("BNCB_VILLE", "Lomé");
        String pays = param("BNCB_PAYS", "TOGO");

        // Coordonnees venant de l'entite Organisme BNCB si presente, sinon parametres BNCB_*.
        String adresse = bnOpt.map(Organisme::getAdresse).orElseGet(() -> param("BNCB_ADRESSE", null));
        String bp = bnOpt.map(Organisme::getBoitePostale).orElseGet(() -> param("BNCB_BP", null));
        String tel = bnOpt.map(Organisme::getTelephonePrincipal).orElseGet(() -> param("BNCB_TELEPHONE", null));
        String email = bnOpt.map(Organisme::getEmail).orElseGet(() -> param("BNCB_EMAIL", null));

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(VERT_BNCB);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(16);

        Paragraph titre = new Paragraph(raison, F_HEADER_WHITE);
        titre.setSpacingAfter(4);
        cell.addElement(titre);

        StringBuilder lignes = new StringBuilder();
        if (notBlank(adresse)) lignes.append(adresse).append("  •  ");
        if (notBlank(bp))      lignes.append(bp).append("  •  ");
        lignes.append(ville).append(", ").append(pays);
        cell.addElement(new Paragraph(lignes.toString(), F_HEADER_LINE));

        if (notBlank(tel) || notBlank(email)) {
            StringBuilder contact = new StringBuilder();
            if (notBlank(tel))   contact.append("Tél : ").append(tel);
            if (notBlank(tel) && notBlank(email)) contact.append("   |   ");
            if (notBlank(email)) contact.append("Email : ").append(email);
            Paragraph p = new Paragraph(contact.toString(), F_HEADER_LINE);
            p.setSpacingBefore(2);
            cell.addElement(p);
        }
        header.addCell(cell);
        doc.add(header);
    }

    // ── 2. Titre central : type de facture + numero ───────────────────────────
    private void ajouterTitre(Document doc, boolean proforma, FactureAttestation f) throws DocumentException {
        Paragraph type = new Paragraph(proforma ? "FACTURE PROFORMA" : "FACTURE", F_TITLE_HUGE);
        type.setAlignment(Element.ALIGN_CENTER);
        type.setSpacingBefore(20);
        type.setSpacingAfter(2);
        doc.add(type);

        Paragraph num = new Paragraph("N° " + f.getNumeroFacture(), F_TITLE_NUM);
        num.setAlignment(Element.ALIGN_CENTER);
        num.setSpacingAfter(20);
        doc.add(num);
    }

    // ── 3. Deux blocs cote a cote : Emetteur / Destinataire ───────────────────
    private void ajouterBlocsParties(Document doc, Optional<Organisme> bnOpt,
                                     Organisme destinataire, FactureAttestation f, CommandeAttestation commande) throws DocumentException {
        PdfPTable blocs = new PdfPTable(2);
        blocs.setWidthPercentage(100);
        blocs.setWidths(new float[] { 1f, 1f });
        blocs.setSpacingAfter(16);

        // Bloc Emetteur
        PdfPCell emetteur = boxCell();
        emetteur.addElement(labelLine("ÉMETTEUR"));
        String nom = param("BNCB_RAISON_SOCIALE", "BUREAU NATIONAL");
        emetteur.addElement(textLine(nom, F_BOLD));
        bnOpt.ifPresent(bn -> {
            if (notBlank(bn.getAdresse())) emetteur.addElement(textLine(bn.getAdresse(), F_NORMAL_SMALL));
            String l2 = joinSep(bn.getBoitePostale(), bn.getVille());
            if (notBlank(l2)) emetteur.addElement(textLine(l2, F_NORMAL_SMALL));
        });
        emetteur.addElement(textLine(param("BNCB_VILLE", "Lomé") + ", " + param("BNCB_PAYS", "TOGO"), F_NORMAL_SMALL));

        // Bloc Destinataire
        PdfPCell dest = boxCell();
        dest.addElement(labelLine("FACTURÉ À"));
        if (destinataire != null) {
            dest.addElement(textLine(destinataire.getRaisonSociale(), F_BOLD));
            if (notBlank(destinataire.getAdresse()))
                dest.addElement(textLine(destinataire.getAdresse(), F_NORMAL_SMALL));
            String l2 = joinSep(destinataire.getBoitePostale(), destinataire.getVille());
            if (notBlank(l2)) dest.addElement(textLine(l2, F_NORMAL_SMALL));
            if (notBlank(destinataire.getTelephonePrincipal()))
                dest.addElement(textLine("Tél : " + destinataire.getTelephonePrincipal(), F_NORMAL_SMALL));
            if (notBlank(destinataire.getEmail()))
                dest.addElement(textLine(destinataire.getEmail(), F_NORMAL_SMALL));
        } else {
            dest.addElement(textLine("—", F_NORMAL));
        }
        blocs.addCell(emetteur);
        blocs.addCell(dest);
        doc.add(blocs);

        // Ligne d'infos cles : date d'emission + numero commande
        PdfPTable infos = new PdfPTable(2);
        infos.setWidthPercentage(100);
        infos.setWidths(new float[] { 1f, 1f });
        infos.setSpacingAfter(8);
        LocalDate dateRef = f.getDateFacture() != null ? f.getDateFacture() : LocalDate.now();
        infos.addCell(infoCell("Date d'émission", DATE_FR.format(dateRef)));
        infos.addCell(infoCell("Référence commande",
            commande != null ? commande.getNumeroCommande() : "—"));
        doc.add(infos);
    }

    // ── 4. Tableau des prestations ────────────────────────────────────────────
    private void ajouterTableau(Document doc, CommandeAttestation commande, FactureAttestation f) throws DocumentException {
        PdfPTable t = new PdfPTable(4);
        t.setWidthPercentage(100);
        t.setWidths(new float[] { 3.4f, 1.2f, 1.6f, 1.8f });
        t.setSpacingBefore(4);
        t.setSpacingAfter(8);

        // En-tete : fond vert + texte blanc
        addHeadCell(t, "DÉSIGNATION", Element.ALIGN_LEFT);
        addHeadCell(t, "QUANTITÉ", Element.ALIGN_CENTER);
        addHeadCell(t, "PU (FCFA)", Element.ALIGN_RIGHT);
        addHeadCell(t, "MONTANT (FCFA)", Element.ALIGN_RIGHT);

        Integer quantite = commande != null ? commande.getQuantite() : null;
        java.math.BigDecimal pu = commande != null ? commande.getPrixUnitaireVente() : null;
        java.math.BigDecimal taux = commande != null ? commande.getTauxContributionFonds() : null;

        // Ligne 1 : Attestations Carte Brune (qte fusionnee sur 2 lignes)
        addBodyCell(t, "Attestations Carte Brune CEDEAO", Element.ALIGN_LEFT, false);
        PdfPCell qteCell = bodyCell(quantite != null ? NUM.format(quantite) : "—", Element.ALIGN_CENTER, false);
        qteCell.setRowspan(2);
        qteCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        t.addCell(qteCell);
        addBodyCell(t, pu != null ? NUM.format(pu) : "—", Element.ALIGN_RIGHT, false);
        addBodyCell(t, NUM.format(f.getMontantAttestation()), Element.ALIGN_RIGHT, false);

        // Ligne 2 : Contribution fonds (zebra)
        addBodyCell(t, "Contribution au fonds de compensation", Element.ALIGN_LEFT, true);
        addBodyCell(t, taux != null ? NUM.format(taux) : "—", Element.ALIGN_RIGHT, true);
        addBodyCell(t, NUM.format(f.getMontantContributionFonds()), Element.ALIGN_RIGHT, true);

        // Ligne total : libelle sur 3 colonnes + montant
        PdfPCell totLib = new PdfPCell(new Phrase("TOTAL À PAYER", F_TOTAL));
        totLib.setColspan(3);
        totLib.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totLib.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totLib.setBackgroundColor(VERT_CLAIR);
        totLib.setBorderColor(VERT_BNCB);
        totLib.setPaddingTop(10); totLib.setPaddingBottom(10); totLib.setPaddingRight(12);
        t.addCell(totLib);
        PdfPCell totVal = new PdfPCell(new Phrase(NUM.format(f.getMontantTotal()) + "  FCFA", F_TOTAL));
        totVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totVal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        totVal.setBackgroundColor(VERT_CLAIR);
        totVal.setBorderColor(VERT_BNCB);
        totVal.setPaddingTop(10); totVal.setPaddingBottom(10); totVal.setPaddingRight(8);
        t.addCell(totVal);

        doc.add(t);
    }

    // ── 5. Encart "Arretee a la somme de : ..." ───────────────────────────────
    private void ajouterMontantEnLettres(Document doc, boolean proforma, FactureAttestation f) throws DocumentException {
        PdfPTable wrap = new PdfPTable(1);
        wrap.setWidthPercentage(100);
        wrap.setSpacingBefore(6);
        wrap.setSpacingAfter(14);
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(GRIS_FOND);
        c.setBorderColor(GRIS_BORDURE);
        c.setBorderWidth(0.5f);
        c.setPadding(10);

        c.addElement(labelLine(proforma
            ? "ARRÊTÉE LA PRÉSENTE PROFORMA À LA SOMME DE"
            : "ARRÊTÉE LA PRÉSENTE FACTURE À LA SOMME DE"));
        String enLettres = MontantEnLettresFr.convertir(f.getMontantTotal()).toUpperCase(Locale.FRENCH) + ".";
        c.addElement(new Paragraph(enLettres, F_LETTRES));
        wrap.addCell(c);
        doc.add(wrap);
    }

    // ── 6. Encart final : instructions cheque (proforma) OU plage serie ──────
    private void ajouterEncartFinal(Document doc, boolean proforma, CommandeAttestation commande, FactureAttestation f) throws DocumentException {
        PdfPTable wrap = new PdfPTable(1);
        wrap.setWidthPercentage(100);
        wrap.setSpacingAfter(20);
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(VERT_CLAIR);
        c.setBorderColor(VERT_BNCB);
        c.setBorderWidth(0.5f);
        c.setPadding(10);

        if (proforma) {
            String benef;
            String nb = commande != null ? commande.getNomBeneficiaireCheque() : null;
            if (notBlank(nb)) benef = nb;
            else if (notBlank(f.getInstructionCheque())) benef = f.getInstructionCheque();
            else benef = param("BNCB_BENEFICIAIRE_CHEQUE", "BUREAU NATIONAL TOGOLAIS CARTE BRUNE CEDEAO");

            c.addElement(labelLine("BÉNÉFICIAIRE DU CHÈQUE"));
            c.addElement(new Paragraph(benef, F_BOLD));
            Paragraph note = new Paragraph("Prière d'établir le chèque à l'ordre exclusif du bénéficiaire ci-dessus.", F_NORMAL_SMALL);
            note.setSpacingBefore(4);
            c.addElement(note);
            wrap.addCell(c);
            doc.add(wrap);
        } else if (commande != null) {
            List<TrancheLivraisonAttestation> tranches = trancheRepository.findByCommande(commande.getCommandeTrackingId());
            if (!tranches.isEmpty()) {
                String debut = tranches.stream().map(TrancheLivraisonAttestation::getNumeroDebutSerie)
                    .min(Comparator.naturalOrder()).orElse("—");
                String fin = tranches.stream().map(TrancheLivraisonAttestation::getNumeroFinSerie)
                    .max(Comparator.naturalOrder()).orElse("—");
                int totalLivre = tranches.stream().mapToInt(t -> t.getQuantiteLivree() == null ? 0 : t.getQuantiteLivree()).sum();
                c.addElement(labelLine("PLAGE ATTRIBUÉE"));
                c.addElement(new Paragraph(
                    "Du N° " + debut + " au N° " + fin + "  —  " + NUM.format(totalLivre) + " attestations",
                    F_BOLD));
                wrap.addCell(c);
                doc.add(wrap);
            }
        }
    }

    // ── 7. Signature : ville + date + "POUR LE BUREAU NATIONAL" + signataire ──
    private void ajouterSignature(Document doc, FactureAttestation f) throws DocumentException {
        String ville = param("BNCB_VILLE", "Lomé");
        LocalDate dateRef = f.getDateFacture() != null ? f.getDateFacture() : LocalDate.now();

        Paragraph dateP = new Paragraph("Fait à " + ville + ", le " + DATE_FR.format(dateRef), F_NORMAL);
        dateP.setAlignment(Element.ALIGN_RIGHT);
        dateP.setSpacingBefore(20);
        doc.add(dateP);

        Paragraph pourBn = new Paragraph("POUR LE BUREAU NATIONAL", F_BOLD_SMALL);
        pourBn.setAlignment(Element.ALIGN_RIGHT);
        pourBn.setSpacingBefore(6);
        doc.add(pourBn);

        // Espace cachet/signature
        Paragraph spacer = new Paragraph(" ", F_NORMAL);
        spacer.setSpacingBefore(40);
        doc.add(spacer);

        Paragraph signataire = new Paragraph(param("BNCB_SIGNATAIRE", "—"), F_SIGNATURE);
        signataire.setAlignment(Element.ALIGN_RIGHT);
        doc.add(signataire);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Helpers cellules
    // ────────────────────────────────────────────────────────────────────────────

    private PdfPCell boxCell() {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(GRIS_BORDURE);
        c.setBorderWidth(0.5f);
        c.setPadding(10);
        return c;
    }

    private PdfPCell infoCell(String label, String valeur) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(2);
        c.addElement(labelLine(label));
        c.addElement(new Paragraph(valeur != null ? valeur : "—", F_BOLD));
        return c;
    }

    private Paragraph labelLine(String txt) {
        Paragraph p = new Paragraph(txt.toUpperCase(Locale.FRENCH), F_LABEL_SMALL);
        p.setSpacingAfter(3);
        return p;
    }

    private Paragraph textLine(String txt, Font font) {
        Paragraph p = new Paragraph(txt, font);
        p.setSpacingAfter(1);
        return p;
    }

    private void addHeadCell(PdfPTable t, String txt, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, F_TABLE_HEAD));
        c.setBackgroundColor(VERT_BNCB);
        c.setBorderColor(VERT_BNCB);
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPaddingTop(8); c.setPaddingBottom(8); c.setPaddingLeft(8); c.setPaddingRight(8);
        t.addCell(c);
    }

    private PdfPCell bodyCell(String txt, int align, boolean zebra) {
        PdfPCell c = new PdfPCell(new Phrase(txt, F_TABLE_CELL));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPaddingTop(8); c.setPaddingBottom(8); c.setPaddingLeft(8); c.setPaddingRight(8);
        c.setBorderColor(GRIS_BORDURE);
        c.setBorderWidth(0.5f);
        if (zebra) c.setBackgroundColor(GRIS_FOND);
        return c;
    }

    private void addBodyCell(PdfPTable t, String txt, int align, boolean zebra) {
        t.addCell(bodyCell(txt, align, zebra));
    }

    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static String joinSep(String a, String b) {
        boolean aOk = notBlank(a), bOk = notBlank(b);
        if (aOk && bOk) return a + " — " + b;
        if (aOk) return a;
        if (bOk) return b;
        return "";
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Pied de page (numero / mention legale)
    // ────────────────────────────────────────────────────────────────────────────
    private class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                // Ligne fine
                cb.setColorStroke(GRIS_BORDURE);
                cb.setLineWidth(0.5f);
                cb.moveTo(36, 36);
                cb.lineTo(document.getPageSize().getWidth() - 36, 36);
                cb.stroke();

                String nomBureau = param("BNCB_RAISON_SOCIALE", "Bureau National Carte Brune");
                Phrase left = new Phrase(nomBureau, F_FOOTER);
                Phrase right = new Phrase("Page " + writer.getPageNumber(), F_FOOTER);
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,  left,
                    36, 24, 0);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, right,
                    document.getPageSize().getWidth() - 36, 24, 0);
            } catch (Exception ignored) {}
        }
    }
}
