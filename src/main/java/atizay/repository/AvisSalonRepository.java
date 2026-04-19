package atizay.repository;

import atizay.model.AvisSalon;
import atizay.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

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

    /**
     * Récupère l'avis d'un client pour un salon spécifique
     */
    Optional<AvisSalon> findBySalonIdSalonAndClient(Long idSalon, Client client);

    /**
     * Récupère les avis uniques par client pour un salon (un avis par client, le plus récent)
     */
    @Query("SELECT a FROM AvisSalon a WHERE a.salon.idSalon = :idSalon AND a.dateAvis = (SELECT MAX(a2.dateAvis) FROM AvisSalon a2 WHERE a2.salon.idSalon = :idSalon AND a2.client = a.client) ORDER BY a.dateAvis DESC")
    List<AvisSalon> findUniqueAvisBySalon(Long idSalon);
}
