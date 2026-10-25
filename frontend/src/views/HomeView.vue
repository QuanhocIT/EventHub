<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Play,
  Star,
  Calendar,
  Clock,
  MapPin,
  ChevronRight,
  ChevronLeft,
  ArrowRight,
  Flame,
  CreditCard,
  Armchair,
  Gift,
  Headphones,
  Swords,
  Heart,
  Smile,
  Ghost,
  Compass,
  Home,
  Trophy,
  Smartphone,
  Apple,
  X,
  Sparkles,
  Building2,
  ShieldCheck,
  HelpCircle,
  ChevronDown,
  Check,
  Ticket,
  Eye,
} from 'lucide-vue-next'

const router = useRouter()

// 1. Hero Showcase Carousel Data with Top 4 Blockbusters
const heroMovies = [
  {
    id: 1,
    title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
    slug: 'thanh-guom-diet-quy-vo-han-thanh',
    subtitle: 'Demon Slayer: Kimetsu no Yaiba – Infinity Castle',
    synopsis:
      'Cuộc đại chiến khốc liệt nhất lịch sử giữa Sát Quỷ Đoàn và Chúa quỷ Muzan chính thức bùng nổ trong không gian Vô Hạn Thành biến ảo khôn lường, định đoạt số phận của toàn nhân loại.',
    rating: '9.8 IMDb',
    duration: '135 phút',
    genre: 'Hoạt Hình, Hành Động, Kỳ Ảo',
    formats: 'IMAX LASER, 4DX, DOLBY ATMOS',
    ageRating: 'T16',
    director: 'Haruo Sotozaki',
    actors: 'Natsuki Hanae, Akari Kito, Hiro Shimono, Yoshitsugu Matsuoka',
    posterUrl: '/images/poster_demon_slayer.jpg',
    backdropUrl: '/images/hero_demon_slayer_bg.jpg',
    trailerUrl: 'https://www.youtube.com/watch?v=VQGCKyvzIM4',
  },
  {
    id: 2,
    title: 'F1: The Movie - Chinh Phục Tốc Độ',
    slug: 'f1-the-movie-chinh-phuc-toc-do',
    subtitle: 'Formula 1 Global Phenomenon',
    synopsis:
      'Huyền thoại đua xe Sonny Hayes tái xuất đường đua Công Thức 1 đỉnh cao cùng tay đua trẻ triển vọng trong đội đua APXGP, đương đầu với giới hạn tốc độ và hiểm nguy tử thần.',
    rating: '8.9 IMDb',
    duration: '142 phút',
    genre: 'Hành Động, Thể Thao, Kịch Tính',
    formats: 'IMAX Laser, ScreenX 270°',
    ageRating: 'T13',
    director: 'Joseph Kosinski',
    actors: 'Brad Pitt, Damson Idris, Javier Bardem, Lewis Hamilton',
    posterUrl: '/images/poster_f1.jpg',
    backdropUrl: 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=8qhNEw43g3Q',
  },
  {
    id: 3,
    title: 'Jujutsu Kaisen 0: Chú Thuật Hồi Chiến',
    slug: 'jujutsu-kaisen-0-chu-thuat-hoi-chien',
    subtitle: 'Jujutsu Kaisen The Movie',
    synopsis:
      'Yuta Okkotsu mang theo linh hồn nguyền rủa của người bạn thanh mai trúc mã Rika bước vào trường Chú Thuật Tokyo dưới sự dẫn dắt của thầy Gojo Satoru để phá giải lời nguyền tột cùng.',
    rating: '9.2 IMDb',
    duration: '105 phút',
    genre: 'Anime, Hành Động, Siêu Nhiên',
    formats: '2D, 4DX, Dolby Atmos',
    ageRating: 'T16',
    director: 'Sunghoo Park',
    actors: 'Megumi Ogata, Kana Hanazawa, Mikako Komatsu, Yuichi Nakamura',
    posterUrl: '/images/poster_jujutsu.jpg',
    backdropUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=e8YBesRKq_U',
  },
  {
    id: 4,
    title: 'Mission: Impossible – Nghiệp Báo Cuối Cùng',
    slug: 'mission-impossible-final-reckoning',
    subtitle: 'The Final Reckoning',
    synopsis:
      'Ethan Hunt cùng biệt đội IMF bước vào chiến dịch sinh tử nguy hiểm nhất mọi thời đại nhằm vô hiệu hóa vũ khí trí tuệ nhân tạo The Entity trước khi trật tự thế giới sụp đổ.',
    rating: '8.8 IMDb',
    duration: '155 phút',
    genre: 'Hành Động, Điệp Viên, Kịch Tính',
    formats: 'IMAX Laser, 2D Dolby Atmos',
    ageRating: 'T16',
    director: 'Christopher McQuarrie',
    actors: 'Tom Cruise, Hayley Atwell, Ving Rhames, Simon Pegg',
    posterUrl: '/images/poster_mission.jpg',
    backdropUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=1600&q=80',
    trailerUrl: 'https://www.youtube.com/watch?v=fsQgcxZNPQ0',
  },
]

const currentHeroIndex = ref(0)
const currentHero = computed(() => heroMovies[currentHeroIndex.value])
const isHeroPaused = ref(false)
let heroTimer: any = null

const startHeroTimer = () => {
  stopHeroTimer()
  heroTimer = setInterval(() => {
    if (!isHeroPaused.value) {
      currentHeroIndex.value = (currentHeroIndex.value + 1) % heroMovies.length
    }
  }, 6000)
}

const stopHeroTimer = () => {
  if (heroTimer) {
    clearInterval(heroTimer)
    heroTimer = null
  }
}

onMounted(() => {
  startHeroTimer()
})

onUnmounted(() => {
  stopHeroTimer()
})

const nextHero = () => {
  currentHeroIndex.value = (currentHeroIndex.value + 1) % heroMovies.length
}

const prevHero = () => {
  currentHeroIndex.value = (currentHeroIndex.value - 1 + heroMovies.length) % heroMovies.length
}

const getRatingColor = (code: string) => {
  switch (code) {
    case 'T18':
    case '18+':
      return 'bg-rose-600 text-white border-rose-700'
    case 'T16':
    case '16+':
      return 'bg-amber-500 text-white border-amber-600'
    case 'T13':
    case '13+':
      return 'bg-yellow-400 text-slate-900 border-yellow-500'
    case 'K':
      return 'bg-blue-600 text-white border-blue-700'
    case 'P':
    default:
      return 'bg-emerald-600 text-white border-emerald-700'
  }
}

// 2. Value proposition 5 cards matching Image 2
const valueProps = [
  { title: 'Đặt vé nhanh', desc: 'Chỉ vài thao tác', icon: Calendar },
  { title: 'Thanh toán đa dạng', desc: 'Thẻ, ví điện tử, QR', icon: CreditCard },
  { title: 'Chọn chỗ ngồi', desc: 'Sơ đồ rạp trực quan', icon: Armchair },
  { title: 'Tích điểm thành viên', desc: 'Nhận nhiều ưu đãi', icon: Gift },
  { title: 'Hỗ trợ 24/7', desc: 'Luôn sẵn sàng', icon: Headphones },
]

