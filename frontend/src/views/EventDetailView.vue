<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { Event, EventSeat } from '@/types'
import { eventService } from '@/services/event.service'
import type { EventDetailExtras } from '@/services/eventDetailData'
import { useBookingStore } from '@/stores/booking'
import SeatMap from '@/components/SeatMap.vue'
import EventCard from '@/components/EventCard.vue'
import TicketTierList from '@/components/event-detail/TicketTierList.vue'
import EventAgenda from '@/components/event-detail/EventAgenda.vue'
import EventFaq from '@/components/event-detail/EventFaq.vue'
import EventReviews from '@/components/event-detail/EventReviews.vue'
import EventVenue from '@/components/event-detail/EventVenue.vue'
import BookingSidebar from '@/components/event-detail/BookingSidebar.vue'
import {
  Calendar,
  MapPin,
  Building,
  ArrowRight,
  Sparkles,
  Clock,
  Heart,
  Share2,
  CalendarPlus,
  ChevronRight,
  Home,
  Users,
  BadgeCheck,
  Music,
  Star,
  Gift,
  Mic,
  Camera,
  Trophy,
  RotateCcw,
  ShieldAlert,
  QrCode,
  ArrowLeftRight,
  Briefcase,
  X,
  ChevronLeft,
  CheckCircle2,
  AlertCircle,
  Info,
  Armchair,
  Ticket as TicketIcon,
  Link2,
  Facebook,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const bookingStore = useBookingStore()

const event = ref<Event | null>(null)
const extras = ref<EventDetailExtras | null>(null)
const related = ref<Event[]>([])
const seats = ref<EventSeat[]>([])
const loading = ref(true)
const ticketMode = ref<'seatmap' | 'tiers'>('tiers')
const descExpanded = ref(false)
const isFavorite = ref(false)
const shareOpen = ref(false)
const lightboxIndex = ref<number | null>(null)

/* ---------------- Toasts ---------------- */
type ToastType = 'error' | 'success' | 'info'
const toasts = ref<{ id: number; message: string; type: ToastType }[]>([])
const notify = (message: string, type: ToastType = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => (toasts.value = toasts.value.filter((t) => t.id !== id)), 3000)
}

/* ---------------- Data loading ---------------- */
const FAV_KEY = 'eventhub:favorites'
const readFavs = (): number[] => {
  try {
    return JSON.parse(localStorage.getItem(FAV_KEY) || '[]')
  } catch {
    return []
  }
}

const loadEvent = async (id: number) => {
  loading.value = true
  event.value = null
  try {
    const fetched = await eventService.getEventById(id)
    event.value = fetched
    bookingStore.setEvent(fetched)
    document.title = `${fetched.title} | EventHub`

    const [extraData, relatedEvents, seatData] = await Promise.all([
      eventService.getEventExtras(fetched),
      eventService.getRelatedEvents(fetched),
      fetched.hasSeatMap ? eventService.getSeatMap(fetched.id) : Promise.resolve([]),
    ])
    extras.value = extraData
    related.value = relatedEvents
    seats.value = seatData
    ticketMode.value = fetched.hasSeatMap ? 'seatmap' : 'tiers'
    isFavorite.value = readFavs().includes(fetched.id)
  } finally {
    loading.value = false
    await nextTick()
    setupScrollSpy()
  }
}

watch(
  () => route.params.id,
  (id) => {
    if (id) {
      window.scrollTo({ top: 0 })
      loadEvent(Number(id))
    }
  },
  { immediate: true }
)

