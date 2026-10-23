<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import type { Event } from '@/types'
import { useBookingStore } from '@/stores/booking'
import { ArrowRight, ShieldCheck, Zap, RefreshCcw, Ticket, Trash2, Timer } from 'lucide-vue-next'

const props = defineProps<{
  event: Event
}>()

const emit = defineEmits<{
  (e: 'checkout'): void
}>()

const bookingStore = useBookingStore()

// Countdown
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | undefined
onMounted(() => (timer = setInterval(() => (now.value = Date.now()), 1000)))
onBeforeUnmount(() => timer && clearInterval(timer))

const countdown = computed(() => {
  const diff = Math.max(0, new Date(props.event.startDateTime).getTime() - now.value)
  return {
    ended: diff === 0,
    parts: [
      { label: 'Ngày', value: Math.floor(diff / 86400000) },
      { label: 'Giờ', value: Math.floor((diff / 3600000) % 24) },
      { label: 'Phút', value: Math.floor((diff / 60000) % 60) },
      { label: 'Giây', value: Math.floor((diff / 1000) % 60) },
    ],
  }
})

const selectedTiers = computed(() =>
  (props.event.ticketTypes || [])
    .map((t) => ({ ticket: t, qty: bookingStore.selectedTicketQuantities[t.id] || 0 }))
    .filter((x) => x.qty > 0)
)

const hasSelection = computed(() => bookingStore.totalTicketCount > 0)

const clearAll = () => bookingStore.clearBooking()

const formatCurrency = (val: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
const pad = (n: number) => n.toString().padStart(2, '0')
</script>

<template>
  <div class="space-y-4">
    <!-- Countdown -->
    <div class="rounded-3xl p-5 bg-gradient-to-br from-indigo-600 via-violet-600 to-fuchsia-600 text-white shadow-xl shadow-indigo-500/25 relative overflow-hidden">
      <div class="absolute -top-10 -right-10 w-32 h-32 rounded-full bg-white/10 blur-2xl"></div>
      <div class="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-white/80 mb-3">
        <Timer class="w-4 h-4" />
        {{ countdown.ended ? 'Sự kiện đã bắt đầu' : 'Sự kiện bắt đầu sau' }}
      </div>
      <div class="grid grid-cols-4 gap-2">
        <div v-for="p in countdown.parts" :key="p.label" class="rounded-2xl bg-white/15 backdrop-blur py-2.5 text-center">
          <div class="text-2xl font-black tabular-nums leading-none">{{ pad(p.value) }}</div>
          <div class="text-[10px] font-semibold text-white/75 mt-1">{{ p.label }}</div>
        </div>
      </div>
    </div>

    <!-- Order summary -->
    <div class="rounded-3xl bg-white border border-slate-200 shadow-sm overflow-hidden">
      <div class="px-5 pt-5 pb-4 border-b border-slate-100 flex items-center justify-between">
        <div>
          <span class="text-[11px] font-bold uppercase tracking-wider text-slate-400">Giá vé từ</span>
          <div class="text-2xl font-black text-rose-600 leading-tight">{{ formatCurrency(event.minPrice) }}</div>
        </div>
        <span class="px-2.5 py-1 rounded-full text-[10px] font-black bg-emerald-100 text-emerald-700">Đang mở bán</span>
      </div>

      <div class="p-5">
        <div v-if="!hasSelection" class="text-center py-4">
          <div class="w-12 h-12 mx-auto mb-2 rounded-2xl bg-indigo-50 flex items-center justify-center">
            <Ticket class="w-6 h-6 text-indigo-500" />
          </div>
          <p class="text-sm font-bold text-slate-700">Chưa chọn vé nào</p>
          <p class="text-xs text-slate-400 mt-0.5">
            {{ event.hasSeatMap ? 'Chọn ghế trên sơ đồ hoặc hạng vé bên trái' : 'Chọn hạng vé phù hợp bên trái' }}
          </p>
        </div>

        <div v-else class="space-y-2.5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-bold text-slate-500">Đơn của bạn</span>
            <button type="button" @click="clearAll" class="text-[11px] font-bold text-slate-400 hover:text-rose-600 flex items-center gap-1">
              <Trash2 class="w-3 h-3" /> Xóa tất cả
            </button>
          </div>

          <div v-for="x in selectedTiers" :key="x.ticket.id" class="flex items-center justify-between text-sm">
            <span class="flex items-center gap-2 min-w-0">
              <span class="w-2 h-2 rounded-full shrink-0" :style="{ backgroundColor: x.ticket.colorHex || '#4f46e5' }"></span>
              <span class="truncate text-slate-700">{{ x.ticket.name }}</span>
              <span class="text-slate-400 font-semibold shrink-0">×{{ x.qty }}</span>
            </span>
            <span class="font-bold text-slate-900 shrink-0">{{ formatCurrency(x.qty * x.ticket.price) }}</span>
          </div>

          <div v-for="s in bookingStore.selectedSeats" :key="`seat-${s.id}`" class="flex items-center justify-between text-sm">
            <span class="text-slate-700">Ghế {{ s.rowName }}{{ s.seatNumber }} <span class="text-[11px] text-slate-400">({{ s.zone }})</span></span>
            <span class="font-bold text-slate-900">{{ formatCurrency(s.price) }}</span>
          </div>

          <div class="pt-3 mt-1 border-t border-dashed border-slate-200 flex items-center justify-between">
            <span class="text-sm font-bold text-slate-600">Tạm tính ({{ bookingStore.totalTicketCount }} vé)</span>
            <span class="text-xl font-black text-rose-600">{{ formatCurrency(bookingStore.subtotal) }}</span>
          </div>
        </div>

        <button
          id="sidebar-checkout-btn"
          type="button"
          @click="emit('checkout')"
          :disabled="!hasSelection"
          class="mt-4 w-full py-3.5 rounded-2xl btn-primary text-sm flex items-center justify-center gap-2 shadow-lg shadow-indigo-500/25 disabled:opacity-40 disabled:cursor-not-allowed disabled:shadow-none disabled:transform-none"
        >
          Tiến Hành Đặt Vé <ArrowRight class="w-4 h-4" />
        </button>
        <p class="text-[11px] text-center text-slate-400 mt-2">Ghế được giữ 15 phút khi bạn tiếp tục thanh toán</p>
      </div>

      <!-- Trust badges -->
      <div class="px-5 py-4 bg-slate-50 border-t border-slate-100 grid grid-cols-3 gap-2 text-center">
        <div class="flex flex-col items-center gap-1">
          <ShieldCheck class="w-4 h-4 text-emerald-600" />
          <span class="text-[10px] font-bold text-slate-600 leading-tight">Vé chính hãng</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <Zap class="w-4 h-4 text-amber-500" />
          <span class="text-[10px] font-bold text-slate-600 leading-tight">Nhận vé tức thì</span>
        </div>
        <div class="flex flex-col items-center gap-1">
          <RefreshCcw class="w-4 h-4 text-indigo-600" />
          <span class="text-[10px] font-bold text-slate-600 leading-tight">Hoàn tiền nếu hủy</span>
        </div>
      </div>
    </div>
  </div>
</template>
