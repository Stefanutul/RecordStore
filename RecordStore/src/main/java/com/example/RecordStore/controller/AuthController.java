package com.example.RecordStore.controller;

import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.security.JwtProvider;
import com.example.RecordStore.service.AppUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AppUserService appUserService;

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 40) String username,
            @NotBlank @Size(min = 6) String password
    ) {}

    public record AuthResponse(String token) {}

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String token = jwtProvider.generateToken(auth.getName(), roles);
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        AppUser user = appUserService.register(req.username(), req.password());

        List<String> roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.name())
                .toList();

        String token = jwtProvider.generateToken(user.getUsername(), roles);
        return new AuthResponse(token);
    }
}
