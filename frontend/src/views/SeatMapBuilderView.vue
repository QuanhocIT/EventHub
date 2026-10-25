<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { eventService } from '@/services/event.service'
import type { Event, EventSeat, Venue } from '@/types/event'
import {
  Armchair,
  Layers,
  Crown,
  Lock,
  Unlock,
  DollarSign,
  Save,
  RotateCcw,
  Eye,
  Sliders,
  Sparkles,
  ArrowLeft,
  CheckCircle2,
  Trash2,
  Plus,
  Minus,
  LayoutGrid,
  TrendingUp,
  Settings,
  HelpCircle
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// State
const events = ref<Event[]>([])
const venues = ref<Venue[]>([])
const selectedEventId = ref<number | null>(null)
const selectedVenueId = ref<number | null>(null)
const applyMode = ref<'event' | 'venue'>('event')

// Notification state
const notification = ref<{ text: string; type: 'success' | 'error' | 'info' } | null>(null)
const showNotification = (text: string, type: 'success' | 'error' | 'info' = 'info') => {
  notification.value = { text, type }
  setTimeout(() => {
    notification.value = null
  }, 4000)
}

// Builder Canvas Model
export interface SeatCell {
  id: string // e.g. "A-1"
  dbId?: number
  row: string
  number: number
  zone: 'VIP' | 'STANDARD' | 'BALCONY'
  price: number
  status: 'AVAILABLE' | 'BLOCKED'
  isSelected?: boolean
}

const numRows = ref(7)
const seatsPerRow = ref(12)
const defaultVipPrice = ref(650000)
const defaultStandardPrice = ref(350000)
const defaultBalconyPrice = ref(200000)
const aisleGapIndex = ref(6) // Gap after seat 6

// Active Tool
type ToolMode = 'select' | 'paint_vip' | 'paint_std' | 'paint_balcony' | 'paint_block'
const activeTool = ref<ToolMode>('select')

// Seats Matrix
const seatGrid = ref<SeatCell[][]>([])
const selectedSeatIds = ref<Set<string>>(new Set())
const isSaving = ref(false)
const isPreviewMode = ref(false)
const previewSelectedSeats = ref<SeatCell[]>([])

// Generate empty layout based on rows & columns
const generateLayout = () => {
  const rows: SeatCell[][] = []
  for (let r = 0; r < numRows.value; r++) {
    const rowLabel = String.fromCharCode(65 + r)
    const rowSeats: SeatCell[] = []
    const isVipRow = r < 2 // first 2 rows VIP by default
    const isBalconyRow = r >= numRows.value - 2 // last 2 rows Balcony if large enough

    const zone: 'VIP' | 'STANDARD' | 'BALCONY' = isVipRow
      ? 'VIP'
      : (isBalconyRow && numRows.value > 5 ? 'BALCONY' : 'STANDARD')
    const price = zone === 'VIP'
      ? defaultVipPrice.value
      : (zone === 'BALCONY' ? defaultBalconyPrice.value : defaultStandardPrice.value)

    for (let s = 1; s <= seatsPerRow.value; s++) {
      rowSeats.push({
        id: `${rowLabel}-${s}`,
        row: rowLabel,
        number: s,
        zone,
        price,
        status: 'AVAILABLE',
      })
    }
    rows.push(rowSeats)
  }
  seatGrid.value = rows
  selectedSeatIds.value.clear()
}

// Preset Handlers
const applyPreset = (preset: 'opera' | 'arena' | 'acoustic') => {
  if (preset === 'opera') {
    numRows.value = 8
    seatsPerRow.value = 14
    generateLayout()
    // First 3 rows VIP
    seatGrid.value.forEach((row, rIdx) => {
      row.forEach((seat) => {
        if (rIdx < 3) {
          seat.zone = 'VIP'
          seat.price = defaultVipPrice.value
        } else if (rIdx >= 6) {
          seat.zone = 'BALCONY'
          seat.price = defaultBalconyPrice.value
        } else {
          seat.zone = 'STANDARD'
          seat.price = defaultStandardPrice.value
        }
      })
    })
    showNotification('Đã áp dụng mẫu Nhà Hát Kịch (3 hàng VIP, 3 hàng Tiêu chuẩn, 2 hàng Ban công)', 'success')
  } else if (preset === 'arena') {
    numRows.value = 10
    seatsPerRow.value = 16
    generateLayout()
    // Center cluster VIP
    seatGrid.value.forEach((row, rIdx) => {
      row.forEach((seat) => {
        if (rIdx < 4 && seat.number >= 4 && seat.number <= 13) {
          seat.zone = 'VIP'
          seat.price = defaultVipPrice.value
        } else if (rIdx >= 8) {
          seat.zone = 'BALCONY'
          seat.price = defaultBalconyPrice.value
        } else {
          seat.zone = 'STANDARD'
          seat.price = defaultStandardPrice.value
        }
      })
    })
    showNotification('Đã áp dụng mẫu Đấu Trường Arena (Khu vực VIP trung tâm)', 'success')
  } else {
    numRows.value = 5
    seatsPerRow.value = 10
    generateLayout()
    seatGrid.value.forEach((row) => {
      row.forEach((seat) => {
        seat.zone = 'VIP'
        seat.price = defaultVipPrice.value
      })
    })
    showNotification('Đã áp dụng mẫu Đêm Nhạc Acoustic (Toàn bộ vé VIP)', 'success')
  }
}

// Handle Seat Interaction
const handleSeatClick = (seat: SeatCell) => {
  if (isPreviewMode.value) {
    if (seat.status === 'BLOCKED') return
    const exists = previewSelectedSeats.value.find((s) => s.id === seat.id)
    if (exists) {
      previewSelectedSeats.value = previewSelectedSeats.value.filter((s) => s.id !== seat.id)
    } else {
      previewSelectedSeats.value.push(seat)
    }
    return
  }

  if (activeTool.value === 'paint_vip') {
    seat.zone = 'VIP'
    seat.price = defaultVipPrice.value
    seat.status = 'AVAILABLE'
  } else if (activeTool.value === 'paint_std') {
    seat.zone = 'STANDARD'
    seat.price = defaultStandardPrice.value
    seat.status = 'AVAILABLE'
  } else if (activeTool.value === 'paint_balcony') {
    seat.zone = 'BALCONY'
    seat.price = defaultBalconyPrice.value
    seat.status = 'AVAILABLE'
  } else if (activeTool.value === 'paint_block') {
    seat.status = seat.status === 'BLOCKED' ? 'AVAILABLE' : 'BLOCKED'
  } else {
    // Select mode
    if (selectedSeatIds.value.has(seat.id)) {
      selectedSeatIds.value.delete(seat.id)
    } else {
      selectedSeatIds.value.add(seat.id)
    }
  }
}

// Row-level batch selection
const toggleSelectRow = (rowLabel: string) => {
  const targetRow = seatGrid.value.find((r) => r.length > 0 && r[0].row === rowLabel)
  if (!targetRow) return

  const allSelected = targetRow.every((s) => selectedSeatIds.value.has(s.id))
  targetRow.forEach((s) => {
    if (allSelected) {
      selectedSeatIds.value.delete(s.id)
    } else {
      selectedSeatIds.value.add(s.id)
    }
  })
}

// Batch Actions for Selected Seats
const batchSetZone = (zone: 'VIP' | 'STANDARD' | 'BALCONY') => {
  const price = zone === 'VIP' ? defaultVipPrice.value : (zone === 'BALCONY' ? defaultBalconyPrice.value : defaultStandardPrice.value)
  seatGrid.value.forEach((row) => {
    row.forEach((seat) => {
      if (selectedSeatIds.value.has(seat.id)) {
        seat.zone = zone
        seat.price = price
        seat.status = 'AVAILABLE'
      }
    })
  })
  showNotification(`Đã đổi ${selectedSeatIds.value.size} ghế sang hạng ${zone}`, 'info')
}

const batchToggleBlock = () => {
  seatGrid.value.forEach((row) => {
    row.forEach((seat) => {
      if (selectedSeatIds.value.has(seat.id)) {
        seat.status = seat.status === 'BLOCKED' ? 'AVAILABLE' : 'BLOCKED'
      }
    })
  })
  showNotification(`Đã cập nhật trạng thái Khóa/Mở cho ${selectedSeatIds.value.size} ghế`, 'info')
}

const batchSetCustomPrice = (price: number) => {
  if (price < 0) return
  seatGrid.value.forEach((row) => {
    row.forEach((seat) => {
      if (selectedSeatIds.value.has(seat.id)) {
        seat.price = price
      }
    })
  })
  showNotification(`Đã gán giá ${price.toLocaleString('vi-VN')} đ cho ${selectedSeatIds.value.size} ghế`, 'info')
}

// Live Calculations & Revenue Forecast
const stats = computed(() => {
  let total = 0
  let vipCount = 0
  let stdCount = 0
  let balconyCount = 0
  let blockedCount = 0
  let maxRevenue = 0

  seatGrid.value.forEach((row) => {
    row.forEach((seat) => {
      total++
      if (seat.status === 'BLOCKED') {
        blockedCount++
      } else {
        if (seat.zone === 'VIP') vipCount++
        else if (seat.zone === 'BALCONY') balconyCount++
        else stdCount++

        maxRevenue += Number(seat.price || 0)
      }
    })
  })

  return {
    total,
    available: total - blockedCount,
    vipCount,
    stdCount,
    balconyCount,
    blockedCount,
    maxRevenue,
  }
})

// Load Event or Venue Existing Seat Map
const loadExistingEventSeats = async (eventId: number) => {
  try {
    const seats = await eventService.getSeatMap(eventId)
    if (seats && seats.length > 0) {
      // Group by row
      const rowMap: Record<string, EventSeat[]> = {}
      seats.forEach((s) => {
        const r = s.seatRow || s.rowName || 'A'
        if (!rowMap[r]) rowMap[r] = []
        rowMap[r].push(s)
      })

      const rows: SeatCell[][] = []
      const sortedRowKeys = Object.keys(rowMap).sort()
      numRows.value = sortedRowKeys.length

      let maxCols = 0
      sortedRowKeys.forEach((r) => {
        const list = rowMap[r].sort((a, b) => a.seatNumber - b.seatNumber)
        if (list.length > maxCols) maxCols = list.length
        rows.push(
          list.map((s) => ({
            id: `${r}-${s.seatNumber}`,
            dbId: s.id,
            row: r,
            number: s.seatNumber,
            zone: (s.zone as any) || 'STANDARD',
            price: s.price || defaultStandardPrice.value,
            status: s.status === 'BLOCKED' ? 'BLOCKED' : 'AVAILABLE',
          }))
        )
      })
      seatsPerRow.value = maxCols
      seatGrid.value = rows
      showNotification(`Đã tải thành công sơ đồ ghế của sự kiện (${seats.length} ghế)`, 'success')
      return
    }
  } catch (err) {
    console.warn('Could not load existing event seats:', err)
  }
  generateLayout()
}

// Save & Deploy Seat Map
const handleSave = async () => {
  if (seatGrid.value.length === 0) return
  isSaving.value = true

  const flattenedSeats: any[] = []
  seatGrid.value.forEach((row) => {
    row.forEach((seat) => {
      flattenedSeats.push({
        seatRow: seat.row,
        seatNumber: seat.number,
        zone: seat.zone,
        price: seat.price,
        status: seat.status,
      })
    })
  })

  try {
    if (applyMode.value === 'event' && selectedEventId.value) {
      await eventService.configureEventSeats(selectedEventId.value, {
        venueId: selectedVenueId.value || undefined,
        defaultPrice: defaultStandardPrice.value,
        seats: flattenedSeats,
      })
      showNotification('LƯU THÀNH CÔNG: Sơ đồ ghế đã được cập nhật trực tiếp lên Sự Kiện!', 'success')
    } else if (applyMode.value === 'venue' && selectedVenueId.value) {
      await eventService.configureVenueLayout(selectedVenueId.value, {
        seats: flattenedSeats.map((s) => ({
          seatRow: s.seatRow,
          seatNumber: s.seatNumber,
          zone: s.zone,
        })),
      })
      showNotification('LƯU THÀNH CÔNG: Sơ đồ đã được lưu làm Layout chuẩn của Địa Điểm!', 'success')
    } else {
      showNotification('Vui lòng chọn Sự Kiện hoặc Địa Điểm để lưu sơ đồ!', 'error')
    }
  } catch (err: any) {
    showNotification(err.response?.data?.message || err.message || 'Lỗi khi lưu sơ đồ ghế', 'error')
  } finally {
    isSaving.value = false
  }
}

// Initial Data Fetch
onMounted(async () => {
  try {
    const [evList, vnList] = await Promise.all([
      eventService.getEvents({ page: 0, size: 50 }),
      eventService.getVenues(),
    ])
    events.value = evList.content || []
    venues.value = vnList || []

    const queryEventId = Number(route.query.eventId)
    if (queryEventId) {
      selectedEventId.value = queryEventId
      await loadExistingEventSeats(queryEventId)
    } else if (events.value.length > 0) {
      selectedEventId.value = events.value[0].id
      await loadExistingEventSeats(events.value[0].id)
    } else {
      generateLayout()
    }

    if (venues.value.length > 0) {
      selectedVenueId.value = venues.value[0].id
    }
  } catch {
    generateLayout()
  }
})

watch(selectedEventId, (newId) => {
  if (newId && applyMode.value === 'event') {
    loadExistingEventSeats(newId)
  }
})
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-amber-500 selection:text-slate-950">
    <!-- Notification Banner -->
    <div
      v-if="notification"
      class="fixed top-5 right-5 z-50 p-4 rounded-2xl shadow-2xl flex items-center gap-3 text-xs font-bold transition-all animate-bounce"
      :class="{
        'bg-emerald-500 text-slate-950': notification.type === 'success',
        'bg-rose-500 text-white': notification.type === 'error',
        'bg-indigo-600 text-white': notification.type === 'info'
      }"
    >
      <CheckCircle2 class="w-4 h-4" />
      <span>{{ notification.text }}</span>
    </div>

    <!-- 1. TOP CONTROL BAR -->
    <header class="border-b border-slate-800 bg-slate-900/90 backdrop-blur-md sticky top-0 z-30 px-4 py-3">
      <div class="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-3">
        <!-- Back & Title -->
        <div class="flex items-center gap-3 w-full sm:w-auto">
          <button
            type="button"
            @click="router.push('/dashboard?tab=org_events')"
            class="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition cursor-pointer"
            title="Quay lại Quản lý Sự Kiện"
          >
            <ArrowLeft class="w-5 h-5" />
          </button>

          <div>
            <div class="flex items-center gap-2">
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-amber-500/20 text-amber-400 border border-amber-500/30">
                <Sparkles class="w-2.5 h-2.5" />
                ORGANIZER STUDIO PRO
              </span>
              <span class="text-xs text-slate-400">Visual Seat Map Builder</span>
            </div>
            <h1 class="text-base sm:text-lg font-black tracking-tight text-white flex items-center gap-2">
              Công Cụ Thiết Kế Sơ Đồ Ghế & Phân Hạng Giá Trực Quan
            </h1>
          </div>
        </div>

        <!-- Target Selector & Save Actions -->
        <div class="flex items-center gap-2.5 w-full sm:w-auto justify-end">
          <!-- Target Switcher -->
          <div class="flex items-center rounded-xl bg-slate-800 p-1 border border-slate-700 text-xs font-bold">
            <button
              type="button"
              @click="applyMode = 'event'"
              class="px-3 py-1.5 rounded-lg transition cursor-pointer"
              :class="applyMode === 'event' ? 'bg-amber-500 text-slate-950 font-black' : 'text-slate-400 hover:text-white'"
            >
              Áp Dụng Sự Kiện
            </button>
            <button
              type="button"
              @click="applyMode = 'venue'"
              class="px-3 py-1.5 rounded-lg transition cursor-pointer"
              :class="applyMode === 'venue' ? 'bg-amber-500 text-slate-950 font-black' : 'text-slate-400 hover:text-white'"
            >
              Lưu Thành Địa Điểm
            </button>
          </div>

          <!-- Event Dropdown -->
          <select
            v-if="applyMode === 'event'"
            v-model="selectedEventId"
            class="bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white font-bold text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none max-w-[200px] truncate"
          >
            <option v-for="ev in events" :key="ev.id" :value="ev.id">
              {{ ev.title }}
            </option>
          </select>

          <!-- Venue Dropdown -->
          <select
            v-else
            v-model="selectedVenueId"
            class="bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-white font-bold text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none max-w-[200px] truncate"
          >
            <option v-for="vn in venues" :key="vn.id" :value="vn.id">
              {{ vn.name }} ({{ vn.city }})
            </option>
          </select>

          <!-- Preview Mode Toggle -->
          <button
            type="button"
            @click="isPreviewMode = !isPreviewMode"
            class="p-2 sm:px-3 sm:py-2 rounded-xl border border-slate-700 text-xs font-bold transition flex items-center gap-1.5 cursor-pointer"
            :class="isPreviewMode ? 'bg-indigo-600 text-white' : 'bg-slate-800 text-slate-300 hover:text-white'"
            :title="isPreviewMode ? 'Thoát xem trước' : 'Xem trước như khán giả'"
          >
            <Eye class="w-4 h-4" />
            <span class="hidden sm:inline">{{ isPreviewMode ? 'Chế Độ Thiết Kế' : 'Xem Trước' }}</span>
          </button>

          <!-- Save Button -->
          <button
            type="button"
            @click="handleSave"
            :disabled="isSaving"
            class="px-4 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-xs flex items-center gap-1.5 shadow-lg transition cursor-pointer disabled:opacity-50"
          >
            <Save class="w-4 h-4" />
            <span>{{ isSaving ? 'Đang Lưu...' : 'Lưu Sơ Đồ' }}</span>
          </button>
        </div>
      </div>
    </header>

    <!-- 2. STUDIO WORKSPACE -->
    <main class="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 grid grid-cols-1 lg:grid-cols-12 gap-5">
      <!-- LEFT / TOP: TOOLBAR & CONTROLS (3 Cols) -->
      <aside class="lg:col-span-3 space-y-4">
        <!-- 1. Tool Mode Selector -->
        <div class="p-4 rounded-3xl bg-slate-900 border border-slate-800 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-xs font-black uppercase tracking-wider text-slate-400">Công Cụ Vẽ Nhanh</span>
            <span class="text-[10px] text-amber-400 font-bold">Chạm để chọn</span>
          </div>

          <div class="grid grid-cols-2 gap-2 text-xs font-bold">
            <button
              type="button"
              @click="activeTool = 'select'"
              class="p-2.5 rounded-xl border flex items-center gap-2 transition cursor-pointer"
              :class="activeTool === 'select' ? 'bg-amber-500 text-slate-950 border-amber-400 font-black' : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'"
            >
              <Armchair class="w-4 h-4" />
              <span>Chọn / Soi Ghế</span>
            </button>

            <button
              type="button"
              @click="activeTool = 'paint_vip'"
              class="p-2.5 rounded-xl border flex items-center gap-2 transition cursor-pointer"
              :class="activeTool === 'paint_vip' ? 'bg-amber-500 text-slate-950 border-amber-400 font-black' : 'bg-slate-800 text-amber-300 border-slate-700 hover:bg-slate-700'"
            >
              <Crown class="w-4 h-4" />
              <span>Cọ Vé VIP</span>
            </button>

            <button
              type="button"
              @click="activeTool = 'paint_std'"
              class="p-2.5 rounded-xl border flex items-center gap-2 transition cursor-pointer"
              :class="activeTool === 'paint_std' ? 'bg-amber-500 text-slate-950 border-amber-400 font-black' : 'bg-slate-800 text-indigo-300 border-slate-700 hover:bg-slate-700'"
            >
              <Armchair class="w-4 h-4" />
              <span>Cọ Standard</span>
            </button>

            <button
              type="button"
              @click="activeTool = 'paint_balcony'"
              class="p-2.5 rounded-xl border flex items-center gap-2 transition cursor-pointer"
              :class="activeTool === 'paint_balcony' ? 'bg-amber-500 text-slate-950 border-amber-400 font-black' : 'bg-slate-800 text-purple-300 border-slate-700 hover:bg-slate-700'"
            >
              <Layers class="w-4 h-4" />
              <span>Cọ Balcony</span>
            </button>

            <button
              type="button"
              @click="activeTool = 'paint_block'"
              class="col-span-2 p-2.5 rounded-xl border flex items-center justify-center gap-2 transition cursor-pointer"
              :class="activeTool === 'paint_block' ? 'bg-rose-500 text-white border-rose-400 font-black' : 'bg-slate-800 text-rose-300 border-slate-700 hover:bg-slate-700'"
            >
              <Lock class="w-4 h-4" />
              <span>Cọ Khóa Ghế (Kỹ thuật/Khách mời)</span>
            </button>
          </div>
        </div>

        <!-- 2. Grid Dimensions & Presets -->
        <div class="p-4 rounded-3xl bg-slate-900 border border-slate-800 space-y-3.5 text-xs">
          <span class="text-xs font-black uppercase tracking-wider text-slate-400 block">Kích Thước Khán Đài</span>

          <!-- Rows Slider -->
          <div>
            <div class="flex justify-between text-slate-300 font-bold mb-1">
              <span>Số Hàng Ghế (A - {{ String.fromCharCode(64 + numRows) }}):</span>
              <span class="font-mono text-amber-400 font-black">{{ numRows }} hàng</span>
            </div>
            <input
              type="range"
              min="2"
              max="20"
              v-model.number="numRows"
              @change="generateLayout"
              class="w-full accent-amber-500 cursor-pointer"
            />
          </div>

          <!-- Seats Per Row Slider -->
          <div>
            <div class="flex justify-between text-slate-300 font-bold mb-1">
              <span>Ghế Mỗi Hàng:</span>
              <span class="font-mono text-amber-400 font-black">{{ seatsPerRow }} ghế</span>
            </div>
            <input
              type="range"
              min="4"
              max="24"
              v-model.number="seatsPerRow"
              @change="generateLayout"
              class="w-full accent-amber-500 cursor-pointer"
            />
          </div>

          <!-- Presets -->
          <div class="pt-2 border-t border-slate-800">
            <span class="text-[11px] text-slate-400 font-bold block mb-2">Mẫu khán đài chuẩn:</span>
            <div class="flex flex-col gap-1.5">
              <button
                type="button"
                @click="applyPreset('opera')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-left font-bold text-xs transition cursor-pointer"
              >
                Nhà Hát Opera (3 Hàng VIP)
              </button>
              <button
                type="button"
                @click="applyPreset('arena')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-left font-bold text-xs transition cursor-pointer"
              >
                Đấu Trường Arena (VIP Trung Tâm)
              </button>
              <button
                type="button"
                @click="applyPreset('acoustic')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-left font-bold text-xs transition cursor-pointer"
              >
                Phòng Trà Acoustic (100% VIP)
              </button>
            </div>
          </div>
        </div>

        <!-- 3. Pricing Tokens -->
        <div class="p-4 rounded-3xl bg-slate-900 border border-slate-800 space-y-3 text-xs">
          <span class="text-xs font-black uppercase tracking-wider text-slate-400 block">Cấu Hình Giá Chuẩn</span>

          <div class="space-y-2">
            <div>
              <label class="text-[11px] text-amber-300 font-bold block mb-1">Giá Vé VIP (đ)</label>
              <input
                v-model.number="defaultVipPrice"
                type="number"
                step="50000"
                class="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-white font-mono text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="text-[11px] text-indigo-300 font-bold block mb-1">Giá Vé Standard (đ)</label>
              <input
                v-model.number="defaultStandardPrice"
                type="number"
                step="50000"
                class="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-white font-mono text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none"
              />
            </div>
            <div>
              <label class="text-[11px] text-purple-300 font-bold block mb-1">Giá Vé Balcony (đ)</label>
              <input
                v-model.number="defaultBalconyPrice"
                type="number"
                step="50000"
                class="w-full px-3 py-2 rounded-xl bg-slate-800 border border-slate-700 text-white font-mono text-xs focus:ring-2 focus:ring-amber-500 focus:outline-none"
              />
            </div>
          </div>
        </div>
      </aside>

      <!-- RIGHT / MAIN: INTERACTIVE SEATING CANVAS (9 Cols) -->
      <section class="lg:col-span-9 space-y-4 flex flex-col">
        <!-- Live Forecast HUD Banner -->
        <div class="p-4 rounded-3xl bg-gradient-to-r from-slate-900 via-slate-850 to-slate-900 border border-slate-800 grid grid-cols-2 sm:grid-cols-5 gap-3 text-xs">
          <div>
            <span class="text-slate-400 text-[10px] uppercase font-bold block">Tổng Ghế Sức Chứa</span>
            <strong class="text-lg font-black font-mono text-white">{{ stats.total }}</strong>
          </div>
          <div>
            <span class="text-amber-400 text-[10px] uppercase font-bold block">Ghế VIP</span>
            <strong class="text-lg font-black font-mono text-amber-300">{{ stats.vipCount }}</strong>
          </div>
          <div>
            <span class="text-indigo-400 text-[10px] uppercase font-bold block">Ghế Standard</span>
            <strong class="text-lg font-black font-mono text-indigo-300">{{ stats.stdCount }}</strong>
          </div>
          <div>
            <span class="text-purple-400 text-[10px] uppercase font-bold block">Ghế Balcony</span>
            <strong class="text-lg font-black font-mono text-purple-300">{{ stats.balconyCount }}</strong>
          </div>
          <div class="col-span-2 sm:col-span-1 border-t sm:border-t-0 sm:border-l border-slate-800 pt-2 sm:pt-0 sm:pl-3">
            <span class="text-emerald-400 text-[10px] uppercase font-black block">Doanh Thu Tối Đa (GMV)</span>
            <strong class="text-sm sm:text-base font-black font-mono text-emerald-300 truncate block">
              {{ stats.maxRevenue.toLocaleString('vi-VN') }} đ
            </strong>
          </div>
        </div>

        <!-- SEAT GRID CANVAS BOARD -->
        <div class="p-6 rounded-3xl bg-slate-950 border-2 border-slate-800 shadow-2xl overflow-x-auto flex-1 flex flex-col items-center">
          <!-- 1. STAGE ILLUMINATION BANNER -->
          <div class="w-full max-w-2xl mb-8 flex flex-col items-center">
            <div class="w-full h-8 rounded-b-3xl bg-gradient-to-b from-amber-500/20 via-amber-500/5 to-transparent border-t-2 border-amber-400 flex items-center justify-center shadow-[0_0_25px_rgba(245,158,11,0.2)]">
              <span class="text-[11px] font-black uppercase tracking-widest text-amber-300 flex items-center gap-2">
                <Sparkles class="w-3.5 h-3.5" />
                SÂN KHẤU CHÍNH / MAIN STAGE
                <Sparkles class="w-3.5 h-3.5" />
              </span>
            </div>
            <div class="w-3/4 h-3 bg-gradient-to-b from-amber-500/10 to-transparent blur-sm"></div>
          </div>

          <!-- 2. SEAT ROWS MATRIX -->
          <div class="space-y-3 pb-6">
            <div
              v-for="row in seatGrid"
              :key="row[0]?.row"
              class="flex items-center gap-2 justify-center"
            >
              <!-- Row Label Clickable for Batch Selection -->
              <button
                type="button"
                @click="toggleSelectRow(row[0].row)"
                class="w-6 h-6 rounded-md bg-slate-900 hover:bg-slate-800 border border-slate-700 text-slate-300 hover:text-white font-mono font-bold text-xs flex items-center justify-center transition cursor-pointer shrink-0"
                :title="`Chọn toàn bộ hàng ${row[0].row}`"
              >
                {{ row[0].row }}
              </button>

              <!-- Row Seats -->
              <div class="flex items-center gap-1.5 sm:gap-2">
                <template v-for="seat in row" :key="seat.id">
                  <!-- Aisle Gap Divider -->
                  <div
                    v-if="seat.number === aisleGapIndex + 1"
                    class="w-4 sm:w-6"
                  ></div>

                  <!-- Individual Seat Button -->
                  <button
                    type="button"
                    @click="handleSeatClick(seat)"
                    class="w-8 h-8 sm:w-9 sm:h-9 rounded-xl border flex flex-col items-center justify-center text-[10px] font-mono font-bold transition transform active:scale-90 cursor-pointer relative group"
                    :class="{
                      // Selected in Builder
                      'ring-2 ring-white scale-105 z-10': selectedSeatIds.has(seat.id),
                      // Selected in Preview Mode
                      'bg-emerald-500 text-slate-950 border-emerald-300 ring-2 ring-emerald-400': isPreviewMode && previewSelectedSeats.some(s => s.id === seat.id),
                      // Normal Builder States
                      'bg-gradient-to-br from-amber-500/20 to-amber-600/30 text-amber-300 border-amber-500/60 hover:border-amber-300': seat.zone === 'VIP' && seat.status !== 'BLOCKED' && (!isPreviewMode || !previewSelectedSeats.some(s => s.id === seat.id)),
                      'bg-gradient-to-br from-indigo-500/20 to-indigo-600/30 text-indigo-300 border-indigo-500/60 hover:border-indigo-300': seat.zone === 'STANDARD' && seat.status !== 'BLOCKED' && (!isPreviewMode || !previewSelectedSeats.some(s => s.id === seat.id)),
                      'bg-gradient-to-br from-purple-500/20 to-purple-600/30 text-purple-300 border-purple-500/60 hover:border-purple-300': seat.zone === 'BALCONY' && seat.status !== 'BLOCKED' && (!isPreviewMode || !previewSelectedSeats.some(s => s.id === seat.id)),
                      'bg-slate-900 text-slate-600 border-slate-800 border-dashed opacity-50': seat.status === 'BLOCKED'
                    }"
                  >
                    <span v-if="seat.status === 'BLOCKED'">
                      <Lock class="w-3 h-3 text-rose-500/80" />
                    </span>
                    <span v-else>
                      {{ seat.number }}
                    </span>

                    <!-- Mini Crown for VIP -->
                    <Crown
                      v-if="seat.zone === 'VIP' && seat.status !== 'BLOCKED'"
                      class="w-2.5 h-2.5 text-amber-400 absolute -top-1 -right-1"
                    />

                    <!-- Tooltip on Hover -->
                    <div class="absolute bottom-full mb-1 hidden group-hover:flex flex-col items-center z-30 pointer-events-none">
                      <div class="px-2 py-1 rounded-md bg-slate-900 text-white text-[10px] font-sans font-bold shadow-xl border border-slate-700 whitespace-nowrap">
                        Ghế {{ seat.row }}-{{ seat.number }} &bull; {{ seat.zone }} ({{ seat.price.toLocaleString('vi-VN') }}đ)
                        <span v-if="seat.status === 'BLOCKED'" class="text-rose-400 block">[ĐÃ KHÓA]</span>
                      </div>
                    </div>
                  </button>
                </template>
              </div>

              <!-- Row Label Right -->
              <span class="w-6 text-slate-500 font-mono text-xs text-center font-bold shrink-0">
                {{ row[0].row }}
              </span>
            </div>
          </div>

          <!-- 3. MAP LEGEND -->
          <div class="pt-4 border-t border-slate-800/80 flex flex-wrap items-center justify-center gap-4 text-xs font-bold text-slate-400">
            <div class="flex items-center gap-1.5">
              <span class="w-3.5 h-3.5 rounded-md bg-amber-500/20 border border-amber-500 text-amber-400 flex items-center justify-center text-[9px]">★</span>
              <span class="text-slate-300">Vé VIP ({{ defaultVipPrice.toLocaleString('vi-VN') }}đ)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3.5 h-3.5 rounded-md bg-indigo-500/20 border border-indigo-500"></span>
              <span class="text-slate-300">Standard ({{ defaultStandardPrice.toLocaleString('vi-VN') }}đ)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3.5 h-3.5 rounded-md bg-purple-500/20 border border-purple-500"></span>
              <span class="text-slate-300">Balcony ({{ defaultBalconyPrice.toLocaleString('vi-VN') }}đ)</span>
            </div>
            <div class="flex items-center gap-1.5">
              <span class="w-3.5 h-3.5 rounded-md bg-slate-900 border border-dashed border-slate-700 flex items-center justify-center">
                <Lock class="w-2.5 h-2.5 text-rose-500" />
              </span>
              <span class="text-slate-400">Ghế Khóa (Không bán)</span>
            </div>
          </div>
        </div>

        <!-- 4. BATCH ACTIONS BAR (WHEN SEATS SELECTED) -->
        <div
          v-if="selectedSeatIds.size > 0 && !isPreviewMode"
          class="p-4 rounded-2xl bg-amber-500/10 border border-amber-500/30 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs"
        >
          <div class="flex items-center gap-2">
            <span class="px-2.5 py-1 rounded-lg bg-amber-500 text-slate-950 font-black font-mono">
              {{ selectedSeatIds.size }} ghế đã chọn
            </span>
            <span class="text-slate-300 font-bold">Thao tác hàng loạt:</span>
          </div>

          <div class="flex flex-wrap items-center gap-2">
            <button
              type="button"
              @click="batchSetZone('VIP')"
              class="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-amber-950 text-amber-300 border border-slate-700 font-bold transition cursor-pointer"
            >
              Chuyển sang VIP
            </button>
            <button
              type="button"
              @click="batchSetZone('STANDARD')"
              class="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-indigo-950 text-indigo-300 border border-slate-700 font-bold transition cursor-pointer"
            >
              Chuyển sang Standard
            </button>
            <button
              type="button"
              @click="batchSetZone('BALCONY')"
              class="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-purple-950 text-purple-300 border border-slate-700 font-bold transition cursor-pointer"
            >
              Chuyển sang Balcony
            </button>
            <button
              type="button"
              @click="batchToggleBlock"
              class="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-rose-950 text-rose-300 border border-slate-700 font-bold transition cursor-pointer"
            >
              Khóa / Mở Ghế
            </button>
            <button
              type="button"
              @click="selectedSeatIds.clear()"
              class="p-1.5 text-slate-400 hover:text-white transition cursor-pointer"
              title="Bỏ chọn tất cả"
            >
              Hủy chọn
            </button>
          </div>
        </div>

        <!-- 5. PREVIEW MODE TICKET TALLY (WHEN IN PREVIEW) -->
        <div
          v-if="isPreviewMode"
          class="p-4 rounded-2xl bg-indigo-950/40 border border-indigo-500/30 flex items-center justify-between text-xs"
        >
          <div class="flex items-center gap-2">
            <Eye class="w-4 h-4 text-indigo-400" />
            <span class="text-white font-bold">Chế độ giả lập trải nghiệm Khách Hàng:</span>
            <span class="text-indigo-300">Đã chọn {{ previewSelectedSeats.length }} vé</span>
          </div>

          <div class="flex items-center gap-3">
            <span class="text-slate-400">Tổng tiền tạm tính:</span>
            <strong class="text-base font-black font-mono text-emerald-400">
              {{ previewSelectedSeats.reduce((sum, s) => sum + s.price, 0).toLocaleString('vi-VN') }} đ
            </strong>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>
