<script setup lang="ts">
import { ref, computed, reactive} from 'vue'
import { useRouter } from 'vue-router'
import { signup } from '@/api/auth'  
const errorMsg = ref('')
const router = useRouter()
const f = reactive({
  signup: {
    username: '',
    password: '',
    email: ''
  }
})
const loading = ref(false)
const error = ref('')
const success = ref('');
const isSignupFormValid = computed(() => {
  return f.signup.username.trim() && f.signup.email.trim() && f.signup.password.trim()
})

async function submit() {
  if (!isSignupFormValid.value || loading.value) 
    return
  error.value = ''
  success.value = ''
  loading.value = true

  try {
    const res = await signup({ username: f.signup.username, 
      password: f.signup.password, 
      email: f.signup.email })
    
    success.value = res.data?.message || 'Registration successful. Please check your email the link to verify.'
    setTimeout(() => router.push('/signin'), 1200)
  } catch (e: any) {  
    const code = e?.response?.data?.code
    const detail  = e?.response?.data?.detail
    
    if (code === 'USERNAME_USED' || code === 'EMAIL_USED') {
      errorMsg.value = detail
      return
    }

  } finally {
    loading.value = false
  }
}

</script>

<template>
  <div class="min-h-screen flex flex-col">
    <main class="flex-1">
      <div class="max-w-md mx-auto px-4 pt-8">
        <h1 class="text-2xl font-bold mb-6">Create your account</h1>
        <form class="space-y-4" @submit.prevent="submit">
          <input v-model.trim="f.signup.username" type="text" placeholder="Username" class="w-full border rounded-lg px-3 py-2" required />
          <input v-model.trim="f.signup.email" type="email" placeholder="Email" class="w-full border rounded-lg px-3 py-2" required />
          <input v-model="f.signup.password" type="password" placeholder="Password" class="w-full border rounded-lg px-3 py-2" required />
          
          <p v-if="errorMsg" class="text-red-500 text-sm">
            {{ errorMsg }}
          </p>
          <button :disabled="!isSignupFormValid || loading"
                  @click="submit"
                  class="w-full rounded-full bg-slate-900 text-white py-2.5 disabled:opacity-40 hover:opacity-90 flex items-center justify-center">
            <span v-if="!loading">Create account</span>
            <span v-else class="flex items-center gap-2">
              <svg class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
              </svg>
              Processing...
            </span>
          </button>
        </form>
      </div>
    </main>
  </div>
</template>
