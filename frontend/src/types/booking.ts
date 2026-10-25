export type BookingStatus = 'PENDING_PAYMENT' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED'
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED'
export type PaymentProvider = 'MOCK_SANDBOX' | 'VIETQR' | 'MOMO' | 'VNPAY' | 'CREDIT_CARD'
export type TicketStatus = 'VALID' | 'USED' | 'CANCELLED'

export interface PaymentResponse {
  id: number
  transactionCode: string
  bookingCode: string
  eventId: number
  eventTitle: string
  amount: number
  provider: PaymentProvider
  status: PaymentStatus
  paymentUrl?: string
  qrCodeUrl?: string
  bankName?: string
  accountNumber?: string
  accountName?: string
  transferContent?: string
  remainingSeconds?: number
  paymentTime?: string
  errorMessage?: string
  createdAt?: string
}

export interface Coupon {
  code: string
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT'
  discountValue: number
  maxDiscountAmount?: number
  minOrderAmount: number
  description?: string
}

export interface BookingItem {
  id?: number
  ticketTypeId?: number
  ticketTypeName: string
  eventSeatId?: number
  seatLabel?: string
  unitPrice: number
  quantity: number
  subtotal: number
}

export interface Booking {
  id: number
  bookingCode: string
  eventId: number
  eventTitle: string
  eventBannerUrl?: string
  eventStartDateTime: string
  venueName: string
  status: BookingStatus
  subtotal: number
  discountAmount: number
  couponCode?: string
  totalAmount: number
  expiresAt: string
  createdAt: string
  items: BookingItem[]
}

export interface Ticket {
  id: number
  ticketCode: string
  bookingCode: string
  eventId: number
  eventTitle: string
  eventBannerUrl?: string
  eventStartDateTime: string
  venueName: string
  ticketTypeName: string
  seatLabel?: string
  qrCodeBase64: string
  status: TicketStatus
  attendeeName: string
  checkInAt?: string
  price?: number
  gate?: string
  zone?: string
  orderDate?: string
  transferredTo?: string
  transferredAt?: string
  cancelReason?: string
  categoryName?: string
  googleMapsUrl?: string
  invoiceRequested?: boolean
  invoiceTaxCode?: string
  invoiceCompanyName?: string
  hasReviewed?: boolean
  reviewRating?: number
  reviewComment?: string
  refundAmount?: number
  refundDate?: string
  refundMethod?: string
}

export interface CreateBookingRequest {
  eventId: number
  items: {
    ticketTypeId?: number
    quantity: number
    eventSeatId?: number
  }[]
  couponCode?: string
}
