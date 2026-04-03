import { UserType } from '@/types/user'

export enum ThreadStatus {
  WAITING_ADMIN = 'WAITING_ADMIN',
  WAITING_USER = 'WAITING_USER',
  RESOLVED = 'RESOLVED',
}

export enum ThreadType {
  BUG = 'BUG',
  PERFORMANCE = 'PERFORMANCE',
  UX = 'UX',
  SUGGESTION = 'SUGGESTION',
}

// Basic message type
export type Message = {
  id: number
  threadId: number
  userType: UserType
  senderId: number
  body: string
  isInternal: boolean
  createdAt: string
}

// Basic thread type
export type Thread = {
  id: number
  userId: number
  username: string
  title: string
  status: ThreadStatus
  type: ThreadType
  createdAt: string
  lastMessageAt: string
  unreadByUser: number
  unreadByAdmin: number
}

export type PostMessageResp = {
  message: Message
  thread: Thread
}

export interface GetThreadsReq {
  status?: string
  size?: number
  cursor?: string | null
}

export interface ThreadPageResp {
  items: Thread[]
  nextCursor: string | null
  hasMore: boolean
}

export type ThreadStatsResp = {
  all: number
  waitingAdmin: number
  waitingUser: number
  resolved: number
  open: number
}

export type MessagePageResp = {
  items: Message[]
  nextCursor: string | null
  hasMore: boolean
}

export type GetMessagesParams = {
  size?: number
  cursor?: string | null
}

export type MessageState = {
  items: Message[]
  nextCursor: string | null
  hasMore: boolean
  initialized: boolean
  loading: boolean
  loadingMore: boolean
}

export type GetLatestMessagesResp = {
  items: Message[]
}
