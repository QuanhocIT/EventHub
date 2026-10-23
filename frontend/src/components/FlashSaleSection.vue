<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import type { Event } from '@/types'
import { Zap, Flame, Clock, ArrowRight, ShieldCheck } from 'lucide-vue-next'

const props = defineProps<{
  events: Event[]
}>()

// Countdown timer (HH:MM:SS)
const hours = ref(6)
const minutes = ref(42)
const seconds = ref(18)
let timer: any = null

onMounted(() => {
  timer = setInterval(() => {
    if (seconds.value > 0) {
      seconds.value--
    } else {
      seconds.value = 59
      if (minutes.value > 0) {
        minutes.value--
      } else {
        minutes.value = 59
        if (hours.value > 0) {
          hours.value--
        } else {
          hours.value = 12
        }
      }
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}
</script>

<template>
  <div class="rounded-3xl bg-gradient-to-br from-rose-50 via-amber-50/50 to-orange-50 border border-rose-200/80 p-6 sm:p-8 shadow-sm">
    <!-- Header with Ticking Timer -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 pb-4 border-b border-rose-200/70">
      <div class="flex items-center gap-3">
        <div class="w-11 h-11 rounded-2xl bg-rose-600 text-white flex items-center justify-center shadow-md shadow-rose-600/30 animate-bounce">
          <Zap class="w-6 h-6 fill-white" />
        </div>
        <div>
          <div class="flex items-center gap-2">
            <h3 class="text-xl sm:text-2xl font-black text-slate-900 uppercase tracking-tight">
              Flash Sale Vé Sớm
            </h3>
            <span class="px-2 py-0.5 rounded text-[10px] font-black uppercase bg-rose-600 text-white">
              GIẢM ĐẾN 25%
            </span>
          </div>
          <p class="text-xs text-slate-600 mt-0.5 font-medium">Số lượng vé ưu đãi có hạn, hệ thống tự động khóa khi hết slot</p>
        </div>
      </div>

      <!-- Real-time Countdown Box -->
      <div class="flex items-center gap-1.5 self-start sm:self-center">
        <span class="text-xs font-bold text-slate-600 mr-1 flex items-center gap-1">
          <Clock class="w-3.5 h-3.5 text-rose-600" />
          Kết thúc sau:
        </span>
        <div class="w-9 h-9 rounded-xl bg-slate-900 text-white font-mono font-black text-sm flex items-center justify-center shadow-sm">
          {{ hours.toString().padStart(2, '0') }}
        </div>
        <span class="font-black text-slate-900">:</span>
        <div class="w-9 h-9 rounded-xl bg-slate-900 text-white font-mono font-black text-sm flex items-center justify-center shadow-sm">
          {{ minutes.toString().padStart(2, '0') }}
        </div>
        <span class="font-black text-slate-900">:</span>
        <div class="w-9 h-9 rounded-xl bg-rose-600 text-white font-mono font-black text-sm flex items-center justify-center shadow-sm animate-pulse">
          {{ seconds.toString().padStart(2, '0') }}
        </div>
      </div>
    </div>

    <!-- Flash Sale Cards Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="(evt, idx) in events.slice(0, 3)"
        :key="evt.id"
        class="bg-white rounded-2xl overflow-hidden border border-rose-200 shadow-sm hover:shadow-xl hover:border-rose-400 transition-all flex flex-col group"
      >
        <!-- Image with Discount Tag -->
        <div class="relative aspect-video overflow-hidden bg-slate-100">
          <img
            :src="evt.bannerUrl"
            :alt="evt.title"
            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <div class="absolute top-3 left-3 px-2.5 py-1 rounded-lg bg-rose-600 text-white text-xs font-black shadow-md flex items-center gap-1">
            <Flame class="w-3.5 h-3.5 fill-white" />
            <span>-{{ 15 + idx * 5 }}% EARLY BIRD</span>
          </div>
          <div class="absolute bottom-2 left-2 right-2 px-2.5 py-1 rounded-md bg-black/60 backdrop-blur-sm text-white text-[11px] truncate">
            📍 {{ evt.locationName }}
          </div>
        </div>

        <!-- Body -->
        <div class="p-4 flex-1 flex flex-col justify-between">
          <div>
            <h4 class="font-bold text-slate-900 text-sm leading-snug line-clamp-2 mb-2 group-hover:text-rose-600 transition-colors">
              {{ evt.title }}
            </h4>

            <!-- Progress Bar of Sold Tickets -->
            <div class="mt-2 mb-3">
              <div class="flex items-center justify-between text-[11px] font-bold text-slate-600 mb-1">
                <span class="text-rose-600">Đã bán {{ 78 + idx * 7 }}%</span>
                <span>Chỉ còn {{ 12 - idx * 3 }} vé</span>
              </div>
              <div class="h-2 w-full rounded-full bg-slate-100 overflow-hidden">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-amber-500 to-rose-600 transition-all duration-1000"
                  :style="{ width: `${78 + idx * 7}%` }"
                ></div>
              </div>
            </div>
          </div>

          <!-- Price & Action -->
          <div class="pt-3 border-t border-slate-100 flex items-center justify-between">
            <div>
              <span class="text-[10px] text-slate-400 line-through block">
                {{ formatCurrency(Math.round(evt.minPrice * 1.25)) }}
              </span>
              <span class="text-base font-black text-rose-600">
                {{ formatCurrency(evt.minPrice) }}
              </span>
            </div>

            <router-link
              :to="`/events/${evt.id}`"
              class="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-700 text-white text-xs font-bold transition-colors flex items-center gap-1 shadow-sm"
            >
              <span>Săn Vé Ngay</span>
              <ArrowRight class="w-3.5 h-3.5" />
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
