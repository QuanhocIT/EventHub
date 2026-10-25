<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { THEATERS_DATA, getTheaterById, type TheaterComplex, type MovieAtTheater, type TheaterShowtime } from '@/services/cinemas.data'
import {
  MapPin,
  Phone,
  Clock,
  Star,
  Monitor,
  ChevronLeft,
  ChevronRight,
  Calendar,
  Ticket,
  CheckCircle2,
  Car,
  Tv,
  Volume2,
  ExternalLink,
  Film,
  Building2,
  Sparkles,
  Share2,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const theaterId = computed(() => Number(route.params.id))
const theater = computed<TheaterComplex | undefined>(() => getTheaterById(theaterId.value))

// Date selection
const selectedShowDate = ref('TODAY')
const showDates = [
  { id: 'TODAY', label: 'Hôm Nay', sub: '25/10 (Thứ 7)' },
  { id: 'TOMORROW', label: 'Ngày Mai', sub: '26/10 (CN)' },
  { id: 'DAY_2', label: 'Thứ Hai', sub: '27/10' },
  { id: 'DAY_3', label: 'Thứ Ba', sub: '28/10' },
  { id: 'DAY_4', label: 'Thứ Tư', sub: '29/10 (Happy Day)' },
]

// Movie format filter
const selectedFormatFilter = ref('ALL')
const availableFormats = computed(() => {
  if (!theater.value) return []
  return ['ALL', ...theater.value.formats]
})

const getRatingColor = (code: string) => {
  switch (code) {
    case 'T18':
      return 'bg-rose-500/15 text-rose-400 border-rose-500/30'
    case 'T16':
      return 'bg-amber-500/15 text-amber-400 border-amber-500/30'
    case 'T13':
      return 'bg-yellow-500/15 text-yellow-300 border-yellow-500/30'
    case 'K':
      return 'bg-blue-500/15 text-blue-400 border-blue-500/30'
    case 'P':
    default:
      return 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30'
  }
}

const bookShowtime = (movie: MovieAtTheater, time: TheaterShowtime) => {
  router.push(`/movies/${movie.slug || movie.id}`)
}

const copyShareLink = () => {
  navigator.clipboard?.writeText(window.location.href)
  alert('Đã sao chép liên kết rạp chiếu vào khay nhớ tạm!')
}

onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
})
</script>

