<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Newspaper,
  Calendar,
  Clock,
  Eye,
  Film,
  Music,
  Sparkles,
  ArrowRight,
  TrendingUp,
  Share2,
  Tag,
} from 'lucide-vue-next'

const router = useRouter()

interface NewsArticle {
  id: string
  title: string
  summary: string
  content: string
  category: 'cinema' | 'music' | 'tech' | 'tips'
  categoryLabel: string
  author: string
  publishedAt: string
  readTime: string
  views: number
  imageUrl: string
  featured?: boolean
  tags: string[]
}

const activeCategory = ref<string>('all')
const selectedArticle = ref<NewsArticle | null>(null)

const categories = [
  { id: 'all', label: 'Tất Cả Tin Tức' },
  { id: 'cinema', label: 'Điện Ảnh & Phim Rạp' },
  { id: 'music', label: 'Concert & Âm Nhạc' },
  { id: 'tech', label: 'Sự Kiện & Công Nghệ' },
  { id: 'tips', label: 'Mẹo Đặt Vé & Trải Nghiệm' },
]

const articles = ref<NewsArticle[]>([
  {
    id: '1',
    title: 'Top 5 Phim Chiếu Rạp Bom Tấn Đổ Bộ Tháng 11: Mãn Nhãn Kỹ Xảo IMAX',
    summary: 'Điểm danh loạt siêu phẩm điện ảnh quốc tế và Việt Nam được mong chờ nhất mùa lễ hội cuối năm với suất chiếu đặc biệt tại CineVerse.',
    content: 'Mùa phim cuối năm 2026 chính thức bùng nổ với hàng loạt tác phẩm điện ảnh đỉnh cao thuộc đa dạng thể loại từ hành động viễn tưởng, giật gân hồi hộp đến tâm lý gia đình sâu sắc. Trong đó, các cụm rạp đối tác của CineVerse đã hoàn tất nâng cấp hệ thống âm thanh vòm Dolby Atmos và màn chiếu cực đại IMAX Laser để mang đến cho khán giả cảm giác chân thực từng khuôn hình. Hãy nhanh tay săn vé sớm và áp dụng mã CINEVIP50 để nhận ưu đãi ngay hôm nay!',
    category: 'cinema',
    categoryLabel: 'Điện Ảnh',
    author: 'CineVerse Media',
    publishedAt: '24/10/2026',
    readTime: '4 phút đọc',
    views: 12450,
    imageUrl: 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=1200&q=80',
    featured: true,
    tags: ['Phim Mới', 'IMAX', 'CineVerse', 'Top 5'],
  },
  {
    id: '2',
    title: 'Cơ Chế Vé Động QR Anti-Passback: Giải Pháp Chống Vé Giả Toàn Diện',
    summary: 'Hệ thống EventHub áp dụng công nghệ bảo mật mã QR động chống chụp màn hình bán lại, bảo vệ quyền lợi 100% cho người mua vé.',
    content: 'Tình trạng vé giả hoặc một mã vé bị in bán lại cho nhiều người trên các hội nhóm phe vé từ lâu đã là nỗi lo lớn của khán giả đi concert. Với kiến trúc Anti-Passback và mã hóa thời gian thực tại EventHub, mỗi mã vé chỉ cho phép check-in một lần duy nhất tại cổng soát vé nhân viên. Ngay khi nhân viên quét thành công, trạng thái vé chuyển sang USED và vô hiệu hóa tức thì.',
    category: 'tips',
    categoryLabel: 'Bảo Mật Vé',
    author: 'EventHub Security Team',
    publishedAt: '22/10/2026',
    readTime: '3 phút đọc',
    views: 8900,
    imageUrl: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80',
    featured: false,
    tags: ['Anti-Passback', 'QR Code', 'Bảo Mật'],
  },
  {
    id: '3',
    title: 'Đại Nhạc Hội Mùa Đông "Nexus Symphony": Hơn 15.000 Khán Giả Đã Giữ Chỗ',
    summary: 'Sự kiện âm nhạc hoành tráng nhất năm tại Sân vận động Mỹ Đình chuẩn bị mở bán đợt vé VIP cuối cùng kèm quà tặng merchandise.',
    content: 'Quy tụ dàn nghệ sĩ hàng đầu trong nước và quốc tế, Nexus Symphony hứa hẹn sẽ làm nổ tung khán đài với dàn âm thanh ánh sáng chuẩn concert thế giới. Đơn vị tổ chức đã phối hợp cùng EventHub để phân chia sơ đồ ghế thông minh, giúp khán giả chọn đúng vị trí góc nhìn đẹp nhất với mức giá hợp lý.',
    category: 'music',
    categoryLabel: 'Concert',
    author: 'Ban Văn Hóa & Nghệ Thuật',
    publishedAt: '20/10/2026',
    readTime: '5 phút đọc',
    views: 15300,
    imageUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1200&q=80',
    featured: false,
    tags: ['Concert', 'Mỹ Đình', 'Nexus Symphony'],
  },
  {
    id: '4',
    title: 'Trải Nghiệm Đặt Ghế Xem Phim Trực Quan: Rạp Chiếu 3D Ngay Trong Tầm Tay',
    summary: 'Giao diện đặt vé xem phim hoàn toàn mới của CineVerse hỗ trợ chọn vị trí ghế VIP, Sweetbox đôi và combo bỏng ngô siêu tiện lợi.',
    content: 'Không cần xếp hàng dài tại quầy vé, giờ đây khách hàng chỉ mất chưa đầy 30 giây để hoàn tất việc chọn rạp, chọn suất chiếu và sơ đồ ghế. Mọi thông tin về hàng ghế lối đi, khoảng cách màn hình đều được mô phỏng chi tiết và rõ ràng.',
    category: 'cinema',
    categoryLabel: 'Công Nghệ',
    author: 'CineVerse Tech',
    publishedAt: '18/10/2026',
    readTime: '3 phút đọc',
    views: 6720,
    imageUrl: 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?auto=format&fit=crop&w=1200&q=80',
    featured: false,
    tags: ['Đặt Vé Online', 'Sơ Đồ Ghế', 'CineVerse'],
  },
])

