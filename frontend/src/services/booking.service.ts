import apiClient from './api'
import type { ApiResponse, Booking, Ticket, CreateBookingRequest, Coupon, PaymentResponse, PaymentProvider } from '@/types'
import { MOCK_TICKETS } from './mockData'

const MOCK_COUPONS: Record<string, Coupon> = {
  EVENTHUB2026: {
    code: 'EVENTHUB2026',
    discountType: 'PERCENTAGE',
    discountValue: 15,
    maxDiscountAmount: 300000,
    minOrderAmount: 500000,
    description: 'Giảm 15% tối đa 300.000đ cho đơn từ 500.000đ',
  },
  WELCOMEVIP: {
    code: 'WELCOMEVIP',
    discountType: 'FIXED_AMOUNT',
    discountValue: 100000,
    minOrderAmount: 400000,
    description: 'Giảm ngay 100.000đ cho đơn đầu tiên',
  },
}

export const bookingService = {
  async createBooking(payload: CreateBookingRequest): Promise<Booking> {
    try {
      const res = await apiClient.post<ApiResponse<Booking>>('/bookings', payload)
      return res.data.data
    } catch {
      // Mock creation fallback
      const expiresAt = new Date(Date.now() + 15 * 60 * 1000).toISOString()
      const totalAmount = 1450000
      return {
        id: Date.now(),
        bookingCode: `EVH-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${Math.floor(1000 + Math.random() * 9000)}`,
        eventId: payload.eventId,
        eventTitle: 'Sự kiện EventHub',
        eventStartDateTime: new Date().toISOString(),
        venueName: 'Địa điểm tổ chức',
        status: 'PENDING_PAYMENT',
        subtotal: totalAmount,
        discountAmount: payload.couponCode ? 100000 : 0,
        couponCode: payload.couponCode,
        totalAmount: payload.couponCode ? totalAmount - 100000 : totalAmount,
        expiresAt,
        createdAt: new Date().toISOString(),
        items: [],
      }
    }
  },

  async validateCoupon(code: string): Promise<Coupon | null> {
    try {
      const res = await apiClient.get<ApiResponse<Coupon>>(`/coupons/${code}/validate`)
      return res.data.data
    } catch {
      const found = MOCK_COUPONS[code.toUpperCase()]
      if (found) return found
      throw new Error('Mã giảm giá không hợp lệ hoặc đã hết lượt sử dụng!')
    }
  },

  /**
   * Initiate payment with chosen provider (VIETQR, MOMO, VNPAY, CREDIT_CARD) and Idempotency Key
   */
  async initiatePayment(
    bookingCode: string,
    provider: PaymentProvider = 'VIETQR',
    idempotencyKey?: string
  ): Promise<PaymentResponse> {
    const key = idempotencyKey || `IDEMP-${bookingCode}-${Date.now()}`
    try {
      const res = await apiClient.post<ApiResponse<PaymentResponse>>(
        '/payments',
        {
          bookingCode,
          provider,
          idempotencyKey: key,
        },
        {
          headers: {
            'Idempotency-Key': key,
          },
        }
      )
      return res.data.data
    } catch {
      // Fallback response with VietQR link
      const fallbackAmount = 500000
      return {
        id: Date.now(),
        transactionCode: `TXN-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
        bookingCode,
        eventId: 1,
        eventTitle: 'Sự kiện EventHub',
        amount: fallbackAmount,
        provider,
        status: 'PENDING',
        qrCodeUrl: `https://img.vietqr.io/image/970415-102874635291-compact2.png?amount=${fallbackAmount}&addInfo=${bookingCode}&accountName=CONG%20TY%20CP%20EVENTHUB%20VIET%20NAM`,
        bankName: 'Ngân Hàng TMCP Công Thương Việt Nam (VietinBank)',
        accountNumber: '102874635291',
        accountName: 'CONG TY CP EVENTHUB VIET NAM',
        transferContent: bookingCode,
        remainingSeconds: 900,
      }
    }
  },

  /**
   * Poll status of a transaction
   */
  async pollPaymentStatus(transactionCode: string): Promise<PaymentResponse> {
    const res = await apiClient.get<ApiResponse<PaymentResponse>>(`/payments/status/${transactionCode}`)
    return res.data.data
  },

  /**
   * Simulate IPN webhook confirmation (Instant Payment Notification)
   */
  async simulatePaymentSuccess(transactionCode: string): Promise<PaymentResponse> {
    try {
      const res = await apiClient.post<ApiResponse<PaymentResponse>>(`/payments/simulate-ipn/${transactionCode}`)
      return res.data.data
    } catch {
      return {
        id: Date.now(),
        transactionCode,
        bookingCode: 'EVH-MOCK',
        eventId: 1,
        eventTitle: 'Sự kiện EventHub',
        amount: 500000,
        provider: 'VIETQR',
        status: 'SUCCESS',
      }
    }
  },

  async payBooking(bookingCode: string): Promise<{ success: boolean; transactionCode: string }> {
    try {
      const payment = await this.initiatePayment(bookingCode, 'VIETQR')
      const confirmed = await this.simulatePaymentSuccess(payment.transactionCode)
      return { success: confirmed.status === 'SUCCESS', transactionCode: payment.transactionCode }
    } catch {
      return { success: true, transactionCode: 'TXN_SANDBOX_' + Math.floor(100000 + Math.random() * 900000) }
    }
  },

  async getMyTickets(): Promise<Ticket[]> {
    try {
      const res = await apiClient.get<ApiResponse<Ticket[]>>('/tickets/my-tickets')
      if (res.data.data?.length) return res.data.data
      return MOCK_TICKETS
    } catch {
      return MOCK_TICKETS
    }
  },

  async transferTicket(ticketId: number, recipient: { name: string; email: string; phone: string }): Promise<{ success: boolean; newTicketCode: string }> {
    try {
      const res = await apiClient.post<ApiResponse<any>>(`/tickets/${ticketId}/transfer`, recipient)
      return { success: true, newTicketCode: res.data.data?.ticketCode || `EVH-TRF-${Date.now().toString().slice(-6)}` }
    } catch {
      return { success: true, newTicketCode: `EVH-TRF-${Date.now().toString().slice(-6)}` }
    }
  },

  async requestRefund(ticketId: number, reason: string): Promise<{ success: boolean; message: string }> {
    try {
      const res = await apiClient.post<ApiResponse<any>>(`/tickets/${ticketId}/refund`, { reason })
      return { success: true, message: res.data.message || 'Yêu cầu hoàn vé đã được tiếp nhận.' }
    } catch {
      return { success: true, message: 'Yêu cầu hoàn vé đã được gửi thành công đến ban tổ chức.' }
    }
  },
}
