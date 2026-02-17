package com.example.RecordStore.service;

import com.example.RecordStore.model.AppUser;
import com.example.RecordStore.model.Role;

import java.util.List;

public interface AppUserService {

    AppUser register(String username , String rawPassword , Role... roles);

    AppUser getByUsername(String username);

    List<AppUser> getAll();

    AppUser addRole(String username, Role role);
}
