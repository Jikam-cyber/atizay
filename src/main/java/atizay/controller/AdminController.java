package atizay.controller;

import atizay.model.*;
import atizay.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private SalonRepository salonRepository;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private ProprietaireRepository proprietaireRepository;
    
    @Autowired
    private EmployeRepository employeRepository;
    
    @Autowired
    private RendezVousRepository rendezVousRepository;
    
    @Autowired
    private AbonnementRepository abonnementRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${admin.email}")
    private String adminEmail;
    
    @Value("${admin.password}")
    private String adminPassword;
    
    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        
        if (!"admin".equals(userType)) {
            return "redirect:/auth/connexion";
        }
        
        // S'assurer que l'admin existe dans la base de données
        ensureAdminExists();
        
        Admin admin = (Admin) session.getAttribute("user");
        
        // Statistiques générales
        long totalSalons = salonRepository.count();
        long totalClients = clientRepository.count();
        long totalProprietaires = proprietaireRepository.count();
        long totalEmployes = employeRepository.count();
        long totalRendezVous = rendezVousRepository.count();
        
        // Statistiques par abonnement
        List<Abonnement> allAbonnements = abonnementRepository.findAll();
        Map<String, Long> abonnementsParPlan = new HashMap<>();
        abonnementsParPlan.put("gratuit", 0L);
        abonnementsParPlan.put("pro", 0L);
        abonnementsParPlan.put("premium", 0L);
        
        for (Abonnement abonnement : allAbonnements) {
            String plan = abonnement.getTypeAbonnement().toLowerCase();
            abonnementsParPlan.put(plan, abonnementsParPlan.getOrDefault(plan, 0L) + 1);
        }
        
        // Salons récents
        List<Salon> salonsRecents = salonRepository.findAll();
        salonsRecents = salonsRecents.stream()
                .sorted((s1, s2) -> s2.getIdSalon().compareTo(s1.getIdSalon()))
                .limit(10)
                .toList();
        
        // Rendez-vous récents
        List<RendezVous> rendezVousRecents = rendezVousRepository.findAll();
        rendezVousRecents = rendezVousRecents.stream()
                .sorted((r1, r2) -> r2.getDateRendezVous().compareTo(r1.getDateRendezVous()))
                .limit(10)
                .toList();
        
        model.addAttribute("admin", admin);
        model.addAttribute("totalSalons", totalSalons);
        model.addAttribute("totalClients", totalClients);
        model.addAttribute("totalProprietaires", totalProprietaires);
        model.addAttribute("totalEmployes", totalEmployes);
        model.addAttribute("totalRendezVous", totalRendezVous);
        model.addAttribute("abonnementsParPlan", abonnementsParPlan);
        model.addAttribute("salonsRecents", salonsRecents);
        model.addAttribute("rendezVousRecents", rendezVousRecents);
        
        return "admin/dashboard-admin";
    }
    
    private void ensureAdminExists() {
        Admin existingAdmin = adminRepository.findByEmail(adminEmail);
        if (existingAdmin == null) {
            Admin admin = new Admin(adminEmail, passwordEncoder.encode(adminPassword), "Administrateur");
            adminRepository.save(admin);
            System.out.println("Admin créé avec succès: " + adminEmail);
        }
    }
}
