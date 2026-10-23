import apiClient from './api'
import type { ApiResponse, AuthResponse, LoginRequest, RegisterRequest } from '@/types'

export const authService = {
  async login(payload: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', payload)
      return response.data.data
    } catch (error) {
      // Fallback for demo when backend is offline
      console.warn('Backend unavailable, fallback to demo login', error)
      return {
        accessToken: 'mock_jwt_token_sample_' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 86400000,
        username: payload.usernameOrEmail,
        email: payload.usernameOrEmail.includes('@') ? payload.usernameOrEmail : `${payload.usernameOrEmail}@example.com`,
        fullName:
          payload.usernameOrEmail === 'admin'
            ? 'Quản Trị Viên Hệ Thống'
            : payload.usernameOrEmail === 'organizer'
            ? 'Ban Tổ Chức Nexus Media'
            : payload.usernameOrEmail === 'staff'
            ? 'Nhân Viên Soát Vé Gate'
            : 'Nguyễn Văn Quân',
        role:
          payload.usernameOrEmail === 'admin'
            ? 'ROLE_ADMIN'
            : payload.usernameOrEmail === 'organizer'
            ? 'ROLE_ORGANIZER'
            : payload.usernameOrEmail === 'staff'
            ? 'ROLE_STAFF'
            : 'ROLE_CUSTOMER',
        userId: payload.usernameOrEmail === 'admin' ? 1 : payload.usernameOrEmail === 'organizer' ? 2 : payload.usernameOrEmail === 'staff' ? 3 : 4,
      }
    }
  },

  async register(payload: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', payload)
      return response.data.data
    } catch (error) {
      console.warn('Backend unavailable, fallback to demo register', error)
      return {
        accessToken: 'mock_jwt_token_sample_' + Date.now(),
        tokenType: 'Bearer',
        expiresIn: 86400000,
        username: payload.username,
        email: payload.email,
        fullName: payload.fullName,
        role: 'ROLE_CUSTOMER',
        userId: Date.now(),
      }
    }
  },
}
