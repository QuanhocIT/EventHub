<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userService } from '@/services/user.service'
import { loyaltyService } from '@/services/loyalty.service'
import type { User, Booking, FollowedOrganizerItem, LoyaltyProfile } from '@/types'
import {
  User as UserIcon,
  Shield,
  ShoppingBag,
  Heart,
  KeyRound,
  CheckCircle2,
  AlertCircle,
  ExternalLink,
  Trash2,
  Calendar,
  MapPin,
  Ticket,
  ChevronRight,
  Sparkles,
  Phone,
  Mail,
  Camera,
  Eye,
  EyeOff,
  Lock,
  ArrowRight,
  ShieldCheck,
  Building2,
  Users,
  Coins,
  Crown,
  Gift,
  Zap,
  Award,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

type ProfileTab = 'profile' | 'loyalty' | 'security' | 'orders' | 'following'
const activeTab = ref<ProfileTab>('profile')

// Loyalty Profile State
const loyaltyProfile = ref<LoyaltyProfile | null>(null)
const loadingLoyalty = ref(false)

// User Profile State
const profile = ref<User>({
  id: 0,
  username: '',
  email: '',
  fullName: '',
  phoneNumber: '',
  role: 'ROLE_CUSTOMER',
  avatarUrl: '',
})

// Password Form
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

// Orders State
const bookings = ref<Booking[]>([])
const loadingOrders = ref(false)
const cancellingBooking = ref<string | null>(null)
const cancelModalOpen = ref(false)
const selectedBookingToCancel = ref<Booking | null>(null)

// Followed Organizers State
const followedOrganizers = ref<FollowedOrganizerItem[]>([])
const loadingFollowing = ref(false)

// Notifications / Toasts
interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'info'
}
const toasts = ref<Toast[]>([])
const notify = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 4000)
}

