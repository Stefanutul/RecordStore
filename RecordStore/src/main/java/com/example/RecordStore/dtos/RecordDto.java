package com.example.RecordStore.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RecordDto {

    public record Request(
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 255) String artist,
            @Size(max = 255) String label,
            @NotBlank @Size(max = 100) String genre,
            @Min(1900) @Max(2100) int publishingYear,
            @NotNull @Positive BigDecimal price
    ) {}

    public record Response(
            Long id,
            String title,
            String artist,
            String label,
            String genre,
            int publishingYear,
            BigDecimal price
    ) {}
}
