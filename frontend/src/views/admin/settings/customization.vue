<template>
  <div class="customization-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 网站自定义 -->
      <el-tab-pane label="网站自定义" name="customization">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>网站自定义设置</span>
              <el-button type="primary" @click="saveCustomization" :loading="saving">
                保存设置
              </el-button>
            </div>
          </template>

          <el-form :model="customizationForm" label-width="140px">
            <el-divider content-position="left">基本信息</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="网站名称">
                  <el-input v-model="customizationForm.siteName" placeholder="API Platform" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="网站标题">
                  <el-input v-model="customizationForm.siteTitle" placeholder="API Platform - 您的API聚合平台" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="网站描述">
              <el-input
                v-model="customizationForm.siteDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入网站描述"
              />
            </el-form-item>

            <el-form-item label="网站Logo">
              <div class="logo-upload">
                <el-input v-model="customizationForm.siteLogo" placeholder="请输入Logo URL" />
                <el-button type="primary" @click="uploadLogo">上传图片</el-button>
              </div>
              <div v-if="customizationForm.siteLogo" class="logo-preview">
                <img :src="customizationForm.siteLogo" alt="Logo" />
              </div>
            </el-form-item>

            <el-form-item label="网站图标(Favicon)">
              <el-input v-model="customizationForm.favicon" placeholder="请输入Favicon URL" />
            </el-form-item>

            <el-divider content-position="left">主题设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="默认主题">
                  <el-select v-model="customizationForm.defaultTheme">
                    <el-option label="浅色主题" value="light" />
                    <el-option label="深色主题" value="dark" />
                    <el-option label="跟随系统" value="auto" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="主题色">
                  <el-color-picker v-model="customizationForm.primaryColor" />
                  <span class="color-value">{{ customizationForm.primaryColor }}</span>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="可用主题">
              <el-checkbox-group v-model="customizationForm.availableThemes">
                <el-checkbox label="light">浅色主题</el-checkbox>
                <el-checkbox label="dark">深色主题</el-checkbox>
                <el-checkbox label="blue">蓝色主题</el-checkbox>
                <el-checkbox label="green">绿色主题</el-checkbox>
                <el-checkbox label="purple">紫色主题</el-checkbox>
              </el-checkbox-group>
            </el-form-item>

            <el-divider content-position="left">页脚设置</el-divider>

            <el-form-item label="版权信息">
              <el-input v-model="customizationForm.copyright" placeholder="© 2024 API Platform. All rights reserved." />
            </el-form-item>

            <el-form-item label="备案号">
              <el-input v-model="customizationForm.icp" placeholder="京ICP备XXXXXXXX号" />
            </el-form-item>

            <el-form-item label="联系方式">
              <el-input v-model="customizationForm.contactEmail" placeholder="support@example.com" />
            </el-form-item>

            <el-form-item label="社交链接">
              <el-input
                v-model="customizationForm.socialLinks"
                type="textarea"
                :rows="3"
                placeholder='{"github": "https://github.com/xxx", "twitter": "https://twitter.com/xxx"}'
              />
            </el-form-item>

            <el-divider content-position="left">功能开关</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="显示模型列表">
                  <el-switch v-model="customizationForm.showModels" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="显示价格表">
                  <el-switch v-model="customizationForm.showPricing" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="显示用量统计">
                  <el-switch v-model="customizationForm.showUsage" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="显示Chat入口">
                  <el-switch v-model="customizationForm.showChat" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="自定义CSS">
              <el-input
                v-model="customizationForm.customCss"
                type="textarea"
                :rows="6"
                placeholder=".custom-class { color: red; }"
              />
            </el-form-item>

            <el-form-item label="自定义JS">
              <el-input
                v-model="customizationForm.customJs"
                type="textarea"
                :rows="6"
                placeholder="console.log('Hello');"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- SEO 设置 -->
      <el-tab-pane label="SEO 设置" name="seo">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>SEO 设置</span>
              <el-button type="primary" @click="saveSeo" :loading="saving">
                保存设置
              </el-button>
            </div>
          </template>

          <el-form :model="seoForm" label-width="140px">
            <el-divider content-position="left">基础 SEO</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="Meta Title">
                  <el-input v-model="seoForm.metaTitle" placeholder="API Platform - 您的一站式API服务平台" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Meta Keywords">
                  <el-input v-model="seoForm.metaKeywords" placeholder="API, AI, GPT, Claude, OpenAI" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="Meta Description">
              <el-input
                v-model="seoForm.metaDescription"
                type="textarea"
                :rows="4"
                placeholder="API Platform 提供最优质的AI API服务，支持OpenAI、Anthropic、Azure等多个渠道..."
              />
            </el-form-item>

            <el-divider content-position="left">社交分享</el-divider>

            <el-form-item label="OG Title">
              <el-input v-model="seoForm.ogTitle" placeholder="Open Graph 标题" />
            </el-form-item>

            <el-form-item label="OG Description">
              <el-input
                v-model="seoForm.ogDescription"
                type="textarea"
                :rows="3"
                placeholder="Open Graph 描述"
              />
            </el-form-item>

            <el-form-item label="OG Image">
              <el-input v-model="seoForm.ogImage" placeholder="请输入分享图片URL" />
              <div v-if="seoForm.ogImage" class="og-image-preview">
                <img :src="seoForm.ogImage" alt="OG Image" />
              </div>
            </el-form-item>

            <el-divider content-position="left">高级设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="站点地图">
                  <el-switch v-model="seoForm.enableSitemap" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="Robots.txt">
                  <el-switch v-model="seoForm.enableRobots" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="自定义 Robots">
              <el-input
                v-model="seoForm.customRobots"
                type="textarea"
                :rows="4"
                placeholder="User-agent: *&#10;Allow: /&#10;Disallow: /admin"
              />
            </el-form-item>

            <el-form-item label="Canonical URL">
              <el-input v-model="seoForm.canonicalUrl" placeholder="https://api.example.com" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 注册设置 -->
      <el-tab-pane label="注册设置" name="registration">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>注册与配额设置</span>
              <el-button type="primary" @click="saveRegistration" :loading="saving">
                保存设置
              </el-button>
            </div>
          </template>

          <el-form :model="registrationForm" label-width="160px">
            <el-divider content-position="left">注册设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="允许注册">
                  <el-switch v-model="registrationForm.allowRegister" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="允许邮箱验证">
                  <el-switch v-model="registrationForm.requireEmailVerification" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="注册验证码">
                  <el-switch v-model="registrationForm.requireCaptcha" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="默认用户组">
                  <el-select v-model="registrationForm.defaultGroupId" placeholder="请选择默认用户组">
                    <el-option label="免费用户" :value="1" />
                    <el-option label="基础用户" :value="2" />
                    <el-option label="专业用户" :value="3" />
                    <el-option label="企业用户" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="注册协议">
              <el-input
                v-model="registrationForm.termsOfService"
                type="textarea"
                :rows="6"
                placeholder="请输入服务条款内容..."
              />
            </el-form-item>

            <el-form-item label="隐私政策">
              <el-input
                v-model="registrationForm.privacyPolicy"
                type="textarea"
                :rows="6"
                placeholder="请输入隐私政策内容..."
              />
            </el-form-item>

            <el-divider content-position="left">配额设置</el-divider>

            <el-form-item label="新用户赠送积分">
              <el-input-number v-model="registrationForm.newUserBonus" :min="0" />
              <span class="form-tip">新注册用户将获得此数量的积分奖励</span>
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="免费用户分钟限制">
                  <el-input-number v-model="registrationForm.freeMinuteLimit" :min="0" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="免费用户每日限制">
                  <el-input-number v-model="registrationForm.freeDailyLimit" :min="0" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="免费用户每月限制">
              <el-input-number v-model="registrationForm.freeMonthlyLimit" :min="0" />
            </el-form-item>

            <el-divider content-position="left">邀请设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="启用邀请注册">
                  <el-switch v-model="registrationForm.enableInvite" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="邀请奖励">
                  <el-input-number v-model="registrationForm.inviteBonus" :min="0" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 代理设置 -->
      <el-tab-pane label="代理设置" name="proxy">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>代理功能设置</span>
              <el-button type="primary" @click="saveProxy" :loading="saving">
                保存设置
              </el-button>
            </div>
          </template>

          <el-form :model="proxyForm" label-width="160px">
            <el-divider content-position="left">基本设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="启用代理功能">
                  <el-switch v-model="proxyForm.enableProxy" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="启用自定义端点">
                  <el-switch v-model="proxyForm.enableCustomEndpoint" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">代理规则</el-divider>

            <el-form-item label="允许的域名">
              <el-input
                v-model="proxyForm.allowedDomains"
                type="textarea"
                :rows="4"
                placeholder="api.openai.com&#10;api.anthropic.com&#10;*.azure.com"
              />
              <div class="form-tip">每行一个域名，支持通配符 *</div>
            </el-form-item>

            <el-form-item label="禁止的域名">
              <el-input
                v-model="proxyForm.blockedDomains"
                type="textarea"
                :rows="4"
                placeholder="malicious.com&#10;spam.com"
              />
            </el-form-item>

            <el-divider content-position="left">性能设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="默认超时(ms)">
                  <el-input-number v-model="proxyForm.defaultTimeout" :min="1000" :max="300000" :step="1000" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大超时(ms)">
                  <el-input-number v-model="proxyForm.maxTimeout" :min="1000" :max="300000" :step="1000" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="启用请求日志">
                  <el-switch v-model="proxyForm.enableRequestLog" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="日志保留天数">
                  <el-input-number v-model="proxyForm.logRetentionDays" :min="1" :max="365" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">安全设置</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="启用请求签名">
                  <el-switch v-model="proxyForm.enableSignature" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="签名密钥">
                  <el-input v-model="proxyForm.signatureSecret" placeholder="请输入签名密钥" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="请求限流">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-form-item label="每分钟请求数">
                    <el-input-number v-model="proxyForm.rateLimitPerMinute" :min="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="每小时请求数">
                    <el-input-number v-model="proxyForm.rateLimitPerHour" :min="0" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="每天请求数">
                    <el-input-number v-model="proxyForm.rateLimitPerDay" :min="0" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getCustomizationSettings,
  updateCustomizationSettings,
  getSeoSettings,
  updateSeoSettings,
  getRegistrationSettings,
  updateRegistrationSettings,
  getProxySettings,
  updateProxySettings
} from '@/api/settings'

