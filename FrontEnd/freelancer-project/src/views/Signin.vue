<script setup lang="ts">
import { ref, onUnmounted, computed, reactive} from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import { signin } from '@/api/auth'  
import { useAuth } from '@/stores/auth'
import { resendEmail } from '@/api/auth'  

const errorMsg = ref('')
const { setToken, setUser } = useAuth()
const router = useRouter()
const f = reactive({
  signin: {
    username: '',
    password: '',
  }
})
const loading = ref(false)
const error = ref('')
const showVerifyModal = ref(false)
const lastTriedUsername = ref('')
const message = useMessage()

const resendCooldown = ref(0)
let cooldownTimer: number | null = null
const isSignupFormValid = computed(() => {
  return f.signin.username.trim() && f.signin.password.trim()
})
function startCooldown(seconds = 60) {
  resendCooldown.value = seconds
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
  cooldownTimer = window.setInterval(() => {
    if (resendCooldown.value <= 1) {
      resendCooldown.value = 0
      if (cooldownTimer) {
        clearInterval(cooldownTimer)
        cooldownTimer = null
      }
    } else {
      resendCooldown.value--
    }
  }, 1000)
}

// The on query method to query for metrics
async function submit() {
  error.value = ''
  loading.value = true
  try {
    const res = await signin({
      username: f.signin.username,
      password: f.signin.password,
    });
    
    // Receive the access token from backend.
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
      console.log(e)
      const error = e?.response?.data?.error
      if (error === 'Invalid credentials') {
         errorMsg.value = error
         return
      }      

      const code = e?.response?.data?.code


      if (code === 'EMAIL_NOT_VERIFIED') {
        showVerifyModal.value = true
        return
      }

  } finally {
    loading.value = false
    lastTriedUsername.value = f.signin.username
  }
}

async function onResendEmailClick() {
  if (!lastTriedUsername.value) {
    message.error('Missing username, please input username and login again.')
    return
  }
  if (resendCooldown.value > 0) 
    return

  try {
    await resendEmail({ username: lastTriedUsername.value })
    message.success('Verification email has been resent, please check your inbox.')
    startCooldown(60)
  } catch (e: any) {
    message.error(
      e?.response?.data?.message || e?.message || 'Failed to resend email.'
    )
  } finally {
    showVerifyModal.value = false
  }
}

function onCloseVerifyModal() {
  showVerifyModal.value = false
}

onUnmounted(() => {
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
  }
})
</script>

<template>
  <!-- Sign in part -->
  <div class="min-h-screen flex flex-col">
    <main class="flex-1">
      <div class="max-w-md mx-auto px-4 pt-8">
        <h1 class="text-2xl font-bold mb-6">Login your account</h1>
        <form class="space-y-4" @submit.prevent="submit">
          <input v-model.trim="f.signin.username" type="text" placeholder="Username" class="w-full border rounded-lg px-3 py-2" required />
          <input v-model="f.signin.password" type="password" placeholder="Password" class="w-full border rounded-lg px-3 py-2" required />
          <p v-if="errorMsg" class="text-red-500 text-sm">
            {{ errorMsg }}
          </p>
          <button :disabled="!isSignupFormValid || loading"
                  @click="submit"
                  class="w-full rounded-full bg-slate-900 text-white py-2.5 disabled:opacity-40 hover:opacity-90 flex items-center justify-center">
            <span v-if="!loading">Login account</span>
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

  <!-- Resend email part -->
  <n-modal
    v-model:show="showVerifyModal"
    preset="dialog"
    title="Please verify your email"
    :closable="false"
    :mask-closable="false">
    <div class="space-y-2">
      <p>
        Your account is registered but the email address has not been verified yet.
      </p>
      <p>
        Please check your inbox (and spam folder). If you didn't receive the email,
        you can resend the verification email.
      </p>
    </div>

    <template #action>
      <n-space justify="end">
        <n-button quaternary @click="onCloseVerifyModal">
          Cancel
        </n-button>
        <n-button
          type="primary"
          :disabled="resendCooldown > 0"
          @click="onResendEmailClick">
          <template v-if="resendCooldown > 0">
            Resend in {{ resendCooldown }}
          </template>
          <template v-else>
            Resend email
          </template>
        </n-button>
      </n-space>
    </template>
  </n-modal>
</template>  