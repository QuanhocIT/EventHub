<script setup lang="ts">
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import type { EventSeat, SeatZone } from '@/types'
import { useBookingStore } from '@/stores/booking'
import { seatHoldService } from '@/services/seatHold.service'
import { Lock, X, ZoomIn, ZoomOut, RotateCcw, Trash2, Armchair, Clock, Check, ShieldAlert } from 'lucide-vue-next'

const props = defineProps<{
  seats: EventSeat[]
  eventId?: number
}>()

const emit = defineEmits<{
  (e: 'notify', message: string, type?: 'error' | 'success' | 'info'): void
}>()

const bookingStore = useBookingStore()
const MAX_SEATS = 6

const zoneFilter = ref<SeatZone | 'ALL'>('ALL')
const zoom = ref(1)

// Real-time Countdown Timer State
const remainingTime = ref('')
const remainingSeconds = ref(0)
let timer: any = null
let unsubscribeStream: (() => void) | null = null

const startCountdown = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    if (!bookingStore.holdExpiresAt || bookingStore.selectedSeats.length === 0) {
      remainingTime.value = ''
      remainingSeconds.value = 0
      return
    }
    const diff = Math.max(0, Math.floor((bookingStore.holdExpiresAt - Date.now()) / 1000))
    remainingSeconds.value = diff
    const mins = Math.floor(diff / 60)
    const secs = diff % 60
    remainingTime.value = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`

    if (diff === 0) {
      clearInterval(timer)
      emit('notify', 'Đã hết 15 phút giữ chỗ tạm thời! Ghế đã được tự động hoàn lại hệ thống.', 'error')
      bookingStore.clearBooking()
    }
  }, 1000)
}

onMounted(() => {
  startCountdown()

  if (props.eventId) {
    unsubscribeStream = seatHoldService.subscribeSeatStream(props.eventId, (update) => {
      props.seats.forEach((s) => {
        if (update.seatIds.includes(s.id)) {
          if (update.action === 'HELD') {
            if (!isSeatSelected(s)) {
              s.status = 'HELD'
            }
          } else if (update.action === 'RELEASED') {
            if (!isSeatSelected(s)) {
              s.status = 'AVAILABLE'
            }
          } else if (update.action === 'SOLD') {
            s.status = 'SOLD'
          }
        }
      })
    })
  }
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (unsubscribeStream) unsubscribeStream()
})

const ZONE_META: Record<SeatZone, { label: string; dot: string; seat: string; chip: string }> = {
  VIP: {
    label: 'VIP',
    dot: 'bg-amber-400',
    seat: 'bg-amber-50 border-amber-300 text-amber-900 hover:bg-amber-400 hover:text-slate-950',
    chip: 'bg-amber-50 text-amber-800 border-amber-200',
  },
  STANDARD: {
    label: 'Standard',
    dot: 'bg-indigo-500',
    seat: 'bg-indigo-50 border-indigo-200 text-indigo-900 hover:bg-indigo-600 hover:text-white',
    chip: 'bg-indigo-50 text-indigo-800 border-indigo-200',
  },
  BALCONY: {
    label: 'Balcony',
    dot: 'bg-emerald-500',
    seat: 'bg-emerald-50 border-emerald-300 text-emerald-900 hover:bg-emerald-500 hover:text-white',
    chip: 'bg-emerald-50 text-emerald-800 border-emerald-200',
  },
} as Record<SeatZone, { label: string; dot: string; seat: string; chip: string }>

const zoneMeta = (zone: SeatZone) => ZONE_META[zone] || ZONE_META.STANDARD

// Group seats by row
const groupedRows = computed(() => {
  const map: Record<string, EventSeat[]> = {}
  props.seats.forEach((seat) => {
    const row = seat.rowName || seat.seatRow || 'A'
    if (!map[row]) map[row] = []
    map[row].push(seat)
  })
  Object.keys(map).forEach((row) => map[row].sort((a, b) => a.seatNumber - b.seatNumber))
  return Object.keys(map)
    .sort()
    .map((row) => ({ row, seats: map[row] }))
})

// Zone summary: rows, price, availability
const zoneStats = computed(() => {
  const stats: Record<string, { zone: SeatZone; rows: Set<string>; price: number; available: number; total: number }> = {}
  props.seats.forEach((s) => {
    const row = s.rowName || s.seatRow || 'A'
    if (!stats[s.zone]) stats[s.zone] = { zone: s.zone, rows: new Set(), price: s.price, available: 0, total: 0 }
    const st = stats[s.zone]
    st.rows.add(row)
    st.total++
    if (s.status === 'AVAILABLE') st.available++
    st.price = Math.min(st.price, s.price)
  })
  return Object.values(stats)
    .map((s) => {
      const rows = [...s.rows].sort()
      return {
        ...s,
        rowRange: rows.length > 1 ? `${rows[0]}–${rows[rows.length - 1]}` : rows[0],
        percent: Math.round((s.available / s.total) * 100),
      }
    })
    .sort((a, b) => b.price - a.price)
})

const totalAvailable = computed(() => props.seats.filter((s) => s.status === 'AVAILABLE').length)

const middleIndex = computed(() => {
  const maxLen = Math.max(0, ...groupedRows.value.map((r) => r.seats.length))
  return Math.floor(maxLen / 2)
})

const isSeatSelected = (seat: EventSeat) => bookingStore.selectedSeats.some((s) => s.id === seat.id)
const isDimmed = (seat: EventSeat) => zoneFilter.value !== 'ALL' && seat.zone !== zoneFilter.value

const handleSeatClick = async (seat: EventSeat) => {
  try {
    const wasSelected = isSeatSelected(seat)
    await bookingStore.toggleSeat(seat)
    if (!wasSelected) {
      emit('notify', `Đã chọn và tạm giữ ghế ${seat.rowName || seat.seatRow}${seat.seatNumber}`, 'success')
      startCountdown()
    }
  } catch (err: any) {
    // If backend rejects due to race condition / hold by another, update local status
    seat.status = 'HELD'
    emit('notify', err.message || 'Ghế này vừa được người khác giữ chỗ!', 'error')
  }
}

const clearSeats = async () => {
  await bookingStore.clearBooking(true)
}

const setZoom = (delta: number) => {
  zoom.value = Math.min(1.4, Math.max(0.7, +(zoom.value + delta).toFixed(2)))
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
</script>

<template>
  <div class="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
    <!-- Header -->
    <div class="px-5 sm:px-7 pt-6 pb-5 border-b border-slate-100 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
      <div>
        <h3 class="text-lg font-black text-slate-900 flex items-center gap-2">
          <Armchair class="w-5 h-5 text-indigo-600" />
          Chọn vị trí ngồi thời gian thực
        </h3>
        <p class="text-xs text-slate-500 mt-0.5">
          Còn <strong class="text-emerald-600 font-bold">{{ totalAvailable }}</strong> / {{ seats.length }} ghế trống •
          Tối đa {{ MAX_SEATS }} ghế mỗi đơn • Tạm giữ tự động 15 phút
        </p>
      </div>

      <!-- Zone filter -->
      <div class="flex flex-wrap items-center gap-2">
        <button
          id="seat-zone-all"
          type="button"
          @click="zoneFilter = 'ALL'"
          class="px-3 py-1.5 rounded-full text-xs font-bold border transition-all"
          :class="zoneFilter === 'ALL' ? 'bg-slate-900 text-white border-slate-900' : 'bg-white text-slate-600 border-slate-200 hover:border-slate-400'"
        >
          Tất cả
        </button>
        <button
          v-for="z in zoneStats"
          :id="`seat-zone-${z.zone.toLowerCase()}`"
          :key="z.zone"
          type="button"
          @click="zoneFilter = zoneFilter === z.zone ? 'ALL' : z.zone"
          class="px-3 py-1.5 rounded-full text-xs font-bold border transition-all flex items-center gap-1.5"
          :class="zoneFilter === z.zone ? 'bg-slate-900 text-white border-slate-900' : 'bg-white text-slate-600 border-slate-200 hover:border-slate-400'"
        >
          <span class="w-2 h-2 rounded-full" :class="zoneMeta(z.zone).dot"></span>
          {{ zoneMeta(z.zone).label }}
        </button>
      </div>
    </div>

    <!-- Real-time Hold Countdown Alert -->
    <div
      v-if="bookingStore.selectedSeats.length > 0 && remainingTime"
      class="mx-5 sm:mx-7 mt-4 p-3.5 rounded-2xl border flex items-center justify-between gap-3 text-xs transition-all shadow-sm"
      :class="remainingSeconds < 120 ? 'bg-rose-50 border-rose-200 text-rose-800' : 'bg-amber-50 border-amber-200 text-amber-900'"
    >
      <div class="flex items-center gap-2.5">
        <Clock class="w-4 h-4 shrink-0" :class="remainingSeconds < 120 ? 'text-rose-600 animate-bounce' : 'text-amber-600 animate-pulse'" />
        <span>
          Đang tạm giữ an toàn <strong>{{ bookingStore.selectedSeats.length }}</strong> ghế cho bạn. Vui lòng hoàn tất trong thời gian giữ chỗ!
        </span>
      </div>
      <div
        class="flex items-center gap-1.5 font-mono font-black text-sm px-3 py-1 rounded-xl bg-white border shrink-0 shadow-sm"
        :class="remainingSeconds < 120 ? 'text-rose-600 border-rose-300' : 'text-amber-700 border-amber-300'"
      >
        <Clock class="w-3.5 h-3.5" />
        {{ remainingTime }}
      </div>
    </div>

    <!-- Zone price cards -->
    <div class="px-5 sm:px-7 pt-5 grid grid-cols-1 sm:grid-cols-3 gap-3">
      <button
        v-for="z in zoneStats"
        :key="z.zone"
        type="button"
        @click="zoneFilter = zoneFilter === z.zone ? 'ALL' : z.zone"
        class="text-left p-3.5 rounded-2xl border transition-all hover:shadow-md"
        :class="zoneFilter === z.zone ? 'border-indigo-500 ring-2 ring-indigo-100 bg-indigo-50/40' : 'border-slate-200 bg-slate-50/60'"
      >
        <div class="flex items-center justify-between mb-1.5">
          <span class="flex items-center gap-1.5 text-xs font-black text-slate-800">
            <span class="w-2.5 h-2.5 rounded-full" :class="zoneMeta(z.zone).dot"></span>
            {{ zoneMeta(z.zone).label }}
            <span class="font-semibold text-slate-400">• Hàng {{ z.rowRange }}</span>
          </span>
          <span class="text-[11px] font-bold" :class="z.percent < 30 ? 'text-rose-600' : 'text-emerald-600'">
            {{ z.available }} trống
          </span>
        </div>
        <div class="text-base font-black text-rose-600">{{ formatCurrency(z.price) }}</div>
        <div class="mt-2 h-1.5 rounded-full bg-slate-200 overflow-hidden">
          <div class="h-full rounded-full" :class="zoneMeta(z.zone).dot" :style="{ width: `${z.percent}%` }"></div>
        </div>
      </button>
    </div>

    <!-- Map canvas -->
    <div class="relative mx-5 sm:mx-7 mt-5 rounded-2xl bg-gradient-to-b from-slate-50 to-white border border-slate-200">
      <!-- Zoom controls -->
      <div class="absolute top-3 right-3 z-20 flex flex-col gap-1 bg-white/90 backdrop-blur rounded-xl border border-slate-200 shadow-sm p-1">
        <button id="seat-zoom-in" type="button" @click="setZoom(0.1)" class="w-7 h-7 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-700" title="Phóng to">
          <ZoomIn class="w-4 h-4" />
        </button>
        <button id="seat-zoom-out" type="button" @click="setZoom(-0.1)" class="w-7 h-7 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-700" title="Thu nhỏ">
          <ZoomOut class="w-4 h-4" />
        </button>
        <button id="seat-zoom-reset" type="button" @click="zoom = 1" class="w-7 h-7 rounded-lg hover:bg-slate-100 flex items-center justify-center text-slate-700" title="Đặt lại">
          <RotateCcw class="w-3.5 h-3.5" />
        </button>
      </div>

      <div class="overflow-auto py-8 px-4">
        <div class="min-w-[620px] origin-top transition-transform duration-300" :style="{ transform: `scale(${zoom})` }">
          <!-- Stage -->
          <div class="max-w-xl mx-auto mb-10 text-center">
            <div class="relative h-14 mx-auto w-full rounded-b-[100%] bg-gradient-to-b from-indigo-600 to-violet-500 shadow-lg shadow-indigo-500/30 flex items-center justify-center">
              <span class="text-[11px] font-black uppercase tracking-[0.3em] text-white/95">Sân khấu chính</span>
            </div>
            <div class="h-6 w-3/4 mx-auto bg-gradient-to-b from-indigo-200/50 to-transparent blur-md"></div>
          </div>

          <!-- Seat grid -->
          <div class="flex flex-col items-center gap-2">
            <div v-for="r in groupedRows" :key="r.row" class="flex items-center gap-2">
              <span class="w-6 text-center text-xs font-black text-slate-400 shrink-0">{{ r.row }}</span>

              <div class="flex items-center gap-1.5 sm:gap-2">
                <template v-for="(seat, idx) in r.seats" :key="seat.id">
                  <div v-if="idx === middleIndex" class="w-6 sm:w-10 shrink-0"></div>

                  <button
                    type="button"
                    :id="`seat-${seat.rowName || seat.seatRow}${seat.seatNumber}`"
                    :disabled="seat.status !== 'AVAILABLE' && !isSeatSelected(seat)"
                    @click="handleSeatClick(seat)"
                    :aria-label="`Ghế ${seat.rowName || seat.seatRow}${seat.seatNumber} ${seat.zone} ${formatCurrency(seat.price)}`"
                    class="w-8 h-8 rounded-t-xl rounded-b-md text-[11px] font-bold flex items-center justify-center transition-all relative group border"
                    :class="[
                      isSeatSelected(seat)
                        ? 'bg-indigo-600 border-indigo-600 text-white shadow-lg shadow-indigo-500/40 ring-2 ring-indigo-300 scale-110 z-10 font-black'
                        : seat.status === 'SOLD' || seat.status === 'BLOCKED'
                        ? 'bg-slate-200 text-slate-400 cursor-not-allowed border-slate-300'
                        : seat.status === 'HELD'
                        ? 'bg-amber-100 text-amber-800 cursor-not-allowed border-amber-300 font-bold shadow-sm'
                        : `${zoneMeta(seat.zone).seat} hover:scale-110 hover:-translate-y-0.5`,
                      isDimmed(seat) && !isSeatSelected(seat) ? 'opacity-25' : '',
                    ]"
                  >
                    <Lock v-if="seat.status === 'SOLD' || seat.status === 'BLOCKED'" class="w-3 h-3" />
                    <Clock v-else-if="seat.status === 'HELD' && !isSeatSelected(seat)" class="w-3 h-3 text-amber-600 animate-pulse" />
                    <Check v-else-if="isSeatSelected(seat)" class="w-3.5 h-3.5 text-white" />
                    <span v-else>{{ seat.seatNumber }}</span>

                    <!-- Tooltip -->
                    <div
                      v-if="seat.status === 'AVAILABLE' || isSeatSelected(seat)"
                      class="hidden group-hover:block absolute bottom-full mb-2 left-1/2 -translate-x-1/2 px-3 py-2 bg-slate-900 text-white rounded-xl shadow-xl whitespace-nowrap z-30 pointer-events-none text-left"
                    >
                      <div class="text-[11px] font-black">Ghế {{ seat.rowName || seat.seatRow }}{{ seat.seatNumber }} • {{ zoneMeta(seat.zone).label }}</div>
                      <div class="text-[11px] text-amber-300 font-bold">{{ formatCurrency(seat.price) }}</div>
                      <div v-if="isSeatSelected(seat)" class="text-[10px] text-emerald-300 mt-0.5 font-semibold">✔ Đang tạm giữ cho bạn</div>
                    </div>
                    <div
                      v-else-if="seat.status === 'HELD'"
                      class="hidden group-hover:block absolute bottom-full mb-2 left-1/2 -translate-x-1/2 px-3 py-1.5 bg-amber-900 text-amber-100 rounded-xl shadow-xl whitespace-nowrap z-30 pointer-events-none text-left text-[11px] font-semibold"
                    >
                      Đang được khách hàng khác tạm giữ
                    </div>
                  </button>
                </template>
              </div>

              <span class="w-6 text-center text-xs font-black text-slate-400 shrink-0">{{ r.row }}</span>
            </div>
          </div>

          <!-- Entrance -->
          <div class="mt-8 flex justify-center gap-24 text-[10px] font-bold uppercase tracking-widest text-slate-400">
            <span>⬆ Lối vào A</span>
            <span>⬆ Lối vào B</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Legend -->
    <div class="px-5 sm:px-7 py-4 flex flex-wrap items-center justify-center gap-x-5 gap-y-2 text-xs">
      <div class="flex items-center gap-2">
        <span class="w-4 h-4 rounded-t-md rounded-b-sm bg-white border border-slate-300"></span>
        <span class="text-slate-600 font-medium">Còn trống</span>
      </div>
      <div class="flex items-center gap-2">
        <span class="w-4 h-4 rounded-t-md rounded-b-sm bg-indigo-600 ring-2 ring-indigo-300 flex items-center justify-center">
          <Check class="w-2.5 h-2.5 text-white" />
        </span>
        <span class="text-slate-900 font-bold">Đang chọn (Tạm giữ cho bạn)</span>
      </div>
      <div class="flex items-center gap-2">
        <span class="w-4 h-4 rounded-t-md rounded-b-sm bg-amber-100 border border-amber-300 flex items-center justify-center">
          <Clock class="w-2.5 h-2.5 text-amber-600" />
        </span>
        <span class="text-amber-800 font-bold">Người khác đang tạm giữ</span>
      </div>
      <div class="flex items-center gap-2">
        <span class="w-4 h-4 rounded-t-md rounded-b-sm bg-slate-200 border border-slate-300 flex items-center justify-center">
          <Lock class="w-2.5 h-2.5 text-slate-400" />
        </span>
        <span class="text-slate-500">Đã bán</span>
      </div>
    </div>

    <!-- Selection Bar -->
    <transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0 translate-y-2"
      leave-active-class="transition duration-200 ease-in"
      leave-to-class="opacity-0 translate-y-2"
    >
      <div
        v-if="bookingStore.selectedSeats.length > 0"
        class="mx-5 sm:mx-7 mb-6 p-4 rounded-2xl border border-indigo-200 bg-gradient-to-r from-indigo-50 to-violet-50 flex flex-col sm:flex-row sm:items-center justify-between gap-4"
      >
        <div class="min-w-0">
          <p class="text-xs text-slate-600 font-semibold mb-2">
            Ghế đã chọn & tạm giữ ({{ bookingStore.selectedSeats.length }}/{{ MAX_SEATS }})
          </p>
          <div class="flex flex-wrap gap-1.5">
            <span
              v-for="s in bookingStore.selectedSeats"
              :key="s.id"
              class="pl-2.5 pr-1 py-1 rounded-lg border font-bold text-xs flex items-center gap-1 bg-white"
              :class="zoneMeta(s.zone).chip"
            >
              {{ s.rowName || s.seatRow }}{{ s.seatNumber }}
              <span class="text-[10px] font-semibold opacity-70">{{ zoneMeta(s.zone).label }}</span>
              <button
                type="button"
                @click="handleSeatClick(s)"
                class="w-4 h-4 rounded hover:bg-slate-200 flex items-center justify-center"
                :aria-label="`Bỏ chọn ghế ${s.rowName || s.seatRow}${s.seatNumber}`"
              >
                <X class="w-3 h-3" />
              </button>
            </span>
          </div>
        </div>

        <div class="flex items-center gap-4 shrink-0">
          <button
            id="seat-clear-all"
            type="button"
            @click="clearSeats"
            class="text-xs font-bold text-slate-500 hover:text-rose-600 flex items-center gap-1"
          >
            <Trash2 class="w-3.5 h-3.5" /> Bỏ chọn
          </button>
          <div class="text-right">
            <span class="text-[11px] text-slate-500 block">Tổng tiền ghế</span>
            <span class="text-xl font-black text-rose-600">{{ formatCurrency(bookingStore.seatSubtotal) }}</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>
