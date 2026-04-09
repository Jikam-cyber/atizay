package atizay.repository;

import atizay.model.HoraireSalon;
import atizay.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface HoraireSalonRepository extends JpaRepository<HoraireSalon, Long> {
    List<HoraireSalon> findBySalonOrderByJourSemaine(Salon salon);
    
    @Transactional
    void deleteBySalon(Salon salon);
}
