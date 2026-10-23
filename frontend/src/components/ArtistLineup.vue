<script setup lang="ts">
import { ref } from 'vue'
import { Music, Check, Plus, Star } from 'lucide-vue-next'

interface Artist {
  id: number
  name: string
  role: string
  avatar: string
  eventsCount: number
  isFollowing?: boolean
}

const artists = ref<Artist[]>([
  {
    id: 1,
    name: 'Đen Vâu',
    role: 'Nghệ sĩ Rapper & Nhạc sĩ',
    avatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
    eventsCount: 3,
    isFollowing: false,
  },
  {
    id: 2,
    name: 'Mỹ Tâm',
    role: 'Ca sĩ & Nhạc sĩ Pop Ballad',
    avatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=200&q=80',
    eventsCount: 2,
    isFollowing: true,
  },
  {
    id: 3,
    name: 'TS. David Miller',
    role: 'AI Research Director (Google DeepMind)',
    avatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
    eventsCount: 1,
    isFollowing: false,
  },
  {
    id: 4,
    name: 'Vũ. (Hoàng Thái Vũ)',
    role: 'Hoàng tử Indie Việt',
    avatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
    eventsCount: 4,
    isFollowing: false,
  },
  {
    id: 5,
    name: 'Saigon Heat',
    role: 'CLB Bóng Rổ ĐKVĐ VBA',
    avatar: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?auto=format&fit=crop&w=200&q=80',
    eventsCount: 5,
    isFollowing: true,
  },
])

const toggleFollow = (art: Artist) => {
  art.isFollowing = !art.isFollowing
}
</script>

<template>
  <div class="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-sm">
    <div class="flex items-center justify-between mb-6 pb-3 border-b border-slate-100">
      <div>
        <h3 class="text-xl sm:text-2xl font-black text-slate-900">
          Nghệ Sĩ & Diễn Giả Tâm Điểm
        </h3>
        <p class="text-xs text-slate-500 mt-0.5">Theo dõi thần tượng để nhận thông báo sớm khi mở bán vé concert</p>
      </div>
    </div>

    <!-- Artist Circles Row -->
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
      <div
        v-for="art in artists"
        :key="art.id"
        class="p-4 rounded-2xl bg-slate-50 hover:bg-white border border-slate-200/80 hover:border-indigo-300 hover:shadow-lg transition-all flex flex-col items-center text-center group"
      >
        <div class="relative mb-3">
          <img
            :src="art.avatar"
            :alt="art.name"
            class="w-20 h-20 rounded-full object-cover border-2 border-indigo-600/30 group-hover:scale-105 group-hover:border-indigo-600 transition-all shadow-md"
          />
          <span class="absolute bottom-0 right-0 w-6 h-6 rounded-full bg-indigo-600 text-white flex items-center justify-center text-[10px] shadow">
            <Star class="w-3 h-3 fill-white" />
          </span>
        </div>

        <h4 class="font-black text-slate-900 text-sm group-hover:text-indigo-600 transition-colors">
          {{ art.name }}
        </h4>
        <p class="text-[11px] text-slate-500 line-clamp-1 mb-2">{{ art.role }}</p>

        <span class="text-[10px] font-bold text-indigo-700 bg-indigo-50 px-2 py-0.5 rounded-full mb-3">
          {{ art.eventsCount }} sự kiện sắp tới
        </span>

        <button
          @click="toggleFollow(art)"
          class="w-full py-1.5 px-3 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-1 shadow-sm"
          :class="art.isFollowing ? 'bg-slate-200 text-slate-700 hover:bg-slate-300' : 'bg-indigo-600 text-white hover:bg-indigo-700'"
        >
          <component :is="art.isFollowing ? Check : Plus" class="w-3.5 h-3.5" />
          <span>{{ art.isFollowing ? 'Đang theo dõi' : 'Theo dõi' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
