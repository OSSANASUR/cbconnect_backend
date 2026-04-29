package com.ossanasur.cbconnect.module.baremes.repository;
import com.ossanasur.cbconnect.module.baremes.entity.BaremePrejudiceMoral266;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaremePrejudiceMoral266Repository extends JpaRepository<BaremePrejudiceMoral266, Integer> {
    Optional<BaremePrejudiceMoral266> findByLienParente(String lienParente);

    @Query("SELECT b FROM BaremePrejudiceMoral266 b WHERE b.actif=true ORDER BY b.lienParente")
    List<BaremePrejudiceMoral266> findAllActif();
}