const filteredArticles = computed(() => {
  if (activeCategory.value === 'all') {
    return articles.value
  }
  return articles.value.filter((a) => a.category === activeCategory.value)
})

const featuredArticle = computed(() => {
  return articles.value.find((a) => a.featured) || articles.value[0]
})
</script>

<template>
  <div class="min-h-screen bg-[#070A12] text-slate-200 py-8 sm:py-12">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 space-y-10">
      
      <!-- Top Title -->
      <div class="space-y-3">
        <div class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-blue-500/20 border border-blue-400/30 text-blue-400 text-xs font-bold uppercase tracking-wider">
          <Newspaper class="w-4 h-4" />
          <span>Trung Tâm Tin Tức & Sự Kiện</span>
        </div>
        <h1 class="text-3xl sm:text-4xl font-black text-white tracking-tight">
          Cập Nhật Xu Hướng Điện Ảnh & Đại Nhạc Hội
        </h1>
        <p class="text-sm sm:text-base text-slate-400 max-w-2xl leading-relaxed">
          Nơi tổng hợp thông tin phim chiếu rạp mới nhất, lịch mở bán vé concert âm nhạc và cẩm nang kinh nghiệm giải trí đỉnh cao.
        </p>
      </div>

      <!-- Featured Spotlight Article -->
      <div
        v-if="featuredArticle"
        @click="selectedArticle = featuredArticle"
        class="group relative overflow-hidden rounded-3xl bg-[#0D121F] border border-[#1C2538] hover:border-blue-500/50 transition-all duration-300 cursor-pointer shadow-2xl grid grid-cols-1 lg:grid-cols-12"
      >
        <div class="lg:col-span-7 relative h-64 sm:h-80 lg:h-full overflow-hidden">
          <img
            :src="featuredArticle.imageUrl"
            :alt="featuredArticle.title"
            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <div class="absolute inset-0 bg-gradient-to-t lg:bg-gradient-to-r from-black/80 via-black/30 to-transparent"></div>
          <span class="absolute top-4 left-4 px-3 py-1 rounded-xl text-xs font-black uppercase bg-blue-600 text-white shadow-lg">
            ★ TIÊU ĐIỂM
          </span>
        </div>

        <div class="lg:col-span-5 p-6 sm:p-8 flex flex-col justify-between space-y-4">
          <div class="space-y-3">
            <div class="flex items-center gap-2 text-xs text-slate-400">
              <span class="px-2.5 py-0.5 rounded-full bg-blue-500/10 text-blue-400 font-bold border border-blue-500/20">
                {{ featuredArticle.categoryLabel }}
              </span>
              <span>•</span>
              <div class="flex items-center gap-1">
                <Calendar class="w-3.5 h-3.5" />
                <span>{{ featuredArticle.publishedAt }}</span>
              </div>
              <span>•</span>
              <div class="flex items-center gap-1">
                <Clock class="w-3.5 h-3.5" />
                <span>{{ featuredArticle.readTime }}</span>
              </div>
            </div>

            <h2 class="text-xl sm:text-2xl font-black text-white group-hover:text-blue-400 transition-colors leading-snug">
              {{ featuredArticle.title }}
            </h2>

            <p class="text-xs sm:text-sm text-slate-300 line-clamp-3 leading-relaxed">
              {{ featuredArticle.summary }}
            </p>
          </div>

          <div class="pt-4 border-t border-slate-800 flex items-center justify-between">
            <div class="flex items-center gap-2 text-xs text-slate-400">
              <Eye class="w-4 h-4 text-slate-500" />
              <span>{{ featuredArticle.views.toLocaleString() }} lượt xem</span>
            </div>
            <div class="flex items-center gap-1 text-xs font-bold text-blue-400 group-hover:translate-x-1 transition-transform">
              <span>Đọc chi tiết</span>
              <ArrowRight class="w-4 h-4" />
            </div>
          </div>
        </div>
      </div>

      <!-- Categories Filter Tabs -->
      <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        <button
          v-for="cat in categories"
          :key="cat.id"
          @click="activeCategory = cat.id"
          class="px-4 py-2 rounded-xl text-xs sm:text-sm font-bold whitespace-nowrap transition cursor-pointer border"
          :class="
            activeCategory === cat.id
              ? 'bg-blue-600 text-white border-blue-500 shadow-md shadow-blue-600/30'
              : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white hover:bg-slate-800'
          "
        >
          {{ cat.label }}
        </button>
      </div>

      <!-- Articles Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="article in filteredArticles"
          :key="article.id"
          @click="selectedArticle = article"
          class="group rounded-3xl bg-[#0D121F] border border-[#1C2538] hover:border-blue-500/50 transition-all duration-300 cursor-pointer overflow-hidden flex flex-col justify-between shadow-lg hover:shadow-xl hover:shadow-blue-500/10"
        >
          <div>
            <div class="relative h-48 overflow-hidden">
              <img
                :src="article.imageUrl"
                :alt="article.title"
                class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent"></div>
              <span class="absolute bottom-3 left-3 px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase bg-blue-600/90 text-white backdrop-blur-sm">
                {{ article.categoryLabel }}
              </span>
            </div>

            <div class="p-5 space-y-2">
              <div class="flex items-center gap-2 text-[11px] text-slate-400">
                <Calendar class="w-3.5 h-3.5 text-slate-500" />
                <span>{{ article.publishedAt }}</span>
                <span>•</span>
                <span>{{ article.readTime }}</span>
              </div>

              <h3 class="text-base font-bold text-white group-hover:text-blue-400 transition-colors line-clamp-2 leading-snug">
                {{ article.title }}
              </h3>

              <p class="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                {{ article.summary }}
              </p>
            </div>
          </div>

          <div class="px-5 pb-5 pt-2 flex items-center justify-between text-xs border-t border-slate-800/80">
            <span class="text-[11px] text-slate-500 flex items-center gap-1">
              <Eye class="w-3.5 h-3.5" />
              {{ article.views.toLocaleString() }}
            </span>
            <span class="font-bold text-blue-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
              Xem thêm <ArrowRight class="w-3.5 h-3.5" />
            </span>
          </div>
        </div>
      </div>

      <!-- Quick Article Reading Modal -->
      <Teleport to="body">
        <div
          v-if="selectedArticle"
          class="fixed inset-0 z-50 overflow-y-auto bg-black/80 backdrop-blur-sm flex items-center justify-center p-4 sm:p-6"
          @click.self="selectedArticle = null"
        >
          <div class="relative max-w-2xl w-full rounded-3xl bg-[#0D121F] border border-[#1C2538] shadow-2xl overflow-hidden my-8 text-slate-200">
            <!-- Modal Header Image -->
            <div class="relative h-56 sm:h-72 w-full overflow-hidden">
              <img
                :src="selectedArticle.imageUrl"
                :alt="selectedArticle.title"
                class="w-full h-full object-cover"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-[#0D121F] via-[#0D121F]/40 to-transparent"></div>
              <button
                type="button"
                @click="selectedArticle = null"
                class="absolute top-4 right-4 w-9 h-9 rounded-full bg-black/60 text-white flex items-center justify-center hover:bg-black transition cursor-pointer"
              >
                ✕
              </button>
            </div>

            <!-- Content Area -->
            <div class="p-6 sm:p-8 space-y-4 -mt-10 relative z-10">
              <div class="flex items-center gap-2 text-xs text-slate-400">
                <span class="px-2.5 py-0.5 rounded-full bg-blue-600 text-white font-bold text-[10px]">
                  {{ selectedArticle.categoryLabel }}
                </span>
                <span>•</span>
                <span>{{ selectedArticle.publishedAt }}</span>
                <span>•</span>
                <span>Tác giả: {{ selectedArticle.author }}</span>
              </div>

              <h2 class="text-xl sm:text-2xl font-black text-white leading-tight">
                {{ selectedArticle.title }}
              </h2>

              <div class="p-4 rounded-2xl bg-blue-500/10 border border-blue-500/20 text-xs sm:text-sm text-blue-200 leading-relaxed font-medium">
                {{ selectedArticle.summary }}
              </div>

              <div class="text-xs sm:text-sm text-slate-300 leading-relaxed space-y-3">
                <p>{{ selectedArticle.content }}</p>
                <p>
                  Đừng quên theo dõi CineVerse và EventHub để nhận thông báo sớm nhất về các đợt mở bán vé rạp cũng như concert âm nhạc hoành tráng trong năm 2026.
                </p>
              </div>

              <!-- Tags -->
              <div class="pt-4 border-t border-slate-800 flex flex-wrap items-center gap-2">
                <Tag class="w-3.5 h-3.5 text-slate-500" />
                <span
                  v-for="tag in selectedArticle.tags"
                  :key="tag"
                  class="px-2.5 py-1 rounded-lg bg-slate-900 border border-slate-800 text-[11px] text-slate-400 font-medium"
                >
                  #{{ tag }}
                </span>
              </div>

              <!-- Footer Buttons -->
              <div class="pt-2 flex items-center justify-end gap-3">
                <button
                  type="button"
                  @click="selectedArticle = null"
                  class="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-white font-bold text-xs transition cursor-pointer"
                >
                  Đóng
                </button>
                <button
                  type="button"
                  @click="router.push('/cinema')"
                  class="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs transition cursor-pointer shadow-md shadow-blue-600/30"
                >
                  Đặt Vé Ngay
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>

    </div>
  </div>
</template>
