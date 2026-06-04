import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '控制台' }
      },
      {
        path: 'tokens',
        name: 'Tokens',
        component: () => import('@/views/tokens/index.vue'),
        meta: { title: 'API密钥' }
      },
      {
        path: 'tokens/create',
        name: 'TokenCreate',
        component: () => import('@/views/tokens/create.vue'),
        meta: { title: '创建密钥' }
      },
      {
        path: 'recharge',
        name: 'Recharge',
        component: () => import('@/views/recharge/index.vue'),
        meta: { title: '充值' }
      },
      {
        path: 'usage',
        name: 'Usage',
        component: () => import('@/views/usage/index.vue'),
        meta: { title: '用量统计' }
      },
      {
        path: 'models',
        name: 'Models',
        component: () => import('@/views/models/index.vue'),
        meta: { title: '模型定价' }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/index.vue'),
        meta: { title: '个人设置' }
      },
      {
        path: 'docs',
        name: 'Docs',
        component: () => import('@/views/docs/index.vue'),
        meta: { title: 'API文档' }
      },
      {
        path: 'agent',
        name: 'AgentCenter',
        component: () => import('@/views/agent/index.vue'),
        meta: { title: '代理商中心' }
      }
    ]
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('@/views/chat/index.vue'),
    meta: { title: 'AI Chat', public: true }
  },
  // ==================== 管理员后台路由 ====================
  {
    path: '/admin',
    component: () => import('@/layout/admin.vue'),
    meta: { title: '管理后台', requireAdmin: true },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '管理控制台' }
      },
      {
        path: 'channels',
        name: 'AdminChannels',
        component: () => import('@/views/admin/channels/index.vue'),
        meta: { title: '渠道管理' }
      },
      {
        path: 'models',
        name: 'AdminModels',
        component: () => import('@/views/admin/models/index.vue'),
        meta: { title: '模型管理' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/orders/index.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'packages',
        name: 'AdminPackages',
        component: () => import('@/views/admin/packages/index.vue'),
        meta: { title: '套餐管理' }
      },
      {
        path: 'system-settings',
        name: 'AdminSystemSettings',
        component: () => import('@/views/admin/settings/system.vue'),
        meta: { title: '系统设置' }
      },
      {
        path: 'oauth-settings',
        name: 'AdminOAuthSettings',
        component: () => import('@/views/admin/settings/oauth.vue'),
        meta: { title: 'OAuth设置' }
      },
      {
        path: 'user-groups',
        name: 'AdminUserGroups',
        component: () => import('@/views/admin/user-groups/index.vue'),
        meta: { title: '用户分组' }
      },
      {
        path: 'proxies',
        name: 'AdminProxies',
        component: () => import('@/views/admin/proxies/index.vue'),
        meta: { title: '代理管理' }
      },
      {
        path: 'customization',
        name: 'AdminCustomization',
        component: () => import('@/views/admin/settings/customization.vue'),
        meta: { title: '网站自定义' }
      },
      {
        path: 'agents',
        name: 'AdminAgents',
        component: () => import('@/views/admin/agent/index.vue'),
        meta: { title: '代理商管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/announcements/index.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'revenue',
        name: 'AdminRevenue',
        component: () => import('@/views/admin/revenue/index.vue'),
        meta: { title: '营收统计' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  NProgress.start()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - API Platform` : 'API Platform'

  // 检查登录状态
  const token = localStorage.getItem('access_token')
  const userRole = localStorage.getItem('user_role')

  // 公开路由直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 需要登录的路由
  if (!token) {
    next('/login')
    return
  }

  // 需要管理员权限的路由
  if (to.meta.requireAdmin && userRole !== 'admin') {
    next('/dashboard')
    return
  }

  // 已登录用户访问登录/注册页
  if ((to.path === '/login' || to.path === '/register') && token) {
    if (userRole === 'admin') {
      next('/admin/dashboard')
    } else {
      next('/dashboard')
    }
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
