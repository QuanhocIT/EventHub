<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { THEATERS_DATA, type TheaterComplex } from '@/services/cinemas.data'
import {
  MapPin,
  Star,
  Search,
  ChevronRight,
  X,
  Building2,
  Film,
  Clock,
  SlidersHorizontal,
  Sparkles,
  Ticket,
  Tv,
  Percent,
  Phone,
  CheckCircle2,
  Navigation,
  Compass,
  Award,
} from 'lucide-vue-next'

const router = useRouter()

// Filters
const selectedCity = ref('ALL')
const selectedBrand = ref('ALL')
const searchKeyword = ref('')
const selectedFormat = ref('ALL')

const cities = [
  { id: 'ALL', name: 'Tất cả thành phố', count: THEATERS_DATA.length },
  { id: 'TP. Hồ Chí Minh', name: 'TP. Hồ Chí Minh', count: 3 },
  { id: 'Hà Nội', name: 'Hà Nội', count: 2 },
  { id: 'Đà Nẵng', name: 'Đà Nẵng', count: 1 },
]

const brands = [
  { id: 'ALL', name: 'Tất cả' },
  { id: 'CineVerse', name: 'CineVerse' },
  { id: 'CGV', name: 'CGV Cinemas' },
  { id: 'BHD', name: 'BHD Star' },
]

const formats = ['ALL', 'IMAX Laser', 'Dolby Atmos', 'ScreenX 270°', 'Gold Class', '4DX Dynamic']

const filteredTheaters = computed(() => {
  return THEATERS_DATA.filter((t) => {
    const matchCity = selectedCity.value === 'ALL' || t.city === selectedCity.value
    const matchBrand = selectedBrand.value === 'ALL' || t.brand.toLowerCase().includes(selectedBrand.value.toLowerCase())
    const matchFormat = selectedFormat.value === 'ALL' || t.formats.some((f) => f.includes(selectedFormat.value) || selectedFormat.value === 'ALL')
    const kw = searchKeyword.value.trim().toLowerCase()
    const matchKeyword =
      !kw ||
      t.name.toLowerCase().includes(kw) ||
      t.address.toLowerCase().includes(kw) ||
      t.district.toLowerCase().includes(kw) ||
      t.brand.toLowerCase().includes(kw)

    return matchCity && matchBrand && matchFormat && matchKeyword
  })
})

const goToTheaterDetail = (theaterId: number) => {
  router.push(`/cinemas/${theaterId}`)
}

// Spotlight theater for sidebar
const featuredTheater = computed(() => THEATERS_DATA[0])

// Spotlight movies for Hero visual cards
const spotlightMovie = computed(() => THEATERS_DATA[0]?.movies[0])
const secondSpotlightMovie = computed(() => THEATERS_DATA[0]?.movies[1])

const goToMovieDetail = (slugOrId: string | number) => {
  router.push(`/movies/${slugOrId}`)
}
</script>

