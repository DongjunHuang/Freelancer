<template>
    <div class="date-config">
      <div v-if="headers.length" style="margin-top: 12px;">
        <label>
          Select date column:
          <select v-model="selectedDateColumn">
            <option disabled value="">-- Select --</option>
            <option v-for="h in headers" :key="h" :value="h">
              {{ h }}
            </option>
          </select>
        </label>
      </div>
  
      <div v-if="headers.length" style="margin-top: 12px;">
        <label>
          Date Format：
          <select v-model="selectedFormat">
            <option disabled value="">-- Select --</option>
            <option v-for="f in dateFormats" :key="f" :value="f">
              {{ f }}
            </option>
          </select>
        </label>
      </div>
      <div v-if="headers.length" style="margin-top: 16px;">
    </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, watch } from 'vue';
  
  const props = defineProps<{
    headers: string[];
    column: string;   // v-model:column → props.column
    format: string;   // v-model:format → props.format
  }>();
  
  const emit = defineEmits<{
    'update:column': [value: string];
    'update:format': [value: string];
  }>();

  const selectedDateColumn = ref(props.column);
  const selectedFormat = ref(props.format);
  
  watch(selectedDateColumn, (val) => emit('update:column', val));
  watch(selectedFormat, (val) => emit('update:format', val));
  
  watch(
  () => props.headers,
  (newHeaders) => {
    if (newHeaders.length && !selectedDateColumn.value) {
      selectedDateColumn.value = newHeaders[0];
    }
  },
  { immediate: true },
);

  const dateFormats = [
    'N/A',
    'yyyy-MM-dd',
    'yyyy/MM/dd',
    'MM/dd/yyyy',
    'dd/MM/yyyy'
  ];

  </script>