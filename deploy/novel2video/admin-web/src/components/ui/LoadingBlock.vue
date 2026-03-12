<template>
  <div class="loading list-card">{{ label }}</div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label?: string;
  }>(),
  {
    label: '加载中...'
  }
);
</script>
