<template>
  <div class="admin-orders">
    <h2 class="page-title">{{ $t('admin.orders') }}</h2>

    <!-- 工具栏 -->
    <el-card class="toolbar-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input
            v-model="searchForm.orderNo"
            :placeholder="$t('admin.orderNo')"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.type" :placeholder="$t('admin.orderType')" clearable>
            <el-option label="充值订单" value="recharge" />
            <el-option label="套餐订单" value="package" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.status" :placeholder="$t('admin.status')" clearable>
            <el-option label="待支付" value="pending" />
            <el-option label="已支付" value="paid" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange"
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

    <!-- 订单列表 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="orderList" border stripe style="width: 100%">
        <el-table-column type="index" :label="$t('admin.index')" width="60" />
        <el-table-column prop="orderNo" :label="$t('admin.orderNo')" width="200" show-overflow-tooltip />
        <el-table-column :label="$t('admin.user')" width="150">
          <template slot-scope="{ row }">
            <div class="user-info">
              <span>{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="$t('admin.orderType')" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.type === 'recharge' ? 'success' : 'primary'">
              {{ row.type === 'recharge' ? '充值' : '套餐' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.amount')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('admin.paidAmount')" width="120" align="center">
          <template slot-scope="{ row }">
            <span class="amount">¥{{ row.paidAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" :label="$t('admin.paymentMethod')" width="100">
          <template slot-scope="{ row }">
            {{ getPaymentMethodText(row.paymentMethod) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="$t('admin.orderStatus')" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="getOrderStatusType(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="$t('admin.createdAt')" width="180" />
        <el-table-column prop="paidAt" :label="$t('admin.paidAt')" width="180" />
        <el-table-column :label="$t('admin.actions')" width="120" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleViewDetail(row)">
              {{ $t('admin.detail') }}
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="text"
              size="small"
              @click="handleCancel(row)"
            >
              {{ $t('admin.cancel') }}
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

    <!-- 订单详情对话框 -->
    <el-dialog
      :title="$t('admin.orderDetail')"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="$t('admin.orderNo')" :span="2">
          {{ currentOrder.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.user')">
          {{ currentOrder.username }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.orderType')">
          {{ currentOrder.type === 'recharge' ? '充值' : '套餐' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.amount')">
          <span class="amount">¥{{ currentOrder.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.paidAmount')">
          <span class="amount">¥{{ currentOrder.paidAmount || 0 }}</span>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.paymentMethod')">
          {{ getPaymentMethodText(currentOrder.paymentMethod) }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.orderStatus')">
          <el-tag :type="getOrderStatusType(currentOrder.status)">
            {{ getOrderStatusText(currentOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.createdAt')">
          {{ currentOrder.createdAt }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.paidAt')">
          {{ currentOrder.paidAt || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.expireTime')">
          {{ currentOrder.expireTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item :label="$t('admin.transactionId')" :span="2">
          {{ currentOrder.transactionId || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script>
import { getOrderList } from '@/api/admin'

export default {
  name: 'AdminOrders',
  data() {
    return {
      loading: false,
      detailDialogVisible: false,
      searchForm: {
        orderNo: '',
        type: '',
        status: '',
        startDate: '',
        endDate: ''
      },
      dateRange: [],
      orderList: [],
      pagination: {
        page: 1,
        size: 10,
        total: 0
      },
      currentOrder: {}
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
        const res = await getOrderList(params)
        this.orderList = res.data?.records || []
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
      this.searchForm = { orderNo: '', type: '', status: '', startDate: '', endDate: '' }
      this.dateRange = []
      this.handleSearch()
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.searchForm.startDate = val[0]
        this.searchForm.endDate = val[1]
      } else {
        this.searchForm.startDate = ''
        this.searchForm.endDate = ''
      }
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
      this.currentOrder = { ...row }
      this.detailDialogVisible = true
    },
    async handleCancel(row) {
      try {
        await this.$confirm('确定要取消该订单吗？', '确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.$http.put(`/admin/orders/${row.id}/cancel`)
        this.$message.success(this.$t('admin.cancelSuccess'))
        this.loadData()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('admin.operationFailed'))
        }
      }
    },
    getPaymentMethodText(method) {
      const methods = {
        alipay: '支付宝',
        wechat: '微信支付',
        stripe: '信用卡',
        trc20: 'USDT'
      }
      return methods[method] || method
    },
    getOrderStatusType(status) {
      const types = {
        pending: 'warning',
        paid: 'success',
        cancelled: 'info',
        expired: 'danger'
      }
      return types[status] || ''
    },
    getOrderStatusText(status) {
      const texts = {
        pending: this.$t('admin.pending'),
        paid: this.$t('admin.paid'),
        cancelled: this.$t('admin.cancelled'),
        expired: this.$t('admin.expired')
      }
      return texts[status] || status
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-orders {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .toolbar-card {
    margin-bottom: 20px;
  }

  .amount {
    color: #409eff;
    font-weight: 500;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
  }
}
</style>
