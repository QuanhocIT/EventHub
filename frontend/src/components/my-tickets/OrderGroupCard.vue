<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ticket } from '@/types'
import {
  Calendar,
  MapPin,
  ChevronDown,
  ChevronUp,
  Receipt,
  Ticket as TicketIcon,
  CreditCard,
  QrCode,
  Download,
  Share2,
  FileText,
  Clock,
  ShieldCheck,
} from 'lucide-vue-next'
import TicketItem from './TicketItem.vue'

const props = defineProps<{
  bookingCode: string
  tickets: Ticket[]
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

const isExpanded = ref(true)

const firstTicket = computed(() => props.tickets[0])

const totalAmount = computed(() => {
  return props.tickets.reduce((sum, t) => sum + (t.price || 0), 0)
})

const orderStatus = computed(() => {
  const hasValid = props.tickets.some((t) => t.status === 'VALID')
  const hasUsed = props.tickets.some((t) => t.status === 'USED')
  const allCancelled = props.tickets.every((t) => t.status === 'CANCELLED')

  if (allCancelled) return 'CANCELLED'
  if (hasValid) return 'VALID'
  if (hasUsed) return 'USED'
  return 'COMPLETED'
})

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateStr))
}

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount)
}

const downloadAllPdf = () => {
  emit('notify', `Đang xuất tập tin PDF toàn bộ ${props.tickets.length} vé của đơn hàng #${props.bookingCode}...`, 'success')
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-slate-200/90 shadow-xs overflow-hidden transition-all duration-300">
    <!-- Order Header Bar (Refined & Clean) -->
    <div
      class="p-4 sm:p-5 bg-slate-50/80 hover:bg-slate-100/60 border-b border-slate-200/80 text-slate-800 flex flex-col md:flex-row md:items-center justify-between gap-3 cursor-pointer select-none transition-colors"
      @click="isExpanded = !isExpanded"
    >
      <div class="flex items-center gap-3.5">
        <div class="w-10 h-10 rounded-xl bg-indigo-50 border border-indigo-100 flex items-center justify-center shrink-0">
          <Receipt class="w-5 h-5 text-indigo-600" />
        </div>
        <div>
          <div class="flex flex-wrap items-center gap-2 mb-0.5">
            <span class="font-mono text-xs sm:text-sm font-bold text-slate-900 tracking-wide">
              #{{ bookingCode }}
            </span>
            <span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-white text-slate-600 border border-slate-200 shadow-2xs">
              {{ tickets.length }} vé trong đơn
            </span>
            <span
              class="px-2 py-0.5 rounded-md text-[10px] font-semibold"
              :class="[
                orderStatus === 'VALID' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
                orderStatus === 'USED' ? 'bg-slate-100 text-slate-600 border border-slate-200' :
                'bg-rose-50 text-rose-700 border border-rose-200'
              ]"
            >
              {{ orderStatus === 'VALID' ? 'Đã thanh toán' : orderStatus === 'USED' ? 'Đã tham gia' : 'Đã hoàn tất hủy' }}
            </span>
          </div>

          <h3 class="text-xs sm:text-sm font-bold text-slate-900 line-clamp-1">
            {{ firstTicket?.eventTitle }}
          </h3>
          <p class="text-[11px] text-slate-400 mt-0.5">
            Ngày đặt: {{ formatDate(firstTicket?.orderDate) }} &bull; {{ firstTicket?.venueName }}
          </p>
        </div>
      </div>

      <!-- Right summary & expand toggle -->
      <div class="flex items-center justify-between md:justify-end gap-4 shrink-0 pt-2 md:pt-0 border-t md:border-t-0 border-slate-200/60">
        <div class="text-left md:text-right">
          <div class="text-[10px] text-slate-400 uppercase font-semibold">Tổng thanh toán</div>
          <div class="text-base sm:text-lg font-bold text-slate-900">
            {{ formatCurrency(totalAmount) }}
          </div>
        </div>

        <div class="flex items-center gap-1.5">
          <button
            type="button"
            @click.stop="downloadAllPdf"
            class="px-3 py-1.5 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-700 text-xs font-semibold flex items-center gap-1.5 transition shadow-2xs"
            title="Tải tất cả vé PDF"
          >
            <Download class="w-3.5 h-3.5 text-slate-500" />
            <span class="hidden sm:inline">Tải PDF</span>
          </button>

          <button
            type="button"
            class="w-8 h-8 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 flex items-center justify-center transition shadow-2xs"
          >
            <ChevronUp v-if="isExpanded" class="w-4 h-4" />
            <ChevronDown v-else class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>

    <!-- Collapsible Tickets Container -->
    <div v-show="isExpanded" class="p-4 sm:p-5 bg-white space-y-3.5">
      <TicketItem
        v-for="ticket in tickets"
        :key="ticket.id"
        :ticket="ticket"
        @open-qr="emit('openQr', $event)"
        @open-detail="emit('openDetail', $event)"
        @open-transfer="emit('openTransfer', $event)"
        @open-refund="emit('openRefund', $event)"
        @open-invoice="emit('openInvoice', $event)"
        @open-review="emit('openReview', $event)"
        @download-calendar="emit('downloadCalendar', $event)"
        @notify="emit('notify', $event)"
      />
    </div>
  </div>
</template>
