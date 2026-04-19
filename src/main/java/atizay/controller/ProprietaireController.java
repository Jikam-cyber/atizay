package atizay.controller;

import atizay.model.Prestation;
import atizay.model.Proprietaire;
import atizay.model.Salon;
import atizay.model.Employe;
import atizay.model.RendezVous;
import atizay.repository.PrestationRepository;
import atizay.repository.SalonRepository;
import atizay.repository.EmployeRepository;
import atizay.repository.RendezVousRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalTime;
import org.springframework.web.multipart.MultipartFile;
import atizay.repository.MediaSalonRepository;
import atizay.repository.HoraireSalonRepository;
import atizay.model.MediaSalon;
import atizay.model.HoraireSalon;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import atizay.repository.MediaPrestationRepository;
import atizay.model.MediaPrestation;
import org.springframework.security.crypto.password.PasswordEncoder;
import atizay.repository.ClientRepository;
import atizay.repository.ProprietaireRepository;

@Controller
@RequestMapping("/proprietaire")
public class ProprietaireController {

    @Autowired
    private SalonRepository salonRepository;

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private MediaSalonRepository mediaSalonRepository;

    @Autowired
    private HoraireSalonRepository horaireSalonRepository;

    @Autowired
    private MediaPrestationRepository mediaPrestationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProprietaireRepository proprietaireRepository;


    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/salons/";

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            List<Salon> salons = salonRepository.findByProprietaire(proprietaire);

            model.addAttribute("proprietaire", proprietaire);
            model.addAttribute("salons", salons);
            model.addAttribute("salonsCount", salons.size());
            model.addAttribute("employesCount", 0);
            model.addAttribute("rdvCount", 0);
            model.addAttribute("revenus", "0 HTG");
            
