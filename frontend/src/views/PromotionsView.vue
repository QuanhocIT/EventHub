<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  TicketPercent,
  Copy,
  Check,
  Sparkles,
  Film,
  Compass,
  CreditCard,
  Gift,
  Clock,
  ArrowRight,
  ShieldCheck,
  HelpCircle,
  Percent,
  Flame,
  Search,
  CheckCircle2,
  Tag,
  Zap,
  ChevronRight,
  AlertCircle,
  Info,
} from 'lucide-vue-next'

const router = useRouter()

interface PromoVoucher {
  id: string
  code: string
  title: string
  description: string
  discount: string
  discountValue: string
  discountUnit: string
  type: 'cinema' | 'event' | 'new_user' | 'payment'
  minOrder: string
  maxDiscount?: string
  expiryDate: string
  usedPercent: number
  featured?: boolean
  badge?: string
  image?: string
}

const copiedCode = ref<string | null>(null)
const selectedCategory = ref<'all' | 'cinema' | 'event' | 'new_user' | 'payment'>('all')
const searchKeyword = ref('')

// Toast notifications
const toastMessage = ref<string | null>(null)
const toastType = ref<'success' | 'info'>('success')
let toastTimer: any = null

const showToast = (msg: string, type: 'success' | 'info' = 'success') => {
  if (toastTimer) clearTimeout(toastTimer)
  toastMessage.value = msg
  toastType.value = type
  toastTimer = setTimeout(() => {
    toastMessage.value = null
  }, 3000)
}

const categories = [
  { id: 'all', label: 'Tất Cả Ưu Đãi', icon: Sparkles },
  { id: 'cinema', label: 'Vé Phim Rạp', icon: Film },
  { id: 'event', label: 'Concert & Sự Kiện', icon: Compass },
  { id: 'new_user', label: 'Thành Viên Mới', icon: Gift },
  { id: 'payment', label: 'Thanh Toán VietQR', icon: CreditCard },
]