// Formatters
const formatCurrency = (val?: number) => {
  if (val === undefined || val === null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const formatDate = (isoString?: string) => {
  if (!isoString) return ''
  return new Date(isoString).toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// Load Profile Data
const savingProfile = ref(false)
const loadProfile = async () => {
  try {
    const data = await userService.getMyProfile()
    profile.value = { ...data }
    if (authStore.user) {
      authStore.user.fullName = data.fullName
      authStore.user.phoneNumber = data.phoneNumber
      authStore.user.avatarUrl = data.avatarUrl
    }
  } catch (err: any) {
    notify('Không thể tải thông tin hồ sơ', 'error')
  }
}

// Update Profile Form
const handleUpdateProfile = async () => {
  if (!profile.value.fullName.trim()) {
    notify('Vui lòng nhập họ và tên', 'error')
    return
  }
  savingProfile.value = true
  try {
    const updated = await userService.updateProfile({
      fullName: profile.value.fullName.trim(),
      phoneNumber: profile.value.phoneNumber?.trim(),
      avatarUrl: profile.value.avatarUrl?.trim(),
    })
    profile.value = { ...updated }
    if (authStore.user) {
      authStore.user.fullName = updated.fullName
      authStore.user.phoneNumber = updated.phoneNumber
      authStore.user.avatarUrl = updated.avatarUrl
    }
    notify('Cập nhật thông tin hồ sơ thành công!', 'success')
  } catch (err: any) {
    notify(err.response?.data?.message || err.message || 'Lỗi khi cập nhật hồ sơ', 'error')
  } finally {
    savingProfile.value = false
  }
}

// Handle Password Change
const changingPassword = ref(false)
const handleChangePassword = async () => {
  if (!passwordForm.value.currentPassword) {
    notify('Vui lòng nhập mật khẩu hiện tại', 'error')
    return
  }
  if (passwordForm.value.newPassword.length < 6) {
    notify('Mật khẩu mới phải có ít nhất 6 ký tự', 'error')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    notify('Mật khẩu xác nhận không khớp', 'error')
    return
  }
  if (passwordForm.value.currentPassword === passwordForm.value.newPassword) {
    notify('Mật khẩu mới không được trùng với mật khẩu hiện tại', 'error')
    return
  }

  changingPassword.value = true
  try {
    await userService.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword,
      confirmPassword: passwordForm.value.confirmPassword,
    })
    notify('Đổi mật khẩu thành công! Vui lòng ghi nhớ mật khẩu mới.', 'success')
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
  } catch (err: any) {
    notify(err.response?.data?.message || err.message || 'Mật khẩu hiện tại không chính xác', 'error')
  } finally {
    changingPassword.value = false
  }
}

// Load Bookings
const loadBookings = async () => {
  loadingOrders.value = true
  try {
    const res = await userService.getMyBookings()
    bookings.value = res.content
  } catch (err) {
    notify('Không thể tải lịch sử đơn hàng', 'error')
  } finally {
    loadingOrders.value = false
  }
}

// Cancel Booking
const openCancelModal = (booking: Booking) => {
  selectedBookingToCancel.value = booking
  cancelModalOpen.value = true
}

const confirmCancelBooking = async () => {
  if (!selectedBookingToCancel.value) return
  const code = selectedBookingToCancel.value.bookingCode
  cancellingBooking.value = code
  try {
    await userService.cancelBooking(code)
    notify(`Đã hủy thành công đơn hàng ${code}. Ghế và mã giảm giá đã được hoàn trả!`, 'success')
    cancelModalOpen.value = false
    loadBookings()
  } catch (err: any) {
    notify(err.response?.data?.message || err.message || 'Không thể hủy đơn hàng này', 'error')
  } finally {
    cancellingBooking.value = null
  }
}

// Load Followed Organizers
const loadFollowed = async () => {
  loadingFollowing.value = true
  try {
    const res = await userService.getFollowedOrganizers()
    followedOrganizers.value = res.content
  } catch (err) {
    notify('Không thể tải danh sách ban tổ chức đang theo dõi', 'error')
  } finally {
    loadingFollowing.value = false
  }
}

const handleUnfollow = async (organizerId: number, orgName: string) => {
  try {
    await userService.unfollowOrganizer(organizerId)
    followedOrganizers.value = followedOrganizers.value.filter((o) => o.id !== organizerId)
    notify(`Đã bỏ theo dõi "${orgName}"`, 'info')
  } catch (err) {
    notify('Lỗi khi bỏ theo dõi', 'error')
  }
}

const loadLoyalty = async () => {
  loadingLoyalty.value = true
  try {
    const data = await loyaltyService.getMyProfile()
    loyaltyProfile.value = data
  } catch (err) {
    console.warn('Could not load loyalty profile:', err)
  } finally {
    loadingLoyalty.value = false
  }
}

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab as ProfileTab
  }
  loadProfile()
  loadLoyalty()
  loadBookings()
  loadFollowed()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 py-8 px-4 sm:px-6 lg:px-8">
    <!-- Toasts Notifications -->
    <Teleport to="body">
      <div class="fixed top-20 right-4 z-50 space-y-2 max-w-sm pointer-events-none">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="pointer-events-auto p-4 rounded-xl shadow-lg border backdrop-blur-md flex items-center gap-3 transition-all transform animate-slide-in"
          :class="{
            'bg-emerald-900/90 text-white border-emerald-500/50': toast.type === 'success',
            'bg-rose-900/90 text-white border-rose-500/50': toast.type === 'error',
            'bg-slate-900/90 text-white border-slate-700/50': toast.type === 'info',
          }"
        >
          <CheckCircle2 v-if="toast.type === 'success'" class="w-5 h-5 text-emerald-400 shrink-0" />
          <AlertCircle v-else-if="toast.type === 'error'" class="w-5 h-5 text-rose-400 shrink-0" />
          <Sparkles v-else class="w-5 h-5 text-indigo-400 shrink-0" />
          <p class="text-sm font-medium">{{ toast.message }}</p>
        </div>
      </div>
    </Teleport>

    <div class="max-w-6xl mx-auto space-y-8">
      <!-- Header Banner & Avatar Profile Overview -->
      <div class="relative bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
        <!-- Banner Gradient -->
        <div class="h-36 sm:h-44 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 relative">
          <div class="absolute inset-0 bg-black/10"></div>
          <div class="absolute top-4 right-4">
            <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-white/20 text-white backdrop-blur-md border border-white/20">
              <ShieldCheck class="w-3.5 h-3.5" />
              {{ profile.role }}
            </span>
          </div>
        </div>

        <!-- Profile Info Bar -->
        <div class="px-6 sm:px-8 pb-6 pt-0 relative flex flex-col sm:flex-row sm:items-end justify-between gap-4 -mt-14 sm:-mt-16">
          <div class="flex items-end gap-4 sm:gap-6">
            <!-- Avatar -->
            <div class="relative group">
              <img
                :src="profile.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80'"
                alt="Avatar"
                class="w-24 h-24 sm:w-28 sm:h-28 rounded-2xl object-cover border-4 border-white shadow-md bg-slate-100"
              />
              <div class="absolute inset-0 rounded-2xl bg-black/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                <Camera class="w-6 h-6 text-white" />
              </div>
            </div>

            <!-- Name and Username -->
            <div class="space-y-1 mb-1">
              <div class="flex items-center gap-2">
                <h1 class="text-2xl sm:text-3xl font-black text-slate-900">{{ profile.fullName || 'Người dùng EventHub' }}</h1>
                <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
              </div>
              <p class="text-xs sm:text-sm text-slate-500 font-medium">@{{ profile.username }} &bull; {{ profile.email }}</p>
            </div>
          </div>

          <!-- Quick Navigation Actions -->
          <div class="flex items-center gap-2">
            <router-link
              to="/my-tickets"
              class="px-4 py-2.5 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 text-xs font-bold flex items-center gap-2 transition"
            >
              <Ticket class="w-4 h-4" />
              <span>Xem vé của tôi</span>
            </router-link>
            <router-link
              v-if="authStore.isAdmin || authStore.isOrganizer || authStore.isStaff"
              to="/dashboard"
              class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold flex items-center gap-2 shadow-sm transition"
            >
              <Shield class="w-4 h-4" />
              <span>Cổng quản trị</span>
            </router-link>
          </div>
        </div>

        <!-- Navigation Tabs -->
        <div class="border-t border-slate-100 px-6 sm:px-8 flex items-center gap-2 overflow-x-auto scrollbar-none">
          <button
            type="button"
            @click="activeTab = 'profile'"
            class="py-3 px-3.5 text-xs font-bold border-b-2 flex items-center gap-2 transition cursor-pointer shrink-0"
            :class="activeTab === 'profile' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          >
            <UserIcon class="w-4 h-4" />
            <span>Hồ sơ cá nhân</span>
          </button>

          <button
            type="button"
            @click="activeTab = 'loyalty'"
            class="py-3 px-3.5 text-xs font-bold border-b-2 flex items-center gap-2 transition cursor-pointer shrink-0"
            :class="activeTab === 'loyalty' ? 'border-amber-500 text-amber-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          >
            <Crown class="w-4 h-4 text-amber-500" />
            <span>Điểm Thưởng & Hạng VIP</span>
            <span v-if="loyaltyProfile" class="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-amber-100 text-amber-800">
              {{ loyaltyProfile.pointsBalance }} điểm
            </span>
          </button>

          <button
            type="button"
            @click="activeTab = 'security'"
            class="py-3 px-3.5 text-xs font-bold border-b-2 flex items-center gap-2 transition cursor-pointer shrink-0"
            :class="activeTab === 'security' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          >
            <KeyRound class="w-4 h-4" />
            <span>Bảo mật & Đổi mật khẩu</span>
          </button>

          <button
            type="button"
            @click="activeTab = 'orders'"
            class="py-3 px-3.5 text-xs font-bold border-b-2 flex items-center gap-2 transition cursor-pointer shrink-0"
            :class="activeTab === 'orders' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          >
            <ShoppingBag class="w-4 h-4" />
            <span>Lịch sử đơn hàng</span>
            <span v-if="bookings.length" class="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-indigo-100 text-indigo-700">
              {{ bookings.length }}
            </span>
          </button>

          <button
            type="button"
            @click="activeTab = 'following'"
            class="py-3 px-3.5 text-xs font-bold border-b-2 flex items-center gap-2 transition cursor-pointer shrink-0"
            :class="activeTab === 'following' ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-slate-500 hover:text-slate-800'"
          >
            <Heart class="w-4 h-4" />
            <span>Ban tổ chức theo dõi</span>
            <span v-if="followedOrganizers.length" class="px-1.5 py-0.2 rounded-full text-[10px] font-black bg-pink-100 text-pink-700">
              {{ followedOrganizers.length }}
            </span>
          </button>
        </div>
      </div>

      <!-- TAB 1: PROFILE INFO -->
      <div v-if="activeTab === 'profile'" class="bg-white rounded-3xl p-6 sm:p-8 shadow-sm border border-slate-200 space-y-6">
        <div>
          <h2 class="text-lg font-black text-slate-900">Thông tin cá nhân</h2>
          <p class="text-xs text-slate-500">Cập nhật họ tên, số điện thoại và đường dẫn ảnh đại diện của bạn.</p>
        </div>

        <form @submit.prevent="handleUpdateProfile" class="space-y-5 max-w-2xl">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">Tên đăng nhập (Username)</label>
              <div class="relative">
                <input
                  v-model="profile.username"
                  type="text"
                  disabled
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-500 text-sm font-semibold cursor-not-allowed"
                />
                <Lock class="w-4 h-4 text-slate-400 absolute right-3.5 top-1/2 -translate-y-1/2" />
              </div>
              <span class="text-[10px] text-slate-400 mt-0.5 block">Tên người dùng không thể thay đổi</span>
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">Email đăng ký</label>
              <div class="relative">
                <input
                  v-model="profile.email"
                  type="email"
                  disabled
                  class="w-full px-3.5 py-2.5 rounded-xl bg-slate-100 border border-slate-200 text-slate-500 text-sm font-semibold cursor-not-allowed"
                />
                <Mail class="w-4 h-4 text-slate-400 absolute right-3.5 top-1/2 -translate-y-1/2" />
              </div>
              <span class="text-[10px] text-slate-400 mt-0.5 block">Dùng để nhận vé điện tử và hóa đơn</span>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Họ và tên đầy đủ <span class="text-rose-500">*</span></label>
            <input
              v-model="profile.fullName"
              type="text"
              placeholder="Ví dụ: Nguyễn Văn An"
              class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm text-slate-900 font-semibold"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Số điện thoại liên hệ</label>
            <div class="relative">
              <input
                v-model="profile.phoneNumber"
                type="tel"
                placeholder="Ví dụ: 0912345678"
                class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm text-slate-900 font-semibold"
              />
              <Phone class="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">URL Ảnh đại diện (Avatar)</label>
            <input
              v-model="profile.avatarUrl"
              type="url"
              placeholder="https://example.com/avatar.jpg"
              class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm text-slate-900 font-semibold"
            />
          </div>

          <div class="pt-2 flex items-center gap-3">
            <button
              type="submit"
              :disabled="savingProfile"
              class="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-md shadow-indigo-600/20 transition flex items-center gap-2 cursor-pointer disabled:opacity-50"
            >
              <CheckCircle2 class="w-4 h-4" />
              <span>{{ savingProfile ? 'Đang lưu...' : 'Lưu thay đổi' }}</span>
            </button>
          </div>
        </form>
      </div>

      <!-- TAB: LOYALTY PROGRAM & VIP TIERS (CINECLUB / EVENTHUB) -->
      <div v-else-if="activeTab === 'loyalty'" class="space-y-6">
        <!-- VIP Card (Metallic / Glassmorphism) -->
        <div class="relative rounded-3xl p-6 sm:p-8 overflow-hidden text-white shadow-2xl bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 border border-slate-800">
          <!-- Background Glow Effect -->
          <div class="absolute -right-16 -top-16 w-64 h-64 rounded-full bg-gradient-to-br from-amber-500/20 to-purple-600/20 blur-3xl pointer-events-none"></div>

          <div class="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
            <!-- Left: Card Info -->
            <div class="space-y-3">
              <div class="flex items-center gap-2.5">
                <span class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-black uppercase tracking-wider bg-amber-400/20 text-amber-300 border border-amber-400/30 backdrop-blur-md">
                  <Crown class="w-3.5 h-3.5 text-amber-400" />
                  CineClub & EventHub VIP Pass
                </span>
                <span class="text-xs text-slate-400">&bull; Thẻ Thành Viên Số</span>
              </div>
              <h2 class="text-2xl sm:text-3xl font-black tracking-tight text-white flex items-center gap-2">
                <span>{{ loyaltyProfile?.tierDisplayName || 'Thành Viên Bạc' }}</span>
                <Sparkles class="w-5 h-5 text-amber-400" />
              </h2>
              <p class="text-xs text-slate-300 max-w-md">
                Chủ thẻ: <strong>{{ profile.fullName || authStore.user?.fullName || 'Khách Hàng' }}</strong> &bull; Tận hưởng chiết khấu <strong>{{ loyaltyProfile?.discountPercent || 5 }}%</strong> cho toàn bộ vé sự kiện và phim chiếu rạp.
              </p>
            </div>

            <!-- Right: Points Balance Badge -->
            <div class="p-4 sm:p-5 rounded-2xl bg-white/10 backdrop-blur-md border border-white/15 flex items-center gap-4 shrink-0">
              <div class="w-12 h-12 rounded-xl bg-amber-400/20 border border-amber-400/40 flex items-center justify-center">
                <Coins class="w-7 h-7 text-amber-300" />
              </div>
              <div>
                <span class="text-[11px] text-amber-200 font-bold uppercase tracking-wider block">Điểm Khả Dụng</span>
                <div class="text-2xl sm:text-3xl font-black text-amber-300">
                  {{ (loyaltyProfile?.pointsBalance || 0).toLocaleString('vi-VN') }} <span class="text-xs text-white font-normal">điểm</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Progress Bar to Next Tier -->
          <div class="mt-6 pt-6 border-t border-white/10 space-y-2">
            <div class="flex items-center justify-between text-xs">
              <span class="text-slate-300">
                Tiến trình lên hạng kế tiếp: <strong class="text-amber-300">{{ loyaltyProfile?.nextTierProgressPercent || 35 }}%</strong>
              </span>
              <span class="text-slate-400">
                Còn thiếu <strong class="text-white">{{ (loyaltyProfile?.nextTierPointsRequired || 250).toLocaleString('vi-VN') }}</strong> điểm
              </span>
            </div>
            <div class="w-full h-3 rounded-full bg-white/10 overflow-hidden p-0.5 border border-white/15">
              <div
                class="h-full rounded-full bg-gradient-to-r from-amber-400 via-amber-300 to-indigo-400 transition-all duration-700 shadow-sm"
                :style="{ width: `${loyaltyProfile?.nextTierProgressPercent || 35}%` }"
              ></div>
            </div>
          </div>
        </div>

        <!-- Tier Matrix & Privileges -->
        <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
          <div
            class="p-4 rounded-2xl border transition-all"
            :class="loyaltyProfile?.tier === 'MEMBER' ? 'bg-amber-50/50 border-amber-300 shadow-sm ring-2 ring-amber-400/20' : 'bg-white border-slate-200'"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-black uppercase text-slate-500">MEMBER</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-600">0đ</span>
            </div>
            <h4 class="text-sm font-black text-slate-900">Thành Viên</h4>
            <p class="text-[11px] text-slate-500 mt-1">Tích 1 điểm / 10.000đ khi đặt vé</p>
            <div class="mt-3 pt-3 border-t border-slate-100 text-[11px] text-slate-600 space-y-1">
              <div>&bull; Quà sinh nhật bất ngờ</div>
              <div>&bull; Tích điểm review sự kiện</div>
            </div>
          </div>

          <div
            class="p-4 rounded-2xl border transition-all"
            :class="loyaltyProfile?.tier === 'SILVER' ? 'bg-indigo-50/50 border-indigo-300 shadow-sm ring-2 ring-indigo-400/20' : 'bg-white border-slate-200'"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-black uppercase text-indigo-600">SILVER</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-indigo-100 text-indigo-700">500 điểm</span>
            </div>
            <h4 class="text-sm font-black text-slate-900">Hạng Bạc (Silver)</h4>
            <p class="text-[11px] text-slate-500 mt-1">Giảm ngay 5% mọi đơn hàng</p>
            <div class="mt-3 pt-3 border-t border-slate-100 text-[11px] text-slate-600 space-y-1">
              <div>&bull; Giảm giá 5% vé sự kiện/phim</div>
              <div>&bull; Đổi voucher đặc quyền</div>
            </div>
          </div>

          <div
            class="p-4 rounded-2xl border transition-all"
            :class="loyaltyProfile?.tier === 'GOLD' ? 'bg-amber-50/50 border-amber-400 shadow-sm ring-2 ring-amber-400/30' : 'bg-white border-slate-200'"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-black uppercase text-amber-600">GOLD</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-100 text-amber-800">1.500 điểm</span>
            </div>
            <h4 class="text-sm font-black text-slate-900">Hạng Vàng (Gold)</h4>
            <p class="text-[11px] text-slate-500 mt-1">Giảm ngay 10% mọi đơn hàng</p>
            <div class="mt-3 pt-3 border-t border-slate-100 text-[11px] text-slate-600 space-y-1">
              <div>&bull; Giảm giá 10% trọn đời hạng</div>
              <div>&bull; Quyền mua vé sớm Early Bird</div>
              <div>&bull; Miễn phí combo bắp nước</div>
            </div>
          </div>

          <div
            class="p-4 rounded-2xl border transition-all"
            :class="loyaltyProfile?.tier === 'DIAMOND' ? 'bg-purple-50/50 border-purple-400 shadow-sm ring-2 ring-purple-400/30' : 'bg-white border-slate-200'"
          >
            <div class="flex items-center justify-between mb-2">
              <span class="text-xs font-black uppercase text-purple-600">DIAMOND</span>
              <span class="text-[10px] font-bold px-2 py-0.5 rounded bg-purple-100 text-purple-800">4.000 điểm</span>
            </div>
            <h4 class="text-sm font-black text-slate-900">Kim Cương (Diamond)</h4>
            <p class="text-[11px] text-slate-500 mt-1">Giảm ngay 15% VIP Lounge</p>
            <div class="mt-3 pt-3 border-t border-slate-100 text-[11px] text-slate-600 space-y-1">
              <div>&bull; Giảm giá 15% tối đa</div>
              <div>&bull; Lối đi soát vé ưu tiên VIP</div>
              <div>&bull; Quà tặng VIP EventHub Box</div>
            </div>
          </div>
        </div>

        <!-- Loyalty Points Activity History -->
        <div class="bg-white rounded-3xl p-6 sm:p-8 shadow-sm border border-slate-200 space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-base font-black text-slate-900">Lịch Sử Biến Động Điểm Thưởng</h3>
              <p class="text-xs text-slate-500">Chi tiết các lượt tích điểm mua vé, review kèm ảnh và check-in</p>
            </div>
            <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-xl">
              Cập nhật thời gian thực
            </span>
          </div>

          <div v-if="!loyaltyProfile?.recentTransactions?.length" class="py-8 text-center text-slate-400">
            <Coins class="w-10 h-10 mx-auto text-slate-300 mb-2" />
            <p class="text-xs">Chưa có giao dịch tích điểm nào gần đây.</p>
          </div>

          <div v-else class="divide-y divide-slate-100">
            <div
              v-for="tx in loyaltyProfile.recentTransactions"
              :key="tx.id"
              class="py-3.5 flex items-center justify-between gap-3"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-9 h-9 rounded-xl flex items-center justify-center shrink-0 font-bold"
                  :class="tx.pointsChange >= 0 ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'"
                >
                  <Sparkles v-if="tx.pointsChange >= 0" class="w-4 h-4" />
                  <Coins v-else class="w-4 h-4" />
                </div>
                <div>
                  <h4 class="text-xs font-black text-slate-900">{{ tx.activityName }}</h4>
                  <p class="text-[11px] text-slate-400">{{ tx.description || formatDate(tx.createdAt) }}</p>
                </div>
              </div>

              <div class="text-right">
                <span
                  class="text-xs font-black"
                  :class="tx.pointsChange >= 0 ? 'text-emerald-600' : 'text-rose-600'"
                >
                  {{ tx.pointsChange >= 0 ? `+${tx.pointsChange}` : tx.pointsChange }} điểm
                </span>
                <span class="block text-[10px] text-slate-400">{{ formatDate(tx.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 2: SECURITY & CHANGE PASSWORD -->
      <div v-if="activeTab === 'security'" class="bg-white rounded-3xl p-6 sm:p-8 shadow-sm border border-slate-200 space-y-6">
        <div>
          <h2 class="text-lg font-black text-slate-900">Đổi mật khẩu tài khoản</h2>
          <p class="text-xs text-slate-500">Mật khẩu mới cần tối thiểu 6 ký tự để đảm bảo an toàn cho tài khoản và vé điện tử của bạn.</p>
        </div>

        <form @submit.prevent="handleChangePassword" class="space-y-4 max-w-xl">
          <!-- Current Password -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Mật khẩu hiện tại <span class="text-rose-500">*</span></label>
            <div class="relative">
              <input
                v-model="passwordForm.currentPassword"
                :type="showCurrentPassword ? 'text' : 'password'"
                placeholder="Nhập mật khẩu hiện tại..."
                class="w-full px-3.5 py-2.5 pr-10 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm"
              />
              <button
                type="button"
                @click="showCurrentPassword = !showCurrentPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <EyeOff v-if="showCurrentPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- New Password -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Mật khẩu mới <span class="text-rose-500">*</span></label>
            <div class="relative">
              <input
                v-model="passwordForm.newPassword"
                :type="showNewPassword ? 'text' : 'password'"
                placeholder="Tối thiểu 6 ký tự..."
                class="w-full px-3.5 py-2.5 pr-10 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm"
              />
              <button
                type="button"
                @click="showNewPassword = !showNewPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <EyeOff v-if="showNewPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Confirm Password -->
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Xác nhận mật khẩu mới <span class="text-rose-500">*</span></label>
            <div class="relative">
              <input
                v-model="passwordForm.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="Nhập lại mật khẩu mới..."
                class="w-full px-3.5 py-2.5 pr-10 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 transition text-sm"
              />
              <button
                type="button"
                @click="showConfirmPassword = !showConfirmPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
              >
                <EyeOff v-if="showConfirmPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <div class="pt-2">
            <button
              type="submit"
              :disabled="changingPassword"
              class="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs shadow-md shadow-indigo-600/20 transition flex items-center gap-2 cursor-pointer disabled:opacity-50"
            >
              <KeyRound class="w-4 h-4" />
              <span>{{ changingPassword ? 'Đang cập nhật...' : 'Cập nhật mật khẩu' }}</span>
            </button>
          </div>
        </form>
      </div>

      <!-- TAB 3: BOOKINGS & ORDERS -->
      <div v-if="activeTab === 'orders'" class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-lg font-black text-slate-900">Lịch sử đặt vé & đơn hàng</h2>
            <p class="text-xs text-slate-500">Xem tình trạng thanh toán, tải mã QR hoặc hủy đơn hàng khi quá hạn.</p>
          </div>
        </div>

        <div v-if="loadingOrders" class="py-12 text-center text-slate-400">
          <p class="text-sm">Đang tải danh sách đơn hàng...</p>
        </div>

        <div v-else-if="!bookings.length" class="bg-white rounded-3xl p-12 text-center border border-slate-200">
          <ShoppingBag class="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p class="text-sm font-bold text-slate-700">Bạn chưa có đơn đặt vé nào</p>
          <p class="text-xs text-slate-400 mt-1">Khám phá các sự kiện âm nhạc, thể thao và công nghệ hot nhất ngay hôm nay.</p>
          <router-link
            to="/events"
            class="inline-flex items-center gap-2 mt-4 px-4 py-2 rounded-xl bg-indigo-600 text-white text-xs font-bold hover:bg-indigo-700 transition"
          >
            <span>Khám phá sự kiện</span>
            <ArrowRight class="w-3.5 h-3.5" />
          </router-link>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="b in bookings"
            :key="b.bookingCode"
            class="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs hover:border-indigo-200 transition space-y-4"
          >
            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-slate-100 pb-3">
              <div class="flex items-center gap-2">
                <span class="text-xs font-extrabold text-slate-900">Mã đơn: {{ b.bookingCode }}</span>
                <span
                  class="px-2 py-0.5 rounded-full text-[10px] font-black uppercase"
                  :class="{
                    'bg-emerald-100 text-emerald-800': b.status === 'CONFIRMED',
                    'bg-amber-100 text-amber-800': b.status === 'PENDING_PAYMENT',
                    'bg-rose-100 text-rose-800': b.status === 'CANCELLED',
                  }"
                >
                  {{ b.status === 'CONFIRMED' ? 'Đã Thanh Toán' : b.status === 'PENDING_PAYMENT' ? 'Chờ Thanh Toán' : 'Đã Hủy' }}
                </span>
              </div>
              <span class="text-[11px] text-slate-400">{{ formatDate(b.createdAt) }}</span>
            </div>

            <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div class="space-y-1">
                <h3 class="font-extrabold text-slate-900 text-sm sm:text-base">{{ b.eventTitle }}</h3>
                <p class="text-xs text-slate-500 flex items-center gap-1.5">
                  <MapPin class="w-3.5 h-3.5 text-slate-400" />
                  <span>{{ b.venueName }}</span>
                </p>
                <p class="text-xs text-slate-500 flex items-center gap-1.5">
                  <Calendar class="w-3.5 h-3.5 text-slate-400" />
                  <span>{{ formatDate(b.eventStartDateTime) }}</span>
                </p>
              </div>

              <div class="text-left sm:text-right space-y-1">
                <p class="text-xs text-slate-400">Tổng thanh toán</p>
                <p class="text-lg font-black text-indigo-600">{{ formatCurrency(b.totalAmount) }}</p>
                <p v-if="b.discountAmount > 0" class="text-[10px] text-emerald-600 font-bold">
                  (Đã giảm {{ formatCurrency(b.discountAmount) }} với mã {{ b.couponCode }})
                </p>
              </div>
            </div>

            <!-- Booking Items -->
            <div class="bg-slate-50 rounded-xl p-3 text-xs space-y-1.5">
              <div
                v-for="(item, idx) in b.items"
                :key="idx"
                class="flex items-center justify-between text-slate-700"
              >
                <span>{{ item.ticketTypeName }} &bull; Ghế: {{ item.seatLabel || 'Tự do' }} x {{ item.quantity }}</span>
                <span class="font-bold">{{ formatCurrency(item.subtotal || item.unitPrice * item.quantity) }}</span>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex items-center justify-end gap-2 pt-1">
              <button
                v-if="b.status === 'PENDING_PAYMENT'"
                type="button"
                @click="openCancelModal(b)"
                class="px-3 py-1.5 rounded-xl border border-rose-200 text-rose-600 hover:bg-rose-50 text-xs font-bold transition flex items-center gap-1 cursor-pointer"
              >
                <Trash2 class="w-3.5 h-3.5" />
                <span>Hủy đơn</span>
              </button>

              <router-link
                v-if="b.status === 'CONFIRMED'"
                to="/my-tickets"
                class="px-3.5 py-1.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold transition flex items-center gap-1.5"
              >
                <Ticket class="w-3.5 h-3.5" />
                <span>Xem vé & QR Code</span>
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 4: FOLLOWED ORGANIZERS -->
      <div v-if="activeTab === 'following'" class="space-y-4">
        <div>
          <h2 class="text-lg font-black text-slate-900">Ban tổ chức đang theo dõi</h2>
          <p class="text-xs text-slate-500">Cập nhật nhanh các sự kiện mới nhất từ các nhà tổ chức sự kiện bạn quan tâm.</p>
        </div>

        <div v-if="loadingFollowing" class="py-12 text-center text-slate-400">
          <p class="text-sm">Đang tải danh sách ban tổ chức...</p>
        </div>

        <div v-else-if="!followedOrganizers.length" class="bg-white rounded-3xl p-12 text-center border border-slate-200">
          <Heart class="w-12 h-12 text-slate-300 mx-auto mb-3" />
          <p class="text-sm font-bold text-slate-700">Bạn chưa theo dõi ban tổ chức nào</p>
          <p class="text-xs text-slate-400 mt-1">Theo dõi các ban tổ chức uy tín để không bỏ lỡ đợt mở bán vé Early Bird.</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="org in followedOrganizers"
            :key="org.id"
            class="bg-white rounded-2xl p-5 border border-slate-200 shadow-2xs hover:border-indigo-200 transition flex items-start gap-4"
          >
            <img
              :src="org.logoUrl || 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=200&q=80'"
              :alt="org.organizationName"
              class="w-14 h-14 rounded-xl object-cover border border-slate-200 shrink-0"
            />
            <div class="flex-1 min-w-0 space-y-1">
              <div class="flex items-center gap-1.5">
                <h3 class="font-extrabold text-sm text-slate-900 truncate">{{ org.organizationName }}</h3>
                <ShieldCheck v-if="org.verified" class="w-4 h-4 text-indigo-600 shrink-0" title="Đã xác thực" />
              </div>
              <p class="text-xs text-slate-500 line-clamp-2">{{ org.bio }}</p>
              <div class="flex items-center justify-between pt-2">
                <span class="text-[11px] font-bold text-slate-400 flex items-center gap-1">
                  <Users class="w-3.5 h-3.5" />
                  {{ org.followersCount?.toLocaleString('vi-VN') || 0 }} người theo dõi
                </span>

                <button
                  type="button"
                  @click="handleUnfollow(org.id, org.organizationName)"
                  class="text-xs font-bold text-rose-600 hover:text-rose-700 px-2.5 py-1 rounded-lg hover:bg-rose-50 transition cursor-pointer"
                >
                  Bỏ theo dõi
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Confirm Cancel Modal -->
    <Teleport to="body">
      <div v-if="cancelModalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <div class="fixed inset-0 bg-slate-900/60 backdrop-blur-xs" @click="cancelModalOpen = false"></div>
        <div class="relative bg-white rounded-2xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-rose-100 text-rose-600 flex items-center justify-center">
              <AlertCircle class="w-5 h-5" />
            </div>
            <div>
              <h3 class="font-extrabold text-base text-slate-900">Xác nhận hủy đơn hàng</h3>
              <p class="text-xs text-slate-500">{{ selectedBookingToCancel?.bookingCode }}</p>
            </div>
          </div>
          <p class="text-xs text-slate-600 leading-relaxed">
            Bạn có chắc chắn muốn hủy đơn hàng này không? Ghế đang giữ và mã giảm giá đã sử dụng sẽ được hoàn trả lại cho hệ thống ngay lập tức.
          </p>
          <div class="flex items-center justify-end gap-2 pt-2">
            <button
              type="button"
              @click="cancelModalOpen = false"
              class="px-4 py-2 rounded-xl text-xs font-bold text-slate-600 hover:bg-slate-100 transition"
            >
              Đóng
            </button>
            <button
              type="button"
              @click="confirmCancelBooking"
              :disabled="!!cancellingBooking"
              class="px-4 py-2 rounded-xl text-xs font-bold text-white bg-rose-600 hover:bg-rose-700 shadow-sm transition disabled:opacity-50"
            >
              {{ cancellingBooking ? 'Đang hủy...' : 'Đồng ý hủy' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-slide-in {
  animation: slideIn 0.25s ease-out forwards;
}
</style>
