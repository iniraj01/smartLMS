package com.smartlms.controller;

import com.smartlms.dto.RegisterRequest;
import com.smartlms.service.UserService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("registerRequest",
                           new RegisterRequest());

        return "register";
    }

    // Handle registration form
    @PostMapping("/register")
    public String registerStudent(
            @Valid @ModelAttribute("registerRequest")
            RegisterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {

            userService.registerStudent(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhone()
            );

            return "redirect:/login?registered";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            return "register";
        }
    }
    @GetMapping("/login-success")
    public String loginSuccess() {
        return "login-success";
    }
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
}