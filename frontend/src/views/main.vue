<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <Top />
      </el-header>

      <el-container class="body-container">
        <el-aside :width="'var(--mg-aside-w)'">
          <Leftside />
        </el-aside>

        <el-main>
          <router-view v-slot="{ Component, route }">
            <transition name="mg-fade" mode="out-in">
              <component :is="Component" :key="route.path" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import Top from "@/components/layout/top.vue";
import Leftside from "@/components/layout/leftside.vue";
</script>

<style scoped>
.common-layout,
.common-layout > .el-container {
  height: 100vh;
}
.el-header {
  padding: 0;
  height: var(--mg-header-h);
  background: var(--mg-surface);
  border-bottom: 1px solid var(--mg-border);
  box-shadow: 0 1px 0 rgba(27, 42, 56, .02);
  z-index: 10;
}
.body-container {
  height: calc(100vh - var(--mg-header-h));
}
.el-aside {
  border-right: 1px solid var(--mg-border);
  background-color: var(--mg-surface);
  transition: width var(--mg-dur) var(--mg-ease);
}
.el-main {
  background-color: var(--mg-bg);
  padding: 22px 24px;
  overflow-y: auto;
}
</style>
