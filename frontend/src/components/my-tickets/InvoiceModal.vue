<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ticket } from '@/types'
import {
  X,
  FileText,
  Building2,
  Mail,
  MapPin,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  Download,
  ShieldCheck,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'invoiceSubmitted', ticketId: number, data: { companyName: string; taxCode: string; address: string; email: string }): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const invoiceType = ref<'COMPANY' | 'PERSONAL'>('COMPANY')
const companyName = ref(props.ticket?.invoiceCompanyName || '')
const taxCode = ref(props.ticket?.invoiceTaxCode || '')
const companyAddress = ref('Tòa nhà Landmark 81, 720A Điện Biên Phủ, Phường 22, Bình Thạnh, TP. HCM')
const recipientEmail = ref('ketoan@doanhnghiep.vn')
const note = ref('')
const isSubmitting = ref(false)
const submitted = ref(!!props.ticket?.invoiceRequested)

const isValid = computed(() => {
  if (invoiceType.value === 'COMPANY') {
    return companyName.value.trim().length > 3 && taxCode.value.trim().length >= 10 && recipientEmail.value.includes('@')
  }
  return recipientEmail.value.includes('@')
})

const handleSubmit = () => {
  if (!props.ticket) return
  isSubmitting.value = true
  setTimeout(() => {
    isSubmitting.value = false
    submitted.value = true
    emit('invoiceSubmitted', props.ticket!.id, {
      companyName: companyName.value,
      taxCode: taxCode.value,
      address: companyAddress.value,
      email: recipientEmail.value,
    })
    emit('notify', 'Yêu cầu xuất hóa đơn VAT đã được ghi nhận thành công!', 'success')
  }, 700)
}

const handleClose = () => {
  emit('close')
}
</script>

