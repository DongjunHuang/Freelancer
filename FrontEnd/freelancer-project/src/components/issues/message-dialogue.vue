<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { NButton, NCard, NInput, NTag, NSelect } from 'naive-ui'

import type {
  MessageDialogueProps,
  MessageDialogueEmits,
  DialogueMessage,
  DialogueRole,
} from '@/components/issues/types'

const props = withDefaults(defineProps<MessageDialogueProps>(), {
  loading: false,
  loadingMore: false,
  sending: false,
  canReply: true,
  hasMore: false,
  replyText: '',
  replyPlaceholder: 'Write a reply...',
  showHeader: true,
  autoScrollToBottom: true,
  title: 'Conversation',
  subtitle: '',
  emptyText: 'No messages yet.',
  currentActor: 'admin',
})

const emit = defineEmits<MessageDialogueEmits>()

const canSend = computed(() => {
  return props.canReply && !!props.replyText.trim() && !props.sending
})

function handleSend() {
  const content = props.replyText.trim()
  if (!content || !props.canReply || props.sending) return

  emit('send', content)
}

function handleReplyInput(value: string) {
  emit('update:replyText', value)
}

function handleLoadMore() {
  if (props.loadingMore || !props.hasMore) return
  emit('load-more')
}

function handleMessageClick(message: DialogueMessage) {
  emit('message-click', message)
}

function formatDate(value: string) {
  if (!value) return ''
  return new Date(value).toLocaleString()
}

function isOwnMessage(message: DialogueMessage) {
  return message.role === props.currentActor
}

function roleLabel(role: DialogueRole) {
  switch (role) {
    case 'user':
      return 'User'
    case 'admin':
      return 'Admin'
    case 'system':
      return 'System'
    default:
      return role
  }
}

function statusTagType(status?: string) {
  switch (status) {
    case 'WAITING_ADMIN':
      return 'warning'
    case 'WAITING_USER':
      return 'info'
    case 'RESOLVED':
      return 'success'
    default:
      return 'default'
  }
}

function handleStatusChange(value: string) {
  if (!value || value === props.threadStatus) return

  emit('change-status', value)
}

const statusOptions = [
  { label: 'Waiting Admin', value: 'WAITING_ADMIN' },
  { label: 'Waiting User', value: 'WAITING_USER' },
  { label: 'Resolved', value: 'RESOLVED' },
]

const messageContainer = ref<HTMLElement | null>(null)
const isNearBottom = (el: HTMLElement) => {
  return el.scrollHeight - el.scrollTop - el.clientHeight < 50
}

watch(
  () => props.messages,
  async () => {
    await nextTick()

    const el = messageContainer.value
    if (el && isNearBottom(el)) {
      el.scrollTop = el.scrollHeight
    }
  },
)
</script>
<template>
  <div class="flex min-h-0 flex-1 min-w-0">
    <NCard class="h-full w-full rounded-2xl" content-class="h-full p-0">
      <div class="flex h-full min-h-0 flex-col">
        <!-- Header -->
        <div
          v-if="props.showHeader"
          class="shrink-0 border-b px-6 py-4 flex items-start justify-between gap-4"
        >
          <div class="min-w-0">
            <div class="truncate text-xl font-semibold text-gray-900">
              {{ props.title }}
            </div>

            <div v-if="props.subtitle" class="mt-2 text-sm text-gray-500">
              {{ props.subtitle }}
            </div>
          </div>

          <div class="flex items-center gap-2 shrink-0">
            <NTag size="small" :type="statusTagType(props.threadStatus)">
              {{ props.threadStatus }}
            </NTag>

            <NSelect
              size="small"
              style="width: 160px"
              :value="props.threadStatus"
              :options="statusOptions"
              @update:value="handleStatusChange"
            />
          </div>
        </div>

        <!-- Loading -->
        <div
          v-if="props.loading"
          class="flex min-h-0 flex-1 items-center justify-center px-6 py-4 text-gray-500"
        >
          Loading conversation...
        </div>

        <!-- Content -->
        <div v-else class="flex min-h-0 flex-1 flex-col px-6 py-4">
          <!-- Messages -->
          <div ref="messageContainer" class="min-h-0 flex-1 overflow-y-auto">
            <div class="space-y-4 pr-1">
              <div class="flex justify-center">
                <NButton
                  v-if="props.hasMore"
                  size="small"
                  tertiary
                  :loading="props.loadingMore"
                  @click="handleLoadMore"
                >
                  Load older messages
                </NButton>
              </div>

              <div
                v-for="message in props.messages"
                :key="message.id"
                class="flex"
                :class="isOwnMessage(message) ? 'justify-end' : 'justify-start'"
              >
                <div
                  class="max-w-[75%] rounded-2xl border px-4 py-3 transition"
                  :class="
                    isOwnMessage(message)
                      ? 'border-blue-600/20 bg-blue-600/10 text-gray-900'
                      : 'border-gray-200 bg-white text-gray-900'
                  "
                  @click="handleMessageClick(message)"
                >
                  <div class="mb-1 flex items-center gap-2">
                    <span
                      class="text-xs font-medium"
                      :class="
                        message.role === 'user'
                          ? 'text-gray-700'
                          : message.role === 'admin'
                            ? 'text-blue-700'
                            : 'text-gray-500'
                      "
                    >
                      {{ roleLabel(message.role) }}
                    </span>

                    <span class="text-xs text-gray-400">
                      {{ formatDate(message.createdAt) }}
                    </span>

                    <NTag v-if="message.isInternal" size="small" type="warning"> Internal </NTag>
                  </div>

                  <div class="whitespace-pre-wrap break-words text-sm text-gray-900">
                    {{ message.content }}
                  </div>
                </div>
              </div>

              <div
                v-if="props.messages.length === 0"
                class="py-8 text-center text-sm text-gray-500"
              >
                {{ props.emptyText }}
              </div>
            </div>
          </div>

          <!-- Reply -->
          <div v-if="props.canReply" class="mt-4 shrink-0 border-t pt-4 space-y-3">
            <div class="font-medium">Reply</div>

            <NInput
              :value="props.replyText"
              type="textarea"
              :autosize="{ minRows: 4, maxRows: 8 }"
              :placeholder="props.replyPlaceholder"
              :disabled="props.sending"
              @update:value="handleReplyInput"
            />

            <div class="flex justify-end">
              <NButton
                type="primary"
                :loading="props.sending"
                :disabled="!canSend"
                @click="handleSend"
              >
                Send Reply
              </NButton>
            </div>
          </div>
        </div>
      </div>
    </NCard>
  </div>
</template>
