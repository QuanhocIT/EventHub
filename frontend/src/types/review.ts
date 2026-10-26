export interface Review {
  id: number
  eventId: number
  userId: number
  reviewerName: string
  reviewerAvatar?: string
  rating: number
  comment: string
  imageUrls?: string[]
  likesCount?: number
  isLiked?: boolean
  organizerReply?: string
  organizerReplyAt?: string
  isVerifiedAttendee?: boolean
  createdAt: string
  updatedAt?: string
}

export interface CreateReviewPayload {
  rating: number
  comment: string
  imageUrls?: string[]
}

export interface EventRatingSummary {
  averageRating: number
  totalReviews: number
  ratingBreakdown: Record<number, number>
}
