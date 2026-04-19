package atizay.controller;

import atizay.model.Client;
import atizay.model.Salon;
import atizay.model.Prestation;
import atizay.model.RendezVous;
import atizay.repository.SalonRepository;
import atizay.repository.PrestationRepository;
import atizay.repository.MediaSalonRepository;
import atizay.repository.RendezVousRepository;
import atizay.repository.AvisSalonRepository;
import atizay.repository.ClientRepository;
import atizay.repository.HoraireSalonRepository;
import atizay.repository.EmployeRepository;
import atizay.repository.AvisEmployeRepository;
import atizay.model.AvisEmploye;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private MediaSalonRepository mediaSalonRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private AvisSalonRepository avisSalonRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private HoraireSalonRepository horaireSalonRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private AvisEmployeRepository avisEmployeRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if ("client".equals(userType) && client != null) {
            // Recharger le client pour avoir les favoris à jour
            client = clientRepository.findById(client.getId()).orElse(client);
            model.addAttribute("client", client);
            
            // Liste des IDs des salons favoris pour l'affichage
            List<Long> favoriIds = client.getSalonsFavoris().stream()
                .map(Salon::getIdSalon)
                .collect(Collectors.toList());
            model.addAttribute("favoriIds", favoriIds);
            
            // Charger tous les salons pour le dashboard
            List<Salon> salons = salonRepository.findAll();
            // Charger les médias pour chaque salon
            for (Salon salon : salons) {
                salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
                salon.setListeAvis(avisSalonRepository.findBySalonIdSalon(salon.getIdSalon()));
            }
            
            model.addAttribute("salons", salons);
            model.addAttribute("reservationsCount", rendezVousRepository.findByClient_Id(client.getId()).size());
            model.addAttribute("favorisCount", favoriIds.size());
            model.addAttribute("salonsVisites", 0);
            return "client/dashboard-client";
        }

        return "redirect:/auth/connexion";
    }

    @GetMapping("/recherche")
    public String recherche(@RequestParam(value = "prestation", required = false, defaultValue = "") String quoi,
                           @RequestParam(value = "lieu", required = false, defaultValue = "") String ou,
                           HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if ("client".equals(userType) && client != null) {
            client = clientRepository.findById(client.getId()).orElse(client);
            model.addAttribute("client", client);
            
            List<Long> favoriIds = client.getSalonsFavoris().stream()
                .map(Salon::getIdSalon)
                .collect(Collectors.toList());
            model.addAttribute("favoriIds", favoriIds);

            // Recherche avec les critères
            List<Salon> salons = salonRepository.searchSalons(quoi, ou);
            
            // Charger les médias pour chaque salon
            for (Salon salon : salons) {
                salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
                salon.setListeAvis(avisSalonRepository.findBySalonIdSalon(salon.getIdSalon()));
            }
            
            model.addAttribute("salons", salons);
            model.addAttribute("quoi", quoi);
            model.addAttribute("ou", ou);
            model.addAttribute("searchMode", true);
            
            return "client/dashboard-client";
        }
        return "redirect:/auth/connexion";
    }

    @GetMapping("/reservations")
    public String reservations(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if (!"client".equals(userType) || client == null) {
            return "redirect:/auth/connexion";
        }

        model.addAttribute("client", client);

        // Charger les réservations du client triées par date décroissante
        List<RendezVous> reservations = rendezVousRepository.findByClient_IdOrderByDateRendezVousDesc(client.getId());
        model.addAttribute("reservations", reservations);

        return "client/reservations";
    }

    @GetMapping("/favoris")
    public String favoris(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if (!"client".equals(userType) || client == null) {
            return "redirect:/auth/connexion";
        }

        // Recharger le client pour avoir les favoris à jour
        client = clientRepository.findById(client.getId()).orElse(client);
        model.addAttribute("client", client);

        // Charger les favoris du client (salons favoris)
        List<Salon> favoris = client.getSalonsFavoris();
        // S'assurer que les médias sont chargés pour chaque favori
        for (Salon salon : favoris) {
            salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
        }
        
        model.addAttribute("favoris", favoris);

        return "client/favoris";
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

    @PostMapping("/favoris/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleFavori(@PathVariable("id") Long idSalon, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Client client = (Client) session.getAttribute("user");

        if (client == null) {
            response.put("error", "Utilisateur non connecté");
            return response;
        }

        client = clientRepository.findById(client.getId()).orElse(null);
        Salon salon = salonRepository.findById(idSalon).orElse(null);

        if (client != null && salon != null) {
            boolean isFavori = false;
            if (client.getSalonsFavoris() == null) {
                client.setSalonsFavoris(new ArrayList<>());
            }

            // Vérifier si déjà présent
            Long salonId = salon.getIdSalon();
            boolean exists = client.getSalonsFavoris().stream()
                .anyMatch(s -> s.getIdSalon().equals(salonId));

            if (exists) {
                client.getSalonsFavoris().removeIf(s -> s.getIdSalon().equals(salonId));
                isFavori = false;
            } else {
                client.getSalonsFavoris().add(salon);
                isFavori = true;
            }

            clientRepository.save(client);
            // Mettre à jour la session
            session.setAttribute("user", client);
            
            response.put("success", true);
            response.put("isFavori", isFavori);
        } else {
            response.put("error", "Client ou Salon non trouvé");
        }

        return response;
    }

    @GetMapping("/favoris/supprimer/{id}")
    public String supprimerFavori(@PathVariable("id") Long idSalon, HttpSession session, RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Client client = (Client) session.getAttribute("user");

        if (!"client".equals(userType) || client == null) {
            return "redirect:/auth/connexion";
        }

        client = clientRepository.findById(client.getId()).orElse(null);
        Salon salon = salonRepository.findById(idSalon).orElse(null);

        if (client != null && salon != null) {
            client.getSalonsFavoris().removeIf(s -> s.getIdSalon().equals(idSalon));
            clientRepository.save(client);
            session.setAttribute("user", client);
            redirectAttributes.addFlashAttribute("success", "Salon retiré de vos favoris.");
        }

        return "redirect:/client/favoris";
    }

    @GetMapping("/salon/{id}")
    public String voirSalon(@PathVariable("id") Long idSalon, HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Object user = session.getAttribute("user");

        if (user == null || (!"client".equals(userType) && !"proprietaire".equals(userType))) {
            return "redirect:/auth/connexion";
        }

        Salon salon = salonRepository.findById(idSalon).orElse(null);
        if (salon != null) {
            // Charger explicitement les horaires AVANT les avis pour éviter les problèmes de lazy loading
            List<atizay.model.HoraireSalon> horaires = horaireSalonRepository.findBySalonOrderByJourSemaine(salon);
            System.out.println("Horaires chargés: " + (horaires != null ? horaires.size() : 0));
            salon.setListeHoraires(horaires != null ? horaires : new java.util.ArrayList<>());
            
            salon.setListeMedias(mediaSalonRepository.findBySalon(salon));
            // Charger les avis en dernier
            List<atizay.model.AvisSalon> avis = avisSalonRepository.findBySalonIdSalonOrderByDateAvisDesc(idSalon);
            System.out.println("Avis chargés: " + avis.size());
            salon.setListeAvis(avis);
            
            List<Prestation> prestations = prestationRepository.findBySalonIdSalon(idSalon);
            
            // Grouper les prestations par catégorie
            Map<String, List<Prestation>> prestationsParCategorie = prestations.stream()
                .collect(Collectors.groupingBy(p -> p.getCategorie() != null ? p.getCategorie() : "Autres"));
            
            // Préparer les URLs des photos pour le JS
            List<String> photoUrls = salon.getListeMedias().stream()
                .map(m -> m.getUrlMedia().startsWith("/uploads/salons/") ? m.getUrlMedia() : "/uploads/salons/" + m.getUrlMedia())
                .collect(Collectors.toList());
            
            // Favoris du client
            if (user instanceof Client) {
                Client client = (Client) user;
                client = clientRepository.findById(client.getId()).orElse(client);
                model.addAttribute("client", client);
                List<Long> favoriIds = client.getSalonsFavoris().stream()
                    .map(Salon::getIdSalon)
                    .collect(Collectors.toList());
                model.addAttribute("favoriIds", favoriIds);
            }

            // Récupérer les employés actifs du salon
            List<atizay.model.Employe> employes = employeRepository.findBySalon(salon).stream()
                .filter(atizay.model.Employe::isActifEmploye)
                .collect(Collectors.toList());

            model.addAttribute("salon", salon);
            model.addAttribute("prestationsParCategorie", prestationsParCategorie);
            model.addAttribute("photoUrls", photoUrls);
            model.addAttribute("employes", employes);
        }

        return "client/salon-detail";
    }

    @PostMapping("/salon/{id}/avis")
    public String donnerAvis(@PathVariable("id") Long idSalon,
                            @RequestParam("note") Integer note,
                            @RequestParam(value = "commentaire", required = false, defaultValue = "Note sans commentaire") String commentaire,
                            HttpSession session, RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (user == null) return "redirect:/auth/connexion";

        Salon salon = salonRepository.findById(idSalon).orElse(null);
        if (salon != null) {
            // On gère à la fois Client et Proprietaire (car Pr. hérite de Client)
            if (user instanceof Client) {
                Client client = (Client) user;
                
                // Vérifier si le client a déjà donné un avis pour ce salon
                java.util.Optional<atizay.model.AvisSalon> avisExistant = avisSalonRepository.findBySalonIdSalonAndClient(idSalon, client);
                
                if (avisExistant.isPresent()) {
                    // Mettre à jour l'avis existant
                    atizay.model.AvisSalon avis = avisExistant.get();
                    avis.setNote(note);
                    avis.setDateAvis(java.time.LocalDateTime.now());
                    avisSalonRepository.save(avis);
                    redirectAttributes.addFlashAttribute("success", "Votre avis a été mis à jour !");
                } else {
                    // Créer un nouvel avis
                    atizay.model.AvisSalon avis = new atizay.model.AvisSalon();
                    avis.setSalon(salon);
                    avis.setClient(client);
                    avis.setNote(note);
                    avis.setDateAvis(java.time.LocalDateTime.now());
                    avisSalonRepository.save(avis);
                    redirectAttributes.addFlashAttribute("success", "Merci pour votre avis !");
                }
            } else {
                return "redirect:/auth/connexion";
            }
            
            // Recalculer la note moyenne
            List<atizay.model.AvisSalon> tousLesAvis = avisSalonRepository.findBySalonIdSalon(idSalon);
            double moyenne = tousLesAvis.stream().mapToInt(atizay.model.AvisSalon::getNote).average().orElse(0.0);
            salon.setNoteMoyenne(moyenne);
            // Ne pas sauvegarder le salon pour éviter les problèmes de lazy loading
        }
        
        return "redirect:/client/salon/" + idSalon;
    }

    @GetMapping("/nettoyer-avis-duplicats")
    public String nettoyerAvisDuplicats(HttpSession session, RedirectAttributes redirectAttributes) {
        Object user = session.getAttribute("user");
        if (user == null) return "redirect:/auth/connexion";

        // Nettoyer les avis duplicatifs pour tous les salons
        List<Salon> tousLesSalons = salonRepository.findAll();
        int totalSupprimes = 0;

        for (Salon salon : tousLesSalons) {
            List<atizay.model.AvisSalon> tousLesAvis = avisSalonRepository.findBySalonIdSalon(salon.getIdSalon());
            Map<Long, atizay.model.AvisSalon> avisParClient = new java.util.LinkedHashMap<>();

            // Garder seulement l'avis le plus récent par client
            for (atizay.model.AvisSalon avis : tousLesAvis) {
                if (avis.getClient() != null) {
                    Long clientId = avis.getClient().getId();
                    if (!avisParClient.containsKey(clientId)) {
                        avisParClient.put(clientId, avis);
                    } else {
                        // Supprimer l'avis duplicat
                        avisSalonRepository.delete(avis);
                        totalSupprimes++;
                    }
                }
            }
        }

        redirectAttributes.addFlashAttribute("success", totalSupprimes + " avis duplicatifs supprimés.");
        return "redirect:/client/dashboard";
    }

    @GetMapping("/rendez-vous")
    public String afficherFormulaireRendezVous(@RequestParam("salonId") Long salonId,
                                                @RequestParam("prestationId") Long prestationId,
                                                HttpSession session, Model model) {
        Object user = session.getAttribute("user");
        if (user == null || !(user instanceof Client)) {
            return "redirect:/auth/connexion";
        }

        Salon salon = salonRepository.findById(salonId).orElse(null);
        Prestation prestation = prestationRepository.findById(prestationId).orElse(null);

        if (salon == null || prestation == null) {
            return "redirect:/client/dashboard";
        }

        // Récupérer les employés du salon
        List<atizay.model.Employe> tousEmployes = employeRepository.findBySalon(salon);
        List<atizay.model.Employe> employes = tousEmployes.stream()
            .filter(e -> e.isActifEmploye())
            .collect(Collectors.toList());

        model.addAttribute("salon", salon);
        model.addAttribute("prestation", prestation);
        model.addAttribute("employes", employes);
        model.addAttribute("client", user);

        return "client/rendez-vous";
    }

    @PostMapping("/rendez-vous/confirmer")
    public String confirmerRendezVous(@RequestParam("salonId") Long salonId,
                                     @RequestParam("prestationId") Long prestationId,
                                     @RequestParam("employeOption") String employeOption,
                                     @RequestParam(value = "employeId", required = false) Long employeId,
                                     @RequestParam("date") String date,
                                     @RequestParam("heure") String heure,
                                     HttpSession session, RedirectAttributes redirectAttributes) {
        System.out.println("DEBUG: Début création rendez-vous - salonId=" + salonId + ", prestationId=" + prestationId + ", employeOption=" + employeOption + ", date=" + date + ", heure=" + heure);
        
        Object client = session.getAttribute("user");
        if (client == null || !(client instanceof Client)) {
            System.out.println("DEBUG: Utilisateur non connecté ou n'est pas un client");
            return "redirect:/auth/connexion";
        }

        Salon salon = salonRepository.findById(salonId).orElse(null);
        Prestation prestation = prestationRepository.findById(prestationId).orElse(null);

        if (salon == null || prestation == null) {
            System.out.println("DEBUG: Salon ou prestation null");
            return "redirect:/client/dashboard";
        }

        // Parser la date et l'heure
        java.time.LocalDate dateRendezVous = java.time.LocalDate.parse(date);
        java.time.LocalTime heureDebut = java.time.LocalTime.parse(heure);
        java.time.LocalDateTime dateTimeDebut = java.time.LocalDateTime.of(dateRendezVous, heureDebut);
        java.time.LocalDateTime dateTimeFin = dateTimeDebut.plusMinutes(prestation.getDureeMinutes());

        System.out.println("DEBUG: dateTimeDebut=" + dateTimeDebut + ", dateTimeFin=" + dateTimeFin);

        // Récupérer les employés du salon
        List<atizay.model.Employe> tousEmployes = employeRepository.findBySalon(salon);
        List<atizay.model.Employe> employesActifs = tousEmployes.stream()
            .filter(e -> e.isActifEmploye())
            .collect(Collectors.toList());

        System.out.println("DEBUG: Nombre d'employés actifs: " + employesActifs.size());

        // Si un employé est choisi, créer un seul rendez-vous pour cet employé
        if ("yes".equals(employeOption) && employeId != null) {
            System.out.println("DEBUG: Employé spécifique choisi - employeId=" + employeId);
            atizay.model.Employe employe = employeRepository.findById(employeId).orElse(null);
            if (employe != null) {
                RendezVous rendezVous = new RendezVous();
                rendezVous.setClient((Client) client);
                rendezVous.setSalon(salon);
                rendezVous.setPrestation(prestation);
                rendezVous.setEmploye(employe);
                rendezVous.setDateRendezVous(dateTimeDebut);
                rendezVous.setHeureDebut(dateTimeDebut);
                rendezVous.setHeureFin(dateTimeFin);
                rendezVous.setStatut("En attente");
                rendezVous.setPrixTotal(prestation.getPrix());
                rendezVous.setDureeMinutes(prestation.getDureeMinutes());
                RendezVous saved = rendezVousRepository.save(rendezVous);
                System.out.println("DEBUG: Rendez-vous créé avec ID=" + saved.getIdRendezVous() + " pour employé " + employe.getPrenom() + " " + employe.getNom());
            } else {
                System.out.println("DEBUG: Employé non trouvé avec ID=" + employeId);
            }
        } else {
            // OPTION : Pas d'employé spécifique ("n'importe qui")
            // On crée un seul rendez-vous avec employe = null
            System.out.println("DEBUG: Aucun employé spécifique choisi - création d'un rendez-vous sans employé assigné");
            
            RendezVous rendezVous = new RendezVous();
            rendezVous.setClient((Client) client);
            rendezVous.setSalon(salon);
            rendezVous.setPrestation(prestation);
            rendezVous.setEmploye(null); // ACCEPTÉ par la DB maintenant
            rendezVous.setDateRendezVous(dateTimeDebut);
            rendezVous.setHeureDebut(dateTimeDebut);
            rendezVous.setHeureFin(dateTimeFin);
            rendezVous.setStatut("En attente");
            rendezVous.setPrixTotal(prestation.getPrix());
            rendezVous.setDureeMinutes(prestation.getDureeMinutes());
            
            rendezVousRepository.save(rendezVous);
            System.out.println("DEBUG: Rendez-vous créé avec ID=" + rendezVous.getIdRendezVous() + " sans employé assigné");
        }

        return "redirect:/client/reservations";
    }

    @GetMapping("/rendez-vous/horaires")
    @ResponseBody
    public Map<String, String> getHorairesSalon(@RequestParam("salonId") Long salonId,
                                                 @RequestParam("date") String date) {
        Map<String, String> horaires = new HashMap<>();
        horaires.put("ouverture", null);
        horaires.put("fermeture", null);
        horaires.put("ferme", "false");

        try {
            java.time.LocalDate dateRendezVous = java.time.LocalDate.parse(date);
            java.time.DayOfWeek jourSemaine = dateRendezVous.getDayOfWeek();

            // Convertir le jour de la semaine en String (Dimanche, Lundi, etc.)
            String jourSemaineStr = switch (jourSemaine.getValue()) {
                case 1 -> "Lundi";
                case 2 -> "Mardi";
                case 3 -> "Mercredi";
                case 4 -> "Jeudi";
                case 5 -> "Vendredi";
                case 6 -> "Samedi";
                case 7 -> "Dimanche";
                default -> "";
            };

            System.out.println("DEBUG: Recherche horaires pour salonId=" + salonId + ", date=" + date + ", jour=" + jourSemaineStr);

            // Récupérer les horaires du salon pour ce jour
            List<atizay.model.HoraireSalon> tousHoraires = horaireSalonRepository.findBySalonOrderByJourSemaine(
                salonRepository.findById(salonId).orElse(null)
            );

            System.out.println("DEBUG: Tous les horaires trouvés: " + tousHoraires.size());
            tousHoraires.forEach(h -> System.out.println("DEBUG: - " + h.getJourSemaine() + " ouvert=" + h.isEstOuvert() + " " + h.getHeureOuverture() + "-" + h.getHeureFermeture()));

            // Filtrer par jour de la semaine
            List<atizay.model.HoraireSalon> horairesJour = tousHoraires.stream()
                .filter(h -> h.getJourSemaine().equals(jourSemaineStr))
                .collect(Collectors.toList());

            System.out.println("DEBUG: Horaires filtrés pour " + jourSemaineStr + ": " + horairesJour.size());

            if (horairesJour.isEmpty()) {
                System.out.println("DEBUG: Aucun horaire trouvé pour ce jour -> salon fermé");
                horaires.put("ferme", "true");
                return horaires;
            }

            atizay.model.HoraireSalon horaire = horairesJour.get(0);
            System.out.println("DEBUG: Horaire trouvé - estOuvert=" + horaire.isEstOuvert());

            if (!horaire.isEstOuvert()) {
                System.out.println("DEBUG: Horaire marqué comme fermé -> salon fermé");
                horaires.put("ferme", "true");
                return horaires;
            }

            horaires.put("ouverture", horaire.getHeureOuverture().toString());
            horaires.put("fermeture", horaire.getHeureFermeture().toString());
            horaires.put("ferme", "false");
            System.out.println("DEBUG: Salon ouvert - " + horaire.getHeureOuverture() + "-" + horaire.getHeureFermeture());

        } catch (Exception e) {
            System.out.println("DEBUG: Erreur: " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, retourner des valeurs null
        }

        return horaires;
    }
    @PostMapping("/reservations/annuler/{id}")
    @Transactional
    public String annulerReservation(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Client client = (Client) session.getAttribute("user");
        RendezVous rdv = rendezVousRepository.findById(id).orElse(null);

        if (rdv != null && rdv.getClient().getId().equals(client.getId())) {
            rdv.setStatut("Annulé");
            rendezVousRepository.save(rdv);
        }
        return "redirect:/client/reservations";
    }
    @PostMapping("/employe/{id}/note")
    @ResponseBody
    public Map<String, Object> noterEmploye(@PathVariable("id") Long idEmploye,
                                           @RequestParam("note") Integer note,
                                           HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Client client = (Client) session.getAttribute("user");

        if (client == null) {
            response.put("error", "Veuillez vous connecter");
            return response;
        }

        atizay.model.Employe employe = employeRepository.findById(idEmploye).orElse(null);
        if (employe != null) {
            // Créer un nouvel avis
            AvisEmploye avis = new AvisEmploye();
            avis.setEmploye(employe);
            avis.setClient(client);
            avis.setNote(note);
            avis.setDateAvis(java.time.LocalDateTime.now());
            avisEmployeRepository.save(avis);

            // Recalculer la moyenne de l'employé
            List<AvisEmploye> avisListe = avisEmployeRepository.findByEmployeId(idEmploye);
            double moyenne = avisListe.stream().mapToInt(AvisEmploye::getNote).average().orElse(0.0);
            employe.setNoteMoyenne(moyenne);
            employeRepository.save(employe);

            response.put("success", true);
            response.put("moyenne", moyenne);
        } else {
            response.put("error", "Employé non trouvé");
        }

        return response;
    }
}
