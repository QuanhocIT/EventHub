import type { UserRole } from './auth'

export interface AdminDashboardData {
  totalUsers: number
  totalCustomers: number
  totalOrganizers: number
  totalStaff: number
  totalEvents: number
  publishedEvents: number
  totalRevenue: number
  totalBookings: number
  confirmedBookings: number
  totalTickets: number
}

export interface OrganizerDashboardData {
  organizerId: number
  totalEvents: number
  publishedEvents: number
  totalRevenue: number
  totalTicketsIssued: number
  totalTicketsCheckedIn: number
  checkInRate: number
}

export interface HourlySalesItem {
  timeSlot: string
  revenue: number
  ticketsSold: number
}

export interface TierSalesItem {
  tierName: string
  price: number
  soldCount: number
  totalCapacity: number
  revenue: number
  fillPercent: number
}

export interface CheckInFlowItem {
  timeSlot: string
  count: number
}

export interface EventAnalyticsData {
  eventId: number
  eventTitle: string
  venueName?: string
  totalRevenue: number
  netRevenue?: number
  discountTotal?: number
  totalTickets: number
  totalCapacity?: number
  occupancyRate?: number
  checkedInTickets: number
  checkInRate: number
  averageOrderValue?: number
  hourlySalesVelocity?: HourlySalesItem[]
  tierBreakdown?: TierSalesItem[]
  paymentMethods?: Record<string, number>
  rushHourCheckIn?: CheckInFlowItem[]
}

export interface AdminUserItem {
  id: number
  username: string
  email: string
  fullName: string
  phoneNumber?: string
  role: UserRole
  avatarUrl?: string
  enabled: boolean
  createdAt: string
}

export interface AuditLogItem {
  id: number
  action: string
  entityType: string
  entityId: string
  username: string
  userId?: number
  ipAddress: string
  userAgent: string
  status: 'SUCCESS' | 'FAILED'
  errorMessage?: string
  executionTimeMs: number
  createdAt: string
}
