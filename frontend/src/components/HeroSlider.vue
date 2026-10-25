<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import type { Event } from '@/types'
import {
  Calendar,
  MapPin,
  ChevronLeft,
  ChevronRight,
  ArrowRight,
  Flame,
  Users,
  Clock,
  Sparkles,
} from 'lucide-vue-next'

const props = defineProps<{
  events: Event[]
}>()

const currentIndex = ref(0)
const isPaused = ref(false)
const slideDuration = 5500 // 5.5s per slide
let autoSlideTimer: any = null

const activeEvent = computed(() => {
  if (!props.events.length) return null
  return props.events[currentIndex.value] || props.events[0]
})

const nextSlide = () => {
  if (!props.events.length) return
  currentIndex.value = (currentIndex.value + 1) % props.events.length
}

const prevSlide = () => {
  if (!props.events.length) return
  currentIndex.value = (currentIndex.value - 1 + props.events.length) % props.events.length
}

const goToSlide = (idx: number) => {
  currentIndex.value = idx
}

onMounted(() => {
  autoSlideTimer = setInterval(() => {
    if (!isPaused.value) {
      nextSlide()
    }
  }, slideDuration)
})

onUnmounted(() => {
  if (autoSlideTimer) clearInterval(autoSlideTimer)
})

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatDate = (dateStr: string) => {
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(dateStr))
}
</script>

