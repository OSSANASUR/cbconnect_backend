package com.ossanasur.cbconnect.module.auth.repository;
import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    @Query("SELECT p FROM Parametre p WHERE p.cle = :cle AND p.activeData = true AND p.deletedData = false")
    Optional<Parametre> findByCle(@Param("cle") String cle);
    @Query("SELECT p FROM Parametre p WHERE p.activeData = true AND p.deletedData = false ORDER BY p.typeParametre, p.cle")
    List<Parametre> findAllActive();
    boolean existsByCleAndActiveDataTrueAndDeletedDataFalse(String cle);
}
