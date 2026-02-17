package com.example.RecordStore.controller;


import com.example.RecordStore.service.ListingService;
import com.example.RecordStore.dtos.ListingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog")
public class CatalogController {

    private final ListingService listingService;

    @GetMapping
    public Page<ListingDto.Response> browse(Pageable pageable) {
        return listingService.getGlobalCatalog(pageable);
    }

    @PostMapping("/{listingId}/reserve")
    public ListingDto.Response reserve(@PathVariable Long listingId) {
        return listingService.reserve(listingId, currentUsername());
    }

    @PostMapping("/{listingId}/purchase")
    public ListingDto.Response purchase(@PathVariable Long listingId) {
        return listingService.purchase(listingId, currentUsername());
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
