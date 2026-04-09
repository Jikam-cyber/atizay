package atizay.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "horaire_employe")
public class HoraireEmploye {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHoraireEmploye;

    @Column(name = "jour_semaine", nullable = false)
    private DayOfWeek jourSemaine; // LUNDI, MARDI, etc.

    @Column(name = "heure_debut_matin", nullable = false)
    private LocalTime heureDebutMatin;

    @Column(name = "heure_fin_matin", nullable = false)
    private LocalTime heureFinMatin;

    @Column(name = "heure_debut_apres_midi", nullable = false)
    private LocalTime heureDebutApresMidi;

    @Column(name = "heure_fin_apres_midi", nullable = false)
    private LocalTime heureFinApresMidi;

    @Column(name = "actif")
    private boolean actif = true;

    @ManyToOne
    @JoinColumn(name = "id_employe", nullable = false)
    private Employe employe;

    public HoraireEmploye() {}

    // Getters et Setters
    public Long getIdHoraireEmploye() {
        return idHoraireEmploye;
    }

    public void setIdHoraireEmploye(Long idHoraireEmploye) {
        this.idHoraireEmploye = idHoraireEmploye;
    }

    public DayOfWeek getJourSemaine() {
        return jourSemaine;
    }

    public void setJourSemaine(DayOfWeek jourSemaine) {
        this.jourSemaine = jourSemaine;
    }

    public LocalTime getHeureDebutMatin() {
        return heureDebutMatin;
    }

    public void setHeureDebutMatin(LocalTime heureDebutMatin) {
        this.heureDebutMatin = heureDebutMatin;
    }

    public LocalTime getHeureFinMatin() {
        return heureFinMatin;
    }

    public void setHeureFinMatin(LocalTime heureFinMatin) {
        this.heureFinMatin = heureFinMatin;
    }

    public LocalTime getHeureDebutApresMidi() {
        return heureDebutApresMidi;
    }

    public void setHeureDebutApresMidi(LocalTime heureDebutApresMidi) {
        this.heureDebutApresMidi = heureDebutApresMidi;
    }

    public LocalTime getHeureFinApresMidi() {
        return heureFinApresMidi;
    }

    public void setHeureFinApresMidi(LocalTime heureFinApresMidi) {
        this.heureFinApresMidi = heureFinApresMidi;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public Employe getEmploye() {
        return employe;
    }

    public void setEmploye(Employe employe) {
        this.employe = employe;
    }
}
