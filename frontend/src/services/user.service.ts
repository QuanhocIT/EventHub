import apiClient from './api'
import type {
  ApiResponse,
  PageResponse,
  User,
  OrganizerProfile,
  UpdateOrganizerProfilePayload,
  FollowedOrganizerItem,
  Booking,
} from '@/types'

export interface UpdateProfileRequest {
  fullName: string
  phoneNumber?: string
  avatarUrl?: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
  confirmPassword: string
}

export interface CheckInResult {
  ticketCode: string
  bookingCode: string
  eventTitle: string
  attendeeName: string
  ticketTypeName: string
  seatLabel?: string
  checkInAt: string
  checkedInBy: string
}

// Fallback Mock Storage for Offline / Preview
const MOCK_PROFILE: User = {
  id: 4,
  username: 'customer',
  email: 'customer@eventhub.vn',
  fullName: 'Trần Minh Hoàng',
  phoneNumber: '0933445566',
  role: 'ROLE_CUSTOMER',
  avatarUrl: 'https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?auto=format&fit=crop&w=300&q=80',
}

let mockFollowed: FollowedOrganizerItem[] = [
  {
    id: 2,
    organizationName: 'Nexus Tech & Concert Media',
    logoUrl: 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=200&q=80',
    bio: 'Đơn vị tổ chức các đại nhạc hội và hội nghị công nghệ quy mô hàng đầu Việt Nam.',
    verified: true,
    followersCount: 14200,
    followedAt: '2026-03-12T10:00:00Z',
  },
  {
    id: 1,
    organizationName: 'Vietnam Symphony Orchestra Foundation',
    logoUrl: 'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=200&q=80',
    bio: 'Ban tổ chức các chương trình giao hưởng thính phòng và hòa nhạc hàn lâm cổ điển.',
    verified: true,
    followersCount: 8900,
    followedAt: '2026-02-18T14:30:00Z',
  },
]

let mockBookings: Booking[] = [
  {
    id: 1,
    bookingCode: 'EVH-20261020-0001',
    eventId: 1,
    eventTitle: 'Skyline Symphony & Philharmonic Concert 2026',
    eventBannerUrl: 'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=1200&q=80',
    eventStartDateTime: '2026-11-20T19:30:00Z',
    venueName: 'Nhà hát Lớn Hà Nội, Số 1 Tràng Tiền',
    status: 'CONFIRMED',
    subtotal: 3000000,
    discountAmount: 300000,
    couponCode: 'EVENTHUB2026',
    totalAmount: 2700000,
    expiresAt: '2026-10-20T20:00:00Z',
    createdAt: '2026-10-20T19:45:00Z',
    items: [
      { ticketTypeName: 'VIP Diamond', seatLabel: 'A-12', unitPrice: 1500000, quantity: 1, subtotal: 1500000 },
      { ticketTypeName: 'VIP Diamond', seatLabel: 'A-14', unitPrice: 1500000, quantity: 1, subtotal: 1500000 },
    ],
  },
  {
    id: 2,
    bookingCode: 'EVH-20261021-0002',
    eventId: 2,
    eventTitle: 'Vietnam AI & Cloud Computing Summit 2026',
    eventBannerUrl: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80',
    eventStartDateTime: '2026-11-28T08:30:00Z',
    venueName: 'Trung tâm Hội nghị Quốc gia NCC, Hà Nội',
    status: 'CONFIRMED',
    subtotal: 450000,
    discountAmount: 0,
    totalAmount: 450000,
    expiresAt: '2026-10-21T09:00:00Z',
    createdAt: '2026-10-21T08:45:00Z',
    items: [
      { ticketTypeName: 'Standard Phổ Thông', seatLabel: 'C-08', unitPrice: 450000, quantity: 1, subtotal: 450000 },
    ],
  },
  {
    id: 3,
    bookingCode: 'EVH-20261022-0003',
    eventId: 3,
    eventTitle: 'Hanoi Midnight Heritage Marathon 2026',
    eventBannerUrl: 'https://images.unsplash.com/photo-1452626038306-9aae5e071dd3?auto=format&fit=crop&w=1200&q=80',
    eventStartDateTime: '2026-12-05T23:00:00Z',
    venueName: 'Quảng trường Đông Kinh Nghĩa Thục, Hoàn Kiếm',
    status: 'PENDING_PAYMENT',
    subtotal: 650000,
    discountAmount: 100000,
    couponCode: 'WELCOMEVIP',
    totalAmount: 550000,
    expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
    createdAt: new Date().toISOString(),
    items: [
      { ticketTypeName: 'Cự ly 21KM Bán Marathon', seatLabel: 'BIB-21088', unitPrice: 650000, quantity: 1, subtotal: 650000 },
    ],
  },
]

