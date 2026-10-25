package com.eventhub.services.impl;

import com.eventhub.dto.request.ConfigureVenueLayoutRequest;
import com.eventhub.dto.request.CreateVenueRequest;
import com.eventhub.dto.response.VenueResponse;
import com.eventhub.entities.Seat;
import com.eventhub.entities.Venue;
import com.eventhub.enums.SeatZone;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.SeatRepository;
import com.eventhub.repositories.VenueRepository;
import com.eventhub.services.VenueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public VenueResponse createVenue(CreateVenueRequest request) {
        log.info("Creating venue with name: {}", request.getName());

        if (venueRepository.existsByName(request.getName())) {
            throw new BadRequestException("Venue with name '" + request.getName() + "' already exists");
        }

        Venue venue = Venue.builder()
                .name(request.getName())
                .address(request.getAddress())
                .city(request.getCity())
                .capacity(request.getCapacity())
                .build();

        Venue savedVenue = venueRepository.save(venue);

        // Auto-generate seats if configured
        int rows = request.getNumberOfRows() != null ? request.getNumberOfRows() : 0;
        int seatsPerRow = request.getSeatsPerRow() != null ? request.getSeatsPerRow() : 0;
        int vipRows = request.getVipRowsCount() != null ? request.getVipRowsCount() : 0;

        List<Seat> generatedSeats = new ArrayList<>();
        if (rows > 0 && seatsPerRow > 0) {
            log.info("Auto-generating {} rows x {} seats for venue: {}", rows, seatsPerRow, savedVenue.getName());
            for (int r = 0; r < rows; r++) {
                String rowLabel = String.valueOf((char) ('A' + r));
                SeatZone zone = (r < vipRows) ? SeatZone.VIP : SeatZone.STANDARD;

                for (int s = 1; s <= seatsPerRow; s++) {
                    Seat seat = Seat.builder()
                            .venue(savedVenue)
                            .seatRow(rowLabel)
                            .seatNumber(s)
                            .zone(zone)
                            .build();
                    generatedSeats.add(seat);
                }
            }
            seatRepository.saveAll(generatedSeats);
        }

        return mapToResponse(savedVenue, generatedSeats.size());
    }

    @Override
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(v -> {
                    long totalSeats = seatRepository.countByVenueId(v.getId());
                    return mapToResponse(v, (int) totalSeats);
                })
                .collect(Collectors.toList());
    }

    @Override
    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
        long totalSeats = seatRepository.countByVenueId(venue.getId());
        return mapToResponse(venue, (int) totalSeats);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));
        venueRepository.delete(venue);
        log.info("Venue deleted with ID: {}", id);
    }

    @Override
    @Transactional
    public VenueResponse configureVenueLayout(Long id, ConfigureVenueLayoutRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        log.info("Configuring custom visual seating layout for venue ID: {}, total seats: {}", id, request.getSeats().size());

        List<Seat> existing = seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(id);
        if (!existing.isEmpty()) {
            seatRepository.deleteAll(existing);
            seatRepository.flush();
        }

        List<Seat> newSeats = request.getSeats().stream()
                .map(item -> Seat.builder()
                        .venue(venue)
                        .seatRow(item.getSeatRow().trim().toUpperCase())
                        .seatNumber(item.getSeatNumber())
                        .zone(item.getZone() != null ? item.getZone() : SeatZone.STANDARD)
                        .build())
                .collect(Collectors.toList());

        seatRepository.saveAll(newSeats);
        venue.setCapacity(newSeats.size());
        Venue updated = venueRepository.save(venue);

        log.info("Successfully updated venue ID: {} layout with {} seats", id, newSeats.size());
        return mapToResponse(updated, newSeats.size());
    }

    private VenueResponse mapToResponse(Venue venue, int totalSeats) {
        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .capacity(venue.getCapacity())
                .totalSeats(totalSeats)
                .createdAt(venue.getCreatedAt())
                .build();
    }
}
