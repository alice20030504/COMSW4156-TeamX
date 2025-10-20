package com.teamx.fitness.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for handling the root URL and providing navigation.
 */
@Controller
public class HomeController {

    /**
     * Redirect root URL to Swagger UI for API documentation.
     *
     * @return redirect to Swagger UI
     */
    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/swagger-ui.html");
    }
}
