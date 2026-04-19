package atizay.controller;

import atizay.model.Salon;
import atizay.model.Prestation;
import atizay.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private MediaSalonRepository mediaSalonRepository;

    @Autowired
    private AvisSalonRepository avisSalonRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    /**
     * Page d'accueil publique avec liste des salons
     */
    @GetMapping("/")
    public String home(Model model) {
        List<Salon> salons = salonRepository.findAll();
        for (Salon salon : salons) {
            salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
            salon.setListeAvis(avisSalonRepository.findBySalonIdSalon(salon.getIdSalon()));
        }
        model.addAttribute("salons", salons);
        model.addAttribute("searchMode", false);
        return "index";
    }

    /**
     * Recherche publique de salons
     */
    @GetMapping("/recherche")
    public String recherche(@RequestParam(value = "prestation", required = false, defaultValue = "") String quoi,
                           @RequestParam(value = "lieu", required = false, defaultValue = "") String ou,
                           Model model) {
        List<Salon> salons = salonRepository.searchSalons(quoi, ou);
        for (Salon salon : salons) {
            salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
            salon.setListeAvis(avisSalonRepository.findBySalonIdSalon(salon.getIdSalon()));
        }
        model.addAttribute("salons", salons);
        model.addAttribute("quoi", quoi);
        model.addAttribute("ou", ou);
        model.addAttribute("searchMode", true);
        return "index";
    }

    /**
     * Voir les détails d'un salon publiquement
     */
    @GetMapping("/salon/{id}")
    public String voirSalon(@PathVariable("id") Long id, Model model) {
        Salon salon = salonRepository.findById(id).orElse(null);
        if (salon == null) return "redirect:/";
        
        salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
        salon.setListeAvis(avisSalonRepository.findBySalonIdSalon(salon.getIdSalon()));
        
        model.addAttribute("salon", salon);
        model.addAttribute("employes", salon.getListeEmployes());

        // Grouper les prestations par catégorie
        Map<String, List<Prestation>> prestationsParCategorie = salon.getListePrestations()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategorie() != null && !p.getCategorie().isEmpty() ? p.getCategorie() : "Autres"));

        model.addAttribute("prestationsParCategorie", prestationsParCategorie);
        
        // Liste des URLs d'images pour la galerie
        List<String> photoUrls = salon.getListeMedias().stream()
                .map(m -> m.getUrlMedia().startsWith("/uploads/salons/") ? m.getUrlMedia() : "/uploads/salons/" + m.getUrlMedia())
                .collect(Collectors.toList());
        model.addAttribute("photoUrls", photoUrls);
        
        return "client/salon-detail";
    }
    
    @GetMapping("/home")
    public String homePage() {
        return "index";
    }

    @GetMapping("/concept")
    public String concept() {
        return "concept";
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/technique")
    public String technique() {
        return "technique";
    }
}
