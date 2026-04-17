package com.ossanasur.cbconnect.module.comptabilite.repository;
import com.ossanasur.cbconnect.common.enums.TypeTransactionComptable;
import com.ossanasur.cbconnect.module.comptabilite.entity.RegleEcriture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface RegleEcritureRepository extends JpaRepository<RegleEcriture, Integer> {
    Optional<RegleEcriture> findByTypeTransactionAndActifTrue(TypeTransactionComptable type);
}
