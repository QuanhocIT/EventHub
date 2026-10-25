<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
import Footer from '@/components/Footer.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col bg-[#070A12] text-slate-100 selection:bg-blue-600 selection:text-white">
    <Navbar />
    <main class="flex-1 bg-[#070A12]">
      <router-view />
    </main>
    <Footer />
  </div>
</template>
