<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Event, Category } from '@/types'
import { eventService } from '@/services/event.service'
import { MOCK_CITIES, MOCK_REVIEWS, MOCK_NEWS } from '@/services/mockData'
import EventCard from '@/components/EventCard.vue'
import HeroSlider from '@/components/HeroSlider.vue'
import MarqueeTicker from '@/components/MarqueeTicker.vue'
import FlashSaleSection from '@/components/FlashSaleSection.vue'
import ArtistLineup from '@/components/ArtistLineup.vue'
import {
  Sparkles,
  ArrowRight,
  Cpu,
  Music,
  Trophy,
  Palette,
  Compass,
  ShieldCheck,
  CheckCircle,
  Copy,
  Star,
  Zap,
  Film,
  Monitor,
} from 'lucide-vue-next'

const router = useRouter()
const allEvents = ref<Event[]>([])
const categories = ref<Category[]>([])
const loading = ref(true)
const activeCategoryTab = ref<number | null>(null)
const copiedCoupon = ref<string | null>(null)

const categoryIcons: Record<string, any> = {
  'am-nhac': Music,
  'cong-nghe': Cpu,
  'the-thao': Trophy,
  'nghe-thuat': Palette,
  'le-hoi': Sparkles,
  'trien-lam': Compass,
}

onMounted(async () => {
  try {
    const [eventsRes, catsRes] = await Promise.all([
      eventService.getEvents({ size: 12 }),
      eventService.getCategories(),
    ])
    allEvents.value = eventsRes.content
    categories.value = catsRes
  } finally {
    loading.value = false
  }
})

// Filtered events by category tab
const displayedEvents = computed(() => {
  if (!activeCategoryTab.value) return allEvents.value
  return allEvents.value.filter((e) => e.category.id === activeCategoryTab.value)
})

// Slideshow events (top 4 featured)
const sliderEvents = computed(() => allEvents.value.slice(0, 4))

const copyCoupon = (code: string) => {
  navigator.clipboard.writeText(code)
  copiedCoupon.value = code
  setTimeout(() => {
    copiedCoupon.value = null
  }, 2500)
}
</script>

