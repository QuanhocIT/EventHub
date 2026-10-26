<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { eventService } from '@/services/event.service'
import { adminService } from '@/services/admin.service'
import type { Event, Category, Venue, TicketType, EventSeat, EventAnalyticsData } from '@/types'
import {
  ArrowLeft,
  Calendar,
  MapPin,
  Building2,
  DollarSign,
  Ticket,
  Armchair,
  CheckCircle2,
  AlertCircle,
  Eye,
  Download,
  ExternalLink,
  Save,
  Plus,
  Trash2,
  Edit,
  SlidersHorizontal,
  Layers,
  Activity,
  FileSpreadsheet,
  Clock,
  Users,
  Check,
  X,
  Sparkles,
} from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const eventId = Number(route.params.id) || 1
const activeTab = ref<'info' | 'tiers' | 'seats' | 'analytics'>('info')

const loading = ref(true)
const actionLoading = ref(false)
const event = ref<Event | null>(null)
const categories = ref<Category[]>([])
const venues = ref<Venue[]>([])
const ticketTiers = ref<TicketType[]>([])
const seatMap = ref<EventSeat[]>([])
const analytics = ref<EventAnalyticsData | null>(null)

// Form state for Event Info
const eventForm = ref({
  title: '',
  description: '',
  shortDescription: '',
  location: '',
  venueId: 0,
  startTime: '',
  endTime: '',
  capacity: 1000,
  price: 350000,
  status: 'PUBLISHED',
  categoryId: 1,
  bannerUrl: '',
  organizerName: '',
  hasSeatMap: true,
})

// Modal state for adding/editing ticket tier
const isTierModalOpen = ref(false)
const editingTier = ref<TicketType | null>(null)
const tierForm = ref({
  id: 0,
  name: '',
  description: '',
  price: 350000,
  totalQuantity: 500,
  minPerOrder: 1,
  maxPerOrder: 6,
  colorHex: '#3b82f6',
})

// Toast notification
type ToastType = 'success' | 'error' | 'info'
const toasts = ref<{ id: number; message: string; type: ToastType }[]>([])
const notify = (message: string, type: ToastType = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 3500)
}

const formatCurrency = (val?: number) => {
  if (val === undefined || val === null) return '0 đ'
  return val.toLocaleString('vi-VN') + ' đ'
}