<template>
  <div class="min-h-screen bg-[#070A11] text-slate-100 py-6 sm:py-10 relative overflow-hidden selection:bg-rose-500 selection:text-white">
    <!-- Ambient Subtle Cinema Lighting (Dịu mắt, không lòe loẹt) -->
    <div class="fixed inset-0 pointer-events-none z-0">
      <div class="absolute -top-32 left-1/2 -translate-x-1/2 w-[900px] h-[450px] bg-[radial-gradient(ellipse_at_center,rgba(225,29,72,0.06),transparent_70%)]"></div>
      <div class="absolute top-1/3 -right-40 w-[600px] h-[600px] bg-slate-800/[0.1] rounded-full blur-[150px]"></div>
    </div>

    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-6 relative z-10">

      <!-- ================= CINEMATIC HERO BANNER ĐẦU TRANG CÓ ẢNH RẠP CHIẾU & POSTER ================= -->
      <section class="relative overflow-hidden rounded-3xl border border-white/[0.08] shadow-2xl bg-[#0B0F19]">
        <!-- ẢNH NỀN KHÁN PHÒNG RẠP CHIẾU THỰC TẾ (Cinema Hall Real Backdrop) -->
        <div class="absolute inset-0 z-0">
          <img
            src="/images/about_cinema_hall.jpg"
            alt="Hội trường rạp chiếu phim IMAX"
            class="w-full h-full object-cover object-center opacity-30 scale-105 filter brightness-90"
          />
          <!-- Lớp phủ Gradient điện ảnh cao cấp -->
          <div class="absolute inset-0 bg-gradient-to-r from-[#070A12] via-[#070A12]/95 to-[#070A12]/80"></div>
          <div class="absolute inset-0 bg-gradient-to-t from-[#070A12] via-transparent to-transparent"></div>
        </div>

        <div class="relative z-10 p-6 sm:p-8 lg:p-10">
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 items-center">
            
            <!-- Cột Trái: Tiêu đề & Chọn nhanh Thành phố (Lg:col-span-7) -->
            <div class="lg:col-span-7 space-y-4">
              <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/10 border border-rose-500/20 text-rose-400 text-xs font-semibold backdrop-blur-md">
                <span class="w-2 h-2 rounded-full bg-rose-500 animate-pulse"></span>
                <Film class="w-3.5 h-3.5" />
                <span>Hệ Thống 38+ Cụm Rạp Toàn Quốc • IMAX with Laser</span>
              </div>

              <h1 class="text-2xl sm:text-4xl lg:text-5xl font-black text-white tracking-tight leading-tight">
                Khám Phá Cụm Rạp &amp; <br />
                <span class="text-white">Lịch Chiếu Phim </span>
                <span class="text-rose-500">Mới Nhất</span>
              </h1>

              <p class="text-xs sm:text-sm text-slate-300 max-w-xl leading-relaxed">
                Dễ dàng tra cứu cụm rạp gần bạn, cập nhật các suất chiếu IMAX Laser, Dolby Atmos sớm nhất và đặt vé giữ chỗ trực tuyến tiện lợi.
              </p>

              <!-- Quick City Selection Tabs (Nút bấm chọn thành phố trang nhã, không chói) -->
              <div class="space-y-2 pt-2">
                <div class="flex items-center justify-between text-xs">
                  <span class="font-bold text-slate-200 flex items-center gap-1.5">
                    <MapPin class="w-3.5 h-3.5 text-rose-500" />
                    <span>Lọc nhanh theo khu vực:</span>
                  </span>
                  <span class="text-[11px] text-slate-400">Chọn thành phố của bạn</span>
                </div>

                <div class="flex flex-wrap items-center gap-2">
                  <button
                    v-for="c in cities"
                    :key="c.id"
                    type="button"
                    @click="selectedCity = c.id"
                    class="px-3.5 py-2 rounded-xl text-xs font-semibold transition cursor-pointer border flex items-center gap-1.5"
                    :class="
                      selectedCity === c.id
                        ? 'bg-rose-600 text-white border-rose-500 shadow-md shadow-rose-600/30'
                        : 'bg-slate-900/80 text-slate-300 border-white/10 hover:border-white/20 hover:text-white hover:bg-slate-800'
                    "
                  >
                    <span>{{ c.name }}</span>
                    <span
                      class="text-[10px] px-1.5 py-0.2 rounded-full font-bold"
                      :class="selectedCity === c.id ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-400'"
                    >
                      {{ c.count }}
                    </span>
                  </button>
                </div>
              </div>

              <!-- Metrics Pills Nhỏ gọn -->
              <div class="flex flex-wrap items-center gap-3 pt-3 text-xs text-slate-300 border-t border-white/[0.08]">
                <div class="flex items-center gap-1.5">
                  <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400" />
                  <span><strong>38+</strong> Cụm rạp đối tác</span>
                </div>
                <span>&bull;</span>
                <div class="flex items-center gap-1.5">
                  <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400" />
                  <span><strong>300+</strong> Suất chiếu/ngày</span>
                </div>
                <span>&bull;</span>
                <div class="flex items-center gap-1.5">
                  <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400" />
                  <span><strong>100%</strong> Vé điện tử QR</span>
                </div>
              </div>
            </div>

            <!-- Cột Phải: VISUAL SHOWCASE PHIM BOM TẤN TẠI RẠP (Lg:col-span-5) -->
            <div class="lg:col-span-5 flex justify-center lg:justify-end">
              <div v-if="spotlightMovie" class="relative max-w-sm w-full">
                <!-- Thẻ poster nền phụ phía sau (tạo hiệu ứng 3D layer) -->
                <div
                  v-if="secondSpotlightMovie"
                  class="absolute -top-3 -right-3 w-48 h-64 rounded-2xl overflow-hidden opacity-40 blur-[0.5px] border border-white/10 hidden sm:block pointer-events-none transform rotate-3"
                >
                  <img :src="secondSpotlightMovie.posterUrl" :alt="secondSpotlightMovie.title" class="w-full h-full object-cover" />
                </div>

                <!-- Thẻ Poster Phim Chính (Visual Cinema Card) -->
                <div
                  @click="goToMovieDetail(spotlightMovie.slug || spotlightMovie.id)"
                  class="relative rounded-2xl bg-slate-900/90 border border-white/10 hover:border-rose-500/50 p-4 shadow-2xl backdrop-blur-xl transition duration-300 cursor-pointer group space-y-3"
                >
                  <div class="flex items-center justify-between text-xs">
                    <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-rose-600 text-white flex items-center gap-1">
                      <Sparkles class="w-3 h-3" />
                      <span>Phim Hot Nhất Tuần</span>
                    </span>
                    <span class="text-amber-400 font-bold text-xs flex items-center gap-1">
                      <Star class="w-3.5 h-3.5 fill-amber-400" />
                      <span>{{ spotlightMovie.rating }} IMDb</span>
                    </span>
                  </div>

                  <div class="flex items-center gap-3.5">
                    <img
                      :src="spotlightMovie.posterUrl"
                      :alt="spotlightMovie.title"
                      class="w-20 h-28 rounded-xl object-cover shadow-lg shrink-0 border border-white/10 group-hover:scale-105 transition-transform duration-300"
                    />

                    <div class="space-y-1.5 min-w-0">
                      <span class="px-2 py-0.5 rounded text-[10px] font-bold bg-slate-800 text-slate-300 border border-white/10">
                        {{ spotlightMovie.ageRatingCode }} &bull; {{ spotlightMovie.durationMinutes }} phút
                      </span>
                      <h3 class="text-sm font-bold text-white group-hover:text-rose-400 transition-colors leading-snug line-clamp-2">
                        {{ spotlightMovie.title }}
                      </h3>
                      <p class="text-[11px] text-slate-400 truncate">
                        {{ spotlightMovie.genre }}
                      </p>
                      <p class="text-[11px] text-emerald-400 font-semibold flex items-center gap-1">
                        <CheckCircle2 class="w-3 h-3" />
                        <span>Đang chiếu tại tất cả cụm rạp</span>
                      </p>
                    </div>
                  </div>

                  <!-- Nút CTA xem lịch chiếu -->
                  <div class="pt-2 border-t border-white/[0.08] flex items-center justify-between text-xs">
                    <span class="text-slate-400 text-[11px]">Định dạng: <strong>IMAX Laser, 2D</strong></span>
                    <span class="inline-flex items-center gap-1 text-rose-400 font-bold group-hover:translate-x-1 transition-transform">
                      <span>Xem lịch chiếu &rarr;</span>
                    </span>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>
      </section>

      <!-- ================= SEARCH, BRAND & FORMAT HUB ================= -->
      <div class="p-4 sm:p-5 rounded-2xl bg-[#0a0d18] border border-white/[0.08] backdrop-blur-xl shadow-xl space-y-4">
        <div class="grid grid-cols-1 sm:grid-cols-12 gap-3 items-center">
          <!-- Keyword input -->
          <div class="sm:col-span-5 relative">
            <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="Tìm theo tên rạp, đường, quận..."
              class="w-full pl-10 pr-9 py-2.5 rounded-xl bg-slate-950 border border-white/10 text-xs sm:text-sm text-white placeholder-slate-400 focus:outline-none focus:border-rose-500 focus:ring-1 focus:ring-rose-500/20 transition"
            />
            <button
              v-if="searchKeyword"
              @click="searchKeyword = ''"
              class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-white"
            >
              <X class="w-3.5 h-3.5" />
            </button>
          </div>

          <!-- City Dropdown -->
          <div class="sm:col-span-4 relative">
            <MapPin class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-rose-500 pointer-events-none" />
            <select
              v-model="selectedCity"
              class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-950 border border-white/10 text-xs sm:text-sm text-slate-200 focus:outline-none focus:border-rose-500 transition cursor-pointer appearance-none font-medium"
            >
              <option v-for="c in cities" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>

          <!-- Format Dropdown -->
          <div class="sm:col-span-3 relative">
            <SlidersHorizontal class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
            <select
              v-model="selectedFormat"
              class="w-full pl-10 pr-8 py-2.5 rounded-xl bg-slate-950 border border-white/10 text-xs sm:text-sm text-slate-200 focus:outline-none focus:border-rose-500 transition cursor-pointer appearance-none font-medium"
            >
              <option value="ALL">Tất cả định dạng</option>
              <option v-for="fmt in formats.slice(1)" :key="fmt" :value="fmt">{{ fmt }}</option>
            </select>
          </div>
        </div>

        <!-- Filter Row 2: Chọn Thương Hiệu Rạp (Brands Pill Strip) -->
        <div class="flex flex-wrap items-center justify-between gap-3 pt-3 border-t border-white/[0.06] text-xs">
          <div class="flex flex-wrap items-center gap-1.5">
            <span class="text-slate-400 font-semibold mr-1">Thương hiệu:</span>
            <button
              v-for="b in brands"
              :key="b.id"
              type="button"
              @click="selectedBrand = b.id"
              class="px-3.5 py-1.5 rounded-lg text-xs font-semibold transition cursor-pointer"
              :class="
                selectedBrand === b.id
                  ? 'bg-rose-600 text-white shadow-md shadow-rose-600/30'
                  : 'bg-slate-900/80 text-slate-300 hover:text-white border border-white/10 hover:border-white/20 hover:bg-slate-800'
              "
            >
              {{ b.name }}
            </button>
          </div>

          <div class="text-xs text-slate-400">
            Tìm thấy <strong class="text-white">{{ filteredTheaters.length }}</strong> cụm rạp phù hợp
          </div>
        </div>
      </div>

      <!-- ================= 2-COLUMN LAYOUT: 70% BÊN TRÁI & 30% BÊN PHẢI ================= -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">

        <!-- ================= BÊN TRÁI: 70% (LG:COL-SPAN-8) - HIỂN THỊ THEO TỪNG DÒNG ================= -->
        <main class="lg:col-span-8 space-y-4">
          <div
            v-for="theater in filteredTheaters"
            :key="theater.id"
            @click="goToTheaterDetail(theater.id)"
            class="group rounded-2xl bg-slate-900/70 hover:bg-slate-900 border border-white/[0.08] hover:border-rose-500/40 p-5 space-y-4 transition-all duration-300 cursor-pointer shadow-xl backdrop-blur-md"
          >
            <!-- Dòng 1: Header rạp (Tên rạp, Brand, Rating, Địa chỉ & Ảnh nhỏ) -->
            <div class="flex items-start justify-between gap-4">
              <div class="space-y-1.5 flex-1 min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <span class="px-2.5 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider bg-rose-500/10 text-rose-400 border border-rose-500/20">
                    {{ theater.brand }}
                  </span>
                  <span class="text-amber-400 font-bold text-xs flex items-center gap-1">
                    <Star class="w-3.5 h-3.5 fill-amber-400" />
                    {{ theater.rating }}
                  </span>
                  <span class="text-slate-400 text-xs hidden sm:inline">
                    ({{ theater.reviewsCount }} đánh giá)
                  </span>
                  <span class="text-slate-400 text-xs hidden sm:inline">
                    &bull; {{ theater.totalScreens }} phòng chiếu kỹ thuật số
                  </span>
                </div>

                <h3 class="text-base sm:text-lg font-bold text-white group-hover:text-rose-400 transition-colors leading-snug">
                  {{ theater.name }}
                </h3>

                <p class="text-xs text-slate-400 flex items-start gap-1.5 leading-relaxed">
                  <MapPin class="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                  <span>{{ theater.address }}</span>
                </p>
              </div>

              <!-- Thumbnail ảnh rạp -->
              <div class="relative w-20 h-16 sm:w-24 sm:h-20 rounded-xl overflow-hidden shrink-0 border border-white/[0.1] bg-slate-950 shadow-md">
                <img
                  :src="theater.imageUrl"
                  :alt="theater.name"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                />
              </div>
            </div>

            <!-- Dòng 2: Định dạng phòng chiếu (Badges) -->
            <div class="flex flex-wrap items-center gap-1.5">
              <span
                v-for="fmt in theater.formats"
                :key="fmt"
                class="px-2.5 py-0.5 rounded-md text-[10px] font-medium bg-black/40 text-slate-300 border border-white/[0.08]"
              >
                {{ fmt }}
              </span>
            </div>

            <!-- Dòng 3: HIỂN THỊ 1 PHẦN PHIM CÔNG CHIẾU TẠI RẠP (Theo từng dòng nhỏ gọn) -->
            <div class="pt-3 border-t border-white/[0.06] space-y-2">
              <div class="flex items-center justify-between text-xs">
                <span class="font-semibold text-slate-300 flex items-center gap-1.5">
                  <Film class="w-3.5 h-3.5 text-rose-500" />
                  <span>Phim công chiếu hôm nay:</span>
                </span>
                <span class="text-[11px] text-rose-400 font-semibold group-hover:underline flex items-center gap-0.5">
                  Xem tất cả {{ theater.movies.length }} phim &rarr;
                </span>
              </div>

              <!-- Danh sách preview 2-3 phim (Hiển thị 1 phần) -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                <div
                  v-for="m in theater.movies.slice(0, 2)"
                  :key="m.id"
                  class="flex items-center justify-between p-2 rounded-xl bg-black/40 border border-white/[0.06] hover:border-white/20 gap-2.5 transition"
                >
                  <div class="flex items-center gap-2 min-w-0">
                    <img :src="m.posterUrl" :alt="m.title" class="w-7 h-10 rounded object-cover shrink-0 shadow-sm" />
                    <div class="min-w-0">
                      <p class="text-xs font-semibold text-white truncate">{{ m.title }}</p>
                      <p class="text-[10px] text-slate-400">{{ m.genre.split(',')[0] }} &bull; {{ m.durationMinutes }}'</p>
                    </div>
                  </div>

                  <!-- 2 suất chiếu mẫu -->
                  <div class="flex items-center gap-1 shrink-0">
                    <span
                      v-for="st in m.showtimes.slice(0, 2)"
                      :key="st.time"
                      class="px-2 py-1 rounded-md text-[10px] font-mono font-bold bg-slate-800 text-slate-200 border border-white/10"
                    >
                      {{ st.time }}
                    </span>
                    <span v-if="m.showtimes.length > 2" class="text-[10px] text-slate-400 font-semibold">
                      +{{ m.showtimes.length - 2 }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="flex items-center justify-between text-[11px] text-slate-400 pt-0.5">
                <span>+ Còn <strong class="text-white">{{ theater.movies.length - 2 }}</strong> phim khác đang chiếu tại rạp này</span>
                <span class="text-slate-400 flex items-center gap-1">
                  <Clock class="w-3 h-3 text-slate-400" />
                  <span>{{ theater.openHours }}</span>
                </span>
              </div>
            </div>

            <!-- Dòng 4: Nút Xem chi tiết rạp & lịch chiếu -->
            <div class="pt-2.5 border-t border-white/[0.06] flex items-center justify-end text-xs">
              <span class="inline-flex items-center gap-1 text-rose-400 font-bold group-hover:text-rose-300 transition">
                <span>Xem chi tiết rạp &amp; lịch chiếu</span>
                <ChevronRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </span>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="filteredTheaters.length === 0" class="text-center py-16 bg-slate-900/60 rounded-2xl border border-white/[0.08] space-y-3">
            <Building2 class="w-10 h-10 text-slate-500 mx-auto" />
            <h3 class="text-base font-bold text-white">Không tìm thấy rạp chiếu nào phù hợp</h3>
            <p class="text-xs text-slate-400">Vui lòng chọn thành phố khác hoặc tìm kiếm với từ khóa khác.</p>
            <button
              type="button"
              @click="selectedCity = 'ALL'; selectedBrand = 'ALL'; selectedFormat = 'ALL'; searchKeyword = ''"
              class="px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition cursor-pointer shadow-lg shadow-rose-600/30"
            >
              Xem tất cả rạp
            </button>
          </div>
        </main>

        <!-- ================= BÊN PHẢI: 30% (LG:COL-SPAN-4) - THÔNG TIN KHÁC BỔ TRỢ ================= -->
        <aside class="lg:col-span-4 space-y-5 lg:sticky lg:top-24">

          <!-- 1. Thẻ Cụm Rạp Nổi Bật Trong Tuần (Featured Theater) -->
          <div
            v-if="featuredTheater"
            class="p-5 rounded-2xl bg-slate-900/80 border border-rose-500/30 space-y-3.5 relative overflow-hidden shadow-xl backdrop-blur-xl"
          >
            <div class="flex items-center justify-between">
              <span class="px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider bg-rose-500/15 text-rose-400 border border-rose-500/30 flex items-center gap-1">
                <Sparkles class="w-3 h-3 text-amber-400" />
                <span>Rạp Nổi Bật Tuần Này</span>
              </span>
              <span class="text-amber-400 font-bold text-xs flex items-center gap-1">
                <Star class="w-3.5 h-3.5 fill-amber-400" />
                {{ featuredTheater.rating }}
              </span>
            </div>

            <div class="relative h-28 rounded-xl overflow-hidden border border-white/[0.1] shadow-md">
              <img :src="featuredTheater.imageUrl" :alt="featuredTheater.name" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent"></div>
              <span class="absolute bottom-2 left-2 text-xs font-bold text-white truncate max-w-[90%]">
                {{ featuredTheater.name }}
              </span>
            </div>

            <p class="text-xs text-slate-300 leading-relaxed">
              Trang bị màn chiếu cực đại IMAX Laser cùng âm thanh vòm Dolby Atmos 64 kênh mang lại trải nghiệm đỉnh cao.
            </p>

            <button
              type="button"
              @click="goToTheaterDetail(featuredTheater.id)"
              class="w-full py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition flex items-center justify-center gap-1.5 shadow-md shadow-rose-600/30 cursor-pointer"
            >
              <span>Xem chi tiết &amp; Lịch chiếu rạp này</span>
              <ChevronRight class="w-3.5 h-3.5" />
            </button>
          </div>

          <!-- 2. Ưu Đãi & Khuyến Mãi Thành Viên -->
          <div class="p-5 rounded-2xl bg-slate-900/80 border border-white/[0.08] backdrop-blur-xl shadow-xl space-y-3.5">
            <h3 class="text-sm font-bold text-white flex items-center gap-2">
              <Percent class="w-4 h-4 text-amber-400" />
              <span>Ưu Đãi Rạp Chiếu Đang Áp Dụng</span>
            </h3>

            <div class="space-y-2.5 text-xs">
              <div class="p-2.5 rounded-xl bg-black/40 border border-white/[0.07] space-y-1">
                <p class="font-bold text-amber-300">Thứ Tư Vui Vẻ - Đồng Giá 55K</p>
                <p class="text-[11px] text-slate-400">Áp dụng cho mọi vé 2D thành viên CineVerse và HSSV vào thứ 4 hàng tuần.</p>
              </div>

              <div class="p-2.5 rounded-xl bg-black/40 border border-white/[0.07] space-y-1">
                <p class="font-bold text-rose-300">Giảm 20% Combo Bắp Nước</p>
                <p class="text-[11px] text-slate-400">Nhận mã giảm giá bắp phô mai khi thanh toán đặt vé trực tuyến qua VietQR.</p>
              </div>
            </div>

            <router-link
              to="/promotions"
              class="block text-center text-xs text-rose-400 hover:underline font-semibold pt-1"
            >
              Xem tất cả voucher khuyến mãi &rarr;
            </router-link>
          </div>

          <!-- 3. Cẩm Nang Công Nghệ Phòng Chiếu -->
          <div class="p-5 rounded-2xl bg-slate-900/80 border border-white/[0.08] backdrop-blur-xl shadow-xl space-y-3">
            <h3 class="text-sm font-bold text-white flex items-center gap-2">
              <Tv class="w-4 h-4 text-slate-300" />
              <span>Chuẩn Công Nghệ Chiếu</span>
            </h3>

            <div class="space-y-2 text-xs">
              <div class="space-y-0.5">
                <p class="font-bold text-slate-200">IMAX Laser</p>
                <p class="text-[11px] text-slate-400">Màn cong khổng lồ, máy chiếu Laser 4K kép siêu nét.</p>
              </div>

              <div class="space-y-0.5">
                <p class="font-bold text-slate-200">Dolby Atmos</p>
                <p class="text-[11px] text-slate-400">Hệ thống âm vòm 360 độ từ trần và các vách tường.</p>
              </div>

              <div class="space-y-0.5">
                <p class="font-bold text-slate-200">ScreenX 270°</p>
                <p class="text-[11px] text-slate-400">Phóng chiếu hình ảnh bao quát 3 mặt vách phòng chiếu.</p>
              </div>
            </div>
          </div>

          <!-- 4. Hỗ Trợ Đặt Vé Đoàn & Hotline -->
          <div class="p-4 rounded-2xl bg-slate-900/60 border border-white/[0.07] text-xs text-slate-400 space-y-2">
            <div class="flex items-center gap-2 text-slate-200 font-bold">
              <Phone class="w-4 h-4 text-emerald-400" />
              <span>Hỗ trợ đặt vé &amp; giải đáp thắc mắc</span>
            </div>
            <p class="text-[11px]">Tổng đài CSKH: <strong class="text-white">1900 6868</strong> (08:00 – 24:00 hàng ngày)</p>
          </div>

        </aside>

      </div>

    </div>
  </div>
</template>
