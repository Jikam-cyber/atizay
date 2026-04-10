package atizay.controller;

import atizay.model.Client;
import atizay.model.Proprietaire;
import atizay.model.Employe;
import atizay.service.ClientService;
import atizay.service.ProprietaireService;
import atizay.repository.EmployeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProprietaireService proprietaireService;

    @Autowired
    private EmployeRepository employeRepository;

    @GetMapping("/connexion")
    public String showConnexion(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("proprietaire", new Proprietaire());
        return "auth/connexion";
    }

    @GetMapping("/inscription")
    public String showInscription(Model model) {
        model.addAttribute("client", new Client());
        model.addAttribute("proprietaire", new Proprietaire());
        return "auth/inscription";
    }

    @PostMapping("/inscription")
    public String handleInscription(@RequestParam String type,
            @RequestParam(required = false) String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String nom,
            @RequestParam(required = false) String prenom,
            @RequestParam(required = false) String adresse,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String statutCommercial,
            @RequestParam(required = false) Integer nombreSalons,
            RedirectAttributes redirectAttributes) {

        try {
            if ("client".equals(type)) {
                Client client = new Client();
                client.setUsername(username != null && !username.isEmpty() ? username : email);
                client.setEmail(email);
                client.setPassword(password);
                client.setNom(nom != null ? nom : "");
                client.setPrenom(prenom != null ? prenom : "");
                client.setAdresse(adresse != null ? adresse : "");
                client.setVille(ville != null ? ville : "");
                client.setTypeClient("PARTICULIER");

                clientService.saveClient(client);

                redirectAttributes.addFlashAttribute("success", "Inscription client réussie !");
                return "redirect:/auth/connexion";

            } else if ("proprietaire".equals(type)) {
                Proprietaire proprietaire = new Proprietaire();
                proprietaire.setUsername(username != null && !username.isEmpty() ? username : email);
                proprietaire.setEmail(email);
                proprietaire.setPassword(password);
                proprietaire.setNom(nom != null ? nom : "Propriétaire");
                proprietaire.setPrenom(prenom != null ? prenom : "");
                proprietaire.setAdresse(adresse != null ? adresse : "");
                proprietaire.setVille(ville != null ? ville : "");
                proprietaire.setStatutCommercial(statutCommercial != null ? statutCommercial : "Actif");
                proprietaire.setNombreSalons(nombreSalons != null ? nombreSalons : 1);

                proprietaireService.saveProprietaire(proprietaire);

                redirectAttributes.addFlashAttribute("success", "Création de compte professionnel réussie !");
                return "redirect:/auth/connexion";
            }

            // Type non reconnu
            redirectAttributes.addFlashAttribute("error", "Type d'utilisateur non valide");
            return "redirect:/auth/inscription";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'inscription: " + e.getMessage());
            return "redirect:/auth/inscription";
        }
    }

    @PostMapping("/connexion")
    public String handleConnexion(@RequestParam String email,
                                   @RequestParam String password,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si c'est un propriétaire (d'abord car Proprietaire hérite de Client)
            Proprietaire proprietaire = proprietaireService.authenticateProprietaire(email, password);
            if (proprietaire != null) {
                session.setAttribute("userType", "proprietaire");
                session.setAttribute("userId", proprietaire.getId());
                session.setAttribute("user", proprietaire);
                return "redirect:/proprietaire/dashboard";
            }

            // Vérifier si c'est un client
            Client client = clientService.authenticateClient(email, password);
            if (client != null) {
                session.setAttribute("userType", "client");
                session.setAttribute("userId", client.getId());
                session.setAttribute("user", client);
                return "redirect:/client/dashboard";
            }

            // Vérifier si c'est un employé
            Employe employe = employeRepository.findByEmail(email);
            if (employe != null && employe.getPassword().equals(password)) {
                session.setAttribute("userType", "employe");
                session.setAttribute("userId", employe.getId());
                session.setAttribute("user", employe);
                return "redirect:/employe/dashboard";
            }

            // Identifiants invalides
            redirectAttributes.addFlashAttribute("error", "Email ou mot de passe incorrect");
            return "redirect:/auth/connexion";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la connexion: " + e.getMessage());
            return "redirect:/auth/connexion";
        }
    }

    @GetMapping("/deconnexion")
    public String deconnexion(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/connexion";
    }
}
