<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import ticketService from '@/services/ticket.service'
import { eventService } from '@/services/event.service'
import type { CheckInResponse, GateStatsResponse } from '@/types/ticket'
import type { Event } from '@/types/event'
import {
  ScanLine,
  Camera,
  CameraOff,
  Flashlight,
  RefreshCw,
  Maximize2,
  Minimize2,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  Clock,
  User,
  Ticket,
  Armchair,
  Sparkles,
  ArrowLeft,
  Volume2,
  VolumeX,
  History,
  ShieldAlert,
  Search,
  ChevronDown
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

// State
const events = ref<Event[]>([])
const selectedEventId = ref<number | null>(null)
const gateStats = ref<GateStatsResponse | null>(null)
const isLoadingStats = ref(false)

// Scanner state
const scannerInputCode = ref('')
const inputRef = ref<HTMLInputElement | null>(null)
const isScanning = ref(false)
const isCameraActive = ref(false)
const hasCameraSupport = ref(true)
const cameraError = ref<string | null>(null)
const videoRef = ref<HTMLVideoElement | null>(null)
let mediaStream: MediaStream | null = null
const facingMode = ref<'environment' | 'user'>('environment')
const isTorchOn = ref(false)
const soundEnabled = ref(true)
const isFullscreen = ref(false)
let barcodeDetector: any = null
let animationFrameId: number | null = null

// Result state
interface ExtendedScanResult {
  status: 'SUCCESS' | 'ANTI_PASSBACK' | 'INVALID'
  ticketCode: string
  attendeeName?: string
  tier?: string
  seat?: string
  checkInTime: string
  staff?: string
  message: string
}
const lastScanResult = ref<ExtendedScanResult | null>(null)
const localGateLogs = ref<ExtendedScanResult[]>([])

// Real-time Clock
const currentTimeStr = ref(new Date().toLocaleTimeString('vi-VN'))
let clockTimer: any = null

// Audio Synthesis using Web Audio API
const playSound = (type: 'success' | 'anti_passback' | 'invalid') => {
  if (!soundEnabled.value) return
  try {
    const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext
    if (!AudioContextClass) return
    const ctx = new AudioContextClass()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain)
    gain.connect(ctx.destination)

    if (type === 'success') {
      // Pleasant high-pitched chord (880Hz to 1320Hz)
      osc.type = 'sine'
      osc.frequency.setValueAtTime(880, ctx.currentTime)
      osc.frequency.exponentialRampToValueAtTime(1320, ctx.currentTime + 0.12)
      gain.gain.setValueAtTime(0.3, ctx.currentTime)
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.22)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 0.22)
    } else if (type === 'anti_passback') {
      // Harsh warning double buzz (180Hz sawtooth)
      osc.type = 'sawtooth'
      osc.frequency.setValueAtTime(180, ctx.currentTime)
      gain.gain.setValueAtTime(0.4, ctx.currentTime)
      gain.gain.linearRampToValueAtTime(0.05, ctx.currentTime + 0.18)
      gain.gain.setValueAtTime(0.4, ctx.currentTime + 0.2)
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.45)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 0.45)
    } else {
      // Low error tone (120Hz)
      osc.type = 'sawtooth'
      osc.frequency.setValueAtTime(130, ctx.currentTime)
      gain.gain.setValueAtTime(0.3, ctx.currentTime)
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.35)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 0.35)
    }
  } catch {
    // Ignore audio restriction before interaction
  }
}

// Mobile Haptic Vibration
const triggerVibration = (type: 'success' | 'anti_passback' | 'invalid') => {
  if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
    if (type === 'success') {
      navigator.vibrate(80)
    } else if (type === 'anti_passback') {
      navigator.vibrate([150, 60, 150, 60, 200])
    } else {
      navigator.vibrate([120, 80, 120])
    }
  }
}

// Load Events for staff to select
const loadEvents = async () => {
  try {
    const res = await eventService.getEvents({ page: 0, size: 20 })
    events.value = res.content || []
    if (events.value.length > 0 && !selectedEventId.value) {
      selectedEventId.value = events.value[0].id
      await loadGateStats()
    }
  } catch (err) {
    console.warn('Could not load events list:', err)
  }
}

