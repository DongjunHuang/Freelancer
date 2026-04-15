import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import type { NotificationCategorySummary } from '@/types/notification'

export const useUserNotificationStore = defineStore('userNotification', () => {
  const unreadCount = ref(0)

  const categorySummaries = ref<NotificationCategorySummary[]>([])
  const summaryLoaded = ref(false)
  const summaryLoading = ref(false)

  const bellOpen = ref(false)
  const lastUnreadFetchedAt = ref<number | null>(null)

  const hasUnread = computed(() => unreadCount.value > 0)

  function setUnreadCount(count: number) {
    unreadCount.value = Math.max(0, count ?? 0)
    lastUnreadFetchedAt.value = Date.now()
  }

  function setUnreadSummary(unread: number, categories: NotificationCategorySummary[]) {
    unreadCount.value = Math.max(0, unread ?? 0)
    categorySummaries.value = categories ?? []
    summaryLoaded.value = true
    lastUnreadFetchedAt.value = Date.now()
  }

  function setSummaryLoading(value: boolean) {
    summaryLoading.value = value
  }

  function markAllReadLocally() {
    unreadCount.value = 0
    categorySummaries.value = categorySummaries.value.map((item) => ({
      ...item,
      unreadCount: 0,
    }))
  }

  function decrementCategoryLocally(category: string, count = 1) {
    if (!category || count <= 0) return

    unreadCount.value = Math.max(0, unreadCount.value - count)

    categorySummaries.value = categorySummaries.value
      .map((item) => {
        if (item.category !== category) return item

        return {
          ...item,
          unreadCount: Math.max(0, item.unreadCount - count),
        }
      })
      .filter((item) => item.unreadCount > 0)
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
    categorySummaries.value = []
    summaryLoaded.value = false
    summaryLoading.value = false
    bellOpen.value = false
    lastUnreadFetchedAt.value = null
  }

  return {
    unreadCount,
    categorySummaries,
    summaryLoaded,
    summaryLoading,
    bellOpen,
    hasUnread,
    lastUnreadFetchedAt,

    setUnreadCount,
    setUnreadSummary,
    setSummaryLoading,

    markAllReadLocally,
    decrementCategoryLocally,

    openBell,
    closeBell,
    toggleBell,
    reset,
  }
})
