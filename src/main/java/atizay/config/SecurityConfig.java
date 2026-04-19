package atizay.config;

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


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/recherche", "/salon/**", "/concept", "/guide", "/technique", "/auth/**", "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/client/**", "/proprietaire/**", "/employe/**")
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
            HttpSession session = request.getSession();
            String email;

            if (authentication instanceof OAuth2AuthenticationToken) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                email = oauth2User.getAttribute("email");
            } else {
                email = authentication.getName();
            }

            // Un utilisateur connecté via OAuth2 est toujours un Client initialement dans notre système
            Client client = clientRepository.findByEmail(email);
            if (client != null) {
                // On vérifie s'il est aussi propriétaire ou employé (cas rare mais possible)
                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                atizay.model.Employe employe = employeRepository.findByEmail(email);

                if (proprietaire != null && !(authentication instanceof OAuth2AuthenticationToken)) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");
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
                if (proprietaire != null) {
                    session.setAttribute("user", proprietaire);
                    session.setAttribute("userType", "proprietaire");
                    response.sendRedirect("/proprietaire/dashboard");
                    return;
                }
                
                atizay.model.Employe employe = employeRepository.findByEmail(email);
                if (employe != null) {
                    session.setAttribute("user", employe);
                    session.setAttribute("userType", "employe");
                    response.sendRedirect("/employe/dashboard");
                    return;
                }

                response.sendRedirect("/auth/connexion?error=true");
            }
        };
    }
}
