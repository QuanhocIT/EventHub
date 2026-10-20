package com.eventhub.dto.response;

import com.eventhub.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long id;
    private String ticketCode;
    private String bookingCode;
    private Long eventId;
    private String eventTitle;
    private String attendeeName;
    private String ticketTypeName;
    private String seatInfo;
    private TicketStatus status;
    private String qrCodeBase64;
    private LocalDateTime checkedInAt;
    private String checkedInByStaff;
    private LocalDateTime createdAt;
}
