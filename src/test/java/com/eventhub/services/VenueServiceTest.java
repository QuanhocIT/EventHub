package com.eventhub.services;

import com.eventhub.dto.request.CreateVenueRequest;
import com.eventhub.dto.response.VenueResponse;
import com.eventhub.entities.Venue;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.SeatRepository;
import com.eventhub.repositories.VenueRepository;
import com.eventhub.services.impl.VenueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private VenueServiceImpl venueService;

    private CreateVenueRequest createVenueRequest;
    private Venue sampleVenue;

    @BeforeEach
    void setUp() {
        createVenueRequest = CreateVenueRequest.builder()
                .name("Grand Theater")
                .address("123 Le Loi, District 1")
                .city("Ho Chi Minh")
                .capacity(100)
                .numberOfRows(3) // rows A, B, C
                .seatsPerRow(10) // 10 seats per row = 30 seats total
                .vipRowsCount(1) // row A is VIP
                .build();

        sampleVenue = Venue.builder()
                .id(1L)
                .name("Grand Theater")
                .address("123 Le Loi, District 1")
                .city("Ho Chi Minh")
                .capacity(100)
                .build();
    }

    @Test
    @DisplayName("Should create venue successfully and auto-generate seats")
    void createVenue_Success() {
        when(venueRepository.existsByName("Grand Theater")).thenReturn(false);
        when(venueRepository.save(any(Venue.class))).thenReturn(sampleVenue);

        VenueResponse response = venueService.createVenue(createVenueRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Grand Theater");
        assertThat(response.getTotalSeats()).isEqualTo(30);

        verify(venueRepository).save(any(Venue.class));
        verify(seatRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw BadRequestException when venue name already exists")
    void createVenue_DuplicateName_ThrowsBadRequestException() {
        when(venueRepository.existsByName("Grand Theater")).thenReturn(true);

        assertThatThrownBy(() -> venueService.createVenue(createVenueRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");

        verify(venueRepository, never()).save(any());
        verify(seatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Should return venue by ID when venue exists")
    void getVenueById_Success() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(sampleVenue));
        when(seatRepository.countByVenueId(1L)).thenReturn(30L);

        VenueResponse response = venueService.getVenueById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTotalSeats()).isEqualTo(30);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when venue does not exist")
    void getVenueById_NotFound_ThrowsResourceNotFoundException() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.getVenueById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue not found with id: '999'");
    }

    @Test
    @DisplayName("Should delete venue when venue exists")
    void deleteVenue_Success() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(sampleVenue));

        venueService.deleteVenue(1L);

        verify(venueRepository).delete(sampleVenue);
    }
}
