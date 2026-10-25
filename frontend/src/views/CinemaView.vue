<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { cinemaService } from '@/services/cinema.service'
import type { Movie, CinemaComplex } from '@/types/cinema'
import {
  Film,
  Play,
  Star,
  Clock,
  Sparkles,
  MapPin,
  Phone,
  Monitor,
  Volume2,
  X,
  Ticket,
  ChevronLeft,
  ChevronRight,
  Flame,
  Search,
  Trophy,
} from 'lucide-vue-next'

const router = useRouter()

const nowShowing = ref<Movie[]>([])
const comingSoon = ref<Movie[]>([])
const complexes = ref<CinemaComplex[]>([])
const loading = ref(true)
const activeTab = ref<'now' | 'soon'>('now')
const selectedCity = ref('ALL')
const searchKeyword = ref('')

// Trailer Modal state
const trailerModalOpen = ref(false)
const currentTrailerUrl = ref('')
const currentTrailerTitle = ref('')

// Top Spotlight Movies (Top 3-4 blockbusters)
const selectedSpotlightIndex = ref(0)
const isSpotlightPaused = ref(false)
let spotlightInterval: any = null

const topSpotlightMovies = computed(() => {
  const combined = [...nowShowing.value, ...comingSoon.value]
  const seen = new Set<string>()
  const list: Movie[] = []
  for (const m of combined) {
    const key = m.slug || m.title
    if (!seen.has(key)) {
      seen.add(key)
      list.push(m)
    }
  }
  return list.slice(0, 4)
})

const featuredMovie = computed(() => {
  if (topSpotlightMovies.value.length === 0) {
    return nowShowing.value[0] || comingSoon.value[0] || null
  }
  const idx = selectedSpotlightIndex.value % topSpotlightMovies.value.length
  return topSpotlightMovies.value[idx]
})

const selectTopMovie = (idx: number) => {
  selectedSpotlightIndex.value = idx
}

const prevSpotlight = () => {
  if (topSpotlightMovies.value.length === 0) return
  selectedSpotlightIndex.value =
    (selectedSpotlightIndex.value - 1 + topSpotlightMovies.value.length) % topSpotlightMovies.value.length
}

const nextSpotlight = () => {
  if (topSpotlightMovies.value.length === 0) return
  selectedSpotlightIndex.value = (selectedSpotlightIndex.value + 1) % topSpotlightMovies.value.length
}

const startSpotlightTimer = () => {
  stopSpotlightTimer()
  spotlightInterval = setInterval(() => {
    if (!isSpotlightPaused.value && topSpotlightMovies.value.length > 1) {
      selectedSpotlightIndex.value = (selectedSpotlightIndex.value + 1) % topSpotlightMovies.value.length
    }
  }, 6000)
}

const stopSpotlightTimer = () => {
  if (spotlightInterval) {
    clearInterval(spotlightInterval)
    spotlightInterval = null
  }
}

