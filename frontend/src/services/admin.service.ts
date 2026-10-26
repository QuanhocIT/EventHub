import apiClient from './api'
import type {
  AdminDashboardData,
  OrganizerDashboardData,
  EventAnalyticsData,
  AdminUserItem,
  AuditLogItem,
  UserRole,
} from '@/types'

// Mock Fallback Data for offline / local preview
const MOCK_ADMIN_DASHBOARD: AdminDashboardData = {
  totalUsers: 1248,
  totalCustomers: 1084,
  totalOrganizers: 96,
  totalStaff: 68,
  totalEvents: 34,
  publishedEvents: 28,
  totalRevenue: 2854600000,
  totalBookings: 3420,
  confirmedBookings: 3180,
  totalTickets: 5820,
}

const MOCK_ORGANIZER_DASHBOARD: OrganizerDashboardData = {
  organizerId: 2,
  totalEvents: 6,
  publishedEvents: 5,
  totalRevenue: 845000000,
  totalTicketsIssued: 1420,
  totalTicketsCheckedIn: 980,
  checkInRate: 69.01,
}

const MOCK_EVENT_ANALYTICS: Record<number, EventAnalyticsData> = {
  1: {
    eventId: 1,
    eventTitle: 'Skyline Symphony & Philharmonic Concert 2026',
    venueName: 'Nhà Hát Lớn Hà Nội (Hanoi Opera House)',
    totalRevenue: 420000000,
    netRevenue: 399000000,
    discountTotal: 21000000,
    totalTickets: 650,
    totalCapacity: 800,
    occupancyRate: 81.25,
    checkedInTickets: 540,
    checkInRate: 83.08,
    averageOrderValue: 1292000,
    hourlySalesVelocity: [
      { timeSlot: '08:00 - 10:00', revenue: 50400000, ticketsSold: 78 },
      { timeSlot: '10:00 - 12:00', revenue: 117600000, ticketsSold: 182 },
      { timeSlot: '12:00 - 14:00', revenue: 63000000, ticketsSold: 98 },
      { timeSlot: '14:00 - 17:00', revenue: 105000000, ticketsSold: 162 },
      { timeSlot: '17:00 - 20:00', revenue: 63000000, ticketsSold: 98 },
      { timeSlot: '20:00 - 23:00', revenue: 21000000, ticketsSold: 32 },
    ],
    tierBreakdown: [
      { tierName: 'VIP Royal Diamond', price: 1200000, soldCount: 150, totalCapacity: 150, revenue: 180000000, fillPercent: 100 },
      { tierName: 'Gold Balcony Suite', price: 750000, soldCount: 220, totalCapacity: 250, revenue: 165000000, fillPercent: 88 },
      { tierName: 'Standard Symphony Floor', price: 450000, soldCount: 280, totalCapacity: 400, revenue: 75000000, fillPercent: 70 },
    ],
    paymentMethods: { VIETQR: 65.0, MOMO: 20.0, VNPAY: 15.0 },
    rushHourCheckIn: [
      { timeSlot: 'T - 60 phút', count: 54 },
      { timeSlot: 'T - 30 phút', count: 189 },
      { timeSlot: 'T - 15 phút (Cao điểm)', count: 216 },
      { timeSlot: 'Giờ Khai Mạc (T)', count: 65 },
      { timeSlot: 'T + 15 phút trở đi', count: 16 },
    ],
  },
  2: {
    eventId: 2,
    eventTitle: 'Vietnam AI & Cloud Computing Summit 2026',
    venueName: 'Trung Tâm Hội Nghị Quốc Gia (NCC) - Hà Nội',
    totalRevenue: 215000000,
    netRevenue: 204250000,
    discountTotal: 10750000,
    totalTickets: 420,
    totalCapacity: 500,
    occupancyRate: 84.0,
    checkedInTickets: 310,
    checkInRate: 73.81,
    averageOrderValue: 1023000,
    hourlySalesVelocity: [
      { timeSlot: '08:00 - 10:00', revenue: 25800000, ticketsSold: 50 },
      { timeSlot: '10:00 - 12:00', revenue: 60200000, ticketsSold: 118 },
      { timeSlot: '12:00 - 14:00', revenue: 32250000, ticketsSold: 63 },
      { timeSlot: '14:00 - 17:00', revenue: 53750000, ticketsSold: 105 },
      { timeSlot: '17:00 - 20:00', revenue: 32250000, ticketsSold: 63 },
      { timeSlot: '20:00 - 23:00', revenue: 10750000, ticketsSold: 21 },
    ],
    tierBreakdown: [
      { tierName: 'All-Access Pass', price: 950000, soldCount: 120, totalCapacity: 120, revenue: 114000000, fillPercent: 100 },
      { tierName: 'Standard Developer', price: 450000, soldCount: 300, totalCapacity: 380, revenue: 101000000, fillPercent: 78.9 },
    ],
    paymentMethods: { VIETQR: 72.0, MOMO: 18.0, VNPAY: 10.0 },
    rushHourCheckIn: [
      { timeSlot: 'T - 60 phút', count: 31 },
      { timeSlot: 'T - 30 phút', count: 108 },
      { timeSlot: 'T - 15 phút (Cao điểm)', count: 124 },
      { timeSlot: 'Giờ Khai Mạc (T)', count: 37 },
      { timeSlot: 'T + 15 phút trở đi', count: 10 },
    ],
  },
}

