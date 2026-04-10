package atizay.controller;

import atizay.model.Employe;
import atizay.model.RendezVous;
import atizay.repository.EmployeRepository;
import atizay.repository.RendezVousRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/employe")
public class EmployeController {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        Employe employe = (Employe) session.getAttribute("user");
        
        if (!"employe".equals(userType) || employe == null) {
            return "redirect:/auth/connexion";
        }

        // Recharger l'employé pour avoir les données à jour
        employe = employeRepository.findById(employe.getId()).orElse(employe);
        
        List<RendezVous> appointments = rendezVousRepository.findByEmploye(employe);
        
        model.addAttribute("employe", employe);
        model.addAttribute("rdvCount", appointments.size());
        model.addAttribute("clientsCount", 0);
        model.addAttribute("prestationsCount", 0);
        model.addAttribute("todayAppointments", appointments);
        
        return "employe/dashboard-employe";
    }

    @GetMapping("/profil")
    public String profil(HttpSession session, Model model) {
        Employe employe = (Employe) session.getAttribute("employe");
        if (employe == null) {
            return "redirect:/auth/connexion";
        }

        model.addAttribute("employe", employe);
        model.addAttribute("section", "profil");
        return "employe/dashboard";
    }

    @PostMapping("/profil/modifier")
    public String modifierProfil(@ModelAttribute("employe") Employe employeForm, 
                                 @RequestParam(value = "nouveauPassword", required = false) String nouveauPassword,
                                 HttpSession session) {
        Employe employeSession = (Employe) session.getAttribute("employe");
        if (employeSession == null) {
            return "redirect:/auth/connexion";
        }

        Employe employe = employeRepository.findById(employeSession.getId()).orElse(null);
        if (employe != null) {
            employe.setNom(employeForm.getNom());
            employe.setPrenom(employeForm.getPrenom());
            employe.setSpecialite(employeForm.getSpecialite());
            employe.setBiographie(employeForm.getBiographie());
            
            if (nouveauPassword != null && !nouveauPassword.isEmpty()) {
                employe.setPassword(nouveauPassword); // En test, pas de hashage (NoOp)
            }
            
            employeRepository.save(employe);
            session.setAttribute("employe", employe);
        }

        return "redirect:/employe/profil?success=true";
    }
}
