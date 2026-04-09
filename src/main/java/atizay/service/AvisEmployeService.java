package atizay.service;

import atizay.dto.AvisEmployeDTO;
import atizay.model.AvisEmploye;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvisEmployeService {

    @Autowired
    private atizay.repository.AvisEmployeRepository avisEmployeRepository;

    /**
     * Récupère tous les avis d'un employé
     */
    public List<AvisEmployeDTO> getAvisByEmploye(Long idEmploye) {
        List<AvisEmploye> avis = avisEmployeRepository.findByEmployeIdOrderByDateAvisDesc(idEmploye);
        
        return avis.stream()
            .map(this::convertToAvisEmployeDTO)
            .collect(Collectors.toList());
    }

    /**
     * Crée un nouvel avis pour un employé
     */
    public AvisEmployeDTO creerAvisEmploye(AvisEmploye avis) {
        // Validation
        if (avis.getNote() < 1 || avis.getNote() > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        
        // Sauvegarde
        AvisEmploye savedAvis = avisEmployeRepository.save(avis);
        
        return convertToAvisEmployeDTO(savedAvis);
    }

    /**
     * Convertit AvisEmploye en AvisEmployeDTO
     */
    private AvisEmployeDTO convertToAvisEmployeDTO(AvisEmploye avis) {
        AvisEmployeDTO dto = new AvisEmployeDTO();
        
        dto.setIdAvis(avis.getIdAvis());
        dto.setNote(avis.getNote());
        dto.setDateAvis(avis.getDateAvis());
        
        // Client
        dto.setNomClient(avis.getNomClient());
        
        // Employé
        if (avis.getEmploye() != null) {
            dto.setIdEmploye(avis.getEmploye().getId());
            dto.setNomEmploye(avis.getNomEmploye());
        }
        
        return dto;
    }
}