<template>
  <div>
    <!-- 1. Live Animated Marquee Ticker -->
    <MarqueeTicker />

    <div class="space-y-10 sm:space-y-12 pb-16 pt-3 sm:pt-4">
      <!-- 2. Hero Interactive Slideshow Carousel (Centerpiece) -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <HeroSlider :events="sliderEvents" />
      </section>

      <!-- 3. Topic Category Pills -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
          <button
            v-for="cat in categories"
            :key="cat.id"
            @click="activeCategoryTab = activeCategoryTab === cat.id ? null : cat.id"
            class="p-4 rounded-2xl border text-center flex flex-col items-center justify-center gap-2 transition-all group"
            :class="activeCategoryTab === cat.id
              ? 'bg-indigo-600 text-white border-indigo-600 shadow-md shadow-indigo-600/30'
              : 'bg-white text-slate-700 border-slate-200 hover:border-indigo-400 hover:shadow-sm'"
          >
            <div
              class="w-11 h-11 rounded-xl flex items-center justify-center transition-colors"
              :class="activeCategoryTab === cat.id ? 'bg-white/20 text-white' : 'bg-indigo-50 text-indigo-600 group-hover:bg-indigo-100'"
            >
              <component :is="categoryIcons[cat.slug] || Sparkles" class="w-5 h-5" />
            </div>
            <span class="text-xs font-bold leading-tight">{{ cat.name }}</span>
          </button>
        </div>
      </section>

      <!-- 4. Flash Sale Early Bird Section with Ticking Clock & Stock Bars -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <FlashSaleSection :events="allEvents" />
      </section>

      <!-- 5. Exclusive Promo Coupons Banner -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="rounded-3xl bg-gradient-to-r from-purple-700 via-indigo-700 to-blue-700 p-6 sm:p-8 text-white shadow-xl relative overflow-hidden">
          <div class="relative z-10 flex flex-col lg:flex-row items-center justify-between gap-6">
            <div class="text-center lg:text-left">
              <span class="inline-block px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-wider bg-white/20 text-white mb-2">
                🎁 MÃ ƯU ĐÃI ĐỘC QUYỀN HÔM NAY
              </span>
              <h3 class="text-2xl sm:text-3xl font-black">Nhận Giảm Giá Vé Ngay Lập Tức</h3>
              <p class="text-xs sm:text-sm text-purple-100 mt-1">
                Nhấp để sao chép mã và dán vào bước thanh toán đơn hàng của bạn.
              </p>
            </div>

            <!-- Coupon Cards -->
            <div class="flex flex-wrap items-center justify-center gap-4">
              <!-- Coupon 1 -->
              <div class="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-4 flex items-center gap-4 shadow-sm">
                <div>
                  <span class="block text-xl font-black text-amber-300">GIẢM 15%</span>
                  <span class="text-[11px] text-white/80">Cho đơn từ 500.000đ</span>
                </div>
                <button
                  @click="copyCoupon('EVENTHUB2026')"
                  class="px-3.5 py-2 rounded-xl bg-white text-indigo-700 font-black text-xs flex items-center gap-1.5 hover:bg-slate-100 transition-colors shadow"
                >
                  <Copy class="w-3.5 h-3.5" />
                  <span>{{ copiedCoupon === 'EVENTHUB2026' ? 'Đã chép!' : 'EVENTHUB2026' }}</span>
                </button>
              </div>

              <!-- Coupon 2 -->
              <div class="bg-white/10 backdrop-blur-md border border-white/20 rounded-2xl p-4 flex items-center gap-4 shadow-sm">
                <div>
                  <span class="block text-xl font-black text-emerald-300">GIẢM 100K</span>
                  <span class="text-[11px] text-white/80">Đơn hàng đầu tiên</span>
                </div>
                <button
                  @click="copyCoupon('WELCOMEVIP')"
                  class="px-3.5 py-2 rounded-xl bg-white text-purple-700 font-black text-xs flex items-center gap-1.5 hover:bg-slate-100 transition-colors shadow"
                >
                  <Copy class="w-3.5 h-3.5" />
                  <span>{{ copiedCoupon === 'WELCOMEVIP' ? 'Đã chép!' : 'WELCOMEVIP' }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 5.5. EventHub Cinema Mega Banner -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="rounded-3xl bg-gradient-to-r from-slate-950 via-slate-900 to-rose-950 p-6 sm:p-8 border border-rose-900/40 text-white shadow-2xl relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-6">
          <div class="space-y-3 max-w-xl text-center md:text-left">
            <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-rose-500/20 text-rose-400 border border-rose-500/30 text-xs font-black uppercase">
              <Film class="w-3.5 h-3.5" />
              <span>Mở Rộng Hệ Thống 2026: EventHub Cinema</span>
            </div>
            <h3 class="text-2xl sm:text-3xl font-black text-white">
              Phim Chiếu Rạp Bom Tấn & Phòng Chiếu IMAX Laser
            </h3>
            <p class="text-xs sm:text-sm text-slate-300 leading-relaxed">
              Đặt vé trực tuyến các rạp Landmark 81 IMAX, Vincom Đồng Khởi & Bà Triệu. Trải nghiệm ghế đôi Sweetbox lãng mạn và đặt trước Bắp Nước F&B ưu đãi 15%!
            </p>
          </div>

          <div class="flex flex-col sm:flex-row items-center gap-3 shrink-0">
            <router-link
              to="/cinema"
              class="px-6 py-3.5 rounded-2xl bg-gradient-to-r from-rose-600 to-pink-600 hover:from-rose-500 hover:to-pink-500 text-white font-black text-sm shadow-lg shadow-rose-600/30 transition flex items-center gap-2 cursor-pointer"
            >
              <Film class="w-4 h-4" />
              <span>Vào Cổng Rạp Phim</span>
              <ArrowRight class="w-4 h-4" />
            </router-link>
          </div>
        </div>
      </section>

      <!-- 6. Main Events Grid with Category Filtering -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-8 pb-3 border-b border-slate-200">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full bg-rose-600 animate-ping"></span>
              <h2 class="text-2xl sm:text-3xl font-black text-slate-900">
                {{ activeCategoryTab ? 'Sự Kiện Theo Danh Mục' : 'Sự Kiện Đang Mở Bán' }}
              </h2>
            </div>
            <p class="text-xs sm:text-sm text-slate-500 mt-1">
              Đặt vé trực tiếp, kiểm soát số lượng theo thời gian thực không lo tranh chấp chỗ
            </p>
          </div>

          <router-link
            to="/events"
            class="text-xs sm:text-sm font-bold text-indigo-600 hover:text-indigo-800 flex items-center gap-1 group"
          >
            Xem tất cả sự kiện ({{ allEvents.length }})
            <ArrowRight class="w-4 h-4 group-hover:translate-x-1 transition-transform" />
          </router-link>
        </div>

        <!-- Events Grid -->
        <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <div v-for="i in 8" :key="i" class="h-80 rounded-2xl bg-slate-200 animate-pulse"></div>
        </div>

        <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <EventCard v-for="evt in displayedEvents" :key="evt.id" :event="evt" />
        </div>
      </section>

      <!-- 7. Artist Lineup & Star Speakers -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <ArtistLineup />
      </section>

      <!-- 8. Explore by Cities -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="mb-8">
          <h2 class="text-2xl sm:text-3xl font-black text-slate-900">Khám Phá Theo Điểm Đến</h2>
          <p class="text-xs sm:text-sm text-slate-500 mt-1">Các sự kiện sôi động nhất tại các trung tâm văn hóa và giải trí</p>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-4 gap-4 sm:gap-6">
          <router-link
            v-for="city in MOCK_CITIES"
            :key="city.id"
            :to="{ path: '/events', query: { keyword: city.name } }"
            class="group relative rounded-2xl overflow-hidden aspect-[4/3] bg-slate-900 shadow-sm border border-slate-200"
          >
            <img
              :src="city.imageUrl"
              :alt="city.name"
              class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500 opacity-80"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent"></div>

            <div class="absolute bottom-3 left-3 right-3 text-white">
              <h3 class="font-black text-sm sm:text-base leading-tight">{{ city.name }}</h3>
              <span class="text-[11px] text-indigo-300 font-semibold">{{ city.eventCount }}+ sự kiện</span>
            </div>
          </router-link>
        </div>
      </section>

      <!-- 9. Platform Guarantees / Why EventHub -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="bg-white rounded-3xl p-8 sm:p-12 border border-slate-200 shadow-sm">
          <div class="text-center max-w-2xl mx-auto mb-10">
            <span class="text-xs uppercase font-extrabold tracking-wider text-indigo-600 block mb-2">
              TIÊU CHUẨN KỸ THUẬT QUỐC TẾ
            </span>
            <h2 class="text-2xl sm:text-3xl font-black text-slate-900">
              Trải Nghiệm Mua Vé An Tâm Tuyệt Đối
            </h2>
            <p class="text-xs sm:text-sm text-slate-500 mt-2">
              Giải quyết triệt để bài toán quá tải, vé giả và tranh chấp ghế thường gặp trên các nền tảng cũ.
            </p>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div class="p-6 rounded-2xl bg-slate-50 border border-slate-200/80">
              <div class="w-12 h-12 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center mb-4">
                <Zap class="w-6 h-6" />
              </div>
              <h4 class="font-bold text-slate-900 text-base mb-2">Chống Trùng Ghế 100%</h4>
              <p class="text-xs text-slate-600 leading-relaxed">
                Áp dụng cơ chế <strong>Pessimistic Write Lock</strong> kết hợp Redis Distributed Locking. Ngay khi bạn chọn ghế, hệ thống khóa giữ chỗ 15 phút an toàn tuyệt đối.
              </p>
            </div>

            <div class="p-6 rounded-2xl bg-slate-50 border border-slate-200/80">
              <div class="w-12 h-12 rounded-xl bg-purple-100 text-purple-600 flex items-center justify-center mb-4">
                <ShieldCheck class="w-6 h-6" />
              </div>
              <h4 class="font-bold text-slate-900 text-base mb-2">Mã QR Anti-Passback</h4>
              <p class="text-xs text-slate-600 leading-relaxed">
                Vé điện tử được mã hóa chuẩn Token SHA-256 duy nhất. Cổng kiểm soát vé tự động hủy lượt quét thứ 2, loại bỏ hoàn toàn nạn bán lại vé quay vòng.
              </p>
            </div>

            <div class="p-6 rounded-2xl bg-slate-50 border border-slate-200/80">
              <div class="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center mb-4">
                <CheckCircle class="w-6 h-6" />
              </div>
              <h4 class="font-bold text-slate-900 text-base mb-2">Thanh Toán Toàn Vẹn</h4>
              <p class="text-xs text-slate-600 leading-relaxed">
                Cơ chế <strong>Idempotency Key</strong> đảm bảo giao dịch ngân hàng / sandbox không bị trừ tiền 2 lần khi đường truyền mạng người dùng chập chờn.
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- 10. Customer Reviews -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="mb-8">
          <h2 class="text-2xl sm:text-3xl font-black text-slate-900">Khán Giả Nói Gì Về EventHub</h2>
          <p class="text-xs sm:text-sm text-slate-500 mt-1">Hơn 50.000 khán giả đã tin tưởng đặt vé trên nền tảng</p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="rev in MOCK_REVIEWS"
            :key="rev.id"
            class="clean-card p-6 rounded-2xl bg-white border border-slate-200 flex flex-col justify-between"
          >
            <div>
              <div class="flex items-center gap-1 text-amber-400 mb-3">
                <Star v-for="s in rev.rating" :key="s" class="w-4 h-4 fill-amber-400" />
              </div>
              <p class="text-xs sm:text-sm text-slate-700 italic leading-relaxed mb-4">
                "{{ rev.comment }}"
              </p>
            </div>

            <div class="flex items-center gap-3 pt-4 border-t border-slate-100">
              <img :src="rev.userAvatar" :alt="rev.userName" class="w-10 h-10 rounded-full object-cover border border-slate-200" />
              <div>
                <h4 class="font-bold text-slate-900 text-xs sm:text-sm">{{ rev.userName }}</h4>
                <p class="text-[11px] text-slate-500">{{ rev.eventTitle }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 11. Blog & Guide -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between mb-8 pb-3 border-b border-slate-200">
          <div>
            <h2 class="text-2xl sm:text-3xl font-black text-slate-900">Cẩm Nang & Xu Hướng Sự Kiện</h2>
            <p class="text-xs sm:text-sm text-slate-500 mt-1">Kinh nghiệm săn vé, mẹo chọn ghế đẹp và lịch trình nghệ sĩ</p>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="news in MOCK_NEWS"
            :key="news.id"
            class="clean-card rounded-2xl overflow-hidden bg-white border border-slate-200 flex flex-col group cursor-pointer"
          >
            <div class="aspect-video w-full overflow-hidden bg-slate-100">
              <img :src="news.imageUrl" :alt="news.title" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
            </div>
            <div class="p-5 flex-1 flex flex-col justify-between">
              <div>
                <div class="flex items-center justify-between text-[11px] text-slate-400 mb-2 font-medium">
                  <span class="text-indigo-600 font-bold uppercase">{{ news.category }}</span>
                  <span>{{ news.readTime }}</span>
                </div>
                <h3 class="font-bold text-slate-900 text-sm leading-snug group-hover:text-indigo-600 transition-colors mb-2">
                  {{ news.title }}
                </h3>
                <p class="text-xs text-slate-500 line-clamp-2 leading-relaxed">
                  {{ news.summary }}
                </p>
              </div>
              <div class="mt-4 pt-3 border-t border-slate-100 text-[11px] text-slate-400">
                Đăng ngày {{ news.date }}
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 12. Early Bird Newsletter CTA -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="rounded-3xl bg-slate-900 p-8 sm:p-12 text-white text-center relative overflow-hidden">
          <div class="max-w-2xl mx-auto relative z-10">
            <span class="px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider bg-white/20 text-white mb-4 inline-block">
              📬 ĐĂNG KÝ NHẬN THÔNG BÁO VÉ SỚM (EARLY BIRD)
            </span>
            <h2 class="text-2xl sm:text-4xl font-black mb-3">Không Bỏ Lỡ Bất Kỳ Concert Nào</h2>
            <p class="text-xs sm:text-sm text-slate-300 mb-8">
              Nhận thông báo mở bán vé trước 24 giờ và mã giảm giá độc quyền dành riêng cho thành viên EventHub.
            </p>

            <form @submit.prevent class="flex flex-col sm:flex-row gap-3 max-w-md mx-auto">
              <input
                type="email"
                placeholder="Nhập địa chỉ email của bạn..."
                class="flex-1 px-4 py-3 rounded-xl bg-slate-800 border border-slate-700 text-sm text-white placeholder-slate-400 focus:outline-none focus:border-indigo-500"
              />
              <button
                type="submit"
                class="px-6 py-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 font-bold text-sm text-white shadow-lg transition-colors shrink-0"
              >
                Đăng Ký Ngay
              </button>
            </form>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>
