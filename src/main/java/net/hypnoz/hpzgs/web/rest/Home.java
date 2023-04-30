package net.hypnoz.hpzgs.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
public class Home {

    @GetMapping
    public String home(){
        return "home";
    }
}
