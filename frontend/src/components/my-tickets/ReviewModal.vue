<script setup lang="ts">
import { ref } from 'vue'
import type { Ticket } from '@/types'
import {
  X,
  Star,
  Award,
  Sparkles,
  CheckCircle2,
  Download,
  Share2,
  Calendar,
  MapPin,
  Heart,
  MessageSquare,
} from 'lucide-vue-next'

const props = defineProps<{
  ticket: Ticket | null
  show: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'reviewSubmitted', ticketId: number, rating: number, comment: string): void
  (e: 'notify', message: string, type?: 'success' | 'error' | 'info'): void
}>()

const rating = ref(5)
const hoverRating = ref(0)
const comment = ref('')
const organizationRating = ref(5)
const soundLightingRating = ref(5)
const venueRating = ref(5)
const isSubmitting = ref(false)
const submitted = ref(false)
const showCertificate = ref(false)

const ratingLabels: Record<number, string> = {
  1: 'Rất thất vọng',
  2: 'Chưa hài lòng',
  3: 'Bình thường',
  4: 'Hài lòng & Ấn tượng',
  5: 'Xuất sắc & Tuyệt vời',
}

const handleSubmit = () => {
  if (!props.ticket) return
  isSubmitting.value = true
  setTimeout(() => {
    isSubmitting.value = false
    submitted.value = true
    emit('reviewSubmitted', props.ticket!.id, rating.value, comment.value)
    emit('notify', 'Cảm ơn bạn đã gửi đánh giá quý giá về sự kiện!', 'success')
  }, 600)
}

const downloadCertificate = () => {
  emit('notify', 'Đang tải Giấy Chứng Nhận Tham Dự Sự Kiện (PDF)...', 'success')
}

