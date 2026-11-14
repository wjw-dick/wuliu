<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
    actionText?: string
    icon?: string
  }>(),
  {
    title: '暂无数据',
    description: '',
    actionText: '',
    icon: '!'
  }
)

defineEmits<{ (e: 'action'): void }>()
</script>

<template>
  <div class="state-block">
    <div class="state-block__icon" aria-hidden="true">{{ icon }}</div>
    <h3>{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
    <slot />
    <button v-if="actionText" class="btn btn--outline btn--sm" type="button" @click="$emit('action')">
      {{ actionText }}
    </button>
  </div>
</template>
