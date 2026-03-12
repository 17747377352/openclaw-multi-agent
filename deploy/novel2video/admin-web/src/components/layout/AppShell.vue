<template>
  <div class="page-shell">
    <section class="page-hero">
      <div class="page-hero-top">
        <div>
          <RouterLink v-if="backTo" class="page-back" :to="backTo">{{ backLabel }}</RouterLink>
          <h1 class="page-title">{{ title }}</h1>
          <div class="page-subtitle">{{ subtitle }}</div>
        </div>
        <div v-if="$slots.actions" class="page-actions">
          <slot name="actions" />
        </div>
      </div>
    </section>

    <slot />
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router';

withDefaults(
  defineProps<{
    title: string;
    subtitle?: string;
    backTo?: string;
    backLabel?: string;
  }>(),
  {
    subtitle: '',
    backTo: '',
    backLabel: '返回项目列表'
  }
);
</script>
