<script setup>
// import { useMetricStore } from '../stores/metric'
// const metric = useMetricStore()
</script>

<template>
  <div>
    <h2>About</h2>

    <div v-if="metric.loading">加载中…</div>
    <div v-else-if="!metric.data">还没有数据，请在 Home 点击按钮获取。</div>
    <div v-else>
      <pre>{{ metric.data }}</pre>
    </div>
  </div>
</template>