import apiClient from './api'
import type { ApiResponse, PageResponse, Event, Category, EventFilterParams, EventSeat, Venue } from '@/types'
import { MOCK_EVENTS, MOCK_CATEGORIES, generateMockSeatMap } from './mockData'
import { getEventDetailExtras, type EventDetailExtras } from './eventDetailData'

// Curated high-resolution Unsplash event banners
const EVENT_BANNERS = [
  'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80', // Tech AI Summit
  'https://images.unsplash.com/photo-1501386761578-eac5c94b800a?auto=format&fit=crop&w=1200&q=80', // Monsoon Music Fest
  'https://images.unsplash.com/photo-1511512578047-dfb367046420?auto=format&fit=crop&w=1200&q=80', // VCS Esports
  'https://images.unsplash.com/photo-1465847899084-d164df4dedc6?auto=format&fit=crop&w=1200&q=80', // Autumn Symphony
  'https://images.unsplash.com/photo-1515187029135-18ee286d815b?auto=format&fit=crop&w=1200&q=80', // Startup Pitch
  'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=1200&q=80', // Food & Music Festival
  'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1200&q=80', // Neon Lights Live
  'https://images.unsplash.com/photo-1533174072545-7a4b6ad7a6c3?auto=format&fit=crop&w=1200&q=80', // EDM Festival
]

function mapBackendEvent(raw: any, index: number): Event {
  const minPrice = raw.price !== undefined ? Number(raw.price) : raw.minPrice || 350000
  const maxPrice = raw.maxPrice || Math.round(minPrice * 2.5)

  // Choose appropriate banner
  let banner = raw.bannerUrl
  if (!banner) {
    const slug = (raw.categorySlug || raw.category?.slug || '').toLowerCase()
    if (slug.includes('cong-nghe')) banner = EVENT_BANNERS[0]
    else if (slug.includes('am-nhac')) banner = EVENT_BANNERS[1]
    else if (slug.includes('the-thao')) banner = EVENT_BANNERS[2]
    else if (slug.includes('nghe-thuat')) banner = EVENT_BANNERS[3]
    else if (slug.includes('khoi-nghiep')) banner = EVENT_BANNERS[4]
    else if (slug.includes('le-hoi')) banner = EVENT_BANNERS[5]
    else banner = EVENT_BANNERS[index % EVENT_BANNERS.length]
  }

  // Generate slug from title if missing
  const slug =
    raw.slug ||
    raw.title
      ?.toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)/g, '') ||
    `event-${raw.id}`

  const categoryObj: Category = raw.category || {
    id: raw.categoryId || 1,
    name: raw.categoryName || 'Sự kiện nổi bật',
    slug: raw.categorySlug || 'su-kien',
  }

  return {
    id: raw.id,
    title: raw.title,
    slug,
    shortDescription: raw.shortDescription || raw.description?.substring(0, 120) + '...',
    description: raw.description || '',
    bannerUrl: banner,
    locationName: raw.location || raw.locationName || 'Trung Tâm Triển Lãm & Sự Kiện',
    address: raw.address || raw.location || 'TP. Hồ Chí Minh',
    startDateTime: raw.startTime || raw.startDateTime || new Date(Date.now() + 86400000 * (index + 2)).toISOString(),
    endDateTime: raw.endTime || raw.endDateTime || new Date(Date.now() + 86400000 * (index + 3)).toISOString(),
    status: raw.status || 'PUBLISHED',
    minPrice,
    maxPrice,
    category: categoryObj,
    organizerName: raw.organizerName || 'Ban Tổ Chức Sự Kiện',
    hasSeatMap: raw.hasSeatMap !== undefined ? raw.hasSeatMap : true,
    featured: raw.featured !== undefined ? raw.featured : true,
    ticketTypes: raw.ticketTypes || [
      {
        id: raw.id * 100 + 1,
        name: 'Standard Pass',
        price: minPrice,
        totalQuantity: 1000,
        remainingQuantity: 280,
        minPerOrder: 1,
        maxPerOrder: 6,
      },
      {
        id: raw.id * 100 + 2,
        name: 'VIP Executive Pass',
        price: maxPrice,
        totalQuantity: 200,
        remainingQuantity: 42,
        minPerOrder: 1,
        maxPerOrder: 4,
      },
    ],
  }
}

