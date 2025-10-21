<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="site-header">
    <nav style="display:flex; gap:12px">
      <RouterLink :to="{ name: 'home' }">Home</RouterLink>
      <RouterLink :to="{ name: 'testing' }">Testing</RouterLink>
      <RouterLink :to="{ name: 'data' }">Data</RouterLink>
    </nav>
  </header>

  <main class="page">
    <RouterView :key="$route.fullPath" v-slot="{ Component }">
      <component :is="Component" />
    </RouterView>
  </main>
</template>

<style scoped>
.site-header { padding: 12px 16px; border-bottom: 1px solid #eee; }
.nav { display: flex; gap: 12px; }
.page { padding: 16px; }
</style>