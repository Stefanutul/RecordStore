package com.example.RecordStore.service;


import com.example.RecordStore.dtos.ListingDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingService {

    Page<ListingDto.Response> getGlobalCatalog(Pageable pageable);

    Page<ListingDto.Response> getMyListings(String username, Pageable pageable);

    ListingDto.Response createDraft(Long recordId, String username);

    ListingDto.Response createListing(Long recordId, String username, ListingDto.CreateRequest req);

    ListingDto.Response reserve(Long listingId, String username);

    ListingDto.Response purchase(Long listingId, String username);

    ListingDto.Response update(Long listingId, String username, ListingDto.UpdateRequest req);

    void cancel(Long listingId, String username);
}
