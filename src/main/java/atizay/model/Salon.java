package atizay.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "salon")
public class Salon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSalon;

    @Column(name = "nom_salon", nullable = false)
    private String nomSalon;

    @Column(name = "description_salon", columnDefinition = "TEXT")
    private String descriptionSalon;

    @Column(name = "type_salon")
    private String typeSalon;

    @Column(name = "adresse_salon")
    private String adresseSalon;

    @Column(name = "ville_salon")
    private String villeSalon;

    @Column(name = "email_salon")
    private String emailSalon;

    @Column(name = "telephone_salon")
    private String telephoneSalon;

    @Column(name = "departement")
    private String departement;

    @Column(name = "commune")
    private String commune;

    @Column(name = "quartier")
    private String quartier;

    @Column(name = "rue")
    private String rue;

    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0.0;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "nombre_vues")
    private Integer nombreVues = 0;

    @Column(name = "nombre_reservations")
    private Integer nombreReservations = 0;

    @ManyToOne
    @JoinColumn(name = "id_proprietaire", nullable = false)
    private Proprietaire proprietaire;

    // Lien avec les barbiers travaillant dans ce salon
    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employe> listeEmployes;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prestation> listePrestations;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaSalon> listeMedias;

    @OneToOne(mappedBy = "salon", cascade = CascadeType.ALL)
    private Abonnement abonnementActif;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idHoraireSalon ASC")
    private List<HoraireSalon> listeHoraires;

    @OneToMany(mappedBy = "salon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RendezVous> listeRendezVous;

    public Salon() {
    }

    // Getters et Setters
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

    public String getDescriptionSalon() {
        return descriptionSalon;
    }

    public void setDescriptionSalon(String descriptionSalon) {
        this.descriptionSalon = descriptionSalon;
    }

    public String getTypeSalon() {
        return typeSalon;
    }

    public void setTypeSalon(String typeSalon) {
        this.typeSalon = typeSalon;
    }

    public String getAdresseSalon() {
        return adresseSalon;
    }

    public void setAdresseSalon(String adresseSalon) {
        this.adresseSalon = adresseSalon;
    }

    public String getVilleSalon() {
        return villeSalon;
    }

    public void setVilleSalon(String villeSalon) {
        this.villeSalon = villeSalon;
    }

    public String getEmailSalon() {
        return emailSalon;
    }

    public void setEmailSalon(String emailSalon) {
        this.emailSalon = emailSalon;
    }

    public String getTelephoneSalon() {
        return telephoneSalon;
    }

    public void setTelephoneSalon(String telephoneSalon) {
        this.telephoneSalon = telephoneSalon;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public Double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(Double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getNombreVues() {
        return nombreVues;
    }

    public void setNombreVues(Integer nombreVues) {
        this.nombreVues = nombreVues;
    }

    public Integer getNombreReservations() {
        return nombreReservations;
    }

    public void setNombreReservations(Integer nombreReservations) {
        this.nombreReservations = nombreReservations;
    }

    public Proprietaire getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Proprietaire proprietaire) {
        this.proprietaire = proprietaire;
    }

    public List<Employe> getListeEmployes() {
        return listeEmployes;
    }

    public void setListeEmployes(List<Employe> listeEmployes) {
        this.listeEmployes = listeEmployes;
    }

    public List<Prestation> getListePrestations() {
        return listePrestations;
    }

    public void setListePrestations(List<Prestation> listePrestations) {
        this.listePrestations = listePrestations;
    }

    public List<MediaSalon> getListeMedias() {
        return listeMedias;
    }

    public void setListeMedias(List<MediaSalon> listeMedias) {
        this.listeMedias = listeMedias;
    }

    public Abonnement getAbonnementActif() {
        return abonnementActif;
    }

    public void setAbonnementActif(Abonnement abonnementActif) {
        this.abonnementActif = abonnementActif;
    }

    public List<HoraireSalon> getListeHoraires() {
        return listeHoraires;
    }

    public void setListeHoraires(List<HoraireSalon> listeHoraires) {
        this.listeHoraires = listeHoraires;
    }

    public List<RendezVous> getListeRendezVous() {
        return listeRendezVous;
    }

    public void setListeRendezVous(List<RendezVous> listeRendezVous) {
        this.listeRendezVous = listeRendezVous;
    }
}