// 3. Movies List matching Image 2
const moviesList = [
  {
    id: 1,
    title: 'Thanh Gươm Diệt Quỷ: Vô Hạn Thành',
    slug: 'thanh-guom-diet-quy-vo-han-thanh',
    badge: 'HOT',
    badgeColor: 'bg-rose-600',
    rating: 8.8,
    genre: 'Hành động • Anime',
    posterUrl: '/images/poster_demon_slayer.jpg',
    showtimes: ['10:00', '12:30', '15:00', '17:30', '20:00'],
  },
  {
    id: 2,
    title: 'F1: The Movie',
    slug: 'f1-the-movie',
    badge: 'MỚI',
    badgeColor: 'bg-blue-600',
    rating: 7.9,
    genre: 'Hành động • Thể thao',
    posterUrl: '/images/poster_f1.jpg',
    showtimes: ['09:30', '12:00', '14:30', '17:00', '19:30', '22:00'],
  },
  {
    id: 3,
    title: 'Jujutsu Kaisen 0',
    slug: 'jujutsu-kaisen-0',
    badge: 'HOT',
    badgeColor: 'bg-rose-600',
    rating: 8.5,
    genre: 'Hành động • Anime',
    posterUrl: '/images/poster_jujutsu.jpg',
    showtimes: ['10:15', '12:45', '15:15', '17:45', '20:15', '22:45'],
  },
  {
    id: 4,
    title: 'Lilo & Stitch',
    slug: 'lilo-and-stitch',
    badge: 'GIA ĐÌNH',
    badgeColor: 'bg-emerald-600',
    rating: 7.3,
    genre: 'Phiêu lưu • Gia đình',
    posterUrl: '/images/poster_stitch.jpg',
    showtimes: ['09:00', '11:30', '14:00', '16:30', '19:00', '21:30'],
  },
  {
    id: 5,
    title: 'Mission: Impossible – The Final Reckoning',
    slug: 'mission-impossible-final-reckoning',
    badge: 'HOT',
    badgeColor: 'bg-rose-600',
    rating: 8.2,
    genre: 'Hành động • Phiêu lưu',
    posterUrl: '/images/poster_mission.jpg',
    showtimes: ['09:00', '12:15', '15:45', '18:15', '21:00'],
  },
  {
    id: 6,
    title: 'Deadpool & Wolverine',
    slug: 'deadpool-wolverine-hon-loan-da-vu-tru',
    badge: 'T18',
    badgeColor: 'bg-amber-600',
    rating: 8.3,
    genre: 'Hành động • Siêu anh hùng',
    posterUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80',
    showtimes: ['11:00', '13:45', '16:30', '19:15', '22:00'],
  },
  {
    id: 7,
    title: 'Dune: Hành Tinh Cát - Phần Hai',
    slug: 'dune-hanh-tinh-cat-phan-hai',
    badge: 'IMAX',
    badgeColor: 'bg-indigo-600',
    rating: 8.9,
    genre: 'Khoa học viễn tưởng',
    posterUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=600&q=80',
    showtimes: ['10:30', '14:00', '17:30', '21:00'],
  },
  {
    id: 8,
    title: 'Godzilla x Kong: Đế Chế Mới',
    slug: 'godzilla-x-kong-de-che-moi',
    badge: '3D',
    badgeColor: 'bg-cyan-600',
    rating: 7.8,
    genre: 'Hành động • Quái vật',
    posterUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=600&q=80',
    showtimes: ['09:45', '12:15', '15:15', '18:00', '20:45'],
  },
  {
    id: 9,
    title: 'Inside Out 2: Cảm Xúc Bùng Nổ',
    slug: 'inside-out-2-nhung-manh-ghep-cam-xuc',
    badge: 'P',
    badgeColor: 'bg-emerald-600',
    rating: 8.6,
    genre: 'Hoạt hình • Gia đình',
    posterUrl: 'https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=600&q=80',
    showtimes: ['10:00', '13:00', '15:30', '18:00', '20:30'],
  },
  {
    id: 10,
    title: 'Spider-Man: Du Hành Vũ Trụ Nhện',
    slug: 'spider-man-du-hanh-vu-tru-nhen',
    badge: 'HOT',
    badgeColor: 'bg-rose-600',
    rating: 8.7,
    genre: 'Hoạt hình • Hành động',
    posterUrl: 'https://images.unsplash.com/photo-1635805737707-575885ab0820?auto=format&fit=crop&w=600&q=80',
    showtimes: ['10:30', '13:15', '16:00', '18:45', '21:30'],
  },
]

// Box Office Rankings for Sidebar Widget
const boxOfficeRankings = [
  {
    title: 'Thanh Gươm Diệt Quỷ',
    revenue: '45.8 Tỷ VNĐ',
    change: '▲ Tăng 28%',
    tickets: '320K vé',
  },
  {
    title: 'F1: The Movie',
    revenue: '31.2 Tỷ VNĐ',
    change: '▲ Mới ra mắt',
    tickets: '215K vé',
  },
  {
    title: 'Jujutsu Kaisen 0',
    revenue: '22.6 Tỷ VNĐ',
    change: '▲ Tăng 12%',
    tickets: '160K vé',
  },
]

// 4. Events List matching Image 2
const eventsList = [
  {
    id: 101,
    title: 'CONCERT JACK - J97',
    date: '15.11.2025',
    venue: 'Sân vận động Mỹ Đình, Hà Nội',
    category: 'Âm nhạc',
    imageUrl: '/images/event_jack.jpg',
  },
  {
    id: 102,
    title: 'FAN MEETING LEE MIN HO',
    date: '28.12.2025',
    venue: 'Trung tâm Hội nghị Quốc gia, Hà Nội',
    category: 'Gặp gỡ',
    imageUrl: '/images/event_leeminho.jpg',
  },
  {
    id: 103,
    title: 'BLACKPINK WORLD TOUR',
    date: '30.11.2025',
    venue: 'Sân vận động Quốc gia, TP.HCM',
    category: 'Âm nhạc',
    imageUrl: '/images/event_blackpink.jpg',
  },
  {
    id: 104,
    title: 'HỘI CHỢ ẨM THỰC HÀN QUỐC 2025',
    date: '12.12.2025 - 14.12.2025',
    venue: 'SECC, Quận 7, TP.HCM',
    category: 'Ẩm thực',
    imageUrl: '/images/event_food.jpg',
  },
  {
    id: 105,
    title: 'TRIỂN LÃM CÔNG NGHỆ 2025',
    date: '20.11.2025 - 23.11.2025',
    venue: 'ICE, Hà Nội',
    category: 'Công nghệ',
    imageUrl: '/images/event_tech.jpg',
  },
]

// 5. Bento Genre Showcase Data
const bentoGenres = [
  {
    id: 'action',
    title: 'Hành Động & Bom Tấn',
    tagline: 'Cháy nổ mãn nhãn, kỹ xảo đỉnh cao',
    count: '1.245+ phim',
    badge: '🔥 HOT NHẤT PHÒNG VÉ',
    colSpan: 'lg:col-span-2 min-h-[220px]',
    icon: Swords,
    bgImage: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=1200&q=80',
    overlay: 'bg-gradient-to-r from-rose-950/95 via-black/75 to-rose-950/30',
    borderColor: 'hover:border-rose-500/80 hover:shadow-rose-500/25',
    iconColor: 'text-rose-400 bg-rose-500/20 border-rose-500/30',
    textColor: 'group-hover:text-rose-400',
  },
  {
    id: 'anime',
    title: 'Anime & Fantasy Nhật Bản',
    tagline: 'Thế giới huyền ảo, đồ họa đỉnh cao',
    count: '412+ phim',
    badge: '✨ XU HƯỚNG GEN Z',
    colSpan: 'lg:col-span-2 min-h-[220px]',
    icon: Sparkles,
    bgImage: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=1200&q=80',
    overlay: 'bg-gradient-to-r from-purple-950/95 via-black/75 to-indigo-950/30',
    borderColor: 'hover:border-purple-500/80 hover:shadow-purple-500/25',
    iconColor: 'text-purple-400 bg-purple-500/20 border-purple-500/30',
    textColor: 'group-hover:text-purple-400',
  },
  {
    id: 'romance',
    title: 'Tình Cảm',
    tagline: 'Cảm xúc ngọt ngào',
    count: '876+ phim',
    colSpan: 'lg:col-span-1 min-h-[190px]',
    icon: Heart,
    bgImage: 'https://images.unsplash.com/photo-1518199266791-5375a83190b7?auto=format&fit=crop&w=600&q=80',
    overlay: 'bg-gradient-to-t from-pink-950/95 via-black/70 to-transparent',
    borderColor: 'hover:border-pink-500/80 hover:shadow-pink-500/25',
    iconColor: 'text-pink-400 bg-pink-500/20 border-pink-500/30',
    textColor: 'group-hover:text-pink-400',
  },
  {
    id: 'horror',
    title: 'Kinh Dị & Bí Ẩn',
    tagline: 'Rùng rợn thót tim',
    count: '532+ phim',
    colSpan: 'lg:col-span-1 min-h-[190px]',
    icon: Ghost,
    bgImage: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=600&q=80',
    overlay: 'bg-gradient-to-t from-purple-950/95 via-black/75 to-transparent',
    borderColor: 'hover:border-purple-500/80 hover:shadow-purple-500/25',
    iconColor: 'text-purple-300 bg-purple-900/30 border-purple-700/40',
    textColor: 'group-hover:text-purple-300',
  },
  {
    id: 'comedy',
    title: 'Hài Hước',
    tagline: 'Tiếng cười rộn rã',
    count: '645+ phim',
    colSpan: 'lg:col-span-1 min-h-[190px]',
    icon: Smile,
    bgImage: 'https://images.unsplash.com/photo-1514306191717-452ec28c7814?auto=format&fit=crop&w=600&q=80',
    overlay: 'bg-gradient-to-t from-amber-950/95 via-black/70 to-transparent',
    borderColor: 'hover:border-amber-500/80 hover:shadow-amber-500/25',
    iconColor: 'text-amber-400 bg-amber-500/20 border-amber-500/30',
    textColor: 'group-hover:text-amber-400',
  },
  {
    id: 'speed',
    title: 'Tốc Độ & F1',
    tagline: 'Nghẹt thở từng giây',
    count: '156+ phim',
    colSpan: 'lg:col-span-1 min-h-[190px]',
    icon: Trophy,
    bgImage: '/images/poster_f1.jpg',
    overlay: 'bg-gradient-to-t from-orange-950/95 via-black/70 to-transparent',
    borderColor: 'hover:border-orange-500/80 hover:shadow-orange-500/25',
    iconColor: 'text-orange-400 bg-orange-500/20 border-orange-500/30',
    textColor: 'group-hover:text-orange-400',
  },
  {
    id: 'scifi',
    title: 'Viễn Tưởng & Phiêu Lưu',
    tagline: 'Khám phá dải ngân hà kỳ bí và tương lai công nghệ vượt bậc',
    count: '368+ phim',
    badge: '🚀 DU HÀNH VŨ TRỤ',
    colSpan: 'lg:col-span-2 min-h-[210px]',
    icon: Compass,
    bgImage: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=1200&q=80',
    overlay: 'bg-gradient-to-r from-blue-950/95 via-black/75 to-sky-950/30',
    borderColor: 'hover:border-blue-500/80 hover:shadow-blue-500/25',
    iconColor: 'text-blue-400 bg-blue-500/20 border-blue-500/30',
    textColor: 'group-hover:text-blue-400',
  },
  {
    id: 'family',
    title: 'Gia Đình & Hoạt Hình 3D',
    tagline: 'Những câu chuyện ấm áp, nhân văn dành trọn vẹn cho cả gia đình',
    count: '287+ phim',
    badge: '🎈 PHÙ HỢP MỌI LỨA TUỔI',
    colSpan: 'lg:col-span-2 min-h-[210px]',
    icon: Home,
    bgImage: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=1200&q=80',
    overlay: 'bg-gradient-to-r from-teal-950/95 via-black/75 to-emerald-950/30',
    borderColor: 'hover:border-teal-500/80 hover:shadow-teal-500/25',
    iconColor: 'text-teal-400 bg-teal-500/20 border-teal-500/30',
    textColor: 'group-hover:text-teal-400',
  },
]

