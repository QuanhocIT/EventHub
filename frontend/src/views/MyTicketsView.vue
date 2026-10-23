<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { Ticket, TicketStatus } from '@/types'
import { bookingService } from '@/services/booking.service'
import QrModal from '@/components/QrModal.vue'
import TicketItem from '@/components/my-tickets/TicketItem.vue'
import TicketDetailModal from '@/components/my-tickets/TicketDetailModal.vue'
import TransferTicketModal from '@/components/my-tickets/TransferTicketModal.vue'
import RefundTicketModal from '@/components/my-tickets/RefundTicketModal.vue'
import InvoiceModal from '@/components/my-tickets/InvoiceModal.vue'
import ReviewModal from '@/components/my-tickets/ReviewModal.vue'
import LookupTicketModal from '@/components/my-tickets/LookupTicketModal.vue'
import OrderGroupCard from '@/components/my-tickets/OrderGroupCard.vue'
import CheckInGuide from '@/components/my-tickets/CheckInGuide.vue'
import {
  Ticket as TicketIcon,
  Search,
  Calendar,
  CheckCircle2,
  Clock,
  ArrowRight,
  Sparkles,
  HelpCircle,
  ChevronDown,
  AlertCircle,
  Info,
  ShieldCheck,
  CreditCard,
  RefreshCw,
  LayoutGrid,
  Layers,
  FileText,
  Navigation,
  ChevronRight,
  Download,
  CalendarDays,
  X,
} from 'lucide-vue-next'

const tickets = ref<Ticket[]>([])
const loading = ref(true)

// Filter & Search states
const searchQuery = ref('')
const filterStatus = ref<'ALL' | TicketStatus>('VALID')
const selectedCategory = ref<string>('ALL')
const sortBy = ref<'DATE_ASC' | 'DATE_DESC' | 'PRICE_DESC' | 'PRICE_ASC'>('DATE_ASC')
const viewMode = ref<'INDIVIDUAL' | 'ORDER_GROUP'>('INDIVIDUAL')

// Modals
const selectedTicketForQr = ref<Ticket | null>(null)
const showQrModal = ref(false)

const selectedTicketForDetail = ref<Ticket | null>(null)
const showDetailModal = ref(false)

const selectedTicketForTransfer = ref<Ticket | null>(null)
const showTransferModal = ref(false)

const selectedTicketForRefund = ref<Ticket | null>(null)
const showRefundModal = ref(false)

const selectedTicketForInvoice = ref<Ticket | null>(null)
const showInvoiceModal = ref(false)

const selectedTicketForReview = ref<Ticket | null>(null)
const showReviewModal = ref(false)

const showLookupModal = ref(false)

// Toast notifications
type ToastType = 'error' | 'success' | 'info'
const toasts = ref<{ id: number; message: string; type: ToastType }[]>([])

const notify = (message: string, type: ToastType = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 4000)
}

