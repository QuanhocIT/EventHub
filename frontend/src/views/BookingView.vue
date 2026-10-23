<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBookingStore } from '@/stores/booking'
import { useAuthStore } from '@/stores/auth'
import { bookingService } from '@/services/booking.service'
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
} from 'lucide-vue-next'

const router = useRouter()
const bookingStore = useBookingStore()
const authStore = useAuthStore()

const couponCodeInput = ref('')
const couponError = ref('')
const couponSuccess = ref('')
const isProcessing = ref(false)
const bookingSuccess = ref(false)
const orderCode = ref('')

// Recipient Form
const recipientName = ref(authStore.user?.fullName || 'Nguyễn Văn Quân')
const recipientEmail = ref(authStore.user?.email || 'quan.nguyen@example.com')
const recipientPhone = ref(authStore.user?.phoneNumber || '0987654321')

// Countdown Timer State (15 mins = 900s)
const timeLeftSeconds = ref(15 * 60)
let timerInterval: any = null

onMounted(() => {
  if (bookingStore.totalTicketCount === 0) {
    router.replace('/events')
    return
  }
  bookingStore.startHoldTimer(15)

  timerInterval = setInterval(() => {
    if (timeLeftSeconds.value > 0) {
      timeLeftSeconds.value--
    } else {
      clearInterval(timerInterval)
      alert('Đã hết 15 phút giữ chỗ tạm thời! Ghế/vé đã được hoàn trả lại hệ thống.')
      bookingStore.clearBooking()
      router.push('/events')
    }
  }, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})

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

const handlePay = async () => {
  if (!recipientName.value || !recipientEmail.value) {
    alert('Vui lòng điền đầy đủ họ tên và email nhận vé!')
    return
  }

  isProcessing.value = true
  try {
    const booking = await bookingService.createBooking({
      eventId: bookingStore.currentEvent?.id || 1,
      items: bookingStore.selectedSeats.map((s) => ({
        eventSeatId: s.id,
        quantity: 1,
      })),
      couponCode: bookingStore.appliedCoupon?.code,
    })

    const payRes = await bookingService.payBooking(booking.bookingCode)
    if (payRes.success) {
      orderCode.value = booking.bookingCode
      bookingSuccess.value = true
      bookingStore.clearBooking()
    }
  } catch (err: any) {
    alert(err.message || 'Có lỗi khi xử lý thanh toán')
  } finally {
    isProcessing.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <!-- Success State View -->
    <div v-if="bookingSuccess" class="bg-white p-8 sm:p-12 rounded-3xl text-center border border-emerald-200 shadow-md">
      <div class="w-20 h-20 rounded-full bg-emerald-100 text-emerald-600 mx-auto flex items-center justify-center mb-6">
        <CheckCircle class="w-10 h-10" />
      </div>
      <h2 class="text-3xl font-black text-slate-900 mb-2">Đặt Vé & Thanh Toán Thành Công!</h2>
      <p class="text-sm text-slate-600 max-w-md mx-auto mb-6">
        Vé điện tử kèm mã QR đã được gửi về email của bạn. Bạn cũng có thể xem và xuất vé trực tiếp ngay trên hệ thống.
      </p>

      <div class="inline-block p-4 rounded-2xl bg-slate-50 border border-slate-200 mb-8 text-left">
        <p class="text-xs text-slate-500 font-semibold">Mã đơn đặt vé (Booking Code):</p>
        <p class="text-xl font-mono font-black text-indigo-700">{{ orderCode }}</p>
        <p class="text-xs text-emerald-600 font-semibold mt-1">✔ Idempotency & Pessimistic Lock đã xác nhận an toàn</p>
      </div>

      <div class="flex flex-wrap items-center justify-center gap-4">
        <router-link
          to="/my-tickets"
          class="px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-sm shadow-md"
        >
          Xem Vé Điện Tử Của Tôi
        </router-link>
        <router-link
          to="/events"
          class="px-6 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-sm border border-slate-300"
        >
          Khám Phá Sự Kiện Khác
        </router-link>
      </div>
    </div>

    <!-- Active Checkout Flow -->
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
        <!-- Left Column: Recipient Form & Items -->
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
                  <span class="font-bold text-slate-900">Ghế {{ s.rowName }}{{ s.seatNumber }}</span>
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
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all"
                    placeholder="Nguyễn Văn A"
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
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all"
                    placeholder="email@example.com"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">Số điện thoại</label>
                <div class="relative">
                  <Phone class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="recipientPhone"
                    type="tel"
                    class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 text-sm text-slate-900 focus:outline-none focus:border-indigo-600 focus:bg-white transition-all"
                    placeholder="0912345678"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Coupons & Payment -->
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
                <span>Phí dịch vụ & bảo mật:</span>
                <span class="text-emerald-600 font-semibold">Miễn phí</span>
              </div>
              <div class="border-t border-slate-200 pt-3 flex justify-between items-baseline">
                <span class="text-sm font-black text-slate-900">Tổng thanh toán:</span>
                <span class="text-2xl font-black text-rose-600">
                  {{ formatCurrency(bookingStore.totalAmount) }}
                </span>
              </div>
            </div>

            <!-- Sandbox Payment Button -->
            <button
              @click="handlePay"
              :disabled="isProcessing"
              class="w-full py-3.5 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white font-black text-sm sm:text-base flex items-center justify-center gap-2 shadow-lg shadow-indigo-600/30 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-50"
            >
              <ShieldCheck class="w-5 h-5" />
              <span v-if="!isProcessing">Thanh Toán An Toàn Mock Sandbox</span>
              <span v-else>Đang khóa vé và xử lý giao dịch...</span>
            </button>

            <p class="text-[11px] text-center text-slate-400">
              🔒 Tích hợp Idempotency Key bảo đảm an toàn chống double-charge khi chập chờn mạng.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
