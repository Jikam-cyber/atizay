package atizay.repository;

import atizay.model.MediaSalon;
import atizay.model.Salon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MediaSalonRepository extends JpaRepository<MediaSalon, Long> {
    List<MediaSalon> findBySalon(Salon salon);
}
