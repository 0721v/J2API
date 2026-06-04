<template>
  <div class="admin-dashboard">
    <h2 class="page-title">{{ $t('admin.dashboard') }}</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon users">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
            <div class="stat-label">{{ $t('admin.totalUsers') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon tokens">
            <i class="el-icon-key"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalTokens || 0 }}</div>
            <div class="stat-label">{{ $t('admin.totalTokens') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon balance">
            <i class="el-icon-coin"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ formatNumber(stats.totalBalance || 0) }}</div>
            <div class="stat-label">{{ $t('admin.totalBalance') }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="stat-card">
          <div class="stat-icon orders">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalOrders || 0 }}</div>
            <div class="stat-label">{{ $t('admin.totalOrders') }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header">
            <span>{{ $t('admin.usageTrend') }}</span>
          </div>
          <div ref="usageChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <div slot="header">
            <span>{{ $t('admin.revenueTrend') }}</span>
          </div>
          <div ref="revenueChartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 渠道状态 -->
    <el-row :gutter="20" class="channels-row">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>{{ $t('admin.channelStatus') }}</span>
          </div>
          <el-table :data="channels" style="width: 100%">
            <el-table-column prop="name" :label="$t('admin.channelName')" width="150" />
            <el-table-column prop="type" :label="$t('admin.channelType')" width="120">
              <template slot-scope="{ row }">
                <el-tag :type="getChannelTypeColor(row.type)">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="models" :label="$t('admin.supportedModels')">
              <template slot-scope="{ row }">
                <el-tag
                  v-for="model in row.supportedModels"
                  :key="model"
                  size="small"
                  style="margin-right: 5px"
                >
                  {{ model }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="weight" :label="$t('admin.weight')" width="80" />
            <el-table-column prop="status" :label="$t('admin.status')" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="row.enabled ? 'success' : 'danger'">
                  {{ row.enabled ? $t('admin.enabled') : $t('admin.disabled') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column :label="$t('admin.actions')" width="100">
              <template slot-scope="{ row }">
                <el-button type="text" size="small" @click="editChannel(row)">
                  {{ $t('admin.edit') }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最新订单 -->
    <el-row :gutter="20" class="orders-row">
      <el-col :span="24">
        <el-card>
          <div slot="header">
            <span>{{ $t('admin.latestOrders') }}</span>
          </div>
          <el-table :data="latestOrders" style="width: 100%">
            <el-table-column prop="orderNo" :label="$t('admin.orderNo')" width="180" />
            <el-table-column prop="username" :label="$t('admin.user')" width="120" />
            <el-table-column prop="type" :label="$t('admin.orderType')" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="row.type === 'recharge' ? 'success' : 'primary'">
                  {{ row.type === 'recharge' ? $t('admin.recharge') : $t('admin.package') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" :label="$t('admin.amount')" width="100">
              <template slot-scope="{ row }">
                ¥{{ row.amount }}
              </template>
            </el-table-column>
            <el-table-column prop="status" :label="$t('admin.orderStatus')" width="100">
              <template slot-scope="{ row }">
                <el-tag :type="getOrderStatusType(row.status)">
                  {{ getOrderStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" :label="$t('admin.createdAt')" width="180" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getAdminStats, getChannelList, getOrderList } from '@/api/admin'
import * as echarts from 'echarts'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      stats: {},
      channels: [],
      latestOrders: []
    }
  },
  mounted() {
    this.loadStats()
    this.loadChannels()
    this.loadLatestOrders()
  },
  methods: {
    async loadStats() {
      try {
        const res = await getAdminStats()
        this.stats = res.data || {}
      } catch (error) {
        console.error('Failed to load stats:', error)
      }
    },
    async loadChannels() {
      try {
        const res = await getChannelList({ page: 1, size: 10 })
        this.channels = res.data?.records || []
      } catch (error) {
        console.error('Failed to load channels:', error)
      }
    },
    async loadLatestOrders() {
      try {
        const res = await getOrderList({ page: 1, size: 10 })
        this.latestOrders = res.data?.records || []
      } catch (error) {
        console.error('Failed to load orders:', error)
      }
    },
    formatNumber(num) {
      return Number(num).toLocaleString()
    },
    getChannelTypeColor(type) {
      const colors = {
        openai: 'primary',
        azure: 'warning',
        anthropic: 'success',
        google: 'info'
      }
      return colors[type] || ''
    },
    getOrderStatusType(status) {
      const types = {
        pending: 'warning',
        paid: 'success',
        cancelled: 'info',
        expired: 'danger'
      }
      return types[status] || ''
    },
    getOrderStatusText(status) {
      const texts = {
        pending: this.$t('admin.pending'),
        paid: this.$t('admin.paid'),
        cancelled: this.$t('admin.cancelled'),
        expired: this.$t('admin.expired')
      }
      return texts[status] || status
    },
    editChannel(row) {
      this.$router.push(`/admin/channels?action=edit&id=${row.id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-dashboard {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 10px;

    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;

      i {
        font-size: 28px;
        color: #fff;
      }

      &.users {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      &.tokens {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      &.balance {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
      &.orders {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .charts-row,
  .channels-row,
  .orders-row {
    margin-bottom: 20px;
  }

  .chart-card {
    .el-card__header {
      font-weight: 600;
    }
  }
}
</style>