// 6. Editorial News & CineBlog Data
const featuredNews = {
  id: 1,
  title: 'Kỷ lục phòng vé 2025: Siêu phẩm Vô Hạn Thành và làn sóng rạp chiếu công nghệ IMAX Laser bùng nổ',
  excerpt:
    'Phân tích chi tiết mức tăng trưởng doanh thu kỷ lục, sự lên ngôi của công nghệ màn chiếu cong 12 kênh âm thanh Dolby Atmos và xu hướng khán giả Việt Nam săn đón trải nghiệm điện ảnh chuẩn quốc tế.',
  date: '22.10.2025',
  author: 'Ban Biên Tập CineVerse',
  readTime: '4 phút đọc',
  views: '28.4K',
  badge: 'TIÊU ĐIỂM ĐIỆN ẢNH',
  tag: 'Phim Chiếu Rạp',
  imageUrl: '/images/hero_demon_slayer_bg.jpg',
}

const trendingNewsList = [
  {
    id: 2,
    title: 'Brad Pitt đích thân lái siêu xe 300km/h trên đường đua Silverstone trong F1: The Movie',
    summary:
      'Ghi hình trực tiếp tại giải Grand Prix danh tiếng với công nghệ camera 6K siêu nhỏ gắn ngay trong buồng lái, mang lại trải nghiệm tốc độ nghẹt thở không dùng kỹ xảo CGI.',
    date: '20.10.2025',
    readTime: '3 phút đọc',
    views: '16.8K',
    source: 'Apple Studios / Warner Bros.',
    tag: 'HẬU TRƯỜNG F1',
    tagColor: 'text-amber-400 bg-amber-500/15 border-amber-500/30',
    imageUrl: '/images/poster_f1.jpg',
  },
  {
    id: 3,
    title: 'Blackpink xác nhận world tour 2025 với 4 đêm concert cuồng nhiệt tại SVĐ Mỹ Đình & TP.HCM',
    summary:
      'Cơn sốt âm nhạc bùng nổ trở lại với hệ thống âm thanh vòm đỉnh cao và dàn hiệu ứng ánh sáng quy mô thế giới, sẵn sàng phục vụ hơn 120.000 khán giả Việt Nam.',
    date: '18.10.2025',
    readTime: '2 phút đọc',
    views: '34.2K',
    source: 'YG Entertainment',
    tag: 'CONCERT LIVE',
    tagColor: 'text-rose-400 bg-rose-500/15 border-rose-500/30',
    imageUrl: '/images/event_blackpink.jpg',
  },
  {
    id: 4,
    title: 'CineVerse Rewards: Ra mắt combo bắp nước phiên bản giới hạn kỷ niệm đại lễ điện ảnh',
    summary:
      'Sở hữu xô bắp caramel phô mai thượng hạng cùng bình giữ nhiệt đổi màu độc quyền, tặng kèm voucher giảm 50% vé xem phim khi thanh toán qua ví đối tác.',
    date: '15.10.2025',
    readTime: '2 phút đọc',
    views: '11.5K',
    source: 'CineVerse F&B Special',
    tag: 'ƯU ĐÃI ĐẶC QUYỀN',
    tagColor: 'text-blue-400 bg-blue-500/15 border-blue-500/30',
    imageUrl: '/images/promo_popcorn.jpg',
  },
]

// Trailer Modal
const trailerOpen = ref(false)
const trailerUrl = ref('')
const trailerTitle = ref('')

const openTrailerModal = (url: string, title: string) => {
  let embed = url
  if (embed.includes('watch?v=')) embed = embed.replace('watch?v=', 'embed/')
  if (!embed.includes('autoplay=')) embed += (embed.includes('?') ? '&' : '?') + 'autoplay=1'
  trailerUrl.value = embed
  trailerTitle.value = title
  trailerOpen.value = true
}

// 7. Cinema Complexes Showcase
const selectedCity = ref('all')
const cinemaComplexes = [
  {
    id: 1,
    name: 'CineVerse Megamall Landmark 81',
    city: 'hcm',
    cityName: 'TP. Hồ Chí Minh',
    address: 'Tầng B1, Vincom Landmark 81, 720A Điện Biên Phủ, P.22, Q. Bình Thạnh',
    formats: ['IMAX Laser', 'Dolby Atmos', 'Gold Class'],
    rating: '4.9',
    screens: 8,
    image: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 2,
    name: 'CineVerse Vincom Center Bà Triệu',
    city: 'hn',
    cityName: 'Hà Nội',
    address: 'Tầng 6, Vincom Center, 191 Bà Triệu, Q. Hai Bà Trưng',
    formats: ['Gold Class', 'Sweetbox VIP', 'Laser 4K'],
    rating: '4.8',
    screens: 10,
    image: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 3,
    name: 'CineVerse Gigamall Phạm Văn Đồng',
    city: 'hcm',
    cityName: 'TP. Hồ Chí Minh',
    address: 'Tầng 6, TTTM Gigamall, 240-242 Phạm Văn Đồng, TP. Thủ Đức',
    formats: ['ScreenX 270°', '4DX Motion', 'Dolby Atmos'],
    rating: '4.8',
    screens: 12,
    image: 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 4,
    name: 'CineVerse Lotte Mall Tây Hồ',
    city: 'hn',
    cityName: 'Hà Nội',
    address: 'Tầng 4, Lotte Mall West Lake, 272 Võ Chí Công, Q. Tây Hồ',
    formats: ['Ultimate Laser', 'CineBed', 'Lounge Bar'],
    rating: '4.9',
    screens: 9,
    image: 'https://images.unsplash.com/photo-1512149177596-f817c7ef5d4c?auto=format&fit=crop&w=500&q=80',
  },
  {
    id: 5,
    name: 'CineVerse Riverside Đà Nẵng',
    city: 'dn',
    cityName: 'Đà Nẵng',
    address: 'Đường 2/9, P. Bình Hiên, Q. Hải Châu',
    formats: ['Laser Projection', 'Dolby 7.1', 'Sweetbox'],
    rating: '4.7',
    screens: 6,
    image: 'https://images.unsplash.com/photo-1485846234645-a62644f84728?auto=format&fit=crop&w=500&q=80',
  },
]

