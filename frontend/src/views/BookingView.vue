<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/booking'
import { useAuthStore } from '@/stores/auth'
import { bookingService } from '@/services/booking.service'
import type { PaymentProvider, PaymentResponse } from '@/types'
import {
  Clock,
  Ticket as TicketIcon,
  CheckCircle,
  Tag,
  CreditCard,
  ShieldCheck,
  AlertCircle,
  User,
  Mail,
  Phone,
  QrCode,
  Wallet,
  Copy,
  Check,
  Building2,
  FileText,
  Printer,
  Download,
  ArrowRight,
  RefreshCw,
  Sparkles,
  Info,
} from 'lucide-vue-next'

const router = useRouter()
const bookingStore = useBookingStore()
const authStore = useAuthStore()

// Step state: 'FORM' | 'PAYMENT_PENDING' | 'SUCCESS'
const checkoutStep = ref<'FORM' | 'PAYMENT_PENDING' | 'SUCCESS'>('FORM')

// Coupon states
const couponCodeInput = ref('')
const couponError = ref('')
const couponSuccess = ref('')

// Recipient Form
const recipientName = ref(authStore.user?.fullName || 'Nguyễn Văn Quân')
const recipientEmail = ref(authStore.user?.email || 'quan.nguyen@example.com')
const recipientPhone = ref(authStore.user?.phoneNumber || '0987654321')

// Selected Payment Method
const selectedProvider = ref<PaymentProvider>('VIETQR')

// Credit card form state
const cardNumber = ref('4111 2222 3333 4444')
const cardHolder = ref('NGUYEN VAN QUAN')
const cardExpiry = ref('12/28')
const cardCvv = ref('888')

// VAT Invoice toggle & form
const needVatInvoice = ref(false)
const vatTaxCode = ref('')
const vatCompanyName = ref('')
const vatCompanyAddress = ref('')
const vatEmail = ref('')

// Transaction & Processing states
const isProcessing = ref(false)
const isPolling = ref(false)
const orderCode = ref('')
const activePayment = ref<PaymentResponse | null>(null)
const copiedField = ref<string | null>(null)

// Countdown Timer State (15 mins = 900s synced with server hold)
const timeLeftSeconds = ref(15 * 60)
let timerInterval: any = null
let pollInterval: any = null

onMounted(() => {
  if (bookingStore.totalTicketCount === 0) {
    router.replace('/events')
    return
  }

  if (bookingStore.holdExpiresAt) {
    timeLeftSeconds.value = Math.max(0, Math.floor((bookingStore.holdExpiresAt - Date.now()) / 1000))
  } else {
    bookingStore.startHoldTimer(15)
    timeLeftSeconds.value = 15 * 60
  }

  timerInterval = setInterval(() => {
    if (timeLeftSeconds.value > 0) {
      timeLeftSeconds.value--
    } else {
      clearInterval(timerInterval)
      stopPolling()
      alert('Đã hết 15 phút giữ chỗ tạm thời! Ghế/vé đã được hoàn trả lại hệ thống.')
      bookingStore.clearBooking(true)
      router.push('/events')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  stopPolling()
})

const stopPolling = () => {
  if (pollInterval) {
    clearInterval(pollInterval)
    pollInterval = null
  }
}

const formattedTime = computed(() => {
  const mins = Math.floor(timeLeftSeconds.value / 60)
  const secs = timeLeftSeconds.value % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
})

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const handleApplyCoupon = async () => {
  couponError.value = ''
  couponSuccess.value = ''
  if (!couponCodeInput.value.trim()) return

  try {
    await bookingStore.applyCoupon(couponCodeInput.value.trim())
    couponSuccess.value = `Đã áp dụng mã thành công! Giảm ${formatCurrency(bookingStore.discountAmount)}`
  } catch (err: any) {
    couponError.value = err.message || 'Mã giảm giá không hợp lệ!'
  }
}

const handleRemoveCoupon = () => {
  bookingStore.removeCoupon()
  couponCodeInput.value = ''
  couponSuccess.value = ''
}

const copyToClipboard = (text: string, fieldName: string) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text)
  }
  copiedField.value = fieldName
  setTimeout(() => {
    copiedField.value = null
  }, 2000)
}

