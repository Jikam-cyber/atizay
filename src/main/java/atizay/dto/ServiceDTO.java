package atizay.dto;

import java.util.List;

public class ServiceDTO {
    
    private Long idPrestation;
    private String nomPrestation;
    private String descriptionPrestation;
    private Integer dureeMinutes;
    private Double prix;
    private String categorie;
    private boolean actif;
    
    // Informations sur les employés qui proposent ce service
    private List<EmployeServiceDTO> employesDisponibles;
    
    // Médias du service (photos, vidéos)
    private List<MediaServiceDTO> medias;
    
    // Statistiques
    private Integer nombreRendezVous;
    private Double noteMoyenne;
    
    public ServiceDTO() {}
    
    public ServiceDTO(Long idPrestation, String nomPrestation, Integer dureeMinutes, 
                    Double prix, String categorie) {
        this.idPrestation = idPrestation;
        this.nomPrestation = nomPrestation;
        this.dureeMinutes = dureeMinutes;
        this.prix = prix;
        this.categorie = categorie;
    }
    
    // Getters et Setters
    public Long getIdPrestation() {
        return idPrestation;
    }
    
    public void setIdPrestation(Long idPrestation) {
        this.idPrestation = idPrestation;
    }
    
    public String getNomPrestation() {
        return nomPrestation;
    }
    
    public void setNomPrestation(String nomPrestation) {
        this.nomPrestation = nomPrestation;
    }
    
    public String getDescriptionPrestation() {
        return descriptionPrestation;
    }
    
    public void setDescriptionPrestation(String descriptionPrestation) {
        this.descriptionPrestation = descriptionPrestation;
    }
    
    public Integer getDureeMinutes() {
        return dureeMinutes;
    }
    
    public void setDureeMinutes(Integer dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }
    
    public Double getPrix() {
        return prix;
    }
    
    public void setPrix(Double prix) {
        this.prix = prix;
    }
    
    public String getCategorie() {
        return categorie;
    }
    
    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public List<EmployeServiceDTO> getEmployesDisponibles() {
        return employesDisponibles;
    }
    
    public void setEmployesDisponibles(List<EmployeServiceDTO> employesDisponibles) {
        this.employesDisponibles = employesDisponibles;
    }
    
    public List<MediaServiceDTO> getMedias() {
        return medias;
    }
    
    public void setMedias(List<MediaServiceDTO> medias) {
        this.medias = medias;
    }
    
    public Integer getNombreRendezVous() {
        return nombreRendezVous;
    }
    
    public void setNombreRendezVous(Integer nombreRendezVous) {
        this.nombreRendezVous = nombreRendezVous;
    }
    
    public Double getNoteMoyenne() {
        return noteMoyenne;
    }
    
    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }
}
