<template>
  <div class="settings-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>个人设置</span>
          </template>
          
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="profile">
              <el-form ref="profileFormRef" :model="profileForm" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" disabled />
                </el-form-item>
                <el-form-item label="显示名称">
                  <el-input v-model="profileForm.displayName" />
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile">保存</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="password">
              <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="偏好设置" name="preferences">
              <el-form label-width="100px">
                <el-form-item label="语言">
                  <el-select v-model="preferences.language" @change="handleLanguageChange">
                    <el-option label="简体中文" value="zh-CN" />
                    <el-option label="English" value="en-US" />
                  </el-select>
                </el-form-item>
                <el-form-item label="主题">
                  <el-select v-model="preferences.theme" @change="handleThemeChange">
                    <el-option label="浅色" value="light" />
                    <el-option label="深色" value="dark" />
                  </el-select>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>账户信息</span>
          </template>
          <div class="account-info">
            <div class="info-item">
              <span class="label">用户ID</span>
              <span class="value">{{ userStore.userInfo?.id }}</span>
            </div>
            <div class="info-item">
              <span class="label">注册时间</span>
              <span class="value">{{ userStore.userInfo?.createdAt }}</span>
            </div>
            <div class="info-item">
              <span class="label">最后登录</span>
              <span class="value">{{ userStore.userInfo?.lastLoginTime }}</span>
            </div>
            <div class="info-item">
              <span class="label">累计消费</span>
              <span class="value">¥{{ formatAmount(userStore.userInfo?.totalConsumption) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateProfile, changePassword as apiChangePassword, updateLanguage, updateTheme } from '@/api/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const activeTab = ref('profile')

const profileForm = reactive({
  username: '',
  email: '',
  displayName: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const preferences = reactive({
  language: 'zh-CN',
  theme: 'light'
})

function formatAmount(amount) {
  return ((amount || 0) / 100).toFixed(2)
}

async function saveProfile() {
  try {
    await updateProfile({
      displayName: profileForm.displayName,
      phone: profileForm.phone
    })
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
  } catch (error) {
    console.error('Save profile failed:', error)
  }
}

async function changePassword() {
  try {
    await apiChangePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('Change password failed:', error)
  }
}

async function handleLanguageChange(lang) {
  await updateLanguage(lang)
  userStore.setLocale(lang)
  location.reload()
}

async function handleThemeChange(theme) {
  await updateTheme(theme)
  userStore.setTheme(theme)
}

onMounted(() => {
  if (userStore.userInfo) {
    profileForm.username = userStore.userInfo.username
    profileForm.email = userStore.userInfo.email
    profileForm.displayName = userStore.userInfo.displayName
    profileForm.phone = userStore.userInfo.phone || ''
    preferences.language = userStore.userInfo.preferredLanguage || 'zh-CN'
    preferences.theme = userStore.userInfo.preferredTheme || 'light'
  }
})
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;
}

.account-info {
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      color: #909399;
    }
    
    .value {
      color: #303133;
      font-weight: 500;
    }
  }
}
</style>
