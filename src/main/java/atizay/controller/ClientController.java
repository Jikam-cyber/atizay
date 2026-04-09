package atizay.controller;

import atizay.model.Client;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/client")
public class ClientController {

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String type = (String) session.getAttribute("type");

        if ("client".equals(type)) {
            Client client = (Client) session.getAttribute("client");
            model.addAttribute("user", client);
            model.addAttribute("type", "client");
            return "client/dashboard-client";
        }

        return "redirect:/auth/connexion";
    }
}
