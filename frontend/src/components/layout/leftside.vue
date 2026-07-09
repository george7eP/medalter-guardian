<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/users'
import type { Permission } from '@/api/user'
import {
  Management,
  User,
  Setting,
  Bell,
  Document,
  Tools,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('')

const iconMap: Record<string, any> = {
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
          title: '日誌管理',
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

activeMenu.value = getDefaultActive()
</script>

<template>
  <el-menu
      :default-active="activeMenu"
      class="el-menu-vertical"
      background-color="#ffffff"
      text-color="#303133"
      active-text-color="#409EFF"
      @select="handleMenuSelect"
  >
    <template v-for="item in menuTree" :key="item.index">
      <el-sub-menu v-if="item.children && item.children.length > 0" :index="item.index">
        <template #title>
          <el-icon>
            <component :is="getIcon(item.icon)" />
          </el-icon>
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
        <el-icon>
          <component :is="getIcon(item.icon)" />
        </el-icon>
        <template #title>{{ item.title }}</template>
      </el-menu-item>
    </template>
  </el-menu>
</template>

<style scoped>
.el-menu-vertical {
  height: calc(100vh - 60px);
  border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}
</style>