// Load Gate Statistics for selected event
const loadGateStats = async () => {
  if (!selectedEventId.value) return
  isLoadingStats.value = true
  try {
    const stats = await ticketService.getGateStats(selectedEventId.value)
    gateStats.value = stats
  } catch (err) {
    console.warn('Could not load gate stats:', err)
  } finally {
    isLoadingStats.value = false
  }
}

watch(selectedEventId, () => {
  loadGateStats()
})

// Camera Stream Handler
const startCamera = async () => {
  cameraError.value = null
  try {
    if (mediaStream) {
      mediaStream.getTracks().forEach((track) => track.stop())
      mediaStream = null
    }

    const constraints: MediaStreamConstraints = {
      video: {
        facingMode: facingMode.value,
        width: { ideal: 1280 },
        height: { ideal: 720 },
      },
      audio: false,
    }

    mediaStream = await navigator.mediaDevices.getUserMedia(constraints)
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
      await videoRef.value.play()
      isCameraActive.value = true
      startBarcodeDetectionLoop()
    }
  } catch (err: any) {
    isCameraActive.value = false
    cameraError.value = 'Không thể truy cập camera hoặc quyền bị từ chối'
    console.warn('Camera stream error:', err)
  }
}

const stopCamera = () => {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
  if (mediaStream) {
    mediaStream.getTracks().forEach((track) => track.stop())
    mediaStream = null
  }
  isCameraActive.value = false
  isTorchOn.value = false
}

const toggleTorch = async () => {
  if (!mediaStream) return
  const track = mediaStream.getVideoTracks()[0]
  if (track && 'applyConstraints' in track) {
    try {
      isTorchOn.value = !isTorchOn.value
      await (track as any).applyConstraints({
        advanced: [{ torch: isTorchOn.value }],
      })
    } catch {
      isTorchOn.value = false
    }
  }
}

const flipCamera = async () => {
  facingMode.value = facingMode.value === 'environment' ? 'user' : 'environment'
  await startCamera()
}

// Barcode Detector Loop
const startBarcodeDetectionLoop = () => {
  if (typeof (window as any).BarcodeDetector !== 'undefined') {
    try {
      barcodeDetector = new (window as any).BarcodeDetector({
        formats: ['qr_code', 'code_128', 'ean_13'],
      })
    } catch {
      barcodeDetector = null
    }
  }

  const scanFrame = async () => {
    if (!isCameraActive.value || !videoRef.value) return

    if (barcodeDetector && videoRef.value.readyState >= 2 && !isScanning.value) {
      try {
        const barcodes = await barcodeDetector.detect(videoRef.value)
        if (barcodes.length > 0) {
          const rawValue = barcodes[0].rawValue
          if (rawValue) {
            let extractedCode = rawValue
            if (rawValue.includes('tickets/')) {
              const match = rawValue.match(/tickets\/([A-Za-z0-9_-]+)/)
              if (match) extractedCode = match[1]
            }
            if (extractedCode !== lastScanResult.value?.ticketCode) {
              handleScanTicket(extractedCode)
            }
          }
        }
      } catch {
        // Continuous scan frame error ignored
      }
    }

    animationFrameId = requestAnimationFrame(scanFrame)
  }

  animationFrameId = requestAnimationFrame(scanFrame)
}