const vouchers = ref<PromoVoucher[]>([
  {
    id: '1',
    code: 'EVENTHUB2026',
    title: 'Đại Tiệc Khởi Sắc - Giảm 15% Vé Sự Kiện & Concert',
    description: 'Áp dụng cho tất cả các đại nhạc hội, giải chạy marathon và triển lãm nghệ thuật trên toàn quốc.',
    discount: 'GIẢM 15%',
    discountValue: '15%',
    discountUnit: 'GIẢM TRỰC TIẾP',
    type: 'event',
    minOrder: '200.000 đ',
    maxDiscount: '150.000 đ',
    expiryDate: '31/12/2026',
    usedPercent: 68,
    featured: true,
    badge: 'HOT CONCERT',
    image: '/images/about_hero_concert.jpg',
  },
  {
    id: '2',
    code: 'CINEVIP50',
    title: 'Thứ Tư Vui Vẻ - Giảm Ngay 50.000đ Vé Phim Rạp',
    description: 'Áp dụng cho các suất chiếu rạp 2D/3D/IMAX trong hệ thống đối tác CGV, Lotte, BHD, Galaxy.',
    discount: 'GIẢM 50K',
    discountValue: '50K',
    discountUnit: 'GIẢM TIỀN MẶT',
    type: 'cinema',
    minOrder: '120.000 đ',
    expiryDate: '30/11/2026',
    usedPercent: 82,
    featured: true,
    badge: 'CINEMA DEAL',
    image: '/images/about_hero_cinema.jpg',
  },
  {
    id: '3',
    code: 'WELCOMEVIP',
    title: 'Chào Bạn Mới - Giảm 100.000đ Cho Lần Đầu Đặt Vé',
    description: 'Dành riêng cho khách hàng đăng ký tài khoản mới khi đặt vé xem phim hoặc mua vé concert lần đầu.',
    discount: 'GIẢM 100K',
    discountValue: '100K',
    discountUnit: 'TÂN THỦ ĐẶC QUYỀN',
    type: 'new_user',
    minOrder: '300.000 đ',
    expiryDate: '31/12/2026',
    usedPercent: 45,
    featured: true,
    badge: 'DÀNH CHO BẠN MỚI',
    image: '/images/promo_member.jpg',
  },
  {
    id: '4',
    code: 'VIETQR10',
    title: 'Hoàn Tiền 10% Khi Quét Mã VietQR / Mobile Banking',
    description: 'Nhận chiết khấu trực tiếp vào hóa đơn khi thanh toán qua cổng quét mã VietQR tự động 24/7.',
    discount: 'GIẢM 10%',
    discountValue: '10%',
    discountUnit: 'HOÀN TIỀN TỨC THÌ',
    type: 'payment',
    minOrder: '150.000 đ',
    maxDiscount: '50.000 đ',
    expiryDate: '31/12/2026',
    usedPercent: 91,
    badge: 'VIETQR AUTO',
    image: '/images/promo_popcorn.jpg',
  },
  {
    id: '5',
    code: 'EARLYBIRD10',
    title: 'Early Bird Pass - Giảm 10% Cho Khán Giả Đặt Chỗ Sớm',
    description: 'Ưu đãi dành cho người hâm mộ giữ chỗ sớm trước ngày diễn ra sự kiện từ 14 ngày trở lên.',
    discount: 'GIẢM 10%',
    discountValue: '10%',
    discountUnit: 'ĐẶT CHỖ SỚM',
    type: 'event',
    minOrder: '250.000 đ',
    maxDiscount: '100.000 đ',
    expiryDate: '15/12/2026',
    usedPercent: 35,
    badge: 'SỰ KIỆN',
    image: '/images/event_blackpink.jpg',
  },
  {
    id: '6',
    code: 'POPCORNFREE',
    title: 'Tặng Combo Bắp Nước Khi Mua Cặp Vé Chiếu Rạp',
    description: 'Áp dụng khi mua từ 2 vé xem phim trở lên tại các cụm rạp đối tác đối với các phim bom tấn đang chiếu.',
    discount: 'FREE COMBO',
    discountValue: 'FREE',
    discountUnit: 'BẮP CARAMEL & COLA',
    type: 'cinema',
    minOrder: '200.000 đ',
    expiryDate: '30/11/2026',
    usedPercent: 77,
    badge: 'QUÀ TẶNG RẠP',
    image: '/images/promo_popcorn.jpg',
  },
])

const filteredVouchers = computed(() => {
  return vouchers.value.filter((v) => {
    const matchCategory = selectedCategory.value === 'all' || v.type === selectedCategory.value
    const kw = searchKeyword.value.trim().toLowerCase()
    const matchKw =
      !kw ||
      v.code.toLowerCase().includes(kw) ||
      v.title.toLowerCase().includes(kw) ||
      v.description.toLowerCase().includes(kw)
    return matchCategory && matchKw
  })
})

const copyToClipboard = async (code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    copiedCode.value = code
    showToast(`Đã sao chép mã "${code}" thành công! Dán vào bước thanh toán để nhận giảm giá.`)
    setTimeout(() => {
      copiedCode.value = null
    }, 3000)
  } catch (err) {
    showToast(`Mã ưu đãi: ${code}`, 'info')
  }
}

const handleUsePromo = (type: string) => {
  if (type === 'cinema') {
    router.push('/cinema')
  } else {
    router.push('/events')
  }
}

const getCategoryCount = (type: string) => {
  if (type === 'all') return vouchers.value.length
  return vouchers.value.filter((v) => v.type === type).length
}
</script>