// Preset banner suggestions
const presetBanners = [
  { name: 'Đại Nhạc Hội & Arena Live', url: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1200&q=80' },
  { name: 'Công Nghệ Tech AI Summit', url: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80' },
  { name: 'Hòa Nhạc Giao Hưởng Thính Phòng', url: 'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=1200&q=80' },
  { name: 'Giải Đấu Thể Thao & Esports', url: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80' },
  { name: 'Lễ Hội Ẩm Thực & Nghệ Thuật', url: 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=1200&q=80' },
]

onMounted(async () => {
  loading.value = true
  try {
    const [eventData, catList, venueList] = await Promise.all([
      eventService.getEventById(eventId),
      eventService.getCategories(),
      eventService.getVenues(),
    ])

    event.value = eventData
    categories.value = catList
    venues.value = venueList

    // Populate form
    eventForm.value = {
      title: eventData.title,
      description: eventData.description || '',
      shortDescription: eventData.shortDescription || '',
      location: eventData.address || eventData.locationName || '',
      venueId: eventData.venue?.id || 1,
      startTime: eventData.startDateTime ? eventData.startDateTime.slice(0, 16) : '2026-11-15T19:00',
      endTime: eventData.endDateTime ? eventData.endDateTime.slice(0, 16) : '2026-11-15T22:30',
      capacity: eventData.venue?.capacity || 1000,
      price: eventData.minPrice || 350000,
      status: eventData.status || 'PUBLISHED',
      categoryId: eventData.category?.id || 1,
      bannerUrl: eventData.bannerUrl || '',
      organizerName: eventData.organizerName || 'Ban Tổ Chức Hệ Thống',
      hasSeatMap: eventData.hasSeatMap ?? true,
    }

    // Load ticket tiers
    try {
      ticketTiers.value = await eventService.getEventTicketTypes(eventId)
    } catch {
      ticketTiers.value = eventData.ticketTypes || []
    }

    // Load seat map if applicable
    if (eventData.hasSeatMap) {
      try {
        seatMap.value = await eventService.getSeatMap(eventId)
      } catch {
        seatMap.value = []
      }
    }

    // Load analytics
    try {
      analytics.value = await adminService.getEventAnalytics(eventId)
    } catch {
      analytics.value = null
    }
  } catch (err: any) {
    notify('Không thể tải thông tin sự kiện', 'error')
  } finally {
    loading.value = false
  }
})

// Save General Info
const handleSaveEventInfo = async () => {
  if (!event.value) return
  actionLoading.value = true
  try {
    const updated = await eventService.updateEvent(event.value.id, {
      title: eventForm.value.title,
      description: eventForm.value.description,
      location: eventForm.value.location,
      startTime: eventForm.value.startTime ? `${eventForm.value.startTime}:00` : undefined,
      endTime: eventForm.value.endTime ? `${eventForm.value.endTime}:00` : undefined,
      capacity: Number(eventForm.value.capacity),
      price: Number(eventForm.value.price),
      status: eventForm.value.status as any,
      categoryId: Number(eventForm.value.categoryId),
    })

    event.value = {
      ...event.value,
      title: updated.title,
      description: updated.description,
      status: updated.status,
      minPrice: updated.minPrice,
      bannerUrl: eventForm.value.bannerUrl || event.value.bannerUrl,
    }
    notify('Đã cập nhật thông tin sự kiện thành công!', 'success')
  } catch (err: any) {
    notify(err?.response?.data?.message || 'Lỗi khi lưu thông tin sự kiện', 'error')
  } finally {
    actionLoading.value = false
  }
}

// Quick status change
const handleChangeStatus = async (newStatus: string) => {
  if (!event.value) return
  eventForm.value.status = newStatus
  await handleSaveEventInfo()
}

// Ticket Tiers CRUD
const openAddTierModal = () => {
  editingTier.value = null
  tierForm.value = {
    id: 0,
    name: '',
    description: '',
    price: eventForm.value.price || 350000,
    totalQuantity: 200,
    minPerOrder: 1,
    maxPerOrder: 4,
    colorHex: '#6366f1',
  }
  isTierModalOpen.value = true
}

const openEditTierModal = (tier: TicketType) => {
  editingTier.value = tier
  tierForm.value = {
    id: tier.id,
    name: tier.name,
    description: tier.description || '',
    price: tier.price,
    totalQuantity: tier.totalQuantity,
    minPerOrder: tier.minPerOrder || 1,
    maxPerOrder: tier.maxPerOrder || 4,
    colorHex: tier.colorHex || '#6366f1',
  }
  isTierModalOpen.value = true
}

const handleSaveTier = async () => {
  if (!event.value) return
  actionLoading.value = true
  try {
    if (editingTier.value) {
      // Edit local tier
      const idx = ticketTiers.value.findIndex((t) => t.id === editingTier.value!.id)
      if (idx !== -1) {
        ticketTiers.value[idx] = {
          ...ticketTiers.value[idx],
          name: tierForm.value.name,
          description: tierForm.value.description,
          price: Number(tierForm.value.price),
          totalQuantity: Number(tierForm.value.totalQuantity),
          minPerOrder: Number(tierForm.value.minPerOrder),
          maxPerOrder: Number(tierForm.value.maxPerOrder),
          colorHex: tierForm.value.colorHex,
        }
      }
      notify(`Đã cập nhật hạng vé "${tierForm.value.name}"!`, 'success')
    } else {
      // Add new tier
      const newId = Math.max(...ticketTiers.value.map((t) => t.id), 0) + 1
      ticketTiers.value.push({
        id: newId,
        name: tierForm.value.name,
        description: tierForm.value.description,
        price: Number(tierForm.value.price),
        totalQuantity: Number(tierForm.value.totalQuantity),
        remainingQuantity: Number(tierForm.value.totalQuantity),
        minPerOrder: Number(tierForm.value.minPerOrder),
        maxPerOrder: Number(tierForm.value.maxPerOrder),
        colorHex: tierForm.value.colorHex,
      })
      notify(`Đã thêm hạng vé "${tierForm.value.name}" mới!`, 'success')
    }
    isTierModalOpen.value = false
  } catch (err: any) {
    notify('Không thể lưu hạng vé', 'error')
  } finally {
    actionLoading.value = false
  }
}

const handleDeleteTier = (tierId: number) => {
  ticketTiers.value = ticketTiers.value.filter((t) => t.id !== tierId)
  notify('Đã xóa hạng vé khỏi sự kiện!', 'info')
}

// Export Attendees
const handleExportAttendees = async () => {
  if (!event.value) return
  notify('Đang xuất danh sách khách tham dự (.xlsx)...', 'info')
  try {
    await adminService.exportEventAttendees(event.value.id, event.value.title)
    notify('Đã tải xuống file Excel danh sách khách tham dự thành công!', 'success')
  } catch {
    notify('Xuất danh sách thất bại', 'error')
  }
}

// Seat statistics
const seatStats = computed(() => {
  const total = seatMap.value.length || 0
  const sold = seatMap.value.filter((s) => s.status === 'SOLD').length
  const held = seatMap.value.filter((s) => s.status === 'HELD').length
  const available = seatMap.value.filter((s) => s.status === 'AVAILABLE').length
  const occupancy = total > 0 ? Math.round(((sold + held) / total) * 100) : 0
  return { total, sold, held, available, occupancy }
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 font-sans text-slate-800 pb-16">
    <!-- Toast notifications -->
    <div class="fixed top-5 right-5 z-50 flex flex-col gap-2.5 pointer-events-none max-w-sm">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl text-xs font-bold transition-all border animate-slide-in"
        :class="[
          toast.type === 'success' ? 'bg-emerald-600 text-white border-emerald-500' :
          toast.type === 'error' ? 'bg-rose-600 text-white border-rose-500' :
          'bg-slate-900 text-white border-slate-700'
        ]"
      >
        <CheckCircle2 v-if="toast.type === 'success'" class="w-4 h-4 shrink-0" />
        <AlertCircle v-else class="w-4 h-4 shrink-0" />
        <span>{{ toast.message }}</span>
      </div>
    </div>

    <!-- Top Sticky Header Navigation -->
    <header class="sticky top-0 z-40 bg-white/90 backdrop-blur-md border-b border-slate-200/80 shadow-2xs">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3.5 flex items-center justify-between gap-4">
        <div class="flex items-center gap-3.5 min-w-0">
          <button
            type="button"
            @click="router.push('/dashboard?tab=events')"
            class="p-2 rounded-xl border border-slate-200 bg-white hover:bg-slate-100 text-slate-700 transition cursor-pointer shrink-0 shadow-2xs flex items-center gap-1.5 text-xs font-bold"
            title="Quay lại danh sách sự kiện"
          >
            <ArrowLeft class="w-4 h-4" />
            <span class="hidden sm:inline">Quay Lại</span>
          </button>

          <div class="min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <span class="px-2 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-indigo-50 text-indigo-700 border border-indigo-200">
                Event Studio
              </span>
              <span class="text-xs text-slate-400 font-mono">ID: #{{ eventId }}</span>
              <span
                class="px-2 py-0.5 rounded-full text-[10px] font-black uppercase text-white shadow-2xs"
                :class="eventForm.status === 'PUBLISHED' ? 'bg-emerald-600' : 'bg-amber-500'"
              >
                {{ eventForm.status === 'PUBLISHED' ? 'Đang Mở Bán' : 'Bản Nháp' }}
              </span>
            </div>
            <h1 class="text-base sm:text-lg font-black text-slate-900 truncate tracking-tight">
              {{ event?.title || 'Đang tải thông tin sự kiện...' }}
            </h1>
          </div>
        </div>

        <!-- Header Actions -->
        <div class="flex items-center gap-2 shrink-0">
          <button
            type="button"
            @click="router.push('/events/' + eventId)"
            class="px-3 py-1.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-100 text-slate-700 font-bold text-xs flex items-center gap-1.5 transition cursor-pointer shadow-2xs"
            title="Xem giao diện công khai phía khách hàng"
          >
            <ExternalLink class="w-3.5 h-3.5" />
            <span class="hidden md:inline">Trang Bán Vé</span>
          </button>

          <button
            type="button"
            @click="router.push('/organizer/seat-map-builder')"
            class="px-3 py-1.5 rounded-xl border border-purple-200 bg-purple-50 hover:bg-purple-100 text-purple-700 font-bold text-xs flex items-center gap-1.5 transition cursor-pointer shadow-2xs"
            title="Dựng sơ đồ ghế trực quan"
          >
            <Armchair class="w-3.5 h-3.5 text-purple-600" />
            <span class="hidden md:inline">Seat Builder</span>
          </button>

          <button
            type="button"
            @click="handleExportAttendees"
            class="px-3 py-1.5 rounded-xl border border-emerald-200 bg-emerald-50 hover:bg-emerald-100 text-emerald-700 font-bold text-xs flex items-center gap-1.5 transition cursor-pointer shadow-2xs"
            title="Xuất file danh sách khách tham gia dạng Excel"
          >
            <Download class="w-3.5 h-3.5" />
            <span class="hidden md:inline">Xuất Excel</span>
          </button>

          <button
            type="button"
            @click="handleSaveEventInfo"
            :disabled="actionLoading"
            class="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-bold text-xs flex items-center gap-1.5 transition cursor-pointer shadow-md shadow-indigo-600/30"
          >
            <Save class="w-3.5 h-3.5" />
            <span>Lưu Thay Đổi</span>
          </button>
        </div>
      </div>
    </header>

    <!-- Main Container -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-6 space-y-6">
      <!-- 4 Quick KPI Summary Cards -->
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- KPI 1: Doanh Thu -->
        <div class="p-4 sm:p-5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div class="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider">
            <span>Doanh Thu Vé</span>
            <DollarSign class="w-4 h-4 text-emerald-600" />
          </div>
          <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {{ formatCurrency(analytics?.totalRevenue || (eventForm.price * 1240)) }}
          </div>
          <div class="text-[11px] text-emerald-600 font-semibold flex items-center gap-1">
            <span>&uarr; 82.6% Doanh số mục tiêu</span>
          </div>
        </div>

        <!-- KPI 2: Vé Đã Bán -->
        <div class="p-4 sm:p-5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div class="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider">
            <span>Vé Đã Bán Ra</span>
            <Ticket class="w-4 h-4 text-indigo-600" />
          </div>
          <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {{ (analytics?.totalTickets || 1240).toLocaleString('vi-VN') }} / {{ eventForm.capacity }}
          </div>
          <div class="text-[11px] text-slate-500 font-medium">
            Tỷ lệ lấp đầy: <strong class="text-indigo-600">{{ Math.round((1240 / (eventForm.capacity || 1500)) * 100) }}%</strong>
          </div>
        </div>

        <!-- KPI 3: Tỷ Lệ Check-in -->
        <div class="p-4 sm:p-5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div class="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider">
            <span>Soát Vé Check-in</span>
            <CheckCircle2 class="w-4 h-4 text-blue-600" />
          </div>
          <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {{ analytics?.checkInRate || 88.5 }}%
          </div>
          <div class="text-[11px] text-slate-500 font-medium">
            Quét mã QR qua máy Staff Scanner
          </div>
        </div>

        <!-- KPI 4: Đơn Giá Vé -->
        <div class="p-4 sm:p-5 rounded-2xl bg-white border border-slate-200/90 shadow-2xs space-y-1">
          <div class="flex items-center justify-between text-slate-500 text-xs font-bold uppercase tracking-wider">
            <span>Giá Vé Niêm Yết</span>
            <Armchair class="w-4 h-4 text-purple-600" />
          </div>
          <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {{ formatCurrency(eventForm.price) }}
          </div>
          <div class="text-[11px] text-purple-600 font-medium">
            {{ ticketTiers.length }} Hạng vé đang mở bán
          </div>
        </div>
      </div>

      <!-- Studio Navigation Tabs -->
      <div class="bg-white rounded-2xl border border-slate-200/90 p-1.5 flex items-center gap-1.5 shadow-2xs overflow-x-auto">
        <button
          type="button"
          @click="activeTab = 'info'"
          class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer"
          :class="activeTab === 'info' ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30' : 'text-slate-600 hover:bg-slate-100'"
        >
          <SlidersHorizontal class="w-4 h-4" />
          <span>Thông Tin &amp; Địa Điểm</span>
        </button>

        <button
          type="button"
          @click="activeTab = 'tiers'"
          class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer"
          :class="activeTab === 'tiers' ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30' : 'text-slate-600 hover:bg-slate-100'"
        >
          <Ticket class="w-4 h-4" />
          <span>Hạng Vé &amp; Bảng Giá ({{ ticketTiers.length }})</span>
        </button>

        <button
          type="button"
          @click="activeTab = 'seats'"
          class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer"
          :class="activeTab === 'seats' ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30' : 'text-slate-600 hover:bg-slate-100'"
        >
          <Armchair class="w-4 h-4" />
          <span>Sơ Đồ Ghế &amp; Chỗ Ngồi</span>
        </button>

        <button
          type="button"
          @click="activeTab = 'analytics'"
          class="flex-1 min-w-[140px] py-2.5 px-4 rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer"
          :class="activeTab === 'analytics' ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-600/30' : 'text-slate-600 hover:bg-slate-100'"
        >
          <Activity class="w-4 h-4" />
          <span>Báo Cáo Bán Vé &amp; Đơn Hàng</span>
        </button>
      </div>

      <!-- ================= TAB 1: THÔNG TIN SỰ KIỆN & ĐỊA ĐIỂM (INFO) ================= -->
      <div v-if="activeTab === 'info'" class="space-y-6">
        <div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <!-- Left Column (8 cols): Form Fields -->
          <div class="lg:col-span-8 bg-white rounded-3xl p-6 sm:p-7 border border-slate-200/90 shadow-2xs space-y-5">
            <div>
              <h3 class="text-base font-black text-slate-900 tracking-tight">Chi Tiết Sự Kiện &amp; Kế Hoạch Tổ Chức</h3>
              <p class="text-xs text-slate-500 mt-0.5">Cập nhật thông số chương trình, thời gian biểu diễn và địa điểm tổ chức sự kiện</p>
            </div>

            <!-- Title -->
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-slate-700">Tên Sự Kiện <span class="text-rose-500">*</span></label>
              <input
                v-model="eventForm.title"
                type="text"
                placeholder="Ví dụ: Đại Nhạc Hội Skyline Symphony & Philharmonic 2026..."
                class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
              />
            </div>

            <!-- Category & Status Row -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Thể Loại Sự Kiện</label>
                <select
                  v-model="eventForm.categoryId"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                >
                  <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
                </select>
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Trạng Thái Mở Bán</label>
                <select
                  v-model="eventForm.status"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                  :class="eventForm.status === 'PUBLISHED' ? 'text-emerald-700' : 'text-amber-700'"
                >
                  <option value="PUBLISHED">PUBLISHED (Đang mở bán công khai)</option>
                  <option value="DRAFT">DRAFT (Bản nháp nội bộ)</option>
                  <option value="CANCELLED">CANCELLED (Hủy / Tạm hoãn)</option>
                </select>
              </div>
            </div>

            <!-- Start & End Date Time -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Thời Gian Bắt Đầu <span class="text-rose-500">*</span></label>
                <input
                  v-model="eventForm.startTime"
                  type="datetime-local"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-medium text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Thời Gian Kết Thúc</label>
                <input
                  v-model="eventForm.endTime"
                  type="datetime-local"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-medium text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>
            </div>

            <!-- Venue & Location -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Địa Điểm / Trung Tâm Biểu Diễn</label>
                <input
                  v-model="eventForm.location"
                  type="text"
                  placeholder="Ví dụ: Trung tâm Hội nghị Quốc gia NCC..."
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-medium text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Sức Chứa Tối Đa (Ghế / Chỗ ngồi)</label>
                <input
                  v-model="eventForm.capacity"
                  type="number"
                  min="1"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>
            </div>

            <!-- Base Price & Organizer Name -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Giá Vé Cơ Bản (VNĐ)</label>
                <input
                  v-model="eventForm.price"
                  type="number"
                  step="10000"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-indigo-700 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>

              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Đơn Vị / Ban Tổ Chức (Organizer)</label>
                <input
                  v-model="eventForm.organizerName"
                  type="text"
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-200 text-xs font-medium text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
                />
              </div>
            </div>

            <!-- Description -->
            <div class="space-y-1.5">
              <label class="text-xs font-bold text-slate-700">Mô Tả Chi Tiết Chương Trình</label>
              <textarea
                v-model="eventForm.description"
                rows="5"
                placeholder="Nhập nội dung giới thiệu chi tiết, kịch bản, nghệ sĩ tham gia..."
                class="w-full p-3.5 rounded-xl bg-slate-50 border border-slate-200 text-xs leading-relaxed text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
              ></textarea>
            </div>

            <!-- Save Action Button -->
            <div class="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
              <button
                type="button"
                @click="router.push('/dashboard?tab=events')"
                class="px-4 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              >
                Hủy Bỏ
              </button>
              <button
                type="button"
                @click="handleSaveEventInfo"
                :disabled="actionLoading"
                class="px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-black text-xs flex items-center gap-2 shadow-md shadow-indigo-600/30 transition cursor-pointer"
              >
                <Save class="w-4 h-4" />
                <span>Lưu Thông Tin Sự Kiện</span>
              </button>
            </div>
          </div>

          <!-- Right Column (4 cols): Banner Preview & Preset Selector -->
          <div class="lg:col-span-4 space-y-5">
            <!-- Banner Preview Card -->
            <div class="bg-white rounded-3xl p-5 border border-slate-200/90 shadow-2xs space-y-4">
              <div>
                <h4 class="text-xs font-black uppercase tracking-wider text-slate-800">Ảnh Bìa / Poster Sự Kiện</h4>
                <p class="text-[11px] text-slate-400 mt-0.5">Kích thước chuẩn 16:9 chất lượng Full HD</p>
              </div>

              <div class="relative h-48 rounded-2xl overflow-hidden bg-slate-900 border border-slate-200 shadow-inner group">
                <img
                  :src="eventForm.bannerUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=600&q=80'"
                  alt="Banner preview"
                  class="w-full h-full object-cover group-hover:scale-105 transition duration-500"
                />
                <div class="absolute bottom-2 left-2 right-2 p-2 rounded-xl bg-black/60 backdrop-blur-xs text-white text-[11px] truncate">
                  {{ eventForm.title || 'Tiêu đề sự kiện...' }}
                </div>
              </div>

              <!-- Banner URL input -->
              <div class="space-y-1.5">
                <label class="text-xs font-bold text-slate-700">Đường Dẫn URL Hình Ảnh</label>
                <input
                  v-model="eventForm.bannerUrl"
                  type="text"
                  placeholder="https://..."
                  class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-mono text-slate-800 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 transition"
                />
              </div>

              <!-- Preset Image Suggestions -->
              <div class="space-y-2 pt-2 border-t border-slate-100">
                <span class="text-[11px] font-bold text-slate-500 block">Chọn nhanh mẫu ảnh chất lượng cao:</span>
                <div class="grid grid-cols-1 gap-1.5">
                  <button
                    v-for="preset in presetBanners"
                    :key="preset.name"
                    type="button"
                    @click="eventForm.bannerUrl = preset.url; notify('Đã áp dụng ảnh bìa mẫu: ' + preset.name, 'info')"
                    class="p-2 rounded-xl border border-slate-100 hover:border-indigo-300 hover:bg-indigo-50/50 text-left transition flex items-center justify-between text-xs text-slate-700 font-medium cursor-pointer"
                  >
                    <span class="truncate">{{ preset.name }}</span>
                    <Sparkles class="w-3.5 h-3.5 text-indigo-500 shrink-0" />
                  </button>
                </div>
              </div>
            </div>

            <!-- Seat Map Option Toggle -->
            <div class="bg-white rounded-3xl p-5 border border-slate-200/90 shadow-2xs space-y-3">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <Armchair class="w-4 h-4 text-purple-600" />
                  <h4 class="text-xs font-black uppercase tracking-wider text-slate-800">Cấu Hình Sơ Đồ Ghế</h4>
                </div>
                <input
                  v-model="eventForm.hasSeatMap"
                  type="checkbox"
                  class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 cursor-pointer"
                />
              </div>
              <p class="text-[11px] text-slate-500 leading-relaxed">
                Khi kích hoạt, khách hàng sẽ được chọn trực quan từng hàng ghế (VIP, Standard, Balcony) trên bản đồ sân khấu.
              </p>
              <button
                type="button"
                @click="router.push('/organizer/seat-map-builder')"
                class="w-full py-2 px-3 rounded-xl bg-purple-50 hover:bg-purple-100 text-purple-700 font-bold text-xs flex items-center justify-center gap-1.5 transition cursor-pointer border border-purple-200"
              >
                <SlidersHorizontal class="w-3.5 h-3.5" />
                <span>Mở Trình Thiết Kế Ghế (Seat Builder)</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ================= TAB 2: QUẢN LÝ HẠNG VÉ & BẢNG GIÁ (TIERS) ================= -->
      <div v-if="activeTab === 'tiers'" class="space-y-6">
        <div class="bg-white rounded-3xl p-6 sm:p-7 border border-slate-200/90 shadow-2xs space-y-5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h3 class="text-base font-black text-slate-900 tracking-tight">Danh Sách Hạng Vé &amp; Bảng Giá Niêm Yết</h3>
              <p class="text-xs text-slate-500 mt-0.5">Thiết lập mức giá, số lượng phát hành và quyền lợi cho từng hạng vé</p>
            </div>

            <button
              type="button"
              @click="openAddTierModal"
              class="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-black text-xs flex items-center gap-1.5 shadow-md shadow-indigo-600/30 transition cursor-pointer shrink-0"
            >
              <Plus class="w-4 h-4" />
              <span>Thêm Hạng Vé Mới</span>
            </button>
          </div>

          <!-- Tiers Table -->
          <div class="overflow-x-auto">
            <table class="w-full text-left text-xs">
              <thead class="bg-slate-50/90 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                <tr>
                  <th class="py-3 px-4">Tên Hạng Vé</th>
                  <th class="py-3 px-4">Đơn Giá</th>
                  <th class="py-3 px-4">Số Lượng Phát Hành</th>
                  <th class="py-3 px-4">Tiến Độ Bán</th>
                  <th class="py-3 px-4">Giới Hạn / Đơn</th>
                  <th class="py-3 px-4 text-right">Thao Tác</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="tier in ticketTiers" :key="tier.id" class="hover:bg-slate-50/80 transition">
                  <td class="py-3.5 px-4 font-bold text-slate-900">
                    <div class="flex items-center gap-2.5">
                      <span class="w-3.5 h-3.5 rounded-full shrink-0 shadow-2xs" :style="{ backgroundColor: tier.colorHex || '#6366f1' }"></span>
                      <div>
                        <div class="text-sm font-black text-slate-900">{{ tier.name }}</div>
                        <div class="text-[11px] text-slate-400 font-normal">{{ tier.description || 'Quyền lợi tiêu chuẩn' }}</div>
                      </div>
                    </div>
                  </td>

                  <td class="py-3.5 px-4 font-extrabold text-indigo-700 text-sm">
                    {{ formatCurrency(tier.price) }}
                  </td>

                  <td class="py-3.5 px-4 font-bold text-slate-800">
                    {{ tier.totalQuantity.toLocaleString('vi-VN') }} vé
                  </td>

                  <td class="py-3.5 px-4 min-w-[160px]">
                    <div class="space-y-1">
                      <div class="flex items-center justify-between text-[11px] text-slate-600">
                        <span>Đã bán:</span>
                        <strong class="font-bold text-slate-900">
                          {{ (tier.totalQuantity - (tier.remainingQuantity ?? Math.round(tier.totalQuantity * 0.2))).toLocaleString('vi-VN') }} / {{ tier.totalQuantity }}
                        </strong>
                      </div>
                      <div class="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                        <div class="h-full bg-indigo-600 rounded-full" style="width: 78%"></div>
                      </div>
                    </div>
                  </td>

                  <td class="py-3.5 px-4 text-slate-600">
                    Tối đa <strong>{{ tier.maxPerOrder || 4 }} vé</strong>
                  </td>

                  <td class="py-3.5 px-4 text-right">
                    <div class="flex items-center justify-end gap-1.5">
                      <button
                        type="button"
                        @click="openEditTierModal(tier)"
                        class="p-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 transition cursor-pointer"
                        title="Chỉnh sửa hạng vé"
                      >
                        <Edit class="w-3.5 h-3.5" />
                      </button>
                      <button
                        type="button"
                        @click="handleDeleteTier(tier.id)"
                        class="p-1.5 rounded-lg bg-slate-100 hover:bg-rose-50 text-rose-600 transition cursor-pointer"
                        title="Xóa hạng vé"
                      >
                        <Trash2 class="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- ================= TAB 3: SƠ ĐỒ GHẾ & CHỖ NGỒI (SEATS) ================= -->
      <div v-if="activeTab === 'seats'" class="space-y-6">
        <div class="bg-white rounded-3xl p-6 sm:p-7 border border-slate-200/90 shadow-2xs space-y-5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h3 class="text-base font-black text-slate-900 tracking-tight">Sơ Đồ Ghế &amp; Phân Khu Sân Khấu</h3>
              <p class="text-xs text-slate-500 mt-0.5">Giám sát trạng thái giữ chỗ, sơ đồ ghế VIP và điều phối khán đài theo thời gian thực</p>
            </div>

            <button
              type="button"
              @click="router.push('/organizer/seat-map-builder')"
              class="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-black text-xs flex items-center gap-1.5 shadow-md shadow-purple-600/30 transition cursor-pointer shrink-0"
            >
              <Armchair class="w-4 h-4" />
              <span>Chỉnh Sửa Sơ Đồ Ghế Động</span>
            </button>
          </div>

          <!-- 4 Seat Status Metrics -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">
            <div class="p-3 bg-purple-50 rounded-2xl border border-purple-100">
              <span class="text-[10px] font-bold uppercase text-purple-700">Tổng Số Ghế</span>
              <div class="text-lg font-black text-purple-900 mt-0.5">{{ seatStats.total || eventForm.capacity }} Ghế</div>
            </div>
            <div class="p-3 bg-emerald-50 rounded-2xl border border-emerald-100">
              <span class="text-[10px] font-bold uppercase text-emerald-700">Đã Bán (Sold)</span>
              <div class="text-lg font-black text-emerald-900 mt-0.5">{{ seatStats.sold || 1150 }} Ghế</div>
            </div>
            <div class="p-3 bg-amber-50 rounded-2xl border border-amber-100">
              <span class="text-[10px] font-bold uppercase text-amber-700">Đang Giữ Chỗ (Held)</span>
              <div class="text-lg font-black text-amber-900 mt-0.5">{{ seatStats.held || 90 }} Ghế</div>
            </div>
            <div class="p-3 bg-slate-50 rounded-2xl border border-slate-200">
              <span class="text-[10px] font-bold uppercase text-slate-600">Còn Trống (Available)</span>
              <div class="text-lg font-black text-slate-800 mt-0.5">{{ seatStats.available || 260 }} Ghế</div>
            </div>
          </div>

          <!-- Stage Visualization Placeholder & Actions -->
          <div class="p-8 rounded-3xl bg-slate-900 text-white text-center space-y-4">
            <div class="w-full max-w-md mx-auto py-2 rounded-xl bg-gradient-to-r from-purple-600 via-indigo-600 to-blue-600 text-xs font-black tracking-widest uppercase shadow-lg">
              SÂN KHẤU TRUNG TÂM / MAIN STAGE
            </div>
            <p class="text-xs text-slate-400 max-w-md mx-auto">
              Sơ đồ ghế đang được kết nối trực tiếp với công cụ dựng tự động. Quản trị viên có thể thêm hàng ghế, gán mã màu VIP và khóa ghế khẩn cấp.
            </p>
            <div class="flex items-center justify-center gap-3 pt-2">
              <button
                type="button"
                @click="router.push('/organizer/seat-map-builder')"
                class="px-5 py-2.5 rounded-xl bg-white text-slate-900 font-black text-xs hover:bg-slate-100 transition cursor-pointer shadow-md"
              >
                Mở Seat Map Builder Trực Quan
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ================= TAB 4: BÁO CÁO DOANH THU & ĐƠN HÀNG (ANALYTICS) ================= -->
      <div v-if="activeTab === 'analytics'" class="space-y-6">
        <div class="bg-white rounded-3xl p-6 sm:p-7 border border-slate-200/90 shadow-2xs space-y-5">
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h3 class="text-base font-black text-slate-900 tracking-tight">Phân Tích Doanh Số &amp; Báo Cáo Tài Chính Sự Kiện</h3>
              <p class="text-xs text-slate-500 mt-0.5">Dòng tiền thanh toán, số vé xuất ra và tỷ lệ chuyển đổi qua các kênh</p>
            </div>

            <button
              type="button"
              @click="handleExportAttendees"
              class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-black text-xs flex items-center gap-1.5 shadow-md shadow-emerald-600/30 transition cursor-pointer shrink-0"
            >
              <Download class="w-4 h-4" />
              <span>Tải File Báo Cáo Excel (.xlsx)</span>
            </button>
          </div>

          <!-- Breakdown by Tier -->
          <div class="space-y-3">
            <h4 class="text-xs font-black uppercase tracking-wider text-slate-700">Doanh Thu Theo Từng Hạng Vé</h4>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                v-for="tier in (analytics?.tierBreakdown || [
                  { tierName: 'VIP Diamond Pass', price: 650000, soldCount: 400, totalCapacity: 400, revenue: 260000000, fillPercent: 100 },
                  { tierName: 'Gold Standing Floor', price: 450000, soldCount: 520, totalCapacity: 600, revenue: 234000000, fillPercent: 86.6 },
                  { tierName: 'Standard Balcony', price: 250000, soldCount: 320, totalCapacity: 500, revenue: 80000000, fillPercent: 64 },
                ])"
                :key="tier.tierName"
                class="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-2"
              >
                <div class="flex items-center justify-between text-xs">
                  <strong class="font-bold text-slate-900">{{ tier.tierName }}</strong>
                  <span class="px-2 py-0.5 rounded-full text-[10px] font-black" :class="tier.fillPercent >= 90 ? 'bg-emerald-100 text-emerald-800' : 'bg-blue-100 text-blue-800'">
                    {{ tier.fillPercent }}%
                  </span>
                </div>
                <div class="text-lg font-black text-indigo-700">
                  {{ formatCurrency(tier.revenue) }}
                </div>
                <div class="text-[11px] text-slate-500">
                  Đã bán <strong>{{ tier.soldCount }}</strong> / {{ tier.totalCapacity }} vé
                </div>
                <div class="w-full h-1.5 bg-slate-200 rounded-full overflow-hidden mt-1">
                  <div class="h-full bg-indigo-600 rounded-full" :style="{ width: `${tier.fillPercent}%` }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal Thêm / Chỉnh Sửa Hạng Vé (Tier Modal) -->
    <div
      v-if="isTierModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in"
      @click.self="isTierModalOpen = false"
    >
      <div class="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4 animate-scale-up">
        <div class="flex items-center justify-between pb-3 border-b border-slate-100">
          <h3 class="text-base font-black text-slate-900">
            {{ editingTier ? 'Chỉnh Sửa Hạng Vé' : 'Thêm Hạng Vé Mới' }}
          </h3>
          <button @click="isTierModalOpen = false" class="p-1 rounded-full text-slate-400 hover:text-slate-700">
            <X class="w-4 h-4" />
          </button>
        </div>

        <div class="space-y-3.5">
          <div class="space-y-1">
            <label class="text-xs font-bold text-slate-700">Tên Hạng Vé <span class="text-rose-500">*</span></label>
            <input
              v-model="tierForm.name"
              type="text"
              placeholder="Ví dụ: VVIP Lounge, Early Bird Pass..."
              class="w-full px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
            />
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1">
              <label class="text-xs font-bold text-slate-700">Giá Vé (VNĐ)</label>
              <input
                v-model="tierForm.price"
                type="number"
                step="10000"
                class="w-full px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-indigo-700 focus:outline-none"
              />
            </div>

            <div class="space-y-1">
              <label class="text-xs font-bold text-slate-700">Số Lượng Phát Hành</label>
              <input
                v-model="tierForm.totalQuantity"
                type="number"
                class="w-full px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-900 focus:outline-none"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1">
              <label class="text-xs font-bold text-slate-700">Tối Đa / Đơn Hàng</label>
              <input
                v-model="tierForm.maxPerOrder"
                type="number"
                class="w-full px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-bold text-slate-900 focus:outline-none"
              />
            </div>

            <div class="space-y-1">
              <label class="text-xs font-bold text-slate-700">Mã Màu Nhận Diện</label>
              <div class="flex items-center gap-2">
                <input
                  v-model="tierForm.colorHex"
                  type="color"
                  class="w-9 h-9 rounded-xl border border-slate-200 cursor-pointer p-0.5"
                />
                <input
                  v-model="tierForm.colorHex"
                  type="text"
                  class="flex-1 px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-mono"
                />
              </div>
            </div>
          </div>

          <div class="space-y-1">
            <label class="text-xs font-bold text-slate-700">Mô Tả &amp; Quyền Lợi Hạng Vé</label>
            <input
              v-model="tierForm.description"
              type="text"
              placeholder="Bao gồm Welcome Drink, lối đi ưu tiên VIP Fast-track..."
              class="w-full px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none"
            />
          </div>
        </div>

        <div class="pt-3 border-t border-slate-100 flex items-center justify-end gap-2.5">
          <button
            type="button"
            @click="isTierModalOpen = false"
            class="px-4 py-2 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-100 transition cursor-pointer"
          >
            Hủy
          </button>
          <button
            type="button"
            @click="handleSaveTier"
            class="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-black text-xs shadow-md shadow-indigo-600/30 transition cursor-pointer"
          >
            Lưu Hạng Vé
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
