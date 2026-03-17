<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getThreads } from "@/api/issue";
import type { ThreadItem, ThreadFilterStatus } from "@/types/thread";

const threads = ref<ThreadItem[]>([]);
const nextCursor = ref<string | null>(null);
const hasMore = ref(false);
const filterStatus = ref<ThreadFilterStatus | undefined>();

const route = useRoute()
const router = useRouter()
const status = ref<string>(String(route.query.status ?? 'ALL'))


const statusTabs = [
  { key: 'ALL', label: 'All' },
  { key: 'OPEN', label: 'Open' },       
  { key: 'RESOLVED', label: 'Resolved' },
]

async function setStatus(s: string) {
  status.value = s
  page.value = 0
  // await syncUrlQuery()
  await fetchList()
}

async function fetchList() {
  loading.value = true;

  try {
    const resp = await getThreads({
      status: status.value,
      size: 20,
      cursor: null
    });
    console.log(resp)

    threads.value = resp.data.items;
    nextCursor.value = resp.data.nextCursor;
    hasMore.value = resp.data.hasMore;

  } finally {
    loading.value = false;
  }
}

async function loadMore() {
  if (!hasMore.value || !nextCursor.value) {
    return;
  }

  loading.value = true;

  try {
    const resp = await getThreads({
      status: filterStatus.value,
      size: 20,
      cursor: nextCursor.value
    });

    threads.value.push(...resp.data.items);
    nextCursor.value = resp.data.nextCursor;
    hasMore.value = resp.data.hasMore;

  } finally {
    loading.value = false;
  }
}

onMounted( () => {
    fetchList()
    console.log("Page loaded");
  }
)

function openThread(id: number) {
  router.push(`/issue/details/${id}`)
}


const loading = ref(false)
const error = ref('')

const page = ref<number>(Number(route.query.page ?? 0))
const size = ref<number>(Number(route.query.size ?? 20))
const anchor = ref<string>(String(route.query.anchor ?? ''))

const totalElements = ref<number | null>(null)
const totalPages = ref<number | null>(null)

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

async function goPage(p: number) {
  if (p < 0) return
  if (totalPages.value !== null && p >= totalPages.value) return
  page.value = p
  await syncUrlQuery()
  await fetchList()
}


async function refresh() {
  anchor.value = ''
  page.value = 0
  await syncUrlQuery()
  await fetchList()
}

const pageButtons = computed(() => {
  const tp = totalPages.value ?? 0
  if (tp <= 0) return [0]
  const current = page.value
  const window = 5
  const half = Math.floor(window / 2)
  let start = Math.max(0, current - half)
  let end = Math.min(tp - 1, start + window - 1)
  start = Math.max(0, end - window + 1)
  const arr: number[] = []
  for (let i = start; i <= end; i++) arr.push(i)
  return arr
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


watch(
  () => route.query,
  () => {
    page.value = Number(route.query.page ?? 0)
    size.value = Number(route.query.size ?? 20)
    status.value = String(route.query.status ?? 'ALL')
    anchor.value = String(route.query.anchor ?? '')
  }
)
</script>

<template>
  <div class="min-h-screen">
    <!-- Issue Headers -->
    <div class="max-w-5xl mx-auto px-4 py-8">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h1 class="text-2xl font-bold">My Feedbacks.</h1>
        </div>
  
        <div class="flex items-center gap-2">
          <router-link
            to="/issue/new"
            class="px-3 py-2 rounded-lg bg-black text-white hover:bg-black/90">
            Create
          </router-link>
        </div>
      </div>
  
      <!-- Issue Filters -->
      <div class="mt-6 flex flex-wrap items-center gap-2">
        <button
          v-for="s in statusTabs"
          :key="s.key"
          class="px-3 py-1.5 rounded-full border text-sm"
          :class="status === s.key ? 'bg-black text-white border-black' : 'hover:bg-gray-50'"
          @click="setStatus(s.key)"
          :disabled="loading">
          {{ s.label }}
        </button>
      </div>
  
      <!-- Issue List -->
      <div class="mt-4 border rounded-xl overflow-hidden bg-white">
        <div v-if="loading && threads.length === 0" class="p-6 text-gray-600">
          Loading...
        </div>
  
        <div v-else-if="!loading && threads.length === 0" class="p-6 text-gray-600">
          No feedback yet.
        </div>
  
        <ul v-else class="divide-y">
          <li
            v-for="it in threads"
            :key="it.title"
            class="p-4 hover:bg-gray-50"
            @click="openThread(it.id)">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <div class="mt-2 font-medium truncate">
                  <!-- Selection, you can change to detail page -->
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
                <span class="text-xs px-2 py-0.5 rounded-full border"
                        :class="statusPillClass(it.status)">
                    {{ it.status }}
                  </span>
              </div>
            </div>
          </li>
        </ul>
      </div>
  
      <!--Issue Paging -->
      <div class="mt-6 flex items-center justify-between gap-4">
        <div class="text-sm text-gray-600">
          <span v-if="totalElements !== null">
            Total: {{ totalElements }}
          </span>
        </div>
  
        <div class="flex items-center gap-2">
          <button
            class="px-3 py-2 rounded-lg border hover:bg-gray-50 disabled:opacity-50"
            @click="goPage(page - 1)"
            :disabled="loading || page <= 0">
            Prev
          </button>
  
          <button
            v-for="p in pageButtons"
            :key="p"
            class="w-10 h-10 rounded-lg border text-sm hover:bg-gray-50"
            :class="p === page ? 'bg-black text-white border-black hover:bg-black' : ''"
            @click="goPage(p)"
            :disabled="loading">
            {{ p + 1 }}
          </button>
  
          <button
            class="px-3 py-2 rounded-lg border hover:bg-gray-50 disabled:opacity-50"
            @click="goPage(page + 1)"
            :disabled="loading || (totalPages !== null && page >= totalPages - 1)">
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
  
