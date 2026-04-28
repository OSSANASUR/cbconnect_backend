package com.ossanasur.cbconnect.module.delai.repository;

import com.ossanasur.cbconnect.module.delai.entity.ParametreSysteme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParametreSystemeRepository extends JpaRepository<ParametreSysteme, Integer> {
    Optional<ParametreSysteme> findByCleAndActifTrue(String cle);
    List<ParametreSysteme> findAllByActifTrueOrderByCle();
}
