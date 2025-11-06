
<script setup lang="ts">
import { useRouter, RouterLink, RouterView } from 'vue-router'
import { useAuth } from '@/stores/auth'
const router = useRouter()
const { isLoggedIn, setToken } = useAuth()
const signOut = () => {
  setToken(null)           
  router.push('/signin')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 overflow-x-hidden">
    <!-- The navigation bar -->
    <header class="w-full bg-white border-b flex items-center justify-between h-16 px-8">
      <!-- The left part -->
      <div class="flex items-center space-x-8">
        <div class="text-xl font-bold text-blue-700">Data Reporter</div>
      </div>

      <!-- The right part: signin & sign up buttons avialable -->
      <div v-if="!isLoggedIn" class="flex items-center space-x-4">
        <RouterLink
          to="/signin"
          class="px-5 py-1.5 border border-blue-800 text-blue-800 rounded-full hover:bg-blue-50">Log In
        </RouterLink>
        <RouterLink
          to="/signup"
          class="px-5 py-1.5 bg-blue-800 text-white rounded-full hover:bg-blue-900">Sign Up
        </RouterLink>
      </div>

      <!-- The right part: sign out button -->
      <div v-else class="flex items-center space-x-4">
        <button
          @click="signOut"
          class="px-5 py-1.5 bg-blue-800 text-white rounded-full hover:bg-red-700">
          LOG OUT
        </button>
      </div>
    </header>

    <!-- the main part-->
    <main class="pt-16">  
      <RouterView />
    </main>
  </div>
</template>