onMounted(async () => {
  try {
    tickets.value = await bookingService.getMyTickets()
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
})

// Statistics computation
const stats = computed(() => {
  const total = tickets.value.length
  const upcoming = tickets.value.filter(
    (t) => t.status === 'VALID' && new Date(t.eventStartDateTime).getTime() > Date.now()
  ).length
  const used = tickets.value.filter((t) => t.status === 'USED').length
  const cancelled = tickets.value.filter((t) => t.status === 'CANCELLED').length
  const totalSpent = tickets.value
    .filter((t) => t.status !== 'CANCELLED')
    .reduce((sum, t) => sum + (t.price || 0), 0)

  return { total, upcoming, used, cancelled, totalSpent }
})

// Next Upcoming Event Spotlight
const nextUpcomingTicket = computed(() => {
  const validUpcoming = tickets.value
    .filter((t) => t.status === 'VALID' && new Date(t.eventStartDateTime).getTime() > Date.now())
    .sort((a, b) => new Date(a.eventStartDateTime).getTime() - new Date(b.eventStartDateTime).getTime())
  return validUpcoming[0] || null
})

// Categories extracted from tickets
const categories = computed(() => {
  const set = new Set<string>()
  tickets.value.forEach((t) => {
    if (t.categoryName) set.add(t.categoryName)
  })
  return Array.from(set)
})

// Filtered & Sorted tickets
const filteredTickets = computed(() => {
  let result = [...tickets.value]

  // Status Filter
  if (filterStatus.value !== 'ALL') {
    result = result.filter((t) => t.status === filterStatus.value)
  }

  // Category Filter
  if (selectedCategory.value !== 'ALL') {
    result = result.filter((t) => t.categoryName === selectedCategory.value)
  }

  // Keyword Search
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.toLowerCase().trim()
    result = result.filter(
      (t) =>
        t.eventTitle.toLowerCase().includes(q) ||
        t.ticketCode.toLowerCase().includes(q) ||
        t.venueName.toLowerCase().includes(q) ||
        t.attendeeName.toLowerCase().includes(q) ||
        t.bookingCode.toLowerCase().includes(q)
    )
  }

  // Sorting
  result.sort((a, b) => {
    const dateA = new Date(a.eventStartDateTime).getTime()
    const dateB = new Date(b.eventStartDateTime).getTime()

    if (sortBy.value === 'DATE_ASC') return dateA - dateB
    if (sortBy.value === 'DATE_DESC') return dateB - dateA
    if (sortBy.value === 'PRICE_DESC') return (b.price || 0) - (a.price || 0)
    if (sortBy.value === 'PRICE_ASC') return (a.price || 0) - (b.price || 0)
    return 0
  })

  return result
})

// Grouped by Booking Orders
const orderGroups = computed(() => {
  const map: Record<string, Ticket[]> = {}
  filteredTickets.value.forEach((t) => {
    if (!map[t.bookingCode]) {
      map[t.bookingCode] = []
    }
    map[t.bookingCode].push(t)
  })

  return Object.entries(map).map(([bookingCode, groupTickets]) => ({
    bookingCode,
    tickets: groupTickets,
  }))
})

// Modal Openers
const openQr = (t: Ticket) => {
  selectedTicketForQr.value = t
  showQrModal.value = true
}

const openDetail = (t: Ticket) => {
  selectedTicketForDetail.value = t
  showDetailModal.value = true
}

const openTransfer = (t: Ticket) => {
  selectedTicketForTransfer.value = t
  showTransferModal.value = true
}

const openRefund = (t: Ticket) => {
  selectedTicketForRefund.value = t
  showRefundModal.value = true
}

const openInvoice = (t: Ticket) => {
  selectedTicketForInvoice.value = t
  showInvoiceModal.value = true
}

const openReview = (t: Ticket) => {
  selectedTicketForReview.value = t
  showReviewModal.value = true
}

// Transfer Handler
const handleTicketTransferred = (ticketId: number, recipientName: string, newTicketCode: string) => {
  const index = tickets.value.findIndex((t) => t.id === ticketId)
  if (index > -1) {
    tickets.value[index].status = 'CANCELLED'
    tickets.value[index].cancelReason = `Đã chuyển nhượng vé cho ${recipientName} (Mã mới: ${newTicketCode})`
  }
  showTransferModal.value = false
  notify(`Đã chuyển nhượng vé thành công cho ${recipientName}!`, 'success')
}

// Refund Handler
const handleRefundRequested = (ticketId: number, reason: string) => {
  const index = tickets.value.findIndex((t) => t.id === ticketId)
  if (index > -1) {
    tickets.value[index].status = 'CANCELLED'
    tickets.value[index].cancelReason = reason
    tickets.value[index].refundAmount = Math.round(((tickets.value[index].price || 0) * 70) / 100)
    tickets.value[index].refundDate = new Date().toISOString()
    tickets.value[index].refundMethod = 'Tài khoản ngân hàng của khán giả'
  }
  showRefundModal.value = false
  notify('Yêu cầu hoàn vé đã được gửi đến Ban Tổ Chức!', 'success')
}

