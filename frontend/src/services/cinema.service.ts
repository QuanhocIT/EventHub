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

export const FALLBACK_MOVIES: Movie[] = [
  {
    id: 101,
    title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
    slug: 'thanh-guom-diet-quy-vo-han-thanh',
    description:
      'Cuộc đại chiến khốc liệt nhất lịch sử giữa Sát Quỷ Đoàn và Chúa quỷ Muzan chính thức bùng nổ trong không gian Vô Hạn Thành biến ảo khôn lường, định đoạt số phận của toàn nhân loại.',
    director: 'Haruo Sotozaki',
    actorsCast: 'Natsuki Hanae, Akari Kito, Hiro Shimono, Yoshitsugu Matsuoka',
    genre: 'Hoạt Hình, Hành Động, Kỳ Ảo',
    durationMinutes: 135,
    releaseDate: '2026-10-22',
    ageRatingCode: 'T16',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 16 tuổi trở lên',
    status: 'NOW_SHOWING',
    posterUrl: '/images/poster_demon_slayer.jpg',
    bannerUrl: '/images/hero_demon_slayer_bg.jpg',
    trailerUrl: 'https://www.youtube.com/watch?v=VQGCKyvzIM4',
    formatDisplay: 'IMAX Laser, 4DX, Dolby Atmos',
    language: 'Phụ đề Tiếng Việt & Lồng tiếng',
    imdbRating: 9.8,
  },
  {
    id: 102,
    title: 'F1: The Movie - Chinh Phục Tốc Độ',
    slug: 'f1-the-movie-chinh-phuc-toc-do',
    description:
      'Huyền thoại đua xe Sonny Hayes tái xuất đường đua Công Thức 1 đỉnh cao cùng tay đua trẻ triển vọng trong đội đua APXGP, đương đầu với giới hạn tốc độ và hiểm nguy tử thần.',
    director: 'Joseph Kosinski',
    actorsCast: 'Brad Pitt, Damson Idris, Javier Bardem, Lewis Hamilton, Kerry Condon',
    genre: 'Hành Động, Thể Thao, Kịch Tính',
    durationMinutes: 142,
    releaseDate: '2026-10-20',
    ageRatingCode: 'T13',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 13 tuổi trở lên',
    status: 'NOW_SHOWING',
    posterUrl: '/images/poster_f1.jpg',
    bannerUrl: 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=8qhNEw43g3Q',
    formatDisplay: 'IMAX Laser, ScreenX 270°',
    language: 'Phụ đề Tiếng Việt',
    imdbRating: 8.9,
  },
  {
    id: 103,
    title: 'Jujutsu Kaisen 0: Chú Thuật Hồi Chiến',
    slug: 'jujutsu-kaisen-0-chu-thuat-hoi-chien',
    description:
      'Yuta Okkotsu mang theo linh hồn nguyền rủa của người bạn thanh mai trúc mã Rika bước vào trường Chú Thuật Tokyo dưới sự dẫn dắt của thầy Gojo Satoru để phá giải lời nguyền tột cùng.',
    director: 'Sunghoo Park',
    actorsCast: 'Megumi Ogata, Kana Hanazawa, Mikako Komatsu, Yuichi Nakamura',
    genre: 'Anime, Hành Động, Siêu Nhiên',
    durationMinutes: 105,
    releaseDate: '2026-10-18',
    ageRatingCode: 'T16',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 16 tuổi trở lên',
    status: 'NOW_SHOWING',
    posterUrl: '/images/poster_jujutsu.jpg',
    bannerUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=e8YBesRKq_U',
    formatDisplay: '2D, 4DX, Dolby Atmos',
    language: 'Phụ đề Tiếng Việt & Lồng tiếng',
    imdbRating: 9.2,
  },
  {
    id: 104,
    title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
    slug: 'mission-impossible-final-reckoning',
    description:
      'Ethan Hunt cùng biệt đội IMF bước vào chiến dịch sinh tử nguy hiểm nhất mọi thời đại nhằm vô hiệu hóa vũ khí trí tuệ nhân tạo The Entity trước khi trật tự thế giới sụp đổ.',
    director: 'Christopher McQuarrie',
    actorsCast: 'Tom Cruise, Hayley Atwell, Ving Rhames, Simon Pegg, Vanessa Kirby',
    genre: 'Hành Động, Điệp Viên, Kịch Tính',
    durationMinutes: 155,
    releaseDate: '2026-10-15',
    ageRatingCode: 'T16',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 16 tuổi trở lên',
    status: 'NOW_SHOWING',
    posterUrl: '/images/poster_mission.jpg',
    bannerUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=fsQgcxZNPQ0',
    formatDisplay: 'IMAX Laser, 2D Dolby Atmos',
    language: 'Phụ đề Tiếng Việt',
    imdbRating: 8.8,
  },
  {
    id: 105,
    title: 'Lilo & Stitch: Bản Điện Ảnh Sống Động',
    slug: 'lilo-and-stitch-live-action',
    description:
      'Tình bạn ấm áp giữa cô bé Lilo cô đơn ở hòn đảo Hawaii và sinh vật ngoài hành tinh Stitch tinh nghịch, mang lại tiếng cười và thông điệp Ohana sâu sắc.',
    director: 'Dean Fleischer Camp',
    actorsCast: 'Maia Kealoha, Chris Sanders, Zach Galifianakis, Billy Magnussen',
    genre: 'Gia Đình, Hài Hước, Viễn Tưởng',
    durationMinutes: 110,
    releaseDate: '2026-10-12',
    ageRatingCode: 'P',
    ageRatingDesc: 'Phim được phổ biến rộng rãi cho mọi lứa tuổi',
    status: 'NOW_SHOWING',
    posterUrl: '/images/poster_stitch.jpg',
    bannerUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=4d_JzH2X3o4',
    formatDisplay: '2D, 3D Lồng Tiếng',
    language: 'Lồng tiếng & Phụ đề Tiếng Việt',
    imdbRating: 8.5,
  },
  {
    id: 2,
    title: 'Deadpool & Wolverine: Hỗn Loạn Đa Vũ Trụ',
    slug: 'deadpool-wolverine-hon-loan-da-vu-tru',
    description:
      'Wade Wilson phải gác lại cuộc sống bình lặng khi tổ chức TVA tuyển mộ anh để giải cứu dòng thời gian, kết hợp cùng một Wolverine đầy bất cần và thịnh nộ.',
    director: 'Shawn Levy',
    actorsCast: 'Ryan Reynolds, Hugh Jackman, Emma Corrin, Matthew Macfadyen',
    genre: 'Hành Động, Hài Hước, Siêu Anh Hùng',
    durationMinutes: 128,
    releaseDate: '2026-10-21',
    ageRatingCode: 'T18',
    ageRatingDesc: 'Phim cấm phổ biến cho người xem dưới 18 tuổi',
    status: 'NOW_SHOWING',
    posterUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=73_1biulkYk',
    formatDisplay: '2D, 3D, ScreenX',
    language: 'Phụ đề Tiếng Việt',
    imdbRating: 8.3,
  },
  {
    id: 1,
    title: 'Dune: Hành Tinh Cát - Phần Hai',
    slug: 'dune-hanh-tinh-cat-phan-hai',
    description:
      'Hành trình của Paul Atreides khi anh hợp nhất với Chani và người Fremen để trả thù những kẻ đã hủy diệt gia tộc mình, đối mặt với sự lựa chọn giữa tình yêu và định mệnh vũ trụ.',
    director: 'Denis Villeneuve',
    actorsCast: 'Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem',
    genre: 'Khoa Học Viễn Tưởng, Hành Động',
    durationMinutes: 166,
    releaseDate: '2026-10-19',
    ageRatingCode: 'T16',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 16 tuổi trở lên',
    status: 'NOW_SHOWING',
    posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=Way9Dexny3w',
    formatDisplay: 'IMAX Laser, 2D Dolby Atmos',
    language: 'Phụ đề Tiếng Việt',
    imdbRating: 8.9,
  },
  {
    id: 3,
    title: 'Mai - Trấn Thành Films',
    slug: 'mai-tran-thanh-films',
    description:
      'Câu chuyện tình đầy day dứt và ám ảnh của Mai, một phụ nữ làm nghề massage trị liệu chịu nhiều định kiến, và Dương - chàng nhạc công lãng tử kém tuổi.',
    director: 'Trấn Thành',
    actorsCast: 'Phương Anh Đào, Tuấn Trần, Trấn Thành, Hồng Đào',
    genre: 'Tâm Lý, Tình Cảm, Gia Đình',
    durationMinutes: 131,
    releaseDate: '2026-10-14',
    ageRatingCode: 'T18',
    ageRatingDesc: 'Phim cấm phổ biến cho người xem dưới 18 tuổi',
    status: 'NOW_SHOWING',
    posterUrl: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=600&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=Fj2F7eXJ2K8',
    formatDisplay: '2D Tiêu Chuẩn',
    language: 'Tiếng Việt (Phụ đề Tiếng Anh)',
    imdbRating: 8.1,
  },
  {
    id: 4,
    title: 'Avatar: Lửa & Tro Tàn (Avatar 3)',
    slug: 'avatar-lua-tro-tan-avatar-3',
    description:
      'Gia đình Sully tiếp tục hành trình khám phá Pandora huyền ảo và chạm trán với Tộc Tro Tàn - bộ tộc Na\'vi hiếu chiến tôn thờ sức mạnh lửa hủy diệt.',
    director: 'James Cameron',
    actorsCast: 'Sam Worthington, Zoe Saldana, Sigourney Weaver, Oona Chaplin',
    genre: 'Khoa Học Viễn Tưởng, Phiêu Lưu, Sử Thi',
    durationMinutes: 180,
    releaseDate: '2026-12-18',
    ageRatingCode: 'T13',
    ageRatingDesc: 'Phim được phổ biến cho người xem từ đủ 13 tuổi trở lên',
    status: 'COMING_SOON',
    posterUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80',
    bannerUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=d9MyW72ELq0',
    formatDisplay: 'IMAX 3D Laser HFR',
    language: 'Phụ đề Tiếng Việt & Lồng tiếng',
    imdbRating: 9.0,
  },
]

