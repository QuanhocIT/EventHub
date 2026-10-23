<script setup lang="ts">
import { ref } from 'vue'
import type { AgendaDay } from '@/services/eventDetailData'
import { Clock, MapPin } from 'lucide-vue-next'

defineProps<{
  agenda: AgendaDay[]
}>()

const activeDay = ref(0)
</script>

<template>
  <div>
    <div v-if="agenda.length > 1" class="flex gap-2 mb-5">
      <button
        v-for="(day, i) in agenda"
        :key="day.label"
        :id="`agenda-day-${i}`"
        type="button"
        @click="activeDay = i"
        class="px-4 py-2 rounded-xl text-xs font-bold transition-all"
        :class="activeDay === i ? 'bg-indigo-600 text-white shadow-md shadow-indigo-500/25' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
      >
        {{ day.label }}
      </button>
    </div>

    <ol class="relative ml-3 border-l-2 border-dashed border-indigo-200">
      <li
        v-for="(item, idx) in agenda[activeDay]?.items || []"
        :key="`${activeDay}-${idx}`"
        class="relative pl-7 pb-6 last:pb-0 group"
      >
        <span
          class="absolute -left-[9px] top-1 w-4 h-4 rounded-full border-4 border-white shadow ring-2 transition-all group-hover:scale-125"
          :class="idx === 0 ? 'bg-indigo-600 ring-indigo-200' : 'bg-violet-400 ring-violet-100'"
        ></span>
        <div class="p-4 rounded-2xl bg-slate-50 border border-slate-100 group-hover:bg-white group-hover:border-indigo-200 group-hover:shadow-md transition-all">
          <div class="flex flex-wrap items-center gap-3 mb-1">
            <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-md bg-indigo-100 text-indigo-700 text-xs font-black tabular-nums">
              <Clock class="w-3 h-3" /> {{ item.time }}
            </span>
            <span v-if="item.location" class="inline-flex items-center gap-1 text-[11px] text-slate-500 font-semibold">
              <MapPin class="w-3 h-3" /> {{ item.location }}
            </span>
          </div>
          <h4 class="font-black text-slate-900 text-sm">{{ item.title }}</h4>
          <p v-if="item.description" class="text-xs text-slate-500 mt-0.5">{{ item.description }}</p>
        </div>
      </li>
    </ol>
  </div>
</template>
