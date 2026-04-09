package atizay.repository;

import atizay.model.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProprietaireRepository extends JpaRepository<Proprietaire, Long> {
    
    /**
     * Recherche un propriétaire par email et mot de passe
     */
    Proprietaire findByEmailAndPassword(String email, String password);
    
    /**
     * Recherche un propriétaire par email
     */
    Proprietaire findByEmail(String email);
    
    /**
     * Recherche un propriétaire par username
     */
    Proprietaire findByUsername(String username);
}
