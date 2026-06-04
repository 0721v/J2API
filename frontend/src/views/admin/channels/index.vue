<template>
  <div class="admin-channels">
    <h2 class="page-title">{{ $t('admin.channels') }}</h2>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.keyword"
            :placeholder="$t('admin.searchPlaceholder')"
            clearable
            @keyup.enter.native="handleSearch"
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.type" placeholder="渠道类型" clearable>
            <el-option label="OpenAI" value="openai" />
            <el-option label="Azure" value="azure" />
            <el-option label="Anthropic" value="anthropic" />
            <el-option label="Google" value="gemini" />
            <el-option label="Groq" value="groq" />
            <el-option label="Cohere" value="cohere" />
            <el-option label="Mistral" value="mistral" />
            <el-option label="Custom" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" :placeholder="$t('admin.status')" clearable>
            <el-option :label="$t('admin.enabled')" value="true" />
            <el-option :label="$t('admin.disabled')" value="false" />
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
        <el-button type="success" @click="showTemplateDialog = true">
          <i class="el-icon-magic-stick"></i>
          快速添加（模板）
        </el-button>
        <el-button type="primary" @click="handleCreate">
          <i class="el-icon-plus"></i>
          {{ $t('admin.create') }}
        </el-button>
      </div>
    </el-card>

    <!-- 渠道列表 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="channelList"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="渠道名称" min-width="150">
          <template slot-scope="{ row }">
            <div class="channel-name">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.isDefault" size="mini" type="success">默认</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="getChannelTypeColor(row.type)">{{ getChannelTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="API地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="models" label="支持的模型" min-width="200">
          <template slot-scope="{ row }">
            <el-tag
              v-for="model in (row.supportedModels || []).slice(0, 3)"
              :key="model"
              size="small"
              style="margin-right: 5px"
            >
              {{ model }}
            </el-tag>
            <el-tag v-if="(row.supportedModels || []).length > 3" size="small">
              +{{ row.supportedModels.length - 3 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="权重" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="{ row }">
            <el-switch
              v-model="row.enabled"
              active-text="启用"
              inactive-text="禁用"
              @change="handleToggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button type="text" size="small" @click="handleViewLogs(row)">日志</el-button>
            <el-divider direction="vertical" />
            <el-button type="text" size="small" class="danger-text" @click="handleDelete(row)">删除</el-button>
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

    <!-- 模板快速添加对话框 -->
    <el-dialog
      title="快速添加渠道"
      :visible.sync="showTemplateDialog"
      width="900px"
      :close-on-click-modal="false"
    >
      <div class="template-grid">
        <div
          v-for="template in channelTemplates"
          :key="template.id"
          class="template-card"
          :class="{ selected: selectedTemplateId === template.id }"
          @click="selectTemplate(template)"
        >
          <div class="template-header">
            <el-tag :type="getChannelTypeColor(template.type)" size="small">
              {{ template.category.toUpperCase() }}
            </el-tag>
            <span class="template-name">{{ template.name }}</span>
          </div>
          <div class="template-desc">{{ template.description }}</div>
          <div class="template-models">
            <el-tag
              v-for="model in template.supportedModels.slice(0, 4)"
              :key="model"
              size="mini"
              style="margin-right: 4px; margin-bottom: 4px"
            >
              {{ model }}
            </el-tag>
            <el-tag v-if="template.supportedModels.length > 4" size="mini">
              +{{ template.supportedModels.length - 4 }}
            </el-tag>
          </div>
          <div class="template-footer">
            <el-link :href="template.docUrl" target="_blank" type="primary" :underline="false">
              文档 <i class="el-icon-link"></i>
            </el-link>
          </div>
        </div>
      </div>

      <!-- API Key输入 -->
      <div v-if="selectedTemplateId" class="api-key-input">
        <el-divider />
        <el-form label-width="100px">
          <el-form-item label="API密钥">
            <el-input
              v-model="templateApiKey"
              type="password"
              show-password
              placeholder="请输入您的API密钥"
            />
          </el-form-item>
        </el-form>
      </div>

      <span slot="footer">
        <el-button @click="showTemplateDialog = false">取消</el-button>
        <el-button type="primary" :loading="templateSubmitting" :disabled="!selectedTemplateId || !templateApiKey" @click="handleCreateFromTemplate">
          创建渠道
        </el-button>
      </span>
    </el-dialog>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="渠道名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择渠道类型" @change="handleTypeChange">
            <el-option label="OpenAI" value="openai" />
            <el-option label="Azure OpenAI" value="azure" />
            <el-option label="Anthropic Claude" value="anthropic" />
            <el-option label="Google Gemini" value="gemini" />
            <el-option label="Groq" value="groq" />
            <el-option label="Cohere" value="cohere" />
            <el-option label="Mistral AI" value="mistral" />
            <el-option label="Together AI" value="together" />
            <el-option label="Ollama (本地)" value="ollama" />
            <el-option label="Custom" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="API地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="请输入API地址">
            <template slot="append">
              <el-button @click="testConnection">测试</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="API密钥" prop="apiKey">
          <el-input
            v-model="form.apiKey"
            type="password"
            show-password
            placeholder="请输入API密钥"
          />
        </el-form-item>
        <el-form-item label="支持的模型" prop="models">
          <el-select
            v-model="form.models"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请选择或输入模型名称"
            style="width: 100%"
          >
            <el-option
              v-for="model in availableModels"
              :key="model"
              :label="model"
              :value="model"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权重" prop="weight">
              <el-input-number v-model="form.weight" :min="0" :max="100" />
              <span class="form-tip">值越大优先级越高</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="enabled">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ $t('admin.confirm') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getChannelList, createChannel, updateChannel, deleteChannel, getChannelTemplates, createChannelFromTemplate } from '@/api/admin'

export default {
  name: 'AdminChannels',
  data() {
    return {
      loading: false,
      submitting: false,
      dialogVisible: false,
      showTemplateDialog: false,
      templateSubmitting: false,
      isEdit: false,
      searchForm: {
        keyword: '',
        type: '',
        status: ''
      },
      channelList: [],
      channelTemplates: [],
      selectedTemplateId: null,
      selectedTemplate: null,
      templateApiKey: '',
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      form: {
        id: null,
        name: '',
        type: 'openai',
        baseUrl: '',
        apiKey: '',
        models: [],
        weight: 10,
        enabled: true,
        isDefault: false,
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
        baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }],
        apiKey: [{ required: true, message: '请输入API密钥', trigger: 'blur' }]
      },
      availableModels: [
        // OpenAI
        'gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'gpt-4', 'gpt-4-32k', 'gpt-3.5-turbo', 'gpt-3.5-turbo-16k',
        // Anthropic
        'claude-3-5-sonnet', 'claude-3-5-haiku', 'claude-3-opus', 'claude-3-sonnet', 'claude-3-haiku',
        // Google
        'gemini-2.0-flash', 'gemini-1.5-pro', 'gemini-1.5-flash', 'gemini-1.0-pro',
        // Groq
        'llama-3.3-70b', 'llama-3.1-70b', 'llama-3.1-8b', 'mixtral-8x7b', 'gemma2-9b-it',
        // Cohere
        'command-r-plus', 'command-r', 'command', 'embed-english-v3.0', 'embed-multilingual-v3.0',
        // Mistral
        'mistral-large-latest', 'mistral-medium-latest', 'mistral-small-latest', 'mistral-tiny-latest',
        // Ollama
        'llama3', 'llama3.1', 'mistral', 'mixtral', 'codellama', 'qwen2'
      ]
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑渠道' : '创建渠道'
    }
  },
  created() {
    this.loadData()
    this.loadTemplates()
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
        const res = await getChannelList(params)
        this.channelList = res.data?.records || []
        this.pagination.total = res.data?.total || 0
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    async loadTemplates() {
      try {
        const res = await getChannelTemplates()
        this.channelTemplates = res.data || []
      } catch (error) {
        console.error('加载模板失败', error)
      }
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { keyword: '', type: '', status: '' }
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
    selectTemplate(template) {
      this.selectedTemplateId = template.id
      this.selectedTemplate = template
    },
    async handleCreateFromTemplate() {
      if (!this.selectedTemplateId || !this.templateApiKey) return

      this.templateSubmitting = true
      try {
        await createChannelFromTemplate(this.selectedTemplateId, { apiKey: this.templateApiKey })
        this.$message.success('渠道创建成功')
        this.showTemplateDialog = false
        this.selectedTemplateId = null
        this.templateApiKey = ''
        this.loadData()
      } catch (error) {
        this.$message.error(error.message || '创建失败')
      } finally {
        this.templateSubmitting = false
      }
    },
    handleCreate() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        type: 'openai',
        baseUrl: '',
        apiKey: '',
        models: [],
        weight: 10,
        enabled: true,
        isDefault: false,
        description: ''
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleTypeChange(type) {
      const defaultUrls = {
        openai: 'https://api.openai.com/v1',
        anthropic: 'https://api.anthropic.com',
        gemini: 'https://generativelanguage.googleapis.com',
        groq: 'https://api.groq.com/openai/v1',
        cohere: 'https://api.cohere.ai',
        mistral: 'https://api.mistral.ai/v1',
        together: 'https://api.together.xyz/v1',
        ollama: 'http://localhost:11434/api',
        azure: 'https://your-resource.openai.azure.com',
        custom: ''
      }
      if (defaultUrls[type] && !this.form.baseUrl) {
        this.form.baseUrl = defaultUrls[type]
      }

      // 更新可用模型列表
      const modelMap = {
        openai: ['gpt-4o', 'gpt-4o-mini', 'gpt-4-turbo', 'gpt-4', 'gpt-3.5-turbo'],
        anthropic: ['claude-3-5-sonnet', 'claude-3-opus', 'claude-3-sonnet'],
        gemini: ['gemini-1.5-pro', 'gemini-1.5-flash', 'gemini-1.0-pro'],
        groq: ['llama-3.1-70b', 'llama-3.1-8b', 'mixtral-8x7b'],
        cohere: ['command-r-plus', 'command-r', 'command'],
        mistral: ['mistral-large-latest', 'mistral-medium-latest', 'mistral-small-latest'],
        together: ['meta-llama/Llama-3-70b-chat-hf', 'mistralai/Mixtral-8x22B-Instruct-v0.1'],
        ollama: ['llama3', 'mistral', 'mixtral', 'codellama']
      }
      if (modelMap[type]) {
        this.availableModels = [...new Set([...modelMap[type], ...this.availableModels])]
      }
    },
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true

        if (this.isEdit) {
          await updateChannel(this.form.id, this.form)
          this.$message.success('更新成功')
        } else {
          await createChannel(this.form)
          this.$message.success('创建成功')
        }

        this.dialogVisible = false
        this.loadData()
      } catch (error) {
        if (error !== false) {
          this.$message.error(error.message || '操作失败')
        }
      } finally {
        this.submitting = false
      }
    },
    async handleToggleStatus(row) {
      try {
        await updateChannel(row.id, { enabled: row.enabled })
        this.$message.success('更新成功')
      } catch (error) {
        row.enabled = !row.enabled
        this.$message.error('操作失败')
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该渠道吗？', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deleteChannel(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      }
    },
    handleViewLogs(row) {
      this.$router.push(`/admin/channels/${row.id}/logs`)
    },
    testConnection() {
      this.$message.info('连接测试功能开发中...')
    },
    getChannelTypeColor(type) {
      const colors = {
        openai: 'primary',
        azure: 'warning',
        anthropic: 'success',
        gemini: 'info',
        groq: 'success',
        cohere: '',
        mistral: 'warning',
        together: 'primary',
        ollama: 'info',
        custom: 'danger'
      }
      return colors[type] || ''
    },
    getChannelTypeName(type) {
      const names = {
        openai: 'OpenAI',
        azure: 'Azure',
        anthropic: 'Claude',
        gemini: 'Gemini',
        groq: 'Groq',
        cohere: 'Cohere',
        mistral: 'Mistral',
        together: 'Together',
        ollama: 'Ollama',
        custom: 'Custom'
      }
      return names[type] || type
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-channels {
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
      gap: 10px;
      margin-top: 10px;
    }
  }

  .channel-name {
    display: flex;
    align-items: center;
    gap: 8px;
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

  .form-tip {
    margin-left: 10px;
    color: #999;
    font-size: 12px;
  }
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  max-height: 500px;
  overflow-y: auto;

  .template-card {
    border: 2px solid #e8e8e8;
    border-radius: 8px;
    padding: 16px;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
      box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
    }

    &.selected {
      border-color: #409eff;
      background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
    }

    .template-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .template-name {
        font-weight: 600;
        font-size: 14px;
      }
    }

    .template-desc {
      color: #666;
      font-size: 12px;
      line-height: 1.5;
      margin-bottom: 12px;
      height: 36px;
      overflow: hidden;
    }

    .template-models {
      margin-bottom: 8px;
    }

    .template-footer {
      text-align: right;
      font-size: 12px;
    }
  }
}

.api-key-input {
  margin-top: 16px;
}
</style>
