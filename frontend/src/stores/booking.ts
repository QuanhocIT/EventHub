import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Event, EventSeat, TicketType, Coupon } from '@/types'
import { bookingService } from '@/services/booking.service'
import { seatHoldService } from '@/services/seatHold.service'
import { useAuthStore } from './auth'

export const useBookingStore = defineStore('booking', () => {
  const currentEvent = ref<Event | null>(null)
  const selectedSeats = ref<EventSeat[]>([])
  const selectedTicketQuantities = ref<Record<number, number>>({}) // ticketTypeId -> quantity
  const appliedCoupon = ref<Coupon | null>(null)
  const isHolding = ref(false)
  const holdExpiresAt = ref<number | null>(null)

  // Subtotal for ticket tiers
  const tierSubtotal = computed(() => {
    if (!currentEvent.value?.ticketTypes) return 0
    return currentEvent.value.ticketTypes.reduce((acc, tt) => {
      const qty = selectedTicketQuantities.value[tt.id] || 0
      return acc + qty * tt.price
    }, 0)
  })

  // Subtotal for seat map seats
  const seatSubtotal = computed(() => {
    return selectedSeats.value.reduce((acc, seat) => acc + seat.price, 0)
  })

  const subtotal = computed(() => {
    return tierSubtotal.value + seatSubtotal.value
  })

  const discountAmount = computed(() => {
    if (!appliedCoupon.value || subtotal.value < appliedCoupon.value.minOrderAmount) {
      return 0
    }
    if (appliedCoupon.value.discountType === 'PERCENTAGE') {
      const discount = Math.round((subtotal.value * appliedCoupon.value.discountValue) / 100)
      return appliedCoupon.value.maxDiscountAmount
        ? Math.min(discount, appliedCoupon.value.maxDiscountAmount)
        : discount
    }
    return appliedCoupon.value.discountValue
  })

  const totalAmount = computed(() => {
    return Math.max(0, subtotal.value - discountAmount.value)
  })

  const totalTicketCount = computed(() => {
    const tierCount = Object.values(selectedTicketQuantities.value).reduce((a, b) => a + b, 0)
    return tierCount + selectedSeats.value.length
  })

  function setEvent(event: Event) {
    if (currentEvent.value?.id !== event.id) {
      clearBooking(false)
      currentEvent.value = event
    }
  }

  async function toggleSeat(seat: EventSeat) {
    const authStore = useAuthStore()
    const idx = selectedSeats.value.findIndex((s) => s.id === seat.id)

    if (idx > -1) {
      // Deselecting seat
      if (authStore.isAuthenticated && currentEvent.value) {
        try {
          await seatHoldService.releaseSeats(currentEvent.value.id, [seat.id])
        } catch (err) {
          console.warn('Failed to release seat hold on server:', err)
        }
      }
      selectedSeats.value.splice(idx, 1)
      if (selectedSeats.value.length === 0) {
        isHolding.value = false
        holdExpiresAt.value = null
      }
    } else {
      // Selecting seat
      if (seat.status !== 'AVAILABLE') {
        throw new Error(`Ghế ${seat.rowName || seat.seatRow}${seat.seatNumber} hiện không khả dụng!`)
      }
      if (selectedSeats.value.length >= 6) {
        throw new Error('Bạn chỉ được chọn tối đa 6 ghế cho mỗi lượt đặt!')
      }

      // If user is authenticated, immediately hold on backend with pessimistic lock & Redis TTL
      if (authStore.isAuthenticated && currentEvent.value) {
        try {
          const holdRes = await seatHoldService.holdSeats(currentEvent.value.id, [seat.id])
          isHolding.value = true
          if (holdRes.expiresAt) {
            holdExpiresAt.value = new Date(holdRes.expiresAt).getTime()
          } else {
            holdExpiresAt.value = Date.now() + 15 * 60 * 1000
          }
        } catch (err: any) {
          const msg = err.response?.data?.message || err.message || 'Ghế này vừa được người khác giữ chỗ!'
          throw new Error(msg)
        }
      } else {
        // Fallback local hold
        if (!isHolding.value) {
          startHoldTimer(15)
        }
      }

      selectedSeats.value.push(seat)
    }
  }

  function updateTicketQuantity(ticketType: TicketType, quantity: number) {
    if (quantity < 0) return
    if (quantity > ticketType.maxPerOrder) {
      throw new Error(`Tối đa ${ticketType.maxPerOrder} vé cho hạng ${ticketType.name}`)
    }
    if (quantity > ticketType.remainingQuantity) {
      throw new Error(`Chỉ còn ${ticketType.remainingQuantity} vé cho hạng ${ticketType.name}`)
    }
    selectedTicketQuantities.value[ticketType.id] = quantity
  }

  async function applyCoupon(code: string) {
    const coupon = await bookingService.validateCoupon(code)
    if (coupon) {
      if (subtotal.value < coupon.minOrderAmount) {
        throw new Error(
          `Đơn hàng cần tối thiểu ${new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(
            coupon.minOrderAmount
          )} để áp dụng mã này!`
        )
      }
      appliedCoupon.value = coupon
      return true
    }
    return false
  }

  function removeCoupon() {
    appliedCoupon.value = null
  }

  function startHoldTimer(minutes = 15) {
    isHolding.value = true
    holdExpiresAt.value = Date.now() + minutes * 60 * 1000
  }

  async function clearBooking(releaseServer = true) {
    const authStore = useAuthStore()
    if (releaseServer && authStore.isAuthenticated && currentEvent.value && selectedSeats.value.length > 0) {
      try {
        await seatHoldService.releaseSeats(
          currentEvent.value.id,
          selectedSeats.value.map((s) => s.id)
        )
      } catch (err) {
        console.warn('Failed to release server holds on clearBooking:', err)
      }
    }
    selectedSeats.value = []
    selectedTicketQuantities.value = {}
    appliedCoupon.value = null
    isHolding.value = false
    holdExpiresAt.value = null
  }

  return {
    currentEvent,
    selectedSeats,
    selectedTicketQuantities,
    appliedCoupon,
    isHolding,
    holdExpiresAt,
    seatSubtotal,
    tierSubtotal,
    subtotal,
    discountAmount,
    totalAmount,
    totalTicketCount,
    setEvent,
    toggleSeat,
    updateTicketQuantity,
    applyCoupon,
    removeCoupon,
    startHoldTimer,
    clearBooking,
  }
})
