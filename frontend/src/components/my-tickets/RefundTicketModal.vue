<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ticket } from '@/types'
import { bookingService } from '@/services/booking.service'
import {
  X,
  RotateCcw,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
  CreditCard,
  Building2,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'refundRequested', ticketId: number, reason: string): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const reasons = [
  'Bận việc đột xuất không thể tham dự',
  'Mua nhầm hạng vé / nhầm số lượng',
  'Sự kiện dời lịch hoặc thay đổi địa điểm',
  'Lý do sức khỏe / cá nhân',
  'Khác (ghi rõ trong phần ghi chú)',
]

const selectedReason = ref(reasons[0])
const bankName = ref('Vietcombank')
const accountNumber = ref('')
const accountHolder = ref('')
const additionalNote = ref('')
const agreed = ref(false)
const loading = ref(false)
const success = ref(false)

const formatCurrency = (val: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const ticketPrice = computed(() => props.ticket?.price || 1000000)
// Calculate refund policy: 70% refund before 7 days
const refundPercentage = 70
const refundAmount = computed(() => Math.round((ticketPrice.value * refundPercentage) / 100))

const handleClose = () => {
  success.value = false
  agreed.value = false
  accountNumber.value = ''
  accountHolder.value = ''
  additionalNote.value = ''
  emit('close')
}

const submitRefund = async () => {
  if (!props.ticket) return
  if (!accountNumber.value.trim() || !accountHolder.value.trim()) {
    emit('notify', 'Vui lòng điền số tài khoản và tên chủ tài khoản nhận hoàn tiền', 'error')
    return
  }
  if (!agreed.value) {
    emit('notify', 'Vui lòng đồng ý với chính sách hoàn vé', 'error')
    return
  }

  loading.value = true
  try {
    const fullReason = `${selectedReason.value}${additionalNote.value ? ': ' + additionalNote.value : ''} (STK: ${accountNumber.value} - ${bankName.value} - ${accountHolder.value})`
    await bookingService.requestRefund(props.ticket.id, fullReason)
    success.value = true
    emit('refundRequested', props.ticket.id, fullReason)
    emit('notify', 'Yêu cầu hoàn vé đã được gửi thành công!', 'success')
  } catch (err: any) {
    emit('notify', err.message || 'Lỗi khi gửi yêu cầu hoàn vé', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div
    v-if="show && ticket"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-sm animate-fade-in"
    @click.self="handleClose"
  >
    <div class="relative w-full max-w-lg bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200 max-h-[90vh] flex flex-col">
      <!-- Modal Header -->
      <div class="px-6 py-5 bg-gradient-to-r from-rose-600 via-rose-700 to-red-700 text-white relative shrink-0">
        <button
          @click="handleClose"
          class="absolute top-4 right-4 w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors"
          aria-label="Đóng"
        >
          <X class="w-4 h-4" />
        </button>

        <div class="flex items-center gap-2.5">
          <div class="w-10 h-10 rounded-2xl bg-white/15 flex items-center justify-center shadow-inner">
            <RotateCcw class="w-5 h-5 text-rose-100" />
          </div>
          <div>
            <h3 class="text-lg font-black leading-tight">Yêu Cầu Hoàn Tiền / Hủy Vé</h3>
            <p class="text-xs text-rose-100 mt-0.5">Quy trình hoàn tiền tự động theo quy định của ban tổ chức</p>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="p-6 overflow-y-auto">
        <!-- Success State -->
        <div v-if="success" class="text-center py-6 space-y-4">
          <div class="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-inner">
            <CheckCircle2 class="w-9 h-9" />
          </div>
          <div>
            <h4 class="text-xl font-black text-slate-900">Đã Tiếp Nhận Yêu Cầu Hoàn Vé!</h4>
            <p class="text-sm text-slate-600 mt-1">
              Số tiền dự kiến hoàn trả <strong class="text-rose-600 font-bold">{{ formatCurrency(refundAmount) }}</strong> sẽ được chuyển về tài khoản của bạn trong 3 - 5 ngày làm việc.
            </p>
          </div>
          <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-left text-xs space-y-1.5">
            <div class="flex justify-between">
              <span class="text-slate-500">Mã vé:</span>
              <span class="font-mono font-bold text-slate-800">{{ ticket.ticketCode }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">Tài khoản nhận:</span>
              <span class="font-bold text-slate-800">{{ accountNumber }} ({{ bankName }})</span>
            </div>
            <div class="flex justify-between">
              <span class="text-slate-500">Trạng thái:</span>
              <span class="font-bold text-amber-600">Đang xử lý ngân hàng</span>
            </div>
          </div>
          <button
            @click="handleClose"
            class="w-full py-3 rounded-2xl bg-slate-900 hover:bg-slate-800 text-white font-black text-sm transition"
          >
            Đóng
          </button>
        </div>

        <!-- Form State -->
        <form v-else @submit.prevent="submitRefund" class="space-y-4">
          <!-- Ticket Details -->
          <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-between">
            <div>
              <span class="text-[10px] uppercase font-bold text-slate-400 block">Sự kiện & Vé</span>
              <h4 class="text-sm font-bold text-slate-900 line-clamp-1">{{ ticket.eventTitle }}</h4>
              <span class="text-xs text-slate-500 font-medium">{{ ticket.ticketTypeName }} • {{ ticket.seatLabel || 'Vé GA' }}</span>
            </div>
            <div class="text-right shrink-0">
              <span class="text-[10px] uppercase font-bold text-slate-400 block">Giá gốc</span>
              <span class="text-sm font-black text-slate-900">{{ formatCurrency(ticketPrice) }}</span>
            </div>
          </div>

          <!-- Refund Calculation Table -->
          <div class="p-4 rounded-2xl bg-rose-50/60 border border-rose-200/80 space-y-2">
            <div class="flex items-center justify-between text-xs">
              <span class="text-slate-600">Giá trị vé:</span>
              <span class="font-bold text-slate-800">{{ formatCurrency(ticketPrice) }}</span>
            </div>
            <div class="flex items-center justify-between text-xs">
              <span class="text-slate-600">Tỷ lệ hoàn trả (Chủ động trước 7 ngày):</span>
              <span class="font-bold text-indigo-700">{{ refundPercentage }}%</span>
            </div>
            <div class="pt-2 border-t border-rose-200 flex items-center justify-between">
              <span class="text-xs font-black text-slate-900">Số tiền bạn thực nhận:</span>
              <span class="text-base font-black text-rose-600">{{ formatCurrency(refundAmount) }}</span>
            </div>
          </div>

          <!-- Reason Selection -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Lý do yêu cầu hoàn vé <span class="text-rose-500">*</span>
            </label>
            <select
              v-model="selectedReason"
              class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 text-slate-900 bg-white"
            >
              <option v-for="r in reasons" :key="r" :value="r">{{ r }}</option>
            </select>
          </div>

          <!-- Bank Account Info -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1.5">
                Ngân hàng nhận hoàn tiền <span class="text-rose-500">*</span>
              </label>
              <select
                v-model="bankName"
                class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 text-slate-900 bg-white"
              >
                <option value="Vietcombank">Vietcombank</option>
                <option value="Techcombank">Techcombank</option>
                <option value="MB Bank">MB Bank</option>
                <option value="VietinBank">VietinBank</option>
                <option value="BIDV">BIDV</option>
                <option value="ACB">ACB</option>
                <option value="VPBank">VPBank</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1.5">
                Số tài khoản nhận tiền <span class="text-rose-500">*</span>
              </label>
              <input
                v-model="accountNumber"
                type="text"
                required
                placeholder="1012345678"
                class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 text-slate-900"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Họ tên chủ tài khoản (Không dấu) <span class="text-rose-500">*</span>
            </label>
            <input
              v-model="accountHolder"
              type="text"
              required
              placeholder="NGUYEN VAN QUAN"
              class="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 text-sm uppercase focus:outline-none focus:ring-2 focus:ring-rose-500 text-slate-900"
            />
          </div>

          <!-- Note -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">Ghi chú bổ sung (nếu có)</label>
            <textarea
              v-model="additionalNote"
              rows="2"
              placeholder="Thông tin thêm để ban tổ chức hỗ trợ nhanh hơn..."
              class="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-rose-500 text-slate-900"
            ></textarea>
          </div>

          <!-- Checkbox -->
          <label class="flex items-start gap-2.5 pt-1 cursor-pointer">
            <input
              v-model="agreed"
              type="checkbox"
              class="w-4 h-4 mt-0.5 text-rose-600 rounded border-slate-300 focus:ring-rose-500"
            />
            <span class="text-xs text-slate-600 leading-snug">
              Tôi đồng ý hủy vé và chấp nhận mức hoàn tiền theo chính sách quy định (70% giá trị vé). Vé sẽ bị khóa ngay sau khi xác nhận.
            </span>
          </label>

          <!-- Buttons -->
          <div class="flex items-center gap-3 pt-2">
            <button
              type="button"
              @click="handleClose"
              class="flex-1 py-3 rounded-xl border border-slate-300 text-slate-700 text-xs font-bold hover:bg-slate-50 transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              :disabled="loading || !agreed"
              class="flex-1 py-3 rounded-xl bg-rose-600 hover:bg-rose-700 disabled:opacity-40 disabled:cursor-not-allowed text-white text-xs font-black shadow-md flex items-center justify-center gap-2 transition"
            >
              <span>{{ loading ? 'Đang gửi yêu cầu...' : 'Gửi Yêu Cầu Hoàn Tiền' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
