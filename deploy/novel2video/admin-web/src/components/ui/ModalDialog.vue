<template>
  <Teleport to="body">
    <div v-if="open" class="modal-mask" @click.self="$emit('close')">
      <div class="modal-card">
        <div class="modal-header">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;">
            <div>
              <div class="card-title">{{ title }}</div>
              <div v-if="description" class="card-muted" style="margin-top:6px;">{{ description }}</div>
            </div>
            <button class="btn btn-ghost" @click="$emit('close')">关闭</button>
          </div>
        </div>
        <div class="modal-body">
          <slot />
        </div>
        <div v-if="$slots.footer" class="modal-footer">
          <slot name="footer" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineEmits<{ close: [] }>();

withDefaults(
  defineProps<{
    open: boolean;
    title: string;
    description?: string;
  }>(),
  {
    description: ''
  }
);
</script>
