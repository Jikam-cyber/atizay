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

    /**
     * Récupère les salons par nom ou ville
     */
    List<Salon> findByNomSalonContainingIgnoreCaseOrVilleSalonContainingIgnoreCase(String nomSalon, String villeSalon);

    /**
     * Recherche avancée de salons par nom/prestation et lieu
     */
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM Salon s LEFT JOIN s.listePrestations p WHERE " +
           "(LOWER(s.nomSalon) LIKE LOWER(CONCAT('%', :quoi, '%')) OR LOWER(p.nomPrestation) LIKE LOWER(CONCAT('%', :quoi, '%')) OR LOWER(s.typeSalon) LIKE LOWER(CONCAT('%', :quoi, '%'))) AND " +
           "(LOWER(s.villeSalon) LIKE LOWER(CONCAT('%', :ou, '%')) OR LOWER(s.adresseSalon) LIKE LOWER(CONCAT('%', :ou, '%'))) " +
           "ORDER BY s.noteMoyenne DESC")
    List<Salon> searchSalons(@org.springframework.data.repository.query.Param("quoi") String quoi, @org.springframework.data.repository.query.Param("ou") String ou);
}