const handleClose = () => {
  submitted.value = false
  showCertificate.value = false
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
      <!-- Header -->
      <div class="px-6 py-5 bg-gradient-to-r from-amber-600 via-amber-500 to-indigo-700 text-white flex items-center justify-between shrink-0">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center backdrop-blur-sm">
            <Award class="w-5 h-5 text-amber-200" />
          </div>
          <div>
            <h3 class="text-base sm:text-lg font-black text-white">
              Đánh Giá & Kỷ Niệm Sự Kiện
            </h3>
            <p class="text-xs text-amber-100">
              Chia sẻ trải nghiệm của bạn sau khi tham dự
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

      <!-- Body -->
      <div class="p-6 overflow-y-auto space-y-5 flex-1">
        <!-- Post-submission Certificate View -->
        <div v-if="submitted || showCertificate" class="text-center py-4 space-y-5">
          <div class="p-6 rounded-3xl bg-gradient-to-br from-amber-50 via-indigo-50/40 to-slate-50 border-2 border-amber-200 shadow-md relative overflow-hidden text-center">
            <!-- Ornamental corner rings -->
            <div class="absolute -top-10 -left-10 w-24 h-24 rounded-full bg-amber-400/10 border-2 border-amber-400/20"></div>
            <div class="absolute -bottom-10 -right-10 w-24 h-24 rounded-full bg-indigo-600/10 border-2 border-indigo-600/20"></div>

            <div class="flex items-center justify-center gap-1.5 text-xs font-black tracking-widest uppercase text-amber-700 mb-2">
              <Sparkles class="w-4 h-4 text-amber-500" />
              <span>Chứng Nhận Khán Giả Chính Thức</span>
              <Sparkles class="w-4 h-4 text-amber-500" />
            </div>

            <h4 class="text-xl sm:text-2xl font-serif font-black text-slate-900 mb-1">
              Certificate of Attendance
            </h4>
            <p class="text-xs text-slate-500 italic mb-4">Chứng nhận hoàn thành tham dự sự kiện</p>

            <div class="py-2 px-4 rounded-xl bg-white border border-slate-200 inline-block mb-3 shadow-sm">
              <span class="text-xs text-slate-400">Khán giả vinh dự:</span>
              <div class="text-base font-black text-indigo-700">{{ ticket.attendeeName }}</div>
            </div>

            <p class="text-xs text-slate-700 max-w-sm mx-auto leading-relaxed mb-4">
              Đã tham gia trọn vẹn sự kiện <strong>{{ ticket.eventTitle }}</strong> tổ chức tại {{ ticket.venueName }}.
            </p>

            <div class="flex items-center justify-center gap-4 text-[11px] text-slate-500 border-t border-slate-200/80 pt-3">
              <span>Mã vé: <strong class="font-mono text-slate-800">{{ ticket.ticketCode }}</strong></span>
              <span>&bull;</span>
              <span>Hạng: <strong class="text-indigo-600">{{ ticket.ticketTypeName }}</strong></span>
            </div>
          </div>

          <div class="flex items-center justify-center gap-3">
            <button
              @click="downloadCertificate"
              class="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-md transition flex items-center gap-2"
            >
              <Download class="w-4 h-4" />
              <span>Tải Chứng Nhận PDF (A4)</span>
            </button>
            <button
              @click="handleClose"
              class="px-5 py-2.5 rounded-xl border border-slate-300 text-slate-700 font-bold text-xs hover:bg-slate-50"
            >
              Đóng
            </button>
          </div>
        </div>

        <!-- Review Form -->
        <form v-else @submit.prevent="handleSubmit" class="space-y-5">
          <!-- Event brief card -->
          <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 flex items-center gap-3">
            <img
              :src="ticket.eventBannerUrl"
              :alt="ticket.eventTitle"
              class="w-16 h-16 rounded-xl object-cover shrink-0"
            />
            <div class="min-w-0 flex-1">
              <h4 class="text-xs sm:text-sm font-black text-slate-900 truncate">
                {{ ticket.eventTitle }}
              </h4>
              <p class="text-[11px] text-slate-500 mt-0.5 truncate">
                {{ ticket.venueName }}
              </p>
              <span class="inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded bg-emerald-50 text-emerald-700 border border-emerald-200">
                Đã check-in thành công
              </span>
            </div>
          </div>

          <!-- Overall Star Rating -->
          <div class="text-center py-2">
            <label class="block text-xs font-bold text-slate-600 uppercase tracking-wider mb-2">
              Mức độ hài lòng chung của bạn
            </label>
            <div class="flex items-center justify-center gap-2 mb-2">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                @mouseenter="hoverRating = star"
                @mouseleave="hoverRating = 0"
                @click="rating = star"
                class="p-1 transition-transform hover:scale-125 focus:outline-none"
              >
                <Star
                  class="w-8 h-8 transition-colors"
                  :class="[
                    (hoverRating || rating) >= star
                      ? 'fill-amber-400 text-amber-400'
                      : 'text-slate-200'
                  ]"
                />
              </button>
            </div>
            <div class="text-sm font-black text-amber-600">
              {{ ratingLabels[hoverRating || rating] }}
            </div>
          </div>

          <!-- Sub-ratings -->
          <div class="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-3">
            <div class="flex items-center justify-between text-xs">
              <span class="font-bold text-slate-700">Khâu tổ chức & soát vé cổng:</span>
              <div class="flex gap-1">
                <button
                  v-for="s in 5"
                  :key="s"
                  type="button"
                  @click="organizationRating = s"
                  class="p-0.5"
                >
                  <Star class="w-4 h-4" :class="organizationRating >= s ? 'fill-indigo-600 text-indigo-600' : 'text-slate-300'" />
                </button>
              </div>
            </div>

            <div class="flex items-center justify-between text-xs">
              <span class="font-bold text-slate-700">Âm thanh & Ánh sáng & Sân khấu:</span>
              <div class="flex gap-1">
                <button
                  v-for="s in 5"
                  :key="s"
                  type="button"
                  @click="soundLightingRating = s"
                  class="p-0.5"
                >
                  <Star class="w-4 h-4" :class="soundLightingRating >= s ? 'fill-indigo-600 text-indigo-600' : 'text-slate-300'" />
                </button>
              </div>
            </div>

            <div class="flex items-center justify-between text-xs">
              <span class="font-bold text-slate-700">Địa điểm & Không gian khán phòng:</span>
              <div class="flex gap-1">
                <button
                  v-for="s in 5"
                  :key="s"
                  type="button"
                  @click="venueRating = s"
                  class="p-0.5"
                >
                  <Star class="w-4 h-4" :class="venueRating >= s ? 'fill-indigo-600 text-indigo-600' : 'text-slate-300'" />
                </button>
              </div>
            </div>
          </div>

          <!-- Comment input -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1.5 flex items-center gap-1.5">
              <MessageSquare class="w-3.5 h-3.5 text-indigo-600" />
              <span>Cảm nhận và đóng góp ý kiến cho Ban Tổ Chức</span>
            </label>
            <textarea
              v-model="comment"
              rows="3"
              placeholder="Chia sẻ khoảnh khắc ấn tượng nhất của bạn hoặc những điều BTC có thể cải thiện tốt hơn ở các mùa sau..."
              class="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            ></textarea>
          </div>

          <!-- Submit Buttons -->
          <div class="pt-2 flex items-center justify-end gap-3">
            <button
              type="button"
              @click="handleClose"
              class="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 text-xs font-bold hover:bg-slate-50 transition"
            >
              Bỏ qua
            </button>
            <button
              type="submit"
              :disabled="isSubmitting"
              class="px-6 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-black shadow-md transition flex items-center gap-2"
            >
              <span v-if="isSubmitting">Đang gửi...</span>
              <span v-else>Gửi Đánh Giá & Nhận Chứng Nhận</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