// 状态
const activeTab = ref('customization')
const saving = ref(false)

// 网站自定义表单
const customizationForm = reactive({
  siteName: 'API Platform',
  siteTitle: '',
  siteDescription: '',
  siteLogo: '',
  favicon: '',
  defaultTheme: 'light',
  primaryColor: '#409eff',
  availableThemes: ['light', 'dark'],
  copyright: '',
  icp: '',
  contactEmail: '',
  socialLinks: '',
  showModels: true,
  showPricing: true,
  showUsage: true,
  showChat: true,
  customCss: '',
  customJs: ''
})

// SEO 表单
const seoForm = reactive({
  metaTitle: '',
  metaKeywords: '',
  metaDescription: '',
  ogTitle: '',
  ogDescription: '',
  ogImage: '',
  enableSitemap: true,
  enableRobots: true,
  customRobots: '',
  canonicalUrl: ''
})

// 注册设置表单
const registrationForm = reactive({
  allowRegister: true,
  requireEmailVerification: true,
  requireCaptcha: true,
  defaultGroupId: 1,
  termsOfService: '',
  privacyPolicy: '',
  newUserBonus: 0,
  freeMinuteLimit: 60,
  freeDailyLimit: 1000,
  freeMonthlyLimit: 10000,
  enableInvite: false,
  inviteBonus: 0
})

