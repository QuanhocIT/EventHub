package com.eventhub.services.impl;

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
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Override
    public OrganizerDashboardResponse getOrganizerDashboard(String username) {
        log.info("Fetching dashboard metrics for organizer username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Long organizerId = user.getId();
        long totalEvents = eventRepository.countByOrganizerId(organizerId);
        long publishedEvents = eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatus.PUBLISHED);
        BigDecimal totalRevenue = bookingRepository.sumTotalAmountByOrganizerIdAndStatus(organizerId, BookingStatus.CONFIRMED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalTicketsIssued = ticketRepository.countByEventOrganizerId(organizerId);
        long totalTicketsCheckedIn = ticketRepository.countByEventOrganizerIdAndStatus(organizerId, TicketStatus.USED);

        double checkInRate = totalTicketsIssued == 0 ? 0.0 :
                Math.round(((double) totalTicketsCheckedIn / totalTicketsIssued) * 10000.0) / 100.0;

        return OrganizerDashboardResponse.builder()
                .organizerId(organizerId)
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .totalRevenue(totalRevenue)
                .totalTicketsIssued(totalTicketsIssued)
                .totalTicketsCheckedIn(totalTicketsCheckedIn)
                .checkInRate(checkInRate)
                .build();
    }

    @Override
    public EventAnalyticsResponse getEventAnalytics(Long eventId, String username) {
        log.info("Fetching analytics for eventId: {}, requested by username: {}", eventId, username);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (username != null) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

            boolean isAdmin = user.getRole() == UserRole.ROLE_ADMIN;
            boolean isOwner = event.getOrganizer() != null && event.getOrganizer().getId().equals(user.getId());

            if (!isAdmin && !isOwner) {
                throw new ForbiddenException("You do not have permission to view analytics for this event");
            }
        }

        BigDecimal totalRevenue = bookingRepository.sumTotalAmountByEventIdAndStatus(eventId, BookingStatus.CONFIRMED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalTickets = ticketRepository.countByEventId(eventId);
        long checkedInTickets = ticketRepository.countByEventIdAndStatus(eventId, TicketStatus.USED);

        double checkInRate = totalTickets == 0 ? 0.0 :
                Math.round(((double) checkedInTickets / totalTickets) * 10000.0) / 100.0;

        return EventAnalyticsResponse.builder()
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .totalRevenue(totalRevenue)
                .totalTickets(totalTickets)
                .checkedInTickets(checkedInTickets)
                .checkInRate(checkInRate)
                .build();
    }

    @Override
    public AdminDashboardResponse getAdminDashboard() {
        log.info("Fetching admin system-wide dashboard metrics");

        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(UserRole.ROLE_CUSTOMER);
        long totalOrganizers = userRepository.countByRole(UserRole.ROLE_ORGANIZER);
        long totalStaff = userRepository.countByRole(UserRole.ROLE_STAFF);

        long totalEvents = eventRepository.count();
        long publishedEvents = eventRepository.countByStatus(EventStatus.PUBLISHED);

        BigDecimal totalRevenue = bookingRepository.sumTotalAmountByStatus(BookingStatus.CONFIRMED);
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long totalTickets = ticketRepository.count();

        return AdminDashboardResponse.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalOrganizers(totalOrganizers)
                .totalStaff(totalStaff)
                .totalEvents(totalEvents)
                .publishedEvents(publishedEvents)
                .totalRevenue(totalRevenue)
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .totalTickets(totalTickets)
                .build();
    }
}
