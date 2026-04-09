package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "prestation")
public class Prestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrestation;

    @Column(name = "nom_prestation", nullable = false)
    private String nomPrestation;

    @Column(name = "description_prestation", columnDefinition = "TEXT")
    private String descriptionPrestation;

    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    @Column(name = "prix", nullable = false)
    private Double prix;

    @Column(name = "categorie", nullable = false)
    private String categorie; // Coupe, Barbe, Coloration, Soins, etc.

    @Column(name = "actif")
    private boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = false)
    private Salon salon;

    @ManyToMany
    @JoinTable(
        name = "prestation_categorie",
        joinColumns = @JoinColumn(name = "id_prestation"),
        inverseJoinColumns = @JoinColumn(name = "id_categorie")
    )
    private List<CategoriePrestation> categories;

    @OneToMany(mappedBy = "prestation", cascade = CascadeType.ALL)
    private List<MediaPrestation> medias;

    public Prestation() {}

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

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public List<CategoriePrestation> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoriePrestation> categories) {
        this.categories = categories;
    }

    public List<MediaPrestation> getMedias() {
        return medias;
    }

    public void setMedias(List<MediaPrestation> medias) {
        this.medias = medias;
    }

    public static List<Prestation> findBySalonIdSalonAndNomPrestationContainingIgnoreCase(Long idSalon, String motCle) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findBySalonIdSalonAndNomPrestationContainingIgnoreCase'");
    }
}
