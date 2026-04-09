package atizay.service;

import atizay.dto.*;
import atizay.model.*;
import atizay.repository.AvisEmployeRepository;
import atizay.repository.AvisSalonRepository;
import atizay.repository.EmployeRepository;
import atizay.repository.SalonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvisService {

    @Autowired
    private AvisSalonRepository avisSalonRepository;
    
    @Autowired
    private AvisEmployeRepository avisEmployeRepository;
    
    @Autowired
    private SalonRepository salonRepository;
    
    /**
     * Récupère tous les avis d'un salon avec statistiques
     */
    public List<AvisSalonDTO> getAvisBySalon(Long idSalon) {
        List<AvisSalon> avis = avisSalonRepository.findBySalonIdSalonOrderByDateAvisDesc(idSalon);
        
        return avis.stream()
            .map(this::convertToAvisSalonDTO)
            .collect(Collectors.toList());
    }

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
     * Crée un nouvel avis pour un salon
     */
    public AvisSalonDTO creerAvisSalon(AvisSalon avis) {
        // Validation
        if (avis.getNote() < 1 || avis.getNote() > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        
        // Sauvegarde
        AvisSalon savedAvis = avisSalonRepository.save(avis);
        
        // Mettre à jour la note moyenne du salon
        updateNoteMoyenneSalon(avis.getSalon().getIdSalon());
        
        return convertToAvisSalonDTO(savedAvis);
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
        
        // Mettre à jour la note moyenne du salon
        updateNoteMoyenneSalon(avis.getEmploye().getSalon().getIdSalon());
        
        return convertToAvisEmployeDTO(savedAvis);
    }

    
    /**
     * Récupère les statistiques d'avis pour un salon
     */
    public Map<String, Object> getStatistiquesAvisSalon(Long idSalon) {
        List<AvisSalon> avis = avisSalonRepository.findBySalonIdSalon(idSalon);
        
        Map<String, Object> stats = new HashMap<>();
        
        if (avis.isEmpty()) {
            stats.put("totalAvis", 0);
            stats.put("noteMoyenne", 0.0);
            stats.put("distributionNotes", Map.of());
            return stats;
        }
        
        // Note moyenne
        double noteMoyenne = avis.stream()
            .mapToInt(AvisSalon::getNote)
            .average()
            .orElse(0.0);
        
        // Distribution des notes
        Map<Integer, Long> distribution = avis.stream()
            .collect(Collectors.groupingBy(AvisSalon::getNote, Collectors.counting()));
        
        stats.put("totalAvis", avis.size());
        stats.put("noteMoyenne", Math.round(noteMoyenne * 10.0) / 10.0);
        stats.put("distributionNotes", distribution);
        
        return stats;
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

    /**
     * Met à jour la note moyenne d'un salon
     */
    private void updateNoteMoyenneSalon(Long idSalon) {
        List<AvisSalon> avis = avisSalonRepository.findBySalonIdSalon(idSalon);
        
        if (!avis.isEmpty()) {
            double noteMoyenne = avis.stream()
                .mapToInt(AvisSalon::getNote)
                .average()
                .orElse(0.0);
            
            Optional<Salon> salonOpt = salonRepository.findById(idSalon);
            if (salonOpt.isPresent()) {
                Salon salon = salonOpt.get();
                salon.setNoteMoyenne(Math.round(noteMoyenne * 10.0) / 10.0);
                salonRepository.save(salon);
            }
        }
    }
}
