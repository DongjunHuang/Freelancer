<script setup lang="ts">
import { ref, computed, reactive} from 'vue'
import { useRouter } from 'vue-router'
import { signup, resendEmail } from '@/api/auth'  

const router = useRouter()
const f = reactive({
  signup: {
    username: '',
    password: '',
    email: ''
  },
  resend: {
    email: ''
  }
})
const cooldown = ref(0)
const loading = ref(false)
const error = ref('')
const success = ref('');
const isSignupFormValid = computed(() => {
  return f.signup.username.trim() && f.signup.email.trim() && f.signup.password.trim()
})
const isResendFormValid = computed(() => {
  return f.resend.email.trim()
})
const toast = (msg: string) => alert(msg)

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

const onResendEmailClick = async () => {
  if (cooldown.value) 
      return
  cooldown.value = 60
  const timer = setInterval(() => { cooldown.value!--; if (!cooldown.value) clearInterval(timer) }, 1000)
  const res = await resendEmail({ email: f.resend.email })
  toast(res.data?.message)
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
          <p class="text-xs text-slate-500">
            By signing up you agree to our Terms and Privacy Policy.
          </p>
        </form>
        <form class="space-y-4" @submit.prevent="onResendEmailClick">
          <input v-model.trim="f.resend.email" type="email" placeholder="Email" class="w-full border rounded-lg px-3 py-2" required />
          
          <button :disabled="!isResendFormValid || loading"
                  @click="onResendEmailClick"
                  class="w-full rounded-full bg-slate-900 text-white py-2.5 disabled:opacity-40 hover:opacity-90 flex items-center justify-center">
            <span v-if="!loading">Resend</span>
            <span v-else class="flex items-center gap-2">
              <svg class="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
              </svg>
              Processing...
            </span>
          </button>
          <p class="text-xs text-slate-500">
            By signing up you agree to our Terms and Privacy Policy.
          </p>
        </form>
      </div>
    </main>
  </div>
</template>
