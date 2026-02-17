package com.example.RecordStore.dtos;

import java.util.List;

public record HomeResponse(
    String message,
    String username,
    List<String> roles
){}
