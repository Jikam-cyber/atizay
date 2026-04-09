package atizay.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horaire_salon")
public class HoraireSalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHoraireSalon;

    @Column(name = "jour_semaine", nullable = false)
    private String jourSemaine; // LUN, MAR, MER, JEU, VEN, SAM, DIM

    @Column(name = "heure_ouverture")
    private LocalTime heureOuverture;

    @Column(name = "heure_fermeture")
    private LocalTime heureFermeture;

    @Column(name = "est_ouvert")
    private boolean estOuvert = true;

    @ManyToOne
    @JoinColumn(name = "id_salon", nullable = false)
    private Salon salon;

    public HoraireSalon() {}

    public HoraireSalon(String jourSemaine, LocalTime heureOuverture, LocalTime heureFermeture, boolean estOuvert, Salon salon) {
        this.jourSemaine = jourSemaine;
        this.heureOuverture = heureOuverture;
        this.heureFermeture = heureFermeture;
        this.estOuvert = estOuvert;
        this.salon = salon;
    }

    // Getters et Setters
    public Long getIdHoraireSalon() {
        return idHoraireSalon;
    }

    public void setIdHoraireSalon(Long idHoraireSalon) {
        this.idHoraireSalon = idHoraireSalon;
    }

    public String getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(String jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public LocalTime getHeureOuverture() {
        return heureOuverture;
    }

    public void setHeureOuverture(LocalTime heureOuverture) {
        this.heureOuverture = heureOuverture;
    }

    public LocalTime getHeureFermeture() {
        return heureFermeture;
    }

    public void setHeureFermeture(LocalTime heureFermeture) {
        this.heureFermeture = heureFermeture;
    }

    public boolean isEstOuvert() {
        return estOuvert;
    }

    public void setEstOuvert(boolean estOuvert) {
        this.estOuvert = estOuvert;
    }

    public Salon getSalon() {
        return salon;
    }

    public void setSalon(Salon salon) {
        this.salon = salon;
    }
}
