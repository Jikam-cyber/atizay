package atizay.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseFixConfig {

    @Bean
    public CommandLineRunner fixDatabaseSchema(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                System.out.println("🔧 Tentative de modification de la table rendez_vous pour autoriser id_employe NULL...");
                jdbcTemplate.execute("ALTER TABLE rendez_vous MODIFY id_employe BIGINT NULL;");
                System.out.println("✅ Table rendez_vous modifiée avec succès !");
            } catch (Exception e) {
                System.out.println("⚠️ Note: La modification de la table a échoué ou était déjà faite : " + e.getMessage());
            }
        };
    }
}