onMounted(async () => {
  try {
    const [nowRes, soonRes, complexRes] = await Promise.all([
      cinemaService.getNowShowingMovies(),
      cinemaService.getComingSoonMovies(),
      cinemaService.getCinemaComplexes(),
    ])
    nowShowing.value = nowRes
    comingSoon.value = soonRes
    complexes.value = complexRes
    startSpotlightTimer()
  } catch (error) {
    console.error('Error fetching cinema data:', error)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  stopSpotlightTimer()
})

// Current movie list based on active tab
const currentMovies = computed(() => {
  const list = activeTab.value === 'now' ? nowShowing.value : comingSoon.value
  return list.filter((m) => {
    const matchSearch =
      !searchKeyword.value.trim() ||
      m.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      m.genre.toLowerCase().includes(searchKeyword.value.toLowerCase())
    return matchSearch
  })
})

// Filtered complexes by selected city
const filteredComplexes = computed(() => {
  if (selectedCity.value === 'ALL') return complexes.value
  return complexes.value.filter((c) => c.city.toLowerCase().includes(selectedCity.value.toLowerCase()))
})

const openTrailer = (movie: Movie) => {
  if (!movie.trailerUrl) return
  // Transform standard youtube watch url to embed url if needed
  let embedUrl = movie.trailerUrl
  if (embedUrl.includes('watch?v=')) {
    embedUrl = embedUrl.replace('watch?v=', 'embed/')
  } else if (embedUrl.includes('youtu.be/')) {
    embedUrl = embedUrl.replace('youtu.be/', 'www.youtube.com/embed/')
  }
  // Add autoplay parameter
  if (!embedUrl.includes('autoplay=')) {
    embedUrl += (embedUrl.includes('?') ? '&' : '?') + 'autoplay=1'
  }
  currentTrailerUrl.value = embedUrl
  currentTrailerTitle.value = movie.title
  trailerModalOpen.value = true
}

const closeTrailer = () => {
  trailerModalOpen.value = false
  currentTrailerUrl.value = ''
}

const getRatingColor = (code: string) => {
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
    <!-- Cinema Hero Spotlight Banner (Top 4 Blockbusters) -->
    <div
      v-if="featuredMovie"
      class="relative overflow-hidden border-b border-slate-800 bg-slate-950 py-8 lg:py-12 transition-all duration-700"
      @mouseenter="isSpotlightPaused = true"
      @mouseleave="isSpotlightPaused = false"
    >
      <!-- Background Backdrop with Gradient Overlays -->
      <div class="absolute inset-0 z-0 overflow-hidden">
        <img
          :key="featuredMovie.id"
          :src="featuredMovie.bannerUrl || featuredMovie.posterUrl"
          :alt="featuredMovie.title"
          class="w-full h-full object-cover object-center opacity-30 filter blur-xs scale-105 transform duration-1000"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-slate-950 via-slate-950/90 to-transparent"></div>
        <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/50 to-transparent"></div>
      </div>

      <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
        <!-- Main Spotlight Row -->
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
          <!-- Text Info (lg:col-span-8) -->
          <div class="lg:col-span-8 space-y-4">
            <div class="flex flex-wrap items-center gap-3">
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/20 border border-rose-500/30 text-rose-400 text-xs font-black tracking-wide uppercase">
                <Flame class="w-3.5 h-3.5 text-rose-400 animate-pulse" />
                <span>TOP #{{ selectedSpotlightIndex + 1 }} BOM TẤN CHIẾU RẠP TÂM ĐIỂM</span>
              </div>

              <!-- Mini Carousel Arrow Navigation -->
              <div v-if="topSpotlightMovies.length > 1" class="flex items-center gap-1.5 ml-auto sm:ml-0 bg-slate-900/90 border border-slate-800 rounded-lg p-1">
                <button
                  type="button"
                  @click="prevSpotlight"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Phim trước"
                >
                  <ChevronLeft class="w-4 h-4" />
                </button>
                <span class="text-xs font-mono font-bold px-1.5 text-slate-400">
                  {{ selectedSpotlightIndex + 1 }} / {{ topSpotlightMovies.length }}
                </span>
                <button
                  type="button"
                  @click="nextSpotlight"
                  class="p-1 rounded hover:bg-slate-800 text-slate-300 hover:text-white transition cursor-pointer"
                  title="Phim tiếp theo"
                >
                  <ChevronRight class="w-4 h-4" />
                </button>
              </div>
            </div>

            <h1 class="text-3xl sm:text-4xl lg:text-5xl font-black tracking-tight text-white leading-tight">
              {{ featuredMovie.title }}
            </h1>

            <!-- Meta Tags -->
            <div class="flex flex-wrap items-center gap-3 text-xs sm:text-sm font-semibold text-slate-300">
              <span
                class="px-2 py-0.5 rounded font-black border text-xs"
                :class="getRatingColor(featuredMovie.ageRatingCode)"
              >
                {{ featuredMovie.ageRatingCode }}
              </span>
              <span class="flex items-center gap-1 text-amber-400 font-bold">
                <Star class="w-4 h-4 fill-amber-400" />
                {{ featuredMovie.imdbRating || '8.8' }} IMDb
              </span>
              <span>&bull;</span>
              <span class="flex items-center gap-1">
                <Clock class="w-4 h-4 text-slate-400" />
                {{ featuredMovie.durationMinutes }} phút
              </span>
              <span>&bull;</span>
              <span class="text-rose-400 font-bold">{{ featuredMovie.genre }}</span>
              <span v-if="featuredMovie.formatDisplay" class="px-2 py-0.5 rounded bg-indigo-900/60 border border-indigo-500/40 text-indigo-300 text-xs uppercase font-extrabold">
                {{ featuredMovie.formatDisplay }}
              </span>
            </div>

            <p class="text-sm sm:text-base text-slate-300 line-clamp-2 max-w-2xl leading-relaxed">
              {{ featuredMovie.description }}
            </p>

            <div class="text-xs text-slate-400 space-y-1">
              <p><strong class="text-slate-300">Đạo diễn:</strong> {{ featuredMovie.director }}</p>
              <p><strong class="text-slate-300">Diễn viên chính:</strong> {{ featuredMovie.actorsCast }}</p>
            </div>

            <!-- CTA Buttons -->
            <div class="flex flex-wrap items-center gap-3 pt-2">
              <router-link
                :to="`/movies/${featuredMovie.slug || featuredMovie.id}`"
                class="px-6 py-3 rounded-xl bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white font-extrabold text-sm sm:text-base shadow-lg shadow-rose-600/30 transition flex items-center gap-2 group cursor-pointer"
              >
                <Ticket class="w-5 h-5 group-hover:rotate-12 transition-transform" />
                <span>Đặt Vé Suất Chiếu Ngay</span>
              </router-link>

              <button
                type="button"
                @click="openTrailer(featuredMovie)"
                class="px-5 py-3 rounded-xl bg-slate-800/80 hover:bg-slate-700/80 border border-slate-700 text-slate-200 font-bold text-sm transition flex items-center gap-2 cursor-pointer hover:border-slate-500"
              >
                <Play class="w-4 h-4 text-rose-400 fill-rose-400" />
                <span>Xem Trailer Full HD</span>
              </button>
            </div>
          </div>

          <!-- Featured Poster Card (lg:col-span-4) -->
          <div class="hidden lg:flex lg:col-span-4 justify-end">
            <div class="relative group cursor-pointer" @click="router.push(`/movies/${featuredMovie.slug || featuredMovie.id}`)">
              <div class="absolute -inset-1 rounded-2xl bg-gradient-to-r from-rose-600 to-indigo-600 opacity-40 blur group-hover:opacity-75 transition duration-500"></div>
              <img
                :src="featuredMovie.posterUrl"
                :alt="featuredMovie.title"
                class="relative w-64 lg:w-72 rounded-2xl shadow-2xl object-cover aspect-[2/3] border border-slate-800 transform group-hover:scale-[1.02] transition duration-300"
              />
              <div class="absolute top-3 right-3 px-2 py-1 rounded bg-black/80 backdrop-blur-xs text-[10px] font-black uppercase text-rose-400 border border-rose-500/30">
                {{ featuredMovie.formatDisplay?.split(',')[0] || 'IMAX Laser' }}
              </div>
              <div class="absolute bottom-3 left-3 right-3 px-3 py-1.5 rounded-lg bg-slate-950/85 backdrop-blur-xs text-xs font-bold text-slate-200 border border-slate-800 flex items-center justify-between">
                <span>Top #{{ selectedSpotlightIndex + 1 }} Thịnh Hành</span>
                <span class="text-rose-400 text-[11px] font-semibold">Xem chi tiết &rarr;</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Top 4 Movies Selector Strip -->
        <div v-if="topSpotlightMovies.length > 1" class="mt-8 pt-6 border-t border-slate-800/80">
          <div class="flex items-center justify-between mb-3.5">
            <div class="flex items-center gap-2">
              <Trophy class="w-4 h-4 text-amber-400" />
              <span class="text-xs uppercase font-extrabold tracking-wider text-slate-300">
                Top {{ topSpotlightMovies.length }} Bom Tấn Dẫn Đầu Phòng Vé - Bấm để chọn xem
              </span>
            </div>
            <div class="hidden sm:flex items-center gap-2 text-xs text-slate-400 font-medium">
              <span class="inline-block w-2 h-2 rounded-full bg-rose-500 animate-pulse"></span>
              <span>Tự động chuyển tiếp sau 6s</span>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
            <div
              v-for="(movie, idx) in topSpotlightMovies"
              :key="movie.id"
              @click="selectTopMovie(idx)"
              class="group relative flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-all duration-300 border backdrop-blur-xs"
              :class="[
                selectedSpotlightIndex === idx
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
                    {{ movie.imdbRating || '8.8' }}
                  </span>
                  <span
                    v-if="selectedSpotlightIndex === idx"
                    class="px-1.5 py-0.2 rounded bg-rose-500/20 text-rose-400 text-[9px] font-black uppercase tracking-wider"
                  >
                    Đang xem
                  </span>
                </div>
                <h4 class="text-xs font-bold text-white truncate group-hover:text-rose-400 transition-colors">
                  {{ movie.title }}
                </h4>
                <p class="text-[10px] text-slate-400 truncate mt-0.5">
                  {{ movie.formatDisplay?.split(',')[0] || movie.genre }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Container -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 space-y-12">
      <!-- Section Header & Filter Toolbar -->
      <section class="space-y-6">
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-slate-800 pb-5">
          <!-- Left: Tab Switcher (Đang Chiếu / Sắp Chiếu) -->
          <div class="flex items-center gap-2 p-1.5 rounded-2xl bg-slate-900 border border-slate-800">
            <button
              type="button"
              @click="activeTab = 'now'"
              class="px-5 py-2 rounded-xl text-sm font-extrabold transition flex items-center gap-2 cursor-pointer"
              :class="activeTab === 'now' ? 'bg-rose-600 text-white shadow-md shadow-rose-600/30' : 'text-slate-400 hover:text-white'"
            >
              <Film class="w-4 h-4" />
              <span>Phim Đang Chiếu ({{ nowShowing.length }})</span>
            </button>
            <button
              type="button"
              @click="activeTab = 'soon'"
              class="px-5 py-2 rounded-xl text-sm font-extrabold transition flex items-center gap-2 cursor-pointer"
              :class="activeTab === 'soon' ? 'bg-rose-600 text-white shadow-md shadow-rose-600/30' : 'text-slate-400 hover:text-white'"
            >
              <Sparkles class="w-4 h-4" />
              <span>Sắp Khởi Chiếu ({{ comingSoon.length }})</span>
            </button>
          </div>

          <!-- Right: Search & City Filter -->
          <div class="flex items-center gap-3">
            <div class="relative w-48 sm:w-64">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-500" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="Tìm phim theo tên, thể loại..."
                class="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-rose-500"
              />
            </div>

            <select
              v-model="selectedCity"
              class="px-3 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs font-bold text-slate-300 focus:outline-none focus:border-rose-500 cursor-pointer"
            >
              <option value="ALL">Toàn quốc</option>
              <option value="Hồ Chí Minh">TP. Hồ Chí Minh</option>
              <option value="Hà Nội">Hà Nội</option>
            </select>
          </div>
        </div>

        <!-- Movie Grid -->
        <div v-if="loading" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
          <div v-for="i in 8" :key="i" class="h-80 rounded-2xl bg-slate-900/60 animate-pulse border border-slate-800"></div>
        </div>

        <div v-else-if="currentMovies.length === 0" class="text-center py-16 space-y-3">
          <Film class="w-12 h-12 text-slate-600 mx-auto" />
          <h3 class="text-base font-bold text-slate-400">Không tìm thấy bộ phim nào phù hợp</h3>
          <p class="text-xs text-slate-500">Vui lòng thử tìm kiếm với từ khóa khác hoặc chuyển sang danh mục khác.</p>
        </div>

        <div v-else class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-5 sm:gap-6">
          <div
            v-for="movie in currentMovies"
            :key="movie.id"
            class="group rounded-2xl bg-slate-900 border border-slate-800 hover:border-rose-500/50 flex flex-col overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-rose-950/30"
          >
            <!-- Poster Wrap -->
            <div class="relative aspect-[2/3] overflow-hidden bg-slate-950">
              <img
                :src="movie.posterUrl"
                :alt="movie.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />

              <!-- Age Rating Badge -->
              <div
                class="absolute top-2.5 left-2.5 px-2 py-0.5 rounded font-black text-[11px] border shadow-sm"
                :class="getRatingColor(movie.ageRatingCode)"
              >
                {{ movie.ageRatingCode }}
              </div>

              <!-- IMDb Badge -->
              <div
                v-if="movie.imdbRating"
                class="absolute top-2.5 right-2.5 px-2 py-0.5 rounded bg-black/80 backdrop-blur-xs border border-amber-500/40 text-amber-400 font-bold text-[11px] flex items-center gap-1"
              >
                <Star class="w-3 h-3 fill-amber-400" />
                {{ movie.imdbRating }}
              </div>

              <!-- Format Banner on Poster Bottom -->
              <div
                v-if="movie.formatDisplay"
                class="absolute bottom-0 inset-x-0 bg-gradient-to-t from-black via-black/80 to-transparent p-2 text-center"
              >
                <span class="text-[10px] font-black uppercase text-indigo-300 tracking-wider">
                  {{ movie.formatDisplay }}
                </span>
              </div>

              <!-- Hover Play Overlay -->
              <button
                type="button"
                @click.stop="openTrailer(movie)"
                class="absolute inset-0 bg-slate-950/60 backdrop-blur-2xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300 cursor-pointer"
                title="Xem Trailer"
              >
                <div class="w-12 h-12 rounded-full bg-rose-600 text-white flex items-center justify-center shadow-lg transform scale-90 group-hover:scale-100 transition-transform">
                  <Play class="w-5 h-5 fill-white ml-0.5" />
                </div>
              </button>
            </div>

            <!-- Movie Details -->
            <div class="p-3.5 sm:p-4 flex-1 flex flex-col justify-between space-y-3">
              <div>
                <h3
                  @click="router.push(`/movies/${movie.slug || movie.id}`)"
                  class="font-extrabold text-sm sm:text-base text-white group-hover:text-rose-400 transition-colors line-clamp-1 cursor-pointer"
                  :title="movie.title"
                >
                  {{ movie.title }}
                </h3>
                <div class="flex items-center gap-2 text-[11px] text-slate-400 mt-1 font-medium">
                  <span class="text-rose-400 font-semibold">{{ movie.genre }}</span>
                  <span>&bull;</span>
                  <span>{{ movie.durationMinutes }}'</span>
                </div>
              </div>

              <!-- Card Action CTA -->
              <div class="grid grid-cols-2 gap-2 pt-1 border-t border-slate-800/80">
                <button
                  type="button"
                  @click="openTrailer(movie)"
                  class="py-2 px-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-bold transition flex items-center justify-center gap-1 cursor-pointer"
                >
                  <Play class="w-3.5 h-3.5 text-rose-400" />
                  <span>Trailer</span>
                </button>

                <router-link
                  :to="`/movies/${movie.slug || movie.id}`"
                  class="py-2 px-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-black transition flex items-center justify-center gap-1 text-center shadow-sm cursor-pointer"
                >
                  <Ticket class="w-3.5 h-3.5" />
                  <span>Mua Vé</span>
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Section: Hệ Thống Cụm Rạp EventHub Cinema -->
      <section class="space-y-6 pt-6 border-t border-slate-800">
        <div class="flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2 text-rose-500 text-xs font-black uppercase tracking-wider">
              <Monitor class="w-4 h-4" />
              <span>Hệ Thống Phòng Chiếu Tiêu Chuẩn Quốc Tế</span>
            </div>
            <h2 class="text-2xl sm:text-3xl font-black text-white mt-1">Cụm Rạp EventHub Cinema</h2>
          </div>
          <span class="text-xs text-slate-400 hidden sm:inline">Phục vụ hàng ngày: 08:30 - 01:00 sáng</span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="complex in filteredComplexes"
            :key="complex.id"
            class="rounded-2xl bg-slate-900 border border-slate-800 p-5 space-y-4 hover:border-slate-700 transition"
          >
            <div class="relative h-40 rounded-xl overflow-hidden bg-slate-950">
              <img
                :src="complex.imageUrl || 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800'"
                :alt="complex.name"
                class="w-full h-full object-cover"
              />
              <div class="absolute top-2 right-2 px-2 py-1 rounded bg-black/80 backdrop-blur-xs text-[10px] font-black uppercase text-indigo-400 border border-indigo-500/30">
                {{ complex.totalScreens }} Phòng Chiếu
              </div>
            </div>

            <div class="space-y-2">
              <h3 class="font-extrabold text-base text-white">{{ complex.name }}</h3>
              <p class="text-xs text-slate-400 flex items-start gap-1.5 leading-relaxed">
                <MapPin class="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                <span>{{ complex.address }}</span>
              </p>
              <p v-if="complex.phone" class="text-xs text-slate-400 flex items-center gap-1.5">
                <Phone class="w-3.5 h-3.5 text-slate-500" />
                <span>{{ complex.phone }}</span>
              </p>
            </div>

            <div class="pt-2 border-t border-slate-800 flex items-center justify-between text-xs">
              <div class="flex items-center gap-2">
                <span class="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-bold text-slate-300">IMAX Laser</span>
                <span class="px-2 py-0.5 rounded bg-slate-800 text-[10px] font-bold text-slate-300">Dolby Atmos</span>
              </div>
              <span class="text-rose-400 font-bold hover:underline cursor-pointer flex items-center gap-1">
                Lịch chiếu <ChevronRight class="w-3 h-3" />
              </span>
            </div>
          </div>
        </div>
      </section>

      <!-- F&B Concession Highlights Banner -->
      <section class="rounded-3xl bg-gradient-to-r from-amber-950/60 via-slate-900 to-rose-950/50 border border-amber-600/30 p-6 sm:p-8 flex flex-col md:flex-row items-center justify-between gap-6">
        <div class="space-y-2 max-w-xl">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/20 text-amber-300 text-xs font-black uppercase">
            <Sparkles class="w-3.5 h-3.5" />
            <span>F&B Cinema Concession</span>
          </div>
          <h3 class="text-xl sm:text-2xl font-black text-white">Bắp Rang Bơ Nóng Giòn & Nước Ngọt Thưởng Thức Trọn Vẹn</h3>
          <p class="text-xs sm:text-sm text-slate-300">
            Đặt trước Combo Bắp Phô Mai / Caramel kèm nước ngọt mát lạnh trực tiếp trong bước đặt vé để nhận chiết khấu 15% và nhận ngay tại quầy không cần xếp hàng!
          </p>
        </div>
        <div class="flex items-center gap-3 shrink-0">
          <div class="w-16 h-16 rounded-2xl bg-amber-500/20 border border-amber-500/40 flex items-center justify-center text-3xl">
            🍿
          </div>
          <div class="w-16 h-16 rounded-2xl bg-rose-500/20 border border-rose-500/40 flex items-center justify-center text-3xl">
            🥤
          </div>
        </div>
      </section>
    </main>

    <!-- Trailer Embed Modal -->
    <Teleport to="body">
      <div v-if="trailerModalOpen" class="fixed inset-0 z-50 overflow-hidden flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black/90 backdrop-blur-sm transition-opacity" @click="closeTrailer"></div>

        <!-- Modal Dialog -->
        <div class="relative w-full max-w-4xl bg-slate-900 rounded-2xl border border-slate-800 shadow-2xl overflow-hidden z-10 space-y-0">
          <div class="p-4 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <Film class="w-4 h-4 text-rose-500" />
              <span class="font-extrabold text-sm text-white">{{ currentTrailerTitle }} - Official Trailer</span>
            </div>
            <button
              type="button"
              @click="closeTrailer"
              class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Video iFrame -->
          <div class="relative aspect-video bg-black">
            <iframe
              :src="currentTrailerUrl"
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
