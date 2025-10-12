<script setup>
import { useRouter } from 'vue-router'
// import { useMetricStore } from '../stores/metric'

const router = useRouter()
const metric = useMetricStore()

const fetchAndGo = async () => {
  await metric.fetchLatest()
  router.push({ name: 'about' })
}
</script>

<template>
  <div>
    <h2>Home</h2>
    <button @click="fetchAndGo" :disabled="metric.loading">
      {{ metric.loading ? 'Loading…' : '获取 Metric 并跳转到 About' }}
    </button>
    <p v-if="metric.error" style="color:red;">错误：{{ metric.error }}</p>
  </div>
</template>