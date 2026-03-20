<script setup lang="ts">
import { reactive, onMounted, ref } from 'vue'
import { NButton, NCard, NInput, NTag, useMessage } from 'naive-ui'
import {
  getAdminThreadStats,
  getAdminThreads,
  getAdminMessages,
  postAdminMessage,
  getAdminThread,
} from '@/api/admin'
import { AdminThread, ThreadStatus, AdminThreadStatsResp } from '@/types/thread'
import { Message, UserType } from '@/types/message'

const keyword = ref('')
const selectedStatus = ref<ThreadStatus>(ThreadStatus.WAITING_ADMIN)
const nextCursor = ref<string | null>(null)
const hasMore = ref(false)
const threadCounts = ref<AdminThreadStatsResp | null>(null)
const threads = ref<AdminThread[]>([])

const messages = ref<Message[]>([])
const message = useMessage()

const selectedThread = ref<AdminThread | null>(null)
const reply = reactive({
  content: '',
})

const detailLoading = ref(false)
const sending = ref(false)
const updatingStatus = ref(false)
const loading = ref(false)

const statusOptions = [
  {
    label: 'Waiting Admin',
    value: ThreadStatus.WAITING_ADMIN,
  },
  {
    label: 'Waiting User',
    value: ThreadStatus.WAITING_USER,
  },
  {
    label: 'Resolved',
    value: ThreadStatus.RESOLVED,
  },
]

function handleStatusChange(value: ThreadStatus) {
  if (!value || !selectedThread.value) return
  if (value === selectedThread.value.status) return

  changeStatus(value)
}

onMounted(async () => {
  await Promise.all([fetchStats(), fetchThread()])
})

async function fetchStats() {
  const threadC = await getAdminThreadStats()
  threadCounts.value = threadC.data
}

async function fetchThread() {
  const resp = await getAdminThreads({
    status: selectedStatus.value,
    size: 20,
    cursor: null,
  })

  threads.value = resp.data.items
  nextCursor.value = resp.data.nextCursor
  hasMore.value = resp.data.hasMore
}

async function fetchThreadDetails(thread: AdminThread) {
  selectedThread.value = thread
  detailLoading.value = true
  try {
    const messageResp = await getAdminMessages(thread.id)
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

async function submitReply() {
  const content = reply.content.trim()

  if (!content) {
    message.warning('Please enter your reply.')
    return
  }

  sending.value = true
  try {
    if (selectedThread.value) {
      await postAdminMessage(selectedThread.value.id, content)
      const [threadResp, messageResp] = await Promise.all([
        getAdminThread(selectedThread.value.id),
        getAdminMessages(selectedThread.value.id, { size: 20 }),
      ])

      selectedThread.value = threadResp.data
      messages.value = messageResp.data.items
      nextCursor.value = messageResp.data.nextCursor
      hasMore.value = messageResp.data.hasMore
    }
  } finally {
    sending.value = false
    reply.content = ''
  }
}

async function changeStatus(status: ThreadStatus) {
  selectedStatus.value = status
  fetchThread()
}
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
                @click="fetchThreadDetails(item)"
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

          <!-- Right: detail panel -->
          <div class="flex-1 min-w-0">
            <NCard class="h-full rounded-2xl">
              <template v-if="detailLoading">
                <div class="h-full flex items-center justify-center text-gray-500">
                  Loading conversation...
                </div>
              </template>

              <template v-else-if="!selectedThread">
                <div class="h-full flex items-center justify-center text-gray-500">
                  Select a thread to view the conversation.
                </div>
              </template>

              <template v-else>
                <div class="h-full flex flex-col">
                  <!-- Header -->
                  <div class="pb-4 border-b">
                    <div class="flex items-start justify-between gap-4 flex-wrap">
                      <div class="min-w-0">
                        <div class="flex items-center gap-2 flex-wrap">
                          <div class="text-xl font-semibold text-gray-900 truncate">
                            {{ selectedThread.title }}
                          </div>

                          <NTag size="small" :type="statusTagType(selectedThread.status)">
                            {{ selectedThread.status }}
                          </NTag>

                          <NTag size="small" type="info">
                            {{ selectedThread.type }}
                          </NTag>
                        </div>

                        <div class="mt-2 text-sm text-gray-500 flex flex-wrap gap-x-4 gap-y-1">
                          <span>Thread #{{ selectedThread.id }}</span>
                          <span>User #{{ selectedThread.userId }}</span>
                          <span>Created {{ formatDate(selectedThread.createdAt) }}</span>
                          <span>Updated {{ formatDate(selectedThread.lastMessageAt) }}</span>
                        </div>
                      </div>

                      <div class="flex items-center gap-2">
                        <div class="flex items-center gap-2">
                          <span class="text-sm text-gray-500">Status</span>

                          <NSelect
                            style="width: 180px"
                            size="small"
                            :value="selectedThread.status"
                            :options="statusOptions"
                            :disabled="updatingStatus"
                            @update:value="handleStatusChange"
                          />
                        </div>

                        <NButton
                          size="small"
                          secondary
                          :disabled="
                            updatingStatus || selectedThread.status !== ThreadStatus.RESOLVED
                          "
                          @click="changeStatus(ThreadStatus.WAITING_USER)"
                        >
                          Reopen
                        </NButton>
                      </div>
                    </div>
                  </div>

                  <!-- Messages -->
                  <div class="flex-1 overflow-y-auto py-4 space-y-4">
                    <div
                      v-for="m in messages"
                      :key="m.id"
                      class="flex"
                      :class="m.userType === UserType.USER ? 'justify-start' : 'justify-end'"
                    >
                      <div
                        class="max-w-[75%] rounded-2xl px-4 py-3 border"
                        :class="
                          m.userType === UserType.USER
                            ? 'bg-white border-gray-200'
                            : 'bg-blue-600/10 border-blue-600/20'
                        "
                      >
                        <div class="flex items-center gap-2 mb-1">
                          <span
                            class="text-xs font-medium"
                            :class="
                              m.userType === UserType.USER ? 'text-gray-700' : 'text-blue-700'
                            "
                          >
                            {{ m.userType === UserType.USER ? 'User' : 'Admin' }}
                          </span>
                          <span class="text-xs text-gray-400">
                            {{ formatDate(m.createdAt) }}
                          </span>
                        </div>

                        <div class="whitespace-pre-wrap text-sm text-gray-900">
                          {{ m.body }}
                        </div>
                      </div>
                    </div>

                    <div
                      v-if="messages.length === 0"
                      class="text-sm text-gray-500 text-center py-8"
                    >
                      No messages yet.
                    </div>
                  </div>

                  <!-- Reply -->
                  <div class="pt-4 border-t space-y-3">
                    <div class="flex items-center justify-between">
                      <div class="font-medium">Reply</div>
                      <div
                        v-if="selectedThread.status === 'RESOLVED'"
                        class="text-sm text-gray-500"
                      >
                        This thread is resolved. Reopen it to continue.
                      </div>
                    </div>

                    <NInput
                      v-model:value="reply.content"
                      type="textarea"
                      :autosize="{ minRows: 4, maxRows: 8 }"
                      placeholder="Write your reply..."
                      :disabled="selectedThread.status === 'RESOLVED' || sending"
                    />

                    <div class="flex justify-end">
                      <NButton
                        type="primary"
                        :loading="sending"
                        :disabled="selectedThread.status === 'RESOLVED' || !reply.content.trim()"
                        @click="submitReply"
                      >
                        Send Reply
                      </NButton>
                    </div>
                  </div>
                </div>
              </template>
            </NCard>
          </div>
        </div>
      </NCard>
    </main>
  </div>
</template>
