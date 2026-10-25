<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { cinemaService } from '@/services/cinema.service'
import { useAuthStore } from '@/stores/auth'
import type {
  Movie,
  Showtime,
  CinemaSeat,
  ConcessionItem,
  CinemaBookingRequest,
} from '@/types/cinema'
import type { Booking } from '@/types/booking'
import {
  Film,
  Play,
  Star,
  Clock,
  Sparkles,
  MapPin,
  Calendar,
  Monitor,
  X,
  Ticket,
  Check,
  ChevronRight,
  Plus,
  Minus,
  ShoppingBag,
  ShieldCheck,
  AlertCircle,
  Heart,
  QrCode,
  ThumbsUp,
  MessageSquare,
  Send,
  User,
  Share2,
  CheckCircle2,
  Info,
  ShieldAlert,
  Award,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const movie = ref<Movie | null>(null)
const showtimes = ref<Showtime[]>([])
const concessions = ref<ConcessionItem[]>([])
const loading = ref(true)
const loadingShowtimes = ref(false)

// Reviews & Comments State
interface MovieReview {
  id: number
  userName: string
  userAvatar: string
  rating: number
  date: string
  theaterWatched?: string
  content: string
  likes: number
  isLiked?: boolean
}

const reviews = ref<MovieReview[]>([
  {
    id: 1,
    userName: 'Minh Quân (Vip CineClub)',
    userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80',
    rating: 5,
    date: '24/10/2026',
    theaterWatched: 'CineVerse Landmark 81 IMAX Laser',
    content: 'Phim quá đỉnh! Kỹ xảo hình ảnh và âm thanh của Ufotable đưa lên màn chiếu IMAX Laser thực sự choáng ngợp. Từng đường kiếm chuyển động mượt mà không góc chết, âm bass rung chuyển cả phòng chiếu. Chắc chắn sẽ xem lại lần 2!',
    likes: 142,
  },
  {
    id: 2,
    userName: 'Thanh Hà',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80',
    rating: 5,
    date: '23/10/2026',
    theaterWatched: 'CGV Vincom Đồng Khởi',
    content: 'Đoạn mở đầu Vô Hạn Thành thực sự là kiệt tác nghệ thuật CGI. Cảm xúc dâng trào trong từng câu thoại, âm nhạc cất lên là nổi da gà. Các bạn nên chọn hàng ghế giữa để tận hưởng trọn vẹn màn hình!',
    likes: 89,
  },
  {
    id: 3,
    userName: 'Đức Anh Media',
    userAvatar: 'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=120&q=80',
    rating: 4.5,
    date: '22/10/2026',
    theaterWatched: 'CineVerse Bà Triệu',
    content: 'Xứng đáng là bom tấn chiếu rạp số 1 năm nay. Nhịp phim nhanh, nghẹt thở từ phút đầu tiên tới cuối cùng. Khuyến khích mọi người đi xem phòng chiếu Dolby Atmos hoặc IMAX để có trải nghiệm tốt nhất.',
    likes: 67,
  },
])

const newCommentRating = ref(5)
const newCommentText = ref('')
const newCommentName = ref('')

const handleAddComment = () => {
  if (!newCommentText.value.trim()) return
  const userName = authStore.user?.fullName || newCommentName.value.trim() || 'Khán giả CineVerse'
  const newRev: MovieReview = {
    id: Date.now(),
    userName,
    userAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=120&q=80',
    rating: newCommentRating.value,
    date: 'Vừa xong',
    theaterWatched: 'Suất chiếu trực tuyến EventHub',
    content: newCommentText.value.trim(),
    likes: 0,
  }
  reviews.value.unshift(newRev)
  newCommentText.value = ''
  newCommentName.value = ''
  alert('Cảm ơn bạn đã gửi đánh giá! Bình luận của bạn đã được xuất bản.')
}

const toggleLikeReview = (review: MovieReview) => {
  if (review.isLiked) {
    review.likes--
    review.isLiked = false
  } else {
    review.likes++
    review.isLiked = true
  }
}

// Cast list based on movie title
const castList = computed(() => {
  if (!movie.value) return []
  if (movie.value.title.includes('Thanh Gươm') || movie.value.title.includes('Demon Slayer')) {
    return [
      { name: 'Natsuki Hanae', role: 'Kamado Tanjiro', avatar: 'https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=120&q=80' },
      { name: 'Akari Kito', role: 'Kamado Nezuko', avatar: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=120&q=80' },
      { name: 'Hiro Shimono', role: 'Agatsuma Zenitsu', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80' },
      { name: 'Yoshitsugu Matsuoka', role: 'Hashibira Inosuke', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=80' },
      { name: 'Toshihiko Seki', role: 'Kibutsuji Muzan', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&q=80' },
    ]
  }
  if (movie.value.title.includes('F1')) {
    return [
      { name: 'Brad Pitt', role: 'Sonny Hayes', avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=120&q=80' },
      { name: 'Damson Idris', role: 'Joshua Pearce', avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=120&q=80' },
      { name: 'Javier Bardem', role: 'Chủ đội APXGP', avatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=120&q=80' },
      { name: 'Kerry Condon', role: 'Kỹ sư trưởng Kate', avatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80' },
    ]
  }
  const actors = movie.value.actorsCast ? movie.value.actorsCast.split(',').map((s) => s.trim()) : ['Diễn viên chính']
  return actors.map((name, i) => ({
    name,
    role: `Diễn viên chính ${i + 1}`,
    avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80',
  }))
})

// Date filter pills (Next 5 days)
const today = new Date()
const dateOptions = Array.from({ length: 6 }).map((_, i) => {
  const d = new Date()
  d.setDate(today.getDate() + i)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const iso = `${yyyy}-${mm}-${dd}`
  const label =
    i === 0
      ? 'Hôm nay'
      : i === 1
      ? 'Ngày mai'
      : `Thứ ${d.getDay() === 0 ? 'CN' : d.getDay() + 1}`
  const displayDate = `${dd}/${mm}`
  return { iso, label, displayDate }
})

const selectedDate = ref(dateOptions[0].iso)
const selectedCity = ref('ALL')

// Trailer Modal state
const trailerModalOpen = ref(false)
const trailerUrl = ref('')

// Booking Modal State
const bookingModalOpen = ref(false)
const selectedShowtime = ref<Showtime | null>(null)
const bookingStep = ref<'seats' | 'concessions' | 'checkout' | 'success'>('seats')

// Seat Map state
const seats = ref<CinemaSeat[]>([])
const loadingSeats = ref(false)
const selectedSeats = ref<CinemaSeat[]>([])

// Concession state: concessionId -> quantity
const selectedConcessions = ref<Record<number, number>>({})

// Coupon code
const couponInput = ref('')
const appliedDiscount = ref(0)
const couponError = ref('')

// Submitting state
const isSubmitting = ref(false)
const completedBooking = ref<Booking | null>(null)

// Load movie detail and showtimes
const loadMovieData = async () => {
  loading.value = true
  const idOrSlug = route.params.id as string
  try {
    const movieData = await cinemaService.getMovieDetail(idOrSlug)
    movie.value = movieData

    if (movieData.trailerUrl) {
      let embed = movieData.trailerUrl
      if (embed.includes('watch?v=')) embed = embed.replace('watch?v=', 'embed/')
      else if (embed.includes('youtu.be/')) embed = embed.replace('youtu.be/', 'www.youtube.com/embed/')
      if (!embed.includes('autoplay=')) embed += (embed.includes('?') ? '&' : '?') + 'autoplay=1'
      trailerUrl.value = embed
    }

    await loadShowtimes(movieData.id, selectedDate.value)
    concessions.value = await cinemaService.getConcessions()
  } catch (err) {
    console.error('Error loading movie:', err)
  } finally {
    loading.value = false
  }
}

const loadShowtimes = async (movieId: number, date: string) => {
  loadingShowtimes.value = true
  try {
    showtimes.value = await cinemaService.getMovieShowtimes(movieId, date)
  } catch (err) {
    console.error('Error fetching showtimes:', err)
  } finally {
    loadingShowtimes.value = false
  }
}

watch(selectedDate, (newDate) => {
  if (movie.value) {
    loadShowtimes(movie.value.id, newDate)
  }
})

onMounted(() => {
  loadMovieData()
})

// Group showtimes by Cinema Complex, then by Screen Type
const showtimesByComplex = computed(() => {
  let list = showtimes.value
  if (selectedCity.value !== 'ALL') {
    list = list.filter((s) => s.city.toLowerCase().includes(selectedCity.value.toLowerCase()))
  }

  const map = new Map<string, { complexId: number; complexName: string; city: string; formats: Map<string, Showtime[]> }>()

  for (const s of list) {
    const key = `${s.complexId}`
    if (!map.has(key)) {
      map.set(key, {
        complexId: s.complexId,
        complexName: s.complexName,
        city: s.city,
        formats: new Map(),
      })
    }
    const complexObj = map.get(key)!
    const formatKey = s.screenType || 'STANDARD_2D'
    if (!complexObj.formats.has(formatKey)) {
      complexObj.formats.set(formatKey, [])
    }
    complexObj.formats.get(formatKey)!.push(s)
  }

  return Array.from(map.values())
})

// Open Booking Flow
const openBookingFlow = async (showtime: Showtime) => {
  if (!authStore.isAuthenticated) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  selectedShowtime.value = showtime
  bookingStep.value = 'seats'
  selectedSeats.value = []
  selectedConcessions.value = {}
  appliedDiscount.value = 0
  couponInput.value = ''
  couponError.value = ''
  bookingModalOpen.value = true

  loadingSeats.value = true
  try {
    seats.value = await cinemaService.getShowtimeSeats(showtime.id)
  } catch (err) {
    console.error('Failed to load seats:', err)
  } finally {
    loadingSeats.value = false
  }
}

// Seat Row Organization
const seatRows = computed(() => {
  const rowsMap = new Map<string, CinemaSeat[]>()
  for (const s of seats.value) {
    if (!rowsMap.has(s.row)) {
      rowsMap.set(s.row, [])
    }
    rowsMap.get(s.row)!.push(s)
  }
  // Sort rows A -> J
  const sorted = Array.from(rowsMap.entries()).sort((a, b) => a[0].localeCompare(b[0]))
  // Sort seats within row by seat number
  for (const [, rowSeats] of sorted) {
    rowSeats.sort((a, b) => a.number - b.number)
  }
  return sorted
})

// Handle Seat Toggle
const toggleSeat = (seat: CinemaSeat) => {
  if (seat.status !== 'AVAILABLE') return

  const index = selectedSeats.value.findIndex((s) => s.seatCode === seat.seatCode)
  if (index >= 0) {
    // Deselect
    selectedSeats.value.splice(index, 1)
  } else {
    // Max 8 seats
    if (selectedSeats.value.length >= 8) {
      alert('Bạn chỉ có thể chọn tối đa 8 ghế trong một lần đặt!')
      return
    }
    selectedSeats.value.push(seat)
  }
}

const isSeatSelected = (seat: CinemaSeat) => {
  return selectedSeats.value.some((s) => s.seatCode === seat.seatCode)
}

// Concessions update
const updateConcessionQty = (itemId: number, delta: number) => {
  const current = selectedConcessions.value[itemId] || 0
  const next = Math.max(0, current + delta)
  if (next === 0) {
    delete selectedConcessions.value[itemId]
  } else {
    selectedConcessions.value[itemId] = next
  }
}

// Price Calculations
const seatsSubtotal = computed(() => {
  return selectedSeats.value.reduce((acc, s) => acc + (s.price || 0), 0)
})

const concessionsSubtotal = computed(() => {
  let sum = 0
  for (const [idStr, qty] of Object.entries(selectedConcessions.value)) {
    const item = concessions.value.find((c) => c.id === Number(idStr))
    if (item) {
      sum += item.price * qty
    }
  }
  return sum
})

const totalAmount = computed(() => {
  const sub = seatsSubtotal.value + concessionsSubtotal.value
  return Math.max(0, sub - appliedDiscount.value)
})

// Apply Coupon
const applyCoupon = () => {
  couponError.value = ''
  if (!couponInput.value.trim()) return
  if (couponInput.value.trim().toUpperCase() === 'EVENTHUB2026') {
    appliedDiscount.value = Math.round((seatsSubtotal.value + concessionsSubtotal.value) * 0.15)
  } else {
    couponError.value = 'Mã giảm giá không hợp lệ hoặc đã hết hạn.'
    appliedDiscount.value = 0
  }
}

// Confirm Booking
const submitBooking = async () => {
  if (!selectedShowtime.value || selectedSeats.value.length === 0) return
  isSubmitting.value = true

  const concessionList = Object.entries(selectedConcessions.value).map(([idStr, qty]) => ({
    concessionId: Number(idStr),
    quantity: qty,
  }))

  const request: CinemaBookingRequest = {
    showtimeId: selectedShowtime.value.id,
    seatCodes: selectedSeats.value.map((s) => s.seatCode),
    concessions: concessionList.length > 0 ? concessionList : undefined,
    couponCode: appliedDiscount.value > 0 ? couponInput.value.trim() : undefined,
  }

  try {
    const result = await cinemaService.bookCinemaTickets(request)
    completedBooking.value = result
    bookingStep.value = 'success'
  } catch (err: any) {
    alert(err.response?.data?.message || 'Đặt vé không thành công, vui lòng thử lại!')
  } finally {
    isSubmitting.value = false
  }
}

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatTime = (isoString?: string) => {
  if (!isoString) return ''
  const d = new Date(isoString)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

const getRatingColor = (code?: string) => {
  switch (code) {
    case 'T18':
      return 'bg-rose-600 text-white border-rose-700'
    case 'T16':
      return 'bg-orange-600 text-white border-orange-700'
    case 'T13':
      return 'bg-amber-500 text-slate-950 border-amber-600'
    case 'P':
      return 'bg-emerald-600 text-white border-emerald-700'
    case 'K':
      return 'bg-blue-600 text-white border-blue-700'
    default:
      return 'bg-slate-700 text-white border-slate-600'
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#030712] text-slate-100 selection:bg-rose-500 selection:text-white relative overflow-hidden">
    <!-- Ambient Cinematic Lighting Gradient Meshes -->
    <div class="fixed inset-0 pointer-events-none z-0">
      <!-- Top Cinema Ceiling Light Cone -->
      <div class="absolute -top-32 left-1/2 -translate-x-1/2 w-[1000px] h-[550px] bg-[radial-gradient(ellipse_at_center,rgba(244,63,94,0.11),rgba(99,102,241,0.08),transparent_70%)]"></div>
      <div class="absolute -top-40 left-1/4 w-[650px] h-[650px] bg-rose-600/[0.08] rounded-full blur-[150px]"></div>
      <div class="absolute top-1/3 -right-40 w-[650px] h-[650px] bg-indigo-600/[0.07] rounded-full blur-[160px]"></div>
      <div class="absolute bottom-10 left-10 w-[550px] h-[550px] bg-cyan-600/[0.06] rounded-full blur-[150px]"></div>
      <div class="absolute inset-0 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:32px_32px] opacity-[0.03]"></div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="max-w-7xl mx-auto px-4 py-20 text-center space-y-4 relative z-10">
      <div class="w-12 h-12 border-4 border-rose-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
      <p class="text-sm text-slate-400">Đang tải thông tin phim và suất chiếu...</p>
    </div>

    <!-- Movie Content -->
    <div v-else-if="movie" class="space-y-10 pb-20 relative z-10">
      <!-- Movie Hero Backdrop Header -->
      <section class="relative overflow-hidden border-b border-white/[0.08] bg-gradient-to-b from-[#0c1328]/95 via-[#080d1e]/90 to-[#030712] pt-8 pb-12 sm:pb-16">
        <div class="absolute inset-0 z-0">
          <img
            :src="movie.bannerUrl || movie.posterUrl"
            :alt="movie.title"
            class="w-full h-full object-cover object-center opacity-30 filter blur-[1px] scale-105"
          />
          <div class="absolute inset-0 bg-gradient-to-r from-[#030712] via-[#030712]/90 to-[#030712]/60"></div>
          <div class="absolute inset-0 bg-gradient-to-t from-[#030712] via-transparent to-transparent"></div>
        </div>

        <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <!-- Back Link -->
          <div class="mb-6">
            <router-link
              to="/cinema"
              class="inline-flex items-center gap-1.5 text-xs font-bold text-slate-400 hover:text-rose-400 transition"
            >
              ← Quay lại danh sách phim rạp
            </router-link>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-12 gap-8 items-start">
            <!-- Movie Poster -->
            <div class="md:col-span-4 lg:col-span-3 flex justify-center md:justify-start">
              <div class="relative group">
                <img
                  :src="movie.posterUrl"
                  :alt="movie.title"
                  class="w-56 sm:w-64 rounded-2xl shadow-2xl object-cover aspect-[2/3] border border-white/[0.08]"
                />
                <button
                  v-if="movie.trailerUrl"
                  type="button"
                  @click="trailerModalOpen = true"
                  class="absolute inset-0 bg-black/60 backdrop-blur-2xs flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity rounded-2xl cursor-pointer"
                >
                  <div class="w-14 h-14 rounded-full bg-rose-600 text-white flex items-center justify-center shadow-lg transform scale-90 group-hover:scale-100 transition-transform">
                    <Play class="w-6 h-6 fill-white ml-1" />
                  </div>
                  <span class="text-xs font-bold text-white uppercase tracking-wider">Xem Trailer</span>
                </button>
              </div>
            </div>

            <!-- Movie Information -->
            <div class="md:col-span-8 lg:col-span-9 space-y-5">
              <div class="space-y-3">
                <div class="flex flex-wrap items-center gap-2.5">
                  <span
                    class="px-2.5 py-0.5 rounded font-black text-xs border"
                    :class="getRatingColor(movie.ageRatingCode)"
                  >
                    {{ movie.ageRatingCode }}
                  </span>
                  <span v-if="movie.ageRatingDesc" class="text-xs text-slate-400">
                    ({{ movie.ageRatingDesc }})
                  </span>
                  <span v-if="movie.formatDisplay" class="px-2.5 py-0.5 rounded bg-indigo-900/60 border border-indigo-500/40 text-indigo-300 text-xs uppercase font-extrabold">
                    {{ movie.formatDisplay }}
                  </span>
                </div>

                <h1 class="text-3xl sm:text-4xl lg:text-5xl font-black text-white leading-tight">
                  {{ movie.title }}
                </h1>

                <div class="flex flex-wrap items-center gap-3 text-xs sm:text-sm font-semibold text-slate-300">
                  <span v-if="movie.imdbRating" class="flex items-center gap-1 text-amber-400 font-bold">
                    <Star class="w-4 h-4 fill-amber-400" />
                    {{ movie.imdbRating }} IMDb
                  </span>
                  <span>&bull;</span>
                  <span class="flex items-center gap-1">
                    <Clock class="w-4 h-4 text-slate-400" />
                    {{ movie.durationMinutes }} phút
                  </span>
                  <span>&bull;</span>
                  <span class="text-rose-400 font-bold">{{ movie.genre }}</span>
                  <span>&bull;</span>
                  <span class="text-slate-400">Khởi chiếu: {{ movie.releaseDate }}</span>
                </div>
              </div>

              <!-- Synopsis -->
              <div class="space-y-1.5 max-w-3xl">
                <h4 class="text-xs font-extrabold uppercase tracking-wider text-slate-400">Nội dung phim</h4>
                <p class="text-sm text-slate-300/90 leading-relaxed">
                  {{ movie.description }}
                </p>
              </div>

              <!-- Details Grid -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-300 pt-2 border-t border-white/[0.08] max-w-2xl">
                <p><strong class="text-slate-400">Đạo diễn:</strong> {{ movie.director || 'Chưa cập nhật' }}</p>
                <p><strong class="text-slate-400">Ngôn ngữ:</strong> {{ movie.language || 'Phụ đề tiếng Việt' }}</p>
                <p class="sm:col-span-2"><strong class="text-slate-400">Diễn viên:</strong> {{ movie.actorsCast || 'Chưa cập nhật' }}</p>
              </div>

              <!-- Trailer Trigger Button -->
              <div v-if="movie.trailerUrl" class="pt-2">
                <button
                  type="button"
                  @click="trailerModalOpen = true"
                  class="px-4 py-2 rounded-xl bg-slate-900/90 hover:bg-slate-800 border border-white/[0.08] text-slate-200 font-bold text-xs transition flex items-center gap-2 cursor-pointer hover:border-slate-600 shadow-md"
                >
                  <Play class="w-3.5 h-3.5 text-rose-400 fill-rose-400" />
                  <span>Xem Trailer Chính Thức</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= 2-COLUMN MAIN CONTENT (70% TRÁI / 30% PHẢI) ================= -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

          <!-- ================= CỘT TRÁI: 70% (LG:COL-SPAN-8) ================= -->
          <div class="lg:col-span-8 space-y-10">

            <!-- 1. LỊCH CHIẾU PHIM TẠI RẠP -->
            <div class="space-y-6">
              <div class="flex flex-wrap items-center justify-between gap-4 border-b border-white/[0.08] pb-4">
                <div>
                  <h2 class="text-xl sm:text-2xl font-black text-white flex items-center gap-2">
                    <Calendar class="w-6 h-6 text-rose-500" />
                    <span>Lịch Chiếu Phim Tại Rạp</span>
                  </h2>
                  <p class="text-xs text-slate-400 mt-1">Chọn ngày và cụm rạp để hiển thị các suất chiếu khả dụng</p>
                </div>

                <!-- City Filter -->
                <select
                  v-model="selectedCity"
                  class="px-3.5 py-2 rounded-xl bg-[#060a18] border border-white/[0.1] text-xs font-bold text-slate-200 focus:outline-none focus:border-cyan-400 cursor-pointer shadow-sm"
                >
                  <option value="ALL">Tất cả thành phố</option>
                  <option value="Hồ Chí Minh">TP. Hồ Chí Minh</option>
                  <option value="Hà Nội">Hà Nội</option>
                </select>
              </div>

              <!-- Date Selector Pills -->
              <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
                <button
                  v-for="opt in dateOptions"
                  :key="opt.iso"
                  type="button"
                  @click="selectedDate = opt.iso"
                  class="px-4 py-2.5 rounded-2xl border text-center shrink-0 transition flex flex-col items-center gap-0.5 cursor-pointer min-w-[90px]"
                  :class="selectedDate === opt.iso
                    ? 'bg-gradient-to-r from-rose-600 to-indigo-600 border-rose-400 text-white shadow-lg shadow-rose-600/30'
                    : 'bg-[#070b1a] border-white/[0.08] text-slate-300 hover:border-cyan-500/40 hover:text-white hover:bg-[#0c142c]'"
                >
                  <span class="text-[11px] font-bold opacity-80 uppercase">{{ opt.label }}</span>
                  <span class="text-sm font-black">{{ opt.displayDate }}</span>
                </button>
              </div>

              <!-- Showtime Loading State -->
              <div v-if="loadingShowtimes" class="py-12 text-center text-slate-400 space-y-2">
                <div class="w-8 h-8 border-2 border-rose-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
                <p class="text-xs">Đang tải suất chiếu...</p>
              </div>

              <!-- Empty Showtimes -->
              <div v-else-if="showtimesByComplex.length === 0" class="rounded-3xl bg-[#080d1f]/80 border border-white/[0.08] p-10 text-center space-y-3 backdrop-blur-md">
                <Clock class="w-10 h-10 text-slate-500 mx-auto" />
                <h3 class="text-base font-bold text-slate-200">Không có suất chiếu nào trong ngày đã chọn</h3>
                <p class="text-xs text-slate-400">Vui lòng chọn ngày chiếu khác hoặc đổi thành phố để xem thêm suất chiếu.</p>
              </div>

              <!-- Showtime Groups by Complex -->
              <div v-else class="space-y-5">
                <div
                  v-for="complex in showtimesByComplex"
                  :key="complex.complexId"
                  class="rounded-3xl bg-gradient-to-br from-[#0c1633]/90 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] p-5 sm:p-6 space-y-5 hover:border-cyan-500/40 backdrop-blur-xl shadow-2xl transition duration-300"
                >
                  <!-- Complex Header -->
                  <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/[0.06] pb-4">
                    <div class="space-y-1">
                      <h3 class="font-extrabold text-base sm:text-lg text-white flex items-center gap-2">
                        <MapPin class="w-4 h-4 text-rose-500 shrink-0" />
                        <span>{{ complex.complexName }}</span>
                      </h3>
                      <span class="text-xs text-slate-400">{{ complex.city }}</span>
                    </div>
                    <span class="text-[11px] font-extrabold px-2.5 py-1 rounded-lg bg-cyan-500/10 text-cyan-300 border border-cyan-500/20 self-start sm:self-auto">
                      Ghế Sweetbox &amp; VIP Khả dụng
                    </span>
                  </div>

                  <!-- Formats and Showtime Chips -->
                  <div class="space-y-4">
                    <div
                      v-for="[formatName, showtimesList] in complex.formats.entries()"
                      :key="formatName"
                      class="space-y-2.5"
                    >
                      <!-- Format Title -->
                      <div class="flex items-center gap-2 text-xs font-black uppercase text-cyan-400">
                        <Monitor class="w-3.5 h-3.5" />
                        <span>{{ formatName.replace('_', ' ') }}</span>
                      </div>

                      <!-- Showtime Chips -->
                      <div class="flex flex-wrap gap-2.5">
                        <button
                          v-for="st in showtimesList"
                          :key="st.id"
                          type="button"
                          @click="openBookingFlow(st)"
                          class="group px-4 py-2.5 rounded-xl bg-[#050814] border border-white/[0.09] hover:border-rose-500 hover:bg-gradient-to-b hover:from-rose-600 hover:to-indigo-600 transition flex flex-col items-center gap-0.5 cursor-pointer min-w-[95px] shadow-sm hover:shadow-lg hover:shadow-rose-600/30"
                        >
                          <span class="text-sm font-black text-white group-hover:text-white transition-colors">
                            {{ formatTime(st.startTime) }}
                          </span>
                          <span class="text-[10px] text-slate-400 group-hover:text-rose-100">
                            ~{{ formatTime(st.endTime) }}
                          </span>
                          <span class="text-[10px] font-extrabold text-amber-400 group-hover:text-amber-200 mt-0.5">
                            {{ formatCurrency(st.basePrice) }}
                          </span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2. ĐÁNH GIÁ & BÌNH LUẬN KHÁN GIẢ (REVIEWS & COMMENTS) -->
            <div class="space-y-6 pt-6 border-t border-white/[0.08]">
              <div class="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <h3 class="text-xl font-black text-white flex items-center gap-2">
                    <MessageSquare class="w-5 h-5 text-amber-400" />
                    <span>Đánh Giá &amp; Bình Luận Khán GiẢ</span>
                  </h3>
                  <p class="text-xs text-slate-400 mt-0.5">Đánh giá thực tế từ khán giả đã xem phim tại hệ thống rạp</p>
                </div>

                <span class="text-xs text-slate-400">
                  Tổng cộng: <strong class="text-white">{{ reviews.length + 324 }}</strong> đánh giá
                </span>
              </div>

              <!-- Rating Score Overview Card -->
              <div class="grid grid-cols-1 sm:grid-cols-12 gap-4 p-5 rounded-3xl bg-gradient-to-br from-[#0c1633]/85 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] items-center backdrop-blur-xl shadow-xl">
                <!-- Average Score Left -->
                <div class="sm:col-span-4 text-center sm:border-r border-white/[0.08] sm:pr-4 space-y-1">
                  <p class="text-4xl sm:text-5xl font-black text-white flex items-center justify-center gap-1">
                    <span>{{ movie?.imdbRating || 9.8 }}</span>
                    <span class="text-lg font-bold text-slate-500">/ 10</span>
                  </p>
                  <div class="flex items-center justify-center gap-1 text-amber-400">
                    <Star class="w-4 h-4 fill-amber-400" />
                    <Star class="w-4 h-4 fill-amber-400" />
                    <Star class="w-4 h-4 fill-amber-400" />
                    <Star class="w-4 h-4 fill-amber-400" />
                    <Star class="w-4 h-4 fill-amber-400" />
                  </div>
                  <p class="text-[11px] text-slate-400 font-semibold">Khuyên xem trên màn chiếu lớn</p>
                </div>

                <!-- Rating Bars Right -->
                <div class="sm:col-span-8 space-y-1.5 text-xs text-slate-300">
                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[11px] text-slate-400">5 sao</span>
                    <div class="flex-1 h-2 rounded-full bg-[#050814] overflow-hidden">
                      <div class="h-full bg-amber-400 rounded-full" style="width: 92%"></div>
                    </div>
                    <span class="w-8 text-[11px] text-right text-slate-400">92%</span>
                  </div>

                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[11px] text-slate-400">4 sao</span>
                    <div class="flex-1 h-2 rounded-full bg-[#050814] overflow-hidden">
                      <div class="h-full bg-amber-400 rounded-full" style="width: 6%"></div>
                    </div>
                    <span class="w-8 text-[11px] text-right text-slate-400">6%</span>
                  </div>

                  <div class="flex items-center gap-2">
                    <span class="w-12 text-[11px] text-slate-400">3 sao</span>
                    <div class="flex-1 h-2 rounded-full bg-[#050814] overflow-hidden">
                      <div class="h-full bg-amber-400 rounded-full" style="width: 2%"></div>
                    </div>
                    <span class="w-8 text-[11px] text-right text-slate-400">2%</span>
                  </div>
                </div>
              </div>

              <!-- Write Review Box -->
              <div class="p-5 rounded-3xl bg-gradient-to-br from-[#0c1633]/85 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] space-y-3.5 backdrop-blur-xl shadow-xl">
                <div class="flex items-center justify-between">
                  <h4 class="text-xs font-bold uppercase tracking-wider text-slate-300 flex items-center gap-1.5">
                    <Sparkles class="w-3.5 h-3.5 text-rose-500" />
                    <span>Viết cảm nhận của bạn về bộ phim:</span>
                  </h4>

                  <!-- Star Selector -->
                  <div class="flex items-center gap-1">
                    <span class="text-xs text-slate-400 mr-1">Chấm điểm:</span>
                    <button
                      v-for="s in [1, 2, 3, 4, 5]"
                      :key="s"
                      type="button"
                      @click="newCommentRating = s"
                      class="text-amber-400 hover:scale-125 transition cursor-pointer"
                    >
                      <Star
                        class="w-4 h-4"
                        :class="s <= newCommentRating ? 'fill-amber-400' : 'text-slate-600'"
                      />
                    </button>
                  </div>
                </div>

                <div v-if="!authStore.isAuthenticated" class="relative">
                  <input
                    v-model="newCommentName"
                    type="text"
                    placeholder="Họ và tên hoặc biệt danh của bạn..."
                    class="w-full px-3.5 py-2.5 rounded-xl bg-[#040711] border border-white/[0.1] text-xs text-white placeholder-slate-400 focus:outline-none focus:border-cyan-400 shadow-inner"
                  />
                </div>

                <textarea
                  v-model="newCommentText"
                  rows="3"
                  placeholder="Chia sẻ cảm xúc của bạn về hình ảnh, diễn xuất, âm nhạc của bộ phim..."
                  class="w-full p-3.5 rounded-xl bg-[#040711] border border-white/[0.1] text-xs text-slate-200 placeholder-slate-400 focus:outline-none focus:border-cyan-400 transition resize-none shadow-inner"
                ></textarea>

                <div class="flex items-center justify-between pt-1">
                  <span class="text-[11px] text-slate-400">Bình luận tuân thủ quy chuẩn văn minh cộng đồng</span>
                  <button
                    type="button"
                    @click="handleAddComment"
                    class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-rose-600 to-indigo-600 hover:from-rose-500 hover:to-indigo-500 text-white text-xs font-bold transition flex items-center gap-1.5 cursor-pointer shadow-lg shadow-rose-600/30"
                  >
                    <Send class="w-3.5 h-3.5" />
                    <span>Gửi Đánh Giá</span>
                  </button>
                </div>
              </div>

              <!-- Reviews List -->
              <div class="space-y-3.5">
                <div
                  v-for="rev in reviews"
                  :key="rev.id"
                  class="p-4 rounded-2xl bg-[#080d1f]/85 border border-white/[0.08] hover:border-cyan-500/30 space-y-2.5 transition shadow-sm backdrop-blur-md"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="flex items-center gap-3">
                      <img :src="rev.userAvatar" :alt="rev.userName" class="w-9 h-9 rounded-full object-cover border border-white/[0.1]" />
                      <div>
                        <p class="text-xs font-bold text-white flex items-center gap-1.5">
                          <span>{{ rev.userName }}</span>
                          <span v-if="rev.theaterWatched" class="text-[10px] text-slate-400 font-normal hidden sm:inline">
                            &bull; Xem tại {{ rev.theaterWatched }}
                          </span>
                        </p>
                        <div class="flex items-center gap-2 mt-0.5">
                          <div class="flex items-center gap-0.5 text-amber-400">
                            <Star v-for="i in Math.floor(rev.rating)" :key="i" class="w-3 h-3 fill-amber-400" />
                          </div>
                          <span class="text-[10px] text-slate-400">{{ rev.date }}</span>
                        </div>
                      </div>
                    </div>

                    <!-- Like Button -->
                    <button
                      type="button"
                      @click="toggleLikeReview(rev)"
                      class="flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-xs font-semibold transition border cursor-pointer"
                      :class="rev.isLiked ? 'bg-rose-500/20 border-rose-500/40 text-rose-400' : 'bg-[#040711] border-white/[0.08] text-slate-400 hover:text-white'"
                    >
                      <ThumbsUp class="w-3.5 h-3.5" :class="rev.isLiked ? 'fill-rose-400' : ''" />
                      <span>{{ rev.likes }}</span>
                    </button>
                  </div>

                  <p class="text-xs text-slate-300/90 leading-relaxed pl-12">
                    {{ rev.content }}
                  </p>
                </div>
              </div>
            </div>

          </div>

          <!-- ================= CỘT PHẢI: 30% (LG:COL-SPAN-4) ================= -->
          <aside class="lg:col-span-4 space-y-6 lg:sticky lg:top-24">

            <!-- 1. TÓM TẮT NỘI DUNG & ĐIỂM NHẤN CỐT TRUYỆN -->
            <div class="p-5 rounded-3xl bg-gradient-to-br from-[#0c1633]/90 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] space-y-3.5 backdrop-blur-xl shadow-xl">
              <h3 class="text-sm font-bold text-white flex items-center gap-2 border-b border-white/[0.08] pb-3">
                <Film class="w-4 h-4 text-cyan-400" />
                <span>Tóm Tắt Cốt Truyện &amp; Điểm Nhấn</span>
              </h3>

              <div class="space-y-3 text-xs leading-relaxed text-slate-300">
                <p>
                  {{ movie?.description }}
                </p>

                <div class="space-y-2 pt-2 border-t border-white/[0.06]">
                  <p class="font-bold text-white">Điểm nhấn nổi bật:</p>
                  <div class="space-y-1.5 text-slate-400">
                    <p class="flex items-start gap-2">
                      <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                      <span>Đồ họa &amp; hiệu ứng hình ảnh chuẩn điện ảnh quốc tế.</span>
                    </p>
                    <p class="flex items-start gap-2">
                      <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                      <span>Âm thanh vòm Dolby Atmos tái hiện chi tiết từng pha hành động.</span>
                    </p>
                    <p class="flex items-start gap-2">
                      <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400 shrink-0 mt-0.5" />
                      <span>Trải nghiệm trọn vẹn nhất trên các màn chiếu cực đại IMAX Laser.</span>
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 2. DÀN DIỄN VIÊN & NHÂN VẬT NỔI BẬT -->
            <div class="p-5 rounded-3xl bg-gradient-to-br from-[#0c1633]/90 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] space-y-3.5 backdrop-blur-xl shadow-xl">
              <h3 class="text-sm font-bold text-white flex items-center gap-2 border-b border-white/[0.08] pb-3">
                <User class="w-4 h-4 text-indigo-400" />
                <span>Diễn Viên &amp; Nhân Vật</span>
              </h3>

              <div class="space-y-2.5">
                <div
                  v-for="(actor, idx) in castList"
                  :key="idx"
                  class="flex items-center gap-3 p-2 rounded-xl bg-[#050814]/90 border border-white/[0.07]"
                >
                  <img :src="actor.avatar" :alt="actor.name" class="w-10 h-10 rounded-full object-cover border border-white/[0.1]" />
                  <div class="min-w-0">
                    <p class="text-xs font-bold text-white truncate">{{ actor.name }}</p>
                    <p class="text-[11px] text-slate-400 truncate">{{ actor.role }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- 3. THÔNG TIN CHI TIẾT & EKIP SẢN XUẤT -->
            <div class="p-5 rounded-3xl bg-gradient-to-br from-[#0c1633]/90 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] space-y-3 backdrop-blur-xl shadow-xl">
              <h3 class="text-sm font-bold text-white flex items-center gap-2 border-b border-white/[0.08] pb-3">
                <Info class="w-4 h-4 text-cyan-400" />
                <span>Thông Tin Chi Tiết Phim</span>
              </h3>

              <div class="space-y-2 text-xs">
                <div class="flex items-center justify-between py-1 border-b border-white/[0.06]">
                  <span class="text-slate-400">Đạo diễn:</span>
                  <span class="font-bold text-white">{{ movie?.director || 'Đang cập nhật' }}</span>
                </div>
                <div class="flex items-center justify-between py-1 border-b border-white/[0.06]">
                  <span class="text-slate-400">Thời lượng:</span>
                  <span class="font-bold text-white">{{ movie?.durationMinutes }} phút</span>
                </div>
                <div class="flex items-center justify-between py-1 border-b border-white/[0.06]">
                  <span class="text-slate-400">Ngày phát hành:</span>
                  <span class="font-bold text-white">{{ movie?.releaseDate }}</span>
                </div>
                <div class="flex items-center justify-between py-1 border-b border-white/[0.06]">
                  <span class="text-slate-400">Độ tuổi:</span>
                  <span class="font-bold text-amber-400">{{ movie?.ageRatingCode }} ({{ movie?.ageRatingDesc || 'Theo quy định' }})</span>
                </div>
                <div class="flex items-center justify-between py-1 border-b border-white/[0.06]">
                  <span class="text-slate-400">Ngôn ngữ:</span>
                  <span class="font-bold text-white">{{ movie?.language || 'Phụ đề tiếng Việt' }}</span>
                </div>
                <div class="flex items-center justify-between py-1">
                  <span class="text-slate-400">Định dạng:</span>
                  <span class="font-bold text-cyan-400">{{ movie?.formatDisplay || '2D Digital' }}</span>
                </div>
              </div>
            </div>

            <!-- 4. QUY ĐỊNH PHÒNG VÉ & VÀO RẠP -->
            <div class="p-4 rounded-2xl bg-[#080d1f]/75 border border-white/[0.08] text-xs text-slate-400 space-y-2 backdrop-blur-md">
              <div class="flex items-center gap-2 text-slate-200 font-bold">
                <ShieldCheck class="w-4 h-4 text-emerald-400" />
                <span>Lưu ý phòng vé EventHub</span>
              </div>
              <p class="text-[11px] leading-relaxed">
                Khán giả vui lòng xuất trình CCCD/Hộ chiếu đối với phim có giới hạn độ tuổi (T16, T18). Vé đã đặt trực tuyến quét mã QR để vào thẳng phòng chiếu.
              </p>
            </div>

          </aside>
        </div>
      </section>
    </div>

    <!-- CINEMA BOOKING STEP FLOW MODAL -->
    <Teleport to="body">
      <div v-if="bookingModalOpen" class="fixed inset-0 z-50 overflow-hidden flex items-center justify-center p-2 sm:p-4">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/90 backdrop-blur-md transition-opacity" @click="bookingStep !== 'checkout' && !isSubmitting ? (bookingModalOpen = false) : null"></div>

        <!-- Modal Dialog -->
        <div class="relative w-full max-w-4xl bg-[#090f24] border border-white/[0.1] rounded-3xl shadow-2xl flex flex-col max-h-[92vh] overflow-hidden z-10 backdrop-blur-2xl">
          <!-- Modal Header -->
          <div class="p-4 sm:p-5 bg-[#050814] border-b border-white/[0.08] flex items-center justify-between shrink-0">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl bg-rose-600/20 border border-rose-500/30 flex items-center justify-center text-rose-500">
                <Ticket class="w-5 h-5" />
              </div>
              <div>
                <h3 class="font-extrabold text-base text-white">
                  {{ movie?.title }}
                </h3>
                <p v-if="selectedShowtime" class="text-xs text-slate-400 flex items-center gap-2">
                  <span>{{ selectedShowtime.complexName }}</span>
                  <span>&bull;</span>
                  <span class="text-rose-400 font-bold">{{ selectedShowtime.screenName }} ({{ selectedShowtime.screenType }})</span>
                  <span>&bull;</span>
                  <span class="text-amber-400 font-bold">{{ formatTime(selectedShowtime.startTime) }}</span>
                </p>
              </div>
            </div>

            <!-- Step Progress Indicator -->
            <div v-if="bookingStep !== 'success'" class="hidden sm:flex items-center gap-2 text-xs font-bold">
              <span
                class="px-2.5 py-1 rounded-lg"
                :class="bookingStep === 'seats' ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-400'"
              >
                1. Chọn Ghế
              </span>
              <span class="text-slate-600">→</span>
              <span
                class="px-2.5 py-1 rounded-lg"
                :class="bookingStep === 'concessions' ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-400'"
              >
                2. Bắp Nước F&B
              </span>
              <span class="text-slate-600">→</span>
              <span
                class="px-2.5 py-1 rounded-lg"
                :class="bookingStep === 'checkout' ? 'bg-rose-600 text-white' : 'bg-slate-800 text-slate-400'"
              >
                3. Thanh Toán
              </span>
            </div>

            <button
              v-if="bookingStep !== 'success'"
              type="button"
              @click="bookingModalOpen = false"
              class="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Step 1: SEAT MAP -->
          <div v-if="bookingStep === 'seats'" class="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6">
            <!-- Curved Screen Indicator -->
            <div class="text-center space-y-2">
              <div class="w-3/4 max-w-lg mx-auto h-2 rounded-t-full bg-gradient-to-r from-rose-500/20 via-indigo-500 to-rose-500/20 shadow-lg shadow-indigo-500/30"></div>
              <p class="text-[11px] font-black uppercase tracking-widest text-slate-400">
                Màn Hình Chiếu / Cinema Screen
              </p>
            </div>

            <!-- Seat Map Grid -->
            <div v-if="loadingSeats" class="py-16 text-center text-slate-400 space-y-2">
              <div class="w-8 h-8 border-2 border-rose-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
              <p class="text-xs">Đang tải sơ đồ phòng chiếu...</p>
            </div>

            <div v-else class="space-y-2.5 max-w-xl mx-auto py-2">
              <div
                v-for="[rowLetter, rowSeats] in seatRows"
                :key="rowLetter"
                class="flex items-center justify-center gap-1.5 sm:gap-2"
              >
                <!-- Row Letter Left -->
                <span class="w-5 text-center text-xs font-black text-slate-500">{{ rowLetter }}</span>

                <!-- Seats in Row -->
                <div class="flex items-center gap-1 sm:gap-1.5">
                  <template v-for="seat in rowSeats" :key="seat.seatCode">
                    <!-- Standard / VIP Seat -->
                    <button
                      v-if="seat.zone !== 'SWEETBOX'"
                      type="button"
                      :disabled="seat.status !== 'AVAILABLE'"
                      @click="toggleSeat(seat)"
                      class="w-7 h-7 sm:w-8 sm:h-8 rounded-lg text-[10px] sm:text-xs font-black flex items-center justify-center transition cursor-pointer"
                      :class="[
                        seat.status !== 'AVAILABLE'
                          ? 'bg-slate-800 text-slate-600 cursor-not-allowed border border-slate-700/50'
                          : isSeatSelected(seat)
                          ? 'bg-rose-600 text-white ring-2 ring-rose-400 shadow-md shadow-rose-600/50 scale-105'
                          : seat.zone === 'VIP'
                          ? 'bg-amber-500/20 text-amber-300 border border-amber-500/50 hover:bg-amber-500 hover:text-slate-900'
                          : 'bg-slate-800 text-slate-300 border border-slate-700 hover:bg-indigo-600 hover:text-white',
                      ]"
                      :title="`${seat.seatCode} (${seat.zone}) - ${formatCurrency(seat.price)}`"
                    >
                      {{ seat.number }}
                    </button>

                    <!-- Sweetbox Double Couple Seat (Row J) -->
                    <button
                      v-else-if="seat.number % 2 !== 0"
                      type="button"
                      :disabled="seat.status !== 'AVAILABLE'"
                      @click="toggleSeat(seat)"
                      class="px-2.5 h-7 sm:h-8 rounded-xl text-[10px] sm:text-xs font-black flex items-center justify-center gap-1 transition cursor-pointer"
                      :class="[
                        seat.status !== 'AVAILABLE'
                          ? 'bg-slate-800 text-slate-600 cursor-not-allowed border border-slate-700/50'
                          : isSeatSelected(seat)
                          ? 'bg-rose-600 text-white ring-2 ring-rose-400 shadow-md shadow-rose-600/50 scale-105'
                          : 'bg-rose-500/20 text-rose-300 border border-rose-500/50 hover:bg-rose-600 hover:text-white',
                      ]"
                      :title="`Ghế Đôi Sweetbox ${seat.seatCode} - ${formatCurrency(seat.price)}`"
                    >
                      <Heart class="w-3 h-3 fill-current" />
                      <span>{{ seat.seatCode }}</span>
                    </button>
                  </template>
                </div>

                <!-- Row Letter Right -->
                <span class="w-5 text-center text-xs font-black text-slate-500">{{ rowLetter }}</span>
              </div>
            </div>

            <!-- Seat Legend -->
            <div class="flex flex-wrap items-center justify-center gap-4 sm:gap-6 pt-4 border-t border-slate-800 text-xs">
              <div class="flex items-center gap-2">
                <div class="w-5 h-5 rounded-md bg-slate-800 border border-slate-700"></div>
                <span class="text-slate-400">Thường</span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-5 h-5 rounded-md bg-amber-500/20 border border-amber-500/50"></div>
                <span class="text-amber-300">VIP</span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-6 h-5 rounded-md bg-rose-500/20 border border-rose-500/50 flex items-center justify-center text-[10px]">
                  <Heart class="w-3 h-3 text-rose-400 fill-rose-400" />
                </div>
                <span class="text-rose-400">Sweetbox Đôi</span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-5 h-5 rounded-md bg-rose-600"></div>
                <span class="text-white font-bold">Đang chọn</span>
              </div>
              <div class="flex items-center gap-2">
                <div class="w-5 h-5 rounded-md bg-slate-800 border border-slate-700/50 opacity-40"></div>
                <span class="text-slate-500">Đã bán</span>
              </div>
            </div>
          </div>

          <!-- Step 2: F&B CONCESSIONS -->
          <div v-else-if="bookingStep === 'concessions'" class="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6">
            <div class="space-y-1">
              <h4 class="font-black text-lg text-white flex items-center gap-2">
                <Sparkles class="w-5 h-5 text-amber-400" />
                <span>Bắp Rang Bơ & Nước Ngọt Thưởng Thức Trọn Vẹn</span>
              </h4>
              <p class="text-xs text-slate-400">
                Nhận combo ngay tại quầy Concession EventHub Cinema mà không cần xếp hàng mua vé!
              </p>
            </div>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div
                v-for="item in concessions"
                :key="item.id"
                class="rounded-2xl bg-slate-950 border border-slate-800 p-4 flex items-center justify-between gap-4 hover:border-slate-700 transition"
              >
                <div class="flex items-center gap-3">
                  <div class="w-14 h-14 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-center text-2xl shrink-0 overflow-hidden">
                    <img
                      v-if="item.imageUrl"
                      :src="item.imageUrl"
                      :alt="item.name"
                      class="w-full h-full object-cover"
                    />
                    <span v-else>🍿</span>
                  </div>
                  <div class="space-y-0.5">
                    <div class="flex items-center gap-1.5">
                      <span class="font-bold text-xs sm:text-sm text-white">{{ item.name }}</span>
                      <span
                        v-if="item.isHotDeal"
                        class="px-1.5 py-0.2 rounded text-[9px] font-black uppercase bg-amber-500/20 text-amber-400 border border-amber-500/40"
                      >
                        Hot Deal
                      </span>
                    </div>
                    <p class="text-[11px] text-slate-400 line-clamp-1">{{ item.description }}</p>
                    <p class="text-xs font-black text-rose-400">{{ formatCurrency(item.price) }}</p>
                  </div>
                </div>

                <!-- Quantity Counter -->
                <div class="flex items-center gap-2 shrink-0">
                  <button
                    type="button"
                    @click="updateConcessionQty(item.id, -1)"
                    :disabled="!selectedConcessions[item.id]"
                    class="w-7 h-7 rounded-lg bg-slate-800 hover:bg-slate-700 disabled:opacity-30 disabled:cursor-not-allowed text-white flex items-center justify-center font-bold text-xs cursor-pointer"
                  >
                    <Minus class="w-3.5 h-3.5" />
                  </button>
                  <span class="w-5 text-center text-xs font-black text-white">
                    {{ selectedConcessions[item.id] || 0 }}
                  </span>
                  <button
                    type="button"
                    @click="updateConcessionQty(item.id, 1)"
                    class="w-7 h-7 rounded-lg bg-rose-600 hover:bg-rose-500 text-white flex items-center justify-center font-bold text-xs cursor-pointer"
                  >
                    <Plus class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Step 3: CHECKOUT & COUPON -->
          <div v-else-if="bookingStep === 'checkout'" class="flex-1 overflow-y-auto p-4 sm:p-6 space-y-6">
            <h4 class="font-black text-lg text-white">Tóm Tắt & Xác Nhận Đặt Vé</h4>

            <!-- Order Recap Box -->
            <div class="rounded-2xl bg-slate-950 border border-slate-800 p-5 space-y-4">
              <!-- Showtime Recap -->
              <div class="flex items-center justify-between border-b border-slate-800 pb-3 text-xs">
                <span class="text-slate-400">Suất chiếu:</span>
                <span class="font-bold text-white text-right">
                  {{ selectedShowtime?.complexName }} - {{ formatTime(selectedShowtime?.startTime) }} ({{ selectedShowtime?.screenName }})
                </span>
              </div>

              <!-- Seats Recap -->
              <div class="flex items-center justify-between border-b border-slate-800 pb-3 text-xs">
                <span class="text-slate-400">Ghế đã chọn ({{ selectedSeats.length }}):</span>
                <span class="font-black text-rose-400">
                  {{ selectedSeats.map((s) => s.seatCode).join(', ') }} ({{ formatCurrency(seatsSubtotal) }})
                </span>
              </div>

              <!-- Concessions Recap -->
              <div v-if="concessionsSubtotal > 0" class="border-b border-slate-800 pb-3 space-y-1.5 text-xs">
                <div class="flex items-center justify-between font-bold text-slate-300">
                  <span>Bắp Nước F&B:</span>
                  <span>{{ formatCurrency(concessionsSubtotal) }}</span>
                </div>
                <div
                  v-for="[idStr, qty] in Object.entries(selectedConcessions)"
                  :key="idStr"
                  class="flex items-center justify-between text-[11px] text-slate-400 pl-2"
                >
                  <span>{{ concessions.find((c) => c.id === Number(idStr))?.name }} x{{ qty }}</span>
                  <span>{{ formatCurrency((concessions.find((c) => c.id === Number(idStr))?.price || 0) * qty) }}</span>
                </div>
              </div>

              <!-- Coupon Voucher Row -->
              <div class="pt-1">
                <div class="flex gap-2">
                  <input
                    v-model="couponInput"
                    type="text"
                    placeholder="Nhập mã coupon (VD: EVENTHUB2026)"
                    class="flex-1 px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white placeholder-slate-500 uppercase font-bold focus:outline-none focus:border-rose-500"
                  />
                  <button
                    type="button"
                    @click="applyCoupon"
                    class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-white border border-slate-700 transition cursor-pointer"
                  >
                    Áp Dụng
                  </button>
                </div>
                <p v-if="appliedDiscount > 0" class="text-xs text-emerald-400 font-bold mt-1.5 flex items-center gap-1">
                  <Check class="w-3.5 h-3.5" />
                  Đã áp dụng mã giảm 15% (-{{ formatCurrency(appliedDiscount) }})
                </p>
                <p v-if="couponError" class="text-xs text-rose-400 mt-1.5">
                  {{ couponError }}
                </p>
              </div>

              <!-- Total Row -->
              <div class="flex items-center justify-between pt-3 border-t border-slate-800">
                <span class="text-sm font-extrabold text-slate-300">Tổng Thanh Toán:</span>
                <span class="text-xl font-black text-white text-rose-500">
                  {{ formatCurrency(totalAmount) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Step 4: SUCCESS MODAL -->
          <div v-else-if="bookingStep === 'success'" class="flex-1 overflow-y-auto p-6 sm:p-8 text-center space-y-6">
            <div class="w-16 h-16 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 flex items-center justify-center mx-auto text-2xl">
              <Check class="w-8 h-8" />
            </div>

            <div class="space-y-1">
              <h3 class="text-2xl font-black text-white">Đặt Vé Phim Thành Công!</h3>
              <p class="text-xs sm:text-sm text-slate-400">
                Mã đặt vé của bạn là:
                <strong class="text-rose-400 font-mono text-base ml-1">{{ completedBooking?.bookingCode }}</strong>
              </p>
            </div>

            <div class="p-4 rounded-2xl bg-slate-950 border border-slate-800 max-w-sm mx-auto text-xs text-slate-300 space-y-2 text-left">
              <p><strong>Phim:</strong> {{ movie?.title }}</p>
              <p><strong>Cụm rạp:</strong> {{ selectedShowtime?.complexName }}</p>
              <p><strong>Phòng & Suất:</strong> {{ selectedShowtime?.screenName }} ({{ formatTime(selectedShowtime?.startTime) }})</p>
              <p><strong>Ghế:</strong> {{ selectedSeats.map((s) => s.seatCode).join(', ') }}</p>
              <p><strong>Tổng tiền:</strong> {{ formatCurrency(totalAmount) }}</p>
            </div>

            <div class="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
              <router-link
                to="/my-tickets"
                @click="bookingModalOpen = false"
                class="w-full sm:w-auto px-6 py-3 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-extrabold text-xs transition flex items-center justify-center gap-2"
              >
                <Ticket class="w-4 h-4" />
                <span>Xem Vé Trong "Vé Của Tôi"</span>
              </router-link>
              <button
                type="button"
                @click="bookingModalOpen = false"
                class="w-full sm:w-auto px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs transition"
              >
                Đóng
              </button>
            </div>
          </div>

          <!-- Modal Footer with Navigation & Action Buttons -->
          <div
            v-if="bookingStep !== 'success'"
            class="p-4 sm:p-5 bg-slate-950 border-t border-slate-800 flex items-center justify-between shrink-0"
          >
            <!-- Left Info: Total & Seats selected -->
            <div class="space-y-0.5">
              <p class="text-[11px] text-slate-400">
                <span class="font-bold text-white">{{ selectedSeats.length }} ghế</span>
                <span v-if="selectedSeats.length > 0">: {{ selectedSeats.map((s) => s.seatCode).join(', ') }}</span>
              </p>
              <p class="text-base sm:text-lg font-black text-rose-500">
                {{ formatCurrency(totalAmount) }}
              </p>
            </div>

            <!-- Right Buttons: Back & Next -->
            <div class="flex items-center gap-2">
              <button
                v-if="bookingStep === 'concessions'"
                type="button"
                @click="bookingStep = 'seats'"
                class="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 transition cursor-pointer"
              >
                ← Quay lại ghế
              </button>
              <button
                v-if="bookingStep === 'checkout'"
                type="button"
                @click="bookingStep = 'concessions'"
                class="px-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-xs font-bold text-slate-300 transition cursor-pointer"
              >
                ← Quay lại bắp nước
              </button>

              <!-- Next Button for Step 1 -->
              <button
                v-if="bookingStep === 'seats'"
                type="button"
                :disabled="selectedSeats.length === 0"
                @click="bookingStep = 'concessions'"
                class="px-6 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 disabled:opacity-40 disabled:cursor-not-allowed text-white font-extrabold text-xs transition flex items-center gap-1.5 shadow-md shadow-rose-600/30 cursor-pointer"
              >
                <span>Tiếp tục: Bắp nước</span>
                <ChevronRight class="w-4 h-4" />
              </button>

              <!-- Next Button for Step 2 -->
              <button
                v-if="bookingStep === 'concessions'"
                type="button"
                @click="bookingStep = 'checkout'"
                class="px-6 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-extrabold text-xs transition flex items-center gap-1.5 shadow-md shadow-rose-600/30 cursor-pointer"
              >
                <span>Tiếp tục: Thanh toán</span>
                <ChevronRight class="w-4 h-4" />
              </button>

              <!-- Confirm Button for Step 3 -->
              <button
                v-if="bookingStep === 'checkout'"
                type="button"
                :disabled="isSubmitting"
                @click="submitBooking"
                class="px-6 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 disabled:opacity-50 text-white font-black text-xs transition flex items-center gap-2 shadow-lg shadow-rose-600/30 cursor-pointer"
              >
                <div v-if="isSubmitting" class="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <Check v-else class="w-4 h-4" />
                <span>{{ isSubmitting ? 'Đang tạo đơn đặt vé...' : 'Xác Nhận & Đặt Vé' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Trailer Embed Modal -->
    <Teleport to="body">
      <div v-if="trailerModalOpen" class="fixed inset-0 z-50 overflow-hidden flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-black/90 backdrop-blur-sm transition-opacity" @click="trailerModalOpen = false"></div>
        <div class="relative w-full max-w-4xl bg-slate-900 rounded-2xl border border-slate-800 shadow-2xl overflow-hidden z-10 space-y-0">
          <div class="p-4 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
            <span class="font-extrabold text-sm text-white">{{ movie?.title }} - Official Trailer</span>
            <button
              type="button"
              @click="trailerModalOpen = false"
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
