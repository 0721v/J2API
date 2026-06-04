<template>
  <div class="agent-admin-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>代理商管理</h2>
      <el-button type="primary" @click="loadData">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="代理商总数" :value="stats.total" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="待审核" :value="stats.pending" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="今日新增" :value="stats.todayNew" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月佣金发放" :value="'¥' + formatMoney(stats.monthCommission)" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选表单 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部" clearable>
            <el-option label="待审核" value="pending" />
            <el-option label="正常" value="active" />
            <el-option label="暂停" value="suspended" />
            <el-option label="拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="filterForm.levelId" placeholder="全部" clearable>
            <el-option v-for="level in levels" :key="level.id" :label="level.name" :value="level.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 代理商列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="代理商信息" min-width="200">
          <template #default="{ row }">
            <div class="agent-info">
              <div class="agent-code">邀请码: {{ row.agentCode }}</div>
              <div class="agent-contact">
                {{ row.contactName || row.user?.username }} | {{ row.contactPhone || row.user?.email }}
              </div>
              <div v-if="row.companyName" class="company">{{ row.companyName }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="等级" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.level" type="success">{{ row.level.name }}</el-tag>
            <span v-else class="no-level">未分配</span>
          </template>
        </el-table-column>
        <el-table-column label="业绩" width="150">
          <template #default="{ row }">
            <div>用户: {{ row.totalUsers }}</div>
            <div>充值: ¥{{ formatMoney(row.totalRecharge) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="佣金" width="150">
          <template #default="{ row }">
            <div>累计: ¥{{ formatMoney(row.totalCommission) }}</div>
            <div>可用: ¥{{ formatMoney(row.availableCommission) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            {{ row.createdAt }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'pending'" type="success" link @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status === 'pending'" type="danger" link @click="handleReject(row)">拒绝</el-button>
            <el-button v-if="row.status === 'active'" type="warning" link @click="handleSuspend(row)">暂停</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="代理商详情" width="800px">
      <div v-if="currentAgent" class="agent-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="代理商ID">{{ currentAgent.id }}</el-descriptions-item>
          <el-descriptions-item label="邀请码">{{ currentAgent.agentCode }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ currentAgent.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ currentAgent.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="电子邮箱">{{ currentAgent.contactEmail }}</el-descriptions-item>
          <el-descriptions-item label="公司名称">{{ currentAgent.companyName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ currentAgent.idCard || '-' }}</el-descriptions-item>
          <el-descriptions-item label="当前等级">
            <el-tag v-if="currentAgent.level" type="success">{{ currentAgent.level.name }}</el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="下级用户">{{ currentAgent.totalUsers }}</el-descriptions-item>
          <el-descriptions-item label="累计充值">¥{{ formatMoney(currentAgent.totalRecharge) }}</el-descriptions-item>
          <el-descriptions-item label="累计佣金">¥{{ formatMoney(currentAgent.totalCommission) }}</el-descriptions-item>
          <el-descriptions-item label="可提现佣金">¥{{ formatMoney(currentAgent.availableCommission) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentAgent.status)">{{ getStatusText(currentAgent.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ currentAgent.createdAt }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ currentAgent.reviewedAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="拒绝原因" :span="2">{{ currentAgent.rejectReason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">收款信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="开户银行">{{ currentAgent.bankName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="银行账号">{{ currentAgent.bankAccount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付宝">{{ currentAgent.alipayAccount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="微信">{{ currentAgent.wechatAccount || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝申请" width="400px">
      <el-form ref="rejectFormRef" :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="processing">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  getAgentLevels,
  getAgentList,
  reviewAgent,
  getAgentDetail
} from '@/api/agent'

// 状态
const loading = ref(false)
const tableData = ref([])
const levels = ref([])
const stats = reactive({
  total: 0,
  pending: 0,
  todayNew: 0,
  monthCommission: 0
})
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  status: '',
  levelId: null
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentAgent = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const processing = ref(false)
const rejectFormRef = ref()
const rejectForm = reactive({
  reason: ''
})
const currentRejectId = ref(null)

// 方法
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return (amount / 100).toFixed(2)
}

const getStatusType = (status) => {
  const types = { pending: 'warning', active: 'success', suspended: 'info', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待审核', active: '正常', suspended: '已暂停', rejected: '已拒绝' }
  return texts[status] || status
}

const loadData = async () => {
  loading.value = true
  try {
    // 加载等级列表
    const levelsRes = await getAgentLevels()
    levels.value = levelsRes.data || []

    // 加载代理商列表
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      status: filterForm.status || undefined,
      levelId: filterForm.levelId || undefined
    }
    const listRes = await getAgentList(params)
    tableData.value = listRes.data?.list || listRes.data || []
    pagination.total = listRes.data?.total || 0

    // 统计
    stats.total = tableData.value.length
    stats.pending = tableData.value.filter(a => a.status === 'pending').length
    stats.todayNew = 0
    stats.monthCommission = 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  pagination.page = 1
  loadData()
}

const handleReset = () => {
  filterForm.status = ''
  filterForm.levelId = null
  handleFilter()
}

const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

const handleView = async (row) => {
  try {
    const res = await getAgentDetail(row.id)
    currentAgent.value = res.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定通过代理商 "${row.contactName || row.user?.username}" 的申请吗？`, '提示', {
      type: 'success'
    })
    await reviewAgent(row.id, { status: 'active' })
    ElMessage.success('已通过申请')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

const handleReject = (row) => {
  currentRejectId.value = row.id
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  processing.value = true
  try {
    await reviewAgent(currentRejectId.value, {
      status: 'rejected',
      rejectReason: rejectForm.reason
    })
    ElMessage.success('已拒绝申请')
    rejectDialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    processing.value = false
  }
}

const handleSuspend = async (row) => {
  const action = row.status === 'suspended' ? '恢复' : '暂停'
  try {
    await ElMessageBox.confirm(`确定${action}代理商 "${row.contactName || row.user?.username}" 吗？`, '提示', {
      type: 'warning'
    })
    await reviewAgent(row.id, {
      status: row.status === 'suspended' ? 'active' : 'suspended'
    })
    ElMessage.success(`已${action}`)
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.agent-admin-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.filter-card, .table-card {
  margin-bottom: 20px;
}

.agent-info {
  font-size: 13px;
}

.agent-code {
  font-family: monospace;
  color: #409EFF;
}

.agent-contact {
  color: #666;
}

.company {
  color: #999;
  font-size: 12px;
}

.no-level {
  color: #999;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
