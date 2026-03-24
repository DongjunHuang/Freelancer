<script setup lang="ts">
import AppHeader from '@/components/main/app-header.vue'
import AppFooter from '@/components/main/app-footer.vue'
import { NMessageProvider } from 'naive-ui'
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
  <div class="min-h-screen bg-gray-50 overflow-x-hidden">
    <!-- The navigation bar -->
    <AppHeader v-show="route.meta.layout !== 'blank'" />

    <!-- the main part-->
    <main>
      <n-message-provider>
        <RouterView />
      </n-message-provider>
    </main>
    <AppFooter />
  </div>
</template>
