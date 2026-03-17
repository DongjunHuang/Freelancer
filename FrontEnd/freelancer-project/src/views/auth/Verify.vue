<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { verifyEmail } from '@/api/auth'

const route = useRoute()
const router = useRouter()

const state = ref<'loading' | 'ok' | 'error'>('loading')
const errorMsg = ref<string>('')
onMounted(async () => {
  const token = String(route.query.token || '')
  if (!token) {
    state.value = 'error'
    errorMsg.value = 'need token parameter'
    return
  }

  try {
    await verifyEmail(token)
    state.value = 'ok'
    // Move to sign in page
    setTimeout(() => router.push('/signin'), 2000) 
  } catch (err: any) {
    state.value = 'error'
    errorMsg.value = err?.response?.data?.message || err?.message || 'Error'
  }
})
</script>

<template>
  <div class="min-h-screen flex justify-center items-start bg-gray-50 px-4 pt-20">
      <div class="w-full max-w-md bg-white rounded-2xl shadow p-8 text-center flex flex-col items-center space-y-3">
  
        <!-- Loading -->
        <template v-if="state === 'loading'">
          <div class="mx-auto h-10 w-10 animate-spin border-2 border-blue-600 border-t-transparent rounded-full"></div>
          <h1 class="text-lg font-semibold">Verifying your account…</h1>
          <p class="text-sm text-slate-500">Please wait</p>
        </template>
  
        <!-- Success -->
        <template v-else-if="state === 'ok'">
          <div class="mx-auto h-10 w-10 rounded-full bg-green-100 flex items-center justify-center">
            <span class="text-green-700 text-xl">✓</span>
          </div>
          <h1 class="text-xl font-bold">Successfully registered!</h1>
          <p class="text-sm text-slate-600">Moving to signin page.</p>
          <RouterLink
            to="/signin"
            class="inline-block mt-4 px-5 py-2 rounded-full bg-blue-700 text-white hover:bg-blue-800">
            Sign In
          </RouterLink>
        </template>
  
        <!-- Failed -->
        <template v-else>
          <div class="mx-auto h-10 w-10 rounded-full bg-red-100 flex items-center justify-center">
            <span class="text-red-700 text-xl">!</span>
          </div>
          <h1 class="text-xl font-bold">Verification failed.</h1>
          <p class="text-sm text-red-600 break-words">
            {{ errorMsg || 'The link is no.' }}
          </p>
          <RouterLink
            to="/signup"
            class="inline-block mt-4 px-5 py-2 rounded-full border border-slate-300 hover:bg-slate-50">
            Return to sign up
          </RouterLink>
        </template>
      </div>
    </div>
</template>