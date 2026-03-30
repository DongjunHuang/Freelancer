<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserType } from '@/types/user'
import { getThreads } from '@/api/issue'

import type { Thread } from '@/types/thread'

const route = useRoute()
const router = useRouter()

type CachedThreadPage = {
  requestCursor: string | null
  items: Thread[]
  nextCursor: string | null
  hasMore: boolean
}

const pageCache = ref<CachedThreadPage[]>([])
const page = ref<number>(Number(route.query.page ?? 0))
const size = ref<number>(Number(route.query.size ?? 10))

const threads = ref<Thread[]>([])
const nextCursor = ref<string | null>(null)
const hasMore = ref(false)

const status = ref<string>(String(route.query.status ?? 'ALL'))
const anchor = ref<string>(String(route.query.anchor ?? ''))

const canGoPrev = computed(() => page.value > 0)
const canGoNext = computed(() => {
  const current = getCurrentCachedPage()
  return !!current?.hasMore
})

function getCurrentCachedPage() {
  return pageCache.value[page.value] ?? null
}

function applyPageData(cached: CachedThreadPage) {
  threads.value = cached.items
  nextCursor.value = cached.nextCursor
  hasMore.value = cached.hasMore
}

async function fetchPageByCursor(targetPage: number, cursor: string | null) {
  loading.value = true
  error.value = ''

  try {
    const resp = await getThreads(UserType.USER, {
      status: status.value,
      size: size.value,
      cursor,
    })

    const cachedPage: CachedThreadPage = {
      requestCursor: cursor,
      items: resp.data.items,
      nextCursor: resp.data.nextCursor,
      hasMore: resp.data.hasMore,
    }

    pageCache.value[targetPage] = cachedPage
    page.value = targetPage
    applyPageData(cachedPage)

    await syncUrlQuery()
  } catch (e: any) {
    error.value = e?.message ?? 'Failed to load threads'
  } finally {
    loading.value = false
  }
}

async function loadFirstPage() {
  pageCache.value = []
  await fetchPageByCursor(0, null)
}

async function setStatus(s: string) {
  if (status.value === s) return
  status.value = s
  page.value = 0
  await loadFirstPage()
}

async function goNext() {
  if (loading.value) return

  const current = getCurrentCachedPage()
  if (!current || !current.hasMore || !current.nextCursor) return

  const targetPage = page.value + 1
  const cachedNext = pageCache.value[targetPage]

  if (cachedNext) {
    page.value = targetPage
    applyPageData(cachedNext)
    await syncUrlQuery()
    return
  }

  await fetchPageByCursor(targetPage, current.nextCursor)
}

async function goPrev() {
  if (loading.value) return
  if (page.value <= 0) return

  const targetPage = page.value - 1
  const cachedPrev = pageCache.value[targetPage]

  if (!cachedPrev) return

  page.value = targetPage
  applyPageData(cachedPrev)
  await syncUrlQuery()
}

async function goPage(targetPage: number) {
  if (loading.value) return
  if (targetPage < 0) return
  if (targetPage === page.value) return

  const cached = pageCache.value[targetPage]
  if (cached) {
    page.value = targetPage
    applyPageData(cached)
    await syncUrlQuery()
    return
  }

  if (targetPage === page.value + 1) {
    await goNext()
    return
  }

  return
}

onMounted(async () => {
  await loadFirstPage()
})

function openThread(id: number) {
  router.push(`/issue/details/${id}`)
}

const loading = ref(false)
const error = ref('')

async function syncUrlQuery() {
  await router.replace({
    path: route.path,
    query: {
      ...route.query,
      page: String(page.value),
      size: String(size.value),
      status: String(status.value),
      ...(anchor.value ? { anchor: anchor.value } : {}),
    },
  })
}

const pageButtons = computed(() => {
  return pageCache.value.map((_, index) => index)
})

function formatDate(iso: string) {
  try {
    const d = new Date(iso)
    return d.toLocaleString()
  } catch {
    return iso
  }
}

