package com.example.RecordStore.controller;


import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.repository.AppUserRepository;
import com.example.RecordStore.service.AppUserService;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class PracticeController {

    public final AppUserService userService;


    @GetMapping(value = "/api/users")
    public List<AppUser> getAllUsers(){
       return userService.getAll();
    }


    @GetMapping(value = "/api/users/{id}")
    public Optional<AppUser> getUserById(@RequestParam Long id){
        return Optional.of(userService.getById(id));
    }



}