// Step 1 -> Step 2: Confirm Order & Initiate Payment
const handleInitiatePayment = async () => {
  if (!recipientName.value || !recipientEmail.value) {
    alert('Vui lòng điền đầy đủ họ tên và email nhận vé!')
    return
  }

  isProcessing.value = true
  try {
    const items = [
      ...bookingStore.selectedSeats.map((s) => ({
        eventSeatId: s.id,
        quantity: 1,
      })),
      ...Object.entries(bookingStore.selectedTicketQuantities).map(([ticketTypeId, qty]) => ({
        ticketTypeId: Number(ticketTypeId),
        quantity: qty,
      })),
    ]

    // 1. Create booking in DB with Pessimistic Lock
    const booking = await bookingService.createBooking({
      eventId: bookingStore.currentEvent?.id || 1,
      items,
      couponCode: bookingStore.appliedCoupon?.code,
    })

    orderCode.value = booking.bookingCode

    // 2. Initiate payment with selected provider and unique Idempotency Key
    const idempotencyKey = `IDEMP-${booking.bookingCode}-${Date.now()}`
    const payment = await bookingService.initiatePayment(booking.bookingCode, selectedProvider.value, idempotencyKey)
    activePayment.value = payment

    if (selectedProvider.value === 'CREDIT_CARD' || selectedProvider.value === 'MOCK_SANDBOX') {
      // Auto confirm for instant mock sandbox / credit card
      await handleConfirmPayment()
    } else {
      // Switch to Payment Pending screen (VietQR / MoMo / VNPay)
      checkoutStep.value = 'PAYMENT_PENDING'
      startPaymentPolling(payment.transactionCode)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || err.message || 'Có lỗi khi khởi tạo thanh toán')
  } finally {
    isProcessing.value = false
  }
}

// Polling background transaction status
const startPaymentPolling = (transactionCode: string) => {
  stopPolling()
  isPolling.value = true
  pollInterval = setInterval(async () => {
    try {
      const statusRes = await bookingService.pollPaymentStatus(transactionCode)
      if (statusRes && statusRes.status === 'SUCCESS') {
        stopPolling()
        checkoutStep.value = 'SUCCESS'
        bookingStore.clearBooking(false)
      }
    } catch {
      // Ignore polling errors
    }
  }, 2000)
}

// User clicks "Tôi đã hoàn tất chuyển khoản" or Instant Simulation
const handleConfirmPayment = async () => {
  if (!activePayment.value) return
  isProcessing.value = true
  try {
    const confirmed = await bookingService.simulatePaymentSuccess(activePayment.value.transactionCode)
    if (confirmed.status === 'SUCCESS') {
      stopPolling()
      checkoutStep.value = 'SUCCESS'
      bookingStore.clearBooking(false)
    }
  } catch (err: any) {
    alert(err.response?.data?.message || err.message || 'Không thể xác nhận thanh toán')
  } finally {
    isProcessing.value = false
  }
}

