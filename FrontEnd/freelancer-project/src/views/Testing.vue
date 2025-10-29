<template>
  <section class="wrap">
    <h2>Testing Page</h2>

    <div class="actions">
      <button :disabled="loading" @click="onInsertMysql">➕ Insert one metric</button>
      <button :disabled="loading" @click="onQueryMysql">🔍 Query metrics</button>
      <button :disabled="loading" @click="onInsertNosql">➕ Insert one metric to MongoDB</button>
      <button :disabled="loading" @click="onQueryNosql">🔍 Query metrics from MongoDB</button>
    
    </div>

    <p v-if="toast" class="toast">{{ toast }}</p>

    <table v-if="rows.length" class="tbl">
      <thead>
        <tr>
          <th>#</th>
          <th>Timestamp</th>
          <th>Value</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(r, i) in rows" :key="r.ts + '-' + i">
          <td>{{ i + 1 }}</td>
          <td>{{ formatTs(r.ts) }}</td>
          <td>{{ r.value }}</td>
        </tr>
      </tbody>
    </table>

    <p v-else class="empty">No data yet. Click “Query metrics”.</p>
  </section>
</template>

<script setup>

import { ref } from 'vue'
import { onMounted, onBeforeUnmount } from 'vue'
import { insertMetricMysql, fetchCountMysql,  insertMetricNosql, fetchCountNosql} from '../api/metrics'
import { isAxiosError } from 'axios'

onMounted(() => console.log('[Testing] mounted'))
onBeforeUnmount(() => console.log('[Testing] unmounted'))
const loading = ref(false)
const toast = ref('')
const rows = ref([])

function showToast(msg, ms = 1800) {
  toast.value = msg
  setTimeout(() => (toast.value = ''), ms)
}

// The on insert function to insert metric to the backend server
async function onInsertMysql() {
  try {
    loading.value = true
    // 演示：随机值 0~100
    const val = +(Math.random() * 100).toFixed(2)
    await insertMetricMysql(val)
    showToast(`Inserted value=${val}`)
  } catch (e) {
    showToast('Insert failed')
  } finally {
    loading.value = false
  }
}

// The on query method to query for metrics
async function onQueryMysql() {
  try {
    loading.value = true
    const res = await fetchCountMysql()
    const count = typeof res.data === 'number' ? res.data : res.data.count
    showToast(`Fetched count = ${count}`)
  } catch (e) {
    showToast('Query failed')
  } finally {
    loading.value = false
  }
}

// The on insert function to insert metric to the backend server
async function onInsertNosql() {
  try {
    loading.value = true
    const val = +(Math.random() * 100).toFixed(2)
    await insertMetricNosql(val)
    showToast(`Inserted value=${val}`)
  } catch (e) {
    showToast('Insert failed')
  } finally {
    loading.value = false
  }
}

// The on query method to query for metrics
async function onQueryNosql() {
  try {
    loading.value = true
    const res = await fetchCountNosql()
    const count = typeof res.data === 'number' ? res.data : res.data.count
    showToast(`Fetched count = ${count}`)
  } catch (e) {
    showToast('Query failed')
  } finally {
    loading.value = false
  }
}



function formatTs(ts) {
  try {
    return new Date(ts).toLocaleString()
  } catch {
    return ts
  }
}
</script>

<style scoped>
.wrap { max-width: 840px; margin: 32px auto; font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; }
.actions { display: flex; gap: 12px; margin: 12px 0 18px; }
button { padding: 8px 14px; border: 1px solid #ddd; border-radius: 10px; cursor: pointer; }
button:disabled { opacity: 0.6; cursor: not-allowed; }
.toast { color: #0a7; margin: 6px 0 12px; }
.tbl { width: 100%; border-collapse: collapse; }
.tbl th, .tbl td { border: 1px solid #eee; padding: 8px 10px; text-align: left; }
.tbl thead { background: #fafafa; }
.empty { color: #777; }
</style>