<template>
  <div class="min-h-screen bg-[#030712] text-slate-100 py-6 sm:py-10 relative overflow-hidden selection:bg-cyan-500 selection:text-black">
    <!-- Ambient Cinematic Lighting Gradient Meshes -->
    <div class="fixed inset-0 pointer-events-none z-0">
      <!-- Top Cinema Ceiling Light Cone -->
      <div class="absolute -top-32 left-1/2 -translate-x-1/2 w-[1000px] h-[550px] bg-[radial-gradient(ellipse_at_center,rgba(56,189,248,0.12),rgba(99,102,241,0.08),transparent_70%)]"></div>
      <div class="absolute top-1/4 -left-40 w-[600px] h-[600px] bg-blue-600/[0.08] rounded-full blur-[150px]"></div>
      <div class="absolute top-2/3 -right-40 w-[650px] h-[650px] bg-purple-600/[0.07] rounded-full blur-[160px]"></div>
      <div class="absolute inset-0 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:36px_36px] opacity-[0.03]"></div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 space-y-8 relative z-10">

      <!-- Breadcrumbs & Back Button -->
      <div class="flex items-center justify-between gap-4 text-xs">
        <button
          type="button"
          @click="router.push('/cinemas')"
          class="inline-flex items-center gap-1.5 text-slate-400 hover:text-cyan-300 transition cursor-pointer font-medium"
        >
          <ChevronLeft class="w-4 h-4" />
          <span>Quay lại danh sách rạp</span>
        </button>

        <div class="flex items-center gap-2 text-slate-400">
          <router-link to="/" class="hover:text-cyan-300">Trang chủ</router-link>
          <span>/</span>
          <router-link to="/cinemas" class="hover:text-cyan-300">Rạp chiếu</router-link>
          <span>/</span>
          <span class="text-slate-200 font-semibold truncate max-w-[200px]">{{ theater?.name || 'Chi tiết rạp' }}</span>
        </div>
      </div>

      <!-- Theater Not Found State -->
      <div v-if="!theater" class="text-center py-20 bg-[#080d1f]/80 rounded-3xl border border-white/[0.08] space-y-4 backdrop-blur-xl">
        <Building2 class="w-12 h-12 text-slate-500 mx-auto" />
        <h2 class="text-xl font-bold text-white">Không tìm thấy thông tin cụm rạp này</h2>
        <p class="text-xs text-slate-400">Rạp chiếu bạn tìm kiếm không tồn tại hoặc đã ngừng hoạt động.</p>
        <button
          type="button"
          @click="router.push('/cinemas')"
          class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white text-xs font-bold transition shadow-lg shadow-blue-600/30 cursor-pointer"
        >
          Xem tất cả cụm rạp
        </button>
      </div>

      <!-- Main Theater Content -->
      <template v-else>
        <!-- ================= THEATER HERO COVER ================= -->
        <section class="relative rounded-3xl overflow-hidden border border-white/[0.08] bg-[#060a18] shadow-2xl backdrop-blur-2xl">
          <!-- Background Image with Gradient Mask -->
          <div class="relative h-64 sm:h-80 lg:h-96 w-full overflow-hidden">
            <img
              :src="theater.imageUrl"
              :alt="theater.name"
              class="w-full h-full object-cover opacity-60 scale-105"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-[#060a18] via-[#060a18]/70 to-transparent"></div>

            <!-- Share Button Top Right -->
            <button
              type="button"
              @click="copyShareLink"
              class="absolute top-4 right-4 p-2.5 rounded-full bg-[#040711]/80 hover:bg-[#0c142c] text-slate-300 hover:text-white border border-white/[0.1] backdrop-blur transition cursor-pointer"
              title="Chia sẻ rạp"
            >
              <Share2 class="w-4 h-4" />
            </button>
          </div>

          <!-- Theater Meta on Cover -->
          <div class="p-6 sm:p-8 -mt-24 relative z-10 space-y-4">
            <div class="flex flex-wrap items-center gap-2">
              <span class="px-3 py-1 rounded-full text-xs font-bold bg-gradient-to-r from-blue-600 to-indigo-600 text-white shadow-md">
                {{ theater.brand }}
              </span>
              <span class="text-amber-400 font-bold text-xs flex items-center gap-1 px-3 py-1 rounded-full bg-[#040711]/80 border border-white/[0.08] backdrop-blur-md">
                <Star class="w-3.5 h-3.5 fill-amber-400" />
                <span>{{ theater.rating }}</span>
                <span class="text-slate-400 font-normal">({{ theater.reviewsCount }} đánh giá)</span>
              </span>
              <span class="text-slate-300 text-xs px-3 py-1 rounded-full bg-[#040711]/80 border border-white/[0.08] backdrop-blur-md">
                {{ theater.totalScreens }} Phòng chiếu &bull; {{ theater.totalSeats }} Ghế
              </span>
            </div>

            <h1 class="text-2xl sm:text-4xl font-extrabold text-white tracking-tight leading-tight">
              {{ theater.name }}
            </h1>

            <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs text-slate-300 pt-2">
              <div class="flex items-start gap-2">
                <MapPin class="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                <span class="leading-relaxed">{{ theater.address }}</span>
              </div>
              <div class="flex items-center gap-2">
                <Phone class="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Hotline: <strong class="text-white">{{ theater.phone }}</strong></span>
              </div>
              <div class="flex items-center gap-2">
                <Clock class="w-4 h-4 text-amber-400 shrink-0" />
                <span>Giờ mở cửa: <strong class="text-white">{{ theater.openHours }}</strong></span>
              </div>
            </div>

            <!-- Action buttons: Google Maps & Hotline -->
            <div class="flex flex-wrap items-center gap-3 pt-3">
              <a
                :href="`https://maps.google.com/?q=${encodeURIComponent(theater.name + ' ' + theater.address)}`"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white text-xs font-bold transition shadow-lg shadow-blue-600/30 cursor-pointer"
              >
                <ExternalLink class="w-3.5 h-3.5" />
                <span>Chỉ đường trên Google Maps</span>
              </a>

              <a
                :href="`tel:${theater.phone.split(' ')[0]}`"
                class="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-[#040711] hover:bg-[#0c142c] text-slate-200 text-xs font-bold transition border border-white/[0.1] cursor-pointer"
              >
                <Phone class="w-3.5 h-3.5 text-cyan-400" />
                <span>Gọi tổng đài hỗ trợ</span>
              </a>
            </div>
          </div>
        </section>

        <!-- ================= LỊCH CHIẾU VÀ TẤT CẢ PHIM CÔNG CHIẾU ================= -->
        <section class="space-y-6">
          <div class="flex flex-wrap items-center justify-between gap-4 border-b border-white/[0.08] pb-4">
            <div>
              <h2 class="text-xl sm:text-2xl font-extrabold text-white flex items-center gap-2.5">
                <Film class="w-5 h-5 text-cyan-400" />
                <span>Lịch Chiếu &amp; Suất Chiếu Tại Rạp</span>
              </h2>
              <p class="text-xs text-slate-400 mt-0.5">
                Đang có <strong class="text-white">{{ theater.movies.length }}</strong> phim công chiếu hôm nay. Chọn khung giờ để đặt vé trực tiếp.
              </p>
            </div>

            <!-- Format filter -->
            <div class="flex items-center gap-2 text-xs">
              <span class="text-slate-400">Định dạng:</span>
              <select
                v-model="selectedFormatFilter"
                class="px-3.5 py-2 rounded-xl bg-[#040711] border border-white/[0.1] text-slate-200 text-xs font-semibold focus:outline-none focus:border-cyan-400 cursor-pointer"
              >
                <option value="ALL">Tất cả định dạng</option>
                <option v-for="fmt in theater.formats" :key="fmt" :value="fmt">{{ fmt }}</option>
              </select>
            </div>
          </div>

          <!-- Date Selector Tabs -->
          <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
            <button
              v-for="d in showDates"
              :key="d.id"
              type="button"
              @click="selectedShowDate = d.id"
              class="px-4 py-2.5 rounded-2xl text-xs font-bold transition cursor-pointer border text-center shrink-0 min-w-[110px]"
              :class="
                selectedShowDate === d.id
                  ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white border-blue-400/50 shadow-md shadow-blue-600/30'
                  : 'bg-[#080d1f] text-slate-300 border-white/[0.08] hover:text-white hover:border-cyan-500/40 hover:bg-[#0c142c]'
              "
            >
              <p class="font-extrabold">{{ d.label }}</p>
              <p class="text-[10px] opacity-75 mt-0.5">{{ d.sub }}</p>
            </button>
          </div>

          <!-- Movie Cards with Complete Showtimes -->
          <div class="space-y-4">
            <div
              v-for="movie in theater.movies"
              :key="movie.id"
              class="p-5 sm:p-6 rounded-2xl bg-gradient-to-br from-[#0c1633]/85 via-[#090f26]/90 to-[#050917]/95 border border-white/[0.08] hover:border-cyan-500/40 space-y-4 backdrop-blur-xl shadow-xl transition duration-300"
            >
              <!-- Movie Top Info -->
              <div class="flex items-start gap-4">
                <img
                  :src="movie.posterUrl"
                  :alt="movie.title"
                  class="w-16 h-24 sm:w-20 sm:h-28 rounded-xl object-cover shrink-0 border border-white/[0.1] shadow-md"
                />

                <div class="space-y-1.5 flex-1 min-w-0">
                  <div class="flex flex-wrap items-center gap-2">
                    <span
                      class="px-2 py-0.5 rounded text-[10px] font-bold border"
                      :class="getRatingColor(movie.ageRatingCode)"
                    >
                      {{ movie.ageRatingCode }}
                    </span>
                    <span class="text-amber-400 font-bold text-xs flex items-center gap-1">
                      <Star class="w-3.5 h-3.5 fill-amber-400" />
                      <span>{{ movie.rating }}</span>
                    </span>
                    <span class="text-slate-400 text-xs">
                      {{ movie.genre }} &bull; {{ movie.durationMinutes }} phút
                    </span>
                  </div>

                  <h3
                    @click="router.push(`/movies/${movie.slug || movie.id}`)"
                    class="text-base sm:text-lg font-bold text-white hover:text-cyan-300 transition cursor-pointer leading-snug"
                  >
                    {{ movie.title }}
                  </h3>

                  <p class="text-xs text-slate-400">
                    Đạo diễn: <span class="text-slate-300">{{ movie.director }}</span>
                  </p>
                </div>
              </div>

              <!-- Complete Showtimes Strip -->
              <div class="pt-3 border-t border-white/[0.06] space-y-2">
                <p class="text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Ticket class="w-3.5 h-3.5 text-cyan-400" />
                  <span>Suất chiếu trong ngày (Nhấn để chọn ghế):</span>
                </p>

                <div class="flex flex-wrap gap-2.5">
                  <button
                    v-for="st in movie.showtimes"
                    :key="st.time"
                    type="button"
                    @click="bookShowtime(movie, st)"
                    class="group/time px-3.5 py-2 rounded-xl bg-[#040711] border border-white/[0.09] hover:border-cyan-400 hover:bg-gradient-to-r hover:from-blue-600 hover:to-indigo-600 text-left transition cursor-pointer shadow-sm hover:shadow-lg hover:shadow-blue-600/30"
                  >
                    <div class="flex items-center gap-2">
                      <span class="text-sm font-mono font-bold text-white group-hover/time:text-white">{{ st.time }}</span>
                      <span class="text-[10px] font-bold px-1.5 py-0.5 rounded bg-[#0a1024] group-hover/time:bg-white/20 text-cyan-300 group-hover/time:text-white border border-cyan-500/20 group-hover/time:border-transparent">
                        {{ st.format }}
                      </span>
                    </div>
                    <div class="flex items-center justify-between text-[10px] text-slate-400 group-hover/time:text-blue-100 mt-1 gap-2">
                      <span>{{ st.screen }}</span>
                      <span class="text-emerald-400 group-hover/time:text-emerald-200">Còn {{ st.availableSeats }} ghế</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </section>

        <!-- ================= THÔNG TIN CÔNG NGHỆ, TIỆN ÍCH & GỬI XE ================= -->
        <section class="grid grid-cols-1 md:grid-cols-2 gap-6 pt-4">
          <!-- Tech Specs & Facilities -->
          <div class="p-6 rounded-2xl bg-[#080d1f]/85 border border-white/[0.08] backdrop-blur-xl shadow-xl space-y-4">
            <h3 class="text-base font-bold text-white flex items-center gap-2">
              <Monitor class="w-4 h-4 text-cyan-400" />
              <span>Công Nghệ &amp; Tiện Ích Rạp</span>
            </h3>

            <!-- Tech specs -->
            <div class="space-y-3">
              <div
                v-for="(spec, idx) in theater.techSpecs"
                :key="idx"
                class="p-3.5 rounded-xl bg-[#040711] border border-white/[0.07] space-y-1"
              >
                <p class="text-xs font-bold text-white">{{ spec.title }}</p>
                <p class="text-[11px] text-slate-400 leading-relaxed">{{ spec.description }}</p>
              </div>
            </div>

            <!-- Facilities badges -->
            <div class="pt-2 border-t border-white/[0.06]">
              <p class="text-xs font-semibold text-slate-300 mb-2">Tiện ích phục vụ:</p>
              <div class="flex flex-wrap gap-2 text-xs">
                <span
                  v-for="fac in theater.facilities"
                  :key="fac"
                  class="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-[#040711] border border-white/[0.07] text-slate-300"
                >
                  <CheckCircle2 class="w-3.5 h-3.5 text-emerald-400" />
                  <span>{{ fac }}</span>
                </span>
              </div>
            </div>
          </div>

          <!-- Parking & Location Guide -->
          <div class="p-6 rounded-2xl bg-[#080d1f]/85 border border-white/[0.08] backdrop-blur-xl shadow-xl space-y-4 flex flex-col justify-between">
            <div class="space-y-4">
              <h3 class="text-base font-bold text-white flex items-center gap-2">
                <Car class="w-4 h-4 text-emerald-400" />
                <span>Hướng Dẫn Gửi Xe &amp; Di Chuyển</span>
              </h3>

              <div class="p-4 rounded-xl bg-[#040711] border border-white/[0.07] text-xs text-slate-300 space-y-2">
                <p class="font-bold text-white">Vị trí đỗ xe:</p>
                <p class="leading-relaxed text-slate-400">{{ theater.parkingGuide }}</p>
              </div>

              <div class="p-4 rounded-xl bg-[#040711] border border-white/[0.07] text-xs space-y-1">
                <p class="font-bold text-white">Lưu ý vào rạp:</p>
                <p class="text-slate-400 leading-relaxed">
                  Quý khách vui lòng có mặt trước giờ chiếu 10-15 phút. Xuất trình mã vé điện tử QR tại cổng soát vé để vào thẳng phòng chiếu mà không cần in vé giấy.
                </p>
              </div>
            </div>

            <div class="pt-4 border-t border-white/[0.06] flex items-center justify-between text-xs text-slate-400">
              <span>Hệ thống vé điện tử EventHub</span>
              <button
                type="button"
                @click="router.push('/cinemas')"
                class="text-cyan-400 hover:underline font-semibold"
              >
                Khám phá cụm rạp khác &rarr;
              </button>
            </div>
          </div>
        </section>
      </template>

    </div>
  </div>
</template>