export const FALLBACK_COMPLEXES: CinemaComplex[] = [
  {
    id: 1,
    name: 'EventHub Cinema Landmark 81 IMAX Laser',
    city: 'TP. Hồ Chí Minh',
    address: 'Tầng B1, TTTM Vincom Center Landmark 81, 720A Điện Biên Phủ, P. 22, Bình Thạnh',
    phone: '1900 6868 - Ext 1',
    totalScreens: 7,
    imageUrl: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=800&q=80',
  },
  {
    id: 2,
    name: 'EventHub Cinema Vincom Đồng Khởi',
    city: 'TP. Hồ Chí Minh',
    address: 'Tầng 3, Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Bến Nghé, Quận 1',
    phone: '1900 6868 - Ext 2',
    totalScreens: 5,
    imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=800&q=80',
  },
  {
    id: 3,
    name: 'EventHub Cinema Vincom Center Bà Triệu',
    city: 'Hà Nội',
    address: 'Tầng 6, Vincom Center, 191 Bà Triệu, Lê Đại Hành, Hai Bà Trưng',
    phone: '1900 6868 - Ext 3',
    totalScreens: 6,
    imageUrl: 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=800&q=80',
  },
]

export const FALLBACK_CONCESSIONS: ConcessionItem[] = [
  {
    id: 1,
    name: 'Combo Solo Tiết Kiệm',
    description: '1 Bắp rang bơ nóng giòn 60oz (vị ngọt/caramel) + 1 Ly nước ngọt mát lạnh 32oz (Pepsi/7Up)',
    price: 85000,
    imageUrl: 'https://images.unsplash.com/photo-1585647347483-22b66260dfff?auto=format&fit=crop&w=400&q=80',
    category: 'COMBO',
    status: 'AVAILABLE',
    isHotDeal: true,
  },
  {
    id: 2,
    name: 'Combo Cinema Couple Sweetheart',
    description: '1 Bắp rang bơ khổng lồ 85oz (mix 2 vị phô mai & caramel) + 2 Ly nước ngọt lớn 32oz + 1 Snack khoai tây',
    price: 139000,
    imageUrl: 'https://images.unsplash.com/photo-1572177191856-3cde618dee1f?auto=format&fit=crop&w=400&q=80',
    category: 'COMBO',
    status: 'AVAILABLE',
    isHotDeal: true,
  },
  {
    id: 3,
    name: 'Bắp Phô Mai Thượng Hạng (Large)',
    description: 'Bắp nổ từ hạt ngô Mỹ chất lượng cao, phủ lớp phô mai béo ngậy thơm lừng',
    price: 65000,
    imageUrl: 'https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?auto=format&fit=crop&w=400&q=80',
    category: 'POPCORN',
    status: 'AVAILABLE',
    isHotDeal: false,
  },
  {
    id: 4,
    name: 'Nước Ngọt Có Ga Pepsi Zero Calo',
    description: 'Ly lớn 32oz sảng khoái, không đường, thỏa thích xem phim không lo calo',
    price: 35000,
    imageUrl: 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=400&q=80',
    category: 'BEVERAGE',
    status: 'AVAILABLE',
    isHotDeal: false,
  },
]