let mockUsers: AdminUserItem[] = [
  {
    id: 1,
    username: 'admin',
    email: 'admin@eventhub.vn',
    fullName: 'Quản Trị Viên Hệ Thống',
    phoneNumber: '0901234567',
    role: 'ROLE_ADMIN',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80',
    enabled: true,
    createdAt: '2026-01-01T08:00:00Z',
  },
  {
    id: 2,
    username: 'organizer',
    email: 'contact@nexusmedia.vn',
    fullName: 'Nexus Tech & Concert Media',
    phoneNumber: '0912345678',
    role: 'ROLE_ORGANIZER',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80',
    enabled: true,
    createdAt: '2026-01-05T09:30:00Z',
  },
  {
    id: 3,
    username: 'staff',
    email: 'staff.gate@eventhub.vn',
    fullName: 'Đội Trưởng Soát Vé Check-in',
    phoneNumber: '0987654321',
    role: 'ROLE_STAFF',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80',
    enabled: true,
    createdAt: '2026-01-10T14:15:00Z',
  },
  {
    id: 4,
    username: 'customer',
    email: 'customer@eventhub.vn',
    fullName: 'Trần Minh Hoàng',
    phoneNumber: '0933445566',
    role: 'ROLE_CUSTOMER',
    avatarUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=200&q=80',
    enabled: true,
    createdAt: '2026-02-01T10:00:00Z',
  },
  {
    id: 5,
    username: 'lequynhanh',
    email: 'quynhanh.le@gmail.com',
    fullName: 'Lê Quỳnh Anh',
    phoneNumber: '0944556677',
    role: 'ROLE_CUSTOMER',
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=200&q=80',
    enabled: true,
    createdAt: '2026-02-15T11:20:00Z',
  },
  {
    id: 6,
    username: 'vietnam_show',
    email: 'events@vietnamshow.vn',
    fullName: 'Vietnam Show Production',
    phoneNumber: '0977889900',
    role: 'ROLE_ORGANIZER',
    avatarUrl: 'https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=200&q=80',
    enabled: false,
    createdAt: '2026-03-01T16:45:00Z',
  },
]

