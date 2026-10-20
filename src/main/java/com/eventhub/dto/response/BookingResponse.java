package com.eventhub.dto.response;

import com.eventhub.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private Long eventId;
    private String eventTitle;
    private String username;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime expiresAt;
    private List<BookingItemResponse> items;
    private LocalDateTime createdAt;
}
