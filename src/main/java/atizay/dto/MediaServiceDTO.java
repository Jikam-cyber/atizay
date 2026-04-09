package atizay.dto;

public class MediaServiceDTO {
    
    private Long idMedia;
    private String urlMedia;
    private String typeMedia; // Photo, Vidéo
    private String legende;
    private Integer ordreAffichage;
    
    public MediaServiceDTO() {}
    
    public MediaServiceDTO(Long idMedia, String urlMedia, String typeMedia) {
        this.idMedia = idMedia;
        this.urlMedia = urlMedia;
        this.typeMedia = typeMedia;
    }
    
    // Getters et Setters
    public Long getIdMedia() {
        return idMedia;
    }
    
    public void setIdMedia(Long idMedia) {
        this.idMedia = idMedia;
    }
    
    public String getUrlMedia() {
        return urlMedia;
    }
    
    public void setUrlMedia(String urlMedia) {
        this.urlMedia = urlMedia;
    }
    
    public String getTypeMedia() {
        return typeMedia;
    }
    
    public void setTypeMedia(String typeMedia) {
        this.typeMedia = typeMedia;
    }
    
    public String getLegende() {
        return legende;
    }
    
    public void setLegende(String legende) {
        this.legende = legende;
    }
    
    public Integer getOrdreAffichage() {
        return ordreAffichage;
    }
    
    public void setOrdreAffichage(Integer ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }
}
