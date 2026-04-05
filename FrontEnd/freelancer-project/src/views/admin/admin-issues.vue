<script setup lang="ts">
import { reactive, onMounted, onUnmounted, ref, computed } from 'vue'
import { NButton, NCard, NInput, NTag } from 'naive-ui'
import {
  getThreadStats,
  getThreads,
  getMessages,
  updateThreadStatus,
  postMessage,
  getLatestMessages,
  markThreadAsRead,
} from '@/api/issue'
import { Thread, ThreadStatus, ThreadStatsResp, MessageState } from '@/types/issue'
import { UserType } from '@/types/user'

import { toDialogueMessage } from '@/mappers/message-mapper'
import MessageDialogue from '@/components/issues/message-dialogue.vue'

onMounted(async () => {
  await Promise.all([fetchStats(), loadThreads()])
  startPolling()
})

onUnmounted(async () => {
  stopPolling()
})

// ========================================Thread Field==================================================
type ThreadListState = {
  items: Thread[]
  loading: boolean
  loadingMore: boolean
  hasMore: boolean
  nextCursor: string | null
}

const threadState = reactive<ThreadListState>({
  items: [],
  loading: false,
  loadingMore: false,
  hasMore: false,
  nextCursor: null,
})

const THREAD_PAGE_SIZE = 10
const threadSelected = ref<Thread | null>(null)
const threadCounts = ref<ThreadStatsResp | null>(null)
const threadKeyword = ref('')
const threadSelectedStatus = ref<ThreadStatus>(ThreadStatus.WAITING_ADMIN) // Default status

/**
 * Fetch first page of all threads.
 */
async function loadThreads(reset = true) {
  if (reset) {
    threadState.loading = true
    threadState.items = []
    threadState.nextCursor = null
    threadState.hasMore = false
  } else {
    threadState.loadingMore = true
  }

  try {
    const resp = await getThreads(UserType.ADMIN, {
      size: THREAD_PAGE_SIZE,
      cursor: reset ? null : threadState.nextCursor,
      status: threadSelectedStatus.value,
    })

    if (reset) {
      threadState.items = resp.data.items
    } else {
      threadState.items.push(...resp.data.items)
    }

    threadState.nextCursor = resp.data.nextCursor
    threadState.hasMore = resp.data.hasMore
  } finally {
    threadState.loading = false
    threadState.loadingMore = false
  }
}

async function loadMoreThreads() {
  if (!threadState.hasMore || threadState.loadingMore) return
  await loadThreads(false)
}

/**
 * Fetch the summary of all threads.
 */
async function fetchStats() {
  const resp = await getThreadStats(UserType.ADMIN)
  threadCounts.value = resp.data
}

// ========================================Message Field==================================================
const messageReplyText = ref('')
const messageSending = ref(false)
const messagesDialogue = computed(() => {
  return messageCurrentState.value?.items?.map(toDialogueMessage) ?? []
})

const messageCurrentState = computed(() => {
  if (!threadSelected.value) return null
  return getOrCreateMessageState(threadSelected.value.id)
})

const messageStateMap = ref<Record<number, MessageState>>({}) // <Thread ID, message[]>

function getOrCreateMessageState(threadId: number): MessageState {
  if (!messageStateMap.value[threadId]) {
    messageStateMap.value[threadId] = {
      items: [],
      nextCursor: null,
      hasMore: false,
      initialized: false,
      loading: false,
      loadingMore: false,
    }
  }

  return messageStateMap.value[threadId]
}

async function loadMessages(thread: Thread) {
  threadSelected.value = thread

  const state = getOrCreateMessageState(thread.id)

  if (state.initialized) {
    return
  }

  threadState.loading = true
  try {
    await fetchInitialMessages(thread.id)
  } finally {
    threadState.loading = false
  }
}

/**
 * Fetch initial messages for the corresponding thread id.
 *
 * @param threadId the thread id.
 */
async function fetchInitialMessages(threadId: number) {
  const state = getOrCreateMessageState(threadId)

  if (state.loading) {
    return
  }

  if (state.initialized) {
    return
  }

  state.loading = true

  try {
    const resp = await getMessages(UserType.ADMIN, threadId, {
      size: 10,
    })
    state.items = [...resp.data.items]
    state.nextCursor = resp.data.nextCursor
    state.hasMore = resp.data.hasMore
    state.initialized = true
    await markThreadAsRead(UserType.ADMIN, threadId)
    if (threadSelected.value) {
      threadSelected.value.unreadByAdmin = 0
    }
  } finally {
    state.loading = false
  }
}

