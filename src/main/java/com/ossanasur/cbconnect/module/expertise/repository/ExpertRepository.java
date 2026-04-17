package com.ossanasur.cbconnect.module.expertise.repository;
import com.ossanasur.cbconnect.common.enums.TypeExpert;
import com.ossanasur.cbconnect.module.expertise.entity.Expert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional; import java.util.UUID;
@Repository
public interface ExpertRepository extends JpaRepository<Expert, Integer> {
    @Query("SELECT e FROM Expert e WHERE e.expertTrackingId=:id AND e.activeData=true AND e.deletedData=false")
    Optional<Expert> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT e FROM Expert e WHERE e.activeData=true AND e.deletedData=false AND e.actif=true AND e.typeExpert=:type ORDER BY e.nomComplet")
    List<Expert> findAllActifsByType(@Param("type") TypeExpert type);
}
