package atizay.config;

import atizay.controller.PaymentController;
import atizay.model.Client;
import atizay.model.Proprietaire;
import atizay.repository.ClientRepository;
import atizay.repository.ProprietaireRepository;
import atizay.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProprietaireRepository proprietaireRepository;

    @Autowired
    private atizay.repository.EmployeRepository employeRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private PaymentController paymentController;

    @Autowired
    private atizay.repository.AdminRepository adminRepository;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/recherche", "/salon/**", "/concept", "/guide", "/technique", "/auth/**", "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/client/**", "/proprietaire/**", "/employe/**", "/payment/**", "/admin/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/connexion")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/auth/connexion?error=true")
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/connexion")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(customSuccessHandler())
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/deconnexion")
                        .logoutSuccessUrl("/auth/connexion?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            System.out.println("=== DEBUG: customSuccessHandler appelé ===");
            System.out.println("Authentication: " + (authentication != null ? authentication.getClass().getName() : "null"));
            
            HttpSession session = request.getSession();
            String email;

            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                email = oauth2User.getAttribute("email");
            } else {
                email = authentication.getName();
            }
            
            System.out.println("Email: " + email);

            // Vérifier si c'est un admin
            atizay.model.Admin admin = adminRepository.findByEmail(email);
            System.out.println("Admin trouvé: " + (admin != null ? "oui" : "non"));
            
            if (admin != null) {
                session.setAttribute("user", admin);
                session.setAttribute("userType", "admin");
                response.sendRedirect("/admin/dashboard");
                return;
            }

            // Un utilisateur connecté via OAuth2 est toujours un Client initialement dans notre système
            Client client = clientRepository.findByEmail(email);
            System.out.println("Client trouvé: " + (client != null ? "oui" : "non"));
            
            if (client != null) {
                // On vérifie s'il est aussi propriétaire ou employé (cas rare mais possible)
                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                atizay.model.Employe employe = employeRepository.findByEmail(email);
                
                System.out.println("Propriétaire trouvé: " + (proprietaire != null ? "oui" : "non"));
                System.out.println("Employé trouvé: " + (employe != null ? "oui" : "non"));

                if (proprietaire != null && !(authentication instanceof OAuth2AuthenticationToken)) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");
                    // Créer l'abonnement s'il y a un paiement en attente
                    paymentController.createSubscriptionAfterLogin(proprietaire, session);
                    response.sendRedirect("/proprietaire/dashboard");
                } else if (employe != null && !(authentication instanceof OAuth2AuthenticationToken)) {
                    session.setAttribute("user", employe);
                    session.setAttribute("userType", "employe");
                    response.sendRedirect("/employe/dashboard");
                } else {
                    session.setAttribute("user", client);
                    session.setAttribute("userType", "client");
                    response.sendRedirect("/client/dashboard");
                }
            } else {
                // Cas d'un propriétaire qui se connecte via formulaire
                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                System.out.println("Propriétaire (sans client) trouvé: " + (proprietaire != null ? "oui" : "non"));
                
                if (proprietaire != null) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");
                    // Créer l'abonnement s'il y a un paiement en attente
                    paymentController.createSubscriptionAfterLogin(proprietaire, session);
                    response.sendRedirect("/proprietaire/dashboard");
                    return;
                }
                
                atizay.model.Employe employe = employeRepository.findByEmail(email);
                System.out.println("Employé (sans client) trouvé: " + (employe != null ? "oui" : "non"));
                
                if (employe != null) {
                    session.setAttribute("user", employe);
                    session.setAttribute("userType", "employe");
                    response.sendRedirect("/employe/dashboard");
                    return;
                }

                System.out.println("Aucun utilisateur trouvé, redirection vers login avec erreur");
                response.sendRedirect("/auth/connexion?error=true");
            }
        };
    }
}
