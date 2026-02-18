package com.example.RecordStore.service.impl;

import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Role;
import com.example.RecordStore.repository.AppUserRepository;
import com.example.RecordStore.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AppUser register(String username, String rawPassword, Role... roles) {
        if (repository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        Set<Role> roleSet = (roles == null || roles.length == 0)
                ? Set.of(Role.USER)
                : Arrays.stream(roles).collect(Collectors.toSet());

        AppUser user = new AppUser(
                username,
                passwordEncoder.encode(rawPassword),
                roleSet
        );

        return repository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }


    @Override
    @Transactional(readOnly = true)
    public List<AppUser> getAll() {
        return repository.findAll();
    }

    @Override
    public AppUser addRole(String username, Role role) {
        AppUser user = getByUsername(username);
        user.getRoles().add(role); // roles is a mutable Set, allowed
        return repository.save(user);
    }

   @Transactional(readOnly = true)
   @Override
   public <Optional>AppUser getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }
}
