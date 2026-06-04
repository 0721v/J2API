<template>
  <div class="tokens-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>API密钥</span>
          <el-button type="primary" @click="$router.push('/tokens/create')">
            <el-icon><Plus /></el-icon> 创建密钥
          </el-button>
        </div>
      </template>
      
      <el-table :data="tokens" v-loading="loading">
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column prop="apiKey" label="API密钥" min-width="200">
          <template #default="{ row }">
            <code class="api-key">{{ maskKey(row.apiKey) }}</code>
            <el-button 
              size="small" 
              text 
              @click="copyKey(row.apiKey)"
            >
              复制
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="额度" width="150">
          <template #default="{ row }">
            <span v-if="row.remainingQuota < 0">无限制</span>
            <span v-else>¥{{ formatAmount(row.remainingQuota) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expiresAt || '永不过期' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewToken(row)">详情</el-button>
            <el-button 
              size="small" 
              type="danger" 
              text
              @click="deleteToken(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchTokens"
        @current-change="fetchTokens"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTokens, deleteToken as apiDeleteToken } from '@/api/token'

const loading = ref(false)
const tokens = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

async function fetchTokens() {
  loading.value = true
  try {
    const res = await getTokens({
      page: pagination.page,
      size: pagination.size
    })
    tokens.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('Failed to fetch tokens:', error)
  } finally {
    loading.value = false
  }
}

function maskKey(key) {
  if (!key) return ''
  return key.substring(0, 8) + '****' + key.substring(key.length - 4)
}

function getStatusType(status) {
  const types = {
    active: 'success',
    disabled: 'info',
    expired: 'danger'
  }
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = {
    active: '正常',
    disabled: '已禁用',
    expired: '已过期'
  }
  return texts[status] || status
}

function formatAmount(amount) {
  return ((amount || 0) / 100).toFixed(2)
}

async function copyKey(key) {
  try {
    await navigator.clipboard.writeText(key)
    ElMessage.success('密钥已复制，请妥善保管')
  } catch {
    ElMessage.error('复制失败')
  }
}

function viewToken(row) {
  // TODO: 打开详情弹窗
  console.log('View token:', row)
}

async function deleteToken(row) {
  try {
    await ElMessageBox.confirm('确定要删除此密钥吗？删除后无法恢复', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await apiDeleteToken(row.id)
    ElMessage.success('删除成功')
    fetchTokens()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete token:', error)
    }
  }
}

onMounted(() => {
  fetchTokens()
})
</script>

<style lang="scss" scoped>
.tokens-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.api-key {
  background: #f5f7fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 13px;
}
</style>
