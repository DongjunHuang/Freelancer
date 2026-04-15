import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'
import { UserType } from '@/types/user'
import type {
  NotificationListResponse,
  MarkAllNotificationsReadResp,
  GetNotificationParams,
  GetNotificationUnreadCountResp,
  NotificationUnreadSummaryResponse,
} from '@/types/notification'

export const getNotificationUnreadCount = (userType: UserType) => {
  return getApiClient(userType).get<GetNotificationUnreadCountResp>(
    API_ENDPOINTS.notification.getUnreadCount(userType),
  )
}

export const markNotificationRead = (userType: UserType, ids: number[]) => {
  return getApiClient(userType).post(API_ENDPOINTS.notification.markRead(userType), { ids })
}

export const markAllNotificationsRead = (userType: UserType) => {
  return getApiClient(userType).post<MarkAllNotificationsReadResp>(
    API_ENDPOINTS.notification.markAllRead(userType),
  )
}

export const getNotifications = (userType: UserType, params: GetNotificationParams) => {
  return getApiClient(userType).get<NotificationListResponse>(
    API_ENDPOINTS.notification.getNotifications(userType),
    {
      params: {
        size: params.size ?? 20,
        cursor: params.cursor ?? undefined,
        status: params.status,
        category: params.category,
      },
    },
  )
}

export const getNotificationUnreadSummary = (userType: UserType) => {
  return getApiClient(userType).get<NotificationUnreadSummaryResponse>(
    API_ENDPOINTS.notification.getNotificationUnreadSummary(userType),
  )
}
