<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>注册</h1>
        <p>创建您的API Platform账户</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <!-- 邀请码输入区域 -->
        <el-form-item :error="inviteCodeError">
          <el-input
            v-model="form.inviteCode"
            placeholder="邀请码（选填）"
            size="large"
            prefix-icon="Gift"
            clearable
            @blur="handleInviteCodeBlur"
            @input="handleInviteCodeInput"
          >
            <template #suffix>
              <el-icon v-if="inviteCodeValidating" class="is-loading">
                <Loading />
              </el-icon>
              <el-icon v-else-if="inviteCodeValid" color="#67C23A">
                <CircleCheck />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <!-- 邀请奖励预览 -->
        <div v-if="inviteRewards.length > 0" class="invite-reward-preview">
          <div class="reward-title">
            <el-icon><Gift /></el-icon>
            <span>使用邀请码可获得以下奖励</span>
          </div>
          <div class="reward-list">
            <div v-for="reward in inviteRewards" :key="reward.id" class="reward-item">
              <span class="reward-name">{{ reward.name }}</span>
              <span class="reward-value">
                <template v-if="reward.type === 'register'">
                  注册即得 {{ reward.inviteeRewardValue }} 积分
                </template>
                <template v-else-if="reward.type === 'recharge'">
                  首充返 {{ reward.inviteeRewardPercent / 100 }}%
                </template>
                <template v-else-if="reward.type === 'consumption'">
                  消费返 {{ reward.inviteeRewardPercent / 100 }}%
                </template>
              </span>
            </div>
          </div>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <router-link to="/login">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { validateInviteCode, getInviteRewards } from '@/api/user'
import { ElMessage } from 'element-plus'
import { Gift, CircleCheck, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const inviteCodeValidating = ref(false)
const inviteCodeValid = ref(false)
const inviteCodeError = ref('')
const inviteRewards = ref([])

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  inviteCode: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载奖励配置
async function loadInviteRewards() {
  try {
    const res = await getInviteRewards()
    if (res.code === 200 && res.data) {
      inviteRewards.value = res.data.filter(r => r.inviteeRewardValue > 0 || r.inviteeRewardPercent > 0)
    }
  } catch (error) {
    console.error('Failed to load invite rewards:', error)
  }
}

// 验证邀请码
async function validateInviteCodeRemote(code) {
  if (!code || code.trim() === '') {
    inviteCodeValid.value = false
    inviteCodeError.value = ''
    return
  }

  inviteCodeValidating.value = true
  inviteCodeError.value = ''

  try {
    const res = await validateInviteCode(code)
    if (res.code === 200 && res.data?.valid) {
      inviteCodeValid.value = true
      inviteCodeError.value = ''
    } else {
      inviteCodeValid.value = false
      inviteCodeError.value = res.message || '邀请码无效'
    }
  } catch (error) {
    inviteCodeValid.value = false
    inviteCodeError.value = '邀请码验证失败'
  } finally {
    inviteCodeValidating.value = false
  }
}

// 邀请码输入处理
let validateTimer = null
function handleInviteCodeInput() {
  inviteCodeValid.value = false
  inviteCodeError.value = ''

  // 防抖处理
  if (validateTimer) {
    clearTimeout(validateTimer)
  }

  if (form.inviteCode && form.inviteCode.length >= 6) {
    validateTimer = setTimeout(() => {
      validateInviteCodeRemote(form.inviteCode)
    }, 500)
  }
}

// 邀请码失焦验证
function handleInviteCodeBlur() {
  if (form.inviteCode && form.inviteCode.length >= 6) {
    validateInviteCodeRemote(form.inviteCode)
  }
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  // 如果填写了邀请码，验证其有效性
  if (form.inviteCode && !inviteCodeValid.value) {
    await validateInviteCodeRemote(form.inviteCode)
    if (!inviteCodeValid.value) {
      ElMessage.error('请输入有效的邀请码或留空')
      return
    }
  }

  loading.value = true
  try {
    const success = await userStore.register(
      form.username,
      form.email,
      form.password,
      form.inviteCode || null
    )
    if (success) {
      ElMessage.success('注册成功，欢迎加入！')
      // 清除URL中的邀请码参数
      if (route.query.invite) {
        router.replace({ query: {} })
      }
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  // 从URL读取邀请码
  const urlInviteCode = route.query.invite
  if (urlInviteCode) {
    form.inviteCode = urlInviteCode
    validateInviteCodeRemote(urlInviteCode)
  }

  // 加载奖励配置
  loadInviteRewards()
})
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  h1 {
    font-size: 28px;
    color: #333;
    margin: 0 0 10px;
  }

  p {
    color: #666;
    font-size: 14px;
    margin: 0;
  }
}

.register-form {
  .register-button {
    width: 100%;
  }
}

// 邀请奖励预览样式
.invite-reward-preview {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;

  .reward-title {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #0284c7;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 10px;

    .el-icon {
      font-size: 16px;
    }
  }

  .reward-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .reward-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;

    .reward-name {
      color: #64748b;
    }

    .reward-value {
      color: #0284c7;
      font-weight: 500;
    }
  }
}

.register-footer {
  text-align: center;
  margin-top: 20px;

  a {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

// 响应式
@media (max-width: 480px) {
  .register-box {
    width: 100%;
    margin: 20px;
    padding: 24px;
  }
}
</style>
