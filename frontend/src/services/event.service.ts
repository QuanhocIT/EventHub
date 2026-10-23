import apiClient from './api'
import type { ApiResponse, PageResponse, Event, Category, EventFilterParams, EventSeat } from '@/types'
import { MOCK_EVENTS, MOCK_CATEGORIES, generateMockSeatMap } from './mockData'
import { getEventDetailExtras, type EventDetailExtras } from './eventDetailData'

export const eventService = {
  async getEvents(params?: EventFilterParams): Promise<PageResponse<Event>> {
    try {
      const response = await apiClient.get<ApiResponse<PageResponse<Event>>>('/events', { params })
      if (response.data.data?.content?.length) {
        return response.data.data
      }
      throw new Error('Empty backend data, use mock')
    } catch {
      // Filter mock events
      let filtered = [...MOCK_EVENTS]
      if (params?.keyword) {
        const kw = params.keyword.toLowerCase().trim()
        filtered = filtered.filter(
          (e) =>
            e.title.toLowerCase().includes(kw) ||
            e.locationName.toLowerCase().includes(kw) ||
            e.address.toLowerCase().includes(kw) ||
            e.shortDescription.toLowerCase().includes(kw) ||
            e.organizerName.toLowerCase().includes(kw)
        )
      }
      if (params?.categoryId) {
        filtered = filtered.filter((e) => e.category.id === Number(params.categoryId))
      }
      if (params?.city && params.city !== 'ALL') {
        const c = params.city.toLowerCase()
        filtered = filtered.filter(
          (e) => e.locationName.toLowerCase().includes(c) || e.address.toLowerCase().includes(c)
        )
      }
      if (params?.hasSeatMap) {
        filtered = filtered.filter((e) => e.hasSeatMap)
      }
      if (params?.featured) {
        filtered = filtered.filter((e) => e.featured)
      }
      if (params?.minPrice !== undefined) {
        filtered = filtered.filter((e) => e.minPrice >= params.minPrice!)
      }
      if (params?.maxPrice !== undefined) {
        filtered = filtered.filter((e) => e.minPrice <= params.maxPrice!)
      }

      // Time Range Filter
      if (params?.timeRange && params.timeRange !== 'ALL') {
        const now = Date.now()
        filtered = filtered.filter((e) => {
          const eventTime = new Date(e.startDateTime).getTime()
          const diffDays = Math.ceil((eventTime - now) / (1000 * 60 * 60 * 24))
          if (params.timeRange === 'THIS_WEEK') return diffDays >= 0 && diffDays <= 7
          if (params.timeRange === 'THIS_MONTH') return diffDays >= 0 && diffDays <= 30
          if (params.timeRange === 'UPCOMING') return diffDays >= 0
          return true
        })
      }

      // Sorting
      if (params?.sortBy === 'price_asc') {
        filtered.sort((a, b) => a.minPrice - b.minPrice)
      } else if (params?.sortBy === 'price_desc') {
        filtered.sort((a, b) => b.minPrice - a.minPrice)
      } else if (params?.sortBy === 'featured') {
        filtered.sort((a, b) => (b.featured ? 1 : 0) - (a.featured ? 1 : 0))
      } else if (params?.sortBy === 'date_desc') {
        filtered.sort((a, b) => new Date(b.startDateTime).getTime() - new Date(a.startDateTime).getTime())
      } else {
        // default: date_asc (soonest first)
        filtered.sort((a, b) => new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime())
      }

      return {
        content: filtered,
        pageNo: params?.page || 0,
        pageSize: params?.size || 12,
        totalElements: filtered.length,
        totalPages: Math.ceil(filtered.length / (params?.size || 12)) || 1,
        last: true,
      }
    }
  },

  async getEventById(id: number | string): Promise<Event> {
    try {
      const response = await apiClient.get<ApiResponse<Event>>(`/events/${id}`)
      if (response.data.data) {
        return response.data.data
      }
      throw new Error('Not found in backend')
    } catch {
      const found = MOCK_EVENTS.find((e) => e.id === Number(id)) || MOCK_EVENTS[0]
      return found
    }
  },

  async getCategories(): Promise<Category[]> {
    try {
      const response = await apiClient.get<ApiResponse<Category[]>>('/categories')
      if (response.data.data?.length) {
        return response.data.data
      }
      return MOCK_CATEGORIES
    } catch {
      return MOCK_CATEGORIES
    }
  },

  async getSeatMap(eventId: number): Promise<EventSeat[]> {
    try {
      const response = await apiClient.get<ApiResponse<EventSeat[]>>(`/events/${eventId}/seats`)
      if (response.data.data?.length) {
        return response.data.data
      }
      return generateMockSeatMap(eventId)
    } catch {
      return generateMockSeatMap(eventId)
    }
  },

  /** Extended detail content (agenda, lineup, FAQ, policies, reviews...). */
  async getEventExtras(event: Event): Promise<EventDetailExtras> {
    return getEventDetailExtras(event)
  },

  /** Events in the same category (fallback: other featured events). */
  async getRelatedEvents(event: Event, limit = 4): Promise<Event[]> {
    let pool: Event[] = MOCK_EVENTS
    try {
      const page = await this.getEvents({ categoryId: event.category.id, size: limit + 1 })
      if (page.content.length) pool = page.content
    } catch {
      /* use mock pool */
    }
    const sameCat = pool.filter((e) => e.id !== event.id && e.category.id === event.category.id)
    const others = MOCK_EVENTS.filter((e) => e.id !== event.id && !sameCat.some((s) => s.id === e.id))
    return [...sameCat, ...others].slice(0, limit)
  },
}
