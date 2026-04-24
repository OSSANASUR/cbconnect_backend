package com.ossanasur.cbconnect.module.baremes.repository;
import com.ossanasur.cbconnect.module.baremes.entity.BaremeCleRepartition265;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaremeCleRepartition265Repository extends JpaRepository<BaremeCleRepartition265, Integer> {
    Optional<BaremeCleRepartition265> findByCodeSituation(String codeSituation);

    @Query("SELECT b FROM BaremeCleRepartition265 b WHERE b.actif=true ORDER BY b.codeSituation")
    List<BaremeCleRepartition265> findAllActif();
}
