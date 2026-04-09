package atizay.repository;

import atizay.model.CategoriePrestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriePrestationRepository extends JpaRepository<CategoriePrestation, Long> {
    
    /**
     * Récupère les catégories actives
     */
    List<CategoriePrestation> findByActifTrueOrderByOrdreAffichageAsc();
    
    /**
     * Récupère toutes les catégories
     */
    List<CategoriePrestation> findAllByOrderByOrdreAffichageAsc();
}
