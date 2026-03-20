import http from './httpAdmin'
import {
  AdminThreadStatsResp,
  GetThreadsParams,
  AdminThreadPageResp,
  AdminThread,
} from '@/types/thread'
import { MessagePageResp, GetThreadMessagesParams } from '@/types/message'

// The log in API
export const adminSignin = (data: { username: string; password: string }) =>
  http.post(`/admin/auth/signin`, data)

// The sign out api
export const adminSignout = () => http.post(`/admin/auth/signout`)

export const getAdminThreads = (params: GetThreadsParams = {}) =>
  http.get<AdminThreadPageResp>(`/admin/issues/getThreads`, {
    params: {
      status: params.status,
      size: params.size ?? 20,
      cursor: params.cursor ?? undefined,
    },
  })

export const getAdminThreadStats = () =>
  http.get<AdminThreadStatsResp>(`/admin/issues/thread-stats`)

export const getAdminMessages = (threadId: number, params: GetThreadMessagesParams = {}) =>
  http.get<MessagePageResp>(`/admin/issues/${threadId}/messages`, {
    params: {
      size: params.size ?? 20,
    },
  })

export const getAdminThread = (threadId: number) =>
  http.get<AdminThread>(`/admin/issues/${threadId}`)

export const postAdminMessage = (threadId: number, body: string) =>
  http.post(`/admin/issues/${threadId}/messages`, {
    body,
  })
