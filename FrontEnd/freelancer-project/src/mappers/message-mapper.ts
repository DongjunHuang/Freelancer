import { UserType } from '@/types/user'

import type { Message } from '@/types/issue'
import type { DialogueMessage } from '@/components/issues/types'

export function toDialogueMessage(message: Message | undefined): DialogueMessage {
  if (!message) {
    console.error('toDialogueMessage received invalid message:', message)
    throw new Error('Invalid message passed to toDialogueMessage')
  }

  return {
    id: message.id,
    role: message.userType === UserType.USER ? 'user' : 'admin',
    content: message.body,
    createdAt: message.createdAt,
    isInternal: message.isInternal ?? false,
  }
}
