<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Ticket,
  UserPlus,
  Lock,
  User,
  Mail,
  Phone,
  AlertCircle,
  Eye,
  EyeOff,
  Sparkles,
  ShieldCheck,
  CheckCircle2,
  Gift,
  BellRing,
  RefreshCw,
  ArrowRight,
} from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const fullName = ref('')
const username = ref('')
const email = ref('')
const password = ref('')
const phoneNumber = ref('')
const agreedToTerms = ref(true)
const showPassword = ref(false)
const localError = ref('')

// Password strength calculation
const passwordStrength = computed(() => {
  const p = password.value
  if (!p) return 0
  let score = 0
  if (p.length >= 6) score++
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p) || /[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  if (passwordStrength.value <= 1) return { text: 'Yếu', color: 'text-rose-500', bar: 'bg-rose-500 w-1/4' }
  if (passwordStrength.value === 2) return { text: 'Trung bình', color: 'text-amber-500', bar: 'bg-amber-500 w-2/4' }
  if (passwordStrength.value === 3) return { text: 'Khá', color: 'text-indigo-500', bar: 'bg-indigo-500 w-3/4' }
  return { text: 'Mạnh & An toàn', color: 'text-emerald-500', bar: 'bg-emerald-500 w-full' }
})

const handleRegister = async () => {
  localError.value = ''
  if (!fullName.value.trim() || !username.value.trim() || !email.value.trim() || !password.value.trim()) {
    localError.value = 'Vui lòng điền đầy đủ các trường thông tin bắt buộc (*)'
    return
  }

  if (!agreedToTerms.value) {
    localError.value = 'Vui lòng đồng ý với Điều khoản dịch vụ & Chính sách bảo mật!'
    return
  }

  const success = await authStore.register({
    fullName: fullName.value.trim(),
    username: username.value.trim(),
    email: email.value.trim(),
    password: password.value.trim(),
    phoneNumber: phoneNumber.value.trim() || undefined,
  })

  if (success) {
    router.push('/')
  } else {
    localError.value = authStore.error || 'Đăng ký tài khoản không thành công'
  }
}
</script>

