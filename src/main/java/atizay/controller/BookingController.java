package atizay.controller;

import atizay.model.Employe;
import atizay.model.Prestation;
import atizay.model.Salon;
import atizay.repository.EmployeRepository;
import atizay.repository.PrestationRepository;
import atizay.repository.SalonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private PrestationRepository prestationRepository;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private SalonRepository salonRepository;

    @GetMapping("/booking/choisir-date")
    public String choisirDate(@RequestParam("prestation") Long idPrestation,
            @RequestParam(value = "employe", required = false) String employeIdStr,
            Model model) {

        Prestation prestation = prestationRepository.findById(idPrestation).orElse(null);
        if (prestation == null) {
            return "redirect:/";
        }

        model.addAttribute("prestation", prestation);
        model.addAttribute("salon", prestation.getSalon());

        Employe employe = null;
        if (employeIdStr != null && !employeIdStr.equals("any")) {
            try {
                Long empId = Long.parseLong(employeIdStr);
                employe = employeRepository.findById(empId).orElse(null);
            } catch (Exception e) {
            }
        }

        model.addAttribute("employe", employe);

        // Generate next 14 days for the UI selector
        List<LocalDate> dates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            dates.add(today.plusDays(i));
        }
        model.addAttribute("dates", dates);

        return "client/booking-date";
    }

    @org.springframework.web.bind.annotation.PostMapping("/booking/confirm")
    public String confirmBooking(@RequestParam("prestationId") Long prestationId,
            @RequestParam(value = "employeId", required = false) String employeIdStr,
            @RequestParam("date") String date,
            @RequestParam("time") String time,
            jakarta.servlet.http.HttpSession session,
            Model model) {

        String type = (String) session.getAttribute("userType");
        if (!"client".equals(type)) {
            // Require login before booking
            return "redirect:/auth/connexion";
        }

        Prestation prestation = prestationRepository.findById(prestationId).orElse(null);
        if (prestation == null)
            return "redirect:/";

        Employe employe = null;
        if (employeIdStr != null && !employeIdStr.isEmpty() && !employeIdStr.equals("any")) {
            try {
                employe = employeRepository.findById(Long.parseLong(employeIdStr)).orElse(null);
            } catch (Exception e) {
            }
        }

        model.addAttribute("prestation", prestation);
        model.addAttribute("employe", employe);
        model.addAttribute("salon", prestation.getSalon());
        model.addAttribute("date", date);
        model.addAttribute("time", time);

        // Here we pass everything to a confirmation summary view
        return "client/booking-confirm";
    }

    @Autowired
    private atizay.repository.RendezVousRepository rendezVousRepository;

    @org.springframework.web.bind.annotation.PostMapping("/booking/finalize")
    public String finalizeBooking(@RequestParam("prestationId") Long prestationId,
            @RequestParam(value = "employeId", required = false) Long employeId,
            @RequestParam("date") String date,
            @RequestParam("time") String time,
            jakarta.servlet.http.HttpSession session) {

        String type = (String) session.getAttribute("userType");
        if (!"client".equals(type)) {
            return "redirect:/auth/connexion";
        }

        atizay.model.Client client = (atizay.model.Client) session.getAttribute("user");
        Prestation prestation = prestationRepository.findById(prestationId).orElse(null);
        if (prestation == null)
            return "redirect:/";

        Employe employe = null;
        if (employeId != null) {
            employe = employeRepository.findById(employeId).orElse(null);
        } else if (!prestation.getSalon().getListeEmployes().isEmpty()) {
            employe = prestation.getSalon().getListeEmployes().get(0);
        }

        java.time.LocalDateTime dateTime = java.time.LocalDateTime
                .parse(date + "T" + (time.length() == 5 ? time + ":00" : time));
        java.time.LocalDateTime endTime = dateTime.plusMinutes(prestation.getDureeMinutes());

        atizay.model.RendezVous rv = new atizay.model.RendezVous();
        rv.setDateRendezVous(dateTime);
        rv.setHeureDebut(dateTime);
        rv.setHeureFin(endTime);
        rv.setStatut("Confirmé");
        rv.setPrixTotal(prestation.getPrix());
        rv.setDureeMinutes(prestation.getDureeMinutes());
        rv.setClient(client);
        rv.setEmploye(employe);
        rv.setSalon(prestation.getSalon());
        rv.setPrestation(prestation);

        rendezVousRepository.save(rv);

        return "redirect:/client/dashboard";
    }
}
