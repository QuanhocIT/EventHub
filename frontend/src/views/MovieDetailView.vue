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
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const movie = ref<Movie | null>(null)
const showtimes = ref<Showtime[]>([])
const concessions = ref<ConcessionItem[]>([])
const loading = ref(true)
const loadingShowtimes = ref(false)

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
      return 'bg-amber-500 text-white border-amber-600'
    case 'T13':
      return 'bg-yellow-400 text-slate-900 border-yellow-500'
    case 'K':
      return 'bg-blue-600 text-white border-blue-700'
    case 'P':
    default:
      return 'bg-emerald-600 text-white border-emerald-700'
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 selection:bg-rose-500 selection:text-white">
    <!-- Loading State -->
    <div v-if="loading" class="max-w-7xl mx-auto px-4 py-20 text-center space-y-4">
      <div class="w-12 h-12 border-4 border-rose-500 border-t-transparent rounded-full animate-spin mx-auto"></div>
      <p class="text-sm text-slate-400">Đang tải thông tin phim và suất chiếu...</p>
    </div>

    <!-- Movie Content -->
    <div v-else-if="movie" class="space-y-10 pb-20">
      <!-- Movie Hero Backdrop Header -->
      <section class="relative overflow-hidden border-b border-slate-800 bg-slate-900 pt-8 pb-12 sm:pb-16">
        <div class="absolute inset-0 z-0">
          <img
            :src="movie.bannerUrl || movie.posterUrl"
            :alt="movie.title"
            class="w-full h-full object-cover object-center opacity-25 filter blur-xs scale-105"
          />
          <div class="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/90 to-slate-950/60"></div>
          <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-transparent to-transparent"></div>
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
                  class="w-56 sm:w-64 rounded-2xl shadow-2xl object-cover aspect-[2/3] border border-slate-800"
                />
                <button
                  v-if="movie.trailerUrl"
                  type="button"
                  @click="trailerModalOpen = true"
                  class="absolute inset-0 bg-slate-950/60 backdrop-blur-2xs flex flex-col items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity rounded-2xl cursor-pointer"
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
                  <span v-if="movie.formatDisplay" class="px-2 py-0.5 rounded bg-indigo-900/60 border border-indigo-500/40 text-indigo-300 text-xs uppercase font-extrabold">
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
                <p class="text-sm text-slate-300 leading-relaxed">
                  {{ movie.description }}
                </p>
              </div>

              <!-- Details Grid -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs text-slate-300 pt-2 border-t border-slate-800/80 max-w-2xl">
                <p><strong class="text-slate-400">Đạo diễn:</strong> {{ movie.director || 'Chưa cập nhật' }}</p>
                <p><strong class="text-slate-400">Ngôn ngữ:</strong> {{ movie.language || 'Phụ đề tiếng Việt' }}</p>
                <p class="sm:col-span-2"><strong class="text-slate-400">Diễn viên:</strong> {{ movie.actorsCast || 'Chưa cập nhật' }}</p>
              </div>

              <!-- Trailer Trigger Button -->
              <div v-if="movie.trailerUrl" class="pt-2">
                <button
                  type="button"
                  @click="trailerModalOpen = true"
                  class="px-4 py-2 rounded-xl bg-slate-800/90 hover:bg-slate-700 border border-slate-700 text-slate-200 font-bold text-xs transition flex items-center gap-2 cursor-pointer hover:border-slate-500"
                >
                  <Play class="w-3.5 h-3.5 text-rose-400 fill-rose-400" />
                  <span>Xem Trailer Chính Thức</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Showtime Matrix Section -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
        <div class="flex items-center justify-between border-b border-slate-800 pb-4">
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
            class="px-3.5 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-bold text-slate-300 focus:outline-none focus:border-rose-500 cursor-pointer"
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
              ? 'bg-rose-600 border-rose-600 text-white shadow-lg shadow-rose-600/30'
              : 'bg-slate-900 border-slate-800 text-slate-300 hover:border-slate-700'"
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
        <div v-else-if="showtimesByComplex.length === 0" class="rounded-2xl bg-slate-900 border border-slate-800 p-10 text-center space-y-3">
          <Clock class="w-10 h-10 text-slate-600 mx-auto" />
          <h3 class="text-base font-bold text-slate-400">Không có suất chiếu nào trong ngày đã chọn</h3>
          <p class="text-xs text-slate-500">Vui lòng chọn ngày chiếu khác hoặc đổi thành phố để xem thêm suất chiếu.</p>
        </div>

        <!-- Showtime Groups by Complex -->
        <div v-else class="space-y-6">
          <div
            v-for="complex in showtimesByComplex"
            :key="complex.complexId"
            class="rounded-2xl bg-slate-900 border border-slate-800 p-5 sm:p-6 space-y-5"
          >
            <!-- Complex Header -->
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-800 pb-4">
              <div class="space-y-1">
                <h3 class="font-extrabold text-base sm:text-lg text-white flex items-center gap-2">
                  <MapPin class="w-4 h-4 text-rose-500 shrink-0" />
                  <span>{{ complex.complexName }}</span>
                </h3>
                <span class="text-xs text-slate-400">{{ complex.city }}</span>
              </div>
              <span class="text-[11px] font-extrabold px-2.5 py-1 rounded bg-slate-800 text-indigo-400 border border-slate-700 self-start sm:self-auto">
                Ghế Sweetbox & VIP Khả dụng
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
                <div class="flex items-center gap-2 text-xs font-black uppercase text-indigo-400">
                  <Monitor class="w-3.5 h-3.5" />
                  <span>{{ formatName.replace('_', ' ') }}</span>
                </div>

                <!-- Showtime Chips -->
                <div class="flex flex-wrap gap-3">
                  <button
                    v-for="st in showtimesList"
                    :key="st.id"
                    type="button"
                    @click="openBookingFlow(st)"
                    class="group px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 hover:border-rose-500 hover:bg-slate-900 transition flex flex-col items-center gap-0.5 cursor-pointer min-w-[95px] shadow-sm hover:shadow-md hover:shadow-rose-950/40"
                  >
                    <span class="text-sm font-black text-white group-hover:text-rose-400 transition-colors">
                      {{ formatTime(st.startTime) }}
                    </span>
                    <span class="text-[10px] text-slate-400">
                      ~{{ formatTime(st.endTime) }}
                    </span>
                    <span class="text-[10px] font-extrabold text-amber-400 mt-0.5">
                      {{ formatCurrency(st.basePrice) }}
                    </span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- CINEMA BOOKING STEP FLOW MODAL -->
    <Teleport to="body">
      <div v-if="bookingModalOpen" class="fixed inset-0 z-50 overflow-hidden flex items-center justify-center p-2 sm:p-4">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/90 backdrop-blur-md transition-opacity" @click="bookingStep !== 'checkout' && !isSubmitting ? (bookingModalOpen = false) : null"></div>

        <!-- Modal Dialog -->
        <div class="relative w-full max-w-4xl bg-slate-900 border border-slate-800 rounded-3xl shadow-2xl flex flex-col max-h-[92vh] overflow-hidden z-10">
          <!-- Modal Header -->
          <div class="p-4 sm:p-5 bg-slate-950 border-b border-slate-800 flex items-center justify-between shrink-0">
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
