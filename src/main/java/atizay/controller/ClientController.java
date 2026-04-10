package atizay.controller;

import atizay.model.Client;
import atizay.model.Salon;
import atizay.model.Prestation;
import atizay.repository.SalonRepository;
import atizay.repository.PrestationRepository;
import atizay.repository.MediaSalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private MediaSalonRepository mediaSalonRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if ("client".equals(userType) && client != null) {
            model.addAttribute("client", client);
            
            // Charger tous les salons pour le dashboard
            List<Salon> salons = salonRepository.findAll();
            // Charger les médias pour chaque salon
            for (Salon salon : salons) {
                salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
            }
            
            model.addAttribute("salons", salons);
            model.addAttribute("reservationsCount", 0);
            model.addAttribute("favorisCount", 0);
            model.addAttribute("salonsVisites", 0);
            return "client/dashboard-client";
        }

        return "redirect:/auth/connexion";
    }

    @GetMapping("/recherche")
    public String recherche(@RequestParam(value = "prestation", required = false) String prestation,
                           @RequestParam(value = "lieu", required = false) String lieu,
                           HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if ("client".equals(userType) && client != null) {
            model.addAttribute("client", client);
            
            List<Salon> salons;
            
            // Filtrer par prestation et/ou lieu
            if (prestation != null && !prestation.isEmpty() && lieu != null && !lieu.isEmpty()) {
                // Recherche par prestation et lieu
                List<Prestation> prestations = prestationRepository.findByNomPrestationContainingIgnoreCase(prestation);
                salons = salonRepository.findByNomSalonContainingIgnoreCaseOrVilleSalonContainingIgnoreCase(lieu, lieu);
                // Filtrer pour ne garder que les salons qui ont les prestations recherchées
                salons = salons.stream()
                    .filter(s -> prestations.stream().anyMatch(p -> p.getSalon().getIdSalon().equals(s.getIdSalon())))
                    .toList();
            } else if (prestation != null && !prestation.isEmpty()) {
                // Recherche par prestation uniquement
                List<Prestation> prestations = prestationRepository.findByNomPrestationContainingIgnoreCase(prestation);
                salons = prestations.stream()
                    .map(Prestation::getSalon)
                    .distinct()
                    .toList();
            } else if (lieu != null && !lieu.isEmpty()) {
                // Recherche par lieu uniquement
                salons = salonRepository.findByNomSalonContainingIgnoreCaseOrVilleSalonContainingIgnoreCase(lieu, lieu);
            } else {
                // Aucun filtre, afficher tous les salons
                salons = salonRepository.findAll();
            }
            
            // Charger les médias pour chaque salon
            for (Salon salon : salons) {
                salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
            }
            
            model.addAttribute("salons", salons);
            model.addAttribute("prestation", prestation);
            model.addAttribute("lieu", lieu);
            return "client/dashboard-client";
        }

        return "redirect:/auth/connexion";
    }

    @GetMapping("/salon/{id}/prestations")
    @ResponseBody
    public Map<String, Object> getPrestationsSalon(@PathVariable("id") Long idSalon) {
        Map<String, Object> response = new HashMap<>();
        
        Salon salon = salonRepository.findById(idSalon).orElse(null);
        if (salon != null) {
            List<Prestation> prestations = prestationRepository.findBySalonIdSalon(idSalon);
            response.put("salonName", salon.getNomSalon());
            response.put("prestations", prestations);
        } else {
            response.put("error", "Salon non trouvé");
        }
        
        return response;
    }
}
