package com.eventhub.services;

import com.eventhub.dto.request.CreateTicketTypeRequest;
import com.eventhub.dto.response.TicketTypeResponse;
import com.eventhub.entities.Event;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.enums.UserRole;
import com.eventhub.exceptions.BadRequestException;
import com.eventhub.exceptions.ForbiddenException;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.services.impl.TicketTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private TicketTypeServiceImpl ticketTypeService;

    private User organizer;
    private Event sampleEvent;

    @BeforeEach
    void setUp() {
        organizer = User.builder()
                .id(1L)
                .username("john_organizer")
                .role(UserRole.ROLE_ORGANIZER)
                .build();

        sampleEvent = Event.builder()
                .id(10L)
                .title("Tech Summit 2026")
                .organizer(organizer)
                .capacity(500)
                .build();
    }

    @Test
    @DisplayName("Should create ticket type successfully when requester is the owner")
    void createTicketType_Success() {
        CreateTicketTypeRequest request = CreateTicketTypeRequest.builder()
                .name("VIP Pass")
                .description("VIP front row access")
                .price(new BigDecimal("1500000"))
                .totalQuantity(100)
                .minPerOrder(1)
                .maxPerOrder(4)
                .build();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));
        when(ticketTypeRepository.save(any(TicketType.class))).thenAnswer(inv -> {
            TicketType tt = inv.getArgument(0);
            tt.setId(101L);
            return tt;
        });

        TicketTypeResponse response = ticketTypeService.createTicketType(10L, request, "john_organizer", false);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getName()).isEqualTo("VIP Pass");
        assertThat(response.getAvailableQuantity()).isEqualTo(100);
        assertThat(response.getPrice()).isEqualByComparingTo("1500000");

        verify(ticketTypeRepository).save(any(TicketType.class));
    }

    @Test
    @DisplayName("Should throw ForbiddenException when requester is neither owner nor admin")
    void createTicketType_Forbidden() {
        CreateTicketTypeRequest request = CreateTicketTypeRequest.builder()
                .name("Standard")
                .price(new BigDecimal("500000"))
                .totalQuantity(200)
                .build();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> ticketTypeService.createTicketType(10L, request, "hacker_user", false))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permission");

        verify(ticketTypeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when saleStartTime is after saleEndTime")
    void createTicketType_InvalidTimes_ThrowsBadRequestException() {
        LocalDateTime now = LocalDateTime.now();
        CreateTicketTypeRequest request = CreateTicketTypeRequest.builder()
                .name("Early Bird")
                .price(new BigDecimal("300000"))
                .totalQuantity(50)
                .saleStartTime(now.plusDays(5))
                .saleEndTime(now.plusDays(2))
                .build();

        when(eventRepository.findById(10L)).thenReturn(Optional.of(sampleEvent));

        assertThatThrownBy(() -> ticketTypeService.createTicketType(10L, request, "john_organizer", false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Sale start time must be before sale end time");
    }

    @Test
    @DisplayName("Should return ticket types for an event")
    void getTicketTypesByEvent_Success() {
        TicketType tt = TicketType.builder()
                .id(1L)
                .event(sampleEvent)
                .name("General Admission")
                .price(new BigDecimal("200000"))
                .totalQuantity(100)
                .availableQuantity(80)
                .isActive(true)
                .build();

        when(eventRepository.existsById(10L)).thenReturn(true);
        when(ticketTypeRepository.findByEventIdAndIsActiveTrue(10L)).thenReturn(List.of(tt));

        List<TicketTypeResponse> result = ticketTypeService.getTicketTypesByEvent(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("General Admission");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when event not found for ticket types")
    void getTicketTypesByEvent_NotFound() {
        when(eventRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> ticketTypeService.getTicketTypesByEvent(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
