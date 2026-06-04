<template>
  <div class="recharge-container">
    <h2 class="page-title">{{ $t('recharge.title') }}</h2>

    <el-row :gutter="20">
      <!-- 充值金额选择 -->
      <el-col :xs="24" :lg="16">
        <el-card class="main-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('recharge.selectAmount') }}</span>
              <el-tag v-if="currentBonus > 0" type="success" effect="dark">
                {{ $t('recharge.bonus') }}+¥{{ currentBonus }}
              </el-tag>
            </div>
          </template>

          <!-- 预设金额选项 -->
          <div class="amount-options">
            <div
              v-for="option in amountOptions"
              :key="option.amount"
              class="amount-card"
              :class="{ active: selectedAmount === option.amount && !isCustomAmount }"
              @click="selectPresetAmount(option)"
            >
              <div class="amount">¥{{ option.amount }}</div>
              <div v-if="option.bonus > 0" class="bonus">
                <el-icon><Gift /></el-icon>
                送 ¥{{ option.bonus }}
              </div>
              <div v-if="option.bonus > 0" class="tag">+{{ Math.round(option.bonus / option.amount * 100) }}%</div>
            </div>
          </div>

          <!-- 自定义金额 -->
          <div class="custom-amount">
            <el-divider content-position="center">{{ $t('recharge.orCustomAmount') }}</el-divider>
            <div class="custom-input">
              <span class="prefix">¥</span>
              <el-input-number
                v-model="customAmount"
                :min="1"
                :max="100000"
                :precision="2"
                size="large"
                controls-position="right"
                @change="handleCustomAmountChange"
              />
              <span class="tip">{{ $t('recharge.minAmount') }}: ¥1</span>
            </div>
          </div>

          <!-- 支付方式选择 -->
          <div class="payment-methods">
            <div class="section-title">
              {{ $t('recharge.selectPaymentMethod') }}
              <el-tag v-if="getChannelDiscount(selectedChannel) > 0" type="warning" size="small">
                {{ getChannelDiscount(selectedChannel) }}% OFF
              </el-tag>
            </div>

            <el-tabs v-model="paymentTab" class="payment-tabs">
              <!-- 传统支付 -->
              <el-tab-pane label="💳 传统支付" name="traditional">
                <div class="method-list">
                  <div
                    v-for="method in traditionalMethods"
                    :key="method.code"
                    class="method-card"
                    :class="{ active: selectedChannel === method.code }"
                    @click="selectChannel(method.code)"
                  >
                    <div class="method-icon">
                      <component :is="getPaymentIcon(method.code)" />
                    </div>
                    <div class="method-info">
                      <div class="method-name">{{ method.name }}</div>
                      <div v-if="method.discount > 0" class="method-discount">{{ method.discount }}% OFF</div>
                    </div>
                    <div v-if="selectedChannel === method.code" class="check-icon">
                      <el-icon><Check /></el-icon>
                    </div>
                  </div>
                </div>
              </el-tab-pane>

              <!-- 加密货币 -->
              <el-tab-pane label="🪙 加密货币" name="crypto">
                <div class="method-list crypto-list">
                  <div
                    v-for="method in cryptoMethods"
                    :key="method.code"
                    class="method-card crypto-card"
                    :class="{ active: selectedChannel === method.code }"
                    @click="selectChannel(method.code)"
                  >
                    <div class="crypto-icon">
                      <span class="crypto-emoji">{{ getCryptoEmoji(method.code) }}</span>
                    </div>
                    <div class="method-info">
                      <div class="method-name">{{ method.name }}</div>
                      <div class="crypto-details">
                        <span class="discount">{{ method.discount }}% {{ $t('recharge.discount') }}</span>
                      </div>
                    </div>
                    <div v-if="method.supportedCoins" class="supported-coins">
                      <el-tag v-for="coin in method.supportedCoins" :key="coin" size="small">
                        {{ coin }}
                      </el-tag>
                    </div>
                    <div v-if="selectedChannel === method.code" class="check-icon">
                      <el-icon><Check /></el-icon>
                    </div>
                  </div>
                </div>

                <!-- 加密货币支付说明 -->
                <div class="crypto-info-box">
                  <el-alert type="info" :closable="false" show-icon>
                    <template #title>
                      <strong>{{ $t('recharge.cryptoPaymentGuide') }}</strong>
                    </template>
                    <template #default>
                      <ol style="margin: 10px 0; padding-left: 20px;">
                        <li>{{ $t('recharge.cryptoStep1') }}</li>
                        <li>{{ $t('recharge.cryptoStep2') }}</li>
                        <li>{{ $t('recharge.cryptoStep3') }}</li>
                        <li>{{ $t('recharge.cryptoStep4') }}</li>
                      </ol>
                    </template>
                  </el-alert>
                </div>
              </el-tab-pane>

              <!-- 国际支付 -->
              <el-tab-pane label="🌍 国际支付" name="international">
                <div class="method-list">
                  <div
                    v-for="method in internationalMethods"
                    :key="method.code"
                    class="method-card"
                    :class="{ active: selectedChannel === method.code }"
                    @click="selectChannel(method.code)"
                  >
                    <div class="method-icon">
                      <component :is="getPaymentIcon(method.code)" />
                    </div>
                    <div class="method-info">
                      <div class="method-name">{{ method.name }}</div>
                      <div v-if="method.supportedMethods" class="supported-methods">
                        <span v-for="m in method.supportedMethods" :key="m" class="method-tag">{{ m }}</span>
                      </div>
                    </div>
                    <div v-if="selectedChannel === method.code" class="check-icon">
                      <el-icon><Check /></el-icon>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 订单摘要 -->
          <div class="order-summary">
            <el-card class="summary-card" shadow="never">
              <template #header>
                <span>{{ $t('recharge.orderSummary') }}</span>
              </template>
              <div class="summary-content">
                <div class="summary-row">
                  <span>{{ $t('recharge.originalAmount') }}</span>
                  <span class="amount">¥{{ selectedAmount }}</span>
                </div>
                <div v-if="currentBonus > 0" class="summary-row bonus">
                  <span>{{ $t('recharge.bonus') }}</span>
                  <span class="bonus-amount">+¥{{ currentBonus }}</span>
                </div>
                <div v-if="getChannelDiscount(selectedChannel) > 0" class="summary-row discount">
                  <span>{{ $t('recharge.channelDiscount') }}</span>
                  <span class="discount-amount">-¥{{ channelDiscountAmount }}</span>
                </div>
                <el-divider />
                <div class="summary-row total">
                  <span>{{ $t('recharge.total') }}</span>
                  <span class="total-amount">¥{{ totalAmount }}</span>
                </div>
                <div v-if="cryptoEquivalent > 0" class="crypto-equivalent">
                  <span>≈ {{ cryptoEquivalent.toFixed(6) }} USDT</span>
                  <span class="rate">@ ¥{{ usdtRate }}/USDT</span>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 提交按钮 -->
          <div class="submit-section">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              :disabled="!canSubmit"
              @click="handleSubmit"
            >
              <el-icon v-if="!submitting"><CreditCard /></el-icon>
              {{ $t('recharge.payNow') }} ¥{{ totalAmount }}
            </el-button>
            <div class="submit-tip">
              <el-icon><InfoFilled /></el-icon>
              {{ $t('recharge.orderExpireTip') }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :lg="8">
        <!-- 充值说明 -->
        <el-card class="info-card">
          <template #header>
            <span>{{ $t('recharge.instructions') }}</span>
          </template>
          <el-collapse v-model="activeInfoCollapse">
            <el-collapse-item title="💳 支付方式说明" name="payment">
              <ul class="info-list">
                <li><strong>支付宝：</strong>实时到账，支持扫码和网页支付</li>
                <li><strong>微信支付：</strong>实时到账，支持扫码支付</li>
                <li><strong>USDT：</strong>TRC20网络，享5%优惠，30分钟内到账</li>
                <li><strong>Stripe/Creem：</strong>支持全球信用卡、Apple Pay、Google Pay</li>
              </ul>
            </el-collapse-item>
            <el-collapse-item title="🎁 优惠政策" name="bonus">
              <ul class="info-list">
                <li>充值满 ¥50 赠送 ¥2</li>
                <li>充值满 ¥100 赠送 ¥10</li>
                <li>充值满 ¥200 赠送 ¥30</li>
                <li>充值满 ¥500 赠送 ¥100</li>
                <li>充值满 ¥1000 赠送 ¥200</li>
                <li>使用USDT支付额外享5%优惠</li>
              </ul>
            </el-collapse-item>
            <el-collapse-item title="⚠️ 注意事项" name="warning">
              <ul class="info-list">
                <li>充值金额不可提现</li>
                <li>订单有效期为30分钟</li>
                <li>加密货币支付需自行承担网络手续费</li>
                <li>如遇支付问题请联系在线客服</li>
              </ul>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <!-- 最近充值 -->
        <el-card class="history-card">
          <template #header>
            <div class="card-header">
              <span>{{ $t('recharge.recentRecharge') }}</span>
              <el-button type="text" @click="goToHistory">{{ $t('recharge.viewAll') }}</el-button>
            </div>
          </template>
          <el-empty v-if="!recentOrders.length" :description="$t('recharge.noHistory')" />
          <div v-else class="recent-list">
            <div v-for="order in recentOrders" :key="order.id" class="recent-item">
              <div class="recent-info">
                <span class="amount">¥{{ order.amount }}</span>
                <el-tag :type="getStatusType(order.status)" size="small">
                  {{ getStatusText(order.status) }}
                </el-tag>
              </div>
              <div class="recent-meta">
                <span class="method">{{ getChannelName(order.paymentMethod) }}</span>
                <span class="time">{{ formatTime(order.createdAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 支付对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      :title="$t('recharge.completePayment')"
      width="600px"
      :close-on-click-modal="false"
    >
      <!-- 二维码支付 (支付宝/微信) -->
      <div v-if="currentOrder?.qrCode" class="qrcode-section">
        <div class="qrcode-wrapper">
          <qrcode-vue :value="currentOrder.qrCode" :size="250" level="M" />
        </div>
        <p class="qrcode-tip">{{ $t('recharge.scanQrcode') }}</p>
        <el-button type="primary" size="large" @click="checkPaymentStatus">
          {{ $t('recharge.iHavePaid') }}
        </el-button>
      </div>

      <!-- 加密货币支付 -->
      <div v-else-if="currentOrder?.paymentAddress" class="crypto-section">
        <el-alert type="warning" :closable="false" show-icon>
          <template #title>
            {{ $t('recharge.sendCrypto') }} {{ currentOrder.cryptoAmount }} {{ currentOrder.cryptoSymbol }}
          </template>
          <template #default>
            {{ $t('recharge.network') }}: {{ currentOrder.network }}
          </template>
        </el-alert>

        <div class="crypto-address">
          <el-input
            v-model="currentOrder.paymentAddress"
            readonly
            type="textarea"
            :rows="3"
            size="large"
          />
          <el-button type="primary" @click="copyAddress">
            <el-icon><DocumentCopy /></el-icon>
            {{ $t('recharge.copyAddress') }}
          </el-button>
        </div>

        <div class="crypto-qr">
          <qrcode-vue :value="currentOrder.qrCode" :size="200" level="M" />
        </div>

        <el-progress
          v-if="paymentTimer > 0"
          :percentage="paymentProgress"
          :status="paymentProgress > 80 ? 'warning' : undefined"
        />

        <p class="crypto-tip">
          {{ $t('recharge.awaitConfirmation') }}
          <countdown :time="paymentTimer * 1000" @end="onPaymentTimeout">
            <template #default="{ minutes, seconds }">
              {{ $t('recharge.expireIn') }} {{ minutes }}:{{ seconds }}
            </template>
          </countdown>
        </p>

        <el-button type="primary" @click="checkCryptoPayment">
          {{ $t('recharge.checkPayment') }}
        </el-button>
      </div>

      <!-- Stripe/Creem 跳转 -->
      <div v-else-if="currentOrder?.paymentUrl" class="redirect-section">
        <el-result icon="success" :title="$t('recharge.redirecting')">
          <template #sub-title>
            <p>{{ $t('recharge.autoRedirect') }}</p>
          </template>
          <template #extra>
            <el-button type="primary" @click="openPaymentPage">
              {{ $t('recharge.openPaymentPage') }}
            </el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Check, CreditCard, InfoFilled, Gift, DocumentCopy } from '@element-plus/icons-vue'
import { getAmountOptions, getPaymentMethods, createRechargeOrder, getOrderStatus, getRechargeHistory } from '@/api/recharge'
import QrcodeVue from 'qrcode.vue'

const router = useRouter()

// 状态
const submitting = ref(false)
const paymentDialogVisible = ref(false)
const paymentTab = ref('traditional')
const activeInfoCollapse = ref(['payment', 'bonus', 'warning'])

const selectedAmount = ref(100)
const customAmount = ref(100)
const isCustomAmount = ref(false)
const selectedChannel = ref('alipay')
const amountOptions = ref([])
const paymentMethods = ref([])
const recentOrders = ref([])
const currentOrder = ref(null)

// 轮询定时器
let statusPollingTimer = null
let paymentTimer = ref(0)

// 常量
const usdtRate = 7.25 // 简化处理

// 计算属性
const currentBonus = computed(() => {
  const option = amountOptions.value.find(o => o.amount === selectedAmount.value)
  return option?.bonus || 0
})

const channelDiscount = computed(() => getChannelDiscount(selectedChannel.value))

const channelDiscountAmount = computed(() => {
  return (selectedAmount.value * channelDiscount.value / 100).toFixed(2)
})

const totalAmount = computed(() => {
  const base = selectedAmount.value + currentBonus.value
  const discount = base * channelDiscount.value / 100
  return (base - discount).toFixed(2)
})

const cryptoEquivalent = computed(() => {
  return parseFloat(totalAmount.value) / usdtRate
})

const canSubmit = computed(() => {
  return selectedAmount.value > 0
})

const paymentProgress = computed(() => {
  if (paymentTimer.value <= 0) return 100
  return Math.round((1 - paymentTimer.value / 1800) * 100)
})

// 支付方式分组
const traditionalMethods = computed(() =>
  paymentMethods.value.filter(m => ['alipay', 'wechat'].includes(m.code))
)

const cryptoMethods = computed(() =>
  paymentMethods.value.filter(m => m.code === 'okx')
)

const internationalMethods = computed(() =>
  paymentMethods.value.filter(m => ['stripe', 'creem'].includes(m.code))
)

// 方法
function selectPresetAmount(option) {
  selectedAmount.value = option.amount
  customAmount.value = option.amount
  isCustomAmount.value = false
}

function handleCustomAmountChange(val) {
  if (val > 0) {
    selectedAmount.value = val
    isCustomAmount.value = true
  }
}

function selectChannel(channel) {
  selectedChannel.value = channel

  // 自动切换到对应的Tab
  if (['alipay', 'wechat'].includes(channel)) {
    paymentTab.value = 'traditional'
  } else if (channel === 'okx') {
    paymentTab.value = 'crypto'
  } else if (['stripe', 'creem'].includes(channel)) {
    paymentTab.value = 'international'
  }
}

function getChannelDiscount(channel) {
  const method = paymentMethods.value.find(m => m.code === channel)
  return method?.discount || 0
}

function getChannelName(channel) {
  const names = {
    alipay: '支付宝',
    wechat: '微信支付',
    okx: 'USDT',
    stripe: 'Stripe',
    creem: 'Creem'
  }
  return names[channel] || channel
}

function getCryptoEmoji(code) {
  const emojis = {
    okx: '🪙',
    btc: '₿',
    eth: 'Ξ'
  }
  return emojis[code] || '💰'
}

function getPaymentIcon(code) {
  // 返回对应的图标组件
  return 'CreditCard'
}

function getStatusType(status) {
  const types = {
    pending: 'warning',
    paid: 'success',
    cancelled: 'info',
    expired: 'danger'
  }
  return types[status] || ''
}

function getStatusText(status) {
  const texts = {
    pending: '待支付',
    paid: '已支付',
    cancelled: '已取消',
    expired: '已过期'
  }
  return texts[status] || status
}

function formatTime(time) {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

async function fetchOptions() {
  try {
    const [amountRes, methodRes] = await Promise.all([
      getAmountOptions(),
      getPaymentMethods()
    ])
    amountOptions.value = amountRes.data || []
    paymentMethods.value = methodRes.data || []

    // 设置默认值
    if (amountOptions.value.length) {
      selectPresetAmount(amountOptions.value[0])
    }
    if (paymentMethods.value.length) {
      selectedChannel.value = paymentMethods.value[0].code
    }
  } catch (error) {
    console.error('Failed to fetch options:', error)
  }
}

async function fetchRecentOrders() {
  try {
    const res = await getRechargeHistory({ page: 1, size: 5 })
    recentOrders.value = res.data?.records || []
  } catch (error) {
    console.error('Failed to fetch recent orders:', error)
  }
}

async function handleSubmit() {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const res = await createRechargeOrder({
      amount: parseFloat(totalAmount.value),
      channel: selectedChannel.value
    })

    if (res.code === 200) {
      currentOrder.value = res.data
      paymentDialogVisible.value = true

      // 根据支付类型处理
      if (currentOrder.value.qrCode || currentOrder.value.paymentAddress) {
        startStatusPolling()
        startPaymentTimer()
      } else if (currentOrder.value.paymentUrl) {
        // Stripe/Creem 跳转到支付页面
        setTimeout(() => {
          openPaymentPage()
        }, 3000)
      }
    } else {
      ElMessage.error(res.message || '创建订单失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    submitting.value = false
  }
}

function startStatusPolling() {
  if (statusPollingTimer) clearInterval(statusPollingTimer)

  statusPollingTimer = setInterval(async () => {
    if (!currentOrder.value?.orderNo) return

    try {
      const res = await getOrderStatus(currentOrder.value.orderNo)
      if (res.data?.status === 'paid') {
        ElMessage.success('支付成功！')
        stopStatusPolling()
        paymentDialogVisible.value = false
        router.push('/dashboard')
      }
    } catch (error) {
      console.error('Failed to check status:', error)
    }
  }, 3000)
}

function stopStatusPolling() {
  if (statusPollingTimer) {
    clearInterval(statusPollingTimer)
    statusPollingTimer = null
  }
}

function startPaymentTimer() {
  paymentTimer.value = 1800 // 30分钟
  const timer = setInterval(() => {
    if (paymentTimer.value > 0) {
      paymentTimer.value--
    } else {
      clearInterval(timer)
      ElMessage.warning('订单已过期')
    }
  }, 1000)
}

async function checkPaymentStatus() {
  if (!currentOrder.value?.orderNo) return

  try {
    const res = await getOrderStatus(currentOrder.value.orderNo)
    if (res.data?.status === 'paid') {
      ElMessage.success('支付成功！')
      paymentDialogVisible.value = false
      router.push('/dashboard')
    } else {
      ElMessage.info('订单尚未支付，请完成支付后点击"我已支付"')
    }
  } catch (error) {
    ElMessage.error('查询失败')
  }
}

async function checkCryptoPayment() {
  await checkPaymentStatus()
}

function openPaymentPage() {
  if (currentOrder.value?.paymentUrl) {
    window.open(currentOrder.value.paymentUrl, '_blank')
  }
}

function copyAddress() {
  if (currentOrder.value?.paymentAddress) {
    navigator.clipboard.writeText(currentOrder.value.paymentAddress)
    ElMessage.success('地址已复制')
  }
}

function onPaymentTimeout() {
  ElMessage.warning('支付超时，请重新下单')
  paymentDialogVisible.value = false
}

function goToHistory() {
  router.push('/recharge/history')
}

onMounted(() => {
  fetchOptions()
  fetchRecentOrders()
})

onUnmounted(() => {
  stopStatusPolling()
})
</script>

<style lang="scss" scoped>
.recharge-container {
  padding: 20px;

  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }
}

.main-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.amount-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.amount-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;

  &:hover {
    border-color: #409eff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }

  &.active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  }

  .amount {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
  }

  .bonus {
    color: #67c23a;
    font-size: 13px;
    margin-top: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }

  .tag {
    position: absolute;
    top: 8px;
    right: -20px;
    background: #67c23a;
    color: #fff;
    font-size: 10px;
    padding: 2px 25px;
    transform: rotate(45deg);
  }
}

.custom-amount {
  margin: 20px 0;

  .custom-input {
    display: flex;
    align-items: center;
    gap: 10px;

    .prefix {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }

    .tip {
      color: #909399;
      font-size: 12px;
    }
  }
}

.payment-methods {
  margin: 30px 0;

  .section-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.payment-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
  }
}

.method-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.method-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  }

  &.active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  }

  .method-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 10px;
    font-size: 24px;
  }

  .method-info {
    flex: 1;

    .method-name {
      font-weight: 600;
      color: #303133;
    }

    .method-discount,
    .discount {
      color: #67c23a;
      font-size: 12px;
      margin-top: 4px;
    }
  }

  .check-icon {
    position: absolute;
    top: -8px;
    right: -8px;
    width: 24px;
    height: 24px;
    background: #409eff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }
}

