package com.ossanasur.cbconnect.module.auth.repository;

import com.ossanasur.cbconnect.module.auth.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParametreRepository extends JpaRepository<Parametre, Integer> {
    @Query("SELECT p FROM Parametre p WHERE p.cle = :cle AND p.activeData = true AND p.deletedData = false")
    Optional<Parametre> findByCle(@Param("cle") String cle);

    @Query("SELECT p FROM Parametre p WHERE p.activeData = true AND p.deletedData = false ORDER BY p.typeParametre, p.cle")
    List<Parametre> findAllActive();

    boolean existsByCleAndActiveDataTrueAndDeletedDataFalse(String cle);

    /**
     * Récupère tous les paramètres d'une catégorie LISTE.
     * Ex: findByCategorie("PROFESSION") retourne toutes les professions.
     *
     * Convention : cle = "{CATEGORIE}.{CODE}"
     */
    @Query("SELECT p FROM Parametre p " +
            "WHERE p.cle LIKE CONCAT(:categorie, '.%') " +
            "AND p.activeData = true AND p.deletedData = false " +
            "ORDER BY p.valeur")
    List<Parametre> findByCategorie(@Param("categorie") String categorie);

    /**
     * Récupère tous les paramètres de type LISTE, groupés par catégorie.
     */
    @Query("SELECT p FROM Parametre p " +
            "WHERE p.typeParametre = com.ossanasur.cbconnect.common.enums.TypeParametre.LISTE " +
            "AND p.activeData = true AND p.deletedData = false " +
            "ORDER BY p.cle")
    List<Parametre> findAllListe();
}