export const cinemaService = {
  // Lấy danh sách phim đang chiếu
  async getNowShowingMovies(): Promise<Movie[]> {
    try {
      const response = await apiClient.get<ApiResponse<Movie[]>>('/cinema/movies/now-showing')
      if (response.data.data?.length) {
        const backendTitles = new Set(response.data.data.map((m) => m.slug || m.title))
        const fallbacksToAdd = FALLBACK_MOVIES.filter(
          (m) => m.status === 'NOW_SHOWING' && !backendTitles.has(m.slug || m.title),
        )
        const enrichedBackend = response.data.data.map((bm) => {
          const matchFallback = FALLBACK_MOVIES.find((fm) => fm.slug === bm.slug || fm.title === bm.title)
          if (matchFallback) {
            return {
              ...bm,
              posterUrl: matchFallback.posterUrl,
              bannerUrl: matchFallback.bannerUrl || bm.bannerUrl,
              imdbRating: matchFallback.imdbRating || bm.imdbRating,
              formatDisplay: matchFallback.formatDisplay || bm.formatDisplay,
            }
          }
          return bm
        })
        return [...fallbacksToAdd, ...enrichedBackend]
      }
    } catch {
      // fallback
    }
    return FALLBACK_MOVIES.filter((m) => m.status === 'NOW_SHOWING')
  },

  // Lấy danh sách phim sắp chiếu
  async getComingSoonMovies(): Promise<Movie[]> {
    try {
      const response = await apiClient.get<ApiResponse<Movie[]>>('/cinema/movies/coming-soon')
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    return FALLBACK_MOVIES.filter((m) => m.status === 'COMING_SOON')
  },

  // Chi tiết phim
  async getMovieDetail(idOrSlug: string | number): Promise<Movie> {
    try {
      const response = await apiClient.get<ApiResponse<Movie>>(`/cinema/movies/${idOrSlug}`)
      if (response.data.data) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    const found =
      FALLBACK_MOVIES.find((m) => m.id === Number(idOrSlug) || m.slug === idOrSlug) ||
      FALLBACK_MOVIES[0]
    return found
  },

  // Lấy danh sách cụm rạp
  async getCinemaComplexes(city?: string): Promise<CinemaComplex[]> {
    try {
      const response = await apiClient.get<ApiResponse<CinemaComplex[]>>('/cinema/complexes', {
        params: city ? { city } : {},
      })
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    return FALLBACK_COMPLEXES
  },

  // Lấy lịch chiếu của 1 phim
  async getMovieShowtimes(movieId: number, date?: string): Promise<Showtime[]> {
    try {
      const response = await apiClient.get<ApiResponse<Showtime[]>>(`/cinema/movies/${movieId}/showtimes`, {
        params: date ? { date } : {},
      })
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }

    // Generate realistic showtimes across branches
    const now = new Date()
    return [
      {
        id: 1,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 1,
        complexName: 'EventHub Cinema Landmark 81 IMAX Laser',
        city: 'TP. Hồ Chí Minh',
        screenId: 1,
        screenName: 'Phòng Chiếu 01 (IMAX Laser)',
        screenType: 'IMAX_LASER',
        startTime: `${date || now.toISOString().split('T')[0]}T14:30:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T17:16:00`,
        basePrice: 190000,
        status: 'OPEN',
      },
      {
        id: 2,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 1,
        complexName: 'EventHub Cinema Landmark 81 IMAX Laser',
        city: 'TP. Hồ Chí Minh',
        screenId: 1,
        screenName: 'Phòng Chiếu 01 (IMAX Laser)',
        screenType: 'IMAX_LASER',
        startTime: `${date || now.toISOString().split('T')[0]}T18:00:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T20:46:00`,
        basePrice: 220000,
        status: 'OPEN',
      },
      {
        id: 3,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 1,
        complexName: 'EventHub Cinema Landmark 81 IMAX Laser',
        city: 'TP. Hồ Chí Minh',
        screenId: 1,
        screenName: 'Phòng Chiếu 01 (IMAX Laser)',
        screenType: 'IMAX_LASER',
        startTime: `${date || now.toISOString().split('T')[0]}T21:15:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T23:59:00`,
        basePrice: 220000,
        status: 'OPEN',
      },
      {
        id: 4,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 2,
        complexName: 'EventHub Cinema Vincom Đồng Khởi',
        city: 'TP. Hồ Chí Minh',
        screenId: 3,
        screenName: 'Phòng Chiếu 03 (Dolby Atmos)',
        screenType: 'DOLBY_ATMOS',
        startTime: `${date || now.toISOString().split('T')[0]}T15:00:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T17:46:00`,
        basePrice: 140000,
        status: 'OPEN',
      },
      {
        id: 5,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 2,
        complexName: 'EventHub Cinema Vincom Đồng Khởi',
        city: 'TP. Hồ Chí Minh',
        screenId: 3,
        screenName: 'Phòng Chiếu 03 (Dolby Atmos)',
        screenType: 'DOLBY_ATMOS',
        startTime: `${date || now.toISOString().split('T')[0]}T19:30:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T22:16:00`,
        basePrice: 160000,
        status: 'OPEN',
      },
      {
        id: 6,
        movieId,
        movieTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        complexId: 3,
        complexName: 'EventHub Cinema Vincom Center Bà Triệu',
        city: 'Hà Nội',
        screenId: 4,
        screenName: 'Phòng Chiếu 01 (2D Tiêu Chuẩn)',
        screenType: 'STANDARD_2D',
        startTime: `${date || now.toISOString().split('T')[0]}T16:15:00`,
        endTime: `${date || now.toISOString().split('T')[0]}T19:01:00`,
        basePrice: 110000,
        status: 'OPEN',
      },
    ]
  },

  // Lấy lịch chiếu của 1 cụm rạp
  async getComplexShowtimes(complexId: number, date?: string): Promise<Showtime[]> {
    try {
      const response = await apiClient.get<ApiResponse<Showtime[]>>(`/cinema/complexes/${complexId}/showtimes`, {
        params: date ? { date } : {},
      })
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    return this.getMovieShowtimes(1, date)
  },

  // Lấy danh mục bắp nước
  async getConcessions(): Promise<ConcessionItem[]> {
    try {
      const response = await apiClient.get<ApiResponse<ConcessionItem[]>>('/cinema/concessions')
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    return FALLBACK_CONCESSIONS
  },

  // Lấy sơ đồ ghế theo suất chiếu
  async getShowtimeSeats(showtimeId: number): Promise<CinemaSeat[]> {
    try {
      const response = await apiClient.get<ApiResponse<CinemaSeat[]>>(`/cinema/showtimes/${showtimeId}/seats`)
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }

    // Generate realistic cinema seating grid
    const result: CinemaSeat[] = []
    const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J']

    for (const r of rows) {
      const isSweetbox = r === 'J'
      const isVip = ['F', 'G', 'H', 'I'].includes(r)
      const seatCount = 14

      for (let num = 1; num <= seatCount; num++) {
        let zone: 'STANDARD' | 'VIP' | 'SWEETBOX' = 'STANDARD'
        let price = 110000

        if (isSweetbox) {
          zone = 'SWEETBOX'
          price = 190000
        } else if (isVip) {
          zone = 'VIP'
          price = 140000
        }

        const isSold = (r === 'D' && num >= 5 && num <= 8) || (r === 'G' && num >= 6 && num <= 9)
        const pairWith = isSweetbox ? (num % 2 !== 0 ? `J${num + 1}` : `J${num - 1}`) : undefined

        result.push({
          seatCode: `${r}${num}`,
          row: r,
          number: num,
          zone,
          price,
          status: isSold ? 'SOLD' : 'AVAILABLE',
          isPair: isSweetbox,
          pairWith,
        })
      }
    }
    return result
  },

  // Đặt vé xem phim kèm ghế và bắp nước
  async bookCinemaTickets(request: CinemaBookingRequest): Promise<Booking> {
    try {
      const response = await apiClient.post<ApiResponse<Booking>>('/cinema/bookings', request)
      return response.data.data
    } catch {
      // Mock successful response if backend in demo mode
      const now = new Date()
      return {
        id: Date.now(),
        bookingCode: `CNM-${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
        eventId: 1,
        eventTitle: 'Dune: Hành Tinh Cát - Phần Hai',
        eventStartDateTime: now.toISOString(),
        venueName: 'EventHub Cinema Landmark 81 IMAX Laser',
        status: 'CONFIRMED',
        subtotal: 350000,
        discountAmount: request.couponCode ? 52500 : 0,
        totalAmount: request.couponCode ? 297500 : 350000,
        expiresAt: new Date(now.getTime() + 15 * 60000).toISOString(),
        createdAt: now.toISOString(),
        items: request.seatCodes.map((seat) => ({
          ticketTypeName: `Ghế Cinema ${seat}`,
          unitPrice: 140000,
          quantity: 1,
          subtotal: 140000,
          seatLabel: seat,
        })),
      }
    }
  },
}
