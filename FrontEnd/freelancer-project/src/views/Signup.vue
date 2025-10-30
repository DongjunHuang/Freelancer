<!-- src/views/Signup.vue -->
<template>
    <div class="min-h-screen flex items-center justify-center bg-gray-50">
      <form class="w-full max-w-sm space-y-4 bg-white p-6 rounded-2xl border"
            @submit.prevent="submit">
        <h1 class="text-xl font-bold">Create your account</h1>
  
        <input v-model.trim="f.name" type="text" placeholder="Business name"
               class="w-full border rounded-lg px-3 py-2" required />
  
        <input v-model.trim="f.email" type="email" placeholder="Email"
               class="w-full border rounded-lg px-3 py-2" required />
  
        <input v-model="f.password" type="password" placeholder="Password"
               class="w-full border rounded-lg px-3 py-2" required />
  
        <button class="w-full rounded-full bg-slate-900 text-white py-2.5 hover:opacity-90">
          Create account
        </button>
      </form>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  const f = ref({ name: '', email: '', password: '' })
  function submit() {
    alert(`signup ok: ${f.value.email}`)
  }
  </script>
  
  <style scoped>
  /* 可选：页面级样式 */
  </style>