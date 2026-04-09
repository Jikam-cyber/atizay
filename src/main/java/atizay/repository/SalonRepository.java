package atizay.repository;

import atizay.model.Salon;
import atizay.model.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Long> {

    /**
     * Récupère un salon par son nom
     */
    List<Salon> findByNomSalonContainingIgnoreCase(String nomSalon);

    /**
     * Récupère un salon par son slug
     */
    Salon findBySlug(String slug);

    /**
     * Récupère les salons d'un propriétaire
     */
    List<Salon> findByProprietaire(Proprietaire proprietaire);
}
