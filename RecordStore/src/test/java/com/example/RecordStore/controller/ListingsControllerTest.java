package com.example.RecordStore.controller;

import com.example.RecordStore.config.SecurityConfig;
import com.example.RecordStore.dtos.ListingDto;
import com.example.RecordStore.model.ListingStatus;
import com.example.RecordStore.security.JwtAuthenticationFilter;
import com.example.RecordStore.security.JwtProvider;
import com.example.RecordStore.service.ListingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ListingsController.class)
@Import(SecurityConfig.class)
class ListingsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private ListingService listingService;
    @MockitoBean private JwtProvider jwtProvider;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ListingDto.Response sampleResponse() {
        return new ListingDto.Response(1L, ListingStatus.ACTIVE, new BigDecimal("25.00"), "USD",
                Instant.now(), 1L, "Test Album", "Artist", "Rock", 2024, 1L, null, null, null);
    }

    @Test
    @WithMockUser(username = "seller1")
    void createListing_validRequest_returns201() throws Exception {
        var req = new ListingDto.CreateRequest(new BigDecimal("25.00"), "USD");
        when(listingService.createListing(eq(1L), eq("seller1"), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/my/listings/for-record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "seller1")
    void createListing_invalidPrice_returns400() throws Exception {
        var req = new ListingDto.CreateRequest(new BigDecimal("-5.00"), "USD");

        mockMvc.perform(post("/api/my/listings/for-record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    @WithMockUser(username = "seller1")
    void cancelListing_serviceThrowsIllegalState_returns409() throws Exception {
        doThrow(new IllegalStateException("Only ACTIVE listings can be cancelled"))
                .when(listingService).cancel(1L, "seller1");

        mockMvc.perform(delete("/api/my/listings/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void createListing_unauthenticated_returns401() throws Exception {
        var req = new ListingDto.CreateRequest(new BigDecimal("25.00"), "USD");

        mockMvc.perform(post("/api/my/listings/for-record/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }
}