.crypto-card {
  flex-direction: column;
  align-items: flex-start;

  .crypto-icon {
    width: 48px;
    height: 48px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .crypto-emoji {
      font-size: 24px;
    }
  }

  .supported-coins {
    margin-top: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
  }
}

.crypto-info-box {
  margin-top: 20px;
}

.order-summary {
  margin: 30px 0;

  .summary-card {
    background: #fafafa;

    .summary-content {
      .summary-row {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;

        &.bonus .bonus-amount {
          color: #67c23a;
          font-weight: 600;
        }

        &.discount .discount-amount {
          color: #f56c6c;
        }

        &.total {
          font-size: 18px;
          font-weight: 600;

          .total-amount {
            font-size: 24px;
            color: #409eff;
          }
        }
      }

      .crypto-equivalent {
        text-align: center;
        margin-top: 15px;
        padding: 10px;
        background: #f0f9ff;
        border-radius: 8px;
        color: #606266;

        .rate {
          margin-left: 10px;
          color: #909399;
          font-size: 12px;
        }
      }
    }
  }
}

.submit-section {
  text-align: center;
  margin-top: 30px;

  .el-button {
    min-width: 200px;
    height: 50px;
    font-size: 18px;
  }

  .submit-tip {
    margin-top: 15px;
    color: #909399;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 5px;
  }
}

.info-card,
.history-card {
  margin-bottom: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.info-list {
  padding-left: 20px;
  margin: 0;

  li {
    margin: 8px 0;
    color: #606266;
  }
}

.recent-list {
  .recent-item {
    padding: 15px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .recent-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .amount {
        font-size: 16px;
        font-weight: 600;
      }
    }

    .recent-meta {
      display: flex;
      justify-content: space-between;
      font-size: 12px;
      color: #909399;
    }
  }
}

// 支付对话框样式
.qrcode-section {
  text-align: center;

  .qrcode-wrapper {
    background: #fff;
    padding: 20px;
    display: inline-block;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }

  .qrcode-tip {
    margin: 20px 0;
    color: #606266;
  }
}

.crypto-section {
  .crypto-address {
    margin: 20px 0;

    .el-input {
      margin-bottom: 10px;
    }
  }

  .crypto-qr {
    text-align: center;
    margin: 20px 0;
    background: #fff;
    padding: 15px;
    border-radius: 8px;
    display: inline-block;
  }

  .crypto-tip {
    margin: 20px 0;
    text-align: center;
    color: #606266;
  }
}
</style>