<template>
  <div
    v-if="show && ticket"
    class="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in"
    @click.self="handleClose"
  >
    <div class="relative w-full max-w-xl bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200 max-h-[92vh] flex flex-col">
      <!-- Modal Header -->
      <div class="px-6 py-5 bg-gradient-to-r from-indigo-700 via-indigo-600 to-purple-700 text-white flex items-center justify-between shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-white/15 flex items-center justify-center backdrop-blur-sm">
            <FileText class="w-5 h-5 text-white" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-black text-white">
              Yêu Cầu Hóa Đơn Điện Tử (VAT)
            </h3>
            <p class="text-xs text-indigo-100">
              Đơn hàng: #{{ ticket.bookingCode }} &bull; Vé: {{ ticket.ticketCode }}
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

      <!-- Modal Body -->
      <div class="p-6 overflow-y-auto space-y-5 flex-1">
        <!-- Success State -->
        <div v-if="submitted" class="text-center py-6 space-y-4">
          <div class="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 mx-auto flex items-center justify-center shadow-inner">
            <CheckCircle2 class="w-9 h-9" />
          </div>
          <div>
            <h4 class="text-lg font-black text-slate-900">Yêu cầu đã được tiếp nhận</h4>
            <p class="text-xs sm:text-sm text-slate-500 mt-1 max-w-sm mx-auto">
              Hóa đơn điện tử hợp lệ (kèm file XML & PDF có chữ ký số của Ban Tổ Chức) sẽ được phát hành qua email <strong>{{ recipientEmail }}</strong> trong vòng 48 giờ làm việc.
            </p>
          </div>

          <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-left space-y-2 text-xs">
            <div class="flex justify-between py-1 border-b border-slate-200/60">
              <span class="text-slate-500">Đơn vị nhận:</span>
              <span class="font-bold text-slate-800">{{ companyName || ticket.attendeeName }}</span>
            </div>
            <div v-if="taxCode" class="flex justify-between py-1 border-b border-slate-200/60">
              <span class="text-slate-500">Mã số thuế:</span>
              <span class="font-mono font-bold text-slate-800">{{ taxCode }}</span>
            </div>
            <div class="flex justify-between py-1">
              <span class="text-slate-500">Trạng thái:</span>
              <span class="font-bold text-emerald-600 flex items-center gap-1">
                <ShieldCheck class="w-3.5 h-3.5" /> Đang xử lý phát hành
              </span>
            </div>
          </div>

          <button
            @click="handleClose"
            class="px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-md transition"
          >
            Đóng cửa sổ
          </button>
        </div>

        <!-- Form State -->
        <form v-else @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Ticket quick summary banner -->
          <div class="p-3.5 rounded-2xl bg-indigo-50/70 border border-indigo-100 flex items-center justify-between">
            <div class="truncate mr-3">
              <div class="text-[11px] font-bold text-indigo-700 uppercase tracking-wider">Sự kiện áp dụng</div>
              <div class="text-xs font-black text-slate-900 truncate">{{ ticket.eventTitle }}</div>
            </div>
            <div class="text-right shrink-0">
              <span class="text-[11px] text-slate-500">Số tiền:</span>
              <div class="text-xs font-black text-indigo-600">
                {{ new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(ticket.price || 0) }}
              </div>
            </div>
          </div>

          <!-- Type Switch -->
          <div class="grid grid-cols-2 gap-2 p-1 bg-slate-100 rounded-2xl">
            <button
              type="button"
              @click="invoiceType = 'COMPANY'"
              class="py-2 text-xs font-bold rounded-xl transition-all"
              :class="invoiceType === 'COMPANY' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'"
            >
              Doanh Nghiệp / Tổ Chức
            </button>
            <button
              type="button"
              @click="invoiceType = 'PERSONAL'"
              class="py-2 text-xs font-bold rounded-xl transition-all"
              :class="invoiceType === 'PERSONAL' ? 'bg-white text-indigo-600 shadow-sm' : 'text-slate-600 hover:text-slate-900'"
            >
              Cá Nhân Mua Vé
            </button>
          </div>

          <!-- Company Name -->
          <div v-if="invoiceType === 'COMPANY'">
            <label class="block text-xs font-bold text-slate-700 mb-1">
              Tên Doanh Nghiệp / Công Ty <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <Building2 class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                v-model="companyName"
                type="text"
                placeholder="VD: CÔNG TY TNHH CÔNG NGHỆ VÀ TRUYỀN THÔNG ABC"
                required
                class="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 font-medium"
              />
            </div>
          </div>

          <!-- Tax Code -->
          <div v-if="invoiceType === 'COMPANY'">
            <label class="block text-xs font-bold text-slate-700 mb-1">
              Mã Số Thuế (MST) <span class="text-rose-500">*</span>
            </label>
            <input
              v-model="taxCode"
              type="text"
              placeholder="VD: 0312345678"
              required
              class="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 font-mono font-bold"
            />
          </div>

          <!-- Address -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">
              Địa Chỉ Đăng Ký Thuế / Hóa Đơn <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <MapPin class="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <textarea
                v-model="companyAddress"
                rows="2"
                placeholder="Địa chỉ trụ sở chính trên giấy phép kinh doanh..."
                required
                class="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              ></textarea>
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">
              Email Nhận Hóa Đơn Điện Tử <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <Mail class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                v-model="recipientEmail"
                type="email"
                placeholder="ketoan@congty.com"
                required
                class="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <p class="text-[11px] text-slate-400 mt-1">
              File hóa đơn XML gốc và bản thể hiện PDF sẽ được gửi trực tiếp về email này.
            </p>
          </div>

          <!-- Notice -->
          <div class="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-800 text-[11px] leading-relaxed flex items-start gap-2">
            <AlertCircle class="w-4 h-4 shrink-0 text-amber-600 mt-0.5" />
            <span>
              Theo quy định của Tổng cục Thuế, yêu cầu xuất hóa đơn phải được gửi trong vòng 72 giờ kể từ thời điểm thanh toán đơn vé.
            </span>
          </div>

          <!-- Actions -->
          <div class="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              @click="handleClose"
              class="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 text-xs font-bold hover:bg-slate-50 transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              :disabled="!isValid || isSubmitting"
              class="px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white text-xs font-black shadow-md transition flex items-center gap-2"
            >
              <span v-if="isSubmitting">Đang xử lý...</span>
              <span v-else>Gửi Yêu Cầu Xuất Hóa Đơn</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
