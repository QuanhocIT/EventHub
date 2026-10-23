import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, RegisterRequest } from '@/types'
import { authService } from '@/services/auth.service'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('eventhub_token'))
  const user = ref<User | null>(
    localStorage.getItem('eventhub_user') ? JSON.parse(localStorage.getItem('eventhub_user')!) : null
  )
  const loading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'ROLE_ADMIN')
  const isOrganizer = computed(() => user.value?.role === 'ROLE_ORGANIZER' || user.value?.role === 'ROLE_ADMIN')
  const isStaff = computed(() => user.value?.role === 'ROLE_STAFF' || user.value?.role === 'ROLE_ADMIN')

  async function login(credentials: LoginRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await authService.login(credentials)
      token.value = res.accessToken
      user.value = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
      }
      localStorage.setItem('eventhub_token', res.accessToken)
      localStorage.setItem('eventhub_user', JSON.stringify(user.value))
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Đăng nhập không thành công'
      return false
    } finally {
      loading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await authService.register(data)
      token.value = res.accessToken
      user.value = {
        id: res.userId,
        username: res.username,
        email: res.email,
        fullName: res.fullName,
        role: res.role,
      }
      localStorage.setItem('eventhub_token', res.accessToken)
      localStorage.setItem('eventhub_user', JSON.stringify(user.value))
      return true
    } catch (err: any) {
      error.value = err.response?.data?.message || err.message || 'Đăng ký không thành công'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('eventhub_token')
    localStorage.removeItem('eventhub_user')
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    isOrganizer,
    isStaff,
    login,
    register,
    logout,
  }
})
