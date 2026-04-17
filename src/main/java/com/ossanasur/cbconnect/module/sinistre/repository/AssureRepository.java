package com.ossanasur.cbconnect.module.sinistre.repository;
import com.ossanasur.cbconnect.module.sinistre.entity.Assure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; import java.util.UUID;
@Repository
public interface AssureRepository extends JpaRepository<Assure, Integer> {
    @Query("SELECT a FROM Assure a WHERE a.assureTrackingId=:id AND a.activeData=true AND a.deletedData=false")
    Optional<Assure> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT a FROM Assure a WHERE a.numeroAttestation=:num AND a.activeData=true AND a.deletedData=false")
    Optional<Assure> findByNumeroAttestation(@Param("num") String numero);
}
