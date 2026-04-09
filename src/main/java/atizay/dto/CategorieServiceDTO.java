package atizay.dto;

import java.util.List;

public class CategorieServiceDTO {
    
    private Long idCategorie;
    private String nomCategorie;
    private String descriptionCategorie;
    private String icone;
    private Integer ordreAffichage;
    private boolean actif;
    
    // Services dans cette catégorie
    private List<ServiceDTO> services;
    
    // Statistiques
    private Integer nombreServices;
    private Double prixMoyenServices;
    
    public CategorieServiceDTO() {}
    
    public CategorieServiceDTO(Long idCategorie, String nomCategorie, String icone) {
        this.idCategorie = idCategorie;
        this.nomCategorie = nomCategorie;
        this.icone = icone;
    }
    
    // Getters et Setters
    public Long getIdCategorie() {
        return idCategorie;
    }
    
    public void setIdCategorie(Long idCategorie) {
        this.idCategorie = idCategorie;
    }
    
    public String getNomCategorie() {
        return nomCategorie;
    }
    
    public void setNomCategorie(String nomCategorie) {
        this.nomCategorie = nomCategorie;
    }
    
    public String getDescriptionCategorie() {
        return descriptionCategorie;
    }
    
    public void setDescriptionCategorie(String descriptionCategorie) {
        this.descriptionCategorie = descriptionCategorie;
    }
    
    public String getIcone() {
        return icone;
    }
    
    public void setIcone(String icone) {
        this.icone = icone;
    }
    
    public Integer getOrdreAffichage() {
        return ordreAffichage;
    }
    
    public void setOrdreAffichage(Integer ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }
    
    public boolean isActif() {
        return actif;
    }
    
    public void setActif(boolean actif) {
        this.actif = actif;
    }
    
    public List<ServiceDTO> getServices() {
        return services;
    }
    
    public void setServices(List<ServiceDTO> services) {
        this.services = services;
    }
    
    public Integer getNombreServices() {
        return nombreServices;
    }
    
    public void setNombreServices(Integer nombreServices) {
        this.nombreServices = nombreServices;
    }
    
    public Double getPrixMoyenServices() {
        return prixMoyenServices;
    }
    
    public void setPrixMoyenServices(Double prixMoyenServices) {
        this.prixMoyenServices = prixMoyenServices;
    }
}
