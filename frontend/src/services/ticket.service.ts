import apiClient from './api'
import type { ApiResponse } from '@/types/api'
import type { CheckInResponse, GateStatsResponse, TicketResponse } from '@/types/ticket'

export const ticketService = {
  /**
   * Perform gate check-in for a ticket
   */
  async checkInTicket(ticketCode: string): Promise<CheckInResponse> {
    const res = await apiClient.post<ApiResponse<CheckInResponse>>(`/tickets/${ticketCode}/check-in`)
    return res.data?.data || res.data
  },

  /**
   * Pre-verify ticket validity without checking in
   */
  async verifyTicket(ticketCode: string): Promise<CheckInResponse> {
    const res = await apiClient.get<ApiResponse<CheckInResponse>>(`/tickets/${ticketCode}/verify`)
    return res.data?.data || res.data
  },

  /**
   * Get real-time gate statistics for an event
   */
  async getGateStats(eventId: number): Promise<GateStatsResponse> {
    const res = await apiClient.get<ApiResponse<GateStatsResponse>>(`/tickets/events/${eventId}/gate-stats`)
    return res.data?.data || res.data
  },

  /**
   * Get current user's purchased tickets
   */
  async getMyTickets(page = 0, size = 10): Promise<any> {
    const res = await apiClient.get<ApiResponse<any>>('/tickets/my', {
      params: { page, size }
    })
    return res.data?.data || res.data
  },

  /**
   * Get ticket details by ticket code
   */
  async getTicketByCode(ticketCode: string): Promise<TicketResponse> {
    const res = await apiClient.get<ApiResponse<TicketResponse>>(`/tickets/${ticketCode}`)
    return res.data?.data || res.data
  }
}

export default ticketService