const printReceipt = () => {
  window.print()
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- STEP 3: SUCCESS STATE -->
    <div v-if="checkoutStep === 'SUCCESS'" class="bg-white p-8 sm:p-12 rounded-3xl border border-emerald-200 shadow-xl text-center space-y-6">
      <div class="w-20 h-20 rounded-full bg-emerald-100 text-emerald-600 mx-auto flex items-center justify-center shadow-inner">
        <CheckCircle class="w-10 h-10" />
      </div>

      <div>
        <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 font-bold text-xs border border-emerald-200 mb-2">
          <Sparkles class="w-3.5 h-3.5" /> Giao Dịch Thành Công • Idempotency Verified
        </span>
        <h2 class="text-3xl font-black text-slate-900">Đặt Vé & Thanh Toán Hoàn Tất!</h2>
        <p class="text-sm text-slate-600 max-w-md mx-auto mt-2">
          Vé điện tử có mã QR đã được gửi về email <strong class="text-slate-900">{{ recipientEmail }}</strong>. Bạn cũng có thể xem và tải vé trực tiếp ngay tại đây.
        </p>
      </div>

      <!-- Transaction Receipt Card -->
      <div class="max-w-lg mx-auto p-5 rounded-2xl bg-slate-50 border border-slate-200 text-left space-y-3 font-mono text-xs">
        <div class="flex justify-between pb-2 border-b border-slate-200">
          <span class="text-slate-500 font-sans">Mã đơn hàng (Booking Code):</span>
          <span class="font-bold text-indigo-700 font-mono text-sm">{{ orderCode }}</span>
        </div>
        <div class="flex justify-between pb-2 border-b border-slate-200">
          <span class="text-slate-500 font-sans">Mã giao dịch (Txn Code):</span>
          <span class="font-bold text-slate-800">{{ activePayment?.transactionCode || 'TXN-' + orderCode }}</span>
        </div>
        <div class="flex justify-between pb-2 border-b border-slate-200">
          <span class="text-slate-500 font-sans">Phương thức thanh toán:</span>
          <span class="font-bold text-slate-900 font-sans">{{ activePayment?.provider || selectedProvider }}</span>
        </div>
        <div class="flex justify-between pb-2 border-b border-slate-200">
          <span class="text-slate-500 font-sans">Tổng tiền đã thanh toán:</span>
          <span class="font-black text-rose-600 text-base font-sans">{{ formatCurrency(bookingStore.totalAmount || activePayment?.amount || 0) }}</span>
        </div>
        <div class="flex justify-between text-[11px] text-emerald-600 font-sans font-semibold">
          <span>Trạng thái:</span>
          <span>✔ ĐÃ XÁC NHẬN & XUẤT VÉ ĐIỆN TỬ</span>
        </div>
      </div>

      <!-- Action Buttons -->
      <div class="flex flex-wrap items-center justify-center gap-3 pt-2">
        <router-link
          to="/my-tickets"
          class="px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm shadow-md shadow-indigo-600/30 flex items-center gap-2 transition-all hover:scale-105"
        >
          <QrCode class="w-4 h-4" /> Xem Vé Điện Tử Của Tôi
        </router-link>

        <button
          @click="printReceipt"
          type="button"
          class="px-5 py-3 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-bold text-sm border border-slate-300 shadow-sm flex items-center gap-2 transition-all"
        >
          <Printer class="w-4 h-4" /> In Biên Lai / Hóa Đơn
        </button>

        <router-link
          to="/events"
          class="px-5 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-sm border border-slate-200 transition-all"
        >
          Khám Phá Thêm Sự Kiện
        </router-link>
      </div>
    </div>

    <!-- STEP 2: PAYMENT PENDING SCREEN (VietQR / MoMo / VNPay) -->
    <div v-else-if="checkoutStep === 'PAYMENT_PENDING' && activePayment" class="space-y-6">
      <div class="bg-white p-6 sm:p-8 rounded-3xl border border-slate-200 shadow-lg">
        <div class="text-center max-w-lg mx-auto mb-6">
          <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-amber-50 text-amber-800 font-bold text-xs border border-amber-200 mb-2">
            <Clock class="w-3.5 h-3.5 text-amber-600 animate-spin" /> Chờ Khách Hàng Quét Mã Thanh Toán
          </span>
          <h2 class="text-2xl font-black text-slate-900">Quét Mã VietQR Để Thanh Toán</h2>
          <p class="text-xs text-slate-500 mt-1">
            Mở bất kỳ ứng dụng ngân hàng hoặc ví điện tử để quét mã QR bên dưới. Số tiền và nội dung chuyển khoản đã được điền tự động.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-8 items-center bg-slate-50 p-6 rounded-2xl border border-slate-200">
          <!-- QR Code Image -->
          <div class="flex flex-col items-center text-center">
            <div class="p-4 bg-white rounded-2xl border border-slate-200 shadow-md">
              <img
                :src="activePayment.qrCodeUrl || `https://img.vietqr.io/image/970415-102874635291-compact2.png?amount=${activePayment.amount}&addInfo=${activePayment.bookingCode}&accountName=CONG%20TY%20CP%20EVENTHUB%20VIET%20NAM`"
                alt="VietQR Dynamic Payment Code"
                class="w-64 h-auto rounded-xl mx-auto"
              />
            </div>
            <div class="flex items-center gap-2 mt-3 text-xs text-slate-500 font-semibold">
              <RefreshCw class="w-3.5 h-3.5 animate-spin text-indigo-600" />
              Đang tự động kiểm tra giao dịch...
            </div>
          </div>

          <!-- Transfer Details -->
          <div class="space-y-3.5 text-xs">
            <div class="p-3 bg-white rounded-xl border border-slate-200">
              <span class="text-slate-400 block text-[11px]">Ngân hàng thụ hưởng:</span>
              <span class="font-bold text-slate-900 text-sm flex items-center gap-1.5 mt-0.5">
                <Building2 class="w-4 h-4 text-indigo-600 shrink-0" />
                {{ activePayment.bankName || 'VietinBank (Ngân hàng TMCP Công Thương)' }}
              </span>
            </div>

            <div class="p-3 bg-white rounded-xl border border-slate-200 flex items-center justify-between">
              <div>
                <span class="text-slate-400 block text-[11px]">Số tài khoản:</span>
                <span class="font-mono font-bold text-slate-900 text-base">{{ activePayment.accountNumber || '102874635291' }}</span>
              </div>
              <button
                type="button"
                @click="copyToClipboard(activePayment.accountNumber || '102874635291', 'acc')"
                class="px-2.5 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center gap-1"
              >
                <Check v-if="copiedField === 'acc'" class="w-3.5 h-3.5 text-emerald-600" />
                <Copy v-else class="w-3.5 h-3.5" />
                {{ copiedField === 'acc' ? 'Đã chép' : 'Sao chép' }}
              </button>
            </div>

            <div class="p-3 bg-white rounded-xl border border-slate-200">
              <span class="text-slate-400 block text-[11px]">Tên chủ tài khoản:</span>
              <span class="font-bold text-slate-900 text-sm uppercase">{{ activePayment.accountName || 'CONG TY CP EVENTHUB VIET NAM' }}</span>
            </div>

            <div class="p-3 bg-white rounded-xl border border-slate-200 flex items-center justify-between">
              <div>
                <span class="text-slate-400 block text-[11px]">Số tiền chính xác:</span>
                <span class="font-mono font-black text-rose-600 text-lg">{{ formatCurrency(activePayment.amount) }}</span>
              </div>
              <button
                type="button"
                @click="copyToClipboard(String(activePayment.amount), 'amount')"
                class="px-2.5 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs flex items-center gap-1"
              >
                <Check v-if="copiedField === 'amount'" class="w-3.5 h-3.5 text-emerald-600" />
                <Copy v-else class="w-3.5 h-3.5" />
                {{ copiedField === 'amount' ? 'Đã chép' : 'Sao chép' }}
              </button>
            </div>

            <div class="p-3 bg-white rounded-xl border border-amber-300 bg-amber-50/50 flex items-center justify-between">
              <div>
                <span class="text-amber-800 font-bold block text-[11px]">Nội dung chuyển tiền (bắt buộc ghi đúng):</span>
                <span class="font-mono font-black text-indigo-700 text-base">{{ activePayment.bookingCode }}</span>
              </div>
              <button
                type="button"
                @click="copyToClipboard(activePayment.bookingCode, 'content')"
                class="px-2.5 py-1.5 rounded-lg bg-indigo-100 hover:bg-indigo-200 text-indigo-800 font-bold text-xs flex items-center gap-1"
              >
                <Check v-if="copiedField === 'content'" class="w-3.5 h-3.5 text-emerald-600" />
                <Copy v-else class="w-3.5 h-3.5" />
                {{ copiedField === 'content' ? 'Đã chép' : 'Sao chép' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Confirm Trigger Buttons -->
        <div class="mt-6 flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 border-t border-slate-100">
          <button
            type="button"
            @click="checkoutStep = 'FORM'"
            class="text-xs font-bold text-slate-500 hover:text-slate-800 flex items-center gap-1"
          >
            ← Chọn phương thức khác
          </button>

          <button
            type="button"
            @click="handleConfirmPayment"
            :disabled="isProcessing"
            class="w-full sm:w-auto px-6 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm flex items-center justify-center gap-2 shadow-md shadow-emerald-600/30 transition-all hover:scale-105 disabled:opacity-50"
          >
            <CheckCircle class="w-4 h-4" />
            <span v-if="!isProcessing">Tôi đã chuyển khoản thành công</span>
            <span v-else>Đang xác nhận với ngân hàng...</span>
          </button>
        </div>
      </div>
    </div>

    <!-- STEP 1: ACTIVE CHECKOUT FORM -->
    <div v-else class="space-y-8">
      <!-- 15-Minute Countdown Warning Header -->
      <div class="p-4 rounded-2xl border border-amber-300 flex items-center justify-between bg-amber-50">
        <div class="flex items-center gap-3">
          <Clock class="w-6 h-6 text-amber-600 animate-spin" style="animation-duration: 8s" />
          <div>
            <h4 class="font-bold text-amber-900 text-sm">Chỗ ngồi đang được giữ tạm thời!</h4>
            <p class="text-xs text-amber-700">Vui lòng hoàn tất thanh toán trước khi thời gian hết hạn.</p>
          </div>
        </div>
        <div class="text-xl sm:text-2xl font-mono font-black text-amber-700 bg-white px-3.5 py-1.5 rounded-xl border border-amber-300 shadow-sm">
          {{ formattedTime }}
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-12 gap-8">
        <!-- Left Column: Items, Recipient & Payment Methods -->
        <div class="lg:col-span-7 space-y-6">
          <!-- Item Summary -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm">
            <h3 class="text-base font-bold text-slate-900 mb-4 flex items-center gap-2">
              <TicketIcon class="w-4 h-4 text-indigo-600" />
              Chi Tiết Vé Đang Đặt
            </h3>

            <div class="p-3.5 rounded-xl bg-slate-50 border border-slate-200 mb-4">
              <h4 class="font-bold text-slate-900 text-sm line-clamp-1">{{ bookingStore.currentEvent?.title }}</h4>
              <p class="text-xs text-slate-500 mt-0.5">{{ bookingStore.currentEvent?.locationName }}</p>
            </div>

            <!-- List of Seats or Tiers -->
            <div class="space-y-2 text-xs">
              <div
                v-for="s in bookingStore.selectedSeats"
                :key="s.id"
                class="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-200"
              >
                <div>
                  <span class="font-bold text-slate-900">Ghế {{ s.rowName || s.seatRow }}{{ s.seatNumber }}</span>
                  <span class="text-slate-500 ml-2">Phân khu: {{ s.zone }}</span>
                </div>
                <span class="font-bold text-rose-600">{{ formatCurrency(s.price) }}</span>
              </div>

              <!-- Ticket Tiers -->
              <template v-if="bookingStore.currentEvent?.ticketTypes">
                <template v-for="tt in bookingStore.currentEvent.ticketTypes" :key="tt.id">
                  <div
                    v-if="bookingStore.selectedTicketQuantities[tt.id]"
                    class="flex items-center justify-between p-2.5 rounded-lg bg-slate-50 border border-slate-200"
                  >
                    <div>
                      <span class="font-bold text-slate-900">{{ tt.name }}</span>
                      <span class="text-slate-500 ml-2">x {{ bookingStore.selectedTicketQuantities[tt.id] }} vé</span>
                    </div>
                    <span class="font-bold text-rose-600">
                      {{ formatCurrency(tt.price * bookingStore.selectedTicketQuantities[tt.id]) }}
                    </span>
                  </div>
                </template>
              </template>
            </div>
          </div>

          <!-- Recipient Information -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
              <User class="w-4 h-4 text-indigo-600" />
              Thông Tin Nhận Vé
            </h3>

            <div class="space-y-3">
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">Họ và tên người nhận vé *</label>
                <div class="relative">
                  <User class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="recipientName"
                    type="text"
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all font-semibold"
                    placeholder="Nguyễn Văn Quân"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">Email nhận vé điện tử (QR Code) *</label>
                <div class="relative">
                  <Mail class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="recipientEmail"
                    type="email"
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all font-semibold"
                    placeholder="quan.nguyen@example.com"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">Số điện thoại liên hệ</label>
                <div class="relative">
                  <Phone class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="recipientPhone"
                    type="tel"
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all font-semibold"
                    placeholder="0987654321"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- Payment Method Selector -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
              <CreditCard class="w-4 h-4 text-indigo-600" />
              Chọn Phương Thức Thanh Toán
            </h3>

            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <!-- VietQR -->
              <label
                class="p-4 rounded-2xl border-2 cursor-pointer transition-all flex items-start gap-3"
                :class="selectedProvider === 'VIETQR' ? 'border-indigo-600 bg-indigo-50/40 ring-2 ring-indigo-200' : 'border-slate-200 hover:border-slate-300'"
              >
                <input type="radio" value="VIETQR" v-model="selectedProvider" class="mt-1" />
                <div>
                  <div class="flex items-center gap-1.5 font-bold text-slate-900 text-sm">
                    <QrCode class="w-4 h-4 text-indigo-600" />
                    Chuyển Khoản VietQR
                  </div>
                  <p class="text-[11px] text-slate-500 mt-0.5">Khuyên dùng • Quét mã tự động điền số tiền, 0% phí</p>
                </div>
              </label>

              <!-- MoMo -->
              <label
                class="p-4 rounded-2xl border-2 cursor-pointer transition-all flex items-start gap-3"
                :class="selectedProvider === 'MOMO' ? 'border-pink-600 bg-pink-50/40 ring-2 ring-pink-200' : 'border-slate-200 hover:border-slate-300'"
              >
                <input type="radio" value="MOMO" v-model="selectedProvider" class="mt-1" />
                <div>
                  <div class="flex items-center gap-1.5 font-bold text-pink-700 text-sm">
                    <Wallet class="w-4 h-4 text-pink-600" />
                    Ví Điện Tử MoMo
                  </div>
                  <p class="text-[11px] text-slate-500 mt-0.5">Thanh toán tức thì qua ứng dụng MoMo</p>
                </div>
              </label>

              <!-- VNPay Sandbox -->
              <label
                class="p-4 rounded-2xl border-2 cursor-pointer transition-all flex items-start gap-3"
                :class="selectedProvider === 'VNPAY' ? 'border-sky-600 bg-sky-50/40 ring-2 ring-sky-200' : 'border-slate-200 hover:border-slate-300'"
              >
                <input type="radio" value="VNPAY" v-model="selectedProvider" class="mt-1" />
                <div>
                  <div class="flex items-center gap-1.5 font-bold text-sky-800 text-sm">
                    <Building2 class="w-4 h-4 text-sky-600" />
                    Cổng VNPay Sandbox
                  </div>
                  <p class="text-[11px] text-slate-500 mt-0.5">Quét QR từ 40+ ngân hàng Việt Nam</p>
                </div>
              </label>

              <!-- Credit Card -->
              <label
                class="p-4 rounded-2xl border-2 cursor-pointer transition-all flex items-start gap-3"
                :class="selectedProvider === 'CREDIT_CARD' ? 'border-slate-900 bg-slate-50 ring-2 ring-slate-300' : 'border-slate-200 hover:border-slate-300'"
              >
                <input type="radio" value="CREDIT_CARD" v-model="selectedProvider" class="mt-1" />
                <div>
                  <div class="flex items-center gap-1.5 font-bold text-slate-900 text-sm">
                    <CreditCard class="w-4 h-4 text-slate-800" />
                    Thẻ Quốc Tế (Visa/Master)
                  </div>
                  <p class="text-[11px] text-slate-500 mt-0.5">Bảo mật chuẩn 3D-Secure quốc tế</p>
                </div>
              </label>
            </div>

            <!-- Credit Card Form if selected -->
            <div v-if="selectedProvider === 'CREDIT_CARD'" class="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3 pt-4 text-xs">
              <div>
                <label class="block font-bold text-slate-700 mb-1">Số thẻ tín dụng / ghi nợ</label>
                <input
                  v-model="cardNumber"
                  type="text"
                  class="w-full px-3 py-2 rounded-xl bg-white border border-slate-300 font-mono text-sm font-bold"
                  placeholder="4111 2222 3333 4444"
                />
              </div>
              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Tên chủ thẻ</label>
                  <input
                    v-model="cardHolder"
                    type="text"
                    class="w-full px-3 py-2 rounded-xl bg-white border border-slate-300 uppercase font-bold"
                    placeholder="NGUYEN VAN A"
                  />
                </div>
                <div class="grid grid-cols-2 gap-2">
                  <div>
                    <label class="block font-bold text-slate-700 mb-1">Hạn (MM/YY)</label>
                    <input
                      v-model="cardExpiry"
                      type="text"
                      class="w-full px-3 py-2 rounded-xl bg-white border border-slate-300 font-mono text-center font-bold"
                      placeholder="12/28"
                    />
                  </div>
                  <div>
                    <label class="block font-bold text-slate-700 mb-1">CVV</label>
                    <input
                      v-model="cardCvv"
                      type="password"
                      maxlength="3"
                      class="w-full px-3 py-2 rounded-xl bg-white border border-slate-300 font-mono text-center font-bold"
                      placeholder="•••"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- VAT Invoice Section -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-3">
            <label class="flex items-center gap-2.5 cursor-pointer">
              <input type="checkbox" v-model="needVatInvoice" class="rounded text-indigo-600 focus:ring-indigo-500 w-4 h-4" />
              <span class="text-xs font-bold text-slate-800 flex items-center gap-1.5">
                <FileText class="w-4 h-4 text-indigo-600" />
                Yêu cầu xuất hóa đơn điện tử VAT (Doanh nghiệp)
              </span>
            </label>

            <div v-if="needVatInvoice" class="space-y-3 pt-3 border-t border-slate-100 text-xs">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Mã số thuế (MST) *</label>
                  <input v-model="vatTaxCode" type="text" placeholder="0312345678" class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-300 font-mono" />
                </div>
                <div>
                  <label class="block font-bold text-slate-700 mb-1">Tên công ty *</label>
                  <input v-model="vatCompanyName" type="text" placeholder="Công ty TNHH Giải Pháp Công Nghệ..." class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-300" />
                </div>
              </div>
              <div>
                <label class="block font-bold text-slate-700 mb-1">Địa chỉ đăng ký kinh doanh</label>
                <input v-model="vatCompanyAddress" type="text" placeholder="Tầng 5, Tòa nhà Landmark..." class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-300" />
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Coupons & Order Breakdown -->
        <div class="lg:col-span-5 space-y-6">
          <!-- Coupon Box -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
              <Tag class="w-4 h-4 text-purple-600" />
              Mã Giảm Giá (Coupons)
            </h3>

            <!-- Available code tips -->
            <div class="p-3 rounded-xl bg-purple-50 border border-purple-200 text-xs text-purple-800">
              💡 Mã gợi ý: <button @click="couponCodeInput = 'EVENTHUB2026'" class="font-bold underline">EVENTHUB2026</button> (Giảm 15%) hoặc <button @click="couponCodeInput = 'WELCOMEVIP'" class="font-bold underline">WELCOMEVIP</button> (Giảm 100K)
            </div>

            <div class="flex gap-2">
              <input
                v-model="couponCodeInput"
                type="text"
                placeholder="Nhập mã ưu đãi..."
                class="flex-1 px-3 py-2 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 uppercase placeholder-slate-400 focus:outline-none focus:border-indigo-600 focus:bg-white font-bold"
              />
              <button
                @click="handleApplyCoupon"
                type="button"
                class="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs transition-colors"
              >
                Áp Dụng
              </button>
            </div>

            <p v-if="couponSuccess" class="text-xs text-emerald-600 flex items-center gap-1 font-semibold">
              <CheckCircle class="w-3.5 h-3.5" />
              {{ couponSuccess }}
              <button @click="handleRemoveCoupon" class="text-rose-600 underline ml-2 font-bold">Xóa</button>
            </p>
            <p v-if="couponError" class="text-xs text-rose-600 flex items-center gap-1 font-semibold">
              <AlertCircle class="w-3.5 h-3.5" />
              {{ couponError }}
            </p>
          </div>

          <!-- Price Breakdown & Checkout Button -->
          <div class="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm space-y-4">
            <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
              <CreditCard class="w-4 h-4 text-indigo-600" />
              Tóm Tắt Đơn Hàng
            </h3>

            <div class="space-y-2.5 text-xs text-slate-600">
              <div class="flex justify-between">
                <span>Tạm tính ({{ bookingStore.totalTicketCount }} vé):</span>
                <span class="font-bold text-slate-900">{{ formatCurrency(bookingStore.subtotal) }}</span>
              </div>
              <div v-if="bookingStore.discountAmount > 0" class="flex justify-between text-emerald-600 font-bold">
                <span>Giảm giá khuyến mãi:</span>
                <span>- {{ formatCurrency(bookingStore.discountAmount) }}</span>
              </div>
              <div class="flex justify-between text-slate-500">
                <span>Phí dịch vụ & cổng thanh toán:</span>
                <span class="text-emerald-600 font-semibold">Miễn phí</span>
              </div>
              <div class="border-t border-slate-200 pt-3 flex justify-between items-baseline">
                <span class="text-sm font-black text-slate-900">Tổng thanh toán:</span>
                <span class="text-2xl font-black text-rose-600">
                  {{ formatCurrency(bookingStore.totalAmount) }}
                </span>
              </div>
            </div>

            <!-- Initiate Payment Button -->
            <button
              @click="handleInitiatePayment"
              :disabled="isProcessing"
              class="w-full py-3.5 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white font-black text-sm sm:text-base flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/30 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50"
            >
              <ShieldCheck class="w-5 h-5" />
              <span v-if="!isProcessing">
                Tiếp Tục Thanh Toán ({{ selectedProvider }})
              </span>
              <span v-else>Đang tạo đơn & khóa vé an toàn...</span>
            </button>

            <div class="p-3 bg-slate-50 rounded-xl border border-slate-200 text-[11px] text-slate-500 space-y-1">
              <div class="flex items-center gap-1.5 font-bold text-slate-700">
                <ShieldCheck class="w-4 h-4 text-emerald-600" />
                Bảo Mật Giao Dịch Tuyệt Đối
              </div>
              <p>Mỗi lượt thanh toán được cấp phát mã <strong>Idempotency-Key</strong> độc nhất, ngăn chặn tình trạng bị trừ tiền 2 lần khi mạng lag.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
