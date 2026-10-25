<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Event } from '@/types'
import {
  Calendar,
  MapPin,
  ArrowRight,
  Sparkles,
  Heart,
  Share2,
  Users,
  Ticket,
  Clock,
  Flame,
} from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    event: Event
    viewMode?: 'grid' | 'list'
  }>(),
  {
    viewMode: 'grid',
  }
)

const emit = defineEmits<{
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

// Favorite state with localStorage
const isFavorited = ref(false)
try {
  const saved = localStorage.getItem(`eventhub_fav_${props.event.id}`)
  if (saved) isFavorited.value = JSON.parse(saved)
} catch {
  // ignore
}

const toggleFavorite = (e: MouseEvent) => {
  e.preventDefault()
  e.stopPropagation()
  isFavorited.value = !isFavorited.value
  try {
    localStorage.setItem(`eventhub_fav_${props.event.id}`, JSON.stringify(isFavorited.value))
  } catch {
    // ignore
  }
  emit('notify', isFavorited.value ? `Đã thêm "${props.event.title}" vào mục yêu thích!` : `Đã bỏ lưu sự kiện`, 'info')
}

const handleShare = async (e: MouseEvent) => {
  e.preventDefault()
  e.stopPropagation()
  try {
    await navigator.clipboard.writeText(window.location.origin + `/events/${props.event.id}`)
    emit('notify', 'Đã sao chép liên kết sự kiện vào bộ nhớ tạm!', 'success')
  } catch {
    emit('notify', 'Không thể sao chép liên kết', 'error')
  }
}

const formattedDate = computed(() => {
  if (!props.event.startDateTime) return 'Sắp diễn ra'
  try {
    const d = new Date(props.event.startDateTime)
    if (isNaN(d.getTime())) return 'Sắp diễn ra'
    return new Intl.DateTimeFormat('vi-VN', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(d)
  } catch {
    return 'Sắp diễn ra'
  }
})

const formattedPrice = computed(() => {
  const price = props.event.minPrice || 0
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(price)
})

// Days left until event
const daysLeft = computed(() => {
  if (!props.event.startDateTime) return 0
  const diff = new Date(props.event.startDateTime).getTime() - Date.now()
  if (diff <= 0) return 0
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
})
</script>

<template>
  <!-- LIST VIEW MODE -->
  <div
    v-if="viewMode === 'list'"
    class="bg-[#0D121F] rounded-2xl overflow-hidden border border-[#1C2438] hover:border-blue-500/50 shadow-xl hover:shadow-2xl hover:shadow-blue-950/40 transition-all duration-300 flex flex-col md:flex-row group"
  >
    <!-- Left Thumbnail -->
    <div class="md:w-72 h-52 md:h-auto relative bg-slate-950 overflow-hidden shrink-0">
      <img
        :src="event.bannerUrl"
        :alt="event.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 opacity-90"
        loading="lazy"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-[#0D121F] via-transparent to-black/40"></div>

      <!-- Category Pill -->
      <div class="absolute top-3 left-3 flex flex-wrap gap-1.5 items-center">
        <span class="px-2.5 py-1 rounded-lg text-[10px] font-extrabold bg-black/75 text-slate-200 shadow-md border border-white/20 backdrop-blur-md uppercase tracking-wide">
          {{ event.category?.name || 'Sự kiện' }}
        </span>
        <span
          v-if="event.featured"
          class="px-2 py-0.5 rounded-lg text-[10px] font-black bg-rose-600 text-white shadow-md flex items-center gap-1 uppercase tracking-wide"
        >
          <Flame class="w-3 h-3 text-amber-300 fill-current" />
          Hot
        </span>
      </div>

      <!-- Seat Map Badge -->
      <div v-if="event.hasSeatMap" class="absolute bottom-3 left-3">
        <span class="px-2.5 py-1 rounded-lg text-[10px] font-extrabold bg-emerald-600/90 text-white shadow-md flex items-center gap-1 backdrop-blur-md">
          <Sparkles class="w-3 h-3 text-emerald-200" />
          Chọn chỗ ngồi
        </span>
      </div>

      <!-- Action Floating Buttons -->
      <div class="absolute top-3 right-3 flex items-center gap-1.5">
        <button
          type="button"
          @click="toggleFavorite"
          class="w-8 h-8 rounded-full bg-black/60 hover:bg-black/90 text-slate-300 hover:text-white flex items-center justify-center transition shadow-md border border-white/15"
          :class="isFavorited ? 'text-rose-500 bg-black/90' : ''"
          title="Lưu sự kiện"
        >
          <Heart class="w-4 h-4" :class="isFavorited ? 'fill-rose-500 text-rose-500' : ''" />
        </button>
      </div>
    </div>

    <!-- Center Content -->
    <div class="p-5 flex-1 flex flex-col justify-between min-w-0">
      <div>
        <div class="flex items-center gap-3 text-xs text-blue-400 font-bold mb-2">
          <div class="flex items-center gap-1.5">
            <Calendar class="w-3.5 h-3.5" />
            <span>{{ formattedDate }}</span>
          </div>
          <span class="text-slate-600">&bull;</span>
          <span class="text-slate-400 font-medium">Còn {{ daysLeft }} ngày</span>
        </div>

        <h3 class="font-extrabold text-white text-base sm:text-lg leading-snug line-clamp-2 group-hover:text-blue-400 transition-colors mb-2">
          <router-link :to="`/events/${event.id}`">
            {{ event.title }}
          </router-link>
        </h3>

        <p class="text-xs text-slate-400 line-clamp-2 leading-relaxed mb-3">
          {{ event.shortDescription }}
        </p>

        <div class="flex items-center gap-2 text-xs text-slate-400">
          <MapPin class="w-3.5 h-3.5 text-rose-400 shrink-0" />
          <span class="truncate">{{ event.locationName }} &bull; {{ event.address }}</span>
        </div>
      </div>

      <div class="pt-3 border-t border-slate-800/80 flex items-center justify-between mt-3 text-xs text-slate-400">
        <div class="flex items-center gap-1.5">
          <Users class="w-3.5 h-3.5 text-slate-500" />
          <span>BTC: <strong class="text-slate-300 font-semibold">{{ event.organizerName }}</strong></span>
        </div>
        <button
          type="button"
          @click="handleShare"
          class="hover:text-blue-400 text-slate-400 font-medium flex items-center gap-1 transition cursor-pointer"
          title="Chia sẻ link"
        >
          <Share2 class="w-3.5 h-3.5" /> Chia sẻ
        </button>
      </div>
    </div>

    <!-- Right Price & CTA Stub -->
    <div class="p-5 md:w-56 bg-slate-950/70 border-t md:border-t-0 md:border-l border-slate-800/80 flex flex-col items-center justify-center text-center gap-3 shrink-0">
      <div>
        <span class="text-[10px] font-extrabold uppercase tracking-wider text-slate-400 block">Giá vé từ</span>
        <span class="text-lg sm:text-xl font-black text-white">{{ formattedPrice }}</span>
      </div>

      <router-link
        :to="`/events/${event.id}`"
        class="w-full py-2.5 px-4 rounded-xl text-xs font-extrabold text-white bg-blue-600 hover:bg-blue-500 transition flex items-center justify-center gap-1.5 shadow-md shadow-blue-600/30 cursor-pointer"
      >
        <span>{{ event.hasSeatMap ? 'Chọn chỗ ngồi' : 'Đặt vé ngay' }}</span>
        <ArrowRight class="w-3.5 h-3.5" />
      </router-link>
    </div>
  </div>

  <!-- GRID VIEW MODE (DEFAULT) -->
  <div
    v-else
    class="rounded-2xl overflow-hidden flex flex-col group h-full bg-[#0D121F] border border-[#1C2438] hover:border-blue-500/60 shadow-xl hover:shadow-2xl hover:shadow-blue-950/40 transition-all duration-300 hover:-translate-y-1"
  >
    <!-- Banner Image Container -->
    <div class="relative aspect-[16/10] w-full overflow-hidden bg-slate-950">
      <img
        :src="event.bannerUrl"
        :alt="event.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
        loading="lazy"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-[#0D121F] via-black/20 to-transparent"></div>

      <!-- Top Badges Row -->
      <div class="absolute top-3 left-3 right-3 flex items-start justify-between gap-1.5 pointer-events-none">
        <div class="flex flex-wrap gap-1.5 items-center pointer-events-auto">
          <span class="px-2.5 py-1 rounded-lg text-[10px] font-extrabold bg-black/75 text-slate-200 shadow-md border border-white/20 backdrop-blur-md uppercase tracking-wide">
            {{ event.category?.name || 'Sự kiện' }}
          </span>
          <span
            v-if="event.featured"
            class="px-2 py-0.5 rounded-lg text-[10px] font-black bg-rose-600 text-white shadow-md flex items-center gap-1 uppercase tracking-wide"
          >
            <Flame class="w-3 h-3 text-amber-300 fill-current" />
            Hot
          </span>
        </div>

        <!-- Favorite button -->
        <button
          type="button"
          @click="toggleFavorite"
          class="w-7 h-7 rounded-full bg-black/60 hover:bg-black/90 text-slate-300 hover:text-white flex items-center justify-center transition shadow-md border border-white/15 pointer-events-auto shrink-0"
          :class="isFavorited ? 'text-rose-500 bg-black/90' : ''"
          title="Lưu sự kiện"
        >
          <Heart class="w-3.5 h-3.5" :class="isFavorited ? 'fill-rose-500 text-rose-500' : ''" />
        </button>
      </div>

      <!-- Seat Map Badge -->
      <div v-if="event.hasSeatMap" class="absolute bottom-8 left-2.5">
        <span class="px-2 py-0.5 rounded-md text-[10px] font-extrabold bg-emerald-600/90 text-white shadow-md flex items-center gap-1 backdrop-blur-md">
          <Sparkles class="w-3 h-3 text-emerald-200" />
          Sơ đồ ghế
        </span>
      </div>

      <!-- Location banner overlay bottom -->
      <div class="absolute bottom-2 left-2 right-2 px-2.5 py-1 rounded-lg bg-black/70 backdrop-blur-md text-slate-200 text-[11px] flex items-center gap-1.5 truncate border border-white/10">
        <MapPin class="w-3 h-3 shrink-0 text-rose-400" />
        <span class="truncate">{{ event.locationName }}</span>
      </div>
    </div>

    <!-- Content Area -->
    <div class="p-4 sm:p-5 flex-1 flex flex-col justify-between">
      <div>
        <!-- Date & Countdown Row -->
        <div class="flex items-center justify-between text-xs font-bold text-blue-400 mb-2">
          <div class="flex items-center gap-1.5">
            <Calendar class="w-3.5 h-3.5 shrink-0" />
            <span class="truncate">{{ formattedDate }}</span>
          </div>
          <span v-if="daysLeft > 0" class="text-[10px] text-slate-400 font-semibold shrink-0">
            Còn {{ daysLeft }} ngày
          </span>
        </div>

        <!-- Title -->
        <h3 class="font-extrabold text-white text-base leading-snug line-clamp-2 group-hover:text-blue-400 transition-colors mb-2">
          <router-link :to="`/events/${event.id}`">
            {{ event.title }}
          </router-link>
        </h3>

        <!-- Short Description -->
        <p class="text-xs text-slate-400 line-clamp-2 leading-relaxed mb-4">
          {{ event.shortDescription }}
        </p>
      </div>

      <!-- Price & CTA -->
      <div class="pt-3 border-t border-slate-800/80 flex items-center justify-between mt-auto">
        <div>
          <span class="text-[10px] font-extrabold uppercase tracking-wider text-slate-400 block">Giá vé từ</span>
          <span class="text-base sm:text-lg font-black text-white">{{ formattedPrice }}</span>
        </div>

        <router-link
          :to="`/events/${event.id}`"
          class="px-3.5 py-2 rounded-xl text-xs font-extrabold text-white bg-blue-600 hover:bg-blue-500 transition flex items-center gap-1 shadow-md shadow-blue-600/30 group-hover:shadow-lg group-hover:shadow-blue-600/50 cursor-pointer"
        >
          <span>{{ event.hasSeatMap ? 'Chọn chỗ' : 'Mua vé' }}</span>
          <ArrowRight class="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </router-link>
      </div>
    </div>
  </div>
</template>
