<template>
  <div class="announcement-admin">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          发布公告
        </el-button>
        <el-button @click="loadAnnouncements">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部公告" name="all" />
          <el-tab-pane label="历史公告" name="history" />
        </el-tabs>
      </div>
    </div>

    <!-- 公告列表 -->
    <el-table :data="announcements" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getTypeTag(row.type)" size="small">{{ row.typeLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="100" sortable />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : 'info'" size="small">
            {{ row.statusLabel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="80" />
      <el-table-column label="发布时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.publishedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'draft'" type="success" size="small" link @click="handlePublish(row)">发布</el-button>
          <el-button v-if="row.status === 'published'" type="warning" size="small" link @click="handleArchive(row)">归档</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑公告' : '发布公告'"
      width="700px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="公告类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型">
            <el-option label="信息" value="info" />
            <el-option label="通知" value="notice" />
            <el-option label="警告" value="warning" />
            <el-option label="重要" value="important" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="form.priority" :min="0" :max="100" />
          <span class="form-tip">数值越大越靠前显示</span>
        </el-form-item>
        <el-form-item label="发布对象" prop="targetType">
          <el-select v-model="form.targetType" placeholder="请选择发布对象">
            <el-option label="全部用户" value="all" />
            <el-option label="所有用户" value="all_users" />
            <el-option label="所有代理商" value="all_agents" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期时间" prop="expiredAt">
          <el-date-picker
            v-model="form.expiredAt"
            type="datetime"
            placeholder="不设置则永不过期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="公告内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入公告内容，支持HTML格式"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAdminAnnouncements,
  getAdminAnnouncementHistory,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement,
  archiveAnnouncement
} from '@/api/admin-announcement'
import { Plus, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const announcements = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('all')
const dialogVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  title: '',
  content: '',
  type: 'info',
  priority: 0,
  targetType: 'all',
  expiredAt: null
})

const rules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
  type: [{ required: true, message: '请选择公告类型', trigger: 'change' }]
}

function getTypeTag(type) {
  const map = { important: 'danger', warning: 'warning', notice: 'success', info: 'primary' }
  return map[type] || 'info'
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const api = activeTab.value === 'history' ? getAdminAnnouncementHistory : getAdminAnnouncements
    const res = await api({ page: currentPage.value, pageSize: pageSize.value })
    if (res.code === 200) {
      announcements.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  loadAnnouncements()
}

function handleSizeChange(size) {
  pageSize.value = size
  loadAnnouncements()
}

function handlePageChange(page) {
  currentPage.value = page
  loadAnnouncements()
}

function handleCreate() {
  isEdit.value = false
  Object.assign(form, { title: '', content: '', type: 'info', priority: 0, targetType: 'all', expiredAt: null })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    title: row.title,
    content: row.content,
    type: row.type,
    priority: row.priority,
    targetType: row.targetType,
    expiredAt: row.expiredAt
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const api = isEdit.value ? updateAnnouncement : createAnnouncement
    const data = { ...form }
    if (isEdit.value) {
      // 编辑时需要ID，但当前实现通过URL传参
    }
    const res = await api(form.title ? 0 : 0, data) // TODO: 修复API参数
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '保存成功' : '发布成功')
      dialogVisible.value = false
      loadAnnouncements()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

async function handlePublish(row) {
  try {
    await ElMessageBox.confirm('确定要发布此公告吗？', '提示', { type: 'warning' })
    const res = await publishAnnouncement(row.id)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadAnnouncements()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('发布失败')
  }
}

async function handleArchive(row) {
  try {
    await ElMessageBox.confirm('确定要归档此公告吗？', '提示', { type: 'warning' })
    const res = await archiveAnnouncement(row.id)
    if (res.code === 200) {
      ElMessage.success('归档成功')
      loadAnnouncements()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('归档失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除此公告吗？此操作不可恢复', '警告', { type: 'error' })
    const res = await deleteAnnouncement(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadAnnouncements()
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-admin {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .toolbar-left {
      display: flex;
      gap: 12px;
    }
  }

  .form-tip {
    margin-left: 12px;
    color: #999;
    font-size: 12px;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
