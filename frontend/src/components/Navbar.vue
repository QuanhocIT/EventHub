<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useBookingStore } from '@/stores/booking'
import {
  Ticket,
  Compass,
  LogOut,
  UserPlus,
  Search,
  Menu,
  X,
  Clock,
  Sparkles,
  ChevronDown,
  LayoutDashboard,
  BarChart3,
  Calendar,
  Users,
  ShieldAlert,
  ShieldCheck,
  Music,
  Cpu,
  Trophy,
  Palette,
  SlidersHorizontal,
  ChevronRight,
  User as UserIcon,
  Film,
  Bell,
  Play,
  TicketPercent,
  Newspaper,
  Info,
  Building2,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const bookingStore = useBookingStore()

const isDashboardRoute = computed(() => {
  return route.path.startsWith('/dashboard') || route.path.startsWith('/admin') || route.path.startsWith('/staff')
})

const isActiveAdminTab = (tabName: string) => {
  return (
    route.path === '/dashboard' &&
    (route.query.tab === tabName || (!route.query.tab && tabName === 'overview'))
  )
}

const verticalDrawerOpen = ref(false)
const mobileMenuOpen = ref(false)
const userDropdownOpen = ref(false)
const searchKeyword = ref('')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/events', query: { keyword: searchKeyword.value.trim() } })
  } else {
    router.push('/events')
  }
}

const handleLogout = () => {
  authStore.logout()
  userDropdownOpen.value = false
  router.push('/')
}
</script>

