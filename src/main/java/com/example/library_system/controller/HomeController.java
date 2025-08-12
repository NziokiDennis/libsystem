package com.example.library_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Controller to handle root URL
@Controller
public class HomeController {
    // Maps root URL (/) to index.html
    @GetMapping("/")
    public String home() {
        return "index"; // Resolves to templates/index.html
    }
}