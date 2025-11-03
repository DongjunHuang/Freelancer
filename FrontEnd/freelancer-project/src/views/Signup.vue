<template>
    <div class="min-h-screen flex flex-col">
      <main class="flex-1">
        <div class="max-w-md mx-auto px-4 pt-8">
          <h1 class="text-2xl font-bold mb-6">Create your account</h1>
          <form class="space-y-4" @submit.prevent="submit">
            <input v-model.trim="f.username" type="text" placeholder="Username" class="w-full border rounded-lg px-3 py-2" required />
            <input v-model.trim="f.email" type="email" placeholder="Email" class="w-full border rounded-lg px-3 py-2" required />
            <input v-model="f.password" type="password" placeholder="Password" class="w-full border rounded-lg px-3 py-2" required />
            <button class="w-full rounded-full bg-slate-900 text-white py-2.5 hover:opacity-90">Create account</button>
            <p class="text-xs text-slate-500">
              By signing up you agree to our Terms and Privacy Policy.
            </p>
          </form>
        </div>
      </main>
    </div>
  </template>
  
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { signup } from '@/api/auth'  

const router = useRouter()
const f = ref({ username: '', password: '', email: '' })
const loading = ref(false)
const error = ref('')
const success = ref('');

async function submit() {
  error.value = ''
  success.value = ''
  loading.value = true

  try {
    const res = await signup({ username: f.value.username, password: f.value.password, email: f.value.email })
    
    success.value = res.data?.message || 'Registration successful. Please check your email the link to verify.'
    setTimeout(() => router.push('/login'), 1200)
  } catch (e: any) {
    if (e.response?.data?.detail) 
      error.value = e.response.data.detail
    else if (e.response?.data?.message) 
      error.value = e.response.data.message
    else 
      error.value = e.message || 'Registration failed'
  } finally {
    loading.value = false
  }
}
  </script>∏