package com.eventhub.config;

import com.eventhub.entities.Booking;
import com.eventhub.entities.BookingItem;
import com.eventhub.entities.Category;
import com.eventhub.entities.Coupon;
import com.eventhub.entities.Event;
import com.eventhub.entities.EventSeat;
import com.eventhub.entities.Payment;
import com.eventhub.entities.Seat;
import com.eventhub.entities.Ticket;
import com.eventhub.entities.TicketType;
import com.eventhub.entities.User;
import com.eventhub.entities.Venue;
import com.eventhub.enums.BookingStatus;
import com.eventhub.enums.DiscountType;
import com.eventhub.enums.EventSeatStatus;
import com.eventhub.enums.EventStatus;
import com.eventhub.enums.PaymentProvider;
import com.eventhub.enums.PaymentStatus;
import com.eventhub.enums.SeatZone;
import com.eventhub.enums.TicketStatus;
import com.eventhub.enums.UserRole;
import com.eventhub.entities.Review;
import com.eventhub.repositories.BookingRepository;
import com.eventhub.repositories.CategoryRepository;
import com.eventhub.repositories.CouponRepository;
import com.eventhub.repositories.EventRepository;
import com.eventhub.repositories.EventSeatRepository;
import com.eventhub.repositories.PaymentRepository;
import com.eventhub.repositories.ReviewRepository;
import com.eventhub.repositories.SeatRepository;
import com.eventhub.repositories.TicketRepository;
import com.eventhub.repositories.TicketTypeRepository;
import com.eventhub.repositories.UserRepository;
import com.eventhub.repositories.VenueRepository;
import com.eventhub.services.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final CouponRepository couponRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final QrCodeService qrCodeService;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database seed status...");
        ensureUsersSeeded();
        seedCategories();
        seedVenues();
        seedEventsAndTicketTypes();
        seedCoupons();
        seedSampleBookingsAndTickets();
        seedReviews();
        log.info("Database seeding process completed successfully!");
    }

    private void ensureUsersSeeded() {
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@eventhub.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("EventHub Administrator")
                    .phoneNumber("0901234567")
                    .avatarUrl("https://images.unsplash.com/photo-1472099645785-5658abf4ff4e")
                    .role(UserRole.ROLE_ADMIN)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .username("organizer")
                    .email("organizer@eventhub.com")
                    .password(passwordEncoder.encode("organizer123"))
                    .fullName("Event Organizer Pro")
                    .phoneNumber("0912345678")
                    .avatarUrl("https://images.unsplash.com/photo-1519085360753-af0119f7cbe7")
                    .role(UserRole.ROLE_ORGANIZER)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .username("staff")
                    .email("staff@eventhub.com")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Gate Check-in Staff")
                    .phoneNumber("0923456789")
                    .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
                    .role(UserRole.ROLE_STAFF)
                    .enabled(true)
                    .build());

            userRepository.save(User.builder()
                    .username("customer")
                    .email("customer@eventhub.com")
                    .password(passwordEncoder.encode("customer123"))
                    .fullName("Nguyen Van A")
                    .phoneNumber("0987654321")
                    .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb")
                    .role(UserRole.ROLE_CUSTOMER)
                    .enabled(true)
                    .build());
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        log.info("Seeding event categories...");
        List<Category> categories = List.of(
                Category.builder()
                        .name("Công nghệ & Hội thảo")
                        .slug("cong-nghe-hoi-thao")
                        .description("Diễn đàn công nghệ, Trí tuệ nhân tạo (AI), Cloud, Blockchain & Chuyển đổi số doanh nghiệp.")
                        .iconUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87")
                        .active(true)
                        .build(),
                Category.builder()
                        .name("Âm nhạc & Concert")
                        .slug("am-nhac-concert")
                        .description("Các đêm nhạc, liveshow, đại nhạc hội âm nhạc quốc tế và trong nước đỉnh cao.")
                        .iconUrl("https://images.unsplash.com/photo-1514525253161-7a46d19cd819")
                        .active(true)
                        .build(),
                Category.builder()
                        .name("Thể thao & Esports")
                        .slug("the-thao-esports")
                        .description("Giải đấu thể thao chuyên nghiệp, bóng đá, giải chạy Marathon và Esports quy mô lớn.")
                        .iconUrl("https://images.unsplash.com/photo-1461896836934-ffe607ba8211")
                        .active(true)
                        .build(),
                Category.builder()
                        .name("Nghệ thuật & Kịch nghệ")
                        .slug("nghe-thuat-kich-nghe")
                        .description("Hòa nhạc giao hưởng cổ điển, kịch nói, triển lãm nghệ thuật thị giác và giao lưu văn hóa.")
                        .iconUrl("https://images.unsplash.com/photo-1507676184212-d03ab07a01bf")
                        .active(true)
                        .build(),
                Category.builder()
                        .name("Khởi nghiệp & Đầu tư")
                        .slug("khoi-nghiep-dau-tu")
                        .description("Diễn đàn kết nối khởi nghiệp sáng tạo, gọi vốn đầu tư mạo hiểm và networking doanh nhân.")
                        .iconUrl("https://images.unsplash.com/photo-1515187029135-18ee286d815b")
                        .active(true)
                        .build(),
                Category.builder()
                        .name("Lễ hội & Ẩm thực")
                        .slug("le-hoi-am-thuc")
                        .description("Lễ hội văn hóa quốc tế, ngày hội ẩm thực đường phố và triển lãm đặc sản vùng miền.")
                        .iconUrl("https://images.unsplash.com/photo-1555939594-58d7cb561ad1")
                        .active(true)
                        .build()
        );

        categoryRepository.saveAll(categories);
        log.info("Seeded {} categories successfully", categories.size());
    }

    private void seedVenues() {
        if (venueRepository.count() > 0) {
            return;
        }

        log.info("Seeding prestigious venues and seat maps...");

        Venue ncc = Venue.builder()
                .name("Trung tâm Hội nghị Quốc gia (NCC Hà Nội)")
                .address("Số 57 Phạm Hùng, Mễ Trì, Nam Từ Liêm, Hà Nội")
                .capacity(3500)
                .build();
        venueRepository.save(ncc);
        createSeatsForVenue(ncc, List.of("A", "B"), 10, SeatZone.VIP);
        createSeatsForVenue(ncc, List.of("C", "D"), 10, SeatZone.STANDARD);
        createSeatsForVenue(ncc, List.of("E"), 10, SeatZone.BALCONY);

        Venue myDinh = Venue.builder()
                .name("Sân vận động Quốc gia Mỹ Đình")
                .address("Đường Lê Đức Thọ, Mỹ Đình 1, Nam Từ Liêm, Hà Nội")
                .capacity(40000)
                .build();
        venueRepository.save(myDinh);
        createSeatsForVenue(myDinh, List.of("A"), 15, SeatZone.VIP);
        createSeatsForVenue(myDinh, List.of("B", "C"), 15, SeatZone.STANDARD);

        Venue phuTho = Venue.builder()
                .name("Nhà thi đấu Thể dục Thể thao Phú Thọ")
                .address("Số 1 Lữ Gia, Phường 15, Quận 11, TP. Hồ Chí Minh")
                .capacity(5000)
                .build();
        venueRepository.save(phuTho);
        createSeatsForVenue(phuTho, List.of("A"), 12, SeatZone.VIP);
        createSeatsForVenue(phuTho, List.of("B", "C"), 12, SeatZone.STANDARD);

        Venue operaHouse = Venue.builder()
                .name("Nhà hát Lớn Hà Nội (Hanoi Opera House)")
                .address("Số 1 Tràng Tiền, Hoàn Kiếm, Hà Nội")
                .capacity(600)
                .build();
        venueRepository.save(operaHouse);
        createSeatsForVenue(operaHouse, List.of("A"), 8, SeatZone.VIP);
        createSeatsForVenue(operaHouse, List.of("B", "C"), 10, SeatZone.STANDARD);

        Venue secc = Venue.builder()
                .name("Trung tâm Hội chợ và Triển lãm Sài Gòn (SECC)")
                .address("799 Nguyễn Văn Linh, Tân Phú, Quận 7, TP. Hồ Chí Minh")
                .capacity(10000)
                .build();
        venueRepository.save(secc);
        createSeatsForVenue(secc, List.of("A"), 10, SeatZone.VIP);
        createSeatsForVenue(secc, List.of("B", "C"), 15, SeatZone.STANDARD);

        log.info("Seeded 5 famous venues with seat layouts");
    }

    private void createSeatsForVenue(Venue venue, List<String> rows, int seatsPerRow, SeatZone zone) {
        List<Seat> seats = new ArrayList<>();
        for (String row : rows) {
            for (int i = 1; i <= seatsPerRow; i++) {
                seats.add(Seat.builder()
                        .venue(venue)
                        .seatRow(row)
                        .seatNumber(i)
                        .zone(zone)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }

    private void seedEventsAndTicketTypes() {
        if (eventRepository.count() > 0) {
            return;
        }

        log.info("Seeding realistic events and ticket types...");

        User organizer = userRepository.findByUsername("organizer")
                .orElseThrow(() -> new IllegalStateException("Organizer user not found"));

        Category techCat = categoryRepository.findBySlug("cong-nghe-hoi-thao").orElse(null);
        Category musicCat = categoryRepository.findBySlug("am-nhac-concert").orElse(null);
        Category sportsCat = categoryRepository.findBySlug("the-thao-esports").orElse(null);
        Category artCat = categoryRepository.findBySlug("nghe-thuat-kich-nghe").orElse(null);
        Category startupCat = categoryRepository.findBySlug("khoi-nghiep-dau-tu").orElse(null);
        Category foodCat = categoryRepository.findBySlug("le-hoi-am-thuc").orElse(null);

        Venue ncc = venueRepository.findAll().stream()
                .filter(v -> v.getName().contains("NCC"))
                .findFirst().orElse(null);
        Venue myDinh = venueRepository.findAll().stream()
                .filter(v -> v.getName().contains("Mỹ Đình"))
                .findFirst().orElse(null);
        Venue phuTho = venueRepository.findAll().stream()
                .filter(v -> v.getName().contains("Phú Thọ"))
                .findFirst().orElse(null);
        Venue opera = venueRepository.findAll().stream()
                .filter(v -> v.getName().contains("Nhà hát Lớn"))
                .findFirst().orElse(null);
        Venue secc = venueRepository.findAll().stream()
                .filter(v -> v.getName().contains("SECC"))
                .findFirst().orElse(null);

        // Event 1: Tech Summit
        Event techEvent = Event.builder()
                .title("Vietnam Tech Summit 2026 - Kỷ Nguyên Trí Tuệ Nhân Tạo & Cloud")
                .description("Hội nghị công nghệ thường niên quy mô lớn quy tụ hơn 50 chuyên gia hàng đầu từ Google, Microsoft, AWS và các kỳ lân công nghệ Đông Nam Á.")
                .location("Trung tâm Hội nghị Quốc gia (NCC Hà Nội)")
                .startTime(LocalDateTime.now().plusDays(20))
                .endTime(LocalDateTime.now().plusDays(20).plusHours(9))
                .capacity(3000)
                .price(BigDecimal.valueOf(499000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(techCat)
                .venue(ncc)
                .build();
        eventRepository.save(techEvent);
        generateEventSeats(techEvent, ncc, BigDecimal.valueOf(499000));

        ticketTypeRepository.save(TicketType.builder()
                .event(techEvent)
                .name("Early Bird Tech Pass")
                .description("Vé ưu đãi đăng ký sớm, bao gồm tài liệu hội thảo và teabreak")
                .price(BigDecimal.valueOf(399000))
                .totalQuantity(300)
                .availableQuantity(290)
                .minPerOrder(1)
                .maxPerOrder(5)
                .isActive(true)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .event(techEvent)
                .name("Standard Pass")
                .description("Vé tiêu chuẩn tham gia toàn bộ phiên thảo luận chuyên sâu")
                .price(BigDecimal.valueOf(499000))
                .totalQuantity(1500)
                .availableQuantity(1500)
                .minPerOrder(1)
                .maxPerOrder(10)
                .isActive(true)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .event(techEvent)
                .name("VIP Executive Pass")
                .description("Vé VIP ngồi hàng ghế đầu, tiệc trưa riêng với diễn giả và kỷ yếu hội nghị")
                .price(BigDecimal.valueOf(1200000))
                .totalQuantity(200)
                .availableQuantity(198)
                .minPerOrder(1)
                .maxPerOrder(4)
                .isActive(true)
                .build());

        // Event 2: Music Festival
        Event musicEvent = Event.builder()
                .title("Monsoon Music Festival 2026 - Gió Mùa Đổi Nhịp")
                .description("Đại nhạc hội quốc tế ngoài trời sôi động với dàn line-up nghệ sĩ đình đám quốc tế và ban nhạc Indie hàng đầu Việt Nam.")
                .location("Sân vận động Quốc gia Mỹ Đình, Hà Nội")
                .startTime(LocalDateTime.now().plusDays(35))
                .endTime(LocalDateTime.now().plusDays(35).plusHours(7))
                .capacity(25000)
                .price(BigDecimal.valueOf(550000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(musicCat)
                .venue(myDinh)
                .build();
        eventRepository.save(musicEvent);
        generateEventSeats(musicEvent, myDinh, BigDecimal.valueOf(550000));

        ticketTypeRepository.save(TicketType.builder()
                .event(musicEvent)
                .name("GA Early Bird")
                .description("Khu vực sân cỏ đứng, ưu đãi sớm")
                .price(BigDecimal.valueOf(450000))
                .totalQuantity(2000)
                .availableQuantity(2000)
                .minPerOrder(1)
                .maxPerOrder(6)
                .isActive(true)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .event(musicEvent)
                .name("VIP Lounge Ticket")
                .description("Khu vực VIP có mái che, quầy bar miễn phí nước uống và lối đi riêng")
                .price(BigDecimal.valueOf(1800000))
                .totalQuantity(500)
                .availableQuantity(500)
                .minPerOrder(1)
                .maxPerOrder(4)
                .isActive(true)
                .build());

        // Event 3: Esports Championship
        Event esportsEvent = Event.builder()
                .title("Chung Kết Vietnam Championship Series (VCS) Mùa Hè 2026")
                .description("Trận chung kết tranh cúp vô địch Esports lớn nhất năm giữa hai đội tuyển huyền thoại để giành tấm vé đến CKTG.")
                .location("Nhà thi đấu Thể dục Thể thao Phú Thọ, TP.HCM")
                .startTime(LocalDateTime.now().plusDays(14))
                .endTime(LocalDateTime.now().plusDays(14).plusHours(6))
                .capacity(4500)
                .price(BigDecimal.valueOf(250000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(sportsCat)
                .venue(phuTho)
                .build();
        eventRepository.save(esportsEvent);
        generateEventSeats(esportsEvent, phuTho, BigDecimal.valueOf(250000));

        ticketTypeRepository.save(TicketType.builder()
                .event(esportsEvent)
                .name("Vé Khán Đài Tiêu Chuẩn")
                .description("Khán đài trung tâm, quan sát toàn cảnh màn hình LED khổng lồ")
                .price(BigDecimal.valueOf(250000))
                .totalQuantity(3000)
                .availableQuantity(3000)
                .minPerOrder(1)
                .maxPerOrder(4)
                .isActive(true)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .event(esportsEvent)
                .name("Fan Zone Gold")
                .description("Ghế sàn gần sân khấu tuyển thủ, tặng kèm áo thi đấu và poster có chữ ký")
                .price(BigDecimal.valueOf(500000))
                .totalQuantity(500)
                .availableQuantity(500)
                .minPerOrder(1)
                .maxPerOrder(2)
                .isActive(true)
                .build());

        // Event 4: Classical Autumn Symphony
        Event symphonyEvent = Event.builder()
                .title("Hòa Nhạc Giao Hưởng Cổ Điển Mùa Thu - The Autumn Symphony")
                .description("Đêm hòa nhạc cổ điển đặc biệt biểu diễn các kiệt tác của Beethoven, Mozart và Tchaikovsky qua dàn nhạc giao hưởng quốc gia.")
                .location("Nhà hát Lớn Hà Nội")
                .startTime(LocalDateTime.now().plusDays(25))
                .endTime(LocalDateTime.now().plusDays(25).plusHours(3))
                .capacity(550)
                .price(BigDecimal.valueOf(650000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(artCat)
                .venue(opera)
                .build();
        eventRepository.save(symphonyEvent);
        generateEventSeats(symphonyEvent, opera, BigDecimal.valueOf(650000));

        ticketTypeRepository.save(TicketType.builder()
                .event(symphonyEvent)
                .name("Hạng Vé Thính Giả")
                .description("Ghế ngồi tầng 2 và tầng 3 nhà hát")
                .price(BigDecimal.valueOf(650000))
                .totalQuantity(300)
                .availableQuantity(300)
                .minPerOrder(1)
                .maxPerOrder(4)
                .isActive(true)
                .build());

        ticketTypeRepository.save(TicketType.builder()
                .event(symphonyEvent)
                .name("Hạng Vé Royal Box")
                .description("Khoang VIP tầng 1 góc nhìn hoàn hảo và phục vụ rượu vang trước giờ diễn")
                .price(BigDecimal.valueOf(1500000))
                .totalQuantity(60)
                .availableQuantity(60)
                .minPerOrder(1)
                .maxPerOrder(2)
                .isActive(true)
                .build());

        // Event 5: Startup Summit
        Event startupEvent = Event.builder()
                .title("Vietnam Startup Pitch & Venture Summit 2026")
                .description("Diễn đàn kết nối 100+ quỹ đầu tư mạo hiểm quốc tế và 50 startup công nghệ tiềm năng nhất Đông Nam Á.")
                .location("Trung tâm Hội chợ và Triển lãm Sài Gòn (SECC), TP.HCM")
                .startTime(LocalDateTime.now().plusDays(40))
                .endTime(LocalDateTime.now().plusDays(40).plusHours(8))
                .capacity(5000)
                .price(BigDecimal.valueOf(350000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(startupCat)
                .venue(secc)
                .build();
        eventRepository.save(startupEvent);
        generateEventSeats(startupEvent, secc, BigDecimal.valueOf(350000));

        ticketTypeRepository.save(TicketType.builder()
                .event(startupEvent)
                .name("Founder Pass")
                .description("Tham gia các phiên gọi vốn, networking và triển lãm giải pháp")
                .price(BigDecimal.valueOf(350000))
                .totalQuantity(1000)
                .availableQuantity(1000)
                .minPerOrder(1)
                .maxPerOrder(5)
                .isActive(true)
                .build());

        // Event 6: International Food Fest
        Event foodEvent = Event.builder()
                .title("Lễ Hội Ẩm Thực Đường Phố Quốc Tế Sài Gòn 2026")
                .description("Thiên đường ẩm thực quy tụ hơn 200 gian hàng ẩm thực từ Việt Nam, Thái Lan, Hàn Quốc, Nhật Bản và châu Âu.")
                .location("Trung tâm Hội chợ và Triển lãm Sài Gòn (SECC), TP.HCM")
                .startTime(LocalDateTime.now().plusDays(10))
                .endTime(LocalDateTime.now().plusDays(10).plusHours(12))
                .capacity(8000)
                .price(BigDecimal.valueOf(100000))
                .status(EventStatus.PUBLISHED)
                .organizer(organizer)
                .category(foodCat)
                .venue(secc)
                .build();
        eventRepository.save(foodEvent);

        ticketTypeRepository.save(TicketType.builder()
                .event(foodEvent)
                .name("Vé Vào Cổng + 2 Phiếu Thưởng Thức")
                .description("Vé tham quan lễ hội kèm 2 coupon đổi đồ ăn miễn phí")
                .price(BigDecimal.valueOf(100000))
                .totalQuantity(3000)
                .availableQuantity(3000)
                .minPerOrder(1)
                .maxPerOrder(10)
                .isActive(true)
                .build());

        log.info("Seeded 6 comprehensive events with tickets and seat layouts");
    }

    private void generateEventSeats(Event event, Venue venue, BigDecimal basePrice) {
        if (venue == null) return;
        List<Seat> seats = seatRepository.findByVenueIdOrderBySeatRowAscSeatNumberAsc(venue.getId());
        List<EventSeat> eventSeats = new ArrayList<>();
        for (Seat seat : seats) {
            BigDecimal price = basePrice;
            if (seat.getZone() == SeatZone.VIP) {
                price = basePrice.multiply(BigDecimal.valueOf(1.5));
            } else if (seat.getZone() == SeatZone.BALCONY) {
                price = basePrice.multiply(BigDecimal.valueOf(0.8));
            }

            eventSeats.add(EventSeat.builder()
                    .event(event)
                    .seat(seat)
                    .status(EventSeatStatus.AVAILABLE)
                    .price(price)
                    .build());
        }
        eventSeatRepository.saveAll(eventSeats);
    }

    private void seedCoupons() {
        if (couponRepository.count() > 0) {
            return;
        }

        log.info("Seeding discount coupons...");
        Event techEvent = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().contains("Tech Summit"))
                .findFirst().orElse(null);

        List<Coupon> coupons = List.of(
                Coupon.builder()
                        .code("CHAOMUNG2026")
                        .description("Giảm 10% tối đa 100.000đ cho tất cả sự kiện chào đón 2026")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(BigDecimal.valueOf(10.0))
                        .maxDiscountAmount(BigDecimal.valueOf(100000))
                        .minOrderAmount(BigDecimal.valueOf(200000))
                        .usageLimit(500)
                        .usedCount(1)
                        .startDate(LocalDateTime.now().minusDays(5))
                        .endDate(LocalDateTime.now().plusMonths(6))
                        .active(true)
                        .event(null) // Global coupon
                        .build(),
                Coupon.builder()
                        .code("EVENTHUB50K")
                        .description("Giảm ngay 50.000đ cho đơn hàng từ 300.000đ")
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(BigDecimal.valueOf(50000))
                        .maxDiscountAmount(null)
                        .minOrderAmount(BigDecimal.valueOf(300000))
                        .usageLimit(200)
                        .usedCount(0)
                        .startDate(LocalDateTime.now().minusDays(5))
                        .endDate(LocalDateTime.now().plusMonths(3))
                        .active(true)
                        .event(null)
                        .build(),
                Coupon.builder()
                        .code("TECHSUMMIT20")
                        .description("Ưu đãi đặc biệt giảm 20% tối đa 200.000đ cho sự kiện Vietnam Tech Summit 2026")
                        .discountType(DiscountType.PERCENTAGE)
                        .discountValue(BigDecimal.valueOf(20.0))
                        .maxDiscountAmount(BigDecimal.valueOf(200000))
                        .minOrderAmount(BigDecimal.valueOf(350000))
                        .usageLimit(100)
                        .usedCount(0)
                        .startDate(LocalDateTime.now().minusDays(5))
                        .endDate(LocalDateTime.now().plusMonths(2))
                        .active(true)
                        .event(techEvent)
                        .build()
        );

        couponRepository.saveAll(coupons);
        log.info("Seeded 3 discount coupons");
    }

    private void seedSampleBookingsAndTickets() {
        if (bookingRepository.count() > 0) {
            return;
        }

        log.info("Seeding sample confirmed booking and e-tickets for real dashboard analytics...");
        User customer = userRepository.findByUsername("customer").orElse(null);
        User staff = userRepository.findByUsername("staff").orElse(null);
        Event techEvent = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().contains("Tech Summit"))
                .findFirst().orElse(null);

        if (customer == null || techEvent == null) return;

        List<TicketType> ticketTypes = ticketTypeRepository.findByEventId(techEvent.getId());
        TicketType earlyBird = ticketTypes.stream()
                .filter(tt -> tt.getName().contains("Early Bird"))
                .findFirst().orElse(null);

        if (earlyBird == null) return;

        // Create confirmed booking
        Booking booking = Booking.builder()
                .bookingCode("EVH-DEMO-20261021")
                .user(customer)
                .event(techEvent)
                .status(BookingStatus.CONFIRMED)
                .subtotalAmount(BigDecimal.valueOf(798000))
                .discountAmount(BigDecimal.valueOf(79800))
                .couponCode("CHAOMUNG2026")
                .totalAmount(BigDecimal.valueOf(718200))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        BookingItem item1 = BookingItem.builder()
                .ticketType(earlyBird)
                .quantity(2)
                .unitPrice(earlyBird.getPrice())
                .subtotal(earlyBird.getPrice().multiply(BigDecimal.valueOf(2)))
                .build();
        booking.addItem(item1);

        bookingRepository.save(booking);

        // Create payment
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .provider(PaymentProvider.MOCK_SANDBOX)
                .transactionCode("TXN-DEMO-99998888")
                .idempotencyKey("IDEMP-DEMO-001")
                .status(PaymentStatus.SUCCESS)
                .build();
        paymentRepository.save(payment);

        // Generate 2 tickets
        // Ticket 1: USED (checked in by staff)
        String ticketCode1 = "TK-DEMO-0001";
        String qr1 = qrCodeService.generateQrCodeBase64("EVENTHUB:" + ticketCode1, 250, 250);
        Ticket ticket1 = Ticket.builder()
                .ticketCode(ticketCode1)
                .booking(booking)
                .bookingItem(item1)
                .event(techEvent)
                .ticketType(earlyBird)
                .status(TicketStatus.USED)
                .qrCodeData(qr1)
                .checkedInAt(LocalDateTime.now().minusHours(2))
                .checkedInBy(staff)
                .build();

        // Ticket 2: VALID (ready to check-in)
        String ticketCode2 = "TK-DEMO-0002";
        String qr2 = qrCodeService.generateQrCodeBase64("EVENTHUB:" + ticketCode2, 250, 250);
        Ticket ticket2 = Ticket.builder()
                .ticketCode(ticketCode2)
                .booking(booking)
                .bookingItem(item1)
                .event(techEvent)
                .ticketType(earlyBird)
                .status(TicketStatus.VALID)
                .qrCodeData(qr2)
                .build();

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);

        log.info("Seeded demo confirmed booking, payment and e-tickets (1 VALID, 1 USED for check-in demo)");
    }

    private void seedReviews() {
        if (reviewRepository.count() > 0) {
            log.info("Reviews already seeded, skipping.");
            return;
        }

        User customer = userRepository.findByUsername("customer").orElse(null);
        Event techEvent = eventRepository.findAll().stream()
                .filter(e -> e.getTitle().contains("Vietnam Tech Summit"))
                .findFirst()
                .orElse(null);

        if (customer != null && techEvent != null) {
            Review review = Review.builder()
                    .event(techEvent)
                    .user(customer)
                    .rating(5)
                    .comment("Sự kiện quy tụ dàn diễn giả công nghệ hàng đầu, tổ chức rất chuyên nghiệp và công nghệ check-in QR cực nhanh!")
                    .isVerifiedAttendee(true)
                    .build();
            reviewRepository.save(review);

            techEvent.setAverageRating(5.0);
            techEvent.setReviewCount(1);
            eventRepository.save(techEvent);

            log.info("Seeded demo verified review for event: {}", techEvent.getTitle());
        }
    }
}
