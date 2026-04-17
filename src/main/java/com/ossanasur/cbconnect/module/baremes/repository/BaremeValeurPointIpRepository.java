package com.ossanasur.cbconnect.module.baremes.repository;
import com.ossanasur.cbconnect.module.baremes.entity.BaremeValeurPointIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal; import java.util.Optional;
@Repository
public interface BaremeValeurPointIpRepository extends JpaRepository<BaremeValeurPointIp, Integer> {
    @Query("SELECT b FROM BaremeValeurPointIp b WHERE b.ageMin<=:age AND (b.ageMax IS NULL OR b.ageMax>=:age) AND b.ippMin<=:ipp AND b.ippMax>=:ipp AND b.actif=true")
    Optional<BaremeValeurPointIp> findByAgeAndIpp(@Param("age") int age, @Param("ipp") BigDecimal ipp);
}