/* ---------------- Formatting ---------------- */
const formatCurrency = (val: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

const dateParts = computed(() => {
  if (!event.value) return null
  const s = new Date(event.value.startDateTime)
  const e = new Date(event.value.endDateTime)
  const sameDay = s.toDateString() === e.toDateString()
  const time = (d: Date) => new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit' }).format(d)
  const day = (d: Date) =>
    new Intl.DateTimeFormat('vi-VN', { weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric' }).format(d)
  const durationH = Math.round(((e.getTime() - s.getTime()) / 3600000) * 10) / 10
  return {
    weekday: new Intl.DateTimeFormat('vi-VN', { weekday: 'short' }).format(s),
    dayNum: s.getDate().toString().padStart(2, '0'),
    month: `Th${s.getMonth() + 1}`,
    full: day(s),
    timeRange: sameDay ? `${time(s)} – ${time(e)}` : `${time(s)} ${day(s)} → ${time(e)} ${day(e)}`,
    duration: durationH >= 24 ? `${Math.ceil(durationH / 24)} ngày` : `${durationH} giờ`,
  }
})

const totalRemaining = computed(() =>
  (event.value?.ticketTypes || []).reduce((acc, t) => acc + t.remainingQuantity, 0)
)

/** Minimal markdown: **bold** and "- " bullet lists. Content is escaped first. */
const descriptionHtml = computed(() => {
  if (!event.value) return ''
  const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  const lines = esc(event.value.description).split('\n').map((l) => l.trim())
  let html = ''
  let inList = false
  for (const line of lines) {
    const formatted = line.replace(/\*\*(.+?)\*\*/g, '<strong class="text-slate-900">$1</strong>')
    if (line.startsWith('- ')) {
      if (!inList) {
        html += '<ul class="space-y-1.5 my-3">'
        inList = true
      }
      html += `<li class="flex gap-2"><span class="mt-2 w-1.5 h-1.5 rounded-full bg-indigo-500 shrink-0"></span><span>${formatted.slice(2)}</span></li>`
    } else {
      if (inList) {
        html += '</ul>'
        inList = false
      }
      if (line) html += `<p class="mb-3">${formatted}</p>`
    }
  }
  if (inList) html += '</ul>'
  return html
})

const highlightIcons = { music: Music, star: Star, users: Users, gift: Gift, mic: Mic, camera: Camera, trophy: Trophy, sparkles: Sparkles }
const policyIcons = { refund: RotateCcw, age: ShieldAlert, checkin: QrCode, camera: Camera, bag: Briefcase, transfer: ArrowLeftRight }

/* ---------------- Section nav + scroll spy ---------------- */
const sections = computed(() => {
  const list = [
    { id: 'about', label: 'Giới thiệu' },
    { id: 'tickets', label: event.value?.hasSeatMap ? 'Chọn ghế & vé' : 'Chọn vé' },
    { id: 'agenda', label: 'Lịch trình' },
    { id: 'lineup', label: extras.value?.performerLabel || 'Nghệ sĩ' },
    { id: 'venue', label: 'Địa điểm' },
    { id: 'policies', label: 'Quy định' },
    { id: 'faq', label: 'Hỏi đáp' },
    { id: 'reviews', label: 'Đánh giá' },
  ]
  return list
})
const activeSection = ref('about')
let observer: IntersectionObserver | null = null

const setupScrollSpy = () => {
  observer?.disconnect()
  observer = new IntersectionObserver(
    (entries) => {
      const visible = entries.filter((e) => e.isIntersecting).sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
      if (visible[0]) activeSection.value = visible[0].target.id
    },
    { rootMargin: '-90px 0px -60% 0px', threshold: 0 }
  )
  sections.value.forEach((s) => {
    const el = document.getElementById(s.id)
    if (el) observer!.observe(el)
  })
}

const scrollTo = (id: string) => {
  const el = document.getElementById(id)
  if (!el) return
  const y = el.getBoundingClientRect().top + window.scrollY - 80
  window.scrollTo({ top: y, behavior: 'smooth' })
}

onBeforeUnmount(() => {
  observer?.disconnect()
  window.removeEventListener('keydown', onKey)
})

/* ---------------- Actions ---------------- */
const toggleFavorite = () => {
  if (!event.value) return
  const favs = readFavs()
  const id = event.value.id
  const next = favs.includes(id) ? favs.filter((f) => f !== id) : [...favs, id]
  localStorage.setItem(FAV_KEY, JSON.stringify(next))
  isFavorite.value = next.includes(id)
  notify(isFavorite.value ? 'Đã thêm vào danh sách yêu thích' : 'Đã bỏ khỏi danh sách yêu thích', 'success')
}

const shareUrl = computed(() => window.location.href)
const share = async () => {
  if (!event.value) return
  if (navigator.share) {
    try {
      await navigator.share({ title: event.value.title, text: event.value.shortDescription, url: shareUrl.value })
      return
    } catch {
      /* user cancelled -> fall back to menu */
    }
  }
  shareOpen.value = !shareOpen.value
}
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value)
    notify('Đã sao chép liên kết sự kiện', 'success')
  } catch {
    notify('Không thể sao chép liên kết', 'error')
  }
  shareOpen.value = false
}

