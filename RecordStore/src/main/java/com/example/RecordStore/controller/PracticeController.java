package com.example.RecordStore.controller;


import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.service.AppUserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/users")
public class PracticeController {

    public final AppUserService userService;

    public PracticeController(AppUserService userService){
        this.userService = userService;
    }


    @GetMapping()
    public List<AppUser> getAllUsers(){
       return userService.getAll();
    }

    @GetMapping(value = "/{id}")
    public AppUser getUserById(@PathVariable Long id){
        return userService.getById(id);
    }

    @PostMapping
    public AppUser createUser(@RequestBody AppUser user) {
        return userService.addUser(user);
    }


}
