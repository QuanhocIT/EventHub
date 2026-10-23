import apiClient from './api'
import type { ApiResponse } from '@/types/api'
import type {
  Movie,
  CinemaComplex,
  Showtime,
  CinemaSeat,
  ConcessionItem,
  CinemaBookingRequest,
} from '@/types/cinema'
import type { Booking } from '@/types/booking'

export const cinemaService = {
  // Lấy danh sách phim đang chiếu
  async getNowShowingMovies(): Promise<Movie[]> {
    const response = await apiClient.get<ApiResponse<Movie[]>>('/cinema/movies/now-showing')
    return response.data.data
  },

  // Lấy danh sách phim sắp chiếu
  async getComingSoonMovies(): Promise<Movie[]> {
    const response = await apiClient.get<ApiResponse<Movie[]>>('/cinema/movies/coming-soon')
    return response.data.data
  },

  // Chi tiết phim
  async getMovieDetail(idOrSlug: string | number): Promise<Movie> {
    const response = await apiClient.get<ApiResponse<Movie>>(`/cinema/movies/${idOrSlug}`)
    return response.data.data
  },

  // Lấy danh sách cụm rạp
  async getCinemaComplexes(city?: string): Promise<CinemaComplex[]> {
    const response = await apiClient.get<ApiResponse<CinemaComplex[]>>('/cinema/complexes', {
      params: city ? { city } : {},
    })
    return response.data.data
  },

  // Lấy lịch chiếu của 1 phim
  async getMovieShowtimes(movieId: number, date?: string): Promise<Showtime[]> {
    const response = await apiClient.get<ApiResponse<Showtime[]>>(`/cinema/movies/${movieId}/showtimes`, {
      params: date ? { date } : {},
    })
    return response.data.data
  },

  // Lấy lịch chiếu của 1 cụm rạp
  async getComplexShowtimes(complexId: number, date?: string): Promise<Showtime[]> {
    const response = await apiClient.get<ApiResponse<Showtime[]>>(`/cinema/complexes/${complexId}/showtimes`, {
      params: date ? { date } : {},
    })
    return response.data.data
  },

  // Lấy danh mục bắp nước
  async getConcessions(): Promise<ConcessionItem[]> {
    const response = await apiClient.get<ApiResponse<ConcessionItem[]>>('/cinema/concessions')
    return response.data.data
  },

  // Lấy sơ đồ ghế theo suất chiếu
  async getShowtimeSeats(showtimeId: number): Promise<CinemaSeat[]> {
    const response = await apiClient.get<ApiResponse<CinemaSeat[]>>(`/cinema/showtimes/${showtimeId}/seats`)
    return response.data.data
  },

  // Đặt vé xem phim kèm ghế và bắp nước
  async bookCinemaTickets(request: CinemaBookingRequest): Promise<Booking> {
    const response = await apiClient.post<ApiResponse<Booking>>('/cinema/bookings', request)
    return response.data.data
  },
}