<template>
  <div
    v-if="activeEvent"
    class="relative rounded-3xl overflow-hidden bg-slate-900 shadow-2xl border border-slate-200/90 group"
    @mouseenter="isPaused = true"
    @mouseleave="isPaused = false"
  >
    <!-- Slides Container with Crossfade Transition -->
    <div class="relative min-h-[440px] sm:min-h-[480px] lg:min-h-[510px] w-full overflow-hidden">
      <transition-group name="slide-fade">
        <div
          v-for="(evt, idx) in events"
          :key="evt.id"
          v-show="idx === currentIndex"
          class="absolute inset-0 w-full h-full"
        >
          <!-- Background Image with Zoom Effect -->
          <img
            :src="evt.bannerUrl"
            :alt="evt.title"
            class="w-full h-full object-cover animate-ken-burns opacity-75"
          />
          <!-- Multi-stop Gradient Overlay for Crisp Readability -->
          <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/65 to-transparent"></div>
          <div class="absolute inset-0 bg-gradient-to-r from-slate-950/90 via-slate-950/40 to-transparent"></div>

          <!-- Slide Content Overlay -->
          <div class="absolute inset-0 max-w-7xl mx-auto px-6 sm:px-10 flex flex-col justify-end pt-6 pb-16 sm:pb-18">
            <div class="max-w-2xl text-white">
              <!-- Live Badges -->
              <div class="flex flex-wrap items-center gap-2 mb-3 animate-fade-in-up">
                <span class="px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider bg-rose-600 text-white shadow-lg shadow-rose-600/40 flex items-center gap-1.5 animate-pulse">
                  <Flame class="w-3.5 h-3.5 fill-white" />
                  SỰ KIỆN NỔI BẬT NHẤT
                </span>
                <span class="px-3 py-1 rounded-full text-xs font-bold bg-white/20 backdrop-blur-md text-white border border-white/20">
                  {{ evt.category?.name || 'Sự kiện nổi bật' }}
                </span>
                <span class="hidden sm:inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-500/20 text-emerald-300 border border-emerald-400/30">
                  <Users class="w-3.5 h-3.5" />
                  {{ 180 + idx * 45 }} người đang xem
                </span>
              </div>

              <!-- Title -->
              <h1 class="text-xl sm:text-3xl lg:text-4xl font-black text-white leading-snug mb-2.5 drop-shadow-lg tracking-tight">
                {{ evt.title }}
              </h1>

              <!-- Description -->
              <p class="text-xs sm:text-sm text-slate-200 line-clamp-2 leading-relaxed mb-4 max-w-xl drop-shadow">
                {{ evt.shortDescription }}
              </p>

              <!-- Meta & CTA -->
              <div class="flex flex-wrap items-center gap-4 sm:gap-6 pt-3.5 border-t border-white/20">
                <div class="flex flex-wrap items-center gap-2 sm:gap-6 text-xs sm:text-sm font-semibold text-slate-200">
                  <div class="flex items-center gap-1.5 text-indigo-300">
                    <Calendar class="w-4 h-4 text-indigo-400" />
                    <span>{{ formatDate(evt.startDateTime) }}</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <MapPin class="w-4 h-4 text-pink-400" />
                    <span class="truncate max-w-[200px]">{{ evt.locationName }}</span>
                  </div>
                  <div class="text-rose-400 font-black text-base sm:text-lg">
                    Từ {{ formatCurrency(evt.minPrice) }}
                  </div>
                </div>

                <router-link
                  :to="`/events/${evt.id}`"
                  class="px-6 py-2.5 rounded-xl bg-white text-slate-950 hover:bg-slate-100 font-black text-xs sm:text-sm flex items-center gap-2 shadow-xl hover:scale-105 active:scale-95 transition-all group/btn"
                >
                  <span>Đặt Vé Ngay</span>
                  <ArrowRight class="w-4 h-4 group-hover/btn:translate-x-1 transition-transform" />
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <!-- Navigation Arrows -->
    <button
      @click="prevSlide"
      class="absolute left-4 top-1/2 -translate-y-1/2 w-11 h-11 rounded-2xl bg-black/40 hover:bg-black/70 backdrop-blur-md text-white flex items-center justify-center border border-white/20 transition-all opacity-0 group-hover:opacity-100 hover:scale-110 z-20"
      title="Slide trước"
    >
      <ChevronLeft class="w-6 h-6" />
    </button>
    <button
      @click="nextSlide"
      class="absolute right-4 top-1/2 -translate-y-1/2 w-11 h-11 rounded-2xl bg-black/40 hover:bg-black/70 backdrop-blur-md text-white flex items-center justify-center border border-white/20 transition-all opacity-0 group-hover:opacity-100 hover:scale-110 z-20"
      title="Slide tiếp theo"
    >
      <ChevronRight class="w-6 h-6" />
    </button>

    <!-- Thumbnail Bar Indicators Bottom -->
    <div class="absolute bottom-3 sm:bottom-3.5 left-6 right-6 z-20 flex items-center justify-between">
      <!-- Thumbnail Previews for Desktop -->
      <div class="hidden sm:flex items-center gap-3">
        <button
          v-for="(evt, idx) in events.slice(0, 4)"
          :key="evt.id"
          @click="goToSlide(idx)"
          class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-black/50 backdrop-blur-md border text-left transition-all hover:bg-black/70"
          :class="idx === currentIndex ? 'border-indigo-400 ring-2 ring-indigo-500/50 bg-black/80' : 'border-white/10 opacity-70 hover:opacity-100'"
        >
          <img :src="evt.bannerUrl" :alt="evt.title" class="w-7 h-7 rounded-lg object-cover" />
          <div class="max-w-[130px]">
            <p class="text-[11px] font-bold text-white truncate">{{ evt.title }}</p>
            <p class="text-[9px] text-indigo-300 truncate">{{ evt.category.name }}</p>
          </div>
        </button>
      </div>

      <!-- Slide Dots / Progress for Mobile -->
      <div class="flex items-center gap-2 sm:ml-auto">
        <button
          v-for="(_, idx) in events"
          :key="idx"
          @click="goToSlide(idx)"
          class="h-2 rounded-full transition-all"
          :class="idx === currentIndex ? 'w-8 bg-indigo-500' : 'w-2 bg-white/40 hover:bg-white/80'"
        ></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Crossfade Transition */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: opacity 0.6s ease-in-out;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
}

/* Subtle Slow Ken Burns Zoom */
@keyframes kenburns {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.08);
  }
}

.animate-ken-burns {
  animation: kenburns 12s ease-out infinite alternate;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in-up {
  animation: fadeInUp 0.5s ease-out;
}
</style>
