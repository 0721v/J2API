<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 余额卡片 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon balance">
            <el-icon><Coin /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">账户余额</div>
            <div class="stat-value">¥{{ formatBalance(userStore.balance) }}</div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 今日使用 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon usage">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">今日使用</div>
            <div class="stat-value">{{ stats.todayUsage || 0 }} 次</div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 本月消费 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon cost">
            <el-icon><Money /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">本月消费</div>
            <div class="stat-value">¥{{ formatBalance(stats.monthlyCost) }}</div>
          </div>
        </el-card>
      </el-col>
      
      <!-- API密钥 -->
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-icon keys">
            <el-icon><Key /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">API密钥</div>
            <div class="stat-value">{{ stats.tokenCount || 0 }} 个</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 快捷操作 -->
    <el-card class="quick-actions">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      <div class="action-buttons">
        <el-button type="primary" @click="$router.push('/tokens/create')">
          <el-icon><Plus /></el-icon> 创建API密钥
        </el-button>
        <el-button type="success" @click="$router.push('/recharge')">
          <el-icon><Coin /></el-icon> 余额充值
        </el-button>
        <el-button @click="$router.push('/docs')">
          <el-icon><Document /></el-icon> API文档
        </el-button>
        <el-button @click="$router.push('/chat')">
          <el-icon><ChatDotRound /></el-icon> AI对话
        </el-button>
      </div>
    </el-card>
    
    <!-- 使用趋势 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>使用趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button label="7">7天</el-radio-button>
                <el-radio-button label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>模型使用排行</span>
            </div>
          </template>
          <div class="ranking-list">
            <div v-for="(item, index) in modelRanking" :key="index" class="ranking-item">
              <span class="rank">{{ index + 1 }}</span>
              <span class="model">{{ item.model }}</span>
              <span class="count">{{ item.count }}次</span>
            </div>
            <el-empty v-if="!modelRanking.length" description="暂无数据" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserStats } from '@/api/user'
import * as echarts from 'echarts'

const userStore = useUserStore()

const trendPeriod = ref('7')
const trendChartRef = ref(null)
const stats = reactive({
  todayUsage: 0,
  monthlyCost: 0,
  tokenCount: 0
})
const modelRanking = ref([])

function formatBalance(balance) {
  return ((balance || 0) / 100).toFixed(2)
}

async function fetchStats() {
  try {
    const res = await getUserStats()
    Object.assign(stats, res.data)
    modelRanking.value = res.data.modelRanking || []
    renderTrendChart(res.data.trend || [])
  } catch (error) {
    console.error('Failed to fetch stats:', error)
  }
}

function renderTrendChart(data) {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date)
    },
    yAxis: { type: 'value' },
    series: [{
      data: data.map(d => d.count),
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])
      },
      lineStyle: { color: '#409eff' },
      itemStyle: { color: '#409eff' }
    }]
  }
  chart.setOption(option)
}

onMounted(() => {
  fetchStats()
  window.addEventListener('resize', () => {
    echarts.getInstanceByDom(trendChartRef.value)?.resize()
  })
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.stat-card {
  .el-card__body {
    display: flex;
    align-items: center;
    gap: 20px;
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  
  &.balance { background: linear-gradient(135deg, #667eea, #764ba2); }
  &.usage { background: linear-gradient(135deg, #11998e, #38ef7d); }
  &.cost { background: linear-gradient(135deg, #fc4a1a, #f7b733); }
  &.keys { background: linear-gradient(135deg, #4facfe, #00f2fe); }
}

.stat-content {
  .stat-label {
    color: #909399;
    font-size: 14px;
    margin-bottom: 8px;
  }
  
  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #303133;
  }
}

.quick-actions {
  margin: 20px 0;
}

.action-buttons {
  display: flex;
  gap: 10px;
  
  .el-button {
    flex: 1;
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

.ranking-list {
  .ranking-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child { border-bottom: none; }
    
    .rank {
      width: 24px;
      height: 24px;
      background: #f0f0f0;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      margin-right: 12px;
      
      &:nth-child(1) { background: #ffd700; color: white; }
      &:nth-child(2) { background: #c0c0c0; color: white; }
      &:nth-child(3) { background: #cd7f32; color: white; }
    }
    
    .model {
      flex: 1;
      color: #303133;
    }
    
    .count {
      color: #909399;
      font-size: 14px;
    }
  }
}
</style>
