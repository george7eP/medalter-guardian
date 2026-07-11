import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/users'
import { ElMessage } from 'element-plus' // ✨ 引入讯息提示

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login.vue'),
    },
    {
      path: '/',
      name: 'main',
      component: () => import('@/views/main.vue'),
      redirect: '/device', // ✨ 关键修复：把登入后的预设首页改成 /device，不要再预设跳到敏感的 /user
      children: [
        {
          path: '/user',
          name: 'user',
          meta: { permission: 'system:user' }, // ✨ 绑定权限标签
          component: () => import('@/views/user/userlist.vue'),
        },
        {
          path: '/system/role',
          name: 'role',
          meta: { permission: 'system:role' }, // ✨ 绑定权限标签
          component: () => import('@/views/system/role.vue'),
        },
        {
          path: '/system/log',
          name: 'system-log',
          meta: { permission: 'system:log' }, // ✨ 绑定权限标签
          component: () => import('@/views/system/log.vue'),
        },
        {
          path: '/device',
          name: 'device',
          meta: { permission: 'device:list' },
          component: () => import('@/views/device/index.vue'),
        },
        {
          path: '/inspect/plan',
          name: 'inspect-plan',
          meta: { permission: 'inspect:plan' },
          component: () => import('@/views/inspect/plan.vue'),
        },
        {
          path: '/inspect/record',
          name: 'inspect-record',
          meta: { permission: 'inspect:record' },
          component: () => import('@/views/inspect/record.vue'),
        },
        {
          path: '/warn/rule',
          name: 'warn-rule',
          meta: { permission: 'warn:rule' },
          component: () => import('@/views/warn/rule.vue'),
        },
        {
          path: '/warn/handle',
          name: 'warn-handle',
          meta: { permission: 'warn:handle' },
          component: () => import('@/views/warn/handle.vue'),
        },
        {
          path: '/user/profile',
          name: 'profile',
          component: () => import('@/views/user/profile.vue'),
        }
      ],
    }
  ],
})

// 全局路由守卫 (拦截越权访问)
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login') {
    if (userStore.isLoggedIn) {
      next('/')
    } else {
      next()
    }
  } else {
    if (userStore.isLoggedIn) {
      // 检查即将前往的路由是否需要特定权限
      if (to.meta.permission && !userStore.hasPermission(to.meta.permission as string)) {
        ElMessage.error('拒绝访问：您没有权限进入此页面')
        // 如果没有权限，踢回上一个页面或设备管理首页
        // 如果没有权限，踢回上一个页面或设备管理首页
        if (from.path === '/') {
          next('/device')
        } else {
          next(false)
        }
      } else {
        next()
      }
    } else {
      next('/login')
    }
  }
})

export default router