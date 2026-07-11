<script setup lang="ts">
withDefaults(defineProps<{
  title: string
  subtitle?: string
}>(), {
  subtitle: '',
})
</script>

<template>
  <div class="page-header">
    <div class="ph-accent" />
    <div class="ph-text">
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <div class="ph-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}
.ph-accent {
  width: 4px;
  align-self: stretch;
  min-height: 34px;
  border-radius: 3px;
  background: linear-gradient(180deg, var(--mg-primary), var(--mg-accent));
}
.ph-text { display: flex; flex-direction: column; gap: 3px; }
.ph-text h1 {
  margin: 0;
  font-size: 21px;
  font-weight: 700;
  color: var(--mg-ink);
  letter-spacing: .5px;
}
.ph-text p {
  margin: 0;
  font-size: 13px;
  color: var(--mg-muted);
}
.ph-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
