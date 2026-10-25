export interface TheaterShowtime {
  time: string
  format: string
  screen: string
  price: string
  availableSeats: number
}

export interface MovieAtTheater {
  id: number
  title: string
  slug: string
  posterUrl: string
  ageRatingCode: string
  durationMinutes: number
  genre: string
  rating: number
  director: string
  showtimes: TheaterShowtime[]
}

export interface TechSpec {
  title: string
  description: string
}

export interface TheaterComplex {
  id: number
  name: string
  brand: string
  city: string
  district: string
  address: string
  phone: string
  totalScreens: number
  totalSeats: number
  imageUrl: string
  formats: string[]
  facilities: string[]
  openHours: string
  rating: number
  reviewsCount: number
  parkingGuide: string
  techSpecs: TechSpec[]
  movies: MovieAtTheater[]
}

const generateShowtimes = (baseHour: number): TheaterShowtime[] => [
  { time: `${String(baseHour).padStart(2, '0')}:15`, format: '2D Phụ Đề', screen: 'Rạp 02', price: '95.000đ', availableSeats: 48 },
  { time: `${String(baseHour + 2).padStart(2, '0')}:30`, format: 'IMAX Laser', screen: 'Rạp IMAX 01', price: '165.000đ', availableSeats: 26 },
  { time: `${String(baseHour + 4).padStart(2, '0')}:45`, format: 'Dolby Atmos', screen: 'Rạp VIP 03', price: '125.000đ', availableSeats: 62 },
  { time: `${String(baseHour + 7).padStart(2, '0')}:15`, format: 'ScreenX 270°', screen: 'Rạp ScreenX', price: '145.000đ', availableSeats: 34 },
  { time: `${String(baseHour + 9).padStart(2, '0')}:50`, format: '2D Lồng Tiếng', screen: 'Rạp 04', price: '95.000đ', availableSeats: 80 },
]

