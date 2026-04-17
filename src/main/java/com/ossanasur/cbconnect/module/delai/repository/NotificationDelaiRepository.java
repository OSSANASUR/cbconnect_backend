package com.ossanasur.cbconnect.module.delai.repository;
import com.ossanasur.cbconnect.module.delai.entity.NotificationDelai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate; import java.util.List; import java.util.UUID;
@Repository
public interface NotificationDelaiRepository extends JpaRepository<NotificationDelai, Integer> {
    @Query("SELECT n FROM NotificationDelai n WHERE n.statut NOT IN ('RESOLU','ANNULE') AND n.dateEcheance<=:date ORDER BY n.dateEcheance")
    List<NotificationDelai> findEchus(@Param("date") LocalDate date);
    @Query("SELECT n FROM NotificationDelai n WHERE n.sinistre.sinistreTrackingId=:sid AND n.statut NOT IN ('RESOLU','ANNULE')")
    List<NotificationDelai> findActiveBySinistre(@Param("sid") UUID sinistreId);
    @Query("SELECT n FROM NotificationDelai n WHERE n.responsable.utilisateurTrackingId=:uid AND n.statut NOT IN ('RESOLU','ANNULE') ORDER BY n.dateEcheance")
    List<NotificationDelai> findActiveByResponsable(@Param("uid") UUID utilisateurId);
    @Query("SELECT n FROM NotificationDelai n WHERE n.niveauAlerte IN ('URGENT','CRITIQUE') AND n.statut NOT IN ('RESOLU','ANNULE') ORDER BY n.dateEcheance")
    List<NotificationDelai> findUrgents();
}