<template>
  <div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4 sm:p-6 lg:p-8 bg-slate-50/70">
    <div class="w-full max-w-5xl bg-white rounded-3xl border border-slate-200/90 shadow-2xl shadow-slate-200/50 overflow-hidden grid grid-cols-1 lg:grid-cols-12 min-h-[680px]">
      
      <!-- Left Column: Perks Showcase (5 Cols on LG) -->
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
            <Gift class="w-3.5 h-3.5 text-amber-300" />
            <span>Đặc quyền thành viên mới</span>
          </div>

          <h2 class="text-2xl xl:text-3xl font-extrabold text-white leading-tight tracking-tight mb-3">
            Tạo tài khoản & Nhận ngay ưu đãi 15%
          </h2>
          <p class="text-xs xl:text-sm text-slate-300 leading-relaxed font-normal">
            Gia nhập cộng đồng người yêu âm nhạc, thể thao và nghệ thuật để nhận thông báo sớm nhất về các đợt mở bán vé Early Bird.
          </p>
        </div>

        <!-- Membership Perks List -->
        <div class="relative z-10 space-y-3.5 my-6">
          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-indigo-500/20 text-indigo-300 flex items-center justify-center shrink-0">
              <BellRing class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Thông báo sớm giờ mở bán</div>
              <div class="text-slate-400 text-[11px]">Không bỏ lỡ vé VIP các concert hot nhất</div>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-emerald-500/20 text-emerald-300 flex items-center justify-center shrink-0">
              <Gift class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Voucher EVENTHUB2026</div>
              <div class="text-slate-400 text-[11px]">Giảm ngay 15% cho đơn hàng đầu tiên</div>
            </div>
          </div>

          <div class="flex items-center gap-3 p-3 rounded-2xl bg-white/5 border border-white/10 backdrop-blur-md">
            <div class="w-8 h-8 rounded-xl bg-amber-500/20 text-amber-300 flex items-center justify-center shrink-0">
              <ShieldCheck class="w-4 h-4" />
            </div>
            <div class="text-xs">
              <div class="font-bold text-white">Ví vé điện tử tập trung</div>
              <div class="text-slate-400 text-[11px]">Lưu trữ và tra cứu lịch sử vé an toàn trọn đời</div>
            </div>
          </div>
        </div>

        <!-- Bottom Link to Login -->
        <div class="relative z-10 pt-4 border-t border-white/10 flex items-center justify-between text-xs text-slate-300">
          <span>Đã có tài khoản?</span>
          <router-link to="/login" class="font-bold text-indigo-400 hover:text-white flex items-center gap-1 transition">
            <span>Đăng nhập ngay</span>
            <ArrowRight class="w-3.5 h-3.5" />
          </router-link>
        </div>
      </div>

      <!-- Right Column: Registration Form (7 Cols on LG) -->
      <div class="lg:col-span-7 p-6 sm:p-10 xl:p-12 flex flex-col justify-between">
        <div>
          <!-- Header -->
          <div class="mb-6">
            <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-indigo-50 text-indigo-700 text-xs font-bold mb-2">
              <UserPlus class="w-3.5 h-3.5" />
              <span>Đăng Ký Tài Khoản Mới</span>
            </div>
            <h2 class="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Bắt Đầu Cùng EventHub
            </h2>
            <p class="text-xs sm:text-sm text-slate-500 mt-1 leading-relaxed">
              Điền thông tin để tạo tài khoản miễn phí và bắt đầu trải nghiệm mua vé tức thì.
            </p>
          </div>

          <!-- Form -->
          <form @submit.prevent="handleRegister" class="space-y-4">
            <!-- Full Name & Username in 2 columns -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">
                  Họ và tên <span class="text-rose-500">*</span>
                </label>
                <div class="relative">
                  <User class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <input
                    v-model="fullName"
                    type="text"
                    placeholder="Nguyễn Văn A"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">
                  Tên đăng nhập <span class="text-rose-500">*</span>
                </label>
                <div class="relative">
                  <User class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <input
                    v-model="username"
                    type="text"
                    placeholder="username_cuaban"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                </div>
              </div>
            </div>

            <!-- Email & Phone in 2 columns -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">
                  Địa chỉ Email <span class="text-rose-500">*</span>
                </label>
                <div class="relative">
                  <Mail class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <input
                    v-model="email"
                    type="email"
                    placeholder="email@example.com"
                    required
                    class="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                </div>
              </div>

              <div>
                <label class="block text-xs font-bold text-slate-700 mb-1">
                  Số điện thoại
                </label>
                <div class="relative">
                  <Phone class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                  <input
                    v-model="phoneNumber"
                    type="tel"
                    placeholder="0987 654 321"
                    class="w-full pl-10 pr-3.5 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                  />
                </div>
              </div>
            </div>

            <!-- Password with Show/Hide and Strength Meter -->
            <div>
              <label class="block text-xs font-bold text-slate-700 mb-1">
                Mật khẩu <span class="text-rose-500">*</span>
              </label>
              <div class="relative">
                <Lock class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 pointer-events-none" />
                <input
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Tối thiểu 6 ký tự"
                  required
                  class="w-full pl-10 pr-10 py-2.5 rounded-xl border border-slate-200 bg-slate-50/60 text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
                >
                  <EyeOff v-if="showPassword" class="w-4 h-4" />
                  <Eye v-else class="w-4 h-4" />
                </button>
              </div>

              <!-- Password Strength indicator -->
              <div v-if="password" class="mt-2 space-y-1">
                <div class="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
                  <div class="h-full transition-all duration-300 rounded-full" :class="strengthLabel.bar"></div>
                </div>
                <div class="flex justify-between text-[11px]">
                  <span class="text-slate-400">Độ an toàn mật khẩu:</span>
                  <span class="font-bold" :class="strengthLabel.color">{{ strengthLabel.text }}</span>
                </div>
              </div>
            </div>

            <!-- Terms Agreement Checkbox -->
            <div class="pt-1">
              <label class="flex items-start gap-2.5 cursor-pointer select-none">
                <input
                  v-model="agreedToTerms"
                  type="checkbox"
                  class="w-4 h-4 mt-0.5 rounded text-indigo-600 focus:ring-indigo-500 border-slate-300 cursor-pointer shrink-0"
                />
                <span class="text-xs text-slate-600 leading-relaxed">
                  Tôi đồng ý với <a href="#" class="text-indigo-600 font-semibold hover:underline">Điều khoản dịch vụ</a> và <a href="#" class="text-indigo-600 font-semibold hover:underline">Chính sách bảo mật</a> của EventHub.
                </span>
              </label>
            </div>

            <!-- Error message alert -->
            <div
              v-if="localError"
              class="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2 font-medium"
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
              <UserPlus v-if="!authStore.loading" class="w-4 h-4" />
              <RefreshCw v-else class="w-4 h-4 animate-spin" />
              <span v-if="!authStore.loading">Đăng Ký Tài Khoản Miễn Phí</span>
              <span v-else>Đang khởi tạo tài khoản...</span>
            </button>
          </form>
        </div>

        <!-- Footer -->
        <div class="text-center mt-6 pt-5 border-t border-slate-100 text-xs text-slate-500">
          Đã có tài khoản EventHub?
          <router-link to="/login" class="text-indigo-600 font-bold hover:underline ml-1">
            Đăng nhập ngay &rarr;
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
