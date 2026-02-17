package com.example.RecordStore.service;

import com.example.RecordStore.model.AppUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserService appUserService;

    public DbUserDetailsService(AppUserService appUserService) {
        this.appUserService = appUserService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser u;
        try {
            u = appUserService.getByUsername(username);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("User not found: " + username, ex);
        }

        return User.builder()
                .username(u.getUsername())
                .password(u.getPasswordHash())
                .authorities(
                        u.getRoles().stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                                .toList()
                )
                .build();
    }
}
