import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { NotificationItem } from '@/types/notification'

export const useUserNotificationStore = defineStore('adminNotification', () => {
  const unreadCount = ref(0)

  const summaryItems = ref<NotificationItem[]>([])
  const summaryLoaded = ref(false)
  const summaryLoading = ref(false)

  const bellOpen = ref(false)
  const lastUnreadFetchedAt = ref<number | null>(null)

  const hasUnread = computed(() => unreadCount.value > 0)

  function setUnreadCount(count: number) {
    unreadCount.value = Math.max(0, count ?? 0)
    lastUnreadFetchedAt.value = Date.now()
  }

  function setSummary(items: NotificationItem[], unread: number) {
    summaryItems.value = items ?? []
    unreadCount.value = Math.max(0, unread ?? 0)
    summaryLoaded.value = true
    lastUnreadFetchedAt.value = Date.now()
  }

  function setSummaryLoading(value: boolean) {
    summaryLoading.value = value
  }

  function markReadLocally(ids: number[]) {
    if (!ids.length) return

    const idSet = new Set(ids)
    let reduced = 0

    summaryItems.value = summaryItems.value.map((item) => {
      if (idSet.has(item.id) && !item.isRead) {
        reduced += 1
        return { ...item, isRead: true }
      }
      return item
    })

    unreadCount.value = Math.max(0, unreadCount.value - reduced)
  }

  function markAllReadLocally() {
    unreadCount.value = 0
    summaryItems.value = summaryItems.value.map((item) => ({
      ...item,
      isRead: true,
    }))
  }

  function openBell() {
    bellOpen.value = true
  }

  function closeBell() {
    bellOpen.value = false
  }

  function toggleBell() {
    bellOpen.value = !bellOpen.value
  }

  function reset() {
    unreadCount.value = 0
    summaryItems.value = []
    summaryLoaded.value = false
    summaryLoading.value = false
    bellOpen.value = false
    lastUnreadFetchedAt.value = null
  }

  return {
    unreadCount,
    summaryItems,
    summaryLoaded,
    summaryLoading,
    bellOpen,
    hasUnread,
    lastUnreadFetchedAt,

    setUnreadCount,
    setSummary,
    setSummaryLoading,

    markReadLocally,
    markAllReadLocally,

    openBell,
    closeBell,
    toggleBell,
    reset,
  }
})
