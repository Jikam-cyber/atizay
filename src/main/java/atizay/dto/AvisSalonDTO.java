package atizay.dto;

import java.time.LocalDateTime;

public class AvisSalonDTO {
    
    private Long idAvis;
    private Integer note;
    private LocalDateTime dateAvis;
    
    // Informations du client
    private String nomClient;
    
    // Informations du salon
    private Long idSalon;
    private String nomSalon;
    
    public AvisSalonDTO() {}
    
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
}