// Invoice Handler
const handleInvoiceSubmitted = (ticketId: number, data: { companyName: string; taxCode: string; address: string; email: string }) => {
  const index = tickets.value.findIndex((t) => t.id === ticketId)
  if (index > -1) {
    tickets.value[index].invoiceRequested = true
    tickets.value[index].invoiceCompanyName = data.companyName
    tickets.value[index].invoiceTaxCode = data.taxCode
  }
}

// Review Handler
const handleReviewSubmitted = (ticketId: number, ratingVal: number, commentVal: string) => {
  const index = tickets.value.findIndex((t) => t.id === ticketId)
  if (index > -1) {
    tickets.value[index].hasReviewed = true
    tickets.value[index].reviewRating = ratingVal
    tickets.value[index].reviewComment = commentVal
  }
}

// Download .ics Calendar
const downloadCalendar = (ticket: Ticket) => {
  const fmt = (iso: string) => new Date(iso).toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z'
  const start = fmt(ticket.eventStartDateTime)
  const endDate = new Date(new Date(ticket.eventStartDateTime).getTime() + 3 * 3600 * 1000)
  const end = fmt(endDate.toISOString())

  const ics = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//EventHub//VN',
    'BEGIN:VEVENT',
    `UID:ticket-${ticket.ticketCode}@eventhub.vn`,
    `DTSTAMP:${fmt(new Date().toISOString())}`,
    `DTSTART:${start}`,
    `DTEND:${end}`,
    `SUMMARY:${ticket.eventTitle}`,
    `LOCATION:${ticket.venueName}`,
    `DESCRIPTION:Vé điện tử EventHub: ${ticket.ticketTypeName} - Ghế: ${ticket.seatLabel || 'GA'} - Mã vé: ${ticket.ticketCode}`,
    'END:VEVENT',
    'END:VCALENDAR',
  ].join('\r\n')

  const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `EventHub-${ticket.ticketCode}.ics`
  link.click()
  URL.revokeObjectURL(url)
  notify('Đã tải lịch sự kiện (.ics) thành công!', 'success')
}

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatDateShort = (iso: string) => {
  return new Intl.DateTimeFormat('vi-VN', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso))
}

// FAQ Accordion
const faqs = ref([
  {
    q: 'Làm thế nào để quét vé check-in tại cổng nhanh nhất?',
    a: 'Bạn hãy mở sẵn mục "Vé của tôi", bấm "Mở Mã QR Check-in", tăng độ sáng màn hình điện thoại lên tối đa và chuẩn bị sẵn CCCD/VNeID để nhân viên đối soát tại cổng turnstile.',
    open: true,
  },
  {
    q: 'Tôi có thể chụp ảnh màn hình mã QR gửi cho người khác được không?',
    a: 'Không nên. Hệ thống áp dụng công nghệ Dynamic QR & Anti-Passback: mã QR sẽ tự động làm mới chu kỳ liên tục và chỉ được quét duy nhất 01 lần. Nếu bạn muốn gửi vé cho người khác, vui lòng sử dụng chức năng "Chuyển Nhượng Vé" chính thức để cấp mã mới hợp lệ.',
    open: false,
  },
  {
    q: 'Quy định hủy hoặc yêu cầu hoàn tiền vé như thế nào?',
    a: 'Nếu bạn có nhu cầu hủy vé chủ động trước sự kiện từ 7 ngày trở lên, hệ thống hỗ trợ hoàn trả 70% giá trị vé theo quy định. Trường hợp sự kiện bị dời ngày hoặc hủy bỏ do ban tổ chức, bạn sẽ được hoàn trả 100% chi phí.',
    open: false,
  },
  {
    q: 'Tôi muốn xuất hóa đơn VAT cho công ty thì làm thế nào?',
    a: 'Tại mỗi thẻ vé, bạn chỉ cần bấm "Hóa đơn VAT", điền tên công ty, mã số thuế và địa chỉ trụ sở. Ban Tổ Chức sẽ phát hành hóa đơn điện tử hợp lệ gửi về email trong vòng 48 giờ.',
    open: false,
  },
  {
    q: 'Tôi có cần in vé giấy mang theo không?',
    a: 'Không bắt buộc. Mã QR điện tử hiển thị trực tiếp trên điện thoại đã có giá trị vào cổng chính thức. Tuy nhiên, bạn vẫn có thể bấm "In / Tải PDF" trong phần mã QR để lưu trữ bản cứng làm kỷ niệm.',
    open: false,
  },
])

