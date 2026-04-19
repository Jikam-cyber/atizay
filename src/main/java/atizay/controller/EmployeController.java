package atizay.controller;

import atizay.model.Employe;
import atizay.model.RendezVous;
import atizay.model.HoraireEmploye;
import atizay.model.HoraireSalon;
import atizay.repository.EmployeRepository;
import atizay.repository.RendezVousRepository;
import atizay.repository.HoraireEmployeRepository;
import atizay.repository.HoraireSalonRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/employe")
public class EmployeController {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private HoraireEmployeRepository horaireEmployeRepository;

    @Autowired
    private HoraireSalonRepository horaireSalonRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/employes/";

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
            @RequestParam(value = "section", required = false) String section) {
        String userType = (String) session.getAttribute("userType");
        Employe employe = (Employe) session.getAttribute("user");

        if (!"employe".equals(userType) || employe == null) {
            return "redirect:/auth/connexion";
        }

        // Recharger l'employé pour avoir les données à jour
        employe = employeRepository.findById(employe.getId()).orElse(employe);

        // Charger les rendez-vous personnels de l'employé
        List<RendezVous> personalAppointments = rendezVousRepository.findByEmploye(employe);

        // Charger tous les rendez-vous du salon
        List<RendezVous> salonAppointments = new java.util.ArrayList<>();
        List<atizay.model.HoraireSalon> salonHours = new java.util.ArrayList<>();
        
        if (employe.getSalon() != null) {
            salonHours = horaireSalonRepository.findBySalonOrderByJourSemaine(employe.getSalon());
            salonAppointments = rendezVousRepository.findBySalon(employe.getSalon());
        }

        model.addAttribute("employe", employe);

        model.addAttribute("section", section);
        model.addAttribute("personalAppointments", personalAppointments);
        model.addAttribute("salonAppointments", salonAppointments);
        model.addAttribute("salonHours", salonHours);

        return "employe/dashboard-employe";
    }

    @PostMapping("/profil/modifier")
    public String modifierProfil(@ModelAttribute("employe") Employe employeForm,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Employe employeSession = (Employe) session.getAttribute("user");

        if (!"employe".equals(userType) || employeSession == null) {
            return "redirect:/auth/connexion";
        }

        Employe employe = employeRepository.findById(employeSession.getId()).orElse(null);
        if (employe != null) {
            employe.setNom(employeForm.getNom());
            employe.setPrenom(employeForm.getPrenom());
            employe.setSpecialite(employeForm.getSpecialite());
            employe.setBiographie(employeForm.getBiographie());
            employe.setTelephone(employeForm.getTelephone());
            employe.setVille(employeForm.getVille());
            employe.setAdresse(employeForm.getAdresse());

            // Gestion de la photo de profil
            if (photo != null && !photo.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                    Path path = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    Files.copy(photo.getInputStream(), path.resolve(fileName));

                    employe.setPhotoProfil(fileName);
                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement de la photo.");
                }
            }

            employeRepository.save(employe);
            session.setAttribute("user", employe);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        }

        return "redirect:/employe/dashboard?section=profil";
    }

    @PostMapping("/changer-mot-de-passe")
    public String changerMotDePasse(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Employe employe = (Employe) session.getAttribute("user");
        if (employe == null) {
            return "redirect:/auth/connexion";
        }

        Employe employeDB = employeRepository.findById(employe.getId()).orElse(null);
        if (employeDB == null) {
            redirectAttributes.addFlashAttribute("error", "Employé non trouvé");
            return "redirect:/employe/dashboard?section=profil";
        }

        if (!employeDB.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe actuel est incorrect");
            return "redirect:/employe/dashboard?section=profil";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
            return "redirect:/employe/dashboard?section=profil";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères");
            return "redirect:/employe/dashboard?section=profil";
        }

        employeDB.setPassword(newPassword);
        employeDB.setDoitChangerMdp(false);
        employeRepository.save(employeDB);
        session.setAttribute("user", employeDB);

        redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès !");
        return "redirect:/employe/dashboard?section=profil";
    }

    @GetMapping("/photo/supprimer")
    public String supprimerPhoto(HttpSession session, RedirectAttributes redirectAttributes) {
        String userType = (String) session.getAttribute("userType");
        Employe employeSession = (Employe) session.getAttribute("user");

        if (!"employe".equals(userType) || employeSession == null) {
            return "redirect:/auth/connexion";
        }

        Employe employe = employeRepository.findById(employeSession.getId()).orElse(null);
        if (employe != null && employe.getPhotoProfil() != null) {
            try {
                // Supprimer le fichier physique
                Path filePath = Paths.get(UPLOAD_DIR + employe.getPhotoProfil());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }

                // Supprimer le nom de la photo dans la base de données
                employe.setPhotoProfil(null);
                employeRepository.save(employe);
                session.setAttribute("user", employe);

                redirectAttributes.addFlashAttribute("success", "Photo de profil supprimée avec succès.");
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de la photo.");
            }
        }

        return "redirect:/employe/dashboard?section=profil";
    }

    @GetMapping("/rendezvous/{id}/confirmer")
    public String confirmerRendezVous(@PathVariable("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Employe employe = (Employe) session.getAttribute("user");
        if (employe == null)
            return "redirect:/auth/connexion";

        RendezVous rdv = rendezVousRepository.findById(id).orElse(null);
        if (rdv != null) {
            if (rdv.getEmploye() != null && rdv.getEmploye().getId().equals(employe.getId())) {
                rdv.setStatut("Confirmé");
                rendezVousRepository.save(rdv);
            }
        }
        return "redirect:/employe/dashboard?section=rendezvous";
    }


    @GetMapping("/rendezvous/{id}/annuler")
    public String annulerRendezVous(@PathVariable("id") Long id, HttpSession session,
            RedirectAttributes redirectAttributes) {
        Employe employe = (Employe) session.getAttribute("user");
        if (employe == null)
            return "redirect:/auth/connexion";

        RendezVous rdv = rendezVousRepository.findById(id).orElse(null);
        if (rdv != null) {
            if (rdv.getEmploye() != null && rdv.getEmploye().getId().equals(employe.getId())) {
                rdv.setStatut("Annulé");
                rendezVousRepository.save(rdv);
            }
        }
        return "redirect:/employe/dashboard?section=rendezvous";
    }

}