<template>
  <div class="min-h-screen bg-[#070A12] text-slate-100 py-6 sm:py-10 selection:bg-rose-600 selection:text-white">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10 sm:space-y-12">

      <!-- ================= HERO SHOWCASE VOUCHER HUB ================= -->
      <section class="relative overflow-hidden rounded-[2rem] bg-gradient-to-br from-[#0E1528] via-[#0A1020] to-[#080C16] border border-white/[0.08] p-6 sm:p-10 lg:p-12 shadow-2xl">
        <!-- Ambient Radial Glows -->
        <div class="absolute -top-32 -left-32 w-[500px] h-[500px] bg-rose-600/15 rounded-full blur-[130px] pointer-events-none"></div>
        <div class="absolute -bottom-32 -right-32 w-[500px] h-[500px] bg-indigo-600/20 rounded-full blur-[130px] pointer-events-none"></div>
        <div class="absolute inset-0 bg-radial-grid opacity-30 pointer-events-none"></div>

        <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 lg:gap-12 items-center relative z-10">
          <!-- Left: Hero Text Content -->
          <div class="lg:col-span-7 space-y-5">
            <!-- Badge -->
            <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-rose-500/15 border border-rose-500/30 text-rose-400 text-xs font-black uppercase tracking-wider">
              <Sparkles class="w-3.5 h-3.5 animate-spin-slow" />
              <span>Kho Ưu Đãi & Khuyến Mãi 2026</span>
            </div>

            <!-- Title -->
            <h1 class="text-3xl sm:text-4xl lg:text-5xl font-black text-white tracking-tight leading-[1.15]">
              Săn Mã Giảm Giá &amp; <br />
              <span class="text-transparent bg-clip-text bg-gradient-to-r from-rose-400 via-pink-300 to-indigo-400 shimmer-text">
                Đặc Quyền Vé Giải Trí
              </span>
            </h1>

            <!-- Description -->
            <p class="text-sm sm:text-base text-slate-300/90 leading-relaxed max-w-xl">
              Khám phá kho voucher độc quyền vé xem phim bom tấn, vé đại nhạc hội concert và chiết khấu thanh toán VietQR tự động. Sao chép mã 1-chạm và tận hưởng ưu đãi ngay hôm nay!
            </p>

            <!-- Trust Badges -->
            <div class="flex flex-wrap items-center gap-4 pt-1 text-xs text-slate-300">
              <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white/[0.04] border border-white/10 backdrop-blur">
                <ShieldCheck class="w-4 h-4 text-emerald-400 shrink-0" />
                <span>100% Áp dụng thành công</span>
              </div>
              <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white/[0.04] border border-white/10 backdrop-blur">
                <Clock class="w-4 h-4 text-amber-400 shrink-0" />
                <span>Cập nhật liên tục mỗi ngày</span>
              </div>
              <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-white/[0.04] border border-white/10 backdrop-blur">
                <Zap class="w-4 h-4 text-cyan-400 shrink-0" />
                <span>Trừ tiền tức thì tại thanh toán</span>
              </div>
            </div>
          </div>

          <!-- Right: Golden Hologram VIP Ticket Card -->
          <div class="lg:col-span-5 flex justify-center lg:justify-end">
            <div class="relative w-full max-w-[380px] rounded-2xl overflow-hidden p-6 bg-gradient-to-br from-[#1C172E] via-[#12162A] to-[#0A0E1C] border border-rose-500/40 shadow-2xl shadow-rose-900/30 group hover:scale-[1.02] transition-transform duration-300">
              <!-- Golden ticket shimmer header -->
              <div class="flex items-center justify-between pb-4 border-b border-white/10">
                <div class="flex items-center gap-2">
                  <div class="w-8 h-8 rounded-lg bg-gradient-to-tr from-rose-500 to-indigo-600 flex items-center justify-center text-white font-black text-xs shadow-md">
                    <TicketPercent class="w-4 h-4" />
                  </div>
                  <div>
                    <p class="text-[10px] font-black uppercase tracking-widest text-rose-400">GOLDEN PASS 2026</p>
                    <p class="text-xs font-bold text-white">CineVerse × EventHub</p>
                  </div>
                </div>
                <span class="px-2.5 py-0.5 rounded-full text-[10px] font-black bg-amber-400/20 text-amber-300 border border-amber-400/30">
                  SIÊU DEAL
                </span>
              </div>

              <!-- Main discount display -->
              <div class="py-5 text-center space-y-1">
                <p class="text-xs font-semibold uppercase tracking-wider text-slate-400">ƯU ĐÃI ĐẶC QUYỀN HỘI VIÊN</p>
                <div class="text-4xl sm:text-5xl font-black text-transparent bg-clip-text bg-gradient-to-r from-amber-300 via-rose-300 to-pink-400 tracking-tight">
                  GIẢM 20%
                </div>
                <p class="text-xs text-slate-300">Áp dụng cho mọi đơn đặt vé từ 300.000đ</p>
              </div>

              <!-- Ticket Code & Copy Action -->
              <div class="p-3 rounded-xl bg-slate-950/80 border border-dashed border-rose-400/40 flex items-center justify-between gap-3">
                <div class="min-w-0">
                  <p class="text-[10px] text-slate-400 uppercase font-mono">Mã giảm giá độc quyền</p>
                  <p class="text-sm font-black font-mono text-rose-400 tracking-wider truncate">CINEVERSE2026</p>
                </div>
                <button
                  type="button"
                  @click="copyToClipboard('CINEVERSE2026')"
                  class="px-3.5 py-2 rounded-lg bg-gradient-to-r from-rose-600 to-indigo-600 hover:from-rose-500 hover:to-indigo-500 text-white font-bold text-xs flex items-center gap-1.5 transition cursor-pointer shadow-md shadow-rose-600/30 shrink-0"
                >
                  <Check v-if="copiedCode === 'CINEVERSE2026'" class="w-3.5 h-3.5 text-emerald-300" />
                  <Copy v-else class="w-3.5 h-3.5" />
                  <span>{{ copiedCode === 'CINEVERSE2026' ? 'Đã chép' : 'Chép mã' }}</span>
                </button>
              </div>

              <!-- Ticket notches (perforated cutouts) -->
              <div class="absolute -left-3 top-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-[#070A12] border-r border-rose-500/40"></div>
              <div class="absolute -right-3 top-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-[#070A12] border-l border-rose-500/40"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- ================= SEARCH & CATEGORY FILTER TABS ================= -->
      <div class="space-y-4">
        <!-- Search and count bar -->
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <span class="text-sm font-bold text-white">Danh mục khuyến mãi</span>
            <span class="px-2 py-0.5 rounded-full text-xs font-black bg-rose-500/20 text-rose-400 border border-rose-500/30">
              {{ filteredVouchers.length }} ưu đãi khả dụng
            </span>
          </div>

          <!-- Quick search input -->
          <div class="relative w-full sm:w-72">
            <Search class="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="Tìm mã hoặc tên ưu đãi..."
              class="w-full pl-10 pr-4 py-2 rounded-xl bg-slate-900/90 border border-slate-800 text-xs text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-rose-500 focus:border-rose-500 transition"
            />
          </div>
        </div>

        <!-- Category tabs -->
        <div class="flex items-center gap-2 sm:gap-2.5 overflow-x-auto pb-2 scrollbar-none">
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            @click="selectedCategory = cat.id as any"
            class="flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-bold whitespace-nowrap transition-all duration-300 cursor-pointer border"
            :class="
              selectedCategory === cat.id
                ? 'bg-gradient-to-r from-rose-600 to-indigo-600 text-white border-rose-500 shadow-lg shadow-rose-600/30 scale-[1.02]'
                : 'bg-slate-900/80 text-slate-400 border-slate-800 hover:text-white hover:bg-slate-800 hover:border-slate-700'
            "
          >
            <component :is="cat.icon" class="w-4 h-4" />
            <span>{{ cat.label }}</span>
            <span
              class="px-1.5 py-0.2 rounded-full text-[10px] font-black"
              :class="selectedCategory === cat.id ? 'bg-white/20 text-white' : 'bg-slate-800 text-slate-400'"
            >
              {{ getCategoryCount(cat.id) }}
            </span>
          </button>
        </div>
      </div>

      <!-- ================= VOUCHERS GRID (PERFORATED TICKET STYLE) ================= -->
      <TransitionGroup
        name="voucher-grid"
        tag="div"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
      >
        <div
          v-for="v in filteredVouchers"
          :key="v.id"
          class="group relative flex flex-col justify-between rounded-3xl bg-[#0D121F] border border-[#1C2538] hover:border-rose-500/50 transition-all duration-300 hover:-translate-y-1 hover:shadow-2xl hover:shadow-rose-500/10 overflow-hidden"
        >
          <!-- Top Card Header: Discount Badge & Image Thumbnail -->
          <div class="relative p-6 space-y-4">
            <!-- Background Image Blur Peek -->
            <div v-if="v.image" class="absolute top-0 right-0 w-32 h-32 overflow-hidden opacity-15 pointer-events-none rounded-bl-3xl">
              <img :src="v.image" :alt="v.title" class="w-full h-full object-cover" />
            </div>

            <!-- Header Badges -->
            <div class="flex items-start justify-between gap-3 relative z-10">
              <div class="flex flex-wrap items-center gap-1.5">
                <span
                  class="px-2.5 py-1 rounded-xl text-[10px] font-black uppercase tracking-wider"
                  :class="[
                    v.type === 'cinema' ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30' :
                    v.type === 'event' ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30' :
                    v.type === 'payment' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' :
                    'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                  ]"
                >
                  {{ v.badge || (v.type === 'cinema' ? 'Vé Phim Rạp' : 'Concert') }}
                </span>
                <span v-if="v.featured" class="px-2 py-0.5 rounded-lg text-[10px] font-black bg-amber-500/20 text-amber-300 border border-amber-500/30">
                  ★ HOT DEAL
                </span>
              </div>

              <!-- Discount Value Highlight -->
              <div class="text-right shrink-0">
                <span class="text-2xl sm:text-3xl font-black text-amber-400 tracking-tight block">
                  {{ v.discount }}
                </span>
                <span class="text-[9px] font-bold text-slate-400 uppercase tracking-widest block">
                  {{ v.discountUnit }}
                </span>
              </div>
            </div>

            <!-- Title & Description -->
            <div class="relative z-10">
              <h3 class="text-base sm:text-lg font-bold text-white group-hover:text-rose-400 transition-colors line-clamp-1">
                {{ v.title }}
              </h3>
              <p class="text-xs text-slate-400 mt-1.5 line-clamp-2 leading-relaxed">
                {{ v.description }}
              </p>
            </div>

            <!-- Rules & Expiry Meta Table -->
            <div class="pt-3 border-t border-slate-800/80 space-y-1.5 text-xs text-slate-400 relative z-10">
              <div class="flex items-center justify-between">
                <span>Đơn tối thiểu:</span>
                <span class="font-bold text-slate-200">{{ v.minOrder }}</span>
              </div>
              <div v-if="v.maxDiscount" class="flex items-center justify-between">
                <span>Giảm tối đa:</span>
                <span class="font-bold text-slate-200">{{ v.maxDiscount }}</span>
              </div>
              <div class="flex items-center justify-between">
                <span>Hạn sử dụng:</span>
                <span class="text-amber-400 font-semibold">{{ v.expiryDate }}</span>
              </div>
            </div>

            <!-- Quota Remaining Progress Bar -->
            <div class="space-y-1 pt-1 relative z-10">
              <div class="flex items-center justify-between text-[10px] text-slate-400">
                <span>Đã sử dụng:</span>
                <span class="font-bold" :class="v.usedPercent > 80 ? 'text-rose-400' : 'text-blue-400'">
                  {{ v.usedPercent }}%
                </span>
              </div>
              <div class="w-full h-1.5 rounded-full bg-slate-800 overflow-hidden">
                <div
                  class="h-full rounded-full transition-all duration-500"
                  :class="[
                    v.usedPercent > 80
                      ? 'bg-gradient-to-r from-amber-500 to-rose-500'
                      : 'bg-gradient-to-r from-blue-500 to-indigo-500'
                  ]"
                  :style="{ width: `${v.usedPercent}%` }"
                ></div>
              </div>
            </div>
          </div>

          <!-- Ticket Cutout Notches at divider line -->
          <div class="relative w-full h-4">
            <div class="absolute left-0 right-0 top-1/2 -translate-y-1/2 border-t border-dashed border-slate-800"></div>
            <div class="absolute -left-3 top-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-[#070A12] border-r border-[#1C2538]"></div>
            <div class="absolute -right-3 top-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-[#070A12] border-l border-[#1C2538]"></div>
          </div>

          <!-- Bottom Action Area: Copy Code & Direct CTA -->
          <div class="p-4 sm:p-5 bg-slate-900/70 border-t border-slate-800/80 flex items-center justify-between gap-3">
            <!-- Coupon Code Container -->
            <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-[#070A12] border border-dashed border-rose-500/40">
              <span class="font-mono font-black text-xs sm:text-sm text-rose-400 tracking-wider">
                {{ v.code }}
              </span>
              <button
                type="button"
                @click="copyToClipboard(v.code)"
                class="p-1 text-slate-400 hover:text-white transition cursor-pointer"
                :title="copiedCode === v.code ? 'Đã sao chép' : 'Sao chép mã'"
              >
                <Check v-if="copiedCode === v.code" class="w-4 h-4 text-emerald-400" />
                <Copy v-else class="w-4 h-4" />
              </button>
            </div>

            <!-- Use Now Button -->
            <button
              type="button"
              @click="handleUsePromo(v.type)"
              class="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-gradient-to-r from-rose-600 to-indigo-600 hover:from-rose-500 hover:to-indigo-500 text-white font-bold text-xs transition cursor-pointer shadow-md shadow-rose-600/30 hover:scale-105 active:scale-95"
            >
              <span>Dùng ngay</span>
              <ArrowRight class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </TransitionGroup>

      <!-- Empty state if search keyword doesn't match -->
      <div
        v-if="filteredVouchers.length === 0"
        class="text-center py-16 px-4 bg-[#0D121F] rounded-3xl border border-slate-800 space-y-3"
      >
        <Search class="w-10 h-10 text-slate-500 mx-auto" />
        <h4 class="text-base font-bold text-white">Không tìm thấy mã ưu đãi nào</h4>
        <p class="text-xs text-slate-400">Hãy thử tìm với từ khóa khác hoặc bấm xem tất cả danh mục.</p>
        <button
          type="button"
          @click="searchKeyword = ''; selectedCategory = 'all'"
          class="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white text-xs font-bold transition cursor-pointer"
        >
          Đặt lại bộ lọc
        </button>
      </div>

      <!-- ================= HOW TO USE VOUCHER (3 STEPS) ================= -->
      <section class="rounded-3xl bg-gradient-to-br from-[#0D121F] to-[#0A0E1A] border border-white/[0.08] p-6 sm:p-8 lg:p-10 space-y-8 shadow-xl">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-rose-600/20 text-rose-400 flex items-center justify-center font-bold shadow-md shadow-rose-600/20">
            <HelpCircle class="w-5 h-5" />
          </div>
          <div>
            <h3 class="text-lg sm:text-xl font-black text-white">Cách Sử Dụng Mã Khuyến Mãi</h3>
            <p class="text-xs text-slate-400 mt-0.5">Chỉ 3 bước đơn giản để nhận chiết khấu trực tiếp khi đặt vé</p>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
          <!-- Step 1 -->
          <div class="group p-5 rounded-2xl bg-slate-900/60 border border-slate-800 hover:border-rose-500/40 transition space-y-3">
            <div class="w-8 h-8 rounded-xl bg-rose-600 text-white font-black text-xs flex items-center justify-center shadow-md shadow-rose-600/30">
              1
            </div>
            <h4 class="text-sm font-bold text-white group-hover:text-rose-400 transition-colors">Sao Chép Mã Ưu Đãi</h4>
            <p class="text-xs text-slate-400 leading-relaxed">
              Nhấn nút copy cạnh mã giảm giá bạn mong muốn trong danh sách kho ưu đãi ở trên.
            </p>
          </div>

          <!-- Step 2 -->
          <div class="group p-5 rounded-2xl bg-slate-900/60 border border-slate-800 hover:border-indigo-500/40 transition space-y-3">
            <div class="w-8 h-8 rounded-xl bg-indigo-600 text-white font-black text-xs flex items-center justify-center shadow-md shadow-indigo-600/30">
              2
            </div>
            <h4 class="text-sm font-bold text-white group-hover:text-indigo-400 transition-colors">Chọn Suất Phim &amp; Sự Kiện</h4>
            <p class="text-xs text-slate-400 leading-relaxed">
              Duyệt qua danh mục phim bom tấn hoặc các concert yêu thích và tiến hành chọn ghế đặt vé.
            </p>
          </div>

          <!-- Step 3 -->
          <div class="group p-5 rounded-2xl bg-slate-900/60 border border-slate-800 hover:border-emerald-500/40 transition space-y-3">
            <div class="w-8 h-8 rounded-xl bg-emerald-600 text-white font-black text-xs flex items-center justify-center shadow-md shadow-emerald-600/30">
              3
            </div>
            <h4 class="text-sm font-bold text-white group-hover:text-emerald-400 transition-colors">Dán Mã Tại Bước Thanh Toán</h4>
            <p class="text-xs text-slate-400 leading-relaxed">
              Dán mã vào ô "Mã giảm giá" tại trang xác nhận thanh toán để hệ thống tự động trừ tiền ngay lập tức.
            </p>
          </div>
        </div>
      </section>

      <!-- ================= VIP REWARDS BANNER ================= -->
      <section class="rounded-3xl p-6 sm:p-8 bg-gradient-to-r from-rose-950/40 via-purple-950/30 to-indigo-950/40 border border-rose-500/20 flex flex-col sm:flex-row items-center justify-between gap-6 shadow-xl">
        <div class="space-y-1.5 text-center sm:text-left">
          <span class="text-xs font-black uppercase tracking-wider text-rose-400">ĐẶC QUYỀN HỘI VIÊN CINEVERSE</span>
          <h3 class="text-lg sm:text-xl font-black text-white">Tích Điểm Mọi Giao Dịch &amp; Đổi Vé Xem Phim Miễn Phí</h3>
          <p class="text-xs text-slate-300">Tích lũy 5% - 10% điểm thưởng trên mỗi đơn đặt vé để đổi bắp nước và nâng hạng ghế VIP.</p>
        </div>
        <button
          type="button"
          @click="router.push('/cinema')"
          class="px-6 py-2.5 rounded-xl bg-white text-slate-950 font-black text-xs hover:bg-slate-200 transition shrink-0 cursor-pointer shadow-lg"
        >
          Khám Phá Vé Chiếu Rạp
        </button>
      </section>

    </div>

    <!-- Toast Notification -->
    <Transition name="toast">
      <div
        v-if="toastMessage"
        class="fixed bottom-6 right-6 z-50 px-4 py-3 rounded-2xl bg-slate-900/95 border border-rose-500/40 text-white text-xs font-semibold shadow-2xl backdrop-blur-md flex items-center gap-2.5 max-w-sm"
      >
        <CheckCircle2 class="w-4 h-4 text-emerald-400 shrink-0" />
        <span>{{ toastMessage }}</span>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.bg-radial-grid {
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 32px 32px;
}

.shimmer-text {
  background-size: 200% auto;
  animation: shimmer 6s linear infinite;
}

@keyframes shimmer {
  to {
    background-position: 200% center;
  }
}

.animate-spin-slow {
  animation: spin 8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Voucher grid transition */
.voucher-grid-enter-active,
.voucher-grid-leave-active {
  transition: all 0.35s ease;
}

.voucher-grid-enter-from {
  opacity: 0;
  transform: translateY(16px) scale(0.98);
}

.voucher-grid-leave-to {
  opacity: 0;
  transform: scale(0.96);
}

.voucher-grid-move {
  transition: transform 0.35s ease;
}

/* Toast animation */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s cubic-bezier(0.22, 1, 0.36, 1);
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.95);
}
</style>
