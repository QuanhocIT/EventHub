<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Event, Category } from '@/types'
import { eventService } from '@/services/event.service'
import EventCard from '@/components/EventCard.vue'
import {
  Search,
  Filter,
  RotateCcw,
  Sparkles,
  MapPin,
  Calendar,
  SlidersHorizontal,
  ChevronDown,
  ChevronRight,
  ChevronLeft,
  LayoutGrid,
  List,
  Tag,
  CheckCircle2,
  ShieldCheck,
  Headphones,
  CreditCard,
  Flame,
  X,
  Copy,
  Ticket,
  Clock,
  Compass,
  Cpu,
  Music,
  Trophy,
  Palette,
  Info,
  AlertCircle,
  Mail,
  Send,
  Play,
  Star,
  ArrowRight,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const allEvents = ref<Event[]>([])
const categories = ref<Category[]>([])
const loading = ref(true)
const viewMode = ref<'grid' | 'list'>('grid')
const showAdvancedFilters = ref(false)

// Pagination states (Exactly 8 events per page max)
const currentPage = ref(1)
const pageSize = 8

const totalEvents = computed(() => allEvents.value.length)
const totalPages = computed(() => Math.max(1, Math.ceil(totalEvents.value / pageSize)))

const displayedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return allEvents.value.slice(start, start + pageSize)
})

const displayedPageNumbers = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages: number[] = []

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push(-1)
    const start = Math.max(2, current - 1)
    const end = Math.min(total - 1, current + 1)
    for (let i = start; i <= end; i++) pages.push(i)
    if (current < total - 2) pages.push(-2)
    pages.push(total)
  }
  return pages
})

const changePage = (p: number) => {
  if (p < 1 || p > totalPages.value || p === currentPage.value) return
  currentPage.value = p
  const el = document.getElementById('events-results-header')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
}

// Filter states
const searchKeyword = ref((route.query.keyword as string) || '')
const selectedCategory = ref<number | null>(route.query.categoryId ? Number(route.query.categoryId) : null)
const selectedCity = ref<string>((route.query.city as string) || 'ALL')
const selectedTimeRange = ref<string>((route.query.timeRange as string) || 'ALL')
const selectedPriceRange = ref<string>((route.query.priceRange as string) || 'ALL')
const filterHasSeatMap = ref<boolean>(route.query.hasSeatMap === 'true')
const filterFeatured = ref<boolean>(route.query.featured === 'true')
const sortBy = ref<string>((route.query.sortBy as string) || 'date_asc')

// Toast notification
type ToastType = 'error' | 'success' | 'info'
const toasts = ref<{ id: number; message: string; type: ToastType }[]>([])

const notify = (message: string, type: ToastType = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3500)
}

// Popular trending hashtags
const trendingTags = [
  { label: 'Vietnam Tech Summit', query: 'Tech Summit' },
  { label: 'Skyline Symphony', query: 'Skyline' },
  { label: 'Midnight Marathon', query: 'Marathon' },
  { label: 'Van Gogh Expo', query: 'Van Gogh' },
  { label: 'Acoustic Festival', query: 'Indie' },
]

// Cities
const cities = [
  { id: 'ALL', name: 'Toàn quốc' },
  { id: 'Hà Nội', name: 'Hà Nội' },
  { id: 'TP. HCM', name: 'TP. Hồ Chí Minh' },
  { id: 'Đà Nẵng', name: 'Đà Nẵng' },
]

// Category icons map
const getCategoryIcon = (slug?: string) => {
  switch (slug) {
    case 'am-nhac':
      return Music
    case 'cong-nghe':
      return Cpu
    case 'the-thao':
      return Trophy
    case 'nghe-thuat':
      return Palette
    case 'trien-lam':
      return Compass
    default:
      return Sparkles
  }
}

// Parse price range
const getPriceLimits = (range: string) => {
  if (range === 'UNDER_500K') return { minPrice: 0, maxPrice: 500000 }
  if (range === '500K_1500K') return { minPrice: 500000, maxPrice: 1500000 }
  if (range === 'OVER_1500K') return { minPrice: 1500000, maxPrice: undefined }
  return { minPrice: undefined, maxPrice: undefined }
}

const fetchEvents = async () => {
  loading.value = true
  const { minPrice, maxPrice } = getPriceLimits(selectedPriceRange.value)
  try {
    const res = await eventService.getEvents({
      keyword: searchKeyword.value || undefined,
      categoryId: selectedCategory.value || undefined,
      city: selectedCity.value !== 'ALL' ? selectedCity.value : undefined,
      timeRange: selectedTimeRange.value !== 'ALL' ? selectedTimeRange.value : undefined,
      minPrice,
      maxPrice,
      hasSeatMap: filterHasSeatMap.value || undefined,
      featured: filterFeatured.value || undefined,
      sortBy: sortBy.value,
      size: 100,
    })
    allEvents.value = res.content
    if (currentPage.value > Math.max(1, Math.ceil(allEvents.value.length / pageSize))) {
      currentPage.value = 1
    }
  } finally {
    loading.value = false
  }
}