export const THEATERS_DATA: TheaterComplex[] = [
  {
    id: 1,
    name: 'CineVerse Megamall Landmark 81 IMAX Laser',
    brand: 'CineVerse Luxe',
    city: 'TP. Hồ Chí Minh',
    district: 'Quận Bình Thạnh',
    address: 'Tầng B1, TTTM Vincom Center Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP.HCM',
    phone: '1900 6868 - Ext 1',
    totalScreens: 10,
    totalSeats: 1850,
    imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1200&q=80',
    formats: ['IMAX Laser', 'Dolby Atmos', 'Gold Class', 'ScreenX 270°'],
    facilities: ['Ghế đôi Sweetbox', 'Bắp nước Gourmet Bar', 'Quầy tự động VietQR', 'Bãi đỗ ô tô 3 tầng B1-B3', 'Sảnh VIP Lounge'],
    openHours: '08:00 – 01:30 (Tất cả các ngày)',
    rating: 4.9,
    reviewsCount: 1420,
    parkingGuide: 'Gửi xe tại hầm B2 hoặc B3 Vincom Landmark 81. Xuất trình cuống vé để nhận giảm 50% phí giữ xe.',
    techSpecs: [
      { title: 'Màn Chiếu IMAX with Laser 28m x 16m', description: 'Màn hình cong cực đại chuẩn Hollywood, độ sáng gấp 1.6 lần máy chiếu laser thông thường.' },
      { title: 'Âm Thanh Vòm Dolby Atmos 64 Kênh', description: 'Loa vòm trần và vách tường độc lập tạo không gian âm thanh 3D sống động chân thực.' },
      { title: 'Ghế Sofa Da Công Thái Học', description: 'Tích hợp cổng sạc không dây, điều chỉnh ngả lưng 140 độ êm ái.' },
    ],
    movies: [
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình, Hành Động',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(9),
      },
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(10),
      },
      {
        id: 103,
        title: 'Jujutsu Kaisen 0: Chú Thuật Hồi Chiến',
        slug: 'jujutsu-kaisen-0-chu-thuat-hoi-chien',
        posterUrl: '/images/poster_jujutsu.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 105,
        genre: 'Anime, Siêu Nhiên',
        rating: 9.2,
        director: 'Sunghoo Park',
        showtimes: generateShowtimes(11),
      },
      {
        id: 104,
        title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
        slug: 'mission-impossible-final-reckoning',
        posterUrl: '/images/poster_mission.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 155,
        genre: 'Hành Động, Điệp Viên',
        rating: 8.8,
        director: 'Christopher McQuarrie',
        showtimes: generateShowtimes(13),
      },
      {
        id: 105,
        title: 'Lilo & Stitch: Bản Điện Ảnh',
        slug: 'lilo-and-stitch-live-action',
        posterUrl: '/images/poster_stitch.jpg',
        ageRatingCode: 'P',
        durationMinutes: 110,
        genre: 'Gia Đình, Hoạt Hình',
        rating: 8.6,
        director: 'Dean Fleischer Camp',
        showtimes: generateShowtimes(14),
      },
    ],
  },
  {
    id: 2,
    name: 'CGV Vincom Center Đồng Khởi Gold Class',
    brand: 'CGV Cinemas',
    city: 'TP. Hồ Chí Minh',
    district: 'Quận 1',
    address: 'Tầng 3, TTTM Vincom Center Đồng Khởi, 72 Lê Thánh Tôn, Bến Nghé, Quận 1, TP.HCM',
    phone: '1900 6017',
    totalScreens: 8,
    totalSeats: 1240,
    imageUrl: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1200&q=80',
    formats: ['Dolby Atmos', 'Gold Class', 'Laser 4K'],
    facilities: ['Phòng chờ VIP Lounge', 'Phục vụ trà bánh tận ghế', 'Lối đi khuyết tật', 'Vị trí trung tâm Q1'],
    openHours: '08:00 – 01:00 (Hàng ngày)',
    rating: 4.88,
    reviewsCount: 980,
    parkingGuide: 'Hầm gửi xe Vincom Đồng Khởi đường Lý Tự Trọng. Giữ cuống vé thanh toán phí ưu đãi.',
    techSpecs: [
      { title: 'Gold Class First-Class Seating', description: 'Ghế sofa da thượng hạng êm ái với gối nhung và tai nghe cá nhân.' },
      { title: 'Laser 4K Christie', description: 'Màu sắc sống động, độ tương phản sâu đạt chuẩn màu DCI-P3.' },
    ],
    movies: [
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(10),
      },
      {
        id: 104,
        title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
        slug: 'mission-impossible-final-reckoning',
        posterUrl: '/images/poster_mission.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 155,
        genre: 'Hành Động, Điệp Viên',
        rating: 8.8,
        director: 'Christopher McQuarrie',
        showtimes: generateShowtimes(12),
      },
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(14),
      },
      {
        id: 103,
        title: 'Jujutsu Kaisen 0: Chú Thuật Hồi Chiến',
        slug: 'jujutsu-kaisen-0-chu-thuat-hoi-chien',
        posterUrl: '/images/poster_jujutsu.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 105,
        genre: 'Anime, Siêu Nhiên',
        rating: 9.2,
        director: 'Sunghoo Park',
        showtimes: generateShowtimes(16),
      },
    ],
  },
  {
    id: 3,
    name: 'BHD Star Cineplex Bitexco Iconic ScreenX',
    brand: 'BHD Star',
    city: 'TP. Hồ Chí Minh',
    district: 'Quận 1',
    address: 'Tầng 3 & 4, Tòa Tháp Bitexco Financial Tower, 02 Hải Triều, P. Bến Nghé, Quận 1, TP.HCM',
    phone: '1900 2099',
    totalScreens: 7,
    totalSeats: 1100,
    imageUrl: 'https://images.unsplash.com/photo-1595769816263-9b910be24d5f?auto=format&fit=crop&w=1200&q=80',
    formats: ['ScreenX 270°', 'Dolby Atmos', 'Laser 2D'],
    facilities: ['Màn hình vách tường ScreenX', 'Quầy cà phê BHD Special', 'View Bitexco Sài Gòn'],
    openHours: '08:30 – 00:30 (Hàng ngày)',
    rating: 4.82,
    reviewsCount: 760,
    parkingGuide: 'Gửi xe hầm Bitexco cổng Hải Triều hoặc bãi xe đường Hồ Tùng Mậu.',
    techSpecs: [
      { title: 'ScreenX 270 Độ', description: 'Hình ảnh phóng chiếu lên cả 2 vách tường mở rộng góc nhìn toàn cảnh.' },
    ],
    movies: [
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(10),
      },
      {
        id: 103,
        title: 'Jujutsu Kaisen 0: Chú Thuật Hồi Chiến',
        slug: 'jujutsu-kaisen-0-chu-thuat-hoi-chien',
        posterUrl: '/images/poster_jujutsu.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 105,
        genre: 'Anime, Siêu Nhiên',
        rating: 9.2,
        director: 'Sunghoo Park',
        showtimes: generateShowtimes(12),
      },
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(15),
      },
    ],
  },
  {
    id: 4,
    name: 'CineVerse Vincom Center Bà Triệu IMAX Laser Hà Nội',
    brand: 'CineVerse Royal',
    city: 'Hà Nội',
    district: 'Quận Hai Bà Trưng',
    address: 'Tầng 6, Vincom Center, 191 Bà Triệu, Phường Lê Đại Hành, Quận Hai Bà Trưng, Hà Nội',
    phone: '1900 6868 - Ext 2',
    totalScreens: 9,
    totalSeats: 1680,
    imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80',
    formats: ['IMAX Laser', 'Dolby Atmos', 'Gold Class'],
    facilities: ['Phòng chiếu IMAX Thủ Đô', 'Sweetbox đôi riêng tư', 'Thang máy riêng biệt', 'Khu check-in Neon'],
    openHours: '08:30 – 01:00 (Hàng ngày)',
    rating: 4.94,
    reviewsCount: 1150,
    parkingGuide: 'Gửi xe hầm B1 Vincom Bà Triệu. Xuất trình mã vé tại quầy lễ tân để nhận vé miễn phí gửi xe.',
    techSpecs: [
      { title: 'IMAX with Laser 4K kép', description: 'Độ sắc nét đỉnh cao và dải tương phản HDR sâu thẳm.' },
      { title: 'Âm Thanh 12 Kênh IMAX', description: 'Loa tần số siêu trầm tạo độ rung động chân thực.' },
    ],
    movies: [
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình, Hành Động',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(9),
      },
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(11),
      },
      {
        id: 104,
        title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
        slug: 'mission-impossible-final-reckoning',
        posterUrl: '/images/poster_mission.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 155,
        genre: 'Hành Động, Điệp Viên',
        rating: 8.8,
        director: 'Christopher McQuarrie',
        showtimes: generateShowtimes(13),
      },
      {
        id: 105,
        title: 'Lilo & Stitch: Bản Điện Ảnh',
        slug: 'lilo-and-stitch-live-action',
        posterUrl: '/images/poster_stitch.jpg',
        ageRatingCode: 'P',
        durationMinutes: 110,
        genre: 'Gia Đình, Hoạt Hình',
        rating: 8.6,
        director: 'Dean Fleischer Camp',
        showtimes: generateShowtimes(15),
      },
    ],
  },
  {
    id: 5,
    name: 'CGV Vincom Mega Mall Royal City 4DX Motion',
    brand: 'CGV Cinemas',
    city: 'Hà Nội',
    district: 'Quận Thanh Xuân',
    address: 'Tầng B2, Vincom Mega Mall Royal City, 72A Nguyễn Trãi, Thượng Đình, Quận Thanh Xuân, Hà Nội',
    phone: '1900 6017',
    totalScreens: 12,
    totalSeats: 2150,
    imageUrl: 'https://images.unsplash.com/photo-1524712245354-2c4e5e7121c0?auto=format&fit=crop&w=1200&q=80',
    formats: ['4DX Dynamic', 'Dolby Atmos', 'Laser 4K'],
    facilities: ['Cụm rạp lớn nhất miền Bắc', 'Phòng chiếu ghế rung lắc 4DX', 'Hiệu ứng gió sương tuyết', 'Sân trượt băng'],
    openHours: '08:00 – 01:00 (Hàng ngày)',
    rating: 4.86,
    reviewsCount: 1320,
    parkingGuide: 'Gửi xe tại hầm B3 Royal City, đi theo thang cuốn lên tầng B2 khu ẩm thực.',
    techSpecs: [
      { title: '4DX Motion & Effects', description: 'Ghế ngồi đồng bộ rung, lắc theo chuyển động khung hình.' },
    ],
    movies: [
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(9),
      },
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(11),
      },
      {
        id: 105,
        title: 'Lilo & Stitch: Bản Điện Ảnh',
        slug: 'lilo-and-stitch-live-action',
        posterUrl: '/images/poster_stitch.jpg',
        ageRatingCode: 'P',
        durationMinutes: 110,
        genre: 'Gia Đình, Hoạt Hình',
        rating: 8.6,
        director: 'Dean Fleischer Camp',
        showtimes: generateShowtimes(13),
      },
    ],
  },
  {
    id: 6,
    name: 'CineVerse Vincom Plaza Ngô Quyền Đà Nẵng',
    brand: 'CineVerse Coast',
    city: 'Đà Nẵng',
    district: 'Quận Sơn Trà',
    address: 'Tầng 4, TTTM Vincom Plaza Đà Nẵng, 910A Ngô Quyền, Phường An Hải Bắc, Quận Sơn Trà, Đà Nẵng',
    phone: '1900 6868 - Ext 3',
    totalScreens: 6,
    totalSeats: 980,
    imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1200&q=80',
    formats: ['Dolby Atmos', 'Laser 4K', 'Sweetbox'],
    facilities: ['Tầm nhìn cầu Sông Hàn', 'Ghế nệm êm ái', 'Gần biển Mỹ Khê', 'In vé QR tự động'],
    openHours: '08:30 – 23:45 (Hàng ngày)',
    rating: 4.85,
    reviewsCount: 640,
    parkingGuide: 'Gửi xe máy và ô tô tại tầng hầm Vincom Ngô Quyền miễn phí với hóa đơn vé.',
    techSpecs: [
      { title: 'Dolby Atmos Surround', description: 'Âm vòm đa tầng sống động, rõ nét.' },
    ],
    movies: [
      {
        id: 101,
        title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
        slug: 'thanh-guom-diet-quy-vo-han-thanh',
        posterUrl: '/images/poster_demon_slayer.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 135,
        genre: 'Anime, Hoạt Hình',
        rating: 9.8,
        director: 'Haruo Sotozaki',
        showtimes: generateShowtimes(10),
      },
      {
        id: 102,
        title: 'F1: The Movie - Chinh Phục Tốc Độ',
        slug: 'f1-the-movie-chinh-phuc-toc-do',
        posterUrl: '/images/poster_f1.jpg',
        ageRatingCode: 'T13',
        durationMinutes: 142,
        genre: 'Hành Động, Thể Thao',
        rating: 8.9,
        director: 'Joseph Kosinski',
        showtimes: generateShowtimes(13),
      },
      {
        id: 104,
        title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
        slug: 'mission-impossible-final-reckoning',
        posterUrl: '/images/poster_mission.jpg',
        ageRatingCode: 'T16',
        durationMinutes: 155,
        genre: 'Hành Động, Điệp Viên',
        rating: 8.8,
        director: 'Christopher McQuarrie',
        showtimes: generateShowtimes(15),
      },
    ],
  },
]

export const getTheaterById = (id: number): TheaterComplex | undefined => {
  return THEATERS_DATA.find((t) => t.id === id)
}
