<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
        <div class="logo">
          <img v-if="!isCollapse" src="/logo.png" alt="Logo" />
          <span v-if="!isCollapse" class="logo-text">API Platform</span>
          <span v-else class="logo-text">API</span>
        </div>

        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <i class="el-icon-odometer"></i>
            <span slot="title">{{ $t('admin.dashboard') }}</span>
          </el-menu-item>

          <el-submenu index="channels">
            <template slot="title">
              <i class="el-icon-connection"></i>
              <span>{{ $t('admin.channels') }}</span>
            </template>
            <el-menu-item index="/admin/channels">
              {{ $t('admin.channelList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="models">
            <template slot="title">
              <i class="el-icon-cpu"></i>
              <span>{{ $t('admin.models') }}</span>
            </template>
            <el-menu-item index="/admin/models">
              {{ $t('admin.modelList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="users">
            <template slot="title">
              <i class="el-icon-user"></i>
              <span>{{ $t('admin.users') }}</span>
            </template>
            <el-menu-item index="/admin/users">
              {{ $t('admin.userList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="orders">
            <template slot="title">
              <i class="el-icon-document"></i>
              <span>{{ $t('admin.orders') }}</span>
            </template>
            <el-menu-item index="/admin/orders">
              {{ $t('admin.orderList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="packages">
            <template slot="title">
              <i class="el-icon-box"></i>
              <span>{{ $t('admin.packages') }}</span>
            </template>
            <el-menu-item index="/admin/packages">
              {{ $t('admin.packageList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="settings">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>{{ $t('admin.settings') }}</span>
            </template>
            <el-menu-item index="/admin/system-settings">
              {{ $t('admin.systemSettings') }}
            </el-menu-item>
            <el-menu-item index="/admin/oauth-settings">
              {{ $t('admin.oauthSettings') }}
            </el-menu-item>
            <el-menu-item index="/admin/customization">
              {{ $t('admin.customization') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="user-management">
            <template slot="title">
              <i class="el-icon-user-solid"></i>
              <span>{{ $t('admin.userManagement') }}</span>
            </template>
            <el-menu-item index="/admin/user-groups">
              {{ $t('admin.userGroups') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="proxy">
            <template slot="title">
              <i class="el-icon-guide"></i>
              <span>{{ $t('admin.proxyManagement') }}</span>
            </template>
            <el-menu-item index="/admin/proxies">
              {{ $t('admin.proxyList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="agent">
            <template slot="title">
              <i class="el-icon-s-marketing"></i>
              <span>{{ $t('admin.agentManagement') }}</span>
            </template>
            <el-menu-item index="/admin/agents">
              {{ $t('admin.agentList') }}
            </el-menu-item>
          </el-submenu>

          <el-submenu index="announcement">
            <template slot="title">
              <i class="el-icon-bell"></i>
              <span>公告管理</span>
            </template>
            <el-menu-item index="/admin/announcements">
              公告列表
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <el-container>
        <!-- 顶部导航 -->
        <el-header class="header">
          <div class="header-left">
            <i
              class="el-icon-s-fold collapse-btn"
              @click="isCollapse = !isCollapse"
            ></i>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">
                {{ $t('admin.home') }}
              </el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute">
                {{ currentRoute }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-dropdown @command="handleCommand" trigger="click">
              <span class="user-dropdown">
                <el-avatar :size="32" :src="userInfo.avatar">
                  {{ userInfo.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userInfo.username }}</span>
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="profile">
                  <i class="el-icon-user"></i>
                  {{ $t('admin.profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="dashboard">
                  <i class="el-icon-s-home"></i>
                  {{ $t('admin.backToUser') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <i class="el-icon-switch-button"></i>
                  {{ $t('admin.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 主内容区 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useUserStore } from '@/stores/user'

export default {
  name: 'AdminLayout',
  data() {
    return {
      isCollapse: false
    }
  },
  computed: {
    ...mapState(useUserStore, ['userInfo']),
    activeMenu() {
      return this.$route.path
    },
    currentRoute() {
      const routeMap = {
        '/admin/dashboard': this.$t('admin.dashboard'),
        '/admin/channels': this.$t('admin.channelList'),
        '/admin/models': this.$t('admin.modelList'),
        '/admin/users': this.$t('admin.userList'),
        '/admin/orders': this.$t('admin.orderList'),
        '/admin/packages': this.$t('admin.packageList'),
        '/admin/system-settings': this.$t('admin.systemSettings'),
        '/admin/oauth-settings': this.$t('admin.oauthSettings'),
        '/admin/customization': this.$t('admin.customization'),
        '/admin/user-groups': this.$t('admin.userGroups'),
        '/admin/proxies': this.$t('admin.proxyList'),
        '/admin/agents': this.$t('admin.agentList'),
        '/admin/announcements': '公告管理',
        '/admin/revenue': '营收统计'
      }
      return routeMap[this.$route.path]
    }
  },
  methods: {
    ...mapActions(useUserStore, ['logout']),
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/settings')
          break
        case 'dashboard':
          this.$router.push('/dashboard')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },
    async handleLogout() {
      try {
        await this.logout()
        this.$router.push('/login')
      } catch (error) {
        console.error('Logout failed:', error)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-layout {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  overflow-x: hidden;
  transition: width 0.3s;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 10px;
    background-color: #2b3a4a;

    img {
      height: 32px;
      margin-right: 10px;
    }

    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .sidebar-menu {
    border-right: none;
  }
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .header-left {
    display: flex;
    align-items: center;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 20px;

      &:hover {
        color: #409eff;
      }
    }
  }

  .header-right {
    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 0 10px;

      .username {
        margin: 0 8px;
        color: #303133;
      }
    }
  }
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
