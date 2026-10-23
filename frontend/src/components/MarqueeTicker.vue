<script setup lang="ts">
import { Flame, Sparkles, Zap, Ticket } from 'lucide-vue-next'

const tickerItems = [
  { icon: Flame, text: 'Concert Skyline Symphony vừa bán hết 500 vé Early Bird!' },
  { icon: Zap, text: 'Flash Sale: Nhập mã EVENTHUB2026 giảm ngay 15% cho mọi vé sự kiện' },
  { icon: Sparkles, text: 'HCMC Midnight Marathon mở bán đợt BIB cuối cùng' },
  { icon: Ticket, text: 'Vietnam Tech Summit 2026: Chỉ còn 9 vé All-Access Diamond' },
  { icon: Flame, text: 'RAP VIET All-Star Concert bổ sung khu vực Fanzone SVIP' },
]
</script>

<template>
  <div class="bg-indigo-900 text-white py-1.5 overflow-hidden border-y border-indigo-800 shadow-inner">
    <div class="flex items-center gap-8 animate-marquee whitespace-nowrap">
      <div
        v-for="(item, idx) in [...tickerItems, ...tickerItems]"
        :key="idx"
        class="inline-flex items-center gap-2 text-xs font-semibold tracking-wide text-indigo-100"
      >
        <span class="w-1.5 h-1.5 rounded-full bg-pink-400 animate-ping"></span>
        <component :is="item.icon" class="w-3.5 h-3.5 text-amber-300" />
        <span>{{ item.text }}</span>
        <span class="text-indigo-400 mx-3">•</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes marquee {
  0% {
    transform: translateX(0%);
  }
  100% {
    transform: translateX(-50%);
  }
}

.animate-marquee {
  display: inline-flex;
  animation: marquee 35s linear infinite;
}

.animate-marquee:hover {
  animation-play-state: paused;
}
</style>
