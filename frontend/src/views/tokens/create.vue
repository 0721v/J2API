<template>
  <div class="token-create-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>创建API密钥</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="密钥名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：我的第一个密钥" />
        </el-form-item>
        
        <el-form-item label="令牌分组">
          <el-select v-model="form.groupId" placeholder="选择分组（可选）" clearable>
            <el-option label="默认分组" :value="1" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="允许的模型">
          <el-select v-model="form.allowedModels" multiple placeholder="留空表示全部模型" clearable>
            <el-option label="全部模型" value="" />
            <el-option label="GPT-4" value="gpt-4,gpt-4-turbo" />
            <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
            <el-option label="Claude 3.5" value="claude-3-5-sonnet-20241022" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="每分钟限制">
          <el-input-number v-model="form.minuteLimit" :min="0" :max="10000" />
          <span class="help-text">0表示不限制</span>
        </el-form-item>
        
        <el-form-item label="每日限制">
          <el-input-number v-model="form.dayLimit" :min="0" :max="100000" />
          <span class="help-text">0表示不限制</span>
        </el-form-item>
        
        <el-form-item label="额度上限">
          <el-input-number v-model="form.quotaLimit" :min="0" :precision="2" placeholder="0表示无限制" />
          <span class="help-text">单位：元，0表示无限制</span>
        </el-form-item>
        
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expiresAt"
            type="datetime"
            placeholder="选择过期时间"
            clearable
          />
          <span class="help-text">留空表示永不过期</span>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            创建密钥
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 创建成功弹窗 -->
    <el-dialog v-model="showSuccessDialog" title="密钥创建成功" width="500px" :close-on-click-modal="false">
      <div class="success-content">
        <p>请妥善保管您的API密钥，密钥只会显示一次！</p>
        <div class="api-key-box">
          <code>{{ createdApiKey }}</code>
          <el-button size="small" @click="copyKey">复制</el-button>
        </div>
        <el-alert type="warning" :closable="false">
          密钥创建后无法找回，请立即保存！
        </el-alert>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleDone">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createToken } from '@/api/token'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const showSuccessDialog = ref(false)
const createdApiKey = ref('')

const form = reactive({
  name: '',
  groupId: null,
  allowedModels: '',
  minuteLimit: 0,
  dayLimit: 0,
  quotaLimit: 0,
  expiresAt: null
})

const rules = {
  name: [{ required: true, message: '请输入密钥名称', trigger: 'blur' }]
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  submitting.value = true
  try {
    const res = await createToken({
      name: form.name,
      groupId: form.groupId,
      allowedModels: Array.isArray(form.allowedModels) ? form.allowedModels.join(',') : form.allowedModels,
      minuteLimit: form.minuteLimit,
      dayLimit: form.dayLimit,
      quotaLimit: form.quotaLimit * 100, // 转换为分
      expiresAt: form.expiresAt
    })
    
    if (res.code === 200) {
      createdApiKey.value = res.data.apiKey
      showSuccessDialog.value = true
    }
  } catch (error) {
    console.error('Create token failed:', error)
  } finally {
    submitting.value = false
  }
}

async function copyKey() {
  try {
    await navigator.clipboard.writeText(createdApiKey.value)
    ElMessage.success('密钥已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

function handleDone() {
  showSuccessDialog.value = false
  router.push('/tokens')
}
</script>

<style lang="scss" scoped>
.token-create-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.help-text {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}

.success-content {
  p {
    margin-bottom: 15px;
    color: #606266;
  }
}

.api-key-box {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 20px 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  
  code {
    flex: 1;
    font-family: monospace;
    font-size: 14px;
    word-break: break-all;
  }
}
</style>
