<script setup lang="ts">
import { ref } from 'vue'
import type { Ticket } from '@/types'
import { bookingService } from '@/services/booking.service'
import {
  X,
  ArrowRightLeft,
  ShieldCheck,
  AlertCircle,
  CheckCircle2,
  Send,
  User,
  Mail,
  Phone,
  Info,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'transferred', ticketId: number, recipientName: string, newTicketCode: string): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const form = ref({
  name: '',
  email: '',
  phone: '',
  message: '',
  confirmed: false,
})

const loading = ref(false)
const successResult = ref<{ newTicketCode: string; recipientName: string } | null>(null)

const handleClose = () => {
  form.value = { name: '', email: '', phone: '', message: '', confirmed: false }
  successResult.value = null
  emit('close')
}

const submitTransfer = async () => {
  if (!props.ticket) return
  if (!form.value.name.trim() || !form.value.email.trim() || !form.value.phone.trim()) {
    emit('notify', 'Vui lòng điền đầy đủ họ tên, email và số điện thoại người nhận', 'error')
    return
  }
  if (!form.value.confirmed) {
    emit('notify', 'Vui lòng xác nhận đồng ý điều khoản chuyển nhượng vé', 'error')
    return
  }

  loading.value = true
  try {
    const res = await bookingService.transferTicket(props.ticket.id, {
      name: form.value.name.trim(),
      email: form.value.email.trim(),
      phone: form.value.phone.trim(),
    })
    successResult.value = {
      newTicketCode: res.newTicketCode,
      recipientName: form.value.name.trim(),
    }
    emit('transferred', props.ticket.id, form.value.name.trim(), res.newTicketCode)
    emit('notify', `Đã chuyển nhượng vé thành công cho ${form.value.name}!`, 'success')
  } catch (err: any) {
    emit('notify', err.message || 'Lỗi khi chuyển nhượng vé', 'error')
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
    <div class="relative w-full max-w-lg bg-white rounded-3xl overflow-hidden shadow-2xl border border-slate-200">
      <!-- Modal Header -->
      <div class="px-6 py-5 bg-gradient-to-r from-indigo-600 via-indigo-700 to-violet-700 text-white relative">
        <button
          @click="handleClose"
          class="absolute top-4 right-4 w-8 h-8 rounded-full bg-white/10 hover:bg-white/20 text-white flex items-center justify-center transition-colors"
          aria-label="Đóng"
        >
          <X class="w-4 h-4" />
        </button>

        <div class="flex items-center gap-2.5">
          <div class="w-10 h-10 rounded-2xl bg-white/15 flex items-center justify-center shadow-inner">
            <ArrowRightLeft class="w-5 h-5 text-indigo-200" />
          </div>
          <div>
            <h3 class="text-lg font-black leading-tight">Chuyển Nhượng Vé An Toàn</h3>
            <p class="text-xs text-indigo-200 mt-0.5">Mã hóa chống vé giả & tự động tạo vé mới cho người nhận</p>
          </div>
        </div>
      </div>

      <!-- Ticket Summary Card -->
      <div class="p-6">
        <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 mb-5 flex items-center gap-3">
          <img
            :src="ticket.eventBannerUrl"
            :alt="ticket.eventTitle"
            class="w-16 h-16 rounded-xl object-cover shrink-0 shadow-sm"
          />
          <div class="min-w-0 flex-1">
            <span class="inline-block px-2 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-indigo-100 text-indigo-700 mb-1">
              {{ ticket.ticketTypeName }}
            </span>
            <h4 class="text-sm font-bold text-slate-900 truncate leading-snug">{{ ticket.eventTitle }}</h4>
            <div class="text-xs text-slate-500 font-medium mt-0.5 flex items-center gap-2">
              <span>{{ ticket.seatLabel || 'Chỗ ngồi tự do' }}</span>
              <span>•</span>
              <span class="font-mono text-slate-700 font-semibold">{{ ticket.ticketCode }}</span>
            </div>
          </div>
        </div>

        <!-- Success Screen -->
        <div v-if="successResult" class="text-center py-6 space-y-4">
          <div class="w-16 h-16 rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center mx-auto shadow-inner">
            <CheckCircle2 class="w-9 h-9" />
          </div>
          <div>
            <h4 class="text-xl font-black text-slate-900">Chuyển Nhượng Thành Công!</h4>
            <p class="text-sm text-slate-600 mt-1">
              Vé đã được chuyển thành công tới khán giả <strong class="text-slate-900">{{ successResult.recipientName }}</strong>.
            </p>
          </div>
          <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-left text-xs space-y-2">
            <div class="flex justify-between">
              <span class="text-slate-500 font-medium">Mã vé cũ:</span>
              <span class="font-mono line-through text-slate-400">{{ ticket.ticketCode }}</span>
            </div>
            <div class="flex justify-between font-bold text-slate-900">
              <span class="text-emerald-700">Mã vé mới đã cấp:</span>
              <span class="font-mono text-indigo-600 font-black">{{ successResult.newTicketCode }}</span>
            </div>
            <p class="text-[11px] text-slate-500 pt-1 border-t border-slate-200">
              * Hệ thống đã gửi email kèm mã QR check-in mới tới người nhận. Vé cũ trong tài khoản của bạn đã được vô hiệu hóa.
            </p>
          </div>
          <button
            @click="handleClose"
            class="w-full py-3 rounded-2xl bg-indigo-600 hover:bg-indigo-700 text-white font-black text-sm shadow-md transition"
          >
            Hoàn Tất
          </button>
        </div>

        <!-- Form Screen -->
        <form v-else @submit.prevent="submitTransfer" class="space-y-4">
          <!-- Information Box -->
          <div class="p-3.5 rounded-2xl bg-amber-50 border border-amber-200/80 text-amber-900 text-xs flex gap-2.5">
            <Info class="w-4 h-4 shrink-0 mt-0.5 text-amber-600" />
            <p class="leading-relaxed">
              Mã QR và mã vé cũ của bạn sẽ <strong>vô hiệu lực tức thì</strong> để chống gian lận. Người nhận sẽ nhận được vé điện tử mới qua email.
            </p>
          </div>

          <!-- Recipient Name -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Họ và tên người nhận <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <User class="w-4 h-4" />
              </div>
              <input
                v-model="form.name"
                type="text"
                required
                placeholder="Ví dụ: Trần Minh Nhật"
                class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900"
              />
            </div>
          </div>

          <!-- Recipient Email -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Email người nhận <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Mail class="w-4 h-4" />
              </div>
              <input
                v-model="form.email"
                type="email"
                required
                placeholder="nguoinhan@gmail.com"
                class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900"
              />
            </div>
          </div>

          <!-- Recipient Phone -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Số điện thoại người nhận <span class="text-rose-500">*</span>
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Phone class="w-4 h-4" />
              </div>
              <input
                v-model="form.phone"
                type="tel"
                required
                placeholder="0912 345 678"
                class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-slate-900"
              />
            </div>
          </div>

          <!-- Message Optional -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5">
              Lời nhắn gửi kèm (không bắt buộc)
            </label>
            <textarea
              v-model="form.message"
              rows="2"
              placeholder="Tặng bạn tấm vé tham dự sự kiện này nhé!"
              class="w-full px-3.5 py-2 rounded-xl border border-slate-300 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 text-slate-900"
            ></textarea>
          </div>

          <!-- Checkbox Confirmation -->
          <label class="flex items-start gap-2.5 pt-1 cursor-pointer">
            <input
              v-model="form.confirmed"
              type="checkbox"
              class="w-4 h-4 mt-0.5 text-indigo-600 rounded border-slate-300 focus:ring-indigo-500"
            />
            <span class="text-xs text-slate-600 leading-snug">
              Tôi xác nhận chuyển nhượng vé này. Tôi hiểu rằng mã vé cũ sẽ bị hủy và tôi sẽ không thể sử dụng vé này để check-in nữa.
            </span>
          </label>

          <!-- Buttons -->
          <div class="flex items-center gap-3 pt-3">
            <button
              type="button"
              @click="handleClose"
              class="flex-1 py-3 rounded-xl border border-slate-300 text-slate-700 text-xs font-bold hover:bg-slate-50 transition"
            >
              Hủy bỏ
            </button>
            <button
              type="submit"
              :disabled="loading || !form.confirmed"
              class="flex-1 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:opacity-40 disabled:cursor-not-allowed text-white text-xs font-black shadow-md flex items-center justify-center gap-2 transition"
            >
              <Send v-if="!loading" class="w-4 h-4" />
              <span>{{ loading ? 'Đang chuyển vé...' : 'Xác Nhận Chuyển' }}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
