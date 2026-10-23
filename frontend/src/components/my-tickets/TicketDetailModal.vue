<script setup lang="ts">
import { computed } from 'vue'
import type { Ticket } from '@/types'
import {
  X,
  Calendar,
  MapPin,
  QrCode,
  ShieldCheck,
  User,
  Clock,
  Ticket as TicketIcon,
  Download,
  Share2,
  ExternalLink,
  CreditCard,
  DoorOpen,
  Info,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'openQr', ticket: Ticket): void
  (e: 'openTransfer', ticket: Ticket): void
  (e: 'openRefund', ticket: Ticket): void
  (e: 'openInvoice', ticket: Ticket): void
  (e: 'openReview', ticket: Ticket): void
  (e: 'downloadCalendar', ticket: Ticket): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long',
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
  if (!props.ticket) return
  try {
    await navigator.clipboard.writeText(props.ticket.ticketCode)
    emit('notify', 'Đã sao chép mã vé!', 'success')
  } catch {
    emit('notify', 'Không thể sao chép mã vé', 'error')
  }
}
</script>

<template>
  <div
    v-if="show && ticket"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-sm animate-fade-in"
    @click.self="emit('close')"
  >
    <div class="relative w-full max-w-2xl bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200 max-h-[90vh] flex flex-col">
      <!-- Modal Header Banner -->
      <div class="relative h-36 bg-slate-900 shrink-0 overflow-hidden">
        <img
          :src="ticket.eventBannerUrl"
          :alt="ticket.eventTitle"
          class="w-full h-full object-cover opacity-50 scale-105"
        />
        <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/40 to-transparent"></div>

        <button
          @click="emit('close')"
          class="absolute top-4 right-4 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors"
          aria-label="Đóng"
        >
          <X class="w-4 h-4" />
        </button>

        <div class="absolute bottom-4 left-6 right-6 flex items-end justify-between gap-4">
          <div>
            <span class="inline-block px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-indigo-600 text-white shadow-sm mb-1.5">
              {{ ticket.ticketTypeName }}
            </span>
            <h3 class="text-lg font-black text-white leading-tight drop-shadow-md truncate max-w-md">
              {{ ticket.eventTitle }}
            </h3>
          </div>
          <span
            v-if="ticket.status === 'VALID'"
            class="px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-500/90 text-white shadow flex items-center gap-1 shrink-0"
          >
            <ShieldCheck class="w-3.5 h-3.5" /> Hợp Lệ
          </span>
          <span
            v-else-if="ticket.status === 'USED'"
            class="px-2.5 py-1 rounded-full text-xs font-bold bg-slate-500 text-white shadow shrink-0"
          >
            Đã Sử Dụng
          </span>
          <span
            v-else
            class="px-2.5 py-1 rounded-full text-xs font-bold bg-rose-500 text-white shadow shrink-0"
          >
            Đã Hủy
          </span>
        </div>
      </div>

      <!-- Scrollable Body -->
      <div class="p-6 overflow-y-auto space-y-6">
        <!-- Event Time & Venue -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4 p-4 rounded-2xl bg-slate-50 border border-slate-200">
          <div class="flex items-start gap-3">
            <div class="w-9 h-9 rounded-xl bg-indigo-100 flex items-center justify-center shrink-0">
              <Calendar class="w-4 h-4 text-indigo-600" />
            </div>
            <div>
              <span class="text-[10px] uppercase font-bold text-slate-400 block">Thời gian diễn ra</span>
              <span class="text-xs font-bold text-slate-900 block capitalize">{{ formatDate(ticket.eventStartDateTime) }}</span>
            </div>
          </div>

          <div class="flex items-start gap-3">
            <div class="w-9 h-9 rounded-xl bg-rose-100 flex items-center justify-center shrink-0">
              <MapPin class="w-4 h-4 text-rose-600" />
            </div>
            <div>
              <span class="text-[10px] uppercase font-bold text-slate-400 block">Địa điểm</span>
              <span class="text-xs font-bold text-slate-900 block">{{ ticket.venueName }}</span>
            </div>
          </div>
        </div>

        <!-- Ticket Specifications Matrix -->
        <div>
          <h4 class="text-xs font-black uppercase tracking-wider text-slate-400 mb-3">Thông Tin Chi Tiết Vé</h4>
          <div class="grid grid-cols-2 sm:grid-cols-3 gap-3">
            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Vị trí chỗ ngồi</span>
              <span class="text-sm font-black text-amber-900 bg-amber-50 px-2 py-0.5 rounded border border-amber-200 inline-block mt-0.5">
                {{ ticket.seatLabel || 'Vé Tự Do (GA)' }}
              </span>
            </div>

            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Cổng soát vé</span>
              <span class="text-sm font-bold text-slate-900 flex items-center gap-1 mt-0.5">
                <DoorOpen class="w-3.5 h-3.5 text-indigo-600" />
                {{ ticket.gate || 'Cổng Chính' }}
              </span>
            </div>

            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Giá vé</span>
              <span class="text-sm font-black text-rose-600 mt-0.5 block">
                {{ formatCurrency(ticket.price) }}
              </span>
            </div>

            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Khán giả sở hữu</span>
              <span class="text-xs font-bold text-slate-900 mt-0.5 flex items-center gap-1">
                <User class="w-3.5 h-3.5 text-slate-400" />
                {{ ticket.attendeeName }}
              </span>
            </div>

            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Mã đơn hàng</span>
              <span class="text-xs font-mono font-bold text-slate-700 mt-0.5 block">
                {{ ticket.bookingCode }}
              </span>
            </div>

            <div class="p-3 rounded-xl border border-slate-200 bg-white">
              <span class="text-[10px] text-slate-400 uppercase font-bold block">Mã vé điện tử</span>
              <div class="flex items-center justify-between gap-1 mt-0.5">
                <span class="text-xs font-mono font-black text-indigo-600 truncate">
                  {{ ticket.ticketCode }}
                </span>
                <button
                  type="button"
                  @click="copyTicketCode"
                  class="text-[10px] font-bold text-indigo-700 hover:underline"
                >
                  Sao chép
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Checkin Details or Cancellation Info -->
        <div v-if="ticket.checkInAt" class="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-xs">
          <div class="flex items-center gap-2 font-bold text-emerald-800 mb-1">
            <ShieldCheck class="w-4 h-4 text-emerald-600" />
            <span>Đã check-in thành công qua cổng soát vé</span>
          </div>
          <p class="text-slate-600">Thời gian quét mã: {{ formatDate(ticket.checkInAt) }} tại {{ ticket.gate || 'Cổng kiểm soát' }}.</p>
        </div>

        <div v-if="ticket.cancelReason" class="p-4 rounded-2xl bg-rose-50 border border-rose-200 text-xs">
          <div class="font-bold text-rose-800 mb-1">Thông tin hủy / hoàn vé:</div>
          <p class="text-slate-700">{{ ticket.cancelReason }}</p>
        </div>

        <!-- Check-in Guide & Important Rules -->
        <div class="p-4 rounded-2xl bg-indigo-50/60 border border-indigo-100 space-y-2 text-xs">
          <div class="font-bold text-indigo-900 flex items-center gap-1.5">
            <Info class="w-4 h-4 text-indigo-600" />
            Lưu Ý Quan Trọng Khi Đến Sự Kiện
          </div>
          <ul class="space-y-1 text-slate-600 list-disc pl-4 leading-relaxed">
            <li>Mang theo <strong>CCCD / VNeID</strong> trùng tên với vé để đối chiếu khi có yêu cầu.</li>
            <li>Xuất trình mã <strong>QR Động</strong> trên app hoặc web trước khi bước vào cổng soát vé.</li>
            <li>Vui lòng có mặt trước giờ biểu diễn tối thiểu <strong>45 phút</strong> để làm thủ tục check-in thuận lợi.</li>
            <li>Mỗi mã QR chỉ quét được 01 lần tại cổng (Cơ chế Anti-Passback bảo mật cao).</li>
          </ul>
        </div>
      </div>

      <!-- Modal Footer Actions -->
      <div class="px-6 py-4 bg-slate-50 border-t border-slate-200 flex flex-wrap items-center justify-between gap-3 shrink-0">
        <div class="flex items-center gap-2">
          <button
            type="button"
            @click="emit('downloadCalendar', ticket)"
            class="px-3 py-2 rounded-xl border border-slate-300 text-slate-700 hover:bg-slate-100 text-xs font-bold flex items-center gap-1.5 transition"
          >
            <Calendar class="w-3.5 h-3.5" /> Thêm Lịch
          </button>

          <button
            v-if="ticket.status === 'VALID'"
            type="button"
            @click="emit('openTransfer', ticket)"
            class="px-3 py-2 rounded-xl border border-slate-300 text-slate-700 hover:bg-slate-100 text-xs font-bold flex items-center gap-1.5 transition"
          >
            Chuyển Nhượng
          </button>

          <button
            v-if="ticket.status === 'VALID'"
            type="button"
            @click="emit('openRefund', ticket)"
            class="px-3 py-2 rounded-xl text-rose-600 hover:bg-rose-50 text-xs font-bold transition"
          >
            Yêu Cầu Hoàn Vé
          </button>
        </div>

        <button
          v-if="ticket.status === 'VALID'"
          type="button"
          @click="emit('openQr', ticket)"
          class="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-black shadow-md flex items-center gap-2 transition"
        >
          <QrCode class="w-4 h-4" /> Mở Mã QR Check-in
        </button>
      </div>
    </div>
  </div>
</template>
