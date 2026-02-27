package com.example.RecordStore.controller;

import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my/listings")
public class ListingsController {

    private final ListingService listingService;

    @PostMapping("/for-record/{recordId}/draft")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingDto.Response createDraftListing(@PathVariable Long recordId) {
        return listingService.createDraft(recordId, currentUsername());
    }

    @PostMapping("/for-record/{recordId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ListingDto.Response createListing(@PathVariable Long recordId,
                                             @Valid @RequestBody ListingDto.CreateRequest req) {
        return listingService.createListing(recordId, currentUsername(), req);
    }

    @PatchMapping("/{listingId}")
    public ListingDto.Response updateListing(@PathVariable Long listingId,
                                             @Valid @RequestBody ListingDto.UpdateRequest req) {
        return listingService.update(listingId, currentUsername(), req);

    }

    @GetMapping
    public Page<ListingDto.Response> myListings(Pageable pageable) {
        return listingService.getMyListings(currentUsername(), pageable);
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long listingId) {
        listingService.cancel(listingId, currentUsername());
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
