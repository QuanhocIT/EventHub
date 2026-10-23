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
  MapPin,
  PhoneCall,
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
  CreditCard,
  FileSpreadsheet,
  User as UserIcon,
  Film,
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
const selectedLocation = ref('TP. Hồ Chí Minh')

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
  <header class="sticky top-0 z-50 bg-white shadow-sm border-b border-slate-200">
    <!-- Top Micro Announcement Bar (Only shown on public consumer routes) -->
    <div
      v-if="!isDashboardRoute"
      class="bg-gradient-to-r from-indigo-700 via-purple-700 to-indigo-800 text-white text-[11px] sm:text-xs py-1 px-4"
    >
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-2">
          <span class="bg-white/20 px-2 py-0.5 rounded font-bold uppercase text-[10px]">Ưu đãi số lượng có hạn</span>
          <span class="hidden sm:inline">Nhập mã <strong>EVENTHUB2026</strong> giảm ngay 15% cho mọi vé sự kiện!</span>
          <span class="sm:hidden font-bold">Mã EVENTHUB2026 giảm 15%</span>
        </div>

        <div class="flex items-center gap-4 text-white/90">
          <div class="hidden md:flex items-center gap-1">
            <PhoneCall class="w-3 h-3" />
            <span>Hotline: 1900 6868 (8:00 - 22:00)</span>
          </div>
          <!-- Location selector -->
          <div class="flex items-center gap-1 font-semibold cursor-pointer hover:text-white">
            <MapPin class="w-3 h-3 text-pink-300" />
            <select
              v-model="selectedLocation"
              class="bg-transparent text-white text-[11px] font-semibold focus:outline-none cursor-pointer"
            >
              <option value="Toàn quốc" class="text-slate-900">Toàn quốc</option>
              <option value="TP. Hồ Chí Minh" class="text-slate-900">TP. Hồ Chí Minh</option>
              <option value="Hà Nội" class="text-slate-900">Hà Nội</option>
              <option value="Đà Nẵng" class="text-slate-900">Đà Nẵng</option>
            </select>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Navigation Bar -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Left Section: Vertical Menu Trigger & Logo -->
        <div class="flex items-center gap-2 sm:gap-3 shrink-0">
          <!-- Button Mở Menu Dọc (Áp dụng chung mọi trang) -->
          <button
            type="button"
            @click="verticalDrawerOpen = true"
            class="flex items-center gap-2 px-2.5 sm:px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-200 text-slate-700 hover:text-indigo-600 transition cursor-pointer text-xs font-bold shadow-2xs group"
            title="Mở menu dọc điều hướng hệ thống"
          >
            <SlidersHorizontal class="w-4 h-4 text-indigo-600 group-hover:rotate-90 transition-transform duration-300" />
            <span class="hidden sm:inline">Menu Dọc</span>
            <span v-if="authStore.isAdmin || authStore.isOrganizer" class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
          </button>

          <!-- Logo -->
          <router-link to="/" class="flex items-center gap-2.5 shrink-0 group">
            <div
              class="w-9 h-9 sm:w-10 sm:h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center shadow-md shadow-indigo-600/20 group-hover:scale-105 transition-transform"
            >
              <Ticket class="w-5 h-5 text-white transform -rotate-12 group-hover:rotate-0 transition-transform" />
            </div>
            <div>
              <span class="text-xl sm:text-2xl font-black tracking-tight text-slate-900">Event<span class="text-indigo-600">Hub</span></span>
              <span
                v-if="!isDashboardRoute"
                class="hidden lg:inline-block ml-1.5 text-[10px] font-bold tracking-wider uppercase px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200"
              >
                Ticketing Platform
              </span>
              <span
                v-else
                class="hidden lg:inline-block ml-1.5 text-[10px] font-black tracking-wider uppercase px-2 py-0.5 rounded bg-purple-50 text-purple-700 border border-purple-200"
              >
                Studio & Management
              </span>
            </div>
          </router-link>
        </div>

        <!-- Center: Search Bar with Location (Public Storefront Mode) -->
        <div v-if="!isDashboardRoute" class="hidden md:flex items-center flex-1 max-w-xl mx-8">
          <form
            @submit.prevent="handleSearch"
            class="flex items-center w-full rounded-full bg-slate-100 hover:bg-slate-200/80 focus-within:bg-white border border-slate-300 focus-within:border-indigo-600 focus-within:ring-2 focus-within:ring-indigo-100 transition-all p-1"
          >
            <div class="flex items-center flex-1 pl-3.5 gap-2">
              <Search class="w-4 h-4 text-slate-400 shrink-0" />
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="Tìm ca sĩ, ban nhạc, tên giải chạy, địa điểm..."
                class="w-full bg-transparent text-sm text-slate-800 placeholder-slate-400 focus:outline-none"
              />
            </div>
            <button
              type="submit"
              class="px-4 py-1.5 rounded-full bg-indigo-600 text-white font-bold text-xs hover:bg-indigo-700 transition-colors shrink-0"
            >
              Tìm kiếm
            </button>
          </form>
        </div>

        <!-- Center: Studio Breadcrumb / Return Link (Dashboard Mode) -->
        <div v-else class="hidden md:flex items-center gap-3">
          <router-link
            to="/events"
            class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-slate-100 hover:bg-indigo-50 border border-slate-200 hover:border-indigo-200 text-slate-600 hover:text-indigo-600 text-xs font-bold transition shadow-2xs group"
          >
            <Compass class="w-3.5 h-3.5 text-indigo-600 group-hover:rotate-45 transition-transform" />
            <span>← Khám Phá Sự Kiện</span>
          </router-link>
          <span class="text-xs text-slate-400">&bull;</span>
          <span class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500">
            <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Hệ Thống Trực Tuyến 2026
          </span>
        </div>

        <!-- Links & User Action -->
        <div class="flex items-center gap-3">
          <!-- Quick Nav (Only on Storefront) -->
          <nav v-if="!isDashboardRoute" class="hidden lg:flex items-center gap-1 mr-2 text-sm font-semibold">
            <router-link
              to="/events"
              class="px-3 py-2 rounded-lg text-slate-700 hover:text-indigo-600 hover:bg-slate-50 transition-colors"
            >
              Khám phá sự kiện
            </router-link>
            <router-link
              to="/cinema"
              class="px-3 py-2 rounded-lg text-slate-700 hover:text-rose-600 hover:bg-rose-50/50 transition-colors flex items-center gap-1.5 font-bold"
            >
              <Film class="w-4 h-4 text-rose-500" />
              <span>Rạp Phim</span>
              <span class="px-1.5 py-0.2 rounded text-[10px] font-black uppercase bg-rose-100 text-rose-700 border border-rose-200">Hot</span>
            </router-link>
            <router-link
              to="/my-tickets"
              class="px-3 py-2 rounded-lg text-slate-700 hover:text-indigo-600 hover:bg-slate-50 transition-colors flex items-center gap-1.5"
            >
              <Ticket class="w-4 h-4 text-indigo-600" />
              Vé của tôi
            </router-link>
            <router-link
              v-if="authStore.isAuthenticated && (authStore.isAdmin || authStore.isOrganizer || authStore.isStaff)"
              to="/dashboard"
              class="px-3 py-2 rounded-lg text-slate-700 hover:text-indigo-600 hover:bg-slate-50 transition-colors flex items-center gap-1.5"
            >
              <LayoutDashboard class="w-4 h-4 text-indigo-600" />
              Quản trị & Báo cáo
            </router-link>
          </nav>


          <!-- Hold Timer Badge -->
          <div
            v-if="bookingStore.totalTicketCount > 0 && bookingStore.currentEvent"
            class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-amber-50 border border-amber-300 text-amber-800 text-xs font-bold animate-pulse"
          >
            <Clock class="w-3.5 h-3.5 text-amber-600" />
            <span>Giữ {{ bookingStore.totalTicketCount }} vé</span>
          </div>

          <!-- Auth States -->
          <template v-if="authStore.isAuthenticated && authStore.user">
            <div class="relative">
              <button
                @click="userDropdownOpen = !userDropdownOpen"
                class="flex items-center gap-2 px-3 py-1.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-sm font-semibold text-slate-800 transition-colors"
              >
                <div class="w-7 h-7 rounded-lg bg-indigo-100 text-indigo-700 font-bold flex items-center justify-center text-xs">
                  {{ authStore.user.fullName.charAt(0) }}
                </div>
                <span class="hidden sm:inline max-w-[120px] truncate">{{ authStore.user.fullName }}</span>
                <ChevronDown class="w-3.5 h-3.5 text-slate-400" />
              </button>

              <div
                v-if="userDropdownOpen"
                class="absolute right-0 mt-2 w-56 rounded-2xl bg-white border border-slate-200 shadow-xl py-2 z-50 text-sm"
              >
                <div class="px-4 py-2.5 border-b border-slate-100">
                  <p class="text-xs text-slate-500">Đăng nhập tài khoản</p>
                  <p class="font-bold text-slate-900 truncate">{{ authStore.user.fullName }}</p>
                  <p class="text-xs text-slate-500 truncate">{{ authStore.user.email }}</p>
                  <span class="inline-block mt-1 text-[10px] font-bold px-1.5 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 uppercase">
                    {{ authStore.user.role }}
                  </span>
                </div>
                <router-link
                  to="/profile"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2.5 px-4 py-2.5 text-slate-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors font-medium"
                >
                  <UserIcon class="w-4 h-4 text-indigo-600" />
                  Hồ sơ cá nhân & Đổi MK
                </router-link>
                <router-link
                  to="/my-tickets"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2.5 px-4 py-2.5 text-slate-700 hover:bg-indigo-50 hover:text-indigo-600 transition-colors"
                >
                  <Ticket class="w-4 h-4 text-indigo-600" />
                  Vé điện tử của tôi
                </router-link>
                <router-link
                  v-if="authStore.isStaff"
                  to="/dashboard?tab=staff_scanner"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2.5 px-4 py-2.5 text-emerald-700 hover:bg-emerald-50 transition-colors font-semibold"
                >
                  <QrCode class="w-4 h-4 text-emerald-600" />
                  Trạm Soát Vé Cổng (Staff)
                </router-link>
                <router-link
                  v-else-if="authStore.isOrganizer"
                  to="/dashboard?tab=org_overview"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2.5 px-4 py-2.5 text-purple-700 hover:bg-purple-50 transition-colors font-semibold"
                >
                  <Building2 class="w-4 h-4 text-purple-600" />
                  Quản Lý Sự Kiện (Organizer)
                </router-link>
                <router-link
                  v-else-if="authStore.isAdmin"
                  to="/dashboard?tab=overview"
                  @click="userDropdownOpen = false"
                  class="flex items-center gap-2.5 px-4 py-2.5 text-indigo-700 hover:bg-indigo-50 transition-colors font-semibold"
                >
                  <LayoutDashboard class="w-4 h-4 text-indigo-600" />
                  Trung Tâm Quản Trị (Admin)
                </router-link>

                <button
                  @click="handleLogout"
                  class="w-full flex items-center gap-2.5 px-4 py-2.5 text-rose-600 hover:bg-rose-50 transition-colors text-left font-medium"
                >
                  <LogOut class="w-4 h-4" />
                  Đăng xuất
                </button>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="flex items-center gap-2">
              <router-link
                to="/login"
                class="px-3.5 py-2 rounded-xl text-sm font-semibold text-slate-700 hover:text-indigo-600 hover:bg-slate-100 transition-colors"
              >
                Đăng nhập
              </router-link>
              <router-link
                to="/register"
                class="px-4 py-2 rounded-xl text-sm font-bold text-white bg-indigo-600 hover:bg-indigo-700 shadow-sm transition-all"
              >
                Đăng ký
              </router-link>
            </div>
          </template>

          <!-- Mobile Toggle -->
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="lg:hidden p-2 rounded-lg text-slate-600 hover:bg-slate-100"
          >
            <Menu v-if="!mobileMenuOpen" class="w-6 h-6" />
            <X v-else class="w-6 h-6" />
          </button>
        </div>
      </div>

      <!-- Mobile Dropdown -->
      <div v-if="mobileMenuOpen" class="lg:hidden py-4 border-t border-slate-200 space-y-3">
        <form @submit.prevent="handleSearch" class="relative w-full">
          <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="Tìm kiếm sự kiện..."
            class="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-100 border border-slate-300 text-sm text-slate-800"
          />
        </form>
        <router-link
          to="/events"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-700 font-semibold text-sm hover:bg-slate-100"
        >
          Khám phá sự kiện
        </router-link>
        <router-link
          to="/cinema"
          @click="mobileMenuOpen = false"
          class="flex items-center justify-between px-3 py-2 rounded-lg text-rose-600 font-bold text-sm bg-rose-50/50 hover:bg-rose-100/60"
        >
          <span class="flex items-center gap-2">
            <Film class="w-4 h-4 text-rose-500" />
            <span>Rạp Chiếu Phim (Cinema)</span>
          </span>
          <span class="px-1.5 py-0.5 rounded text-[10px] font-black uppercase bg-rose-200 text-rose-800">Mới</span>
        </router-link>
        <router-link
          to="/my-tickets"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-slate-700 font-semibold text-sm hover:bg-slate-100"
        >
          Vé của tôi
        </router-link>
        <router-link
          to="/dashboard"
          @click="mobileMenuOpen = false"
          class="block px-3 py-2 rounded-lg text-indigo-700 font-bold text-sm bg-indigo-50/70"
        >
          Quản trị & Báo cáo
        </router-link>
      </div>
    </div>

    <!-- SLIDE-OVER VERTICAL DRAWER (MENU DỌC ÁP DỤNG CHUNG CHO TẤT CẢ CÁC TRANG) -->
    <Teleport to="body">
      <div v-if="verticalDrawerOpen" class="fixed inset-0 z-50 overflow-hidden">
        <!-- Backdrop Blur -->
        <div
          class="fixed inset-0 bg-slate-900/60 backdrop-blur-xs transition-opacity duration-300"
          @click="verticalDrawerOpen = false"
        ></div>

        <!-- Vertical Sidebar Panel -->
        <aside
          class="fixed inset-y-0 left-0 max-w-xs w-full bg-white shadow-2xl flex flex-col justify-between overflow-y-auto z-50 transform transition-transform duration-300"
        >
          <!-- Top Header of Vertical Menu -->
          <div class="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/70">
            <div class="flex items-center gap-2.5">
              <div class="w-8 h-8 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-white shadow-md shadow-indigo-600/20">
                <Ticket class="w-4 h-4 transform -rotate-12" />
              </div>
              <div>
                <span class="text-base font-black text-slate-900">Event<span class="text-indigo-600">Hub</span></span>
                <p class="text-[10px] font-bold text-slate-400 uppercase tracking-wider">Menu Dọc Hệ Thống</p>
              </div>
            </div>

            <button
              type="button"
              @click="verticalDrawerOpen = false"
              class="p-2 rounded-xl text-slate-400 hover:text-slate-700 hover:bg-slate-200/60 transition cursor-pointer"
              title="Đóng menu dọc"
            >
              <X class="w-5 h-5" />
            </button>
          </div>

          <!-- Vertical Content Body -->
          <div class="flex-1 p-4 space-y-6 overflow-y-auto">
            <!-- User Status Card if Logged In -->
            <div
              v-if="authStore.isAuthenticated && authStore.user"
              class="p-3.5 rounded-2xl bg-gradient-to-br from-indigo-50/80 via-white to-purple-50/50 border border-indigo-100 shadow-2xs"
            >
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-xl bg-indigo-600 text-white font-black text-sm flex items-center justify-center shadow-md shadow-indigo-600/20">
                  {{ authStore.user.fullName.charAt(0) }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="font-extrabold text-xs text-slate-900 truncate">{{ authStore.user.fullName }}</p>
                  <p class="text-[11px] text-slate-500 truncate">{{ authStore.user.email }}</p>
                  <span class="inline-flex items-center gap-1 mt-1 text-[10px] font-extrabold px-2 py-0.5 rounded-md bg-indigo-100/80 text-indigo-700 uppercase">
                    <ShieldCheck class="w-3 h-3 text-indigo-600" />
                    {{ authStore.user.role }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Group 1: ADMIN & BÁO CÁO (Khi đăng nhập quyền Admin/Organizer/Staff) -->
            <div v-if="authStore.isAuthenticated && (authStore.isAdmin || authStore.isOrganizer || authStore.isStaff)" class="space-y-1.5">
              <div class="flex items-center justify-between px-2 mb-2">
                <p class="text-[11px] font-black uppercase tracking-wider text-indigo-600 flex items-center gap-1.5">
                  <ShieldCheck class="w-3.5 h-3.5" />
                  <span>Quản Trị & Điều Hành</span>
                </p>
                <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              </div>

              <router-link
                to="/dashboard?tab=overview"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('overview') ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-indigo-600'"
              >
                <div class="flex items-center gap-2.5">
                  <LayoutDashboard class="w-4 h-4" :class="isActiveAdminTab('overview') ? 'text-white' : 'text-indigo-600'" />
                  <span>Tổng Quan KPI & Doanh Thu</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-60 group-hover:translate-x-0.5 transition-transform" />
              </router-link>

              <router-link
                to="/dashboard?tab=analytics"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('analytics') ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-indigo-600'"
              >
                <div class="flex items-center gap-2.5">
                  <BarChart3 class="w-4 h-4" :class="isActiveAdminTab('analytics') ? 'text-white' : 'text-purple-600'" />
                  <span>Phân Tích Chuyên Sâu</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-extrabold uppercase" :class="isActiveAdminTab('analytics') ? 'bg-white/20 text-white' : 'bg-indigo-100 text-indigo-700'">Mới</span>
              </router-link>

              <router-link
                to="/dashboard?tab=events"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('events') ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-indigo-600'"
              >
                <div class="flex items-center gap-2.5">
                  <Calendar class="w-4 h-4" :class="isActiveAdminTab('events') ? 'text-white' : 'text-emerald-600'" />
                  <span>Quản Lý Sự Kiện</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-60 group-hover:translate-x-0.5 transition-transform" />
              </router-link>

              <router-link
                v-if="authStore.isAdmin"
                to="/dashboard?tab=users"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('users') ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-indigo-600'"
              >
                <div class="flex items-center gap-2.5">
                  <Users class="w-4 h-4" :class="isActiveAdminTab('users') ? 'text-white' : 'text-blue-600'" />
                  <span>Quản Trị Người Dùng</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-60 group-hover:translate-x-0.5 transition-transform" />
              </router-link>

              <router-link
                v-if="authStore.isAdmin"
                to="/dashboard?tab=audit"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('audit') ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-indigo-600'"
              >
                <div class="flex items-center gap-2.5">
                  <ShieldAlert class="w-4 h-4" :class="isActiveAdminTab('audit') ? 'text-white' : 'text-rose-600'" />
                  <span>Nhật Ký Kiểm Toán (Audit)</span>
                </div>
                <span class="w-2 h-2 rounded-full" :class="isActiveAdminTab('audit') ? 'bg-emerald-300' : 'bg-emerald-500'"></span>
              </router-link>

              <router-link
                to="/dashboard?tab=staff_scanner"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition group"
                :class="isActiveAdminTab('staff_scanner') ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/20' : 'text-slate-700 hover:bg-slate-100 hover:text-emerald-700'"
              >
                <div class="flex items-center gap-2.5">
                  <Ticket class="w-4 h-4" :class="isActiveAdminTab('staff_scanner') ? 'text-white' : 'text-emerald-600'" />
                  <span>Trạm Soát Vé Tại Cổng (Live)</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-extrabold uppercase bg-emerald-100 text-emerald-800">Cổng</span>
              </router-link>
            </div>

            <!-- Group 2: KHÁM PHÁ & SỰ KIỆN -->
            <div class="space-y-1.5">
              <p class="text-[11px] font-black uppercase tracking-wider text-slate-400 px-2 mb-2 flex items-center gap-1.5">
                <Compass class="w-3.5 h-3.5 text-indigo-500" />
                <span>Khám Phá Danh Mục</span>
              </p>

              <router-link
                to="/cinema"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-black text-rose-700 bg-rose-50/80 hover:bg-rose-100 border border-rose-200/80 transition group"
              >
                <div class="flex items-center gap-2.5">
                  <Film class="w-4 h-4 text-rose-600" />
                  <span>Rạp Phim & Bom Tấn Chiếu Rạp</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-black uppercase bg-rose-600 text-white">Hot</span>
              </router-link>

              <router-link
                to="/events"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Compass class="w-4 h-4 text-indigo-600" />
                  <span>Tất cả sự kiện</span>
                </div>
                <span class="text-[10px] text-slate-400">Xem hết</span>
              </router-link>

              <router-link
                to="/events?categoryId=1"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Music class="w-4 h-4 text-pink-500" />
                  <span>Âm Nhạc & Concert</span>
                </div>
                <span class="text-[10px] text-pink-600 font-bold bg-pink-50 px-1.5 py-0.5 rounded">Hot</span>
              </router-link>

              <router-link
                to="/events?categoryId=2"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Cpu class="w-4 h-4 text-indigo-500" />
                  <span>Công Nghệ & Hội Thảo</span>
                </div>
              </router-link>

              <router-link
                to="/events?categoryId=3"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Trophy class="w-4 h-4 text-amber-500" />
                  <span>Thể Thao & Giải Đấu</span>
                </div>
              </router-link>

              <router-link
                to="/events?categoryId=4"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Palette class="w-4 h-4 text-purple-500" />
                  <span>Sân Khấu & Triển Lãm</span>
                </div>
              </router-link>
            </div>

            <!-- Group 3: VÉ CỦA TÔI & TIỆN ÍCH -->
            <div class="space-y-1.5">
              <p class="text-[11px] font-black uppercase tracking-wider text-slate-400 px-2 mb-2 flex items-center gap-1.5">
                <Ticket class="w-3.5 h-3.5 text-indigo-500" />
                <span>Vé & Cá Nhân</span>
              </p>

              <router-link
                v-if="authStore.isAuthenticated"
                to="/profile"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <UserIcon class="w-4 h-4 text-indigo-600" />
                  <span>Hồ sơ & Đổi mật khẩu</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-40" />
              </router-link>

              <router-link
                to="/my-tickets"
                @click="verticalDrawerOpen = false"
                class="flex items-center justify-between px-3 py-2 rounded-xl text-xs font-bold text-slate-700 hover:bg-slate-100 hover:text-indigo-600 transition"
              >
                <div class="flex items-center gap-2.5">
                  <Ticket class="w-4 h-4 text-indigo-600" />
                  <span>Vé điện tử của tôi</span>
                </div>
                <span v-if="bookingStore.totalTicketCount > 0" class="px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 text-[10px] font-bold">
                  {{ bookingStore.totalTicketCount }}
                </span>
              </router-link>
            </div>

            <!-- System Monitor in Drawer -->
            <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 text-[11px] space-y-2">
              <div class="flex items-center justify-between font-bold text-slate-700">
                <span class="flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
                  VietQR Gateway
                </span>
                <span class="text-emerald-600">99.98% OK</span>
              </div>
              <div class="flex items-center justify-between text-slate-500">
                <span>Hỗ trợ kỹ thuật 24/7</span>
                <span class="font-bold text-indigo-600">1900 6868</span>
              </div>
            </div>
          </div>

          <!-- Bottom Footer of Vertical Menu -->
          <div class="p-4 border-t border-slate-100 bg-slate-50/50">
            <template v-if="authStore.isAuthenticated">
              <button
                type="button"
                @click="handleLogout(); verticalDrawerOpen = false"
                class="w-full py-2.5 px-4 rounded-xl text-rose-600 hover:bg-rose-50 border border-rose-200 font-bold text-xs flex items-center justify-center gap-2 transition cursor-pointer"
              >
                <LogOut class="w-4 h-4" />
                <span>Đăng xuất tài khoản</span>
              </button>
            </template>
            <template v-else>
              <div class="grid grid-cols-2 gap-2">
                <router-link
                  to="/login"
                  @click="verticalDrawerOpen = false"
                  class="py-2.5 px-3 text-center rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs transition"
                >
                  Đăng nhập
                </router-link>
                <router-link
                  to="/register"
                  @click="verticalDrawerOpen = false"
                  class="py-2.5 px-3 text-center rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-sm transition"
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
