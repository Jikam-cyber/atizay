package atizay.repository;

import atizay.model.RendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {
    List<RendezVous> findByClient_Id(Long idClient);
    List<RendezVous> findByClient_IdOrderByDateRendezVousDesc(Long idClient);

    List<RendezVous> findBySalon_IdSalon(Long idSalon);

    List<RendezVous> findBySalon(atizay.model.Salon salon);

    List<RendezVous> findByEmploye(atizay.model.Employe employe);

    List<RendezVous> findByEmployeId(Long id);
}
