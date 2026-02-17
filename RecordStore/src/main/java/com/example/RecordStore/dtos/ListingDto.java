package com.example.RecordStore.dtos;

import com.example.RecordStore.model.ListingStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ListingDto {

    public record CreateRequest(
            @NotNull @Positive BigDecimal price,
            @NotBlank @Size(min = 3, max = 3) String currency
    ) {}

    public record UpdateRequest(
            @Positive BigDecimal price,
            @Size(min = 3, max = 3) String currency
    ) {}

    public record Response(
            Long id,
            ListingStatus status,
            BigDecimal price,
            String currency,
            Instant createdAt,

            Long recordId,
            String title,
            String artist,
            String genre,
            int year,

            Long sellerId,
            Long reservedBy,
            Instant reservedUntil,
            Long buyerId
    ) {}
}
