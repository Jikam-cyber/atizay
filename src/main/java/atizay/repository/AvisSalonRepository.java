package atizay.repository;

import atizay.model.AvisSalon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvisSalonRepository extends JpaRepository<AvisSalon, Long> {
    
    /**
     * Récupère tous les avis d'un salon triés par date décroissante
     */
    List<AvisSalon> findBySalonIdSalonOrderByDateAvisDesc(Long idSalon);
    
    /**
     * Récupère tous les avis d'un salon
     */
    List<AvisSalon> findBySalonIdSalon(Long idSalon);
}
