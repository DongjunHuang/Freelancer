<script setup lang="ts">
import { useRouter, RouterLink } from 'vue-router'
import { useAuth } from '@/stores/auth'
import NotificationBell from '@/components/main/app-notification-bell.vue'
import { useUserNotificationStore } from '@/stores/notification-user'
import { signout } from '@/api/auth'
import { UserType } from '@/types/user'

// The imported function
const router = useRouter()
const auth = useAuth()
const notificationStore = useUserNotificationStore()

async function signOut() {
  try {
    await signout(UserType.USER)
  } finally {
    auth.clear()
    router.push('/signin')
  }
}
</script>

<template>
  <header class="w-full bg-white border-b flex items-center justify-between h-16 px-8">
    <div class="flex items-center space-x-8">
      <div class="text-xl font-bold text-blue-700">Data Reporter</div>

      <nav v-if="auth.isLoggedIn" class="flex items-center space-x-6 text-gray-700">
        <RouterLink
          to="/upload"
          class="hover:text-blue-700 transition-colors"
          active-class="text-blue-800 font-semibold"
        >
          Upload
        </RouterLink>

        <RouterLink
          to="/dashboard"
          class="hover:text-blue-700 transition-colors"
          active-class="text-blue-800 font-semibold"
        >
          Dashboard
        </RouterLink>

        <RouterLink
          to="/issue"
          class="hover:text-blue-700 transition-colors"
          active-class="text-blue-800 font-semibold"
        >
          Feedback
        </RouterLink>
      </nav>
    </div>

    <div v-if="!auth.isLoggedIn" class="flex items-center space-x-4">
      <RouterLink
        to="/signin"
        class="px-5 py-1.5 border border-blue-800 text-blue-800 rounded-full hover:bg-blue-50"
      >
        Log In
      </RouterLink>

      <RouterLink
        to="/signup"
        class="px-5 py-1.5 bg-blue-800 text-white rounded-full hover:bg-blue-900"
      >
        Sign Up
      </RouterLink>
    </div>

    <div v-else class="flex items-center space-x-4">
      <div class="text-sm text-gray-700 max-w-[220px] truncate">
        {{ auth.user?.username ?? '' }}
      </div>

      <NotificationBell :user-type="UserType.USER" :notification-store="notificationStore" />
      <button
        @click="signOut"
        class="px-5 py-1.5 bg-blue-800 text-white rounded-full hover:bg-red-700"
      >
        LOG OUT
      </button>
    </div>
  </header>
</template>