function statusPillClass(s: string) {
  if (s === 'NEW') return 'border-gray-200 text-gray-700'
  if (s === 'IN_PROGRESS') return 'border-amber-200 text-amber-800'
  if (s === 'RESOLVED') return 'border-green-200 text-green-700'
  if (s === 'CLOSED') return 'border-slate-200 text-slate-700'
  return 'border-gray-200 text-gray-700'
}

const statusTabs = [
  { key: 'ALL', label: 'All' },
  { key: 'OPEN', label: 'Open' },
  { key: 'RESOLVED', label: 'Resolved' },
]

watch(
  () => route.query,
  (q) => {
    const newStatus = String(q.status ?? 'ALL')
    const newSize = Number(q.size ?? 10)

    if (newStatus !== status.value || newSize !== size.value) {
      status.value = newStatus
      size.value = newSize
      loadFirstPage()
    }
  },
)
</script>

<template>
  <div class="min-h-screen">
    <div class="max-w-5xl mx-auto px-4 py-8">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold">My Feedbacks.</h1>
        </div>

        <div class="flex items-center gap-2">
          <router-link
            to="/issue/new"
            class="px-3 py-2 rounded-lg bg-black text-white hover:bg-black/90"
          >
            Create
          </router-link>
        </div>
      </div>

      <div class="mt-6 flex flex-wrap items-center gap-2">
        <button
          v-for="s in statusTabs"
          :key="s.key"
          class="px-3 py-1.5 rounded-full border text-sm"
          :class="status === s.key ? 'bg-black text-white border-black' : 'hover:bg-gray-50'"
          @click="setStatus(s.key)"
          :disabled="loading"
        >
          {{ s.label }}
        </button>
      </div>

      <div class="mt-4 border rounded-xl overflow-hidden bg-white">
        <div v-if="loading && threads.length === 0" class="p-6 text-gray-600">Loading...</div>

        <div v-else-if="!loading && threads.length === 0" class="p-6 text-gray-600">
          No feedback yet.
        </div>

        <ul v-else class="divide-y">
          <li
            v-for="it in threads"
            :key="it.id"
            class="p-4 hover:bg-gray-50 cursor-pointer"
            @click="openThread(it.id)"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <div class="mt-2 font-medium truncate">
                  {{ it.title }}
                </div>
                <div class="mt-1 text-sm text-gray-600">
                  Updated: {{ formatDate(it.lastMessageAt) }}
                </div>
                <div class="mt-1 text-sm text-gray-600">
                  Created: {{ formatDate(it.createdAt) }}
                </div>
              </div>

              <div class="text-sm text-gray-500 whitespace-nowrap">
                <span
                  class="text-xs px-2 py-0.5 rounded-full border"
                  :class="statusPillClass(it.status)"
                >
                  {{ it.status }}
                </span>
              </div>
            </div>
          </li>
        </ul>
      </div>

      <div class="mt-6 flex items-center justify-between gap-4">
        <div class="text-sm text-gray-600">Page {{ page + 1 }}</div>

        <div class="flex items-center gap-2">
          <button
            class="px-3 py-2 rounded-lg border hover:bg-gray-50 disabled:opacity-50"
            @click="goPrev"
            :disabled="loading || !canGoPrev"
          >
            Prev
          </button>

          <button
            v-for="p in pageButtons"
            :key="p"
            class="w-10 h-10 rounded-lg border text-sm hover:bg-gray-50"
            :class="p === page ? 'bg-black text-white border-black hover:bg-black' : ''"
            @click="goPage(p)"
            :disabled="loading"
          >
            {{ p + 1 }}
          </button>

          <button
            class="px-3 py-2 rounded-lg border hover:bg-gray-50 disabled:opacity-50"
            @click="goNext"
            :disabled="loading || !canGoNext"
          >
            Next
          </button>
        </div>
      </div>

      <div v-if="error" class="mt-4 text-sm text-red-600">
        {{ error }}
      </div>
    </div>
  </div>
</template>
