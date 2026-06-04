<template>
  <div class="report-page">
    <div class="page-header">
      <h2>我的报表</h2>
      <div class="date-range-picker">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" @click="loadAllData">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card today">
            <div class="stat-icon"><el-icon><Coin /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(statsData.today?.cost || 0) }}</div>
              <div class="stat-label">今日消费</div>
              <div class="stat-sub">{{ statsData.today?.totalCalls || 0 }} 次调用</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card month">
            <div class="stat-icon"><el-icon><Calendar /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(statsData.month?.cost || 0) }}</div>
              <div class="stat-label">本月消费</div>
              <div class="stat-sub">{{ formatNumber(statsData.month?.recharge || 0) }} 充值</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card total">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(statsData.overall?.totalCost || 0) }}</div>
              <div class="stat-label">累计消费</div>
              <div class="stat-sub">{{ formatNumber(statsData.overall?.totalRecharge || 0) }} 总充值</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card balance">
            <div class="stat-icon"><el-icon><Wallet /></el-icon></div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(statsData.overall?.balance || 0) }}</div>
              <div class="stat-label">账户余额</div>
              <div class="stat-sub">当前可用</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 消费趋势图 -->
    <div class="chart-section">
      <div class="section-title">
        <h3>消费趋势</h3>
        <div class="chart-legend">
          <span class="legend-item expense"><span class="dot"></span>消费</span>
          <span class="legend-item income"><span class="dot"></span>收入/充值</span>
        </div>
      </div>
      <div class="chart-container" ref="trendChartRef"></div>
    </div>

    <!-- 消费记录 -->
    <div class="records-section">
      <div class="section-title">
        <h3>消费记录</h3>
        <el-radio-group v-model="recordType" size="small" @change="loadRecords">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="api_call">API调用</el-radio-button>
          <el-radio-button label="recharge">充值</el-radio-button>
          <el-radio-button label="reward">奖励</el-radio-button>
          <el-radio-button label="refund">退款</el-radio-button>
        </el-radio-group>
      </div>
      <el-table :data="records" v-loading="recordsLoading" stripe>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getRecordTypeTag(row.type)" size="small">{{ row.typeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :class="row.isIncome ? 'income-text' : 'expense-text'">
              {{ row.isIncome ? '+' : '-' }}{{ formatNumber(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="余额变动" width="150">
          <template #default="{ row }">
            {{ formatNumber(row.balanceBefore) }} → {{ formatNumber(row.balanceAfter) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="recordsPage"
          :page-size="recordsPageSize"
          :total="recordsTotal"
          layout="total, prev, pager, next"
          @current-change="loadRecords"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import {
  getTodayStats,
  getMonthStats,
  getOverallStats,
  getConsumptionTrend,
  getConsumptionRecords,
  getConsumptionSummary
} from '@/api/announcement'
import { ElMessage } from 'element-plus'
import { Coin, Calendar, TrendCharts, Wallet } from '@element-plus/icons-vue'

const dateRange = ref([])
const statsData = reactive({
  today: {},
  month: {},
  overall: {}
})
const trendData = ref([])
const records = ref([])
const recordsLoading = ref(false)
const recordsPage = ref(1)
const recordsPageSize = ref(20)
const recordsTotal = ref(0)
const recordType = ref('')
const trendChartRef = ref(null)

function formatNumber(num) {
  if (num === null || num === undefined) return '0.00'
  return Number(num).toFixed(2)
}

function formatDateTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

function getRecordTypeTag(type) {
  const map = {
    api_call: 'primary',
    recharge: 'success',
    reward: 'warning',
    refund: 'info',
    adjustment: ''
  }
  return map[type] || ''
}

function getDefaultDateRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  return [
    start.toISOString().split('T')[0],
    end.toISOString().split('T')[0]
  ]
}

function handleDateChange() {
  loadAllData()
}

async function loadTodayStats() {
  try {
    const res = await getTodayStats()
    if (res.code === 200) {
      statsData.today = res.data
    }
  } catch (error) {
    console.error('Failed to load today stats:', error)
  }
}

async function loadMonthStats() {
  try {
    const res = await getMonthStats()
    if (res.code === 200) {
      statsData.month = res.data
    }
  } catch (error) {
    console.error('Failed to load month stats:', error)
  }
}

async function loadOverallStats() {
  try {
    const res = await getOverallStats()
    if (res.code === 200) {
      statsData.overall = res.data
    }
  } catch (error) {
    console.error('Failed to load overall stats:', error)
  }
}

async function loadTrendData() {
  try {
    const [startDate, endDate] = dateRange.value || getDefaultDateRange()
    const res = await getConsumptionTrend({ startDate, endDate })
    if (res.code === 200) {
      trendData.value = res.data || []
      renderTrendChart()
    }
  } catch (error) {
    console.error('Failed to load trend data:', error)
  }
}

async function loadRecords() {
  recordsLoading.value = true
  try {
    const [startDate, endDate] = dateRange.value || getDefaultDateRange()
    const res = await getConsumptionRecords({
      startDate,
      endDate,
      type: recordType.value,
      page: recordsPage.value,
      pageSize: recordsPageSize.value
    })
    if (res.code === 200) {
      records.value = res.data.records
      recordsTotal.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to load records:', error)
  } finally {
    recordsLoading.value = false
  }
}

function renderTrendChart() {
  if (!trendChartRef.value || trendData.value.length === 0) return

  // 简单的Canvas图表实现
  const canvas = trendChartRef.value
  const ctx = canvas.getContext('2d')
  const width = canvas.width = canvas.offsetWidth
  const height = canvas.height = 300

  ctx.clearRect(0, 0, width, height)

  const padding = { top: 20, right: 20, bottom: 40, left: 60 }
  const chartWidth = width - padding.left - padding.right
  const chartHeight = height - padding.top - padding.bottom

  // 数据处理
  const data = trendData.value.map(d => ({
    date: d.date,
    expense: Number(d.expense || 0),
    income: Number(d.income || 0)
  }))

  if (data.length === 0) return

  const maxValue = Math.max(...data.flatMap(d => [d.expense, d.income]), 1)
  const stepX = chartWidth / (data.length - 1 || 1)

  // 绘制网格线
  ctx.strokeStyle = '#f0f0f0'
  ctx.lineWidth = 1
  for (let i = 0; i <= 5; i++) {
    const y = padding.top + (chartHeight / 5) * i
    ctx.beginPath()
    ctx.moveTo(padding.left, y)
    ctx.lineTo(width - padding.right, y)
    ctx.stroke()

    // Y轴标签
    ctx.fillStyle = '#999'
    ctx.font = '12px sans-serif'
    ctx.textAlign = 'right'
    const value = (maxValue * (5 - i) / 5).toFixed(0)
    ctx.fillText(value, padding.left - 10, y + 4)
  }

  // 绘制X轴标签
  ctx.textAlign = 'center'
  const step = Math.ceil(data.length / 10)
  data.forEach((d, i) => {
    if (i % step === 0 || i === data.length - 1) {
      const x = padding.left + stepX * i
      ctx.fillText(String(d.date).slice(5), x, height - padding.bottom + 20)
    }
  })

  // 绘制消费线（蓝色）
  ctx.strokeStyle = '#409eff'
  ctx.lineWidth = 2
  ctx.beginPath()
  data.forEach((d, i) => {
    const x = padding.left + stepX * i
    const y = padding.top + chartHeight - (d.expense / maxValue) * chartHeight
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()

  // 绘制收入线（绿色）
  ctx.strokeStyle = '#67c23a'
  ctx.beginPath()
  data.forEach((d, i) => {
    const x = padding.left + stepX * i
    const y = padding.top + chartHeight - (d.income / maxValue) * chartHeight
    if (i === 0) ctx.moveTo(x, y)
    else ctx.lineTo(x, y)
  })
  ctx.stroke()
}

async function loadAllData() {
  await Promise.all([
    loadTodayStats(),
    loadMonthStats(),
    loadOverallStats(),
    loadTrendData(),
    loadRecords()
  ])
}

onMounted(() => {
  dateRange.value = getDefaultDateRange()
  loadAllData()

  // 监听窗口大小变化
  window.addEventListener('resize', renderTrendChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', renderTrendChart)
})
</script>

<style lang="scss" scoped>
.report-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }

    .date-range-picker {
      display: flex;
      gap: 12px;
    }
  }

  .stats-cards {
    margin-bottom: 24px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
      }

      &.today .stat-icon {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.month .stat-icon {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }

      &.total .stat-icon {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
        color: white;
      }

      &.balance .stat-icon {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
        color: white;
      }

      .stat-content {
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
      }
    }
  }

  .chart-section {
    background: white;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }

      .chart-legend {
        display: flex;
        gap: 20px;

        .legend-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: #666;

          .dot {
            width: 12px;
            height: 3px;
            border-radius: 2px;
          }

          &.expense .dot {
            background: #409eff;
          }

          &.income .dot {
            background: #67c23a;
          }
        }
      }
    }

    .chart-container {
      width: 100%;
      height: 300px;
      background: #fafafa;
      border-radius: 8px;
    }
  }

  .records-section {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
      }
    }

    .income-text {
      color: #67c23a;
      font-weight: 500;
    }

    .expense-text {
      color: #f56c6c;
      font-weight: 500;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }
  }
}
</style>
