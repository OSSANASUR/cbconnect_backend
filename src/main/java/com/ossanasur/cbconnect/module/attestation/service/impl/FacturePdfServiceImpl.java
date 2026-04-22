package com.ossanasur.cbconnect.module.attestation.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.ossanasur.cbconnect.common.enums.TypeFactureAttestation;
import com.ossanasur.cbconnect.exception.RessourceNotFoundException;
import com.ossanasur.cbconnect.module.attestation.entity.FactureAttestation;
import com.ossanasur.cbconnect.module.attestation.entity.TrancheLivraisonAttestation;
import com.ossanasur.cbconnect.module.attestation.repository.FactureAttestationRepository;
import com.ossanasur.cbconnect.module.attestation.repository.TrancheLivraisonAttestationRepository;
import com.ossanasur.cbconnect.module.attestation.service.FacturePdfService;
import com.ossanasur.cbconnect.module.attestation.util.MontantEnLettresFr;
import com.ossanasur.cbconnect.module.auth.repository.ParametreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacturePdfServiceImpl implements FacturePdfService {

    private final FactureAttestationRepository factureRepository;
    private final TrancheLivraisonAttestationRepository trancheRepository;
    private final ParametreRepository parametreRepository;

    private static final Font F_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font F_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 11);
    private static final Font F_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font F_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font F_TABLE_HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    private static final Font F_TABLE_CELL = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Color GRIS_HEAD = new Color(245, 245, 245);

    private final DecimalFormat NUM = new DecimalFormat("#,##0",
        DecimalFormatSymbols.getInstance(Locale.FRANCE));
    private final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.FRENCH);

    private String param(String cle, String defaut) {
        return parametreRepository.findByCle(cle).map(p -> p.getValeur()).orElse(defaut);
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
        var organisme = commande.getOrganisme();
        boolean proforma = TypeFactureAttestation.PROFORMA.equals(f.getTypeFacture());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── En-tête : pays BNCB à gauche, destinataire à droite ─────────────
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 1f, 1.4f });

            PdfPCell pays = new PdfPCell(new Phrase(param("BNCB_PAYS", "TOGO"), F_HEADER));
            pays.setBorder(Rectangle.NO_BORDER);
            pays.setPaddingTop(20);
            header.addCell(pays);

            PdfPCell dest = new PdfPCell();
            dest.setBorder(Rectangle.NO_BORDER);
            dest.addElement(new Paragraph(organisme != null ? organisme.getRaisonSociale() : "—", F_BOLD));
            header.addCell(dest);
            doc.add(header);

            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);

            // ── Titre centré ─────────────────────────────────────────────────────
            String titre = (proforma ? "FACTURE PROFORMA N° " : "FACTURE   N° ") + f.getNumeroFacture();
            Paragraph titreP = new Paragraph(titre, F_TITLE);
            titreP.setAlignment(Element.ALIGN_CENTER);
            doc.add(titreP);
            if (!proforma) {
                Paragraph sub = new Paragraph("LIVRAISON CARTES BRUNES CEDEAO", F_BOLD);
                sub.setAlignment(Element.ALIGN_CENTER);
                doc.add(sub);
            }
            doc.add(Chunk.NEWLINE);

            // ── Tableau ──────────────────────────────────────────────────────────
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3.2f, 1.4f, 1.6f, 1.8f });

            addHeadCell(table, "DESIGNATION");
            addHeadCell(table, "QUANTITE");
            addHeadCell(table, "PRIX UNITAIRE\nF.CFA");
            addHeadCell(table, "MONTANT TOTAL\nF.CFA");

            // Ligne 1 : Attestations Carte brune
            addCell(table, proforma ? "Attestations Carte brune" : "Attestation Carte brune", Element.ALIGN_LEFT);
            // Quantité fusionnée sur 2 lignes
            PdfPCell qte = new PdfPCell(new Phrase(NUM.format(commande.getQuantite()), F_TABLE_CELL));
            qte.setRowspan(2);
            qte.setHorizontalAlignment(Element.ALIGN_CENTER);
            qte.setVerticalAlignment(Element.ALIGN_MIDDLE);
            qte.setPadding(6);
            table.addCell(qte);
            addCell(table, NUM.format(commande.getPrixUnitaireVente()), Element.ALIGN_CENTER);
            addCell(table, NUM.format(f.getMontantAttestation()), Element.ALIGN_RIGHT);

            // Ligne 2 : Contribution fonds
            addCell(table, "Contribution au fonds de compensation", Element.ALIGN_LEFT);
            addCell(table, NUM.format(commande.getTauxContributionFonds()), Element.ALIGN_CENTER);
            addCell(table, NUM.format(f.getMontantContributionFonds()), Element.ALIGN_RIGHT);

            // Ligne total : libellé sur 3 colonnes + total
            PdfPCell totLib = new PdfPCell(new Phrase("Total à payer", F_BOLD));
            totLib.setColspan(3);
            totLib.setHorizontalAlignment(Element.ALIGN_CENTER);
            totLib.setBackgroundColor(GRIS_HEAD);
            totLib.setPadding(6);
            table.addCell(totLib);
            PdfPCell totVal = new PdfPCell(new Phrase(NUM.format(f.getMontantTotal()), F_BOLD));
            totVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totVal.setBackgroundColor(GRIS_HEAD);
            totVal.setPadding(6);
            table.addCell(totVal);

            doc.add(table);

            doc.add(Chunk.NEWLINE);

            // ── Montant en lettres ───────────────────────────────────────────────
            String prefixe = proforma ? "ARRÊTÉE LA PRÉSENTE PROFORMA À LA SOMME DE : "
                                       : "ARRÊTÉE LA PRÉSENTE FACTURE À LA SOMME DE : ";
            String enLettres = MontantEnLettresFr.convertir(f.getMontantTotal()).toUpperCase(Locale.FRENCH) + ".";
            Paragraph lettres = new Paragraph();
            lettres.add(new Chunk(prefixe, F_BOLD));
            lettres.add(new Chunk(enLettres, F_BOLD));
            doc.add(lettres);

            doc.add(Chunk.NEWLINE);

            // ── Instructions chèque (proforma) OU plage série livrée (définitive)
            if (proforma) {
                String benef = (commande.getNomBeneficiaireCheque() != null && !commande.getNomBeneficiaireCheque().isBlank())
                    ? commande.getNomBeneficiaireCheque()
                    : f.getInstructionCheque() != null ? f.getInstructionCheque()
                    : param("BNCB_BENEFICIAIRE_CHEQUE", "BUREAU NATIONAL TOGOLAIS CARTE BRUNE CEDEAO");
                doc.add(new Paragraph("Prière établir le chèque au nom du :", F_NORMAL));
                doc.add(new Paragraph(benef, F_BOLD));
            } else {
                List<TrancheLivraisonAttestation> tranches = trancheRepository.findByCommande(commande.getCommandeTrackingId());
                if (!tranches.isEmpty()) {
                    String debut = tranches.stream().map(TrancheLivraisonAttestation::getNumeroDebutSerie)
                        .min(Comparator.naturalOrder()).orElse("—");
                    String fin = tranches.stream().map(TrancheLivraisonAttestation::getNumeroFinSerie)
                        .max(Comparator.naturalOrder()).orElse("—");
                    int totalLivre = tranches.stream().mapToInt(t -> t.getQuantiteLivree() == null ? 0 : t.getQuantiteLivree()).sum();
                    doc.add(new Paragraph("Livrée sous : le N° " + debut + " à " + fin
                        + " (" + NUM.format(totalLivre) + " cartes brunes)", F_BOLD));
                }
            }

            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);

            // ── Date + signataire alignés à droite ───────────────────────────────
            String ville = param("BNCB_VILLE", "Lomé");
            LocalDate dateRef = f.getDateFacture() != null ? f.getDateFacture() : LocalDate.now();
            Paragraph dateP = new Paragraph("Fait à " + ville + ", le " + DATE_FR.format(dateRef), F_NORMAL);
            dateP.setAlignment(Element.ALIGN_RIGHT);
            doc.add(dateP);

            doc.add(Chunk.NEWLINE);

            Paragraph pourBn = new Paragraph("POUR LE BUREAU NATIONAL", F_BOLD);
            pourBn.setAlignment(Element.ALIGN_RIGHT);
            doc.add(pourBn);

            doc.add(Chunk.NEWLINE);
            doc.add(Chunk.NEWLINE);

            Paragraph signataire = new Paragraph(param("BNCB_SIGNATAIRE", "—"), F_BOLD);
            signataire.setAlignment(Element.ALIGN_RIGHT);
            doc.add(signataire);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Echec generation PDF facture : " + e.getMessage(), e);
        }
        return baos.toByteArray();
    }

    private void addHeadCell(PdfPTable t, String txt) {
        PdfPCell c = new PdfPCell(new Phrase(txt, F_TABLE_HEAD));
        c.setBackgroundColor(GRIS_HEAD);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        t.addCell(c);
    }
    private void addCell(PdfPTable t, String txt, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, F_TABLE_CELL));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        t.addCell(c);
    }

    @SuppressWarnings("unused")
    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