let mockAuditLogs: AuditLogItem[] = [
  {
    id: 101,
    action: 'USER_ROLE_UPDATE',
    entityType: 'User',
    entityId: '2',
    username: 'admin',
    userId: 1,
    ipAddress: '192.168.1.10',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/131.0',
    status: 'SUCCESS',
    executionTimeMs: 42,
    createdAt: '2026-10-23T01:15:20Z',
  },
  {
    id: 102,
    action: 'ORGANIZER_VERIFY',
    entityType: 'OrganizerProfile',
    entityId: '2',
    username: 'admin',
    userId: 1,
    ipAddress: '192.168.1.10',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/131.0',
    status: 'SUCCESS',
    executionTimeMs: 38,
    createdAt: '2026-10-22T19:40:12Z',
  },
  {
    id: 103,
    action: 'USER_STATUS_UPDATE',
    entityType: 'User',
    entityId: '6',
    username: 'admin',
    userId: 1,
    ipAddress: '192.168.1.10',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/131.0',
    status: 'SUCCESS',
    executionTimeMs: 25,
    createdAt: '2026-10-22T15:22:04Z',
  },
  {
    id: 104,
    action: 'EXPORT_ATTENDEES',
    entityType: 'Event',
    entityId: '1',
    username: 'organizer',
    userId: 2,
    ipAddress: '118.69.182.45',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    status: 'SUCCESS',
    executionTimeMs: 85,
    createdAt: '2026-10-22T10:10:00Z',
  },
  {
    id: 105,
    action: 'USER_LOGIN_FAILED',
    entityType: 'Auth',
    entityId: 'spammer_bot',
    username: 'anonymous',
    ipAddress: '45.134.20.12',
    userAgent: 'Python-requests/2.31.0',
    status: 'FAILED',
    errorMessage: 'Bad credentials: IP rate-limited or incorrect password',
    executionTimeMs: 14,
    createdAt: '2026-10-21T03:45:11Z',
  },
]

