<template>
  <div class="usage-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">总调用次数</div>
            <div class="stat-value">{{ stats.totalCalls || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">总消费</div>
            <div class="stat-value">¥{{ formatAmount(stats.totalAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">输入Token</div>
            <div class="stat-value">{{ formatNumber(stats.totalInputTokens) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-label">输出Token</div>
            <div class="stat-value">{{ formatNumber(stats.totalOutputTokens) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>使用趋势</span>
          <el-radio-group v-model="period" size="small" @change="fetchTrend">
            <el-radio-button label="7">7天</el-radio-button>
            <el-radio-button label="30">30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div ref="trendChartRef" class="chart-container"></div>
    </el-card>
    
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>模型使用分布</span>
          </template>
          <div ref="modelChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>调用记录</span>
          </template>
          <el-table :data="recentLogs" size="small">
            <el-table-column prop="modelName" label="模型" width="150" />
            <el-table-column prop="createdAt" label="时间" width="160" />
            <el-table-column prop="requestTokens" label="输入" width="80" />
            <el-table-column prop="responseTokens" label="输出" width="80" />
            <el-table-column prop="billedAmount" label="费用" width="80">
              <template #default="{ row }">
                ¥{{ formatAmount(row.billedAmount) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsageStats, getDailyTrend, getModelRanking, getUsageLogs } from '@/api/usage'
import * as echarts from 'echarts'

const period = ref('7')
const trendChartRef = ref(null)
const modelChartRef = ref(null)

const stats = reactive({
  totalCalls: 0,
  totalAmount: 0,
  totalInputTokens: 0,
  totalOutputTokens: 0
})

const recentLogs = ref([])

function formatAmount(amount) {
  return ((amount || 0) / 100).toFixed(4)
}

function formatNumber(num) {
  if (!num) return '0'
  if (num >= 1000000) {
    return (num / 1000000).toFixed(1) + 'M'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'K'
  }
  return num
}

async function fetchStats() {
  try {
    const res = await getUsageStats({
      startTime: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString(),
      endTime: new Date().toISOString()
    })
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

async function fetchTrend() {
  // TODO: 获取趋势数据
}

async function fetchRecentLogs() {
  try {
    const res = await getUsageLogs({ page: 1, size: 10 })
    recentLogs.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to fetch logs:', error)
  }
}

onMounted(() => {
  fetchStats()
  fetchRecentLogs()
})
</script>

<style lang="scss" scoped>
.usage-container {
  padding: 20px;
}

.stat-card {
  .stat-content {
    text-align: center;
  }
  
  .stat-label {
    color: #909399;
    font-size: 14px;
    margin-bottom: 10px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
}
</style>