const filteredComplexes = computed(() => {
  if (selectedCity.value === 'all') return cinemaComplexes
  return cinemaComplexes.filter((c) => c.city === selectedCity.value)
})

// 8. Cinema Formats
const cinemaFormats = [
  {
    name: 'IMAX with Laser',
    tagline: 'Đỉnh cao nghe nhìn thế hệ mới',
    desc: 'Hệ thống máy chiếu Laser 4K kép độc quyền kết hợp màn hình cong siêu lớn và dàn âm thanh 12 kênh sống động.',
    badge: 'Flagship',
    color: 'border-blue-500/40 text-blue-400 bg-blue-500/10',
  },
  {
    name: 'ScreenX 270°',
    tagline: 'Điện ảnh không giới hạn góc nhìn',
    desc: 'Phóng tầm mắt vượt ra khỏi khung hình truyền thống với công nghệ chiếu đa diện bao trọn cả 2 bức tường hai bên rạp.',
    badge: 'Đột phá',
    color: 'border-indigo-500/40 text-indigo-400 bg-indigo-500/10',
  },
  {
    name: '4DX Motion & Effects',
    tagline: 'Cảm nhận từng khoảnh khắc phim',
    desc: 'Ghế chuyển động đa chiều chân thực hòa quyện cùng hơn 21 hiệu ứng gió, mưa, sương khói, chớp sáng và hương thơm.',
    badge: 'Chân thực',
    color: 'border-rose-500/40 text-rose-400 bg-rose-500/10',
  },
  {
    name: 'Gold Class & CineBed',
    tagline: 'Không gian thượng lưu riêng tư',
    desc: 'Phục vụ ẩm thực cao cấp tại chỗ, ghế sofa bọc da ngả lưng 180 độ cùng chăn ấm gối êm chuẩn phòng khách sạn 5 sao.',
    badge: 'VIP Luxury',
    color: 'border-amber-500/40 text-amber-400 bg-amber-500/10',
  },
]

// 9. Payment & Tech Partners
const partners = [
  { name: 'IMAX Laser', type: 'Công nghệ màn chiếu', tag: 'IMAX' },
  { name: 'Dolby Atmos', type: 'Âm thanh không gian', tag: 'DOLBY' },
  { name: 'ScreenX 270°', type: 'Đa diện vòm rạp', tag: 'SCREENX' },
  { name: 'RealD 3D', type: 'Chuẩn 3D sắc nét', tag: 'REALD' },
  { name: 'MoMo', type: 'Ví điện tử liên kết', tag: 'MOMO' },
  { name: 'ZaloPay', type: 'Thanh toán tức thì', tag: 'ZALOPAY' },
  { name: 'VNPay QR', type: 'Quét mã ngân hàng', tag: 'VNPAY' },
  { name: 'Apple Pay', type: 'Chạm thanh toán bảo mật', tag: 'APPLE PAY' },
  { name: 'Visa / Master', type: 'Thẻ quốc tế', tag: 'VISA' },
]

// 10. Frequently Asked Questions
const activeFaq = ref<number | null>(0)
const toggleFaq = (idx: number) => {
  activeFaq.value = activeFaq.value === idx ? null : idx
}

const faqs = [
  {
    q: 'Làm thế nào để vào rạp mà không cần in vé giấy?',
    a: 'Khi đặt vé thành công trên CineVerse, bạn sẽ nhận được mã vé điện tử kèm mã QR trực tiếp trên ứng dụng và qua Email/SMS. Khi đến rạp, bạn chỉ cần đưa màn hình điện thoại có mã QR cho nhân viên soát vé quét là có thể vào thẳng phòng chiếu.',
  },
  {
    q: 'Chính sách hoàn hoặc đổi vé được quy định như thế nào?',
    a: 'Đối với vé xem phim, bạn có thể thực hiện đổi hoặc hủy vé trước giờ chiếu ít nhất 60 phút theo chính sách của cụm rạp liên kết. Số tiền sẽ được hoàn về Ví CineVerse hoặc tài khoản thanh toán ban đầu. Với vé Concert/Live Show đặc biệt, chính sách sẽ theo từng Ban Tổ Chức.',
  },
  {
    q: 'Quy định phân loại độ tuổi xem phim (P, K, T13, T16, T18) tại rạp?',
    a: 'Phim được phân loại theo chuẩn Cục Điện ảnh: P (Phổ biến mọi lứa tuổi), K (Dưới 13 tuổi xem cùng người giám hộ), T13 (Khán giả từ đủ 13 tuổi trở lên), T16 (Từ đủ 16 tuổi trở lên) và T18 (Từ đủ 18 tuổi trở lên). Vui lòng mang theo giấy tờ tùy thân hợp lệ khi đến rạp.',
  },
  {
    q: 'Quyền lợi hội viên CineVerse Rewards và cách tích lũy điểm thưởng?',
    a: 'Mỗi giao dịch mua vé phim, bắp nước hoặc vé sự kiện đều được tích lũy điểm thưởng tương đương 5% - 10% giá trị hóa đơn. Điểm có thể dùng để đổi vé xem phim miễn phí, nâng hạng phòng chiếu IMAX/Gold Class hoặc nhận quà tặng sinh nhật bất ngờ.',
  },
]
</script>

