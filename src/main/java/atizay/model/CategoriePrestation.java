package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categorie_prestation")
public class CategoriePrestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategorie;

    @Column(name = "nom_categorie", nullable = false, unique = true)
    private String nomCategorie;

    @Column(name = "description_categorie", columnDefinition = "TEXT")
    private String descriptionCategorie;

    @Column(name = "icone")
    private String icone; // Pour l'affichage dans l'interface

    @Column(name = "actif")
    private boolean actif = true;

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage = 0;

    @OneToMany(mappedBy = "categories", cascade = CascadeType.ALL)
    private List<Prestation> prestations;

    public CategoriePrestation() {}

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

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Integer getOrdreAffichage() {
        return ordreAffichage;
    }

    public void setOrdreAffichage(Integer ordreAffichage) {
        this.ordreAffichage = ordreAffichage;
    }

    public List<Prestation> getPrestations() {
        return prestations;
    }

    public void setPrestations(List<Prestation> prestations) {
        this.prestations = prestations;
    }
}
