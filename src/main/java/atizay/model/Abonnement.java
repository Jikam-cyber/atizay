package atizay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonnement")
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAbonnement;

    @Column(name = "type_abonnement", nullable = false)
    private String typeAbonnement; // Basique, Premium, Pro

    @Column(name = "prix_mensuel", nullable = false)
    private Double prixMensuel;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "statut", nullable = false)
    private String statut; // Actif, Expiré, Annulé

    @Column(name = "auto_renouvellement")
    private boolean autoRenouvellement = false;

    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = true)
    private Salon salon;

    @ManyToOne
    @JoinColumn(name = "id_proprietaire", nullable = false)
    private Proprietaire proprietaire;

    public Abonnement() {}

    // Getters et Setters
    public Long getIdAbonnement() {
        return idAbonnement;
    }

    public void setIdAbonnement(Long idAbonnement) {
        this.idAbonnement = idAbonnement;
    }

    public String getTypeAbonnement() {
        return typeAbonnement;
    }

    public void setTypeAbonnement(String typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }

    public Double getPrixMensuel() {
        return prixMensuel;
    }

    public void setPrixMensuel(Double prixMensuel) {
        this.prixMensuel = prixMensuel;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isAutoRenouvellement() {
        return autoRenouvellement;
    }

    public void setAutoRenouvellement(boolean autoRenouvellement) {
        this.autoRenouvellement = autoRenouvellement;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public Proprietaire getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Proprietaire proprietaire) {
        this.proprietaire = proprietaire;
    }
}
