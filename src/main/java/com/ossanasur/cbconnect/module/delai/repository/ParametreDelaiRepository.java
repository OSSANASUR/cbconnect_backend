package com.ossanasur.cbconnect.module.delai.repository;
import com.ossanasur.cbconnect.common.enums.*;
import com.ossanasur.cbconnect.module.delai.entity.ParametreDelai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List; import java.util.Optional;
@Repository
public interface ParametreDelaiRepository extends JpaRepository<ParametreDelai, Integer> {
    Optional<ParametreDelai> findByCodeDelaiAndActifTrue(String codeDelai);
    @Query("SELECT p FROM ParametreDelai p WHERE p.typeSinistre=:type AND p.actif=true ORDER BY p.categorie, p.codeDelai")
    List<ParametreDelai> findAllActiveByType(@Param("type") TypeSinistre type);
}
