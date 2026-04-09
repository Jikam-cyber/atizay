package atizay.repository;

import atizay.model.Prestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrestationRepository extends JpaRepository<Prestation, Long> {
    
    /**
     * Récupère les prestations d'un salon
     */
    List<Prestation> findBySalon(atizay.model.Salon salon);

    /**
     * Récupère les prestations d'un salon par son ID
     */
    List<Prestation> findBySalonIdSalon(Long idSalon);
    
    /**
     * Récupère les prestations par nom
     */
    List<Prestation> findByNomPrestationContainingIgnoreCase(String nomPrestation);
    
    /**
     * Récupère les prestations d'un salon par nom
     */
    List<Prestation> findBySalonIdSalonAndNomPrestationContainingIgnoreCase(Long idSalon, String nomPrestation);
    
    /**
     * Récupère les prestations actives
     */
    List<Prestation> findByActifTrue();
}
