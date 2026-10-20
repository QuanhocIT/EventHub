package com.eventhub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotEmpty(message = "Booking items must not be empty")
    @Valid
    private List<BookingItemRequest> items;

    private String couponCode;
}
