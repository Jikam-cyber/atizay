package atizay.repository;

import atizay.model.Abonnement;
import atizay.model.Proprietaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {
    
    List<Abonnement> findByProprietaire(Proprietaire proprietaire);
    
    Optional<Abonnement> findByProprietaireAndStatut(Proprietaire proprietaire, String statut);
    
    Optional<Abonnement> findFirstByProprietaireOrderByDateDebutDesc(Proprietaire proprietaire);
}
