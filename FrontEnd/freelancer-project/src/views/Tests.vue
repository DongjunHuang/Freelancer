<template>
  <section class="wrap">
    <h2>Testing Page</h2>

    <div class="actions">
      <button :disabled="loading" @click="onInsertMysql">➕ Test Insert to Mysql</button>
      <button :disabled="loading" @click="onQueryMysql">🔍 Test Query from Mysql</button>
      <button :disabled="loading" @click="onInsertNosql">➕ Test Insert to MongoDB</button>
      <button :disabled="loading" @click="onQueryNosql">🔍 Test Query from MongoDB</button>
      <button :disabled="loading" @click="onSendEmailTest">🔍 Test Send Email</button>
      <button :disabled="loading" @click="onRefreshAccessTokenRequest">🔍 Test Refresh Token API</button>
    </div>

    <p v-if="toast" class="toast">{{ toast }}</p>
    <p v-else class="empty">No data yet. Click “Query metrics”.</p>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import * as tests from '@/api/tests'
import * as dashboard from '@/api/dashboard'

const loading = ref(false)
const toast = ref('')

function showToast(msg: string, ms = 1800) {
  toast.value = msg
  setTimeout(() => (toast.value = ''), ms)
}

async function onSendEmailTest() {
  try {
    await tests.sendEmailTest()
  } catch (e) {
    showToast('Insert failed')
  } finally {
    loading.value = false
  }
}

// The on insert function to insert metric to the backend server
async function onInsertMysql() {
  try {
    loading.value = true
    const val = +(Math.random() * 100).toFixed(2)
    await tests.insertMetricMysql(val)
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
    const res = await tests.fetchCountMysql()
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
    await tests.insertMetricNosql(val)
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
    const res = await tests.fetchCountNosql()
    const count = typeof res.data === 'number' ? res.data : res.data.count
    showToast(`Fetched count = ${count}`)
  } catch (e) {
    showToast('Query failed')
  } finally {
    loading.value = false
  }
}

// Test refresh token, this should be called only when you have already logged in
async function onRefreshAccessTokenRequest() {
  try {
    localStorage.removeItem('access_token')
    const res = await dashboard.refreshAccessTokenRequest()
    const count = typeof res.data === 'number' ? res.data : res.data.count
    showToast(`Fetched count = ${count}`)
  } catch (e) {
    showToast('Query failed')
  } finally {
    loading.value = false
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