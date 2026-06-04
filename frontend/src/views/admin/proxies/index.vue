<template>
  <div class="proxies-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>API 代理管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建代理
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="代理名称">
          <el-input v-model="filterForm.name" placeholder="请输入代理名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filterForm.type" placeholder="请选择类型" clearable>
            <el-option label="自定义端点" value="custom_endpoint" />
            <el-option label="代理转发" value="proxy_forward" />
            <el-option label="路径重写" value="path_rewrite" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.enabled" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 代理列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="代理名称" min-width="150">
          <template #default="{ row }">
            <div class="proxy-name">
              <span>{{ row.name }}</span>
              <el-tag :type="getTypeColor(row.type)" size="small">{{ getTypeText(row.type) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="源路径" min-width="150">
          <template #default="{ row }">
            <code>{{ row.sourcePath || '/' }}</code>
          </template>
        </el-table-column>
        <el-table-column label="目标地址" min-width="200">
          <template #default="{ row }">
            <div class="target-url">
              <span class="url-text" :title="row.targetUrl">{{ row.targetUrl }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="请求方法" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.methods === '*'" type="info">全部</el-tag>
            <el-tag v-else type="success">{{ row.methods }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <span class="priority-value">{{ row.priority || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggle(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleTest(row)">测试</el-button>
            <el-button type="info" link @click="handleViewLogs(row)">日志</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="130px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="代理名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入代理名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代理类型" prop="type">
              <el-select v-model="formData.type" placeholder="请选择类型">
                <el-option label="自定义端点" value="custom_endpoint" />
                <el-option label="代理转发" value="proxy_forward" />
                <el-option label="路径重写" value="path_rewrite" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="formData.priority" :min="0" :max="999" />
              <div class="form-tip">数字越大优先级越高</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-switch v-model="formData.enabled" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">路由配置</el-divider>

        <el-form-item label="目标地址" prop="targetUrl">
          <el-input v-model="formData.targetUrl" placeholder="https://api.openai.com" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="源路径" prop="sourcePath">
              <el-input v-model="formData.sourcePath" placeholder="/v1/chat/completions" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标路径" prop="targetPath">
              <el-input v-model="formData.targetPath" placeholder="/chat/completions" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="请求方法" prop="methods">
          <el-select v-model="formData.methods" placeholder="请选择方法" multiple>
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
            <el-option label="PATCH" value="PATCH" />
          </el-select>
        </el-form-item>

        <el-divider content-position="left">请求配置</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="超时时间(ms)">
              <el-input-number v-model="formData.timeout" :min="1000" :max="300000" :step="1000" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重试次数">
              <el-input-number v-model="formData.retryCount" :min="0" :max="5" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="启用缓存">
              <el-switch v-model="formData.cacheEnabled" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="formData.cacheEnabled">
          <el-col :span="12">
            <el-form-item label="缓存TTL(秒)">
              <el-input-number v-model="formData.cacheTtl" :min="60" :max="86400" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">请求头转换</el-divider>

        <el-form-item label="Header转换" prop="headerTransforms">
          <el-input
            v-model="formData.headerTransforms"
            type="textarea"
            :rows="4"
            placeholder='例如: {"Authorization": "Bearer {token}", "X-Forwarded-For": "{ip}"}'
          />
          <div class="form-tip">JSON格式，支持变量替换: {token}, {ip}, {user_id}</div>
        </el-form-item>

        <el-divider content-position="left">请求体转换</el-divider>

        <el-form-item label="请求转换" prop="requestTransforms">
          <el-input
            v-model="formData.requestTransforms"
            type="textarea"
            :rows="4"
            placeholder='例如: {"model": "gpt-4", "max_tokens": 100}'
          />
          <div class="form-tip">JSON格式，用于修改或添加请求参数</div>
        </el-form-item>

        <el-form-item label="响应转换" prop="responseTransforms">
          <el-input
            v-model="formData.responseTransforms"
            type="textarea"
            :rows="4"
            placeholder='例如: {"cost": {"multiply": 1.2}}'
          />
          <div class="form-tip">JSON格式，用于修改响应数据</div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog v-model="testDialogVisible" title="测试代理" width="600px">
      <div class="test-form">
        <el-form :model="testForm" label-width="100px">
          <el-form-item label="测试URL">
            <el-input v-model="testForm.url" placeholder="请输入测试URL" />
          </el-form-item>
          <el-form-item label="请求方法">
            <el-select v-model="testForm.method">
              <el-option label="GET" value="GET" />
              <el-option label="POST" value="POST" />
            </el-select>
          </el-form-item>
          <el-form-item label="请求体">
            <el-input
              v-model="testForm.body"
              type="textarea"
              :rows="6"
              placeholder='{"model": "gpt-3.5-turbo", "messages": [{"role": "user", "content": "Hello"}]}'
            />
          </el-form-item>
        </el-form>
      </div>
      <div v-if="testResult" class="test-result">
        <h4>测试结果:</h4>
        <pre>{{ JSON.stringify(testResult, null, 2) }}</pre>
      </div>
      <template #footer>
        <el-button @click="testDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleTestSubmit" :loading="testLoading">发送测试</el-button>
      </template>
    </el-dialog>

    <!-- 日志对话框 -->
    <el-dialog v-model="logsDialogVisible" title="代理日志" width="900px">
      <div class="log-stats">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总请求数" :value="logStats.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="成功" :value="logStats.success" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="失败" :value="logStats.failed" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="平均延迟" :value="logStats.avgLatency" suffix="ms" />
          </el-col>
        </el-row>
      </div>

      <el-table :data="logData" v-loading="logsLoading" stripe class="log-table">
        <el-table-column prop="timestamp" label="时间" width="180" />
        <el-table-column prop="method" label="方法" width="80" />
        <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status >= 200 && row.status < 300 ? 'success' : 'danger'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="latency" label="延迟(ms)" width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewLogDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getProxies,
  createProxy,
  updateProxy,
  deleteProxy,
  toggleProxy,
  testProxy,
  getProxyLogs,
  getProxyStats
} from '@/api/proxy'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const testLoading = ref(false)
const logsLoading = ref(false)
const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const logsDialogVisible = ref(false)
const isEdit = ref(false)
const currentProxy = ref(null)

// 表格数据
const tableData = ref([])
const logData = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const logStats = reactive({
  total: 0,
  success: 0,
  failed: 0,
  avgLatency: 0
})

// 筛选表单
const filterForm = reactive({
  name: '',
  type: '',
  enabled: null
})

// 表单数据
const formData = reactive({
  name: '',
  type: 'proxy_forward',
  targetUrl: '',
  sourcePath: '',
  targetPath: '',
  methods: ['POST'],
  headerTransforms: '',
  requestTransforms: '',
  responseTransforms: '',
  priority: 0,
  timeout: 60000,
  retryCount: 0,
  cacheEnabled: false,
  cacheTtl: 300,
  enabled: true
})

// 测试表单
const testForm = reactive({
  url: '',
  method: 'POST',
  body: ''
})

// 测试结果
const testResult = ref(null)

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入代理名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择代理类型', trigger: 'change' }],
  targetUrl: [{ required: true, message: '请输入目标地址', trigger: 'blur' }]
}

// 计算对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑代理' : '新建代理')

// 获取类型颜色
const getTypeColor = (type) => {
  const colors = {
    custom_endpoint: 'primary',
    proxy_forward: 'success',
    path_rewrite: 'warning'
  }
  return colors[type] || 'info'
}

// 获取类型文本
const getTypeText = (type) => {
  const texts = {
    custom_endpoint: '自定义端点',
    proxy_forward: '代理转发',
    path_rewrite: '路径重写'
  }
  return texts[type] || type
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filterForm
    }
    const res = await getProxies(params)
    tableData.value = res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleFilter = () => {
  pagination.page = 1
  loadData()
}

// 重置筛选
const handleReset = () => {
  Object.assign(filterForm, {
    name: '',
    type: '',
    enabled: null
  })
  handleFilter()
}

// 分页
const handleSizeChange = () => {
  pagination.page = 1
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 创建
const handleCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    type: row.type,
    targetUrl: row.targetUrl,
    sourcePath: row.sourcePath,
    targetPath: row.targetPath,
    methods: row.methods ? (typeof row.methods === 'string' ? row.methods.split(',') : row.methods) : ['POST'],
    headerTransforms: row.headerTransforms || '',
    requestTransforms: row.requestTransforms || '',
    responseTransforms: row.responseTransforms || '',
    priority: row.priority || 0,
    timeout: row.timeout || 60000,
    retryCount: row.retryCount || 0,
    cacheEnabled: row.cacheEnabled || false,
    cacheTtl: row.cacheTtl || 300,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    type: 'proxy_forward',
    targetUrl: '',
    sourcePath: '',
    targetPath: '',
    methods: ['POST'],
    headerTransforms: '',
    requestTransforms: '',
    responseTransforms: '',
    priority: 0,
    timeout: 60000,
    retryCount: 0,
    cacheEnabled: false,
    cacheTtl: 300,
    enabled: true
  })
}

// 提交表单
const handleSubmit = async () => {
  submitLoading.value = true
  try {
    const submitData = {
      ...formData,
      methods: formData.methods.join(',')
    }
    if (isEdit.value) {
      await updateProxy(formData.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await createProxy(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除代理 "${row.name}" 吗？`, '提示', {
      type: 'warning'
    })
    await deleteProxy(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 切换状态
const handleToggle = async (row) => {
  try {
    await toggleProxy(row.id, row.enabled)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.enabled = !row.enabled
    ElMessage.error('状态更新失败')
  }
}

// 测试
const handleTest = (row) => {
  currentProxy.value = row
  testForm.url = row.targetUrl
  testForm.method = 'POST'
  testForm.body = '{"model": "gpt-3.5-turbo", "messages": [{"role": "user", "content": "Hello"}]}'
  testResult.value = null
  testDialogVisible.value = true
}

// 发送测试
const handleTestSubmit = async () => {
  testLoading.value = true
  try {
    const res = await testProxy(currentProxy.value.id, {
      url: testForm.url,
      method: testForm.method,
      body: testForm.body
    })
    testResult.value = res.data
    ElMessage.success('测试请求已发送')
  } catch (error) {
    testResult.value = { error: error.message || '测试失败' }
    ElMessage.error(error.message || '测试失败')
  } finally {
    testLoading.value = false
  }
}

// 查看日志
const handleViewLogs = async (row) => {
  currentProxy.value = row
  logsDialogVisible.value = true
  logsLoading.value = true
  try {
    // 加载统计数据
    const statsRes = await getProxyStats(row.id)
    Object.assign(logStats, statsRes.data)

    // 加载日志列表
    const logsRes = await getProxyLogs(row.id, { page: 1, pageSize: 50 })
    logData.value = logsRes.data.list || logsRes.data
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    logsLoading.value = false
  }
}

// 查看日志详情
const handleViewLogDetail = (row) => {
  ElMessageBox.alert(
    `<pre>${JSON.stringify(row, null, 2)}</pre>`,
    '日志详情',
    {
      dangerouslyUseHTMLString: true
    }
  )
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.proxies-container {
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
  font-size: 24px;
  font-weight: 600;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.proxy-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.target-url {
  max-width: 200px;
}

.url-text {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.priority-value {
  color: #909399;
  font-weight: 600;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}

.test-form {
  margin-bottom: 20px;
}

.test-result {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}

.test-result h4 {
  margin: 0 0 10px 0;
}

.test-result pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.log-stats {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.log-table {
  max-height: 400px;
  overflow-y: auto;
}
</style>
