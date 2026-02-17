package com.example.RecordStore.repository;

import com.example.RecordStore.model.Listing;
import com.example.RecordStore.model.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    Page<Listing> findBySellerId(Long sellerId, Pageable pageable);

    boolean existsByRecord_IdAndStatusIn(Long recordId, Iterable<ListingStatus> statuses);
}
