package atizay.dto;

import java.util.List;

public class SalonServiceDTO {
    
    private Long idSalon;
    private String nomSalon;
    private String adresseSalon;
    private String telephoneSalon;
    private Double noteMoyenne;
    private String nomProprietaire;
    
    // Services du salon
    private List<ServiceDTO> services;
    
    // Catégories de services disponibles
    private List<CategorieServiceDTO> categories;
    
    // Statistiques
    private Integer nombreTotalServices;
    private Double prixMoyenServices;
    private Integer nombreEmployes;
    
    public SalonServiceDTO() {}
    
    public SalonServiceDTO(Long idSalon, String nomSalon, String adresseSalon, 
                         String telephoneSalon, Double noteMoyenne, String nomProprietaire) {
        this.idSalon = idSalon;
        this.nomSalon = nomSalon;
        this.adresseSalon = adresseSalon;
        this.telephoneSalon = telephoneSalon;
        this.noteMoyenne = noteMoyenne;
        this.nomProprietaire = nomProprietaire;
    }
    
    // Getters et Setters
    public Long getIdSalon() {
        return idSalon;
    }
    
    public void setIdSalon(Long idSalon) {
        this.idSalon = idSalon;
    }
    
    public String getNomSalon() {
        return nomSalon;
    }
    
    public void setNomSalon(String nomSalon) {
        this.nomSalon = nomSalon;
    }
    
    public String getAdresseSalon() {
        return adresseSalon;
    }
    
    public void setAdresseSalon(String adresseSalon) {
        this.adresseSalon = adresseSalon;
    }
    
    public String getTelephoneSalon() {
        return telephoneSalon;
    }
    
    public void setTelephoneSalon(String telephoneSalon) {
        this.telephoneSalon = telephoneSalon;
    }
    
    public Double getNoteMoyenne() {
        return noteMoyenne;
    }
    
    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }
    
    public String getNomProprietaire() {
        return nomProprietaire;
    }
    
    public void setNomProprietaire(String nomProprietaire) {
        this.nomProprietaire = nomProprietaire;
    }
    
    public List<ServiceDTO> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceDTO> services) {
        this.services = services;
    }
    
    public List<CategorieServiceDTO> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategorieServiceDTO> categories) {
        this.categories = categories;
    }
    
    public Integer getNombreTotalServices() {
        return nombreTotalServices;
    }
    
    public void setNombreTotalServices(Integer nombreTotalServices) {
        this.nombreTotalServices = nombreTotalServices;
    }
    
    public Double getPrixMoyenServices() {
        return prixMoyenServices;
    }
    
    public void setPrixMoyenServices(Double prixMoyenServices) {
        this.prixMoyenServices = prixMoyenServices;
    }
    
    public Integer getNombreEmployes() {
        return nombreEmployes;
    }
    
    public void setNombreEmployes(Integer nombreEmployes) {
        this.nombreEmployes = nombreEmployes;
    }
}
