package atizay.controller;

import atizay.model.Salon;
import atizay.model.Proprietaire;
import atizay.repository.SalonRepository;
import atizay.service.MonCashService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class PaymentController {

    @Autowired
    private MonCashService monCashService;

    @Autowired
    private SalonRepository salonRepository;

    /**
     * Initier le paiement MonCash pour un nouveau salon
     */
    @GetMapping("/payment/initiate")
    public String initiatePayment(HttpSession session, RedirectAttributes redirectAttributes) {
        Salon pendingSalon = (Salon) session.getAttribute("pendingSalon");
        if (pendingSalon == null) {
            return "redirect:/proprietaire/salons/creer";
        }

        try {
            // Générer un orderId unique pour MonCash
            String orderId = "SALON-" + UUID.randomUUID().toString().substring(0, 8);
            session.setAttribute("currentOrderId", orderId);

            // Prix fixé à 1000 GDS
            double amount = 1000.0;
            
            String redirectUrl = monCashService.initiatePayment(orderId, amount);
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'initialisation du paiement MonCash : " + e.getMessage());
            return "redirect:/proprietaire/salons/creer";
        }
    }

    /**
     * Retour de MonCash après succès du paiement
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(HttpSession session, RedirectAttributes redirectAttributes) {
        Salon salon = (Salon) session.getAttribute("pendingSalon");
        Proprietaire p = (Proprietaire) session.getAttribute("proprietaire");

        if (salon != null && p != null) {
            salon.setProprietaire(p);
            // Assurer que le slug est présent
            if (salon.getSlug() == null || salon.getSlug().isEmpty()) {
                salon.setSlug(slugify(salon.getNomSalon()));
            }
            
            salonRepository.save(salon);
            
            // Nettoyage de la session
            session.removeAttribute("pendingSalon");
            session.removeAttribute("currentOrderId");

            redirectAttributes.addFlashAttribute("successMessage", "Votre salon " + salon.getNomSalon() + " a été créé avec succès après paiement !");
            return "redirect:/proprietaire/dashboard";
        }

        return "redirect:/proprietaire/dashboard";
    }

    /**
     * Retour de MonCash après échec ou annulation
     */
    @GetMapping("/payment/error")
    public String paymentError(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Le paiement MonCash a été annulé ou a échoué. Veuillez réessayer.");
        return "redirect:/proprietaire/salons/creer";
    }

    // Version simplifiée du slugify (interne au contrôleur pour l'instant)
    private String slugify(String input) {
        if (input == null || input.isEmpty()) return "salon-" + UUID.randomUUID().toString().substring(0, 4);
        return input.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "")
                    .replace(" ", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-", "")
                    .replaceAll("-$", "");
    }
}
