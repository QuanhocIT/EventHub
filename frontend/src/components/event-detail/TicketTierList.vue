<script setup lang="ts">
import type { TicketType } from '@/types'
import { useBookingStore } from '@/stores/booking'
import { Plus, Minus, Flame, Check, Ban } from 'lucide-vue-next'

defineProps<{
  ticketTypes: TicketType[]
}>()

const emit = defineEmits<{
  (e: 'notify', message: string, type?: 'error' | 'success' | 'info'): void
}>()

const bookingStore = useBookingStore()

const qty = (t: TicketType) => bookingStore.selectedTicketQuantities[t.id] || 0

const change = (t: TicketType, delta: number) => {
  try {
    bookingStore.updateTicketQuantity(t, qty(t) + delta)
  } catch (err: any) {
    emit('notify', err.message, 'error')
  }
}

const soldPercent = (t: TicketType) =>
  t.totalQuantity ? Math.round(((t.totalQuantity - t.remainingQuantity) / t.totalQuantity) * 100) : 0

const formatCurrency = (val: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)

/** Very simple perk extraction: split description into bullet points. */
const perks = (t: TicketType) =>
  (t.description || '')
    .split(/,|\+|&/)
    .map((p) => p.trim().replace(/\.$/, ''))
    .filter((p) => p.length > 2)
    .slice(0, 4)
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <article
      v-for="t in ticketTypes"
      :key="t.id"
      :id="`ticket-tier-${t.id}`"
      class="relative bg-white rounded-2xl border overflow-hidden transition-all duration-300 flex flex-col"
      :class="qty(t) > 0 ? 'border-indigo-500 ring-2 ring-indigo-100 shadow-lg shadow-indigo-500/10' : 'border-slate-200 hover:border-indigo-300 hover:shadow-md'"
    >
      <!-- Colored top strip -->
      <div class="h-1.5 w-full" :style="{ background: `linear-gradient(90deg, ${t.colorHex || '#4f46e5'}, ${t.colorHex || '#4f46e5'}55)` }"></div>

      <!-- Ticket notch -->
      <span class="absolute top-1/2 -left-2.5 w-5 h-5 rounded-full bg-slate-50 border border-slate-200"></span>
      <span class="absolute top-1/2 -right-2.5 w-5 h-5 rounded-full bg-slate-50 border border-slate-200"></span>

      <div class="p-5 flex-1 flex flex-col">
        <div class="flex items-start justify-between gap-3 mb-2">
          <div class="flex items-center gap-2.5 min-w-0">
            <span class="w-3 h-3 rounded-full shrink-0" :style="{ backgroundColor: t.colorHex || '#4f46e5' }"></span>
            <h3 class="font-black text-slate-900 text-base leading-tight">{{ t.name }}</h3>
          </div>
          <span
            v-if="t.remainingQuantity === 0"
            class="shrink-0 px-2 py-0.5 rounded-full text-[10px] font-black bg-slate-200 text-slate-600 flex items-center gap-1"
          >
            <Ban class="w-3 h-3" /> Hết vé
          </span>
          <span
            v-else-if="t.remainingQuantity <= 20 || soldPercent(t) >= 85"
            class="shrink-0 px-2 py-0.5 rounded-full text-[10px] font-black bg-rose-100 text-rose-700 flex items-center gap-1 animate-pulse"
          >
            <Flame class="w-3 h-3" /> Sắp hết
          </span>
        </div>

        <ul class="space-y-1 mb-4">
          <li v-for="p in perks(t)" :key="p" class="flex items-start gap-1.5 text-xs text-slate-600">
            <Check class="w-3.5 h-3.5 text-emerald-500 shrink-0 mt-0.5" />
            <span>{{ p }}</span>
          </li>
        </ul>

        <!-- Availability bar -->
        <div class="mt-auto">
          <div class="flex items-center justify-between text-[11px] font-semibold mb-1">
            <span class="text-slate-500">Đã bán {{ soldPercent(t) }}%</span>
            <span :class="t.remainingQuantity <= 20 ? 'text-rose-600' : 'text-emerald-600'">
              Còn {{ t.remainingQuantity }} vé
            </span>
          </div>
          <div class="h-1.5 rounded-full bg-slate-100 overflow-hidden">
            <div
              class="h-full rounded-full transition-all"
              :style="{ width: `${soldPercent(t)}%`, backgroundColor: t.colorHex || '#4f46e5' }"
            ></div>
          </div>
        </div>
      </div>

      <!-- Price & stepper -->
      <div class="px-5 py-4 border-t border-dashed border-slate-200 bg-slate-50/60 flex items-center justify-between gap-3">
        <div>
          <div class="text-lg font-black text-rose-600 leading-none">{{ formatCurrency(t.price) }}</div>
          <div class="text-[10px] text-slate-400 font-semibold mt-1">Tối đa {{ t.maxPerOrder }} vé / đơn</div>
        </div>

        <div class="flex items-center gap-1.5 rounded-xl bg-white border border-slate-200 p-1 shadow-sm">
          <button
            type="button"
            :id="`ticket-minus-${t.id}`"
            @click="change(t, -1)"
            :disabled="qty(t) === 0"
            class="w-8 h-8 rounded-lg text-slate-700 flex items-center justify-center hover:bg-slate-100 disabled:opacity-30 transition"
            :aria-label="`Giảm số lượng ${t.name}`"
          >
            <Minus class="w-4 h-4" />
          </button>
          <span class="w-7 text-center text-sm font-black text-slate-900 tabular-nums">{{ qty(t) }}</span>
          <button
            type="button"
            :id="`ticket-plus-${t.id}`"
            @click="change(t, 1)"
            :disabled="t.remainingQuantity === 0 || qty(t) >= Math.min(t.maxPerOrder, t.remainingQuantity)"
            class="w-8 h-8 rounded-lg bg-indigo-600 text-white flex items-center justify-center hover:bg-indigo-700 disabled:opacity-30 disabled:hover:bg-indigo-600 transition"
            :aria-label="`Tăng số lượng ${t.name}`"
          >
            <Plus class="w-4 h-4" />
          </button>
        </div>
      </div>
    </article>
  </div>
</template>
