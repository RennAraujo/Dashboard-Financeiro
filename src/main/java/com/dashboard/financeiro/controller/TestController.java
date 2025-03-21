package com.dashboard.financeiro.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {
    
    @GetMapping("/public")
    public String publicAccess() {
        return "Conteúdo público.";
    }
    
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ANALYST') or hasRole('ADMIN')")
    public String userAccess() {
        return "Conteúdo para USUÁRIOS.";
    }
    
    @GetMapping("/analyst")
    @PreAuthorize("hasRole('ANALYST') or hasRole('ADMIN')")
    public String analystAccess() {
        return "Conteúdo para ANALISTAS.";
    }
    
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Conteúdo para ADMINISTRADORES.";
    }
}