const toggleFaq = (index: number) => {
  faqs.value[index].open = !faqs.value[index].open
}
</script>

<template>
  <div class="min-h-screen bg-[#f8fafc] pb-24 text-slate-800">
    <!-- Sophisticated Page Header (Light-first, Harmonious with Navbar) -->
    <div class="bg-white border-b border-slate-200/80">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-4 pb-6">
        <!-- Breadcrumb -->
        <div class="flex items-center gap-1.5 text-xs text-slate-500 mb-3">
          <router-link to="/" class="hover:text-indigo-600 transition">Trang chủ</router-link>
          <ChevronRight class="w-3.5 h-3.5 text-slate-400" />
          <span class="font-medium text-slate-800">Vé của tôi</span>
        </div>

        <!-- Header Title Row -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2 mb-1.5">
              <h1 class="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900">
                Vé Sự Kiện Của Tôi
              </h1>
              <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                <ShieldCheck class="w-3 h-3 text-emerald-600" />
                Anti-Passback
              </span>
            </div>
            <p class="text-xs sm:text-sm text-slate-500 max-w-2xl leading-relaxed">
              Quản lý toàn bộ vé điện tử chính hãng, mở mã QR động check-in tại cổng kiểm soát và đồng bộ lịch.
            </p>
          </div>

          <!-- Header Actions -->
          <div class="flex items-center gap-2.5 shrink-0">
            <button
              type="button"
              @click="showLookupModal = true"
              class="px-4 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 font-semibold text-xs transition flex items-center gap-1.5 shadow-2xs"
            >
              <Search class="w-3.5 h-3.5 text-slate-400" />
              <span>Tra Cứu Mã Vé</span>
            </button>

            <router-link
              to="/events"
              class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-semibold text-xs shadow-sm transition flex items-center gap-1.5"
            >
              <span>Khám Phá Thêm</span>
              <ArrowRight class="w-3.5 h-3.5" />
            </router-link>
          </div>
        </div>

        <!-- Metric Strips (Understated, Elegant, Clickable) -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mt-6">
          <!-- Upcoming -->
          <button
            type="button"
            @click="filterStatus = 'VALID'"
            class="p-3.5 rounded-xl border text-left transition-all"
            :class="[
              filterStatus === 'VALID'
                ? 'bg-indigo-50/60 border-indigo-200 ring-1 ring-indigo-200'
                : 'bg-slate-50/70 border-slate-200/80 hover:bg-slate-100/70'
            ]"
          >
            <div class="flex items-center justify-between text-xs text-slate-500 mb-1">
              <span class="font-medium">Sắp diễn ra</span>
              <span class="w-2 h-2 rounded-full bg-indigo-600"></span>
            </div>
            <div class="text-xl sm:text-2xl font-bold text-slate-900">
              {{ stats.upcoming }} <span class="text-xs font-normal text-slate-500">sự kiện</span>
            </div>
          </button>

          <!-- Total tickets -->
          <button
            type="button"
            @click="filterStatus = 'ALL'; selectedCategory = 'ALL'"
            class="p-3.5 rounded-xl border text-left transition-all"
            :class="[
              filterStatus === 'ALL'
                ? 'bg-indigo-50/60 border-indigo-200 ring-1 ring-indigo-200'
                : 'bg-slate-50/70 border-slate-200/80 hover:bg-slate-100/70'
            ]"
          >
            <div class="flex items-center justify-between text-xs text-slate-500 mb-1">
              <span class="font-medium">Tổng vé sở hữu</span>
              <TicketIcon class="w-3.5 h-3.5 text-slate-400" />
            </div>
            <div class="text-xl sm:text-2xl font-bold text-slate-900">
              {{ stats.total }} <span class="text-xs font-normal text-slate-500">vé</span>
            </div>
          </button>

          <!-- Attended -->
          <button
            type="button"
            @click="filterStatus = 'USED'"
            class="p-3.5 rounded-xl border text-left transition-all"
            :class="[
              filterStatus === 'USED'
                ? 'bg-indigo-50/60 border-indigo-200 ring-1 ring-indigo-200'
                : 'bg-slate-50/70 border-slate-200/80 hover:bg-slate-100/70'
            ]"
          >
            <div class="flex items-center justify-between text-xs text-slate-500 mb-1">
              <span class="font-medium">Đã tham gia</span>
              <CheckCircle2 class="w-3.5 h-3.5 text-slate-400" />
            </div>
            <div class="text-xl sm:text-2xl font-bold text-slate-900">
              {{ stats.used }} <span class="text-xs font-normal text-slate-500">vé</span>
            </div>
          </button>

          <!-- Total spent -->
          <div class="p-3.5 rounded-xl border border-slate-200/80 bg-slate-50/70 text-left">
            <div class="flex items-center justify-between text-xs text-slate-500 mb-1">
              <span class="font-medium">Tổng chi tiêu vé</span>
              <CreditCard class="w-3.5 h-3.5 text-slate-400" />
            </div>
            <div class="text-xl sm:text-2xl font-bold text-slate-900 truncate">
              {{ formatCurrency(stats.totalSpent) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Container -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6 space-y-6">
      <!-- Next Upcoming Event Spotlight (Compact & Elegant) -->
      <div
        v-if="nextUpcomingTicket"
        class="bg-white rounded-2xl p-4 sm:p-5 border-l-4 border-l-indigo-600 border border-slate-200/90 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4"
      >
        <div class="flex items-center gap-3.5 min-w-0">
          <img
            :src="nextUpcomingTicket.eventBannerUrl"
            :alt="nextUpcomingTicket.eventTitle"
            class="w-14 h-14 rounded-xl object-cover shrink-0 shadow-xs border border-slate-200"
          />
          <div class="min-w-0">
            <div class="flex items-center gap-2 mb-0.5">
              <span class="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider bg-indigo-50 text-indigo-700">
                Sự kiện kế tiếp
              </span>
              <span class="text-xs text-slate-500 font-medium">
                {{ formatDateShort(nextUpcomingTicket.eventStartDateTime) }}
              </span>
            </div>
            <h3 class="text-sm sm:text-base font-bold text-slate-900 truncate">
              {{ nextUpcomingTicket.eventTitle }}
            </h3>
            <p class="text-xs text-slate-500 flex items-center gap-1.5 mt-0.5 truncate">
              <MapPin class="w-3.5 h-3.5 text-slate-400 shrink-0" />
              <span>{{ nextUpcomingTicket.venueName }}</span>
              <span class="text-slate-300">&bull;</span>
              <span>Ghế: <strong class="text-slate-800">{{ nextUpcomingTicket.seatLabel || 'GA' }}</strong></span>
            </p>
          </div>
        </div>

        <div class="flex items-center gap-2.5 shrink-0 self-end md:self-center">
          <button
            type="button"
            @click="openQr(nextUpcomingTicket)"
            class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 active:scale-95 text-white font-semibold text-xs shadow-sm transition flex items-center gap-1.5"
          >
            <ShieldCheck class="w-4 h-4" />
            <span>Mở QR Check-in Ngay</span>
          </button>
        </div>
      </div>

      <!-- Filter & Controls Toolbar -->
      <div class="bg-white p-4 sm:p-5 rounded-2xl border border-slate-200/90 shadow-xs space-y-4">
        <!-- Top Row: Segmented Tabs & View Mode -->
        <div class="flex flex-col lg:flex-row lg:items-center justify-between gap-3">
          <!-- Segmented Status Tabs -->
          <div class="inline-flex p-1 bg-slate-100 rounded-xl text-xs font-semibold">
            <button
              type="button"
              @click="filterStatus = 'VALID'"
              class="px-3.5 py-1.5 rounded-lg transition"
              :class="filterStatus === 'VALID' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
            >
              Sắp diễn ra ({{ tickets.filter(t => t.status === 'VALID').length }})
            </button>
            <button
              type="button"
              @click="filterStatus = 'ALL'"
              class="px-3.5 py-1.5 rounded-lg transition"
              :class="filterStatus === 'ALL' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
            >
              Tất cả ({{ tickets.length }})
            </button>
            <button
              type="button"
              @click="filterStatus = 'USED'"
              class="px-3.5 py-1.5 rounded-lg transition"
              :class="filterStatus === 'USED' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
            >
              Đã check-in ({{ tickets.filter(t => t.status === 'USED').length }})
            </button>
            <button
              type="button"
              @click="filterStatus = 'CANCELLED'"
              class="px-3.5 py-1.5 rounded-lg transition"
              :class="filterStatus === 'CANCELLED' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
            >
              Đã hủy / Hoàn tiền ({{ tickets.filter(t => t.status === 'CANCELLED').length }})
            </button>
          </div>

          <!-- View Mode Toggle -->
          <div class="flex items-center gap-2 self-start lg:self-auto">
            <span class="text-xs text-slate-400 font-medium hidden sm:inline">Chế độ xem:</span>
            <div class="inline-flex p-0.5 bg-slate-100 rounded-xl text-xs font-semibold">
              <button
                type="button"
                @click="viewMode = 'INDIVIDUAL'"
                class="px-3 py-1.5 rounded-lg transition flex items-center gap-1.5"
                :class="viewMode === 'INDIVIDUAL' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
              >
                <LayoutGrid class="w-3.5 h-3.5" />
                <span>Từng vé</span>
              </button>
              <button
                type="button"
                @click="viewMode = 'ORDER_GROUP'"
                class="px-3 py-1.5 rounded-lg transition flex items-center gap-1.5"
                :class="viewMode === 'ORDER_GROUP' ? 'bg-white text-slate-900 shadow-xs' : 'text-slate-600 hover:text-slate-900'"
              >
                <Layers class="w-3.5 h-3.5" />
                <span>Gom theo đơn</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Bottom Row: Category Chips, Search & Sort -->
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 pt-3 border-t border-slate-100">
          <!-- Categories Filter Chips -->
          <div class="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0 scrollbar-none text-xs">
            <button
              type="button"
              @click="selectedCategory = 'ALL'"
              class="px-3 py-1.5 rounded-lg font-semibold whitespace-nowrap transition"
              :class="selectedCategory === 'ALL' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            >
              Tất cả thể loại
            </button>
            <button
              v-for="cat in categories"
              :key="cat"
              type="button"
              @click="selectedCategory = cat"
              class="px-3 py-1.5 rounded-lg font-semibold whitespace-nowrap transition"
              :class="selectedCategory === cat ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
            >
              {{ cat }}
            </button>
          </div>

          <!-- Search & Sort Box -->
          <div class="flex items-center gap-2">
            <div class="relative flex-1 sm:w-60">
              <Search class="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm vé, sự kiện, khán giả..."
                class="w-full pl-8.5 pr-7 py-1.5 rounded-xl border border-slate-200 text-xs bg-slate-50 focus:bg-white focus:outline-none focus:ring-1 focus:ring-indigo-500 text-slate-900"
              />
              <button
                v-if="searchQuery"
                @click="searchQuery = ''"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <X class="w-3 h-3" />
              </button>
            </div>

            <select
              v-model="sortBy"
              class="px-3 py-1.5 rounded-xl border border-slate-200 text-xs font-semibold bg-slate-50 text-slate-700 focus:outline-none focus:ring-1 focus:ring-indigo-500 cursor-pointer shrink-0"
            >
              <option value="DATE_ASC">Sắp diễn ra nhất</option>
              <option value="DATE_DESC">Sự kiện xa nhất</option>
              <option value="PRICE_DESC">Giá cao &rarr; thấp</option>
              <option value="PRICE_ASC">Giá thấp &rarr; cao</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Tickets List Section -->
      <div>
        <!-- Loading Skeleton -->
        <div v-if="loading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="h-44 rounded-2xl bg-slate-200 animate-pulse"></div>
        </div>

        <!-- Empty State -->
        <div
          v-else-if="filteredTickets.length === 0"
          class="bg-white p-12 sm:p-16 rounded-2xl text-center border border-slate-200 shadow-xs space-y-4"
        >
          <div class="w-14 h-14 rounded-2xl bg-indigo-50 text-indigo-600 mx-auto flex items-center justify-center">
            <TicketIcon class="w-7 h-7" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-bold text-slate-900">Không tìm thấy vé phù hợp</h3>
            <p class="text-xs sm:text-sm text-slate-500 mt-1 max-w-md mx-auto">
              Không có vé nào khớp với tiêu chí tìm kiếm hoặc trạng thái bạn đang chọn.
            </p>
          </div>
          <div class="flex items-center justify-center gap-2.5 pt-1">
            <button
              v-if="searchQuery || filterStatus !== 'ALL' || selectedCategory !== 'ALL'"
              @click="searchQuery = ''; filterStatus = 'ALL'; selectedCategory = 'ALL'"
              class="px-4 py-2 rounded-xl border border-slate-300 text-slate-700 text-xs font-semibold hover:bg-slate-50 transition"
            >
              Xóa bộ lọc
            </button>
            <router-link
              to="/events"
              class="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold shadow-xs transition"
            >
              Xem tất cả sự kiện
            </router-link>
          </div>
        </div>

        <!-- Render List: Grouped by Order View -->
        <div v-else-if="viewMode === 'ORDER_GROUP'" class="space-y-5">
          <OrderGroupCard
            v-for="group in orderGroups"
            :key="group.bookingCode"
            :booking-code="group.bookingCode"
            :tickets="group.tickets"
            @open-qr="openQr"
            @open-detail="openDetail"
            @open-transfer="openTransfer"
            @open-refund="openRefund"
            @open-invoice="openInvoice"
            @open-review="openReview"
            @download-calendar="downloadCalendar"
            @notify="notify"
          />
        </div>

        <!-- Render List: Individual Ticket Cards View -->
        <div v-else class="space-y-4">
          <TicketItem
            v-for="ticket in filteredTickets"
            :key="ticket.id"
            :ticket="ticket"
            @open-qr="openQr"
            @open-detail="openDetail"
            @open-transfer="openTransfer"
            @open-refund="openRefund"
            @open-invoice="openInvoice"
            @open-review="openReview"
            @download-calendar="downloadCalendar"
            @notify="notify"
          />
        </div>
      </div>

      <!-- 4-Step Entrance Check-in Guide Component -->
      <CheckInGuide />

      <!-- FAQ Accordion Section -->
      <div class="rounded-2xl bg-white border border-slate-200/90 p-6 sm:p-8 shadow-xs">
        <div class="flex items-center gap-2 mb-5 pb-3 border-b border-slate-100">
          <HelpCircle class="w-4 h-4 text-indigo-600" />
          <h3 class="text-base sm:text-lg font-bold text-slate-900">
            Các Câu Hỏi Thường Gặp Về Vé & Soát Vé
          </h3>
        </div>

        <div class="divide-y divide-slate-100">
          <div v-for="(f, idx) in faqs" :key="idx" class="py-3.5 first:pt-0 last:pb-0">
            <button
              type="button"
              @click="toggleFaq(idx)"
              class="w-full flex items-center justify-between text-left gap-4 font-semibold text-xs sm:text-sm text-slate-800 hover:text-indigo-600 transition-colors"
            >
              <span>{{ f.q }}</span>
              <ChevronDown
                class="w-4 h-4 shrink-0 transition-transform duration-300"
                :class="f.open ? 'rotate-180 text-indigo-600' : 'text-slate-400'"
              />
            </button>
            <div
              v-show="f.open"
              class="mt-2 text-xs text-slate-500 leading-relaxed pl-0.5"
            >
              {{ f.a }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <QrModal
      :show="showQrModal"
      :ticket="selectedTicketForQr"
      @close="showQrModal = false; selectedTicketForQr = null"
      @notify="notify"
    />

    <TicketDetailModal
      :show="showDetailModal"
      :ticket="selectedTicketForDetail"
      @close="showDetailModal = false; selectedTicketForDetail = null"
      @open-qr="openQr"
      @open-transfer="openTransfer"
      @open-refund="openRefund"
      @open-invoice="openInvoice"
      @open-review="openReview"
      @download-calendar="downloadCalendar"
      @notify="notify"
    />

    <TransferTicketModal
      :show="showTransferModal"
      :ticket="selectedTicketForTransfer"
      @close="showTransferModal = false; selectedTicketForTransfer = null"
      @ticket-transferred="handleTicketTransferred"
      @notify="notify"
    />

    <RefundTicketModal
      :show="showRefundModal"
      :ticket="selectedTicketForRefund"
      @close="showRefundModal = false; selectedTicketForRefund = null"
      @refund-requested="handleRefundRequested"
      @notify="notify"
    />

    <InvoiceModal
      :show="showInvoiceModal"
      :ticket="selectedTicketForInvoice"
      @close="showInvoiceModal = false; selectedTicketForInvoice = null"
      @invoice-submitted="handleInvoiceSubmitted"
      @notify="notify"
    />

    <ReviewModal
      :show="showReviewModal"
      :ticket="selectedTicketForReview"
      @close="showReviewModal = false; selectedTicketForReview = null"
      @review-submitted="handleReviewSubmitted"
      @notify="notify"
    />

    <LookupTicketModal
      :show="showLookupModal"
      :tickets="tickets"
      @close="showLookupModal = false"
      @open-detail="openDetail"
      @open-qr="openQr"
    />

    <!-- Global Toast Notifications -->
    <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-sm pointer-events-none">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="px-4 py-2.5 rounded-xl shadow-lg border pointer-events-auto flex items-center gap-2.5 text-xs font-semibold animate-slide-up backdrop-blur-md"
        :class="[
          toast.type === 'success' ? 'bg-emerald-950/90 text-emerald-100 border-emerald-500/40' :
          toast.type === 'error' ? 'bg-rose-950/90 text-rose-100 border-rose-500/40' :
          'bg-slate-900/95 text-white border-slate-700'
        ]"
      >
        <CheckCircle2 v-if="toast.type === 'success'" class="w-4 h-4 text-emerald-400 shrink-0" />
        <AlertCircle v-else-if="toast.type === 'error'" class="w-4 h-4 text-rose-400 shrink-0" />
        <Info v-else class="w-4 h-4 text-indigo-400 shrink-0" />
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>
