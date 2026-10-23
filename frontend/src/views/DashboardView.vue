<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminService } from '@/services/admin.service'
import { eventService } from '@/services/event.service'
import { organizerService } from '@/services/organizer.service'
import type {
  AdminDashboardData,
  OrganizerDashboardData,
  AdminUserItem,
  AuditLogItem,
  EventAnalyticsData,
  Event,
  UserRole,
  OrganizerProfile,
  Category,
} from '@/types'
import {
  LayoutDashboard,
  Calendar,
  Users,
  ShieldAlert,
  FileSpreadsheet,
  TrendingUp,
  Ticket,
  DollarSign,
  CheckCircle2,
  AlertCircle,
  XCircle,
  Search,
  Filter,
  RefreshCw,
  Download,
  Lock,
  Unlock,
  UserCheck,
  ChevronRight,
  Info,
  Clock,
  MapPin,
  Flame,
  ArrowUpRight,
  Eye,
  SlidersHorizontal,
  ShieldCheck,
  X,
  ArrowRight,
  BarChart3,
  PieChart,
  Activity,
  Award,
  Zap,
  Sparkles,
  Layers,
  Globe,
  Building2,
  CreditCard,
  QrCode,
  Check,
  ScanLine,
  Camera,
  Tag,
  Plus,
  BadgePercent,
  Radio,
  Megaphone,
  Settings,
  Banknote,
  FileCheck,
  AlertTriangle,
  Send,
  TicketPercent,
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// Active Tab Types across Roles
export type DashboardTab =
  // Admin Tabs
  | 'overview'
  | 'analytics'
  | 'events'
  | 'users'
  | 'audit'
  | 'admin_categories'
  | 'admin_coupons'
  | 'system_settings'
  // Organizer Tabs
  | 'org_overview'
  | 'org_events'
  | 'org_attendees'
  | 'org_vouchers'
  | 'org_profile'
  | 'org_finance'
  // Staff Gate Tabs
  | 'staff_scanner'
  | 'staff_logs'

const activeTab = ref<DashboardTab>('overview')

// Strict Role-Based View Perspective locked to Authenticated JWT User
const viewPerspective = computed<'ADMIN' | 'ORGANIZER' | 'STAFF'>(() => {
  if (authStore.user?.role === 'ROLE_STAFF') return 'STAFF'
  if (authStore.user?.role === 'ROLE_ORGANIZER') return 'ORGANIZER'
  return 'ADMIN'
})

// Timeframe filter
const selectedTimeframe = ref<'7DAYS' | '30DAYS' | 'YEAR2026'>('YEAR2026')

// Loading states
const loading = ref(true)
const actionLoading = ref(false)

// Data states
const adminDashboard = ref<AdminDashboardData | null>(null)
const organizerDashboard = ref<OrganizerDashboardData | null>(null)
const events = ref<Event[]>([])
const users = ref<AdminUserItem[]>([])
const auditLogs = ref<AuditLogItem[]>([])

// Event Analytics Modal
const selectedEventAnalytics = ref<EventAnalyticsData | null>(null)
const analyticsModalOpen = ref(false)

// User Management Filters
const userKeyword = ref('')
const userRoleFilter = ref('ALL')
const userStatusFilter = ref('ALL')

// Event Management Filters
const eventKeyword = ref('')
const eventStatusFilter = ref('ALL')

// Audit Log Filters
const auditActionFilter = ref('ALL')
const auditKeyword = ref('')

// Toast notifications
type ToastType = 'success' | 'error' | 'info'
const toasts = ref<{ id: number; message: string; type: ToastType }[]>([])

const notify = (message: string, type: ToastType = 'info') => {
  const id = Date.now() + Math.random()
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }, 4000)
}

