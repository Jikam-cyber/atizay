package atizay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/search")
public class SearchController {

    @GetMapping
    public String searchPage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String query,
            Model model) {
        
        // Ajouter les paramètres de recherche au modèle
        model.addAttribute("selectedService", service);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("searchQuery", query);
        
        // Ajouter des données de test pour la démo
        model.addAttribute("pageTitle", "Rechercher un salon - Atizay");
        model.addAttribute("pageDescription", "Trouvez le salon de beauté parfait près de chez vous");
        
        return "salons/recherche-planity";
    }
    
    @GetMapping("/salons")
    public String searchSalons(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        
        // Logique de recherche des salons
        model.addAttribute("selectedService", service);
        model.addAttribute("selectedLocation", location);
        model.addAttribute("searchQuery", query);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        
        return "salons/recherche-planity";
    }
}
