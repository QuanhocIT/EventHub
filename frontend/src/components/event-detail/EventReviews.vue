<script setup lang="ts">
import { computed, ref } from 'vue'
import type { EventReview } from '@/services/eventDetailData'
import { Star, ThumbsUp, PenLine, Send } from 'lucide-vue-next'

const props = defineProps<{
  reviews: EventReview[]
}>()

const emit = defineEmits<{
  (e: 'notify', message: string, type?: 'error' | 'success' | 'info'): void
}>()

const localReviews = ref<EventReview[]>([...props.reviews])
const likedIds = ref<Set<number>>(new Set())
const showForm = ref(false)
const draft = ref({ rating: 5, comment: '' })
const hoverRating = ref(0)

const avg = computed(() =>
  localReviews.value.length
    ? localReviews.value.reduce((a, r) => a + r.rating, 0) / localReviews.value.length
    : 0
)

const distribution = computed(() =>
  [5, 4, 3, 2, 1].map((star) => {
    const count = localReviews.value.filter((r) => r.rating === star).length
    return { star, count, percent: localReviews.value.length ? (count / localReviews.value.length) * 100 : 0 }
  })
)

const toggleHelpful = (r: EventReview) => {
  if (likedIds.value.has(r.id)) {
    likedIds.value.delete(r.id)
    r.helpful--
  } else {
    likedIds.value.add(r.id)
    r.helpful++
  }
}

const submit = () => {
  if (draft.value.comment.trim().length < 10) {
    emit('notify', 'Nội dung đánh giá cần tối thiểu 10 ký tự.', 'error')
    return
  }
  localReviews.value.unshift({
    id: Date.now(),
    userName: 'Bạn',
    avatar: 'https://api.dicebear.com/7.x/initials/svg?seed=You&backgroundColor=6366f1',
    rating: draft.value.rating,
    comment: draft.value.comment.trim(),
    date: new Date().toISOString(),
    helpful: 0,
  })
  draft.value = { rating: 5, comment: '' }
  showForm.value = false
  emit('notify', 'Cảm ơn bạn đã chia sẻ đánh giá!', 'success')
}

const formatDate = (d: string) => new Intl.DateTimeFormat('vi-VN').format(new Date(d))
</script>

<template>
  <div class="space-y-6">
    <!-- Summary -->
    <div class="grid grid-cols-1 sm:grid-cols-[180px_1fr] gap-6 p-5 rounded-2xl bg-gradient-to-br from-amber-50 to-orange-50 border border-amber-100">
      <div class="text-center sm:border-r border-amber-200/70 sm:pr-6">
        <div class="text-5xl font-black text-slate-900 leading-none">{{ avg.toFixed(1) }}</div>
        <div class="flex justify-center gap-0.5 my-2">
          <Star
            v-for="i in 5"
            :key="i"
            class="w-4 h-4"
            :class="i <= Math.round(avg) ? 'fill-amber-400 text-amber-400' : 'text-amber-200'"
          />
        </div>
        <div class="text-xs text-slate-500 font-semibold">{{ localReviews.length }} đánh giá từ mùa trước</div>
      </div>
      <div class="space-y-1.5">
        <div v-for="d in distribution" :key="d.star" class="flex items-center gap-2 text-xs">
          <span class="w-8 font-bold text-slate-600 flex items-center gap-0.5">{{ d.star }}<Star class="w-3 h-3 fill-amber-400 text-amber-400" /></span>
          <div class="flex-1 h-2 rounded-full bg-white overflow-hidden">
            <div class="h-full bg-gradient-to-r from-amber-400 to-orange-400 rounded-full" :style="{ width: `${d.percent}%` }"></div>
          </div>
          <span class="w-6 text-right text-slate-500 font-semibold">{{ d.count }}</span>
        </div>
      </div>
    </div>

    <!-- Write review -->
    <div>
      <button
        v-if="!showForm"
        id="review-write-btn"
        type="button"
        @click="showForm = true"
        class="w-full py-3 rounded-2xl border-2 border-dashed border-slate-300 text-sm font-bold text-slate-600 hover:border-indigo-400 hover:text-indigo-600 hover:bg-indigo-50/50 transition flex items-center justify-center gap-2"
      >
        <PenLine class="w-4 h-4" /> Viết đánh giá của bạn
      </button>
      <form v-else @submit.prevent="submit" class="p-5 rounded-2xl border border-indigo-200 bg-indigo-50/40 space-y-3">
        <div class="flex items-center gap-1" @mouseleave="hoverRating = 0">
          <button
            v-for="i in 5"
            :key="i"
            type="button"
            @click="draft.rating = i"
            @mouseenter="hoverRating = i"
            :aria-label="`${i} sao`"
          >
            <Star
              class="w-6 h-6 transition-transform hover:scale-110"
              :class="i <= (hoverRating || draft.rating) ? 'fill-amber-400 text-amber-400' : 'text-slate-300'"
            />
          </button>
        </div>
        <textarea
          id="review-comment"
          v-model="draft.comment"
          rows="3"
          placeholder="Chia sẻ trải nghiệm của bạn về sự kiện..."
          class="w-full rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300"
        ></textarea>
        <div class="flex justify-end gap-2">
          <button type="button" @click="showForm = false" class="px-4 py-2 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100">Hủy</button>
          <button id="review-submit" type="submit" class="px-4 py-2 rounded-xl text-xs font-bold bg-indigo-600 text-white hover:bg-indigo-700 flex items-center gap-1.5">
            <Send class="w-3.5 h-3.5" /> Gửi đánh giá
          </button>
        </div>
      </form>
    </div>

    <!-- List -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <article v-for="r in localReviews" :key="r.id" class="p-5 rounded-2xl border border-slate-200 bg-white hover:shadow-md transition">
        <header class="flex items-center gap-3 mb-3">
          <img :src="r.avatar" :alt="r.userName" class="w-10 h-10 rounded-full object-cover" loading="lazy" />
          <div class="flex-1 min-w-0">
            <div class="font-bold text-sm text-slate-900">{{ r.userName }}</div>
            <div class="flex items-center gap-2">
              <div class="flex gap-0.5">
                <Star v-for="i in 5" :key="i" class="w-3 h-3" :class="i <= r.rating ? 'fill-amber-400 text-amber-400' : 'text-slate-200'" />
              </div>
              <span class="text-[11px] text-slate-400">{{ formatDate(r.date) }}</span>
            </div>
          </div>
        </header>
        <p class="text-sm text-slate-600 leading-relaxed mb-3">{{ r.comment }}</p>
        <button
          type="button"
          @click="toggleHelpful(r)"
          class="text-[11px] font-bold flex items-center gap-1 transition"
          :class="likedIds.has(r.id) ? 'text-indigo-600' : 'text-slate-400 hover:text-slate-600'"
        >
          <ThumbsUp class="w-3.5 h-3.5" :class="likedIds.has(r.id) ? 'fill-indigo-600' : ''" />
          Hữu ích ({{ r.helpful }})
        </button>
      </article>
    </div>
  </div>
</template>
