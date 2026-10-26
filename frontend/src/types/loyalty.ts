export type LoyaltyTier = 'MEMBER' | 'SILVER' | 'GOLD' | 'DIAMOND'

export interface LoyaltyTransaction {
  id: number
  pointsChange: number
  activityType: string
  activityName: string
  description?: string
  createdAt: string
}

export interface LoyaltyProfile {
  username: string
  fullName: string
  pointsBalance: number
  lifetimePoints: number
  tier: LoyaltyTier
  tierDisplayName: string
  discountPercent: number
  nextTierPointsRequired: number
  nextTierProgressPercent: number
  recentTransactions: LoyaltyTransaction[]
}
