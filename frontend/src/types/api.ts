export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  errors?: Record<string, string> | null
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  pageNo: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}
