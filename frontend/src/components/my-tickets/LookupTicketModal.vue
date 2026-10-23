<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ticket } from '@/types'
import {
  X,
  Search,
  Ticket as TicketIcon,
  Calendar,
  MapPin,
  CheckCircle,
  AlertCircle,
  ArrowRight,
  ShieldCheck,
  QrCode,
} from 'lucide-vue-next'

const props = defineProps<{
  tickets: Ticket[]
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'openDetail', ticket: Ticket): void
  (e: 'openQr', ticket: Ticket): void
}>()

const query = ref('')
const hasSearched = ref(false)

const searchResults = computed(() => {
  if (!query.value.trim()) return []
  const q = query.value.toLowerCase().trim()
  return props.tickets.filter((t) =>
    t.ticketCode.toLowerCase().includes(q) ||
    t.bookingCode.toLowerCase().includes(q) ||
    t.attendeeName.toLowerCase().includes(q) ||
    t.eventTitle.toLowerCase().includes(q)
  )
})

const handleSearch = () => {
  hasSearched.value = true
}

const formatDate = (dateStr: string) => {
  return new Intl.DateTimeFormat('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(dateStr))
}

const handleClose = () => {
  query.value = ''
  hasSearched.value = false
  emit('close')
}
</script>

<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in"
    @click.self="handleClose"
  >
    <div class="relative w-full max-w-2xl bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200 max-h-[90vh] flex flex-col">
      <!-- Header -->
      <div class="px-6 py-5 bg-gradient-to-r from-slate-900 via-indigo-950 to-indigo-900 text-white flex items-center justify-between shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center">
            <Search class="w-5 h-5 text-indigo-400" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-black text-white">
              Tra Cứu Vé & Đơn Hàng Nhanh
            </h3>
            <p class="text-xs text-slate-300">
              Nhập mã vé (EVH-...), mã đơn hàng hoặc tên người tham dự
            </p>
          </div>
        </div>
        <button
          @click="handleClose"
          class="w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors"
        >
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Search Input Box -->
      <div class="p-6 border-b border-slate-100 bg-slate-50/50">
        <form @submit.prevent="handleSearch" class="flex gap-2">
          <div class="relative flex-1">
            <Search class="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              v-model="query"
              type="text"
              placeholder="VD: EVH-202611-TK88912 hoặc #EVH-20261115-4921..."
              class="w-full pl-10 pr-4 py-3 rounded-2xl border border-slate-200 bg-white text-xs sm:text-sm font-medium focus:outline-none focus:ring-2 focus:ring-indigo-500 shadow-sm"
              autofocus
            />
          </div>
          <button
            type="submit"
            class="px-5 py-3 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-md transition shrink-0"
          >
            Tra Cứu
          </button>
        </form>

        <div class="flex flex-wrap items-center gap-2 mt-3 text-[11px] text-slate-500">
          <span class="font-bold text-slate-400">Gợi ý tìm nhanh:</span>
          <button
            type="button"
            @click="query = 'TK88912'; handleSearch()"
            class="px-2 py-0.5 rounded-lg bg-white border border-slate-200 hover:border-indigo-400 font-mono text-indigo-600"
          >
            TK88912
          </button>
          <button
            type="button"
            @click="query = 'Tech Summit'; handleSearch()"
            class="px-2 py-0.5 rounded-lg bg-white border border-slate-200 hover:border-indigo-400 text-slate-700"
          >
            Tech Summit
          </button>
          <button
            type="button"
            @click="query = 'Nguyễn Văn Quân'; handleSearch()"
            class="px-2 py-0.5 rounded-lg bg-white border border-slate-200 hover:border-indigo-400 text-slate-700"
          >
            Nguyễn Văn Quân
          </button>
        </div>
      </div>

      <!-- Results Area -->
      <div class="p-6 overflow-y-auto flex-1 space-y-3">
        <div v-if="query && searchResults.length === 0" class="text-center py-12 space-y-3">
          <div class="w-12 h-12 rounded-2xl bg-rose-50 text-rose-500 mx-auto flex items-center justify-center">
            <AlertCircle class="w-6 h-6" />
          </div>
          <div>
            <h4 class="text-sm font-black text-slate-800">Không tìm thấy vé phù hợp</h4>
            <p class="text-xs text-slate-500 mt-1">
              Vui lòng kiểm tra lại chính xác mã vé hoặc mã đơn hàng trên email xác nhận.
            </p>
          </div>
        </div>

        <div v-else-if="!query" class="text-center py-12 text-slate-400 text-xs">
          Vui lòng nhập từ khóa tìm kiếm để bắt đầu tra cứu.
        </div>

        <div
          v-for="ticket in searchResults"
          :key="ticket.id"
          class="p-4 rounded-2xl border border-slate-200 bg-white hover:border-indigo-300 transition-all shadow-sm flex flex-col sm:flex-row sm:items-center justify-between gap-4"
        >
          <div class="flex items-start gap-3 min-w-0">
            <img
              :src="ticket.eventBannerUrl"
              :alt="ticket.eventTitle"
              class="w-16 h-16 rounded-xl object-cover shrink-0"
            />
            <div class="min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-mono text-[11px] font-black text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-md">
                  {{ ticket.ticketCode }}
                </span>
                <span
                  class="px-2 py-0.5 rounded-md text-[10px] font-bold"
                  :class="[
                    ticket.status === 'VALID' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
                    ticket.status === 'USED' ? 'bg-slate-100 text-slate-600' : 'bg-rose-50 text-rose-700'
                  ]"
                >
                  {{ ticket.status === 'VALID' ? 'Hợp lệ' : ticket.status === 'USED' ? 'Đã tham gia' : 'Đã hủy' }}
                </span>
              </div>
              <h4 class="text-xs sm:text-sm font-black text-slate-900 truncate">
                {{ ticket.eventTitle }}
              </h4>
              <p class="text-[11px] text-slate-500 truncate">
                {{ ticket.ticketTypeName }} &bull; Ghế: {{ ticket.seatLabel || 'GA' }} &bull; Khán giả: {{ ticket.attendeeName }}
              </p>
            </div>
          </div>

          <div class="flex items-center gap-2 shrink-0 self-end sm:self-center">
            <button
              v-if="ticket.status === 'VALID'"
              type="button"
              @click="emit('openQr', ticket); handleClose()"
              class="px-3.5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm flex items-center gap-1.5"
            >
              <QrCode class="w-3.5 h-3.5" />
              <span>Mở QR</span>
            </button>
            <button
              type="button"
              @click="emit('openDetail', ticket); handleClose()"
              class="px-3.5 py-2 rounded-xl border border-slate-200 text-slate-700 hover:bg-slate-50 text-xs font-bold"
            >
              Xem chi tiết
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
