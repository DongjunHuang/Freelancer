export enum ThreadStatus {
  WAITING_ADMIN = 'WAITING_ADMIN',
  WAITING_USER = 'WAITING_USER',
  RESOLVED = 'RESOLVED',
}

export enum ThreadFilterStatus {
  OPEN = 'OPEN',
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

export interface Thread {
  id: number
  title: string
  status: ThreadStatus
  type: ThreadType
  lastMessageAt: string
  createdAt: string
  unreadByUser: number
  unreadByAdmin: number
}

export interface GetThreadsParams {
  status?: string
  size?: number
  cursor?: string | null
}

export interface ThreadPageResp {
  items: Thread[]
  nextCursor: string | null
  hasMore: boolean
}

export type AdminThreadStatsResp = {
  all: number
  waitingAdmin: number
  waitingUser: number
  resolved: number
  open: number
}

export type AdminThread = {
  id: number
  userId: number
  title: string
  status: ThreadStatus
  type: ThreadType
  createdAt: string
  lastMessageAt: string
  unreadByUser: number
  unreadByAdmin: number
}

export interface AdminThreadPageResp {
  items: AdminThread[]
  nextCursor: string | null
  hasMore: boolean
}
