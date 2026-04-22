package com.ossanasur.cbconnect.module.attestation.repository;
import com.ossanasur.cbconnect.module.attestation.entity.TrancheLivraisonAttestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface TrancheLivraisonAttestationRepository extends JpaRepository<TrancheLivraisonAttestation, Integer> {
    @Query("SELECT t FROM TrancheLivraisonAttestation t WHERE t.trancheTrackingId=:id AND t.activeData=true AND t.deletedData=false")
    Optional<TrancheLivraisonAttestation> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT t FROM TrancheLivraisonAttestation t WHERE t.commande.commandeTrackingId=:cmdId AND t.activeData=true AND t.deletedData=false ORDER BY t.dateLivraison DESC")
    List<TrancheLivraisonAttestation> findByCommande(@Param("cmdId") UUID cmdId);
    @Query("SELECT COALESCE(SUM(t.quantiteLivree),0) FROM TrancheLivraisonAttestation t WHERE t.commande.commandeTrackingId=:cmdId AND t.activeData=true AND t.deletedData=false")
    long sommeLivreParCommande(@Param("cmdId") UUID cmdId);
    @Query("SELECT COALESCE(SUM(t.quantiteLivree),0) FROM TrancheLivraisonAttestation t WHERE t.lot.lotTrackingId=:lotId AND t.activeData=true AND t.deletedData=false")
    long sommeLivreParLot(@Param("lotId") UUID lotId);
}
