package atizay.controller;

import atizay.model.Abonnement;
import atizay.model.Salon;
import atizay.model.Proprietaire;
import atizay.repository.AbonnementRepository;
import atizay.repository.SalonRepository;
import atizay.service.MonCashService;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class PaymentController {

    @Autowired
    private MonCashService monCashService;

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private AbonnementRepository abonnementRepository;

    /**
     * Initier le paiement MonCash pour un abonnement
     */
    @PostMapping("/payment/subscription/initiate")
    public String initiateSubscriptionPayment(@RequestParam("plan") String plan, 
                                             HttpSession session, 
                                             RedirectAttributes redirectAttributes) {
        try {
            // Générer un orderId unique pour MonCash
            String orderId = "SUB-" + UUID.randomUUID().toString().substring(0, 8);
            session.setAttribute("currentOrderId", orderId);
            session.setAttribute("currentPlan", plan);

            // Définir le montant selon le plan
            double amount = switch (plan) {
                case "pro" -> 1000.0;
                case "premium" -> 5000.0;
                default -> 0.0;
            };
            
            session.setAttribute("subscriptionAmount", amount);
            
            String redirectUrl = monCashService.initiatePayment(orderId, amount);
            return "redirect:" + redirectUrl;

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'initialisation du paiement MonCash : " + e.getMessage());
            return "redirect:/proprietaire/dashboard#abonnement";
        }
    }

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
     * Callback MonCash après paiement (succès ou échec)
     * MonCash redirige vers cette URL avec ?transactionId=... et ?orderId=...
     */
    @GetMapping("/payment/callback/atizay-subscription")
    public String paymentCallback(@RequestParam(value = "transactionId", required = false) String transactionId,
                                  @RequestParam(value = "orderId", required = false) String orderId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        System.out.println("=== DEBUG: Callback appelé ===");
        System.out.println("TransactionId: " + transactionId);
        System.out.println("OrderId: " + orderId);
        System.out.println("Session currentOrderId: " + session.getAttribute("currentOrderId"));
        System.out.println("Session currentPlan: " + session.getAttribute("currentPlan"));
        System.out.println("Session subscriptionAmount: " + session.getAttribute("subscriptionAmount"));
        
        try {
            // Vérifier le statut du paiement
            JsonObject paymentStatus = monCashService.verifyPaymentByTransaction(transactionId);
            System.out.println("PaymentStatus: " + (paymentStatus != null ? paymentStatus.toString() : "null"));
            
            // Vérifier si le paiement est réussi (selon documentation MonCash: message="successful")
            if (paymentStatus != null && paymentStatus.has("payment") && 
                paymentStatus.getAsJsonObject("payment").has("message") &&
                paymentStatus.getAsJsonObject("payment").get("message").getAsString().equals("successful")) {
                
                String currentOrderId = (String) session.getAttribute("currentOrderId");
                String currentPlan = (String) session.getAttribute("currentPlan");
                Double subscriptionAmount = (Double) session.getAttribute("subscriptionAmount");
                
                System.out.println("currentOrderId: " + currentOrderId);
                System.out.println("currentPlan: " + currentPlan);
                System.out.println("subscriptionAmount: " + subscriptionAmount);
                
                // Vérifier si c'est un paiement d'abonnement (présence de currentPlan dans la session)
                if (currentPlan != null && subscriptionAmount != null) {
                    // C'est un paiement d'abonnement - stocker les infos pour création après connexion
                    System.out.println("=== Stockage des données de session ===");
                    session.setAttribute("pendingSubscriptionPlan", currentPlan);
                    session.setAttribute("pendingSubscriptionAmount", subscriptionAmount);
                    session.setAttribute("pendingSubscriptionTransactionId", transactionId);
                    session.setAttribute("pendingSubscriptionDate", LocalDateTime.now());
                    session.removeAttribute("currentOrderId");
                    session.removeAttribute("currentPlan");
                    session.removeAttribute("subscriptionAmount");
                    
                    System.out.println("Données stockées dans la session:");
                    System.out.println("pendingSubscriptionPlan: " + session.getAttribute("pendingSubscriptionPlan"));
                    System.out.println("pendingSubscriptionAmount: " + session.getAttribute("pendingSubscriptionAmount"));
                    
                    redirectAttributes.addFlashAttribute("successMessage", "Paiement réussi ! Veuillez vous connecter pour activer votre abonnement " + currentPlan.toUpperCase() + ".");
                    return "redirect:/auth/connexion";
                } else {
                    // C'est un paiement de création de salon
                    Salon salon = (Salon) session.getAttribute("pendingSalon");
                    Proprietaire p = (Proprietaire) session.getAttribute("proprietaire");

                    if (salon != null && p != null) {
                        salon.setProprietaire(p);
                        if (salon.getSlug() == null || salon.getSlug().isEmpty()) {
                            salon.setSlug(slugify(salon.getNomSalon()));
                        }
                        salonRepository.save(salon);
                        session.removeAttribute("pendingSalon");
                        session.removeAttribute("currentOrderId");

                        redirectAttributes.addFlashAttribute("successMessage", "Votre salon " + salon.getNomSalon() + " a été créé avec succès !");
                        return "redirect:/proprietaire/dashboard";
                    }
                }
            }
            
            // Paiement non complété
            redirectAttributes.addFlashAttribute("errorMessage", "Le paiement n'a pas été complété.");
            return "redirect:/auth/connexion";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la vérification du paiement : " + e.getMessage());
            return "redirect:/auth/connexion";
        }
    }

    /**
     * Retour de MonCash après succès du paiement (ancienne méthode)
     */
    @GetMapping("/payment/success")
    public String paymentSuccess(HttpSession session, RedirectAttributes redirectAttributes) {
        Salon salon = (Salon) session.getAttribute("pendingSalon");
        Proprietaire p = (Proprietaire) session.getAttribute("proprietaire");

        if (salon != null && p != null) {
            salon.setProprietaire(p);
            if (salon.getSlug() == null || salon.getSlug().isEmpty()) {
                salon.setSlug(slugify(salon.getNomSalon()));
            }
            salonRepository.save(salon);
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
        return "redirect:/proprietaire/dashboard#abonnement";
    }

    private String slugify(String input) {
        if (input == null || input.isEmpty()) return "salon-" + UUID.randomUUID().toString().substring(0, 4);
        return input.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "")
                    .replace(" ", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-", "")
                    .replaceAll("-$", "");
    }

    /**
     * Créer l'abonnement dans la base de données après connexion du propriétaire
     */
    @Transactional
    public void createSubscriptionAfterLogin(Proprietaire proprietaire, HttpSession session) {
        String plan = (String) session.getAttribute("pendingSubscriptionPlan");
        Double amount = (Double) session.getAttribute("pendingSubscriptionAmount");
        String transactionId = (String) session.getAttribute("pendingSubscriptionTransactionId");
        LocalDateTime date = (LocalDateTime) session.getAttribute("pendingSubscriptionDate");

        System.out.println("=== DEBUG: createSubscriptionAfterLogin appelé ===");
        System.out.println("Propriétaire: " + (proprietaire != null ? proprietaire.getEmail() : "null"));
        System.out.println("Plan: " + plan);
        System.out.println("Amount: " + amount);
        System.out.println("TransactionId: " + transactionId);
        System.out.println("Date: " + date);

        if (plan != null && amount != null) {
            Abonnement abonnement = new Abonnement();
            abonnement.setProprietaire(proprietaire);
            abonnement.setTypeAbonnement(plan.toUpperCase());
            abonnement.setPrixMensuel(amount);
            abonnement.setDateDebut(date != null ? date : LocalDateTime.now());
            abonnement.setDateFin(LocalDateTime.now().plusMonths(1));
            abonnement.setStatut("Actif");
            abonnement.setAutoRenouvellement(true);
            
            Abonnement saved = abonnementRepository.save(abonnement);
            System.out.println("=== DEBUG: Abonnement créé avec ID: " + saved.getIdAbonnement() + " ===");
            
            // Nettoyer la session
            session.removeAttribute("pendingSubscriptionPlan");
            session.removeAttribute("pendingSubscriptionAmount");
            session.removeAttribute("pendingSubscriptionTransactionId");
            session.removeAttribute("pendingSubscriptionDate");
        } else {
            System.out.println("=== DEBUG: Plan ou amount null, abonnement non créé ===");
        }
    }
}
