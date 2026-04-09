package atizay.model;

import jakarta.persistence.*;

@Entity
@Table(name = "media_prestation")
public class MediaPrestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMedia;

    @Column(name = "url_media", nullable = false)
    private String urlMedia;

    @Column(name = "type_media", nullable = false)
    private String typeMedia; // Photo, Vidéo

    @Column(name = "legende")
    private String legende;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage = 0;

    @ManyToOne
    @JoinColumn(name = "id_prestation", nullable = false)
    private Prestation prestation;

    public MediaPrestation() {}

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

    public Prestation getPrestation() {
        return prestation;
    }

    public void setPrestation(Prestation prestation) {
        this.prestation = prestation;
    }
}
