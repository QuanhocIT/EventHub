import apiClient from './api'
import type { ApiResponse } from '@/types'

export interface SeatHoldResponse {
  eventId: number
  eventSeatIds: number[]
  heldByUsername: string
  expiresAt: string
  holdDurationSeconds: number
}

export const seatHoldService = {
  /**
   * Hold seats temporarily with server-enforced Pessimistic Lock & Redis TTL
   */
  async holdSeats(eventId: number, eventSeatIds: number[]): Promise<SeatHoldResponse> {
    const res = await apiClient.post<ApiResponse<SeatHoldResponse>>('/seat-holds', {
      eventId,
      eventSeatIds,
    })
    return res.data.data
  },

  /**
   * Proactively release seats held by current user
   */
  async releaseSeats(eventId: number, eventSeatIds: number[]): Promise<void> {
    await apiClient.delete('/seat-holds', {
      data: {
        eventId,
        eventSeatIds,
      },
    })
  },

  /**
   * Subscribe to real-time seat status updates via Server-Sent Events (SSE)
   * with automatic fallback polling if disconnected.
   */
  subscribeSeatStream(
    eventId: number,
    onUpdate: (payload: { action: 'HELD' | 'RELEASED' | 'SOLD'; seatIds: number[]; heldByUsername: string }) => void
  ): () => void {
    const baseURL = (apiClient.defaults.baseURL || '/api/v1').replace(/\/+$/, '')
    const streamUrl = `${baseURL}/events/${eventId}/seats/stream`

    let eventSource: EventSource | null = null
    let pollInterval: any = null

    try {
      eventSource = new EventSource(streamUrl)

      eventSource.addEventListener('SEAT_UPDATE', (e: MessageEvent) => {
        try {
          const data = JSON.parse(e.data)
          onUpdate(data)
        } catch (err) {
          console.error('Failed to parse SSE seat update:', err)
        }
      })

      eventSource.onerror = () => {
        // Fallback to polling every 5s if SSE is unavailable
        if (!pollInterval) {
          pollInterval = setInterval(async () => {
            try {
              const res = await apiClient.get<ApiResponse<any[]>>(`/events/${eventId}/seats`)
              if (res.data.data) {
                const heldIds = res.data.data.filter((s) => s.status === 'HELD').map((s) => s.id)
                if (heldIds.length > 0) {
                  onUpdate({ action: 'HELD', seatIds: heldIds, heldByUsername: '' })
                }
              }
            } catch {
              // Ignore polling errors
            }
          }, 5000)
        }
      }
    } catch {
      // Direct fallback
      pollInterval = setInterval(async () => {
        try {
          const res = await apiClient.get<ApiResponse<any[]>>(`/events/${eventId}/seats`)
          if (res.data.data) {
            const heldIds = res.data.data.filter((s) => s.status === 'HELD').map((s) => s.id)
            if (heldIds.length > 0) {
              onUpdate({ action: 'HELD', seatIds: heldIds, heldByUsername: '' })
            }
          }
        } catch {
          // Ignore polling errors
        }
      }, 5000)
    }

    return () => {
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      if (pollInterval) {
        clearInterval(pollInterval)
        pollInterval = null
      }
    }
  },
}
