import { UserType } from '@/types/user'

import type { Message } from '@/types/message'
import type { DialogueMessage } from '@/components/issues/types'

export function toDialogueMessage(m: Message): DialogueMessage {
  return {
    id: m.id,
    role: m.userType === UserType.USER ? 'user' : 'admin',
    content: m.body,
    createdAt: m.createdAt,
    isInternal: m.isInternal,
  }
}
