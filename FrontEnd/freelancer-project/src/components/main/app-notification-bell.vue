<script setup lang="ts">
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { NotificationItem, NotificationStoreLike } from '@/types/notification'
import { UserType } from '@/types/user'
import {
  getNotificationUnreadCount,
  markAllNotificationsRead,
  getNotificationUnreadSummary,
} from '@/api/notification'

interface Props {
  userType: UserType
  notificationStore: NotificationStoreLike
  summarySize?: number
}

const router = useRouter()
const rootRef = ref<HTMLElement | null>(null)
const props = defineProps<Props>()

async function loadUnreadCount(force = false) {
  const now = Date.now()
  const last = props.notificationStore.lastUnreadFetchedAt

  if (!force && last && now - last < 30_000) {
    return
  }

  const resp = await getNotificationUnreadCount(props.userType)
  props.notificationStore.setUnreadCount(resp.data.unreadCount ?? 0)
  console.log('The count is ' + resp.data.unreadCount)
}

async function toggleNotificationPanel() {
  props.notificationStore.toggleBell()

  if (props.notificationStore.bellOpen) {
    await loadUnreadSummary()
  }
}

async function loadUnreadSummary(force = false) {
  if (props.notificationStore.summaryLoading) return
  if (props.notificationStore.categorySummaries.length > 0 && !force) return

  props.notificationStore.setSummaryLoading(true)
  try {
    const resp = await getNotificationUnreadSummary(props.userType)
    props.notificationStore.setUnreadSummary(resp.data.unreadCount ?? 0, resp.data.categories ?? [])
  } finally {
    props.notificationStore.setSummaryLoading(false)
  }
}

async function markAllRead() {
  await markAllNotificationsRead(props.userType)
  props.notificationStore.markAllReadLocally()
}

function handleClickOutside(event: MouseEvent) {
  const target = event.target as Node
  if (rootRef.value && !rootRef.value.contains(target)) {
    props.notificationStore.closeBell()
  }
}

onMounted(async () => {
  await loadUnreadCount()
  document.addEventListener('click', handleClickOutside)
})

async function handleCategoryClick(category: string) {
  props.notificationStore.closeBell()
  await router.push({
    path: '/notification',
  })
}

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div ref="rootRef" class="relative">
    <button
      @click="toggleNotificationPanel"
      class="relative flex items-center justify-center w-10 h-10 rounded-full hover:bg-gray-100 transition"
      aria-label="Notifications"
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        class="w-6 h-6 text-gray-700"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        stroke-width="1.8"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          d="M14.857 17H19l-1.405-1.405A2.032 2.032 0 0117 14.158V11a5 5 0 10-10 0v3.159c0 .538-.214 1.055-.595 1.436L5 17h4.143m5.714 0a3 3 0 11-5.714 0m5.714 0H9.143"
        />
      </svg>

      <span
        v-if="props.notificationStore.unreadCount > 0"
        class="absolute -top-1 -right-1 min-w-[18px] h-[18px] px-1 flex items-center justify-center rounded-full bg-red-500 text-white text-[10px] font-semibold leading-none"
      >
        {{ props.notificationStore.unreadCount > 99 ? '99+' : props.notificationStore.unreadCount }}
      </span>
    </button>

    <div
      v-if="props.notificationStore.bellOpen"
      class="absolute right-0 mt-2 w-96 bg-white border border-gray-200 rounded-2xl shadow-lg z-50 overflow-hidden"
    >
      <div class="px-4 py-3 border-b flex items-center justify-between">
        <div class="font-semibold text-gray-800">Notifications</div>

        <button
          v-if="props.notificationStore.unreadCount > 0"
          @click="markAllRead"
          class="text-sm text-blue-700 hover:text-blue-900"
        >
          Mark all read
        </button>
      </div>

      <div v-if="props.notificationStore.summaryLoading" class="px-4 py-6 text-sm text-gray-500">
        Loading...
      </div>

      <div
        v-else-if="props.notificationStore.unreadCount === 0"
        class="px-4 py-6 text-sm text-gray-500"
      >
        No unread notifications
      </div>

      <div v-else class="divide-y divide-gray-100">
        <button
          v-for="item in props.notificationStore.categorySummaries"
          :key="item.category"
          @click="handleCategoryClick(item.category)"
          class="w-full flex items-center justify-between px-4 py-3 text-left hover:bg-gray-50 transition"
        >
          <div class="flex items-center gap-3 min-w-0">
            <span class="text-sm font-medium text-gray-900 truncate">
              {{ item.label }}
            </span>
          </div>

          <span
            class="min-w-[24px] h-6 px-2 flex items-center justify-center rounded-full bg-red-500 text-white text-xs font-semibold shrink-0"
          >
            {{ item.unreadCount > 99 ? '99+' : item.unreadCount }}
          </span>
        </button>
      </div>

      <div class="px-4 py-3 border-t bg-gray-50">
        <RouterLink
          to="/notification"
          class="text-sm text-blue-700 hover:text-blue-900"
          @click="props.notificationStore.closeBell()"
        >
          View all notifications
        </RouterLink>
      </div>
    </div>
  </div>
</template>
