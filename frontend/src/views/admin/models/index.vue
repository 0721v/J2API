<template>
  <div class="admin-models">
    <h2 class="page-title">{{ $t('admin.models') }}</h2>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            :placeholder="$t('admin.searchPlaceholder')"
            clearable
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.provider" :placeholder="$t('admin.provider')" clearable>
            <el-option label="OpenAI" value="openai" />
            <el-option label="Anthropic" value="anthropic" />
            <el-option label="Google" value="google" />
            <el-option label="Cohere" value="cohere" />
            <el-option label="Jina" value="jina" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <i class="el-icon-search"></i>
            {{ $t('admin.search') }}
          </el-button>
          <el-button @click="handleReset">
            <i class="el-icon-refresh"></i>
            {{ $t('admin.reset') }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="toolbar-actions">
        <el-button type="primary" @click="handleCreate">
          <i class="el-icon-plus"></i>
          {{ $t('admin.create') }}
        </el-button>
      </div>
    </el-card>

    <!-- 模型列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="modelList" border stripe style="width: 100%">
        <el-table-column type="index" :label="$t('admin.index')" width="60" />
        <el-table-column prop="name" :label="$t('admin.modelName')" min-width="150">
          <template slot-scope="{ row }">
            <div class="model-name">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.isRecommended" size="mini" type="success">推荐</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="provider" :label="$t('admin.provider')" width="120">
          <template slot-scope="{ row }">
            <el-tag>{{ row.provider }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('admin.modelType')" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="getModelTypeColor(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.inputPrice')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="price">¥{{ row.inputPrice }}/1K</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.outputPrice')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="price">¥{{ row.outputPrice }}/1K</span>
          </template>
        </el-table-column>
        <el-table-column prop="contextLength" :label="$t('admin.contextLength')" width="120" align="center" />
        <el-table-column prop="status" :label="$t('admin.status')" width="100" align="center">
          <template slot-scope="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.actions')" width="150" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">
              {{ $t('admin.edit') }}
            </el-button>
            <el-divider direction="vertical" />
            <el-button type="text" size="small" class="danger-text" @click="handleDelete(row)">
              {{ $t('admin.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('admin.modelName')" prop="name">
          <el-input v-model="form.name" placeholder="例如: gpt-4o" />
        </el-form-item>
        <el-form-item :label="$t('admin.provider')" prop="provider">
          <el-select v-model="form.provider" style="width: 100%">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Anthropic" value="anthropic" />
            <el-option label="Google" value="google" />
            <el-option label="Cohere" value="cohere" />
            <el-option label="Jina" value="jina" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.modelType')" prop="type">
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="Chat" value="chat" />
            <el-option label="Completion" value="completion" />
            <el-option label="Embedding" value="embedding" />
            <el-option label="Rerank" value="rerank" />
            <el-option label="Vision" value="vision" />
            <el-option label="Realtime" value="realtime" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item :label="$t('admin.inputPrice')" prop="inputPrice">
              <el-input-number v-model="form.inputPrice" :min="0" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="$t('admin.outputPrice')" prop="outputPrice">
              <el-input-number v-model="form.outputPrice" :min="0" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('admin.contextLength')" prop="contextLength">
          <el-input-number v-model="form.contextLength" :min="0" :step="1000" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('admin.isRecommended')">
          <el-switch v-model="form.isRecommended" />
        </el-form-item>
        <el-form-item :label="$t('admin.status')">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item :label="$t('admin.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">{{ $t('admin.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ $t('admin.confirm') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getModelList, createModel, updateModel, deleteModel } from '@/api/admin'

export default {
  name: 'AdminModels',
  data() {
    return {
      loading: false,
      submitting: false,
      dialogVisible: false,
      isEdit: false,
      searchForm: {
        keyword: '',
        provider: ''
      },
      modelList: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      form: {
        id: null,
        name: '',
        provider: 'openai',
        type: 'chat',
        inputPrice: 0,
        outputPrice: 0,
        contextLength: 128000,
        isRecommended: false,
        enabled: true,
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
        provider: [{ required: true, message: '请选择提供商', trigger: 'change' }],
        type: [{ required: true, message: '请选择模型类型', trigger: 'change' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? this.$t('admin.editModel') : this.$t('admin.createModel')
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          size: this.pagination.size,
          ...this.searchForm
        }
        const res = await getModelList(params)
        this.modelList = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        this.$message.error(this.$t('admin.loadFailed'))
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { keyword: '', provider: '' }
      this.handleSearch()
    },
    handleSizeChange(size) {
      this.pagination.size = size
      this.loadData()
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    handleCreate() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        provider: 'openai',
        type: 'chat',
        inputPrice: 0,
        outputPrice: 0,
        contextLength: 128000,
        isRecommended: false,
        enabled: true,
        description: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true

        if (this.isEdit) {
          await updateModel(this.form.id, this.form)
          this.$message.success(this.$t('admin.updateSuccess'))
        } else {
          await createModel(this.form)
          this.$message.success(this.$t('admin.createSuccess'))
        }

        this.dialogVisible = false
        this.loadData()
      } catch (error) {
        if (error !== false) {
          this.$message.error(error.message || this.$t('admin.operationFailed'))
        }
      } finally {
        this.submitting = false
      }
    },
    async handleToggleStatus(row) {
      try {
        await updateModel(row.id, { enabled: row.enabled })
        this.$message.success(this.$t('admin.updateSuccess'))
      } catch (error) {
        row.enabled = !row.enabled
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该模型吗？', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteModel(row.id)
        this.$message.success(this.$t('admin.deleteSuccess'))
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('admin.operationFailed'))
        }
      }
    },
    getModelTypeColor(type) {
      const colors = {
        chat: 'primary',
        completion: 'success',
        embedding: 'warning',
        rerank: 'info',
        vision: '',
        realtime: 'danger'
      }
      return colors[type] || ''
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-models {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .toolbar-card {
    margin-bottom: 20px;

    .toolbar-actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 10px;
    }
  }

  .model-name {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .price {
    font-family: monospace;
    color: #409eff;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  .danger-text {
    color: #f56c6c;

    &:hover {
      color: #f78989;
    }
  }
}
</style>
