package eu.europa.esig.dss.web.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    //RequestMaping login
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
