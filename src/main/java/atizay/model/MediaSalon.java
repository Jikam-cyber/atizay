package atizay.model;

import jakarta.persistence.*;

@Entity
@Table(name = "media_salon")
public class MediaSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMediaSalon;

    @Column(name = "url_media", nullable = false)
    private String urlMedia;

    @Column(name = "type_media", nullable = false)
    private String typeMedia; // Photo, Vidéo

    @Column(name = "legende")
    private String legende;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage = 0;

    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = false)
    private Salon salon;

    public MediaSalon() {}

    // Getters et Setters
    public Long getIdMediaSalon() {
        return idMediaSalon;
    }

    public void setIdMediaSalon(Long idMediaSalon) {
        this.idMediaSalon = idMediaSalon;
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

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }
}
