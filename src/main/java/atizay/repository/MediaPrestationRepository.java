package atizay.repository;

import atizay.model.MediaPrestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MediaPrestationRepository extends JpaRepository<MediaPrestation, Long> {
    
    /**
     * Récupère les médias d'une prestation
     */
    java.util.List<MediaPrestation> findByPrestationIdPrestationOrderByOrdreAffichage(Long idPrestation);
}
