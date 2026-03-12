<template>
  <div class="stat-card list-card">
    <div class="stat-label">{{ label }}</div>
    <div class="stat-value">{{ value }}</div>
    <div v-if="meta" class="stat-meta">{{ meta }}</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  value: string | number;
  meta?: string;
}>();
</script>
