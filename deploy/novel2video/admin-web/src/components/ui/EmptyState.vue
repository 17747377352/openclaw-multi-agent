<template>
  <div class="empty list-card">
    <div style="font-size: 40px; margin-bottom: 10px;">{{ icon }}</div>
    <div style="font-size: 16px; font-weight: 600; color: var(--text);">{{ title }}</div>
    <div style="margin-top: 6px;">{{ description }}</div>
    <div v-if="$slots.actions" style="margin-top: 16px;">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    icon?: string;
    title: string;
    description?: string;
  }>(),
  {
    icon: '[]',
    description: ''
  }
);
</script>
