<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
        <div class="logo">
          <span v-if="!isCollapse">API Platform</span>
          <el-icon v-else><Monitor /></el-icon>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
        >
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>控制台</template>
          </el-menu-item>
          
          <el-menu-item index="/tokens">
            <el-icon><Key /></el-icon>
            <template #title>API密钥</template>
          </el-menu-item>
          
          <el-menu-item index="/recharge">
            <el-icon><Coin /></el-icon>
            <template #title>充值</template>
          </el-menu-item>
          
          <el-menu-item index="/usage">
            <el-icon><TrendCharts /></el-icon>
            <template #title>用量统计</template>
          </el-menu-item>

          <el-menu-item index="/report">
            <el-icon><DataLine /></el-icon>
            <template #title>我的报表</template>
          </el-menu-item>

          <el-menu-item index="/announcements">
            <el-icon><Bell /></el-icon>
            <template #title>系统公告</template>
          </el-menu-item>
          
          <el-menu-item index="/models">
            <el-icon><Box /></el-icon>
            <template #title>模型定价</template>
          </el-menu-item>
          
          <el-menu-item index="/docs">
            <el-icon><Document /></el-icon>
            <template #title>API文档</template>
          </el-menu-item>
          
          <el-menu-item index="/chat">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>AI对话</template>
          </el-menu-item>
          
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <template #title>个人设置</template>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <!-- 头部 -->
        <el-header class="header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </div>
          
          <div class="header-right">
            <!-- 消息/通知图标 -->
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
              <el-icon class="header-icon" @click="showNotificationDrawer = true">
                <Bell />
              </el-icon>
            </el-badge>

            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32">{{ userStore.username?.[0]?.toUpperCase() }}</el-avatar>
                <span class="username">{{ userStore.username }}</span>
                <span class="balance">¥{{ formatBalance(userStore.balance) }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="settings">设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <!-- 主内容 -->
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { getUnreadAnnouncementCount, getMessages, markMessageRead } from '@/api/announcement'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(0)
const showNotificationDrawer = ref(false)
const notifications = ref([])
const notificationsLoading = ref(false)

const activeMenu = computed(() => route.path)

function formatBalance(balance) {
  return ((balance || 0) / 100).toFixed(2)
}

async function loadUnreadCount() {
  try {
    const res = await getUnreadAnnouncementCount()
    if (res.code === 200) {
      unreadCount.value = res.data
    }
  } catch (error) {
    console.error('Failed to load unread count:', error)
  }
}

async function loadNotifications() {
  notificationsLoading.value = true
  try {
    const res = await getMessages({ page: 1, pageSize: 10 })
    if (res.code === 200) {
      notifications.value = res.data.records
    }
  } catch (error) {
    console.error('Failed to load notifications:', error)
  } finally {
    notificationsLoading.value = false
  }
}

async function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/settings')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示')
        userStore.logout()
      } catch {}
      break
  }
}

onMounted(() => {
  loadUnreadCount()
})
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: #1a1a1a;
  transition: width 0.3s;
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    font-weight: bold;
    border-bottom: 1px solid #333;
    
    img {
      height: 32px;
      margin-right: 10px;
    }
  }
  
  .el-menu {
    border-right: none;
    background: transparent;
    
    .el-menu-item {
      color: #ccc;
      
      &:hover {
        background: #2a2a2a;
        color: white;
      }
      
      &.is-active {
        background: #409eff;
        color: white;
      }
    }
  }
}

.header {
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
  
  .collapse-btn {
    font-size: 20px;
    cursor: pointer;
    color: #666;
    
    &:hover { color: #409eff; }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    
    .username { color: #333; }
    .balance { 
      color: #67c23a; 
      font-weight: bold;
    }
  }

  .notification-badge {
    margin-right: 16px;

    .header-icon {
      font-size: 20px;
      color: #666;
      cursor: pointer;
      padding: 8px;
      border-radius: 50%;
      transition: all 0.3s;

      &:hover {
        background: #f0f0f0;
        color: #409eff;
      }
    }
  }
}

.main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
