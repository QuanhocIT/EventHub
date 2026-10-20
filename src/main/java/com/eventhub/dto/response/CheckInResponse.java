package com.eventhub.dto.response;

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
public class CheckInResponse {

    private boolean success;
    private String message;
    private String ticketCode;
    private String attendeeName;
    private String eventTitle;
    private String seatInfo;
    private String ticketTypeName;
    private LocalDateTime checkInTime;
    private String staffUsername;
}