export const userService = {
  // 1. Get Current User Profile
  async getMyProfile(): Promise<User> {
    try {
      const res = await apiClient.get<ApiResponse<User>>('/users/me')
      return res.data?.data || res.data
    } catch {
      const stored = localStorage.getItem('eventhub_user')
      if (stored) {
        return JSON.parse(stored)
      }
      return MOCK_PROFILE
    }
  },

  // 2. Update Profile
  async updateProfile(payload: UpdateProfileRequest): Promise<User> {
    try {
      const res = await apiClient.put<ApiResponse<User>>('/users/me', payload)
      const updated = res.data?.data || res.data
      const current = localStorage.getItem('eventhub_user')
      const merged = current ? { ...JSON.parse(current), ...updated } : updated
      localStorage.setItem('eventhub_user', JSON.stringify(merged))
      return merged
    } catch {
      const current = localStorage.getItem('eventhub_user')
      const user = current ? JSON.parse(current) : { ...MOCK_PROFILE }
      user.fullName = payload.fullName
      user.phoneNumber = payload.phoneNumber
      user.avatarUrl = payload.avatarUrl || user.avatarUrl
      localStorage.setItem('eventhub_user', JSON.stringify(user))
      return user
    }
  },

  // 3. Change Password
  async changePassword(payload: ChangePasswordRequest): Promise<void> {
    const res = await apiClient.put<ApiResponse<void>>('/users/me/change-password', payload)
    return res.data?.data
  },

  // 4. Get User's Bookings
  async getMyBookings(page = 0, size = 10): Promise<{ content: Booking[]; totalElements: number; totalPages: number }> {
    try {
      const res = await apiClient.get<ApiResponse<PageResponse<Booking>>>('/bookings/my', {
        params: { page, size },
      })
      const pageData = res.data?.data || res.data
      return {
        content: pageData.content || pageData,
        totalElements: pageData.totalElements ?? mockBookings.length,
        totalPages: pageData.totalPages ?? 1,
      }
    } catch {
      return {
        content: mockBookings,
        totalElements: mockBookings.length,
        totalPages: 1,
      }
    }
  },

  // 5. Cancel Booking
  async cancelBooking(bookingCode: string): Promise<Booking> {
    try {
      const res = await apiClient.post<ApiResponse<Booking>>(`/bookings/${bookingCode}/cancel`)
      return res.data?.data || res.data
    } catch {
      const b = mockBookings.find((item) => item.bookingCode === bookingCode)
      if (b) {
        b.status = 'CANCELLED'
        return b
      }
      throw new Error('Đơn hàng không tồn tại hoặc không thể hủy.')
    }
  },

  // 6. Get Followed Organizers
  async getFollowedOrganizers(page = 0, size = 10): Promise<{ content: FollowedOrganizerItem[]; totalElements: number }> {
    try {
      const res = await apiClient.get<ApiResponse<PageResponse<FollowedOrganizerItem>>>('/organizers/following', {
        params: { page, size },
      })
      const pageData = res.data?.data || res.data
      return {
        content: pageData.content || pageData,
        totalElements: pageData.totalElements ?? mockFollowed.length,
      }
    } catch {
      return {
        content: mockFollowed,
        totalElements: mockFollowed.length,
      }
    }
  },

  // 7. Follow Organizer
  async followOrganizer(organizerId: number): Promise<void> {
    await apiClient.post(`/organizers/${organizerId}/follow`)
  },

  // 8. Unfollow Organizer
  async unfollowOrganizer(organizerId: number): Promise<void> {
    try {
      await apiClient.delete(`/organizers/${organizerId}/follow`)
    } catch {
      mockFollowed = mockFollowed.filter((item) => item.id !== organizerId)
    }
  },
}
