export interface OrganizerProfile {
  id: number
  userId?: number
  username?: string
  organizationName: string
  bio?: string
  logoUrl?: string
  bannerUrl?: string
  website?: string
  contactEmail?: string
  contactPhone?: string
  facebookUrl?: string
  linkedinUrl?: string
  verified: boolean
  followersCount: number
  totalEvents?: number
  averageRating?: number
  isFollowing?: boolean
}

export interface UpdateOrganizerProfilePayload {
  organizationName: string
  bio?: string
  logoUrl?: string
  bannerUrl?: string
  website?: string
  contactEmail?: string
  contactPhone?: string
  facebookUrl?: string
  linkedinUrl?: string
}

export interface FollowedOrganizerItem {
  id: number
  organizationName: string
  logoUrl?: string
  bio?: string
  verified: boolean
  followersCount: number
  followedAt?: string
}
