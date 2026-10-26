<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()
const isDashboard = computed(() => route.path.startsWith('/dashboard') || route.path.startsWith('/staff/scanner') || route.path.startsWith('/organizer/seat-map-builder'))
</script>

<template>
  <div class="min-h-screen flex flex-col bg-[#070A12] text-slate-100 selection:bg-blue-600 selection:text-white">
    <Navbar v-if="!isDashboard" />
    <main class="flex-1" :class="isDashboard ? 'bg-[#f8fafc]' : 'bg-[#070A12]'">
      <router-view />
    </main>
    <Footer v-if="!isDashboard" />
  </div>
</template>