/**
 * Load more messages for the next pages.
 */
async function loadMoreMessages() {
  if (!threadSelected.value) return

  const threadId = threadSelected.value.id
  const state = getOrCreateMessageState(threadId)

  if (state.loadingMore || !state.hasMore) {
    return
  }

  try {
    state.loadingMore = true

    const resp = await getMessages(UserType.USER, threadId, {
      size: 10,
      cursor: state.nextCursor ?? undefined,
    })

    const olderMessages = [...(resp.data.items ?? [])]

    if (!olderMessages.length) {
      state.hasMore = false
      return
    }

    state.items = [...olderMessages, ...state.items]
    state.nextCursor = resp.data.nextCursor
    state.hasMore = resp.data.hasMore
  } catch (error) {
    console.error('Failed to load more messages:', error)
  } finally {
    state.loadingMore = false
  }
}

/**
 * Send message to the user.
 */
async function submitReply() {
  const content = messageReplyText.value.trim()

  if (!content || !threadSelected.value) {
    return
  }

  const threadId = threadSelected.value.id
  const state = getOrCreateMessageState(threadId)

  messageSending.value = true
  try {
    const resp = await postMessage(UserType.ADMIN, threadSelected.value.id, content)
    console.log('resp', resp.data)

    const updatedThread = resp.data.thread
    const newMessage = resp.data.message
    state.items = [...state.items, newMessage]

    threadSelected.value = updatedThread
    threadState.items = threadState.items.map((thread) =>
      thread.id === updatedThread.id ? updatedThread : thread,
    )
  } finally {
    messageSending.value = false
    messageReplyText.value = ''
  }
}

// ========================================Timer==================================================
const pollingTimer = ref<number | null>(null)
async function pollLatestMessages() {
  if (!threadSelected.value) {
    return
  }

  const state = getOrCreateMessageState(threadSelected.value.id)

  if (state.items.length === 0) {
    return
  }

  const latestMessage = state.items[state.items.length - 1]

  const resp = await getLatestMessages(
    UserType.ADMIN,
    threadSelected.value.id,
    latestMessage.createdAt,
  )

  if (resp.data.items.length > 0) {
    state.items = [...state.items, ...resp.data.items]
  }
}

function startPolling() {
  stopPolling()

  pollingTimer.value = window.setInterval(async () => {
    await pollLatestMessages()
  }, 5000)
}

