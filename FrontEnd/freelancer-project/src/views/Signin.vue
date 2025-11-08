<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { signin } from '@/api/auth'  
import { useAuth } from '@/stores/auth'
const { setToken, setUser } = useAuth()

const router = useRouter()
const f = ref({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

// The on query method to query for metrics
async function submit() {
  error.value = ''
  loading.value = true
  try {
    const res = await signin({
      username: f.value.username,
      password: f.value.password,
    });


    const accessToken = res.data?.accessToken
    const user = res.data?.user
    if (accessToken) {
      setToken(accessToken)   
      setUser(user)     
      router.push('/dashboard')
    } else {
      throw new Error('No access token in response')
    }
  } catch (e: any) {
    if (e.response?.data?.detail) 
      error.value = e.response.data.detail
    else 
    error.value = e.response?.data?.message || e.message || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <main class="flex-1">
      <div class="max-w-md mx-auto px-4 pt-8">
        <h1 class="text-2xl font-bold mb-6">Login your account</h1>
        <form class="space-y-4" @submit.prevent="submit">
          <input v-model.trim="f.username" type="text" placeholder="Username" class="w-full border rounded-lg px-3 py-2" required />
          <input v-model="f.password" type="password" placeholder="Password" class="w-full border rounded-lg px-3 py-2" required />
          <button class="w-full rounded-full bg-slate-900 text-white py-2.5 hover:opacity-90">Submit</button>
          <p class="text-xs text-slate-500">
            By signing up you agree to our Terms and Privacy Policy.
          </p>
          <p v-if="error" class="text-sm text-red-600">{{ error }}</p>
        </form>
      </div>
    </main>
  </div>
</template>  