package atizay.service;

import atizay.model.Proprietaire;
import atizay.repository.ProprietaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProprietaireService {

    @Autowired
    private ProprietaireRepository proprietaireRepository;

    /**
     * Authentifie un propriétaire
     */
    public Proprietaire authenticateProprietaire(String email, String password) {
        return proprietaireRepository.findByEmailAndPassword(email, password);
    }

    /**
     * Enregistre un nouveau propriétaire
     */
    public Proprietaire saveProprietaire(Proprietaire proprietaire) {
        return proprietaireRepository.save(proprietaire);
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        return proprietaireRepository.findByEmail(email) != null;
    }

    /**
     * Vérifie si un username existe déjà
     */
    public boolean usernameExists(String username) {
        return proprietaireRepository.findByUsername(username) != null;
    }
}