export const eventService = {
  async getEvents(params?: EventFilterParams): Promise<PageResponse<Event>> {
    let allPool: Event[] = []

    try {
      const response = await apiClient.get<ApiResponse<PageResponse<any>>>('/events', { params: { size: 20 } })
      const backendItems = response.data.data?.content || []
      if (backendItems.length > 0) {
        allPool = backendItems.map((item: any, idx: number) => mapBackendEvent(item, idx))
      }
    } catch (err) {
      console.warn('Backend /events not reachable, using mock catalogue:', err)
    }

    // Merge mock events if needed to make catalogue rich and lively
    const existingTitles = new Set(allPool.map((e) => e.title.toLowerCase().trim()))
    for (const mock of MOCK_EVENTS) {
      if (!existingTitles.has(mock.title.toLowerCase().trim())) {
        allPool.push(mock)
      }
    }

    if (allPool.length === 0) {
      allPool = [...MOCK_EVENTS]
    }

    // Apply Filter Criteria
    let filtered = [...allPool]
    if (params?.keyword) {
      const kw = params.keyword.toLowerCase().trim()
      filtered = filtered.filter(
        (e) =>
          e.title.toLowerCase().includes(kw) ||
          e.locationName.toLowerCase().includes(kw) ||
          e.address.toLowerCase().includes(kw) ||
          e.shortDescription?.toLowerCase().includes(kw) ||
          e.organizerName?.toLowerCase().includes(kw)
      )
    }

    if (params?.categoryId) {
      const targetCatId = Number(params.categoryId)
      filtered = filtered.filter((e) => e.category.id === targetCatId)
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
      // default: soonest first
      filtered.sort((a, b) => new Date(a.startDateTime).getTime() - new Date(b.startDateTime).getTime())
    }

    const pageSize = params?.size || 100
    const pageNo = params?.page || 0
    const startIdx = pageNo * pageSize
    const paginated = filtered.slice(startIdx, startIdx + pageSize)

    return {
      content: paginated,
      pageNo,
      pageSize,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / pageSize) || 1,
      last: startIdx + pageSize >= filtered.length,
    }
  },

  async getEventById(id: number | string): Promise<Event> {
    try {
      const response = await apiClient.get<ApiResponse<any>>(`/events/${id}`)
      if (response.data.data) {
        return mapBackendEvent(response.data.data, 0)
      }
    } catch {
      // fallback
    }
    const found = MOCK_EVENTS.find((e) => e.id === Number(id)) || MOCK_EVENTS[0]
    return found
  },

  async getCategories(): Promise<Category[]> {
    try {
      const response = await apiClient.get<ApiResponse<Category[]>>('/categories')
      if (response.data.data?.length) {
        return response.data.data
      }
    } catch {
      // fallback
    }
    return MOCK_CATEGORIES
  },

  async getSeatMap(eventId: number): Promise<EventSeat[]> {
    try {
      const response = await apiClient.get<ApiResponse<any[]>>(`/events/${eventId}/seats`)
      if (response.data.data?.length) {
        return response.data.data.map((raw) => ({
          id: raw.id,
          seatId: raw.seatId,
          rowName: raw.seatRow || raw.rowName || 'A',
          seatRow: raw.seatRow || raw.rowName || 'A',
          seatNumber: raw.seatNumber,
          zone: raw.zone,
          status: raw.status,
          price: Number(raw.price),
          heldByCurrentUser: Boolean(raw.heldByCurrentUser),
          heldExpiresAt: raw.heldExpiresAt,
          remainingHoldSeconds: raw.remainingHoldSeconds,
        }))
      }
    } catch {
      // fallback
    }
    return generateMockSeatMap(eventId)
  },

  async getEventExtras(event: Event): Promise<EventDetailExtras> {
    return getEventDetailExtras(event)
  },

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

  async updateEventStatus(id: number, status: string): Promise<Event> {
    const response = await apiClient.patch<ApiResponse<any>>(`/events/${id}/status`, null, {
      params: { status }
    })
    return mapBackendEvent(response.data.data, 0)
  },

  async updateEvent(id: number, data: any): Promise<Event> {
    const response = await apiClient.put<ApiResponse<any>>(`/events/${id}`, data)
    return mapBackendEvent(response.data.data, 0)
  },

  async deleteEvent(id: number): Promise<void> {
    await apiClient.delete(`/events/${id}`)
  },

  async getEventTicketTypes(eventId: number): Promise<any[]> {
    const response = await apiClient.get<ApiResponse<any[]>>(`/events/${eventId}/ticket-types`)
    return response.data.data || []
  },

  async createTicketType(eventId: number, data: any): Promise<any> {
    const response = await apiClient.post<ApiResponse<any>>(`/events/${eventId}/ticket-types`, data)
    return response.data.data
  },

  async updateTicketType(eventId: number, ticketTypeId: number, data: any): Promise<any> {
    const response = await apiClient.put<ApiResponse<any>>(`/events/${eventId}/ticket-types/${ticketTypeId}`, data)
    return response.data.data
  },

  async deleteTicketType(eventId: number, ticketTypeId: number): Promise<void> {
    await apiClient.delete(`/events/${eventId}/ticket-types/${ticketTypeId}`)
  },

  async assignVenue(eventId: number, venueId: number, basePrice?: number): Promise<EventSeat[]> {
    const response = await apiClient.post<ApiResponse<EventSeat[]>>(`/events/${eventId}/assign-venue`, null, {
      params: { venueId, basePrice }
    })
    return response.data.data || []
  },

  async getVenues(): Promise<Venue[]> {
    try {
      const response = await apiClient.get<ApiResponse<Venue[]>>('/venues')
      return response.data.data || []
    } catch {
      return []
    }
  },

  async configureEventSeats(eventId: number, payload: { venueId?: number; defaultPrice?: number; seats: any[] }): Promise<EventSeat[]> {
    const response = await apiClient.post<ApiResponse<any[]>>(`/events/${eventId}/seat-map`, payload)
    return (response.data.data || []).map((raw) => ({
      id: raw.id,
      seatId: raw.seatId,
      rowName: raw.seatRow || raw.rowName || 'A',
      seatRow: raw.seatRow || raw.rowName || 'A',
      seatNumber: raw.seatNumber,
      zone: raw.zone,
      status: raw.status,
      price: Number(raw.price),
      heldByCurrentUser: Boolean(raw.heldByCurrentUser),
      heldExpiresAt: raw.heldExpiresAt,
      remainingHoldSeconds: raw.remainingHoldSeconds,
    }))
  },

  async batchUpdateEventSeats(eventId: number, payload: { eventSeatIds: number[]; price?: number; status?: string; zone?: string }): Promise<EventSeat[]> {
    const response = await apiClient.put<ApiResponse<any[]>>(`/events/${eventId}/seats/batch-update`, payload)
    return (response.data.data || []).map((raw) => ({
      id: raw.id,
      seatId: raw.seatId,
      rowName: raw.seatRow || raw.rowName || 'A',
      seatRow: raw.seatRow || raw.rowName || 'A',
      seatNumber: raw.seatNumber,
      zone: raw.zone,
      status: raw.status,
      price: Number(raw.price),
      heldByCurrentUser: Boolean(raw.heldByCurrentUser),
      heldExpiresAt: raw.heldExpiresAt,
      remainingHoldSeconds: raw.remainingHoldSeconds,
    }))
  },

  async configureVenueLayout(venueId: number, payload: { seats: any[] }): Promise<any> {
    const response = await apiClient.put<ApiResponse<any>>(`/venues/${venueId}/layout`, payload)
    return response.data.data
  },

  async createVenue(payload: { name: string; address: string; city?: string; capacity: number; numberOfRows?: number; seatsPerRow?: number; vipRowsCount?: number }): Promise<any> {
    const response = await apiClient.post<ApiResponse<any>>('/venues', payload)
    return response.data.data
  },
}
