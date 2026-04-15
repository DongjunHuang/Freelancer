<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserType } from '@/types/user'
import type { NotificationCategorySummary, NotificationItem } from '@/types/notification'
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from '@/api/notification'
import { useUserNotificationStore } from '@/stores/notification-user'
const bellStore = useUserNotificationStore()
const route = useRoute()
const router = useRouter()

interface Props {
  userType?: UserType
}

const props = withDefaults(defineProps<Props>(), {
  userType: UserType.USER,
})

const loading = ref(false)
const loadingMore = ref(false)
const markingAllRead = ref(false)

const items = ref<NotificationItem[]>([])
const categories = ref<NotificationCategorySummary[]>([])
const nextCursor = ref<string | null>(null)
const totalUnreadCount = ref(0)

const selectedCategory = ref<string>('ALL')
const selectedStatus = ref<'ALL' | 'UNREAD'>('ALL')

const hasMore = computed(() => !!nextCursor.value)

const visibleCategories = computed(() => {
  return [
    {
      category: 'ALL',
      label: 'All',
      unreadCount: totalUnreadCount.value,
    },
    ...categories.value,
  ]
})

async function loadFirstPage() {
  loading.value = true
  try {
    const resp = await getNotifications(props.userType, {
      category: selectedCategory.value,
      status: selectedStatus.value,
      size: 20,
    })

    items.value = resp.data.items ?? []
    nextCursor.value = resp.data.nextCursor ?? null
    totalUnreadCount.value = resp.data.unreadCount ?? 0

    syncBellStore()
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (!nextCursor.value || loadingMore.value) return

  loadingMore.value = true
  try {
    const resp = await getNotifications(props.userType, {
      category: selectedCategory.value,
      status: selectedStatus.value,
      cursor: nextCursor.value,
      size: 20,
    })

    items.value.push(...(resp.data.items ?? []))
    nextCursor.value = resp.data.nextCursor ?? null
    totalUnreadCount.value = resp.data.unreadCount ?? totalUnreadCount.value

    syncBellStore()
  } finally {
    loadingMore.value = false
  }
}

async function handleNotificationClick(item: NotificationItem) {
  if (!item.isRead) {
    await markNotificationRead(props.userType, [item.id])

    item.isRead = true
    totalUnreadCount.value = Math.max(0, totalUnreadCount.value - 1)

    categories.value = categories.value
      .map((c) => {
        if (c.category !== item.category) return c
        return {
          ...c,
          unreadCount: Math.max(0, c.unreadCount - 1),
        }
      })
      .filter((c) => c.unreadCount > 0 || selectedStatus.value === 'ALL')

    if (selectedStatus.value === 'UNREAD') {
      items.value = items.value.filter((x) => x.id !== item.id)
    }

    syncBellStore()
  }

  if (item.actionUrl) {
    await router.push(item.actionUrl)
  }
}

async function handleMarkAllRead() {
  if (markingAllRead.value || totalUnreadCount.value <= 0) return

  markingAllRead.value = true
  try {
    await markAllNotificationsRead(props.userType)

    totalUnreadCount.value = 0
    categories.value = categories.value.map((c) => ({
      ...c,
      unreadCount: 0,
    }))

    if (selectedStatus.value === 'UNREAD') {
      items.value = []
    } else {
      items.value = items.value.map((item) => ({
        ...item,
        isRead: true,
      }))
    }

    syncBellStore()
  } finally {
    markingAllRead.value = false
  }
}

function selectCategory(category: string) {
  if (selectedCategory.value === category) return
  selectedCategory.value = category
}

function selectStatus(status: 'ALL' | 'UNREAD') {
  if (selectedStatus.value === status) return
  selectedStatus.value = status
}

function formatTime(value: string) {
  if (!value) return ''
  return new Date(value).toLocaleString()
}

function syncBellStore() {
  bellStore.setUnreadSummary(totalUnreadCount.value, categories.value)
}

watch(
  () => route.query.category,
  (category) => {
    if (typeof category === 'string' && category) {
      selectedCategory.value = category
    } else {
      selectedCategory.value = 'ALL'
    }
  },
  { immediate: true },
)

watch([selectedCategory, selectedStatus], async () => {
  nextCursor.value = null
  await loadFirstPage()
})

onMounted(async () => {
  await loadFirstPage()
})
</script>

<template>
  <div class="min-h-[calc(100vh-64px)] bg-slate-50">
    <div class="max-w-7xl mx-auto px-6 py-8">
      <div class="mb-6 flex items-center justify-between">
        <div>
          <h1 class="text-2xl font-bold text-slate-900">Notifications</h1>
          <p class="mt-1 text-sm text-slate-500">
            Review your unread updates and recent system activity.
          </p>
        </div>

        <button
          :disabled="totalUnreadCount <= 0 || markingAllRead"
          @click="handleMarkAllRead"
          class="px-4 py-2 rounded-xl text-sm font-medium border transition"
          :class="
            totalUnreadCount > 0
              ? 'border-blue-200 text-blue-700 bg-white hover:bg-blue-50'
              : 'border-slate-200 text-slate-400 bg-slate-100 cursor-not-allowed'
          "
        >
          {{ markingAllRead ? 'Marking...' : 'Mark all read' }}
        </button>
      </div>

      <div class="grid grid-cols-12 gap-6">
        <!-- Left sidebar -->
        <aside class="col-span-12 lg:col-span-3">
          <div class="rounded-2xl bg-white border border-slate-200 p-3">
            <div class="px-3 py-2 text-xs font-semibold tracking-wide text-slate-400 uppercase">
              Categories
            </div>

            <button
              v-for="item in visibleCategories"
              :key="item.category"
              @click="selectCategory(item.category)"
              class="w-full flex items-center justify-between rounded-xl px-3 py-3 text-left transition"
              :class="
                selectedCategory === item.category
                  ? 'bg-blue-50 text-blue-800'
                  : 'hover:bg-slate-50 text-slate-700'
              "
            >
              <span class="text-sm font-medium truncate">
                {{ item.label }}
              </span>

              <span
                v-if="item.unreadCount > 0"
                class="min-w-[24px] h-6 px-2 flex items-center justify-center rounded-full bg-red-500 text-white text-xs font-semibold shrink-0"
              >
                {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
              </span>
            </button>
          </div>
        </aside>

        <!-- Right content -->
        <section class="col-span-12 lg:col-span-9">
          <div class="rounded-2xl bg-white border border-slate-200 overflow-hidden">
            <div class="border-b border-slate-200 px-5 py-4 flex items-center justify-between">
              <div class="text-sm font-medium text-slate-800">
                {{
                  selectedCategory === 'ALL'
                    ? 'All notifications'
                    : visibleCategories.find((c) => c.category === selectedCategory)?.label ||
                      selectedCategory
                }}
              </div>

              <div class="flex items-center gap-2">
                <button
                  @click="selectStatus('ALL')"
                  class="px-3 py-1.5 rounded-full text-sm transition"
                  :class="
                    selectedStatus === 'ALL'
                      ? 'bg-slate-900 text-white'
                      : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                  "
                >
                  All
                </button>

                <button
                  @click="selectStatus('UNREAD')"
                  class="px-3 py-1.5 rounded-full text-sm transition"
                  :class="
                    selectedStatus === 'UNREAD'
                      ? 'bg-slate-900 text-white'
                      : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                  "
                >
                  Unread
                </button>
              </div>
            </div>

            <div v-if="loading" class="px-5 py-10 text-sm text-slate-500">
              Loading notifications...
            </div>

            <div v-else-if="items.length === 0" class="px-5 py-10 text-sm text-slate-500">
              No notifications found.
            </div>

            <div v-else>
              <button
                v-for="item in items"
                :key="item.id"
                @click="handleNotificationClick(item)"
                class="w-full text-left px-5 py-4 border-b border-slate-100 last:border-b-0 hover:bg-slate-50 transition"
                :class="!item.isRead ? 'bg-red-50/30' : ''"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-2 flex-wrap">
                      <span
                        v-if="!item.isRead"
                        class="w-2.5 h-2.5 rounded-full bg-red-500 shrink-0"
                      />

                      <div class="text-sm font-semibold text-slate-900 truncate">
                        {{ item.title }}
                      </div>

                      <span
                        v-if="item.eventCount && item.eventCount > 1"
                        class="px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 text-xs font-medium"
                      >
                        x{{ item.eventCount }}
                      </span>

                      <span
                        class="px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 text-xs font-medium"
                      >
                        {{ item.category }}
                      </span>
                    </div>

                    <div class="mt-2 text-sm text-slate-600 line-clamp-2">
                      {{ item.content }}
                    </div>

                    <div class="mt-2 text-xs text-slate-400">
                      {{ formatTime(item.lastEventAt) }}
                    </div>
                  </div>
                </div>
              </button>

              <div v-if="hasMore" class="px-5 py-4 border-t border-slate-100 bg-slate-50">
                <button
                  :disabled="loadingMore"
                  @click="loadMore"
                  class="px-4 py-2 rounded-xl text-sm font-medium border border-slate-200 bg-white hover:bg-slate-100 transition disabled:opacity-60"
                >
                  {{ loadingMore ? 'Loading...' : 'Load more' }}
                </button>
              </div>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
