import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/users'
import { ElMessage } from 'element-plus' // ✨ 引入訊息提示

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
      redirect: '/device', // ✨ 關鍵修復：把登入後的預設首頁改成 /device，不要再預設跳到敏感的 /user
      children: [
        {
          path: '/user',
          name: 'user',
          meta: { permission: 'system:user' }, // ✨ 綁定權限標籤
          component: () => import('@/views/user/userlist.vue'),
        },
        {
          path: '/system/role',
          name: 'role',
          meta: { permission: 'system:role' }, // ✨ 綁定權限標籤
          component: () => import('@/views/system/role.vue'),
        },
        {
          path: '/system/log',
          name: 'system-log',
          meta: { permission: 'system:log' }, // ✨ 綁定權限標籤
          component: () => import('@/views/system/log.vue'),
        },
        {
          path: '/device',
          name: 'device',
          component: () => import('@/views/device/index.vue'),
        },
        {
          path: '/inspect/plan',
          name: 'inspect-plan',
          component: () => import('@/views/inspect/plan.vue'),
        },
        {
          path: '/inspect/record',
          name: 'inspect-record',
          component: () => import('@/views/inspect/record.vue'),
        },
        {
          path: '/warn/rule',
          name: 'warn-rule',
          component: () => import('@/views/warn/rule.vue'),
        },
        {
          path: '/warn/handle',
          name: 'warn-handle',
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

// 全局路由守衛 (攔截越權訪問)
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
      // 檢查即將前往的路由是否需要特定權限
      if (to.meta.permission && !userStore.hasPermission(to.meta.permission as string)) {
        ElMessage.error('拒絕訪問：您沒有權限進入此頁面')
        // 如果沒有權限，踢回上一個頁面或設備管理首頁
        // 如果沒有權限，踢回上一個頁面或設備管理首頁
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