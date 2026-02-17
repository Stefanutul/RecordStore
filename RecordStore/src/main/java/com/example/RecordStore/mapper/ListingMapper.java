package com.example.RecordStore.mapper;

import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.model.Listing;
import com.example.RecordStore.model.Record;
import org.springframework.stereotype.Component;

@Component
public class ListingMapper {

    public ListingDto.Response toDto(Listing listing) {
        if (listing == null) {
            return null;
        }

        Record r = listing.getRecord();

        return new ListingDto.Response(
                listing.getId(),
                listing.getStatus(),
                listing.getPrice(),
                listing.getCurrency(),
                listing.getCreatedAt(),

                r != null ? r.getId() : null,
                r != null ? r.getTitle() : null,
                r != null ? r.getArtist() : null,
                r != null ? r.getGenre() : null,
                r != null ? r.getPublishingYear() : null,

                listing.getSellerId(),
                listing.getReservedBy(),
                listing.getReservedUntil(),
                listing.getBuyerId()
        );
    }
}
