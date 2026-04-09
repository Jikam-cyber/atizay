package atizay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Page d'accueil
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
    
    /**
     * Page d'accueil alternative
     */
    @GetMapping("/home")
    public String homePage() {
        return "redirect:/auth/connexion";
    }
}
