<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, type Directive } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  ChevronLeft,
  ChevronRight,
  Film,
  Calendar,
  Building2,
  Users,
  Target,
  Eye,
  Gem,
  CheckCircle2,
  Zap,
  ShieldCheck,
  Ticket,
  Headphones,
  Sparkles,
  Compass,
  Rocket,
  MapPin,
  Star,
} from 'lucide-vue-next'

const router = useRouter()

/* ---------------- Scroll reveal directive ---------------- */
const vReveal: Directive<HTMLElement, number | undefined> = {
  mounted(el, binding) {
    el.classList.add('reveal')
    el.style.transitionDelay = `${binding.value ?? 0}ms`
    const io = new IntersectionObserver(
      (entries) => {
        entries.forEach((e) => {
          if (e.isIntersecting) {
            el.classList.add('reveal-in')
            io.unobserve(el)
          }
        })
      },
      { threshold: 0.12 },
    )
    io.observe(el)
  },
}

/* ---------------- Hero slider (autoplay + progress) ---------------- */
const heroSlide = ref(0)
const heroPaused = ref(false)
const SLIDE_MS = 5500
const progressKey = ref(0)
let heroTimer: number | undefined

const heroSlides = [
  {
    type: 'concert',
    badge: 'Đại Nhạc Hội & Live Tour 2026',
    tagline: 'More Than Just Tickets',
    image: '/images/about_hero_concert.jpg',
    highlight: 'Sân khấu âm nhạc bùng nổ cảm xúc cùng hàng ngàn khán giả',
    venue: 'Sân Vận Động Quốc Gia Mỹ Đình • Hà Nội',
  },
  {
    type: 'cinema',
    badge: 'Rạp Chiếu Phim & Trải Nghiệm IMAX',
    tagline: 'Cinematic Experience',
    image: '/images/about_hero_cinema.jpg',
    highlight: 'Không gian điện ảnh đẳng cấp với màn chiếu cực đại & Dolby Atmos',
    venue: 'Hệ thống rạp đối tác CGV, Lotte, BHD, Galaxy',
  },
  {
    type: 'festival',
    badge: 'Lễ Hội Âm Nhạc & Countdown Festival',
    tagline: 'Unforgettable Moments',
    image: '/images/about_hero_real.jpg',
    highlight: 'Khoảnh khắc kết nối nghệ sĩ và người hâm mộ khắp Việt Nam',
    venue: 'Trung Tâm Triển Lãm & Hội Nghị SECC • TP.HCM',
  },
]

const goHero = (i: number) => {
  heroSlide.value = (i + heroSlides.length) % heroSlides.length
  progressKey.value++
}
const nextHeroSlide = () => goHero(heroSlide.value + 1)
const prevHeroSlide = () => goHero(heroSlide.value - 1)

const startAuto = () => {
  stopAuto()
  heroTimer = window.setInterval(() => {
    if (!heroPaused.value) nextHeroSlide()
  }, SLIDE_MS)
}
const stopAuto = () => {
  if (heroTimer) window.clearInterval(heroTimer)
}

