<template>
  <div class="models-container">
    <h2 class="page-title">{{ $t('models.title') }}</h2>

    <!-- 模型分类 -->
    <el-card class="filter-card">
      <el-tabs v-model="activeType" @tab-change="handleTypeChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="对话模型" name="chat">
          <template #label>
            <span><i class="el-icon-chat-line-round"></i> 对话模型</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="嵌入模型" name="embedding">
          <template #label>
            <span><i class="el-icon-document"></i> 嵌入模型</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="重排序模型" name="rerank">
          <template #label>
            <span><i class="el-icon-sort"></i> 重排序模型</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="视频/音频" name="video">
          <template #label>
            <span><i class="el-icon-video-camera"></i> 视频/音频</span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="图像生成" name="image">
          <template #label>
            <span><i class="el-icon-picture"></i> 图像生成</span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 模型列表 -->
    <el-card class="models-card">
      <el-table :data="filteredModels" v-loading="loading" stripe>
        <el-table-column label="模型" min-width="200">
          <template #default="{ row }">
            <div class="model-info">
              <code class="model-id">{{ row.modelId }}</code>
              <div class="model-name">{{ row.name }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="billingType" label="计费方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getBillingTypeTag(row.billingType)" size="small">
              {{ getBillingTypeText(row.billingType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="价格" min-width="250">
          <template #default="{ row }">
            <div class="price-info">
              <!-- 按Token计费 -->
              <template v-if="row.billingType === 'token'">
                <div class="price-item">
                  <span class="label">输入:</span>
                  <span class="value">¥{{ row.inputPrice }} /1K tokens</span>
                </div>
                <div class="price-item">
                  <span class="label">输出:</span>
                  <span class="value">¥{{ row.outputPrice }} /1K tokens</span>
                </div>
              </template>

              <!-- 按次计费 -->
              <template v-else-if="row.billingType === 'per_request'">
                <div class="price-item single">
                  <span class="value highlight">¥{{ row.perRequestPrice }}</span>
                  <span class="unit">/次</span>
                </div>
              </template>

              <!-- 按秒计费 -->
              <template v-else-if="row.billingType === 'per_second'">
                <div class="price-item single">
                  <span class="label">{{ row.mediaType || '时长' }}:</span>
                  <span class="value highlight">¥{{ row.perSecondPrice }}</span>
                  <span class="unit">/秒</span>
                </div>
                <div class="price-note" v-if="row.minBillableSeconds || row.maxBillableSeconds">
                  范围: {{ row.minBillableSeconds || 1 }}s - {{ row.maxBillableSeconds || 300 }}s
                </div>
              </template>

              <!-- 阶梯计费 -->
              <template v-else-if="row.billingType === 'tiered'">
                <div class="price-item single">
                  <span class="value highlight">阶梯计费</span>
                  <el-tooltip content="用量越多单价越低">
                    <i class="el-icon-question"></i>
                  </el-tooltip>
                </div>
                <div class="tiered-preview" v-if="row.tieredConfig">
                  <el-popover placement="bottom" :width="300" trigger="hover">
                    <template #reference>
                      <span class="tiered-hint">查看阶梯详情</span>
                    </template>
                    <div class="tier-list">
                      <div v-for="(tier, index) in parseTieredConfig(row.tieredConfig)" :key="index" class="tier-item">
                        <span class="tier-range">{{ tier.range }}</span>
                        <span class="tier-price">¥{{ tier.price }}/1K</span>
                      </div>
                    </div>
                  </el-popover>
                </div>
              </template>

              <!-- 免费 -->
              <template v-else-if="isFree(row)">
                <div class="price-item single">
                  <el-tag type="success" size="small">免费</el-tag>
                </div>
              </template>

              <!-- 默认 -->
              <template v-else>
                <span class="free">未定价</span>
              </template>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="上下文" width="120" align="center">
          <template #default="{ row }">
            <span class="context-length">{{ formatLength(row.maxInputLength) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="特性" width="200">
          <template #default="{ row }">
            <div class="features">
              <el-tag v-if="row.supportsStreaming" size="small" type="success">流式</el-tag>
              <el-tag v-if="row.supportsFunctionCall" size="small">函数调用</el-tag>
              <el-tag v-if="row.supportsVision" size="small" type="warning">视觉</el-tag>
              <el-tag v-if="row.recommended" size="small" type="danger">推荐</el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 计费说明 -->
    <el-card class="info-card">
      <template #header>
        <span><i class="el-icon-info"></i> 计费说明</span>
      </template>

      <el-collapse v-model="activeInfoCollapse">
        <el-collapse-item title="计费方式详解" name="billing">
          <el-row :gutter="20">
            <el-col :span="12" :xs="24">
              <div class="billing-type">
                <h4><i class="el-icon-coin"></i> 按Token计费</h4>
                <p>根据API调用的输入和输出Token数量分别计费</p>
                <div class="example">
                  <strong>示例:</strong> 输入1000 tokens + 输出500 tokens
                  <br />费用 = 1000/1000 × ¥0.03 + 500/1000 × ¥0.06 = ¥0.06
                </div>
              </div>
            </el-col>
            <el-col :span="12" :xs="24">
              <div class="billing-type">
                <h4><i class="el-icon-document"></i> 按次计费</h4>
                <p>每次API调用收取固定费用，不区分输入输出</p>
                <div class="example">
                  <strong>示例:</strong> 每次调用 ¥0.10
                  <br />调用10次 = ¥1.00
                </div>
              </div>
            </el-col>
            <el-col :span="12" :xs="24">
              <div class="billing-type">
                <h4><i class="el-icon-video-camera"></i> 按秒计费</h4>
                <p>适用于视频生成、音频合成等按时长计费的场景</p>
                <div class="example">
                  <strong>示例:</strong> ¥0.01/秒
                  <br />生成10秒视频 = ¥0.10
                </div>
              </div>
            </el-col>
            <el-col :span="12" :xs="24">
              <div class="billing-type">
                <h4><i class="el-icon-data-line"></i> 阶梯计费</h4>
                <p>累计用量越多，单价越低，用量越高优惠越大</p>
                <div class="example">
                  <strong>示例:</strong>
                  <br />0-100K: ¥0.10/1K
                  <br />100K-1M: ¥0.08/1K
                  <br />1M+: ¥0.06/1K
                </div>
              </div>
            </el-col>
          </el-row>
        </el-collapse-item>

        <el-collapse-item title="缓存计费优惠" name="cache">
          <div class="cache-info">
            <el-alert type="success" :closable="false" show-icon>
              <template #title>
                <strong>缓存命中享受折扣</strong>
              </template>
              <template #default>
                当API请求命中缓存时，输入费用按 <strong>50%</strong> 计算，有效降低使用成本。
              </template>
            </el-alert>

            <div class="cache-example">
              <h5>计算示例</h5>
              <el-table :data="cacheExampleData" border size="small">
                <el-table-column prop="scenario" label="场景" />
                <el-table-column prop="inputTokens" label="输入Tokens" width="120" />
                <el-table-column prop="outputTokens" label="输出Tokens" width="120" />
                <el-table-column prop="cacheHit" label="缓存命中" width="100" />
                <el-table-column label="费用计算" min-width="200">
                  <template #default="{ row }">
                    <code>{{ row.calculation }}</code>
                  </template>
                </el-table-column>
                <el-table-column prop="total" label="总计" width="100" />
              </el-table>
            </div>
          </div>
        </el-collapse-item>

        <el-collapse-item title="价格计算器" name="calculator">
          <div class="calculator">
            <el-form :inline="true" :model="calcForm">
              <el-form-item label="模型">
                <el-select v-model="calcForm.modelId" placeholder="选择模型" @change="onModelChange">
                  <el-option
                    v-for="model in models"
                    :key="model.id"
                    :label="model.name"
                    :value="model.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="输入Tokens" v-if="calcForm.billingType === 'token'">
                <el-input-number v-model="calcForm.inputTokens" :min="0" :step="100" />
              </el-form-item>
              <el-form-item label="输出Tokens" v-if="calcForm.billingType === 'token'">
                <el-input-number v-model="calcForm.outputTokens" :min="0" :step="100" />
              </el-form-item>
              <el-form-item label="调用次数" v-if="calcForm.billingType === 'per_request'">
                <el-input-number v-model="calcForm.requestCount" :min="1" />
              </el-form-item>
              <el-form-item label="时长(秒)" v-if="calcForm.billingType === 'per_second'">
                <el-input-number v-model="calcForm.seconds" :min="1" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="calculate">计算</el-button>
              </el-form-item>
            </el-form>

            <div v-if="calcResult !== null" class="calc-result">
              <el-alert type="success" :closable="false">
                <template #title>
                  <strong>预估费用: ¥{{ calcResult.toFixed(4) }}</strong>
                </template>
              </el-alert>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getModels, getModelsByType } from '@/api/model'

const loading = ref(false)
const models = ref([])
const activeType = ref('all')
const activeInfoCollapse = ref(['billing'])

const calcForm = ref({
  modelId: null,
  billingType: 'token',
  inputTokens: 1000,
  outputTokens: 500,
  requestCount: 10,
  seconds: 10
})

const calcResult = ref(null)

const cacheExampleData = [
  {
    scenario: '普通请求',
    inputTokens: 1000,
    outputTokens: 500,
    cacheHit: '否',
    calculation: '(1000/1000×0.03)+(500/1000×0.06)',
    total: '¥0.06'
  },
  {
    scenario: '缓存命中',
    inputTokens: 1000,
    outputTokens: 500,
    cacheHit: '是',
    calculation: '(1000×50%/1000×0.03)+(500/1000×0.06)',
    total: '¥0.045'
  }
]

const filteredModels = computed(() => {
  if (activeType.value === 'all') {
    return models.value
  }
  return models.value.filter(m => m.type === activeType.value)
})

async function fetchModels() {
  loading.value = true
  try {
    let res
    if (activeType.value === 'all') {
      res = await getModels()
    } else {
      res = await getModelsByType(activeType.value)
    }
    models.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch models:', error)
  } finally {
    loading.value = false
  }
}

function handleTypeChange() {
  fetchModels()
}

function getTypeTag(type) {
  const types = {
    chat: 'primary',
    embedding: 'success',
    rerank: 'warning',
    video: 'danger',
    image: 'info'
  }
  return types[type] || 'info'
}

function getTypeText(type) {
  const texts = {
    chat: '对话',
    embedding: '嵌入',
    rerank: '重排序',
    video: '视频',
    image: '图像',
    audio: '音频'
  }
  return texts[type] || type
}

function getBillingTypeTag(type) {
  const types = {
    token: 'primary',
    per_request: 'success',
    per_second: 'warning',
    tiered: 'info'
  }
  return types[type] || 'info'
}

function getBillingTypeText(type) {
  const texts = {
    token: 'Token',
    per_request: '按次',
    per_second: '按秒',
    tiered: '阶梯'
  }
  return texts[type] || type
}

function isFree(model) {
  return !model.inputPrice && !model.outputPrice && !model.perRequestPrice && !model.perSecondPrice && !model.tieredConfig
}

function formatLength(length) {
  if (!length) return '-'
  if (length >= 1000000) {
    return (length / 1000000).toFixed(0) + 'M'
  }
  if (length >= 1000) {
    return (length / 1000).toFixed(0) + 'K'
  }
  return length
}

function parseTieredConfig(config) {
  if (!config) return []
  try {
    return config.split(',').map(tier => {
      const [range, price] = tier.split(':')
      return { range: range.replace('+', '+'), price }
    })
  } catch {
    return []
  }
}

function onModelChange(modelId) {
  const model = models.value.find(m => m.id === modelId)
  if (model) {
    calcForm.value.billingType = model.billingType || 'token'
  }
}

function calculate() {
  const model = models.value.find(m => m.id === calcForm.value.modelId)
  if (!model) {
    calcResult.value = null
    return
  }

  let result = 0

  switch (calcForm.value.billingType) {
    case 'token':
      const inputCost = (calcForm.value.inputTokens / 1000) * (model.inputPrice || 0)
      const outputCost = (calcForm.value.outputTokens / 1000) * (model.outputPrice || 0)
      result = inputCost + outputCost
      break
    case 'per_request':
      result = calcForm.value.requestCount * (model.perRequestPrice || 0)
      break
    case 'per_second':
      result = calcForm.value.seconds * (model.perSecondPrice || 0)
      break
    case 'tiered':
      // 简化计算，使用平均价格
      result = calcForm.value.inputTokens / 1000 * 0.08
      break
  }

  calcResult.value = result
}

onMounted(() => {
  fetchModels()
})
</script>

<style lang="scss" scoped>
.models-container {
  padding: 20px;

  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }
}

.filter-card {
  margin-bottom: 20px;
}

.models-card {
  margin-bottom: 20px;
}

.model-info {
  .model-id {
    background: #f5f7fa;
    padding: 4px 8px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 13px;
    display: block;
  }

  .model-name {
    color: #606266;
    font-size: 12px;
    margin-top: 4px;
  }
}

.price-info {
  .price-item {
    display: flex;
    align-items: center;
    gap: 5px;
    margin: 2px 0;

    .label {
      color: #909399;
      font-size: 12px;
      min-width: 35px;
    }

    .value {
      color: #303133;

      &.highlight {
        color: #409eff;
        font-weight: 600;
        font-size: 15px;
      }
    }

    .unit {
      color: #909399;
      font-size: 12px;
    }

    &.single {
      justify-content: flex-start;
    }
  }

  .price-note {
    font-size: 11px;
    color: #c0c4cc;
    margin-top: 2px;
  }
}

.tiered-preview {
  margin-top: 4px;

  .tiered-hint {
    color: #409eff;
    font-size: 12px;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.tier-list {
  .tier-item {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    border-bottom: 1px solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    .tier-range {
      color: #606266;
    }

    .tier-price {
      color: #409eff;
      font-weight: 500;
    }
  }
}

.context-length {
  font-family: monospace;
  color: #606266;
}

.features {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.free {
  color: #67c23a;
  font-weight: bold;
}

.info-card {
  .el-collapse-item__header {
    font-weight: 600;
  }
}

.billing-type {
  padding: 10px;

  h4 {
    margin: 0 0 8px;
    display: flex;
    align-items: center;
    gap: 8px;

    i {
      color: #409eff;
    }
  }

  p {
    color: #606266;
    margin: 0 0 10px;
  }

  .example {
    background: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
    font-size: 13px;
    color: #606266;
  }
}

.cache-info {
  .el-alert {
    margin-bottom: 15px;
  }

  .cache-example {
    h5 {
      margin: 15px 0 10px;
      color: #303133;
    }
  }
}

.calculator {
  .calc-result {
    margin-top: 20px;
  }
}
</style>
