<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import type { Ticket } from '@/types'
import {
  X,
  CheckCircle,
  Download,
  Calendar,
  MapPin,
  RefreshCw,
  Sun,
  ShieldCheck,
  DoorOpen,
  Printer,
  Sparkles,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

// Dynamic token countdown (refreshes every 30 seconds for anti-passback)
const REFRESH_SECONDS = 30
const secondsLeft = ref(REFRESH_SECONDS)
const securityToken = ref('SEC-8891-LIVE')
let interval: ReturnType<typeof setInterval> | null = null

// Max brightness simulator mode
const isMaxBrightness = ref(false)

const startCountdown = () => {
  secondsLeft.value = REFRESH_SECONDS
  securityToken.value = 'TOKEN-' + Math.floor(100000 + Math.random() * 900000)
  if (interval) clearInterval(interval)
  interval = setInterval(() => {
    if (secondsLeft.value <= 1) {
      secondsLeft.value = REFRESH_SECONDS
      securityToken.value = 'TOKEN-' + Math.floor(100000 + Math.random() * 900000)
    } else {
      secondsLeft.value--
    }
  }, 1000)
}

watch(
  () => props.show,
  (val) => {
    if (val) {
      startCountdown()
    } else {
      if (interval) clearInterval(interval)
      isMaxBrightness.value = false
    }
  }
)

onBeforeUnmount(() => {
  if (interval) clearInterval(interval)
})

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateStr))
}

const handlePrint = () => {
  window.print()
}

const toggleBrightness = () => {
  isMaxBrightness.value = !isMaxBrightness.value
  emit('notify', isMaxBrightness.value ? 'Đã bật chế độ sáng tối đa để quét cổng' : 'Đã tắt chế độ sáng tối đa', 'info')
}

const simulateWallet = (provider: 'Apple' | 'Google') => {
  emit('notify', `Đã thêm thẻ vé vào ${provider} Wallet thành công!`, 'success')
}
</script>

