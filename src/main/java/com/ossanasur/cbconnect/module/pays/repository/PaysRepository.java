package com.ossanasur.cbconnect.module.pays.repository;
import com.ossanasur.cbconnect.module.pays.entity.Pays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface PaysRepository extends JpaRepository<Pays, Integer> {
    @Query("SELECT p FROM Pays p WHERE p.paysTrackingId=:id AND p.activeData=true AND p.deletedData=false")
    Optional<Pays> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT p FROM Pays p WHERE p.codeIso=:code AND p.activeData=true AND p.deletedData=false")
    Optional<Pays> findByCodeIso(@Param("code") String code);
    @Query("SELECT p FROM Pays p WHERE p.codeCarteBrune=:code AND p.activeData=true AND p.deletedData=false")
    Optional<Pays> findByCodeCarteBrune(@Param("code") String code);
    @Query("SELECT p FROM Pays p WHERE p.activeData=true AND p.deletedData=false AND p.actif=true ORDER BY p.libelle")
    List<Pays> findAllActifs();

    // Recherche floue par libellé — utilisée par la reprise historique
    // quand le fichier Excel ne fournit que le nom du pays (ex: "Bénin").
    @Query("SELECT p FROM Pays p WHERE LOWER(p.libelle) LIKE LOWER(CONCAT('%', :libelle, '%')) " +
            "AND p.activeData=true AND p.deletedData=false")
    Optional<Pays> findByLibelleContainingIgnoreCase(@Param("libelle") String libelle);
}
