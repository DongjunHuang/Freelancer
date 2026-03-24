<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInst, FormRules } from 'naive-ui'
import { NCard, NForm, NFormItem, NInput, NButton, NAlert } from 'naive-ui'
import { useAdminAuth } from '@/stores/auth-admin'
import { adminSignin } from '@/api/admin'

const { setAccessToken, setAdmin } = useAdminAuth()

const errorMsg = ref('')
const router = useRouter()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)
const error = ref('')

const form = ref({
  username: '',
  password: '',
})

const rules: FormRules = {
  email: [
    {
      required: true,
      message: 'Please enter your email',
      trigger: ['input', 'blur'],
    },
    {
      type: 'email',
      message: 'Please enter a valid email address',
      trigger: ['input', 'blur'],
    },
  ],
  password: [
    {
      required: true,
      message: 'Please enter your password',
      trigger: ['input', 'blur'],
    },
  ],
}

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await adminSignin({
      username: form.value.username,
      password: form.value.password,
    })

    // Receive the access token from backend.
    const accessToken = res.data?.accessToken
    const user = res.data?.user

    if (accessToken) {
      setAccessToken(accessToken)
      setAdmin(user)
      router.push('/admin')
    } else {
      throw new Error('No access token in response')
    }
  } catch (e: any) {
    console.log(e)
    const error = e?.response?.data?.error
    if (error === 'Invalid credentials') {
      errorMsg.value = error
    }
  } finally {
    loading.value = false
  }
}
</script>
<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-6">
    <div class="w-full max-w-md">
      <!-- Brand / Title -->
      <div class="text-center mb-8">
        <div class="text-3xl font-semibold text-gray-900">Freelancer</div>
        <div class="mt-2 text-sm text-gray-500">Admin Console</div>
        <div class="mt-1 text-sm text-gray-400">Sign in to manage issues and feedback</div>
      </div>

      <!-- Login Card -->
      <NCard class="shadow-sm rounded-2xl border-0">
        <div class="space-y-5">
          <div>
            <div class="text-lg font-medium text-gray-900">Admin Sign In</div>
            <div class="mt-1 text-sm text-gray-500">
              Restricted access. Authorized administrators only.
            </div>
          </div>

          <NAlert v-if="error" type="error" :show-icon="false" class="rounded-lg">
            {{ error }}
          </NAlert>

          <NForm ref="formRef" :model="form" :rules="rules" label-placement="top">
            <NFormItem label="Username" path="username">
              <NInput
                v-model:value="form.username"
                placeholder="Enter admin username"
                size="large"
                clearable
                @keydown.enter="handleLogin"
              />
            </NFormItem>

            <NFormItem label="Password" path="password">
              <NInput
                v-model:value="form.password"
                type="password"
                show-password-on="click"
                placeholder="Enter password"
                size="large"
                @keydown.enter="handleLogin"
              />
            </NFormItem>

            <div class="pt-2">
              <NButton type="primary" size="large" block :loading="loading" @click="handleLogin">
                Sign In
              </NButton>
            </div>
          </NForm>
        </div>
      </NCard>

      <!-- Footer -->
      <div class="mt-6 text-center text-xs text-gray-400">Internal management portal</div>
    </div>
  </div>
</template>
