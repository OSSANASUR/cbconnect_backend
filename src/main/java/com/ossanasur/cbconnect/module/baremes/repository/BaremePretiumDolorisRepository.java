package com.ossanasur.cbconnect.module.baremes.repository;
import com.ossanasur.cbconnect.module.baremes.entity.BaremePretiumDoloris;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaremePretiumDolorisRepository extends JpaRepository<BaremePretiumDoloris, Integer> {
    Optional<BaremePretiumDoloris> findByQualification(String qualification);

    @Query("SELECT b FROM BaremePretiumDoloris b ORDER BY b.points")
    List<BaremePretiumDoloris> findAllOrdered();
}