/* ---------------- 3D tilt for hero card ---------------- */
const tilt = ref({ x: 0, y: 0 })
const onTilt = (e: MouseEvent) => {
  const r = (e.currentTarget as HTMLElement).getBoundingClientRect()
  tilt.value = {
    x: ((e.clientY - r.top) / r.height - 0.5) * -8,
    y: ((e.clientX - r.left) / r.width - 0.5) * 8,
  }
}
const resetTilt = () => (tilt.value = { x: 0, y: 0 })
const tiltStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${tilt.value.x}deg) rotateY(${tilt.value.y}deg)`,
}))

/* ---------------- Animated counters ---------------- */
const stats = [
  { icon: Film, value: 1200, prefix: '+', suffix: '', label: 'Phim đang chiếu', color: 'blue' },
  { icon: Calendar, value: 5000, prefix: '+', suffix: '', label: 'Sự kiện & concert', color: 'indigo' },
  { icon: Building2, value: 300, prefix: '+', suffix: '', label: 'Rạp chiếu đối tác', color: 'purple' },
  { icon: Users, value: 1.5, prefix: '+', suffix: 'M', label: 'Khách hàng tin tưởng', color: 'cyan' },
]
const counts = ref<number[]>(stats.map(() => 0))
const statsEl = ref<HTMLElement | null>(null)
let statsObserver: IntersectionObserver | undefined

const runCounters = () => {
  const dur = 1800
  const t0 = performance.now()
  const tick = (now: number) => {
    const p = Math.min((now - t0) / dur, 1)
    const eased = 1 - Math.pow(1 - p, 3)
    counts.value = stats.map((s) => s.value * eased)
    if (p < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}
const fmt = (i: number) => {
  const s = stats[i]
  const v = counts.value[i]
  const n = s.suffix === 'M' ? v.toFixed(1) : Math.round(v).toLocaleString('vi-VN')
  return `${s.prefix}${n}${s.suffix}`
}

/* ---------------- Timeline ---------------- */
const milestones = [
  { year: '2021', title: 'Khởi nguồn ý tưởng', desc: 'Nhóm sáng lập cùng đam mê điện ảnh & âm nhạc bắt tay xây nền tảng đặt vé đầu tiên.', icon: Compass },
  { year: '2022', title: 'Ra mắt CineVerse', desc: 'Kết nối những cụm rạp đầu tiên với giữ ghế thời gian thực.', icon: Film },
  { year: '2024', title: 'Mở rộng EventHub', desc: 'Đưa concert, lễ hội và sự kiện lớn lên cùng một hệ sinh thái.', icon: Rocket },
  { year: '2026', title: '1,5 triệu khán giả', desc: 'Trở thành nền tảng đặt vé giải trí được tin dùng trên toàn quốc.', icon: Star },
]

/* ---------------- Partners ---------------- */
const partnerSlide = ref(0)
const partnerEvents = [
  { id: 1, title: 'Đại Nhạc Hội Live Tour', artist: 'Jack - J97', image: '/images/event_jack.jpg' },
  { id: 2, title: 'Giao Lưu Điện Ảnh Quốc Tế', artist: 'Lee Min Ho Fan Meeting', image: '/images/event_leeminho.jpg' },
  { id: 3, title: 'Hòa Nhạc Giao Hưởng Quốc Gia', artist: 'Vietnam National Symphony', image: '/images/about_cinema_hall.jpg' },
  { id: 4, title: 'Born Pink World Tour', artist: 'BlackPink Concert Vietnam', image: '/images/event_blackpink.jpg' },
]
const nextPartner = () => (partnerSlide.value = (partnerSlide.value + 1) % partnerEvents.length)
const prevPartner = () =>
  (partnerSlide.value = (partnerSlide.value - 1 + partnerEvents.length) % partnerEvents.length)

const brands = ['CGV', 'LOTTE CINEMA', 'BHD STAR', 'GALAXY CINEMA', 'THE GRAND HO TRAM', 'MEGA GS', 'BETA CINEMAS']

onMounted(() => {
  startAuto()
  statsObserver = new IntersectionObserver(
    (entries) => {
      if (entries[0].isIntersecting) {
        runCounters()
        statsObserver?.disconnect()
      }
    },
    { threshold: 0.35 },
  )
  if (statsEl.value) statsObserver.observe(statsEl.value)
})
onBeforeUnmount(() => {
  stopAuto()
  statsObserver?.disconnect()
})
</script>

<template>
  <div class="relative min-h-screen bg-[#050811] text-slate-200 py-6 sm:py-10 overflow-hidden">
    <!-- Page-wide animated ambient orbs -->
    <div class="pointer-events-none absolute inset-0 -z-0">
      <div class="orb orb-a"></div>
      <div class="orb orb-b"></div>
      <div class="orb orb-c"></div>
      <div class="absolute inset-0 grid-bg"></div>
    </div>

    <div class="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10 sm:space-y-14">
      <!-- ================= HERO ================= -->
      <section class="relative pt-2">
        <div class="absolute -top-24 -left-24 w-[420px] h-[420px] bg-blue-600/15 rounded-full blur-[120px] pointer-events-none"></div>
        <div class="absolute top-10 right-0 w-[360px] h-[360px] bg-fuchsia-600/15 rounded-full blur-[120px] pointer-events-none"></div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center relative z-10">
          <!-- Left -->
          <div class="lg:col-span-8 space-y-4 hero-in order-2 lg:order-1">
            <!-- Top row: badge + pager -->
            <div class="flex flex-wrap items-center gap-3">
              <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-500/10 border border-blue-400/30 text-blue-300 text-[11px] font-bold uppercase tracking-wide">
                <Sparkles class="w-3.5 h-3.5 animate-spin-slow" />
                <span>Về chúng tôi</span>
              </div>
              <div class="inline-flex items-center gap-1 px-1.5 py-1 rounded-full bg-white/5 border border-white/10 text-[11px] text-slate-300">
                <button type="button" @click="prevHeroSlide" class="w-5 h-5 rounded-full hover:bg-white/10 flex items-center justify-center cursor-pointer" title="Trước"><ChevronLeft class="w-3.5 h-3.5" /></button>
                <span class="px-1 tabular-nums font-semibold">{{ heroSlide + 1 }} / {{ heroSlides.length }}</span>
                <button type="button" @click="nextHeroSlide" class="w-5 h-5 rounded-full hover:bg-white/10 flex items-center justify-center cursor-pointer" title="Sau"><ChevronRight class="w-3.5 h-3.5" /></button>
              </div>
            </div>

            <h1 class="text-3xl sm:text-4xl lg:text-[40px] font-black text-white tracking-tight leading-[1.15]">
              Nền Tảng Đặt Vé Giải Trí &amp;
              <span class="block text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-indigo-300 to-fuchsia-400 shimmer-text pb-1">
                Kết Nối Cảm Xúc Hàng Triệu Khán Giả
              </span>
            </h1>

            <!-- Meta row (changes with slide) -->
            <Transition name="fade" mode="out-in">
              <div :key="heroSlide" class="flex flex-wrap items-center gap-x-3 gap-y-2 text-xs">
                <span class="px-2 py-0.5 rounded bg-amber-400 text-black font-black text-[11px]">2026</span>
                <span class="flex items-center gap-1 text-slate-300"><MapPin class="w-3.5 h-3.5 text-slate-400" />{{ heroSlides[heroSlide].venue }}</span>
                <span class="px-2 py-0.5 rounded bg-indigo-500/15 border border-indigo-400/30 text-indigo-200 font-bold text-[11px] uppercase">{{ heroSlides[heroSlide].badge }}</span>
              </div>
            </Transition>

            <p class="text-sm text-slate-300/90 leading-relaxed max-w-2xl line-clamp-3">
              Ra đời với sứ mệnh mang đến trải nghiệm đặt vé số hóa chuẩn quốc tế, CineVerse &amp; EventHub là giải pháp toàn diện kết nối người yêu điện ảnh với các rạp chiếu phim hàng đầu và người hâm mộ với những đại nhạc hội bùng nổ khắp Việt Nam.
            </p>

            <div class="flex flex-wrap items-center gap-2.5 pt-1">
              <button
                type="button"
                @click="router.push('/cinema')"
                class="btn-glow group px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-sm cursor-pointer flex items-center gap-2"
              >
                <Ticket class="w-4 h-4" />
                <span>Khám Phá Phim Chiếu Rạp</span>
                <ArrowRight class="w-4 h-4 transition-transform group-hover:translate-x-1" />
              </button>
              <button
                type="button"
                @click="router.push('/events')"
                class="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/15 hover:border-white/30 text-slate-100 font-bold text-sm transition cursor-pointer"
              >
                Xem Sự Kiện &amp; Concert
              </button>
            </div>
          </div>

          <!-- Right: poster card -->
          <div class="lg:col-span-4 flex flex-col items-center lg:items-end hero-in order-1 lg:order-2" style="animation-delay: 0.15s">
            <div
              class="relative w-[220px] sm:w-[250px] aspect-[3/4] rounded-2xl overflow-hidden border border-white/15 bg-slate-900 group tilt-card poster-glow"
              :style="tiltStyle"
              @mousemove="onTilt"
              @mouseleave="resetTilt(); heroPaused = false"
              @mouseenter="heroPaused = true"
            >
              <img
                v-for="(s, i) in heroSlides"
                :key="s.image"
                :src="s.image"
                :alt="s.highlight"
                class="absolute inset-0 w-full h-full object-cover transition-all duration-[1200ms] ease-out"
                :class="i === heroSlide ? 'opacity-100 scale-100 ken-burns' : 'opacity-0 scale-110'"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-[#070B14]/90 via-transparent to-transparent pointer-events-none"></div>
              <div class="shine"></div>

              <span class="absolute top-2.5 right-2.5 px-2 py-0.5 rounded bg-blue-600 text-white text-[10px] font-black uppercase tracking-wide">Ticket Pass</span>

              <div class="absolute bottom-2.5 left-2.5 right-2.5 flex items-center justify-between rounded-lg bg-black/55 backdrop-blur-md border border-white/15 px-2.5 py-1.5 text-[11px]">
                <Transition name="fade" mode="out-in">
                  <span :key="heroSlide" class="font-bold text-white truncate">{{ heroSlides[heroSlide].tagline }}</span>
                </Transition>
                <span class="text-cyan-300 font-semibold shrink-0">100% Vé Thật</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick-select strip (like cinema page) -->
        <div class="relative z-10 mt-8 pt-5 border-t border-white/[0.08]">
          <div class="flex items-center justify-between text-[11px] mb-3">
            <span class="flex items-center gap-2 font-bold uppercase tracking-wide text-slate-300">
              <Sparkles class="w-3.5 h-3.5 text-amber-400" />Điểm nhấn trải nghiệm — bấm để xem
            </span>
            <span class="flex items-center gap-1.5 text-slate-500">
              <span class="w-1.5 h-1.5 rounded-full bg-fuchsia-500 animate-pulse"></span>Tự động chuyển tiếp sau {{ Math.round(SLIDE_MS / 1000) }}s
            </span>
          </div>
          <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <button
              v-for="(s, idx) in heroSlides"
              :key="s.image"
              type="button"
              @click="goHero(idx)"
              class="relative overflow-hidden flex items-center gap-3 p-2.5 rounded-xl border text-left cursor-pointer transition-all duration-300"
              :class="heroSlide === idx ? 'bg-white/[0.06] border-blue-400/60 shadow-lg shadow-blue-500/10' : 'bg-white/[0.02] border-white/10 hover:border-white/25 hover:bg-white/[0.04]'"
            >
              <span class="w-6 h-6 rounded-md flex items-center justify-center text-[11px] font-black shrink-0"
                :class="idx === 0 ? 'bg-amber-400 text-black' : idx === 1 ? 'bg-slate-300 text-black' : 'bg-orange-600 text-white'">#{{ idx + 1 }}</span>
              <img :src="s.image" :alt="s.badge" class="w-11 h-11 rounded-lg object-cover shrink-0" />
              <div class="min-w-0">
                <p class="text-xs font-bold text-white truncate">{{ s.badge }}</p>
                <p class="text-[10px] text-slate-400 truncate">{{ s.venue }}</p>
              </div>
              <span
                v-if="heroSlide === idx"
                :key="progressKey"
                class="absolute bottom-0 left-0 h-0.5 w-full bg-gradient-to-r from-blue-400 to-cyan-300 progress-fill"
                :class="{ 'progress-paused': heroPaused }"
                :style="{ animationDuration: SLIDE_MS + 'ms' }"
              ></span>
            </button>
          </div>
        </div>
      </section>

      <!-- ================= STATS ================= -->
      <section ref="statsEl" class="relative">
        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-5">
          <div
            v-for="(s, i) in stats"
            :key="s.label"
            v-reveal="i * 90"
            class="stat-card group relative overflow-hidden rounded-2xl bg-white/[0.03] backdrop-blur border border-white/[0.08] p-5 sm:p-6 hover:-translate-y-1.5 hover:border-blue-400/40 transition-all duration-300"
          >
            <div class="absolute -right-6 -top-6 w-24 h-24 rounded-full bg-blue-500/10 blur-2xl group-hover:bg-blue-500/25 transition"></div>
            <div class="relative flex items-center gap-4">
              <div class="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500/25 to-indigo-500/10 border border-blue-400/25 flex items-center justify-center text-blue-300 shrink-0 group-hover:rotate-6 group-hover:scale-110 transition-transform">
                <component :is="s.icon" class="w-5 h-5" />
              </div>
              <div>
                <div class="text-2xl sm:text-3xl font-black text-white tabular-nums tracking-tight">{{ fmt(i) }}</div>
                <div class="text-[11px] sm:text-xs text-slate-400">{{ s.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= STORY ================= -->
      <section class="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-14 items-center">
        <div class="lg:col-span-5 space-y-5" v-reveal>
          <div class="chip"><Compass class="w-3.5 h-3.5" /><span>Câu chuyện thương hiệu</span></div>
          <h2 class="text-3xl sm:text-4xl font-black text-white leading-tight tracking-tight">
            Từ đam mê điện ảnh
            <span class="block text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-cyan-300">đến hành trình kết nối cộng đồng</span>
          </h2>
          <p class="text-sm text-slate-300 leading-relaxed">
            CineVerse &amp; EventHub được thành lập bởi những người trẻ có chung niềm đam mê với điện ảnh, âm nhạc và những trải nghiệm sống động. Chúng tôi tin rằng, một tấm vé không chỉ là lối vào một bộ phim hay một sự kiện, mà còn là chìa khóa mở ra những cảm xúc, kết nối và kỷ niệm đáng nhớ.
          </p>
          <div class="pl-4 border-l-2 border-cyan-400/60">
            <span class="font-handwriting text-3xl sm:text-4xl text-cyan-400 font-bold tracking-wide drop-shadow-[0_0_12px_rgba(34,211,238,0.6)] leading-tight block">
              Vé trong tay <br />Cảm xúc trong tim
            </span>
          </div>
        </div>

        <!-- Bento gallery -->
        <div class="lg:col-span-7 grid grid-cols-6 grid-rows-2 gap-3 sm:gap-4 h-[380px] sm:h-[460px]">
          <div v-reveal="0" class="bento col-span-4 row-span-2">
            <img src="/images/about_cinema_hall.jpg" alt="Khán giả rạp chiếu phim" />
            <div class="bento-cap"><Film class="w-3.5 h-3.5" />Rạp chiếu phim</div>
          </div>
          <div v-reveal="120" class="bento col-span-2 row-span-1">
            <img src="/images/about_concert_crowd.jpg" alt="Đại nhạc hội bùng nổ" />
            <div class="bento-cap"><Sparkles class="w-3.5 h-3.5" />Concert</div>
          </div>
          <div v-reveal="240" class="bento col-span-2 row-span-1">
            <img src="/images/about_ticket_hand.jpg" alt="Vé điện tử cầm tay" />
            <div class="bento-cap"><Ticket class="w-3.5 h-3.5" />Vé điện tử</div>
          </div>
        </div>
      </section>

      <!-- ================= MISSION / VISION / VALUES ================= -->
      <section class="grid grid-cols-1 md:grid-cols-3 gap-5 sm:gap-6">
        <div v-reveal="0" class="mvv-card group" style="--c: 59 130 246">
          <div class="mvv-icon"><Target class="w-6 h-6" /></div>
          <h3 class="mvv-title group-hover:text-blue-300">Sứ mệnh</h3>
          <p class="text-sm text-slate-300 leading-relaxed">Mang đến trải nghiệm giải trí trọn vẹn, thuận tiện và an toàn cho mọi người, ở mọi nơi.</p>
          <div class="mvv-foot">
            <span class="text-blue-300 font-semibold">Cam kết chất lượng</span>
            <span class="mvv-pill bg-blue-500/15 text-blue-200 border-blue-500/30">100% Vé Thật</span>
          </div>
        </div>

        <div v-reveal="120" class="mvv-card group" style="--c: 99 102 241">
          <div class="mvv-icon"><Eye class="w-6 h-6" /></div>
          <h3 class="mvv-title group-hover:text-indigo-300">Tầm nhìn</h3>
          <p class="text-sm text-slate-300 leading-relaxed">Trở thành nền tảng đặt vé giải trí hàng đầu Việt Nam, kết nối hàng triệu người với những trải nghiệm tuyệt vời.</p>
          <div class="mvv-foot">
            <span class="text-indigo-300 font-semibold">Quy mô toàn quốc</span>
            <span class="mvv-pill bg-indigo-500/15 text-indigo-200 border-indigo-500/30">Top 1 Nền Tảng</span>
          </div>
        </div>

        <div v-reveal="240" class="mvv-card group" style="--c: 168 85 247">
          <div class="mvv-icon"><Gem class="w-6 h-6" /></div>
          <h3 class="mvv-title group-hover:text-purple-300">Giá trị cốt lõi</h3>
          <ul class="space-y-2.5 text-sm text-slate-300">
            <li v-for="v in ['Khách hàng là trung tâm', 'Đổi mới không ngừng', 'Minh bạch & đáng tin cậy', 'Cùng nhau tạo nên giá trị']" :key="v" class="flex items-center gap-2.5">
              <span class="w-5 h-5 rounded-full bg-purple-500/20 border border-purple-400/40 text-purple-300 flex items-center justify-center shrink-0"><CheckCircle2 class="w-3.5 h-3.5" /></span>
              <span>{{ v }}</span>
            </li>
          </ul>
          <div class="mvv-foot">
            <span class="text-purple-300 font-semibold">Tôn chỉ hành động</span>
            <span class="mvv-pill bg-purple-500/15 text-purple-200 border-purple-500/30">Chính Trực</span>
          </div>
        </div>
      </section>

      <!-- ================= TIMELINE ================= -->
      <section class="space-y-10">
        <div class="text-center space-y-3 max-w-2xl mx-auto" v-reveal>
          <div class="chip mx-auto"><Rocket class="w-3.5 h-3.5" /><span>Hành trình phát triển</span></div>
          <h2 class="text-3xl sm:text-4xl font-black text-white tracking-tight">Những cột mốc <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-fuchsia-400">đáng nhớ</span></h2>
        </div>

        <div class="relative grid grid-cols-1 md:grid-cols-4 gap-6">
          <div class="hidden md:block absolute top-[34px] left-[8%] right-[8%] h-px bg-gradient-to-r from-transparent via-blue-500/60 to-transparent"></div>
          <div v-for="(m, i) in milestones" :key="m.year" v-reveal="i * 120" class="relative group text-center md:text-center">
            <div class="mx-auto w-[68px] h-[68px] rounded-2xl bg-gradient-to-br from-[#12203F] to-[#0A1226] border border-blue-400/30 flex items-center justify-center text-blue-300 shadow-lg shadow-blue-500/20 group-hover:scale-110 group-hover:border-blue-300/70 group-hover:shadow-blue-500/50 transition-all duration-300 relative z-10">
              <component :is="m.icon" class="w-7 h-7" />
            </div>
            <div class="mt-4 text-2xl font-black text-white">{{ m.year }}</div>
            <h4 class="mt-1 text-sm font-bold text-blue-300">{{ m.title }}</h4>
            <p class="mt-2 text-xs text-slate-400 leading-relaxed max-w-[240px] mx-auto">{{ m.desc }}</p>
          </div>
        </div>
      </section>

      <!-- ================= WHY US ================= -->
      <section class="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-[#0C1324] via-[#09101F] to-[#060914] border border-white/[0.07] p-6 sm:p-10 lg:p-14 shadow-2xl">
        <div class="absolute -top-24 -right-24 w-80 h-80 bg-blue-600/15 rounded-full blur-[100px] pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-80 h-80 bg-purple-600/15 rounded-full blur-[100px] pointer-events-none"></div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-10 lg:gap-14 items-center relative z-10">
          <div class="lg:col-span-5 space-y-5" v-reveal>
            <div class="chip"><Sparkles class="w-3.5 h-3.5" /><span>Vì sao chọn CineVerse × EventHub?</span></div>
            <h2 class="text-3xl sm:text-4xl font-black text-white leading-tight tracking-tight">
              Trải nghiệm đặt vé dễ dàng,
              <span class="block text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-indigo-300 to-purple-400">trọn vẹn hơn mỗi khoảnh khắc</span>
            </h2>
            <p class="text-sm text-slate-300/90 leading-relaxed">
              Chúng tôi không chỉ cung cấp vé, mà còn mang đến một hành trình giải trí thông minh, tiện lợi và an toàn. Tận hưởng đặc quyền giữ ghế thời gian thực và thanh toán 1-chạm không lo lỡ suất.
            </p>
            <button type="button" @click="router.push('/events')" class="btn-glow group px-6 py-3 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-sm cursor-pointer flex items-center gap-2">
              <span>Tìm hiểu thêm</span>
              <ArrowRight class="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </button>
          </div>

          <div class="lg:col-span-7 grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div v-reveal="0" class="feat-card group" style="--c: 59 130 246">
              <div class="flex items-center justify-between"><div class="feat-icon text-blue-300"><Zap class="w-5 h-5" /></div><span class="feat-tag text-blue-300 bg-blue-500/10">Tốc Độ</span></div>
              <h4 class="feat-title group-hover:text-blue-300">Đặt vé nhanh chóng</h4>
              <p class="text-xs text-slate-400 leading-relaxed">Chỉ vài thao tác chọn suất và giữ chỗ ngay trong tích tắc.</p>
            </div>
            <div v-reveal="100" class="feat-card group" style="--c: 99 102 241">
              <div class="flex items-center justify-between"><div class="feat-icon text-indigo-300"><ShieldCheck class="w-5 h-5" /></div><span class="feat-tag text-indigo-300 bg-indigo-500/10">Bảo Mật</span></div>
              <h4 class="feat-title group-hover:text-indigo-300">Thanh toán an toàn</h4>
              <p class="text-xs text-slate-400 leading-relaxed">Hỗ trợ VietQR tự động, thẻ tín dụng và các ví điện tử hàng đầu.</p>
            </div>
            <div v-reveal="200" class="feat-card group" style="--c: 168 85 247">
              <div class="flex items-center justify-between"><div class="feat-icon text-purple-300"><Ticket class="w-5 h-5" /></div><span class="feat-tag text-purple-300 bg-purple-500/10">Tiện Lợi</span></div>
              <h4 class="feat-title group-hover:text-purple-300">Vé điện tử tiện lợi</h4>
              <p class="text-xs text-slate-400 leading-relaxed">Nhận vé qua email hoặc ứng dụng, QR Anti-Passback không lo thất lạc.</p>
            </div>
            <div v-reveal="300" class="feat-card group" style="--c: 16 185 129">
              <div class="flex items-center justify-between"><div class="feat-icon text-emerald-300"><Headphones class="w-5 h-5" /></div><span class="feat-tag text-emerald-300 bg-emerald-500/10">24/7</span></div>
              <h4 class="feat-title group-hover:text-emerald-300">Hỗ trợ 24/7</h4>
              <p class="text-xs text-slate-400 leading-relaxed">Đội ngũ chăm sóc khách hàng luôn sẵn sàng hỗ trợ bạn mọi lúc, mọi nơi.</p>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= PARTNERS ================= -->
      <section class="rounded-3xl bg-white/[0.025] backdrop-blur border border-white/[0.07] p-5 sm:p-7 lg:p-8 shadow-xl space-y-5">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
          <div class="lg:col-span-5 space-y-3" v-reveal>
            <div class="chip"><Building2 class="w-3.5 h-3.5" /><span>Đối tác của chúng tôi</span></div>
            <h2 class="text-2xl sm:text-[28px] font-black text-white leading-tight tracking-tight">Đồng hành cùng những thương hiệu hàng đầu</h2>
            <p class="text-xs sm:text-sm text-slate-300 leading-relaxed">CineVerse &amp; EventHub tự hào hợp tác với các rạp chiếu phim, đơn vị tổ chức sự kiện và nghệ sĩ hàng đầu tại Việt Nam.</p>
            <button type="button" @click="router.push('/cinema')" class="btn-glow group px-5 py-2.5 rounded-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-xs sm:text-sm cursor-pointer flex items-center gap-2">
              <span>Xem tất cả đối tác</span>
              <ArrowRight class="w-4 h-4 transition-transform group-hover:translate-x-1" />
            </button>
          </div>

          <!-- Featured event showcase -->
          <div class="lg:col-span-7" v-reveal="120">
            <div class="relative rounded-2xl overflow-hidden border border-white/10 h-[170px] sm:h-[200px] group">
              <TransitionGroup name="slide">
                <img
                  v-for="(item, idx) in partnerEvents"
                  v-show="idx === partnerSlide"
                  :key="item.id"
                  :src="item.image"
                  :alt="item.title"
                  class="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-700"
                />
              </TransitionGroup>
              <div class="absolute inset-0 bg-gradient-to-t from-black/85 via-black/20 to-transparent"></div>
              <div class="absolute bottom-3 left-4 right-3 flex items-end justify-between gap-4">
                <Transition name="fade" mode="out-in">
                  <div :key="partnerSlide">
                    <p class="text-[11px] uppercase tracking-widest text-cyan-300 font-bold">Sự kiện nổi bật</p>
                    <p class="text-base sm:text-lg font-black text-white">{{ partnerEvents[partnerSlide].artist }}</p>
                    <p class="text-xs text-slate-300">{{ partnerEvents[partnerSlide].title }}</p>
                  </div>
                </Transition>
                <div class="flex gap-2 shrink-0">
                  <button type="button" @click="prevPartner" class="ctrl-btn" title="Trước"><ChevronLeft class="w-4 h-4" /></button>
                  <button type="button" @click="nextPartner" class="ctrl-btn" title="Sau"><ChevronRight class="w-4 h-4" /></button>
                </div>
              </div>
            </div>
            <div class="grid grid-cols-4 gap-2 mt-2">
              <button
                v-for="(item, idx) in partnerEvents"
                :key="item.id"
                type="button"
                @click="partnerSlide = idx"
                class="relative h-10 rounded-lg overflow-hidden border cursor-pointer transition-all duration-300"
                :class="idx === partnerSlide ? 'border-blue-400 ring-2 ring-blue-500/40 scale-[1.03]' : 'border-white/10 opacity-60 hover:opacity-100'"
              >
                <img :src="item.image" :alt="item.title" class="w-full h-full object-cover" />
              </button>
            </div>
          </div>
        </div>

        <!-- Brand marquee -->
        <div class="marquee-mask overflow-hidden border-y border-white/[0.07] py-3">
          <div class="marquee-track">
            <template v-for="n in 2" :key="n">
              <span v-for="b in brands" :key="b + n" class="brand">{{ b }}</span>
            </template>
          </div>
        </div>
      </section>

      <!-- ================= CTA ================= -->
      <section v-reveal class="cta relative overflow-hidden rounded-3xl border border-white/10 bg-gradient-to-br from-[#0F1B3D] via-[#0A1226] to-[#0B0A1F] p-6 sm:p-8 lg:px-10 lg:py-9">
        <div class="cta-glow"></div>
        <div class="absolute inset-0 hero-noise opacity-60"></div>

        <div class="relative z-10 grid grid-cols-1 md:grid-cols-12 gap-8 items-center">
          <!-- Left -->
          <div class="md:col-span-7 space-y-4">
            <div class="chip"><Ticket class="w-3.5 h-3.5" /><span>Bắt đầu ngay hôm nay</span></div>
            <h2 class="text-2xl sm:text-3xl font-black text-white tracking-tight leading-tight">
              Sẵn sàng cho
              <span class="text-transparent bg-clip-text bg-gradient-to-r from-blue-400 via-indigo-300 to-fuchsia-400 shimmer-text">trải nghiệm tiếp theo?</span>
            </h2>
            <p class="text-sm text-slate-300/90 max-w-lg">Chọn bộ phim hoặc đại nhạc hội yêu thích và giữ chỗ chỉ trong vài giây.</p>

            <ul class="flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-300">
              <li v-for="t in ['Giữ ghế thời gian thực', 'Thanh toán VietQR', 'Vé điện tử QR']" :key="t" class="flex items-center gap-1.5">
                <CheckCircle2 class="w-4 h-4 text-emerald-400" />{{ t }}
              </li>
            </ul>

            <div class="flex flex-wrap gap-2.5 pt-1">
              <button type="button" @click="router.push('/cinema')" class="btn-glow group px-5 py-2.5 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-600 text-white font-bold text-sm cursor-pointer flex items-center gap-2">
                <Film class="w-4 h-4" />Đặt vé xem phim
                <ArrowRight class="w-4 h-4 transition-transform group-hover:translate-x-1" />
              </button>
              <button type="button" @click="router.push('/events')" class="px-5 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/15 hover:border-white/30 text-slate-100 font-bold text-sm transition cursor-pointer flex items-center gap-2">
                <Calendar class="w-4 h-4" />Khám phá sự kiện
              </button>
            </div>
          </div>

          <!-- Right: fanned photos + floating ticket -->
          <div class="md:col-span-5 hidden md:block">
            <div class="cta-fan relative h-[190px]">
              <img src="/images/event_blackpink.jpg" alt="" class="cta-photo cta-p1" />
              <img src="/images/event_jack.jpg" alt="" class="cta-photo cta-p2" />
              <img src="/images/about_hero_cinema.jpg" alt="" class="cta-photo cta-p3" />
              <div class="cta-ticket">
                <Ticket class="w-4 h-4 text-blue-300" />
                <div class="leading-tight">
                  <p class="text-[11px] font-bold text-white">Vé đã xác nhận</p>
                  <p class="text-[10px] text-slate-400">QR Anti-Passback</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ---------- scroll reveal ---------- */
.reveal {
  opacity: 0;
  transform: translateY(32px) scale(0.98);
  transition:
    opacity 0.8s cubic-bezier(0.22, 1, 0.36, 1),
    transform 0.8s cubic-bezier(0.22, 1, 0.36, 1);
}
.reveal-in {
  opacity: 1;
  transform: none;
}

/* ---------- ambient background ---------- */
.orb {
  position: absolute;
  border-radius: 9999px;
  filter: blur(110px);
  opacity: 0.35;
  animation: float 18s ease-in-out infinite;
}
.orb-a { width: 460px; height: 460px; background: #2563eb; top: 5%; left: -140px; }
.orb-b { width: 420px; height: 420px; background: #c026d3; top: 45%; right: -140px; animation-delay: -6s; opacity: 0.22; }
.orb-c { width: 380px; height: 380px; background: #06b6d4; bottom: 4%; left: 30%; animation-delay: -12s; opacity: 0.18; }
.grid-bg {
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.025) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.025) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(ellipse at center, #000 30%, transparent 75%);
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(60px, -40px) scale(1.1); }
  66% { transform: translate(-40px, 50px) scale(0.95); }
}

.hero-noise {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: radial-gradient(rgba(255, 255, 255, 0.07) 1px, transparent 1px);
  background-size: 22px 22px;
  mask-image: linear-gradient(120deg, #000, transparent 70%);
  pointer-events: none;
}

/* ---------- hero entrance ---------- */
.hero-in { animation: heroIn 1s cubic-bezier(0.22, 1, 0.36, 1) both; }
@keyframes heroIn {
  from { opacity: 0; transform: translateY(36px); }
  to { opacity: 1; transform: none; }
}
.shimmer-text { background-size: 200% auto; animation: shimmer 6s linear infinite; }
@keyframes shimmer { to { background-position: 200% center; } }
.logo-pulse { animation: logoPulse 3s ease-in-out infinite; }
@keyframes logoPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.5); }
  50% { box-shadow: 0 0 0 10px rgba(59, 130, 246, 0); }
}
.animate-spin-slow { animation: spin 6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ---------- hero card ---------- */
.poster-glow { box-shadow: 0 0 50px -8px rgba(99, 102, 241, 0.55), 0 25px 50px -15px rgba(0, 0, 0, 0.7); }
.tilt-card { transition: transform 0.25s ease-out; will-change: transform; }
.ken-burns { animation: kenBurns 7s ease-out forwards; }
@keyframes kenBurns { from { transform: scale(1); } to { transform: scale(1.07); } }
.shine {
  position: absolute;
  inset: 0;
  background: linear-gradient(110deg, transparent 35%, rgba(255, 255, 255, 0.12) 50%, transparent 65%);
  transform: translateX(-120%);
  transition: transform 1s ease;
  pointer-events: none;
}
.tilt-card:hover .shine { transform: translateX(120%); }

.progress-fill { width: 100%; transform-origin: left; animation: progress linear forwards; }
.progress-paused { animation-play-state: paused; }
@keyframes progress { from { transform: scaleX(0); } to { transform: scaleX(1); } }

.ctrl-btn {
  width: 34px;
  height: 34px;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
  background: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  cursor: pointer;
  transition: all 0.25s;
}
.ctrl-btn:hover { color: #fff; background: #2563eb; border-color: #60a5fa; transform: scale(1.1); }

/* ---------- transitions ---------- */
.tag-enter-active, .tag-leave-active { transition: all 0.45s ease; }
.tag-enter-from { opacity: 0; transform: translateX(30px); filter: blur(6px); }
.tag-leave-to { opacity: 0; transform: translateX(-30px); filter: blur(6px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.35s ease, transform 0.35s ease; }
.fade-enter-from { opacity: 0; transform: translateY(6px); }
.fade-leave-to { opacity: 0; transform: translateY(-6px); }
.slide-enter-active, .slide-leave-active { transition: opacity 0.7s ease, transform 0.9s cubic-bezier(0.22, 1, 0.36, 1); }
.slide-enter-from { opacity: 0; transform: scale(1.1); }
.slide-leave-to { opacity: 0; }

/* ---------- buttons / chips ---------- */
.btn-glow {
  position: relative;
  box-shadow: 0 10px 30px -8px rgba(59, 130, 246, 0.6);
  transition: transform 0.25s, box-shadow 0.25s;
}
.btn-glow:hover { transform: translateY(-2px) scale(1.02); box-shadow: 0 16px 40px -8px rgba(99, 102, 241, 0.85); }
.chip {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 0.85rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(96, 165, 250, 0.25);
  backdrop-filter: blur(6px);
}

/* ---------- bento ---------- */
.bento {
  position: relative;
  overflow: hidden;
  border-radius: 1.25rem;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 20px 40px -15px rgba(0, 0, 0, 0.6);
}
.bento img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.8s cubic-bezier(0.22, 1, 0.36, 1); }
.bento:hover img { transform: scale(1.08); }
.bento::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to top, rgba(5, 8, 17, 0.75), transparent 55%);
  pointer-events: none;
}
.bento-cap {
  position: absolute;
  z-index: 2;
  left: 0.75rem;
  bottom: 0.75rem;
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.3rem 0.65rem;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  border-radius: 9999px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

/* ---------- mission cards ---------- */
.mvv-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 1.1rem;
  padding: 2rem;
  border-radius: 1.75rem;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.05), rgba(255, 255, 255, 0.01));
  border: 1px solid rgba(255, 255, 255, 0.08);
  overflow: hidden;
  transition: transform 0.35s, border-color 0.35s, box-shadow 0.35s;
}
.mvv-card::before {
  content: '';
  position: absolute;
  inset: -1px;
  background: radial-gradient(400px circle at 50% -20%, rgb(var(--c) / 0.25), transparent 60%);
  opacity: 0;
  transition: opacity 0.4s;
  pointer-events: none;
}
.mvv-card:hover { transform: translateY(-8px); border-color: rgb(var(--c) / 0.5); box-shadow: 0 25px 50px -15px rgb(var(--c) / 0.35); }
.mvv-card:hover::before { opacity: 1; }
.mvv-icon {
  position: relative;
  width: 3.25rem;
  height: 3.25rem;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgb(var(--c));
  background: rgb(var(--c) / 0.15);
  border: 1px solid rgb(var(--c) / 0.35);
  transition: transform 0.4s;
}
.mvv-card:hover .mvv-icon { transform: scale(1.12) rotate(-6deg); }
.mvv-title { font-size: 1.3rem; font-weight: 800; color: #fff; transition: color 0.3s; }
.mvv-foot {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 0.75rem;
}
.mvv-pill { font-size: 10px; font-weight: 700; padding: 0.15rem 0.6rem; border-radius: 9999px; border-width: 1px; }

/* ---------- feature cards ---------- */
.feat-card {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.5rem;
  border-radius: 1.25rem;
  background: rgba(15, 23, 43, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.07);
  backdrop-filter: blur(8px);
  transition: transform 0.3s, border-color 0.3s, box-shadow 0.3s, background 0.3s;
}
.feat-card:hover {
  transform: translateY(-6px);
  background: rgba(20, 32, 59, 0.85);
  border-color: rgb(var(--c) / 0.55);
  box-shadow: 0 20px 40px -15px rgb(var(--c) / 0.4);
}
.feat-icon {
  width: 2.75rem;
  height: 2.75rem;
  border-radius: 0.85rem;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgb(var(--c) / 0.15);
  border: 1px solid rgb(var(--c) / 0.35);
  transition: transform 0.35s;
}
.feat-card:hover .feat-icon { transform: scale(1.15) rotate(8deg); }
.feat-tag { font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; padding: 0.15rem 0.5rem; border-radius: 0.4rem; }
.feat-title { font-size: 1rem; font-weight: 700; color: #fff; transition: color 0.3s; }

/* ---------- marquee ---------- */
.marquee-mask { mask-image: linear-gradient(90deg, transparent, #000 12%, #000 88%, transparent); }
.marquee-track { display: flex; width: max-content; gap: 3.5rem; animation: marquee 28s linear infinite; }
.marquee-mask:hover .marquee-track { animation-play-state: paused; }
.brand {
  font-weight: 900;
  letter-spacing: 0.12em;
  font-size: 0.85rem;
  color: rgba(226, 232, 240, 0.45);
  white-space: nowrap;
  transition: color 0.3s, text-shadow 0.3s;
}
.brand:hover { color: #fff; text-shadow: 0 0 18px rgba(96, 165, 250, 0.9); }
@keyframes marquee { to { transform: translateX(-50%); } }

/* ---------- CTA ---------- */
.cta-glow {
  position: absolute;
  width: 420px;
  height: 420px;
  right: -80px;
  top: 50%;
  margin-top: -210px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.35), transparent 65%);
  animation: pulseGlow 6s ease-in-out infinite;
  pointer-events: none;
}
.cta-photo {
  position: absolute;
  width: 150px;
  height: 190px;
  object-fit: cover;
  border-radius: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.18);
  box-shadow: 0 20px 40px -12px rgba(0, 0, 0, 0.7);
  transition: transform 0.5s cubic-bezier(0.22, 1, 0.36, 1);
}
.cta-p1 { left: 8%; transform: rotate(-9deg); z-index: 1; }
.cta-p2 { left: 32%; transform: rotate(0deg) translateY(-6px); z-index: 2; }
.cta-p3 { left: 56%; transform: rotate(9deg); z-index: 1; }
.cta:hover .cta-p1 { transform: rotate(-14deg) translateX(-10px); }
.cta:hover .cta-p2 { transform: rotate(0deg) translateY(-14px); }
.cta:hover .cta-p3 { transform: rotate(14deg) translateX(10px); }
.cta-ticket {
  position: absolute;
  z-index: 3;
  left: 22%;
  bottom: -6px;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.5rem 0.85rem;
  border-radius: 0.85rem;
  background: rgba(7, 11, 20, 0.8);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 12px 30px -8px rgba(0, 0, 0, 0.7);
  animation: bob 4s ease-in-out infinite;
}
@keyframes bob { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } }
@keyframes pulseGlow {
  0%, 100% { transform: scale(0.85); opacity: 0.6; }
  50% { transform: scale(1.15); opacity: 1; }
}

@media (prefers-reduced-motion: reduce) {
  .orb, .marquee-track, .cta-glow, .cta-ticket, .shimmer-text, .logo-pulse, .ken-burns, .hero-in { animation: none !important; }
  .reveal { opacity: 1; transform: none; transition: none; }
}
</style>
