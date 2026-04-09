package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("PROPRIETAIRE")
public class Proprietaire extends Client {
    
    @Column(name = "statut_commercial")
    private String statutCommercial; // Actif, En pause, Fermé
    
    @Column(name = "nombre_salons")
    private Integer nombreSalons = 1;
    
    // Relations avec les salons du propriétaire
    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL)
    private List<Salon> salons;
    
    public Proprietaire() {
        super();
    }
    
    public Proprietaire(String username, String password, String email, String nom, String prenom,
                       String adresse, String ville, String statutCommercial, Integer nombreSalons) {
        super(username, password, email, nom, prenom, adresse, ville, "PROFESSIONNEL", null);
        this.statutCommercial = statutCommercial;
        this.nombreSalons = nombreSalons;
    }
    
    // Getters et Setters spécifiques au Proprietaire
    
    public String getStatutCommercial() {
        return statutCommercial;
    }
    
    public void setStatutCommercial(String statutCommercial) {
        this.statutCommercial = statutCommercial;
    }
    
    public Integer getNombreSalons() {
        return nombreSalons;
    }
    
    public void setNombreSalons(Integer nombreSalons) {
        this.nombreSalons = nombreSalons;
    }
    
    public List<Salon> getSalons() {
        return salons;
    }
    
    public void setSalons(List<Salon> salons) {
        this.salons = salons;
    }
    
    // Méthodes métier
    public void ajouterSalon() {
        this.nombreSalons++;
    }
    
    public boolean estActifCommercial() {
        return "Actif".equals(this.statutCommercial);
    }
    
    public void mettreEnPause() {
        this.statutCommercial = "En pause";
    }
    
    public void fermerActivite() {
        this.statutCommercial = "Fermé";
    }
    
    @Override
    public boolean peutAvoirPlusieursSalons() {
        return true; // Un propriétaire peut avoir plusieurs salons
    }
}
