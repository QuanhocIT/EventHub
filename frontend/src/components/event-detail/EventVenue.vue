<script setup lang="ts">
import { computed } from 'vue'
import type { Event } from '@/types'
import type { EventDetailExtras } from '@/services/eventDetailData'
import { MapPin, Navigation, Car, Bus, DoorOpen, Users, Copy } from 'lucide-vue-next'

const props = defineProps<{
  event: Event
  venueInfo: EventDetailExtras['venueInfo']
}>()

const emit = defineEmits<{
  (e: 'notify', message: string, type?: 'error' | 'success' | 'info'): void
}>()

const query = computed(() => encodeURIComponent(`${props.event.locationName}, ${props.event.address}`))
const mapSrc = computed(() => `https://maps.google.com/maps?q=${query.value}&z=15&output=embed`)
const directionsUrl = computed(() => `https://www.google.com/maps/dir/?api=1&destination=${query.value}`)

const copyAddress = async () => {
  try {
    await navigator.clipboard.writeText(props.event.address)
    emit('notify', 'Đã sao chép địa chỉ', 'success')
  } catch {
    emit('notify', 'Không thể sao chép địa chỉ', 'error')
  }
}
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-5 gap-5">
    <div class="lg:col-span-3 rounded-2xl overflow-hidden border border-slate-200 bg-slate-100 min-h-[260px]">
      <iframe
        :src="mapSrc"
        :title="`Bản đồ ${event.locationName}`"
        class="w-full h-full min-h-[260px] border-0"
        loading="lazy"
        referrerpolicy="no-referrer-when-downgrade"
      ></iframe>
    </div>

    <div class="lg:col-span-2 flex flex-col gap-3">
      <div class="p-4 rounded-2xl bg-slate-50 border border-slate-100">
        <div class="flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-rose-100 flex items-center justify-center shrink-0">
            <MapPin class="w-4 h-4 text-rose-600" />
          </div>
          <div class="min-w-0">
            <h4 class="font-black text-slate-900 text-sm">{{ event.locationName }}</h4>
            <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">{{ event.address }}</p>
          </div>
        </div>
        <div class="flex gap-2 mt-3">
          <a
            id="venue-directions"
            :href="directionsUrl"
            target="_blank"
            rel="noopener"
            class="flex-1 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold flex items-center justify-center gap-1.5 transition"
          >
            <Navigation class="w-3.5 h-3.5" /> Chỉ đường
          </a>
          <button
            id="venue-copy"
            type="button"
            @click="copyAddress"
            class="px-3 py-2 rounded-xl bg-white border border-slate-200 hover:border-slate-400 text-slate-700 text-xs font-bold flex items-center gap-1.5 transition"
          >
            <Copy class="w-3.5 h-3.5" /> Sao chép
          </button>
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div class="p-3 rounded-2xl border border-slate-200 bg-white">
          <Users class="w-4 h-4 text-indigo-600 mb-1" />
          <div class="text-[10px] font-bold uppercase text-slate-400">Sức chứa</div>
          <div class="text-sm font-black text-slate-900">{{ venueInfo.capacity.toLocaleString('vi-VN') }} chỗ</div>
        </div>
        <div class="p-3 rounded-2xl border border-slate-200 bg-white">
          <DoorOpen class="w-4 h-4 text-emerald-600 mb-1" />
          <div class="text-[10px] font-bold uppercase text-slate-400">Giờ mở cổng</div>
          <div class="text-sm font-black text-slate-900">{{ venueInfo.gateOpen.replace('Mở cổng lúc ', '') }}</div>
        </div>
      </div>

      <div class="p-3.5 rounded-2xl border border-slate-200 bg-white flex gap-3">
        <Car class="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
        <p class="text-xs text-slate-600 leading-relaxed">{{ venueInfo.parking }}</p>
      </div>
      <div class="p-3.5 rounded-2xl border border-slate-200 bg-white flex gap-3">
        <Bus class="w-4 h-4 text-sky-600 shrink-0 mt-0.5" />
        <p class="text-xs text-slate-600 leading-relaxed">{{ venueInfo.transport }}</p>
      </div>
    </div>
  </div>
</template>
