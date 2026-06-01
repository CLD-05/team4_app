package com.example.daily.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.daily.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PreviewController {

    @GetMapping("/")
    public String index() {
        return "service";
    }

    @GetMapping("/diary")
    public String diary() {
        return "diary";
    }

    @GetMapping({"/id-return", "/id_return.html"})
    public String idReturn() {
        return "id_return";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main() {
        return "service";
    }

    @GetMapping({"/pw-reset", "/pw_reset.html"})
    public String pwReset() {
        return "pw_reset";
    }

    @GetMapping("/report")
    public String report() {
        return "report";
    }

    @GetMapping("/service")
    public String service() {
        return "service";
    }

    @GetMapping("/settings")
    public String settings() {
        return "settings";
    }

    @GetMapping({"/sign-up", "/sign_up.html"})
    public String signUp() {
        return "sign_up";
    }

    @GetMapping("/todolist")
    public String todolist() {
        return "todolist";
    }
}
