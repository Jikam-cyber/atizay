package atizay.controller;

import atizay.model.Prestation;
import atizay.model.Proprietaire;
import atizay.model.Salon;
import atizay.model.Employe;
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
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");
        
        if ("proprietaire".equals(userType) && proprietaire != null) {
            model.addAttribute("proprietaire", proprietaire);
            model.addAttribute("salon", new Salon());
            return "proprietaire/creer-salon";
        }
        return "redirect:/auth/connexion";
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
                                   @RequestParam("heureOuverture") String heureOuverture,
                                   @RequestParam("heureFermeture") String heureFermeture,
                                   @RequestParam(value = "joursOuverture", required = false) String[] joursOuverture,
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
                return "redirect:/proprietaire/creer-salon";
            }
        }
        return "redirect:/auth/connexion";
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

                model.addAttribute("proprietaire", proprietaire);
                model.addAttribute("salon", salon);
                System.out.println("Salon chargé: " + salon.getNomSalon() + " avec " + medias.size() + " photos");
                return "proprietaire/salon/gestion-salon";
            }
        }
        return "redirect:/auth/connexion";
    }

    @GetMapping("/salons/creer")
    public String creerSalonForm(HttpSession session, Model model) {
        String type = (String) session.getAttribute("type");
        if ("proprietaire".equals(type)) {
            model.addAttribute("salon", new Salon());
            return "proprietaire/creer-salon";
        }
        return "redirect:/auth/connexion";
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

    @Transactional
    @PostMapping("/salon/{identifier}/supprimer")
    public String supprimerSalon(@PathVariable("identifier") String identifier,
                               @RequestParam("confirmName") String confirmName,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("proprietaire");
        Salon salon = findSalonByIdentifier(identifier, proprietaire);

        if (salon != null) {
            if (salon.getNomSalon().equals(confirmName)) {
                salonRepository.delete(salon);
                redirectAttributes.addFlashAttribute("successMessage", "Le salon '" + confirmName + "' a été supprimé definitivement.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Le nom saisi ne correspond pas. La suppression a été annulée.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Salon non trouvé ou accès refusé.");
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
                    redirectAttributes.addFlashAttribute("successMessage", "Photo supprimée avec succès !");
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion";
                }
            }
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Accès non autorisé");
        return "redirect:/auth/connexion";
    }

    // ========== GESTION DES HORAIRES ==========

    @PostMapping("/salon/{id}/horaire/modifier")
    public String modifierHoraireSalon(@PathVariable("id") Long id,
                                       @RequestParam("heureOuverture") String heureOuverture,
                                       @RequestParam("heureFermeture") String heureFermeture,
                                       @RequestParam(value = "joursOuverture", required = false) String[] joursOuverture,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                // Supprimer les anciens horaires
                horaireSalonRepository.deleteBySalon(salon);

                // Créer les nouveaux horaires
                String[] tousJours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
                LocalTime ouverture = LocalTime.parse(heureOuverture);
                LocalTime fermeture = LocalTime.parse(heureFermeture);

                for (String jour : tousJours) {
                    boolean estOuvert = joursOuverture != null && java.util.Arrays.asList(joursOuverture).contains(jour);
                    HoraireSalon horaire = new HoraireSalon(jour, ouverture, fermeture, estOuvert, salon);
                    horaireSalonRepository.save(horaire);
                }

                redirectAttributes.addFlashAttribute("successMessage", "Horaires mis à jour avec succès !");
                return "redirect:/proprietaire/salon/" + id + "/gestion";
            }
        }
        return "redirect:/auth/connexion";
    }

    // ========== CRUD PRESTATIONS ==========

    @PostMapping("/salon/{id}/prestation/ajouter")
    public String ajouterPrestation(@PathVariable("id") Long id,
                                     @RequestParam("nomPrestation") String nom,
                                     @RequestParam("descriptionPrestation") String description,
                                     @RequestParam("categorie") String categorie,
                                     @RequestParam("dureeMinutes") Integer duree,
                                     @RequestParam("prix") Double prix,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Proprietaire proprietaire = (Proprietaire) session.getAttribute("user");

        if ("proprietaire".equals(userType) && proprietaire != null) {
            Salon salon = salonRepository.findById(id).orElse(null);
            if (salon != null && salon.getProprietaire().getId().equals(proprietaire.getId())) {
                Prestation prestation = new Prestation();
                prestation.setNomPrestation(nom);
                prestation.setDescriptionPrestation(description);
                prestation.setCategorie(categorie);
                prestation.setDureeMinutes(duree);
                prestation.setPrix(prix);
                prestation.setSalon(salon);
                prestation.setActif(true);
                prestationRepository.save(prestation);

                redirectAttributes.addFlashAttribute("successMessage", "Prestation ajoutée avec succès !");
                return "redirect:/proprietaire/salon/" + id + "/gestion";
            }
        }
        return "redirect:/auth/connexion";
    }

    @PostMapping("/salon/{id}/prestation/modifier")
    public String modifierPrestation(@PathVariable("id") Long idSalon,
                                     @RequestParam("idPrestation") Long idPrestation,
                                     @RequestParam("nomPrestation") String nom,
                                     @RequestParam("descriptionPrestation") String description,
                                     @RequestParam("categorie") String categorie,
                                     @RequestParam("dureeMinutes") Integer duree,
                                     @RequestParam("prix") Double prix,
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
                    prestation.setDescriptionPrestation(description);
                    prestation.setCategorie(categorie);
                    prestation.setDureeMinutes(duree);
                    prestation.setPrix(prix);
                    prestationRepository.save(prestation);

                    redirectAttributes.addFlashAttribute("successMessage", "Prestation modifiée avec succès !");
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion";
                }
            }
        }
        return "redirect:/auth/connexion";
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
                    prestationRepository.delete(prestation);
                    redirectAttributes.addFlashAttribute("successMessage", "Prestation supprimée avec succès !");
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion";
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

                redirectAttributes.addFlashAttribute("successMessage", "Service ajouté avec succès !");
                return "redirect:/proprietaire/salon/" + id + "/gestion";
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
                Employe employe = new Employe();
                employe.setNom(nom);
                employe.setPrenom(prenom);
                employe.setEmail(email);
                employe.setPassword(password);
                employe.setSpecialite(specialite);
                employe.setSalon(salon);
                employe.setActifEmploye(true);
                employeRepository.save(employe);

                redirectAttributes.addFlashAttribute("successMessage", "Employé ajouté avec succès !");
                return "redirect:/proprietaire/salon/" + id + "/gestion";
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
                    redirectAttributes.addFlashAttribute("successMessage", "Employé supprimé avec succès !");
                    return "redirect:/proprietaire/salon/" + idSalon + "/gestion";
                }
            }
        }
        return "redirect:/auth/connexion";
    }
}
