<template>
  <div class="agent-center">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>代理商中心</h2>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0">
          <el-button @click="showNotifications = true">
            <el-icon><Bell /></el-icon>
            通知
          </el-button>
        </el-badge>
        <el-button v-if="!isAgent && !hasApplication" type="primary" @click="showApplyDialog = true">
          申请成为代理商
        </el-button>
      </div>
    </div>

    <!-- 非代理商提示 -->
    <el-alert v-if="!isAgent" type="info" :closable="false" class="apply-tip">
      <template #title>
        <div class="apply-tip-content">
          <span>成为代理商，享受丰厚佣金回报！推荐用户充值即可获得5%-18%佣金奖励。</span>
          <el-button type="primary" size="small" @click="showApplyDialog = true">
            立即申请
          </el-button>
        </div>
      </template>
    </el-alert>

    <!-- 待审核提示 -->
    <el-alert v-if="applicationStatus === 'pending'" type="warning" :closable="false" class="apply-tip">
      <template #title>
        <div class="apply-tip-content">
          <span>您的代理商申请正在审核中，请耐心等待...</span>
        </div>
      </template>
    </el-alert>

    <!-- 代理商信息卡片 -->
    <div v-if="isAgent" class="agent-info-card">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">我的邀请码</div>
            <div class="stat-value code">{{ agentInfo?.agentCode }}</div>
            <el-button type="primary" size="small" @click="copyCode">复制邀请码</el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">当前等级</div>
            <div class="stat-value level">{{ agentInfo?.level?.name || '暂无等级' }}</div>
            <div class="stat-sub">{{ agentInfo?.level?.description || '' }}</div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">可提现佣金</div>
            <div class="stat-value money">¥{{ formatMoney(stats?.availableCommission) }}</div>
            <el-button type="success" size="small" @click="showWithdrawDialog = true" 
                       :disabled="!stats?.availableCommission || stats.availableCommission <= 0">
              立即提现
            </el-button>
          </div>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-label">累计佣金</div>
            <div class="stat-value">¥{{ formatMoney(stats?.totalCommission) }}</div>
            <div class="stat-sub">冻结: ¥{{ formatMoney(stats?.frozenCommission) }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 邀请分享区域 -->
    <div v-if="isAgent" class="invite-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>邀请分享</span>
            <el-button type="text" @click="loadInviteStats">刷新</el-button>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :xs="24" :md="16">
            <div class="invite-link-box">
              <div class="invite-url">
                <span class="label">邀请链接:</span>
                <span class="url">{{ inviteLink || '正在生成...' }}</span>
              </div>
              <div class="invite-actions">
                <el-button type="primary" @click="copyInviteLink">
                  <el-icon><CopyDocument /></el-icon>
                  复制链接
                </el-button>
                <el-button @click="shareToWechat">
                  <el-icon><Share /></el-icon>
                  分享微信
                </el-button>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :md="8">
            <div class="invite-stats">
              <div class="invite-stat-item">
                <span class="label">累计邀请</span>
                <span class="value">{{ inviteStats?.totalInvites || 0 }} 人</span>
              </div>
              <div class="invite-stat-item">
                <span class="label">直属用户</span>
                <span class="value">{{ inviteStats?.directInvites || 0 }} 人</span>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 数据统计 -->
    <el-row v-if="isAgent" :gutter="20" class="stats-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon users"><i class="el-icon-user"></i></div>
            <div class="stat-content">
              <div class="stat-number">{{ stats?.totalUsers || 0 }}</div>
              <div class="stat-title">下级用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon recharge"><i class="el-icon-money"></i></div>
            <div class="stat-content">
              <div class="stat-number">¥{{ formatMoney(stats?.totalRecharge) }}</div>
              <div class="stat-title">累计充值</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon direct"><i class="el-icon-s-custom"></i></div>
            <div class="stat-content">
              <div class="stat-number">{{ stats?.directUsers || 0 }}</div>
              <div class="stat-title">直属用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon rate"><i class="el-icon-s-data"></i></div>
            <div class="stat-content">
              <div class="stat-number">{{ ((commissionRate || 0.05) * 100).toFixed(0) }}%</div>
              <div class="stat-title">佣金比例</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Tabs 切换 -->
    <el-card v-if="isAgent" class="tabs-card">
      <el-tabs v-model="activeTab">
        <!-- 等级权益 -->
        <el-tab-pane label="等级权益" name="levels">
          <el-row :gutter="20">
            <el-col v-for="level in levels" :key="level.id" :xs="24" :sm="12" :md="6">
              <div class="level-item" :class="{ active: level.id === agentInfo?.levelId }">
                <div class="level-name">{{ level.name }}</div>
                <div class="level-rate">{{ ((level.commissionRate || 0) * 100).toFixed(0) }}% 佣金</div>
                <div class="level-conditions">
                  <div v-if="level.minUsers">下级用户: {{ level.minUsers }}+</div>
                  <div v-if="level.minRecharge">累计充值: ¥{{ formatMoney(level.minRecharge) }}+</div>
                </div>
                <div class="level-benefits">
                  <el-tag v-if="level.subAgentEnabled" type="success" size="small">可发展代理</el-tag>
                  <el-tag v-if="(level.priceDiscount || 1) < 1" type="warning" size="small">
                    {{ ((level.priceDiscount || 1) * 10).toFixed(1) }}折购买
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 佣金记录 -->
        <el-tab-pane label="佣金记录" name="commissions">
          <div class="filter-bar">
            <el-select v-model="commissionType" placeholder="类型筛选" clearable size="small" 
                       @change="loadCommissions" style="width: 120px;">
              <el-option label="充值佣金" value="recharge" />
              <el-option label="套餐佣金" value="package" />
              <el-option label="升级奖励" value="upgrade" />
            </el-select>
          </div>
          
          <el-table :data="commissions" v-loading="loadingCommissions" stripe>
            <el-table-column prop="type" label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getCommissionTypeColor(row.type)" size="small">
                  {{ getCommissionTypeText(row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="orderAmount" label="订单金额" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.orderAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="rate" label="佣金比例" width="100">
              <template #default="{ row }">
                {{ (((row.rate || 0) * 100)).toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="佣金金额" width="120">
              <template #default="{ row }">
                <span class="commission-amount">+¥{{ formatMoney(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200" />
            <el-table-column prop="createdAt" label="时间" width="180" />
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="commissionPage"
              v-model:page-size="commissionPageSize"
              :total="commissionTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, prev, pager, next"
              @current-change="loadCommissions"
            />
          </div>
        </el-tab-pane>

        <!-- 下级用户 -->
        <el-tab-pane label="下级用户" name="subusers">
          <el-table :data="subUsers" v-loading="loadingSubUsers" stripe>
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="totalRecharge" label="累计充值" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.totalRecharge) }}
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册时间" width="180" />
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="subUsersPage"
              v-model:page-size="subUsersPageSize"
              :total="subUsersTotal"
              layout="total, prev, pager, next"
              @current-change="loadSubUsers"
            />
          </div>
        </el-tab-pane>

        <!-- 提现记录 -->
        <el-tab-pane label="提现记录" name="withdrawals">
          <div class="filter-bar">
            <el-select v-model="withdrawalStatus" placeholder="状态筛选" clearable size="small"
                       @change="loadWithdrawals" style="width: 120px;">
              <el-option label="待处理" value="pending" />
              <el-option label="已完成" value="completed" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
          </div>
          
          <el-table :data="withdrawals" v-loading="loadingWithdrawals" stripe>
            <el-table-column prop="withdrawalNo" label="单号" width="180" />
            <el-table-column prop="method" label="方式" width="100">
              <template #default="{ row }">
                {{ getMethodText(row.method) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="申请金额" width="120">
              <template #default="{ row }">
                ¥{{ formatMoney(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="fee" label="手续费" width="100">
              <template #default="{ row }">
                ¥{{ formatMoney(row.fee) }}
              </template>
            </el-table-column>
            <el-table-column prop="actualAmount" label="实际到账" width="120">
              <template #default="{ row }">
                <strong>¥{{ formatMoney(row.actualAmount) }}</strong>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="申请时间" width="180" />
          </el-table>

          <div class="pagination">
            <el-pagination
              v-model:current-page="withdrawalPage"
              v-model:page-size="withdrawalPageSize"
              :total="withdrawalTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, prev, pager, next"
              @current-change="loadWithdrawals"
            />
          </div>
        </el-tab-pane>

        <!-- 佣金趋势 -->
        <el-tab-pane label="佣金趋势" name="trend">
          <div class="trend-filters">
            <el-radio-group v-model="trendDays" @change="loadTrend">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
              <el-radio-button :label="90">近90天</el-radio-button>
            </el-radio-group>
          </div>
          <div class="trend-chart">
            <div class="trend-summary">
              <div class="summary-item">
                <span class="label">累计佣金</span>
                <span class="value">¥{{ formatMoney(trendSummary.total) }}</span>
              </div>
              <div class="summary-item">
                <span class="label">日均佣金</span>
                <span class="value">¥{{ formatMoney(trendSummary.avg) }}</span>
              </div>
              <div class="summary-item">
                <span class="label">最高单日</span>
                <span class="value">¥{{ formatMoney(trendSummary.max) }}</span>
              </div>
            </div>
            <div class="trend-list">
              <div v-for="item in trendData" :key="item.date" class="trend-item">
                <span class="date">{{ item.date }}</span>
                <div class="bar-container">
                  <div class="bar" :style="{ width: getBarWidth(item.amount) + '%' }"></div>
                </div>
                <span class="amount">¥{{ formatMoney(item.amount) }}</span>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 通知对话框 -->
    <el-dialog v-model="showNotifications" title="消息通知" width="600px">
      <div class="notification-actions">
        <el-button type="text" @click="handleMarkAllRead" :disabled="unreadCount === 0">
          全部标为已读
        </el-button>
      </div>
      <div class="notification-list">
        <div v-for="item in notifications" :key="item.id" 
             class="notification-item" 
             :class="{ unread: item.status === 'unread' }"
             @click="handleReadNotification(item)">
          <div class="notification-header">
            <span class="notification-title">{{ item.title }}</span>
            <span class="notification-time">{{ item.createdAt }}</span>
          </div>
          <div class="notification-content">{{ item.content }}</div>
        </div>
        <el-empty v-if="notifications.length === 0" description="暂无通知" />
      </div>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog v-model="showApplyDialog" title="申请成为代理商" width="600px" :close-on-click-modal="false">
      <el-form ref="applyFormRef" :model="applyForm" :rules="applyRules" label-width="100px">
        <el-form-item label="邀请码" prop="inviteCode">
          <el-input v-model="applyForm.inviteCode" placeholder="可选，填入邀请码可绑定上级代理商" />
        </el-form-item>
        <el-divider content-position="left">联系信息</el-divider>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="applyForm.companyName" placeholder="个人用户可不填" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactName">
          <el-input v-model="applyForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="applyForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="电子邮箱" prop="contactEmail">
          <el-input v-model="applyForm.contactEmail" placeholder="请输入电子邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleApply" :loading="applying">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 提现对话框 -->
    <el-dialog v-model="showWithdrawDialog" title="申请提现" width="500px">
      <el-form ref="withdrawFormRef" :model="withdrawForm" :rules="withdrawRules" label-width="100px">
        <el-form-item label="可提现金额">
          <span class="amount-value">¥{{ formatMoney(stats?.availableCommission) }}</span>
        </el-form-item>
        <el-form-item label="提现金额" prop="amount">
          <el-input-number v-model="withdrawForm.amount" :min="100" :max="(stats?.availableCommission || 0) / 100" :step="10" />
          <span class="amount-tip">最低提现100元</span>
        </el-form-item>
        <el-form-item label="提现方式" prop="method">
          <el-radio-group v-model="withdrawForm.method">
            <el-radio label="bank">银行卡</el-radio>
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="withdrawForm.method === 'bank'">
          <el-form-item label="开户银行" prop="bankName">
            <el-input v-model="withdrawForm.bankName" placeholder="请输入开户银行" />
          </el-form-item>
          <el-form-item label="银行账号" prop="bankAccount">
            <el-input v-model="withdrawForm.bankAccount" placeholder="请输入银行卡号" />
          </el-form-item>
          <el-form-item label="支行名称" prop="bankBranch">
            <el-input v-model="withdrawForm.bankBranch" placeholder="请输入支行名称" />
          </el-form-item>
        </template>
        <template v-else-if="withdrawForm.method === 'alipay'">
          <el-form-item label="支付宝账号" prop="alipayAccount">
            <el-input v-model="withdrawForm.alipayAccount" placeholder="请输入支付宝账号" />
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="微信账号" prop="wechatAccount">
            <el-input v-model="withdrawForm.wechatAccount" placeholder="请输入微信账号" />
          </el-form-item>
        </template>
        <el-form-item label="手续费">
          <span class="fee-value">¥{{ calculateFee }} (1%)</span>
        </el-form-item>
        <el-form-item label="实际到账">
          <span class="actual-value">¥{{ calculateActual }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWithdrawDialog = false">取消</el-button>
        <el-button type="primary" @click="handleWithdraw" :loading="withdrawing">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, CopyDocument, Share } from '@element-plus/icons-vue'
import {
  getAgentLevels,
  checkAgent,
  getAgentInfo,
  getAgentStats,
  applyAgent,
  getCommissions,
  applyWithdrawal,
  getWithdrawals,
  getCommissionRate,
  getInviteLink,
  getInviteStats,
  getSubUsers,
  getCommissionTrend,
  getNotifications,
  getUnreadCount,
  markAsRead,
  markAllAsRead
} from '@/api/agent'

// 状态
const isAgent = ref(false)
const hasApplication = ref(false)
const applicationStatus = ref('none')
const agentInfo = ref(null)
const stats = ref(null)
const levels = ref([])
const commissionRate = ref(0.05)
const unreadCount = ref(0)
const inviteLink = ref('')
const inviteStats = ref(null)

// Tabs
const activeTab = ref('levels')

// 佣金记录
const commissions = ref([])
const loadingCommissions = ref(false)
const commissionPage = ref(1)
const commissionPageSize = ref(10)
const commissionTotal = ref(0)
const commissionType = ref('')

// 下级用户
const subUsers = ref([])
const loadingSubUsers = ref(false)
const subUsersPage = ref(1)
const subUsersPageSize = ref(10)
const subUsersTotal = ref(0)

// 提现记录
const withdrawals = ref([])
const loadingWithdrawals = ref(false)
const withdrawalPage = ref(1)
const withdrawalPageSize = ref(10)
const withdrawalTotal = ref(0)
const withdrawalStatus = ref('')

// 趋势
const trendDays = ref(30)
const trendData = ref([])
const trendSummary = ref({ total: 0, avg: 0, max: 0 })

// 通知
const showNotifications = ref(false)
const notifications = ref([])

// 申请对话框
const showApplyDialog = ref(false)
const applying = ref(false)
const applyFormRef = ref()
const applyForm = reactive({
  inviteCode: '',
  companyName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: ''
})

// 提现对话框
const showWithdrawDialog = ref(false)
const withdrawing = ref(false)
const withdrawFormRef = ref()
const withdrawForm = reactive({
  amount: 100,
  method: 'bank',
  bankName: '',
  bankAccount: '',
  bankBranch: '',
  alipayAccount: '',
  wechatAccount: ''
})

// 表单验证
const applyRules = {
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }]
}

const withdrawRules = {
  amount: [{ required: true, message: '请输入提现金额', trigger: 'blur' }],
  method: [{ required: true, message: '请选择提现方式', trigger: 'change' }]
}

// 计算属性
const calculateFee = computed(() => ((withdrawForm.amount || 0) * 0.01).toFixed(2))
const calculateActual = computed(() => ((withdrawForm.amount || 0) * 0.99).toFixed(2))

// 方法
const formatMoney = (amount) => {
  if (!amount) return '0.00'
  return (amount / 100).toFixed(2)
}

const copyCode = () => {
  if (agentInfo.value?.agentCode) {
    navigator.clipboard.writeText(agentInfo.value.agentCode)
    ElMessage.success('邀请码已复制')
  }
}

const copyInviteLink = () => {
  if (inviteLink.value) {
    navigator.clipboard.writeText(inviteLink.value)
    ElMessage.success('邀请链接已复制')
  }
}

const shareToWechat = () => {
  ElMessage.info('请复制链接到微信分享')
}

const getCommissionTypeColor = (type) => {
  const colors = { recharge: 'success', package: 'warning', upgrade: 'primary', bonus: 'info' }
  return colors[type] || 'info'
}

const getCommissionTypeText = (type) => {
  const texts = { recharge: '充值佣金', package: '套餐佣金', upgrade: '升级奖励', bonus: '推荐奖励' }
  return texts[type] || type
}

const getMethodText = (method) => {
  const texts = { bank: '银行卡', alipay: '支付宝', wechat: '微信' }
  return texts[method] || method
}

const getStatusType = (status) => {
  const types = { pending: 'warning', processing: 'primary', completed: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待处理', processing: '处理中', completed: '已完成', rejected: '已拒绝' }
  return texts[status] || status
}

const getBarWidth = (amount) => {
  if (!trendSummary.value.max) return 0
  return Math.min((amount / trendSummary.value.max) * 100, 100)
}

const loadData = async () => {
  try {
    // 检查代理商状态
    const checkRes = await checkAgent()
    isAgent.value = checkRes.data?.isAgent || false
    hasApplication.value = checkRes.data?.hasApplication || false
    applicationStatus.value = checkRes.data?.status || 'none'

    // 加载等级列表
    const levelsRes = await getAgentLevels()
    levels.value = levelsRes.data || []

    // 如果是代理商，加载详细信息
    if (isAgent.value) {
      const infoRes = await getAgentInfo()
      agentInfo.value = infoRes.data

      const statsRes = await getAgentStats()
      stats.value = statsRes.data

      const rateRes = await getCommissionRate()
      commissionRate.value = rateRes.data || 0.05

      // 加载未读通知数
      loadUnreadCount()

      // 加载邀请链接
      loadInviteLink()
      
      // 加载邀请统计
      loadInviteStats()

      // 加载佣金记录
      loadCommissions()

      // 加载下级用户
      loadSubUsers()

      // 加载提现记录
      loadWithdrawals()

      // 加载佣金趋势
      loadTrend()
    }
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data?.count || 0
  } catch (error) {
    console.error('加载未读数失败', error)
  }
}

const loadInviteLink = async () => {
  try {
    const res = await getInviteLink()
    inviteLink.value = res.data?.link || ''
  } catch (error) {
    console.error('加载邀请链接失败', error)
  }
}

const loadInviteStats = async () => {
  try {
    const res = await getInviteStats()
    inviteStats.value = res.data
  } catch (error) {
    console.error('加载邀请统计失败', error)
  }
}

const loadCommissions = async () => {
  loadingCommissions.value = true
  try {
    const res = await getCommissions({
      page: commissionPage.value,
      pageSize: commissionPageSize.value,
      type: commissionType.value || undefined
    })
    commissions.value = res.data?.list || res.data || []
    commissionTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载佣金记录失败')
  } finally {
    loadingCommissions.value = false
  }
}

const loadSubUsers = async () => {
  loadingSubUsers.value = true
  try {
    const res = await getSubUsers({
      page: subUsersPage.value,
      pageSize: subUsersPageSize.value
    })
    subUsers.value = res.data?.list || res.data || []
    subUsersTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载下级用户失败')
  } finally {
    loadingSubUsers.value = false
  }
}

const loadWithdrawals = async () => {
  loadingWithdrawals.value = true
  try {
    const res = await getWithdrawals({
      page: withdrawalPage.value,
      pageSize: withdrawalPageSize.value,
      status: withdrawalStatus.value || undefined
    })
    withdrawals.value = res.data?.list || res.data || []
    withdrawalTotal.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载提现记录失败')
  } finally {
    loadingWithdrawals.value = false
  }
}

const loadTrend = async () => {
  try {
    const res = await getCommissionTrend({ days: trendDays.value })
    trendData.value = res.data || []
    
    // 计算汇总
    let total = 0, max = 0
    trendData.value.forEach(item => {
      total += item.amount || 0
      max = Math.max(max, item.amount || 0)
    })
    trendSummary.value = {
      total,
      avg: trendData.value.length > 0 ? total / trendData.value.length : 0,
      max
    }
  } catch (error) {
    console.error('加载趋势数据失败', error)
  }
}

const loadNotifications = async () => {
  try {
    const res = await getNotifications({ page: 1, pageSize: 50 })
    notifications.value = res.data?.list || res.data || []
  } catch (error) {
    console.error('加载通知失败', error)
  }
}

const handleReadNotification = async (item) => {
  if (item.status === 'unread') {
    await markAsRead(item.id)
    item.status = 'read'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
}

const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.status = 'read')
    unreadCount.value = 0
    ElMessage.success('已全部标为已读')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleApply = async () => {
  try {
    await applyFormRef.value.validate()
    applying.value = true
    await applyAgent(applyForm)
    ElMessage.success('申请已提交，请等待审核')
    showApplyDialog.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    applying.value = false
  }
}

const handleWithdraw = async () => {
  try {
    await withdrawFormRef.value.validate()
    withdrawing.value = true

    const accountInfo = {}
    if (withdrawForm.method === 'bank') {
      accountInfo.bankName = withdrawForm.bankName
      accountInfo.bankAccount = withdrawForm.bankAccount
      accountInfo.bankBranch = withdrawForm.bankBranch
    } else if (withdrawForm.method === 'alipay') {
      accountInfo.alipayAccount = withdrawForm.alipayAccount
    } else {
      accountInfo.wechatAccount = withdrawForm.wechatAccount
    }

    await applyWithdrawal({
      amount: withdrawForm.amount * 100,
      method: withdrawForm.method,
      accountInfo
    })

    ElMessage.success('提现申请已提交')
    showWithdrawDialog.value = false
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '提交失败')
    }
  } finally {
    withdrawing.value = false
  }
}

// 监听通知对话框打开
const handleShowNotifications = () => {
  showNotifications.value = true
  loadNotifications()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.agent-center {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.apply-tip {
  margin-bottom: 20px;
}

.apply-tip-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.agent-info-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 30px;
  color: #fff;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
  padding: 10px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-value.code {
  font-family: monospace;
  font-size: 18px;
  letter-spacing: 2px;
}

.stat-value.level {
  color: #ffd700;
}

.stat-value.money {
  color: #90EE90;
}

.stat-sub {
  font-size: 12px;
  opacity: 0.8;
}

.invite-section {
  margin-bottom: 20px;
}

.invite-link-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.invite-url {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #f5f7fa;
  padding: 10px 15px;
  border-radius: 4px;
}

.invite-url .label {
  font-weight: bold;
  white-space: nowrap;
}

.invite-url .url {
  color: #409EFF;
  word-break: break-all;
}

.invite-actions {
  display: flex;
  gap: 10px;
}

.invite-stats {
  display: flex;
  flex-direction: column;
  gap: 15px;
  justify-content: center;
  height: 100%;
}

.invite-stat-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.invite-stat-item .label {
  color: #666;
}

.invite-stat-item .value {
  font-weight: bold;
  color: #409EFF;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  margin-right: 15px;
}

.stat-icon.users { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.recharge { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.direct { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.rate { background: linear-gradient(135deg, #43e97b, #38f9d7); }

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 22px;
  font-weight: bold;
  color: #333;
}

.stat-title {
  font-size: 12px;
  color: #999;
}

.tabs-card {
  margin-bottom: 20px;
}

.filter-bar {
  margin-bottom: 15px;
}

.level-item {
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.level-item:hover {
  border-color: #409EFF;
}

.level-item.active {
  border-color: #409EFF;
  background: #ecf5ff;
}

.level-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
}

.level-rate {
  font-size: 14px;
  color: #409EFF;
  margin-bottom: 10px;
}

.level-conditions {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.level-benefits {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
  justify-content: center;
}

.commission-amount {
  color: #67C23A;
  font-weight: bold;
}

.trend-filters {
  margin-bottom: 20px;
}

.trend-summary {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
}

.summary-item {
  flex: 1;
  text-align: center;
}

.summary-item .label {
  display: block;
  color: #666;
  margin-bottom: 5px;
}

.summary-item .value {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.trend-list {
  max-height: 400px;
  overflow-y: auto;
}

.trend-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.trend-item .date {
  width: 100px;
  color: #666;
  font-size: 13px;
}

.bar-container {
  flex: 1;
  height: 20px;
  background: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
}

.bar {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 10px;
  transition: width 0.3s;
}

.trend-item .amount {
  width: 80px;
  text-align: right;
  font-weight: bold;
  color: #67C23A;
}

.notification-actions {
  margin-bottom: 15px;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #ecf5ff;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.notification-title {
  font-weight: bold;
}

.notification-time {
  color: #999;
  font-size: 12px;
}

.notification-content {
  color: #666;
  font-size: 13px;
}

.amount-value, .amount-tip, .fee-value, .actual-value {
  margin-left: 10px;
}

.amount-value {
  font-size: 18px;
  font-weight: bold;
  color: #67C23A;
}

.actual-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
