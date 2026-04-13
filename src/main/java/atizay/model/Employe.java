package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("EMPLOYE")
public class Employe extends Utilisateur {
    
        
    @Column(name = "specialite")
    private String specialite;
    
    @Column(name = "experience_annees")
    private Integer experienceAnnees = 0;
    
    @Column(name = "nombre_services_realises")
    private Integer nombreServicesRealises = 0; 
    
    @Column(name = "biographie", columnDefinition = "TEXT")
    private String biographie;
    
    @Column(name = "disponible")
    private boolean disponible = true;
    
    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0.0;
    
    
    @Column(name = "actif")
    private boolean actifEmploye = true;
    
    @Column(name = "doit_changer_mdp")
    private boolean doitChangerMdp = true;
    
    // Relations avec le salon (OBLIGATOIRE)
    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = false)
    private Salon salon;
    
    // Planning de travail de l'employé
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<HoraireEmploye> planningTravail;
    
    // Rendez-vous de l'employé
    @OneToMany(mappedBy = "employe", cascade = CascadeType.ALL)
    private List<RendezVous> rendezVous;
    
    // Services que l'employé peut réaliser
    @ManyToMany
    @JoinTable(
        name = "employe_prestation",
        joinColumns = @JoinColumn(name = "id_employe"),
        inverseJoinColumns = @JoinColumn(name = "id_prestation")
    )
    private List<Prestation> servicesRealisables;
    
    public Employe() {
        super();
    }
    
    public Employe(String username, String password, String email, String nom, String prenom,
                   String specialite, Integer experienceAnnees, String numeroLicence,
                   String biographie, Salon salon) {
        super(username, password, email, nom, prenom, null, null);
        this.specialite = specialite;
        this.biographie = biographie;
        this.salon = salon;
    }
    
    // Getters et Setters spécifiques à l'Employé
    
    public String getSpecialite() {
        return specialite;
    }
    
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }
    
    
    public String getBiographie() {
        return biographie;
    }
    
    public void setBiographie(String biographie) {
        this.biographie = biographie;
    }
    
    public boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public Double getNoteMoyenne() {
        return noteMoyenne;
    }
    
    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }
    
    public Integer getExperienceAnnees() {
        return experienceAnnees;
    }
    
    public void setExperienceAnnees(Integer experienceAnnees) {
        this.experienceAnnees = experienceAnnees;
    }
    
    public Integer getNombreServicesRealises() {
        return nombreServicesRealises;
    }
    
    public void setNombreServicesRealises(Integer nombreServicesRealises) {
        this.nombreServicesRealises = nombreServicesRealises;
    }
    
    
    
    public boolean isActifEmploye() {
        return actifEmploye;
    }
    
    public void setActifEmploye(boolean actifEmploye) {
        this.actifEmploye = actifEmploye;
    }
    
    public boolean isDoitChangerMdp() {
        return doitChangerMdp;
    }
    
    public void setDoitChangerMdp(boolean doitChangerMdp) {
        this.doitChangerMdp = doitChangerMdp;
    }
    
    public Salon getSalon() {
        return salon;
    }
    
    public void setSalon(Salon salon) {
        this.salon = salon;
    }
    
    public List<HoraireEmploye> getPlanningTravail() {
        return planningTravail;
    }
    
    public void setPlanningTravail(List<HoraireEmploye> planningTravail) {
        this.planningTravail = planningTravail;
    }
    
    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }
    
    public void setRendezVous(List<RendezVous> rendezVous) {
        this.rendezVous = rendezVous;
    }
    
    public List<Prestation> getServicesRealisables() {
        return servicesRealisables;
    }
    
    public void setServicesRealisables(List<Prestation> servicesRealisables) {
        this.servicesRealisables = servicesRealisables;
    }
    
    // Méthodes métier
    public void incrementerServicesRealises() {
        // Logique à implémenter selon les besoins
    }
    
    /**
     * Vérifie si l'employé est bien assigné à un salon (OBLIGATOIRE)
     */
    public boolean estAssigneASalon() {
        return this.salon != null;
    }
    
    /**
     * Assigne l'employé à un salon (OBLIGATOIRE)
     */
    public void assignerASalon(Salon salon) {
        if (salon == null) {
            throw new IllegalArgumentException("Un employé doit obligatoirement être assigné à un salon");
        }
        this.salon = salon;
    }
    
    /**
     * Retourne le nom du salon où travaille l'employé
     */
    public String getNomSalon() {
        return estAssigneASalon() ? this.salon.getNomSalon() : "Non assigné";
    }
    
    /**
     * Vérifie si l'employé peut prendre des rendez-vous
     */
    public boolean peutPrendreRendezVous() {
        return estAssigneASalon() && this.disponible && this.actifEmploye;
    }
    
    public void mettreAJourNote(Double nouvelleNote) {
        this.noteMoyenne = nouvelleNote;
    }
    
    public boolean estDisponiblePourService() {
        return this.disponible && this.actifEmploye;
    }
}
