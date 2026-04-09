package atizay.service;

import atizay.model.Client;
import atizay.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Authentifie un client
     */
    public Client authenticateClient(String email, String password) {
        return clientRepository.findByEmailAndPassword(email, password);
    }

    /**
     * Enregistre un nouveau client
     */
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        return clientRepository.findByEmail(email) != null;
    }

    /**
     * Vérifie si un username existe déjà
     */
    public boolean usernameExists(String username) {
        return clientRepository.findByUsername(username) != null;
    }
}