function stopPolling() {
  if (pollingTimer.value) {
    clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}
// ----------------------------------------Timer----------------------------------------

function formatDate(value: string) {
  return new Date(value).toLocaleString()
}

async function handleChangeStatus(status: string) {
  if (!threadSelected.value) {
    return
  }
  const st = status as ThreadStatus

  try {
    await updateThreadStatus(UserType.ADMIN, threadSelected.value.id, st)

    threadSelected.value.status = st
    await loadThreads()
  } catch (e) {
    console.error(e)
  }
}

async function changeStatus(status: ThreadStatus) {
  threadSelectedStatus.value = status
  loadThreads()
}

function formatIssueType(issueType?: string) {
  switch (issueType) {
    case 'BUG':
      return 'Bug'
    case 'SUGGESTION':
      return 'Suggestion'
    case 'QUESTION':
      return 'Question'
    default:
      return issueType || 'Unknown'
  }
}
</script>
<template>
  <div class="min-h-screen bg-gray-50">
    <main class="w-full px-6 py-6">
      <!-- Issue List -->
      <NCard class="mt-6 rounded-2xl shadow-sm">
        <div v-if="threadState.loading" class="py-10 text-center text-gray-500">Loading...</div>

        <div v-else-if="threadCounts?.all === 0" class="py-10 text-center text-gray-500">
          No threads found.
        </div>

        <div class="h-[calc(100vh-120px)] flex gap-6">
          <!-- Left: thread list -->
          <div class="w-[420px] shrink-0 flex flex-col">
            <!-- Stats -->
            <div class="grid grid-cols-2 gap-3">
              <NCard size="small" class="rounded-2xl">
                <div class="text-xs text-gray-500">All</div>
                <div class="mt-1 text-2xl font-semibold">{{ threadCounts?.all }}</div>
              </NCard>

              <NCard size="small" class="rounded-2xl">
                <div class="text-xs text-gray-500">Waiting Admin</div>
                <div class="mt-1 text-2xl font-semibold">{{ threadCounts?.waitingAdmin }}</div>
              </NCard>

              <NCard size="small" class="rounded-2xl">
                <div class="text-xs text-gray-500">Waiting User</div>
                <div class="mt-1 text-2xl font-semibold">{{ threadCounts?.waitingUser }}</div>
              </NCard>

              <NCard size="small" class="rounded-2xl">
                <div class="text-xs text-gray-500">Resolved</div>
                <div class="mt-1 text-2xl font-semibold">{{ threadCounts?.resolved }}</div>
              </NCard>
            </div>

            <!-- Filters -->
            <div class="mt-4 flex flex-wrap gap-2">
              <NButton
                size="small"
                :type="threadSelectedStatus === ThreadStatus.WAITING_ADMIN ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.WAITING_ADMIN)"
              >
                Waiting Admin
              </NButton>
              <NButton
                size="small"
                :type="threadSelectedStatus === ThreadStatus.WAITING_USER ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.WAITING_USER)"
              >
                Waiting User
              </NButton>
              <NButton
                size="small"
                :type="threadSelectedStatus === ThreadStatus.RESOLVED ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.RESOLVED)"
              >
                Resolved
              </NButton>
            </div>

            <!-- Search -->
            <div class="mt-4">
              <NInput v-model:value="threadKeyword" clearable placeholder="Search threads..." />
            </div>

            <!-- List -->
            <div class="mt-4 flex-1 min-h-0 flex flex-col">
              <div class="flex-1 overflow-y-auto pr-1 space-y-3">
                <div
                  v-for="item in threadState.items"
                  :key="item.id"
                  class="rounded-2xl border p-4 cursor-pointer transition"
                  :class="
                    threadSelected?.id === item.id
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 bg-white hover:border-gray-300'
                  "
                  @click="loadMessages(item)"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0 flex-1">
                      <div class="flex items-center gap-2 flex-wrap">
                        <div class="text-sm font-semibold text-gray-900 truncate">
                          {{ item.title }}
                        </div>

                        <span
                          class="rounded-full bg-gray-100 text-gray-700 text-[11px] px-2 py-0.5 font-medium shrink-0"
                        >
                          {{ formatIssueType(item.type) }}
                        </span>
                      </div>

                      <div class="mt-2 text-xs text-gray-500 flex flex-wrap gap-x-3 gap-y-1">
                        <span>Username: {{ item.username }}</span>
                        <span>{{ formatDate(item.lastMessageAt) }}</span>
                      </div>
                    </div>

                    <div
                      v-if="item.unreadByAdmin > 0"
                      class="h-2.5 w-2.5 rounded-full bg-red-500 shrink-0"
                    />
                  </div>
                </div>

                <div v-if="threadState.loading" class="py-6 text-center text-sm text-gray-500">
                  Loading threads...
                </div>

                <div
                  v-else-if="threadState.loadingMore"
                  class="py-4 text-center text-sm text-gray-500"
                >
                  Loading more...
                </div>

                <div
                  v-else-if="!threadState.loading && threadState.items.length === 0"
                  class="py-10 text-center text-sm text-gray-500"
                >
                  No threads found.
                </div>
              </div>

              <div class="pt-3">
                <NButton
                  v-if="threadState.hasMore"
                  block
                  secondary
                  :loading="threadState.loadingMore"
                  @click="loadMoreThreads"
                >
                  Load more
                </NButton>
              </div>
            </div>
          </div>
          <MessageDialogue
            :messages="messagesDialogue"
            :loading="messageCurrentState?.loading ?? false"
            :loading-more="messageCurrentState?.loadingMore ?? false"
            :sending="messageSending"
            :can-reply="threadSelected?.status !== 'RESOLVED'"
            :has-more="messageCurrentState?.hasMore ?? false"
            :reply-text="messageReplyText"
            :title="threadSelected?.title || 'Conversation'"
            :subtitle="threadSelected ? `Thread #${threadSelected.id}` : ''"
            :thread-status="threadSelected?.status"
            current-actor="admin"
            @update:reply-text="messageReplyText = $event"
            @send="submitReply"
            @load-more="loadMoreMessages"
            @change-status="handleChangeStatus"
          />
        </div>
      </NCard>
    </main>
  </div>
</template>
