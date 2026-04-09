package atizay.controller;

import atizay.model.Prestation;
import atizay.model.Salon;
import atizay.repository.SalonRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class PublicController {

    @Autowired
    private SalonRepository salonRepository;

    @GetMapping("/rechercher")
    public String rechercher(@RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "location", required = false) String location,
            Model model) {
        List<Salon> salons;
        if (q != null && !q.trim().isEmpty()) {
            salons = salonRepository.findByNomSalonContainingIgnoreCase(q);
        } else {
            salons = salonRepository.findAll();
        }

        if (location != null && !location.trim().isEmpty()) {
            String searchLoc = location.toLowerCase();
            salons = salons.stream()
                    .filter(s -> {
                        String adresse = s.getAdresseSalon();
                        if (adresse == null)
                            return false;
                        return adresse.toLowerCase().contains(searchLoc);
                    })
                    .collect(Collectors.toList());
        }

        model.addAttribute("salons", salons);
        model.addAttribute("query", q);
        model.addAttribute("location", location);

        return "salons/recherche";
    }

    @GetMapping("/salon/{id}")
    public String detailsSalon(@PathVariable("id") Long id, Model model) {
        Salon salon = salonRepository.findById(id).orElse(null);
        if (salon == null) {
            return "redirect:/";
        }

        model.addAttribute("salon", salon);
        model.addAttribute("employes", salon.getListeEmployes());

        // Group prestations by their String category
        java.util.Map<String, List<atizay.model.Prestation>> prestationsParCategorie = salon.getListePrestations()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getCategorie() != null && !p.getCategorie().isEmpty() ? p.getCategorie() : "Autres"));

        model.addAttribute("prestationsParCategorie", prestationsParCategorie);

        return "salons/details";
    }
}
