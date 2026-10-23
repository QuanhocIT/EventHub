export type UserRole = 'ROLE_CUSTOMER' | 'ROLE_ORGANIZER' | 'ROLE_STAFF' | 'ROLE_ADMIN'

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  phoneNumber?: string
  role: UserRole
  avatarUrl?: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  username: string
  email: string
  fullName: string
  role: UserRole
  userId: number
}

export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  fullName: string
  phoneNumber?: string
}
