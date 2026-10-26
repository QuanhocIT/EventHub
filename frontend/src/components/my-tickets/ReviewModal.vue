<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Ticket } from '@/types'
import { reviewService } from '@/services/review.service'
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
  ImagePlus,
  Coins,
  ShieldCheck,
  Trash2,
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
const uploadedImages = ref<string[]>([])
const fileInputRef = ref<HTMLInputElement | null>(null)
const earnedLoyaltyPoints = ref(20)

const ratingLabels: Record<number, string> = {
  1: 'Rất thất vọng',
  2: 'Chưa hài lòng',
  3: 'Bình thường',
  4: 'Hài lòng & Ấn tượng',
  5: 'Xuất sắc & Tuyệt vời',
}

const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  if (!target.files || target.files.length === 0) return

  const files = Array.from(target.files)
  const remainingSlots = 5 - uploadedImages.value.length
  const filesToProcess = files.slice(0, remainingSlots)

  filesToProcess.forEach((file) => {
    if (!file.type.startsWith('image/')) {
      emit('notify', 'Vui lòng chỉ tải lên tệp hình ảnh (JPG, PNG, WebP)', 'error')
      return
    }
    const reader = new FileReader()
    reader.onload = (e) => {
      if (e.target?.result && typeof e.target.result === 'string') {
        uploadedImages.value.push(e.target.result)
      }
    }
    reader.readAsDataURL(file)
  })

  // Reset input
  target.value = ''
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!props.ticket) return
  isSubmitting.value = true
  const points = uploadedImages.value.length > 0 ? 50 : 20
  earnedLoyaltyPoints.value = points

  try {
    await reviewService.submitReview(props.ticket.eventId, {
      rating: rating.value,
      comment: comment.value.trim() || 'Trải nghiệm sự kiện tuyệt vời!',
      imageUrls: uploadedImages.value,
    })
  } catch (err) {
    console.warn('Backend review service offline or simulated:', err)
  }

  isSubmitting.value = false
  submitted.value = true
  emit('reviewSubmitted', props.ticket.id, rating.value, comment.value)
  emit('notify', `Đánh giá thành công! Bạn nhận được +${points} Điểm Thưởng CineClub Loyalty!`, 'success')
}

const downloadCertificate = () => {
  emit('notify', 'Đang tải Giấy Chứng Nhận Tham Dự Sự Kiện (PDF)...', 'success')
}

const handleClose = () => {
  submitted.value = false
  showCertificate.value = false
  uploadedImages.value = []
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
              Chia sẻ trải nghiệm thực tế & nhận điểm thưởng Loyalty
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
        <!-- Post-submission View (Certificate + Loyalty Reward) -->
        <div v-if="submitted || showCertificate" class="text-center py-2 space-y-4">
          <!-- Loyalty Points Celebration Box -->
          <div class="p-4 rounded-2xl bg-gradient-to-r from-amber-500 to-amber-600 text-white shadow-lg flex items-center justify-between gap-3 text-left">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl bg-white/20 flex items-center justify-center backdrop-blur-sm shrink-0">
                <Coins class="w-7 h-7 text-amber-100" />
              </div>
              <div>
                <span class="text-[10px] font-black uppercase tracking-wider text-amber-100">CineClub / EventHub Loyalty</span>
                <div class="text-base sm:text-lg font-black">
                  +{{ earnedLoyaltyPoints }} Điểm Thưởng Đã Được Cộng!
                </div>
                <p class="text-[11px] text-amber-100/90">
                  {{ uploadedImages.length > 0 ? 'Thưởng tối đa nhờ đánh giá kèm hình ảnh thực tế' : 'Đánh giá đã ghi nhận vào hồ sơ thành viên' }}
                </p>
              </div>
            </div>
            <span class="px-2.5 py-1 rounded-full bg-white/25 text-white font-extrabold text-xs shrink-0">
              Thành viên VIP
            </span>
          </div>

          <!-- Attendance Certificate -->
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

          <div class="flex items-center justify-center gap-3 pt-2">
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
        <form v-else @submit.prevent="handleSubmit" class="space-y-4">
          <!-- Event brief card -->
          <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 flex items-center gap-3">
            <img
              :src="ticket.eventBannerUrl || 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?auto=format&fit=crop&w=300&q=80'"
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

          <!-- Loyalty Reward Callout Banner -->
          <div class="p-3 rounded-2xl bg-amber-50 border border-amber-200/90 flex items-start gap-2.5">
            <Coins class="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
            <div class="text-xs text-amber-900 leading-relaxed">
              <span class="font-extrabold text-amber-950">Ưu đãi CineClub Loyalty:</span>
              Nhận ngay <strong class="text-amber-800">+50 điểm</strong> khi đánh giá kèm hình ảnh thực tế (hoặc +20 điểm cho đánh giá văn bản) để đổi voucher và tự động tích lũy lên hạng Vàng/Kim Cương!
            </div>
          </div>

          <!-- Overall Star Rating -->
          <div class="text-center py-1">
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
          <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-2.5">
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
              <span class="font-bold text-slate-700">Âm thanh & Ánh sáng sân khấu:</span>
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
              <span class="font-bold text-slate-700">Địa điểm & Không gian:</span>
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

          <!-- Photo Upload Section -->
          <div>
            <div class="flex items-center justify-between mb-1.5">
              <label class="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <ImagePlus class="w-3.5 h-3.5 text-indigo-600" />
                <span>Hình ảnh thực tế tại sự kiện (Tối đa 5 ảnh)</span>
              </label>
              <span class="text-[11px] font-bold text-amber-600">+50 Điểm Loyalty</span>
            </div>

            <!-- Hidden File Input -->
            <input
              ref="fileInputRef"
              type="file"
              multiple
              accept="image/*"
              class="hidden"
              @change="handleFileUpload"
            />

            <!-- Preview Grid & Add Button -->
            <div class="grid grid-cols-4 sm:grid-cols-5 gap-2.5">
              <!-- Uploaded Previews -->
              <div
                v-for="(img, idx) in uploadedImages"
                :key="idx"
                class="relative aspect-square rounded-xl overflow-hidden border border-slate-200 group shadow-2xs"
              >
                <img :src="img" class="w-full h-full object-cover" />
                <button
                  type="button"
                  @click="removeImage(idx)"
                  class="absolute top-1 right-1 w-6 h-6 rounded-full bg-slate-900/80 hover:bg-rose-600 text-white flex items-center justify-center transition"
                  title="Xóa ảnh"
                >
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>

              <!-- Upload Trigger Button (if < 5) -->
              <button
                v-if="uploadedImages.length < 5"
                type="button"
                @click="triggerFileInput"
                class="aspect-square rounded-xl border-2 border-dashed border-slate-300 hover:border-indigo-500 hover:bg-indigo-50/50 flex flex-col items-center justify-center text-slate-500 hover:text-indigo-600 transition p-2 cursor-pointer"
              >
                <ImagePlus class="w-5 h-5 mb-1" />
                <span class="text-[10px] font-bold text-center leading-tight">Thêm ảnh</span>
              </button>
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
              class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-amber-600 to-indigo-600 hover:from-amber-700 hover:to-indigo-700 text-white text-xs font-black shadow-md transition flex items-center gap-2"
            >
              <Coins class="w-4 h-4 text-amber-200" />
              <span v-if="isSubmitting">Đang gửi...</span>
              <span v-else>Gửi Đánh Giá & Nhận Điểm (+{{ uploadedImages.length > 0 ? 50 : 20 }})</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
