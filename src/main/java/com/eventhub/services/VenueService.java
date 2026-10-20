package com.eventhub.services;

import com.eventhub.dto.request.CreateVenueRequest;
import com.eventhub.dto.response.VenueResponse;

import java.util.List;

public interface VenueService {

    VenueResponse createVenue(CreateVenueRequest request);

    List<VenueResponse> getAllVenues();

    VenueResponse getVenueById(Long id);

    void deleteVenue(Long id);
}
