package com.example.RecordStore.controller;


import com.example.RecordStore.dtos.HomeResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    @GetMapping
    public HomeResponse home() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String displayRole = roles.stream()
                .map(r -> r.replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");

        String message = "Hello " + displayRole + " " + username;

        return new HomeResponse(message, username, roles);
    }


}
