<script setup lang="ts">
import type { DashboardFilters } from '@/composables/DashboardState'

const props = defineProps<{
  filters: DashboardFilters,
}>()
</script>
<template>
  <div class="mt-4">
    <div class="mb-2 flex items-center justify-between">
      <h3 class="text-sm font-semibold text-slate-900">
        Details
      </h3>
      <button
        type="button"
        class="text-xs text-slate-500 hover:text-slate-800"
      >
      Show all
      </button>
    </div>
    <div class="overflow-hidden rounded-2xl border border-slate-100">
      <table class="min-w-full text-left text-xs">
        <thead class="bg-slate-50 text-slate-500">
          <tr>
            <th class="px-4 py-2">Time</th>
            <th v-for="colKey in props.filters.selectedColumns" 
                :key="colKey" class="px-4 py-2">
              {{ }}
            </th>
          </tr>
        </thead>
      </table>
    </div>
  </div>
</template>