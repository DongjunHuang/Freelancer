import { UserType } from '@/types/user'

export type Message = {
  id: number
  threadId: number
  userType: UserType
  senderId: number
  body: string
  isInternal: boolean
  createdAt: string
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
