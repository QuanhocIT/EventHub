package com.eventhub.controllers;

import com.eventhub.config.CorsConfig;
import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.enums.EventStatus;
import com.eventhub.security.CustomUserDetailsService;
import com.eventhub.security.JwtAuthenticationEntryPoint;
import com.eventhub.security.JwtAuthenticationFilter;
import com.eventhub.security.JwtService;
import com.eventhub.security.SecurityConfig;
import com.eventhub.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, CorsConfig.class, JwtAuthenticationEntryPoint.class, JwtAuthenticationFilter.class})
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/events/{id} is public and returns 200 OK")
    void getEventById_PublicAccess_Returns200() throws Exception {
        EventResponse response = EventResponse.builder()
                .id(1L)
                .title("Tech Summit 2026")
                .location("Hanoi")
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventService.getEventById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Tech Summit 2026"));
    }

    @Test
    @DisplayName("POST /api/v1/events without token returns 401 Unauthorized")
    void createEvent_Unauthenticated_Returns401() throws Exception {
        CreateEventRequest request = CreateEventRequest.builder()
                .title("Test Event")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .capacity(100)
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/events with authenticated user returns 201 Created")
    void createEvent_Authenticated_Returns201() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .title("AI Summit")
                .description("Cutting edge AI discussions")
                .location("Online")
                .startTime(now.plusDays(5))
                .endTime(now.plusDays(6))
                .capacity(200)
                .price(BigDecimal.TEN)
                .status(EventStatus.PUBLISHED)
                .build();

        EventResponse response = EventResponse.builder()
                .id(10L)
                .title("AI Summit")
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any(String.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("AI Summit"));
    }

    @Test
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @DisplayName("POST /api/v1/events with CUSTOMER role returns 403 Forbidden")
    void createEvent_CustomerRole_Returns403() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .title("Unauthorized Event")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .capacity(100)
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(username = "organizer", roles = {"ORGANIZER"})
    @DisplayName("POST /api/v1/events with ORGANIZER role returns 201 Created")
    void createEvent_OrganizerRole_Returns201() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        CreateEventRequest request = CreateEventRequest.builder()
                .title("Organizer Event")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .capacity(100)
                .build();

        EventResponse response = EventResponse.builder()
                .id(20L)
                .title("Organizer Event")
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any(String.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(20));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/events with invalid payload returns 400 Bad Request")
    void createEvent_InvalidPayload_Returns400() throws Exception {
        // Missing required fields (blank title, null start/end time, null capacity)
        CreateEventRequest invalidRequest = CreateEventRequest.builder()
                .title("")
                .build();

        mockMvc.perform(post("/api/v1/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors.title").exists());
    }
}
