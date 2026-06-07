<template>
  <div class="announcements-page">
    <div class="page-header">
      <h2>系统公告</h2>
    </div>

    <el-card v-loading="loading">
      <el-timeline v-if="announcements.length > 0">
        <el-timeline-item
          v-for="item in announcements"
          :key="item.id"
          :type="item.isImportant ? 'danger' : 'primary'"
          :timestamp="formatDate(item.createdAt)"
        >
          <div class="announcement-item">
            <h4>{{ item.title }}</h4>
            <p>{{ item.content }}</p>
            <el-tag v-if="item.isImportant" type="danger" size="small">重要</el-tag>
          </div>
        </el-timeline-item>
      </el-timeline>

      <el-empty v-else description="暂无公告" />

      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadAnnouncements"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAnnouncements } from '@/api/announcement'

const loading = ref(false)
const announcements = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

async function loadAnnouncements() {
  loading.value = true
  try {
    const res = await getAnnouncements({ page: page.value, pageSize: pageSize.value })
    if (res.code === 200) {
      announcements.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcements-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .announcement-item {
    h4 {
      margin: 0 0 8px;
      font-size: 16px;
      font-weight: 600;
    }

    p {
      margin: 0 0 8px;
      color: #666;
      line-height: 1.6;
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