// Check-in Execution Core
const handleScanTicket = async (customCode?: string) => {
  const code = (customCode || scannerInputCode.value || '').trim().toUpperCase()
  if (!code) return
  if (isScanning.value) return

  isScanning.value = true
  const nowStr = new Date().toLocaleTimeString('vi-VN')

  try {
    const res: CheckInResponse = await ticketService.checkInTicket(code)
    playSound('success')
    triggerVibration('success')

    const result: ExtendedScanResult = {
      status: 'SUCCESS',
      ticketCode: res.ticketCode,
      attendeeName: res.attendeeName,
      tier: res.ticketTypeName || 'VIP Pass',
      seat: res.seatInfo || 'Khu VIP Tầng 1',
      checkInTime: nowStr,
      staff: res.staffUsername || authStore.user?.fullName || 'Staff',
      message: 'HỢP LỆ &bull; CHO PHÉP VÀO CỔNG KHÁN ĐÀI',
    }

    lastScanResult.value = result
    localGateLogs.value.unshift(result)
    scannerInputCode.value = ''

    // Refresh gate stats
    loadGateStats()
  } catch (err: any) {
    const errMsg: string = err.response?.data?.message || err.message || ''
    const isAntiPassback =
      errMsg.toLowerCase().includes('already') ||
      errMsg.toLowerCase().includes('anti-passback') ||
      errMsg.toLowerCase().includes('đã') ||
      errMsg.toLowerCase().includes('used')

    if (isAntiPassback) {
      playSound('anti_passback')
      triggerVibration('anti_passback')

      const result: ExtendedScanResult = {
        status: 'ANTI_PASSBACK',
        ticketCode: code,
        attendeeName: 'Khách chưa xác định / Nghi vấn quét trùng',
        tier: 'Vé đã sử dụng',
        seat: 'Cấm vào',
        checkInTime: `${nowStr} (Quét trùng)`,
        staff: 'Hệ thống an ninh',
        message: errMsg || 'CẢNH BÁO: Vé này đã được check-in trước đó. Vi phạm quy định Anti-Passback! TỪ CHỐI CHO VÀO.',
      }
      lastScanResult.value = result
      localGateLogs.value.unshift(result)
    } else {
      playSound('invalid')
      triggerVibration('invalid')

      const result: ExtendedScanResult = {
        status: 'INVALID',
        ticketCode: code,
        checkInTime: nowStr,
        message: errMsg || 'MÃ VÉ KHÔNG TỒN TẠI HOẶC ĐÃ BỊ HỦY / HOÀN TIỀN. TỪ CHỐI CHO VÀO!',
      }
      lastScanResult.value = result
      localGateLogs.value.unshift(result)
    }
  } finally {
    isScanning.value = false
    nextTick(() => {
      inputRef.value?.focus()
    })
  }
}

// Hardware Barcode Scanner Gun Global Listener
let barcodeBuffer = ''
let lastKeyTime = Date.now()

const handleGlobalKeyDown = (e: KeyboardEvent) => {
  // If active element is an input, allow normal typing
  if (document.activeElement === inputRef.value) return

  const currentTime = Date.now()
  if (currentTime - lastKeyTime > 150) {
    barcodeBuffer = ''
  }
  lastKeyTime = currentTime

  if (e.key === 'Enter') {
    if (barcodeBuffer.length >= 4) {
      handleScanTicket(barcodeBuffer)
      barcodeBuffer = ''
    }
  } else if (e.key.length === 1) {
    barcodeBuffer += e.key
  }
}

// Fullscreen Toggle
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(() => {})
    isFullscreen.value = true
  } else {
    document.exitFullscreen().catch(() => {})
    isFullscreen.value = false
  }
}

onMounted(() => {
  loadEvents()
  clockTimer = setInterval(() => {
    currentTimeStr.value = new Date().toLocaleTimeString('vi-VN')
  }, 1000)

  window.addEventListener('keydown', handleGlobalKeyDown)
  startCamera()
  nextTick(() => {
    inputRef.value?.focus()
  })
})

onUnmounted(() => {
  stopCamera()
  clearInterval(clockTimer)
  window.removeEventListener('keydown', handleGlobalKeyDown)
})
</script>

