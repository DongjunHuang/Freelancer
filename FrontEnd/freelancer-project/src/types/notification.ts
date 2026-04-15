export interface NotificationItem {
  id: number
  title: string
  content: string
  actionUrl?: string | null
  isRead: boolean
  eventCount?: number | null
  lastEventAt: string
  category: string
}
export interface MarkAllNotificationsReadResp {
  updatedCount: number
  unreadCount: number
}

export interface GetNotificationUnreadCountResp {
  unreadCount: number
}

export interface NotificationListResponse {
  items: NotificationItem[]
  nextCursor: string | null
  unreadCount: number
}

export interface MarkReadRequest {
  ids: number[]
}

export type GetNotificationParams = {
  size?: number
  cursor?: string | null
  category: string | null
  status: string
}

export interface NotificationCategorySummary {
  category: string
  label: string
  unreadCount: number
}

export interface NotificationStoreLike {
  unreadCount: number
  summaryLoading: boolean
  categorySummaries: NotificationCategorySummary[]
  bellOpen: boolean
  lastUnreadFetchedAt: number | null

  setUnreadCount: (count: number) => void
  setSummaryLoading: (value: boolean) => void
  setUnreadSummary: (unread: number, categories: NotificationCategorySummary[]) => void

  markAllReadLocally: () => void
  decrementCategoryLocally: (category: string, count?: number) => void

  closeBell: () => void
  toggleBell: () => void
}

export interface NotificationUnreadSummaryResponse {
  unreadCount: number
  categories: NotificationCategorySummary[]
}

export interface NotificationPageResponse {
  items: NotificationItem[]
  nextCursor: string | null
  unreadCount: number
  categories: NotificationCategorySummary[]
}