<template>
  <div
    v-if="show && ticket"
    class="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-black/85 backdrop-blur-md animate-fade-in transition-all"
    :class="isMaxBrightness ? 'bg-black/95' : ''"
    @click.self="emit('close')"
  >
    <div
      class="relative w-full max-w-md rounded-3xl overflow-hidden shadow-2xl transition-all duration-300 max-h-[95vh] flex flex-col"
      :class="isMaxBrightness ? 'bg-white ring-8 ring-white' : 'bg-slate-900 border border-slate-800'"
    >
      <!-- Header Gradient -->
      <div class="h-28 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 p-4 relative shrink-0">
        <button
          @click="emit('close')"
          class="absolute top-4 right-4 w-8 h-8 rounded-full bg-black/30 text-white flex items-center justify-center hover:bg-black/50 transition-colors"
          aria-label="Đóng"
        >
          <X class="w-4 h-4" />
        </button>

        <div class="flex items-center gap-2">
          <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-black/40 text-white border border-white/20 backdrop-blur-sm">
            <ShieldCheck class="w-3.5 h-3.5 text-emerald-400" />
            Vé Điện Tử Chính Hãng (Anti-Passback)
          </span>
        </div>
      </div>

      <!-- Ticket Body with Cutouts -->
      <div
        class="px-6 pb-6 pt-3 relative flex-1 overflow-y-auto"
        :class="isMaxBrightness ? 'bg-white text-slate-900' : 'bg-slate-900 text-white'"
      >
        <!-- Notches on sides -->
        <div class="absolute -top-3 -left-3 w-6 h-6 rounded-full" :class="isMaxBrightness ? 'bg-black/80' : 'bg-black'"></div>
        <div class="absolute -top-3 -right-3 w-6 h-6 rounded-full" :class="isMaxBrightness ? 'bg-black/80' : 'bg-black'"></div>

        <!-- Event Details Header -->
        <div class="text-center mb-5">
          <h3
            class="text-lg font-black leading-tight mb-2"
            :class="isMaxBrightness ? 'text-slate-900' : 'text-white'"
          >
            {{ ticket.eventTitle }}
          </h3>

          <div class="flex items-center justify-center gap-1.5 text-xs font-bold text-indigo-400 mb-1">
            <Calendar class="w-3.5 h-3.5" />
            <span>{{ formatDate(ticket.eventStartDateTime) }}</span>
          </div>
          <div class="flex items-center justify-center gap-1.5 text-xs" :class="isMaxBrightness ? 'text-slate-500' : 'text-slate-400'">
            <MapPin class="w-3.5 h-3.5" />
            <span class="truncate max-w-xs">{{ ticket.venueName }}</span>
          </div>
        </div>

        <!-- Ticket Info Matrix -->
        <div
          class="grid grid-cols-2 gap-2.5 p-3 rounded-2xl text-xs mb-5 border"
          :class="isMaxBrightness ? 'bg-slate-50 border-slate-200' : 'bg-slate-800/70 border-slate-700/60'"
        >
          <div>
            <span class="text-slate-400 block text-[10px] uppercase font-bold">Khán giả</span>
            <span class="font-bold truncate block" :class="isMaxBrightness ? 'text-slate-900' : 'text-white'">
              {{ ticket.attendeeName }}
            </span>
          </div>
          <div>
            <span class="text-slate-400 block text-[10px] uppercase font-bold">Hạng vé</span>
            <span class="font-bold text-indigo-500 truncate block">{{ ticket.ticketTypeName }}</span>
          </div>
          <div>
            <span class="text-slate-400 block text-[10px] uppercase font-bold">Vị trí chỗ ngồi</span>
            <span class="font-black text-amber-500 truncate block">{{ ticket.seatLabel || 'Vé Tự Do (GA)' }}</span>
          </div>
          <div>
            <span class="text-slate-400 block text-[10px] uppercase font-bold">Cổng soát vé</span>
            <span class="font-bold text-emerald-500 flex items-center gap-1 truncate">
              <DoorOpen class="w-3 h-3" /> {{ ticket.gate || 'Cổng Chính' }}
            </span>
          </div>
        </div>

        <!-- Simulated High-Tech Dynamic QR Card -->
        <div
          class="flex flex-col items-center justify-center p-5 rounded-3xl bg-white text-slate-900 mb-5 shadow-2xl border border-slate-100"
          :class="isMaxBrightness ? 'ring-4 ring-indigo-500/30' : ''"
        >
          <!-- Dynamic Refresh Header -->
          <div class="flex items-center justify-between w-full mb-3 text-[11px] font-bold">
            <span class="flex items-center gap-1.5 text-indigo-700">
              <RefreshCw class="w-3.5 h-3.5 animate-spin" style="animation-duration: 4s;" />
              <span>Mã QR Động Bảo Mật</span>
            </span>
            <span class="px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 text-[10px] font-mono">
              Làm mới sau: {{ secondsLeft }}s
            </span>
          </div>

          <!-- Simulated QR Code SVG Pattern -->
          <div class="w-48 h-48 relative bg-white p-2 rounded-2xl flex items-center justify-center border-2 border-slate-900">
            <svg class="w-full h-full" viewBox="0 0 100 100" fill="currentColor">
              <!-- Corner Finder 1 -->
              <rect x="5" y="5" width="26" height="26" rx="4" fill="black" />
              <rect x="9" y="9" width="18" height="18" fill="white" />
              <rect x="13" y="13" width="10" height="10" rx="2" fill="black" />

              <!-- Corner Finder 2 -->
              <rect x="69" y="5" width="26" height="26" rx="4" fill="black" />
              <rect x="73" y="9" width="18" height="18" fill="white" />
              <rect x="77" y="13" width="10" height="10" rx="2" fill="black" />

              <!-- Corner Finder 3 -->
              <rect x="5" y="69" width="26" height="26" rx="4" fill="black" />
              <rect x="9" y="73" width="18" height="18" fill="white" />
              <rect x="13" y="77" width="10" height="10" rx="2" fill="black" />

              <!-- Dense Data Pattern Elements -->
              <rect x="36" y="8" width="6" height="6" fill="black" />
              <rect x="46" y="12" width="6" height="6" fill="black" />
              <rect x="56" y="6" width="6" height="6" fill="black" />
              <rect x="8" y="38" width="6" height="6" fill="black" />
              <rect x="20" y="44" width="6" height="6" fill="black" />
              <rect x="68" y="38" width="6" height="6" fill="black" />
              <rect x="80" y="44" width="6" height="6" fill="black" />
              <rect x="40" y="70" width="10" height="6" fill="black" />
              <rect x="58" y="76" width="14" height="6" fill="black" />
              <rect x="80" y="80" width="8" height="8" fill="black" />

              <!-- Center Logo Shield -->
              <rect x="36" y="36" width="28" height="28" rx="8" fill="#4f46e5" />
              <path d="M44 50 L48 54 L56 46" stroke="white" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
          </div>

          <!-- Dynamic Token & Ticket Code -->
          <div class="mt-3 text-center">
            <span class="text-xs font-mono font-black tracking-widest text-slate-900 block">
              {{ ticket.ticketCode }}
            </span>
            <span class="text-[10px] font-mono text-indigo-600 font-bold block mt-0.5">
              Hash: {{ securityToken }}
            </span>
          </div>

          <!-- Barcode simulation underneath -->
          <div class="w-full mt-3 pt-3 border-t border-slate-100 flex flex-col items-center">
            <div class="h-6 w-44 flex items-center justify-between gap-0.5 px-2">
              <span v-for="i in 32" :key="i" class="bg-black h-full" :style="{ width: `${(i % 3) + 1}px`, opacity: i % 5 === 0 ? 0.3 : 1 }"></span>
            </div>
            <span class="text-[9px] font-mono text-slate-400 mt-1 uppercase tracking-wider">
              Scan Barcode / QR Code
            </span>
          </div>
        </div>

        <!-- Brightness & Anti-passback Tips -->
        <div class="flex items-center justify-between gap-2 mb-4 text-xs">
          <button
            type="button"
            @click="toggleBrightness"
            class="flex-1 py-2 px-3 rounded-xl border flex items-center justify-center gap-1.5 font-bold transition text-[11px]"
            :class="isMaxBrightness ? 'bg-amber-400 text-slate-950 border-amber-500' : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'"
          >
            <Sun class="w-3.5 h-3.5" />
            <span>{{ isMaxBrightness ? 'Giảm độ sáng' : 'Tăng sáng quét mã' }}</span>
          </button>

          <button
            type="button"
            @click="handlePrint"
            class="flex-1 py-2 px-3 rounded-xl border flex items-center justify-center gap-1.5 font-bold transition text-[11px]"
            :class="isMaxBrightness ? 'bg-slate-100 border-slate-300 text-slate-800' : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'"
          >
            <Printer class="w-3.5 h-3.5" />
            <span>In / Tải PDF</span>
          </button>
        </div>

        <!-- Wallet Shortcuts -->
        <div class="grid grid-cols-2 gap-2 mb-4">
          <button
            type="button"
            @click="simulateWallet('Apple')"
            class="py-2 px-3 rounded-xl bg-black border border-white/20 text-white text-[11px] font-bold flex items-center justify-center gap-1.5 hover:bg-slate-900 transition"
          >
            <span> Thêm Apple Wallet</span>
          </button>
          <button
            type="button"
            @click="simulateWallet('Google')"
            class="py-2 px-3 rounded-xl bg-white border border-slate-300 text-slate-800 text-[11px] font-bold flex items-center justify-center gap-1.5 hover:bg-slate-50 transition"
          >
            <span>G Thêm Google Wallet</span>
          </button>
        </div>

        <!-- Security Notice -->
        <p class="text-[10px] text-center mb-2" :class="isMaxBrightness ? 'text-slate-500' : 'text-slate-400'">
          🔒 Mỗi mã QR chỉ hợp lệ cho 01 lần quét duy nhất. Hệ thống tự động khóa vé ngay khi check-in qua cổng.
        </p>
      </div>

      <!-- Footer Action -->
      <div
        class="px-6 py-3 border-t shrink-0 flex items-center justify-between"
        :class="isMaxBrightness ? 'bg-slate-50 border-slate-200' : 'bg-slate-950 border-slate-800'"
      >
        <span class="text-xs text-slate-400 font-medium">Đơn: #{{ ticket.bookingCode }}</span>
        <button
          @click="emit('close')"
          class="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-black transition-colors"
        >
          Xong
        </button>
      </div>
    </div>
  </div>
</template>
