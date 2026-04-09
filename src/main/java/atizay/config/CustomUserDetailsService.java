package atizay.config;

import atizay.model.Client;
import atizay.model.Proprietaire;
import atizay.repository.ClientRepository;
import atizay.repository.ProprietaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProprietaireRepository proprietaireRepository;

    @Autowired
    private atizay.repository.EmployeRepository employeRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Essayer de trouver un propriétaire avec cet email (sous-classe de Client)
        Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
        if (proprietaire != null) {
            return User.withUsername(proprietaire.getEmail())
                    .password(proprietaire.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_PROPRIETAIRE"))
                    .build();
        }

        // Essayer de trouver un client régulier avec cet email
        Client client = clientRepository.findByEmail(email);
        if (client != null) {
            return User.withUsername(client.getEmail())
                    .password(client.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_CLIENT"))
                    .build();
        }

        // Essayer de trouver un employé avec cet email
        atizay.model.Employe employe = employeRepository.findByEmail(email);
        if (employe != null) {
            return User.withUsername(employe.getEmail())
                    .password(employe.getPassword())
                    .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYE"))
                    .build();
        }

        throw new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
    }
}
