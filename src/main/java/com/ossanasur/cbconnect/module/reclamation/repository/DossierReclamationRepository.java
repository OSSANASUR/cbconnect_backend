package com.ossanasur.cbconnect.module.reclamation.repository;

import com.ossanasur.cbconnect.module.reclamation.entity.DossierReclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DossierReclamationRepository extends JpaRepository<DossierReclamation, Integer> {
    @Query("SELECT d FROM DossierReclamation d WHERE d.dossierTrackingId=:id AND d.activeData=true AND d.deletedData=false")
    Optional<DossierReclamation> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT d FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false ORDER BY d.dateOuverture DESC")
    List<DossierReclamation> findByVictime(@Param("vid") UUID victimeId);

    @Query("SELECT COALESCE(SUM(d.montantTotalRetenu),0) FROM DossierReclamation d WHERE d.victime.victimeTrackingId=:vid AND d.activeData=true AND d.deletedData=false")
    Optional<BigDecimal> findMontantRetenuByVictime(@Param("vid") UUID victimeId);

    /**
     * État de réclamation — TOGO ENVERS LES HOMOLOGUES (sinistres ET).
     *
     * Pour chaque dossier de réclamation actif (non clôturé) :
     * - lié à un sinistre SURVENU_TOGO (ET)
     * - la compagnie responsable = sinistre.organisme_homologue
     * - groupement par pays_emetteur → organisme_homologue
     *
     * Le montant est proportionnel au taux RC :
     * montant_proportion = d.montant_total_reclame * s.taux_rc / 100
     * (si taux_rc IS NULL → on prend 100%)
     *
     * Colonnes renvoyées :
     * [0] pays_libelle [1] code_pays
     * [2] compagnie [3] statut_reclamation
     * [4] nb [5] montant_proportionne
     */
    @Query(value = """
            SELECT
                p.libelle                                                           AS pays_libelle,
                p.code_carte_brune                                                 AS code_pays,
                COALESCE(oh.raison_sociale, 'INCONNU')                             AS compagnie,
                COALESCE(d.statut_reclamation, 'AUTRES')                           AS statut_reclamation,
                COUNT(d.historique_id)                                             AS nb,
                COALESCE(
                    SUM(
                        d.montant_total_reclame
                        * COALESCE(s.taux_rc, 100) / 100
                    ), 0
                )                                                                  AS montant_proportionne
            FROM dossier_reclamation d
            JOIN sinistre s   ON s.historique_id = d.sinistre_id
                AND s.type_sinistre = 'SURVENU_TOGO'
                AND s.active_data   = TRUE AND s.deleted_data = FALSE
            JOIN pays p       ON p.historique_id = s.pays_emetteur_id
            LEFT JOIN organisme oh ON oh.historique_id = s.organisme_homologue_id
                AND oh.active_data  = TRUE AND oh.deleted_data = FALSE
            WHERE d.deleted_data     = FALSE
              AND d.active_data      = TRUE
              AND COALESCE(d.statut_reclamation, 'AUTRES') <> 'CLOTURE'
            GROUP BY p.libelle, p.code_carte_brune,
                     COALESCE(oh.raison_sociale, 'INCONNU'),
                     COALESCE(d.statut_reclamation, 'AUTRES')
            ORDER BY p.libelle, COALESCE(oh.raison_sociale, 'INCONNU')
            """, nativeQuery = true)
    List<Object[]> etatReclamationTogoVersHomologues();

    /**
     * État de réclamation — HOMOLOGUES ENVERS TOGO (sinistres TE).
     *
     * Pour chaque dossier de réclamation actif (non clôturé) :
     * - lié à un sinistre SURVENU_ETRANGER (TE)
     * - la compagnie responsable = sinistre.organisme_membre (compagnie togolaise)
     * - groupement par pays_gestionnaire → organisme_membre
     *
     * Colonnes renvoyées : mêmes indices que etatReclamationTogoVersHomologues.
     */
    @Query(value = """
            SELECT
                p.libelle                                                           AS pays_libelle,
                p.code_carte_brune                                                 AS code_pays,
                COALESCE(om.raison_sociale, 'AUTRES')                              AS compagnie,
                COALESCE(d.statut_reclamation, 'AUTRES')                           AS statut_reclamation,
                COUNT(d.historique_id)                                             AS nb,
                COALESCE(
                    SUM(
                        d.montant_total_reclame
                        * COALESCE(s.taux_rc, 100) / 100
                    ), 0
                )                                                                  AS montant_proportionne
            FROM dossier_reclamation d
            JOIN sinistre s   ON s.historique_id = d.sinistre_id
                AND s.type_sinistre = 'SURVENU_ETRANGER'
                AND s.active_data   = TRUE AND s.deleted_data = FALSE
            JOIN pays p       ON p.historique_id = s.pays_gestionnaire_id
            LEFT JOIN organisme om ON om.historique_id = s.organisme_membre_id
                AND om.active_data    = TRUE
                AND om.deleted_data   = FALSE
                AND om.type_organisme = 'COMPAGNIE_MEMBRE'
                AND om.code_pays_bcb  = 'TG'
            WHERE d.deleted_data     = FALSE
              AND d.active_data      = TRUE
              AND COALESCE(d.statut_reclamation, 'AUTRES') <> 'CLOTURE'
            GROUP BY p.libelle, p.code_carte_brune,
                     COALESCE(om.raison_sociale, 'AUTRES'),
                     COALESCE(d.statut_reclamation, 'AUTRES')
            ORDER BY p.libelle, COALESCE(om.raison_sociale, 'AUTRES')
            """, nativeQuery = true)
    List<Object[]> etatReclamationHomologuesVersTogo();

}
