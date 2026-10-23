<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Ticket,
  LogIn,
  Lock,
  Mail,
  User,
  AlertCircle,
  Eye,
  EyeOff,
  ShieldCheck,
  Sparkles,
  CheckCircle2,
  ArrowRight,
  HelpCircle,
  X,
  Send,
  QrCode,
  Smartphone,
  Star,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const rememberMe = ref(true)
const showPassword = ref(false)
const localError = ref('')
const selectedRole = ref<'customer' | 'admin' | 'organizer' | null>(null)

// Clear inputs on mount to avoid unwanted browser autofill
onMounted(() => {
  username.value = ''
  password.value = ''
})

// Forgot Password Modal
const showForgotModal = ref(false)
const forgotEmail = ref('')
const forgotSuccess = ref(false)
const isSendingForgot = ref(false)

const handleLogin = async () => {
  localError.value = ''
  if (!username.value.trim() || !password.value.trim()) {
    localError.value = 'Vui lòng nhập tên đăng nhập và mật khẩu!'
    return
  }

  const success = await authStore.login({
    usernameOrEmail: username.value.trim(),
    password: password.value.trim(),
  })

  if (success) {
    const target = (route.query.redirect as string) || (authStore.isAdmin || authStore.isOrganizer ? '/dashboard' : '/')
    router.push(target)
  } else {
    localError.value = authStore.error || 'Tài khoản hoặc mật khẩu không chính xác'
  }
}

// 1-Click Demo Fill
const fillDemo = (role: 'customer' | 'admin' | 'organizer') => {
  selectedRole.value = role
  localError.value = ''
  if (role === 'customer') {
    username.value = 'customer'
    password.value = 'customer123'
  } else if (role === 'admin') {
    username.value = 'admin'
    password.value = 'admin123'
  } else {
    username.value = 'organizer'
    password.value = 'organizer123'
  }
}

// Social Login Simulation
const handleSocialLogin = async (provider: 'Google' | 'Apple') => {
  username.value = provider === 'Google' ? 'google_user@gmail.com' : 'apple_id@icloud.com'
  password.value = 'SocialOAuth2026!'
  await handleLogin()
}

// Forgot Password Handler
const handleForgotPassword = () => {
  if (!forgotEmail.value.trim()) return
  isSendingForgot.value = true
  setTimeout(() => {
    isSendingForgot.value = false
    forgotSuccess.value = true
  }, 1000)
}

const closeForgotModal = () => {
  showForgotModal.value = false
  forgotSuccess.value = false
  forgotEmail.value = ''
}
</script>

