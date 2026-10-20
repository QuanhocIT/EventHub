package com.eventhub.services;

import com.eventhub.dto.request.CreateEventRequest;
import com.eventhub.dto.request.UpdateEventRequest;
import com.eventhub.dto.response.EventResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.User;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.UserRole;
import com.eventhub.events.EventCreatedEvent;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.mappers.EventMapper;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EventServiceImpl eventService;

    private CreateEventRequest validCreateRequest;
    private User sampleUser;
    private Event sampleEvent;
    private EventResponse sampleResponse;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        sampleUser = User.builder()
                .id(1L)
                .username("john_doe")
                .fullName("John Doe")
                .email("john@example.com")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        validCreateRequest = CreateEventRequest.builder()
                .title("Tech Summit 2026")
                .description("Annual Technology Conference")
                .location("Hanoi, Vietnam")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .capacity(500)
                .price(BigDecimal.valueOf(50))
                .status(EventStatus.PUBLISHED)
                .build();

        sampleEvent = Event.builder()
                .id(1L)
                .title("Tech Summit 2026")
                .description("Annual Technology Conference")
                .location("Hanoi, Vietnam")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .capacity(500)
                .price(BigDecimal.valueOf(50))
                .status(EventStatus.PUBLISHED)
                .organizer(sampleUser)
                .build();

        sampleResponse = EventResponse.builder()
                .id(1L)
                .title("Tech Summit 2026")
                .status(EventStatus.PUBLISHED)
                .organizerId(1L)
                .organizerName("John Doe")
                .build();
    }

    @Test
    @DisplayName("Should create event successfully and publish domain event")
    void createEvent_Success() {
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(eventMapper.toEntity(validCreateRequest)).thenReturn(sampleEvent);
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);
        when(eventMapper.toResponse(sampleEvent)).thenReturn(sampleResponse);

        EventResponse response = eventService.createEvent(validCreateRequest, "john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Tech Summit 2026");
        assertThat(response.getOrganizerId()).isEqualTo(1L);

        verify(userRepository).findByUsername("john_doe");
        verify(eventRepository).save(sampleEvent);
        verify(eventPublisher).publishEvent(any(EventCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when endTime is before startTime")
    void createEvent_EndTimeBeforeStartTime_ThrowsBadRequestException() {
        LocalDateTime now = LocalDateTime.now();
        CreateEventRequest invalidRequest = CreateEventRequest.builder()
                .title("Invalid Event")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(1)) // end before start
                .capacity(100)
                .build();

        assertThatThrownBy(() -> eventService.createEvent(invalidRequest, "john_doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Event end time cannot be before start time");

        verify(eventRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when organizer user does not exist")
    void createEvent_OrganizerNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(validCreateRequest, "nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username: 'nonexistent'");

        verify(eventRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should return EventResponse when event exists by ID")
    void getEventById_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(eventMapper.toResponse(sampleEvent)).thenReturn(sampleResponse);

        EventResponse response = eventService.getEventById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(eventRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event does not exist")
    void getEventById_NotFound_ThrowsResourceNotFoundException() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Event not found with id: '999'");
    }

    @Test
    @DisplayName("Should delete event successfully when current user is owner")
    void deleteEvent_Owner_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        eventService.deleteEvent(1L, "john_doe", false);

        verify(eventRepository).delete(sampleEvent);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when deleting event of another organizer")
    void deleteEvent_NonOwner_ThrowsAccessDeniedException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> eventService.deleteEvent(1L, "other_user", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to delete this event");

        verify(eventRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should allow admin to delete any event even if not owner")
    void deleteEvent_Admin_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        eventService.deleteEvent(1L, "admin_user", true);

        verify(eventRepository).delete(sampleEvent);
    }

    @Test
    @DisplayName("Should update event successfully when current user is owner")
    void updateEvent_Owner_Success() {
        LocalDateTime now = LocalDateTime.now();
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .location("Danang, Vietnam")
                .startTime(now.plusDays(3))
                .endTime(now.plusDays(4))
                .capacity(600)
                .price(BigDecimal.valueOf(75))
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));
        when(eventRepository.save(sampleEvent)).thenReturn(sampleEvent);
        when(eventMapper.toResponse(sampleEvent)).thenReturn(sampleResponse);

        EventResponse response = eventService.updateEvent(1L, updateRequest, "john_doe", false);

        assertThat(response).isNotNull();
        verify(eventMapper).updateEntityFromRequest(updateRequest, sampleEvent);
        verify(eventRepository).save(sampleEvent);
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when non-owner updates event")
    void updateEvent_NonOwner_ThrowsAccessDeniedException() {
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .title("Hacked Title")
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> eventService.updateEvent(1L, updateRequest, "hacker_user", false))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You do not have permission to modify this event");

        verify(eventRepository, never()).save(any());
    }
}
