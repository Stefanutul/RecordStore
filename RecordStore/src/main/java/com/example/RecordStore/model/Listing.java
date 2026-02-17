package com.example.RecordStore.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listings", indexes = {
        @Index(name = "idx_listing_status", columnList = "status"),
        @Index(name = "idx_listing_record_id", columnList = "record_id")
})
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The underlying owned record being sold
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false)
    private Record record;

    // For now: no User entity yet
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;




    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    // allow null while DRAFT
    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ListingStatus status = ListingStatus.DRAFT;

    // Reservation fields
    @Column(name = "reserved_by")
    private Long reservedBy;

    @Column(name = "reserved_until")
    private Instant reservedUntil;

    @Column(name = "buyer_id")
    private Long buyerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // optimistic locking (helps with concurrent reserve/purchase)
    @Version
    private Long version;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}