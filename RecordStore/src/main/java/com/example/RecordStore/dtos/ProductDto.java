package com.example.RecordStore.dtos;

public class ProductDto {

    // Request DTO
    public record Request(
            String name,
            String description,
            Double price
    ) {}

    // Response DTO
    public record Response(
            Long id,
            String name,
            String description,
            Double price
    ) {}
}
