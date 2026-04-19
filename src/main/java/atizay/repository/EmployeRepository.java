package atizay.repository;

import atizay.model.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {
    
    /**
     * Récupère un employé par son email
     */
    Employe findByEmail(String email);

    /**
     * Récupère les employés d'un salon
     */
    List<Employe> findBySalon(atizay.model.Salon salon);
    
    /**
     * Récupère les employés d'un salon
     */
    List<Employe> findBySalonIdSalon(Long idSalon);
    
    /**
     * Récupère les employés par spécialité
     */
    List<Employe> findBySpecialiteContainingIgnoreCase(String specialite);
    
    /**
     * Récupère les employés disponibles
     */
    List<Employe> findByDisponibleTrueAndActifEmployeTrue();
    
    /**
     * Récupère les employés qui peuvent réaliser une prestation
     */
    List<Employe> findByServicesRealisablesContaining(atizay.model.Prestation prestation);

    /**
     * Récupère un employé par son username
     */
    Employe findByUsername(String username);
}
