package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("CLIENT")
public class Client extends Utilisateur {
    
    @Column(name = "type_client")
    private String typeClient; // PARTICULIER, PROFESSIONNEL
    
    @Column(name = "preferences")
    private String preferences; // Préférences de services
    
    @Column(name = "points_fidelite")
    private Integer pointsFidelite = 0;
    
    @Column(name = "visiteur_fidele")
    private boolean visiteurFidele = false;
    
    // Relations avec les salons favoris du client
    @ManyToMany
    @JoinTable(
        name = "client_salon_favori",
        joinColumns = @JoinColumn(name = "id_client"),
        inverseJoinColumns = @JoinColumn(name = "id_salon")
    )
    private List<Salon> salonsFavoris;
    
    // Relations avec les rendez-vous du client
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<RendezVous> rendezVous;
    
    // Relations avec les salons possédés par le client (pour les clients propriétaires)
    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL)
    private List<Salon> salonsPossedes;
    
    public Client() {
        super();
    }
    
    public Client(String username, String password, String email, String nom, String prenom,
                String adresse, String ville, String typeClient, String preferences) {
        super(username, password, email, nom, prenom, adresse, ville);
        this.typeClient = typeClient;
        this.preferences = preferences;
    }
    
    // Getters et Setters spécifiques au Client
    
    public String getTypeClient() {
        return typeClient;
    }
    
    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }
    
    public String getPreferences() {
        return preferences;
    }
    
    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }
    
    public Integer getPointsFidelite() {
        return pointsFidelite;
    }
    
    public void setPointsFidelite(Integer pointsFidelite) {
        this.pointsFidelite = pointsFidelite;
    }
    
    public boolean isVisiteurFidele() {
        return visiteurFidele;
    }
    
    public void setVisiteurFidele(boolean visiteurFidele) {
        this.visiteurFidele = visiteurFidele;
    }
    
    public List<Salon> getSalonsFavoris() {
        return salonsFavoris;
    }
    
    public void setSalonsFavoris(List<Salon> salonsFavoris) {
        this.salonsFavoris = salonsFavoris;
    }
    
    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }
    
    public void setRendezVous(List<RendezVous> rendezVous) {
        this.rendezVous = rendezVous;
    }
    
    public List<Salon> getSalonsPossedes() {
        return salonsPossedes;
    }
    
    public void setSalonsPossedes(List<Salon> salonsPossedes) {
        this.salonsPossedes = salonsPossedes;
    }
    
    // Méthodes métier
    public void ajouterPointsFidelite(Integer points) {
        this.pointsFidelite += points;
    }
    
    public void utiliserPointsFidelite(Integer points) {
        if (this.pointsFidelite >= points) {
            this.pointsFidelite -= points;
        }
    }
    
    public void devenirVisiteurFidele() {
        this.visiteurFidele = true;
    }
    
    public boolean peutAvoirPlusieursSalons() {
        return "PROFESSIONNEL".equals(this.typeClient);
    }
    
    public void ajouterSalonFavori(Salon salon) {
        if (salonsFavoris != null) {
            salonsFavoris.add(salon);
        }
    }
    
    public void supprimerSalonFavori(Salon salon) {
        if (salonsFavoris != null) {
            salonsFavoris.remove(salon);
        }
    }
}
