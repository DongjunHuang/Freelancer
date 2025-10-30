<template>
    <div class="flex flex-col items-center justify-center h-screen bg-gray-50">
      <h2 class="text-2xl font-semibold mb-4">Sign In</h2>
      <form class="flex flex-col w-64 space-y-3">
        <input
          type="text"
          placeholder="Username"
          class="border rounded-lg p-2"
          v-model="username"
        />
        <input
          type="password"
          placeholder="Password"
          class="border rounded-lg p-2"
          v-model="password"
        />
        <button
          type="button"
          class="bg-blue-500 text-white rounded-lg py-2 hover:bg-blue-600"
          @click="signIn"
        >
          Login
        </button>
      </form>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  const username = ref("");
  const password = ref("");
  const signIn = () => {
    console.log("login:", username.value, password.value);
    // TODO: 调用后端 API /api/login
  };
  </script>