export const adminService = {
  // 1. Admin Platform Dashboard
  async getAdminDashboard(): Promise<AdminDashboardData> {
    try {
      const res = await apiClient.get('/dashboard/admin')
      return res.data?.data || res.data
    } catch {
      return MOCK_ADMIN_DASHBOARD
    }
  },

  // 2. Organizer Dashboard
  async getOrganizerDashboard(): Promise<OrganizerDashboardData> {
    try {
      const res = await apiClient.get('/dashboard/organizer')
      return res.data?.data || res.data
    } catch {
      return MOCK_ORGANIZER_DASHBOARD
    }
  },

  // 3. Specific Event Analytics
  async getEventAnalytics(eventId: number): Promise<EventAnalyticsData> {
    try {
      const res = await apiClient.get(`/dashboard/organizer/events/${eventId}`)
      return res.data?.data || res.data
    } catch {
      return (
        MOCK_EVENT_ANALYTICS[eventId] || {
          eventId,
          eventTitle: `Sự kiện #${eventId}`,
          venueName: 'Hội trường sự kiện EventHub',
          totalRevenue: 120000000,
          netRevenue: 114000000,
          discountTotal: 6000000,
          totalTickets: 250,
          totalCapacity: 300,
          occupancyRate: 83.3,
          checkedInTickets: 180,
          checkInRate: 72.0,
          averageOrderValue: 960000,
          hourlySalesVelocity: [
            { timeSlot: '08:00 - 10:00', revenue: 14400000, ticketsSold: 30 },
            { timeSlot: '10:00 - 12:00', revenue: 33600000, ticketsSold: 70 },
            { timeSlot: '12:00 - 14:00', revenue: 18000000, ticketsSold: 38 },
            { timeSlot: '14:00 - 17:00', revenue: 30000000, ticketsSold: 62 },
            { timeSlot: '17:00 - 20:00', revenue: 18000000, ticketsSold: 38 },
            { timeSlot: '20:00 - 23:00', revenue: 6000000, ticketsSold: 12 },
          ],
          tierBreakdown: [
            { tierName: 'Vé VIP', price: 900000, soldCount: 80, totalCapacity: 80, revenue: 72000000, fillPercent: 100 },
            { tierName: 'Vé Tiêu Chuẩn', price: 400000, soldCount: 170, totalCapacity: 220, revenue: 48000000, fillPercent: 77.3 },
          ],
          paymentMethods: { VIETQR: 65, MOMO: 20, VNPAY: 15 },
          rushHourCheckIn: [
            { timeSlot: 'T - 60m', count: 18 },
            { timeSlot: 'T - 30m', count: 63 },
            { timeSlot: 'T - 15m', count: 72 },
            { timeSlot: 'Giờ T', count: 22 },
            { timeSlot: 'T + 15m', count: 5 },
          ],
        }
      )
    }
  },

  async exportEventReportCsv(eventId: number): Promise<Blob> {
    try {
      const res = await apiClient.get(`/dashboard/organizer/events/${eventId}/export-report`, {
        responseType: 'blob',
      })
      return res.data
    } catch {
      // Generate client-side fallback CSV
      const a = MOCK_EVENT_ANALYTICS[eventId] || MOCK_EVENT_ANALYTICS[1]
      const csvStr = `\uFEFFEVENTHUB - BÁO CÁO PHÂN TÍCH TÀI CHÍNH & VẬN HÀNH SỰ KIỆN
Tên Sự Kiện:,"${a.eventTitle}"
Địa Điểm:,"${a.venueName || 'Trung tâm Hội nghị'}"
Doanh Thu Gộp (Gross GMV):,${a.totalRevenue} VND
Doanh Thu Thuần (Net):,${a.netRevenue || a.totalRevenue * 0.95} VND
Tổng Số Vé Bán:,${a.totalTickets}
Số Vé Check-in:,${a.checkedInTickets}
Tỷ Lệ Check-in:,${a.checkInRate}%
Tỷ Lệ Lấp Đầy:,${a.occupancyRate || 80}%

--- PHÂN BỔ HẠNG VÉ ---
Hạng Vé,Đơn Giá,Số Vé Bán,Tổng Sức Chứa,Doanh Thu,Tỷ Lệ
${(a.tierBreakdown || []).map((t) => `"${t.tierName}",${t.price},${t.soldCount},${t.totalCapacity},${t.revenue},${t.fillPercent}%`).join('\n')}
`
      return new Blob([csvStr], { type: 'text/csv;charset=utf-8;' })
    }
  },

  // 4. Admin User Management
  async getAllUsers(params?: {
    keyword?: string
    role?: string
    enabled?: boolean
    page?: number
    size?: number
  }): Promise<{ content: AdminUserItem[]; totalElements: number; totalPages: number }> {
    try {
      const res = await apiClient.get('/users', { params })
      const data = res.data?.data || res.data
      return {
        content: data.content || data,
        totalElements: data.totalElements ?? data.length ?? mockUsers.length,
        totalPages: data.totalPages ?? 1,
      }
    } catch {
      let filtered = [...mockUsers]
      if (params?.keyword) {
        const kw = params.keyword.toLowerCase()
        filtered = filtered.filter(
          (u) =>
            u.fullName.toLowerCase().includes(kw) ||
            u.email.toLowerCase().includes(kw) ||
            u.username.toLowerCase().includes(kw) ||
            (u.phoneNumber && u.phoneNumber.includes(kw))
        )
      }
      if (params?.role && params.role !== 'ALL') {
        filtered = filtered.filter((u) => u.role === params.role)
      }
      if (params?.enabled !== undefined) {
        filtered = filtered.filter((u) => u.enabled === params.enabled)
      }
      return {
        content: filtered,
        totalElements: filtered.length,
        totalPages: 1,
      }
    }
  },

  // 5. Toggle User Active Status (Lock/Unlock)
  async updateUserStatus(id: number, enabled: boolean): Promise<AdminUserItem> {
    try {
      const res = await apiClient.patch(`/users/${id}/status`, { enabled })
      return res.data?.data || res.data
    } catch {
      const user = mockUsers.find((u) => u.id === id)
      if (user) {
        user.enabled = enabled
        // Log action in mock audit
        mockAuditLogs.unshift({
          id: Date.now(),
          action: 'USER_STATUS_UPDATE',
          entityType: 'User',
          entityId: String(id),
          username: 'admin',
          userId: 1,
          ipAddress: '127.0.0.1',
          userAgent: navigator.userAgent,
          status: 'SUCCESS',
          executionTimeMs: 20,
          createdAt: new Date().toISOString(),
        })
        return user
      }
      throw new Error('User not found')
    }
  },

  // 6. Update User Role
  async updateUserRole(id: number, role: UserRole): Promise<AdminUserItem> {
    try {
      const res = await apiClient.patch(`/users/${id}/role`, { role })
      return res.data?.data || res.data
    } catch {
      const user = mockUsers.find((u) => u.id === id)
      if (user) {
        user.role = role
        mockAuditLogs.unshift({
          id: Date.now(),
          action: 'USER_ROLE_UPDATE',
          entityType: 'User',
          entityId: String(id),
          username: 'admin',
          userId: 1,
          ipAddress: '127.0.0.1',
          userAgent: navigator.userAgent,
          status: 'SUCCESS',
          executionTimeMs: 25,
          createdAt: new Date().toISOString(),
        })
        return user
      }
      throw new Error('User not found')
    }
  },

  // 7. Security Audit Logs
  async getAuditLogs(params?: {
    action?: string
    username?: string
    entityType?: string
    page?: number
    size?: number
  }): Promise<{ content: AuditLogItem[]; totalElements: number; totalPages: number }> {
    try {
      const res = await apiClient.get('/admin/audit-logs', { params })
      const data = res.data?.data || res.data
      return {
        content: data.content || data,
        totalElements: data.totalElements ?? data.length ?? mockAuditLogs.length,
        totalPages: data.totalPages ?? 1,
      }
    } catch {
      let filtered = [...mockAuditLogs]
      if (params?.action && params.action !== 'ALL') {
        filtered = filtered.filter((l) => l.action === params.action)
      }
      if (params?.username) {
        filtered = filtered.filter((l) => l.username.toLowerCase().includes(params.username!.toLowerCase()))
      }
      if (params?.entityType && params.entityType !== 'ALL') {
        filtered = filtered.filter((l) => l.entityType === params.entityType)
      }
      return {
        content: filtered,
        totalElements: filtered.length,
        totalPages: 1,
      }
    }
  },

  // 8. Download Excel: Event Attendees List
  async exportEventAttendees(eventId: number, eventTitle = 'Event'): Promise<void> {
    try {
      const res = await apiClient.get(`/export/events/${eventId}/attendees`, {
        responseType: 'blob',
      })
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `DS_Khach_Tham_Gia_Event_${eventId}.xlsx`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch {
      // Offline fallback: generate mock CSV file
      const csvContent =
        '\uFEFF' +
        'STT,Mã vé,Mã đơn hàng,Họ tên,Email,Số điện thoại,Hạng vé,Vị trí ghế,Trạng thái vé,Thời gian check-in,Ngày mua\n' +
        `1,TKT-EVH-2026-001,EVH-20261020-0001,Trần Minh Hoàng,customer@eventhub.vn,0933445566,VIP Diamond,A-12,USED,2026-10-23 18:30:15,2026-10-20\n` +
        `2,TKT-EVH-2026-002,EVH-20261020-0001,Lê Quỳnh Anh,quynhanh.le@gmail.com,0944556677,VIP Diamond,A-14,VALID,,2026-10-20\n` +
        `3,TKT-EVH-2026-003,EVH-20261021-0002,Nguyễn Văn Tuấn,tuan.nv@techcorp.vn,0911223344,Standard,C-08,USED,2026-10-23 18:45:00,2026-10-21\n`
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `DS_Khach_Tham_Gia_Event_${eventId}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }
  },

  // 9. Download Excel: Annual Financial Revenue Report
  async exportOrganizerRevenue(organizerId: number, year = 2026): Promise<void> {
    try {
      const res = await apiClient.get(`/export/organizers/${organizerId}/revenue`, {
        params: { year },
        responseType: 'blob',
      })
      const blob = new Blob([res.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `Bao_Cao_Tai_Chinh_Organizer_${organizerId}_${year}.xlsx`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } catch {
      // Offline fallback: generate mock CSV file
      const csvContent =
        '\uFEFF' +
        'Tháng,Số lượng đơn hàng,Doanh thu vé gộp (VNĐ),Chiết khấu Voucher (VNĐ),Doanh thu ròng (VNĐ)\n' +
        `Tháng 01/${year},120,150000000,15000000,135000000\n` +
        `Tháng 02/${year},180,240000000,24000000,216000000\n` +
        `Tháng 03/${year},210,310000000,31000000,279000000\n` +
        `Tháng 10/${year},195,295000000,29500000,265500000\n` +
        `TỔNG CỘNG,705,995000000,99500000,895500000\n`
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `Bao_Cao_Tai_Chinh_Organizer_${organizerId}_${year}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }
  },
}
