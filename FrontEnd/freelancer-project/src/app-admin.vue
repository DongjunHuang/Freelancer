<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminAuth } from '@/stores/auth-admin'
import { NButton, NCard, NTag } from 'naive-ui'

const router = useRouter()
const adminAuth = useAdminAuth()

const activeMenu = ref<'feedback'>('feedback')

onMounted(() => {
  if (!adminAuth.isLoggedIn) {
    router.replace({
      path: '/admin/login',
      query: { redirect: '/admin' },
    })
  }
})

function handleLogout() {
  adminAuth.logout()
  router.replace('/admin/login')
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
          {{ 'Admin' }}
        </div>
        <NButton quaternary size="small" @click="handleLogout"> Logout </NButton>
      </div>
    </header>

    <div class="flex min-h-[calc(100vh-64px)]">
      <!-- Sidebar -->
      <aside class="w-64 bg-white border-r min-h-[calc(100vh-64px)] p-4">
        <RouterLink to="/admin/issues" class="block mb-3">Feedback</RouterLink>
      </aside>

      <main class="flex-1 p-6">
        <n-message-provider>
          <RouterView />
        </n-message-provider>
      </main>
    </div>
  </div>
</template>
