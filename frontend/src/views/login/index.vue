<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>API Platform</h1>
        <p>智能API聚合与计费管理平台</p>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="loginKey">
          <el-input 
            v-model="form.loginKey" 
            placeholder="邮箱或用户名"
            size="large"
            prefix-icon="User"
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
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-button"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <router-link to="/register">还没有账号？立即注册</router-link>
      </div>
      
      <div class="oauth-section">
        <div class="divider">
          <span>其他登录方式</span>
        </div>
        <div class="oauth-buttons">
          <el-button @click="handleOAuthLogin('linuxdo')">
            <Icon name="brand-linuxdo" /> LinuxDo
          </el-button>
          <el-button @click="handleOAuthLogin('telegram')">
            <Icon name="brand-telegram" /> Telegram
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  loginKey: '',
  password: ''
})

const rules = {
  loginKey: [{ required: true, message: '请输入邮箱或用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const success = await userStore.login(form.loginKey, form.password)
    if (success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    }
  } finally {
    loading.value = false
  }
}

function handleOAuthLogin(provider) {
  window.location.href = `/api/oauth/authorize/${provider}`
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
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

.login-form {
  .login-button {
    width: 100%;
  }
}

.login-footer {
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

.oauth-section {
  margin-top: 30px;
}

.divider {
  text-align: center;
  margin-bottom: 20px;
  position: relative;
  
  &::before,
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    width: 40%;
    height: 1px;
    background: #ddd;
  }
  
  &::before { left: 0; }
  &::after { right: 0; }
  
  span {
    color: #999;
    font-size: 12px;
    background: white;
    padding: 0 10px;
    position: relative;
  }
}

.oauth-buttons {
  display: flex;
  gap: 10px;
  
  .el-button {
    flex: 1;
  }
}
</style>
