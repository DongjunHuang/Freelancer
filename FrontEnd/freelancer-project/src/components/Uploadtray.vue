<template>
  <div v-if="visible" class="upload-tray">
    <div>File：{{ filename }}</div>
    <div>Status：{{ statusText }}</div>
    <div>Progress：{{ progress }}%</div>

    <div class="bar">
      <div class="bar-inner" :style="{ width: progress + '%' }"></div>
    </div>

    <div class="actions">
      <button v-if="status === 'uploading'" @click="$emit('cancel')">
        Cancel
      </button>
      <button v-else @click="$emit('close')">
        Close
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  visible: boolean;
  filename: string;
  progress: number;
  status: 'idle' | 'uploading' | 'done' | 'error';
}>();

const emit = defineEmits<{
  cancel: [];
  close: [];
}>();

const statusText = computed(() => {
  switch (props.status) {
    case 'uploading':
      return 'Uploading';
    case 'done':
      return 'Completed';
    case 'error':
      return 'Failed';
    default:
      return 'Free';
  }
});
</script>