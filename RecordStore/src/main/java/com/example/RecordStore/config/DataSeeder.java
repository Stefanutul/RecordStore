package com.example.RecordStore.config;

import com.example.RecordStore.model.Role;
import com.example.RecordStore.repository.AppUserRepository;
import com.example.RecordStore.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers() {
        return args -> seed();
    }

    @Transactional
    public void seed() {
        upsertUser("adrian", "user123", Role.USER);
        upsertUser("admin", "admin123", Role.ADMIN);
    }

    private void upsertUser(String username, String rawPassword, Role... roles) {
        if (appUserRepository.existsByUsername(username)) {
            // update password hash (no delete => no FK issues)
            appUserRepository.updatePasswordHashByUsername(username, passwordEncoder.encode(rawPassword));

            // ensure roles exist (safe: roles is a mutable Set)
            for (Role r : roles) {
                appUserService.addRole(username, r);
            }
        } else {
            appUserService.register(username, rawPassword, roles);
        }
    }
}