<template>
  <div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50/70">
    <!-- Main Auth Card Container (Modern Split Layout) -->
    <div class="w-full max-w-5xl bg-white rounded-3xl border border-slate-200/90 shadow-2xl shadow-slate-200/50 overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[640px]">
      
      <!-- Left Column: Visual Showcase & Brand Story (5 Cols on LG) -->
      <div class="hidden lg:flex lg:col-span-5 relative bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 text-white p-8 xl:p-10 flex-col justify-between overflow-hidden">
        <!-- Ambient lighting glow -->
        <div class="absolute -top-24 -left-24 w-80 h-80 rounded-full bg-indigo-600/30 blur-3xl pointer-events-none"></div>
        <div class="absolute -bottom-24 -right-24 w-80 h-80 rounded-full bg-violet-600/25 blur-3xl pointer-events-none"></div>

        <!-- Top branding -->
        <div class="relative z-10">
          <div class="flex items-center gap-2.5 mb-6">
            <div class="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-500 to-violet-500 flex items-center justify-center shadow-md shadow-indigo-500/30">
              <Ticket class="w-5 h-5 text-white transform -rotate-12" />
            </div>
            <div>
              <span class="text-xl font-bold tracking-tight text-white">Event<span class="text-indigo-400">Hub</span></span>
              <span class="block text-[10px] font-semibold text-indigo-300 uppercase tracking-widest">Ticketing Platform</span>
            </div>
          </div>

          <div class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-white/10 text-indigo-200 border border-white/15 backdrop-blur-md mb-4">
            <Sparkles class="w-3.5 h-3.5 text-amber-300" />
            <span>Nền tảng vé sự kiện số 1 Việt Nam</span>
          </div>

          <h2 class="text-2xl xl:text-3xl font-extrabold text-white leading-tight tracking-tight mb-3">
            Khám phá & Săn vé những sự kiện đỉnh cao
          </h2>
          <p class="text-xs xl:text-sm text-slate-300 leading-relaxed font-normal">
            Trải nghiệm đặt vé thông minh với mã QR động Anti-Passback, hỗ trợ xuất vé Apple Wallet và bảo mật 100%.
          </p>
        </div>

        <!-- Middle Feature Bullets -->
        <div class="relative z-10 space-y-3.5 my-8">
          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-indigo-500/20 text-indigo-300 flex items-center justify-center shrink-0">
              <QrCode class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Dynamic QR Code</div>
              <div class="text-slate-400 text-[11px]">Chống vé giả & chống chụp màn hình bán lại</div>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-300 flex items-center justify-center shrink-0">
              <Smartphone class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Apple & Google Wallet</div>
              <div class="text-slate-400 text-[11px]">Đồng bộ thẻ vé 1 chạm không cần in vé giấy</div>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-amber-500/20 text-amber-300 flex items-center justify-center shrink-0">
              <ShieldCheck class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Bảo chứng hoàn tiền 100%</div>
              <div class="text-slate-400 text-[11px]">Bảo vệ quyền lợi tối đa khi sự kiện dời ngày</div>
            </div>
          </div>
        </div>

        <!-- Bottom Quote / Social proof -->
        <div class="relative z-10 pt-4 border-t border-white/10">
          <div class="flex items-center gap-1 text-amber-400 mb-1">
            <Star v-for="i in 5" :key="i" class="w-3.5 h-3.5 fill-current" />
          </div>
          <p class="text-xs text-slate-300 italic line-clamp-2">
            "Mua vé concert giao diện cực mượt, check-in qua cổng soát vé chỉ mất 1 giây, rất an tâm."
          </p>
          <div class="text-[11px] text-slate-400 mt-1 font-semibold">
            &mdash; Nguyễn Văn Quân, Khán giả VIP
          </div>
        </div>
      </div>

      <!-- Right Column: Interactive Login Form (7 Cols on LG) -->
      <div class="lg:col-span-7 p-6 sm:p-10 xl:p-12 flex flex-col justify-between">
        <div>
          <!-- Header -->
          <div class="mb-6">
            <div class="flex items-center justify-between mb-2">
              <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-indigo-50 text-indigo-700 text-xs font-bold">
                <LogIn class="w-3.5 h-3.5" />
                <span>Cổng Đăng Nhập An Toàn</span>
              </div>
              <span class="text-xs text-slate-400">256-bit SSL</span>
            </div>

            <h2 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Đăng Nhập Tài Khoản
            </h2>
            <p class="text-xs sm:text-sm text-slate-500 mt-1 leading-relaxed">
              Truy cập để quản lý vé điện tử, lịch trình sự kiện và hóa đơn thanh toán.
            </p>
          </div>

          <!-- Quick 1-Click Role Switcher (Enhanced) -->
          <div class="mb-6 p-3.5 rounded-2xl bg-slate-50 border border-slate-200/90">
            <div class="flex items-center justify-between text-xs mb-2">
              <span class="font-bold text-slate-700 flex items-center gap-1.5">
                <Sparkles class="w-3.5 h-3.5 text-amber-500" />
                <span>Đăng nhập nhanh (Test tài khoản):</span>
              </span>
              <span class="text-[11px] text-slate-400">1 chạm điền tự động</span>
            </div>

            <div class="grid grid-cols-2 sm:grid-cols-3 gap-2">
              <button
                type="button"
                @click="fillDemo('customer')"
                class="py-2 px-2.5 rounded-xl text-xs font-semibold border transition flex items-center justify-center gap-1.5"
                :class="[
                  selectedRole === 'customer'
                    ? 'bg-indigo-600 text-white border-indigo-600 shadow-sm'
                    : 'bg-white text-slate-700 border-slate-200 hover:border-indigo-300 hover:bg-slate-50'
                ]"
              >
                <span>Khách hàng</span>
              </button>

              <button
                type="button"
                @click="fillDemo('admin')"
                class="py-2 px-2.5 rounded-xl text-xs font-semibold border transition flex items-center justify-center gap-1.5"
                :class="[
                  selectedRole === 'admin'
                    ? 'bg-purple-600 text-white border-purple-600 shadow-sm'
                    : 'bg-white text-slate-700 border-slate-200 hover:border-purple-300 hover:bg-slate-50'
                ]"
              >
                <span>Quản trị viên</span>
              </button>

              <button
                type="button"
                @click="fillDemo('organizer')"
                class="col-span-2 sm:col-span-1 py-2 px-2.5 rounded-xl text-xs font-semibold border transition flex items-center justify-center gap-1.5"
                :class="[
                  selectedRole === 'organizer'
                    ? 'bg-emerald-600 text-white border-emerald-600 shadow-sm'
                    : 'bg-white text-slate-700 border-slate-200 hover:border-emerald-300 hover:bg-slate-50'
                ]"
              >
                <span>Ban Tổ Chức</span>
              </button>
            </div>
          </div>

          <!-- Social Login Buttons -->
          <div class="grid grid-cols-2 gap-3 mb-6">
            <button
              type="button"
              @click="handleSocialLogin('Google')"
              class="py-2.5 px-3 rounded-xl border border-slate-200 hover:bg-slate-50 hover:border-slate-300 text-slate-700 text-xs font-semibold flex items-center justify-center gap-2 transition shadow-2xs"
            >
              <!-- Google G Icon -->
              <svg class="w-4 h-4 shrink-0" viewBox="0 0 24 24">
                <path fill="#EA4335" d="M12 5c1.6 0 3 .6 4.1 1.6l3.1-3.1C17.3 1.8 14.8 1 12 1 7.4 1 3.5 3.6 1.6 7.4l3.7 2.9C6.2 7.3 8.9 5 12 5z" />
                <path fill="#4285F4" d="M23.5 12.3c0-.8-.1-1.6-.2-2.3H12v4.6h6.5c-.3 1.5-1.1 2.8-2.4 3.7l3.7 2.9c2.2-2 3.7-5 3.7-8.9z" />
                <path fill="#FBBC05" d="M5.3 14.7c-.2-.7-.4-1.5-.4-2.3s.2-1.6.4-2.3L1.6 7.2C.6 9.2 0 11.5 0 14s.6 4.8 1.6 6.8l3.7-2.9z" />
                <path fill="#34A853" d="M12 23c3.2 0 6-1.1 8-3l-3.7-2.9c-1.1.7-2.5 1.2-4.3 1.2-3.1 0-5.8-2.3-6.7-5.3L1.6 16c1.9 3.8 5.8 6.4 10.4 6.4z" />
              </svg>
              <span>Google</span>
            </button>

            <button
              type="button"
              @click="handleSocialLogin('Apple')"
              class="py-2.5 px-3 rounded-xl border border-slate-200 hover:bg-slate-50 hover:border-slate-300 text-slate-700 text-xs font-semibold flex items-center justify-center gap-2 transition shadow-2xs"
            >
              <!-- Apple Icon -->
              <svg class="w-4 h-4 shrink-0 fill-current text-slate-900" viewBox="0 0 24 24">
                <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M15.97 6.37c.62-.75 1.04-1.8 1.01-2.87-.96.04-2.12.64-2.79 1.42-.58.68-1.1 1.76-.96 2.81 1.06.08 2.14-.58 2.74-1.36z" />
              </svg>
              <span>Apple ID</span>
            </button>
          </div>

          <!-- Divider -->
          <div class="relative flex items-center justify-center mb-6">
            <div class="border-t border-slate-200 w-full"></div>
            <span class="bg-white px-3 text-slate-400 text-xs font-medium shrink-0">hoặc bằng tài khoản</span>
            <div class="border-t border-slate-200 w-full"></div>
          </div>

          <!-- Main Credentials Form -->
          <form @submit.prevent="handleLogin" class="space-y-4" autocomplete="off">
            <!-- Username/Email -->
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1.5">
                Tên đăng nhập hoặc Email <span class="text-rose-500">*</span>
              </label>
              <div class="relative">
                <div class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
                  <User class="w-4 h-4" />
                </div>
                <input
                  v-model="username"
                  type="text"
                  name="eventhub_username_input"
                  id="eventhub_username_input"
                  autocomplete="off"
                  placeholder="vd: admin, organizer, hoặc customer"
                  required
                  class="w-full pl-10 pr-9 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                />
                <button
                  v-if="username"
                  type="button"
                  @click="username = ''; selectedRole = null"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 p-0.5 cursor-pointer"
                  title="Xóa nhanh"
                >
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </div>

            <!-- Password -->
            <div>
              <div class="flex items-center justify-between mb-1.5">
                <label class="block text-xs font-bold text-slate-700">
                  Mật khẩu <span class="text-rose-500">*</span>
                </label>
                <button
                  type="button"
                  @click="showForgotModal = true"
                  class="text-xs font-semibold text-indigo-600 hover:text-indigo-800 transition hover:underline cursor-pointer"
                >
                  Quên mật khẩu?
                </button>
              </div>
              <div class="relative">
                <div class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none">
                  <Lock class="w-4 h-4" />
                </div>
                <input
                  v-model="password"
                  name="eventhub_password_input"
                  id="eventhub_password_input"
                  autocomplete="new-password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  required
                  class="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition cursor-pointer"
                  title="Hiện / Ẩn mật khẩu"
                >
                  <EyeOff v-if="showPassword" class="w-4 h-4" />
                  <Eye v-else class="w-4 h-4" />
                </button>
              </div>
            </div>

            <!-- Remember me checkbox -->
            <div class="flex items-center justify-between pt-1">
              <label class="flex items-center gap-2 cursor-pointer select-none">
                <input
                  v-model="rememberMe"
                  type="checkbox"
                  class="w-4 h-4 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer"
                />
                <span class="text-xs text-slate-600 font-medium">Ghi nhớ đăng nhập trên thiết bị này</span>
              </label>
            </div>

            <!-- Error message alert -->
            <div
              v-if="localError"
              class="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2 font-medium animate-shake"
            >
              <AlertCircle class="w-4 h-4 shrink-0 text-rose-500" />
              <span>{{ localError }}</span>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="authStore.loading"
              class="w-full py-3 px-4 rounded-xl bg-gradient-to-r from-indigo-600 via-indigo-600 to-violet-600 hover:from-indigo-700 hover:to-violet-700 active:scale-[0.99] text-white font-bold text-xs sm:text-sm flex items-center justify-center gap-2 shadow-md shadow-indigo-600/25 transition-all disabled:opacity-50"
            >
              <LogIn v-if="!authStore.loading" class="w-4 h-4" />
              <RefreshCw v-else class="w-4 h-4 animate-spin" />
              <span v-if="!authStore.loading">Đăng Nhập Vào Hệ Thống</span>
              <span v-else>Đang xác thực thông tin...</span>
            </button>
          </form>
        </div>

        <!-- Footer -->
        <div class="text-center mt-6 pt-5 border-t border-slate-100 text-xs text-slate-500">
          Chưa có tài khoản EventHub?
          <router-link to="/register" class="text-indigo-600 font-bold hover:underline ml-1">
            Đăng ký tài khoản mới &rarr;
          </router-link>
        </div>
      </div>
    </div>

    <!-- Forgot Password Modal -->
    <div
      v-if="showForgotModal"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in"
      @click.self="closeForgotModal"
    >
      <div class="relative w-full max-w-md bg-white rounded-3xl p-6 sm:p-8 shadow-2xl border border-slate-200 text-slate-900">
        <button
          @click="closeForgotModal"
          class="absolute top-4 right-4 w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 text-slate-500 flex items-center justify-center transition"
        >
          <X class="w-4 h-4" />
        </button>

        <div v-if="forgotSuccess" class="text-center py-4 space-y-3">
          <div class="w-14 h-14 rounded-full bg-emerald-100 text-emerald-600 mx-auto flex items-center justify-center">
            <CheckCircle2 class="w-8 h-8" />
          </div>
          <h3 class="text-lg font-bold text-slate-900">Đã gửi liên kết khôi phục!</h3>
          <p class="text-xs text-slate-500 leading-relaxed max-w-xs mx-auto">
            Chúng tôi đã gửi hướng dẫn đặt lại mật khẩu đến <strong>{{ forgotEmail }}</strong>. Vui lòng kiểm tra hòm thư của bạn.
          </p>
          <button
            @click="closeForgotModal"
            class="px-5 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm transition"
          >
            Đóng cửa sổ
          </button>
        </div>

        <form v-else @submit.prevent="handleForgotPassword" class="space-y-4">
          <div class="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mb-2">
            <HelpCircle class="w-6 h-6" />
          </div>

          <div>
            <h3 class="text-lg font-bold text-slate-900">Quên Mật Khẩu?</h3>
            <p class="text-xs text-slate-500 mt-0.5 leading-relaxed">
              Nhập địa chỉ email đăng ký tài khoản để nhận liên kết thiết lập lại mật khẩu an toàn.
            </p>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Email đăng ký</label>
            <div class="relative">
              <Mail class="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                v-model="forgotEmail"
                type="email"
                placeholder="email@example.com"
                required
                class="w-full pl-9 pr-3.5 py-2.5 rounded-xl border border-slate-200 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div class="flex items-center justify-end gap-2.5 pt-2">
            <button
              type="button"
              @click="closeForgotModal"
              class="px-4 py-2 rounded-xl border border-slate-200 text-slate-600 text-xs font-bold hover:bg-slate-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              :disabled="isSendingForgot"
              class="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold shadow-sm transition flex items-center gap-1.5"
            >
              <Send v-if="!isSendingForgot" class="w-3.5 h-3.5" />
              <span v-if="!isSendingForgot">Gửi Hướng Dẫn</span>
              <span v-else>Đang gửi...</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
