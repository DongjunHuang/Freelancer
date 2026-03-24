<script setup lang="ts">
import { reactive, onMounted, ref, computed } from 'vue'
import { NButton, NCard, NInput, NTag, useMessage } from 'naive-ui'
import {
  getThreadStats,
  getThreads,
  getMessages,
  updateThreadStatus,
  postMessage,
  getThread,
} from '@/api/issue'

import { Thread, ThreadStatus, ThreadStatsResp } from '@/types/thread'
import { Message } from '@/types/message'
import { UserType } from '@/types/user'

import { toDialogueMessage } from '@/mappers/message-mapper'
import MessageDialogue from '@/components/issues/MessageDialogue.vue'

const selectedStatus = ref<ThreadStatus>(ThreadStatus.WAITING_ADMIN) // Default status
const nextCursor = ref<string | null>(null)
const hasMore = ref(false)

const keyword = ref('')
const threadCounts = ref<ThreadStatsResp | null>(null)
const threads = ref<Thread[]>([])

const selectedThread = ref<Thread | null>(null)
const messages = ref<Message[]>([])
const detailLoading = ref(false)
const loading = ref(false)
const loadingMore = ref(false)
const sending = ref(false)
const replyText = ref('')

onMounted(async () => {
  await Promise.all([fetchStats(), fetchThreads()])
})

async function fetchStats() {
  const resp = await getThreadStats(UserType.ADMIN)
  threadCounts.value = resp.data
}

async function fetchThreads() {
  const resp = await getThreads(UserType.ADMIN, {
    status: selectedStatus.value,
    size: 20,
    cursor: null,
  })

  threads.value = resp.data.items
  nextCursor.value = resp.data.nextCursor
  hasMore.value = resp.data.hasMore
}

async function loadMessages(thread: Thread) {
  selectedThread.value = thread
  detailLoading.value = true
  try {
    const messageResp = await getMessages(UserType.ADMIN, thread.id)
    messages.value = messageResp.data.items
  } finally {
    detailLoading.value = false
  }
}

function formatDate(value: string) {
  return new Date(value).toLocaleString()
}

function statusTagType(status: ThreadStatus) {
  switch (status) {
    case ThreadStatus.WAITING_ADMIN:
      return 'error'
    case ThreadStatus.WAITING_USER:
      return 'warning'
    case ThreadStatus.RESOLVED:
      return 'success'
    default:
      return 'default'
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
    await fetchThreads()
  } catch (e) {
    console.error(e)
  }
}

async function changeStatus(status: ThreadStatus) {
  selectedStatus.value = status
  fetchThreads()
}

async function submitReply() {
  const content = replyText.value.trim()

  if (!content) {
    return
  }

  sending.value = true
  try {
    if (selectedThread.value) {
      await postMessage(UserType.ADMIN, selectedThread.value.id, content)
      const [threadResp, messageResp] = await Promise.all([
        getThread(UserType.ADMIN, selectedThread.value.id),
        getMessages(UserType.ADMIN, selectedThread.value.id, { size: 20 }),
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

async function loadMoreMessages() {
  if (loadingMore.value || !hasMore.value || !selectedThread.value) return

  try {
    loadingMore.value = true

    const oldestMessage = messages.value[0]

    const resp = await getMessages(UserType.ADMIN, selectedThread.value.id, {
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
const dialogueMessages = computed(() => messages.value.map(toDialogueMessage))
</script>
<template>
  <div class="min-h-screen bg-gray-50">
    <main class="w-full px-6 py-6">
      <!-- Issue List -->
      <NCard class="mt-6 rounded-2xl shadow-sm">
        <div v-if="loading" class="py-10 text-center text-gray-500">Loading...</div>

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
                :type="selectedStatus === ThreadStatus.WAITING_ADMIN ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.WAITING_ADMIN)"
              >
                Waiting Admin
              </NButton>
              <NButton
                size="small"
                :type="selectedStatus === ThreadStatus.WAITING_USER ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.WAITING_USER)"
              >
                Waiting User
              </NButton>
              <NButton
                size="small"
                :type="selectedStatus === ThreadStatus.RESOLVED ? 'primary' : 'default'"
                @click="changeStatus(ThreadStatus.RESOLVED)"
              >
                Resolved
              </NButton>
            </div>

            <!-- Search -->
            <div class="mt-4">
              <NInput v-model:value="keyword" clearable placeholder="Search threads..." />
            </div>

            <!-- List -->
            <div class="mt-4 flex-1 overflow-y-auto space-y-3 pr-1">
              <div
                v-for="item in threads"
                :key="item.id"
                class="rounded-2xl border p-4 cursor-pointer transition"
                :class="
                  selectedThread?.id === item.id
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

                      <NTag size="small" :type="statusTagType(item.status)">
                        {{ item.status }}
                      </NTag>
                    </div>

                    <div class="mt-2 text-xs text-gray-500 flex flex-wrap gap-x-3 gap-y-1">
                      <span>#{{ item.id }}</span>
                      <span>User #{{ item.userId }}</span>
                      <span>{{ formatDate(item.lastMessageAt) }}</span>
                    </div>
                  </div>

                  <div
                    v-if="item.unreadByAdmin > 0"
                    class="rounded-full bg-red-50 text-red-600 text-xs px-2 py-0.5 font-medium shrink-0"
                  >
                    {{ item.unreadByAdmin }}
                  </div>
                </div>
              </div>

              <div v-if="threads.length === 0" class="text-sm text-gray-500 py-8 text-center">
                No threads found.
              </div>
            </div>
          </div>
          <MessageDialogue
            :messages="dialogueMessages"
            :loading="detailLoading"
            :loading-more="loadingMore"
            :sending="sending"
            :can-reply="selectedThread?.status !== 'RESOLVED'"
            :has-more="hasMore"
            :reply-text="replyText"
            :title="selectedThread?.title || 'Conversation'"
            :subtitle="selectedThread ? `Thread #${selectedThread.id}` : ''"
            :thread-status="selectedThread?.status"
            current-actor="admin"
            @update:reply-text="replyText = $event"
            @send="submitReply"
            @load-more="loadMoreMessages"
            @change-status="handleChangeStatus"
          />
        </div>
      </NCard>
    </main>
  </div>
</template>
