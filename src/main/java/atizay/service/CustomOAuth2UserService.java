package atizay.service;

import atizay.model.Client;
import atizay.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String firstName = "";
        String lastName = "";

        if (registrationId.equals("google")) {
            firstName = (String) attributes.get("given_name");
            lastName = (String) attributes.get("family_name");
        } else if (registrationId.equals("facebook")) {
            // Facebook attributes might differ, usually 'name' is full name
            if (name != null && name.contains(" ")) {
                firstName = name.split(" ")[0];
                lastName = name.substring(name.indexOf(" ") + 1);
            } else {
                firstName = name;
            }
        }

        // Vérifier si le client existe déjà
        Client client = clientRepository.findByEmail(email);
        if (client == null) {
            // Créer un nouveau compte client
            client = new Client();
            client.setEmail(email);
            client.setPrenom(firstName != null ? firstName : "Utilisateur");
            client.setNom(lastName != null ? lastName : "Social");
            client.setUsername(email.split("@")[0]);
            client.setDateCreation(LocalDateTime.now());
            client.setEstActif(true);
            client.setTypeClient("Social");
            // Pour OAuth2, on peut mettre un password bidon
            client.setPassword("OAUTH2_USER"); 
            clientRepository.save(client);
        }

        return oauth2User;
    }
}
