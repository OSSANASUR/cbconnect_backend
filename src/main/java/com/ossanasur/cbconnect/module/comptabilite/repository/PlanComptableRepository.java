package com.ossanasur.cbconnect.module.comptabilite.repository;
import com.ossanasur.cbconnect.module.comptabilite.entity.PlanComptable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface PlanComptableRepository extends JpaRepository<PlanComptable, Integer> {
    Optional<PlanComptable> findByNumeroCompteAndActifTrue(String numero);
}