// 代理设置表单
const proxyForm = reactive({
  enableProxy: true,
  enableCustomEndpoint: true,
  allowedDomains: '',
  blockedDomains: '',
  defaultTimeout: 60000,
  maxTimeout: 120000,
  enableRequestLog: true,
  logRetentionDays: 30,
  enableSignature: false,
  signatureSecret: '',
  rateLimitPerMinute: 60,
  rateLimitPerHour: 1000,
  rateLimitPerDay: 10000
})

// 上传 Logo
const uploadLogo = () => {
  // 触发文件上传
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (file) {
      // TODO: 实现实际上传逻辑
      ElMessage.info('文件上传功能待实现')
    }
  }
  input.click()
}

// 保存网站自定义
const saveCustomization = async () => {
  saving.value = true
  try {
    await updateCustomizationSettings(customizationForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存 SEO 设置
const saveSeo = async () => {
  saving.value = true
  try {
    await updateSeoSettings(seoForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存注册设置
const saveRegistration = async () => {
  saving.value = true
  try {
    await updateRegistrationSettings(registrationForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 保存代理设置
const saveProxy = async () => {
  saving.value = true
  try {
    await updateProxySettings(proxyForm)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 加载数据
const loadData = async () => {
  try {
    // 加载网站自定义设置
    const customRes = await getCustomizationSettings()
    Object.assign(customizationForm, customRes.data || {})

    // 加载 SEO 设置
    const seoRes = await getSeoSettings()
    Object.assign(seoForm, seoRes.data || {})

    // 加载注册设置
    const regRes = await getRegistrationSettings()
    Object.assign(registrationForm, regRes.data || {})

    // 加载代理设置
    const proxyRes = await getProxySettings()
    Object.assign(proxyForm, proxyRes.data || {})
  } catch (error) {
    console.error('加载设置失败', error)
  }
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.customization-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}

.logo-upload {
  display: flex;
  gap: 10px;
}

.logo-upload .el-input {
  flex: 1;
}

.logo-preview {
  margin-top: 10px;
}

.logo-preview img {
  max-width: 200px;
  max-height: 60px;
  object-fit: contain;
}

.color-value {
  margin-left: 10px;
  color: #909399;
}

.og-image-preview {
  margin-top: 10px;
}

.og-image-preview img {
  max-width: 400px;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>