// Hero Spotlight Events Slider Data
const heroEvents = [
  {
    id: 1,
    title: 'Born Pink World Tour Vietnam',
    subtitle: 'BlackPink World Stadium Live Tour',
    badge: 'TOP #1 ĐẠI NHẠC HỘI NĂM',
    tagline: 'Born Pink Stadium Live',
    synopsis:
      'Đại nhạc hội bùng nổ cảm xúc cùng 4 cô gái BlackPink với hệ thống sân khấu ánh sáng 360°, âm thanh vòm quốc tế và danh sách bản hit đình đám làm say đắm hơn 120.000 khán giả Việt Nam.',
    date: '30.11.2025 • 19:30',
    venue: 'Sân Vận Động Quốc Gia Mỹ Đình, Hà Nội',
    category: 'Âm nhạc & Live Concert',
    price: 'Từ 1.200.000 đ',
    format: 'VÉ VIP SOUNDCHECK',
    imageUrl: '/images/event_blackpink.jpg',
    backdropUrl: '/images/about_concert_crowd.jpg',
  },
  {
    id: 2,
    title: 'Live Tour Concert J97: Bừng Sáng',
    subtitle: 'Jack - J97 Live In Concert',
    badge: 'TOP #2 XU HƯỚNG ÂM NHẠC',
    tagline: 'Domdom Special Concert',
    synopsis:
      'Chuyến lưu diễn quy mô lớn nhất sự nghiệp của Jack - J97 với những bản phối hoàn toàn mới cùng dàn nghệ sĩ khách mời hàng đầu và hiệu ứng laser pháo hoa hoành tráng.',
    date: '15.11.2025 • 20:00',
    venue: 'Sân Vận Động Quân Khu 7, TP. Hồ Chí Minh',
    category: 'Âm nhạc & EDM Festival',
    price: 'Từ 650.000 đ',
    format: 'FANZONE & EARLY BIRD',
    imageUrl: '/images/event_jack.jpg',
    backdropUrl: '/images/about_hero_concert.jpg',
  },
  {
    id: 3,
    title: 'Lee Min Ho Fan Meeting Vietnam 2026',
    subtitle: 'Exclusive Asia Tour Fan Meeting',
    badge: 'TOP #3 GIAO LƯU QUỐC TẾ',
    tagline: 'Minoz Special Evening',
    synopsis:
      'Cơ hội độc quyền giao lưu trực tiếp, ký tặng poster và chụp hình cùng nam tài tử Lee Min Ho trong đêm fan meeting đặc biệt dành riêng cho người hâm mộ Việt Nam.',
    date: '28.12.2025 • 18:30',
    venue: 'Trung tâm Hội nghị Quốc gia, Hà Nội',
    category: 'Gặp gỡ & Fan Meeting',
    price: 'Từ 950.000 đ',
    format: 'HI-TOUCH & PHOTO VIP',
    imageUrl: '/images/event_leeminho.jpg',
    backdropUrl: '/images/about_hero_exact.jpg',
  },
  {
    id: 4,
    title: 'Skyline Symphony & Countdown 2026',
    subtitle: 'Vietnam National Symphony Orchestra',
    badge: 'TOP #4 ĐÊM NGHỆ THUẬT ĐỈNH CAO',
    tagline: 'Symphony Under The Stars',
    synopsis:
      'Trải nghiệm đỉnh cao của âm nhạc giao hưởng hàn lâm kết hợp âm hưởng đương đại với 80 nghệ sĩ tài hoa, mang đến đêm đại tiệc nghệ thuật chào đón năm mới rực rỡ.',
    date: '31.12.2025 • 21:00',
    venue: 'Trung Tâm SECC, TP. Hồ Chí Minh',
    category: 'Nghệ thuật & Giao hưởng',
    price: 'Từ 500.000 đ',
    format: 'PREMIUM SEAT & CHAMPAGNE',
    imageUrl: '/images/about_cinema_hall.jpg',
    backdropUrl: '/images/about_hero_real.jpg',
  },
]

const currentHeroIndex = ref(0)
const currentHero = computed(() => heroEvents[currentHeroIndex.value])
const isHeroPaused = ref(false)
const SLIDE_DURATION = 6000
const progressKey = ref(0)
let heroTimer: any = null

const startHeroTimer = () => {
  stopHeroTimer()
  heroTimer = setInterval(() => {
    if (!isHeroPaused.value) {
      nextHero()
    }
  }, SLIDE_DURATION)
}

const stopHeroTimer = () => {
  if (heroTimer) {
    clearInterval(heroTimer)
    heroTimer = null
  }
}

const nextHero = () => {
  currentHeroIndex.value = (currentHeroIndex.value + 1) % heroEvents.length
  progressKey.value++
}

const prevHero = () => {
  currentHeroIndex.value = (currentHeroIndex.value - 1 + heroEvents.length) % heroEvents.length
  progressKey.value++
}

const selectHero = (idx: number) => {
  currentHeroIndex.value = idx
  progressKey.value++
}

const goToEventDetail = (hero: (typeof heroEvents)[0]) => {
  const matched = allEvents.value.find((e) =>
    e.title.toLowerCase().includes(hero.title.slice(0, 8).toLowerCase())
  )
  if (matched) {
    router.push(`/events/${matched.id}`)
  } else if (allEvents.value.length > 0) {
    router.push(`/events/${allEvents.value[0].id}`)
  } else {
    notify(`Đang mở cổng bán vé cho ${hero.title}`, 'info')
  }
}

onMounted(async () => {
  startHeroTimer()
  categories.value = await eventService.getCategories()
  await fetchEvents()
})

onUnmounted(() => {
  stopHeroTimer()
})

// Sync query params
watch(
  () => route.query,
  () => {
    searchKeyword.value = (route.query.keyword as string) || ''
    selectedCategory.value = route.query.categoryId ? Number(route.query.categoryId) : null
    selectedCity.value = (route.query.city as string) || 'ALL'
    selectedTimeRange.value = (route.query.timeRange as string) || 'ALL'
    selectedPriceRange.value = (route.query.priceRange as string) || 'ALL'
    filterHasSeatMap.value = route.query.hasSeatMap === 'true'
    filterFeatured.value = route.query.featured === 'true'
    sortBy.value = (route.query.sortBy as string) || 'date_asc'
    currentPage.value = 1
    fetchEvents()
  }
)

