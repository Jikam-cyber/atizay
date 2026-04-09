package atizay.dto;

public class EmployeServiceDTO {
    
    private Long idEmploye;
    private String nomComplet;
    private String specialite;
    private Integer experienceAnnees;
    private Double noteMoyenne;
    private boolean disponible;
    private String photoProfil;
    
    // Disponibilités de l'employé
    private String disponibilites;
    
    // Nombre de services réalisés
    private Integer nombreServicesRealises;
    
    public EmployeServiceDTO() {}
    
    public EmployeServiceDTO(Long idEmploye, String nomComplet, String specialite, 
                           Integer experienceAnnees, Double noteMoyenne) {
        this.idEmploye = idEmploye;
        this.nomComplet = nomComplet;
        this.specialite = specialite;
        this.experienceAnnees = experienceAnnees;
        this.noteMoyenne = noteMoyenne;
    }
    
    // Getters et Setters
    public Long getIdEmploye() {
        return idEmploye;
    }
    
    public void setIdEmploye(Long idEmploye) {
        this.idEmploye = idEmploye;
    }
    
    public String getNomComplet() {
        return nomComplet;
    }
    
    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }
    
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    public Integer getExperienceAnnees() {
        return experienceAnnees;
    }
    
    public void setExperienceAnnees(Integer experienceAnnees) {
        this.experienceAnnees = experienceAnnees;
    }
    
    public Double getNoteMoyenne() {
        return noteMoyenne;
    }
    
    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public String getPhotoProfil() {
        return photoProfil;
    }
    
    public void setPhotoProfil(String photoProfil) {
        this.photoProfil = photoProfil;
    }
    
    public String getDisponibilites() {
        return disponibilites;
    }
    
    public void setDisponibilites(String disponibilites) {
        this.disponibilites = disponibilites;
    }
    
    public Integer getNombreServicesRealises() {
        return nombreServicesRealises;
    }
    
    public void setNombreServicesRealises(Integer nombreServicesRealises) {
        this.nombreServicesRealises = nombreServicesRealises;
    }
}
