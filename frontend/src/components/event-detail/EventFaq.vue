<script setup lang="ts">
import { ref } from 'vue'
import type { FaqItem } from '@/services/eventDetailData'
import { ChevronDown } from 'lucide-vue-next'

defineProps<{
  faqs: FaqItem[]
}>()

const openIndex = ref<number | null>(0)
const toggle = (i: number) => (openIndex.value = openIndex.value === i ? null : i)
</script>

<template>
  <div class="divide-y divide-slate-100 rounded-2xl border border-slate-200 overflow-hidden">
    <div v-for="(f, i) in faqs" :key="f.question" class="bg-white">
      <button
        type="button"
        :id="`faq-toggle-${i}`"
        @click="toggle(i)"
        class="w-full flex items-center justify-between gap-4 px-5 py-4 text-left hover:bg-slate-50 transition"
        :aria-expanded="openIndex === i"
      >
        <span class="text-sm font-bold" :class="openIndex === i ? 'text-indigo-700' : 'text-slate-800'">{{ f.question }}</span>
        <ChevronDown
          class="w-4 h-4 shrink-0 transition-transform duration-300"
          :class="openIndex === i ? 'rotate-180 text-indigo-600' : 'text-slate-400'"
        />
      </button>
      <div
        class="grid transition-all duration-300 ease-out"
        :class="openIndex === i ? 'grid-rows-[1fr] opacity-100' : 'grid-rows-[0fr] opacity-0'"
      >
        <div class="overflow-hidden">
          <p class="px-5 pb-4 text-sm text-slate-600 leading-relaxed">{{ f.answer }}</p>
        </div>
      </div>
    </div>
  </div>
</template>
