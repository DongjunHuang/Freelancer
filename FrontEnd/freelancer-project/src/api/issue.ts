import http from '@/api/http-user'
import httpa from '@/api/http-admin'
import { MessagePageResp, GetMessagesParams } from '@/types/message'
import { UserType } from '@/types/user'

import type { AxiosInstance } from 'axios'
import type {
  ThreadStatus,
  Thread,
  GetThreadsParams,
  ThreadPageResp,
  ThreadStatsResp,
} from '@/types/thread'

type ApiContext = {
  client: AxiosInstance
  prefix: string
}

function getApiContext(userType: UserType = UserType.USER): ApiContext {
  if (userType === UserType.ADMIN) {
    return {
      client: httpa,
      prefix: '/admin',
    }
  }

  return {
    client: http,
    prefix: '',
  }
}

// USER API to create feedback thread
export const createThread = (
  title: string,
  description: string,
  impact: string | null,
  type: string,
) =>
  http.post(`/issues/createThread`, {
    title,
    description,
    impact,
    type,
  })

// USER/ADMIN API to post messages
export const postMessage = (userType: UserType, threadId: number, body: string) => {
  const apiContext = getApiContext(userType)
  return apiContext.client.post(`${apiContext.prefix}/issues/${threadId}/postMessage`, {
    body,
  })
}

// USER/ADMIN API to get messages
export const getMessages = (
  userType: UserType,
  threadId: number,
  params: GetMessagesParams = {},
) => {
  const apiContext = getApiContext(userType)

  return apiContext.client.get<MessagePageResp>(
    `${apiContext.prefix}/issues/${threadId}/getMessages`,
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
  const apiContext = getApiContext(userType)
  return apiContext.client.get<Thread>(`${apiContext.prefix}/issues/${threadId}`)
}

// USER/ADMIN API to update thread status
export const updateThreadStatus = (userType: UserType, threadId: number, status: ThreadStatus) => {
  const apiContext = getApiContext(userType)

  return apiContext.client.patch(`${apiContext.prefix}/issues/${threadId}/status`, { status })
}

// USER/ADMIN API to get threads
export const getThreads = (userType: UserType, params: GetThreadsParams = {}) => {
  const apiContext = getApiContext(userType)

  return apiContext.client.get<ThreadPageResp>(`${apiContext.prefix}/issues/getThreads`, {
    params: {
      status: params.status ?? undefined,
      size: params.size ?? 20,
      cursor: params.cursor ?? undefined,
    },
  })
}

// USER/ADMIN API to get thread stats
export const getThreadStats = (userType: UserType) => {
  const apiContext = getApiContext(userType)
  return apiContext.client.get<ThreadStatsResp>(`${apiContext.prefix}/issues/thread-stats`)
}
