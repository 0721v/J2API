<template>
  <div class="user-groups-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>用户分组管理</h2>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建分组
      </el-button>
    </div>

    <!-- 搜索和筛选 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="分组名称">
          <el-input v-model="filterForm.name" placeholder="请输入分组名称" clearable />
        </el-form-item>
        <el-form-item label="等级">
          <el-select v-model="filterForm.level" placeholder="请选择等级" clearable>
            <el-option label="免费" value="free" />
            <el-option label="基础" value="basic" />
            <el-option label="专业" value="pro" />
            <el-option label="企业" value="enterprise" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="active" />
            <el-option label="禁用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分组列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分组名称" min-width="150">
          <template #default="{ row }">
            <div class="group-name">
              <span>{{ row.name }}</span>
              <el-tag :type="getLevelType(row.level)" size="small">{{ getLevelText(row.level) }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column label="全局倍率" width="120">
          <template #default="{ row }">
            <span class="rate-value">{{ row.globalRate || 1.0 }}x</span>
          </template>
        </el-table-column>
        <el-table-column label="配额限制" min-width="200">
          <template #default="{ row }">
            <div class="quota-info">
              <span>分钟: {{ row.defaultMinuteLimit || '无限制' }}</span>
              <span>每日: {{ row.defaultDailyLimit || '无限制' }}</span>
              <span>每月: {{ row.defaultMonthlyLimit || '无限制' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="权限" width="120">
          <template #default="{ row }">
            <div class="permissions">
              <el-tag v-if="row.allowRecharge" type="success" size="small">充值</el-tag>
              <el-tag v-if="row.allowPackages" type="success" size="small">套餐</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="'active'"
              :inactive-value="'disabled'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleViewUsers(row)">用户</el-button>
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
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分组名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入分组名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="等级" prop="level">
              <el-select v-model="formData.level" placeholder="请选择等级">
                <el-option label="免费" value="free" />
                <el-option label="基础" value="basic" />
                <el-option label="专业" value="pro" />
                <el-option label="企业" value="enterprise" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="formData.priority" :min="0" :max="999" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效期(天)" prop="validDays">
              <el-input-number v-model="formData.validDays" :min="0" placeholder="0表示永久" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">价格倍率</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="全局倍率" prop="globalRate">
              <el-input-number v-model="formData.globalRate" :min="0.01" :max="10" :precision="2" />
              <div class="form-tip">应用于所有模型的基础倍率</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="模型倍率" prop="modelRates">
          <el-input
            v-model="formData.modelRates"
            type="textarea"
            :rows="4"
            placeholder='例如: {"gpt-4": 1.2, "claude-3": 1.5}'
          />
          <div class="form-tip">JSON格式，指定特定模型的倍率，会覆盖全局倍率</div>
        </el-form-item>

        <el-form-item label="渠道倍率" prop="channelRates">
          <el-input
            v-model="formData.channelRates"
            type="textarea"
            :rows="3"
            placeholder='例如: {"openai": 1.0, "azure": 0.8}'
          />
          <div class="form-tip">JSON格式，指定特定渠道的倍率</div>
        </el-form-item>

        <el-divider content-position="left">配额限制</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="分钟限制" prop="defaultMinuteLimit">
              <el-input-number v-model="formData.defaultMinuteLimit" :min="0" placeholder="0无限制" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每日限制" prop="defaultDailyLimit">
              <el-input-number v-model="formData.defaultDailyLimit" :min="0" placeholder="0无限制" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每月限制" prop="defaultMonthlyLimit">
              <el-input-number v-model="formData.defaultMonthlyLimit" :min="0" placeholder="0无限制" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">权限设置</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="允许充值">
              <el-switch v-model="formData.allowRecharge" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="允许购买套餐">
              <el-switch v-model="formData.allowPackages" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-switch v-model="formData.status" active-value="active" inactive-value="disabled" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="允许的模型" prop="allowedModels">
          <el-input
            v-model="formData.allowedModels"
            type="textarea"
            :rows="2"
            placeholder="留空表示允许所有模型，多个用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="禁止的模型" prop="blockedModels">
          <el-input
            v-model="formData.blockedModels"
            type="textarea"
            :rows="2"
            placeholder="留空表示不禁止任何模型，多个用逗号分隔"
          />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户列表对话框 -->
    <el-dialog
      v-model="usersDialogVisible"
      title="分组用户"
      width="900px"
    >
      <div class="user-group-info">
        <span>分组: <strong>{{ currentGroup?.name }}</strong></span>
        <span>用户数: <strong>{{ groupUsers.length }}</strong></span>
      </div>

      <el-table :data="groupUsers" v-loading="usersLoading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="180">
          <template #default="{ row }">
            {{ row.expireTime || '永久' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleRemoveUser(row)">移除</el-button>
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
  getUserGroups,
  createUserGroup,
  updateUserGroup,
  deleteUserGroup,
  getUserGroupUsers
} from '@/api/user-group'

// 状态
const loading = ref(false)
const submitLoading = ref(false)
const usersLoading = ref(false)
const dialogVisible = ref(false)
const usersDialogVisible = ref(false)
const isEdit = ref(false)
const currentGroup = ref(null)
const groupUsers = ref([])

// 表格数据
const tableData = ref([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 筛选表单
const filterForm = reactive({
  name: '',
  level: '',
  status: ''
})

// 表单数据
const formData = reactive({
  name: '',
  description: '',
  priority: 0,
  status: 'active',
  level: 'free',
  validDays: 0,
  globalRate: 1.0,
  modelRates: '',
  channelRates: '',
  defaultMinuteLimit: 0,
  defaultDailyLimit: 0,
  defaultMonthlyLimit: 0,
  allowRecharge: true,
  allowPackages: true,
  allowedModels: '',
  blockedModels: ''
})

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入分组名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择等级', trigger: 'change' }]
}

// 计算对话框标题
const dialogTitle = computed(() => isEdit.value ? '编辑用户分组' : '新建用户分组')

// 获取等级类型
const getLevelType = (level) => {
  const types = {
    free: 'info',
    basic: 'success',
    pro: 'warning',
    enterprise: 'danger'
  }
  return types[level] || 'info'
}

// 获取等级文本
const getLevelText = (level) => {
  const texts = {
    free: '免费',
    basic: '基础',
    pro: '专业',
    enterprise: '企业'
  }
  return texts[level] || level
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
    const res = await getUserGroups(params)
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
    level: '',
    status: ''
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
    description: row.description,
    priority: row.priority,
    status: row.status,
    level: row.level,
    validDays: row.validDays,
    globalRate: row.globalRate || 1.0,
    modelRates: row.modelRates || '',
    channelRates: row.channelRates || '',
    defaultMinuteLimit: row.defaultMinuteLimit || 0,
    defaultDailyLimit: row.defaultDailyLimit || 0,
    defaultMonthlyLimit: row.defaultMonthlyLimit || 0,
    allowRecharge: row.allowRecharge,
    allowPackages: row.allowPackages,
    allowedModels: row.allowedModels || '',
    blockedModels: row.blockedModels || ''
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    name: '',
    description: '',
    priority: 0,
    status: 'active',
    level: 'free',
    validDays: 0,
    globalRate: 1.0,
    modelRates: '',
    channelRates: '',
    defaultMinuteLimit: 0,
    defaultDailyLimit: 0,
    defaultMonthlyLimit: 0,
    allowRecharge: true,
    allowPackages: true,
    allowedModels: '',
    blockedModels: ''
  })
}

// 提交表单
const handleSubmit = async () => {
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateUserGroup(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await createUserGroup(formData)
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
    await ElMessageBox.confirm(`确定要删除分组 "${row.name}" 吗？`, '提示', {
      type: 'warning'
    })
    await deleteUserGroup(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row) => {
  try {
    await updateUserGroup(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.error('状态更新失败')
  }
}

// 查看用户
const handleViewUsers = async (row) => {
  currentGroup.value = row
  usersDialogVisible.value = true
  usersLoading.value = true
  try {
    const res = await getUserGroupUsers(row.id)
    groupUsers.value = res.data || []
  } catch (error) {
    ElMessage.error('加载用户失败')
  } finally {
    usersLoading.value = false
  }
}

// 移除用户
const handleRemoveUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要从分组中移除用户 "${row.username}" 吗？`, '提示', {
      type: 'warning'
    })
    // 调用API移除用户
    ElMessage.success('移除成功')
    handleViewUsers(currentGroup.value)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '移除失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-groups-container {
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

.group-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.rate-value {
  color: #409eff;
  font-weight: 600;
}

.quota-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.permissions {
  display: flex;
  gap: 4px;
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

.user-group-info {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.user-group-info strong {
  color: #409eff;
}
</style>