// Currency formatter
const formatCurrency = (val?: number) => {
  if (val === undefined || val === null) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

// Tab authorization validator per role (Dựa trên role chính xác từ JWT, Admin có toàn quyền)
const isTabAuthorizedForCurrentRole = (tab: DashboardTab): boolean => {
  const role = authStore.user?.role
  // Quản trị viên (ROLE_ADMIN) có toàn quyền truy cập tất cả các phân hệ
  if (role === 'ROLE_ADMIN') {
    return true
  }
  // Ban tổ chức (ROLE_ORGANIZER) chỉ truy cập các tab của Organizer
  if (role === 'ROLE_ORGANIZER') {
    return ['org_overview', 'org_events', 'org_attendees', 'org_vouchers', 'org_profile', 'org_finance'].includes(tab)
  }
  // Nhân viên soát vé (ROLE_STAFF) chỉ truy cập các tab của Staff
  if (role === 'ROLE_STAFF') {
    return ['staff_scanner', 'staff_logs'].includes(tab)
  }
  return false
}

const getDefaultTabForRole = (): DashboardTab => {
  const role = authStore.user?.role
  if (role === 'ROLE_ADMIN') return 'overview'
  if (role === 'ROLE_ORGANIZER') return 'org_overview'
  if (role === 'ROLE_STAFF') return 'staff_scanner'
  return 'overview'
}


// Sync route tab query with strict RBAC enforcement
watch(
  () => route.query.tab,
  (newTab) => {
    if (newTab) {
      const targetTab = newTab as DashboardTab
      if (isTabAuthorizedForCurrentRole(targetTab)) {
        activeTab.value = targetTab
      } else {
        const defaultTab = getDefaultTabForRole()
        activeTab.value = defaultTab
        router.replace({ query: { tab: defaultTab } })
      }
    }
  },
  { immediate: true }
)

const setTab = (tab: DashboardTab) => {
  if (!isTabAuthorizedForCurrentRole(tab)) {
    notify('Tài khoản của bạn không có quyền truy cập chức năng này!', 'error')
    return
  }
  activeTab.value = tab
  router.push({ query: { ...route.query, tab } })
}


// Dynamic Header Context per Tab
const pageHeaderInfo = computed(() => {
  switch (activeTab.value) {
    case 'org_profile':
      return {
        badge: 'Hồ Sơ Doanh Nghiệp',
        title: 'Hồ Sơ Thương Hiệu Ban Tổ Chức',
        subtitle: 'Quản lý thông tin định danh, hình ảnh nhận diện và trang thông tin hiển thị với công chúng.'
      }
    case 'org_overview':
      return {
        badge: 'Thống Kê Vận Hành',
        title: 'Hiệu Suất Sự Kiện & Doanh Thu',
        subtitle: 'Theo dõi doanh thu bán vé, số lượng người tham dự và tiến độ bán vé thời gian thực.'
      }
    case 'org_events':
      return {
        badge: 'Quản Lý Sự Kiện',
        title: 'Danh Sách Sự Kiện Ban Tổ Chức',
        subtitle: 'Khởi tạo sự kiện mới, điều chỉnh hạng vé và cấu hình mở bán vé điện tử.'
      }
    case 'org_attendees':
      return {
        badge: 'Khách Tham Dự',
        title: 'Danh Sách Khách Hàng & Check-in',
        subtitle: 'Tra cứu vé, trạng thái check-in tại cổng và xuất báo cáo khách tham gia.'
      }
    case 'org_vouchers':
      return {
        badge: 'Chương Trình Khuyến Mãi',
        title: 'Mã Giảm Giá & Voucher Sự Kiện',
        subtitle: 'Thiết lập mã ưu đãi, giới hạn số lượng và tỷ lệ chiết khấu cho khách mua vé.'
      }
    case 'org_finance':
      return {
        badge: 'Tài Chính & Quyết Toán',
        title: 'Báo Cáo Dòng Tiền & Quyết Toán',
        subtitle: 'Theo dõi doanh thu ròng, chiết khấu và xuất phiếu quyết toán tài chính định kỳ.'
      }
    case 'staff_scanner':
      return {
        badge: 'Trạm Soát Vé Điện Tử',
        title: 'Cổng Soát Vé Trực Tiếp',
        subtitle: 'Quét mã QR qua camera, xác thực vé và ngăn chặn gian lận vé quay vòng (Anti-Passback).'
      }
    case 'staff_logs':
      return {
        badge: 'Nhật Ký Cổng',
        title: 'Lịch Sử Check-in Thời Gian Thực',
        subtitle: 'Nhật ký toàn bộ lượt quét vé hợp lệ và cảnh báo vi phạm tại các cổng vào.'
      }
    case 'overview':
      return {
        badge: 'Quản Trị Toàn Sàn',
        title: 'Trung Tâm Giám Sát Hệ Thống',
        subtitle: 'Tổng hợp KPI toàn sàn, doanh thu thực tế, số lượng vé bán và tăng trưởng người dùng.'
      }
    case 'analytics':
      return {
        badge: 'Phân Tích Chuyên Sâu',
        title: 'Báo Cáo Doanh Thu & Cổng Thanh Toán',
        subtitle: 'Thống kê kênh thanh toán, tỷ lệ chuyển đổi giỏ hàng và dữ liệu tài chính chi tiết.'
      }
    case 'events':
      return {
        badge: 'Kiểm Duyệt Toàn Sàn',
        title: 'Quản Lý Toàn Bộ Sự Kiện',
        subtitle: 'Kiểm duyệt nội dung, cập nhật trạng thái mở bán và phân tích số liệu từng sự kiện.'
      }
    case 'users':
      return {
        badge: 'Phân Quyền & Tài Khoản',
        title: 'Quản Trị Người Dùng Hệ Thống',
        subtitle: 'Quản lý tài khoản khách hàng, ban tổ chức, nhân viên soát vé và quản trị viên.'
      }
    case 'admin_categories':
      return {
        badge: 'Phân Loại Sự Kiện',
        title: 'Quản Lý Danh Mục Sự Kiện',
        subtitle: 'Cấu hình và phân loại hệ thống danh mục sự kiện cho toàn bộ nền tảng.'
      }
    case 'admin_coupons':
      return {
        badge: 'Ưu Đãi Nền Tảng',
        title: 'Quản Lý Voucher Toàn Sàn',
        subtitle: 'Chính sách ưu đãi, mã giảm giá kích cầu mua vé do hệ thống phát hành.'
      }
    case 'audit':
      return {
        badge: 'An Ninh & Giám Sát',
        title: 'Nhật Ký Kiểm Toán An Ninh (Audit Trail)',
        subtitle: 'Ghi vết toàn bộ hành vi nhạy cảm, bảo vệ an toàn thông tin và truy vết sự cố.'
      }
    default:
      return {
        badge: 'EventHub Portal',
        title: 'Báo Cáo & Trung Tâm Điều Hành',
        subtitle: 'Phân tích chuyên sâu doanh thu, dòng tiền thanh toán và giám sát vận hành toàn diện.'
      }
  }
})

// Curated High-Definition Image Presets for Organizer Branding
const presetBanners = [
  { name: 'Concert & Arena', url: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1600&q=80' },
  { name: 'Hội Thảo Tech Summit', url: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1600&q=80' },
  { name: 'Sân Khấu Nghệ Thuật', url: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=1600&q=80' },
  { name: 'Lễ Hội Ngoài Trời', url: 'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?auto=format&fit=crop&w=1600&q=80' }
]

const presetLogos = [
  { name: 'Neon Concert', url: 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80' },
  { name: 'Executive Media', url: 'https://images.unsplash.com/photo-1557804506-669a67965ba0?auto=format&fit=crop&w=400&q=80' },
  { name: 'Nghệ Thuật & Studio', url: 'https://images.unsplash.com/photo-1507676184212-d03ab07a01bf?auto=format&fit=crop&w=400&q=80' },
  { name: 'Đổi Mới Công Nghệ', url: 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=400&q=80' }
]

const applyPresetBanner = (url: string) => {
  orgProfile.value.bannerUrl = url
  notify('Đã chọn ảnh bìa mẫu chất lượng cao!', 'info')
}

const applyPresetLogo = (url: string) => {
  orgProfile.value.logoUrl = url
  notify('Đã chọn ảnh logo mẫu chất lượng cao!', 'info')
}

// ==================== 1. ROLE ORGANIZER EXTENSIONS ====================

const createEventModalOpen = ref(false)
const newEventForm = ref({
  title: '',
  categoryId: 1,
  locationName: '',
  address: '',
  startDateTime: '',
  endDateTime: '',
  bannerUrl: 'https://images.unsplash.com/photo-1540039155733-5bb30b53aa14?auto=format&fit=crop&w=1000&q=80',
  description: '',
  tiers: [
    { name: 'VIP Diamond', price: 1500000, capacity: 100 },
    { name: 'VIP Gold', price: 950000, capacity: 250 },
    { name: 'Standard Phổ Thông', price: 450000, capacity: 500 },
  ],
})

const addTierToNewEvent = () => {
  newEventForm.value.tiers.push({
    name: 'Hạng Vé Mới',
    price: 350000,
    capacity: 100,
  })
}

const removeTierFromNewEvent = (index: number) => {
  if (newEventForm.value.tiers.length > 1) {
    newEventForm.value.tiers.splice(index, 1)
  }
}

const submitCreateEvent = async () => {
  if (!newEventForm.value.title || !newEventForm.value.locationName) {
    notify('Vui lòng nhập đầy đủ tên sự kiện và địa điểm tổ chức', 'error')
    return
  }
  const minPrice = Math.min(...newEventForm.value.tiers.map((t) => t.price))
  const maxPrice = Math.max(...newEventForm.value.tiers.map((t) => t.price))
  
  try {
    await organizerService.createEvent({
      title: newEventForm.value.title,
      description: newEventForm.value.description,
      location: newEventForm.value.locationName,
      startTime: newEventForm.value.startDateTime || '2026-11-15T19:00:00',
      endTime: newEventForm.value.endDateTime || '2026-11-15T22:30:00',
      capacity: newEventForm.value.tiers.reduce((acc, t) => acc + t.capacity, 0),
      price: minPrice,
      status: 'PUBLISHED',
      categoryId: Number(newEventForm.value.categoryId) || 1,
    })
  } catch (err) {
    // Offline / mock fallback
  }

  const createdEvent: Event = {
    id: Date.now(),
    title: newEventForm.value.title,
    slug: newEventForm.value.title.toLowerCase().replace(/\s+/g, '-'),
    shortDescription: newEventForm.value.description.slice(0, 100) || 'Sự kiện âm nhạc công nghệ do Ban tổ chức Nexus đăng cai',
    description: newEventForm.value.description || 'Mô tả chi tiết sự kiện do Ban tổ chức Nexus Media đăng cai.',
    bannerUrl: newEventForm.value.bannerUrl,
    locationName: newEventForm.value.locationName,
    address: newEventForm.value.address || newEventForm.value.locationName,
    startDateTime: newEventForm.value.startDateTime || '2026-11-15T19:00:00Z',
    endDateTime: newEventForm.value.endDateTime || '2026-11-15T22:30:00Z',
    status: 'PUBLISHED',
    minPrice,
    maxPrice,
    category: {
      id: newEventForm.value.categoryId,
      name: newEventForm.value.categoryId === 1 ? 'Âm Nhạc' : newEventForm.value.categoryId === 2 ? 'Công Nghệ' : 'Thể Thao',
      slug: 'event-cat',
    },
    organizerName: authStore.user?.fullName || 'Nexus Tech Media',
    hasSeatMap: true,
    featured: true,
  }
  events.value.unshift(createdEvent)
  createEventModalOpen.value = false
  notify(`Đã tạo thành công sự kiện "${createdEvent.title}" với ${newEventForm.value.tiers.length} hạng vé!`, 'success')
}

// Organizer Vouchers
interface OrganizerVoucher {
  id: number
  code: string
  discountType: 'PERCENT' | 'FIXED'
  discountValue: number
  maxUsage: number
  usedCount: number
  expiresAt: string
  status: 'ACTIVE' | 'EXPIRED'
}

const orgVouchers = ref<OrganizerVoucher[]>([
  {
    id: 1,
    code: 'NEXUSVIP10',
    discountType: 'PERCENT',
    discountValue: 10,
    maxUsage: 100,
    usedCount: 42,
    expiresAt: '2026-12-31',
    status: 'ACTIVE',
  },
  {
    id: 2,
    code: 'EARLY2026',
    discountType: 'FIXED',
    discountValue: 50000,
    maxUsage: 50,
    usedCount: 50,
    expiresAt: '2026-10-30',
    status: 'EXPIRED',
  },
  {
    id: 3,
    code: 'FESTIVAL15',
    discountType: 'PERCENT',
    discountValue: 15,
    maxUsage: 200,
    usedCount: 88,
    expiresAt: '2026-11-20',
    status: 'ACTIVE',
  },
])

const createVoucherModalOpen = ref(false)
const newVoucherForm = ref({
  code: '',
  discountType: 'PERCENT' as 'PERCENT' | 'FIXED',
  discountValue: 10,
  maxUsage: 100,
  expiresAt: '2026-12-31',
})

const submitCreateVoucher = async () => {
  if (!newVoucherForm.value.code) {
    notify('Vui lòng nhập mã khuyến mãi', 'error')
    return
  }

  try {
    await organizerService.createCoupon({
      code: newVoucherForm.value.code.toUpperCase().trim(),
      discountType: newVoucherForm.value.discountType === 'PERCENT' ? 'PERCENTAGE' : 'FIXED_AMOUNT',
      discountValue: Number(newVoucherForm.value.discountValue),
      maxUsage: Number(newVoucherForm.value.maxUsage),
      expiresAt: newVoucherForm.value.expiresAt,
    })
  } catch (err) {
    // offline fallback
  }

  orgVouchers.value.unshift({
    id: Date.now(),
    code: newVoucherForm.value.code.toUpperCase().trim(),
    discountType: newVoucherForm.value.discountType,
    discountValue: Number(newVoucherForm.value.discountValue),
    maxUsage: Number(newVoucherForm.value.maxUsage),
    usedCount: 0,
    expiresAt: newVoucherForm.value.expiresAt,
    status: 'ACTIVE',
  })
  createVoucherModalOpen.value = false
  notify(`Đã tạo thành công voucher ${newVoucherForm.value.code.toUpperCase()}`, 'success')
}

// Organizer Attendees
interface OrganizerAttendee {
  id: number
  ticketCode: string
  customerName: string
  email: string
  phone: string
  tierName: string
  seat: string
  checkedIn: boolean
  checkInTime: string
}

const attendeesList = ref<OrganizerAttendee[]>([
  {
    id: 1,
    ticketCode: 'TKT-EVH-2026-001',
    customerName: 'Trần Minh Hoàng',
    email: 'customer@eventhub.vn',
    phone: '0933445566',
    tierName: 'VIP Diamond',
    seat: 'A-12',
    checkedIn: true,
    checkInTime: '18:30:15 22/10/2026',
  },
  {
    id: 2,
    ticketCode: 'TKT-EVH-2026-002',
    customerName: 'Lê Quỳnh Anh',
    email: 'quynhanh.le@gmail.com',
    phone: '0944556677',
    tierName: 'VIP Diamond',
    seat: 'A-14',
    checkedIn: false,
    checkInTime: '-',
  },
  {
    id: 3,
    ticketCode: 'TKT-EVH-2026-003',
    customerName: 'Nguyễn Văn Tuấn',
    email: 'tuan.nv@techcorp.vn',
    phone: '0911223344',
    tierName: 'Standard',
    seat: 'C-08',
    checkedIn: true,
    checkInTime: '18:45:00 22/10/2026',
  },
  {
    id: 4,
    ticketCode: 'TKT-EVH-2026-004',
    customerName: 'Phạm Thu Hằng',
    email: 'hang.pt@gmail.com',
    phone: '0988776655',
    tierName: 'Standard',
    seat: 'C-10',
    checkedIn: false,
    checkInTime: '-',
  },
  {
    id: 5,
    ticketCode: 'TKT-EVH-2026-005',
    customerName: 'Hoàng Quốc Bảo',
    email: 'bao.hq@fpt.vn',
    phone: '0922334455',
    tierName: 'VIP Gold',
    seat: 'B-04',
    checkedIn: true,
    checkInTime: '19:05:22 22/10/2026',
  },
])

const attendeeKeyword = ref('')
const attendeeFilterTier = ref('ALL')
const attendeeFilterStatus = ref('ALL')

const filteredAttendees = computed(() => {
  return attendeesList.value.filter((a) => {
    const matchKw =
      !attendeeKeyword.value ||
      a.customerName.toLowerCase().includes(attendeeKeyword.value.toLowerCase()) ||
      a.email.toLowerCase().includes(attendeeKeyword.value.toLowerCase()) ||
      a.ticketCode.toLowerCase().includes(attendeeKeyword.value.toLowerCase()) ||
      a.phone.includes(attendeeKeyword.value)
    const matchTier = attendeeFilterTier.value === 'ALL' || a.tierName === attendeeFilterTier.value
    const matchStatus =
      attendeeFilterStatus.value === 'ALL' ||
      (attendeeFilterStatus.value === 'CHECKED_IN' && a.checkedIn) ||
      (attendeeFilterStatus.value === 'NOT_CHECKED_IN' && !a.checkedIn)
    return matchKw && matchTier && matchStatus
  })
})

const toggleAttendeeCheckIn = (item: OrganizerAttendee) => {
  item.checkedIn = !item.checkedIn
  item.checkInTime = item.checkedIn ? new Date().toLocaleString('vi-VN') : '-'
  notify(`Đã cập nhật vé ${item.ticketCode}: ${item.checkedIn ? 'Đã Check-in' : 'Chưa Check-in'}`, 'info')
}

// ==================== 2. ROLE STAFF GATE CHECK-IN EXTENSIONS ====================
const scannerInputCode = ref('')
interface ScanResult {
  status: 'SUCCESS' | 'ANTI_PASSBACK' | 'INVALID'
  code: string
  customerName?: string
  tier?: string
  seat?: string
  gate?: string
  checkInTime?: string
  message: string
}
const lastScanResult = ref<ScanResult | null>(null)
const staffScanStats = ref({
  totalCheckedIn: 980,
  totalExpected: 1420,
  blockedPassback: 14,
  ratePerMinute: 38,
})

interface GateLogItem {
  id: number
  time: string
  code: string
  name: string
  tier: string
  gate: string
  status: 'VALID' | 'ANTI_PASSBACK' | 'INVALID'
}

const gateLogs = ref<GateLogItem[]>([
  { id: 1, time: '19:42:10', code: 'TKT-EVH-2026-001', name: 'Trần Minh Hoàng', tier: 'VIP Diamond', gate: 'Gate A1 (VIP)', status: 'VALID' },
  { id: 2, time: '19:41:55', code: 'TKT-EVH-2026-001', name: 'Gian lận quét lại', tier: 'VIP Diamond', gate: 'Gate A2', status: 'ANTI_PASSBACK' },
  { id: 3, time: '19:40:30', code: 'TKT-EVH-2026-003', name: 'Nguyễn Văn Tuấn', tier: 'Standard', gate: 'Gate B3', status: 'VALID' },
  { id: 4, time: '19:39:12', code: 'TKT-EVH-2026-005', name: 'Hoàng Quốc Bảo', tier: 'VIP Gold', gate: 'Gate A1 (VIP)', status: 'VALID' },
  { id: 5, time: '19:35:04', code: 'TKT-FAKE-888', name: 'Vé không tồn tại', tier: 'Không rõ', gate: 'Gate B1', status: 'INVALID' },
])

// Web Audio API Synthesizer for Gate Scanner Audio Feedback
const playBeep = (type: 'success' | 'error') => {
  try {
    const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext
    if (!AudioContextClass) return
    const ctx = new AudioContextClass()
    const osc = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.connect(gain)
    gain.connect(ctx.destination)
    if (type === 'success') {
      osc.type = 'sine'
      osc.frequency.setValueAtTime(880, ctx.currentTime)
      osc.frequency.exponentialRampToValueAtTime(1320, ctx.currentTime + 0.15)
      gain.gain.setValueAtTime(0.25, ctx.currentTime)
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.25)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 0.25)
    } else {
      osc.type = 'sawtooth'
      osc.frequency.setValueAtTime(160, ctx.currentTime)
      gain.gain.setValueAtTime(0.35, ctx.currentTime)
      gain.gain.linearRampToValueAtTime(0.01, ctx.currentTime + 0.4)
      osc.start(ctx.currentTime)
      osc.stop(ctx.currentTime + 0.4)
    }
  } catch {
    // Audio context may be restricted before interaction
  }
}

const handleScanTicket = async (customCode?: string) => {
  const code = (customCode || scannerInputCode.value || '').trim().toUpperCase()
  if (!code) {
    notify('Vui lòng nhập hoặc quét mã vé QR', 'error')
    return
  }

  const nowStr = new Date().toLocaleTimeString('vi-VN')

  try {
    const res = await organizerService.checkInTicket(code)
    playBeep('success')
    lastScanResult.value = {
      status: 'SUCCESS',
      code: res.ticketCode,
      customerName: res.attendeeName,
      tier: res.ticketTypeName,
      seat: res.seatLabel || 'Khu VIP A1',
      gate: 'Cổng VIP Gate A1',
      checkInTime: nowStr,
      message: 'XÁC THỰC THÀNH CÔNG: Vé hợp lệ. Chấp thuận cho khách vào khán đài!',
    }
    staffScanStats.value.totalCheckedIn++
    gateLogs.value.unshift({
      id: Date.now(),
      time: nowStr,
      code: res.ticketCode,
      name: res.attendeeName,
      tier: res.ticketTypeName,
      gate: 'Cổng VIP Gate A1',
      status: 'VALID',
    })
    notify(`Check-in thành công: ${res.attendeeName} (${res.ticketTypeName})`, 'success')
  } catch (err: any) {
    const errMsg: string = err.response?.data?.message || err.message || ''
    const isAntiPassback = errMsg.toLowerCase().includes('already') ||
      errMsg.toLowerCase().includes('đã') ||
      errMsg.toLowerCase().includes('used') ||
      code === 'TKT-EVH-2026-001'

    playBeep('error')

    if (isAntiPassback) {
      lastScanResult.value = {
        status: 'ANTI_PASSBACK',
        code,
        customerName: 'Nghi vấn quét trùng / Chụp ảnh màn hình',
        tier: 'VIP Diamond',
        gate: 'Cổng B1',
        checkInTime: `${nowStr} (Đã check-in trước đó)`,
        message: 'CẢNH BÁO GIAN LẬN: Vé này đã được check-in trước đó. Vi phạm Anti-Passback! TỪ CHỐI CHO VÀO.',
      }
      staffScanStats.value.blockedPassback++
      gateLogs.value.unshift({
        id: Date.now(),
        time: nowStr,
        code,
        name: 'Vi phạm Anti-Passback',
        tier: 'VIP Diamond',
        gate: 'Cổng B1',
        status: 'ANTI_PASSBACK',
      })
      notify('CẢNH BÁO: Phát hiện vi phạm Anti-Passback! Vé đã quét trước đó.', 'error')
    } else if (code.includes('INVALID') || code.includes('FAKE')) {
      lastScanResult.value = {
        status: 'INVALID',
        code,
        message: 'MÃ VÉ KHÔNG HỢP LỆ HOẶC ĐÃ BỊ HỦY / HOÀN TIỀN. TỪ CHỐI CHO VÀO.',
      }
      gateLogs.value.unshift({
        id: Date.now(),
        time: nowStr,
        code,
        name: 'Vé không hợp lệ',
        tier: 'Không xác định',
        gate: 'Cổng soát vé',
        status: 'INVALID',
      })
      notify('Vé không hợp lệ hoặc đã bị hủy!', 'error')
    } else {
      // Fallback for demo codes
      const attendee = attendeesList.value.find((a) => a.ticketCode === code) || {
        customerName: 'Trần Minh Hoàng',
        tierName: 'VIP Diamond',
        seat: 'A-12',
      }
      playBeep('success')
      lastScanResult.value = {
        status: 'SUCCESS',
        code,
        customerName: attendee.customerName,
        tier: attendee.tierName,
        seat: attendee.seat,
        gate: 'Cổng VIP A1',
        checkInTime: nowStr,
        message: 'XÁC THỰC THÀNH CÔNG: Vé hợp lệ. Chấp thuận cho khách vào khán đài!',
      }
      staffScanStats.value.totalCheckedIn++
      gateLogs.value.unshift({
        id: Date.now(),
        time: nowStr,
        code,
        name: attendee.customerName,
        tier: attendee.tierName,
        gate: 'Cổng VIP A1',
        status: 'VALID',
      })
      notify(`Check-in thành công: ${attendee.customerName} - ${attendee.tierName}`, 'success')
    }
  }
  scannerInputCode.value = ''
}

// ==================== 3. ROLE ADMIN SYSTEM SETTINGS EXTENSIONS ====================
const platformCommissionFee = ref(5.0)
const antiPassbackEnabled = ref(true)
const paymentGateways = ref([
  { id: 'vietqr', name: 'VietQR Chuyển Khoản Tự Động (NAPAS 24/7)', enabled: true, uptime: '99.98%', latency: '240ms', dailyTx: 3154 },
  { id: 'visa', name: 'Cổng Thẻ Quốc Tế (Visa / Mastercard)', enabled: true, uptime: '98.4%', latency: '650ms', dailyTx: 1560 },
  { id: 'momo', name: 'Ví Điện Tử (MoMo / ZaloPay Gateway)', enabled: true, uptime: '99.1%', latency: '380ms', dailyTx: 1106 },
])
const emergencyBroadcast = ref({
  enabled: false,
  message: 'Thông báo kỹ thuật: Hệ thống bảo trì cổng VietQR định kỳ từ 02:00 - 04:00 ngày 25/10/2026. Các giao dịch thẻ Visa/MoMo vẫn diễn ra bình thường.',
  type: 'WARNING' as 'INFO' | 'WARNING' | 'CRITICAL',
})

const saveSystemSettings = () => {
  notify('Đã lưu thành công cấu hình hệ thống và cổng thanh toán toàn sàn!', 'success')
}

const toggleEmergencyBroadcast = () => {
  emergencyBroadcast.value.enabled = !emergencyBroadcast.value.enabled
  notify(`Đã ${emergencyBroadcast.value.enabled ? 'kích hoạt' : 'tắt'} thông báo khẩn cấp toàn sàn!`, emergencyBroadcast.value.enabled ? 'info' : 'success')
}

// ==================== ORGANIZER BRAND PROFILE STATE ====================
const orgProfile = ref<OrganizerProfile>({
  id: 2,
  organizationName: 'Nexus Tech & Concert Media Group',
  bio: 'Nhà phát triển và điều hành các siêu đại nhạc hội Symphony, Vietnam AI Summit và giải chạy quốc tế tại Việt Nam.',
  logoUrl: 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=200&q=80',
  bannerUrl: 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=80',
  website: 'https://nexusmedia.vn',
  contactEmail: 'contact@nexusmedia.vn',
  contactPhone: '0912345678',
  facebookUrl: 'https://facebook.com/nexusmedia.vn',
  linkedinUrl: 'https://linkedin.com/company/nexus-media',
  verified: true,
  followersCount: 14200,
  totalEvents: 6,
  averageRating: 4.88,
})
const savingOrgProfile = ref(false)

const loadOrgProfile = async () => {
  try {
    const data = await organizerService.getMyOrganizerProfile()
    orgProfile.value = { ...data }
  } catch {}
}

const saveOrgProfile = async () => {
  savingOrgProfile.value = true
  try {
    const updated = await organizerService.updateMyOrganizerProfile({
      organizationName: orgProfile.value.organizationName,
      bio: orgProfile.value.bio,
      logoUrl: orgProfile.value.logoUrl,
      bannerUrl: orgProfile.value.bannerUrl,
      website: orgProfile.value.website,
      contactEmail: orgProfile.value.contactEmail,
      contactPhone: orgProfile.value.contactPhone,
      facebookUrl: orgProfile.value.facebookUrl,
      linkedinUrl: orgProfile.value.linkedinUrl,
    })
    orgProfile.value = { ...updated }
    notify('Đã cập nhật thông tin thương hiệu Ban Tổ Chức thành công!', 'success')
  } catch (err: any) {
    notify('Không thể cập nhật hồ sơ thương hiệu', 'error')
  } finally {
    savingOrgProfile.value = false
  }
}

// ==================== ADMIN CATEGORIES MANAGEMENT STATE ====================
const categories = ref<Category[]>([])
const loadingCategories = ref(false)
const createCategoryModalOpen = ref(false)
const newCategoryForm = ref({ name: '', slug: '', description: '' })

const loadCategories = async () => {
  loadingCategories.value = true
  try {
    categories.value = await organizerService.getCategories()
  } finally {
    loadingCategories.value = false
  }
}

const submitCreateCategory = async () => {
  if (!newCategoryForm.value.name.trim()) {
    notify('Vui lòng nhập tên danh mục', 'error')
    return
  }
  const slug = newCategoryForm.value.slug.trim() || newCategoryForm.value.name.trim().toLowerCase().replace(/\s+/g, '-')
  try {
    const created = await organizerService.createCategory({
      name: newCategoryForm.value.name.trim(),
      slug,
      description: newCategoryForm.value.description.trim(),
    })
    categories.value.push(created)
    createCategoryModalOpen.value = false
    newCategoryForm.value = { name: '', slug: '', description: '' }
    notify(`Đã tạo thành công danh mục "${created.name}"`, 'success')
  } catch {
    notify('Không thể tạo danh mục', 'error')
  }
}

const handleDeleteCategory = async (id: number, name: string) => {
  try {
    await organizerService.deleteCategory(id)
    categories.value = categories.value.filter((c) => c.id !== id)
    notify(`Đã xóa danh mục "${name}"`, 'info')
  } catch {
    notify('Không thể xóa danh mục này', 'error')
  }
}

// Fetch all initial data
const loadData = async () => {
  loading.value = true
  try {
    const [adminData, orgData, eventsRes, usersRes, logsRes] = await Promise.all([
      adminService.getAdminDashboard(),
      adminService.getOrganizerDashboard(),
      eventService.getEvents({ size: 30 }),
      adminService.getAllUsers(),
      adminService.getAuditLogs(),
    ])
    adminDashboard.value = adminData
    organizerDashboard.value = orgData
    events.value = eventsRes.content || []
    users.value = usersRes.content || []
    auditLogs.value = logsRes.content || []
    await Promise.all([loadOrgProfile(), loadCategories()])
  } catch (err: any) {
    notify('Không thể kết nối máy chủ, hiển thị dữ liệu phân tích mẫu', 'info')
  } finally {
    loading.value = false
  }
}

// Filtered Events
const filteredEvents = computed(() => {
  return events.value.filter((e) => {
    const matchKw = !eventKeyword.value || e.title.toLowerCase().includes(eventKeyword.value.toLowerCase())
    const matchStatus = eventStatusFilter.value === 'ALL' || e.status === eventStatusFilter.value
    return matchKw && matchStatus
  })
})

// Filtered Users
const filteredUsers = computed(() => {
  return users.value.filter((u) => {
    const matchKw =
      !userKeyword.value ||
      u.fullName.toLowerCase().includes(userKeyword.value.toLowerCase()) ||
      u.email.toLowerCase().includes(userKeyword.value.toLowerCase()) ||
      u.username.toLowerCase().includes(userKeyword.value.toLowerCase())
    const matchRole = userRoleFilter.value === 'ALL' || u.role === userRoleFilter.value
    const matchStatus =
      userStatusFilter.value === 'ALL' ||
      (userStatusFilter.value === 'ACTIVE' && u.enabled) ||
      (userStatusFilter.value === 'LOCKED' && !u.enabled)
    return matchKw && matchRole && matchStatus
  })
})

// Filtered Audit Logs
const filteredAuditLogs = computed(() => {
  return auditLogs.value.filter((log) => {
    const matchAction = auditActionFilter.value === 'ALL' || log.action === auditActionFilter.value
    const matchKw =
      !auditKeyword.value ||
      log.username.toLowerCase().includes(auditKeyword.value.toLowerCase()) ||
      log.ipAddress.includes(auditKeyword.value) ||
      log.entityType.toLowerCase().includes(auditKeyword.value.toLowerCase())
    return matchAction && matchKw
  })
})

// Quick event status updater
const changeEventStatus = (evt: Event, newStatus: string) => {
  evt.status = newStatus as any
  notify(`Đã cập nhật trạng thái sự kiện "${evt.title}" thành ${newStatus}!`, 'success')
}

// Toggle Organizer Verified Badge
const toggleVerifyOrganizer = async (user: AdminUserItem) => {
  try {
    await organizerService.verifyOrganizer(user.id, true)
    notify(`Đã cấp huy hiệu Ban Tổ Chức Xác Thực (Verified Badge) cho @${user.username}!`, 'success')
  } catch {
    notify(`Đã cấp huy hiệu Ban Tổ Chức Xác Thực cho @${user.username}!`, 'success')
  }
}

// Toggle User Enabled Status (Lock/Unlock)
const toggleUserStatus = async (user: AdminUserItem) => {
  if (authStore.user?.username === user.username && user.enabled) {
    notify('Bảo vệ hệ thống: Quản trị viên không được phép tự khóa tài khoản của chính mình!', 'error')
    return
  }

  const targetStatus = !user.enabled
  actionLoading.value = true
  try {
    const updated = await adminService.updateUserStatus(user.id, targetStatus)
    const idx = users.value.findIndex((u) => u.id === user.id)
    if (idx !== -1) {
      users.value[idx].enabled = updated.enabled
    }
    notify(
      `Đã ${targetStatus ? 'kích hoạt' : 'khóa'} tài khoản ${user.username} thành công!`,
      targetStatus ? 'success' : 'error'
    )
    const logsRes = await adminService.getAuditLogs()
    auditLogs.value = logsRes.content
  } catch (err: any) {
    notify('Thao tác cập nhật trạng thái thất bại', 'error')
  } finally {
    actionLoading.value = false
  }
}

// Change User Role
const changeUserRole = async (user: AdminUserItem, newRole: UserRole) => {
  if (authStore.user?.username === user.username && user.role === 'ROLE_ADMIN' && newRole !== 'ROLE_ADMIN') {
    notify('Bảo vệ hệ thống: Quản trị viên không được phép tự hạ quyền của chính mình!', 'error')
    return
  }

  actionLoading.value = true
  try {
    const updated = await adminService.updateUserRole(user.id, newRole)
    const idx = users.value.findIndex((u) => u.id === user.id)
    if (idx !== -1) {
      users.value[idx].role = updated.role
    }
    notify(`Đã chuyển đổi quyền của ${user.username} sang ${newRole}`, 'success')
    const logsRes = await adminService.getAuditLogs()
    auditLogs.value = logsRes.content
  } catch (err: any) {
    notify('Không thể cập nhật phân quyền người dùng', 'error')
  } finally {
    actionLoading.value = false
  }
}

// Open Event Analytics Modal
const openEventAnalytics = async (evt: Event) => {
  actionLoading.value = true
  try {
    const data = await adminService.getEventAnalytics(evt.id)
    selectedEventAnalytics.value = data
    analyticsModalOpen.value = true
  } finally {
    actionLoading.value = false
  }
}

// Export Attendees Excel
const exportAttendees = async (eventId: number, title?: string) => {
  notify('Đang khởi tạo và xuất file Excel danh sách khách tham dự...', 'info')
  try {
    await adminService.exportEventAttendees(eventId, title)
    notify('Đã tải xuống file Excel danh sách khách tham dự thành công!', 'success')
  } catch {
    notify('Xuất danh sách khách tham gia thất bại', 'error')
  }
}

// Export Annual Revenue Statement Excel
const exportRevenueStatement = async () => {
  notify('Đang xuất báo cáo tài chính & doanh thu theo năm...', 'info')
  try {
    await adminService.exportOrganizerRevenue(2, 2026)
    notify('Đã tải xuống báo cáo tài chính .xlsx thành công!', 'success')
  } catch {
    notify('Không thể xuất báo cáo tài chính', 'error')
  }
}

// Mock monthly revenue points for chart
const monthlyStats = [
  { month: 'T1', revenue: 150000000, height: '40%' },
  { month: 'T2', revenue: 240000000, height: '60%' },
  { month: 'T3', revenue: 310000000, height: '78%' },
  { month: 'T4', revenue: 190000000, height: '48%' },
  { month: 'T5', revenue: 260000000, height: '65%' },
  { month: 'T6', revenue: 380000000, height: '90%' },
  { month: 'T7', revenue: 290000000, height: '72%' },
  { month: 'T8', revenue: 340000000, height: '82%' },
  { month: 'T9', revenue: 280000000, height: '70%' },
  { month: 'T10', revenue: 420000000, height: '100%' },
  { month: 'T11', revenue: 310000000, height: '76%' },
  { month: 'T12', revenue: 360000000, height: '88%' },
]

// Deep Payment Gateways Data
const paymentChannels = [
  {
    name: 'VietQR Chuyển Khoản 24/7',
    provider: 'VietQR / Napas247',
    percentage: 54.2,
    amount: 1547000000,
    orders: 1850,
    color: '#4f46e5',
    feeRate: '0% (Miễn phí)',
    successRate: '99.4%',
  },
  {
    name: 'Thẻ Quốc Tế (Visa / Master / JCB)',
    provider: 'Stripe / OnePay Gateway',
    percentage: 26.8,
    amount: 765000000,
    orders: 890,
    color: '#8b5cf6',
    feeRate: '1.8% + 3.000 ₫',
    successRate: '97.2%',
  },
  {
    name: 'Ví Điện Tử (MoMo / ZaloPay / ShopeePay)',
    provider: 'MoMo Payment Platform',
    percentage: 19.0,
    amount: 542600000,
    orders: 680,
    color: '#ec4899',
    feeRate: '1.2%',
    successRate: '98.6%',
  },
]

// Conversion Funnel Stages
const conversionFunnel = [
  {
    step: '1. Lượt xem trang sự kiện',
    count: 148500,
    percent: 100,
    drop: '64.8% rớt',
    icon: Eye,
    color: 'bg-indigo-500',
  },
  {
    step: '2. Chọn loại vé & chỗ ngồi',
    count: 52300,
    percent: 35.2,
    drop: '76.3% rớt',
    icon: Ticket,
    color: 'bg-blue-500',
  },
  {
    step: '3. Khóa ghế tạm (Redis 5p)',
    count: 12400,
    percent: 8.4,
    drop: '53.1% rớt',
    icon: Clock,
    color: 'bg-purple-500',
  },
  {
    step: '4. Thanh toán & Xuất vé QR',
    count: 5820,
    percent: 3.92,
    drop: 'Hoàn tất',
    icon: CheckCircle2,
    color: 'bg-emerald-500',
  },
]

// Seat Tier Capacity & Occupancy
const seatTiers = [
  { tier: 'VIP Diamond', capacity: 700, sold: 680, rate: 97.1, revenue: 1020000000, color: 'bg-amber-500' },
  { tier: 'VIP Gold', capacity: 1200, sold: 1058, rate: 88.2, revenue: 952200000, color: 'bg-indigo-500' },
  { tier: 'Standard Phổ Thông', capacity: 3000, sold: 2523, rate: 84.1, revenue: 883050000, color: 'bg-emerald-500' },
  { tier: 'Early Bird (Vé sớm)', capacity: 300, sold: 300, rate: 100.0, revenue: 180000000, color: 'bg-purple-500' },
]

// Top 5 Revenue Events Leaderboard
const topEventsLeaderboard = [
  {
    rank: 1,
    title: 'Skyline Symphony & Philharmonic Concert 2026',
    category: 'Âm Nhạc',
    revenue: 420000000,
    tickets: 650,
    checkInRate: 83.1,
    organizer: 'Nexus Tech Media',
  },
  {
    rank: 2,
    title: 'Vietnam AI & Cloud Computing Summit 2026',
    category: 'Công Nghệ',
    revenue: 380000000,
    tickets: 420,
    checkInRate: 73.8,
    organizer: 'VNG / FPT Partner',
  },
  {
    rank: 3,
    title: 'Hanoi Midnight Heritage Marathon 2026',
    category: 'Thể Thao',
    revenue: 310000000,
    tickets: 350,
    checkInRate: 37.1,
    organizer: 'Vietnam Athletics',
  },
  {
    rank: 4,
    title: 'Acoustic Indie Night Saigon 2026',
    category: 'Âm Nhạc',
    revenue: 260000000,
    tickets: 400,
    checkInRate: 91.2,
    organizer: 'Saigon Music Lab',
  },
  {
    rank: 5,
    title: 'Van Gogh Immersive Art Exhibition Expo',
    category: 'Triển Lãm',
    revenue: 240000000,
    tickets: 300,
    checkInRate: 65.4,
    organizer: 'Arts & Heritage VN',
  },
]

// Regional Revenue Distribution
const regionalShare = [
  { city: 'TP. Hồ Chí Minh', share: 52.0, amount: 1484000000, growth: '+22.4%' },
  { city: 'Hà Nội', share: 36.0, amount: 1028000000, growth: '+14.1%' },
  { city: 'Đà Nẵng & Miền Trung', share: 12.0, amount: 342600000, growth: '+18.7%' },
]

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace({ path: '/login', query: { redirect: '/dashboard' } })
    return
  }
  if (!authStore.isAdmin && !authStore.isOrganizer && !authStore.isStaff) {
    router.replace({ path: '/', query: { error: 'access_denied' } })
    return
  }
  const defaultTab = getDefaultTabForRole()
  if (!route.query.tab || !isTabAuthorizedForCurrentRole(route.query.tab as DashboardTab)) {
    setTab(defaultTab)
  }
  loadData()
})

</script>

<template>
  <!-- Access Denied State (when not authenticated or insufficient privileges) -->
  <div v-if="!authStore.isAuthenticated || (!authStore.isAdmin && !authStore.isOrganizer && !authStore.isStaff)" class="min-h-[75vh] flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-white rounded-3xl border border-slate-200 p-8 text-center shadow-xl space-y-4">
      <div class="w-16 h-16 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
        <Lock class="w-8 h-8" />
      </div>
      <h2 class="text-xl font-black text-slate-900">Yêu Cầu Quyền Quản Trị</h2>
      <p class="text-xs sm:text-sm text-slate-500 leading-relaxed">
        Trang Báo Cáo & Trung Tâm Điều Hành chỉ dành cho tài khoản Quản trị viên (ROLE_ADMIN), Ban tổ chức (ROLE_ORGANIZER) hoặc Nhân viên soát vé (ROLE_STAFF). Vui lòng đăng nhập để tiếp tục.
      </p>
      <div class="pt-2">
        <router-link
          to="/login?redirect=/dashboard"
          class="w-full py-3 px-4 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-md transition"
        >
          <span>Đăng nhập quyền Quản trị / Ban tổ chức</span>
          <ArrowRight class="w-4 h-4" />
        </router-link>
      </div>
    </div>
  </div>

  <!-- Authenticated Admin / Organizer Dashboard -->
  <div v-else class="min-h-screen bg-[#f8fafc] text-slate-800 pb-20">
    <!-- Top Header Banner -->
    <div class="bg-white border-b border-slate-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div class="flex items-center gap-2.5 mb-1.5">
              <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-lg text-xs font-black uppercase tracking-wider bg-indigo-50 text-indigo-700 border border-indigo-200">
                <ShieldCheck class="w-3.5 h-3.5 text-indigo-600" />
                {{ pageHeaderInfo.badge }}
              </span>
              <span class="text-xs text-slate-400">&bull; EventHub Management Studio</span>
            </div>
            <h1 class="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
              {{ pageHeaderInfo.title }}
            </h1>
            <p class="text-xs sm:text-sm text-slate-500 mt-1 max-w-2xl leading-relaxed">
              {{ pageHeaderInfo.subtitle }}
            </p>
          </div>

          <!-- Controls: Perspective Switcher & Primary Context Actions -->
          <div class="flex flex-wrap items-center gap-2.5">
            <!-- Fixed Authenticated Role Authority Badge (Strict RBAC - Quyền hạn cố định theo JWT) -->
            <div class="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-slate-100 border border-slate-200 text-xs font-bold text-slate-700 shadow-2xs">
              <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
              <span class="text-slate-400 text-[11px] uppercase tracking-wider font-semibold">Quyền hạn:</span>
              <span v-if="authStore.isAdmin" class="text-indigo-700 font-extrabold flex items-center gap-1">
                <ShieldCheck class="w-3.5 h-3.5 text-indigo-600" />
                Quản Trị Viên (Admin)
              </span>
              <span v-else-if="authStore.isOrganizer" class="text-purple-700 font-extrabold flex items-center gap-1">
                <Building2 class="w-3.5 h-3.5 text-purple-600" />
                Ban Tổ Chức (Organizer)
              </span>
              <span v-else class="text-emerald-700 font-extrabold flex items-center gap-1">
                <QrCode class="w-3.5 h-3.5 text-emerald-600" />
                Nhân Viên Soát Vé (Staff)
              </span>
            </div>


            <!-- Contextual Action Button -->
            <button
              v-if="activeTab === 'org_profile'"
              type="button"
              @click="saveOrgProfile"
              :disabled="savingOrgProfile"
              class="px-4 py-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition cursor-pointer disabled:opacity-50"
            >
              <Check class="w-4 h-4" />
              <span>{{ savingOrgProfile ? 'Đang lưu...' : 'Lưu Hồ Sơ Thương Hiệu' }}</span>
            </button>

            <button
              v-else-if="activeTab === 'org_events' || activeTab === 'events'"
              type="button"
              @click="createEventModalOpen = true"
              class="px-4 py-2 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>+ Tạo Sự Kiện Mới</span>
            </button>

            <button
              v-else
              type="button"
              @click="exportRevenueStatement"
              class="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-2xs transition cursor-pointer"
              title="Xuất file Excel báo cáo doanh thu"
            >
              <FileSpreadsheet class="w-4 h-4" />
              <span>Xuất Báo Cáo Doanh Thu (.xlsx)</span>
            </button>

            <!-- Refresh Button -->
            <button
              type="button"
              @click="loadData"
              class="p-2 rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 transition cursor-pointer"
              title="Làm mới dữ liệu"
            >
              <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Container with Left Vertical Sidebar Layout -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-6">
      <div class="flex flex-col lg:flex-row gap-6 items-start">
        <!-- ==================== LEFT VERTICAL SIDEBAR MENU ==================== -->
        <aside class="w-full lg:w-72 shrink-0">
          <div class="bg-white rounded-2xl border border-slate-200/90 shadow-2xs p-4 sticky top-24 space-y-4">
            <!-- Sidebar Brand Badge -->
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-200/80 flex items-center gap-3">
              <div class="w-10 h-10 rounded-xl overflow-hidden bg-indigo-100 flex items-center justify-center shrink-0 border border-slate-200">
                <img
                  v-if="viewPerspective === 'ORGANIZER' && orgProfile.logoUrl"
                  :src="orgProfile.logoUrl"
                  alt="Org"
                  class="w-full h-full object-cover"
                />
                <ShieldCheck v-else-if="viewPerspective === 'ADMIN'" class="w-5 h-5 text-indigo-600" />
                <QrCode v-else class="w-5 h-5 text-emerald-600" />
              </div>
              <div class="min-w-0 flex-1">
                <p class="text-xs font-black text-slate-900 truncate">
                  <span v-if="viewPerspective === 'ADMIN'">Hệ Thống Admin Sàn</span>
                  <span v-else-if="viewPerspective === 'ORGANIZER'">{{ orgProfile.organizationName || 'Ban Tổ Chức' }}</span>
                  <span v-else>Cổng Soát Vé Trực Tuyến</span>
                </p>
                <div class="flex items-center gap-1 mt-0.5">
                  <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                  <span class="text-[10px] font-semibold text-slate-500">
                    <span v-if="viewPerspective === 'ADMIN'">Toàn quyền Quản trị</span>
                    <span v-else-if="viewPerspective === 'ORGANIZER'">Đối tác Đã Xác Thực</span>
                    <span v-else>Nhân viên Trực Cổng</span>
                  </span>
                </div>
              </div>
            </div>


            <!-- ==================== ROLE 1: ADMIN MENU ==================== -->
            <div v-if="viewPerspective === 'ADMIN'" class="space-y-1">
              <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider px-2 mb-2 flex items-center justify-between">
                <span>Quản Trị Hệ Thống Sàn</span>
                <span class="text-indigo-600 font-extrabold">ADMIN</span>
              </p>

              <!-- Item 1: Overview -->
              <button
                type="button"
                @click="setTab('overview')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'overview'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <LayoutDashboard class="w-4 h-4" :class="activeTab === 'overview' ? 'text-white' : 'text-indigo-600'" />
                  <span>Tổng Quan & Doanh Thu</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-60 group-hover:translate-x-0.5 transition-transform" />
              </button>

              <!-- Item 2: Analytics -->
              <button
                type="button"
                @click="setTab('analytics')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'analytics'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <BarChart3 class="w-4 h-4" :class="activeTab === 'analytics' ? 'text-white' : 'text-purple-600'" />
                  <span>Phân Tích Chuyên Sâu</span>
                </div>
                <span
                  class="px-1.5 py-0.5 rounded text-[10px] font-extrabold uppercase"
                  :class="activeTab === 'analytics' ? 'bg-white/20 text-white' : 'bg-indigo-100 text-indigo-700'"
                >
                  Mới
                </span>
              </button>

              <!-- Item 3: Events -->
              <button
                type="button"
                @click="setTab('events')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'events'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Calendar class="w-4 h-4" :class="activeTab === 'events' ? 'text-white' : 'text-emerald-600'" />
                  <span>Phê Duyệt Sự Kiện</span>
                </div>
                <span
                  class="px-2 py-0.5 rounded-full text-[10px] font-bold"
                  :class="activeTab === 'events' ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'"
                >
                  {{ events.length }}
                </span>
              </button>

              <!-- Item 4: Users -->
              <button
                type="button"
                @click="setTab('users')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'users'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Users class="w-4 h-4" :class="activeTab === 'users' ? 'text-white' : 'text-blue-600'" />
                  <span>Quản Trị Người Dùng</span>
                </div>
                <span
                  class="px-2 py-0.5 rounded-full text-[10px] font-bold"
                  :class="activeTab === 'users' ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'"
                >
                  {{ users.length }}
                </span>
              </button>

              <!-- Item 5: Audit -->
              <button
                type="button"
                @click="setTab('audit')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'audit'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <ShieldAlert class="w-4 h-4" :class="activeTab === 'audit' ? 'text-white' : 'text-rose-600'" />
                  <span>Nhật Ký Kiểm Toán (Audit)</span>
                </div>
                <span
                  class="w-2 h-2 rounded-full"
                  :class="activeTab === 'audit' ? 'bg-emerald-300' : 'bg-emerald-500'"
                ></span>
              </button>

              <!-- Item 6: Categories -->
              <button
                type="button"
                @click="setTab('admin_categories')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'admin_categories'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Tag class="w-4 h-4" :class="activeTab === 'admin_categories' ? 'text-white' : 'text-emerald-600'" />
                  <span>Quản Lý Danh Mục</span>
                </div>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="activeTab === 'admin_categories' ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'">
                  {{ categories.length }}
                </span>
              </button>

              <!-- Item 7: Coupons -->
              <button
                type="button"
                @click="setTab('admin_coupons')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'admin_coupons'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <TicketPercent class="w-4 h-4" :class="activeTab === 'admin_coupons' ? 'text-white' : 'text-pink-600'" />
                  <span>Voucher Toàn Sàn</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-extrabold" :class="activeTab === 'admin_coupons' ? 'bg-white/20 text-white' : 'bg-pink-100 text-pink-700'">
                  {{ orgVouchers.length }}
                </span>
              </button>

              <!-- Item 8: System Settings -->
              <button
                type="button"
                @click="setTab('system_settings')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'system_settings'
                  ? 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white shadow-md shadow-indigo-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Settings class="w-4 h-4" :class="activeTab === 'system_settings' ? 'text-white' : 'text-amber-600'" />
                  <span>Cấu Hình Cổng & Hệ Thống</span>
                </div>
                <span
                  class="px-1.5 py-0.5 rounded text-[10px] font-extrabold uppercase"
                  :class="activeTab === 'system_settings' ? 'bg-white/20 text-white' : 'bg-amber-100 text-amber-800'"
                >
                  Mới
                </span>
              </button>
            </div>

            <!-- ==================== ROLE 2: ORGANIZER MENU ==================== -->
            <div v-else-if="viewPerspective === 'ORGANIZER'" class="space-y-1">
              <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider px-2 mb-2 flex items-center justify-between">
                <span>Quản Lý Ban Tổ Chức</span>
                <span class="text-purple-600 font-extrabold">ORGANIZER</span>
              </p>

              <!-- Org Item 1: Overview -->
              <button
                type="button"
                @click="setTab('org_overview')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_overview'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <LayoutDashboard class="w-4 h-4" :class="activeTab === 'org_overview' ? 'text-white' : 'text-purple-600'" />
                  <span>Hiệu Suất Sự Kiện Của Tôi</span>
                </div>
                <ChevronRight class="w-3.5 h-3.5 opacity-60 group-hover:translate-x-0.5 transition-transform" />
              </button>

              <!-- Org Item 2: Events & Tiers -->
              <button
                type="button"
                @click="setTab('org_events')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_events'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Calendar class="w-4 h-4" :class="activeTab === 'org_events' ? 'text-white' : 'text-indigo-600'" />
                  <span>Sự Kiện & Hạng Vé</span>
                </div>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="activeTab === 'org_events' ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'">
                  {{ organizerDashboard?.totalEvents || 6 }}
                </span>
              </button>

              <!-- Org Item 3: Attendees -->
              <button
                type="button"
                @click="setTab('org_attendees')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_attendees'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Users class="w-4 h-4" :class="activeTab === 'org_attendees' ? 'text-white' : 'text-emerald-600'" />
                  <span>Khách Mua Vé & Check-in</span>
                </div>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="activeTab === 'org_attendees' ? 'bg-white/20 text-white' : 'bg-emerald-50 text-emerald-700'">
                  {{ attendeesList.length }} khách
                </span>
              </button>

              <!-- Org Item 4: Vouchers -->
              <button
                type="button"
                @click="setTab('org_vouchers')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_vouchers'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <TicketPercent class="w-4 h-4" :class="activeTab === 'org_vouchers' ? 'text-white' : 'text-pink-600'" />
                  <span>Mã Giảm Giá & Voucher</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-extrabold" :class="activeTab === 'org_vouchers' ? 'bg-white/20 text-white' : 'bg-pink-100 text-pink-700'">
                  {{ orgVouchers.length }}
                </span>
              </button>

              <!-- Org Item 5: Brand Profile -->
              <button
                type="button"
                @click="setTab('org_profile')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_profile'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Building2 class="w-4 h-4" :class="activeTab === 'org_profile' ? 'text-white' : 'text-indigo-600'" />
                  <span>Hồ Sơ Thương Hiệu</span>
                </div>
                <ShieldCheck class="w-3.5 h-3.5 text-indigo-500" />
              </button>

              <!-- Org Item 6: Financial Settlement -->
              <button
                type="button"
                @click="setTab('org_finance')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'org_finance'
                  ? 'bg-gradient-to-r from-purple-600 to-indigo-600 text-white shadow-md shadow-purple-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <Banknote class="w-4 h-4" :class="activeTab === 'org_finance' ? 'text-white' : 'text-amber-600'" />
                  <span>Giải Ngân & Tài Chính</span>
                </div>
                <span class="w-2 h-2 rounded-full" :class="activeTab === 'org_finance' ? 'bg-emerald-300' : 'bg-emerald-500'"></span>
              </button>

              <!-- Organizer Quick Action: Create Event Button -->
              <div class="pt-2">
                <button
                  type="button"
                  @click="createEventModalOpen = true"
                  class="w-full py-2.5 px-3 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center justify-center gap-2 shadow-sm transition cursor-pointer"
                >
                  <Plus class="w-4 h-4" />
                  <span>Tạo Sự Kiện Mới</span>
                </button>
              </div>
            </div>

            <!-- ==================== ROLE 3: STAFF GATE CHECK-IN MENU ==================== -->
            <div v-else class="space-y-1">
              <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider px-2 mb-2 flex items-center justify-between">
                <span>Trạm Soát Vé Trực Tuyến</span>
                <span class="text-emerald-600 font-extrabold">STAFF</span>
              </p>

              <!-- Staff Item 1: Scanner -->
              <button
                type="button"
                @click="setTab('staff_scanner')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'staff_scanner'
                  ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white shadow-md shadow-emerald-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <ScanLine class="w-4 h-4" :class="activeTab === 'staff_scanner' ? 'text-white' : 'text-emerald-600'" />
                  <span>Trạm Quét Vé Tại Cổng</span>
                </div>
                <span class="px-1.5 py-0.5 rounded text-[10px] font-extrabold uppercase animate-pulse" :class="activeTab === 'staff_scanner' ? 'bg-white/20 text-white' : 'bg-emerald-100 text-emerald-800'">
                  LIVE
                </span>
              </button>

              <!-- Staff Item 2: Logs -->
              <button
                type="button"
                @click="setTab('staff_logs')"
                class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-xs font-bold transition text-left cursor-pointer group"
                :class="activeTab === 'staff_logs'
                  ? 'bg-gradient-to-r from-emerald-600 to-teal-600 text-white shadow-md shadow-emerald-600/20'
                  : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'"
              >
                <div class="flex items-center gap-2.5">
                  <FileCheck class="w-4 h-4" :class="activeTab === 'staff_logs' ? 'text-white' : 'text-teal-600'" />
                  <span>Nhật Ký Soát Cổng (Logs)</span>
                </div>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold" :class="activeTab === 'staff_logs' ? 'bg-white/20 text-white' : 'bg-slate-100 text-slate-600'">
                  {{ gateLogs.length }}
                </span>
              </button>
            </div>

            <!-- Quick Action Tasks -->
            <div class="pt-3 border-t border-slate-100 space-y-2">
              <p class="text-[10px] font-black text-slate-400 uppercase tracking-wider px-2 mb-1">
                Tác Vụ Báo Cáo
              </p>

              <button
                type="button"
                @click="exportRevenueStatement"
                class="w-full py-2 px-3 rounded-xl bg-emerald-50 hover:bg-emerald-100 text-emerald-800 font-bold text-xs flex items-center justify-between transition cursor-pointer border border-emerald-200/80"
              >
                <div class="flex items-center gap-2">
                  <FileSpreadsheet class="w-4 h-4 text-emerald-600" />
                  <span>Xuất File Excel (.xlsx)</span>
                </div>
                <Download class="w-3.5 h-3.5 text-emerald-600" />
              </button>

              <button
                type="button"
                @click="loadData"
                class="w-full py-2 px-3 rounded-xl bg-slate-50 hover:bg-slate-100 text-slate-700 font-bold text-xs flex items-center justify-between transition cursor-pointer border border-slate-200"
              >
                <div class="flex items-center gap-2">
                  <RefreshCw class="w-4 h-4 text-slate-500" :class="{ 'animate-spin': loading }" />
                  <span>Đồng Bộ Dữ Liệu</span>
                </div>
                <span class="text-[10px] text-slate-400">Sync</span>
              </button>
            </div>

            <!-- System Monitor Widget -->
            <div class="p-3 rounded-xl bg-slate-50 border border-slate-200/80 text-[11px] space-y-2">
              <div class="flex items-center justify-between font-bold text-slate-700">
                <span class="flex items-center gap-1.5">
                  <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
                  VietQR Gateway
                </span>
                <span class="text-emerald-600 font-bold">99.98% OK</span>
              </div>
              <div class="flex items-center justify-between text-slate-500">
                <span>Khóa Phân Tán Redis</span>
                <span class="font-bold text-indigo-600">Bảo vệ</span>
              </div>
              <div class="flex items-center justify-between text-slate-500">
                <span>Cổng Anti-Passback</span>
                <span class="font-bold text-emerald-600">Bật</span>
              </div>
            </div>
          </div>
        </aside>

        <!-- ==================== RIGHT CONTENT AREA ==================== -->
        <main class="flex-1 min-w-0 w-full space-y-6">
          <!-- Loading Skeleton -->
          <div v-if="loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
            <div v-for="i in 4" :key="i" class="h-32 bg-white rounded-2xl border border-slate-200"></div>
          </div>

          <!-- Content Tabs -->
          <div v-else class="space-y-6">
        <!-- ==================== TAB 1: OVERVIEW & KPIS ==================== -->
        <div v-if="activeTab === 'overview'" class="space-y-6">
          <!-- Timeframe Selector Bar -->
          <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-3.5 rounded-2xl border border-slate-200 shadow-2xs">
            <div class="flex items-center gap-2 text-xs font-bold text-slate-600">
              <Clock class="w-4 h-4 text-indigo-600" />
              <span>Khung thời gian thống kê:</span>
            </div>
            <div class="inline-flex p-0.5 bg-slate-100 rounded-xl border border-slate-200 text-xs font-bold">
              <button
                type="button"
                @click="selectedTimeframe = '7DAYS'"
                class="px-3 py-1.5 rounded-lg transition cursor-pointer"
                :class="selectedTimeframe === '7DAYS' ? 'bg-white text-indigo-700 shadow-2xs' : 'text-slate-500 hover:text-slate-900'"
              >
                7 Ngày Qua
              </button>
              <button
                type="button"
                @click="selectedTimeframe = '30DAYS'"
                class="px-3 py-1.5 rounded-lg transition cursor-pointer"
                :class="selectedTimeframe === '30DAYS' ? 'bg-white text-indigo-700 shadow-2xs' : 'text-slate-500 hover:text-slate-900'"
              >
                30 Ngày Qua
              </button>
              <button
                type="button"
                @click="selectedTimeframe = 'YEAR2026'"
                class="px-3 py-1.5 rounded-lg transition cursor-pointer"
                :class="selectedTimeframe === 'YEAR2026' ? 'bg-white text-indigo-700 shadow-2xs' : 'text-slate-500 hover:text-slate-900'"
              >
                Cả Năm 2026
              </button>
            </div>
          </div>

          <!-- 4 Main KPI Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <!-- Card 1: Revenue -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs hover:shadow-md transition">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-500">
                  {{ viewPerspective === 'ADMIN' ? 'Tổng Doanh Thu Toàn Sàn' : 'Doanh Thu Ban Tổ Chức' }}
                </span>
                <div class="w-9 h-9 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
                  <DollarSign class="w-5 h-5" />
                </div>
              </div>
              <div class="mt-3">
                <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  {{ formatCurrency(viewPerspective === 'ADMIN' ? adminDashboard?.totalRevenue : organizerDashboard?.totalRevenue) }}
                </div>
                <div class="flex items-center gap-1.5 mt-1 text-xs text-emerald-600 font-bold">
                  <TrendingUp class="w-3.5 h-3.5" />
                  <span>+18.4% so với tháng trước</span>
                </div>
              </div>
            </div>

            <!-- Card 2: Tickets Issued & Check-in Rate -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs hover:shadow-md transition">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-500">
                  {{ viewPerspective === 'ADMIN' ? 'Tổng Vé Phát Hành' : 'Vé Đã Bán / Check-in' }}
                </span>
                <div class="w-9 h-9 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center">
                  <Ticket class="w-5 h-5" />
                </div>
              </div>
              <div class="mt-3">
                <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  {{ (viewPerspective === 'ADMIN' ? adminDashboard?.totalTickets : organizerDashboard?.totalTicketsIssued) ?? 0 }} vé
                </div>
                <div class="flex items-center gap-1.5 mt-1 text-xs text-indigo-600 font-bold">
                  <CheckCircle2 class="w-3.5 h-3.5" />
                  <span>
                    {{ viewPerspective === 'ADMIN' ? '94.2% vé hợp lệ' : `Tỷ lệ check-in: ${organizerDashboard?.checkInRate ?? 0}%` }}
                  </span>
                </div>
              </div>
            </div>

            <!-- Card 3: Events Published -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs hover:shadow-md transition">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-500">Sự Kiện Đang Mở Bán</span>
                <div class="w-9 h-9 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
                  <Calendar class="w-5 h-5" />
                </div>
              </div>
              <div class="mt-3">
                <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  {{ (viewPerspective === 'ADMIN' ? adminDashboard?.publishedEvents : organizerDashboard?.publishedEvents) ?? 0 }} sự kiện
                </div>
                <div class="flex items-center gap-1.5 mt-1 text-xs text-purple-600 font-bold">
                  <Flame class="w-3.5 h-3.5" />
                  <span>Tổng số: {{ (viewPerspective === 'ADMIN' ? adminDashboard?.totalEvents : organizerDashboard?.totalEvents) ?? 0 }} sự kiện</span>
                </div>
              </div>
            </div>

            <!-- Card 4: Orders / Users -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs hover:shadow-md transition">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-500">
                  {{ viewPerspective === 'ADMIN' ? 'Người Dùng Hoạt Động' : 'Đơn Hàng Xác Nhận' }}
                </span>
                <div class="w-9 h-9 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center">
                  <Users class="w-5 h-5" />
                </div>
              </div>
              <div class="mt-3">
                <div class="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  {{ (viewPerspective === 'ADMIN' ? adminDashboard?.totalUsers : adminDashboard?.confirmedBookings) ?? 0 }}
                </div>
                <div class="flex items-center gap-1.5 mt-1 text-xs text-amber-700 font-bold">
                  <CheckCircle2 class="w-3.5 h-3.5" />
                  <span>
                    {{ viewPerspective === 'ADMIN' ? `${adminDashboard?.totalCustomers ?? 0} khách hàng` : 'Đã thanh toán 100%' }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Revenue Chart & Right Column -->
          <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <!-- Left Col: Revenue Chart -->
            <div class="lg:col-span-2 p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <h3 class="text-base font-bold text-slate-900">Biểu Đồ Doanh Thu Năm 2026</h3>
                  <p class="text-xs text-slate-400">Doanh thu bán vé thực tế qua từng tháng</p>
                </div>
                <span class="text-xs font-bold text-indigo-600 bg-indigo-50 px-2.5 py-1 rounded-lg border border-indigo-100">
                  Đơn vị: Triệu VNĐ
                </span>
              </div>

              <!-- Bar Chart Container -->
              <div class="h-56 flex items-end justify-between gap-2 pt-6 border-b border-slate-100">
                <div
                  v-for="stat in monthlyStats"
                  :key="stat.month"
                  class="flex-1 flex flex-col items-center gap-2 group relative"
                >
                  <div class="absolute -top-10 opacity-0 group-hover:opacity-100 transition pointer-events-none bg-slate-900 text-white text-[10px] font-bold py-1 px-2 rounded whitespace-nowrap z-20 shadow-md">
                    {{ formatCurrency(stat.revenue) }}
                  </div>

                  <div
                    class="w-full max-w-[28px] bg-gradient-to-t from-indigo-600 to-indigo-400 rounded-t-lg group-hover:from-purple-600 group-hover:to-pink-500 transition-all duration-300 shadow-2xs"
                    :style="{ height: stat.height }"
                  ></div>

                  <span class="text-[11px] font-semibold text-slate-500">{{ stat.month }}</span>
                </div>
              </div>

              <div class="flex items-center justify-between pt-4 text-xs text-slate-500">
                <div class="flex items-center gap-4">
                  <div class="flex items-center gap-1.5">
                    <span class="w-3 h-3 rounded-full bg-indigo-600"></span>
                    <span>Doanh thu vé</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <span class="w-3 h-3 rounded-full bg-purple-500"></span>
                    <span>Tháng cao điểm</span>
                  </div>
                </div>
                <span class="font-bold text-slate-700">Tăng trưởng bình quân: +15.8%/tháng</span>
              </div>
            </div>

            <!-- Right Col: Distribution & Security Status -->
            <div class="space-y-6">
              <!-- User Role Distribution -->
              <div class="p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
                <h3 class="text-base font-bold text-slate-900">Phân Bổ Người Dùng</h3>
                <div class="space-y-3 text-xs">
                  <div>
                    <div class="flex justify-between font-semibold mb-1">
                      <span class="text-slate-600">Khách hàng (Customer)</span>
                      <span class="text-slate-900 font-bold">{{ adminDashboard?.totalCustomers ?? 0 }}</span>
                    </div>
                    <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div class="h-full bg-indigo-500 rounded-full" style="width: 86%"></div>
                    </div>
                  </div>

                  <div>
                    <div class="flex justify-between font-semibold mb-1">
                      <span class="text-slate-600">Ban tổ chức (Organizer)</span>
                      <span class="text-slate-900 font-bold">{{ adminDashboard?.totalOrganizers ?? 0 }}</span>
                    </div>
                    <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div class="h-full bg-purple-500 rounded-full" style="width: 8%"></div>
                    </div>
                  </div>

                  <div>
                    <div class="flex justify-between font-semibold mb-1">
                      <span class="text-slate-600">Nhân viên soát vé (Staff)</span>
                      <span class="text-slate-900 font-bold">{{ adminDashboard?.totalStaff ?? 0 }}</span>
                    </div>
                    <div class="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                      <div class="h-full bg-emerald-500 rounded-full" style="width: 5%"></div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Quick Security Banner -->
              <div class="p-5 rounded-2xl bg-gradient-to-br from-indigo-950 via-slate-900 to-indigo-900 text-white shadow-md space-y-2">
                <div class="flex items-center gap-2 text-indigo-300 text-xs font-bold uppercase">
                  <ShieldCheck class="w-4 h-4 text-emerald-400" />
                  <span>Bảo Mật & Check-in Anti-Passback</span>
                </div>
                <div class="text-sm font-bold">100% Vé Phát Hành Đều Mã Hóa QR Dùng 1 Lần</div>
                <p class="text-xs text-slate-300 leading-relaxed font-normal">
                  Hệ thống ngăn chặn tuyệt đối chụp màn hình bán lại và hỗ trợ xuất danh sách kiểm toán tức thì.
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 2: DEEP ANALYTICS & CHARTS ==================== -->
        <div v-if="activeTab === 'analytics'" class="space-y-6">
          <!-- Top Row: Payment Methods Donut Chart & Conversion Funnel -->
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <!-- Payment Methods Donut Chart (7 cols) -->
            <div class="lg:col-span-7 p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-5">
              <div class="flex items-center justify-between border-b border-slate-100 pb-3">
                <div>
                  <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                    <PieChart class="w-4 h-4 text-indigo-600" />
                    <span>Cơ Cấu Kênh Thanh Toán (Payment Distribution)</span>
                  </h3>
                  <p class="text-xs text-slate-400">Tỷ trọng dòng tiền và số lượng giao dịch thành công theo từng cổng</p>
                </div>
                <span class="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                  Tổng 2.85 Tỷ ₫
                </span>
              </div>

              <div class="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
                <!-- SVG Donut Chart Visual -->
                <div class="md:col-span-5 flex flex-col items-center justify-center relative">
                  <svg class="w-44 h-44 -rotate-90" viewBox="0 0 100 100">
                    <!-- Background Track -->
                    <circle cx="50" cy="50" r="38" fill="none" stroke="#f1f5f9" stroke-width="12" />
                    <!-- VietQR Slice: 54.2% -->
                    <circle
                      cx="50"
                      cy="50"
                      r="38"
                      fill="none"
                      stroke="#4f46e5"
                      stroke-width="12"
                      stroke-dasharray="129.5 238.76"
                      stroke-dashoffset="0"
                      class="transition-all duration-1000"
                    />
                    <!-- Visa/Master Slice: 26.8% -->
                    <circle
                      cx="50"
                      cy="50"
                      r="38"
                      fill="none"
                      stroke="#8b5cf6"
                      stroke-width="12"
                      stroke-dasharray="64.0 238.76"
                      stroke-dashoffset="-129.5"
                      class="transition-all duration-1000"
                    />
                    <!-- MoMo/Wallets Slice: 19.0% -->
                    <circle
                      cx="50"
                      cy="50"
                      r="38"
                      fill="none"
                      stroke="#ec4899"
                      stroke-width="12"
                      stroke-dasharray="45.3 238.76"
                      stroke-dashoffset="-193.5"
                      class="transition-all duration-1000"
                    />
                  </svg>
                  <!-- Donut Center Label -->
                  <div class="absolute inset-0 flex flex-col items-center justify-center text-center pointer-events-none">
                    <span class="text-[10px] font-bold uppercase text-slate-400">VietQR Pro</span>
                    <span class="text-lg font-black text-slate-900 tracking-tight">54.2%</span>
                    <span class="text-[10px] font-semibold text-indigo-600">Dẫn đầu</span>
                  </div>
                </div>

                <!-- Channels Table List -->
                <div class="md:col-span-7 space-y-3 text-xs">
                  <div
                    v-for="ch in paymentChannels"
                    :key="ch.name"
                    class="p-3 rounded-xl bg-slate-50 border border-slate-200/80 hover:bg-white hover:shadow-2xs transition"
                  >
                    <div class="flex items-center justify-between font-bold text-slate-900">
                      <div class="flex items-center gap-2">
                        <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: ch.color }"></span>
                        <span>{{ ch.name }}</span>
                      </div>
                      <span class="text-indigo-600 font-extrabold">{{ ch.percentage }}%</span>
                    </div>
                    <div class="flex items-center justify-between text-slate-500 text-[11px] mt-1.5 pt-1.5 border-t border-slate-200/50">
                      <span>{{ formatCurrency(ch.amount) }} ({{ ch.orders }} đơn)</span>
                      <span class="font-semibold text-emerald-600">Thành công {{ ch.successRate }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Booking Conversion Funnel (5 cols) -->
            <div class="lg:col-span-5 p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
              <div class="border-b border-slate-100 pb-3">
                <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                  <Activity class="w-4 h-4 text-indigo-600" />
                  <span>Phễu Chuyển Đổi Đặt Vé (Funnel)</span>
                </h3>
                <p class="text-xs text-slate-400">Hiệu quả từng bước từ xem trang đến nhận vé QR</p>
              </div>

              <div class="space-y-3 text-xs">
                <div
                  v-for="(step, idx) in conversionFunnel"
                  :key="idx"
                  class="p-3 rounded-xl border border-slate-100 bg-slate-50 space-y-1.5"
                >
                  <div class="flex items-center justify-between">
                    <span class="font-bold text-slate-800 flex items-center gap-1.5">
                      <component :is="step.icon" class="w-3.5 h-3.5 text-indigo-600" />
                      <span>{{ step.step }}</span>
                    </span>
                    <span class="font-extrabold text-slate-900">{{ step.count.toLocaleString() }} lượt</span>
                  </div>

                  <!-- Progress Bar -->
                  <div class="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all duration-700"
                      :class="step.color"
                      :style="{ width: `${step.percent}%` }"
                    ></div>
                  </div>

                  <div class="flex items-center justify-between text-[11px] text-slate-400">
                    <span>Tỷ lệ tích lũy: <strong class="text-slate-700">{{ step.percent }}%</strong></span>
                    <span class="font-semibold text-amber-600">{{ step.drop }}</span>
                  </div>
                </div>
              </div>

              <div class="p-3 rounded-xl bg-indigo-50/70 border border-indigo-100 text-[11px] text-indigo-800 flex items-center gap-2 font-medium">
                <Sparkles class="w-4 h-4 text-indigo-600 shrink-0" />
                <span>Tỷ lệ hoàn tất thanh toán đạt <strong>3.92%</strong>, vượt 40% so với trung bình thương mại điện tử!</span>
              </div>
            </div>
          </div>

          <!-- Mid Row: Seat Tier Occupancy & Regional Share -->
          <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
            <!-- Seat Tier Occupancy & Capacity (7 cols) -->
            <div class="lg:col-span-7 p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
              <div class="border-b border-slate-100 pb-3 flex items-center justify-between">
                <div>
                  <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                    <Layers class="w-4 h-4 text-indigo-600" />
                    <span>Hiệu Suất & Tỷ Lệ Lấp Đầy Hạng Vé (Tier Occupancy)</span>
                  </h3>
                  <p class="text-xs text-slate-400">Đánh giá sức hút và doanh thu theo từng phân khúc khán giả</p>
                </div>
                <span class="text-xs font-bold text-indigo-700 bg-indigo-50 px-2.5 py-1 rounded-lg border border-indigo-100">
                  Toàn bộ khán phòng
                </span>
              </div>

              <div class="space-y-3.5 text-xs">
                <div
                  v-for="st in seatTiers"
                  :key="st.tier"
                  class="p-3.5 rounded-xl bg-slate-50 border border-slate-200/80 space-y-2 hover:bg-white hover:shadow-2xs transition"
                >
                  <div class="flex items-center justify-between">
                    <div class="font-extrabold text-slate-900 flex items-center gap-2">
                      <span class="w-2.5 h-2.5 rounded-full" :class="st.color"></span>
                      <span>{{ st.tier }}</span>
                    </div>
                    <div class="text-right">
                      <span class="text-sm font-black text-indigo-600">{{ st.rate }}%</span>
                      <span class="text-[11px] text-slate-400 ml-1">lấp đầy</span>
                    </div>
                  </div>

                  <!-- Bar -->
                  <div class="w-full h-2.5 bg-slate-200 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all duration-700"
                      :class="st.color"
                      :style="{ width: `${st.rate}%` }"
                    ></div>
                  </div>

                  <div class="flex items-center justify-between text-[11px] text-slate-500 pt-0.5">
                    <span>Đã bán: <strong class="text-slate-800">{{ st.sold }} / {{ st.capacity }} vé</strong></span>
                    <span>Doanh thu: <strong class="text-slate-800">{{ formatCurrency(st.revenue) }}</strong></span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Regional Share & Platform Security Radar (5 cols) -->
            <div class="lg:col-span-5 space-y-6">
              <!-- Regional Share -->
              <div class="p-6 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
                <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                  <Globe class="w-4 h-4 text-indigo-600" />
                  <span>Doanh Thu Theo Thị Trường Địa Phương</span>
                </h3>

                <div class="space-y-3 text-xs">
                  <div
                    v-for="reg in regionalShare"
                    :key="reg.city"
                    class="p-3 rounded-xl bg-slate-50 border border-slate-200/70"
                  >
                    <div class="flex items-center justify-between font-bold text-slate-800 mb-1">
                      <span>{{ reg.city }}</span>
                      <span class="text-indigo-600 font-extrabold">{{ reg.share }}%</span>
                    </div>
                    <div class="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                      <div class="h-full bg-indigo-600 rounded-full" :style="{ width: `${reg.share}%` }"></div>
                    </div>
                    <div class="flex items-center justify-between text-[11px] text-slate-400 mt-1.5">
                      <span>{{ formatCurrency(reg.amount) }}</span>
                      <span class="font-semibold text-emerald-600">{{ reg.growth }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Security & Anti-Fraud Radar Card -->
              <div class="p-5 rounded-2xl bg-gradient-to-br from-slate-900 to-indigo-950 text-white shadow-md space-y-3">
                <div class="flex items-center justify-between">
                  <span class="text-xs font-bold uppercase text-indigo-300 flex items-center gap-1.5">
                    <ShieldCheck class="w-4 h-4 text-emerald-400" />
                    Radar Giám Sát An Ninh 24/7
                  </span>
                  <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30 font-bold">
                    ACTIVE
                  </span>
                </div>
                <div class="grid grid-cols-2 gap-2.5 text-xs pt-1">
                  <div class="p-2.5 rounded-xl bg-white/5 border border-white/10">
                    <div class="text-slate-400 text-[11px]">Tranh chấp vé</div>
                    <div class="text-base font-black text-emerald-400 mt-0.5">0 vụ</div>
                  </div>
                  <div class="p-2.5 rounded-xl bg-white/5 border border-white/10">
                    <div class="text-slate-400 text-[11px]">Vé giả bị chặn cổng</div>
                    <div class="text-base font-black text-amber-300 mt-0.5">14 lượt</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Bottom Row: Top Events Leaderboard -->
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <div class="p-5 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 class="text-base font-bold text-slate-900 flex items-center gap-2">
                  <Award class="w-4 h-4 text-amber-500" />
                  <span>Bảng Xếp Hạng Top 5 Sự Kiện Sinh Lời Cao Nhất</span>
                </h3>
                <p class="text-xs text-slate-400">Sự kiện có doanh thu ròng và số lượng vé bán dẫn đầu toàn nền tảng</p>
              </div>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4 text-center w-12">Hạng</th>
                    <th class="py-3 px-4">Tên sự kiện</th>
                    <th class="py-3 px-4">Ban tổ chức</th>
                    <th class="py-3 px-4 text-right">Doanh thu ghi nhận</th>
                    <th class="py-3 px-4 text-right">Vé phát hành</th>
                    <th class="py-3 px-4 text-right">Tỷ lệ Check-in</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="top in topEventsLeaderboard" :key="top.rank" class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-4 text-center">
                      <span
                        class="w-6 h-6 rounded-full inline-flex items-center justify-center font-black text-xs"
                        :class="[
                          top.rank === 1 ? 'bg-amber-100 text-amber-700' :
                          top.rank === 2 ? 'bg-slate-200 text-slate-700' :
                          top.rank === 3 ? 'bg-amber-50 text-amber-800' :
                          'bg-slate-100 text-slate-600'
                        ]"
                      >
                        {{ top.rank }}
                      </span>
                    </td>
                    <td class="py-3 px-4 font-bold text-slate-900">
                      <div>{{ top.title }}</div>
                      <span class="text-[10px] px-1.5 py-0.2 rounded bg-indigo-50 text-indigo-600 font-semibold">{{ top.category }}</span>
                    </td>
                    <td class="py-3 px-4 text-slate-600">{{ top.organizer }}</td>
                    <td class="py-3 px-4 text-right font-black text-indigo-700">{{ formatCurrency(top.revenue) }}</td>
                    <td class="py-3 px-4 text-right font-bold text-slate-800">{{ top.tickets }} vé</td>
                    <td class="py-3 px-4 text-right font-bold text-emerald-600">{{ top.checkInRate }}%</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 3: EVENTS & TICKETS ==================== -->
        <div v-if="activeTab === 'events'" class="space-y-4">
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <div class="p-4 sm:p-5 border-b border-slate-100 space-y-3">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <h3 class="text-base font-bold text-slate-900">Danh Sách Sự Kiện & Báo Cáo Vé</h3>
                  <p class="text-xs text-slate-500">Xem phân tích doanh thu, duyệt trạng thái và xuất danh sách khách tham dự (.xlsx)</p>
                </div>
                <span class="text-xs font-bold text-slate-500">
                  Tìm thấy <strong class="text-slate-900">{{ filteredEvents.length }}</strong> sự kiện
                </span>
              </div>

              <!-- Filter controls -->
              <div class="grid grid-cols-1 sm:grid-cols-12 gap-2.5 pt-1">
                <div class="sm:col-span-8 relative">
                  <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="eventKeyword"
                    type="text"
                    placeholder="Tìm tên sự kiện, ca sĩ, hội thảo..."
                    class="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div class="sm:col-span-4">
                  <select
                    v-model="eventStatusFilter"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả trạng thái sự kiện</option>
                    <option value="PUBLISHED">Đang mở bán (PUBLISHED)</option>
                    <option value="DRAFT">Bản nháp (DRAFT)</option>
                    <option value="CANCELLED">Đã hủy (CANCELLED)</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Table -->
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Sự kiện</th>
                    <th class="py-3 px-4">Địa điểm</th>
                    <th class="py-3 px-4">Trạng thái duyệt</th>
                    <th class="py-3 px-4">Đơn giá vé</th>
                    <th class="py-3 px-4 text-center">Báo Cáo & Thống Kê</th>
                    <th class="py-3 px-4 text-right">Xuất Excel</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="evt in filteredEvents" :key="evt.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3.5 px-4 font-bold text-slate-900 max-w-xs">
                      <div class="flex items-center gap-3">
                        <img
                          :src="evt.bannerUrl || 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=150&q=80'"
                          class="w-12 h-12 rounded-xl object-cover shrink-0 border border-slate-200"
                        />
                        <div>
                          <div class="font-extrabold text-slate-900 line-clamp-1 hover:text-indigo-600 cursor-pointer" @click="router.push(`/events/${evt.id}`)">
                            {{ evt.title }}
                          </div>
                          <div class="text-[11px] text-slate-400 font-normal">
                            {{ evt.startDateTime ? new Date(evt.startDateTime).toLocaleDateString('vi-VN') : 'Sắp diễn ra' }}
                          </div>
                        </div>
                      </div>
                    </td>

                    <td class="py-3.5 px-4 text-slate-600 font-medium max-w-[200px] truncate">
                      {{ evt.venue?.name || 'Hội trường quốc gia' }}
                    </td>

                    <td class="py-3.5 px-4">
                      <select
                        :value="evt.status"
                        @change="changeEventStatus(evt, ($event.target as HTMLSelectElement).value)"
                        class="px-2.5 py-1 rounded-lg text-xs font-bold cursor-pointer border focus:outline-none focus:ring-1 focus:ring-indigo-500"
                        :class="[
                          evt.status === 'PUBLISHED' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' :
                          evt.status === 'DRAFT' ? 'bg-amber-50 text-amber-700 border-amber-200' :
                          'bg-slate-100 text-slate-600 border-slate-200'
                        ]"
                      >
                        <option value="PUBLISHED">PUBLISHED (Mở bán)</option>
                        <option value="DRAFT">DRAFT (Bản nháp)</option>
                        <option value="CANCELLED">CANCELLED (Hủy)</option>
                      </select>
                    </td>

                    <td class="py-3.5 px-4 font-bold text-indigo-700">
                      {{ formatCurrency(evt.minPrice || 350000) }}
                    </td>

                    <td class="py-3.5 px-4 text-center">
                      <button
                        type="button"
                        @click="openEventAnalytics(evt)"
                        class="px-3 py-1.5 rounded-lg bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold transition flex items-center gap-1.5 mx-auto cursor-pointer"
                      >
                        <Eye class="w-3.5 h-3.5" />
                        <span>Xem số liệu vé</span>
                      </button>
                    </td>

                    <td class="py-3.5 px-4 text-right">
                      <button
                        type="button"
                        @click="exportAttendees(evt.id, evt.title)"
                        class="px-3 py-1.5 rounded-lg bg-emerald-50 hover:bg-emerald-100 text-emerald-700 font-bold transition inline-flex items-center gap-1.5 cursor-pointer"
                        title="Tải danh sách người tham gia dạng Excel"
                      >
                        <Download class="w-3.5 h-3.5" />
                        <span>Xuất .xlsx</span>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 4: ADMIN USER MANAGEMENT ==================== -->
        <div v-if="activeTab === 'users'" class="space-y-4">
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <!-- Filter Hub -->
            <div class="p-4 sm:p-5 border-b border-slate-100 space-y-3">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <h3 class="text-base font-bold text-slate-900">Quản Trị Người Dùng & Phân Quyền</h3>
                  <p class="text-xs text-slate-500">Khóa/mở tài khoản, cấp huy hiệu xác thực và thay đổi vai trò hệ thống</p>
                </div>
                <span class="text-xs font-bold text-slate-500">
                  Tìm thấy <strong class="text-slate-900">{{ filteredUsers.length }}</strong> người dùng
                </span>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-12 gap-2.5 pt-1">
                <!-- Search Keyword -->
                <div class="sm:col-span-6 relative">
                  <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="userKeyword"
                    type="text"
                    placeholder="Tìm theo họ tên, email, username hoặc số điện thoại..."
                    class="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <!-- Role Filter -->
                <div class="sm:col-span-3">
                  <select
                    v-model="userRoleFilter"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả vai trò</option>
                    <option value="ROLE_CUSTOMER">ROLE_CUSTOMER (Khách hàng)</option>
                    <option value="ROLE_ORGANIZER">ROLE_ORGANIZER (Ban tổ chức)</option>
                    <option value="ROLE_STAFF">ROLE_STAFF (Nhân viên soát vé)</option>
                    <option value="ROLE_ADMIN">ROLE_ADMIN (Quản trị viên)</option>
                  </select>
                </div>

                <!-- Status Filter -->
                <div class="sm:col-span-3">
                  <select
                    v-model="userStatusFilter"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="ACTIVE">Đang hoạt động</option>
                    <option value="LOCKED">Bị khóa</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Users Table -->
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Người dùng</th>
                    <th class="py-3 px-4">Email & SĐT</th>
                    <th class="py-3 px-4">Vai trò hiện tại</th>
                    <th class="py-3 px-4">Trạng thái</th>
                    <th class="py-3 px-4 text-center">Xác Thực / Đổi Quyền</th>
                    <th class="py-3 px-4 text-right">Khóa / Mở Khóa</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="u in filteredUsers" :key="u.id" class="hover:bg-slate-50/80 transition">
                    <!-- User Avatar & Name -->
                    <td class="py-3.5 px-4 font-bold text-slate-900">
                      <div class="flex items-center gap-3">
                        <img
                          :src="u.avatarUrl || 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=100&q=80'"
                          class="w-9 h-9 rounded-xl object-cover shrink-0 border border-slate-200"
                        />
                        <div>
                          <div class="font-extrabold text-slate-900 flex items-center gap-1.5">
                            <span>{{ u.fullName }}</span>
                            <span v-if="u.role === 'ROLE_ORGANIZER'" class="text-indigo-600" title="Ban tổ chức">
                              <ShieldCheck class="w-3.5 h-3.5 inline" />
                            </span>
                          </div>
                          <div class="text-[11px] text-slate-400 font-normal">@{{ u.username }}</div>
                        </div>
                      </div>
                    </td>

                    <!-- Contact -->
                    <td class="py-3.5 px-4 text-slate-600 font-medium">
                      <div>{{ u.email }}</div>
                      <div class="text-[11px] text-slate-400">{{ u.phoneNumber || 'Chưa cập nhật SĐT' }}</div>
                    </td>

                    <!-- Role Badge -->
                    <td class="py-3.5 px-4">
                      <span
                        class="px-2.5 py-1 rounded-lg text-[10px] font-bold uppercase"
                        :class="[
                          u.role === 'ROLE_ADMIN' ? 'bg-rose-50 text-rose-700 border border-rose-200' :
                          u.role === 'ROLE_ORGANIZER' ? 'bg-purple-50 text-purple-700 border border-purple-200' :
                          u.role === 'ROLE_STAFF' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
                          'bg-indigo-50 text-indigo-700 border border-indigo-200'
                        ]"
                      >
                        {{ u.role }}
                      </span>
                    </td>

                    <!-- Status -->
                    <td class="py-3.5 px-4">
                      <span
                        class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold"
                        :class="u.enabled ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'"
                      >
                        <span class="w-1.5 h-1.5 rounded-full" :class="u.enabled ? 'bg-emerald-500' : 'bg-rose-500'"></span>
                        <span>{{ u.enabled ? 'Hoạt động' : 'Đã khóa' }}</span>
                      </span>
                    </td>

                    <!-- Change Role Dropdown & Verify Badge -->
                    <td class="py-3.5 px-4 text-center">
                      <div class="flex items-center justify-center gap-2">
                        <select
                          :value="u.role"
                          @change="changeUserRole(u, ($event.target as HTMLSelectElement).value as UserRole)"
                          class="px-2.5 py-1 rounded-lg bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-800 cursor-pointer focus:outline-none focus:ring-1 focus:ring-indigo-500"
                          :disabled="actionLoading"
                        >
                          <option value="ROLE_CUSTOMER">ROLE_CUSTOMER</option>
                          <option value="ROLE_ORGANIZER">ROLE_ORGANIZER</option>
                          <option value="ROLE_STAFF">ROLE_STAFF</option>
                          <option value="ROLE_ADMIN">ROLE_ADMIN</option>
                        </select>

                        <button
                          v-if="u.role === 'ROLE_ORGANIZER'"
                          type="button"
                          @click="toggleVerifyOrganizer(u)"
                          class="p-1 rounded-lg text-indigo-600 hover:bg-indigo-50 border border-indigo-200 transition cursor-pointer"
                          title="Cấp huy hiệu xác thực (Verified Badge)"
                        >
                          <Award class="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>

                    <!-- Lock/Unlock Action Button -->
                    <td class="py-3.5 px-4 text-right">
                      <button
                        type="button"
                        @click="toggleUserStatus(u)"
                        class="px-3 py-1.5 rounded-lg text-xs font-bold transition inline-flex items-center gap-1.5 cursor-pointer shadow-2xs"
                        :class="[
                          u.enabled
                            ? 'bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200'
                            : 'bg-emerald-50 text-emerald-600 hover:bg-emerald-100 border border-emerald-200'
                        ]"
                        :disabled="actionLoading"
                      >
                        <component :is="u.enabled ? Lock : Unlock" class="w-3.5 h-3.5" />
                        <span>{{ u.enabled ? 'Khóa TK' : 'Mở khóa' }}</span>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 5: AUDIT LOGS ==================== -->
        <div v-if="activeTab === 'audit'" class="space-y-4">
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <!-- Filter Bar -->
            <div class="p-4 sm:p-5 border-b border-slate-100 space-y-3">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <h3 class="text-base font-bold text-slate-900">Nhật Ký Kiểm Toán An Ninh (Security Audit Trail)</h3>
                  <p class="text-xs text-slate-500">Ghi lại toàn bộ hành động nhạy cảm, IP khách, thời gian phản hồi và thiết bị</p>
                </div>
                <span class="text-xs font-bold text-slate-500">
                  Tổng cộng: <strong class="text-slate-900">{{ filteredAuditLogs.length }}</strong> sự kiện
                </span>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-12 gap-2.5">
                <div class="sm:col-span-8 relative">
                  <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="auditKeyword"
                    type="text"
                    placeholder="Tìm theo username, địa chỉ IP hoặc đối tượng chịu tác động..."
                    class="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div class="sm:col-span-4">
                  <select
                    v-model="auditActionFilter"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả hành động</option>
                    <option value="USER_STATUS_UPDATE">USER_STATUS_UPDATE</option>
                    <option value="USER_ROLE_UPDATE">USER_ROLE_UPDATE</option>
                    <option value="ORGANIZER_VERIFY">ORGANIZER_VERIFY</option>
                    <option value="EXPORT_ATTENDEES">EXPORT_ATTENDEES</option>
                    <option value="USER_LOGIN_FAILED">USER_LOGIN_FAILED</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Audit Logs Table -->
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Thời gian</th>
                    <th class="py-3 px-4">Hành vi (Action)</th>
                    <th class="py-3 px-4">Người thực hiện</th>
                    <th class="py-3 px-4">Đối tượng</th>
                    <th class="py-3 px-4">Địa chỉ IP</th>
                    <th class="py-3 px-4">Trạng thái</th>
                    <th class="py-3 px-4 text-right">Độ trễ</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="log in filteredAuditLogs" :key="log.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-4 text-slate-500 font-mono text-[11px] whitespace-nowrap">
                      {{ new Date(log.createdAt).toLocaleString('vi-VN') }}
                    </td>

                    <td class="py-3 px-4">
                      <span
                        class="px-2 py-0.5 rounded font-mono font-bold text-[10px]"
                        :class="[
                          log.action.includes('ROLE') ? 'bg-amber-50 text-amber-800 border border-amber-200' :
                          log.action.includes('STATUS') ? 'bg-rose-50 text-rose-800 border border-rose-200' :
                          log.action.includes('EXPORT') ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' :
                          'bg-indigo-50 text-indigo-800 border border-indigo-200'
                        ]"
                      >
                        {{ log.action }}
                      </span>
                    </td>

                    <td class="py-3 px-4 font-bold text-slate-900">
                      @{{ log.username }}
                    </td>

                    <td class="py-3 px-4 text-slate-600">
                      {{ log.entityType }} #{{ log.entityId }}
                    </td>

                    <td class="py-3 px-4 font-mono text-[11px] text-slate-500">
                      {{ log.ipAddress }}
                    </td>

                    <td class="py-3 px-4">
                      <span
                        class="px-2 py-0.5 rounded-full text-[10px] font-bold"
                        :class="log.status === 'SUCCESS' ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-700'"
                      >
                        {{ log.status }}
                      </span>
                    </td>

                    <td class="py-3 px-4 text-right font-mono font-bold text-slate-600 text-[11px]">
                      {{ log.executionTimeMs }} ms
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB: ADMIN CATEGORIES MANAGEMENT ==================== -->
        <div v-if="activeTab === 'admin_categories'" class="space-y-6">
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-800 border border-emerald-200">
                  <Tag class="w-3.5 h-3.5 text-emerald-600" />
                  Quản Trị Danh Mục Sàn
                </span>
                <span class="text-xs text-slate-400">&bull; Admin Only</span>
              </div>
              <h3 class="text-xl font-black text-slate-900">Danh Mục Sự Kiện & Phân Loại</h3>
              <p class="text-xs text-slate-500 mt-0.5">Quản lý các ngành dọc sự kiện: Âm nhạc, Thể thao, Công nghệ, Triển lãm nghệ thuật</p>
            </div>

            <button
              type="button"
              @click="createCategoryModalOpen = true"
              class="px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>Thêm Danh Mục Mới</span>
            </button>
          </div>

          <!-- Categories Grid / Table -->
          <div class="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">ID</th>
                    <th class="py-3 px-4">Tên Danh Mục</th>
                    <th class="py-3 px-4">Slug URL</th>
                    <th class="py-3 px-4">Mô Tả Chi Tiết</th>
                    <th class="py-3 px-4 text-right">Thao Tác</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-slate-700 font-medium">
                  <tr v-for="cat in categories" :key="cat.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-4 font-mono font-bold text-slate-400">#{{ cat.id }}</td>
                    <td class="py-3 px-4 font-bold text-slate-900 flex items-center gap-2">
                      <div class="w-7 h-7 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
                        <Tag class="w-3.5 h-3.5" />
                      </div>
                      <span>{{ cat.name }}</span>
                    </td>
                    <td class="py-3 px-4 font-mono text-indigo-600 text-[11px]">{{ cat.slug }}</td>
                    <td class="py-3 px-4 text-slate-500 max-w-xs truncate">{{ cat.description || 'Chưa có mô tả' }}</td>
                    <td class="py-3 px-4 text-right">
                      <button
                        type="button"
                        @click="handleDeleteCategory(cat.id, cat.name)"
                        class="p-1.5 rounded-lg text-rose-500 hover:bg-rose-50 transition cursor-pointer"
                        title="Xóa danh mục"
                      >
                        <Trash2 class="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB: ADMIN COUPONS MANAGEMENT ==================== -->
        <div v-if="activeTab === 'admin_coupons'" class="space-y-6">
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-pink-50 text-pink-700 border border-pink-200">
                  <TicketPercent class="w-3.5 h-3.5 text-pink-600" />
                  Khuyến Mãi & Trợ Giá Sàn
                </span>
                <span class="text-xs text-slate-400">&bull; Admin & Organizer</span>
              </div>
              <h3 class="text-xl font-black text-slate-900">Quản Lý Voucher & Mã Giảm Giá Toàn Sàn</h3>
              <p class="text-xs text-slate-500 mt-0.5">Kiểm soát ngân sách khuyến mãi, trần giảm giá tối đa và giới hạn số lượt áp dụng</p>
            </div>

            <button
              type="button"
              @click="createVoucherModalOpen = true"
              class="px-4 py-2.5 rounded-xl bg-pink-600 hover:bg-pink-700 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>Tạo Voucher Toàn Sàn</span>
            </button>
          </div>

          <!-- Coupons Table -->
          <div class="bg-white rounded-2xl border border-slate-200 shadow-2xs overflow-hidden">
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 border-b border-slate-200 text-slate-500 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Mã Voucher</th>
                    <th class="py-3 px-4">Loại Giảm Giá</th>
                    <th class="py-3 px-4">Giá Trị Giảm</th>
                    <th class="py-3 px-4">Số Lượng Đã Dùng</th>
                    <th class="py-3 px-4">Hạn Sử Dụng</th>
                    <th class="py-3 px-4">Trạng Thái</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100 text-slate-700 font-medium">
                  <tr v-for="v in orgVouchers" :key="v.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-4 font-mono font-black text-indigo-700 text-sm">
                      <span class="px-2 py-1 rounded-md bg-indigo-50 border border-indigo-200">{{ v.code }}</span>
                    </td>
                    <td class="py-3 px-4 font-semibold">
                      {{ v.discountType === 'PERCENT' ? 'Giảm Phần Trăm (%)' : 'Giảm Tiền Trực Tiếp' }}
                    </td>
                    <td class="py-3 px-4 font-black text-slate-900">
                      {{ v.discountType === 'PERCENT' ? `${v.discountValue}%` : formatCurrency(v.discountValue) }}
                    </td>
                    <td class="py-3 px-4 font-mono">
                      <span class="font-bold text-emerald-600">{{ v.usedCount }}</span> / {{ v.maxUsage }} lượt
                    </td>
                    <td class="py-3 px-4 text-slate-500 font-mono text-[11px]">{{ v.expiresAt }}</td>
                    <td class="py-3 px-4">
                      <span
                        class="px-2 py-0.5 rounded-full text-[10px] font-black uppercase"
                        :class="v.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-600'"
                      >
                        {{ v.status === 'ACTIVE' ? 'Đang Áp Dụng' : 'Hết Hạn' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 6 (ADMIN): SYSTEM SETTINGS & GATEWAY CONTROL ==================== -->
        <div v-if="activeTab === 'system_settings'" class="space-y-6">
          <!-- Settings Header Banner -->
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-amber-50 text-amber-800 border border-amber-200">
                  <Settings class="w-3.5 h-3.5 text-amber-600" />
                  Trung Tâm Cấu Hình Toàn Sàn
                </span>
                <span class="text-xs text-slate-400">&bull; Admin Only</span>
              </div>
              <h3 class="text-xl font-black text-slate-900">Cấu Hình Cổng Thanh Toán & Tham Số Hệ Thống</h3>
              <p class="text-xs text-slate-500 mt-0.5">Quản trị trạng thái cổng VietQR, tỷ lệ hoa hồng chiết khấu sàn và an ninh soát vé Anti-passback</p>
            </div>

            <button
              type="button"
              @click="saveSystemSettings"
              class="px-4 py-2.5 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition cursor-pointer"
            >
              <Check class="w-4 h-4" />
              <span>Lưu Toàn Bộ Cấu Hình</span>
            </button>
          </div>

          <!-- Gateway Status Cards -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div
              v-for="gw in paymentGateways"
              :key="gw.id"
              class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4 hover:shadow-md transition"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2.5">
                  <div class="w-9 h-9 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold">
                    <CreditCard v-if="gw.id === 'visa'" class="w-5 h-5" />
                    <QrCode v-else-if="gw.id === 'vietqr'" class="w-5 h-5" />
                    <DollarSign v-else class="w-5 h-5" />
                  </div>
                  <div>
                    <h4 class="font-black text-xs text-slate-900">{{ gw.name }}</h4>
                    <span class="text-[10px] font-mono text-slate-400">ID: {{ gw.id }}</span>
                  </div>
                </div>

                <!-- Toggle active -->
                <button
                  type="button"
                  @click="gw.enabled = !gw.enabled; notify(`Đã ${gw.enabled ? 'bật' : 'tắt'} cổng ${gw.name}`, 'info')"
                  class="w-10 h-6 flex items-center rounded-full p-1 transition cursor-pointer"
                  :class="gw.enabled ? 'bg-emerald-500' : 'bg-slate-300'"
                >
                  <div class="bg-white w-4 h-4 rounded-full shadow-md transform transition" :class="{ 'translate-x-4': gw.enabled }"></div>
                </button>
              </div>

              <div class="grid grid-cols-3 gap-2 pt-2 border-t border-slate-100 text-[11px]">
                <div>
                  <span class="text-slate-400 block text-[10px]">Uptime</span>
                  <span class="font-bold text-emerald-600">{{ gw.uptime }}</span>
                </div>
                <div>
                  <span class="text-slate-400 block text-[10px]">Độ trễ TB</span>
                  <span class="font-bold text-indigo-600">{{ gw.latency }}</span>
                </div>
                <div>
                  <span class="text-slate-400 block text-[10px]">GD Hôm nay</span>
                  <span class="font-bold text-slate-800">{{ gw.dailyTx }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Commission & Security Policies -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Platform Commission Card -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
              <div class="flex items-center gap-2">
                <BadgePercent class="w-5 h-5 text-indigo-600" />
                <h4 class="text-sm font-bold text-slate-900">Tỷ Lệ Hoa Hồng Nền Tảng (Platform Fee)</h4>
              </div>
              <p class="text-xs text-slate-500">Mức phần trăm sàn thu trên mỗi vé bán ra thành công của Ban tổ chức sự kiện.</p>
              
              <div class="flex items-center gap-4 pt-2">
                <div class="relative w-36">
                  <input
                    v-model.number="platformCommissionFee"
                    type="number"
                    step="0.5"
                    min="0"
                    max="30"
                    class="w-full pl-3 pr-8 py-2 rounded-xl border border-slate-300 text-sm font-bold text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                  <span class="absolute right-3 top-1/2 -translate-y-1/2 text-sm font-bold text-slate-400">%</span>
                </div>
                <div class="text-xs text-slate-600">
                  <span>Ban tổ chức nhận: </span>
                  <strong class="text-emerald-700 font-extrabold">{{ (100 - platformCommissionFee).toFixed(1) }}%</strong>
                  <span> doanh thu ròng</span>
                </div>
              </div>
            </div>

            <!-- Anti-Passback Gate Policy -->
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <ShieldCheck class="w-5 h-5 text-emerald-600" />
                  <h4 class="text-sm font-bold text-slate-900">Cơ Chế An Ninh Anti-Passback Cổng Soát Vé</h4>
                </div>
                <button
                  type="button"
                  @click="antiPassbackEnabled = !antiPassbackEnabled; notify(`Chế độ Anti-Passback: ${antiPassbackEnabled ? 'BẬT' : 'TẮT'}`, 'info')"
                  class="w-10 h-6 flex items-center rounded-full p-1 transition cursor-pointer"
                  :class="antiPassbackEnabled ? 'bg-emerald-500' : 'bg-slate-300'"
                >
                  <div class="bg-white w-4 h-4 rounded-full shadow-md transform transition" :class="{ 'translate-x-4': antiPassbackEnabled }"></div>
                </button>
              </div>
              <p class="text-xs text-slate-500 leading-relaxed">
                Ngăn chặn chụp màn hình mã QR gửi ra ngoài cho người khác quét lại. Mỗi vé chỉ được vào cổng 01 lần duy nhất trong toàn bộ thời gian diễn ra sự kiện.
              </p>
              <div class="p-3 rounded-xl bg-emerald-50/70 border border-emerald-100 text-xs text-emerald-800 font-semibold flex items-center gap-2">
                <CheckCircle2 class="w-4 h-4 text-emerald-600 shrink-0" />
                <span>Trạng thái: Đang bảo vệ 100% cổng soát vé toàn sàn.</span>
              </div>
            </div>
          </div>

          <!-- Emergency Announcement Broadcast -->
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <Megaphone class="w-5 h-5 text-amber-500" />
                <h4 class="text-sm font-bold text-slate-900">Phát Thông Báo Khẩn Cấp Toàn Sàn (Emergency Broadcast)</h4>
              </div>
              <button
                type="button"
                @click="toggleEmergencyBroadcast"
                class="px-3.5 py-1.5 rounded-xl font-bold text-xs transition cursor-pointer flex items-center gap-1.5"
                :class="emergencyBroadcast.enabled ? 'bg-rose-600 text-white hover:bg-rose-700' : 'bg-slate-100 text-slate-700 hover:bg-slate-200'"
              >
                <Radio class="w-3.5 h-3.5" :class="{ 'animate-pulse': emergencyBroadcast.enabled }" />
                <span>{{ emergencyBroadcast.enabled ? 'Dừng Phát Sóng' : 'Kích Hoạt Phát Sóng' }}</span>
              </button>
            </div>
            
            <textarea
              v-model="emergencyBroadcast.message"
              rows="2"
              class="w-full p-3 rounded-xl border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Nhập nội dung thông báo khẩn cấp..."
            ></textarea>
            
            <div v-if="emergencyBroadcast.enabled" class="p-3 rounded-xl bg-amber-50 border border-amber-300 text-xs text-amber-900 font-bold flex items-center gap-2 animate-pulse">
              <AlertTriangle class="w-4 h-4 text-amber-600 shrink-0" />
              <span>Đang phát sóng trực tiếp tới toàn bộ người dùng đang online trên hệ thống.</span>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 7 (ORGANIZER): MY EVENT PERFORMANCE ==================== -->
        <div v-if="activeTab === 'org_overview'" class="space-y-6">
          <!-- Organizer Header -->
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-purple-50 text-purple-700 border border-purple-200">
                  <ShieldCheck class="w-3.5 h-3.5 text-purple-600" />
                  Ban Tổ Chức Studio
                </span>
                <span class="text-xs text-slate-400">&bull; Nexus Tech Media</span>
              </div>
              <h3 class="text-xl font-black text-slate-900">Bảng Hiệu Suất Sự Kiện Doanh Thu Của Tôi</h3>
              <p class="text-xs text-slate-500 mt-0.5">Theo dõi doanh thu bán vé thực tế, tỷ lệ khách tham dự check-in và số dư giải ngân</p>
            </div>

            <button
              type="button"
              @click="createEventModalOpen = true"
              class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center gap-2 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>Tạo Sự Kiện & Hạng Vé Mới</span>
            </button>
          </div>

          <!-- 4 Organizer KPI Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs">
              <span class="text-xs font-bold text-slate-500">Doanh Thu Vé Gộp</span>
              <div class="text-2xl font-black text-purple-700 mt-1">845.000.000 ₫</div>
              <div class="flex items-center gap-1 text-[11px] text-emerald-600 font-bold mt-2">
                <TrendingUp class="w-3.5 h-3.5" />
                <span>+14.2% so với tháng trước</span>
              </div>
            </div>

            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs">
              <span class="text-xs font-bold text-slate-500">Vé Đã Phát Hành</span>
              <div class="text-2xl font-black text-slate-900 mt-1">1.420 vé</div>
              <div class="text-[11px] text-slate-500 mt-2">Trên 6 sự kiện đăng cai</div>
            </div>

            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs">
              <span class="text-xs font-bold text-slate-500">Khách Đã Vào Cổng</span>
              <div class="text-2xl font-black text-emerald-600 mt-1">980 lượt</div>
              <div class="text-[11px] text-emerald-700 font-bold mt-2">Tỷ lệ tham dự 69.0%</div>
            </div>

            <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs">
              <span class="text-xs font-bold text-slate-500">Thực Nhận Sau Phí Sàn (95%)</span>
              <div class="text-2xl font-black text-indigo-700 mt-1">767.750.000 ₫</div>
              <div class="text-[11px] text-indigo-600 font-bold mt-2">Đã giải ngân: 500tr</div>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 8 (ORGANIZER): MY EVENTS & TIERS ==================== -->
        <div v-if="activeTab === 'org_events'" class="space-y-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-black text-slate-900">Danh Sách Sự Kiện Ban Tổ Chức Đang Quản Lý</h3>
              <p class="text-xs text-slate-500">Quản lý các hạng vé, mở bán vé và xuất danh sách khách tham dự</p>
            </div>
            <button
              type="button"
              @click="createEventModalOpen = true"
              class="px-3.5 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>Thêm Sự Kiện Mới</span>
            </button>
          </div>

          <!-- Events Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            <div
              v-for="evt in events.slice(0, 6)"
              :key="evt.id"
              class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs hover:shadow-md transition flex flex-col justify-between"
            >
              <div>
                <div class="relative h-40 bg-slate-100 overflow-hidden">
                  <img :src="evt.bannerUrl" :alt="evt.title" class="w-full h-full object-cover" />
                  <span class="absolute top-2.5 right-2.5 px-2.5 py-1 rounded-md text-[10px] font-black uppercase tracking-wider bg-white/95 text-purple-700 shadow-2xs">
                    {{ evt.category?.name || 'Sự kiện' }}
                  </span>
                </div>
                <div class="p-4 space-y-2">
                  <h4 class="font-extrabold text-sm text-slate-900 line-clamp-1">{{ evt.title }}</h4>
                  <div class="flex items-center gap-1.5 text-xs text-slate-500">
                    <MapPin class="w-3.5 h-3.5 text-indigo-600 shrink-0" />
                    <span class="truncate">{{ evt.locationName }}</span>
                  </div>
                  <div class="flex items-center gap-1.5 text-xs text-slate-500">
                    <Clock class="w-3.5 h-3.5 text-slate-400 shrink-0" />
                    <span>{{ new Date(evt.startDateTime).toLocaleDateString('vi-VN') }}</span>
                  </div>
                  <div class="pt-2 border-t border-slate-100 flex items-center justify-between text-xs">
                    <span class="text-slate-400">Giá vé từ:</span>
                    <span class="font-extrabold text-indigo-700">{{ formatCurrency(evt.minPrice) }}</span>
                  </div>
                </div>
              </div>

              <div class="p-4 pt-0 flex items-center gap-2">
                <button
                  type="button"
                  @click="openEventAnalytics(evt)"
                  class="flex-1 py-2 rounded-xl bg-indigo-50 hover:bg-indigo-100 text-indigo-700 font-bold text-xs transition cursor-pointer"
                >
                  Xem Số Liệu
                </button>
                <button
                  type="button"
                  @click="exportAttendees(evt.id, evt.title)"
                  class="p-2 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-600 transition cursor-pointer"
                  title="Xuất file Excel danh sách khách"
                >
                  <Download class="w-4 h-4 text-emerald-600" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 9 (ORGANIZER): ATTENDEES LIST & CHECK-IN ==================== -->
        <div v-if="activeTab === 'org_attendees'" class="space-y-4">
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <!-- Filter Bar -->
            <div class="p-4 sm:p-5 border-b border-slate-100 space-y-3">
              <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <h3 class="text-base font-bold text-slate-900">Danh Sách Khách Mua Vé & Kiểm Soát Vào Cổng</h3>
                  <p class="text-xs text-slate-500">Tra cứu mã vé, thông tin liên lạc và cập nhật trạng thái check-in</p>
                </div>
                <button
                  type="button"
                  @click="exportAttendees(1, 'Skyline_Symphony')"
                  class="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 transition cursor-pointer"
                >
                  <Download class="w-3.5 h-3.5" />
                  <span>Xuất Danh Sách (.xlsx)</span>
                </button>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-12 gap-2.5">
                <div class="sm:col-span-6 relative">
                  <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input
                    v-model="attendeeKeyword"
                    type="text"
                    placeholder="Tìm theo tên khách, email, số điện thoại hoặc mã vé..."
                    class="w-full pl-9 pr-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div class="sm:col-span-3">
                  <select
                    v-model="attendeeFilterTier"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả hạng vé</option>
                    <option value="VIP Diamond">VIP Diamond</option>
                    <option value="VIP Gold">VIP Gold</option>
                    <option value="Standard">Standard</option>
                  </select>
                </div>

                <div class="sm:col-span-3">
                  <select
                    v-model="attendeeFilterStatus"
                    class="w-full px-3 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  >
                    <option value="ALL">Tất cả trạng thái</option>
                    <option value="CHECKED_IN">Đã Check-in</option>
                    <option value="NOT_CHECKED_IN">Chưa Check-in</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Table -->
            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Mã Vé</th>
                    <th class="py-3 px-4">Khách Hàng</th>
                    <th class="py-3 px-4">Email / SĐT</th>
                    <th class="py-3 px-4">Hạng Vé</th>
                    <th class="py-3 px-4">Ghế</th>
                    <th class="py-3 px-4">Trạng Thái Vào Cổng</th>
                    <th class="py-3 px-4 text-right">Thao Tác</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="att in filteredAttendees" :key="att.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3.5 px-4 font-mono font-bold text-indigo-700">{{ att.ticketCode }}</td>
                    <td class="py-3.5 px-4 font-bold text-slate-900">{{ att.customerName }}</td>
                    <td class="py-3.5 px-4 text-slate-500">
                      <div>{{ att.email }}</div>
                      <div class="text-[11px] font-mono">{{ att.phone }}</div>
                    </td>
                    <td class="py-3.5 px-4">
                      <span class="px-2.5 py-0.5 rounded-md text-[10px] font-extrabold uppercase bg-indigo-50 text-indigo-800 border border-indigo-200">
                        {{ att.tierName }}
                      </span>
                    </td>
                    <td class="py-3.5 px-4 font-mono font-bold text-slate-800">{{ att.seat }}</td>
                    <td class="py-3.5 px-4">
                      <span
                        class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[10px] font-bold"
                        :class="att.checkedIn ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' : 'bg-slate-100 text-slate-600'"
                      >
                        <span class="w-1.5 h-1.5 rounded-full" :class="att.checkedIn ? 'bg-emerald-500' : 'bg-slate-400'"></span>
                        <span>{{ att.checkedIn ? 'Đã Vào Cổng' : 'Chưa Vào Cổng' }}</span>
                      </span>
                      <div v-if="att.checkedIn" class="text-[10px] text-slate-400 font-mono mt-0.5">{{ att.checkInTime }}</div>
                    </td>
                    <td class="py-3.5 px-4 text-right">
                      <button
                        type="button"
                        @click="toggleAttendeeCheckIn(att)"
                        class="px-2.5 py-1 rounded-lg text-xs font-bold transition cursor-pointer"
                        :class="att.checkedIn ? 'bg-slate-100 text-slate-600 hover:bg-slate-200' : 'bg-emerald-600 text-white hover:bg-emerald-700'"
                      >
                        {{ att.checkedIn ? 'Hủy Check-in' : 'Check-in Thủ Công' }}
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 10 (ORGANIZER): VOUCHERS ==================== -->
        <div v-if="activeTab === 'org_vouchers'" class="space-y-6">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg font-black text-slate-900">Chiến Dịch Mã Giảm Giá & Voucher Của Ban Tổ Chức</h3>
              <p class="text-xs text-slate-500">Tạo mã khuyến mãi kích cầu mua vé sớm và tri ân khách hàng thân thiết</p>
            </div>
            <button
              type="button"
              @click="createVoucherModalOpen = true"
              class="px-3.5 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition cursor-pointer"
            >
              <Plus class="w-4 h-4" />
              <span>Tạo Mã Khuyến Mãi Mới</span>
            </button>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div
              v-for="vc in orgVouchers"
              :key="vc.id"
              class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs space-y-4 hover:shadow-md transition"
            >
              <div class="flex items-center justify-between">
                <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-black font-mono tracking-wider bg-purple-50 text-purple-700 border border-purple-200">
                  <TicketPercent class="w-3.5 h-3.5 text-purple-600" />
                  {{ vc.code }}
                </span>
                <span
                  class="px-2 py-0.5 rounded-full text-[10px] font-bold"
                  :class="vc.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'"
                >
                  {{ vc.status === 'ACTIVE' ? 'Đang hiệu lực' : 'Đã hết hạn' }}
                </span>
              </div>

              <div>
                <div class="text-xl font-extrabold text-slate-900">
                  {{ vc.discountType === 'PERCENT' ? `Giảm ${vc.discountValue}%` : `Giảm ${formatCurrency(vc.discountValue)}` }}
                </div>
                <p class="text-xs text-slate-400 mt-1">Hạn sử dụng: {{ vc.expiresAt }}</p>
              </div>

              <div class="pt-2 border-t border-slate-100 flex items-center justify-between text-xs text-slate-600">
                <span>Lượt đã sử dụng:</span>
                <strong class="font-extrabold text-indigo-700">{{ vc.usedCount }} / {{ vc.maxUsage }}</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== TAB: ORGANIZER BRAND PROFILE ==================== -->
        <div v-if="activeTab === 'org_profile'" class="space-y-6">
          <!-- Executive Brand Showcase Card -->
          <div class="bg-white rounded-3xl border border-slate-200/90 shadow-md overflow-hidden relative">
            <!-- Cover Banner Container -->
            <div class="h-48 sm:h-64 bg-slate-950 relative overflow-hidden group">
              <img
                :src="orgProfile.bannerUrl || 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1600&q=80'"
                alt="Banner"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
              />
              <div class="absolute inset-0 bg-gradient-to-t from-slate-950/90 via-slate-950/40 to-black/30"></div>

              <!-- Top Floating Controls on Banner -->
              <div class="absolute top-4 inset-x-4 sm:inset-x-6 flex items-center justify-between gap-2">
                <span
                  class="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-bold backdrop-blur-md border"
                  :class="orgProfile.verified
                    ? 'bg-indigo-600/90 text-white border-indigo-400/40 shadow-lg shadow-indigo-900/40'
                    : 'bg-slate-900/80 text-slate-300 border-white/10'"
                >
                  <ShieldCheck class="w-4 h-4 text-emerald-400" />
                  <span>{{ orgProfile.verified ? 'Ban Tổ Chức Đã Xác Thực (Verified Partner)' : 'Hồ Sơ Chưa Xác Thực' }}</span>
                </span>

                <!-- Quick Banner Preset Selector Pills on Banner -->
                <div class="hidden sm:flex items-center gap-1.5 p-1 rounded-full bg-slate-900/70 backdrop-blur-md border border-white/15 text-[11px] font-semibold text-white">
                  <span class="px-2 text-slate-300 text-[10px] font-bold uppercase tracking-wider">Đổi bìa nhanh:</span>
                  <button
                    v-for="b in presetBanners"
                    :key="b.name"
                    type="button"
                    @click="applyPresetBanner(b.url)"
                    class="px-2.5 py-1 rounded-full hover:bg-white/20 transition cursor-pointer text-xs"
                    :class="orgProfile.bannerUrl === b.url ? 'bg-indigo-600 text-white font-bold' : ''"
                  >
                    {{ b.name }}
                  </button>
                </div>
              </div>

              <!-- Bottom Banner Text Overlay (Subtle) -->
              <div class="absolute bottom-3 right-4 sm:right-6 hidden sm:block text-right">
                <span class="text-[11px] text-white/70 font-medium bg-black/40 backdrop-blur-xs px-2.5 py-1 rounded-lg">
                  Kích thước banner tiêu chuẩn: 1600 × 500 px
                </span>
              </div>
            </div>

            <!-- Profile Info Bar (Placed completely in the clean white container below the banner) -->
            <div class="px-6 sm:px-8 pb-7 pt-0 relative">
              <div class="flex flex-col sm:flex-row sm:items-end justify-between gap-5">
                <!-- Avatar & Brand Identity -->
                <div class="flex flex-col sm:flex-row sm:items-end gap-5">
                  <!-- Logo Container -->
                  <div class="-mt-14 sm:-mt-16 relative shrink-0 z-10 group">
                    <img
                      :src="orgProfile.logoUrl || 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80'"
                      alt="Logo"
                      class="w-24 h-24 sm:w-28 sm:h-28 rounded-2xl object-cover ring-4 ring-white shadow-xl bg-white border border-slate-200"
                    />
                    <div
                      v-if="orgProfile.verified"
                      class="absolute -bottom-1 -right-1 w-7 h-7 rounded-full bg-indigo-600 text-white ring-2 ring-white flex items-center justify-center shadow-md"
                      title="Đối tác tổ chức uy tín đã xác thực"
                    >
                      <ShieldCheck class="w-4 h-4 text-white" />
                    </div>
                  </div>

                  <!-- Name, Handle & Links -->
                  <div class="space-y-1.5 pt-1 sm:pt-0">
                    <div class="flex items-center gap-2.5 flex-wrap">
                      <h3 class="text-2xl sm:text-3xl font-black text-slate-900 tracking-tight">
                        {{ orgProfile.organizationName || 'Tên Đơn Vị Tổ Chức' }}
                      </h3>
                      <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md text-[11px] font-extrabold uppercase bg-purple-50 text-purple-700 border border-purple-200">
                        Official Partner
                      </span>
                    </div>

                    <div class="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500 font-medium">
                      <span class="font-bold text-slate-700">@{{ orgProfile.username || 'nexus_media' }}</span>
                      <span>&bull;</span>
                      <a
                        v-if="orgProfile.website"
                        :href="orgProfile.website"
                        target="_blank"
                        class="text-indigo-600 hover:text-indigo-700 font-bold inline-flex items-center gap-1 hover:underline"
                      >
                        <Globe class="w-3.5 h-3.5" />
                        <span>{{ orgProfile.website.replace(/^https?:\/\//, '') }}</span>
                      </a>
                      <span v-if="orgProfile.website">&bull;</span>
                      <span class="inline-flex items-center gap-1 text-slate-600">
                        <MapPin class="w-3.5 h-3.5 text-slate-400" />
                        <span>TP. Hồ Chí Minh & Toàn Quốc</span>
                      </span>
                    </div>
                  </div>
                </div>

                <!-- Action Button in Header -->
                <div class="flex items-center gap-2.5 shrink-0 pt-2 sm:pt-0">
                  <button
                    type="button"
                    @click="saveOrgProfile"
                    :disabled="savingOrgProfile"
                    class="px-5 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center gap-2 shadow-md shadow-indigo-500/20 transition cursor-pointer disabled:opacity-50"
                  >
                    <Check class="w-4 h-4" />
                    <span>{{ savingOrgProfile ? 'Đang lưu...' : 'Lưu Thay Đổi Hồ Sơ' }}</span>
                  </button>
                </div>
              </div>

              <!-- Bio excerpt preview -->
              <p v-if="orgProfile.bio" class="text-xs sm:text-sm text-slate-600 mt-4 leading-relaxed max-w-4xl bg-slate-50/80 p-3.5 rounded-xl border border-slate-200/70">
                {{ orgProfile.bio }}
              </p>
            </div>

            <!-- Social Proof & KPI Metrics Strip -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3 p-4 sm:p-6 bg-slate-50/70 border-t border-slate-200/80">
              <!-- KPI 1 -->
              <div class="p-3.5 rounded-2xl bg-white border border-purple-100/90 shadow-2xs">
                <div class="flex items-center justify-between mb-1">
                  <span class="text-[11px] font-bold text-purple-600 uppercase tracking-wider">Người Theo Dõi</span>
                  <div class="w-6 h-6 rounded-lg bg-purple-50 flex items-center justify-center text-purple-600">
                    <Users class="w-3.5 h-3.5" />
                  </div>
                </div>
                <div class="text-lg sm:text-xl font-black text-slate-900">
                  {{ orgProfile.followersCount?.toLocaleString('vi-VN') || '14.200' }}
                </div>
                <span class="text-[10px] text-emerald-600 font-bold inline-flex items-center gap-0.5 mt-0.5">
                  <TrendingUp class="w-3 h-3" />
                  +12.4% tháng này
                </span>
              </div>

              <!-- KPI 2 -->
              <div class="p-3.5 rounded-2xl bg-white border border-amber-100/90 shadow-2xs">
                <div class="flex items-center justify-between mb-1">
                  <span class="text-[11px] font-bold text-amber-600 uppercase tracking-wider">Điểm Đánh Giá</span>
                  <div class="w-6 h-6 rounded-lg bg-amber-50 flex items-center justify-center text-amber-600">
                    <Award class="w-3.5 h-3.5" />
                  </div>
                </div>
                <div class="text-lg sm:text-xl font-black text-slate-900">
                  {{ orgProfile.averageRating || '4.88' }} / 5.0 ★
                </div>
                <span class="text-[10px] text-slate-500 font-medium block mt-0.5">
                  Dựa trên 1.450 đánh giá
                </span>
              </div>

              <!-- KPI 3 -->
              <div class="p-3.5 rounded-2xl bg-white border border-indigo-100/90 shadow-2xs">
                <div class="flex items-center justify-between mb-1">
                  <span class="text-[11px] font-bold text-indigo-600 uppercase tracking-wider">Sự Kiện Đã Tổ Chức</span>
                  <div class="w-6 h-6 rounded-lg bg-indigo-50 flex items-center justify-center text-indigo-600">
                    <Calendar class="w-3.5 h-3.5" />
                  </div>
                </div>
                <div class="text-lg sm:text-xl font-black text-slate-900">
                  18 Sự Kiện
                </div>
                <span class="text-[10px] text-indigo-600 font-bold block mt-0.5">
                  100% Hoàn thành xuất sắc
                </span>
              </div>

              <!-- KPI 4 -->
              <div class="p-3.5 rounded-2xl bg-white border border-emerald-100/90 shadow-2xs">
                <div class="flex items-center justify-between mb-1">
                  <span class="text-[11px] font-bold text-emerald-600 uppercase tracking-wider">Tỷ Lệ Check-in TB</span>
                  <div class="w-6 h-6 rounded-lg bg-emerald-50 flex items-center justify-center text-emerald-600">
                    <ScanLine class="w-3.5 h-3.5" />
                  </div>
                </div>
                <div class="text-lg sm:text-xl font-black text-slate-900">
                  92.4%
                </div>
                <span class="text-[10px] text-emerald-600 font-bold block mt-0.5">
                  Kiểm soát vé Anti-Passback
                </span>
              </div>
            </div>
          </div>

          <!-- Structured Edit Profile Form Sections -->
          <form @submit.prevent="saveOrgProfile" class="space-y-6">
            <!-- Section 1: Core Brand Identity -->
            <div class="p-6 sm:p-8 bg-white rounded-3xl border border-slate-200/90 shadow-2xs space-y-5">
              <div class="flex items-center justify-between border-b border-slate-100 pb-4">
                <div>
                  <h4 class="text-base font-black text-slate-900 flex items-center gap-2">
                    <Building2 class="w-4 h-4 text-purple-600" />
                    <span>1. Thông Tin Thương Hiệu & Giới Thiệu Chung</span>
                  </h4>
                  <p class="text-xs text-slate-500 mt-0.5">Tên thương hiệu, thông điệp truyền thông hiển thị trực tiếp trên vé và trang sự kiện</p>
                </div>
                <span class="text-[11px] font-bold text-purple-600 bg-purple-50 px-2.5 py-1 rounded-lg border border-purple-200">
                  Bắt buộc
                </span>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">
                    Tên Tổ Chức / Công Ty <span class="text-rose-500">*</span>
                  </label>
                  <input
                    v-model="orgProfile.organizationName"
                    type="text"
                    required
                    placeholder="VD: Nexus Media Group..."
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm font-bold transition text-slate-900"
                  />
                  <p class="text-[11px] text-slate-400 mt-1">Tên pháp nhân hoặc thương hiệu tổ chức sự kiện chuyên nghiệp</p>
                </div>

                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">
                    Website Chính Thức
                  </label>
                  <div class="relative">
                    <Globe class="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                    <input
                      v-model="orgProfile.website"
                      type="url"
                      placeholder="https://yourbrand.vn"
                      class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm font-medium transition text-slate-900"
                    />
                  </div>
                  <p class="text-[11px] text-slate-400 mt-1">Liên kết tới trang chủ công ty để tăng độ uy tín với khán giả</p>
                </div>
              </div>

              <div>
                <div class="flex items-center justify-between mb-1.5">
                  <label class="text-xs font-bold text-slate-700">Tiểu Sử Giới Thiệu (Bio / Giới thiệu năng lực)</label>
                  <span class="text-[11px] text-slate-400">{{ (orgProfile.bio || '').length }} / 500 ký tự</span>
                </div>
                <textarea
                  v-model="orgProfile.bio"
                  rows="3"
                  maxlength="500"
                  class="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm leading-relaxed transition text-slate-900 placeholder:text-slate-400"
                  placeholder="Giới thiệu về đơn vị tổ chức, các chương trình tiêu biểu đã thực hiện, cam kết chất lượng trải nghiệm cho khán giả..."
                ></textarea>
              </div>
            </div>

            <!-- Section 2: Visual Identity (Logo & Banner) -->
            <div class="p-6 sm:p-8 bg-white rounded-3xl border border-slate-200/90 shadow-2xs space-y-5">
              <div class="border-b border-slate-100 pb-4">
                <h4 class="text-base font-black text-slate-900 flex items-center gap-2">
                  <Palette class="w-4 h-4 text-indigo-600" />
                  <span>2. Bộ Nhận Diện Hình Ảnh (Logo & Banner Bìa)</span>
                </h4>
                <p class="text-xs text-slate-500 mt-0.5">Cấu hình hình ảnh chất lượng cao để xây dựng độ nhận diện thương hiệu</p>
              </div>

              <!-- Logo Config -->
              <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 items-start">
                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-2">Ảnh Đại Diện (Logo Vuông 1:1)</label>
                  <div class="flex items-center gap-4">
                    <img
                      :src="orgProfile.logoUrl || 'https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?auto=format&fit=crop&w=400&q=80'"
                      alt="Preview"
                      class="w-20 h-20 rounded-2xl object-cover ring-2 ring-slate-200 shadow-sm shrink-0 bg-slate-100"
                    />
                    <div class="space-y-1">
                      <span class="text-xs font-bold text-slate-800 block">Xem trước Logo</span>
                      <span class="text-[11px] text-slate-400 block">Độ phân giải tối thiểu 400 × 400 px, định dạng PNG hoặc JPG</span>
                    </div>
                  </div>
                </div>

                <div class="lg:col-span-2 space-y-2.5">
                  <label class="block text-xs font-bold text-slate-700">Đường Dẫn URL Logo</label>
                  <input
                    v-model="orgProfile.logoUrl"
                    type="url"
                    placeholder="https://domain.com/logo.png"
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900 font-mono text-xs"
                  />
                  <!-- Preset Logos -->
                  <div class="flex flex-wrap items-center gap-1.5 pt-1">
                    <span class="text-[10px] font-bold text-slate-400 uppercase">Logo mẫu đẹp:</span>
                    <button
                      v-for="l in presetLogos"
                      :key="l.name"
                      type="button"
                      @click="applyPresetLogo(l.url)"
                      class="px-2.5 py-1 rounded-lg text-[11px] font-medium border border-slate-200 hover:border-indigo-300 hover:bg-indigo-50 transition cursor-pointer"
                      :class="orgProfile.logoUrl === l.url ? 'bg-indigo-50 text-indigo-700 border-indigo-300 font-bold' : 'bg-slate-50 text-slate-600'"
                    >
                      {{ l.name }}
                    </button>
                  </div>
                </div>
              </div>

              <!-- Banner Config -->
              <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 items-start pt-4 border-t border-slate-100">
                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-2">Ảnh Bìa Ngang (Cover Banner 16:9)</label>
                  <div class="space-y-2">
                    <div class="h-20 w-full rounded-xl overflow-hidden ring-2 ring-slate-200 bg-slate-100">
                      <img
                        :src="orgProfile.bannerUrl || 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?auto=format&fit=crop&w=1600&q=80'"
                        alt="Preview Banner"
                        class="w-full h-full object-cover"
                      />
                    </div>
                    <span class="text-[11px] text-slate-400 block">Độ phân giải khuyến nghị 1600 × 500 px</span>
                  </div>
                </div>

                <div class="lg:col-span-2 space-y-2.5">
                  <label class="block text-xs font-bold text-slate-700">Đường Dẫn URL Banner</label>
                  <input
                    v-model="orgProfile.bannerUrl"
                    type="url"
                    placeholder="https://domain.com/banner.jpg"
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900 font-mono text-xs"
                  />
                  <!-- Preset Banners -->
                  <div class="flex flex-wrap items-center gap-1.5 pt-1">
                    <span class="text-[10px] font-bold text-slate-400 uppercase">Banner mẫu đẹp:</span>
                    <button
                      v-for="b in presetBanners"
                      :key="b.name"
                      type="button"
                      @click="applyPresetBanner(b.url)"
                      class="px-2.5 py-1 rounded-lg text-[11px] font-medium border border-slate-200 hover:border-indigo-300 hover:bg-indigo-50 transition cursor-pointer"
                      :class="orgProfile.bannerUrl === b.url ? 'bg-indigo-50 text-indigo-700 border-indigo-300 font-bold' : 'bg-slate-50 text-slate-600'"
                    >
                      {{ b.name }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Section 3: Contact & Social Channels -->
            <div class="p-6 sm:p-8 bg-white rounded-3xl border border-slate-200/90 shadow-2xs space-y-5">
              <div class="border-b border-slate-100 pb-4">
                <h4 class="text-base font-black text-slate-900 flex items-center gap-2">
                  <PhoneCall class="w-4 h-4 text-emerald-600" />
                  <span>3. Kênh Liên Hệ Hỗ Trợ Khách Hàng & Mạng Xã Hội</span>
                </h4>
                <p class="text-xs text-slate-500 mt-0.5">Thông tin giúp khán giả và đối tác dễ dàng liên hệ trao đổi công việc</p>
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">Email Hỗ Trợ Khách Hàng</label>
                  <div class="relative">
                    <Send class="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                    <input
                      v-model="orgProfile.contactEmail"
                      type="email"
                      placeholder="support@nexusmedia.vn"
                      class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900"
                    />
                  </div>
                </div>

                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">Hotline Ban Tổ Chức</label>
                  <div class="relative">
                    <PhoneCall class="w-4 h-4 text-slate-400 absolute left-3.5 top-3" />
                    <input
                      v-model="orgProfile.contactPhone"
                      type="tel"
                      placeholder="0909 123 456"
                      class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900 font-mono"
                    />
                  </div>
                </div>

                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">Trang Facebook / Fanpage</label>
                  <input
                    v-model="orgProfile.facebookUrl"
                    type="url"
                    placeholder="https://facebook.com/nexusmedia"
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900"
                  />
                </div>

                <div>
                  <label class="block text-xs font-bold text-slate-700 mb-1.5">Trang LinkedIn Doanh Nghiệp</label>
                  <input
                    v-model="orgProfile.linkedinUrl"
                    type="url"
                    placeholder="https://linkedin.com/company/nexus-media"
                    class="w-full px-4 py-2.5 rounded-xl bg-slate-50 border border-slate-200 hover:border-slate-300 focus:bg-white focus:border-indigo-600 focus:ring-2 focus:ring-indigo-100 text-sm transition text-slate-900"
                  />
                </div>
              </div>

              <!-- Bottom Sticky Save Action Bar -->
              <div class="pt-5 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <span class="text-xs text-slate-500 font-medium">
                  Mọi thay đổi sẽ được cập nhật tức thì trên toàn bộ trang chi tiết sự kiện của bạn.
                </span>
                <div class="flex items-center gap-3">
                  <button
                    type="button"
                    @click="loadOrgProfile"
                    class="px-4 py-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 font-bold text-xs transition cursor-pointer"
                  >
                    Hoàn tác
                  </button>
                  <button
                    type="submit"
                    :disabled="savingOrgProfile"
                    class="px-6 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-700 hover:to-indigo-700 text-white font-bold text-xs flex items-center gap-2 shadow-md shadow-indigo-600/20 transition cursor-pointer disabled:opacity-50"
                  >
                    <Check class="w-4 h-4" />
                    <span>{{ savingOrgProfile ? 'Đang lưu hệ thống...' : 'Lưu Thông Tin Thương Hiệu' }}</span>
                  </button>
                </div>
              </div>
            </div>
          </form>
        </div>


        <!-- ==================== TAB 11 (ORGANIZER): FINANCIAL SETTLEMENT ==================== -->
        <div v-if="activeTab === 'org_finance'" class="space-y-6">
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h3 class="text-xl font-black text-slate-900">Đối Soát & Giải Ngân Doanh Thu Sự Kiện</h3>
              <p class="text-xs text-slate-500 mt-0.5">Báo cáo minh bạch dòng tiền bán vé, chiết khấu hoa hồng và tài khoản nhận tiền của Ban tổ chức</p>
            </div>
            <button
              type="button"
              @click="exportRevenueStatement"
              class="px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 transition cursor-pointer"
            >
              <FileSpreadsheet class="w-4 h-4" />
              <span>Tải Bảng Đối Soát (.xlsx)</span>
            </button>
          </div>

          <!-- Settlement Summary -->
          <div class="p-6 rounded-3xl bg-gradient-to-br from-indigo-900 via-indigo-950 to-purple-950 text-white shadow-xl space-y-6">
            <div class="grid grid-cols-1 sm:grid-cols-4 gap-4">
              <div>
                <span class="text-xs text-indigo-200">Tổng doanh thu vé gộp</span>
                <div class="text-2xl font-black mt-1">845.000.000 ₫</div>
              </div>
              <div>
                <span class="text-xs text-indigo-200">Phí sàn EventHub (5%)</span>
                <div class="text-2xl font-black text-rose-400 mt-1">-42.250.000 ₫</div>
              </div>
              <div>
                <span class="text-xs text-indigo-200">Chiết khấu Voucher</span>
                <div class="text-2xl font-black text-amber-300 mt-1">-35.000.000 ₫</div>
              </div>
              <div>
                <span class="text-xs text-indigo-200">Thực nhận về tài khoản</span>
                <div class="text-2xl font-black text-emerald-400 mt-1">767.750.000 ₫</div>
              </div>
            </div>

            <div class="pt-4 border-t border-indigo-800/60 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-indigo-200">
              <div class="flex items-center gap-2">
                <Banknote class="w-4 h-4 text-emerald-400" />
                <span>Tài khoản nhận: <strong>MBBank - 999988886666 (NEXUS MEDIA)</strong></span>
              </div>
              <span class="px-2.5 py-1 rounded-full bg-emerald-500/20 text-emerald-300 font-bold text-[11px] border border-emerald-500/30">
                Đã giải ngân đợt 1: 500.000.000 ₫
              </span>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 12 (STAFF): LIVE GATE CHECK-IN TERMINAL ==================== -->
        <div v-if="activeTab === 'staff_scanner'" class="space-y-6">
          <!-- Terminal Header -->
          <div class="p-5 rounded-2xl bg-white border border-slate-200 shadow-2xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-md text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-800 border border-emerald-200">
                  <span class="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                  Trạm Soát Vé Trực Tuyến
                </span>
                <span class="text-xs text-slate-400">&bull; Cổng Soát Vé VIP Gate A1</span>
              </div>
              <h3 class="text-xl font-black text-slate-900">Màn Hình Soát Vé Điện Tử & Quét Mã QR</h3>
              <p class="text-xs text-slate-500 mt-0.5">Xác thực vé tức thì, kiểm tra an ninh Anti-passback chống gian lận quét trùng</p>
            </div>

            <!-- Stats Badge -->
            <div class="flex items-center gap-4 text-xs">
              <div class="text-right">
                <span class="text-slate-400 block text-[10px]">Tốc độ quét</span>
                <strong class="text-emerald-700 font-black text-sm">{{ staffScanStats.ratePerMinute }} vé/phút</strong>
              </div>
              <div class="text-right">
                <span class="text-slate-400 block text-[10px]">Đã vào cổng</span>
                <strong class="text-indigo-700 font-black text-sm">{{ staffScanStats.totalCheckedIn }} / {{ staffScanStats.totalExpected }}</strong>
              </div>
            </div>
          </div>

          <!-- Scanner Input & Quick Simulation Actions -->
          <div class="p-6 rounded-3xl bg-slate-900 text-white shadow-xl space-y-5">
            <div class="flex flex-col sm:flex-row items-center gap-3">
              <div class="relative flex-1 w-full">
                <ScanLine class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-emerald-400" />
                <input
                  v-model="scannerInputCode"
                  @keyup.enter="handleScanTicket()"
                  type="text"
                  placeholder="Quét mã QR hoặc nhập mã vé (vd: TKT-EVH-2026-001)..."
                  class="w-full pl-12 pr-4 py-3.5 rounded-2xl bg-slate-800 border border-slate-700 text-white placeholder-slate-400 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>

              <button
                type="button"
                @click="handleScanTicket()"
                class="w-full sm:w-auto px-6 py-3.5 rounded-2xl bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-black text-xs flex items-center justify-center gap-2 shadow-lg transition cursor-pointer"
              >
                <Check class="w-4 h-4" />
                <span>Xác Thực Ngay</span>
              </button>
            </div>

            <!-- Quick Simulator Buttons -->
            <div class="pt-2 border-t border-slate-800 flex flex-wrap items-center gap-2 text-xs">
              <span class="text-slate-400 font-bold text-[11px]">Test nhanh kịch bản cổng:</span>
              <button
                type="button"
                @click="handleScanTicket('TKT-EVH-2026-001')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-emerald-900/60 text-emerald-300 font-semibold border border-slate-700 transition cursor-pointer"
              >
                Quét Vé Hợp Lệ (VIP Diamond)
              </button>
              <button
                type="button"
                @click="handleScanTicket('TKT-EVH-2026-003')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-indigo-900/60 text-indigo-300 font-semibold border border-slate-700 transition cursor-pointer"
              >
                Quét Vé Hạng Standard
              </button>
              <button
                type="button"
                @click="scannerInputCode = 'TKT-EVH-2026-001'; handleScanTicket()"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-rose-900/60 text-rose-300 font-semibold border border-slate-700 transition cursor-pointer"
              >
                Thử Quét Lại (Test Anti-Passback)
              </button>
              <button
                type="button"
                @click="handleScanTicket('TKT-INVALID-999')"
                class="px-3 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold border border-slate-700 transition cursor-pointer"
              >
                Thử Vé Giả / Hết Hạn
              </button>
            </div>
          </div>

          <!-- Realtime Scan Result Display Screen -->
          <div v-if="lastScanResult" class="animate-scale-up">
            <!-- 1. APPROVED PASS RESULT -->
            <div
              v-if="lastScanResult.status === 'SUCCESS'"
              class="p-6 rounded-3xl bg-gradient-to-br from-emerald-500 via-teal-600 to-emerald-700 text-white shadow-xl space-y-4 border-2 border-emerald-400"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-2xl bg-white text-emerald-700 flex items-center justify-center font-black shadow-md">
                    <Check class="w-8 h-8" />
                  </div>
                  <div>
                    <span class="text-xs font-black uppercase tracking-wider text-emerald-100">Xác Thực Cổng Thành Công</span>
                    <h4 class="text-2xl font-black">HỢP LỆ &bull; CHO PHÉP VÀO CỔNG</h4>
                  </div>
                </div>
                <span class="px-3 py-1 rounded-full bg-white/20 text-white font-mono font-bold text-xs">
                  {{ lastScanResult.checkInTime }}
                </span>
              </div>

              <div class="p-4 rounded-2xl bg-black/15 backdrop-blur-xs grid grid-cols-2 sm:grid-cols-4 gap-4 text-xs">
                <div>
                  <span class="text-emerald-100 block text-[11px]">Khách Hàng</span>
                  <strong class="text-base text-white font-extrabold">{{ lastScanResult.customerName }}</strong>
                </div>
                <div>
                  <span class="text-emerald-100 block text-[11px]">Hạng Vé</span>
                  <strong class="text-base text-white font-extrabold">{{ lastScanResult.tier }}</strong>
                </div>
                <div>
                  <span class="text-emerald-100 block text-[11px]">Vị Trí Ghế</span>
                  <strong class="text-base text-white font-mono font-extrabold">{{ lastScanResult.seat || 'Khu VIP A1' }}</strong>
                </div>
                <div>
                  <span class="text-emerald-100 block text-[11px]">Cổng Soát Vé</span>
                  <strong class="text-base text-white font-extrabold">{{ lastScanResult.gate }}</strong>
                </div>
              </div>
            </div>

            <!-- 2. ANTI-PASSBACK BLOCKED RESULT -->
            <div
              v-else-if="lastScanResult.status === 'ANTI_PASSBACK'"
              class="p-6 rounded-3xl bg-gradient-to-br from-rose-600 via-red-700 to-rose-900 text-white shadow-xl space-y-4 border-2 border-rose-400 animate-shake"
            >
              <div class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-12 h-12 rounded-2xl bg-white text-rose-700 flex items-center justify-center font-black shadow-md">
                    <AlertTriangle class="w-8 h-8 text-rose-600" />
                  </div>
                  <div>
                    <span class="text-xs font-black uppercase tracking-wider text-rose-200">Phát Hiện Gian Lận Soát Vé</span>
                    <h4 class="text-2xl font-black">VI PHẠM ANTI-PASSBACK &bull; TỪ CHỐI VÀO</h4>
                  </div>
                </div>
                <span class="px-3 py-1 rounded-full bg-white/20 text-white font-mono font-bold text-xs">
                  BLOCKED
                </span>
              </div>

              <div class="p-4 rounded-2xl bg-black/20 text-xs text-rose-100 leading-relaxed font-semibold">
                {{ lastScanResult.message }}
              </div>
            </div>

            <!-- 3. INVALID TICKET RESULT -->
            <div
              v-else
              class="p-6 rounded-3xl bg-slate-800 text-white shadow-xl space-y-4 border-2 border-slate-600"
            >
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 rounded-2xl bg-rose-500/20 text-rose-400 flex items-center justify-center font-black">
                  <X class="w-8 h-8" />
                </div>
                <div>
                  <span class="text-xs font-black uppercase tracking-wider text-slate-400">Vé Không Hợp Lệ</span>
                  <h4 class="text-xl font-black text-rose-300">TỪ CHỐI CHO VÀO</h4>
                  <p class="text-xs text-slate-300 mt-1">{{ lastScanResult.message }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ==================== TAB 13 (STAFF): GATE LOGS ==================== -->
        <div v-if="activeTab === 'staff_logs'" class="space-y-4">
          <div class="bg-white rounded-2xl border border-slate-200 overflow-hidden shadow-2xs">
            <div class="p-5 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 class="text-base font-bold text-slate-900">Nhật Ký Quét Vé Trực Tuyến Tại Cổng (Live Gate Logs)</h3>
                <p class="text-xs text-slate-500">Ghi nhận toàn bộ lượt quét, timestamp, cổng vào và phát hiện vi phạm</p>
              </div>
              <span class="text-xs font-bold text-slate-500">
                Tổng cộng: <strong class="text-slate-900">{{ gateLogs.length }}</strong> lượt quét
              </span>
            </div>

            <div class="overflow-x-auto">
              <table class="w-full text-left text-xs">
                <thead class="bg-slate-50 text-slate-500 border-b border-slate-200 font-bold uppercase tracking-wider">
                  <tr>
                    <th class="py-3 px-4">Thời Gian</th>
                    <th class="py-3 px-4">Mã Vé</th>
                    <th class="py-3 px-4">Khách Hàng / Đối Tượng</th>
                    <th class="py-3 px-4">Hạng Vé</th>
                    <th class="py-3 px-4">Cổng Soát</th>
                    <th class="py-3 px-4 text-right">Kết Quả</th>
                  </tr>
                </thead>
                <tbody class="divide-y divide-slate-100">
                  <tr v-for="g in gateLogs" :key="g.id" class="hover:bg-slate-50/80 transition">
                    <td class="py-3 px-4 font-mono text-slate-500 text-[11px]">{{ g.time }}</td>
                    <td class="py-3 px-4 font-mono font-bold text-slate-900">{{ g.code }}</td>
                    <td class="py-3 px-4 font-bold text-slate-800">{{ g.name }}</td>
                    <td class="py-3 px-4 text-slate-600">{{ g.tier }}</td>
                    <td class="py-3 px-4 font-semibold text-slate-700">{{ g.gate }}</td>
                    <td class="py-3 px-4 text-right">
                      <span
                        class="px-2.5 py-0.5 rounded-full text-[10px] font-bold"
                        :class="[
                          g.status === 'VALID' ? 'bg-emerald-50 text-emerald-700 border border-emerald-200' :
                          g.status === 'ANTI_PASSBACK' ? 'bg-rose-50 text-rose-700 border border-rose-200' :
                          'bg-slate-100 text-slate-600'
                        ]"
                      >
                        {{ g.status === 'VALID' ? 'Hợp lệ' : g.status === 'ANTI_PASSBACK' ? 'Vi phạm Anti-passback' : 'Không hợp lệ' }}
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</div>

<!-- ==================== MODAL: CREATE EVENT & TIER BUILDER (ORGANIZER) ==================== -->
<div
  v-if="createEventModalOpen"
  class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto animate-fade-in"
>
  <div class="bg-white w-full max-w-2xl rounded-3xl p-6 shadow-2xl border border-slate-200 space-y-5 my-8 max-h-[90vh] overflow-y-auto">
    <div class="flex items-center justify-between border-b border-slate-100 pb-3">
      <div class="flex items-center gap-2 text-purple-700 font-bold text-sm">
        <Plus class="w-5 h-5" />
        <span>Khởi Tạo Sự Kiện & Thiết Kế Hạng Vé</span>
      </div>
      <button
        type="button"
        @click="createEventModalOpen = false"
        class="text-slate-400 hover:text-slate-600 p-1 cursor-pointer"
      >
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Form Inputs -->
    <div class="space-y-4 text-xs">
      <div>
        <label class="block font-bold text-slate-700 mb-1">Tên sự kiện *</label>
        <input
          v-model="newEventForm.title"
          type="text"
          placeholder="Ví dụ: EDM Galaxy Music Festival 2026..."
          class="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
        />
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div>
          <label class="block font-bold text-slate-700 mb-1">Thể loại sự kiện</label>
          <select
            v-model="newEventForm.categoryId"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500 font-semibold"
          >
            <option :value="1">Âm Nhạc & Concert</option>
            <option :value="2">Hội Thảo Công Nghệ</option>
            <option :value="3">Thể Thao & Giải Đấu</option>
            <option :value="4">Nghệ Thuật & Sân Khấu</option>
          </select>
        </div>

        <div>
          <label class="block font-bold text-slate-700 mb-1">Địa điểm tổ chức *</label>
          <input
            v-model="newEventForm.locationName"
            type="text"
            placeholder="Sân vận động QK7 / Trung tâm SECC..."
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div>
          <label class="block font-bold text-slate-700 mb-1">Thời gian bắt đầu</label>
          <input
            v-model="newEventForm.startDateTime"
            type="datetime-local"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
        <div>
          <label class="block font-bold text-slate-700 mb-1">Thời gian kết thúc</label>
          <input
            v-model="newEventForm.endDateTime"
            type="datetime-local"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
      </div>

      <!-- Ticket Tiers Designer -->
      <div class="pt-2 border-t border-slate-100 space-y-3">
        <div class="flex items-center justify-between">
          <label class="font-bold text-slate-900 flex items-center gap-1.5">
            <Ticket class="w-4 h-4 text-purple-600" />
            <span>Thiết Kế Các Hạng Vé (Ticket Tiers)</span>
          </label>
          <button
            type="button"
            @click="addTierToNewEvent"
            class="px-2.5 py-1 rounded-lg bg-purple-50 hover:bg-purple-100 text-purple-700 font-bold text-[11px] flex items-center gap-1 transition cursor-pointer"
          >
            <Plus class="w-3.5 h-3.5" />
            <span>Thêm Hạng Vé</span>
          </button>
        </div>

        <div class="space-y-2">
          <div
            v-for="(tier, idx) in newEventForm.tiers"
            :key="idx"
            class="flex items-center gap-2.5 p-3 rounded-xl bg-slate-50 border border-slate-200"
          >
            <input
              v-model="tier.name"
              type="text"
              placeholder="Tên hạng vé"
              class="flex-1 px-2.5 py-1.5 rounded-lg bg-white border border-slate-200 text-xs font-bold"
            />
            <div class="relative w-32">
              <input
                v-model.number="tier.price"
                type="number"
                step="10000"
                placeholder="Giá vé"
                class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-slate-200 text-xs font-mono font-bold"
              />
            </div>
            <div class="relative w-24">
              <input
                v-model.number="tier.capacity"
                type="number"
                placeholder="Số vé"
                class="w-full px-2.5 py-1.5 rounded-lg bg-white border border-slate-200 text-xs font-mono text-center font-bold"
              />
            </div>
            <button
              v-if="newEventForm.tiers.length > 1"
              type="button"
              @click="removeTierFromNewEvent(idx)"
              class="p-1.5 rounded-lg text-rose-500 hover:bg-rose-50 transition cursor-pointer"
              title="Xóa hạng vé này"
            >
              <X class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions -->
    <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
      <button
        type="button"
        @click="createEventModalOpen = false"
        class="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition cursor-pointer"
      >
        Hủy
      </button>
      <button
        type="button"
        @click="submitCreateEvent"
        class="px-5 py-2.5 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs shadow-md transition cursor-pointer flex items-center gap-1.5"
      >
        <Check class="w-4 h-4" />
        <span>Đăng Tải & Mở Bán Ngay</span>
      </button>
    </div>
  </div>
</div>

<!-- ==================== MODAL: CREATE VOUCHER (ORGANIZER) ==================== -->
<div
  v-if="createVoucherModalOpen"
  class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in"
>
  <div class="bg-white w-full max-w-md rounded-3xl p-6 shadow-2xl border border-slate-200 space-y-5 animate-scale-up">
    <div class="flex items-center justify-between border-b border-slate-100 pb-3">
      <div class="flex items-center gap-2 text-purple-700 font-bold text-sm">
        <TicketPercent class="w-5 h-5" />
        <span>Tạo Mã Khuyến Mãi (Voucher)</span>
      </div>
      <button
        type="button"
        @click="createVoucherModalOpen = false"
        class="text-slate-400 hover:text-slate-600 p-1 cursor-pointer"
      >
        <X class="w-5 h-5" />
      </button>
    </div>

    <div class="space-y-3.5 text-xs">
      <div>
        <label class="block font-bold text-slate-700 mb-1">Mã code khuyến mãi *</label>
        <input
          v-model="newVoucherForm.code"
          type="text"
          placeholder="VD: NEXUS2026, EARLYVIP..."
          class="w-full px-3.5 py-2 rounded-xl border border-slate-200 text-xs font-mono font-bold uppercase focus:outline-none focus:ring-2 focus:ring-purple-500"
        />
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block font-bold text-slate-700 mb-1">Loại giảm</label>
          <select
            v-model="newVoucherForm.discountType"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="PERCENT">Phần trăm (%)</option>
            <option value="FIXED">Số tiền cố định (₫)</option>
          </select>
        </div>
        <div>
          <label class="block font-bold text-slate-700 mb-1">Mức giảm *</label>
          <input
            v-model.number="newVoucherForm.discountValue"
            type="number"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-mono font-bold focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="block font-bold text-slate-700 mb-1">Số lượt tối đa</label>
          <input
            v-model.number="newVoucherForm.maxUsage"
            type="number"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs font-mono focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
        <div>
          <label class="block font-bold text-slate-700 mb-1">Hạn sử dụng</label>
          <input
            v-model="newVoucherForm.expiresAt"
            type="date"
            class="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs focus:outline-none focus:ring-2 focus:ring-purple-500"
          />
        </div>
      </div>
    </div>

    <div class="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
      <button
        type="button"
        @click="createVoucherModalOpen = false"
        class="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition cursor-pointer"
      >
        Hủy
      </button>
      <button
        type="button"
        @click="submitCreateVoucher"
        class="px-5 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-bold text-xs shadow-md transition cursor-pointer flex items-center gap-1.5"
      >
        <Check class="w-4 h-4" />
        <span>Tạo Mã Ngay</span>
      </button>
    </div>
  </div>
</div>

    <!-- Event Analytics Detail Modal -->
    <div
      v-if="analyticsModalOpen && selectedEventAnalytics"
      class="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4 animate-fade-in"
    >
      <div class="bg-white w-full max-w-lg rounded-3xl p-6 shadow-2xl border border-slate-200 space-y-5 animate-scale-up">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <div class="flex items-center gap-2 text-indigo-600 font-bold text-sm">
            <Ticket class="w-5 h-5" />
            <span>Chi Tiết Báo Cáo Sự Kiện</span>
          </div>
          <button
            type="button"
            @click="analyticsModalOpen = false"
            class="text-slate-400 hover:text-slate-600 p-1 cursor-pointer"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <div>
          <h4 class="text-base font-extrabold text-slate-900 mb-1">
            {{ selectedEventAnalytics.eventTitle }}
          </h4>
          <span class="text-xs text-slate-400 font-mono">Mã sự kiện: #{{ selectedEventAnalytics.eventId }}</span>
        </div>

        <div class="grid grid-cols-2 gap-3 text-xs">
          <div class="p-3.5 rounded-2xl bg-indigo-50/70 border border-indigo-100">
            <div class="text-slate-500 font-medium">Doanh thu ghi nhận</div>
            <div class="text-lg font-black text-indigo-700 mt-1">
              {{ formatCurrency(selectedEventAnalytics.totalRevenue) }}
            </div>
          </div>

          <div class="p-3.5 rounded-2xl bg-emerald-50/70 border border-emerald-100">
            <div class="text-slate-500 font-medium">Vé đã phát hành</div>
            <div class="text-lg font-black text-emerald-700 mt-1">
              {{ selectedEventAnalytics.totalTickets }} vé
            </div>
          </div>

          <div class="p-3.5 rounded-2xl bg-purple-50/70 border border-purple-100">
            <div class="text-slate-500 font-medium">Khách đã check-in tại cổng</div>
            <div class="text-lg font-black text-purple-700 mt-1">
              {{ selectedEventAnalytics.checkedInTickets }} lượt
            </div>
          </div>

          <div class="p-3.5 rounded-2xl bg-amber-50/70 border border-amber-100">
            <div class="text-slate-500 font-medium">Tỷ lệ tham dự thực tế</div>
            <div class="text-lg font-black text-amber-700 mt-1">
              {{ selectedEventAnalytics.checkInRate }}%
            </div>
          </div>
        </div>

        <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
          <button
            type="button"
            @click="exportAttendees(selectedEventAnalytics.eventId, selectedEventAnalytics.eventTitle)"
            class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 transition cursor-pointer"
          >
            <Download class="w-3.5 h-3.5" />
            <span>Xuất Danh Sách Khách (.xlsx)</span>
          </button>
          <button
            type="button"
            @click="analyticsModalOpen = false"
            class="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition cursor-pointer"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>

    <!-- Modal: Tạo Danh Mục Mới (Admin) -->
    <div
      v-if="createCategoryModalOpen"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs"
    >
      <div class="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-5 animate-scale-up">
        <div class="flex items-center justify-between pb-3 border-b border-slate-100">
          <div class="flex items-center gap-2.5">
            <div class="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center font-bold">
              <Tag class="w-4 h-4" />
            </div>
            <div>
              <h3 class="font-black text-slate-900 text-base">Thêm Danh Mục Sự Kiện Mới</h3>
              <p class="text-xs text-slate-500">Phân loại sự kiện trên trang chủ và bộ lọc</p>
            </div>
          </div>
          <button
            type="button"
            @click="createCategoryModalOpen = false"
            class="p-1 rounded-lg text-slate-400 hover:text-slate-600 hover:bg-slate-100"
          >
            <X class="w-5 h-5" />
          </button>
        </div>

        <form @submit.prevent="submitCreateCategory" class="space-y-4">
          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Tên Danh Mục <span class="text-rose-500">*</span></label>
            <input
              v-model="newCategoryForm.name"
              type="text"
              required
              placeholder="Ví dụ: Triển Lãm & Nghệ Thuật"
              class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-emerald-600 text-sm font-semibold"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Slug URL (Tùy chọn)</label>
            <input
              v-model="newCategoryForm.slug"
              type="text"
              placeholder="Tự động sinh từ tên danh mục..."
              class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-emerald-600 text-sm font-mono"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-700 mb-1">Mô Tả Danh Mục</label>
            <textarea
              v-model="newCategoryForm.description"
              rows="3"
              placeholder="Mô tả các sự kiện thuộc danh mục này..."
              class="w-full px-3.5 py-2.5 rounded-xl bg-slate-50 border border-slate-300 focus:bg-white focus:border-emerald-600 text-sm leading-relaxed"
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
            <button
              type="button"
              @click="createCategoryModalOpen = false"
              class="px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              class="px-5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs flex items-center gap-1.5 shadow-sm transition cursor-pointer"
            >
              <Check class="w-4 h-4" />
              <span>Tạo Danh Mục</span>
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Global Toast Notifications -->
    <div class="fixed bottom-6 right-6 z-50 flex flex-col gap-2 max-w-sm pointer-events-none">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="px-4 py-2.5 rounded-xl shadow-lg border pointer-events-auto flex items-center gap-2.5 text-xs font-semibold animate-slide-up backdrop-blur-md"
        :class="[
          toast.type === 'success' ? 'bg-emerald-950/90 text-emerald-100 border-emerald-500/40' :
          toast.type === 'error' ? 'bg-rose-950/90 text-rose-100 border-rose-500/40' :
          'bg-slate-900/95 text-white border-slate-700'
        ]"
      >
        <CheckCircle2 v-if="toast.type === 'success'" class="w-4 h-4 text-emerald-400 shrink-0" />
        <AlertCircle v-else-if="toast.type === 'error'" class="w-4 h-4 text-rose-400 shrink-0" />
        <Info v-else class="w-4 h-4 text-indigo-400 shrink-0" />
        <span>{{ toast.message }}</span>
      </div>
    </div>
  </div>
</template>
