<template>
  <div class="revenue-page">
    <!-- 页面标题和日期选择 -->
    <div class="page-header">
      <div class="header-left">
        <h2>营收统计</h2>
        <el-radio-group v-model="dateRangeType" size="default" @change="handleDateRangeChange">
          <el-radio-button label="today">今日</el-radio-button>
          <el-radio-button label="week">近7天</el-radio-button>
          <el-radio-button label="month">本月</el-radio-button>
          <el-radio-button label="year">本年</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>
      </div>
      <div class="header-right">
        <el-date-picker
          v-if="dateRangeType === 'custom'"
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadAllData"
        />
        <el-button type="primary" @click="loadAllData">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 概览统计卡片 -->
    <div class="overview-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card primary">
            <div class="stat-icon"><el-icon><Money /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(overview.totalRevenue) }}</div>
              <div class="stat-label">总营收</div>
              <div class="stat-trend" v-if="overview.netRevenue">
                净营收: ¥{{ formatNumber(overview.netRevenue) }}
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card success">
            <div class="stat-icon"><el-icon><Wallet /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(today.revenue) }}</div>
              <div class="stat-label">今日营收</div>
              <div class="stat-sub">{{ today.orders || 0 }} 笔订单</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card warning">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.payingUsers || 0 }}</div>
              <div class="stat-label">付费用户</div>
              <div class="stat-sub">今日新增 {{ today.newPayingUsers || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card info">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatNumber(month.dailyAvg || 0) }}</div>
              <div class="stat-label">日均营收</div>
              <div class="stat-sub">本月合计 ¥{{ formatNumber(month.revenue) }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <!-- 营收趋势图 -->
      <div class="chart-card main-chart">
        <div class="card-header">
          <h3>营收趋势</h3>
          <el-radio-group v-model="trendType" size="small">
            <el-radio-button label="daily">按日</el-radio-button>
            <el-radio-button label="monthly">按月</el-radio-button>
          </el-radio-group>
        </div>
        <div class="chart-container" ref="trendChartRef"></div>
      </div>

      <!-- 渠道占比图 -->
      <div class="chart-card side-chart">
        <div class="card-header">
          <h3>支付渠道分布</h3>
        </div>
        <div class="chart-container pie-chart" ref="channelChartRef"></div>
        <div class="chart-legend">
          <div v-for="channel in channelData" :key="channel.channel" class="legend-item">
            <span class="dot" :style="{ background: getChannelColor(channel.channel) }"></span>
            <span class="label">{{ getChannelName(channel.channel) }}</span>
            <span class="value">¥{{ formatNumber(channel.revenue) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细数据 -->
    <div class="data-row">
      <!-- 业务类型统计 -->
      <div class="data-card">
        <div class="card-header">
          <h3>业务类型统计</h3>
        </div>
        <el-table :data="businessData" stripe>
          <el-table-column prop="businessType" label="业务类型">
            <template #default="{ row }">
              <el-tag :type="row.businessType === 'recharge' ? 'success' : 'primary'" size="small">
                {{ row.businessType === 'recharge' ? '充值' : '套餐' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="orderCount" label="订单数" />
          <el-table-column prop="revenue" label="营收金额">
            <template #default="{ row }">
              <span class="money">¥{{ formatNumber(row.revenue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比">
            <template #default="{ row }">
              {{ calculatePercent(row.revenue) }}%
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Top消费用户 -->
      <div class="data-card">
        <div class="card-header">
          <h3>Top消费用户</h3>
        </div>
        <el-table :data="topUsers" stripe>
          <el-table-column type="rank" label="排名" width="60" />
          <el-table-column prop="username" label="用户" />
          <el-table-column prop="orderCount" label="订单数" />
          <el-table-column prop="totalAmount" label="消费金额">
            <template #default="{ row }">
              <span class="money">¥{{ formatNumber(row.totalAmount) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 代理分成 -->
    <div class="commission-row">
      <div class="commission-card">
        <div class="card-header">
          <h3>代理分成统计</h3>
        </div>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="commission-item">
              <div class="comm-value">¥{{ formatNumber(agentStats.totalCommission || 0) }}</div>
              <div class="comm-label">已结算分成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="commission-item">
              <div class="comm-value">¥{{ formatNumber(agentStats.pendingCommission || 0) }}</div>
              <div class="comm-label">待结算分成</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="commission-item">
              <div class="comm-value">{{ agentStats.settlementCount || 0 }}</div>
              <div class="comm-label">结算笔数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="commission-item">
              <div class="comm-value">{{ overview.payingUsers || 0 }}</div>
              <div class="comm-label">代理数量</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import {
  getRevenueOverview,
  getTodayRevenue,
  getMonthRevenue,
  getDailyRevenueTrend,
  getMonthlyRevenueTrend,
  getRevenueByChannel,
  getRevenueByBusiness,
  getTopConsumers,
  getAgentCommissionStats
} from '@/api/revenue'
import { ElMessage } from 'element-plus'
import { Money, Wallet, User, TrendCharts, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const dateRangeType = ref('month')
const customDateRange = ref([])
const trendType = ref('daily')

const overview = ref({})
const today = ref({})
const month = ref({})
const trendData = ref([])
const channelData = ref([])
const businessData = ref([])
const topUsers = ref([])
const agentStats = ref({})

const trendChartRef = ref(null)
const channelChartRef = ref(null)

const channelColors = {
  alipay: '#1677FF',
  wechat: '#07C160',
  stripe: '#635BFF',
  okx: '#FFFFFF',
  creem: '#FF6B6B'
}

function formatNumber(num) {
  if (num === null || num === undefined) return '0.00'
  return Number(num).toFixed(2)
}

function getChannelName(channel) {
  const names = {
    alipay: '支付宝',
    wechat: '微信支付',
    stripe: 'Stripe',
    okx: 'OKX',
    creem: 'Creem'
  }
  return names[channel] || channel || '其他'
}

function getChannelColor(channel) {
  return channelColors[channel] || '#999'
}

function calculatePercent(value) {
  const total = businessData.value.reduce((sum, item) => {
    return sum + (item.revenue || 0)
  }, 0)
  if (total === 0) return 0
  return ((value / total) * 100).toFixed(1)
}

function getDateRange() {
  const today = new Date()
  let startDate, endDate

  switch (dateRangeType.value) {
    case 'today':
      startDate = endDate = today.toISOString().split('T')[0]
      break
    case 'week':
      const weekAgo = new Date(today)
      weekAgo.setDate(weekAgo.getDate() - 7)
      startDate = weekAgo.toISOString().split('T')[0]
      endDate = today.toISOString().split('T')[0]
      break
    case 'month':
      startDate = today.toISOString().slice(0, 8) + '01'
      endDate = today.toISOString().split('T')[0]
      break
    case 'year':
      startDate = today.getFullYear() + '-01-01'
      endDate = today.toISOString().split('T')[0]
      break
    case 'custom':
      startDate = customDateRange.value?.[0] || today.toISOString().split('T')[0]
      endDate = customDateRange.value?.[1] || today.toISOString().split('T')[0]
      break
  }

  return { startDate, endDate }
}

async function loadOverview() {
  try {
    const res = await getRevenueOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('Failed to load overview:', error)
  }
}

async function loadTodayRevenue() {
  try {
    const res = await getTodayRevenue()
    if (res.code === 200) {
      today.value = res.data
    }
  } catch (error) {
    console.error('Failed to load today revenue:', error)
  }
}

async function loadMonthRevenue() {
  try {
    const res = await getMonthRevenue()
    if (res.code === 200) {
      month.value = res.data
    }
  } catch (error) {
    console.error('Failed to load month revenue:', error)
  }
}

async function loadTrendData() {
  try {
    const { startDate, endDate } = getDateRange()
    let res

    if (trendType.value === 'daily') {
      res = await getDailyRevenueTrend({ startDate, endDate })
      if (res.code === 200) {
        trendData.value = res.data
        renderTrendChart()
      }
    } else {
      res = await getMonthlyRevenueTrend(new Date().getFullYear())
      if (res.code === 200) {
        trendData.value = res.data
        renderTrendChart()
      }
    }
  } catch (error) {
    console.error('Failed to load trend data:', error)
  }
}

async function loadChannelData() {
  try {
    const { startDate, endDate } = getDateRange()
    const res = await getRevenueByChannel({ startDate, endDate })
    if (res.code === 200) {
      channelData.value = res.data || []
      renderChannelChart()
    }
  } catch (error) {
    console.error('Failed to load channel data:', error)
  }
}

async function loadBusinessData() {
  try {
    const { startDate, endDate } = getDateRange()
    const res = await getRevenueByBusiness({ startDate, endDate })
    if (res.code === 200) {
      businessData.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load business data:', error)
  }
}

async function loadTopUsers() {
  try {
    const res = await getTopConsumers(10)
    if (res.code === 200) {
      topUsers.value = res.data || []
    }
  } catch (error) {
    console.error('Failed to load top users:', error)
  }
}

async function loadAgentStats() {
  try {
    const res = await getAgentCommissionStats()
    if (res.code === 200) {
      agentStats.value = res.data || {}
    }
  } catch (error) {
    console.error('Failed to load agent stats:', error)
  }
}

function renderTrendChart() {
  if (!trendChartRef.value || trendData.value.length === 0) return

  const canvas = trendChartRef.value
  const ctx = canvas.getContext('2d')
  const width = canvas.width = canvas.offsetWidth
  const height = canvas.height = 300

  ctx.clearRect(0, 0, width, height)

  const padding = { top: 20, right: 30, bottom: 40, left: 60 }
  const chartWidth = width - padding.left - padding.right
  const chartHeight = height - padding.top - padding.bottom

  const data = trendData.value.map(d => ({
    label: d.date || `${d.year}-${String(d.month).padStart(2, '0')}`,
    revenue: Number(d.revenue || 0),
    orders: Number(d.order_count || d.orderCount || 0)
  }))

  if (data.length === 0) return

  const maxRevenue = Math.max(...data.map(d => d.revenue), 1)
  const maxOrders = Math.max(...data.map(d => d.orders), 1)
  const stepX = chartWidth / (data.length - 1 || 1)

  // 绘制网格
  ctx.strokeStyle = '#f0f0f0'
  ctx.lineWidth = 1
  for (let i = 0; i <= 5; i++) {
    const y = padding.top + (chartHeight / 5) * i
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(width - padding.right, y)
    ctx.stroke()

    ctx.fillStyle = '#999'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText((maxRevenue * (5 - i) / 5).toFixed(0), padding.left - 10, y + 4)
  }

  // X轴标签
  ctx.textAlign = 'center'
  const step = Math.max(1, Math.floor(data.length / 10))
  data.forEach((d, i) => {
    if (i % step === 0 || i === data.length - 1) {
      const x = padding.left + stepX * i
      ctx.fillText(String(d.label).slice(5), x, height - padding.bottom + 20)
    }
  })

  // 绘制营收线
  ctx.strokeStyle = '#409eff'
  ctx.lineWidth = 2
  ctx.beginPath()
  data.forEach((d, i) => {
    const x = padding.left + stepX * i
    const y = padding.top + chartHeight - (d.revenue / maxRevenue) * chartHeight
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()

  // 绘制订单线
  ctx.strokeStyle = '#67c23a'
  ctx.setLineDash([5, 5])
  ctx.beginPath()
  data.forEach((d, i) => {
    const x = padding.left + stepX * i
    const y = padding.top + chartHeight - (d.orders / maxOrders) * chartHeight
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
  ctx.setLineDash([])

  // 图例
  ctx.font = '12px sans-serif'
  ctx.fillStyle = '#409eff'
  ctx.fillRect(width - 150, 10, 12, 12)
  ctx.fillStyle = '#333'
  ctx.fillText('营收金额', width - 130, 20)

  ctx.fillStyle = '#67c23a'
  ctx.fillRect(width - 150, 30, 12, 12)
  ctx.fillStyle = '#333'
  ctx.fillText('订单数量', width - 130, 40)
}

function renderChannelChart() {
  if (!channelChartRef.value || channelData.value.length === 0) return

  const canvas = channelChartRef.value
  const ctx = canvas.getContext('2d')
  const width = canvas.width = canvas.offsetWidth
  const height = canvas.height = 200

  ctx.clearRect(0, 0, width, height)

  const centerX = width / 2
  const centerY = height / 2
  const radius = Math.min(width, height) / 2 - 20

  let startAngle = -Math.PI / 2
  const total = channelData.value.reduce((sum, d) => sum + Number(d.revenue || 0), 0)

  channelData.value.forEach((channel, index) => {
    const value = Number(channel.revenue || 0)
    const ratio = total > 0 ? value / total : 0
    const endAngle = startAngle + ratio * 2 * Math.PI

    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, radius, startAngle, endAngle)
    ctx.closePath()

    const color = channelColors[channel.channel] || `hsl(${index * 60}, 70%, 50%)`
    ctx.fillStyle = color
    ctx.fill()

    startAngle = endAngle
  })

  // 中心文字
  ctx.fillStyle = '#333'
  ctx.font = 'bold 16px sans-serif'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('¥' + formatNumber(total), centerX, centerY)
  ctx.font = '12px sans-serif'
  ctx.fillStyle = '#999'
  ctx.fillText('总营收', centerX, centerY + 20)
}

function handleDateRangeChange() {
  if (dateRangeType.value !== 'custom') {
    loadAllData()
  }
}

async function loadAllData() {
  await Promise.all([
    loadOverview(),
    loadTodayRevenue(),
    loadMonthRevenue(),
    loadTrendData(),
    loadChannelData(),
    loadBusinessData(),
    loadTopUsers(),
    loadAgentStats()
  ])
}

onMounted(() => {
  loadAllData()
  window.addEventListener('resize', () => {
    renderTrendChart()
    renderChannelChart()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', () => {
    renderTrendChart()
    renderChannelChart()
  })
})
</script>

<style lang="scss" scoped>
.revenue-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 20px;

      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .overview-cards {
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 28px;

        .el-icon {
          color: white;
        }
      }

      &.primary .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.success .stat-icon {
        background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
      }

      &.warning .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.info .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #333;
        }

        .stat-label {
          font-size: 14px;
          color: #666;
          margin-top: 4px;
        }

        .stat-sub {
          font-size: 12px;
          color: #999;
          margin-top: 2px;
        }

        .stat-trend {
          font-size: 12px;
          color: #67c23a;
          margin-top: 4px;
        }
      }
    }
  }

  .charts-row {
    display: flex;
    gap: 20px;
    margin-bottom: 24px;

    .chart-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      &.main-chart {
        flex: 2;
      }

      &.side-chart {
        flex: 1;
        min-width: 300px;
      }

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .chart-container {
        width: 100%;

        &.pie-chart {
          height: 200px;
        }
      }

      .chart-legend {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 12px;
        margin-top: 16px;

        .legend-item {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 13px;

          .dot {
            width: 10px;
            height: 10px;
            border-radius: 50%;
          }

          .label {
            flex: 1;
            color: #666;
          }

          .value {
            font-weight: 500;
            color: #333;
          }
        }
      }
    }
  }

  .data-row {
    display: flex;
    gap: 20px;
    margin-bottom: 24px;

    .data-card {
      flex: 1;
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .card-header {
        margin-bottom: 16px;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .money {
        color: #f56c6c;
        font-weight: 500;
      }
    }
  }

  .commission-row {
    .commission-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

      .card-header {
        margin-bottom: 20px;

        h3 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
        }
      }

      .commission-item {
        text-align: center;
        padding: 20px;
        background: #f8f9fa;
        border-radius: 8px;

        .comm-value {
          font-size: 28px;
          font-weight: 700;
          color: #333;
        }

        .comm-label {
          font-size: 14px;
          color: #666;
          margin-top: 8px;
        }
      }
    }
  }
}
</style>
