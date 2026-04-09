package atizay.config;

import atizay.model.Client;
import atizay.model.Proprietaire;
import atizay.repository.ClientRepository;
import atizay.repository.ProprietaireRepository;
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
    private atizay.repository.EmployeRepository employeRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/auth/**", "/static/**", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/client/**").hasAuthority("ROLE_CLIENT")
                        .requestMatchers("/proprietaire/**").hasAuthority("ROLE_PROPRIETAIRE")
                        .requestMatchers("/employe/**").hasAuthority("ROLE_EMPLOYE")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/connexion")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/auth/connexion?error=true")
                        .permitAll())
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
            String email = authentication.getName();

            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CLIENT"))) {
                Client client = clientRepository.findByEmail(email);
                session.setAttribute("client", client);
                session.setAttribute("type", "client");
                response.sendRedirect("/client/dashboard");
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PROPRIETAIRE"))) {
                Proprietaire proprietaire = proprietaireRepository.findByEmail(email);
                session.setAttribute("proprietaire", proprietaire);
                session.setAttribute("type", "proprietaire");
                response.sendRedirect("/proprietaire/dashboard");
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYE"))) {
                atizay.model.Employe employe = employeRepository.findByEmail(email);
                session.setAttribute("employe", employe);
                session.setAttribute("type", "employe");
                response.sendRedirect("/employe/dashboard");
            } else {
                response.sendRedirect("/");
            }
        };
    }
}