const addToCalendar = () => {
  if (!event.value) return
  const fmt = (iso: string) => new Date(iso).toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z'
  const ics = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//EventHub//VN',
    'BEGIN:VEVENT',
    `UID:eventhub-${event.value.id}@eventhub.vn`,
    `DTSTAMP:${fmt(new Date().toISOString())}`,
    `DTSTART:${fmt(event.value.startDateTime)}`,
    `DTEND:${fmt(event.value.endDateTime)}`,
    `SUMMARY:${event.value.title}`,
    `LOCATION:${event.value.locationName} - ${event.value.address}`,
    `DESCRIPTION:${event.value.shortDescription}`,
    'END:VEVENT',
    'END:VCALENDAR',
  ].join('\r\n')
  const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `${event.value.slug || 'event'}.ics`
  a.click()
  URL.revokeObjectURL(a.href)
  notify('Đã tải file lịch (.ics)', 'success')
}

const proceedToBooking = () => {
  if (bookingStore.totalTicketCount === 0) {
    notify('Vui lòng chọn ít nhất 1 vé hoặc ghế trước khi tiếp tục!', 'error')
    scrollTo('tickets')
    return
  }
  router.push(`/booking/${event.value?.id}`)
}

/* ---------------- Lightbox ---------------- */
const openLightbox = (i: number) => {
  lightboxIndex.value = i
  window.addEventListener('keydown', onKey)
}
const closeLightbox = () => {
  lightboxIndex.value = null
  window.removeEventListener('keydown', onKey)
}
const stepLightbox = (d: number) => {
  const len = extras.value?.gallery.length || 0
  if (lightboxIndex.value === null || !len) return
  lightboxIndex.value = (lightboxIndex.value + d + len) % len
}
function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape') closeLightbox()
  if (e.key === 'ArrowRight') stepLightbox(1)
  if (e.key === 'ArrowLeft') stepLightbox(-1)
}
</script>

