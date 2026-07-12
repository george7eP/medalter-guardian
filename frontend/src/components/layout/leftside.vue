<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/users'
import {
  Odometer,
  Management,
  User,
  Setting,
  Bell,
  Document,
  Tools,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref('')

const iconMap: Record<string, any> = {
  dashboard: Odometer,
  device: Monitor,
  inspect: Tools,
  warn: Bell,
  system: Setting,
  user: User,
  role: Management,
  default: Document
}

const menuTree = computed(() => {
  const permissions = userStore.permissions || []

  const menuItems = [
    {
      index: 'dashboard',
      title: '数据概览',
      icon: 'dashboard',
      path: '/dashboard'
    },
    {
      index: 'device',
      title: '设备管理',
      icon: 'device',
      permission: 'device:list',
      path: '/device'
    },
    {
      index: 'inspect',
      title: '检修管理',
      icon: 'inspect',
      children: [
        {
          index: 'inspect-plan',
          title: '检修计划',
          permission: 'inspect:plan',
          path: '/inspect/plan'
        },
        {
          index: 'inspect-record',
          title: '检修记录',
          permission: 'inspect:record',
          path: '/inspect/record'
        }
      ]
    },
    {
      index: 'warn',
      title: '预警管理',
      icon: 'warn',
      children: [
        {
          index: 'warn-rule',
          title: '预警规则',
          permission: 'warn:rule',
          path: '/warn/rule'
        },
        {
          index: 'warn-handle',
          title: '预警处理',
          permission: 'warn:handle',
          path: '/warn/handle'
        }
      ]
    },
    {
      index: 'system',
      title: '系统管理',
      icon: 'system',
      children: [
        {
          index: 'user',
          title: '用户管理',
          permission: 'system:user',
          path: '/user'
        },
        {
          index: 'role',
          title: '角色管理',
          permission: 'system:role',
          path: '/system/role'
        },
        {
          index: 'log',
          title: '日志管理',
          permission: 'system:log',
          path: '/system/log'
        }
      ]
    }
  ]

  return filterMenuByPermission(menuItems, permissions)
})

function filterMenuByPermission(menu: any[], permissions: string[]): any[] {
  return menu.filter(item => {
    if (item.permission) {
      const hasPermission = permissions.includes(item.permission)
      if (!hasPermission) {
        return false
      }
    }

    if (item.children && item.children.length > 0) {
      item.children = filterMenuByPermission(item.children, permissions)
      return item.children.length > 0
    }

    return true
  })
}

function getIcon(iconName: string) {
  return iconMap[iconName] || iconMap.default
}

function handleMenuSelect(index: string) {
  activeMenu.value = index

  const menuItem = findMenuItem(menuTree.value, index)
  if (menuItem && menuItem.path) {
    router.push(menuItem.path)
  }
}

function findMenuItem(menu: any[], index: string): any {
  for (const item of menu) {
    if (item.index === index) {
      return item
    }
    if (item.children && item.children.length > 0) {
      const found = findMenuItem(item.children, index)
      if (found) {
        return found
      }
    }
  }
  return null
}

function getDefaultActive() {
  const currentPath = router.currentRoute.value.path
  const menuItem = findMenuItemByPath(menuTree.value, currentPath)
  return menuItem ? menuItem.index : ''
}

function findMenuItemByPath(menu: any[], path: string): any {
  for (const item of menu) {
    if (item.path === path) {
      return item
    }
    if (item.children && item.children.length > 0) {
      const found = findMenuItemByPath(item.children, path)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 高亮跟随当前路由：涵盖菜单点击、programmatic 导航（如仪表盘「查看全部」）、浏览器前进后退
watch(
  () => route.path,
  () => { activeMenu.value = getDefaultActive() },
  { immediate: true },
)
</script>

<template>
  <div class="side-wrap">
    <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        @select="handleMenuSelect"
    >
      <template v-for="item in menuTree" :key="item.index">
        <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.index">
          <template #title>
            <el-icon><component :is="getIcon(item.icon)" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item
            v-for="child in item.children"
            :key="child.index"
            :index="child.index"
          >
            {{ child.title }}
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item v-else :index="item.index">
          <el-icon><component :is="getIcon(item.icon)" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>

    <div class="side-footer">
      <span class="dot" /> 系统运行中
    </div>
  </div>
</template>

<style scoped>
.side-wrap {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px 12px 0;
}
.el-menu-vertical {
  flex: 1;
  border-right: none;
  background: transparent;
  --el-menu-item-height: 46px;
  --el-menu-sub-item-height: 42px;
}

/* 菜单项：圆角胶囊 + 间距 */
.el-menu-vertical :deep(.el-menu-item),
.el-menu-vertical :deep(.el-sub-menu__title) {
  height: 46px;
  margin: 4px 0;
  border-radius: 10px;
  color: var(--mg-text);
  font-weight: 500;
}
.el-menu-vertical :deep(.el-menu-item:hover),
.el-menu-vertical :deep(.el-sub-menu__title:hover) {
  background: var(--mg-surface-2);
  color: var(--mg-ink);
}

/* 选中态：柔和青绿背景 + 左侧指示条 */
.el-menu-vertical :deep(.el-menu-item.is-active) {
  background: var(--mg-primary-soft);
  color: var(--mg-primary-strong);
  font-weight: 600;
}
.el-menu-vertical :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  bottom: 9px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: var(--mg-primary);
}
.el-menu-vertical :deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--mg-primary-strong);
}

/* 子菜单缩进项 */
.el-menu-vertical :deep(.el-menu .el-menu-item) {
  min-width: 0;
}

.el-menu-vertical :deep(.el-icon) {
  font-size: 18px;
}

.side-footer {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 14px 12px;
  font-size: 12px;
  color: var(--mg-muted);
  border-top: 1px solid var(--mg-border);
  margin-top: 8px;
}
.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--mg-success);
  box-shadow: 0 0 0 3px rgba(22, 163, 74, .16);
  animation: mg-pulse 2s var(--mg-ease) infinite;
}
@keyframes mg-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: .4; }
}
</style>
