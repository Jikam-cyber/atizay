package atizay.repository;

import atizay.model.AvisEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AvisEmployeRepository extends JpaRepository<AvisEmploye, Long> {
    
    /**
     * Récupère tous les avis d'un employé triés par date décroissante
     */
    List<AvisEmploye> findByEmployeIdOrderByDateAvisDesc(Long idEmploye);
    
    /**
     * Récupère tous les avis d'un employé
     */
    List<AvisEmploye> findByEmployeId(Long idEmploye);
}
