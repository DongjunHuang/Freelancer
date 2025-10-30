<template>
  <div class="min-h-screen bg-gray-50 overflow-x-hidden">
    <!-- 顶部导航栏 -->
    <header class="w-full bg-white border-b flex items-center justify-between h-16 px-8">
      <!-- 左侧 -->
      <div class="flex items-center space-x-8">
        <div class="text-xl font-bold text-blue-700">Data Reporter</div>
        <nav class="flex items-center space-x-6 text-sm text-gray-700">
          <RouterLink to="/" class="hover:text-blue-700">Home</RouterLink>
          <RouterLink to="/testing" class="hover:text-blue-700">Testing</RouterLink>
          <RouterLink to="/data" class="hover:text-blue-700">Data</RouterLink>
        </nav>
      </div>

      <!-- 右侧按钮 -->
      <div class="flex items-center space-x-4">
        <RouterLink to="/login" class="px-5 py-1.5 border border-blue-800 text-blue-800 rounded-full hover:bg-blue-50">
          Log In
        </RouterLink>
        <RouterLink to="/signup" class="px-5 py-1.5 bg-blue-800 text-white rounded-full hover:bg-blue-900">
          Sign Up
        </RouterLink>
      </div>
    </header>

    <!-- 主体内容 -->
    <main class="pt-16">   <!-- 给 main 加上 pt-16 防止 header 遮住 -->
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>