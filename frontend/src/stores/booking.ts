import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Event, EventSeat, TicketType, Coupon } from '@/types'
import { bookingService } from '@/services/booking.service'

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
      clearBooking()
      currentEvent.value = event
    }
  }

  function toggleSeat(seat: EventSeat) {
    if (seat.status !== 'AVAILABLE') return
    const idx = selectedSeats.value.findIndex((s) => s.id === seat.id)
    if (idx > -1) {
      selectedSeats.value.splice(idx, 1)
    } else {
      if (selectedSeats.value.length >= 6) {
        throw new Error('Bạn chỉ được chọn tối đa 6 ghế cho mỗi lượt đặt!')
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

  function clearBooking() {
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
