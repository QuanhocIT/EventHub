package com.eventhub.services;

import com.eventhub.entities.Booking;
import com.eventhub.entities.Event;
import com.eventhub.entities.Ticket;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.TicketStatus;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.ExcelExportServiceImpl;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExcelExportServiceImpl excelExportService;

    private User sampleOrganizer;
    private User sampleAttendee;
    private Event sampleEvent;
    private Booking sampleBooking;
    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        sampleOrganizer = User.builder()
                .id(2L)
                .username("organizer_pro")
                .fullName("Tech Events Vietnam")
                .email("contact@techevents.vn")
                .build();

        sampleAttendee = User.builder()
                .id(10L)
                .username("customer_alice")
                .fullName("Alice Nguyen")
                .email("alice@example.com")
                .phoneNumber("0987654321")
                .build();

        sampleEvent = Event.builder()
                .id(1L)
                .title("Vietnam Tech Summit 2026")
                .location("National Convention Center, Hanoi")
                .startTime(LocalDateTime.now().plusDays(5))
                .organizer(sampleOrganizer)
                .build();

        sampleBooking = Booking.builder()
                .id(100L)
                .bookingCode("EVH-20261021-0001")
                .user(sampleAttendee)
                .event(sampleEvent)
                .status(BookingStatus.CONFIRMED)
                .subtotalAmount(BigDecimal.valueOf(1000000))
                .discountAmount(BigDecimal.valueOf(100000))
                .totalAmount(BigDecimal.valueOf(900000))
                .createdAt(LocalDateTime.now())
                .build();

        sampleTicket = Ticket.builder()
                .id(500L)
                .ticketCode("TK-20261021-AAAA")
                .booking(sampleBooking)
                .event(sampleEvent)
                .ticketType(TicketType.builder().name("VIP Pass").build())
                .status(TicketStatus.VALID)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Export event attendees generates valid readable Excel workbook")
    void exportEventAttendees_Success() throws IOException {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(ticketRepository.findByEventIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(sampleTicket));

        byte[] result = excelExportService.exportEventAttendees(1L, "organizer_pro", false);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Danh Sách Khách Tham Dự");
            assertThat(workbook.getSheetAt(0).getRow(3).getCell(1).getStringCellValue()).isEqualTo("Mã Vé");
            assertThat(workbook.getSheetAt(0).getRow(4).getCell(1).getStringCellValue()).isEqualTo("TK-20261021-AAAA");
            assertThat(workbook.getSheetAt(0).getRow(4).getCell(3).getStringCellValue()).isEqualTo("Alice Nguyen");
        }
    }

    @Test
    @DisplayName("Export event attendees throws Forbidden when caller is not the organizer and not admin")
    void exportEventAttendees_ThrowsForbidden() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> excelExportService.exportEventAttendees(1L, "hacker_user", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized to export attendee reports");
    }

    @Test
    @DisplayName("Export event attendees throws ResourceNotFound when event does not exist")
    void exportEventAttendees_ThrowsNotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> excelExportService.exportEventAttendees(999L, "organizer_pro", true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    @DisplayName("Export organizer revenue report generates valid Excel spreadsheet")
    void exportOrganizerRevenueReport_Success() throws IOException {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));
        when(bookingRepository.findByEventOrganizerIdOrderByCreatedAtDesc(2L)).thenReturn(List.of(sampleBooking));

        byte[] result = excelExportService.exportOrganizerRevenueReport(2L, LocalDateTime.now().getYear(), "organizer_pro", false);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            assertThat(workbook.getSheetAt(0).getRow(5).getCell(0).getStringCellValue()).isEqualTo("Tháng");
            assertThat(workbook.getSheetAt(0).getRow(5).getCell(4).getStringCellValue()).isEqualTo("Thực Thu (VND)");
        }
    }

    @Test
    @DisplayName("Export organizer revenue report throws Forbidden when caller is not organizer and not admin")
    void exportOrganizerRevenueReport_ThrowsForbidden() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleOrganizer));

        assertThatThrownBy(() -> excelExportService.exportOrganizerRevenueReport(2L, 2026, "other_user", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not authorized to export revenue reports");
    }
}
