<template>
  <div id="app" :class="themeClass">
    <router-view />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const themeClass = computed(() => ({
  'theme-light': userStore.theme === 'light',
  'theme-dark': userStore.theme === 'dark'
}))
</script>

<style lang="scss">
// 主题变量
:root {
  --primary-color: #409eff;
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  
  --bg-color: #ffffff;
  --bg-color-page: #f0f2f5;
  --text-color: #303133;
  --text-color-secondary: #606266;
  --border-color: #dcdfe6;
}

.theme-dark {
  --bg-color: #1a1a1a;
  --bg-color-page: #121212;
  --text-color: #e0e0e0;
  --text-color-secondary: #a0a0a0;
  --border-color: #3a3a3a;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

#app {
  min-height: 100vh;
  background-color: var(--bg-color-page);
}
</style>
