package com.example.RecordStore.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity()
@Table(name = "soldListing")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SoldListing {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id ;

    @OneToOne(optional = false , fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id" , nullable = false , unique = true)
    private Listing listing;

    public SoldListing( Listing listing) {
        this.listing = listing;
    }

}
