<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
/* 全局样式：统一为简体中文字体栈，优先系统简体黑体，
   避免 Han 统一表意区字符回退到繁体/日文字形 */
html {
  font-family: "PingFang SC", "Microsoft YaHei", "微软雅黑",
    "Hiragino Sans GB", "Source Han Sans SC", "Noto Sans SC",
    "Helvetica Neue", Helvetica, Arial, sans-serif;
}
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
</style>