package com.eventhub.services;

import com.eventhub.dto.request.CinemaBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.dto.response.CinemaComplexResponse;
import com.eventhub.dto.response.CinemaSeatResponse;
import com.eventhub.dto.response.ConcessionItemResponse;
import com.eventhub.dto.response.MovieResponse;
import com.eventhub.entities.Booking;
import com.eventhub.entities.CinemaComplex;
import com.eventhub.entities.CinemaScreen;
import com.eventhub.entities.ConcessionItem;
import com.eventhub.entities.Movie;
import com.eventhub.entities.Showtime;
import com.eventhub.entities.User;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.ConcessionCategory;
import com.eventhub.enums.MovieAgeRating;
import com.eventhub.enums.MovieStatus;
import com.eventhub.enums.ScreenType;
import com.eventhub.exceptions.ResourceNotFoundException;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.CinemaComplexRepository;
import com.eventhub.repositories.CinemaScreenRepository;
import com.eventhub.repositories.ConcessionItemRepository;
import com.eventhub.repositories.MovieRepository;
import com.eventhub.repositories.ShowtimeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.services.impl.CinemaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private CinemaComplexRepository complexRepository;

    @Mock
    private CinemaScreenRepository screenRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ConcessionItemRepository concessionRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CinemaServiceImpl cinemaService;

    private Movie sampleMovie;
    private CinemaComplex sampleComplex;
    private CinemaScreen sampleScreen;
    private Showtime sampleShowtime;
    private User sampleUser;
    private ConcessionItem sampleConcession;

    @BeforeEach
    void setUp() {
        sampleMovie = Movie.builder()
                .id(1L)
                .title("Dune: Hành Tinh Cát - Phần Hai")
                .slug("dune-hanh-tinh-cat-phan-hai")
                .durationMinutes(166)
                .status(MovieStatus.NOW_SHOWING)
                .ageRating(MovieAgeRating.T16)
                .releaseDate(LocalDate.now().minusDays(5))
                .build();

        sampleComplex = CinemaComplex.builder()
                .id(10L)
                .name("EventHub Cinema Landmark 81")
                .city("TP. Hồ Chí Minh")
                .address("Landmark 81, Q. Bình Thạnh")
                .totalScreens(7)
                .build();

        sampleScreen = CinemaScreen.builder()
                .id(100L)
                .complex(sampleComplex)
                .screenName("Cinema 1 - IMAX Laser")
                .screenType(ScreenType.IMAX_LASER)
                .totalSeats(140)
                .build();

        sampleShowtime = Showtime.builder()
                .id(500L)
                .movie(sampleMovie)
                .screen(sampleScreen)
                .startTime(LocalDateTime.now().plusHours(2))
                .endTime(LocalDateTime.now().plusHours(4).plusMinutes(46))
                .basePrice(120000.0)
                .status("AVAILABLE")
                .build();

        sampleUser = User.builder()
                .id(5L)
                .username("john_doe")
                .fullName("John Doe")
                .email("john@example.com")
                .build();

        sampleConcession = ConcessionItem.builder()
                .id(1L)
                .name("Combo Solo Tiết Kiệm")
                .price(85000.0)
                .category(ConcessionCategory.COMBO)
                .status("AVAILABLE")
                .build();
    }

    @Test
    @DisplayName("Get now showing movies returns active now-showing movie list")
    void getNowShowingMovies_Success() {
        when(movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.NOW_SHOWING))
                .thenReturn(List.of(sampleMovie));

        List<MovieResponse> result = cinemaService.getNowShowingMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Dune: Hành Tinh Cát - Phần Hai");
    }

    @Test
    @DisplayName("Get coming soon movies returns coming soon movie list")
    void getComingSoonMovies_Success() {
        sampleMovie.setStatus(MovieStatus.COMING_SOON);
        when(movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.COMING_SOON))
                .thenReturn(List.of(sampleMovie));

        List<MovieResponse> result = cinemaService.getComingSoonMovies();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(MovieStatus.COMING_SOON.name());
    }

    @Test
    @DisplayName("Get movie by ID or slug returns MovieResponse when found by slug")
    void getMovieByIdOrSlug_Success_BySlug() {
        when(movieRepository.findBySlug("dune-hanh-tinh-cat-phan-hai")).thenReturn(Optional.of(sampleMovie));

        MovieResponse result = cinemaService.getMovieByIdOrSlug("dune-hanh-tinh-cat-phan-hai");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Dune: Hành Tinh Cát - Phần Hai");
    }

    @Test
    @DisplayName("Get movie by ID or slug returns MovieResponse when found by ID")
    void getMovieByIdOrSlug_Success_ById() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));

        MovieResponse result = cinemaService.getMovieByIdOrSlug("1");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Dune: Hành Tinh Cát - Phần Hai");
    }

    @Test
    @DisplayName("Get movie by ID or slug throws ResourceNotFound when movie not found")
    void getMovieByIdOrSlug_ThrowsNotFound() {
        when(movieRepository.findBySlug("unknown-movie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaService.getMovieByIdOrSlug("unknown-movie"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy phim với slug: unknown-movie");
    }

    @Test
    @DisplayName("Get cinema complexes with city filter returns filtered complexes")
    void getCinemaComplexes_WithCity_Success() {
        when(complexRepository.findByCityIgnoreCaseOrderByNameAsc("TP. Hồ Chí Minh"))
                .thenReturn(List.of(sampleComplex));

        List<CinemaComplexResponse> result = cinemaService.getCinemaComplexes("TP. Hồ Chí Minh");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("EventHub Cinema Landmark 81");
    }

    @Test
    @DisplayName("Get cinema complexes without filter returns all complexes")
    void getCinemaComplexes_NoCity_Success() {
        when(complexRepository.findAll()).thenReturn(List.of(sampleComplex));

        List<CinemaComplexResponse> result = cinemaService.getCinemaComplexes(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("EventHub Cinema Landmark 81");
    }

    @Test
    @DisplayName("Get concessions returns available concessions list")
    void getConcessions_Success() {
        when(concessionRepository.findByStatus("AVAILABLE"))
                .thenReturn(List.of(sampleConcession));

        List<ConcessionItemResponse> result = cinemaService.getConcessions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Combo Solo Tiết Kiệm");
    }

    @Test
    @DisplayName("Get showtime seats generates correct seat layout with 140 seats")
    void getShowtimeSeats_Success() {
        when(showtimeRepository.findById(500L)).thenReturn(Optional.of(sampleShowtime));

        List<CinemaSeatResponse> seats = cinemaService.getShowtimeSeats(500L);

        assertThat(seats).isNotEmpty();
        // 10 rows (A-J) x 14 columns = 140 seats
        assertThat(seats).hasSize(140);

        // Check sold status on F7
        CinemaSeatResponse seatF7 = seats.stream().filter(s -> s.getSeatCode().equals("F7")).findFirst().orElse(null);
        assertThat(seatF7).isNotNull();
        assertThat(seatF7.getStatus()).isEqualTo("SOLD");

        // Check available status on A1
        CinemaSeatResponse seatA1 = seats.stream().filter(s -> s.getSeatCode().equals("A1")).findFirst().orElse(null);
        assertThat(seatA1).isNotNull();
        assertThat(seatA1.getStatus()).isEqualTo("AVAILABLE");

        // Check sweetbox pair on J1
        CinemaSeatResponse seatJ1 = seats.stream().filter(s -> s.getSeatCode().equals("J1")).findFirst().orElse(null);
        assertThat(seatJ1).isNotNull();
        assertThat(seatJ1.getIsPair()).isTrue();
        assertThat(seatJ1.getPairWith()).isEqualTo("J2");
    }

    @Test
    @DisplayName("Book cinema tickets creates confirmed booking with seats and concessions")
    void bookCinemaTickets_Success() {
        CinemaBookingRequest request = CinemaBookingRequest.builder()
                .showtimeId(500L)
                .seatCodes(List.of("A1", "A2"))
                .concessions(List.of(
                        CinemaBookingRequest.ConcessionOrderRequest.builder()
                                .concessionId(1L)
                                .quantity(2)
                                .build()
                ))
                .build();

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(sampleUser));
        when(showtimeRepository.findById(500L)).thenReturn(Optional.of(sampleShowtime));
        when(concessionRepository.findById(1L)).thenReturn(Optional.of(sampleConcession));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(99L);
            return b;
        });

        BookingResponse response = cinemaService.bookCinemaTickets(request, "john_doe");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getBookingCode()).startsWith("CINEMA-");

        verify(bookingRepository).save(any(Booking.class));
    }
}