            return "proprietaire/dashboard-proprietaire";
        }

        return "redirect:/auth/connexion";
    }

    @GetMapping("/salons")
    public String salons(HttpSession session) {
        return "redirect:/proprietaire/dashboard";
    }

    @Transactional
    @GetMapping("/salon/{identifier}")
    public String salonDashboard(@PathVariable("identifier") String identifier, 
                                @RequestParam(value = "section", required = false, defaultValue = "statistiques") String section,
                                HttpSession session, Model model) {
        String type = (String) session.getAttribute("type");
        if ("proprietaire".equals(type)) {
            Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
            
            Salon salon = findSalonByIdentifier(identifier, proprietaire);

            if (salon != null) {
                // Ensure salon has a slug for future cleaner URLs
                if (salon.getSlug() == null) {
                    salon.setSlug(slugify(salon.getNomSalon()));
                    salonRepository.save(salon);
                }

                model.addAttribute("salon", salon);
                model.addAttribute("user", proprietaire);
                model.addAttribute("type", "proprietaire");
                model.addAttribute("section", section);
                model.addAttribute("pageTitle", salon.getNomSalon() + " - " + section.substring(0, 1).toUpperCase() + section.substring(1));
                
                // Fetch data common to several sections to avoid null-pointer in Thymeleaf fragments
                List<Prestation> prestations = prestationRepository.findBySalon(salon);
                List<Employe> employes = employeRepository.findBySalon(salon);
                model.addAttribute("prestations", prestations);
                model.addAttribute("employes", employes);
                
                // Fetch data needed for specific sections
                if ("horaires".equals(section)) {
                    if (salon.getListeHoraires() == null || salon.getListeHoraires().isEmpty()) {
                        initDefaultHoraires(salon);
                        salon = salonRepository.findById(salon.getIdSalon()).get();
                        model.addAttribute("salon", salon);
                    }
                }

                if ("prestations".equals(section)) {
                    model.addAttribute("nouvellePrestation", new Prestation());
                } else if ("collaborateurs".equals(section)) {
                    model.addAttribute("nouveauCollaborateur", new Employe());
                } else if ("rendezvous".equals(section)) {
                    model.addAttribute("appointments", rendezVousRepository.findBySalon(salon));
                }
                
                return "proprietaire/dashboard-salon";
            }
            return "redirect:/proprietaire/dashboard";
        }
        return "redirect:/auth/connexion";
    }

    private String slugify(String input) {
        if (input == null || input.isEmpty()) return "salon";
        return input.toLowerCase()
                    .replaceAll("[^a-z0-9 ]", "")
                    .replace(" ", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-", "")
                    .replaceAll("-$", "");
    }

    @GetMapping("/creer-salon")
    public String creerSalonPage(HttpSession session, Model model) {
        return "redirect:/proprietaire/dashboard";
    }

    @PostMapping("/creer-salon")
    public String creerSalonSubmit(HttpSession session,
                                   @RequestParam("typeSalon") String typeSalon,
                                   @RequestParam("nomSalon") String nomSalon,
                                   @RequestParam("descriptionSalon") String descriptionSalon,
                                   @RequestParam("telephone") String telephone,
                                   @RequestParam("departement") String departement,
                                   @RequestParam("ville") String ville,
                                   @RequestParam("adresseSalon") String adresseSalon,
                                   RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            try {
                Salon salon = new Salon();
                salon.setTypeSalon(typeSalon);
                salon.setNomSalon(nomSalon);
                salon.setDescriptionSalon(descriptionSalon);
                salon.setTelephoneSalon(telephone);
                salon.setDepartement(departement);
                salon.setVilleSalon(ville);
                salon.setAdresseSalon(adresseSalon);
                salon.setProprietaire(proprietaire);
                salon.setSlug(slugify(nomSalon));

                salonRepository.save(salon);

                redirectAttributes.addFlashAttribute("successMessage", "Salon créé avec succès !");
                return "redirect:/proprietaire/dashboard";
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la création du salon: " + e.getMessage());
                return "redirect:/proprietaire/dashboard";
            }
        }
        return "redirect:/auth/connexion";
    }

    @PostMapping("/salon/{id}/supprimer")
    @Transactional
    public String supprimerSalon(@PathVariable("id") Long id,
                                  @RequestParam("confirmName") String confirmName,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if (!"proprietaire".equals(userType) || proprietaire == null) {
            return "redirect:/auth/connexion";
        }

        Salon salon = salonRepository.findById(id).orElse(null);
        if (salon == null || !salon.getProprietaire().getId().equals(proprietaire.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Salon introuvable ou accès refusé.");
            return "redirect:/proprietaire/dashboard";
        }

        // Vérification du nom exact
        if (!salon.getNomSalon().equals(confirmName)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Le nom saisi ne correspond pas. Suppression annulée.");
            return "redirect:/proprietaire/dashboard";
        }

        // Supprimer les fichiers physiques du salon (photos + photos de prestations)
        try {
            Path salonDir = Paths.get("uploads/salons/" + salon.getIdSalon());
            if (Files.exists(salonDir)) {
                Files.walk(salonDir)
                        .sorted(java.util.Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(java.io.File::delete);
            }
        } catch (IOException e) {
            System.err.println("Erreur suppression fichiers salon " + id + ": " + e.getMessage());
        }

        // Supprimer le salon (cascade supprime tout : prestations, medias, horaires, rdv, employes)
        salonRepository.delete(salon);

        redirectAttributes.addFlashAttribute("successMessage",
            "Le salon \"" + salon.getNomSalon() + "\" a été supprimé définitivement.");
        return "redirect:/proprietaire/dashboard";
    }

    @GetMapping("/salon/{id}/gestion")
    public String gestionSalon(@PathVariable("id") Long id, HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                // Charger les médias du salon
                List<MediaSalon> medias = mediaSalonRepository.findBySalon(salon);
                salon.setListeMedias(medias);

                // Charger les horaires du salon
                List<HoraireSalon> horaires = horaireSalonRepository.findBySalonOrderByJourSemaine(salon);
                salon.setListeHoraires(horaires);

                // Charger les prestations du salon
                List<Prestation> prestations = prestationRepository.findBySalon(salon);
                salon.setListePrestations(prestations);

                // Charger les rendez-vous du salon
                List<RendezVous> rendezVous = rendezVousRepository.findBySalon(salon);
                salon.setListeRendezVous(rendezVous);

                // Charger les employés du salon
                List<Employe> employes = employeRepository.findBySalon(salon);
                salon.setListeEmployes(employes);

                // Ajouter les horaires individuels au modèle pour faciliter l'affichage
                model.addAttribute("horaireLundi", getHoraireForDay(horaires, "Lundi"));
                model.addAttribute("horaireMardi", getHoraireForDay(horaires, "Mardi"));
                model.addAttribute("horaireMercredi", getHoraireForDay(horaires, "Mercredi"));
                model.addAttribute("horaireJeudi", getHoraireForDay(horaires, "Jeudi"));
                model.addAttribute("horaireVendredi", getHoraireForDay(horaires, "Vendredi"));
                model.addAttribute("horaireSamedi", getHoraireForDay(horaires, "Samedi"));
                model.addAttribute("horaireDimanche", getHoraireForDay(horaires, "Dimanche"));

                model.addAttribute("proprietaire", proprietaire);
                model.addAttribute("salon", salon);
                System.out.println("Salon chargé: " + salon.getNomSalon() + " avec " + medias.size() + " photos, " + horaires.size() + " horaires, " + prestations.size() + " prestations et " + rendezVous.size() + " rendez-vous");
                return "proprietaire/salon/gestion-salon";
            }
        }
        return "redirect:/auth/connexion";
    }

    private HoraireSalon getHoraireForDay(List<HoraireSalon> horaires, String jour) {
        if (horaires == null) return null;
        return horaires.stream()
                .filter(h -> jour.equals(h.getJourSemaine()))
                .findFirst()
                .orElse(null);
    }

    @GetMapping("/salons/creer")
    public String creerSalonForm(HttpSession session, Model model) {
        return "redirect:/proprietaire/dashboard";
    }

    @PostMapping("/salons/creer")
    public String creerSalon(HttpSession session, @ModelAttribute("salon") Salon salon) {
        String type = (String) session.getAttribute("type");
        if ("proprietaire".equals(type)) {
            // Ne pas enregistrer tout de suite, mettre en session pour le paiement
            salon.setSlug(slugify(salon.getNomSalon())); 
            session.setAttribute("pendingSalon", salon);
            
            // Rediriger vers le processus de paiement MonCash (1000 HTG)
            return "redirect:/payment/initiate";
        }
        return "redirect:/auth/connexion";
    }

    @PostMapping("/salon/{identifier}/prestations/ajouter")
    public String ajouterPrestation(@PathVariable("identifier") String identifier, 
                                   @ModelAttribute("nouvellePrestation") Prestation prestation, 
                                   HttpSession session) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            prestation.setSalon(salon);
            prestationRepository.save(prestation);
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=prestations";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @PostMapping("/salon/{identifier}/prestations/modifier")
    public String modifierPrestation(@PathVariable("identifier") String identifier,
                                    @RequestParam("idPrestation") Long id,
                                    @RequestParam("nomPrestation") String nom,
                                    @RequestParam("categorie") String categorie,
                                    @RequestParam("prix") Double prix,
                                    @RequestParam("dureeMinutes") Integer duree,
                                    @RequestParam(value="descriptionPrestation", required=false) String description,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            Prestation prestation = prestationRepository.findById(id).orElse(null);
            if (prestation != null && prestation.getSalon().getIdSalon().equals(salon.getIdSalon())) {
                prestation.setNomPrestation(nom);
                prestation.setCategorie(categorie);
                prestation.setPrix(prix);
                prestation.setDureeMinutes(duree);
                prestation.setDescriptionPrestation(description);
                prestationRepository.save(prestation);
                redirectAttributes.addFlashAttribute("successMessage", "La prestation '" + nom + "' a été mise à jour.");
            }
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=prestations";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @GetMapping("/salon/{identifier}/prestations/supprimer/{id}")
    public String supprimerPrestation(@PathVariable("identifier") String identifier,
                                     @PathVariable("id") Long id,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            Prestation prestation = prestationRepository.findById(id).orElse(null);
            if (prestation != null && prestation.getSalon().getIdSalon().equals(salon.getIdSalon())) {
                String nom = prestation.getNomPrestation();
                prestationRepository.delete(prestation);
                redirectAttributes.addFlashAttribute("successMessage", "La prestation '" + nom + "' a été supprimée.");
            }
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=prestations";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @PostMapping("/salon/{identifier}/collaborateurs/ajouter")
    public String ajouterCollaborateur(@PathVariable("identifier") String identifier, 
                                      @ModelAttribute("nouveauCollaborateur") Employe employe, 
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            // Sécurité : Vérifier si l'email (identifiant) est déjà utilisé
            if (employeRepository.findByEmail(employe.getEmail()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cet email est déjà utilisé par un autre collaborateur.");
                return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=collaborateurs";
            }

            // L'email sert de username par défaut
            employe.setUsername(employe.getEmail());
            employe.setSalon(salon);
            employe.setEstActif(true);
            employeRepository.save(employe);
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=collaborateurs";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @PostMapping("/salon/{identifier}/collaborateurs/modifier")
    public String modifierCollaborateur(@PathVariable("identifier") String identifier,
                                         @RequestParam("id") Long id,
                                         @RequestParam("nom") String nom,
                                         @RequestParam("prenom") String prenom,
                                         @RequestParam("email") String email,
                                         @RequestParam("specialite") String specialite,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            Employe employe = employeRepository.findById(id).orElse(null);
            if (employe != null && employe.getSalon().getIdSalon().equals(salon.getIdSalon())) {
                // Sécurité : Vérifier si le nouvel email est déjà utilisé par un autre collaborateur
                Employe existing = employeRepository.findByEmail(email);
                if (existing != null && !existing.getId().equals(id)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Cet email est déjà utilisé par un autre collaborateur.");
                    return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=collaborateurs";
                }

                employe.setNom(nom);
                employe.setPrenom(prenom);
                employe.setEmail(email);
                employe.setUsername(email); // Sync username with email
                employe.setSpecialite(specialite);
                employeRepository.save(employe);
                redirectAttributes.addFlashAttribute("successMessage", "Collaborateur mis à jour.");
            }
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=collaborateurs";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @GetMapping("/salon/{identifier}/collaborateurs/supprimer/{id}")
    public String supprimerCollaborateur(@PathVariable("identifier") String identifier,
                                          @PathVariable("id") Long id,
                                          HttpSession session) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            Employe employe = employeRepository.findById(id).orElse(null);
            if (employe != null && employe.getSalon().getIdSalon().equals(salon.getIdSalon())) {
                employeRepository.delete(employe);
            }
            return "redirect:/proprietaire/salon/" + (salon.getSlug() != null ? salon.getSlug() : salon.getIdSalon()) + "?section=collaborateurs";
        }
        return "redirect:/proprietaire/dashboard";
    }

    @Transactional
    @PostMapping("/salon/{identifier}/modifier")
    public String modifierSalon(@PathVariable("identifier") String identifier, 
                               @RequestParam("nomSalon") String nomSalon,
                               @RequestParam("departement") String departement,
                               @RequestParam("commune") String commune,
                               @RequestParam(value="quartier", required=false) String quartier,
                               @RequestParam(value="rue", required=false) String rue,
                               @RequestParam("typeSalon") String typeSalon,
                               @RequestParam("emailSalon") String emailSalon,
                               @RequestParam("telephoneSalon") String telephoneSalon,
                               @RequestParam(value="descriptionSalon", required=false) String description,
                               @RequestParam(value="photos", required=false) MultipartFile[] photos,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            salon.setNomSalon(nomSalon);
            salon.setDepartement(departement);
            salon.setCommune(commune);
            salon.setQuartier(quartier);
            salon.setRue(rue);
            
            // Sync with old fields for backward compatibility
            salon.setVilleSalon(commune);
            salon.setAdresseSalon((rue != null ? rue : "") + (quartier != null ? ", " + quartier : ""));
            
            salon.setTypeSalon(typeSalon);
            salon.setEmailSalon(emailSalon);
            salon.setTelephoneSalon(telephoneSalon);
            salon.setDescriptionSalon(description);
            salon.setSlug(slugify(nomSalon)); // Update slug if name changes
            
            // Logic to handle photos
            if (photos != null && photos.length > 0) {
                int currentPhotoCount = (salon.getListeMedias() != null) ? salon.getListeMedias().size() : 0;
                
                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) {
                        if (currentPhotoCount >= 10) {
                            // Stop adding if limit reached
                            System.err.println("Limite de 10 photos atteinte pour le salon : " + salon.getIdSalon());
                            break;
                        }
                        
                        try {
                            // Ensure upload directory exists
                            Path uploadPath = Paths.get(UPLOAD_DIR);
                            if (!Files.exists(uploadPath)) {
                                Files.createDirectories(uploadPath);
                            }

                            // Generate unique filename
                            String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                            Path filePath = uploadPath.resolve(fileName);
                            Files.copy(photo.getInputStream(), filePath);

                            // Create MediaSalon record
                            MediaSalon media = new MediaSalon();
                            media.setUrlMedia(salon.getIdSalon() + "/" + fileName);
                            media.setTypeMedia("Photo");
                            media.setSalon(salon);
                            mediaSalonRepository.save(media);
                            
                        } catch (IOException e) {
                            System.err.println("Erreur lors de l'enregistrement de la photo : " + e.getMessage());
                        }
                    }
                }
            }

            salonRepository.save(salon);
            redirectAttributes.addFlashAttribute("successMessage", "Les informations du salon ont été mises à jour avec succès.");
            return "redirect:/proprietaire/salon/" + salon.getSlug() + "?section=profil";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Salon non trouvé ou accès refusé.");
        return "redirect:/proprietaire/dashboard";
    }

    @GetMapping("/salon/{identifier}/media/supprimer/{id}")
    public String supprimerMedia(@PathVariable("identifier") String identifier, 
                                @PathVariable("id") Long id,
                                HttpSession session) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            MediaSalon media = mediaSalonRepository.findById(id).orElse(null);
            if (media != null && media.getSalon().getIdSalon().equals(salon.getIdSalon())) {
                // Remove physical file
                try {
                    String url = media.getUrlMedia();
                    Path filePath = Paths.get(UPLOAD_DIR + url);
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Impossible de supprimer le fichier physique : " + e.getMessage());
                }
                
                mediaSalonRepository.delete(media);
            }
            return "redirect:/proprietaire/salon/" + salon.getSlug() + "?section=profil";
        }
        return "redirect:/proprietaire/dashboard";
    }

    private Salon findSalonByIdentifier(String identifier, Proprietaire proprietaire) {
        Salon salon = null;
        try {
            Long id = Long.parseLong(identifier);
            salon = salonRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            salon = salonRepository.findBySlug(identifier);
        }
        
        if (salon == null) {
            List<Salon> matchingNames = salonRepository.findByNomSalonContainingIgnoreCase(identifier);
            if (!matchingNames.isEmpty()) {
                salon = matchingNames.get(0);
            }
        }

        if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
            return salon;
        }
        return null;
    }
    
    @Transactional
    private void initDefaultHoraires(Salon salon) {
        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String jour : jours) {
            boolean estFerme = (jour.equals("Dimanche") || jour.equals("Lundi"));
            HoraireSalon h = new HoraireSalon(jour, LocalTime.of(9, 0), LocalTime.of(19, 0), !estFerme, salon);
            if (jour.equals("Samedi")) h.setHeureOuverture(LocalTime.of(8, 30));
            horaireSalonRepository.save(h);
        }
    }

    @Transactional
    @PostMapping("/salon/{identifier}/horaires/modifier")
    public String modifierHoraires(@PathVariable("identifier") String identifier,
                                  @RequestParam("jour") String[] jours,
                                  @RequestParam(value="ouvert", required=false) List<String> ouverts,
                                  @RequestParam("debut") String[] debuts,
                                  @RequestParam("fin") String[] fins,
                                  HttpSession session) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            // Delete old hours and save new ones
            horaireSalonRepository.deleteBySalon(salon);
            
            for (int i = 0; i < jours.length; i++) {
                String jour = jours[i];
                boolean estOuvert = ouverts != null && ouverts.contains(jour);
                LocalTime debut = LocalTime.parse(debuts[i]);
                LocalTime fin = LocalTime.parse(fins[i]);
                
                HoraireSalon h = new HoraireSalon(jour, debut, fin, estOuvert, salon);
                horaireSalonRepository.save(h);
            }
            return "redirect:/proprietaire/salon/" + salon.getSlug() + "?section=horaires";
        }
        return "redirect:/proprietaire/dashboard";
    }

    // ========== GESTION DU PROFIL DU SALON ==========

    @PostMapping("/salon/{id}/profil/modifier")
    public String modifierProfilSalon(@PathVariable("id") Long id,
                                      @RequestParam("descriptionSalon") String description,
                                      @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                try {
                    // Modifier uniquement la description (nom et adresse non modifiables)
                    salon.setDescriptionSalon(description);
                    salonRepository.save(salon);

                    // Gérer les photos (max 15)
                    if (photos != null && photos.length > 0) {
                        int currentPhotos = salon.getListeMedias() != null ? salon.getListeMedias().size() : 0;
                        int photosToAdd = Math.min(photos.length, 15 - currentPhotos);
                        int photosSaved = 0;

                        for (int i = 0; i < photosToAdd; i++) {
                            try {
                                MultipartFile photo = photos[i];
                                if (!photo.isEmpty()) {
                                    String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                                    Path filePath = Paths.get(UPLOAD_DIR + salon.getIdSalon() + "/" + fileName);
                                    Files.createDirectories(filePath.getParent());
                                    photo.transferTo(filePath.toFile());

                                    MediaSalon media = new MediaSalon();
                                    media.setUrlMedia(salon.getIdSalon() + "/" + fileName);
                                    media.setTypeMedia("image");
                                    media.setSalon(salon);
                                    mediaSalonRepository.save(media);
                                    photosSaved++;

                                    System.out.println("Photo sauvegardée: " + fileName + " dans " + filePath);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'upload de la photo " + (i + 1) + ": " + e.getMessage());
                            }
                        }

                        if (photosSaved > 0) {
                            redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour avec succès ! " + photosSaved + " photo(s) ajoutée(s).");
                        } else {
                            redirectAttributes.addFlashAttribute("successMessage", "Description mise à jour avec succès !");
                        }
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage", "Description mise à jour avec succès !");
                    }

                    return "redirect:/proprietaire/salon/" + id + "/gestion";
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de la mise à jour: " + e.getMessage());
                    return "redirect:/proprietaire/salon/" + id + "/gestion";
                }
            }
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé");
        return "redirect:/auth/connexion";
    }

    @GetMapping("/salon/{id}/photo/supprimer/{idMedia}")
    public String supprimerPhoto(@PathVariable("id") Long idSalon,
                                  @PathVariable("idMedia") Long idMedia,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(idSalon).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                MediaSalon media = mediaSalonRepository.findById(idMedia).orElse(null);
                if (media != null && media.getSalon().getIdSalon().equals(idSalon)) {
                    // Supprimer le fichier physique
                    try {
                        Path filePath = Paths.get(UPLOAD_DIR + salon.getIdSalon() + "/" + media.getUrlMedia());
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Supprimer l'enregistrement en base de données
                    mediaSalonRepository.delete(media);
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion#profil";
                }
            }
        }
        return "redirect:/auth/connexion";
    }

    // ========== GESTION DES HORAIRES ==========

    @PostMapping("/salon/{id}/horaire/modifier")
    public String modifierHoraireSalon(@PathVariable("id") Long id,
                                       @RequestParam(value = "lundi_ouverture", required = false) String lundiOuverture,
                                       @RequestParam(value = "lundi_fermeture", required = false) String lundiFermeture,
                                       @RequestParam(value = "lundi_ferme", required = false) String lundiFerme,
                                       @RequestParam(value = "mardi_ouverture", required = false) String mardiOuverture,
                                       @RequestParam(value = "mardi_fermeture", required = false) String mardiFermeture,
                                       @RequestParam(value = "mardi_ferme", required = false) String mardiFerme,
                                       @RequestParam(value = "mercredi_ouverture", required = false) String mercrediOuverture,
                                       @RequestParam(value = "mercredi_fermeture", required = false) String mercrediFermeture,
                                       @RequestParam(value = "mercredi_ferme", required = false) String mercrediFerme,
                                       @RequestParam(value = "jeudi_ouverture", required = false) String jeudiOuverture,
                                       @RequestParam(value = "jeudi_fermeture", required = false) String jeudiFermeture,
                                       @RequestParam(value = "jeudi_ferme", required = false) String jeudiFerme,
                                       @RequestParam(value = "vendredi_ouverture", required = false) String vendrediOuverture,
                                       @RequestParam(value = "vendredi_fermeture", required = false) String vendrediFermeture,
                                       @RequestParam(value = "vendredi_ferme", required = false) String vendrediFerme,
                                       @RequestParam(value = "samedi_ouverture", required = false) String samediOuverture,
                                       @RequestParam(value = "samedi_fermeture", required = false) String samediFermeture,
                                       @RequestParam(value = "samedi_ferme", required = false) String samediFerme,
                                       @RequestParam(value = "dimanche_ouverture", required = false) String dimancheOuverture,
                                       @RequestParam(value = "dimanche_fermeture", required = false) String dimancheFermeture,
                                       @RequestParam(value = "dimanche_ferme", required = false) String dimancheFerme,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");
        
        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                // Supprimer les anciens horaires
                horaireSalonRepository.deleteBySalon(salon);

                // Créer les nouveaux horaires pour chaque jour
                createOrUpdateHoraire("Lundi", lundiOuverture, lundiFermeture, lundiFerme, salon);
                createOrUpdateHoraire("Mardi", mardiOuverture, mardiFermeture, mardiFerme, salon);
                createOrUpdateHoraire("Mercredi", mercrediOuverture, mercrediFermeture, mercrediFerme, salon);
                createOrUpdateHoraire("Jeudi", jeudiOuverture, jeudiFermeture, jeudiFerme, salon);
                createOrUpdateHoraire("Vendredi", vendrediOuverture, vendrediFermeture, vendrediFerme, salon);
                createOrUpdateHoraire("Samedi", samediOuverture, samediFermeture, samediFerme, salon);
                createOrUpdateHoraire("Dimanche", dimancheOuverture, dimancheFermeture, dimancheFerme, salon);

                return "redirect:/proprietaire/salon/" + id + "/gestion#profil";
            }
        }
        return "redirect:/auth/connexion";
    }

    private void createOrUpdateHoraire(String jour, String ouverture, String fermeture, String ferme, Salon salon) {
        boolean estFerme = "true".equals(ferme);
        LocalTime heureOuverture = (ouverture != null && !ouverture.isEmpty()) ? LocalTime.parse(ouverture) : LocalTime.of(8, 0);
        LocalTime heureFermeture = (fermeture != null && !fermeture.isEmpty()) ? LocalTime.parse(fermeture) : LocalTime.of(18, 0);
        
        if (!estFerme) {
            HoraireSalon horaire = new HoraireSalon(jour, heureOuverture, heureFermeture, true, salon);
            horaireSalonRepository.save(horaire);
        } else {
            HoraireSalon horaire = new HoraireSalon(jour, heureOuverture, heureFermeture, false, salon);
            horaireSalonRepository.save(horaire);
        }
    }

    // ========== CRUD PRESTATIONS ==========

    @PostMapping("/salon/{id}/prestation/ajouter")
    public String ajouterPrestation(@PathVariable("id") Long id,
                                     @RequestParam("nomPrestation") String nom,
                                     @RequestParam(value = "descriptionPrestation", required = false) String description,
                                     @RequestParam("categorie") String categorie,
                                     @RequestParam("dureeMinutes") Integer duree,
                                     @RequestParam("prix") Double prix,
                                     @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                List<Prestation> existantes = prestationRepository.findBySalon(salon);
                if (existantes.size() >= 50) {
                    return "redirect:/proprietaire/salon/" + id + "/gestion#prestation";
                }

                Prestation prestation = new Prestation();
                prestation.setNomPrestation(nom);
                prestation.setDescriptionPrestation(description != null ? description : "");
                prestation.setCategorie(categorie);
                prestation.setDureeMinutes(duree);
                prestation.setPrix(prix);
                prestation.setSalon(salon);
                prestation.setActif(true);
                prestation = prestationRepository.save(prestation);

                gererPhotosPrestation(prestation, photos, salon.getIdSalon());

                return "redirect:/proprietaire/salon/" + id + "/gestion#prestation";
            }
        }
        return "redirect:/auth/connexion";
    }

    @PostMapping("/salon/{id}/prestation/modifier")
    public String modifierPrestation(@PathVariable("id") Long idSalon,
                                     @RequestParam("idPrestation") Long idPrestation,
                                     @RequestParam("nomPrestation") String nom,
                                     @RequestParam(value = "descriptionPrestation", required = false) String description,
                                     @RequestParam("categorie") String categorie,
                                     @RequestParam("dureeMinutes") Integer duree,
                                     @RequestParam("prix") Double prix,
                                     @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(idSalon).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                Prestation prestation = prestationRepository.findById(idPrestation).orElse(null);
                if (prestation != null && prestation.getSalon().getIdSalon().equals(idSalon)) {
                    prestation.setNomPrestation(nom);
                    prestation.setDescriptionPrestation(description != null ? description : "");
                    prestation.setCategorie(categorie);
                    prestation.setDureeMinutes(duree);
                    prestation.setPrix(prix);
                    prestationRepository.save(prestation);

                    gererPhotosPrestation(prestation, photos, idSalon);

                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion#prestation";
                }
            }
        }
        return "redirect:/auth/connexion";
    }

    private void gererPhotosPrestation(Prestation prestation, MultipartFile[] photos, Long idSalon) {
        if (photos != null && photos.length > 0) {
            int currentPhotos = prestation.getMedias() != null ? prestation.getMedias().size() : 0;
            int photosToAdd = Math.min(photos.length, 5 - currentPhotos);
            for (int i = 0; i < photosToAdd; i++) {
                try {
                    MultipartFile photo = photos[i];
                    if (!photo.isEmpty()) {
                        String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                        Path filePath = Paths.get(UPLOAD_DIR + idSalon + "/prestations/" + fileName);
                        Files.createDirectories(filePath.getParent());
                        photo.transferTo(filePath.toFile());

                        MediaPrestation media = new MediaPrestation();
                        media.setUrlMedia(idSalon + "/prestations/" + fileName);
                        media.setTypeMedia("image");
                        media.setPrestation(prestation);
                        mediaPrestationRepository.save(media);
                    }
                } catch (IOException e) {
                    System.err.println("Erreur upload photo prestation: " + e.getMessage());
                }
            }
        }
    }

    @GetMapping("/salon/{id}/prestation/supprimer/{idPrestation}")
    public String supprimerPrestation(@PathVariable("id") Long idSalon,
                                       @PathVariable("idPrestation") Long idPrestation,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(idSalon).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                Prestation prestation = prestationRepository.findById(idPrestation).orElse(null);
                if (prestation != null && prestation.getSalon().getIdSalon().equals(idSalon)) {
                    // Supprimer les fichiers physiques des photos associés
                    if (prestation.getMedias() != null) {
                        for (MediaPrestation media : prestation.getMedias()) {
                            try {
                                Path filePath = Paths.get(UPLOAD_DIR + media.getUrlMedia());
                                Files.deleteIfExists(filePath);
                            } catch (IOException e) {}
                        }
                    }
                    prestationRepository.delete(prestation);
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion#prestation";
                }
            }
        }
        return "redirect:/auth/connexion";
    }

    @GetMapping("/salon/{id}/prestation/{idPrestation}/photo/supprimer/{idMedia}")
    public String supprimerPhotoPrestation(@PathVariable("id") Long idSalon,
                                            @PathVariable("idPrestation") Long idPrestation,
                                            @PathVariable("idMedia") Long idMedia,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(idSalon).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                MediaPrestation media = mediaPrestationRepository.findById(idMedia).orElse(null);
                if (media != null && media.getPrestation().getIdPrestation().equals(idPrestation) && media.getPrestation().getSalon().getIdSalon().equals(idSalon)) {
                    try {
                        Path filePath = Paths.get(UPLOAD_DIR + media.getUrlMedia());
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {}
                    mediaPrestationRepository.delete(media);
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion#prestation";
                }
            }
        }
        return "redirect:/auth/connexion";
    }

    // ========== CRUD SERVICES (similaire à prestations) ==========

    @PostMapping("/salon/{id}/service/ajouter")
    public String ajouterService(@PathVariable("id") Long id,
                                  @RequestParam("nomService") String nom,
                                  @RequestParam("descriptionService") String description,
                                  @RequestParam("dureeMinutes") Integer duree,
                                  @RequestParam("prix") Double prix,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                Prestation service = new Prestation();
                service.setNomPrestation(nom);
                service.setDescriptionPrestation(description);
                service.setCategorie("Service");
                service.setDureeMinutes(duree);
                service.setPrix(prix);
                service.setSalon(salon);
                service.setActif(true);
                prestationRepository.save(service);

                return "redirect:/proprietaire/salon/" + id + "/gestion#prestation";
            }
        }
        return "redirect:/auth/connexion";
    }

    // ========== RENDEZ-VOUS AVEC FILTRES ==========

    @GetMapping("/salon/{id}/rendez-vous")
    public String voirRendezVous(@PathVariable("id") Long id,
                                  @RequestParam(value = "filtre", defaultValue = "mois") String filtre,
                                  HttpSession session,
                                  Model model) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                List<atizay.model.RendezVous> rendezVous = rendezVousRepository.findBySalon(salon);
                model.addAttribute("proprietaire", proprietaire);
                model.addAttribute("salon", salon);
                model.addAttribute("rendezVous", rendezVous);
                model.addAttribute("filtre", filtre);
                return "proprietaire/salon/gestion-salon";
            }
        }
        return "redirect:/auth/connexion";
    }

    // ========== CRUD EMPLOYÉS ==========

    @PostMapping("/salon/{id}/employe/ajouter")
    public String ajouterEmploye(@PathVariable("id") Long id,
                                  @RequestParam("nom") String nom,
                                  @RequestParam("prenom") String prenom,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  @RequestParam("specialite") String specialite,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                // Vérifier l'unicité de l'email dans tous les types d'utilisateurs
                if (clientRepository.findByEmail(email) != null || 
                    proprietaireRepository.findByEmail(email) != null || 
                    employeRepository.findByEmail(email) != null) {
                    return "redirect:/proprietaire/salon/" + id + "/gestion#employes";
                }

                // Vérifier l'unicité du username (ceux-ci sont plus spécifiques, on vérifie au moins l'email de base)
                // Pour faire simple et robuste, on s'assure que l'email n'est pas déjà pris
                
                String baseUsername = email.split("@")[0];
                String username = baseUsername;
                int counter = 1;
                
                // Boucle pour trouver un username unique par essais successifs sur tous les dépôts
                while (clientRepository.findByUsername(username) != null || 
                       proprietaireRepository.findByUsername(username) != null || 
                       employeRepository.findByUsername(username) != null) {
                    username = baseUsername + counter;
                    counter++;
                }

                Employe employe = new Employe();
                employe.setNom(nom);
                employe.setPrenom(prenom);
                employe.setEmail(email);
                employe.setPassword(passwordEncoder.encode(password));
                employe.setSpecialite(specialite);
                employe.setSalon(salon);
                employe.setActifEmploye(true);
                employe.setDoitChangerMdp(true);
                employe.setUsername(username);
                employeRepository.save(employe);


                return "redirect:/proprietaire/salon/" + id + "/gestion#employes";
            }
        }
        return "redirect:/auth/connexion";
    }

    @GetMapping("/salon/{id}/employe/supprimer/{idEmploye}")
    public String supprimerEmploye(@PathVariable("id") Long idSalon,
                                    @PathVariable("idEmploye") Long idEmploye,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(idSalon).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                Employe employe = employeRepository.findById(idEmploye).orElse(null);
                if (employe != null && employe.getSalon().getIdSalon().equals(idSalon)) {
                    employeRepository.delete(employe);
                }
                return "redirect:/proprietaire/salon/" + idSalon + "/gestion#employes";
            }
        }
        return "redirect:/auth/connexion";
    }

    @PostMapping("/salon/{salonId}/rendezvous/{id}/confirmer")
    @Transactional
    public String confirmerRendezVousProprio(@PathVariable("id") Long id, @PathVariable("salonId") Long salonId, HttpSession session, RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");
        RendezVous rdv = rendezVousRepository.findById(id).orElse(null);

        if (rdv != null && rdv.getSalon().getProprietaire().getId().equals(proprietaire.getId())) {
            rdv.setStatut("Confirmé");
            rendezVousRepository.save(rdv);
        }
        return "redirect:/proprietaire/salon/" + salonId + "/gestion#rendez-vous";
    }

    @PostMapping("/salon/{salonId}/rendezvous/{id}/annuler")
    @Transactional
    public String annulerRendezVousProprio(@PathVariable("id") Long id, @PathVariable("salonId") Long salonId, HttpSession session, RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");
        RendezVous rdv = rendezVousRepository.findById(id).orElse(null);

        if (rdv != null && rdv.getSalon().getProprietaire().getId().equals(proprietaire.getId())) {
            rdv.setStatut("Annulé");
            rendezVousRepository.save(rdv);
        }
        return "redirect:/proprietaire/salon/" + salonId + "/gestion#rendez-vous";
    }
}
