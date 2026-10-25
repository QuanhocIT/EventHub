export type EventStatus = 'DRAFT' | 'PUBLISHED' | 'POSTPONED' | 'CANCELLED' | 'COMPLETED'
export type SeatZone = 'VIP' | 'STANDARD' | 'BALCONY'
export type SeatStatus = 'AVAILABLE' | 'HELD' | 'SOLD' | 'BLOCKED'

export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  icon?: string
}

export interface Venue {
  id: number
  name: string
  address: string
  city: string
  capacity: number
  hasSeatMap: boolean
}

export interface TicketType {
  id: number
  name: string
  description?: string
  price: number
  totalQuantity: number
  remainingQuantity: number
  minPerOrder: number
  maxPerOrder: number
  colorHex?: string
}

export interface EventSeat {
  id: number
  seatId: number
  rowName: string
  seatRow?: string
  seatNumber: number
  zone: SeatZone
  status: SeatStatus
  price: number
  heldUntil?: string
  heldByCurrentUser?: boolean
  heldExpiresAt?: string
  remainingHoldSeconds?: number
}

export interface Event {
  id: number
  title: string
  slug: string
  shortDescription: string
  description: string
  bannerUrl: string
  locationName: string
  address: string
  startDateTime: string
  endDateTime: string
  status: EventStatus
  minPrice: number
  maxPrice: number
  category: Category
  venue?: Venue
  organizerName: string
  hasSeatMap: boolean
  ticketTypes?: TicketType[]
  featured?: boolean
}

export interface EventFilterParams {
  keyword?: string
  categoryId?: number
  city?: string
  startDate?: string
  endDate?: string
  minPrice?: number
  maxPrice?: number
  hasSeatMap?: boolean
  featured?: boolean
  timeRange?: string
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}
