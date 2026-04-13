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
                            @RequestParam(value = "section", required = false, defaultValue = "profil") String section) {
        String userType = (String) session.getAttribute("userType");
        Employe employe = (Employe) session.getAttribute("user");
        
        if (!"employe".equals(userType) || employe == null) {
            return "redirect:/auth/connexion";
        }

        // Recharger l'employé pour avoir les données à jour
        employe = employeRepository.findById(employe.getId()).orElse(employe);
        
        // Liste des rendez-vous personnels
        List<RendezVous> personalAppointments = rendezVousRepository.findByEmploye(employe);
        
        // Liste des rendez-vous du salon (si l'employé y appartient)
        List<RendezVous> salonAppointments = null;
        List<HoraireSalon> salonHours = null;
        if (employe.getSalon() != null) {
            salonAppointments = rendezVousRepository.findBySalon(employe.getSalon());
            salonHours = horaireSalonRepository.findBySalonOrderByJourSemaine(employe.getSalon());
        }
        
        // Planning de l'employé
        List<HoraireEmploye> employeePlanning = horaireEmployeRepository.findByEmploye(employe);
        
        model.addAttribute("employe", employe);
        model.addAttribute("section", section);
        model.addAttribute("personalAppointments", personalAppointments);
        model.addAttribute("salonAppointments", salonAppointments);
        model.addAttribute("salonHours", salonHours);
        model.addAttribute("employeePlanning", employeePlanning);
        model.addAttribute("rdvCount", personalAppointments.size());
        
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

    @GetMapping("/changer-mot-de-passe")
    public String afficherChangerMotDePasse(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("user");
        if (employe == null) {
            return "redirect:/auth/connexion";
        }
        model.addAttribute("employe", employe);
        return "employe/changer-mot-de-passe";
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
            return "redirect:/employe/changer-mot-de-passe";
        }

        if (!employeDB.getPassword().equals(currentPassword)) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe actuel est incorrect");
            return "redirect:/employe/changer-mot-de-passe";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
            return "redirect:/employe/changer-mot-de-passe";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Le mot de passe doit contenir au moins 6 caractères");
            return "redirect:/employe/changer-mot-de-passe";
        }

        employeDB.setPassword(newPassword);
        employeDB.setDoitChangerMdp(false);
        employeRepository.save(employeDB);
        session.setAttribute("user", employeDB);

        redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès !");
        return "redirect:/employe/dashboard?section=profil";
    }
}

