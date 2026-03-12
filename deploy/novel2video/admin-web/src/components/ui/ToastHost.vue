<template>
  <div class="toast-stack">
    <div v-for="item in toastState.items" :key="item.id" class="toast-item" :class="item.tone">
      {{ item.message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { toastState } from '@/stores/toast';
</script>
