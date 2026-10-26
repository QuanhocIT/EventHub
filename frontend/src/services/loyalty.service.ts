import apiClient from './api'
import type { ApiResponse } from '@/types/api'
import type { LoyaltyProfile } from '@/types/loyalty'

export const loyaltyService = {
  async getMyProfile(): Promise<LoyaltyProfile> {
    try {
      const res = await apiClient.get<ApiResponse<LoyaltyProfile>>('/loyalty/profile')
      return res.data.data
    } catch {
      // Fallback demo mock
      return {
        username: 'john_doe',
        fullName: 'Nguyễn Văn Quân',
        pointsBalance: 680,
        lifetimePoints: 680,
        tier: 'SILVER',
        tierDisplayName: 'Hội viên Bạc (Silver)',
        discountPercent: 5,
        nextTierPointsRequired: 820,
        nextTierProgressPercent: 45.3,
        recentTransactions: [
          {
            id: 1,
            pointsChange: 50,
            activityType: 'REVIEW_WITH_PHOTO',
            activityName: 'Thưởng đánh giá có hình ảnh',
            description: 'Đánh giá chi tiết sự kiện Vietnam Tech Summit 2026 kèm 3 hình ảnh',
            createdAt: '2026-10-25T14:30:00',
          },
          {
            id: 2,
            pointsChange: 38,
            activityType: 'BOOKING_REWARD',
            activityName: 'Tích điểm đặt vé xem phim',
            description: 'Đặt vé Thanh Gươm Diệt Quỷ (IMAX Laser)',
            createdAt: '2026-10-24T18:20:00',
          },
          {
            id: 3,
            pointsChange: 20,
            activityType: 'GATE_CHECKIN_BONUS',
            activityName: 'Thưởng check-in tại cổng',
            description: 'Check-in thành công qua cổng Cổng A - Lối VIP',
            createdAt: '2026-10-22T09:12:00',
          },
          {
            id: 4,
            pointsChange: 100,
            activityType: 'SIGNUP_BONUS',
            activityName: 'Quà tặng thành viên mới',
            description: 'Chào mừng gia nhập cộng đồng EventHub & CineClub',
            createdAt: '2026-10-15T08:00:00',
          },
        ],
      }
    }
  },
}