<template>
  <div class="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-emerald-500 selection:text-slate-950">
    <!-- 1. TOP HEADER & HUD BAR -->
    <header class="border-b border-slate-800 bg-slate-900/90 backdrop-blur-md sticky top-0 z-40 px-4 py-3">
      <div class="max-w-6xl mx-auto flex items-center justify-between gap-3">
        <!-- Back & Title -->
        <div class="flex items-center gap-3">
          <button
            type="button"
            @click="router.push('/dashboard?tab=staff_scanner')"
            class="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white transition cursor-pointer"
            title="Quay lại Tổng Quan"
          >
            <ArrowLeft class="w-5 h-5" />
          </button>
          <div>
            <div class="flex items-center gap-2">
              <span class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                <span class="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
                GATE SCANNER PRO
              </span>
              <span class="hidden sm:inline-block text-xs font-mono text-slate-400">&bull; {{ currentTimeStr }}</span>
            </div>
            <h1 class="text-base sm:text-lg font-black tracking-tight text-white flex items-center gap-2">
              Trạm Soát Vé Trực Tuyến & Anti-Passback
            </h1>
          </div>
        </div>

        <!-- Controls: Sound, Fullscreen, Gate Info -->
        <div class="flex items-center gap-2">
          <button
            type="button"
            @click="soundEnabled = !soundEnabled"
            class="p-2.5 rounded-xl border border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            :title="soundEnabled ? 'Tắt âm thanh' : 'Bật âm thanh'"
          >
            <Volume2 v-if="soundEnabled" class="w-4 h-4 text-emerald-400" />
            <VolumeX v-else class="w-4 h-4 text-slate-500" />
          </button>

          <button
            type="button"
            @click="toggleFullscreen"
            class="hidden sm:flex p-2.5 rounded-xl border border-slate-700 text-slate-300 hover:text-white hover:bg-slate-800 transition cursor-pointer"
            title="Toàn màn hình"
          >
            <Maximize2 v-if="!isFullscreen" class="w-4 h-4" />
            <Minimize2 v-else class="w-4 h-4" />
          </button>

          <!-- Gate Badge -->
          <div class="px-3 py-1.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs font-bold font-mono">
            CỔNG A1 &bull; VIP/STD
          </div>
        </div>
      </div>
    </header>

    <!-- 2. MAIN WORKSPACE CONTAINER -->
    <main class="flex-1 max-w-6xl w-full mx-auto p-4 sm:p-6 space-y-5">
      <!-- Event Selector Bar -->
      <div class="p-3.5 rounded-2xl bg-slate-900 border border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs">
        <div class="flex items-center gap-2.5 w-full sm:w-auto">
          <Ticket class="w-4 h-4 text-indigo-400 shrink-0" />
          <span class="text-slate-400 font-bold whitespace-nowrap">Sự Kiện Đang Soát:</span>
          <select
            v-model="selectedEventId"
            class="bg-slate-800 border border-slate-700 rounded-xl px-3 py-1.5 text-white font-bold text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none w-full sm:w-auto"
          >
            <option v-for="ev in events" :key="ev.id" :value="ev.id">
              {{ ev.title }} ({{ ev.locationName || ev.address }})
            </option>
          </select>
        </div>

        <div class="flex items-center gap-4 text-slate-300 font-mono text-xs w-full sm:w-auto justify-between sm:justify-end">
          <span class="flex items-center gap-1.5">
            <span class="w-2 h-2 rounded-full bg-emerald-400"></span>
            Nhân viên: <strong class="text-white">{{ authStore.user?.fullName || 'Check-in Staff' }}</strong>
          </span>
          <button
            type="button"
            @click="loadGateStats"
            class="p-1.5 hover:bg-slate-800 rounded-lg text-slate-400 hover:text-white transition cursor-pointer"
            title="Làm mới thống kê cổng"
          >
            <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': isLoadingStats }" />
          </button>
        </div>
      </div>

      <!-- Live Gate Attendance Gauge HUD -->
      <div v-if="gateStats" class="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div class="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex flex-col justify-between">
          <span class="text-slate-400 text-[11px] uppercase tracking-wider font-bold">Tổng Vé Đã Phát Hành</span>
          <div class="mt-2 flex items-baseline justify-between">
            <strong class="text-2xl font-black font-mono text-white">{{ gateStats.totalTickets }}</strong>
            <span class="text-[10px] text-slate-500 font-mono">100%</span>
          </div>
        </div>

        <div class="p-4 rounded-2xl bg-emerald-950/40 border border-emerald-500/30 flex flex-col justify-between">
          <span class="text-emerald-400 text-[11px] uppercase tracking-wider font-bold">Đã Vào Cổng</span>
          <div class="mt-2 flex items-baseline justify-between">
            <strong class="text-2xl font-black font-mono text-emerald-300">{{ gateStats.checkedInCount }}</strong>
            <span class="text-xs font-bold text-emerald-400">{{ gateStats.checkInRatePercent }}%</span>
          </div>
          <!-- Gauge mini bar -->
          <div class="w-full h-1.5 bg-slate-800 rounded-full mt-2 overflow-hidden">
            <div class="h-full bg-emerald-500 transition-all duration-500" :style="{ width: `${gateStats.checkInRatePercent}%` }"></div>
          </div>
        </div>

        <div class="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex flex-col justify-between">
          <span class="text-slate-400 text-[11px] uppercase tracking-wider font-bold">Còn Lại Ngoài Cổng</span>
          <div class="mt-2 flex items-baseline justify-between">
            <strong class="text-2xl font-black font-mono text-amber-300">{{ gateStats.remainingCount }}</strong>
            <span class="text-[10px] text-slate-500 font-mono">Chưa check-in</span>
          </div>
        </div>

        <div class="p-4 rounded-2xl bg-slate-900 border border-slate-800 flex flex-col justify-between">
          <span class="text-slate-400 text-[11px] uppercase tracking-wider font-bold">Tốc Độ Quét</span>
          <div class="mt-2 flex items-baseline justify-between">
            <strong class="text-2xl font-black font-mono text-indigo-300">~25-40</strong>
            <span class="text-[10px] text-indigo-400 font-bold">vé/phút</span>
          </div>
        </div>
      </div>

      <!-- 3. VIEWFINDER + INPUT GRID -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-5">
        <!-- LEFT: Camera Viewfinder with HUD laser (7 Cols) -->
        <div class="lg:col-span-7 space-y-4">
          <div class="relative w-full aspect-4/3 sm:aspect-16/10 rounded-3xl bg-slate-950 border-2 border-slate-800 overflow-hidden shadow-2xl flex items-center justify-center">
            <!-- Video Preview Stream -->
            <video
              ref="videoRef"
              class="w-full h-full object-cover"
              autoplay
              playsinline
              muted
            ></video>

            <!-- Camera Inactive / Permission Fallback -->
            <div
              v-if="!isCameraActive"
              class="absolute inset-0 bg-slate-950/90 flex flex-col items-center justify-center p-6 text-center space-y-3 z-10"
            >
              <div class="w-16 h-16 rounded-3xl bg-slate-800 text-slate-400 flex items-center justify-center shadow-lg">
                <CameraOff class="w-8 h-8" />
              </div>
              <div>
                <h4 class="text-sm font-bold text-white">Camera Đang Tắt hoặc Không Có Quyền</h4>
                <p class="text-xs text-slate-400 mt-1 max-w-xs">
                  {{ cameraError || 'Bạn có thể bật camera hoặc dùng trực tiếp máy quét mã vạch (Barcode Scanner Gun).' }}
                </p>
              </div>
              <button
                type="button"
                @click="startCamera"
                class="px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-black text-xs flex items-center gap-2 shadow-lg transition cursor-pointer"
              >
                <Camera class="w-4 h-4" />
                <span>Kích Hoạt Camera</span>
              </button>
            </div>

            <!-- Scanning Overlay Frame when Active -->
            <div v-if="isCameraActive" class="absolute inset-0 pointer-events-none z-10">
              <!-- Animated Scanning Laser Line -->
              <div class="w-full h-0.5 bg-gradient-to-r from-transparent via-emerald-400 to-transparent shadow-[0_0_15px_#10b981] animate-laser"></div>

              <!-- Reticle Target Corners -->
              <div class="absolute inset-12 sm:inset-16 border-2 border-dashed border-emerald-500/30 rounded-2xl flex items-center justify-center">
                <!-- Top-Left -->
                <div class="absolute -top-1 -left-1 w-6 h-6 border-t-3 border-l-3 border-emerald-400 rounded-tl-lg"></div>
                <!-- Top-Right -->
                <div class="absolute -top-1 -right-1 w-6 h-6 border-t-3 border-r-3 border-emerald-400 rounded-tr-lg"></div>
                <!-- Bottom-Left -->
                <div class="absolute -bottom-1 -left-1 w-6 h-6 border-b-3 border-l-3 border-emerald-400 rounded-bl-lg"></div>
                <!-- Bottom-Right -->
                <div class="absolute -bottom-1 -right-1 w-6 h-6 border-b-3 border-r-3 border-emerald-400 rounded-br-lg"></div>

                <div class="text-[11px] font-mono font-bold text-emerald-400/80 bg-slate-950/70 px-3 py-1 rounded-full backdrop-blur-xs">
                  Căn mã QR vào khung
                </div>
              </div>
            </div>

            <!-- Top Viewfinder Floating Controls -->
            <div class="absolute top-4 right-4 z-20 flex items-center gap-2">
              <button
                type="button"
                @click="toggleTorch"
                class="p-2.5 rounded-xl backdrop-blur-md transition cursor-pointer"
                :class="isTorchOn ? 'bg-amber-500 text-slate-950' : 'bg-slate-900/80 text-white hover:bg-slate-800'"
                title="Đèn Flash / Torch"
              >
                <Flashlight class="w-4 h-4" />
              </button>
              <button
                type="button"
                @click="flipCamera"
                class="p-2.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-white backdrop-blur-md transition cursor-pointer"
                title="Đổi camera trước/sau"
              >
                <RefreshCw class="w-4 h-4" />
              </button>
              <button
                type="button"
                @click="isCameraActive ? stopCamera() : startCamera()"
                class="p-2.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 text-white backdrop-blur-md transition cursor-pointer"
                :title="isCameraActive ? 'Tắt camera' : 'Bật camera'"
              >
                <Camera v-if="!isCameraActive" class="w-4 h-4 text-emerald-400" />
                <CameraOff v-else class="w-4 h-4 text-rose-400" />
              </button>
            </div>
          </div>

          <!-- Barcode Gun / Manual Input Bar -->
          <div class="p-4 rounded-2xl bg-slate-900 border border-slate-800 space-y-3">
            <div class="flex items-center justify-between text-xs">
              <span class="text-slate-400 font-bold flex items-center gap-1.5">
                <ScanLine class="w-4 h-4 text-emerald-400" />
                Quét Mã QR hoặc Nhập Thủ Công:
              </span>
              <span class="text-[11px] text-slate-500 font-mono">Hỗ trợ máy quét USB/Bluetooth</span>
            </div>

            <div class="flex items-center gap-2">
              <div class="relative flex-1">
                <input
                  ref="inputRef"
                  v-model="scannerInputCode"
                  @keyup.enter="handleScanTicket()"
                  type="text"
                  placeholder="Quét mã hoặc gõ mã vé (VD: TK-20261021-AAAA1111)..."
                  class="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-700 text-white placeholder-slate-500 font-mono text-sm uppercase focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <button
                type="button"
                @click="handleScanTicket()"
                :disabled="isScanning || !scannerInputCode"
                class="px-5 py-3 rounded-xl bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed text-slate-950 font-black text-xs flex items-center gap-1.5 transition cursor-pointer shrink-0 shadow-lg"
              >
                <RefreshCw v-if="isScanning" class="w-4 h-4 animate-spin" />
                <CheckCircle2 v-else class="w-4 h-4" />
                <span>Xác Thực</span>
              </button>
            </div>

            <!-- Demo Test Scenarios Buttons -->
            <div class="pt-2 border-t border-slate-800 flex flex-wrap items-center gap-1.5 text-xs">
              <span class="text-[11px] text-slate-500 font-bold">Kịch bản thử nghiệm:</span>
              <button
                type="button"
                @click="handleScanTicket('TKT-EVH-2026-001')"
                class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-emerald-950 text-emerald-300 border border-slate-700 transition cursor-pointer text-[11px] font-semibold"
              >
                Quét Vé VIP Diamond
              </button>
              <button
                type="button"
                @click="handleScanTicket('TKT-EVH-2026-003')"
                class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-indigo-950 text-indigo-300 border border-slate-700 transition cursor-pointer text-[11px] font-semibold"
              >
                Quét Vé Standard
              </button>
              <button
                type="button"
                @click="handleScanTicket('TKT-EVH-2026-001')"
                class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-rose-950 text-rose-300 border border-slate-700 transition cursor-pointer text-[11px] font-semibold"
              >
                Thử Quét Lại (Anti-Passback)
              </button>
              <button
                type="button"
                @click="handleScanTicket('TKT-INVALID-999')"
                class="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 border border-slate-700 transition cursor-pointer text-[11px] font-semibold"
              >
                Thử Vé Giả/Hủy
              </button>
            </div>
          </div>
        </div>

        <!-- RIGHT: Instant Result Screen & Recent Log (5 Cols) -->
        <div class="lg:col-span-5 space-y-4">
          <!-- 1. INSTANT RESULT DISPLAY -->
          <div v-if="lastScanResult" class="transition-all duration-300">
            <!-- APPROVED SUCCESS STATE -->
            <div
              v-if="lastScanResult.status === 'SUCCESS'"
              class="p-5 rounded-3xl bg-gradient-to-br from-emerald-600 via-teal-700 to-emerald-800 border-2 border-emerald-400 text-white shadow-2xl space-y-4"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-11 h-11 rounded-2xl bg-white text-emerald-700 flex items-center justify-center font-black shadow-md">
                    <CheckCircle2 class="w-7 h-7" />
                  </div>
                  <div>
                    <span class="text-[10px] font-black uppercase tracking-wider text-emerald-200">Xác Thực Hợp Lệ</span>
                    <h3 class="text-lg font-black leading-tight">CHO PHÉP VÀO CỔNG</h3>
                  </div>
                </div>
                <span class="px-2.5 py-1 rounded-full bg-black/20 text-white font-mono text-[11px] font-bold">
                  {{ lastScanResult.checkInTime }}
                </span>
              </div>

              <!-- Ticket Info Grid -->
              <div class="p-3.5 rounded-2xl bg-black/20 backdrop-blur-xs grid grid-cols-2 gap-3 text-xs">
                <div>
                  <span class="text-emerald-200 block text-[10px] uppercase font-bold">Khách Hàng</span>
                  <strong class="text-sm font-black text-white truncate block">{{ lastScanResult.attendeeName }}</strong>
                </div>
                <div>
                  <span class="text-emerald-200 block text-[10px] uppercase font-bold">Hạng Vé</span>
                  <strong class="text-sm font-black text-white truncate block">{{ lastScanResult.tier }}</strong>
                </div>
                <div>
                  <span class="text-emerald-200 block text-[10px] uppercase font-bold">Vị Trí Ghế</span>
                  <strong class="text-sm font-mono font-black text-emerald-100 truncate block">{{ lastScanResult.seat }}</strong>
                </div>
                <div>
                  <span class="text-emerald-200 block text-[10px] uppercase font-bold">Mã Vé</span>
                  <strong class="text-xs font-mono font-bold text-white truncate block">{{ lastScanResult.ticketCode }}</strong>
                </div>
              </div>

              <div class="text-[11px] text-emerald-100 font-medium flex items-center gap-1.5">
                <Sparkles class="w-4 h-4 text-emerald-300 shrink-0" />
                <span>{{ lastScanResult.message }}</span>
              </div>
            </div>

            <!-- ANTI-PASSBACK VIOLATION ALERT -->
            <div
              v-else-if="lastScanResult.status === 'ANTI_PASSBACK'"
              class="p-5 rounded-3xl bg-gradient-to-br from-rose-700 via-red-800 to-rose-950 border-2 border-rose-500 text-white shadow-2xl space-y-4 animate-shake"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-11 h-11 rounded-2xl bg-white text-rose-700 flex items-center justify-center font-black shadow-md">
                    <ShieldAlert class="w-7 h-7" />
                  </div>
                  <div>
                    <span class="text-[10px] font-black uppercase tracking-wider text-rose-200">Báo Động An Ninh</span>
                    <h3 class="text-lg font-black leading-tight text-white">VI PHẠM ANTI-PASSBACK!</h3>
                  </div>
                </div>
                <span class="px-2.5 py-1 rounded-full bg-black/30 text-rose-200 font-mono text-[11px] font-bold">
                  TỪ CHỐI
                </span>
              </div>

              <div class="p-3.5 rounded-2xl bg-black/30 backdrop-blur-xs space-y-2 text-xs">
                <div class="flex items-start gap-2">
                  <AlertTriangle class="w-4 h-4 text-amber-300 shrink-0 mt-0.5" />
                  <p class="text-xs text-rose-100 font-bold leading-relaxed">
                    {{ lastScanResult.message }}
                  </p>
                </div>
                <div class="pt-2 border-t border-rose-600/40 flex justify-between text-[11px] text-rose-200">
                  <span>Mã vé vi phạm:</span>
                  <strong class="font-mono text-white">{{ lastScanResult.ticketCode }}</strong>
                </div>
              </div>

              <div class="p-2.5 rounded-xl bg-rose-950/80 border border-rose-600/40 text-[11px] text-rose-200 font-medium">
                Yêu cầu khách hàng xuất trình CCCD hoặc ứng dụng EventHub chính chủ để đối chiếu danh tính tại quầy hỗ trợ CSKH!
              </div>
            </div>

            <!-- INVALID / REJECTED STATE -->
            <div
              v-else
              class="p-5 rounded-3xl bg-slate-900 border-2 border-slate-700 text-white shadow-2xl space-y-3"
            >
              <div class="flex items-center gap-3">
                <div class="w-11 h-11 rounded-2xl bg-slate-800 text-rose-400 flex items-center justify-center font-black">
                  <XCircle class="w-7 h-7" />
                </div>
                <div>
                  <span class="text-[10px] font-black uppercase tracking-wider text-slate-400">Không Thể Xác Thực</span>
                  <h3 class="text-base font-black text-white">VÉ KHÔNG HỢP LỆ</h3>
                </div>
              </div>
              <p class="text-xs text-slate-300 font-medium bg-slate-950 p-3 rounded-xl border border-slate-800 font-mono">
                {{ lastScanResult.message }}
              </p>
            </div>
          </div>

          <!-- Empty Result Placeholder -->
          <div
            v-else
            class="p-8 rounded-3xl bg-slate-900 border border-slate-800 text-center flex flex-col items-center justify-center space-y-3 min-h-[180px]"
          >
            <div class="w-12 h-12 rounded-2xl bg-slate-800 text-slate-500 flex items-center justify-center">
              <ScanLine class="w-6 h-6" />
            </div>
            <div>
              <h4 class="text-xs font-bold text-slate-300 uppercase tracking-wider">Sẵn Sàng Quét Vé</h4>
              <p class="text-[11px] text-slate-500 mt-1">Đưa mã QR trước camera hoặc dùng máy quét để kiểm tra trạng thái vé.</p>
            </div>
          </div>

          <!-- 2. RECENT GATE SCAN LOG -->
          <div class="p-4 rounded-3xl bg-slate-900 border border-slate-800 space-y-3">
            <div class="flex items-center justify-between text-xs">
              <span class="text-slate-300 font-bold flex items-center gap-1.5">
                <History class="w-4 h-4 text-slate-400" />
                Lịch Sử Quét Vừa Qua (Tại Cổng)
              </span>
              <span class="text-[11px] text-slate-500 font-mono">{{ localGateLogs.length }} lượt</span>
            </div>

            <div class="space-y-2 max-h-[260px] overflow-y-auto pr-1">
              <div
                v-for="(log, idx) in localGateLogs.slice(0, 10)"
                :key="idx"
                class="p-2.5 rounded-xl bg-slate-950 border border-slate-800 flex items-center justify-between text-xs transition"
              >
                <div class="flex items-center gap-2.5 min-w-0">
                  <div
                    class="w-2 h-2 rounded-full shrink-0"
                    :class="{
                      'bg-emerald-400': log.status === 'SUCCESS',
                      'bg-rose-500': log.status === 'ANTI_PASSBACK',
                      'bg-amber-400': log.status === 'INVALID'
                    }"
                  ></div>
                  <div class="min-w-0">
                    <div class="font-bold text-white text-xs truncate">
                      {{ log.attendeeName || log.ticketCode }}
                    </div>
                    <div class="text-[10px] text-slate-400 truncate">
                      {{ log.tier || log.ticketCode }} &bull; {{ log.seat || 'Cổng A' }}
                    </div>
                  </div>
                </div>

                <div class="text-right shrink-0">
                  <span class="text-[10px] font-mono text-slate-400 block">{{ log.checkInTime }}</span>
                  <span
                    class="text-[9px] font-black uppercase px-1.5 py-0.5 rounded"
                    :class="{
                      'bg-emerald-500/20 text-emerald-400': log.status === 'SUCCESS',
                      'bg-rose-500/20 text-rose-400': log.status === 'ANTI_PASSBACK',
                      'bg-amber-500/20 text-amber-400': log.status === 'INVALID'
                    }"
                  >
                    {{ log.status }}
                  </span>
                </div>
              </div>

              <div v-if="localGateLogs.length === 0" class="text-center py-6 text-xs text-slate-500">
                Chưa có lượt quét nào trong phiên làm việc này.
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@keyframes laser {
  0% {
    top: 5%;
  }
  50% {
    top: 95%;
  }
  100% {
    top: 5%;
  }
}

.animate-laser {
  position: absolute;
  animation: laser 2.4s ease-in-out infinite;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  20%, 60% { transform: translateX(-6px); }
  40%, 80% { transform: translateX(6px); }
}

.animate-shake {
  animation: shake 0.4s ease-in-out;
}
</style>
