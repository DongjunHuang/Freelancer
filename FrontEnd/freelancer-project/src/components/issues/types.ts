export type DialogueRole = 'user' | 'admin' | 'system'

export type DialogueMessage = {
  id: number
  role: DialogueRole
  content: string
  createdAt: string
  isInternal?: boolean
}

export type MessageDialogueProps = {
  messages: DialogueMessage[]
  loading?: boolean
  loadingMore?: boolean
  sending?: boolean
  canReply?: boolean
  hasMore?: boolean
  replyText?: string
  replyPlaceholder?: string
  showHeader?: boolean
  autoScrollToBottom?: boolean
  title?: string
  subtitle?: string
  emptyText?: string
  currentActor?: DialogueRole
  threadStatus?: string
}

export type MessageDialogueEmits = {
  (e: 'send', content: string): void
  (e: 'update:replyText', value: string): void
  (e: 'load-more'): void
  (e: 'message-click', message: DialogueMessage): void
  (e: 'change-status', status: string): void
}
