<template>
  <div class="announcement-list">
    <!-- 公告列表 -->
    <div v-if="announcements.length > 0" class="announcement-cards">
      <div
        v-for="item in announcements"
        :key="item.id"
        class="announcement-card"
        :class="{ unread: !item.read, [`type-${item.type}`]: true }"
        @click="viewDetail(item)"
      >
        <div class="card-header">
          <el-tag :type="getTypeTag(item.type)" size="small">
            {{ item.typeLabel }}
          </el-tag>
          <span v-if="!item.read" class="unread-dot"></span>
        </div>
        <h3 class="card-title">{{ item.title }}</h3>
        <div class="card-content" v-html="item.content"></div>
        <div class="card-footer">
          <span class="card-time">{{ formatTime(item.publishedAt) }}</span>
          <span class="card-views"><el-icon><View /></el-icon> {{ item.viewCount }}</span>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="暂无公告" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadAnnouncements"
      />
    </div>

    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentAnnouncement?.title"
      width="600px"
      destroy-on-close
    >
      <div class="detail-content">
        <div class="detail-meta">
          <el-tag :type="getTypeTag(currentAnnouncement?.type)" size="small">
            {{ currentAnnouncement?.typeLabel }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentAnnouncement?.publishedAt) }}</span>
          <span class="detail-views"><el-icon><View /></el-icon> {{ currentAnnouncement?.viewCount }}次阅读</span>
        </div>
        <el-divider />
        <div class="detail-body" v-html="currentAnnouncement?.content"></div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAnnouncements, getAnnouncementDetail, markAnnouncementRead } from '@/api/announcement'
import { ElMessage } from 'element-plus'
import { View } from '@element-plus/icons-vue'

const announcements = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const detailVisible = ref(false)
const currentAnnouncement = ref(null)

function getTypeTag(type) {
  const map = {
    important: 'danger',
    warning: 'warning',
    notice: 'success',
    info: 'primary'
  }
  return map[type] || 'info'
}

function formatTime(time) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadAnnouncements() {
  try {
    const res = await getAnnouncements({ page: currentPage.value, pageSize: pageSize.value })
    if (res.code === 200) {
      announcements.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    console.error('Failed to load announcements:', error)
  }
}

async function viewDetail(item) {
  try {
    const res = await getAnnouncementDetail(item.id)
    if (res.code === 200) {
      currentAnnouncement.value = res.data
      detailVisible.value = true

      // 标记已读
      if (!item.read) {
        await markAnnouncementRead(item.id)
        item.read = true
      }
    }
  } catch (error) {
    ElMessage.error('获取公告详情失败')
  }
}

onMounted(() => {
  loadAnnouncements()
})
</script>

<style lang="scss" scoped>
.announcement-list {
  .announcement-cards {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .announcement-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s;
    border: 1px solid #e8e8e8;

    &:hover {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      transform: translateY(-2px);
    }

    &.unread {
      border-left: 3px solid #409eff;
      background: linear-gradient(90deg, #f0f9ff 0%, white 20%);
    }

    &.type-important {
      border-left-color: #f56c6c;
      background: linear-gradient(90deg, #fef0f0 0%, white 20%);
    }

    &.type-warning {
      border-left-color: #e6a23c;
      background: linear-gradient(90deg, #fdf6ec 0%, white 20%);
    }

    &.type-notice {
      border-left-color: #67c23a;
      background: linear-gradient(90deg, #f0f9eb 0%, white 20%);
    }
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;

    .unread-dot {
      width: 8px;
      height: 8px;
      background: #409eff;
      border-radius: 50%;
    }
  }

  .card-title {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin: 0 0 12px;
    line-height: 1.4;
  }

  .card-content {
    color: #666;
    font-size: 14px;
    line-height: 1.6;
    max-height: 60px;
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .card-time {
      color: #999;
      font-size: 12px;
    }

    .card-views {
      display: flex;
      align-items: center;
      gap: 4px;
      color: #999;
      font-size: 12px;

      .el-icon {
        font-size: 14px;
      }
    }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 24px;
  }
}

.detail-content {
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #666;
    font-size: 13px;

    .detail-time {
      color: #999;
    }

    .detail-views {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .detail-body {
    color: #333;
    line-height: 1.8;
    font-size: 15px;
  }
}
</style>