<template>
  <!-- Loading skeleton -->
  <div v-if="loading" class="animate-pulse">
    <div class="h-[380px] bg-slate-200"></div>
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-4">
        <div class="h-10 w-2/3 bg-slate-200 rounded-xl"></div>
        <div class="h-40 bg-slate-200 rounded-3xl"></div>
        <div class="h-72 bg-slate-200 rounded-3xl"></div>
      </div>
      <div class="h-96 bg-slate-200 rounded-3xl"></div>
    </div>
  </div>

  <!-- Not found -->
  <div v-else-if="!event" class="max-w-xl mx-auto px-4 py-24 text-center">
    <AlertCircle class="w-12 h-12 text-rose-500 mx-auto mb-3" />
    <h1 class="text-2xl font-black text-slate-900 mb-2">Không tìm thấy sự kiện</h1>
    <p class="text-slate-500 text-sm mb-6">Sự kiện có thể đã kết thúc hoặc không tồn tại.</p>
    <router-link to="/events" class="px-5 py-2.5 rounded-xl btn-primary text-sm inline-flex items-center gap-2">
      Khám phá sự kiện khác <ArrowRight class="w-4 h-4" />
    </router-link>
  </div>

  <div v-else class="pb-28 lg:pb-16 bg-slate-50">
    <!-- ================= HERO ================= -->
    <section class="relative overflow-hidden bg-slate-950">
      <img :src="event.bannerUrl" alt="" aria-hidden="true" class="absolute inset-0 w-full h-full object-cover opacity-40 blur-2xl scale-110" />
      <div class="absolute inset-0 bg-gradient-to-b from-slate-950/40 via-slate-950/70 to-slate-950"></div>
      <div class="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-indigo-600/30 blur-3xl"></div>
      <div class="absolute -bottom-24 right-0 w-96 h-96 rounded-full bg-fuchsia-600/20 blur-3xl"></div>

      <div class="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-6 pb-10 lg:pb-14">
        <!-- Breadcrumb -->
        <nav aria-label="Breadcrumb" class="flex items-center gap-1.5 text-xs text-slate-400 mb-6 overflow-hidden">
          <router-link to="/" class="hover:text-white flex items-center gap-1 shrink-0"><Home class="w-3.5 h-3.5" /> Trang chủ</router-link>
          <ChevronRight class="w-3 h-3 shrink-0" />
          <router-link :to="{ path: '/events', query: { categoryId: event.category.id } }" class="hover:text-white shrink-0">{{ event.category.name }}</router-link>
          <ChevronRight class="w-3 h-3 shrink-0" />
          <span class="text-slate-200 truncate">{{ event.title }}</span>
        </nav>

        <div class="grid grid-cols-1 lg:grid-cols-[1.15fr_1fr] gap-8 items-center">
          <!-- Poster -->
          <div class="relative group order-1 lg:order-none">
            <div class="absolute -inset-1 rounded-[28px] bg-gradient-to-r from-indigo-500 via-violet-500 to-fuchsia-500 opacity-60 blur group-hover:opacity-90 transition"></div>
            <div class="relative aspect-[16/9] rounded-3xl overflow-hidden ring-1 ring-white/10">
              <img :src="event.bannerUrl" :alt="event.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-700" />
              <div class="absolute top-4 left-4 flex flex-col items-center rounded-2xl bg-white/95 backdrop-blur px-3 py-2 shadow-lg">
                <span class="text-[10px] font-black uppercase text-rose-600">{{ dateParts?.month }}</span>
                <span class="text-2xl font-black text-slate-900 leading-none">{{ dateParts?.dayNum }}</span>
                <span class="text-[10px] font-bold text-slate-500 uppercase">{{ dateParts?.weekday }}</span>
              </div>
            </div>
          </div>

          <!-- Info -->
          <div class="text-white">
            <div class="flex flex-wrap items-center gap-2 mb-4">
              <span class="px-3 py-1 rounded-full text-xs font-black bg-indigo-500/90 shadow">{{ event.category.name }}</span>
              <span v-if="event.hasSeatMap" class="px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/90 flex items-center gap-1">
                <Armchair class="w-3 h-3" /> Chọn ghế trực tiếp
              </span>
              <span v-if="event.featured" class="px-3 py-1 rounded-full text-xs font-bold bg-amber-400 text-slate-900 flex items-center gap-1">
                <Sparkles class="w-3 h-3" /> Nổi bật
              </span>
            </div>

            <h1 class="text-2xl sm:text-3xl lg:text-[2.6rem] font-black leading-tight mb-3">{{ event.title }}</h1>
            <p class="text-sm text-slate-300 leading-relaxed mb-6 max-w-xl">{{ event.shortDescription }}</p>

            <div class="space-y-3 mb-6">
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-xl bg-white/10 flex items-center justify-center shrink-0"><Calendar class="w-4 h-4 text-indigo-300" /></div>
                <div>
                  <div class="text-sm font-bold capitalize">{{ dateParts?.full }}</div>
                  <div class="text-xs text-slate-400 flex items-center gap-1"><Clock class="w-3 h-3" /> {{ dateParts?.timeRange }} • {{ dateParts?.duration }}</div>
                </div>
              </div>
              <button type="button" @click="scrollTo('venue')" class="flex items-start gap-3 text-left group/loc">
                <div class="w-9 h-9 rounded-xl bg-white/10 flex items-center justify-center shrink-0"><MapPin class="w-4 h-4 text-rose-400" /></div>
                <div>
                  <div class="text-sm font-bold group-hover/loc:text-indigo-300 transition">{{ event.locationName }}</div>
                  <div class="text-xs text-slate-400">{{ event.address }}</div>
                </div>
              </button>
              <div class="flex items-start gap-3">
                <div class="w-9 h-9 rounded-xl bg-white/10 flex items-center justify-center shrink-0"><Building class="w-4 h-4 text-slate-300" /></div>
                <div class="text-sm font-bold flex items-center gap-1">
                  {{ event.organizerName }} <BadgeCheck class="w-4 h-4 text-sky-400" />
                </div>
              </div>
            </div>

            <!-- Price + CTA -->
            <div class="flex flex-wrap items-center gap-4 p-4 rounded-2xl bg-white/5 border border-white/10 backdrop-blur mb-4">
              <div class="flex-1 min-w-[140px]">
                <div class="text-[11px] uppercase font-bold tracking-wider text-slate-400">Giá vé</div>
                <div class="text-xl font-black">
                  {{ formatCurrency(event.minPrice) }}
                  <span v-if="event.maxPrice > event.minPrice" class="text-slate-400 font-bold text-base"> – {{ formatCurrency(event.maxPrice) }}</span>
                </div>
              </div>
              <button
                id="hero-buy-btn"
                type="button"
                @click="scrollTo('tickets')"
                class="px-6 py-3 rounded-xl bg-gradient-to-r from-indigo-500 to-fuchsia-500 font-black text-sm flex items-center gap-2 shadow-lg shadow-fuchsia-500/30 hover:scale-105 active:scale-95 transition"
              >
                <TicketIcon class="w-4 h-4" /> Mua vé ngay
              </button>
            </div>

            <!-- Actions -->
            <div class="flex flex-wrap items-center gap-2">
              <button
                id="favorite-btn"
                type="button"
                @click="toggleFavorite"
                class="px-3.5 py-2 rounded-xl text-xs font-bold border transition flex items-center gap-1.5"
                :class="isFavorite ? 'bg-rose-500 border-rose-500 text-white' : 'bg-white/5 border-white/15 text-white hover:bg-white/10'"
              >
                <Heart class="w-4 h-4" :class="isFavorite ? 'fill-white' : ''" />
                {{ isFavorite ? 'Đã yêu thích' : 'Yêu thích' }}
              </button>
              <div class="relative">
                <button id="share-btn" type="button" @click="share" class="px-3.5 py-2 rounded-xl text-xs font-bold bg-white/5 border border-white/15 hover:bg-white/10 flex items-center gap-1.5">
                  <Share2 class="w-4 h-4" /> Chia sẻ
                </button>
                <div v-if="shareOpen" class="absolute z-30 top-full mt-2 left-0 w-52 rounded-2xl bg-white text-slate-800 shadow-2xl border border-slate-200 p-1.5">
                  <button type="button" @click="copyLink" class="w-full px-3 py-2 rounded-xl hover:bg-slate-100 text-xs font-bold flex items-center gap-2"><Link2 class="w-4 h-4 text-indigo-600" /> Sao chép liên kết</button>
                  <a :href="`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(shareUrl)}`" target="_blank" rel="noopener" class="w-full px-3 py-2 rounded-xl hover:bg-slate-100 text-xs font-bold flex items-center gap-2"><Facebook class="w-4 h-4 text-blue-600" /> Chia sẻ Facebook</a>
                </div>
              </div>
              <button id="calendar-btn" type="button" @click="addToCalendar" class="px-3.5 py-2 rounded-xl text-xs font-bold bg-white/5 border border-white/15 hover:bg-white/10 flex items-center gap-1.5">
                <CalendarPlus class="w-4 h-4" /> Thêm vào lịch
              </button>
              <span v-if="extras" class="text-xs text-slate-400 flex items-center gap-1.5 ml-1">
                <Users class="w-3.5 h-3.5" /> {{ extras.interestedCount.toLocaleString('vi-VN') }} người quan tâm
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ================= SECTION NAV ================= -->
    <div class="sticky top-0 z-30 bg-white/90 backdrop-blur-md border-b border-slate-200 shadow-sm">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <nav aria-label="Mục lục sự kiện" class="flex gap-1 overflow-x-auto no-scrollbar py-2">
          <button
            v-for="s in sections"
            :key="s.id"
            :id="`nav-${s.id}`"
            type="button"
            @click="scrollTo(s.id)"
            class="relative px-4 py-2 rounded-xl text-sm font-bold whitespace-nowrap transition"
            :class="activeSection === s.id ? 'text-indigo-700 bg-indigo-50' : 'text-slate-500 hover:text-slate-900 hover:bg-slate-100'"
          >
            {{ s.label }}
          </button>
        </nav>
      </div>
    </div>

    <!-- ================= MAIN ================= -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8 min-w-0">
        <!-- About -->
        <section id="about" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Giới thiệu sự kiện</h2>

          <!-- Quick facts -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
            <div class="fact-card">
              <Clock class="w-4 h-4 text-indigo-600" />
              <span class="fact-label">Thời lượng</span>
              <span class="fact-value">{{ dateParts?.duration }}</span>
            </div>
            <div class="fact-card">
              <TicketIcon class="w-4 h-4 text-rose-600" />
              <span class="fact-label">Hạng vé</span>
              <span class="fact-value">{{ event.ticketTypes?.length || 0 }} loại</span>
            </div>
            <div class="fact-card">
              <Users class="w-4 h-4 text-emerald-600" />
              <span class="fact-label">Vé còn lại</span>
              <span class="fact-value">{{ totalRemaining.toLocaleString('vi-VN') }}</span>
            </div>
            <div class="fact-card">
              <ShieldAlert class="w-4 h-4 text-amber-600" />
              <span class="fact-label">Độ tuổi</span>
              <span class="fact-value">8+</span>
            </div>
          </div>

          <div class="relative">
            <div
              class="text-[15px] text-slate-600 leading-relaxed overflow-hidden transition-all"
              :class="descExpanded ? '' : 'max-h-40'"
              v-html="descriptionHtml"
            ></div>
            <div v-if="!descExpanded" class="absolute bottom-0 inset-x-0 h-16 bg-gradient-to-t from-white to-transparent pointer-events-none"></div>
          </div>
          <button id="desc-toggle" type="button" @click="descExpanded = !descExpanded" class="mt-2 text-sm font-bold text-indigo-600 hover:text-indigo-800">
            {{ descExpanded ? 'Thu gọn' : 'Xem thêm' }}
          </button>

          <!-- Highlights -->
          <div v-if="extras" class="mt-8">
            <h3 class="text-sm font-black text-slate-900 uppercase tracking-wider mb-4">Điểm nổi bật</h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div v-for="h in extras.highlights" :key="h.title" class="flex gap-3 p-4 rounded-2xl bg-gradient-to-br from-slate-50 to-indigo-50/40 border border-slate-100 hover:border-indigo-200 hover:shadow-md transition">
                <div class="w-10 h-10 rounded-xl bg-white shadow-sm flex items-center justify-center shrink-0">
                  <component :is="highlightIcons[h.icon]" class="w-5 h-5 text-indigo-600" />
                </div>
                <div>
                  <div class="text-sm font-black text-slate-900">{{ h.title }}</div>
                  <div class="text-xs text-slate-500 leading-relaxed">{{ h.description }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Gallery -->
          <div v-if="extras?.gallery.length" class="mt-8">
            <h3 class="text-sm font-black text-slate-900 uppercase tracking-wider mb-4">Hình ảnh sự kiện</h3>
            <div class="grid grid-cols-4 grid-rows-2 gap-2 h-56 sm:h-72">
              <button
                v-for="(img, i) in extras.gallery"
                :key="img"
                type="button"
                @click="openLightbox(i)"
                class="relative overflow-hidden rounded-2xl group"
                :class="i === 0 ? 'col-span-2 row-span-2' : i === 1 ? 'col-span-2' : ''"
              >
                <img :src="img" :alt="`Hình ảnh ${i + 1}`" loading="lazy" class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
                <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition"></div>
              </button>
            </div>
          </div>

          <!-- Tags -->
          <div v-if="extras?.tags.length" class="mt-6 flex flex-wrap gap-2">
            <span v-for="t in extras.tags" :key="t" class="px-3 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-600">#{{ t }}</span>
          </div>
        </section>

        <!-- Tickets -->
        <section id="tickets" class="scroll-mt-24">
          <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-3 mb-4">
            <div>
              <h2 class="text-xl sm:text-2xl font-black text-slate-900">{{ event.hasSeatMap ? 'Chọn ghế & hạng vé' : 'Chọn hạng vé' }}</h2>
              <p class="text-xs text-slate-500 mt-0.5">Giá đã bao gồm VAT. Phí dịch vụ hiển thị ở bước thanh toán.</p>
            </div>
            <div v-if="event.hasSeatMap" class="inline-flex p-1 rounded-xl bg-white border border-slate-200 shadow-sm">
              <button
                id="mode-seatmap"
                type="button"
                @click="ticketMode = 'seatmap'"
                class="px-4 py-2 rounded-lg text-xs font-bold flex items-center gap-1.5 transition"
                :class="ticketMode === 'seatmap' ? 'bg-indigo-600 text-white shadow' : 'text-slate-600 hover:bg-slate-100'"
              >
                <Armchair class="w-4 h-4" /> Sơ đồ ghế
              </button>
              <button
                id="mode-tiers"
                type="button"
                @click="ticketMode = 'tiers'"
                class="px-4 py-2 rounded-lg text-xs font-bold flex items-center gap-1.5 transition"
                :class="ticketMode === 'tiers' ? 'bg-indigo-600 text-white shadow' : 'text-slate-600 hover:bg-slate-100'"
              >
                <TicketIcon class="w-4 h-4" /> Hạng vé
              </button>
            </div>
          </div>

          <transition mode="out-in" enter-active-class="transition duration-200" enter-from-class="opacity-0 translate-y-1" leave-active-class="transition duration-150" leave-to-class="opacity-0">
            <SeatMap v-if="ticketMode === 'seatmap' && event.hasSeatMap" key="map" :seats="seats" @notify="notify" />
            <TicketTierList v-else key="tiers" :ticket-types="event.ticketTypes || []" @notify="notify" />
          </transition>
        </section>

        <!-- Agenda -->
        <section v-if="extras" id="agenda" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Lịch trình chương trình</h2>
          <EventAgenda :agenda="extras.agenda" />
        </section>

        <!-- Lineup -->
        <section v-if="extras" id="lineup" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">{{ extras.performerLabel }}</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <article v-for="p in extras.performers" :key="p.id" class="flex gap-4 p-4 rounded-2xl border border-slate-100 bg-slate-50/60 hover:bg-white hover:border-indigo-200 hover:shadow-md transition group">
              <div class="relative shrink-0">
                <img :src="p.avatar" :alt="p.name" loading="lazy" class="w-16 h-16 rounded-2xl object-cover ring-2 ring-white shadow group-hover:scale-105 transition" />
                <span v-if="p.headliner" class="absolute -bottom-1.5 -right-1.5 w-6 h-6 rounded-full bg-amber-400 flex items-center justify-center ring-2 ring-white">
                  <Star class="w-3 h-3 fill-slate-900 text-slate-900" />
                </span>
              </div>
              <div class="min-w-0">
                <div class="flex items-center gap-2">
                  <h3 class="font-black text-slate-900 text-sm">{{ p.name }}</h3>
                  <span v-if="p.headliner" class="text-[9px] font-black uppercase px-1.5 py-0.5 rounded bg-amber-100 text-amber-700">Headliner</span>
                </div>
                <div class="text-xs font-semibold text-indigo-600 mb-1">{{ p.role }}</div>
                <p class="text-xs text-slate-500 leading-relaxed line-clamp-2">{{ p.bio }}</p>
              </div>
            </article>
          </div>
        </section>

        <!-- Venue -->
        <section v-if="extras" id="venue" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Địa điểm tổ chức</h2>
          <EventVenue :event="event" :venue-info="extras.venueInfo" @notify="notify" />
        </section>

        <!-- Policies -->
        <section v-if="extras" id="policies" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Quy định & Chính sách</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div v-for="p in extras.policies" :key="p.title" class="p-4 rounded-2xl border border-slate-100 bg-slate-50/60">
              <div class="flex items-center gap-2 mb-1.5">
                <component :is="policyIcons[p.icon]" class="w-4 h-4 text-indigo-600" />
                <h3 class="font-black text-sm text-slate-900">{{ p.title }}</h3>
              </div>
              <p class="text-xs text-slate-600 leading-relaxed">{{ p.content }}</p>
            </div>
          </div>
        </section>

        <!-- FAQ -->
        <section v-if="extras" id="faq" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Câu hỏi thường gặp</h2>
          <EventFaq :faqs="extras.faqs" />
        </section>

        <!-- Reviews -->
        <section v-if="extras" id="reviews" class="scroll-mt-24 bg-white rounded-3xl border border-slate-200 shadow-sm p-6 sm:p-8">
          <h2 class="section-title">Đánh giá từ khán giả</h2>
          <EventReviews :key="event.id" :reviews="extras.reviews" @notify="notify" />
        </section>
      </div>

      <!-- ================= SIDEBAR ================= -->
      <aside class="hidden lg:block">
        <div class="sticky top-20 space-y-4">
          <BookingSidebar :event="event" @checkout="proceedToBooking" />

          <!-- Organizer -->
          <div v-if="extras" class="rounded-3xl bg-white border border-slate-200 shadow-sm p-5">
            <div class="text-[11px] font-bold uppercase tracking-wider text-slate-400 mb-3">Ban tổ chức</div>
            <div class="flex items-center gap-3 mb-3">
              <img :src="extras.organizer.logo" :alt="extras.organizer.name" class="w-12 h-12 rounded-2xl" />
              <div class="min-w-0">
                <div class="font-black text-sm text-slate-900 flex items-center gap-1 truncate">
                  {{ extras.organizer.name }}
                  <BadgeCheck v-if="extras.organizer.verified" class="w-4 h-4 text-sky-500 shrink-0" />
                </div>
                <div class="text-[11px] text-slate-500">
                  {{ extras.organizer.eventsHosted }} sự kiện • {{ (extras.organizer.followers / 1000).toFixed(1) }}K theo dõi
                </div>
              </div>
            </div>
            <p class="text-xs text-slate-500 leading-relaxed">{{ extras.organizer.description }}</p>
          </div>
        </div>
      </aside>
    </div>

    <!-- ================= RELATED ================= -->
    <section v-if="related.length" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-14">
      <div class="flex items-end justify-between mb-5">
        <div>
          <h2 class="text-xl sm:text-2xl font-black text-slate-900">Có thể bạn cũng thích</h2>
          <p class="text-xs text-slate-500 mt-0.5">Sự kiện tương tự đang mở bán</p>
        </div>
        <router-link to="/events" class="text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1">
          Xem tất cả <ArrowRight class="w-4 h-4" />
        </router-link>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <EventCard v-for="e in related" :key="e.id" :event="e" />
      </div>
    </section>

    <!-- ================= MOBILE BOTTOM BAR ================= -->
    <div class="lg:hidden fixed bottom-0 inset-x-0 z-40 bg-white/95 backdrop-blur-md border-t border-slate-200 py-3 shadow-[0_-8px_24px_-12px_rgba(15,23,42,0.2)]">
      <div class="px-4 flex items-center justify-between gap-3">
        <div>
          <span class="text-[11px] text-slate-500 block font-medium">
            {{ bookingStore.totalTicketCount ? `Đã chọn ${bookingStore.totalTicketCount} vé` : 'Giá từ' }}
          </span>
          <span class="text-xl font-black text-rose-600">
            {{ formatCurrency(bookingStore.totalTicketCount ? bookingStore.subtotal : event.minPrice) }}
          </span>
        </div>
        <button
          id="mobile-checkout-btn"
          type="button"
          @click="bookingStore.totalTicketCount ? proceedToBooking() : scrollTo('tickets')"
          class="px-6 py-3 rounded-2xl btn-primary text-sm flex items-center gap-2 shadow-lg shadow-indigo-600/25"
        >
          {{ bookingStore.totalTicketCount ? 'Đặt vé' : 'Chọn vé' }}
          <ArrowRight class="w-4 h-4" />
        </button>
      </div>
    </div>

    <!-- ================= LIGHTBOX ================= -->
    <transition enter-active-class="transition duration-200" enter-from-class="opacity-0" leave-active-class="transition duration-150" leave-to-class="opacity-0">
      <div v-if="lightboxIndex !== null && extras" class="fixed inset-0 z-50 bg-slate-950/90 backdrop-blur flex items-center justify-center p-4" @click.self="closeLightbox">
        <button type="button" @click="closeLightbox" class="absolute top-4 right-4 w-10 h-10 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center" aria-label="Đóng">
          <X class="w-5 h-5" />
        </button>
        <button type="button" @click="stepLightbox(-1)" class="absolute left-4 w-11 h-11 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center" aria-label="Ảnh trước">
          <ChevronLeft class="w-6 h-6" />
        </button>
        <img :src="extras.gallery[lightboxIndex]" alt="Hình ảnh sự kiện" class="max-h-[85vh] max-w-full rounded-2xl shadow-2xl" />
        <button type="button" @click="stepLightbox(1)" class="absolute right-4 w-11 h-11 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center" aria-label="Ảnh sau">
          <ChevronRight class="w-6 h-6" />
        </button>
        <div class="absolute bottom-5 text-white/70 text-xs font-bold">{{ lightboxIndex + 1 }} / {{ extras.gallery.length }}</div>
      </div>
    </transition>

    <!-- ================= TOASTS ================= -->
    <div class="fixed top-4 right-4 z-[60] flex flex-col gap-2 w-[min(360px,calc(100vw-2rem))]">
      <transition-group enter-active-class="transition duration-300 ease-out" enter-from-class="opacity-0 translate-x-6" leave-active-class="transition duration-200" leave-to-class="opacity-0 translate-x-6">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="flex items-start gap-2.5 px-4 py-3 rounded-2xl shadow-xl border text-sm font-semibold bg-white"
          :class="t.type === 'error' ? 'border-rose-200 text-rose-700' : t.type === 'success' ? 'border-emerald-200 text-emerald-700' : 'border-slate-200 text-slate-700'"
        >
          <AlertCircle v-if="t.type === 'error'" class="w-4 h-4 shrink-0 mt-0.5" />
          <CheckCircle2 v-else-if="t.type === 'success'" class="w-4 h-4 shrink-0 mt-0.5" />
          <Info v-else class="w-4 h-4 shrink-0 mt-0.5" />
          <span>{{ t.message }}</span>
        </div>
      </transition-group>
    </div>
  </div>
</template>

<style scoped>
@reference "../style.css";

.section-title {
  @apply text-xl font-black text-slate-900 mb-5 pb-3 border-b border-slate-100 flex items-center gap-2;
}
.section-title::before {
  content: '';
  @apply w-1.5 h-6 rounded-full bg-gradient-to-b from-indigo-500 to-fuchsia-500;
}
.fact-card {
  @apply flex flex-col gap-1 p-3 rounded-2xl bg-slate-50 border border-slate-100;
}
.fact-label {
  @apply text-[10px] font-bold uppercase tracking-wider text-slate-400;
}
.fact-value {
  @apply text-sm font-black text-slate-900;
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  scrollbar-width: none;
}
</style>
