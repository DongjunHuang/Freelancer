<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { UserType } from '@/types/user'
import { NCard } from 'naive-ui'
import {
  getThread,
  getMessages,
  postMessage,
  updateThreadStatus,
  getLatestMessages,
} from '@/api/issue'

import { toDialogueMessage } from '@/mappers/message-mapper'
import MessageDialogue from '@/components/issues/message-dialogue.vue'
import { Thread, Message, ThreadStatus } from '@/types/issue'

const route = useRoute()

onMounted(async () => {
  const threadId = Number(route.params.id)
  await Promise.all([fetchThread(threadId), fetchInitialMessages(threadId)])
  startPolling()
})

onUnmounted(() => {
  stopPolling()
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

// ========================================Timer==================================================
const pollingTimer = ref<number | null>(null)
async function pollLatestMessages() {
  if (!selectedThread.value || messages.value.length === 0) {
    return
  }

  const latestMessage = messages.value[messages.value.length - 1]

  try {
    const resp = await getLatestMessages(
      UserType.USER,
      selectedThread.value.id,
      latestMessage.createdAt,
    )

    const newMessages = resp.data.items ?? []

    if (newMessages.length > 0) {
      messages.value = [...messages.value, ...newMessages]
    }
  } catch (error) {
    console.error('Failed to poll latest messages:', error)
  }
}

function startPolling() {
  stopPolling()

  pollingTimer.value = window.setInterval(() => {
    void pollLatestMessages()
  }, 5000)
}

function stopPolling() {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}
// ----------------------------------------Timer----------------------------------------

// ========================================Thread Field==================================================
const selectedThread = ref<Thread | null>(null)
const selectedStatus = ref<ThreadStatus>(ThreadStatus.WAITING_ADMIN)
const detailLoading = ref(false)

// ========================================Message Field==================================================
const dialogueMessages = computed(() => messages.value.map(toDialogueMessage))
const messages = ref<Message[]>([])
const messageNextCursor = ref<string | null>(null)
const messageHasMore = ref(false)
const messageLoading = ref(false)
const messageLoadingMore = ref(false)
const sending = ref(false)
const replyText = ref('')

// Fetch the initial set of messages
async function fetchInitialMessages(threadId: number) {
  if (messageLoading.value) return

  messageLoading.value = true
  try {
    const resp = await getMessages(UserType.USER, threadId, {
      size: 10,
    })

    messages.value = [...(resp.data.items ?? [])]
    messageNextCursor.value = resp.data.nextCursor
    messageHasMore.value = resp.data.hasMore
  } catch (error) {
    console.error('Failed to fetch initial messages:', error)
    messages.value = []
    messageNextCursor.value = null
    messageHasMore.value = false
  } finally {
    messageLoading.value = false
  }
}

async function loadMoreMessages() {
  if (
    messageLoadingMore.value ||
    !messageHasMore.value ||
    !selectedThread.value ||
    !messageNextCursor.value
  ) {
    return
  }

  try {
    messageLoadingMore.value = true

    const resp = await getMessages(UserType.USER, selectedThread.value.id, {
      size: 10,
      cursor: messageNextCursor.value,
    })

    const olderMessages = [...(resp.data.items ?? [])]

    if (!olderMessages.length) {
      messageHasMore.value = false
      return
    }

    messages.value = [...olderMessages, ...messages.value]
    messageNextCursor.value = resp.data.nextCursor
    messageHasMore.value = resp.data.hasMore
  } catch (error) {
    console.error('Failed to load more messages:', error)
  } finally {
    messageLoadingMore.value = false
  }
}

async function submitReply() {
  const content = replyText.value.trim()

  if (!content || !selectedThread.value) {
    return
  }

  sending.value = true
  try {
    const resp = await postMessage(UserType.USER, selectedThread.value.id, content)

    messages.value = [...messages.value, resp.data.message]
    selectedThread.value = resp.data.thread
  } catch (error) {
    console.error('Failed to submit reply:', error)
  } finally {
    sending.value = false
    replyText.value = ''
  }
}

// ==========================================================================================
async function handleChangeStatus(status: string) {
  if (!selectedThread.value) {
    return
  }

  const st = status as ThreadStatus

  try {
    await updateThreadStatus(UserType.USER, selectedThread.value.id, st)

    selectedThread.value.status = st
  } catch (e) {
    console.error(e)
  }
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-6 py-8">
    <NCard v-if="selectedThread" class="h-[1200px] shadow-sm" content-class="h-full p-0">
      <MessageDialogue
        :messages="dialogueMessages"
        :loading="detailLoading"
        :loading-more="messageLoadingMore"
        :sending="sending"
        :can-reply="selectedThread.status !== 'RESOLVED'"
        :has-more="messageHasMore"
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