<template>
  <div class="min-h-screen bg-[#070A12] text-slate-100 selection:bg-blue-600 selection:text-white space-y-12 sm:space-y-16 pb-20">
    <!-- 1. HERO SPOTLIGHT CAROUSEL & TOP 4 BLOCKBUSTERS -->
    <section
      class="relative overflow-hidden border-b border-slate-800 bg-slate-950 py-8 lg:py-12 transition-all duration-700"
      @mouseenter="isHeroPaused = true"
      @mouseleave="isHeroPaused = false"
    >
      <!-- Background Backdrop with Dark Gradient Overlays -->
      <div class="absolute inset-0 z-0 overflow-hidden">
        <img
          :key="currentHero.id"
          :src="currentHero.backdropUrl || currentHero.posterUrl"
          :alt="currentHero.title"
          class="w-full h-full object-cover object-center opacity-30 filter blur-xs scale-105 transform duration-1000"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-[#070A12] via-[#070A12]/90 to-transparent"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-[#070A12] via-[#070A12]/50 to-transparent"></div>
      </div>

      <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
        <!-- Main Spotlight Row -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Left Content (lg:col-span-8) -->
          <div class="lg:col-span-8 space-y-4">
            <div class="flex flex-wrap items-center gap-3">
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/20 border border-rose-500/30 text-rose-400 text-xs font-black tracking-wide uppercase">
                <Flame class="w-3.5 h-3.5 text-rose-400 animate-pulse" />
                <span>TOP #{{ currentHeroIndex + 1 }} BOM TẤN CHIẾU RẠP TÂM ĐIỂM</span>
              </div>

              <!-- Mini Carousel Arrow Navigation -->
              <div class="flex items-center gap-1.5 ml-auto sm:ml-0 bg-slate-900/90 border border-slate-800 rounded-lg p-1">
                <button
                  type="button"
                  @click="prevHero"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Phim trước"
                >
                  <ChevronLeft class="w-4 h-4" />
                </button>
                <span class="text-xs font-mono font-bold px-1.5 text-slate-400">
                  {{ currentHeroIndex + 1 }} / {{ heroMovies.length }}
                </span>
                <button
                  type="button"
                  @click="nextHero"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Phim tiếp theo"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- Main Title -->
            <h1 class="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white leading-tight">
              {{ currentHero.title }}
            </h1>

            <!-- Meta Tags -->
            <div class="flex flex-wrap items-center gap-3 text-xs sm:text-sm font-semibold text-slate-300">
              <span
                class="px-2 py-0.5 rounded font-black border text-xs"
                :class="getRatingColor(currentHero.ageRating)"
              >
                {{ currentHero.ageRating }}
              </span>
              <span class="flex items-center gap-1 text-amber-400 font-bold">
                <Star class="w-4 h-4 fill-amber-400" />
                {{ currentHero.rating }}
              </span>
              <span>&bull;</span>
              <span class="flex items-center gap-1">
                <Clock class="w-4 h-4 text-slate-400" />
                {{ currentHero.duration }}
              </span>
              <span>&bull;</span>
              <span class="text-rose-400 font-bold">{{ currentHero.genre }}</span>
              <span v-if="currentHero.formats" class="px-2 py-0.5 rounded bg-indigo-900/60 border border-indigo-500/40 text-indigo-300 text-xs uppercase font-extrabold">
                {{ currentHero.formats }}
              </span>
            </div>

            <!-- Synopsis Paragraph -->
            <p class="text-sm sm:text-base text-slate-300 line-clamp-2 max-w-2xl leading-relaxed">
              {{ currentHero.synopsis }}
            </p>

            <div class="text-xs text-slate-400 space-y-1">
              <p><strong class="text-slate-300">Đạo diễn:</strong> {{ currentHero.director }}</p>
              <p><strong class="text-slate-300">Diễn viên chính:</strong> {{ currentHero.actors }}</p>
            </div>

            <!-- CTA Buttons -->
            <div class="flex flex-wrap items-center gap-3 pt-2">
              <router-link
                :to="`/movies/${currentHero.slug || currentHero.id}`"
                class="px-6 py-3 rounded-xl bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white font-extrabold text-sm sm:text-base shadow-lg shadow-rose-600/30 transition flex items-center gap-2 group cursor-pointer hover:scale-105 active:scale-95"
              >
                <Ticket class="w-5 h-5 group-hover:rotate-12 transition-transform" />
                <span>Đặt Vé Suất Chiếu Ngay</span>
              </router-link>

              <button
                type="button"
                @click="openTrailerModal(currentHero.trailerUrl, currentHero.title)"
                class="px-5 py-3 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 text-slate-200 font-bold text-sm transition flex items-center gap-2 cursor-pointer hover:border-slate-500"
              >
                <Play class="w-4 h-4 text-rose-400 fill-rose-400" />
                <span>Xem Trailer Full HD</span>
              </button>
            </div>
          </div>

          <!-- Featured Poster Card (lg:col-span-4) -->
          <div class="hidden lg:flex lg:col-span-4 justify-end">
            <div class="relative group cursor-pointer" @click="router.push(`/movies/${currentHero.slug || currentHero.id}`)">
              <div class="absolute -inset-1 rounded-2xl bg-gradient-to-r from-rose-600 to-indigo-600 opacity-40 blur group-hover:opacity-75 transition duration-500"></div>
              <img
                :src="currentHero.posterUrl"
                :alt="currentHero.title"
                class="relative w-64 lg:w-72 rounded-2xl shadow-2xl object-cover aspect-[2/3] border border-slate-800 transform group-hover:scale-[1.02] transition duration-300"
              />
              <div class="absolute top-3 right-3 px-2 py-1 rounded bg-black/80 backdrop-blur-xs text-[10px] font-black uppercase text-rose-400 border border-rose-500/30">
                {{ currentHero.formats?.split(',')[0] || 'IMAX Laser' }}
              </div>
              <div class="absolute bottom-3 left-3 right-3 px-3 py-1.5 rounded-lg bg-slate-950/85 backdrop-blur-xs text-xs font-bold text-slate-200 border border-slate-800 flex items-center justify-between">
                <span>Top #{{ currentHeroIndex + 1 }} Thịnh Hành</span>
                <span class="text-rose-400 text-[11px] font-semibold">Xem chi tiết &rarr;</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Top 4 Movies Selector Strip -->
        <div class="mt-8 pt-6 border-t border-slate-800/80">
          <div class="flex items-center justify-between mb-3.5">
            <div class="flex items-center gap-2">
              <Trophy class="w-4 h-4 text-amber-400" />
              <span class="text-xs uppercase font-extrabold tracking-wider text-slate-300">
                Top {{ heroMovies.length }} Bom Tấn Dẫn Đầu Phòng Vé - Bấm để chọn xem
              </span>
            </div>
            <div class="hidden sm:flex items-center gap-2 text-xs text-slate-400 font-medium">
              <span class="inline-block w-2 h-2 rounded-full bg-rose-500 animate-pulse"></span>
              <span>Tự động chuyển tiếp sau 6s</span>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div
              v-for="(movie, idx) in heroMovies"
              :key="movie.id"
              @click="currentHeroIndex = idx"
              class="group relative flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-all duration-300 border backdrop-blur-xs"
              :class="[
                currentHeroIndex === idx
                  ? 'bg-slate-900/90 border-rose-500 shadow-lg shadow-rose-500/25 ring-1 ring-rose-500/50 -translate-y-1'
                  : 'bg-slate-950/70 border-slate-800 hover:bg-slate-900/70 hover:border-slate-700'
              ]"
            >
              <!-- Rank Number Badge -->
              <div
                class="flex items-center justify-center w-8 h-8 rounded-lg text-xs font-black shrink-0 shadow-sm"
                :class="[
                  idx === 0 ? 'bg-gradient-to-br from-amber-400 to-amber-600 text-slate-950 ring-1 ring-amber-300' :
                  idx === 1 ? 'bg-gradient-to-br from-slate-200 to-slate-400 text-slate-950' :
                  idx === 2 ? 'bg-gradient-to-br from-amber-700 to-amber-900 text-white' :
                  'bg-gradient-to-br from-indigo-600 to-blue-700 text-white'
                ]"
              >
                #{{ idx + 1 }}
              </div>

              <!-- Poster Thumbnail -->
              <img
                :src="movie.posterUrl"
                :alt="movie.title"
                class="w-12 h-16 rounded-md object-cover shrink-0 border border-slate-800 group-hover:scale-105 transition-transform"
              />

              <!-- Info -->
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between text-[11px] mb-0.5">
                  <span class="flex items-center gap-1 text-amber-400 font-bold">
                    <Star class="w-3 h-3 fill-amber-400 shrink-0" />
                    {{ movie.rating }}
                  </span>
                  <span
                    v-if="currentHeroIndex === idx"
                    class="px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-400 text-[9px] font-black uppercase tracking-wider"
                  >
                    Đang xem
                  </span>
                </div>
                <h4 class="text-xs font-bold text-white truncate group-hover:text-rose-400 transition-colors">
                  {{ movie.title }}
                </h4>
                <p class="text-[10px] text-slate-400 truncate mt-0.5">
                  {{ movie.formats?.split(',')[0] || movie.genre }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. VALUE PROPOSITION (5 CARDS ROW) -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 sm:gap-4">
        <div
          v-for="item in valueProps"
          :key="item.title"
          class="p-4 rounded-2xl bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/50 flex items-center gap-3 transition group"
        >
          <div class="w-10 h-10 rounded-xl bg-blue-600/10 border border-blue-500/20 text-blue-400 flex items-center justify-center shrink-0 group-hover:scale-105 transition-transform">
            <component :is="item.icon" class="w-5 h-5" />
          </div>
          <div class="min-w-0">
            <h4 class="font-bold text-xs sm:text-sm text-white truncate">{{ item.title }}</h4>
            <p class="text-[11px] text-slate-400 truncate">{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. SECTION: NHỮNG BỘ PHIM HOT NHẤT HIỆN NAY -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      <div class="flex items-center justify-between border-b border-slate-800/80 pb-4">
        <div>
          <div class="flex items-center gap-2 text-blue-400 text-xs font-black uppercase tracking-wider mb-1">
            <span>🎬 PHIM ĐANG CHIẾU</span>
          </div>
          <h2 class="text-xl sm:text-2xl font-black text-white">Những bộ phim hot nhất hiện nay</h2>
        </div>
        <router-link to="/cinema" class="text-xs font-bold text-blue-400 hover:text-blue-300 flex items-center gap-1 group">
          <span>Xem tất cả</span>
          <ChevronRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
        </router-link>
      </div>

      <!-- Main Layout: 5 Movies Grid (Left) + Right Promotional Sidebar -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <!-- 5 Movie Cards Grid (Left 9 cols) -->
        <div class="lg:col-span-9 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-4">
          <div
            v-for="movie in moviesList"
            :key="movie.id"
            class="group rounded-2xl bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/50 flex flex-col justify-between overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-blue-950/30"
          >
            <!-- Poster -->
            <div class="relative aspect-[2/3] overflow-hidden bg-slate-950">
              <img
                :src="movie.posterUrl"
                :alt="movie.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <!-- Badge (HOT / MỚI) -->
              <span
                v-if="movie.badge"
                class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded text-[10px] font-black text-white uppercase shadow"
                :class="movie.badgeColor"
              >
                {{ movie.badge }}
              </span>
            </div>

            <!-- Info & Showtimes -->
            <div class="p-3 flex-1 flex flex-col justify-between space-y-2.5">
              <div>
                <h3
                  @click="router.push(`/movies/${movie.slug || movie.id}`)"
                  class="font-extrabold text-xs text-white group-hover:text-blue-400 transition-colors line-clamp-1 cursor-pointer"
                  :title="movie.title"
                >
                  {{ movie.title }}
                </h3>
                <div class="flex items-center gap-1.5 text-[10px] text-slate-400 mt-1">
                  <span class="flex items-center gap-0.5 text-amber-400 font-bold">
                    <Star class="w-3 h-3 fill-amber-400" />
                    {{ movie.rating }}
                  </span>
                  <span>&bull;</span>
                  <span class="truncate">{{ movie.genre }}</span>
                </div>

                <!-- Showtimes Chips -->
                <div class="mt-2 space-y-1">
                  <span class="text-[9px] font-bold text-slate-500 uppercase block">Suất chiếu:</span>
                  <div class="flex flex-wrap items-center gap-1">
                    <span
                      v-for="st in movie.showtimes.slice(0, 3)"
                      :key="st"
                      class="px-1.5 py-0.5 rounded bg-slate-900 border border-slate-800 text-[9px] font-mono font-bold text-slate-300"
                    >
                      {{ st }}
                    </span>
                    <span
                      v-if="movie.showtimes.length > 3"
                      class="px-1 py-0.5 rounded bg-slate-800/80 text-[8px] font-mono font-bold text-slate-400"
                    >
                      +{{ movie.showtimes.length - 3 }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- Buy Ticket Button -->
              <router-link
                :to="`/movies/${movie.slug || movie.id}`"
                class="w-full py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition flex items-center justify-center text-center shadow-md shadow-blue-600/30 cursor-pointer"
              >
                Mua vé
              </router-link>
            </div>
          </div>
        </div>

        <!-- Right Sidebar Promotional Widgets (3 cols) -->
        <div class="lg:col-span-3 flex flex-col justify-between gap-4">
          <!-- Widget 1: Thành viên CineVerse VIP -->
          <div class="rounded-2xl bg-gradient-to-br from-[#101827] via-[#0E1524] to-[#151D30] border border-[#1E293D] shadow-xl overflow-hidden group hover:border-blue-500/40 transition">
            <div class="relative aspect-[16/9] overflow-hidden bg-slate-950 border-b border-slate-800/80">
              <img
                src="/images/promo_member.jpg"
                alt="Thành viên CineVerse VIP"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-[#0E1524] via-transparent to-transparent"></div>
              <span class="absolute top-2.5 right-2.5 px-2 py-0.5 rounded bg-black/80 backdrop-blur-md text-[10px] font-black text-amber-400 border border-amber-500/30">
                VIP CLUB
              </span>
            </div>

            <div class="p-4 space-y-3">
              <div>
                <h4 class="font-extrabold text-sm text-white">Thành viên CineVerse VIP</h4>
                <p class="text-[11px] text-slate-400 mt-0.5">Tận hưởng đặc quyền dành riêng cho bạn</p>
              </div>

              <ul class="space-y-1.5 text-xs text-slate-300">
                <li class="flex items-center gap-2">
                  <span class="w-4 h-4 rounded-full bg-amber-500/20 text-amber-400 flex items-center justify-center text-[10px]">⭐</span>
                  <span class="text-[11px]">Tích lũy 10% điểm đổi vé & quà tặng</span>
                </li>
                <li class="flex items-center gap-2">
                  <span class="w-4 h-4 rounded-full bg-blue-500/20 text-blue-400 flex items-center justify-center text-[10px]">🎫</span>
                  <span class="text-[11px]">Giảm giá vé lên đến 50% ngày hội viên</span>
                </li>
                <li class="flex items-center gap-2">
                  <span class="w-4 h-4 rounded-full bg-indigo-500/20 text-indigo-400 flex items-center justify-center text-[10px]">🎟️</span>
                  <span class="text-[11px]">Ưu tiên chọn ghế VIP & suất chiếu sớm</span>
                </li>
              </ul>

              <router-link
                to="/register"
                class="w-full py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition flex items-center justify-center gap-1.5 shadow-md shadow-blue-600/30 cursor-pointer"
              >
                <span>Đăng ký hội viên ngay</span>
                <ArrowRight class="w-3.5 h-3.5" />
              </router-link>
            </div>
          </div>

          <!-- Widget 2: Ưu đãi đặc biệt giảm 50% bắp nước -->
          <div class="p-4 rounded-2xl bg-gradient-to-br from-rose-950/40 via-[#131926] to-[#0E1524] border border-rose-900/40 shadow-xl flex items-center justify-between gap-3 group hover:border-rose-500/50 transition">
            <div class="space-y-2 flex-1 min-w-0">
              <span class="px-2 py-0.5 rounded text-[9px] font-black uppercase bg-rose-600/30 text-rose-400 border border-rose-500/40 inline-block">
                ƯU ĐÃI ĐẶC BIỆT
              </span>
              <div>
                <h4 class="font-black text-sm text-white leading-tight">GIẢM 50% VÉ XEM PHIM</h4>
                <p class="text-[10px] text-slate-400 mt-0.5">Dành cho thành viên mới đặt vé hôm nay</p>
              </div>
              <button
                type="button"
                @click="router.push('/cinema')"
                class="px-3.5 py-1.5 rounded-lg bg-white text-slate-950 font-black text-xs hover:bg-slate-200 transition shadow cursor-pointer inline-flex items-center gap-1"
              >
                <span>Nhận ngay</span>
                <ArrowRight class="w-3 h-3" />
              </button>
            </div>
            <div class="w-20 h-20 sm:w-22 sm:h-22 shrink-0 rounded-xl overflow-hidden shadow-xl border border-rose-900/50 bg-slate-950">
              <img
                src="/images/promo_popcorn.jpg"
                alt="Combo Bắp Nước"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
            </div>
          </div>

          <!-- Widget 3: Top Doanh Thu Phòng Vé Tuần -->
          <div class="p-4 rounded-2xl bg-gradient-to-br from-[#101827] via-[#0E1524] to-[#151D30] border border-[#1E293D] shadow-xl space-y-2.5 group hover:border-amber-500/40 transition">
            <div class="flex items-center justify-between pb-2 border-b border-slate-800/80">
              <div class="flex items-center gap-1.5">
                <Trophy class="w-4 h-4 text-amber-400" />
                <h4 class="font-extrabold text-xs text-white uppercase tracking-wider">Top Doanh Thu Tuần</h4>
              </div>
              <span class="text-[9px] font-mono font-bold text-amber-400/90 bg-amber-500/10 px-1.5 py-0.5 rounded border border-amber-500/20">
                Tuần 42
              </span>
            </div>

            <div class="space-y-1.5">
              <div
                v-for="(item, rIdx) in boxOfficeRankings"
                :key="item.title"
                class="flex items-center gap-2 p-1.5 rounded-lg bg-slate-900/60 border border-slate-800/60 hover:border-slate-700 transition"
              >
                <span
                  class="w-5 h-5 rounded flex items-center justify-center text-[10px] font-black shrink-0 shadow"
                  :class="[
                    rIdx === 0
                      ? 'bg-amber-500 text-slate-950 font-black'
                      : rIdx === 1
                      ? 'bg-slate-300 text-slate-950 font-bold'
                      : 'bg-amber-700/80 text-white font-bold'
                  ]"
                >
                  {{ rIdx + 1 }}
                </span>
                <div class="min-w-0 flex-1">
                  <h5 class="text-xs font-bold text-white truncate">{{ item.title }}</h5>
                  <p class="text-[9px] text-slate-400">{{ item.revenue }} • {{ item.change }}</p>
                </div>
                <span class="text-[9px] font-mono font-bold text-emerald-400 shrink-0">
                  {{ item.tickets }}
                </span>
              </div>
            </div>

            <router-link
              to="/cinema"
              class="w-full py-1.5 rounded-lg bg-slate-900/90 hover:bg-slate-800 border border-slate-800 text-[10px] font-bold text-slate-300 hover:text-white transition flex items-center justify-center gap-1"
            >
              <span>Xem chi tiết bảng xếp hạng</span>
              <ChevronRight class="w-3 h-3 text-slate-400" />
            </router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. SECTION: SỰ KIỆN NỔI BẬT (LIVE EVENTS 5 IN A ROW) -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      <div class="flex items-center justify-between border-b border-slate-800/80 pb-4">
        <div>
          <div class="flex items-center gap-2 text-blue-400 text-xs font-black uppercase tracking-wider mb-1">
            <span>🎉 SỰ KIỆN NỔI BẬT</span>
          </div>
          <h2 class="text-xl sm:text-2xl font-black text-white">Đừng bỏ lỡ những sự kiện hấp dẫn nhất!</h2>
        </div>
        <router-link to="/events" class="text-xs font-bold text-blue-400 hover:text-blue-300 flex items-center gap-1 group">
          <span>Xem tất cả</span>
          <ChevronRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
        </router-link>
      </div>

      <!-- 5 Event Cards Grid in a row -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <div
          v-for="evt in eventsList"
          :key="evt.id"
          class="group rounded-2xl bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/50 flex flex-col justify-between overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-blue-950/30"
        >
          <!-- Thumbnail -->
          <div class="relative aspect-video overflow-hidden bg-slate-950">
            <img
              :src="evt.imageUrl"
              :alt="evt.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-[#0D121F] via-transparent to-transparent"></div>
          </div>

          <!-- Body -->
          <div class="p-3.5 flex-1 flex flex-col justify-between space-y-3">
            <div class="space-y-1.5">
              <h3 class="font-extrabold text-xs sm:text-sm text-white group-hover:text-blue-400 transition-colors line-clamp-1">
                {{ evt.title }}
              </h3>
              <p class="text-[11px] text-slate-400 flex items-center gap-1">
                <Calendar class="w-3.5 h-3.5 text-blue-400 shrink-0" />
                <span>{{ evt.date }}</span>
              </p>
              <p class="text-[11px] text-slate-400 flex items-center gap-1 truncate">
                <MapPin class="w-3.5 h-3.5 text-rose-400 shrink-0" />
                <span class="truncate">{{ evt.venue }}</span>
              </p>
            </div>

            <!-- Footer: Tag + Button -->
            <div class="flex items-center justify-between gap-2 pt-2 border-t border-slate-800">
              <span class="px-2 py-0.5 rounded bg-slate-900 border border-slate-800 text-[10px] font-bold text-slate-300">
                {{ evt.category }}
              </span>
              <router-link
                :to="`/events/${evt.id}`"
                class="px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-500 text-white font-bold text-[11px] transition shadow-xs"
              >
                Đặt vé ngay
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. SECTION: BENTO SHOWCASE KHÁM PHÁ THEO THỂ LOẠI -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div>
          <div class="flex items-center gap-2 text-rose-500 text-xs font-black uppercase tracking-wider mb-1">
            <Flame class="w-3.5 h-3.5 animate-pulse" />
            <span>VŨ TRỤ ĐIỆN ẢNH ĐA DẠNG</span>
          </div>
          <h2 class="text-2xl sm:text-3xl font-black text-white tracking-tight">
            Khám phá theo thể loại phim
          </h2>
          <p class="text-xs sm:text-sm text-slate-400 mt-1">
            Lựa chọn phong cách điện ảnh yêu thích để đắm chìm vào những trải nghiệm cảm xúc đỉnh cao
          </p>
        </div>
        <router-link to="/cinema" class="text-xs font-extrabold text-blue-400 hover:text-blue-300 flex items-center gap-1 group">
          <span>Xem tất cả danh mục</span>
          <ChevronRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
        </router-link>
      </div>

      <!-- Bento Grid: 4 columns on large screens -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
        <div
          v-for="item in bentoGenres"
          :key="item.id"
          @click="router.push('/cinema')"
          class="group relative rounded-3xl overflow-hidden border border-slate-800/80 p-5 sm:p-6 flex flex-col justify-between cursor-pointer transition-all duration-500 shadow-xl hover:-translate-y-1.5"
          :class="[item.colSpan, item.borderColor]"
        >
          <!-- Background Image with Zoom on hover -->
          <img
            :src="item.bgImage"
            :alt="item.title"
            class="absolute inset-0 w-full h-full object-cover object-center group-hover:scale-110 transition-transform duration-700 ease-out z-0 filter brightness-95"
          />

          <!-- Colored Atmosphere Gradient Overlay -->
          <div class="absolute inset-0 z-0 transition-opacity duration-500" :class="item.overlay"></div>
          <div class="absolute inset-0 bg-black/30 group-hover:bg-black/10 transition-colors z-0"></div>

          <!-- Top Header inside Card -->
          <div class="relative z-10 flex items-center justify-between gap-3">
            <div
              class="w-11 h-11 rounded-2xl flex items-center justify-center shrink-0 backdrop-blur-md border shadow-lg group-hover:scale-110 transition-transform duration-300"
              :class="item.iconColor"
            >
              <component :is="item.icon" class="w-5 h-5" />
            </div>

            <span
              v-if="item.badge"
              class="px-2.5 py-1 rounded-full bg-black/60 backdrop-blur-md border border-white/15 text-[10px] font-black uppercase text-amber-300 tracking-wider shadow"
            >
              {{ item.badge }}
            </span>
            <span
              v-else
              class="px-2.5 py-1 rounded-full bg-black/50 backdrop-blur-md border border-white/10 text-[10px] font-bold text-slate-300 tracking-wide"
            >
              {{ item.count }}
            </span>
          </div>

          <!-- Bottom Content inside Card -->
          <div class="relative z-10 space-y-1.5 pt-6">
            <div class="flex items-end justify-between gap-2">
              <div>
                <h3
                  class="text-lg sm:text-xl font-black text-white transition-colors duration-300 drop-shadow-md"
                  :class="item.textColor"
                >
                  {{ item.title }}
                </h3>
                <p class="text-xs text-slate-300 font-medium line-clamp-1 mt-0.5 drop-shadow">
                  {{ item.tagline }}
                </p>
              </div>

              <!-- Floating Arrow Action Pill -->
              <div class="w-9 h-9 rounded-full bg-white/10 group-hover:bg-white text-white group-hover:text-slate-950 backdrop-blur-md border border-white/20 flex items-center justify-center shrink-0 transition-all duration-300 group-hover:translate-x-1 shadow-lg">
                <ChevronRight class="w-4 h-4" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. SECTION: TIN TỨC & BLOG ĐIỆN ẢNH (EDITORIAL MAGAZINE LAYOUT) -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800/80 pb-4">
        <div>
          <div class="flex items-center gap-2 text-rose-500 text-xs font-black uppercase tracking-wider mb-1">
            <Flame class="w-3.5 h-3.5 animate-pulse" />
            <span>BẢN TIN ĐIỆN ẢNH & SỰ KIỆN NÓNG</span>
          </div>
          <h2 class="text-2xl sm:text-3xl font-black text-white tracking-tight">
            Tin tức & Blog CineVerse
          </h2>
          <p class="text-xs sm:text-sm text-slate-400 mt-1">
            Cập nhật nhanh nhất tin tức hậu trường, lịch chiếu bom tấn và thông tin sự kiện độc quyền
          </p>
        </div>
        <router-link to="/cinema" class="text-xs font-extrabold text-blue-400 hover:text-blue-300 flex items-center gap-1 group">
          <span>Xem tất cả bài viết</span>
          <ChevronRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
        </router-link>
      </div>

      <!-- Editorial Magazine Grid: Left 7 cols (Featured Big Story), Right 5 cols (Trending Stories Stack) -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-stretch">
        <!-- Left: Featured Headline Story (7 cols) -->
        <article
          class="lg:col-span-7 group rounded-3xl bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/60 flex flex-col justify-between overflow-hidden transition-all duration-500 hover:shadow-2xl hover:shadow-blue-950/40 cursor-pointer"
          @click="router.push('/cinema')"
        >
          <!-- Big 16:9 Image with Cinematic Overlays -->
          <div class="relative aspect-[16/9] sm:aspect-[16/9] overflow-hidden bg-slate-950">
            <img
              :src="featuredNews.imageUrl"
              :alt="featuredNews.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700 ease-out"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-[#0D121F] via-[#0D121F]/40 to-transparent"></div>
            
            <!-- Badges -->
            <div class="absolute top-4 left-4 flex items-center gap-2">
              <span class="px-3 py-1 rounded-full bg-rose-600/90 text-white text-[10px] font-black uppercase tracking-wider shadow-lg">
                {{ featuredNews.badge }}
              </span>
              <span class="px-3 py-1 rounded-full bg-black/60 backdrop-blur-md border border-white/20 text-white text-[10px] font-bold">
                {{ featuredNews.tag }}
              </span>
            </div>

            <div class="absolute bottom-4 right-4 px-2.5 py-1 rounded-lg bg-black/70 backdrop-blur-md text-[11px] font-mono font-bold text-slate-300 border border-white/10">
              👁️ {{ featuredNews.views }} lượt xem
            </div>
          </div>

          <!-- Featured Body -->
          <div class="p-6 sm:p-7 flex-1 flex flex-col justify-between space-y-4">
            <div class="space-y-3">
              <div class="flex items-center gap-3 text-xs text-slate-400">
                <span class="text-blue-400 font-bold">{{ featuredNews.author }}</span>
                <span>&bull;</span>
                <span class="flex items-center gap-1">
                  <Calendar class="w-3.5 h-3.5 text-slate-500" />
                  {{ featuredNews.date }}
                </span>
                <span>&bull;</span>
                <span class="flex items-center gap-1 text-slate-400">
                  <Clock class="w-3.5 h-3.5 text-slate-500" />
                  {{ featuredNews.readTime }}
                </span>
              </div>

              <h3 class="text-xl sm:text-2xl font-black text-white group-hover:text-blue-400 transition-colors leading-tight">
                {{ featuredNews.title }}
              </h3>

              <p class="text-sm text-slate-300 leading-relaxed line-clamp-3">
                {{ featuredNews.excerpt }}
              </p>
            </div>

            <!-- Footer Action -->
            <div class="pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs sm:text-sm font-extrabold text-blue-400 group-hover:text-blue-300">
              <span class="flex items-center gap-2">
                <span>Đọc toàn bộ bài phân tích</span>
                <ArrowRight class="w-4 h-4 group-hover:translate-x-1.5 transition-transform" />
              </span>
              <span class="text-slate-500 text-xs font-normal">Chuyên mục Điện Ảnh</span>
            </div>
          </div>
        </article>

        <!-- Right: Trending Stories Stack (5 cols) -->
        <div class="lg:col-span-5 flex flex-col justify-between gap-4">
          <div
            v-for="(news, nIdx) in trendingNewsList"
            :key="news.id"
            @click="router.push('/cinema')"
            class="group p-4 sm:p-5 rounded-2xl bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/50 flex gap-4 items-stretch justify-between transition-all duration-300 cursor-pointer hover:shadow-xl hover:shadow-blue-950/30 hover:-translate-y-1 flex-1"
          >
            <!-- Thumbnail Image (stretched to match content height) -->
            <div class="relative w-28 sm:w-36 rounded-xl overflow-hidden shrink-0 bg-slate-950 border border-slate-800 self-stretch min-h-[110px]">
              <img
                :src="news.imageUrl"
                :alt="news.title"
                class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
              />
              <span class="absolute top-2 left-2 px-1.5 py-0.5 rounded-md bg-black/85 backdrop-blur-md text-[10px] font-black text-white flex items-center justify-center border border-white/20 shadow-md">
                0{{ nIdx + 1 }}
              </span>
            </div>

            <!-- Info with Full Metadata & Summary -->
            <div class="min-w-0 flex-1 flex flex-col justify-between space-y-2">
              <div class="space-y-1.5">
                <div class="flex items-center gap-2 flex-wrap">
                  <span class="px-2 py-0.5 rounded text-[10px] font-black uppercase tracking-wider border" :class="news.tagColor">
                    {{ news.tag }}
                  </span>
                  <span class="text-[11px] text-slate-500 font-mono">{{ news.date }}</span>
                  <span class="text-[10px] text-slate-400 font-mono ml-auto flex items-center gap-1">
                    <Eye class="w-3 h-3 text-slate-500" />
                    {{ news.views }}
                  </span>
                </div>

                <h4 class="text-xs sm:text-sm font-bold text-white group-hover:text-blue-400 transition-colors line-clamp-2 leading-snug">
                  {{ news.title }}
                </h4>

                <p class="text-[11px] sm:text-xs text-slate-400 line-clamp-2 leading-relaxed">
                  {{ news.summary }}
                </p>
              </div>

              <!-- Card Footer with Source, Read Time & Action -->
              <div class="flex items-center justify-between text-[11px] text-slate-400 pt-2 border-t border-slate-800/80">
                <span class="flex items-center gap-1.5 text-slate-400 truncate">
                  <span class="text-blue-400 font-medium truncate">{{ news.source }}</span>
                  <span>&bull;</span>
                  <Clock class="w-3 h-3 text-slate-500 shrink-0" />
                  <span class="shrink-0">{{ news.readTime }}</span>
                </span>
                <span class="text-blue-400 font-bold group-hover:translate-x-1 transition-transform flex items-center gap-0.5 shrink-0 ml-2">
                  Đọc ngay &rarr;
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 6. SECTION: TRẢI NGHIỆM ĐIỆN ẢNH ĐỈNH CAO + APP DOWNLOAD MATCHING IMAGE 2 -->
    <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
        <!-- Left: Experience Cinema -->
        <div class="lg:col-span-8 rounded-3xl relative overflow-hidden border border-[#1C253B] p-6 sm:p-10 shadow-2xl flex flex-col justify-center min-h-[190px]">
          <img
            src="/images/cinema_experience.jpg"
            alt="Cinema Experience"
            class="absolute inset-0 w-full h-full object-cover z-0 filter brightness-90"
          />
          <div class="absolute inset-0 bg-gradient-to-r from-[#070A12]/95 via-[#070A12]/70 to-transparent z-0"></div>
          <div class="relative z-10 space-y-3">
            <h3 class="text-2xl sm:text-3xl font-black text-white">
              Trải nghiệm điện ảnh đỉnh cao
            </h3>
            <p class="text-xs sm:text-sm text-slate-300 max-w-md leading-relaxed">
              Cùng CineVerse khám phá thế giới phim ảnh và những sự kiện giải trí hấp dẫn nhất!
            </p>
            <div class="flex items-center gap-3 pt-2">
              <router-link
                to="/cinema"
                class="px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition shadow-md shadow-blue-600/30 cursor-pointer"
              >
                Mua vé ngay
              </router-link>
              <router-link
                to="/events"
                class="px-5 py-2.5 rounded-xl bg-slate-900/90 hover:bg-slate-800 border border-slate-700 text-slate-200 font-bold text-xs transition cursor-pointer"
              >
                Khám phá thêm
              </router-link>
            </div>
          </div>
        </div>

        <!-- Right: App Download Mockup -->
        <div class="lg:col-span-4 rounded-3xl bg-[#0D121F] border border-[#1C253B] p-5 shadow-2xl relative overflow-hidden flex items-center justify-between">
          <div class="space-y-3 z-10">
            <div>
              <h4 class="font-extrabold text-sm sm:text-base text-white">Tải ứng dụng CineVerse</h4>
              <p class="text-xs text-slate-400">Đặt vé mọi lúc, mọi nơi</p>
            </div>
            <div class="flex items-center gap-2">
              <button type="button" class="px-3 py-1.5 rounded-xl bg-black border border-slate-800 hover:border-slate-700 flex items-center gap-2 text-left">
                <Apple class="w-4 h-4 text-white" />
                <div>
                  <span class="block text-[7px] text-slate-400 uppercase leading-none">Tải trên</span>
                  <span class="block text-[9px] font-bold text-white leading-tight">App Store</span>
                </div>
              </button>
              <button type="button" class="px-3 py-1.5 rounded-xl bg-black border border-slate-800 hover:border-slate-700 flex items-center gap-2 text-left">
                <Smartphone class="w-4 h-4 text-emerald-400" />
                <div>
                  <span class="block text-[7px] text-slate-400 uppercase leading-none">Tải trên</span>
                  <span class="block text-[9px] font-bold text-white leading-tight">Google Play</span>
                </div>
              </button>
            </div>
          </div>

          <div class="w-24 h-28 shrink-0 overflow-hidden rounded-xl border border-slate-800/80 shadow-lg">
            <img src="/images/app_download.jpg" alt="CineVerse App" class="w-full h-full object-cover object-right" />
          </div>
        </div>
      </div>
    </section>

    <!-- Trailer Embed Modal -->
    <Teleport to="body">
      <div v-if="trailerOpen" class="fixed inset-0 z-50 overflow-hidden flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/90 backdrop-blur-sm transition-opacity" @click="trailerOpen = false"></div>
        <div class="relative w-full max-w-4xl bg-slate-900 rounded-2xl border border-slate-800 shadow-2xl overflow-hidden z-10 space-y-0">
          <div class="p-4 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
            <span class="font-extrabold text-sm text-white">{{ trailerTitle }} - Official Trailer</span>
            <button
              type="button"
              @click="trailerOpen = false"
              class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            >
              <X class="w-5 h-5" />
            </button>
          </div>
          <div class="relative aspect-video bg-black">
            <iframe
              :src="trailerUrl"
              class="w-full h-full"
              title="YouTube video player"
              frameborder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowfullscreen
            ></iframe>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
