  
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from "vue-router";
import { useAdminAuth } from '@/stores/authAdmin'
import { NButton, NCard, NTag } from "naive-ui";

  
  const router = useRouter();
  const adminAuth = useAdminAuth();
  
  const activeMenu = ref<"feedback">("feedback");
  
  onMounted(() => {
    if (!adminAuth.isLoggedIn) {
      router.replace({
        path: "/admin/login",
        query: { redirect: "/admin" }
      });
    }
  });
  
  function handleLogout() {
    adminAuth.logout();
    router.replace("/admin/login");
  }
  
  function goToAdminIssues() {
    router.push("/admin/issues");
  }
  </script>

<template>
    <div class="min-h-screen bg-gray-50">
      <!-- Top Navbar -->
      <header class="h-16 bg-white border-b border-gray-200 px-6 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="text-xl font-semibold text-gray-900">Freelancer Admin</div>
          <NTag type="warning" size="small">Console</NTag>
        </div>
  
        <div class="flex items-center gap-3">
          <div class="text-sm text-gray-500">
            {{ "Admin" }}
          </div>
          <NButton quaternary size="small" @click="handleLogout">
            Logout
          </NButton>
        </div>
      </header>
  
      <div class="flex min-h-[calc(100vh-64px)]">
        <!-- Sidebar -->
        <aside class="w-64 bg-white border-r border-gray-200 p-4">
          <div class="text-xs font-medium text-gray-400 uppercase tracking-wide mb-3">
            Navigation
          </div>
  
          <div class="space-y-2">
            <button
              class="w-full flex items-center justify-between rounded-xl px-4 py-3 text-left transition"
              :class="activeMenu === 'feedback'
                ? 'bg-gray-900 text-white'
                : 'bg-gray-50 text-gray-700 hover:bg-gray-100'"
              @click="activeMenu = 'feedback'">
              <span>Feedback</span>
            </button>
          </div>
        </aside>
  
        <!-- Main -->
        <main class="flex-1 p-6">
          <div v-if="activeMenu === 'feedback'" class="space-y-6">
            <!-- Page Header -->
            <div>
              <div class="text-2xl font-semibold text-gray-900">
                User Feedback
              </div>
              <div class="mt-1 text-sm text-gray-500">
                Review and reply to user issues, suggestions, and feedback.
              </div>
            </div>
  
            <!-- Summary Cards -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <NCard class="rounded-2xl shadow-sm">
                <div class="text-sm text-gray-500">Waiting Admin</div>
                <div class="mt-2 text-3xl font-semibold text-gray-900">
                  12
                </div>
              </NCard>
  
              <NCard class="rounded-2xl shadow-sm">
                <div class="text-sm text-gray-500">Waiting User</div>
                <div class="mt-2 text-3xl font-semibold text-gray-900">
                  5
                </div>
              </NCard>
  
              <NCard class="rounded-2xl shadow-sm">
                <div class="text-sm text-gray-500">Resolved</div>
                <div class="mt-2 text-3xl font-semibold text-gray-900">
                  28
                </div>
              </NCard>
            </div>
  
            <!-- Feedback list placeholder -->
            <NCard class="rounded-2xl shadow-sm">
              <div class="flex items-center justify-between mb-4">
                <div>
                  <div class="text-lg font-medium text-gray-900">
                    Recent Threads
                  </div>
                  <div class="text-sm text-gray-500">
                    This section will show the issue list for admins to reply.
                  </div>
                </div>
  
                <NButton type="primary" @click="goToAdminIssues">
                  Open Feedback Panel
                </NButton>
              </div>
  
              <div class="rounded-xl border border-dashed border-gray-300 bg-gray-50 p-8 text-center">
                <div class="text-base font-medium text-gray-800">
                  Admin feedback module
                </div>
                <div class="mt-2 text-sm text-gray-500">
                  Next step: connect this area to your admin issue list API.
                </div>
              </div>
            </NCard>
          </div>
        </main>
      </div>
    </div>
  </template>