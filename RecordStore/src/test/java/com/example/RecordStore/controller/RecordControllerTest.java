package com.example.RecordStore.controller;

import com.example.RecordStore.config.SecurityConfig;
import com.example.RecordStore.dtos.RecordDto;
import com.example.RecordStore.security.JwtAuthenticationFilter;
import com.example.RecordStore.security.JwtProvider;
import com.example.RecordStore.service.RecordService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
@Import(SecurityConfig.class)
class RecordControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private RecordService recordService;
    @MockitoBean private JwtProvider jwtProvider;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "testuser")
    void getMyRecords_returnsRecords() throws Exception {
        var response = new RecordDto.Response(1L, "Test", "Artist", "Label", "Rock", 2024, new BigDecimal("19.99"));
        when(recordService.getAllRecords()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/my/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createRecord_validRequest_returns201() throws Exception {
        var request = new RecordDto.Request("Test", "Artist", "Label", "Rock", 2024, new BigDecimal("19.99"));
        var response = new RecordDto.Response(1L, "Test", "Artist", "Label", "Rock", 2024, new BigDecimal("19.99"));
        when(recordService.createRecord(any())).thenReturn(response);

        mockMvc.perform(post("/api/my/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createRecord_blankTitle_returns400() throws Exception {
        var request = new RecordDto.Request("", "Artist", null, "Rock", 2024, new BigDecimal("19.99"));

        mockMvc.perform(post("/api/my/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void createRecord_unauthenticated_returns401() throws Exception {
        var request = new RecordDto.Request("Test", "Artist", null, "Rock", 2024, new BigDecimal("19.99"));

        mockMvc.perform(post("/api/my/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
