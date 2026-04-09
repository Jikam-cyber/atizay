package atizay.service;

import atizay.dto.*;
import atizay.model.*;
import atizay.repository.SalonRepository;
import atizay.repository.PrestationRepository;
import atizay.repository.EmployeRepository;
import atizay.repository.MediaPrestationRepository;
import atizay.repository.CategoriePrestationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalonServiceService {

    @Autowired
    private SalonRepository salonRepository;
    
    @Autowired
    private PrestationRepository prestationRepository;
    
    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private MediaPrestationRepository mediaPrestationRepository;
    
    @Autowired
    private CategoriePrestationRepository categoriePrestationRepository;

    /**
     * Récupère tous les services d'un salon avec détails complets
     */
    public SalonServiceDTO getServicesBySalon(Long idSalon) {
        Optional<Salon> salonOpt = salonRepository.findById(idSalon);
        if (!salonOpt.isPresent()) {
            return null;
        }
        
        Salon salon = salonOpt.get();
        SalonServiceDTO dto = new SalonServiceDTO();
        
        // Informations du salon
        dto.setIdSalon(salon.getIdSalon());
        dto.setNomSalon(salon.getNomSalon());
        dto.setAdresseSalon(salon.getAdresseSalon());
        dto.setTelephoneSalon(salon.getTelephoneSalon());
        dto.setNoteMoyenne(salon.getNoteMoyenne());
        dto.setNomProprietaire(salon.getProprietaire().getNom() + " " + salon.getProprietaire().getPrenom());
        
        // Récupérer les services du salon
        List<Prestation> prestations = prestationRepository.findBySalonIdSalon(idSalon);
        List<ServiceDTO> servicesDTO = prestations.stream()
            .map(this::convertToServiceDTO)
            .collect(Collectors.toList());
        dto.setServices(servicesDTO);
        
        // Récupère toutes les catégories
        List<CategoriePrestation> categories = categoriePrestationRepository.findAllByOrderByOrdreAffichageAsc();
        List<CategorieServiceDTO> categoriesDTO = categories.stream()
            .map(this::convertToCategorieDTO)
            .collect(Collectors.toList());
        dto.setCategories(categoriesDTO);
        
        // Statistiques
        dto.setNombreTotalServices(prestations.size());
        dto.setPrixMoyenServices(calculerPrixMoyen(prestations));
        dto.setNombreEmployes(salon.getListeEmployes() != null ? salon.getListeEmployes().size() : 0);
        
        return dto;
    }
    
    /**
     * Récupère tous les services disponibles par catégorie pour un salon
     */
    public Map<String, List<ServiceDTO>> getServicesByCategorie(Long idSalon) {
        List<Prestation> prestations = prestationRepository.findBySalonIdSalon(idSalon);
        
        return prestations.stream()
            .collect(Collectors.groupingBy(
                Prestation::getCategorie,
                Collectors.mapping(this::convertToServiceDTO, Collectors.toList())
            ));
    }
    
    /**
     * Recherche de services par nom ou catégorie
     */
    public List<ServiceDTO> rechercherServices(Long idSalon, String motCle) {
        List<Prestation> prestations = Prestation
            .findBySalonIdSalonAndNomPrestationContainingIgnoreCase(idSalon, motCle);
        
        return prestations.stream()
            .map(this::convertToServiceDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Convertit une Prestation en ServiceDTO
     */
    private ServiceDTO convertToServiceDTO(Prestation prestation) {
        ServiceDTO dto = new ServiceDTO();
        dto.setIdPrestation(prestation.getIdPrestation());
        dto.setNomPrestation(prestation.getNomPrestation());
        dto.setDescriptionPrestation(prestation.getDescriptionPrestation());
        dto.setDureeMinutes(prestation.getDureeMinutes());
        dto.setPrix(prestation.getPrix());
        dto.setCategorie(prestation.getCategorie());
        dto.setActif(prestation.isActif());
        
        // Médias du service
        if (prestation.getMedias() != null) {
            List<MediaServiceDTO> mediasDTO = prestation.getMedias().stream()
                .map(this::convertToMediaDTO)
                .collect(Collectors.toList());
            dto.setMedias(mediasDTO);
        }
        
        // Employés qui proposent ce service
        List<Employe> employes = employeRepository.findByServicesRealisablesContaining(prestation);
        if (employes != null) {
            List<EmployeServiceDTO> employesDTO = employes.stream()
                .map(this::convertToEmployeDTO)
                .collect(Collectors.toList());
            dto.setEmployesDisponibles(employesDTO);
        }
        
        return dto;
    }
    
    /**
     * Convertit une CategoriePrestation en CategorieServiceDTO
     */
    private CategorieServiceDTO convertToCategorieDTO(CategoriePrestation categorie) {
        CategorieServiceDTO dto = new CategorieServiceDTO();
        dto.setIdCategorie(categorie.getIdCategorie());
        dto.setNomCategorie(categorie.getNomCategorie());
        dto.setDescriptionCategorie(categorie.getDescriptionCategorie());
        dto.setIcone(categorie.getIcone());
        dto.setOrdreAffichage(categorie.getOrdreAffichage());
        dto.setActif(categorie.isActif());
        return dto;
    }
    
    /**
     * Convertit un MediaPrestation en MediaServiceDTO
     */
    private MediaServiceDTO convertToMediaDTO(MediaPrestation media) {
        MediaServiceDTO dto = new MediaServiceDTO();
        dto.setIdMedia(media.getIdMedia());
        dto.setUrlMedia(media.getUrlMedia());
        dto.setTypeMedia(media.getTypeMedia());
        dto.setLegende(media.getLegende());
        dto.setOrdreAffichage(media.getOrdreAffichage());
        return dto;
    }
    
    /**
     * Convertit un Employe en EmployeServiceDTO
     */
    private EmployeServiceDTO convertToEmployeDTO(Employe employe) {
        EmployeServiceDTO dto = new EmployeServiceDTO();
        dto.setIdEmploye(employe.getId());
        dto.setNomComplet(employe.getNom() + " " + employe.getPrenom());
        dto.setSpecialite(employe.getSpecialite());
        dto.setExperienceAnnees(employe.getExperienceAnnees());
        dto.setNoteMoyenne(employe.getNoteMoyenne());
        dto.setDisponible(employe.getDisponible());
        dto.setNombreServicesRealises(employe.getNombreServicesRealises());
        return dto;
    }
    
    /**
     * Calcule le prix moyen des prestations
     */
    private Double calculerPrixMoyen(List<Prestation> prestations) {
        return prestations.stream()
            .mapToDouble(Prestation::getPrix)
            .average()
            .orElse(0.0);
    }
}
