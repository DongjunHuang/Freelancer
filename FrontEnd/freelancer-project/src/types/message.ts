export enum UserType {
  USER = 'USER',
  ADMIN = 'ADMIN',
}

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

export type GetThreadMessagesParams = {
  size?: number
}
