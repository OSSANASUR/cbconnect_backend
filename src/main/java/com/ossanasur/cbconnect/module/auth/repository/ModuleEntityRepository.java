package com.ossanasur.cbconnect.module.auth.repository;
import com.ossanasur.cbconnect.module.auth.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleEntityRepository extends JpaRepository<ModuleEntity, Integer> {
    @Query("SELECT m FROM ModuleEntity m WHERE m.moduleTrackingId = :id AND m.activeData = true AND m.deletedData = false")
    Optional<ModuleEntity> findActiveByTrackingId(@Param("id") UUID id);
    @Query("SELECT m FROM ModuleEntity m WHERE m.activeData = true AND m.deletedData = false AND m.actif = true ORDER BY m.nomModule")
    List<ModuleEntity> findAllActifs();
}
