package atizay.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "Une ou plusieurs photos dépassent la taille maximale autorisée (5 Mo).");
        // Get the referer to redirect back to the exact section
        String referer = request.getHeader("Referer");
        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/proprietaire/dashboard";
    }
}
