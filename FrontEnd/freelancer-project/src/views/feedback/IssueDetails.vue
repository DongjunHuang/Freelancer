<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ThreadStatus } from '@/types/thread'
import { UserType } from '@/types/user'
import { NCard } from 'naive-ui'
import { getThread, getMessages, postMessage, updateThreadStatus } from '@/api/issue'
import { toDialogueMessage } from '@/mappers/message-mapper'
import MessageDialogue from '@/components/issues/MessageDialogue.vue'
import type { Thread } from '@/types/thread'
import type { Message } from '@/types/message'

const route = useRoute()
const selectedThread = ref<Thread | null>(null)
const messages = ref<Message[]>([])
const detailLoading = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const replyText = ref('')
const nextCursor = ref<string | null>(null)
const hasMore = ref(false)
const selectedStatus = ref<ThreadStatus>(ThreadStatus.WAITING_ADMIN)

onMounted(async () => {
  const threadId = Number(route.params.id)
  fetchThread(threadId)
  fetchMessages(threadId)
})

async function fetchThread(threadId: number) {
  try {
    const messageResp = await getThread(UserType.USER, threadId)
    selectedThread.value = messageResp.data
    console.log(messageResp)
    selectedStatus.value = messageResp.data.status
  } finally {
    detailLoading.value = false
  }
}

async function fetchMessages(threadId: number) {
  detailLoading.value = true
  try {
    const messageResp = await getMessages(UserType.USER, threadId)
    messages.value = messageResp.data.items
  } finally {
    detailLoading.value = false
  }
}

async function submitReply() {
  const content = replyText.value.trim()

  if (!content) {
    return
  }

  sending.value = true
  try {
    if (selectedThread.value) {
      await postMessage(UserType.USER, selectedThread.value.id, content)
      const [threadResp, messageResp] = await Promise.all([
        getThread(UserType.USER, selectedThread.value.id),
        getMessages(UserType.USER, selectedThread.value.id, { size: 20 }),
      ])

      selectedThread.value = threadResp.data
      messages.value = messageResp.data.items
      nextCursor.value = messageResp.data.nextCursor
      hasMore.value = messageResp.data.hasMore
    }
  } finally {
    sending.value = false
    replyText.value = ''
  }
}

const dialogueMessages = computed(() => messages.value.map(toDialogueMessage))

async function loadMoreMessages() {
  if (loadingMore.value || !hasMore.value || !selectedThread.value) return

  try {
    loadingMore.value = true

    const oldestMessage = messages.value[0]

    const resp = await getMessages(UserType.USER, selectedThread.value.id, {
      size: 20,
      cursor: oldestMessage?.createdAt,
    })

    const olderMessages = resp.data.items ?? []

    if (!olderMessages.length) {
      hasMore.value = false
      return
    }

    messages.value = [...olderMessages, ...messages.value]
    hasMore.value = resp.data.hasMore
  } catch (error) {
    console.error('Failed to load more messages:', error)
  } finally {
    loadingMore.value = false
  }
}

async function handleChangeStatus(status: string) {
  if (!selectedThread.value) {
    return
  }

  const st = status as ThreadStatus

  try {
    await updateThreadStatus(UserType.ADMIN, selectedThread.value.id, st)

    selectedThread.value.status = st
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <NCard v-if="selectedThread" class="shadow-sm">
      <MessageDialogue
        :messages="dialogueMessages"
        :loading="detailLoading"
        :loading-more="loadingMore"
        :sending="sending"
        :can-reply="selectedThread.status !== 'RESOLVED'"
        :has-more="hasMore"
        :reply-text="replyText"
        :title="selectedThread.title"
        :subtitle="`Thread #${selectedThread.id}`"
        :thread-status="selectedThread?.status"
        current-actor="user"
        @update:reply-text="replyText = $event"
        @send="submitReply"
        @load-more="loadMoreMessages"
        @change-status="handleChangeStatus"
      />
    </NCard>
  </div>
</template>
