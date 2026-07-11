<template>
  <div class="common-layout">
    <el-container>
      <el-header>
        <Top />
      </el-header>
      
      <el-container>
        <el-aside width="200px">
          <Leftside />
        </el-aside>
        
        <el-main>
          <router-view />
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
.common-layout, .el-container {
  height: 100vh; /* 让整个布局撑满萤幕高度 */
}
.el-header {
  padding: 0; /* 消除顶部导航栏预设的内边距 */
  border-bottom: 1px solid #dcdfe6;
}
.el-aside {
  border-right: 1px solid #dcdfe6;
  background-color: #fff;
}
.el-main {
  background-color: #f5f7fa; /* 给主体内容区域加一点浅灰背景，让白色卡片更凸显 */
}
</style>