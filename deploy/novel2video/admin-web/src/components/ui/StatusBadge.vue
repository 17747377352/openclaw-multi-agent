<template>
  <span class="badge" :class="tone">{{ label }}</span>
</template>

<script setup lang="ts">
defineProps<{
  label: string;
  tone: 'info' | 'success' | 'warning' | 'danger' | 'neutral';
}>();
</script>