<template>
  <header class="sticky top-0 z-50 bg-[#070A12]/95 backdrop-blur-md border-b border-[#1A2338] text-slate-200">
    <!-- Main Navigation Bar -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16 sm:h-20 gap-4">
        <!-- Left: Logo & Menu Dọc trigger -->
        <div class="flex items-center gap-3 shrink-0">
          <!-- Button Mở Menu Dọc -->
          <button
            type="button"
            @click="verticalDrawerOpen = true"
            class="flex items-center gap-1.5 px-2.5 py-1.5 rounded-xl bg-slate-900/80 hover:bg-slate-800 border border-slate-800 text-slate-300 hover:text-blue-400 transition cursor-pointer text-xs font-bold"
            title="Mở menu dọc"
          >
            <SlidersHorizontal class="w-3.5 h-3.5 text-blue-500" />
            <span class="hidden sm:inline">Menu</span>
          </button>

          <!-- CineVerse / EventHub Logo -->
          <router-link to="/" class="flex items-center gap-2.5 group">
            <div class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center shadow-lg shadow-blue-500/25 group-hover:scale-105 transition-transform">
              <Play class="w-5 h-5 text-white fill-white ml-0.5" />
            </div>
            <div class="flex flex-col">
              <span class="text-lg sm:text-xl font-black tracking-tight text-white leading-tight">
                Cine<span class="text-blue-500">Verse</span>
              </span>
              <span class="text-[9px] sm:text-[10px] font-bold text-slate-400 tracking-wider uppercase">
                Movies • Events • More
              </span>
            </div>
          </router-link>
        </div>

        <!-- Center: Navigation Links matching Image 2 -->
        <nav class="hidden xl:flex items-center gap-1.5 text-sm font-semibold">
          <router-link
            to="/"
            class="px-3 py-1.5 rounded-lg transition-colors"
            :class="route.path === '/' ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Trang chủ
          </router-link>
          <router-link
            to="/cinema"
            class="px-3 py-1.5 rounded-lg transition-colors flex items-center gap-1.5"
            :class="route.path === '/cinema' ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Phim
          </router-link>
          <router-link
            to="/events"
            class="px-3 py-1.5 rounded-lg transition-colors"
            :class="route.path.startsWith('/events') ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Sự kiện
          </router-link>
          <router-link
            to="/cinemas"
            class="px-3 py-1.5 rounded-lg transition-colors"
            :class="route.path.startsWith('/cinemas') || route.path.startsWith('/theaters') ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Rạp chiếu
          </router-link>
          <router-link
            to="/promotions"
            class="px-3 py-1.5 rounded-lg transition-colors"
            :class="route.path.startsWith('/promotions') ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Khuyến mãi
          </router-link>
          <router-link
            to="/about"
            class="px-3 py-1.5 rounded-lg transition-colors"
            :class="route.path.startsWith('/about') ? 'text-blue-400 font-bold border-b-2 border-blue-500 pb-0.5' : 'text-slate-300 hover:text-white'"
          >
            Về chúng tôi
          </router-link>
          <router-link
            v-if="authStore.isAuthenticated && (authStore.isAdmin || authStore.isOrganizer || authStore.isStaff)"
            to="/dashboard"
            class="px-3.5 py-2 rounded-lg text-amber-400 hover:text-amber-300 hover:bg-amber-500/10 transition-colors font-bold flex items-center gap-1"
          >
            <LayoutDashboard class="w-4 h-4" />
            <span>Quản trị</span>
          </router-link>
        </nav>

        <!-- Right: Search, Notifications & Auth Buttons -->
        <div class="flex items-center gap-2.5 sm:gap-3 shrink-0">
          <!-- Search Pill matching Image 2 -->
          <div class="hidden md:flex items-center">
            <form
              @submit.prevent="handleSearch"
              class="flex items-center w-56 lg:w-64 rounded-full bg-[#111827] border border-[#232F47] focus-within:border-blue-500 px-3 py-1.5 transition-all text-xs"
            >
              <Search class="w-3.5 h-3.5 text-slate-400 shrink-0 mr-2" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="Tìm phim, sự kiện, nghệ sĩ..."
                class="w-full bg-transparent text-white placeholder-slate-500 focus:outline-none text-xs"
              />
            </form>
          </div>

          <!-- Notification Bell -->
          <button
            type="button"
            class="p-2 rounded-full bg-slate-900 border border-slate-800 text-slate-400 hover:text-white hover:border-slate-700 transition relative"
            title="Thông báo"
          >
            <Bell class="w-4 h-4" />
            <span class="absolute top-1 right-1 w-2 h-2 rounded-full bg-blue-500"></span>
          </button>

          <!-- User Authenticated Profile Dropdown -->
          <template v-if="authStore.isAuthenticated && authStore.user">
            <div class="relative">
              <button
                @click="userDropdownOpen = !userDropdownOpen"
                class="flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 hover:border-slate-700 text-xs font-bold text-white transition"
              >
                <div class="w-6 h-6 rounded-full bg-blue-600 text-white font-bold flex items-center justify-center text-[10px]">
                  {{ authStore.user.fullName.charAt(0) }}
                </div>
                <span class="hidden sm:inline max-w-[100px] truncate">{{ authStore.user.fullName }}</span>
                <ChevronDown class="w-3.5 h-3.5 text-slate-400" />
              </button>

              <div
                v-if="userDropdownOpen"
                class="absolute right-0 mt-2 w-56 rounded-2xl bg-[#0D121F] border border-[#1C2438] shadow-2xl py-2 z-50 text-xs text-slate-200"
              >
                <div class="px-4 py-2.5 border-b border-slate-800">
                  <p class="text-[10px] text-slate-400">Đã đăng nhập</p>
                  <p class="font-bold text-white truncate">{{ authStore.user.fullName }}</p>
                  <p class="text-[10px] text-slate-400 truncate">{{ authStore.user.email }}</p>
                </div>
                <router-link
                  to="/profile"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2 px-4 py-2 hover:bg-slate-800 text-slate-200"
                >
                  <UserIcon class="w-4 h-4 text-blue-400" />
                  <span>Hồ sơ cá nhân</span>
                </router-link>
                <router-link
                  to="/my-tickets"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2 px-4 py-2 hover:bg-slate-800 text-slate-200"
                >
                  <Ticket class="w-4 h-4 text-blue-400" />
                  <span>Vé của tôi</span>
                </router-link>
                <router-link
                  v-if="authStore.isAdmin || authStore.isOrganizer || authStore.isStaff"
                  to="/dashboard"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2 px-4 py-2 hover:bg-slate-800 text-amber-300 font-bold"
                >
                  <LayoutDashboard class="w-4 h-4 text-amber-400" />
                  <span>Trung tâm quản trị</span>
                </router-link>
                <button
                  type="button"
                  @click="handleLogout"
                  class="w-full flex items-center gap-2 px-4 py-2 text-rose-400 hover:bg-rose-500/10 text-left font-bold transition cursor-pointer"
                >
                  <LogOut class="w-4 h-4" />
                  <span>Đăng xuất</span>
                </button>
              </div>
            </div>
          </template>

          <!-- Guest Auth Buttons -->
          <template v-else>
            <div class="flex items-center gap-2">
              <router-link
                to="/login"
                class="px-3.5 py-1.5 rounded-lg text-xs font-bold text-slate-300 hover:text-white transition"
              >
                Đăng nhập
              </router-link>
              <router-link
                to="/register"
                class="px-4 py-2 rounded-lg text-xs font-bold text-white bg-blue-600 hover:bg-blue-500 shadow-md shadow-blue-600/30 transition cursor-pointer"
              >
                Đăng ký
              </router-link>
            </div>
          </template>

          <!-- Mobile Menu Trigger -->
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="xl:hidden p-2 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
          >
            <Menu v-if="!mobileMenuOpen" class="w-5 h-5" />
            <X v-else class="w-5 h-5" />
          </button>
        </div>
      </div>

      <!-- Mobile Dropdown -->
      <div v-if="mobileMenuOpen" class="xl:hidden py-4 border-t border-slate-800 space-y-2">
        <form @submit.prevent="handleSearch" class="relative w-full mb-3">
          <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="Tìm kiếm phim, sự kiện..."
            class="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-900 border border-slate-800 text-xs text-white"
          />
        </form>
        <router-link
          to="/"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Trang chủ
        </router-link>
        <router-link
          to="/cinema"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-blue-400 font-bold text-xs"
        >
          Phim chiếu rạp
        </router-link>
        <router-link
          to="/cinemas"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Rạp chiếu & Cụm rạp
        </router-link>
        <router-link
          to="/events"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Sự kiện nổi bật
        </router-link>
        <router-link
          to="/promotions"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Ưu đãi & Khuyến mãi
        </router-link>
        <router-link
          to="/news"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Tin tức điện ảnh
        </router-link>
        <router-link
          to="/about"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Về chúng tôi
        </router-link>
        <router-link
          v-if="authStore.isAuthenticated"
          to="/my-tickets"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-300 hover:text-white font-bold text-xs"
        >
          Vé của tôi
        </router-link>
        <router-link
          v-if="authStore.isAuthenticated && (authStore.isAdmin || authStore.isOrganizer || authStore.isStaff)"
          to="/dashboard"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-amber-400 font-bold text-xs"
        >
          Quản trị hệ thống
        </router-link>
      </div>
    </div>

    <!-- Slide-over Vertical Drawer (Accessible from "Menu" button) -->
    <Teleport to="body">
      <div v-if="verticalDrawerOpen" class="fixed inset-0 z-50 overflow-hidden">
        <div
          class="fixed inset-0 bg-black/80 backdrop-blur-sm transition-opacity"
          @click="verticalDrawerOpen = false"
        ></div>

        <aside class="fixed inset-y-0 left-0 max-w-xs w-full bg-[#0B0F17] border-r border-[#1C2438] shadow-2xl flex flex-col justify-between overflow-y-auto z-50">
          <div class="p-5 border-b border-slate-800 flex items-center justify-between">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-xl bg-blue-600 flex items-center justify-center text-white">
                <Play class="w-4 h-4 fill-white ml-0.5" />
              </div>
              <div>
                <span class="text-sm font-black text-white">Cine<span class="text-blue-500">Verse</span></span>
                <p class="text-[9px] text-slate-400 uppercase">Hệ Thống Đặt Vé 2026</p>
              </div>
            </div>
            <button
              type="button"
              @click="verticalDrawerOpen = false"
              class="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition"
            >
              <X class="w-4 h-4" />
            </button>
          </div>

          <div class="flex-1 p-4 space-y-5 overflow-y-auto text-xs">
            <!-- Nav Links -->
            <div class="space-y-1">
              <p class="text-[10px] font-black uppercase tracking-wider text-slate-500 px-2 mb-1">Khám Phá</p>
              <router-link
                to="/cinema"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-blue-400 bg-blue-500/10 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Film class="w-4 h-4" />
                  <span>Phim Chiếu Rạp (Cinema)</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[9px] bg-blue-600 text-white font-black">HOT</span>
              </router-link>
              <router-link
                to="/cinemas"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Building2 class="w-4 h-4 text-cyan-400" />
                  <span>Cụm Rạp Chiếu Toàn Quốc</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[9px] bg-cyan-500/20 text-cyan-300 font-black">MỚI</span>
              </router-link>
              <router-link
                to="/events"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Compass class="w-4 h-4 text-blue-400" />
                  <span>Sự Kiện & Concerts</span>
                </div>
              </router-link>
              <router-link
                to="/promotions"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <TicketPercent class="w-4 h-4 text-amber-400" />
                  <span>Ưu Đãi & Khuyến Mãi</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[9px] bg-amber-500/20 text-amber-300 font-black">GIẢM 50%</span>
              </router-link>
              <router-link
                to="/news"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Newspaper class="w-4 h-4 text-emerald-400" />
                  <span>Tin Tức & Sự Kiện</span>
                </div>
              </router-link>
              <router-link
                to="/about"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Info class="w-4 h-4 text-purple-400" />
                  <span>Về Chúng Tôi</span>
                </div>
              </router-link>
              <router-link
                v-if="authStore.isAuthenticated"
                to="/my-tickets"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Ticket class="w-4 h-4 text-indigo-400" />
                  <span>Vé điện tử của tôi</span>
                </div>
              </router-link>
            </div>

            <!-- Admin Links if applicable -->
            <div v-if="authStore.isAuthenticated && (authStore.isAdmin || authStore.isOrganizer || authStore.isStaff)" class="space-y-1">
              <p class="text-[10px] font-black uppercase tracking-wider text-amber-400 px-2 mb-1">Quản Trị</p>
              <router-link
                to="/dashboard?tab=overview"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <LayoutDashboard class="w-4 h-4 text-amber-400" />
                  <span>Tổng quan & KPI</span>
                </div>
              </router-link>
              <router-link
                to="/dashboard?tab=staff_scanner"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-slate-300 hover:bg-slate-900 font-bold"
              >
                <div class="flex items-center gap-2">
                  <Ticket class="w-4 h-4 text-emerald-400" />
                  <span>Trạm soát vé cổng</span>
                </div>
              </router-link>
            </div>
          </div>

          <div class="p-4 border-t border-slate-800">
            <template v-if="authStore.isAuthenticated">
              <button
                type="button"
                @click="handleLogout(); verticalDrawerOpen = false"
                class="w-full py-2 rounded-xl text-rose-400 hover:bg-rose-500/10 border border-rose-500/30 text-xs font-bold transition flex items-center justify-center gap-2"
              >
                <LogOut class="w-4 h-4" />
                <span>Đăng xuất</span>
              </button>
            </template>
            <template v-else>
              <div class="grid grid-cols-2 gap-2">
                <router-link
                  to="/login"
                  @click="verticalDrawerOpen = false"
                  class="py-2 text-center rounded-xl bg-slate-900 text-slate-300 text-xs font-bold"
                >
                  Đăng nhập
                </router-link>
                <router-link
                  to="/register"
                  @click="verticalDrawerOpen = false"
                  class="py-2 text-center rounded-xl bg-blue-600 text-white text-xs font-bold"
                >
                  Đăng ký
                </router-link>
              </div>
            </template>
          </div>
        </aside>
      </div>
    </Teleport>
  </header>
</template>
