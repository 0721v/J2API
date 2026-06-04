<template>
  <div class="admin-users">
    <h2 class="page-title">{{ $t('admin.users') }}</h2>

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
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column type="index" :label="$t('admin.index')" width="60" />
        <el-table-column :label="$t('admin.user')" min-width="200">
          <template slot-scope="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar">
                {{ row.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-detail">
                <div class="username">{{ row.username }}</div>
                <div class="email">{{ row.email }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="balance" :label="$t('admin.balance')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="balance">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tokenCount" :label="$t('admin.tokenCount')" width="100" align="center" />
        <el-table-column prop="role" :label="$t('admin.role')" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'success'">
              {{ row.role === 'admin' ? $t('admin.admin') : $t('admin.user') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('admin.status')" width="100" align="center">
          <template slot-scope="{ row }">
            <el-tag :type="row.disabled ? 'danger' : 'success'">
              {{ row.disabled ? $t('admin.disabled') : $t('admin.normal') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" :label="$t('admin.lastLogin')" width="180" />
        <el-table-column prop="createdAt" :label="$t('admin.createdAt')" width="180" />
        <el-table-column :label="$t('admin.actions')" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleViewDetail(row)">
              {{ $t('admin.detail') }}
            </el-button>
            <el-divider direction="vertical" />
            <el-dropdown trigger="click" @command="handleCommand($event, row)">
              <span class="el-dropdown-link">
                {{ $t('admin.more') }}<i class="el-icon-arrow-down el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="recharge">
                  <i class="el-icon-coin"></i>
                  {{ $t('admin.recharge') }}
                </el-dropdown-item>
                <el-dropdown-item command="resetPassword">
                  <i class="el-icon-key"></i>
                  {{ $t('admin.resetPassword') }}
                </el-dropdown-item>
                <el-dropdown-item v-if="!row.disabled" command="disable">
                  <i class="el-icon-close"></i>
                  {{ $t('admin.disable') }}
                </el-dropdown-item>
                <el-dropdown-item v-else command="enable">
                  <i class="el-icon-check"></i>
                  {{ $t('admin.enable') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
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

    <!-- 用户详情对话框 -->
    <el-dialog
      :title="$t('admin.userDetail')"
      :visible.sync="detailDialogVisible"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('admin.username')">
          {{ currentUser.username }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.email')">
          {{ currentUser.email }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.balance')">
          <span class="balance">¥{{ currentUser.balance }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.role')">
          {{ currentUser.role }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.tokenCount')">
          {{ currentUser.tokenCount }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.status')">
          <el-tag :type="currentUser.disabled ? 'danger' : 'success'">
            {{ currentUser.disabled ? $t('admin.disabled') : $t('admin.normal') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.registerIp')">
          {{ currentUser.registerIp || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.lastLoginIp')">
          {{ currentUser.lastLoginIp || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.createdAt')" :span="2">
          {{ currentUser.createdAt }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog
      :title="$t('admin.adminRecharge')"
      :visible.sync="rechargeDialogVisible"
      width="400px"
    >
      <el-form ref="rechargeFormRef" :model="rechargeForm" :rules="rechargeRules" label-width="80px">
        <el-form-item :label="$t('admin.targetUser')">
          <span>{{ currentUser.username }}</span>
        </el-form-item>
        <el-form-item :label="$t('admin.amount')" prop="amount">
          <el-input-number
            v-model="rechargeForm.amount"
            :min="1"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item :label="$t('admin.remark')">
          <el-input v-model="rechargeForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="rechargeDialogVisible = false">{{ $t('admin.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRecharge">
          {{ $t('admin.confirm') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, updateUserStatus, rechargeUser } from '@/api/admin'

export default {
  name: 'AdminUsers',
  data() {
    return {
      loading: false,
      submitting: false,
      detailDialogVisible: false,
      rechargeDialogVisible: false,
      searchForm: {
        keyword: ''
      },
      userList: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      currentUser: {},
      rechargeForm: {
        amount: 0,
        remark: ''
      },
      rechargeRules: {
        amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }]
      }
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
          keyword: this.searchForm.keyword
        }
        const res = await getUserList(params)
        this.userList = res.data?.records || []
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
      this.searchForm = { keyword: '' }
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
    handleViewDetail(row) {
      this.currentUser = { ...row }
      this.detailDialogVisible = true
    },
    handleCommand(command, row) {
      this.currentUser = { ...row }
      switch (command) {
        case 'recharge':
          this.rechargeDialogVisible = true
          this.rechargeForm = { amount: 0, remark: '' }
          break
        case 'resetPassword':
          this.handleResetPassword(row)
          break
        case 'disable':
          this.handleToggleStatus(row, true)
          break
        case 'enable':
          this.handleToggleStatus(row, false)
          break
      }
    },
    async handleRecharge() {
      try {
        await this.$refs.rechargeFormRef.validate()
        this.submitting = true
        await rechargeUser(this.currentUser.id, {
          amount: this.rechargeForm.amount,
          remark: this.rechargeForm.remark
        })
        this.$message.success(this.$t('admin.rechargeSuccess'))
        this.rechargeDialogVisible = false
        this.loadData()
      } catch (error) {
        if (error !== false) {
          this.$message.error(error.message || this.$t('admin.operationFailed'))
        }
      } finally {
        this.submitting = false
      }
    },
    async handleResetPassword(row) {
      try {
        await this.$confirm('确定要重置该用户的密码吗？新密码将发送至用户邮箱。', '确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await updateUserStatus(row.id, { action: 'resetPassword' })
        this.$message.success(this.$t('admin.resetPasswordSuccess'))
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('admin.operationFailed'))
        }
      }
    },
    async handleToggleStatus(row, disabled) {
      try {
        await updateUserStatus(row.id, { action: disabled ? 'disable' : 'enable' })
        this.$message.success(this.$t('admin.updateSuccess'))
        this.loadData()
      } catch (error) {
        this.$message.error(this.$t('admin.operationFailed'))
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-users {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .toolbar-card {
    margin-bottom: 20px;
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .user-detail {
      .username {
        font-weight: 500;
        color: #303133;
      }
      .email {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .balance {
    color: #409eff;
    font-weight: 500;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }

  .el-dropdown-link {
    cursor: pointer;
    color: #409eff;
  }
}
</style>
