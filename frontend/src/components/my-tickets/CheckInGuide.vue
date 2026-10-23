<script setup lang="ts">
import {
  QrCode,
  Smartphone,
  IdCard,
  Clock,
  ShieldCheck,
  Headphones,
  CheckCircle2,
} from 'lucide-vue-next'

const steps = [
  {
    step: '01',
    icon: Smartphone,
    title: 'Sẵn sàng điện thoại',
    desc: 'Mở sẵn mã QR trong mục "Vé của tôi", tăng độ sáng màn hình lên mức cao nhất để máy quét đọc dễ dàng.',
  },
  {
    step: '02',
    icon: IdCard,
    title: 'Chuẩn bị CCCD / VNeID',
    desc: 'Mang theo giấy tờ tùy thân trùng khớp với thông tin khán giả đã đăng ký trên vé để đối chiếu khi cần.',
  },
  {
    step: '03',
    icon: Clock,
    title: 'Đến trước 45 phút',
    desc: 'Có mặt sớm để gửi xe, check-in qua cổng an ninh và di chuyển tới đúng hàng ghế đã chọn.',
  },
  {
    step: '04',
    icon: QrCode,
    title: 'Quét 1 chạm tại cổng',
    desc: 'Đưa mã QR vào trước mắt quét hoặc nhân viên soát vé. Hệ thống Anti-Passback xác thực trong 1 giây.',
  },
]
</script>

<template>
  <div class="rounded-3xl bg-white border border-slate-200 p-6 sm:p-8 shadow-sm">
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6 pb-4 border-b border-slate-100">
      <div>
        <h3 class="text-lg sm:text-xl font-black text-slate-900 flex items-center gap-2">
          <ShieldCheck class="w-5 h-5 text-indigo-600" />
          Hướng Dẫn Check-in Tại Cổng Soát Vé
        </h3>
        <p class="text-xs text-slate-500 mt-1">
          Hệ thống vé điện tử EventHub được bảo vệ bằng công nghệ mã hóa động chống vé giả
        </p>
      </div>

      <div class="flex items-center gap-2 text-xs font-bold text-indigo-700 bg-indigo-50 border border-indigo-200/80 px-3.5 py-1.5 rounded-full self-start sm:self-auto">
        <CheckCircle2 class="w-4 h-4 text-emerald-600" />
        Xác Thực Chống Vé Giả (Anti-Passback)
      </div>
    </div>

    <!-- 4 Steps Grid -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div
        v-for="s in steps"
        :key="s.step"
        class="p-4 rounded-2xl bg-slate-50 border border-slate-100 hover:bg-white hover:border-indigo-200 hover:shadow-md transition-all group"
      >
        <div class="flex items-center justify-between mb-3">
          <div class="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-600 flex items-center justify-center group-hover:scale-110 group-hover:bg-indigo-600 group-hover:text-white transition-all shadow-sm">
            <component :is="s.icon" class="w-5 h-5" />
          </div>
          <span class="text-xl font-black text-slate-300 font-mono group-hover:text-indigo-400 transition-colors">
            {{ s.step }}
          </span>
        </div>
        <h4 class="text-sm font-black text-slate-900 mb-1">{{ s.title }}</h4>
        <p class="text-xs text-slate-500 leading-relaxed">{{ s.desc }}</p>
      </div>
    </div>

    <!-- Support Banner -->
    <div class="p-4 rounded-2xl bg-gradient-to-r from-slate-900 via-indigo-950 to-slate-900 text-white flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-white/10 flex items-center justify-center shrink-0">
          <Headphones class="w-5 h-5 text-indigo-400" />
        </div>
        <div>
          <div class="text-xs font-bold text-slate-300">Cần hỗ trợ sự cố vé tại hiện trường?</div>
          <div class="text-sm font-black text-white">Tổng đài hỗ trợ 24/7: <span class="text-amber-400">1900 6868</span> (Nhánh 1)</div>
        </div>
      </div>
      <div class="text-xs text-slate-400 font-medium">
        Khu vực hỗ trợ vé của EventHub luôn mở tại cổng chính trước mỗi sự kiện 2 tiếng.
      </div>
    </div>
  </div>
</template>
