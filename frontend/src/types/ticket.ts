export interface CheckInResponse {
  success: boolean
  message: string
  ticketCode: string
  attendeeName: string
  attendeeEmail?: string
  attendeePhone?: string
  eventId?: number
  eventTitle?: string
  seatInfo?: string
  ticketTypeName?: string
  checkInTime?: string
  staffUsername?: string
  bookingCode?: string
  status?: string
}

export interface GateTierStatResponse {
  ticketTypeId: number
  ticketTypeName: string
  totalCount: number
  checkedInCount: number
  remainingCount: number
  checkInRatePercent: number
}

export interface GateStatsResponse {
  eventId: number
  eventTitle: string
  eventStartTime?: string
  location?: string
  totalTickets: number
  checkedInCount: number
  remainingCount: number
  checkInRatePercent: number
  tierStats: GateTierStatResponse[]
  recentCheckIns: CheckInResponse[]
}

export interface TicketResponse {
  id: number
  ticketCode: string
  bookingCode: string
  eventId: number
  eventTitle: string
  attendeeName: string
  ticketTypeName: string
  seatInfo?: string
  status: 'VALID' | 'USED' | 'CANCELLED' | 'EXPIRED'
  qrCodeBase64?: string
  checkedInAt?: string
  checkedInByStaff?: string
  createdAt: string
}
