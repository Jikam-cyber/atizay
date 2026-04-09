package atizay.service;

import atizay.dto.AvisSalonDTO;
import atizay.model.AvisSalon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvisSalonService {

    @Autowired
    private atizay.repository.AvisSalonRepository avisSalonRepository;

    /**
     * Récupère tous les avis d'un salon
     */
    public List<AvisSalonDTO> getAvisBySalon(Long idSalon) {
        List<AvisSalon> avis = avisSalonRepository.findBySalonIdSalonOrderByDateAvisDesc(idSalon);
        
        return avis.stream()
            .map(this::convertToAvisSalonDTO)
            .collect(Collectors.toList());
    }

    /**
     * Crée un nouvel avis pour un salon
     */
    public AvisSalonDTO creerAvisSalon(AvisSalon avis) {
        // Validation
        if (avis.getNote() < 1 || avis.getNote() > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        
        // Sauvegarde
        AvisSalon savedAvis = avisSalonRepository.save(avis);
        
        return convertToAvisSalonDTO(savedAvis);
    }

    /**
     * Convertit AvisSalon en AvisSalonDTO
     */
    private AvisSalonDTO convertToAvisSalonDTO(AvisSalon avis) {
        AvisSalonDTO dto = new AvisSalonDTO();
        
        dto.setIdAvis(avis.getIdAvis());
        dto.setNote(avis.getNote());
        dto.setDateAvis(avis.getDateAvis());
        
        // Client
        dto.setNomClient(avis.getNomClient());
        
        // Salon
        if (avis.getSalon() != null) {
            dto.setIdSalon(avis.getSalon().getIdSalon());
            dto.setNomSalon(avis.getSalon().getNomSalon());
        }
        
        return dto;
    }
}
