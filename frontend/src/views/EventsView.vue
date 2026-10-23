<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
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
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const events = ref<Event[]>([])
const categories = ref<Category[]>([])
const loading = ref(true)
const viewMode = ref<'grid' | 'list'>('grid')
const showAdvancedFilters = ref(false)

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
    })
    events.value = res.content
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  categories.value = await eventService.getCategories()
  await fetchEvents()
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
    fetchEvents()
  }
)

const applyFilters = () => {
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
  selectedCategory.value = catId
  applyFilters()
}

const applyTag = (query: string) => {
  searchKeyword.value = query
  applyFilters()
}

const resetFilters = () => {
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
  <div class="min-h-screen bg-[#f8fafc] pb-20 text-slate-800">
    <!-- Breadcrumb & Top Bar -->
    <div class="bg-white border-b border-slate-200/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3 flex items-center justify-between text-xs text-slate-500">
        <div class="flex items-center gap-2">
          <router-link to="/" class="hover:text-indigo-600 transition">Trang chủ</router-link>
          <ChevronRight class="w-3.5 h-3.5 text-slate-400" />
          <span class="font-medium text-slate-900">Khám phá sự kiện</span>
        </div>
        <div class="hidden sm:flex items-center gap-2 text-indigo-700 font-semibold">
          <Sparkles class="w-3.5 h-3.5 text-indigo-500" />
          <span>Hơn 50+ sự kiện trực tiếp đang mở bán vé</span>
        </div>
      </div>
    </div>

    <!-- Header Hub Banner -->
    <div class="bg-white border-b border-slate-200/80 py-8 sm:py-10">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="max-w-3xl mb-6">
          <h1 class="text-2xl sm:text-3xl lg:text-4xl font-extrabold text-slate-900 tracking-tight mb-2">
            Khám Phá Toàn Bộ Sự Kiện
          </h1>
          <p class="text-xs sm:text-sm text-slate-500 leading-relaxed font-normal">
            Tìm kiếm và đặt vé chính hãng cho các concert âm nhạc, giải chạy marathon, triển lãm nghệ thuật và hội thảo công nghệ trên toàn quốc.
          </p>
        </div>

        <!-- Integrated Search & Filter Hub Bar -->
        <div class="bg-white p-3 sm:p-4 rounded-2xl border border-slate-200 shadow-sm space-y-3">
          <div class="grid grid-cols-1 md:grid-cols-12 gap-2.5">
            <!-- Keyword input -->
            <div class="md:col-span-5 relative">
              <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
              <input
                v-model="searchKeyword"
                @keyup.enter="applyFilters"
                type="text"
                placeholder="Tìm tên sự kiện, ca sĩ, địa điểm, ban tổ chức..."
                class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs sm:text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition"
              />
              <button
                v-if="searchKeyword"
                @click="searchKeyword = ''; applyFilters()"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
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
                class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs sm:text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition cursor-pointer font-medium appearance-none"
              >
                <option v-for="c in cities" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            </div>

            <!-- Time Range Select -->
            <div class="md:col-span-2 relative">
              <Calendar class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
              <select
                v-model="selectedTimeRange"
                @change="applyFilters"
                class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs sm:text-sm text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:bg-white transition cursor-pointer font-medium appearance-none"
              >
                <option value="ALL">Mọi thời điểm</option>
                <option value="THIS_WEEK">7 ngày tới</option>
                <option value="THIS_MONTH">Tháng này</option>
                <option value="UPCOMING">Sắp diễn ra</option>
              </select>
              <ChevronDown class="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            </div>

            <!-- Action Buttons: Apply & Advanced Filter Toggle -->
            <div class="md:col-span-2 flex items-center gap-1.5">
              <button
                @click="applyFilters"
                class="flex-1 py-2.5 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-sm transition"
              >
                <Search class="w-3.5 h-3.5" />
                <span>Tìm kiếm</span>
              </button>

              <button
                type="button"
                @click="showAdvancedFilters = !showAdvancedFilters"
                class="p-2.5 rounded-xl border border-slate-200 transition"
                :class="showAdvancedFilters || filterHasSeatMap || filterFeatured || selectedPriceRange !== 'ALL' ? 'bg-indigo-50 text-indigo-700 border-indigo-200' : 'bg-slate-50 hover:bg-slate-100 text-slate-600'"
                title="Bộ lọc nâng cao"
              >
                <SlidersHorizontal class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Collapsible Advanced Filters Drawer -->
          <div
            v-if="showAdvancedFilters"
            class="pt-3 border-t border-slate-100 grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs"
          >
            <!-- Price Range Dropdown -->
            <div>
              <label class="block font-bold text-slate-700 mb-1">Khoảng giá vé</label>
              <select
                v-model="selectedPriceRange"
                @change="applyFilters"
                class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 font-medium text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="ALL">Tất cả mức giá</option>
                <option value="UNDER_500K">Dưới 500.000 đ</option>
                <option value="500K_1500K">500.000 đ &ndash; 1.500.000 đ</option>
                <option value="OVER_1500K">Trên 1.500.000 đ</option>
              </select>
            </div>

            <!-- Feature Checkboxes -->
            <div class="flex flex-col justify-end gap-2 pb-1">
              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input
                  v-model="filterHasSeatMap"
                  @change="applyFilters"
                  type="checkbox"
                  class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
                />
                <span class="font-medium text-slate-700">Chỉ sự kiện có sơ đồ chọn ghế ngồi</span>
              </label>

              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input
                  v-model="filterFeatured"
                  @change="applyFilters"
                  type="checkbox"
                  class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
                />
                <span class="font-medium text-slate-700">Chỉ sự kiện Hot / Thịnh hành</span>
              </label>
            </div>

            <!-- Reset All in Drawer -->
            <div class="flex items-end justify-end">
              <button
                type="button"
                @click="resetFilters"
                class="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 font-bold transition flex items-center gap-1.5"
              >
                <RotateCcw class="w-3.5 h-3.5" />
                <span>Đặt lại tất cả</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Trending Keyword Tags -->
        <div class="flex items-center gap-2 mt-3 text-xs text-slate-500 overflow-x-auto pb-1 scrollbar-none">
          <span class="font-bold text-slate-400 shrink-0 flex items-center gap-1">
            <Flame class="w-3.5 h-3.5 text-amber-500" />
            <span>Tìm kiếm phổ biến:</span>
          </span>
          <button
            v-for="tag in trendingTags"
            :key="tag.query"
            type="button"
            @click="applyTag(tag.query)"
            class="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-indigo-50 hover:text-indigo-600 text-slate-600 text-xs font-medium whitespace-nowrap transition"
          >
            #{{ tag.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- Category Tabs Navigation Bar -->
    <div class="bg-white border-b border-slate-200/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center gap-2 overflow-x-auto py-2.5 scrollbar-none text-xs font-semibold">
          <!-- All Categories Tab -->
          <button
            type="button"
            @click="selectCategoryTab(null)"
            class="px-4 py-2 rounded-xl transition flex items-center gap-1.5 whitespace-nowrap shrink-0"
            :class="[
              selectedCategory === null
                ? 'bg-slate-900 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
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
            class="px-4 py-2 rounded-xl transition flex items-center gap-1.5 whitespace-nowrap shrink-0"
            :class="[
              selectedCategory === cat.id
                ? 'bg-slate-900 text-white shadow-xs'
                : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
            ]"
          >
            <component :is="getCategoryIcon(cat.slug)" class="w-3.5 h-3.5" />
            <span>{{ cat.name }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content Grid -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">
      <!-- Active Filter Tags Row & Controls -->
      <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs">
        <!-- Results Counter & Active Chips -->
        <div class="flex flex-wrap items-center gap-2">
          <span class="text-slate-500 font-medium">
            Tìm thấy <strong class="text-slate-900 font-bold text-sm">{{ events.length }}</strong> sự kiện
          </span>

          <!-- Chips -->
          <div v-for="(chip, idx) in activeFilterChips" :key="idx" class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-indigo-50 text-indigo-700 text-[11px] font-semibold border border-indigo-100">
            <span>{{ chip.label }}</span>
            <button type="button" @click="chip.clear" class="hover:text-indigo-900 p-0.5">
              <X class="w-3 h-3" />
            </button>
          </div>

          <button
            v-if="activeFilterChips.length > 0"
            type="button"
            @click="resetFilters"
            class="text-[11px] text-rose-600 hover:underline font-semibold ml-1"
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
              class="px-3 py-1.5 rounded-xl border border-slate-200 bg-white text-xs font-semibold text-slate-800 focus:outline-none focus:ring-1 focus:ring-indigo-500 cursor-pointer shadow-2xs"
            >
              <option value="date_asc">Sắp diễn ra nhất</option>
              <option value="featured">Nổi bật / Thịnh hành</option>
              <option value="price_asc">Giá vé: Thấp &rarr; Cao</option>
              <option value="price_desc">Giá vé: Cao &rarr; Thấp</option>
              <option value="date_desc">Sự kiện xa nhất</option>
            </select>
          </div>

          <!-- View Mode Toggle -->
          <div class="inline-flex p-0.5 bg-slate-100 rounded-xl border border-slate-200">
            <button
              type="button"
              @click="viewMode = 'grid'"
              class="p-1.5 rounded-lg transition"
              :class="viewMode === 'grid' ? 'bg-white text-slate-900 shadow-2xs' : 'text-slate-500 hover:text-slate-900'"
              title="Xem dạng lưới"
            >
              <LayoutGrid class="w-4 h-4" />
            </button>
            <button
              type="button"
              @click="viewMode = 'list'"
              class="p-1.5 rounded-lg transition"
              :class="viewMode === 'list' ? 'bg-white text-slate-900 shadow-2xs' : 'text-slate-500 hover:text-slate-900'"
              title="Xem dạng danh sách"
            >
              <List class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      <!-- Loading Skeleton -->
      <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div v-for="i in 8" :key="i" class="h-80 rounded-2xl bg-slate-200 animate-pulse"></div>
      </div>

      <!-- Empty State -->
      <div
        v-else-if="events.length === 0"
        class="text-center py-16 px-4 bg-white rounded-3xl border border-slate-200/90 shadow-sm space-y-4"
      >
        <div class="w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 mx-auto flex items-center justify-center">
          <Search class="w-8 h-8" />
        </div>
        <div>
          <h3 class="text-lg font-bold text-slate-900 mb-1">Không tìm thấy sự kiện phù hợp</h3>
          <p class="text-xs sm:text-sm text-slate-500 max-w-md mx-auto leading-relaxed">
            Rất tiếc, không có sự kiện nào khớp với tiêu chí bạn đang chọn. Hãy thử xóa bộ lọc hoặc tìm kiếm với từ khóa khác.
          </p>
        </div>
        <div class="pt-2 flex items-center justify-center gap-3">
          <button
            @click="resetFilters"
            class="px-5 py-2.5 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:bg-indigo-700 transition shadow-sm"
          >
            Xem tất cả sự kiện
          </button>
        </div>
      </div>

      <!-- Events Render Container -->
      <div
        v-else
        :class="[
          viewMode === 'grid'
            ? 'grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5'
            : 'space-y-4'
        ]"
      >
        <EventCard
          v-for="evt in events"
          :key="evt.id"
          :event="evt"
          :view-mode="viewMode"
          @notify="notify"
        />
      </div>

      <!-- Elegant VIP Ticket Alerts & Promo Section -->
      <div class="mt-8 bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 rounded-3xl p-6 sm:p-8 text-white border border-slate-800 shadow-xl relative overflow-hidden">
        <div class="absolute -top-24 -right-24 w-80 h-80 rounded-full bg-indigo-500/10 blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-80 h-80 rounded-full bg-purple-500/10 blur-3xl pointer-events-none"></div>

        <div class="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div class="max-w-xl">
            <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-indigo-500/20 text-indigo-300 border border-indigo-500/30 mb-3">
              <Sparkles class="w-3.5 h-3.5 text-amber-400" />
              <span>Đặc Quyền Hội Viên EventHub</span>
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
                  class="w-full pl-10 pr-3 py-2.5 rounded-xl bg-slate-800/80 border border-slate-700 text-xs sm:text-sm text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition"
                />
              </div>
              <button
                type="submit"
                class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white font-bold text-xs flex items-center gap-1.5 transition shrink-0 shadow-md cursor-pointer"
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
                class="inline-flex items-center gap-1.5 font-mono font-bold text-indigo-300 hover:text-white transition bg-slate-800/80 hover:bg-slate-700/80 px-2.5 py-1 rounded-lg border border-slate-700 cursor-pointer"
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
