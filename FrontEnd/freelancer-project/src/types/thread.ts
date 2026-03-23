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

export type ThreadStatsResp = {
  all: number
  waitingAdmin: number
  waitingUser: number
  resolved: number
  open: number
}

export type Thread = {
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
