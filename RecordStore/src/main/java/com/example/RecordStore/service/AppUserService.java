package com.example.RecordStore.service;

import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Role;


import java.util.List;
import java.util.Optional;


public interface AppUserService {

    AppUser register(String username , String rawPassword , Role... roles);

    AppUser getByUsername(String username);


    AppUser getById(Long id);

    List<AppUser> getAll();

    AppUser addRole(String username, Role role);

    AppUser addUser(AppUser user);

    }
