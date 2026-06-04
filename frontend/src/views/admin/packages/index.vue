<template>
  <div class="admin-packages">
    <h2 class="page-title">{{ $t('admin.packages') }}</h2>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="primary" @click="handleCreate">
            <i class="el-icon-plus"></i>
            {{ $t('admin.create') }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 套餐列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="packageList" border stripe style="width: 100%">
        <el-table-column type="index" :label="$t('admin.index')" width="60" />
        <el-table-column prop="name" :label="$t('admin.packageName')" min-width="150">
          <template slot-scope="{ row }">
            <div class="package-name">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.isPopular" size="mini" type="danger">热门</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('admin.packageType')" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.type === 'count' ? 'success' : 'primary'">
              {{ row.type === 'count' ? '计次套餐' : '包月套餐' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="price" :label="$t('admin.price')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.packageContent')" min-width="200">
          <template slot-scope="{ row }">
            <div v-if="row.type === 'count'">
              <span>{{ row.totalQuota }}次</span>
              <el-tag size="mini" style="margin-left: 8px">额度</el-tag>
            </div>
            <div v-else>
              <span>{{ row.validDays }}天</span>
              <el-tag size="mini" style="margin-left: 8px">有效期</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="modelLimit" :label="$t('admin.modelLimit')" width="150">
          <template slot-scope="{ row }">
            <span v-if="row.modelLimit">{{ row.modelLimit }}</span>
            <span v-else class="text-muted">不限</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" :label="$t('admin.sort')" width="80" align="center" />
        <el-table-column prop="salesCount" :label="$t('admin.salesCount')" width="100" align="center" />
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
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item :label="$t('admin.packageName')" prop="name">
          <el-input v-model="form.name" placeholder="例如: 基础套餐" />
        </el-form-item>
        <el-form-item :label="$t('admin.packageType')" prop="type">
          <el-radio-group v-model="form.type" @change="handleTypeChange">
            <el-radio label="count">计次套餐</el-radio>
            <el-radio label="subscription">包月套餐</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('admin.price')" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="form.type === 'count'" :label="$t('admin.totalQuota')" prop="totalQuota">
          <el-input-number v-model="form.totalQuota" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="form.type === 'subscription'" :label="$t('admin.validDays')" prop="validDays">
          <el-input-number v-model="form.validDays" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="$t('admin.modelLimit')">
          <el-select
            v-model="form.modelLimit"
            filterable
            allow-create
            default-first-option
            placeholder="留空表示不限"
            style="width: 100%"
          >
            <el-option label="不限" value="" />
            <el-option label="GPT-4系列" value="gpt-4,gpt-4-turbo" />
            <el-option label="Claude系列" value="claude-3" />
            <el-option label="全部模型" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('admin.isPopular')">
          <el-switch v-model="form.isPopular" />
        </el-form-item>
        <el-form-item :label="$t('admin.sort')">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item :label="$t('admin.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="$t('admin.status')">
          <el-switch v-model="form.enabled" />
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
import { getPackageList, createPackage, updatePackage, deletePackage } from '@/api/admin'

export default {
  name: 'AdminPackages',
  data() {
    return {
      loading: false,
      submitting: false,
      dialogVisible: false,
      isEdit: false,
      packageList: [],
      form: {
        id: null,
        name: '',
        type: 'count',
        price: 0,
        totalQuota: 0,
        validDays: 30,
        modelLimit: '',
        isPopular: false,
        sort: 0,
        description: '',
        enabled: true
      },
      rules: {
        name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择套餐类型', trigger: 'change' }],
        price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? this.$t('admin.editPackage') : this.$t('admin.createPackage')
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getPackageList()
        this.packageList = res.data || []
      } catch (error) {
        this.$message.error(this.$t('admin.loadFailed'))
      } finally {
        this.loading = false
      }
    },
    handleCreate() {
      this.isEdit = false
      this.form = {
        id: null,
        name: '',
        type: 'count',
        price: 0,
        totalQuota: 100,
        validDays: 30,
        modelLimit: '',
        isPopular: false,
        sort: 0,
        description: '',
        enabled: true
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.isEdit = true
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleTypeChange(type) {
      if (type === 'count') {
        this.form.totalQuota = 100
      } else {
        this.form.validDays = 30
      }
    },
    async handleSubmit() {
      try {
        await this.$refs.formRef.validate()
        this.submitting = true

        if (this.isEdit) {
          await updatePackage(this.form.id, this.form)
          this.$message.success(this.$t('admin.updateSuccess'))
        } else {
          await createPackage(this.form)
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
        await updatePackage(row.id, { enabled: row.enabled })
        this.$message.success(this.$t('admin.updateSuccess'))
      } catch (error) {
        row.enabled = !row.enabled
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该套餐吗？', '警告', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await deletePackage(row.id)
        this.$message.success(this.$t('admin.deleteSuccess'))
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('admin.operationFailed'))
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-packages {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .toolbar-card {
    margin-bottom: 20px;
  }

  .package-name {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .price {
    color: #409eff;
    font-weight: 500;
  }

  .text-muted {
    color: #909399;
  }

  .danger-text {
    color: #f56c6c;

    &:hover {
      color: #f78989;
    }
  }
}
</style>
