package com.example.RecordStore.service;

import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.model.*;
import com.example.RecordStore.model.Record;
import com.example.RecordStore.repository.ListingRepository;
import com.example.RecordStore.repository.RecordRepository;
import com.example.RecordStore.service.impl.ListingServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceImplTest {

    @Mock private ListingRepository listingRepository;
    @Mock private RecordRepository recordRepository;
    @Mock private AppUserService appUserService;
    @InjectMocks private ListingServiceImpl service;

    private AppUser seller;
    private AppUser buyer;
    private Record record;
    private Listing listing;

    @BeforeEach
    void setUp() throws Exception {
        seller = new AppUser("seller", "encoded", Set.of(Role.USER));
        setId(seller, 1L);

        buyer = new AppUser("buyer", "encoded", Set.of(Role.USER));
        setId(buyer, 2L);

        record = new Record();
        record.setId(1L);
        record.setTitle("Test Album");
        record.setArtist("Test Artist");
        record.setGenre("Rock");
        record.setPublishingYear(2024);
        record.setPrice(new BigDecimal("19.99"));

        listing = new Listing();
        listing.setId(1L);
        listing.setRecord(record);
        listing.setSellerId(1L);
        listing.setPrice(new BigDecimal("25.00"));
        listing.setCurrency("USD");
        listing.setStatus(ListingStatus.ACTIVE);
    }

    private void setId(AppUser user, Long id) throws Exception {
        Field idField = AppUser.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, id);
    }

    // --- createDraft ---

    @Test
    void createDraft_success() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> {
            Listing l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        ListingDto.Response result = service.createDraft(1L, "seller");

        assertThat(result.status()).isEqualTo(ListingStatus.DRAFT);
        assertThat(result.sellerId()).isEqualTo(1L);
    }

    @Test
    void createDraft_recordNotFound_throwsEntityNotFoundException() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(recordRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDraft(99L, "seller"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- createListing ---

    @Test
    void createListing_success() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(listingRepository.existsByRecord_IdAndStatusIn(eq(1L), any())).thenReturn(false);
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> {
            Listing l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        var req = new ListingDto.CreateRequest(new BigDecimal("30.00"), "usd");
        ListingDto.Response result = service.createListing(1L, "seller", req);

        assertThat(result.status()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(result.currency()).isEqualTo("USD");
    }

    @Test
    void createListing_alreadyListed_throwsIllegalStateException() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(recordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(listingRepository.existsByRecord_IdAndStatusIn(eq(1L), any())).thenReturn(true);

        var req = new ListingDto.CreateRequest(new BigDecimal("30.00"), "USD");

        assertThatThrownBy(() -> service.createListing(1L, "seller", req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already listed");
    }

    // --- reserve ---

    @Test
    void reserve_success() {
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        ListingDto.Response result = service.reserve(1L, "buyer");

        assertThat(result.status()).isEqualTo(ListingStatus.RESERVED);
        assertThat(result.reservedBy()).isEqualTo(2L);
    }

    @Test
    void reserve_notActive_throwsIllegalStateException() {
        listing.setStatus(ListingStatus.SOLD);
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.reserve(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not ACTIVE");
    }

    @Test
    void reserve_concurrentModification_throwsIllegalStateException() {
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any())).thenThrow(OptimisticLockingFailureException.class);

        assertThatThrownBy(() -> service.reserve(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("concurrently");
    }

    // --- purchase ---

    @Test
    void purchase_success() {
        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedBy(2L);
        listing.setReservedUntil(Instant.now().plus(Duration.ofMinutes(5)));

        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        ListingDto.Response result = service.purchase(1L, "buyer");

        assertThat(result.status()).isEqualTo(ListingStatus.SOLD);
        assertThat(result.buyerId()).isEqualTo(2L);
    }

    @Test
    void purchase_notReserved_throwsIllegalStateException() {
        listing.setStatus(ListingStatus.ACTIVE);
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.purchase(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("RESERVED");
    }

    @Test
    void purchase_wrongBuyer_throwsIllegalStateException() {
        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedBy(999L);
        listing.setReservedUntil(Instant.now().plus(Duration.ofMinutes(5)));

        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.purchase(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another buyer");
    }

    @Test
    void purchase_expiredReservation_revertsToActive() {
        listing.setStatus(ListingStatus.RESERVED);
        listing.setReservedBy(2L);
        listing.setReservedUntil(Instant.now().minus(Duration.ofMinutes(1)));

        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> service.purchase(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expired");

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.ACTIVE);
        assertThat(listing.getReservedBy()).isNull();
    }

    // --- update ---

    @Test
    void update_success() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new ListingDto.UpdateRequest(new BigDecimal("35.00"), "eur");
        ListingDto.Response result = service.update(1L, "seller", req);

        assertThat(result.price()).isEqualByComparingTo("35.00");
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void update_notOwner_throwsIllegalStateException() {
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        var req = new ListingDto.UpdateRequest(new BigDecimal("35.00"), null);

        assertThatThrownBy(() -> service.update(1L, "buyer", req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your listing");
    }

    @Test
    void update_soldListing_throwsIllegalStateException() {
        listing.setStatus(ListingStatus.SOLD);
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        var req = new ListingDto.UpdateRequest(new BigDecimal("35.00"), null);

        assertThatThrownBy(() -> service.update(1L, "seller", req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT or ACTIVE");
    }

    // --- cancel ---

    @Test
    void cancel_success() {
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        service.cancel(1L, "seller");

        assertThat(listing.getStatus()).isEqualTo(ListingStatus.CANCELLED);
        verify(listingRepository).save(listing);
    }

    @Test
    void cancel_notOwner_throwsIllegalStateException() {
        when(appUserService.getByUsername("buyer")).thenReturn(buyer);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.cancel(1L, "buyer"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your listing");
    }

    @Test
    void cancel_notActive_throwsIllegalStateException() {
        listing.setStatus(ListingStatus.RESERVED);
        when(appUserService.getByUsername("seller")).thenReturn(seller);
        when(listingRepository.findById(1L)).thenReturn(Optional.of(listing));

        assertThatThrownBy(() -> service.cancel(1L, "seller"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVE");
    }
}
