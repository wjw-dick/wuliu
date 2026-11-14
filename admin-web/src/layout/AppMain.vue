<template>
  <main class="app-main">
    <router-view v-slot="{ Component, route: current }">
      <transition name="fade" mode="out-in">
        <keep-alive :include="tabs.cachedNames">
          <component :is="Component" :key="current.path" />
        </keep-alive>
      </transition>
    </router-view>
  </main>
</template>

<script setup lang="ts">
import { useTabsStore } from '@/stores/tabs'

const tabs = useTabsStore()
</script>

<style scoped>
.app-main {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  background: var(--page-bg);
  position: relative;
}
</style>
