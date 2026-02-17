package com.example.RecordStore.dtos;

import jakarta.validation.constraints.*;

public class TrackDto {

    public record Request(
            @NotBlank @Size(max = 255) String title,
            @NotNull @Positive Integer durationSeconds,
            @Size(max = 10) String trackKey
    ) {}

    public record Response(Long id, String title, Integer durationSeconds, String trackKey) {}
}
