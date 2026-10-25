<script setup lang="ts">
import { ref } from 'vue'
import { Play, ArrowRight, ArrowUp, Send } from 'lucide-vue-next'

const emailInput = ref('')
const subscribed = ref(false)

const handleSubscribe = () => {
  if (emailInput.value.trim()) {
    subscribed.value = true
    setTimeout(() => {
      subscribed.value = false
      emailInput.value = ''
    }, 3000)
  }
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}
</script>

<template>
  <footer id="about" class="border-t border-[#1A2338] bg-[#070A12] text-slate-400 pt-16 pb-8">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-12">
      <!-- Top Grid: 4 Columns -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 lg:gap-10">
        <!-- Col 1: Brand Info (2 cols on large) -->
        <div class="lg:col-span-2 space-y-4">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-xl bg-blue-600 flex items-center justify-center text-white shadow-md shadow-blue-500/30">
              <Play class="w-4 h-4 fill-white ml-0.5" />
            </div>
            <div class="flex flex-col">
              <span class="text-lg font-black text-white leading-tight">
                Cine<span class="text-blue-500">Verse</span>
              </span>
              <span class="text-[9px] font-bold text-slate-500 tracking-wider uppercase">
                Movies • Events • More
              </span>
            </div>
          </div>
          <p class="text-xs text-slate-400 leading-relaxed max-w-sm">
            Nền tảng đặt vé xem phim chiếu rạp và sự kiện giải trí trực tuyến hàng đầu Việt Nam. Kết nối hàng triệu khán giả với trải nghiệm điện ảnh đỉnh cao và concert bùng nổ.
          </p>
          <p class="text-xs text-slate-500">
            Hotline: <strong class="text-slate-300">1900 6868</strong> • Email: <strong class="text-slate-300">support@cineverse.vn</strong>
          </p>
        </div>

        <!-- Col 2: Về CineVerse -->
        <div class="space-y-3">
          <h4 class="text-xs font-bold text-white uppercase tracking-wider">Khám Phá CineVerse</h4>
          <ul class="space-y-2 text-xs text-slate-400">
            <li><router-link to="/about" class="hover:text-blue-400 transition-colors">Về chúng tôi & Sứ mệnh</router-link></li>
            <li><router-link to="/promotions" class="hover:text-blue-400 transition-colors">Kho ưu đãi & Mã giảm giá</router-link></li>
            <li><router-link to="/news" class="hover:text-blue-400 transition-colors">Tin tức điện ảnh & Sự kiện</router-link></li>
            <li><router-link to="/cinema" class="hover:text-blue-400 transition-colors">Lịch chiếu rạp toàn quốc</router-link></li>
          </ul>
        </div>

        <!-- Col 3: Hỗ trợ khách hàng -->
        <div class="space-y-3">
          <h4 class="text-xs font-bold text-white uppercase tracking-wider">Hỗ Trợ Khách Hàng</h4>
          <ul class="space-y-2 text-xs text-slate-400">
            <li><a href="#" class="hover:text-blue-400 transition-colors">Câu hỏi thường gặp</a></li>
            <li><a href="#" class="hover:text-blue-400 transition-colors">Chính sách bảo mật</a></li>
            <li><a href="#" class="hover:text-blue-400 transition-colors">Điều khoản sử dụng</a></li>
            <li><a href="#" class="hover:text-blue-400 transition-colors">Chính sách đổi trả & hoàn tiền</a></li>
          </ul>
        </div>

        <!-- Col 4: Kết nối & Đăng ký nhận tin -->
        <div class="space-y-4">
          <div class="space-y-2">
            <h4 class="text-xs font-bold text-white uppercase tracking-wider">Kết Nối Với Chúng Tôi</h4>
            <div class="flex items-center gap-2">
              <a href="#" class="w-8 h-8 rounded-full bg-[#111827] border border-[#1E293D] flex items-center justify-center text-xs font-bold text-slate-300 hover:text-white hover:border-blue-500 hover:bg-blue-600 transition">f</a>
              <a href="#" class="w-8 h-8 rounded-full bg-[#111827] border border-[#1E293D] flex items-center justify-center text-xs font-bold text-slate-300 hover:text-white hover:border-blue-500 hover:bg-blue-600 transition">ig</a>
              <a href="#" class="w-8 h-8 rounded-full bg-[#111827] border border-[#1E293D] flex items-center justify-center text-xs font-bold text-slate-300 hover:text-white hover:border-blue-500 hover:bg-blue-600 transition">yt</a>
              <a href="#" class="w-8 h-8 rounded-full bg-[#111827] border border-[#1E293D] flex items-center justify-center text-xs font-bold text-slate-300 hover:text-white hover:border-blue-500 hover:bg-blue-600 transition">tt</a>
              <a href="#" class="w-8 h-8 rounded-full bg-[#111827] border border-[#1E293D] flex items-center justify-center text-xs font-bold text-slate-300 hover:text-white hover:border-blue-500 hover:bg-blue-600 transition">zl</a>
            </div>
          </div>

          <!-- Subscribe newsletter -->
          <div class="space-y-2">
            <h4 class="text-xs font-bold text-white uppercase tracking-wider">Đăng Ký Nhận Tin</h4>
            <p class="text-[11px] text-slate-500">Nhận thông tin khuyến mãi, suất chiếu sớm & sự kiện mới nhất</p>
            <form @submit.prevent="handleSubscribe" class="flex items-center gap-2">
              <input
                v-model="emailInput"
                type="email"
                placeholder="Nhập email của bạn..."
                class="flex-1 px-3 py-2 rounded-xl bg-[#111827] border border-[#1E293D] text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500"
              />
              <button
                type="submit"
                class="px-3.5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition flex items-center justify-center shrink-0 cursor-pointer shadow-md shadow-blue-600/30"
              >
                <Send class="w-3.5 h-3.5" />
              </button>
            </form>
            <p v-if="subscribed" class="text-[11px] text-emerald-400 font-bold">
              ✓ Đã đăng ký nhận tin thành công!
            </p>
          </div>
        </div>
      </div>

      <!-- Bottom Bar matching Image 2 -->
      <div class="pt-8 border-t border-slate-900 flex flex-col md:flex-row items-center justify-between gap-4 text-xs text-slate-500">
        <div class="flex flex-wrap items-center gap-5 sm:gap-6">
          <router-link to="/about" class="text-slate-400 hover:text-blue-400 transition-colors">Về chúng tôi</router-link>
          <a href="#" class="text-slate-400 hover:text-blue-400 transition-colors">Điều khoản sử dụng</a>
          <a href="#" class="text-slate-400 hover:text-blue-400 transition-colors">Chính sách bảo mật</a>
          <a href="#" class="text-slate-400 hover:text-blue-400 transition-colors">Liên hệ</a>
        </div>

        <div class="flex items-center gap-6">
          <p class="text-slate-500">© 2026 CineVerse & EventHub. All rights reserved.</p>
          <!-- Back to top button -->
          <button
            type="button"
            @click="scrollToTop"
            class="flex items-center gap-1.5 text-slate-400 hover:text-blue-400 font-bold transition cursor-pointer"
          >
            <span>Lên đầu trang</span>
            <ArrowUp class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  </footer>
</template>
