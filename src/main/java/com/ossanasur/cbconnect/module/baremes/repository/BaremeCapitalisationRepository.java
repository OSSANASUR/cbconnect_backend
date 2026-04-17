package com.ossanasur.cbconnect.module.baremes.repository;
import com.ossanasur.cbconnect.module.baremes.entity.BaremeCapitalisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface BaremeCapitalisationRepository extends JpaRepository<BaremeCapitalisation, Integer> {
    @Query("SELECT b FROM BaremeCapitalisation b WHERE b.typeBareme=:type AND b.age=:age AND b.actif=true")
    Optional<BaremeCapitalisation> findByTypeAndAge(@Param("type") String type, @Param("age") int age);
    @Query("SELECT b FROM BaremeCapitalisation b WHERE b.typeBareme=:type AND b.age<=:age AND b.actif=true ORDER BY b.age DESC LIMIT 1")
    Optional<BaremeCapitalisation> findByTypeAndAgeClose(@Param("type") String type, @Param("age") int age);
}
