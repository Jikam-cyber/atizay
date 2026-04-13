package atizay.repository;

import atizay.model.Employe;
import atizay.model.HoraireEmploye;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoraireEmployeRepository extends JpaRepository<HoraireEmploye, Long> {
    List<HoraireEmploye> findByEmploye(Employe employe);
}
