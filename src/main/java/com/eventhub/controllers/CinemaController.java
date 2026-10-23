package com.eventhub.controllers;

import com.eventhub.common.ApiResponse;
import com.eventhub.dto.request.CinemaBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.dto.response.CinemaComplexResponse;
import com.eventhub.dto.response.CinemaSeatResponse;
import com.eventhub.dto.response.ConcessionItemResponse;
import com.eventhub.dto.response.MovieResponse;
import com.eventhub.dto.response.ShowtimeResponse;
import com.eventhub.services.CinemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cinema")
@RequiredArgsConstructor
@Tag(name = "EventHub Cinema", description = "Hệ Thống Rạp Chiếu Phim: Phim Bom Tấn, Cụm Rạp, Suất Chiếu, Ghế Sweetbox & Bắp Nước F&B")
public class CinemaController {

    private final CinemaService cinemaService;

    @Operation(summary = "Lấy danh sách phim đang chiếu tại các rạp")
    @GetMapping("/movies/now-showing")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getNowShowingMovies() {
        List<MovieResponse> movies = cinemaService.getNowShowingMovies();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phim đang chiếu thành công", movies));
    }

    @Operation(summary = "Lấy danh sách phim sắp khởi chiếu")
    @GetMapping("/movies/coming-soon")
    public ResponseEntity<ApiResponse<List<MovieResponse>>> getComingSoonMovies() {
        List<MovieResponse> movies = cinemaService.getComingSoonMovies();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phim sắp chiếu thành công", movies));
    }

    @Operation(summary = "Lấy chi tiết phim theo ID hoặc Slug (kèm trailer, đạo diễn, diễn viên, thời lượng)")
    @GetMapping("/movies/{idOrSlug}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieDetail(@PathVariable String idOrSlug) {
        MovieResponse movie = cinemaService.getMovieByIdOrSlug(idOrSlug);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phim thành công", movie));
    }

    @Operation(summary = "Lấy danh sách Cụm rạp chi nhánh theo thành phố")
    @GetMapping("/complexes")
    public ResponseEntity<ApiResponse<List<CinemaComplexResponse>>> getCinemaComplexes(
            @RequestParam(required = false) String city
    ) {
        List<CinemaComplexResponse> complexes = cinemaService.getCinemaComplexes(city);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cụm rạp thành công", complexes));
    }

    @Operation(summary = "Lấy lịch chiếu của một bộ phim theo ngày và cụm rạp")
    @GetMapping("/movies/{id}/showtimes")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getMovieShowtimes(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ShowtimeResponse> showtimes = cinemaService.getShowtimesByMovieAndDate(id, date);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch chiếu phim thành công", showtimes));
    }

    @Operation(summary = "Lấy toàn bộ lịch chiếu trong ngày của một cụm rạp")
    @GetMapping("/complexes/{id}/showtimes")
    public ResponseEntity<ApiResponse<List<ShowtimeResponse>>> getComplexShowtimes(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<ShowtimeResponse> showtimes = cinemaService.getShowtimesByComplexAndDate(id, date);
        return ResponseEntity.ok(ApiResponse.success("Lấy lịch chiếu theo cụm rạp thành công", showtimes));
    }

    @Operation(summary = "Lấy danh mục Bắp Rang Bơ & Nước Ngọt (F&B Concessions)")
    @GetMapping("/concessions")
    public ResponseEntity<ApiResponse<List<ConcessionItemResponse>>> getConcessions() {
        List<ConcessionItemResponse> items = cinemaService.getConcessions();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh mục bắp nước thành công", items));
    }

    @Operation(summary = "Lấy sơ đồ ghế chi tiết của suất chiếu (Standard, VIP, Sweetbox)")
    @GetMapping("/showtimes/{id}/seats")
    public ResponseEntity<ApiResponse<List<CinemaSeatResponse>>> getShowtimeSeats(@PathVariable Long id) {
        List<CinemaSeatResponse> seats = cinemaService.getShowtimeSeats(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy sơ đồ ghế suất chiếu thành công", seats));
    }

    @Operation(summary = "Đặt vé xem phim kèm ghế và bắp nước F&B")
    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> bookCinemaTickets(
            @Valid @RequestBody CinemaBookingRequest request,
            Authentication authentication
    ) {
        String username = authentication != null ? authentication.getName() : "customer";
        BookingResponse booking = cinemaService.bookCinemaTickets(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt vé rạp chiếu phim thành công", booking));
    }
}