const applyFilters = () => {
  currentPage.value = 1
  router.push({
    query: {
      ...(searchKeyword.value ? { keyword: searchKeyword.value } : {}),
      ...(selectedCategory.value ? { categoryId: selectedCategory.value } : {}),
      ...(selectedCity.value !== 'ALL' ? { city: selectedCity.value } : {}),
      ...(selectedTimeRange.value !== 'ALL' ? { timeRange: selectedTimeRange.value } : {}),
      ...(selectedPriceRange.value !== 'ALL' ? { priceRange: selectedPriceRange.value } : {}),
      ...(filterHasSeatMap.value ? { hasSeatMap: 'true' } : {}),
      ...(filterFeatured.value ? { featured: 'true' } : {}),
      ...(sortBy.value !== 'date_asc' ? { sortBy: sortBy.value } : {}),
    },
  })
}

const selectCategoryTab = (catId: number | null) => {
  currentPage.value = 1
  selectedCategory.value = catId
  applyFilters()
}

const applyTag = (query: string) => {
  currentPage.value = 1
  searchKeyword.value = query
  applyFilters()
}

const resetFilters = () => {
  currentPage.value = 1
  searchKeyword.value = ''
  selectedCategory.value = null
  selectedCity.value = 'ALL'
  selectedTimeRange.value = 'ALL'
  selectedPriceRange.value = 'ALL'
  filterHasSeatMap.value = false
  filterFeatured.value = false
  sortBy.value = 'date_asc'
  router.push({ query: {} })
}

// Active Filter Chips computation
const activeFilterChips = computed(() => {
  const chips: { label: string; clear: () => void }[] = []
  if (searchKeyword.value) {
    chips.push({ label: `Từ khóa: "${searchKeyword.value}"`, clear: () => { searchKeyword.value = ''; applyFilters() } })
  }
  if (selectedCategory.value) {
    const c = categories.value.find((cat) => cat.id === selectedCategory.value)
    if (c) chips.push({ label: `Danh mục: ${c.name}`, clear: () => { selectedCategory.value = null; applyFilters() } })
  }
  if (selectedCity.value !== 'ALL') {
    chips.push({ label: `Địa điểm: ${selectedCity.value}`, clear: () => { selectedCity.value = 'ALL'; applyFilters() } })
  }
  if (selectedTimeRange.value !== 'ALL') {
    const timeLabel = selectedTimeRange.value === 'THIS_WEEK' ? '7 ngày tới' : selectedTimeRange.value === 'THIS_MONTH' ? 'Tháng này' : 'Sắp tới'
    chips.push({ label: `Thời gian: ${timeLabel}`, clear: () => { selectedTimeRange.value = 'ALL'; applyFilters() } })
  }
  if (selectedPriceRange.value !== 'ALL') {
    const pLabel = selectedPriceRange.value === 'UNDER_500K' ? 'Dưới 500k' : selectedPriceRange.value === '500K_1500K' ? '500k - 1.5 triệu' : 'Trên 1.5 triệu'
    chips.push({ label: `Mức giá: ${pLabel}`, clear: () => { selectedPriceRange.value = 'ALL'; applyFilters() } })
  }
  if (filterHasSeatMap.value) {
    chips.push({ label: 'Có chọn chỗ ngồi', clear: () => { filterHasSeatMap.value = false; applyFilters() } })
  }
  if (filterFeatured.value) {
    chips.push({ label: 'Sự kiện Hot', clear: () => { filterFeatured.value = false; applyFilters() } })
  }
  return chips
})

const copyPromo = async () => {
  try {
    await navigator.clipboard.writeText('EVENTHUB2026')
    notify('Đã sao chép mã giảm giá EVENTHUB2026 (-15%)!', 'success')
  } catch {
    notify('Mã giảm giá: EVENTHUB2026', 'info')
  }
}

const newsletterEmail = ref('')
const subscribeNewsletter = () => {
  const email = newsletterEmail.value.trim()
  if (!email || !email.includes('@')) {
    notify('Vui lòng nhập địa chỉ email hợp lệ để nhận ưu đãi', 'error')
    return
  }
  notify(`Đăng ký thành công! Ưu đãi đã được gửi đến ${email}`, 'success')
  newsletterEmail.value = ''
}
</script>

