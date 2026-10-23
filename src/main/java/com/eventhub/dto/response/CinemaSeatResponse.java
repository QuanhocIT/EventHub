package com.eventhub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaSeatResponse {
    private String seatCode; // e.g. "A1", "F7", "J1-J2" (Sweetbox)
    private String row;      // "A", "B", ... "J"
    private Integer number;  // 1, 2, ...
    private String zone;     // "STANDARD", "VIP", "SWEETBOX"
    private Double price;
    private String status;   // "AVAILABLE", "HELD", "SOLD"
    private Boolean isPair;  // true for sweetbox couple seats
    private String pairWith; // e.g. "J2" paired with "J1"
}
