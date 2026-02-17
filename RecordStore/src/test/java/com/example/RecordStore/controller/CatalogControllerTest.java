package com.example.RecordStore.controller;

import com.example.RecordStore.config.SecurityConfig;
import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.model.ListingStatus;
import com.example.RecordStore.security.JwtAuthenticationFilter;
import com.example.RecordStore.security.JwtProvider;
import com.example.RecordStore.service.ListingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
@Import(SecurityConfig.class)
class CatalogControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ListingService listingService;
    @MockitoBean private JwtProvider jwtProvider;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ListingDto.Response sampleResponse() {
        return new ListingDto.Response(1L, ListingStatus.ACTIVE, new BigDecimal("25.00"), "USD",
                Instant.now(), 1L, "Test Album", "Artist", "Rock", 2024, 1L, null, null, null);
    }

    @Test
    @WithMockUser(username = "buyer1")
    void browse_returnsPage() throws Exception {
        when(listingService.getGlobalCatalog(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Album"));
    }

    @Test
    @WithMockUser(username = "buyer1")
    void reserve_success_returns200() throws Exception {
        var reserved = new ListingDto.Response(1L, ListingStatus.RESERVED, new BigDecimal("25.00"), "USD",
                Instant.now(), 1L, "Test Album", "Artist", "Rock", 2024, 1L, 2L, Instant.now(), null);
        when(listingService.reserve(eq(1L), eq("buyer1"))).thenReturn(reserved);

        mockMvc.perform(post("/api/catalog/1/reserve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVED"));
    }

    @Test
    @WithMockUser(username = "buyer1")
    void reserve_notActive_returns409() throws Exception {
        when(listingService.reserve(eq(1L), eq("buyer1")))
                .thenThrow(new IllegalStateException("Listing is not ACTIVE."));

        mockMvc.perform(post("/api/catalog/1/reserve"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Listing is not ACTIVE."));
    }
}
