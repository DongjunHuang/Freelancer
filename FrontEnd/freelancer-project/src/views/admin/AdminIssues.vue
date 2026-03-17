<template>
    <div class="min-h-screen bg-gray-50">
      <!-- Top Navbar -->
      <header class="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="text-xl font-semibold text-gray-900">Freelancer Admin</div>
          <NTag type="warning" size="small">Issues</NTag>
        </div>
  
        <div class="flex items-center gap-3">
          <NButton quaternary @click="goHome">Home</NButton>
          <NButton quaternary @click="handleLogout">Logout</NButton>
        </div>
      </header>
  
      <main class="max-w-7xl mx-auto px-6 py-6">
        <!-- Header -->
        <div class="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <div class="text-2xl font-semibold text-gray-900">User Feedback</div>
            <div class="mt-1 text-sm text-gray-500">
              Review user issues, suggestions, and reply to open threads.
            </div>
          </div>
  
          <div class="flex items-center gap-3">
            <NInput
              v-model:value="keyword"
              clearable
              placeholder="Search by title..."
              style="width: 260px"
            />
          </div>
        </div>
  
        <!-- Stats -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
          <NCard class="rounded-2xl shadow-sm">
            <div class="text-sm text-gray-500">All</div>
            <div class="mt-2 text-3xl font-semibold text-gray-900">{{ allCount }}</div>
          </NCard>
  
          <NCard class="rounded-2xl shadow-sm">
            <div class="text-sm text-gray-500">Waiting Admin</div>
            <div class="mt-2 text-3xl font-semibold text-gray-900">{{ waitingAdminCount }}</div>
          </NCard>
  
          <NCard class="rounded-2xl shadow-sm">
            <div class="text-sm text-gray-500">Waiting User</div>
            <div class="mt-2 text-3xl font-semibold text-gray-900">{{ waitingUserCount }}</div>
          </NCard>
  
          <NCard class="rounded-2xl shadow-sm">
            <div class="text-sm text-gray-500">Resolved</div>
            <div class="mt-2 text-3xl font-semibold text-gray-900">{{ resolvedCount }}</div>
          </NCard>
        </div>
  
        <!-- Filter Tabs -->
        <div class="mt-6 flex flex-wrap gap-2">
          <NButton
            :type="activeStatus === 'ALL' ? 'primary' : 'default'"
            @click="activeStatus = 'ALL'">
            All
          </NButton>
          <NButton
            :type="activeStatus === 'WAITING_ADMIN' ? 'primary' : 'default'"
            @click="activeStatus = 'WAITING_ADMIN'">
            Waiting Admin
          </NButton>
          <NButton
            :type="activeStatus === 'WAITING_USER' ? 'primary' : 'default'"
            @click="activeStatus = 'WAITING_USER'"
          >
            Waiting User
          </NButton>
          <NButton
            :type="activeStatus === 'RESOLVED' ? 'primary' : 'default'"
            @click="activeStatus = 'RESOLVED'"
          >
            Resolved
          </NButton>
        </div>
  
        <!-- Issue List -->
        <NCard class="mt-6 rounded-2xl shadow-sm">
          <template #header>
            <div class="flex items-center justify-between">
              <div class="text-lg font-medium">Threads</div>
              <div class="text-sm text-gray-500">
                {{ filteredThreads.length }} result<span v-if="filteredThreads.length !== 1">s</span>
              </div>
            </div>
          </template>
  
          <div v-if="loading" class="py-10 text-center text-gray-500">
            Loading...
          </div>
  
          <div v-else-if="filteredThreads.length === 0" class="py-10 text-center text-gray-500">
            No threads found.
          </div>
  
          <div v-else class="space-y-3">
            <div
              v-for="item in filteredThreads"
              :key="item.id"
              class="rounded-2xl border border-gray-200 bg-white p-4 hover:border-gray-300 transition"
            >
              <div class="flex items-start justify-between gap-4 flex-wrap">
                <div class="min-w-0 flex-1">
                  <div class="flex items-center gap-2 flex-wrap">
                    <div class="text-base font-semibold text-gray-900 truncate">
                      {{ item.title }}
                    </div>
  
                    <NTag :type="statusTagType(item.status)" size="small">
                      {{ item.status }}
                    </NTag>
  
                    <NTag type="info" size="small">
                      {{ item.type }}
                    </NTag>
                  </div>
  
                  <div class="mt-2 text-sm text-gray-500 flex flex-wrap gap-x-4 gap-y-1">
                    <span>Thread #{{ item.id }}</span>
                    <span>User #{{ item.userId }}</span>
                    <span>Updated {{ formatDate(item.lastMessageAt) }}</span>
                    <span>Created {{ formatDate(item.createdAt) }}</span>
                  </div>
  
                  <div class="mt-3 text-sm text-gray-700 line-clamp-2">
                    {{ item.preview }}
                  </div>
                </div>
  
                <div class="flex items-center gap-3">
                  <div
                    v-if="item.unreadByAdmin > 0"
                    class="rounded-full bg-red-50 text-red-600 text-xs px-2.5 py-1 font-medium"
                  >
                    {{ item.unreadByAdmin }} unread
                  </div>
  
                  <NButton type="primary" @click="openThread(item.id)">
                    Reply
                  </NButton>
                </div>
              </div>
            </div>
          </div>
        </NCard>
      </main>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed, onMounted, ref } from "vue";
  import { useRouter } from "vue-router";
  import { NButton, NCard, NInput, NTag } from "naive-ui";
  import { useAdminAuth } from "@/stores/authAdmin";
  
  type AdminThreadStatus = "WAITING_ADMIN" | "WAITING_USER" | "RESOLVED";
  type AdminThreadType = "BUG" | "SUGGESTION" | "CONTACT" | "FEATURE";
  
  type AdminThreadItem = {
    id: number;
    userId: number;
    title: string;
    status: AdminThreadStatus;
    type: AdminThreadType;
    createdAt: string;
    lastMessageAt: string;
    unreadByAdmin: number;
    preview: string;
  };
  
  const router = useRouter();
  const adminAuth = useAdminAuth();
  
  const loading = ref(false);
  const keyword = ref("");
  const activeStatus = ref<"ALL" | AdminThreadStatus>("ALL");
  
  const threads = ref<AdminThreadItem[]>([]);
  
  onMounted(async () => {
    if (!adminAuth.isLoggedIn) {
      router.replace({
        path: "/admin/login",
        query: { redirect: "/admin/issues" }
      });
      return;
    }
  
    await fetchThreads();
  });
  
  async function fetchThreads() {
    loading.value = true;
    try {
      // TODO: replace with real admin API
      // const resp = await getAdminThreads(...)
      // threads.value = resp.items
  
      await new Promise((resolve) => setTimeout(resolve, 400));
  
      threads.value = [
        {
          id: 101,
          userId: 12,
          title: "CSV upload fails when file contains UTF-8 BOM",
          status: "WAITING_ADMIN",
          type: "BUG",
          createdAt: "2026-03-13T09:00:00Z",
          lastMessageAt: "2026-03-13T11:30:00Z",
          unreadByAdmin: 2,
          preview: "Hi, I tried uploading a CSV file and the system fails when the file contains BOM characters..."
        },
        {
          id: 102,
          userId: 8,
          title: "Would love a dark mode for dashboard",
          status: "WAITING_USER",
          type: "SUGGESTION",
          createdAt: "2026-03-12T08:30:00Z",
          lastMessageAt: "2026-03-13T10:20:00Z",
          unreadByAdmin: 0,
          preview: "A dark mode would make the dashboard easier to use at night. Is this already planned?"
        },
        {
          id: 103,
          userId: 20,
          title: "Interested in collaborating on your analytics platform",
          status: "RESOLVED",
          type: "CONTACT",
          createdAt: "2026-03-10T07:45:00Z",
          lastMessageAt: "2026-03-11T14:00:00Z",
          unreadByAdmin: 0,
          preview: "I saw your project and I'm interested in discussing a possible collaboration..."
        },
        {
          id: 104,
          userId: 15,
          title: "Need export to PDF feature",
          status: "WAITING_ADMIN",
          type: "FEATURE",
          createdAt: "2026-03-11T13:10:00Z",
          lastMessageAt: "2026-03-13T12:05:00Z",
          unreadByAdmin: 1,
          preview: "It would be great if reports could be exported directly as PDF with chart snapshots."
        }
      ];
    } finally {
      loading.value = false;
    }
  }
  
  const filteredThreads = computed(() => {
    const q = keyword.value.trim().toLowerCase();
  
    return threads.value.filter((item) => {
      const matchStatus =
        activeStatus.value === "ALL" || item.status === activeStatus.value;
  
      const matchKeyword =
        !q ||
        item.title.toLowerCase().includes(q) ||
        item.preview.toLowerCase().includes(q) ||
        String(item.id).includes(q);
  
      return matchStatus && matchKeyword;
    });
  });
  
  const allCount = computed(() => threads.value.length);
  const waitingAdminCount = computed(
    () => threads.value.filter((t) => t.status === "WAITING_ADMIN").length
  );
  const waitingUserCount = computed(
    () => threads.value.filter((t) => t.status === "WAITING_USER").length
  );
  const resolvedCount = computed(
    () => threads.value.filter((t) => t.status === "RESOLVED").length
  );
  
  function statusTagType(status: AdminThreadStatus) {
    switch (status) {
      case "WAITING_ADMIN":
        return "error";
      case "WAITING_USER":
        return "warning";
      case "RESOLVED":
        return "success";
      default:
        return "default";
    }
  }
  
  function formatDate(value: string) {
    return new Date(value).toLocaleString();
  }
  
  function openThread(id: number) {
    router.push(`/admin/issues/${id}`);
  }
  
  function goHome() {
    router.push("/admin");
  }
  
  function handleLogout() {
    adminAuth.logout();
    router.replace("/admin/login");
  }
  </script>