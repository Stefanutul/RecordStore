package com.example.RecordStore.service.impl;


import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.model.Listing;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.model.ListingStatus;
import com.example.RecordStore.repository.ListingRepository;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.service.AppUserService;
import com.example.RecordStore.service.ListingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingServiceImpl implements ListingService {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(10);

    private final ListingRepository listingRepository;
    private final RecordRepository recordRepository;
    private final AppUserService appUserService;

    private Long resolveUserId(String username) {
        return appUserService.getByUsername(username).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingDto.Response> getGlobalCatalog(Pageable pageable) {
        return listingRepository.findByStatus(ListingStatus.ACTIVE, pageable)
                .map(ListingServiceImpl::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListingDto.Response> getMyListings(String username, Pageable pageable) {
        Long sellerId = resolveUserId(username);
        return listingRepository.findBySellerId(sellerId, pageable)
                .map(ListingServiceImpl::toDto);
    }

    @Override
    @Transactional
    public ListingDto.Response createDraft(Long recordId, String username) {
        Long sellerId = resolveUserId(username);
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Record not found: " + recordId));

        Listing listing = new Listing();
        listing.setRecord(record);
        listing.setSellerId(sellerId);
        listing.setStatus(ListingStatus.DRAFT);

        return toDto(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public ListingDto.Response createListing(Long recordId, String username, ListingDto.CreateRequest req) {
        Long sellerId = resolveUserId(username);
        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new EntityNotFoundException("Record not found: " + recordId));

        boolean alreadyListed = listingRepository.existsByRecord_IdAndStatusIn(
                recordId, List.of(ListingStatus.ACTIVE, ListingStatus.RESERVED)
        );
        if (alreadyListed) {
            throw new IllegalStateException("Record already listed (ACTIVE/RESERVED).");
        }

        Listing listing = new Listing();
        listing.setRecord(record);
        listing.setSellerId(sellerId);
        listing.setPrice(req.price());
        listing.setCurrency(req.currency().toUpperCase());
        listing.setStatus(ListingStatus.ACTIVE);

        return toDto(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public ListingDto.Response reserve(Long listingId, String username) {
        Long buyerId = resolveUserId(username);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + listingId));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Listing is not ACTIVE.");
        }

        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedBy(buyerId);
        listing.setReservedUntil(Instant.now().plus(RESERVATION_TTL));

        try {
            return toDto(listingRepository.save(listing));
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Listing updated concurrently. Try again.");
        }
    }

    @Override
    @Transactional
    public ListingDto.Response purchase(Long listingId, String username) {
        Long buyerId = resolveUserId(username);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + listingId));

        if (listing.getStatus() != ListingStatus.RESERVED) {
            throw new IllegalStateException("Listing must be RESERVED to purchase.");
        }
        if (listing.getReservedBy() == null || !listing.getReservedBy().equals(buyerId)) {
            throw new IllegalStateException("Listing is reserved by another buyer.");
        }
        if (listing.getReservedUntil() != null && listing.getReservedUntil().isBefore(Instant.now())) {
            listing.setStatus(ListingStatus.ACTIVE);
            listing.setReservedBy(null);
            listing.setReservedUntil(null);
            listingRepository.save(listing);
            throw new IllegalStateException("Reservation expired. Listing is ACTIVE again.");
        }

        listing.setStatus(ListingStatus.SOLD);
        listing.setBuyerId(buyerId);

        return toDto(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public ListingDto.Response update(Long listingId, String username, ListingDto.UpdateRequest req) {
        Long sellerId = resolveUserId(username);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("Not your listing.");
        }
        if (listing.getStatus() != ListingStatus.DRAFT && listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Only DRAFT or ACTIVE listings can be updated.");
        }

        if (req.price() != null) {
            listing.setPrice(req.price());
        }
        if (req.currency() != null) {
            listing.setCurrency(req.currency().toUpperCase());
        }

        return toDto(listingRepository.save(listing));
    }

    @Override
    @Transactional
    public void cancel(Long listingId, String username) {
        Long sellerId = resolveUserId(username);
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + listingId));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new IllegalStateException("Not your listing.");
        }
        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE listings can be cancelled.");
        }

        listing.setStatus(ListingStatus.CANCELLED);
        listingRepository.save(listing);
    }

    private static ListingDto.Response toDto(Listing l) {
        Record r = l.getRecord();
        return new ListingDto.Response(
                l.getId(),
                l.getStatus(),
                l.getPrice(),
                l.getCurrency(),
                l.getCreatedAt(),

                r.getId(),
                r.getTitle(),
                r.getArtist(),
                r.getGenre(),
                r.getPublishingYear(),

                l.getSellerId(),
                l.getReservedBy(),
                l.getReservedUntil(),
                l.getBuyerId()
        );
    }
}
