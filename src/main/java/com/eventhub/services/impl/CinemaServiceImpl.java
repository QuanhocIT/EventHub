package com.eventhub.services.impl;

import com.eventhub.dto.request.CinemaBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.dto.response.CinemaComplexResponse;
import com.eventhub.dto.response.CinemaSeatResponse;
import com.eventhub.dto.response.ConcessionItemResponse;
import com.eventhub.dto.response.MovieResponse;
import com.eventhub.dto.response.ShowtimeResponse;
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
import com.eventhub.services.CinemaService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaServiceImpl implements CinemaService {

    private final MovieRepository movieRepository;
    private final CinemaComplexRepository complexRepository;
    private final CinemaScreenRepository screenRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ConcessionItemRepository concessionRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    @PostConstruct
    @Transactional
    public void seedInitialCinemaDataIfEmpty() {
        if (movieRepository.count() > 0) return;
        log.info("Khởi tạo dữ liệu mẫu cho Phân hệ Rạp Chiếu Phim EventHub Cinema...");

        // 1. Seed Movies
        Movie m1 = Movie.builder()
                .title("Dune: Hành Tinh Cát - Phần Hai")
                .slug("dune-hanh-tinh-cat-phan-hai")
                .description("Hành trình của Paul Atreides khi anh hợp nhất với Chani và người Fremen để trả thù những kẻ đã hủy diệt gia tộc mình, đối mặt với sự lựa chọn giữa tình yêu và định mệnh vũ trụ.")
                .director("Denis Villeneuve")
                .actorsCast("Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem")
                .genre("Khoa Học Viễn Tưởng, Hành Động, Phiêu Lưu")
                .durationMinutes(166)
                .releaseDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(25))
                .ageRating(MovieAgeRating.T16)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80")
                .bannerUrl("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=Way9Dexny3w")
                .formatDisplay("IMAX Laser, 2D Dolby Atmos")
                .language("Phụ đề Tiếng Việt")
                .imdbRating(8.9)
                .build();

        Movie m2 = Movie.builder()
                .title("Deadpool & Wolverine: Hỗn Loạn Đa Vũ Trụ")
                .slug("deadpool-wolverine-hon-loan-da-vu-tru")
                .description("Wade Wilson phải gác lại cuộc sống bình lặng khi tổ chức TVA tuyển mộ anh để giải cứu dòng thời gian, kết hợp cùng một Wolverine đầy bất cần và thịnh nộ.")
                .director("Shawn Levy")
                .actorsCast("Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen")
                .genre("Hành Động, Hài Hước, Siêu Anh Hùng")
                .durationMinutes(128)
                .releaseDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(30))
                .ageRating(MovieAgeRating.T18)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=600&q=80")
                .bannerUrl("https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=1600&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=73_1biulkYk")
                .formatDisplay("2D, 3D, ScreenX")
                .language("Phụ đề Tiếng Việt")
                .imdbRating(8.3)
                .build();

        Movie m3 = Movie.builder()
                .title("Mai - Trấn Thành Films")
                .slug("mai-tran-thanh-films")
                .description("Câu chuyện tình đầy day dứt và ám ảnh của Mai, một phụ nữ làm nghề massage trị liệu chịu nhiều định kiến, và Dương - chàng nhạc công lãng tử kém tuổi.")
                .director("Trấn Thành")
                .actorsCast("Phương Anh Đào, Tuấn Trần, Trấn Thành, Hồng Đào")
                .genre("Tâm Lý, Tình Cảm, Gia Đình")
                .durationMinutes(131)
                .releaseDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .ageRating(MovieAgeRating.T18)
                .status(MovieStatus.NOW_SHOWING)
                .posterUrl("https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=600&q=80")
                .bannerUrl("https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1600&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=Fj2F7eXJ2K8")
                .formatDisplay("2D Tiêu Chuẩn")
                .language("Tiếng Việt (Phụ đề Tiếng Anh)")
                .imdbRating(8.1)
                .build();

        Movie m4 = Movie.builder()
                .title("Avatar 3: Tro Tàn & Lửa (Fire and Ash)")
                .slug("avatar-3-tro-tan-va-lua")
                .description("Jake Sully và Neytiri đưa gia đình bước vào một vùng đất chưa từng được khám phá trên Pandora, nơi sinh sống của Tộc Tro Tàn đầy hiếu chiến.")
                .director("James Cameron")
                .actorsCast("Sam Worthington, Zoe Saldaña, Sigourney Weaver")
                .genre("Khoa Học Viễn Tưởng, Sử Thi")
                .durationMinutes(190)
                .releaseDate(LocalDate.now().plusDays(45))
                .ageRating(MovieAgeRating.T13)
                .status(MovieStatus.COMING_SOON)
                .posterUrl("https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80")
                .bannerUrl("https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1600&q=80")
                .trailerUrl("https://www.youtube.com/watch?v=d9MyW72ELq0")
                .formatDisplay("IMAX 3D Laser, HFR 48fps")
                .language("Phụ đề Tiếng Việt")
                .imdbRating(9.2)
                .build();

        movieRepository.saveAll(List.of(m1, m2, m3, m4));

        // 2. Seed Cinema Complexes
        CinemaComplex c1 = CinemaComplex.builder()
                .name("EventHub Cinema Landmark 81")
                .city("TP. Hồ Chí Minh")
                .address("Tầng B1, TTTM Vincom Center Landmark 81, 720A Điện Biên Phủ, Q. Bình Thạnh")
                .phone("028 7300 2222")
                .totalScreens(7)
                .imageUrl("https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80")
                .build();

        CinemaComplex c2 = CinemaComplex.builder()
                .name("EventHub Cinema Vincom Đồng Khởi")
                .city("TP. Hồ Chí Minh")
                .address("Tầng 3, Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Bến Nghé, Quận 1")
                .phone("028 3822 8899")
                .totalScreens(6)
                .imageUrl("https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80")
                .build();

        CinemaComplex c3 = CinemaComplex.builder()
                .name("EventHub Cinema Vincom Bà Triệu")
                .city("Hà Nội")
                .address("Tầng 6, Vincom Center, 191 Bà Triệu, Hai Bà Trưng, Hà Nội")
                .phone("024 3974 3333")
                .totalScreens(5)
                .imageUrl("https://images.unsplash.com/photo-1478720568477-152d9b164e26?auto=format&fit=crop&w=800&q=80")
                .build();

        complexRepository.saveAll(List.of(c1, c2, c3));

        // 3. Seed Cinema Screens
        CinemaScreen s1 = CinemaScreen.builder().complex(c1).screenName("Cinema 1 - IMAX Laser").screenType(ScreenType.IMAX_LASER).totalSeats(140).build();
        CinemaScreen s2 = CinemaScreen.builder().complex(c1).screenName("Cinema 2 - Dolby Atmos").screenType(ScreenType.DOLBY_ATMOS).totalSeats(120).build();
        CinemaScreen s3 = CinemaScreen.builder().complex(c1).screenName("Cinema 3 - Gold Class").screenType(ScreenType.GOLD_CLASS).totalSeats(40).build();
        CinemaScreen s4 = CinemaScreen.builder().complex(c2).screenName("Phòng 1 - Standard 2D").screenType(ScreenType.STANDARD_2D).totalSeats(120).build();
        CinemaScreen s5 = CinemaScreen.builder().complex(c2).screenName("Phòng 2 - ScreenX 270").screenType(ScreenType.SCREEN_X).totalSeats(110).build();
        screenRepository.saveAll(List.of(s1, s2, s3, s4, s5));

        // 4. Seed Showtimes for Today and Next 3 Days
        LocalDate today = LocalDate.now();
        List<Showtime> showtimes = new ArrayList<>();

        LocalTime[] times = { LocalTime.of(9, 30), LocalTime.of(12, 15), LocalTime.of(15, 0), LocalTime.of(18, 30), LocalTime.of(21, 15) };
        for (int dayOffset = 0; dayOffset <= 4; dayOffset++) {
            LocalDate d = today.plusDays(dayOffset);
            for (LocalTime t : times) {
                showtimes.add(Showtime.builder()
                        .movie(m1)
                        .screen(s1)
                        .startTime(LocalDateTime.of(d, t))
                        .endTime(LocalDateTime.of(d, t).plusMinutes(m1.getDurationMinutes()))
                        .basePrice(120000.0)
                        .build());
                showtimes.add(Showtime.builder()
                        .movie(m2)
                        .screen(s2)
                        .startTime(LocalDateTime.of(d, t.plusMinutes(20)))
                        .endTime(LocalDateTime.of(d, t.plusMinutes(20)).plusMinutes(m2.getDurationMinutes()))
                        .basePrice(105000.0)
                        .build());
                showtimes.add(Showtime.builder()
                        .movie(m3)
                        .screen(s4)
                        .startTime(LocalDateTime.of(d, t.plusMinutes(45)))
                        .endTime(LocalDateTime.of(d, t.plusMinutes(45)).plusMinutes(m3.getDurationMinutes()))
                        .basePrice(95000.0)
                        .build());
            }
        }
        showtimeRepository.saveAll(showtimes);

        // 5. Seed F&B Concessions
        ConcessionItem cb1 = ConcessionItem.builder()
                .name("Combo Solo Tiết Kiệm")
                .description("1 Bắp rang bơ nóng giòn 60oz (vị ngọt/caramel) + 1 Ly nước ngọt mát lạnh 32oz (Pepsi/7Up)")
                .price(85000.0)
                .category(ConcessionCategory.COMBO)
                .imageUrl("https://images.unsplash.com/photo-1585647347483-22b66260dfff?auto=format&fit=crop&w=400&q=80")
                .isHotDeal(true)
                .build();

        ConcessionItem cb2 = ConcessionItem.builder()
                .name("Combo Cinema Couple Sweetheart")
                .description("1 Bắp rang bơ khổng lồ 85oz (mix 2 vị phô mai & caramel) + 2 Ly nước ngọt lớn 32oz + 1 Snack khoai tây")
                .price(139000.0)
                .category(ConcessionCategory.COMBO)
                .imageUrl("https://images.unsplash.com/photo-1572177191856-3cde618dee1f?auto=format&fit=crop&w=400&q=80")
                .isHotDeal(true)
                .build();

        ConcessionItem cb3 = ConcessionItem.builder()
                .name("Bắp Phô Mai Thượng Hạng (Large)")
                .description("Bắp nổ từ hạt ngô Mỹ chất lượng cao, phủ lớp phô mai béo ngậy thơm lừng")
                .price(65000.0)
                .category(ConcessionCategory.POPCORN)
                .imageUrl("https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?auto=format&fit=crop&w=400&q=80")
                .isHotDeal(false)
                .build();

        ConcessionItem cb4 = ConcessionItem.builder()
                .name("Nước Ngọt Có Ga Pepsi Zero Calo")
                .description("Ly lớn 32oz sảng khoái, không đường, thỏa thích xem phim không lo calo")
                .price(35000.0)
                .category(ConcessionCategory.BEVERAGE)
                .imageUrl("https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=400&q=80")
                .isHotDeal(false)
                .build();

        concessionRepository.saveAll(List.of(cb1, cb2, cb3, cb4));
        log.info("Khởi tạo thành công dữ liệu EventHub Cinema (4 Phim bom tấn, 3 Cụm rạp, 5 Phòng chiếu, Showtimes và Combo F&B)!");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getNowShowingMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.NOW_SHOWING)
                .stream().map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> getComingSoonMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.COMING_SOON)
                .stream().map(MovieResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieByIdOrSlug(String idOrSlug) {
        try {
            Long id = Long.parseLong(idOrSlug);
            return movieRepository.findById(id)
                    .map(MovieResponse::fromEntity)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với mã: " + id));
        } catch (NumberFormatException e) {
            return movieRepository.findBySlug(idOrSlug)
                    .map(MovieResponse::fromEntity)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phim với slug: " + idOrSlug));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CinemaComplexResponse> getCinemaComplexes(String city) {
        if (city != null && !city.trim().isEmpty()) {
            return complexRepository.findByCityIgnoreCaseOrderByNameAsc(city)
                    .stream().map(CinemaComplexResponse::fromEntity).collect(Collectors.toList());
        }
        return complexRepository.findAll()
                .stream().map(CinemaComplexResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(Long movieId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime startRange = targetDate.atStartOfDay();
        LocalDateTime endRange = targetDate.atTime(LocalTime.MAX);
        return showtimeRepository.findShowtimesByMovieAndDateRange(movieId, startRange, endRange)
                .stream().map(ShowtimeResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByComplexAndDate(Long complexId, LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        LocalDateTime startRange = targetDate.atStartOfDay();
        LocalDateTime endRange = targetDate.atTime(LocalTime.MAX);
        return showtimeRepository.findShowtimesByComplexAndDateRange(complexId, startRange, endRange)
                .stream().map(ShowtimeResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConcessionItemResponse> getConcessions() {
        return concessionRepository.findByStatus("AVAILABLE")
                .stream().map(ConcessionItemResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CinemaSeatResponse> getShowtimeSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu #" + showtimeId));

        Double base = showtime.getBasePrice();
        List<CinemaSeatResponse> seats = new ArrayList<>();

        // Generate 10 rows: A to J, 14 seats per row
        String[] rows = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };

        for (String r : rows) {
            boolean isSweetboxRow = "J".equals(r);
            boolean isVipRow = "F".equals(r) || "G".equals(r) || "H".equals(r) || "I".equals(r);

            for (int col = 1; col <= 14; col++) {
                String seatCode = r + col;
                String zone;
                Double price;
                boolean isPair = false;
                String pairWith = null;

                if (isSweetboxRow) {
                    zone = "SWEETBOX";
                    price = base + 70000.0;
                    isPair = true;
                    // J1 pairs with J2, J3 pairs with J4, etc.
                    int partner = (col % 2 == 1) ? col + 1 : col - 1;
                    pairWith = r + partner;
                } else if (isVipRow) {
                    zone = "VIP";
                    price = base + 20000.0;
                } else {
                    zone = "STANDARD";
                    price = base;
                }

                // Deterministic mock sold seats for realism based on showtimeId
                String status = "AVAILABLE";
                if ((col == 7 || col == 8) && ("F".equals(r) || "G".equals(r))) {
                    status = "SOLD";
                } else if (col == 5 && "D".equals(r)) {
                    status = "HELD";
                }

                seats.add(CinemaSeatResponse.builder()
                        .seatCode(seatCode)
                        .row(r)
                        .number(col)
                        .zone(zone)
                        .price(price)
                        .status(status)
                        .isPair(isPair)
                        .pairWith(pairWith)
                        .build());
            }
        }
        return seats;
    }

    @Override
    @Transactional
    public BookingResponse bookCinemaTickets(CinemaBookingRequest request, String username) {
        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu #" + request.getShowtimeId()));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));

        // Calculate seats total
        double seatSubtotal = 0;
        List<CinemaSeatResponse> layout = getShowtimeSeats(request.getShowtimeId());
        for (String code : request.getSeatCodes()) {
            CinemaSeatResponse s = layout.stream()
                    .filter(item -> item.getSeatCode().equalsIgnoreCase(code))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Mã ghế không hợp lệ: " + code));
            seatSubtotal += s.getPrice();
        }

        // Calculate F&B concessions
        double concessionSubtotal = 0;
        StringBuilder concessionsSummary = new StringBuilder();
        if (request.getConcessions() != null && !request.getConcessions().isEmpty()) {
            for (CinemaBookingRequest.ConcessionOrderRequest item : request.getConcessions()) {
                ConcessionItem ci = concessionRepository.findById(item.getConcessionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy món bắp nước #" + item.getConcessionId()));
                concessionSubtotal += ci.getPrice() * item.getQuantity();
                concessionsSummary.append(ci.getName()).append(" x").append(item.getQuantity()).append(", ");
            }
        }

        double total = seatSubtotal + concessionSubtotal;
        String bookingCode = "CINEMA-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .showtime(showtime)
                .bookingType("CINEMA")
                .concessionsJson(concessionsSummary.length() > 2 ? concessionsSummary.substring(0, concessionsSummary.length() - 2) : "Không có bắp nước")
                .subtotalAmount(BigDecimal.valueOf(total))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(total))
                .status(BookingStatus.CONFIRMED)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        Booking saved = bookingRepository.save(booking);

        return BookingResponse.builder()
                .id(saved.getId())
                .bookingCode(saved.getBookingCode())
                .username(user.getUsername())
                .eventTitle(showtime.getMovie().getTitle() + " (" + showtime.getScreen().getScreenName() + ")")
                .status(saved.getStatus())
                .subtotalAmount(saved.getSubtotalAmount())
                .discountAmount(saved.getDiscountAmount())
                .totalAmount(saved.getTotalAmount())
                .expiresAt(saved.getExpiresAt())
                .items(Collections.emptyList())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