<template>
  <div class="min-h-screen bg-[#070A12] pb-20 text-slate-100 selection:bg-blue-600 selection:text-white space-y-6 sm:space-y-8">
    <!-- Breadcrumb & Top Bar -->
    <div class="border-b border-slate-800/80 bg-slate-950/70 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between text-xs text-slate-400">
        <div class="flex items-center gap-2">
          <router-link to="/" class="hover:text-blue-400 transition">Trang chủ</router-link>
          <ChevronRight class="w-3.5 h-3.5 text-slate-600" />
          <span class="font-bold text-white">Khám phá sự kiện</span>
        </div>
        <div class="hidden sm:flex items-center gap-2 text-blue-400 font-bold">
          <Sparkles class="w-3.5 h-3.5 text-amber-400 animate-pulse" />
          <span>Hơn 50+ sự kiện trực tiếp đang mở bán vé</span>
        </div>
      </div>
    </div>

    <!-- 1. HERO SPOTLIGHT CAROUSEL CHO ĐẠI NHẠC HỘI & SỰ KIỆN TÂM ĐIỂM -->
    <section
      class="relative overflow-hidden border-b border-slate-800 bg-slate-950 py-8 lg:py-12 transition-all duration-700"
      @mouseenter="isHeroPaused = true"
      @mouseleave="isHeroPaused = false"
    >
      <!-- Background Backdrop with Dark Gradient Overlays -->
      <div class="absolute inset-0 z-0 overflow-hidden">
        <Transition name="hero-bg" mode="out-in">
          <img
            :key="currentHero.id"
            :src="currentHero.backdropUrl || currentHero.imageUrl"
            :alt="currentHero.title"
            class="w-full h-full object-cover object-center opacity-30 filter blur-xs scale-105 transform duration-1000 ken-burns"
          />
        </Transition>
        <div class="absolute inset-0 bg-gradient-to-r from-[#070A12] via-[#070A12]/90 to-transparent"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-[#070A12] via-[#070A12]/60 to-transparent"></div>
        <div class="absolute top-0 right-1/4 w-[450px] h-[450px] bg-rose-600/15 rounded-full blur-[140px] pointer-events-none"></div>
        <div class="absolute bottom-0 left-10 w-[400px] h-[400px] bg-blue-600/15 rounded-full blur-[140px] pointer-events-none"></div>
      </div>

      <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
        <!-- Main Spotlight Row -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Left Content (lg:col-span-8) -->
          <div class="lg:col-span-8 space-y-4">
            <div class="flex flex-wrap items-center gap-3">
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/20 border border-rose-500/30 text-rose-400 text-xs font-black tracking-wide uppercase">
                <Flame class="w-3.5 h-3.5 text-rose-400 animate-pulse" />
                <span>{{ currentHero.badge }}</span>
              </div>

              <!-- Mini Carousel Arrow Navigation -->
              <div class="flex items-center gap-1.5 ml-auto sm:ml-0 bg-slate-900/90 border border-slate-800 rounded-lg p-1">
                <button
                  type="button"
                  @click="prevHero"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Sự kiện trước"
                >
                  <ChevronLeft class="w-4 h-4" />
                </button>
                <span class="text-xs font-mono font-bold px-1.5 text-slate-400">
                  {{ currentHeroIndex + 1 }} / {{ heroEvents.length }}
                </span>
                <button
                  type="button"
                  @click="nextHero"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Sự kiện tiếp theo"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- Main Title with Crossfade Transition -->
            <Transition name="fade" mode="out-in">
              <h1 :key="currentHero.title" class="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white leading-tight">
                {{ currentHero.title }}
              </h1>
            </Transition>

            <!-- Meta Tags -->
            <Transition name="fade" mode="out-in">
              <div :key="currentHero.id" class="flex flex-wrap items-center gap-2.5 sm:gap-3 text-xs sm:text-sm font-semibold text-slate-300">
                <span class="flex items-center gap-1.5 text-amber-400 font-bold bg-amber-400/10 border border-amber-400/20 px-2.5 py-0.5 rounded-lg">
                  <Calendar class="w-3.5 h-3.5" />
                  {{ currentHero.date }}
                </span>
                <span>&bull;</span>
                <span class="flex items-center gap-1 text-slate-200">
                  <MapPin class="w-3.5 h-3.5 text-rose-400 shrink-0" />
                  <span class="line-clamp-1">{{ currentHero.venue }}</span>
                </span>
                <span>&bull;</span>
                <span class="text-rose-400 font-bold">{{ currentHero.category }}</span>
                <span class="px-2 py-0.5 rounded bg-indigo-900/60 border border-indigo-500/40 text-indigo-300 text-xs uppercase font-extrabold">
                  {{ currentHero.format }}
                </span>
              </div>
            </Transition>

            <!-- Synopsis Paragraph -->
            <Transition name="fade" mode="out-in">
              <p :key="currentHero.id + '-synopsis'" class="text-sm sm:text-base text-slate-300 line-clamp-2 max-w-2xl leading-relaxed">
                {{ currentHero.synopsis }}
              </p>
            </Transition>

            <div class="flex items-center gap-2 text-xs text-slate-400">
              <Ticket class="w-3.5 h-3.5 text-blue-400" />
              <span>Giá vé chính thức: <strong class="text-white font-bold">{{ currentHero.price }}</strong> &bull; Hoàn tiền 100% nếu sự kiện bị hủy</span>
            </div>

            <!-- CTA Buttons -->
            <div class="flex flex-wrap items-center gap-3 pt-2">
              <button
                type="button"
                @click="goToEventDetail(currentHero)"
                class="px-6 py-3 rounded-xl bg-gradient-to-r from-rose-600 via-pink-600 to-indigo-600 hover:from-rose-500 hover:to-indigo-500 text-white font-extrabold text-sm sm:text-base shadow-lg shadow-rose-600/30 transition flex items-center gap-2 group cursor-pointer hover:scale-105 active:scale-95"
              >
                <Ticket class="w-5 h-5 group-hover:rotate-12 transition-transform" />
                <span>Đặt Vé Suất Diễn Ngay</span>
              </button>

              <button
                type="button"
                @click="goToEventDetail(currentHero)"
                class="px-5 py-3 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 text-slate-200 font-bold text-sm transition flex items-center gap-2 cursor-pointer hover:border-slate-500"
              >
                <Sparkles class="w-4 h-4 text-amber-400" />
                <span>Xem Sơ Đồ & Khán Đài</span>
              </button>
            </div>
          </div>

          <!-- Featured Poster Card (lg:col-span-4) -->
          <div class="hidden lg:flex lg:col-span-4 justify-end">
            <div
              class="relative group cursor-pointer"
              @click="goToEventDetail(currentHero)"
            >
              <div class="absolute -inset-1 rounded-2xl bg-gradient-to-r from-rose-600 via-purple-600 to-indigo-600 opacity-50 blur group-hover:opacity-85 transition duration-500"></div>
              <Transition name="fade" mode="out-in">
                <img
                  :key="currentHero.imageUrl"
                  :src="currentHero.imageUrl"
                  :alt="currentHero.title"
                  class="relative w-64 lg:w-72 rounded-2xl shadow-2xl object-cover aspect-[2/3] border border-slate-700/80 transform group-hover:scale-[1.03] transition duration-300"
                />
              </Transition>
              <div class="absolute top-3 right-3 px-2 py-1 rounded bg-black/80 backdrop-blur-xs text-[10px] font-black uppercase text-rose-400 border border-rose-500/30">
                {{ currentHero.format?.split('&')[0] || 'VIP PASS' }}
              </div>
              <div class="absolute bottom-3 left-3 right-3 px-3 py-1.5 rounded-lg bg-slate-950/85 backdrop-blur-xs text-xs font-bold text-slate-200 border border-slate-800 flex items-center justify-between">
                <span>{{ currentHero.tagline }}</span>
                <span class="text-rose-400 text-[11px] font-semibold">Xem chi tiết &rarr;</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Top 4 Events Selector Strip with Live Progress -->
        <div class="mt-8 pt-6 border-t border-slate-800/80">
          <div class="flex items-center justify-between mb-3.5">
            <div class="flex items-center gap-2">
              <Trophy class="w-4 h-4 text-amber-400" />
              <span class="text-xs uppercase font-extrabold tracking-wider text-slate-300">
                Top {{ heroEvents.length }} Đại Nhạc Hội & Sự Kiện Dẫn Đầu Xu Hướng - Bấm để chọn xem
              </span>
            </div>
            <div class="hidden sm:flex items-center gap-2 text-xs text-slate-400 font-medium">
              <span class="inline-block w-2 h-2 rounded-full bg-rose-500 animate-pulse"></span>
              <span>Tự động chuyển tiếp sau 6s</span>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div
              v-for="(evt, idx) in heroEvents"
              :key="evt.id"
              @click="selectHero(idx)"
              class="group relative flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-all duration-300 border backdrop-blur-xs overflow-hidden"
              :class="[
                currentHeroIndex === idx
                  ? 'bg-rose-500/15 border-rose-500/60 shadow-lg shadow-rose-500/15 scale-[1.02]'
                  : 'bg-slate-900/60 hover:bg-slate-900/90 border-slate-800 hover:border-slate-700 opacity-70 hover:opacity-100'
              ]"
            >
              <!-- Index Badge -->
              <span
                class="w-7 h-7 rounded-lg flex items-center justify-center text-xs font-black shrink-0 transition"
                :class="[
                  currentHeroIndex === idx
                    ? 'bg-rose-500 text-white shadow-md shadow-rose-500/40'
                    : 'bg-slate-800 text-slate-400 group-hover:text-white'
                ]"
              >
                #{{ idx + 1 }}
              </span>

              <!-- Thumbnail -->
              <img
                :src="evt.imageUrl"
                :alt="evt.title"
                class="w-11 h-11 rounded-lg object-cover border border-slate-700 shrink-0 group-hover:scale-105 transition-transform"
              />

              <!-- Text Info -->
              <div class="min-w-0 flex-1">
                <div class="flex items-center gap-1.5">
                  <span class="text-[10px] font-bold text-amber-400 uppercase tracking-wide">{{ evt.category.split('&')[0] }}</span>
                  <span
                    v-if="currentHeroIndex === idx"
                    class="ml-auto text-[9px] font-bold px-1.5 py-0.2 rounded bg-rose-500 text-white uppercase tracking-wider animate-pulse"
                  >
                    ĐANG XEM
                  </span>
                </div>
                <h4 class="text-xs font-bold text-white truncate group-hover:text-rose-400 transition">
                  {{ evt.title }}
                </h4>
                <p class="text-[10px] text-slate-400 truncate">{{ evt.venue.split(',')[0] }}</p>
              </div>

              <!-- Animated Progress Bar for Active Slide -->
              <div
                v-if="currentHeroIndex === idx"
                :key="progressKey"
                class="absolute bottom-0 left-0 h-0.5 bg-gradient-to-r from-rose-500 via-pink-400 to-indigo-400 hero-progress-fill"
                :class="{ 'hero-progress-paused': isHeroPaused }"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 2. INTEGRATED SEARCH & FILTER HUB BAR -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-2">
      <div class="bg-[#0D121F]/90 backdrop-blur-md p-3.5 sm:p-5 rounded-2xl border border-slate-800 shadow-2xl space-y-3 relative overflow-hidden">
        <div class="absolute -top-12 -right-12 w-48 h-48 bg-blue-600/10 rounded-full blur-2xl pointer-events-none"></div>

        <div class="grid grid-cols-1 md:grid-cols-12 gap-2.5 relative z-10">
          <!-- Keyword input -->
          <div class="md:col-span-5 relative">
            <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <input
              v-model="searchKeyword"
              @keyup.enter="applyFilters"
              type="text"
              placeholder="Tìm tên concert, nghệ sĩ, giải chạy, địa điểm..."
              class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700/80 text-xs sm:text-sm text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:border-rose-500 transition"
            />
            <button
              v-if="searchKeyword"
              @click="searchKeyword = ''; applyFilters()"
              class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
            >
              <X class="w-3.5 h-3.5" />
            </button>
          </div>

          <!-- City Select -->
          <div class="md:col-span-3 relative">
            <MapPin class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <select
              v-model="selectedCity"
              @change="applyFilters"
              class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700/80 text-xs sm:text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-rose-500 transition cursor-pointer font-medium appearance-none"
            >
              <option v-for="c in cities" :key="c.id" :value="c.id" class="bg-slate-900 text-white">{{ c.name }}</option>
            </select>
            <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
          </div>

          <!-- Time Range Select -->
          <div class="md:col-span-2 relative">
            <Calendar class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <select
              v-model="selectedTimeRange"
              @change="applyFilters"
              class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-900/90 border border-slate-700/80 text-xs sm:text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-rose-500 transition cursor-pointer font-medium appearance-none"
            >
              <option value="ALL" class="bg-slate-900 text-white">Mọi thời điểm</option>
              <option value="THIS_WEEK" class="bg-slate-900 text-white">7 ngày tới</option>
              <option value="THIS_MONTH" class="bg-slate-900 text-white">Tháng này</option>
              <option value="UPCOMING" class="bg-slate-900 text-white">Sắp diễn ra</option>
            </select>
            <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
          </div>

          <!-- Action Buttons: Apply & Advanced Filter Toggle -->
          <div class="md:col-span-2 flex items-center gap-1.5">
            <button
              @click="applyFilters"
              class="flex-1 py-2.5 px-3 rounded-xl bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white text-xs font-black flex items-center justify-center gap-1.5 shadow-md shadow-rose-600/30 transition cursor-pointer"
            >
              <Search class="w-3.5 h-3.5" />
              <span>Tìm kiếm</span>
            </button>

            <button
              type="button"
              @click="showAdvancedFilters = !showAdvancedFilters"
              class="p-2.5 rounded-xl border transition cursor-pointer"
              :class="showAdvancedFilters || filterHasSeatMap || filterFeatured || selectedPriceRange !== 'ALL' ? 'bg-rose-600/20 text-rose-400 border-rose-500/50' : 'bg-slate-900 hover:bg-slate-800 text-slate-400 border-slate-700'"
              title="Bộ lọc nâng cao"
            >
              <SlidersHorizontal class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Collapsible Advanced Filters Drawer -->
        <div
          v-if="showAdvancedFilters"
          class="pt-3 border-t border-slate-800 grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs"
        >
          <!-- Price Range Dropdown -->
          <div>
            <label class="block font-bold text-slate-300 mb-1">Khoảng giá vé</label>
            <select
              v-model="selectedPriceRange"
              @change="applyFilters"
              class="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 font-medium text-slate-200 focus:outline-none focus:ring-2 focus:ring-rose-500"
            >
              <option value="ALL" class="bg-slate-900 text-white">Tất cả mức giá</option>
              <option value="UNDER_500K" class="bg-slate-900 text-white">Dưới 500.000 đ</option>
              <option value="500K_1500K" class="bg-slate-900 text-white">500.000 đ &ndash; 1.500.000 đ</option>
              <option value="OVER_1500K" class="bg-slate-900 text-white">Trên 1.500.000 đ</option>
            </select>
          </div>

          <!-- Feature Checkboxes -->
          <div class="flex flex-col justify-end gap-2 pb-1">
            <label class="flex items-center gap-2 cursor-pointer select-none">
              <input
                v-model="filterHasSeatMap"
                @change="applyFilters"
                type="checkbox"
                class="w-4 h-4 rounded text-rose-600 focus:ring-rose-500 border-slate-700 bg-slate-900 cursor-pointer"
              />
              <span class="font-medium text-slate-300">Chỉ sự kiện có sơ đồ chọn ghế ngồi</span>
            </label>

            <label class="flex items-center gap-2 cursor-pointer select-none">
              <input
                v-model="filterFeatured"
                @change="applyFilters"
                type="checkbox"
                class="w-4 h-4 rounded text-rose-600 focus:ring-rose-500 border-slate-700 bg-slate-900 cursor-pointer"
              />
              <span class="font-medium text-slate-300">Chỉ sự kiện Hot / Thịnh hành</span>
            </label>
          </div>

          <!-- Reset All in Drawer -->
          <div class="flex items-end justify-end">
            <button
              type="button"
              @click="resetFilters"
              class="px-4 py-2 rounded-xl border border-slate-700 text-slate-300 hover:bg-slate-800 font-bold transition flex items-center gap-1.5 cursor-pointer"
            >
              <RotateCcw class="w-3.5 h-3.5" />
              <span>Đặt lại tất cả</span>
            </button>
          </div>
        </div>

        <!-- Trending Keyword Tags -->
        <div class="flex items-center gap-2 mt-3.5 text-xs text-slate-400 overflow-x-auto pb-1 scrollbar-none">
          <span class="font-bold text-slate-400 shrink-0 flex items-center gap-1">
            <Flame class="w-3.5 h-3.5 text-rose-500 animate-pulse" />
            <span>Tìm kiếm phổ biến:</span>
          </span>
          <button
            v-for="tag in trendingTags"
            :key="tag.query"
            type="button"
            @click="applyTag(tag.query)"
            class="px-2.5 py-1 rounded-lg bg-slate-900/80 hover:bg-rose-600/20 hover:text-rose-400 text-slate-300 border border-slate-800 hover:border-rose-500/30 text-xs font-semibold whitespace-nowrap transition cursor-pointer"
          >
            #{{ tag.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- Category Tabs Navigation Bar -->
    <div class="border-b border-slate-800/80 bg-slate-950/70 backdrop-blur-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center gap-2 overflow-x-auto py-3 scrollbar-none text-xs font-bold">
          <!-- All Categories Tab -->
          <button
            type="button"
            @click="selectCategoryTab(null)"
            class="px-4 py-2 rounded-xl transition flex items-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer"
            :class="[
              selectedCategory === null
                ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-black shadow-lg shadow-blue-600/30 border border-blue-400/40'
                : 'bg-slate-900/80 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800/80'
            ]"
          >
            <Compass class="w-3.5 h-3.5" />
            <span>Tất cả sự kiện</span>
          </button>

          <!-- Individual Category Tabs -->
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            @click="selectCategoryTab(cat.id)"
            class="px-4 py-2 rounded-xl transition flex items-center gap-1.5 whitespace-nowrap shrink-0 cursor-pointer"
            :class="[
              selectedCategory === cat.id
                ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-black shadow-lg shadow-blue-600/30 border border-blue-400/40'
                : 'bg-slate-900/80 text-slate-400 hover:text-white hover:bg-slate-800 border border-slate-800/80'
            ]"
          >
            <component :is="getCategoryIcon(cat.slug)" class="w-3.5 h-3.5" />
            <span>{{ cat.name }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div id="events-results-header" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6">
      <!-- Active Filter Tags Row & Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
        <!-- Results Counter & Active Chips -->
        <div class="flex flex-wrap items-center gap-2">
          <span class="text-slate-400 font-medium">
            Tìm thấy <strong class="text-white font-black text-sm">{{ totalEvents }}</strong> sự kiện
            <span v-if="totalEvents > 0" class="text-slate-500 font-normal ml-1">
              (Trang {{ currentPage }}/{{ totalPages }})
            </span>
          </span>

          <!-- Chips -->
          <div v-for="(chip, idx) in activeFilterChips" :key="idx" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-500/15 text-blue-300 text-[11px] font-semibold border border-blue-500/30">
            <span>{{ chip.label }}</span>
            <button type="button" @click="chip.clear" class="hover:text-white p-0.5">
              <X class="w-3 h-3" />
            </button>
          </div>

          <button
            v-if="activeFilterChips.length > 0"
            type="button"
            @click="resetFilters"
            class="text-[11px] text-rose-400 hover:text-rose-300 hover:underline font-semibold ml-1 cursor-pointer"
          >
            Xóa bộ lọc
          </button>
        </div>

        <!-- Sort Dropdown & View Mode Switcher -->
        <div class="flex items-center gap-3 self-end sm:self-auto">
          <!-- Sort -->
          <div class="flex items-center gap-1.5">
            <span class="text-slate-400 font-medium hidden md:inline">Sắp xếp:</span>
            <select
              v-model="sortBy"
              @change="applyFilters"
              class="px-3 py-1.5 rounded-xl border border-slate-700 bg-slate-900 text-xs font-semibold text-slate-200 focus:outline-none focus:ring-1 focus:ring-blue-500 cursor-pointer shadow-md"
            >
              <option value="date_asc" class="bg-slate-900 text-white">Sắp diễn ra nhất</option>
              <option value="featured" class="bg-slate-900 text-white">Nổi bật / Thịnh hành</option>
              <option value="price_asc" class="bg-slate-900 text-white">Giá vé: Thấp &rarr; Cao</option>
              <option value="price_desc" class="bg-slate-900 text-white">Giá vé: Cao &rarr; Thấp</option>
              <option value="date_desc" class="bg-slate-900 text-white">Sự kiện xa nhất</option>
            </select>
          </div>

          <!-- View Mode Toggle -->
          <div class="inline-flex p-0.5 bg-slate-900 rounded-xl border border-slate-800">
            <button
              type="button"
              @click="viewMode = 'grid'"
              class="p-1.5 rounded-lg transition cursor-pointer"
              :class="viewMode === 'grid' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-400 hover:text-white'"
              title="Xem dạng lưới"
            >
              <LayoutGrid class="w-4 h-4" />
            </button>
            <button
              type="button"
              @click="viewMode = 'list'"
              class="p-1.5 rounded-lg transition cursor-pointer"
              :class="viewMode === 'list' ? 'bg-blue-600 text-white shadow-sm' : 'text-slate-400 hover:text-white'"
              title="Xem dạng danh sách"
            >
              <List class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Loading Skeleton -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="i in 8" :key="i" class="h-80 rounded-2xl bg-slate-900 border border-slate-800 animate-pulse"></div>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="totalEvents === 0"
        class="text-center py-16 px-4 bg-[#0D121F] rounded-3xl border border-slate-800 shadow-xl space-y-4"
      >
        <div class="w-16 h-16 rounded-2xl bg-blue-600/10 text-blue-400 border border-blue-500/20 mx-auto flex items-center justify-center">
          <Search class="w-8 h-8" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-white mb-1">Không tìm thấy sự kiện phù hợp</h3>
          <p class="text-xs sm:text-sm text-slate-400 max-w-md mx-auto leading-relaxed">
            Rất tiếc, không có sự kiện nào khớp với tiêu chí bạn đang chọn. Hãy thử xóa bộ lọc hoặc tìm kiếm với từ khóa khác.
          </p>
        </div>
        <div class="pt-2 flex items-center justify-center gap-3">
          <button
            @click="resetFilters"
            class="px-5 py-2.5 rounded-xl bg-blue-600 text-white text-xs font-bold hover:bg-blue-500 transition shadow-md shadow-blue-600/30 cursor-pointer"
          >
            Xem tất cả sự kiện
          </button>
        </div>
      </div>

      <!-- Events Render Container (Max 8 events per page with Stagger Animation) -->
      <TransitionGroup
        v-else
        name="card-fade"
        tag="div"
        :class="[
          viewMode === 'grid'
            ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5'
            : 'space-y-4'
        ]"
      >
        <EventCard
          v-for="evt in displayedEvents"
          :key="evt.id"
          :event="evt"
          :view-mode="viewMode"
          @notify="notify"
        />
      </TransitionGroup>

      <!-- Elegant CineVerse Pagination Bar (Only visible when totalPages > 1) -->
      <div
        v-if="totalPages > 1"
        class="mt-10 pt-6 border-t border-slate-800/80 flex flex-col sm:flex-row items-center justify-between gap-4"
      >
        <!-- Item range text -->
        <div class="text-xs text-slate-400 font-medium">
          Hiển thị
          <strong class="text-white font-bold">{{ (currentPage - 1) * pageSize + 1 }} &ndash; {{ Math.min(currentPage * pageSize, totalEvents) }}</strong>
          trong tổng số
          <strong class="text-white font-bold">{{ totalEvents }}</strong>
          sự kiện
        </div>

        <!-- Page Numbers and Prev/Next Navigation -->
        <div class="flex items-center gap-1.5">
          <!-- Previous Page Button -->
          <button
            type="button"
            @click="changePage(currentPage - 1)"
            :disabled="currentPage === 1"
            class="px-3 py-2 rounded-xl border border-slate-800 text-xs font-bold transition flex items-center gap-1 cursor-pointer"
            :class="[
              currentPage === 1
                ? 'opacity-40 cursor-not-allowed bg-slate-900/50 text-slate-500'
                : 'bg-slate-900 text-slate-200 hover:text-white hover:bg-slate-800 hover:border-slate-700'
            ]"
            title="Trang trước"
          >
            <ChevronLeft class="w-4 h-4" />
            <span class="hidden sm:inline">Trước</span>
          </button>

          <!-- Numbered Page Pills -->
          <template v-for="(num, idx) in displayedPageNumbers" :key="idx">
            <span
              v-if="num < 0"
              class="w-9 h-9 flex items-center justify-center text-slate-500 text-xs font-bold select-none"
            >
              ...
            </span>
            <button
              v-else
              type="button"
              @click="changePage(num)"
              class="w-9 h-9 rounded-xl text-xs font-black transition flex items-center justify-center cursor-pointer"
              :class="[
                currentPage === num
                  ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-lg shadow-blue-600/30 border border-blue-400/40 scale-105'
                  : 'bg-slate-900 border border-slate-800 text-slate-300 hover:text-white hover:bg-slate-800 hover:border-slate-700'
              ]"
            >
              {{ num }}
            </button>
          </template>

          <!-- Next Page Button -->
          <button
            type="button"
            @click="changePage(currentPage + 1)"
            :disabled="currentPage === totalPages"
            class="px-3 py-2 rounded-xl border border-slate-800 text-xs font-bold transition flex items-center gap-1 cursor-pointer"
            :class="[
              currentPage === totalPages
                ? 'opacity-40 cursor-not-allowed bg-slate-900/50 text-slate-500'
                : 'bg-slate-900 text-slate-200 hover:text-white hover:bg-slate-800 hover:border-slate-700'
            ]"
            title="Trang sau"
          >
            <span class="hidden sm:inline">Sau</span>
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Elegant VIP Ticket Alerts & Promo Section -->
      <div class="mt-8 bg-gradient-to-br from-[#0D121F] via-[#101827] to-[#151D30] rounded-3xl p-6 sm:p-8 text-white border border-[#1E293D] shadow-2xl relative overflow-hidden">
        <div class="absolute -top-24 -right-24 w-80 h-80 rounded-full bg-blue-600/10 blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-80 h-80 rounded-full bg-rose-600/10 blur-3xl pointer-events-none"></div>

        <div class="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div class="max-w-xl">
            <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-extrabold bg-blue-500/20 text-blue-300 border border-blue-500/30 mb-3">
              <Sparkles class="w-3.5 h-3.5 text-amber-400" />
              <span>ĐẶC QUYỀN HỘI VIÊN CINEVERSE & EVENTHUB</span>
            </div>
            <h3 class="text-xl sm:text-2xl font-black text-white tracking-tight mb-2">
              Không bỏ lỡ bất kỳ sự kiện nào bạn yêu thích
            </h3>
            <p class="text-xs sm:text-sm text-slate-300 leading-relaxed font-normal">
              Đăng ký nhận email thông báo sớm nhất khi mở bán vé các concert âm nhạc, giải chạy và nhận mã giảm giá độc quyền dành riêng cho bạn.
            </p>
          </div>

          <div class="w-full lg:max-w-md space-y-3">
            <form @submit.prevent="subscribeNewsletter" class="flex items-center gap-2">
              <div class="relative flex-1">
                <Mail class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input
                  v-model="newsletterEmail"
                  type="email"
                  placeholder="Nhập địa chỉ email của bạn..."
                  class="w-full pl-10 pr-3 py-2.5 rounded-xl bg-slate-900 border border-slate-700 text-xs sm:text-sm text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
                />
              </div>
              <button
                type="submit"
                class="px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-extrabold text-xs flex items-center gap-1.5 transition shrink-0 shadow-md shadow-blue-600/30 cursor-pointer"
              >
                <span>Đăng ký</span>
                <Send class="w-3.5 h-3.5" />
              </button>
            </form>

            <div class="flex items-center justify-between text-[11px] text-slate-400 pt-1">
              <span>Mã ưu đãi 15% cho đơn đầu tiên:</span>
              <button
                type="button"
                @click="copyPromo"
                class="inline-flex items-center gap-1.5 font-mono font-bold text-blue-400 hover:text-white transition bg-slate-900 hover:bg-slate-800 px-2.5 py-1 rounded-lg border border-slate-700 cursor-pointer"
                title="Sao chép mã"
              >
                <span>EVENTHUB2026</span>
                <Copy class="w-3 h-3 text-slate-400" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Global Toast Notifications -->
    <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-sm pointer-events-none">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="px-4 py-2.5 rounded-xl shadow-lg border pointer-events-auto flex items-center gap-2.5 text-xs font-semibold animate-slide-up backdrop-blur-md"
        :class="[
          toast.type === 'success' ? 'bg-emerald-950/90 text-emerald-100 border-emerald-500/40' :
          toast.type === 'error' ? 'bg-rose-950/90 text-rose-100 border-rose-500/40' :
          'bg-slate-900/95 text-white border-slate-700'
        ]"
      >
        <CheckCircle2 v-if="toast.type === 'success'" class="w-4 h-4 text-emerald-400 shrink-0" />
        <AlertCircle v-else-if="toast.type === 'error'" class="w-4 h-4 text-rose-400 shrink-0" />
        <Info v-else class="w-4 h-4 text-indigo-400 shrink-0" />
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Hero Carousel Progress Bar */
.hero-progress-fill {
  width: 100%;
  transform-origin: left;
  animation: heroProgress 6000ms linear forwards;
}

.hero-progress-paused {
  animation-play-state: paused;
}

@keyframes heroProgress {
  from {
    transform: scaleX(0);
  }
  to {
    transform: scaleX(1);
  }
}

/* Ken Burns effect for backdrop */
.ken-burns {
  animation: kenBurnsZoom 8s ease-out forwards;
}

@keyframes kenBurnsZoom {
  from {
    transform: scale(1.02);
  }
  to {
    transform: scale(1.1);
  }
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.hero-bg-enter-active,
.hero-bg-leave-active {
  transition: opacity 0.8s ease;
}

.hero-bg-enter-from,
.hero-bg-leave-to {
  opacity: 0;
}

/* Card list animations */
.card-fade-enter-active,
.card-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1);
}

.card-fade-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}

.card-fade-leave-to {
  opacity: 0;
  transform: scale(0.96);
}

.card-fade-move {
  transition: transform 0.4s ease;
}

@media (prefers-reduced-motion: reduce) {
  .hero-progress-fill,
  .ken-burns {
    animation: none !important;
  }
}
</style>
