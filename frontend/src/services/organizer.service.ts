import apiClient from './api'
import type {
  ApiResponse,
  OrganizerProfile,
  UpdateOrganizerProfilePayload,
  Category,
  Coupon,
} from '@/types'

export interface CreateEventApiRequest {
  title: string
  description?: string
  location?: string
  startTime: string
  endTime: string
  capacity: number
  price: number
  status?: string
  categoryId?: number
}

export interface CheckInResponse {
  ticketCode: string
  bookingCode: string
  eventTitle: string
  attendeeName: string
  ticketTypeName: string
  seatLabel?: string
  checkInAt: string
  checkedInBy: string
}

const MOCK_ORGANIZER_PROFILE: OrganizerProfile = {
  id: 2,
  userId: 2,
  username: 'organizer',
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
  isFollowing: false,
}

let mockCategories: Category[] = [
  { id: 1, name: 'Âm Nhạc & Concert', slug: 'am-nhac', description: 'Đại nhạc hội, liveshow ca sĩ và hòa nhạc thính phòng', icon: 'Music' },
  { id: 2, name: 'Công Nghệ & Hội Thảo', slug: 'cong-nghe', description: 'Diễn đàn AI, Cloud Computing, Startup & DevFest', icon: 'Cpu' },
  { id: 3, name: 'Thể Thao & Giải Đấu', slug: 'the-thao', description: 'Marathon đêm, giải bóng đá, bóng rổ chuyên nghiệp', icon: 'Trophy' },
  { id: 4, name: 'Sân Khấu & Triển Lãm', slug: 'san-khau', description: 'Kịch nói nghệ thuật, triển lãm tranh & văn hóa đa phương tiện', icon: 'Palette' },
]

export const organizerService = {
  // 1. Get Logged-in Organizer Profile
  async getMyOrganizerProfile(): Promise<OrganizerProfile> {
    try {
      const res = await apiClient.get<ApiResponse<OrganizerProfile>>('/organizers/me')
      return res.data?.data || res.data
    } catch {
      return MOCK_ORGANIZER_PROFILE
    }
  },

  // 2. Update Organizer Profile
  async updateMyOrganizerProfile(payload: UpdateOrganizerProfilePayload): Promise<OrganizerProfile> {
    try {
      const res = await apiClient.put<ApiResponse<OrganizerProfile>>('/organizers/me', payload)
      return res.data?.data || res.data
    } catch {
      Object.assign(MOCK_ORGANIZER_PROFILE, payload)
      return MOCK_ORGANIZER_PROFILE
    }
  },

  // 3. Admin: Verify Organizer Badge
  async verifyOrganizer(id: number, verified: boolean): Promise<OrganizerProfile> {
    try {
      const res = await apiClient.patch<ApiResponse<OrganizerProfile>>(`/organizers/${id}/verify`, null, {
        params: { verified },
      })
      return res.data?.data || res.data
    } catch {
      if (id === MOCK_ORGANIZER_PROFILE.id) {
        MOCK_ORGANIZER_PROFILE.verified = verified
      }
      return MOCK_ORGANIZER_PROFILE
    }
  },

  // 4. Create Event via Real API
  async createEvent(payload: CreateEventApiRequest): Promise<any> {
    const res = await apiClient.post<ApiResponse<any>>('/events', payload)
    return res.data?.data || res.data
  },

  // 5. Staff Gate Check-in via Real API
  async checkInTicket(ticketCode: string): Promise<CheckInResponse> {
    const res = await apiClient.post<ApiResponse<CheckInResponse>>(`/tickets/${ticketCode}/check-in`)
    return res.data?.data || res.data
  },

  // 6. Categories Management
  async getCategories(): Promise<Category[]> {
    try {
      const res = await apiClient.get<ApiResponse<Category[]>>('/categories')
      const data = res.data?.data || res.data
      return Array.isArray(data) ? data : mockCategories
    } catch {
      return mockCategories
    }
  },

  async createCategory(payload: { name: string; slug: string; description?: string; iconUrl?: string }): Promise<Category> {
    try {
      const res = await apiClient.post<ApiResponse<Category>>('/categories', payload)
      return res.data?.data || res.data
    } catch {
      const newCat: Category = {
        id: Date.now(),
        name: payload.name,
        slug: payload.slug,
        description: payload.description,
        icon: payload.iconUrl || 'Tag',
      }
      mockCategories.push(newCat)
      return newCat
    }
  },

  async updateCategory(id: number, payload: { name: string; slug: string; description?: string; iconUrl?: string }): Promise<Category> {
    try {
      const res = await apiClient.put<ApiResponse<Category>>(`/categories/${id}`, payload)
      return res.data?.data || res.data
    } catch {
      const cat = mockCategories.find((c) => c.id === id)
      if (cat) {
        cat.name = payload.name
        cat.slug = payload.slug
        cat.description = payload.description
        return cat
      }
      throw new Error('Danh mục không tồn tại')
    }
  },

  async deleteCategory(id: number): Promise<void> {
    try {
      await apiClient.delete(`/categories/${id}`)
    } catch {
      mockCategories = mockCategories.filter((c) => c.id !== id)
    }
  },

  // 7. Coupons Management
  async getCoupons(): Promise<any[]> {
    try {
      const res = await apiClient.get<ApiResponse<any>>('/coupons')
      const data = res.data?.data || res.data
      return data?.content || (Array.isArray(data) ? data : [])
    } catch {
      return [
        { id: 1, code: 'EVENTHUB2026', discountType: 'PERCENTAGE', discountValue: 15, maxDiscountAmount: 300000, minOrderAmount: 500000, maxUsage: 500, usedCount: 142, expiresAt: '2026-12-31' },
        { id: 2, code: 'WELCOMEVIP', discountType: 'FIXED_AMOUNT', discountValue: 100000, maxDiscountAmount: 100000, minOrderAmount: 400000, maxUsage: 200, usedCount: 88, expiresAt: '2026-11-30' },
        { id: 3, code: 'TECHSUMMIT50', discountType: 'PERCENTAGE', discountValue: 50, maxDiscountAmount: 500000, minOrderAmount: 800000, maxUsage: 50, usedCount: 46, expiresAt: '2026-11-28' },
      ]
    }
  },

  async createCoupon(payload: {
    code: string
    discountType: 'PERCENTAGE' | 'FIXED_AMOUNT'
    discountValue: number
    maxDiscountAmount?: number
    minOrderAmount?: number
    maxUsage?: number
    expiresAt?: string
    eventId?: number
  }): Promise<any> {
    const res = await apiClient.post<ApiResponse<any>>('/coupons', payload)
    return res.data?.data || res.data
  },

  async deleteCoupon(id: number): Promise<void> {
    await apiClient.delete(`/coupons/${id}`)
  },
}
