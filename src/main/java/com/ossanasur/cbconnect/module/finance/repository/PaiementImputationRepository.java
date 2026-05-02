package com.ossanasur.cbconnect.module.finance.repository;

import com.ossanasur.cbconnect.module.finance.entity.PaiementImputation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementImputationRepository extends JpaRepository<PaiementImputation, Integer> {

    @Query(nativeQuery = true, value = """
        SELECT * FROM paiement_imputation
        WHERE imputation_tracking_id = :id
          AND active_data = TRUE AND deleted_data = FALSE
        """)
    Optional<PaiementImputation> findActiveByTrackingId(@Param("id") UUID id);

    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(pi.montant_impute), 0)
        FROM paiement_imputation pi
        WHERE pi.encaissement_id = :encId
          AND pi.active_data = TRUE AND pi.deleted_data = FALSE
        """)
    BigDecimal sumImputationsByEncaissement(@Param("encId") Integer encaissementId);

    @Query(nativeQuery = true, value = """
        SELECT COALESCE(SUM(pi.montant_impute), 0)
        FROM paiement_imputation pi
        WHERE pi.paiement_id = :paiementId
          AND pi.active_data = TRUE AND pi.deleted_data = FALSE
        """)
    BigDecimal sumImputationsByPaiement(@Param("paiementId") Integer paiementId);

    @Query(nativeQuery = true, value = """
        SELECT pi.* FROM paiement_imputation pi
        WHERE pi.encaissement_id = :encId
          AND pi.active_data = TRUE AND pi.deleted_data = FALSE
        ORDER BY pi.created_at ASC
        """)
    List<PaiementImputation> findActiveByEncaissement(@Param("encId") Integer encId);

    @Query(nativeQuery = true, value = """
        SELECT pi.* FROM paiement_imputation pi
        WHERE pi.paiement_id = :paiementId
          AND pi.active_data = TRUE AND pi.deleted_data = FALSE
        ORDER BY pi.created_at ASC
        """)
    List<PaiementImputation> findActiveByPaiement(@Param("paiementId") Integer paiementId);

    @Query(nativeQuery = true, value = """
        SELECT pi.* FROM paiement_imputation pi
        JOIN paiement p ON p.historique_id = pi.paiement_id
        JOIN sinistre s ON s.historique_id = p.sinistre_id
        WHERE s.sinistre_tracking_id = :sid
          AND pi.active_data = TRUE AND pi.deleted_data = FALSE
        """)
    List<PaiementImputation> findActiveBySinistreTrackingId(@Param("sid") UUID sinistreTrackingId);
}
