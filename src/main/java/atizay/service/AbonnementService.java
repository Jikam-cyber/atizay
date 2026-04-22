package atizay.service;

import atizay.model.Abonnement;
import atizay.model.Proprietaire;
import atizay.repository.AbonnementRepository;
import atizay.repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AbonnementService {

    @Autowired
    private AbonnementRepository abonnementRepository;

    @Autowired
    private SalonRepository salonRepository;

    /**
     * Obtenir l'abonnement actif d'un propriétaire
     */
    public Optional<Abonnement> getAbonnementActif(Proprietaire proprietaire) {
        return abonnementRepository.findByProprietaireAndStatut(proprietaire, "Actif");
    }

    /**
     * Vérifier si l'abonnement est encore valide
     */
    public boolean isAbonnementValide(Abonnement abonnement) {
        if (abonnement == null || !"Actif".equals(abonnement.getStatut())) {
            return false;
        }
        LocalDateTime maintenant = LocalDateTime.now();
        return abonnement.getDateFin() != null && abonnement.getDateFin().isAfter(maintenant);
    }

    /**
     * Obtenir le nombre maximum de salons autorisé selon le plan
     */
    public int getMaxSalons(String plan) {
        return switch (plan.toLowerCase()) {
            case "gratuit" -> 1;
            case "pro" -> 3;
            case "premium" -> Integer.MAX_VALUE; // Illimité
            default -> 1;
        };
    }

    /**
     * Obtenir le nombre maximum de prestations par salon autorisé selon le plan
     */
    public int getMaxPrestationsParSalon(String plan) {
        return switch (plan.toLowerCase()) {
            case "gratuit" -> 10;
            case "pro" -> 30;
            case "premium" -> Integer.MAX_VALUE; // Illimité
            default -> 10;
        };
    }

    /**
     * Obtenir le nombre maximum d'employés par salon autorisé selon le plan
     */
    public int getMaxEmployesParSalon(String plan) {
        return switch (plan.toLowerCase()) {
            case "gratuit" -> 2;
            case "pro" -> 5;
            case "premium" -> Integer.MAX_VALUE; // Illimité
            default -> 2;
        };
    }

    /**
     * Obtenir le nombre maximum de photos par salon autorisé selon le plan
     */
    public int getMaxPhotosParSalon(String plan) {
        return switch (plan.toLowerCase()) {
            case "gratuit" -> 5;
            case "pro" -> 15;
            case "premium" -> Integer.MAX_VALUE; // Illimité
            default -> 5;
        };
    }

    /**
     * Vérifier si le propriétaire peut créer un nouveau salon
     */
    public boolean peutCreerSalon(Proprietaire proprietaire) {
        Optional<Abonnement> abonnementOpt = getAbonnementActif(proprietaire);
        int currentSalons = salonRepository.findByProprietaire(proprietaire).size();
        
        if (abonnementOpt.isEmpty() || !isAbonnementValide(abonnementOpt.get())) {
            // Pas d'abonnement actif = plan gratuit par défaut
            return currentSalons < getMaxSalons("gratuit");
        }

        Abonnement abonnement = abonnementOpt.get();
        String plan = abonnement.getTypeAbonnement().toLowerCase();
        int maxSalons = getMaxSalons(plan);

        return currentSalons < maxSalons;
    }

    /**
     * Vérifier si le propriétaire peut ajouter une prestation à un salon
     */
    public boolean peutAjouterPrestation(Proprietaire proprietaire, int currentPrestations) {
        Optional<Abonnement> abonnementOpt = getAbonnementActif(proprietaire);
        if (abonnementOpt.isEmpty() || !isAbonnementValide(abonnementOpt.get())) {
            return currentPrestations < getMaxPrestationsParSalon("gratuit");
        }

        Abonnement abonnement = abonnementOpt.get();
        String plan = abonnement.getTypeAbonnement().toLowerCase();
        int maxPrestations = getMaxPrestationsParSalon(plan);

        return currentPrestations < maxPrestations;
    }

    /**
     * Vérifier si le propriétaire peut ajouter un employé à un salon
     */
    public boolean peutAjouterEmploye(Proprietaire proprietaire, int currentEmployes) {
        Optional<Abonnement> abonnementOpt = getAbonnementActif(proprietaire);
        if (abonnementOpt.isEmpty() || !isAbonnementValide(abonnementOpt.get())) {
            return currentEmployes < getMaxEmployesParSalon("gratuit");
        }

        Abonnement abonnement = abonnementOpt.get();
        String plan = abonnement.getTypeAbonnement().toLowerCase();
        int maxEmployes = getMaxEmployesParSalon(plan);

        return currentEmployes < maxEmployes;
    }

    /**
     * Vérifier si le propriétaire peut ajouter une photo à un salon
     */
    public boolean peutAjouterPhoto(Proprietaire proprietaire, int currentPhotos) {
        Optional<Abonnement> abonnementOpt = getAbonnementActif(proprietaire);
        if (abonnementOpt.isEmpty() || !isAbonnementValide(abonnementOpt.get())) {
            return currentPhotos < getMaxPhotosParSalon("gratuit");
        }

        Abonnement abonnement = abonnementOpt.get();
        String plan = abonnement.getTypeAbonnement().toLowerCase();
        int maxPhotos = getMaxPhotosParSalon(plan);

        return currentPhotos < maxPhotos;
    }

    /**
     * Obtenir le nom du plan pour affichage
     */
    public String getPlanNom(Proprietaire proprietaire) {
        Optional<Abonnement> abonnementOpt = getAbonnementActif(proprietaire);
        if (abonnementOpt.isEmpty() || !isAbonnementValide(abonnementOpt.get())) {
            return "Gratuit";
        }
        return abonnementOpt.get().getTypeAbonnement();
    }
}
