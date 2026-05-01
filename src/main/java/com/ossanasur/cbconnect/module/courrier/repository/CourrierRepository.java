package com.ossanasur.cbconnect.module.courrier.repository;

import com.ossanasur.cbconnect.module.courrier.entity.Courrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourrierRepository extends JpaRepository<Courrier, Integer> {
    @Query("SELECT c FROM Courrier c WHERE c.courrierTrackingId=:id AND c.activeData=true AND c.deletedData=false")
    Optional<Courrier> findActiveByTrackingId(@Param("id") UUID id);

    @Query("SELECT c FROM Courrier c WHERE c.sinistre.sinistreTrackingId=:sid AND c.activeData=true AND c.deletedData=false ORDER BY c.dateCourrier DESC")
    List<Courrier> findBySinistre(@Param("sid") UUID sinistreId);

    @Query("SELECT c FROM Courrier c WHERE c.traite=false AND c.activeData=true AND c.deletedData=false ORDER BY c.dateCourrier ASC")
    List<Courrier> findNonTraites();

    @Query("SELECT c FROM Courrier c WHERE c.activeData=true AND c.deletedData=false ORDER BY c.dateCourrier DESC")
    List<Courrier> findAllActive();

    // Courriers sortants envoyés via messagerie CBConnect, triés par date d'envoi
    // desc
    @Query("SELECT c FROM Courrier c WHERE c.typeCourrier = 'SORTANT' AND c.envoyeParMail = true " +
            "AND c.activeData = true AND c.deletedData = false ORDER BY c.dateEnvoi DESC")
    List<Courrier> findSortantsEnvoyesParMail();

    /** Courriers SORTANTS transportés par coursier ou poste, prêts à embarquer (pas encore attachés à un bordereau). */
    @Query("SELECT c FROM Courrier c WHERE c.typeCourrier = 'SORTANT' " +
            "AND c.canal IN ('PHYSIQUE','COURRIER_POSTAL') AND c.bordereau IS NULL " +
            "AND c.activeData = true AND c.deletedData = false " +
            "ORDER BY c.dateCourrier ASC")
    List<Courrier> findSortantsPretsAEmbarquer();

    /** Courriers SORTANTS transportés (PHYSIQUE/COURRIER_POSTAL) prêts pour un destinataire homologue précis. */
    @Query("SELECT c FROM Courrier c WHERE c.typeCourrier = 'SORTANT' " +
            "AND c.canal IN ('PHYSIQUE','COURRIER_POSTAL') " +
            "AND c.bordereau IS NULL " +
            "AND c.destinataireOrganisme.organismeTrackingId = :destId " +
            "AND c.activeData = true AND c.deletedData = false " +
            "ORDER BY c.dateCourrier ASC")
    List<Courrier> findSortantsPretsPourDestinataire(@Param("destId") UUID destinataireOrganismeId);

    /** Courriers rattachés à un bordereau, dans l'ordre d'impression. */
    @Query("SELECT c FROM Courrier c WHERE c.bordereau.bordereauTrackingId = :bid " +
            "AND c.activeData = true AND c.deletedData = false " +
            "ORDER BY c.ordreDansBordereau ASC")
    List<Courrier> findByBordereau(@Param("bid") UUID bordereauTrackingId);

    /** Courriers d'un registre journalier (ARRIVEE ou DEPART). */
    @Query("SELECT c FROM Courrier c WHERE c.registreJour.registreTrackingId = :rid " +
            "AND c.activeData = true AND c.deletedData = false " +
            "ORDER BY c.createdAt ASC")
    List<Courrier> findByRegistre(@Param("rid") UUID registreTrackingId);
}
