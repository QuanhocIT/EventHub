package com.eventhub.services;

import com.eventhub.dto.request.CinemaBookingRequest;
import com.eventhub.dto.response.BookingResponse;
import com.eventhub.dto.response.CinemaComplexResponse;
import com.eventhub.dto.response.CinemaSeatResponse;
import com.eventhub.dto.response.ConcessionItemResponse;
import com.eventhub.dto.response.MovieResponse;
import com.eventhub.dto.response.ShowtimeResponse;

import java.time.LocalDate;
import java.util.List;

public interface CinemaService {

    List<MovieResponse> getNowShowingMovies();

    List<MovieResponse> getComingSoonMovies();

    MovieResponse getMovieByIdOrSlug(String idOrSlug);

    List<CinemaComplexResponse> getCinemaComplexes(String city);

    List<ShowtimeResponse> getShowtimesByMovieAndDate(Long movieId, LocalDate date);

    List<ShowtimeResponse> getShowtimesByComplexAndDate(Long complexId, LocalDate date);

    List<ConcessionItemResponse> getConcessions();

    List<CinemaSeatResponse> getShowtimeSeats(Long showtimeId);

    BookingResponse bookCinemaTickets(CinemaBookingRequest request, String username);
}
