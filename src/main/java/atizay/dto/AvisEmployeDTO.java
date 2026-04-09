package atizay.dto;

import java.time.LocalDateTime;

public class AvisEmployeDTO {
    
    private Long idAvis;
    private Integer note;
    private LocalDateTime dateAvis;
    
    // Informations du client
    private String nomClient;
    
    // Informations de l'employé
    private Long idEmploye;
    private String nomEmploye;
    
    public AvisEmployeDTO() {}
    
    // Getters et Setters
    public Long getIdAvis() {
        return idAvis;
    }
    
    public void setIdAvis(Long idAvis) {
        this.idAvis = idAvis;
    }
    
    public Integer getNote() {
        return note;
    }
    
    public void setNote(Integer note) {
        this.note = note;
    }
    
    public LocalDateTime getDateAvis() {
        return dateAvis;
    }
    
    public void setDateAvis(LocalDateTime dateAvis) {
        this.dateAvis = dateAvis;
    }
    
    public String getNomClient() {
        return nomClient;
    }
    
    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }
    
    public Long getIdEmploye() {
        return idEmploye;
    }
    
    public void setIdEmploye(Long idEmploye) {
        this.idEmploye = idEmploye;
    }
    
    public String getNomEmploye() {
        return nomEmploye;
    }
    
    public void setNomEmploye(String nomEmploye) {
        this.nomEmploye = nomEmploye;
    }
}
