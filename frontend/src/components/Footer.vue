<script setup lang="ts">
import { Ticket, ShieldCheck, Zap, Headphones, Heart, Phone, Mail, MapPin } from 'lucide-vue-next'
</script>

<template>
  <footer class="border-t border-slate-200 bg-white mt-20 text-slate-600">
    <!-- Top Highlights Bar -->
    <div class="border-b border-slate-100 py-10 bg-slate-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-2xl bg-indigo-100 text-indigo-700 flex items-center justify-center shrink-0">
              <Zap class="w-6 h-6" />
            </div>
            <div>
              <h4 class="font-bold text-slate-900 text-base">Khóa Ghế Thời Gian Thực</h4>
              <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                Tích hợp Redis Distributed Lock & Pessimistic Write Lock cam kết không xảy ra tranh chấp hoặc bán quá số vé.
              </p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-2xl bg-purple-100 text-purple-700 flex items-center justify-center shrink-0">
              <ShieldCheck class="w-6 h-6" />
            </div>
            <div>
              <h4 class="font-bold text-slate-900 text-base">Vé QR Bảo Mật Anti-Passback</h4>
              <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                Mã hóa Token QR dùng 1 lần, chống chụp màn hình bán lại hoặc quay vòng vé tại cổng check-in.
              </p>
            </div>
          </div>

          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center shrink-0">
              <Headphones class="w-6 h-6" />
            </div>
            <div>
              <h4 class="font-bold text-slate-900 text-base">Hỗ Trợ Khách Hàng 24/7</h4>
              <p class="text-xs text-slate-500 mt-1 leading-relaxed">
                Tổng đài trực tuyến sẵn sàng giải quyết thắc mắc về đơn hàng, đổi thông tin và hỗ trợ vào cổng.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Footer Links -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
        <!-- Col 1: Brand & Contact -->
        <div class="space-y-4">
          <div class="flex items-center gap-2.5">
            <div class="w-9 h-9 rounded-xl bg-indigo-600 text-white flex items-center justify-center shadow-md">
              <Ticket class="w-5 h-5" />
            </div>
            <span class="text-xl font-black text-slate-900">Event<span class="text-indigo-600">Hub</span></span>
          </div>
          <p class="text-xs text-slate-500 leading-relaxed">
            Hệ thống bán vé sự kiện và quản lý check-in điện tử hàng đầu Việt Nam, phục vụ hàng triệu khán giả và ban tổ chức.
          </p>
          <div class="space-y-1.5 text-xs text-slate-500">
            <div class="flex items-center gap-2">
              <Phone class="w-3.5 h-3.5 text-indigo-600" />
              <span>Hotline CSKH: 1900 6868</span>
            </div>
            <div class="flex items-center gap-2">
              <Mail class="w-3.5 h-3.5 text-indigo-600" />
              <span>Email: support@eventhub.vn</span>
            </div>
          </div>
        </div>

        <!-- Col 2: Categories -->
        <div>
          <h4 class="text-xs font-bold text-slate-900 uppercase tracking-wider mb-4">Danh Mục Sự Kiện</h4>
          <ul class="space-y-2.5 text-xs text-slate-600">
            <li><router-link to="/events?categoryId=1" class="hover:text-indigo-600 transition-colors">Âm Nhạc & Concerts</router-link></li>
            <li><router-link to="/events?categoryId=2" class="hover:text-indigo-600 transition-colors">Hội Thảo & Tech Summit</router-link></li>
            <li><router-link to="/events?categoryId=3" class="hover:text-indigo-600 transition-colors">Thể Thao & Giải Chạy</router-link></li>
            <li><router-link to="/events?categoryId=4" class="hover:text-indigo-600 transition-colors">Sân Khấu & Nhạc Kịch</router-link></li>
            <li><router-link to="/events?categoryId=5" class="hover:text-indigo-600 transition-colors">Lễ Hội Văn Hóa</router-link></li>
          </ul>
        </div>

        <!-- Col 3: Customer Service -->
        <div>
          <h4 class="text-xs font-bold text-slate-900 uppercase tracking-wider mb-4">Khách Hàng</h4>
          <ul class="space-y-2.5 text-xs text-slate-600">
            <li><router-link to="/events" class="hover:text-indigo-600 transition-colors">Tìm kiếm sự kiện</router-link></li>
            <li><router-link to="/my-tickets" class="hover:text-indigo-600 transition-colors">Quản lý vé đã mua</router-link></li>
            <li><router-link to="/login" class="hover:text-indigo-600 transition-colors">Đăng nhập tài khoản</router-link></li>
            <li><router-link to="/register" class="hover:text-indigo-600 transition-colors">Đăng ký thành viên</router-link></li>
            <li><a href="http://localhost:8080/swagger-ui.html" target="_blank" class="hover:text-indigo-600 transition-colors">Tài liệu API Swagger ↗</a></li>
          </ul>
        </div>

        <!-- Col 4: Payments & Tech -->
        <div>
          <h4 class="text-xs font-bold text-slate-900 uppercase tracking-wider mb-3">Phương Thức Thanh Toán</h4>
          <p class="text-[11px] text-slate-500 mb-3">Hỗ trợ đa dạng cổng thanh toán an toàn:</p>
          <div class="flex flex-wrap gap-2 text-[11px] font-bold text-slate-700">
            <span class="px-2 py-1 rounded bg-slate-100 border border-slate-200">Visa / Master</span>
            <span class="px-2 py-1 rounded bg-slate-100 border border-slate-200">MoMo</span>
            <span class="px-2 py-1 rounded bg-slate-100 border border-slate-200">VNPay QR</span>
            <span class="px-2 py-1 rounded bg-slate-100 border border-slate-200">ZaloPay</span>
          </div>

          <h4 class="text-xs font-bold text-slate-900 uppercase tracking-wider mt-6 mb-2">Hạ Tầng Backend</h4>
          <div class="flex flex-wrap gap-1.5 text-[10px] font-medium text-slate-600">
            <span class="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-bold">Java 21</span>
            <span class="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-bold">Spring Boot 3</span>
            <span class="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-bold">Redis 7</span>
            <span class="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 border border-indigo-200 font-bold">MySQL 8.4</span>
          </div>
        </div>
      </div>

      <div class="border-t border-slate-200 mt-10 pt-6 flex flex-col sm:flex-row items-center justify-between text-xs text-slate-500 gap-4">
        <p>© 2026 EventHub Inc. Mọi quyền được bảo lưu. Giấy phép sàn thương mại điện tử số 03189921.</p>
        <p class="flex items-center gap-1">
          Thiết kế với <Heart class="w-3.5 h-3.5 text-rose-500 fill-rose-500" /> bởi đội ngũ EventHub Việt Nam
        </p>
      </div>
    </div>
  </footer>
</template>
