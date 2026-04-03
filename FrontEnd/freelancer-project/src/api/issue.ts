import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'
import { UserType } from '@/types/user'

import type {
  ThreadStatus,
  Thread,
  GetThreadsReq,
  ThreadPageResp,
  ThreadStatsResp,
  GetMessagesParams,
  PostMessageResp,
  GetLatestMessagesResp,
  MessagePageResp,
} from '@/types/issue'

// USER only api to create feedback thread
export const createThread = (
  title: string,
  description: string,
  impact: string | null,
  type: string,
) =>
  getApiClient().post(API_ENDPOINTS.issue.createThread, {
    title,
    description,
    impact,
    type,
  })

// USER/ADMIN API to post messages
export const postMessage = (userType: UserType, threadId: number, body: string) => {
  return getApiClient(userType).post<PostMessageResp>(
    API_ENDPOINTS.issue.postMessage(userType, threadId),
    {
      body,
    },
  )
}

// USER/ADMIN API to get messages
export const getMessages = (
  userType: UserType,
  threadId: number,
  params: GetMessagesParams = {},
) => {
  return getApiClient(userType).get<MessagePageResp>(
    API_ENDPOINTS.issue.getMessages(userType, threadId),
    {
      params: {
        size: params.size ?? 20,
        cursor: params.cursor ?? undefined,
      },
    },
  )
}

// USER/ADMIN API to get single thread
export const getThread = (userType: UserType, threadId: number) => {
  return getApiClient(userType).get<Thread>(API_ENDPOINTS.issue.getThread(userType, threadId))
}

// USER/ADMIN API to update thread status
export const updateThreadStatus = (userType: UserType, threadId: number, status: ThreadStatus) => {
  return getApiClient(userType).patch(API_ENDPOINTS.issue.updateThreadStatus(userType, threadId), {
    status,
  })
}

// USER/ADMIN API to get threads
export const getThreads = (userType: UserType, params: GetThreadsReq = {}) => {
  return getApiClient(userType).get<ThreadPageResp>(API_ENDPOINTS.issue.getThreads(userType), {
    params: {
      status: params.status ?? undefined,
      size: params.size ?? 20,
      cursor: params.cursor ?? undefined,
    },
  })
}

// USER/ADMIN API to get thread stats
export const getThreadStats = (userType: UserType) => {
  return getApiClient(userType).get<ThreadStatsResp>(API_ENDPOINTS.issue.getThreadStats(userType))
}

// USER/ADMIN API to get messages for the corresponding thread after specific time
export const getLatestMessages = (userType: UserType, threadId: number, after: string) => {
  return getApiClient(userType).get<GetLatestMessagesResp>(
    API_ENDPOINTS.issue.getLatestMessages(userType, threadId),
    {
      params: {
        after,
      },
    },
  )
}

// USER/ADMIN API to mark the thread as read.
export const markThreadAsRead = (userType: UserType, threadId: number) => {
  return getApiClient(userType).patch(API_ENDPOINTS.issue.markThreadAsRead(userType, threadId))
}
