export interface Movie {
  id: number
  title: string
  slug: string
  description?: string
  director?: string
  actorsCast?: string
  genre: string
  durationMinutes: number
  releaseDate: string
  ageRatingCode: string // "P", "K", "T13", "T16", "T18", "C"
  ageRatingDesc?: string
  status: 'NOW_SHOWING' | 'COMING_SOON' | 'ENDED'
  posterUrl: string
  bannerUrl?: string
  trailerUrl?: string
  formatDisplay?: string
  language?: string
  imdbRating?: number
}

export interface CinemaComplex {
  id: number
  name: string
  city: string
  address: string
  phone?: string
  totalScreens: number
  imageUrl?: string
}

export interface Showtime {
  id: number
  movieId: number
  movieTitle: string
  moviePoster?: string
  ageRating?: string
  complexId: number
  complexName: string
  city: string
  screenId: number
  screenName: string
  screenType: 'STANDARD_2D' | 'IMAX_LASER' | 'DOLBY_ATMOS' | 'GOLD_CLASS' | 'SCREEN_X'
  startTime: string
  endTime: string
  basePrice: number
  status: string
}

export interface CinemaSeat {
  seatCode: string
  row: string
  number: number
  zone: 'STANDARD' | 'VIP' | 'SWEETBOX'
  price: number
  status: 'AVAILABLE' | 'HELD' | 'SOLD'
  isPair?: boolean
  pairWith?: string
}

export interface ConcessionItem {
  id: number
  name: string
  description?: string
  price: number
  imageUrl?: string
  category: 'COMBO' | 'POPCORN' | 'BEVERAGE' | 'SNACK'
  status: string
  isHotDeal?: boolean
}

export interface CinemaBookingRequest {
  showtimeId: number
  seatCodes: string[]
  concessions?: {
    concessionId: number
    quantity: number
  }[]
  couponCode?: string
}
