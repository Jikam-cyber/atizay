package atizay.repository;

import atizay.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    /**
     * Recherche un client par email et mot de passe
     */
    Client findByEmailAndPassword(String email, String password);
    
    /**
     * Recherche un client par email
     */
    Client findByEmail(String email);
    
    /**
     * Recherche un client par username
     */
    Client findByUsername(String username);
}
