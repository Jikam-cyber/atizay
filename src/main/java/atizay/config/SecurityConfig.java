package atizay.config;

import atizay.controller.PaymentController;
import atizay.model.Client;
import atizay.model.Proprietaire;
import atizay.repository.ClientRepository;
import atizay.repository.ProprietaireRepository;
import atizay.repository.EmployeRepository;
import atizay.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private EmployeRepository employeRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PaymentController paymentController;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home", "/recherche", "/salon/**",
                                "/concept", "/guide", "/technique",
                                "/auth/**", "/static/**", "/css/**",
                                "/js/**", "/images/**", "/uploads/**"
                        ).permitAll()

                        .requestMatchers(
                                "/client/**", "/proprietaire/**",
                                "/employe/**", "/payment/**", "/admin/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/connexion")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/auth/connexion?error=true")
                        .permitAll()
                )

                // ❌ OAuth2 supprimé pour éviter l’erreur
                //.oauth2Login(...)

                .logout(logout -> logout
                        .logoutUrl("/auth/deconnexion")
                        .logoutSuccessUrl("/auth/connexion?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

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

            HttpSession session = request.getSession();
            String email = authentication.getName();

            // 🔍 ADMIN
            atizay.model.Admin admin = adminRepository.findByEmail(email);
            if (admin != null) {
                session.setAttribute("user", admin);
                session.setAttribute("userType", "admin");
                response.sendRedirect("/admin/dashboard");
                return;
            }

            // 🔍 CLIENT
            Client client = clientRepository.findByEmail(email);

            if (client != null) {

                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                atizay.model.Employe employe = employeRepository.findByEmail(email);

                if (proprietaire != null) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");

                    // abonnement après login
                    paymentController.createSubscriptionAfterLogin(proprietaire, session);

                    response.sendRedirect("/proprietaire/dashboard");

                } else if (employe != null) {
                    session.setAttribute("user", employe);
                    session.setAttribute("userType", "employe");

                    response.sendRedirect("/employe/dashboard");

                } else {
                    session.setAttribute("user", client);
                    session.setAttribute("userType", "client");

                    response.sendRedirect("/client/dashboard");
                }

            } else {

                // 🔍 PROPRIETAIRE sans client
                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                if (proprietaire != null) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");

                    paymentController.createSubscriptionAfterLogin(proprietaire, session);

                    response.sendRedirect("/proprietaire/dashboard");
                    return;
                }

                // 🔍 EMPLOYE
                atizay.model.Employe employe = employeRepository.findByEmail(email);
                if (employe != null) {
                    session.setAttribute("user", employe);
                    session.setAttribute("userType", "employe");

                    response.sendRedirect("/employe/dashboard");
                    return;
                }

                // ❌ aucun utilisateur
                response.sendRedirect("/auth/connexion?error=true");
            }
        };
    }
}