package atizay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avis_salon")
public class AvisSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvis;

    @Column(name = "note", nullable = false)
    private Integer note; // Note de 1 à 5

    @Column(name = "date_avis", nullable = false)
    private LocalDateTime dateAvis;

    // Relations
    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = false)
    private Salon salon;

    @ManyToOne
    @JoinColumn(name = "id_client", nullable = false)
    private Utilisateur client;

    public AvisSalon() {
        this.dateAvis = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(Long idAvis) {
        this.idAvis = idAvis;
    }

    public Integer getNote() {
        return note;
    }

    public void setNote(Integer note) {
        if (note < 1 || note > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        this.note = note;
    }

    public LocalDateTime getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(LocalDateTime dateAvis) {
        this.dateAvis = dateAvis;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }

    public Utilisateur getClient() {
        return client;
    }

    public void setClient(Utilisateur client) {
        this.client = client;
    }

    // Méthodes métier
    public String getNomClient() {
        return client != null ? client.getNom() + " " + client.getPrenom() : "Client inconnu";
    }

    public void repondreAvis(String reponse) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'repondreAvis'");
    }
}
