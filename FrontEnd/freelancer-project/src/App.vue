<script setup lang="ts">
import AppHeader from '@/components/Main/AppHeader.vue'
import AppFooter from '@/components/Main/AppFooter.vue'
import { NMessageProvider } from 'naive-ui';
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<template>
    <div class="min-h-screen bg-gray-50 overflow-x-hidden">
      <!-- The navigation bar -->
      <AppHeader v-show="route.meta.layout !== 'blank'" />

      <!-- the main part-->
      <main class="pt-16">  
        <n-message-provider>
          <RouterView />
        </n-message-provider>
      </main>
      <AppFooter />
    </div>
</template>