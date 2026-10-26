package com.eventhub.services;

import com.eventhub.dto.response.AdminDashboardResponse;
import com.eventhub.dto.response.EventAnalyticsResponse;
import com.eventhub.dto.response.OrganizerDashboardResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.TicketStatus;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User organizerUser;
    private User adminUser;
    private User otherUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        organizerUser = User.builder()
                .id(1L)
                .username("organizer1")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin1")
                .role(UserRole.ROLE_ADMIN)
                .build();

        otherUser = User.builder()
                .id(3L)
                .username("organizer2")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        testEvent = Event.builder()
                .id(100L)
                .title("Tech Conference 2026")
                .organizer(organizerUser)
                .status(EventStatus.PUBLISHED)
                .build();
    }

    @Test
    @DisplayName("getOrganizerDashboard - Success with correct check-in rate calculation")
    void getOrganizerDashboard_Success() {
        when(userRepository.findByUsername("organizer1")).thenReturn(Optional.of(organizerUser));
        when(eventRepository.countByOrganizerId(1L)).thenReturn(5L);
        when(eventRepository.countByOrganizerIdAndStatus(1L, EventStatus.PUBLISHED)).thenReturn(3L);
        when(bookingRepository.sumTotalAmountByOrganizerIdAndStatus(1L, BookingStatus.CONFIRMED))
                .thenReturn(BigDecimal.valueOf(50000000));
        when(ticketRepository.countByEventOrganizerId(1L)).thenReturn(200L);
        when(ticketRepository.countByEventOrganizerIdAndStatus(1L, TicketStatus.USED)).thenReturn(150L);

        OrganizerDashboardResponse response = dashboardService.getOrganizerDashboard("organizer1");

        assertThat(response).isNotNull();
        assertThat(response.getOrganizerId()).isEqualTo(1L);
        assertThat(response.getTotalEvents()).isEqualTo(5L);
        assertThat(response.getPublishedEvents()).isEqualTo(3L);
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(50000000));
        assertThat(response.getTotalTicketsIssued()).isEqualTo(200L);
        assertThat(response.getTotalTicketsCheckedIn()).isEqualTo(150L);
        assertThat(response.getCheckInRate()).isEqualTo(75.0); // 150 / 200 = 75%
    }

    @Test
    @DisplayName("getOrganizerDashboard - Zero tickets issued returns 0.0 check-in rate")
    void getOrganizerDashboard_ZeroTickets() {
        when(userRepository.findByUsername("organizer1")).thenReturn(Optional.of(organizerUser));
        when(eventRepository.countByOrganizerId(1L)).thenReturn(1L);
        when(eventRepository.countByOrganizerIdAndStatus(1L, EventStatus.PUBLISHED)).thenReturn(0L);
        when(bookingRepository.sumTotalAmountByOrganizerIdAndStatus(1L, BookingStatus.CONFIRMED))
                .thenReturn(BigDecimal.ZERO);
        when(ticketRepository.countByEventOrganizerId(1L)).thenReturn(0L);
        when(ticketRepository.countByEventOrganizerIdAndStatus(1L, TicketStatus.USED)).thenReturn(0L);

        OrganizerDashboardResponse response = dashboardService.getOrganizerDashboard("organizer1");

        assertThat(response.getCheckInRate()).isEqualTo(0.0);
        assertThat(response.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getEventAnalytics - Success when accessed by owner")
    void getEventAnalytics_Success_AsOwner() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("organizer1")).thenReturn(Optional.of(organizerUser));
        when(bookingRepository.sumTotalAmountByEventIdAndStatus(100L, BookingStatus.CONFIRMED))
                .thenReturn(BigDecimal.valueOf(25000000));
        when(ticketRepository.countByEventId(100L)).thenReturn(100L);
        when(ticketRepository.countByEventIdAndStatus(100L, TicketStatus.USED)).thenReturn(80L);

        EventAnalyticsResponse response = dashboardService.getEventAnalytics(100L, "organizer1");

        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo(100L);
        assertThat(response.getEventTitle()).isEqualTo("Tech Conference 2026");
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(25000000));
        assertThat(response.getTotalTickets()).isEqualTo(100L);
        assertThat(response.getCheckedInTickets()).isEqualTo(80L);
        assertThat(response.getCheckInRate()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("getEventAnalytics - Success when accessed by admin")
    void getEventAnalytics_Success_AsAdmin() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("admin1")).thenReturn(Optional.of(adminUser));
        when(bookingRepository.sumTotalAmountByEventIdAndStatus(100L, BookingStatus.CONFIRMED))
                .thenReturn(BigDecimal.valueOf(10000000));
        when(ticketRepository.countByEventId(100L)).thenReturn(50L);
        when(ticketRepository.countByEventIdAndStatus(100L, TicketStatus.USED)).thenReturn(25L);

        EventAnalyticsResponse response = dashboardService.getEventAnalytics(100L, "admin1");

        assertThat(response).isNotNull();
        assertThat(response.getCheckInRate()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("getEventAnalytics - Throws ForbiddenException when accessed by another organizer")
    void getEventAnalytics_Forbidden_WhenNotOwner() {
        when(eventRepository.findById(100L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findByUsername("organizer2")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> dashboardService.getEventAnalytics(100L, "organizer2"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("You do not have permission");
    }

    @Test
    @DisplayName("getAdminDashboard - Aggregates platform statistics accurately")
    void getAdminDashboard_Success() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByRole(UserRole.ROLE_CUSTOMER)).thenReturn(80L);
        when(userRepository.countByRole(UserRole.ROLE_ORGANIZER)).thenReturn(15L);
        when(userRepository.countByRole(UserRole.ROLE_STAFF)).thenReturn(5L);

        when(eventRepository.count()).thenReturn(20L);
        when(eventRepository.countByStatus(EventStatus.PUBLISHED)).thenReturn(12L);

        when(bookingRepository.sumTotalAmountByStatus(BookingStatus.CONFIRMED))
                .thenReturn(BigDecimal.valueOf(120000000));
        when(bookingRepository.count()).thenReturn(350L);
        when(bookingRepository.countByStatus(BookingStatus.CONFIRMED)).thenReturn(300L);
        when(ticketRepository.count()).thenReturn(850L);

        AdminDashboardResponse response = dashboardService.getAdminDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getTotalUsers()).isEqualTo(100L);
        assertThat(response.getTotalCustomers()).isEqualTo(80L);
        assertThat(response.getTotalOrganizers()).isEqualTo(15L);
        assertThat(response.getTotalStaff()).isEqualTo(5L);
        assertThat(response.getTotalEvents()).isEqualTo(20L);
        assertThat(response.getPublishedEvents()).isEqualTo(12L);
        assertThat(response.getTotalRevenue()).isEqualByComparingTo(BigDecimal.valueOf(120000000));
        assertThat(response.getTotalBookings()).isEqualTo(350L);
        assertThat(response.getConfirmedBookings()).isEqualTo(300L);
        assertThat(response.getTotalTickets()).isEqualTo(850L);
    }
}
