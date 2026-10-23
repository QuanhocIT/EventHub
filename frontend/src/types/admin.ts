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

export interface EventAnalyticsData {
  eventId: number
  eventTitle: string
  totalRevenue: number
  totalTickets: number
  checkedInTickets: number
  checkInRate: number
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
