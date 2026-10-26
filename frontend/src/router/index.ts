import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import EventsView from '@/views/EventsView.vue'
import EventDetailView from '@/views/EventDetailView.vue'
import BookingView from '@/views/BookingView.vue'
import MyTicketsView from '@/views/MyTicketsView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import DashboardView from '@/views/DashboardView.vue'
import ProfileView from '@/views/ProfileView.vue'
import CinemaView from '@/views/CinemaView.vue'
import MovieDetailView from '@/views/MovieDetailView.vue'
import PromotionsView from '@/views/PromotionsView.vue'
import NewsView from '@/views/NewsView.vue'
import AboutView from '@/views/AboutView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
  },
  {
    path: '/promotions',
    alias: '/vouchers',
    name: 'promotions',
    component: PromotionsView,
  },
  {
    path: '/news',
    name: 'news',
    component: NewsView,
  },
  {
    path: '/about',
    name: 'about',
    component: AboutView,
  },
  {
    path: '/events',
    name: 'events',
    component: EventsView,
  },
  {
    path: '/events/:id',
    name: 'event-detail',
    component: EventDetailView,
  },
  {
    path: '/events/:id/manage',
    alias: ['/admin/events/:id/manage', '/admin/events/:id'],
    redirect: (to) => ({
      path: '/dashboard',
      query: { tab: 'events', manageId: to.params.id },
    }),
  },
  {
    path: '/cinema',
    alias: '/movies',
    name: 'cinema',
    component: CinemaView,
  },
  {
    path: '/cinemas',
    alias: ['/theaters', '/rap-chieu'],
    name: 'cinemas',
    component: () => import('@/views/CinemasView.vue'),
  },
  {
    path: '/cinemas/:id',
    alias: ['/theaters/:id'],
    name: 'cinema-detail',
    component: () => import('@/views/CinemaDetailView.vue'),
  },
  {
    path: '/movies/:id',
    name: 'movie-detail',
    component: MovieDetailView,
  },
  {
    path: '/booking/:eventId',
    name: 'booking',
    component: BookingView,
  },
  {
    path: '/my-tickets',
    name: 'my-tickets',
    component: MyTicketsView,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DashboardView,
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN', 'ROLE_ORGANIZER', 'ROLE_STAFF'],
    },
  },
  {
    path: '/staff',
    redirect: '/staff/scanner',
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STAFF', 'ROLE_ADMIN', 'ROLE_ORGANIZER'],
    },
  },
  {
    path: '/staff/scanner',
    name: 'staff-scanner',
    component: () => import('@/views/StaffScannerView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_STAFF', 'ROLE_ADMIN', 'ROLE_ORGANIZER'],
    },
  },
  {
    path: '/organizer/seat-map-builder',
    name: 'seat-map-builder',
    component: () => import('@/views/SeatMapBuilderView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ORGANIZER', 'ROLE_ADMIN'],
    },
  },
  {
    path: '/admin',
    redirect: '/dashboard',
    meta: {
      requiresAuth: true,
      roles: ['ROLE_ADMIN'],
    },
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// Navigation Guard: Protect private and role-restricted routes
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('eventhub_token')
  const userStr = localStorage.getItem('eventhub_user')
  const user = userStr ? JSON.parse(userStr) : null

  // 1. Check if route requires authentication
  if (to.meta.requiresAuth && !token) {
    return next({
      path: '/login',
      query: { redirect: to.fullPath },
    })
  }

  // 2. Check if route requires specific roles (e.g. ROLE_ADMIN, ROLE_ORGANIZER)
  if (to.meta.roles && Array.isArray(to.meta.roles)) {
    const requiredRoles = to.meta.roles as string[]
    if (!user || !requiredRoles.includes(user.role)) {
      // Access Denied: redirect to home or login
      return next({
        path: '/',
        query: { error: 'access_denied' },
      })
    }
  }

  next()
})

export default router
