<script setup lang="ts">
import { computed } from 'vue'
import type { Ticket } from '@/types'
import {
  Calendar,
  MapPin,
  QrCode,
  CheckCircle,
  Copy,
  DoorOpen,
  ArrowRightLeft,
  RotateCcw,
  CalendarPlus,
  Eye,
  Download,
  AlertCircle,
  ShieldCheck,
  Tag,
  FileText,
  Star,
  ExternalLink,
  Navigation,
  Sparkles,
  Clock,
  ArrowUpRight,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket
}>()

const emit = defineEmits<{
  (e: 'openQr', ticket: Ticket): void
  (e: 'openDetail', ticket: Ticket): void
  (e: 'openTransfer', ticket: Ticket): void
  (e: 'openRefund', ticket: Ticket): void
  (e: 'openInvoice', ticket: Ticket): void
  (e: 'openReview', ticket: Ticket): void
  (e: 'downloadCalendar', ticket: Ticket): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const formatDate = (dateStr: string) => {
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateStr))
}

const formatCurrency = (amount?: number) => {
  if (!amount) return 'Miễn phí'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

const copyTicketCode = async () => {
  try {
    await navigator.clipboard.writeText(props.ticket.ticketCode)
    emit('notify', `Đã sao chép mã vé: ${props.ticket.ticketCode}`, 'success')
  } catch {
    emit('notify', 'Không thể sao chép mã vé', 'error')
  }
}

// Countdown calculation
const daysLeft = computed(() => {
  const diff = new Date(props.ticket.eventStartDateTime).getTime() - Date.now()
  if (diff <= 0) return 0
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
})

const isUpcoming = computed(() => {
  return new Date(props.ticket.eventStartDateTime).getTime() > Date.now() && props.ticket.status === 'VALID'
})

const isToday = computed(() => {
  const d = new Date(props.ticket.eventStartDateTime)
  const now = new Date()
  return (
    d.getDate() === now.getDate() &&
    d.getMonth() === now.getMonth() &&
    d.getFullYear() === now.getFullYear()
  )
})

const openGoogleMaps = () => {
  if (props.ticket.googleMapsUrl) {
    window.open(props.ticket.googleMapsUrl, '_blank')
  } else {
    window.open(`https://maps.google.com/?q=${encodeURIComponent(props.ticket.venueName)}`, '_blank')
  }
}

const simulateWallet = (type: 'Apple' | 'Google') => {
  emit('notify', `Đã lưu vé ${props.ticket.ticketCode} vào ${type} Wallet!`, 'success')
}
</script>

<template>
  <div
    class="relative bg-white rounded-2xl overflow-hidden border transition-all duration-300 flex flex-col md:flex-row group shadow-[0_2px_10px_rgba(0,0,0,0.03)] hover:shadow-[0_10px_30px_rgba(0,0,0,0.08)]"
    :class="[
      ticket.status === 'VALID' ? 'border-slate-200 hover:border-indigo-300' : '',
      ticket.status === 'USED' ? 'border-slate-200/80 bg-slate-50/30' : '',
      ticket.status === 'CANCELLED' ? 'border-rose-200/80 bg-rose-50/15' : ''
    ]"
  >
    <!-- Left Thumbnail & Banner (Refined Proportion) -->
    <div class="md:w-64 h-48 md:h-auto relative bg-slate-900 overflow-hidden shrink-0">
      <img
        :src="ticket.eventBannerUrl"
        :alt="ticket.eventTitle"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 opacity-90"
      />
      <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent"></div>

      <!-- Category & Ticket Type Tag -->
      <div class="absolute top-3 left-3 right-3 flex items-start justify-between gap-1.5">
        <span class="px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider bg-white/95 text-slate-900 shadow-sm backdrop-blur-sm">
          {{ ticket.ticketTypeName }}
        </span>

        <span
          v-if="isUpcoming"
          class="px-2 py-0.5 rounded-lg text-[10px] font-bold shadow-sm"
          :class="isToday ? 'bg-rose-500 text-white animate-pulse' : 'bg-indigo-600 text-white'"
        >
          {{ isToday ? 'Hôm nay' : `Còn ${daysLeft} ngày` }}
        </span>
      </div>

      <!-- Bottom Code & Copy -->
      <div class="absolute bottom-3 left-3 right-3 flex items-center justify-between text-white text-xs">
        <div class="flex items-center gap-1.5 font-mono text-[11px] text-slate-200 font-semibold truncate">
          <span>{{ ticket.ticketCode }}</span>
        </div>
        <button
          type="button"
          @click.stop="copyTicketCode"
          class="p-1 rounded-md bg-white/20 hover:bg-white/30 text-white transition backdrop-blur-xs"
          title="Sao chép mã vé"
        >
          <Copy class="w-3 h-3" />
        </button>
      </div>
    </div>

    <!-- Middle Ticket Information Column -->
    <div class="p-5 sm:p-6 flex-1 flex flex-col justify-between min-w-0">
      <div>
        <!-- Date, Category & Status Row -->
        <div class="flex flex-wrap items-center justify-between gap-2 mb-2">
          <div class="flex items-center gap-2">
            <span
              v-if="ticket.categoryName"
              class="px-2 py-0.5 rounded-md text-[10px] font-bold uppercase tracking-wider bg-slate-100 text-slate-600"
            >
              {{ ticket.categoryName }}
            </span>
            <div class="flex items-center gap-1.5 text-xs text-indigo-600 font-bold">
              <Calendar class="w-3.5 h-3.5 shrink-0" />
              <span class="capitalize">{{ formatDate(ticket.eventStartDateTime) }}</span>
            </div>
          </div>

          <!-- Status Indicator Pill -->
          <div>
            <span
              v-if="ticket.status === 'VALID'"
              class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200"
            >
              <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
              Hợp Lệ &bull; Sẵn sàng vào cổng
            </span>
            <span
              v-else-if="ticket.status === 'USED'"
              class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-600 border border-slate-200"
            >
              <CheckCircle class="w-3 h-3 text-slate-500" />
              Đã tham gia
            </span>
            <span
              v-else
              class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-rose-50 text-rose-700 border border-rose-200"
            >
              <AlertCircle class="w-3 h-3 text-rose-500" />
              Đã Hủy Vé & Hoàn Tiền
            </span>
          </div>
        </div>

        <!-- Event Title -->
        <h3 class="text-base sm:text-lg font-bold text-slate-900 mb-2 leading-snug line-clamp-2">
          <router-link
            :to="`/events/${ticket.eventId}`"
            class="hover:text-indigo-600 transition-colors"
          >
            {{ ticket.eventTitle }}
          </router-link>
        </h3>

        <!-- Venue Name with Google Maps link -->
        <div class="flex items-center gap-1.5 text-xs text-slate-500 mb-4">
          <MapPin class="w-3.5 h-3.5 shrink-0 text-slate-400" />
          <span class="truncate">{{ ticket.venueName }}</span>
          <button
            type="button"
            @click="openGoogleMaps"
            class="text-indigo-600 hover:text-indigo-700 font-semibold flex items-center gap-0.5 ml-1 transition hover:underline"
            title="Mở Google Maps chỉ đường"
          >
            <Navigation class="w-3 h-3" />
            <span>Chỉ đường</span>
          </button>
        </div>

        <!-- Seat & Gate Chips Matrix -->
        <div class="flex flex-wrap items-center gap-2 mb-4 text-xs">
          <!-- Seat Label -->
          <div
            class="px-2.5 py-1 rounded-lg font-semibold text-xs flex items-center gap-1 border"
            :class="[
              ticket.seatLabel?.includes('VIP')
                ? 'bg-amber-50/80 text-amber-900 border-amber-200'
                : 'bg-indigo-50/70 text-indigo-900 border-indigo-100'
            ]"
          >
            <span class="text-slate-400 text-[10px] uppercase font-bold">Ghế:</span>
            <span>{{ ticket.seatLabel || 'Tự do (GA)' }}</span>
          </div>

          <!-- Zone -->
          <div
            v-if="ticket.zone"
            class="px-2.5 py-1 rounded-lg bg-slate-50 text-slate-700 font-semibold flex items-center gap-1 border border-slate-200"
          >
            <span class="text-slate-400 text-[10px] uppercase font-bold">Khu:</span>
            <span>{{ ticket.zone }}</span>
          </div>

          <!-- Gate -->
          <div class="px-2.5 py-1 rounded-lg bg-slate-50 text-slate-700 font-semibold flex items-center gap-1 border border-slate-200">
            <DoorOpen class="w-3 h-3 text-slate-400" />
            <span>{{ ticket.gate || 'Cổng Chính' }}</span>
          </div>

          <!-- Price -->
          <div class="px-2.5 py-1 rounded-lg bg-slate-50 text-slate-900 font-bold border border-slate-200">
            {{ formatCurrency(ticket.price) }}
          </div>
        </div>

        <!-- Cancelled Summary Box -->
        <div
          v-if="ticket.status === 'CANCELLED'"
          class="p-3 rounded-xl bg-rose-50/60 border border-rose-100 text-xs text-rose-800 mb-3 space-y-1"
        >
          <div class="font-bold flex items-center gap-1 text-[11px] text-rose-700">
            <AlertCircle class="w-3.5 h-3.5 text-rose-600" />
            <span>Thông tin hoàn tiền:</span>
          </div>
          <p class="text-slate-600 leading-relaxed">{{ ticket.cancelReason }}</p>
          <div v-if="ticket.refundAmount" class="pt-1 text-[11px] text-slate-600 flex flex-wrap gap-x-4 border-t border-rose-100">
            <span>Số tiền hoàn: <strong class="text-rose-700">{{ formatCurrency(ticket.refundAmount) }}</strong></span>
            <span v-if="ticket.refundMethod">Phương thức: <strong>{{ ticket.refundMethod }}</strong></span>
          </div>
        </div>
      </div>

      <!-- Bottom Meta & Actions Toolbar -->
      <div class="pt-3 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between text-xs text-slate-500 gap-3">
        <div class="flex items-center gap-2">
          <span>Khán giả: <strong class="text-slate-800 font-semibold">{{ ticket.attendeeName }}</strong></span>
          <span class="text-slate-300">&bull;</span>
          <span class="text-slate-400 font-mono">#{{ ticket.bookingCode }}</span>
        </div>

        <!-- Action Links (Clean, understated) -->
        <div class="flex flex-wrap items-center gap-3">
          <button
            type="button"
            @click="emit('openDetail', ticket)"
            class="font-semibold text-slate-700 hover:text-indigo-600 flex items-center gap-1 transition"
          >
            <Eye class="w-3.5 h-3.5" /> Chi tiết
          </button>

          <!-- VAT Invoice -->
          <button
            type="button"
            @click="emit('openInvoice', ticket)"
            class="font-semibold text-slate-700 hover:text-indigo-600 flex items-center gap-1 transition"
            :class="ticket.invoiceRequested ? 'text-emerald-600' : ''"
            title="Xuất hóa đơn điện tử VAT"
          >
            <FileText class="w-3.5 h-3.5" />
            <span>{{ ticket.invoiceRequested ? 'Đã yêu cầu VAT' : 'Hóa đơn VAT' }}</span>
          </button>

          <!-- Calendar -->
          <button
            v-if="ticket.status === 'VALID'"
            type="button"
            @click="emit('downloadCalendar', ticket)"
            class="font-semibold text-slate-700 hover:text-indigo-600 flex items-center gap-1 transition"
            title="Lưu vào Calendar (.ics)"
          >
            <CalendarPlus class="w-3.5 h-3.5" /> Thêm lịch
          </button>

          <!-- Transfer -->
          <button
            v-if="ticket.status === 'VALID'"
            type="button"
            @click="emit('openTransfer', ticket)"
            class="font-semibold text-slate-700 hover:text-indigo-600 flex items-center gap-1 transition"
          >
            <ArrowRightLeft class="w-3.5 h-3.5" /> Chuyển vé
          </button>

          <!-- Refund -->
          <button
            v-if="ticket.status === 'VALID'"
            type="button"
            @click="emit('openRefund', ticket)"
            class="font-semibold text-slate-500 hover:text-rose-600 flex items-center gap-1 transition"
          >
            <RotateCcw class="w-3.5 h-3.5" /> Hoàn vé
          </button>

          <!-- Review -->
          <button
            v-if="ticket.status === 'USED'"
            type="button"
            @click="emit('openReview', ticket)"
            class="font-semibold text-amber-700 hover:text-amber-800 flex items-center gap-1 transition"
          >
            <Star class="w-3.5 h-3.5 fill-amber-500 text-amber-500" />
            <span>Đánh giá & Kỷ niệm</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Perforation Dividers (desktop notch top & bottom) -->
    <div class="hidden md:block relative w-0 border-r border-dashed border-slate-200 shrink-0">
      <div class="absolute -top-2.5 -right-2.5 w-5 h-5 rounded-full bg-[#f8fafc] border border-slate-200"></div>
      <div class="absolute -bottom-2.5 -right-2.5 w-5 h-5 rounded-full bg-[#f8fafc] border border-slate-200"></div>
    </div>

    <!-- Right Column: Interactive QR Trigger & Stub -->
    <div class="p-5 md:w-56 bg-slate-50/60 border-t md:border-t-0 md:border-l border-slate-100 flex flex-col items-center justify-center gap-3 shrink-0">
      <!-- VALID: Big interactive QR Check-in button -->
      <template v-if="ticket.status === 'VALID'">
        <button
          @click="emit('openQr', ticket)"
          class="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white text-xs font-bold flex items-center justify-center gap-2 shadow-sm transition group/qr"
        >
          <QrCode class="w-4 h-4 group-hover/qr:scale-110 transition-transform" />
          <span>Mở Mã QR Check-in</span>
        </button>

        <p class="text-[11px] text-slate-400 text-center leading-tight">
          Mã QR động &bull; Quét tại cổng soát vé
        </p>

        <!-- Quick Wallet sync pills -->
        <div class="w-full flex items-center justify-center gap-1.5 pt-1 border-t border-slate-200/60">
          <button
            type="button"
            @click="simulateWallet('Apple')"
            class="text-[10px] font-semibold text-slate-600 hover:text-black hover:bg-slate-200/60 px-2 py-0.5 rounded-md transition"
            title="Lưu vào Apple Wallet"
          >
             Apple Wallet
          </button>
          <span class="text-slate-300">&bull;</span>
          <button
            type="button"
            @click="simulateWallet('Google')"
            class="text-[10px] font-semibold text-slate-600 hover:text-indigo-600 hover:bg-slate-200/60 px-2 py-0.5 rounded-md transition"
            title="Lưu vào Google Wallet"
          >
            G-Wallet
          </button>
        </div>
      </template>

      <!-- USED: View QR archive & review -->
      <template v-else-if="ticket.status === 'USED'">
        <div class="w-full text-center space-y-2">
          <div class="w-10 h-10 rounded-full bg-emerald-50 text-emerald-600 mx-auto flex items-center justify-center border border-emerald-100">
            <CheckCircle class="w-5 h-5" />
          </div>
          <div class="text-xs font-bold text-slate-700">Đã Check-in Thành Công</div>
          <button
            @click="emit('openQr', ticket)"
            class="w-full py-2 px-3 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold transition flex items-center justify-center gap-1.5"
          >
            <QrCode class="w-3.5 h-3.5 text-slate-400" />
            <span>Xem lại vé</span>
          </button>
        </div>
      </template>

      <!-- CANCELLED: Refunded state -->
      <template v-else>
        <div class="w-full text-center space-y-2">
          <div class="w-10 h-10 rounded-full bg-rose-50 text-rose-500 mx-auto flex items-center justify-center border border-rose-100">
            <RotateCcw class="w-5 h-5" />
          </div>
          <div class="text-xs font-bold text-slate-700">Vé Không Khả Dụng</div>
          <p class="text-[10px] text-slate-400">Đã xử lý hoàn tiền</p>
          <button
            @click="emit('openDetail', ticket)"
            class="text-xs font-semibold text-indigo-600 hover:underline"
          >
            Hồ sơ chi tiết &rarr;
          </button>
        </div>
      </template>
    </div>
  </div>
</template>
