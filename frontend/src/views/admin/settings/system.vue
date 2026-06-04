<template>
  <div class="admin-settings">
    <h2 class="page-title">{{ $t('admin.systemSettings') }}</h2>

    <el-card>
      <el-tabs v-model="activeTab">
        <!-- 基础设置 -->
        <el-tab-pane :label="$t('admin.basicSettings')" name="basic">
          <el-form ref="basicFormRef" :model="basicForm" :rules="basicRules" label-width="150px">
            <el-form-item :label="$t('admin.siteName')" prop="siteName">
              <el-input v-model="basicForm.siteName" />
            </el-form-item>
            <el-form-item :label="$t('admin.siteUrl')" prop="siteUrl">
              <el-input v-model="basicForm.siteUrl" />
            </el-form-item>
            <el-form-item :label="$t('admin.defaultLanguage')">
              <el-select v-model="basicForm.defaultLanguage" style="width: 100%">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
              </el-select>
            </el-form-item>
            <el-form-item :label="$t('admin.defaultTheme')">
              <el-select v-model="basicForm.defaultTheme" style="width: 100%">
                <el-option label="Light" value="light" />
                <el-option label="Dark" value="dark" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicSettings">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 充值设置 -->
        <el-tab-pane :label="$t('admin.rechargeSettings')" name="recharge">
          <el-form label-width="150px">
            <el-form-item :label="$t('admin.minRechargeAmount')">
              <el-input-number v-model="rechargeSettings.minAmount" :min="0" />
            </el-form-item>
            <el-form-item :label="$t('admin.rechargeBonus')">
              <el-switch v-model="rechargeSettings.enableBonus" />
            </el-form-item>
            <el-form-item v-if="rechargeSettings.enableBonus" :label="$t('admin.bonusRules')">
              <el-input
                v-model="rechargeSettings.bonusRules"
                type="textarea"
                :rows="4"
                placeholder="JSON格式，例如: [{&quot;minAmount&quot;:100,&quot;bonusPercent&quot;:0},...]"
              />
            </el-form-item>
            <el-form-item :label="$t('admin.paymentMethods')">
              <el-checkbox-group v-model="rechargeSettings.enabledMethods">
                <el-checkbox label="alipay">支付宝</el-checkbox>
                <el-checkbox label="wechat">微信支付</el-checkbox>
                <el-checkbox label="stripe">Stripe</el-checkbox>
                <el-checkbox label="trc20">USDT(TRC20)</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item :label="$t('admin.orderExpireMinutes')">
              <el-input-number v-model="rechargeSettings.orderExpireMinutes" :min="5" :max="1440" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveRechargeSettings">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- API设置 -->
        <el-tab-pane :label="$t('admin.apiSettings')" name="api">
          <el-form label-width="150px">
            <el-form-item :label="$t('admin.defaultRateLimit')">
              <el-input-number v-model="apiSettings.defaultRateLimit" :min="0" />
              <span style="margin-left: 10px">次/分钟</span>
            </el-form-item>
            <el-form-item :label="$t('admin.dailyRequestLimit')">
              <el-input-number v-model="apiSettings.dailyRequestLimit" :min="0" />
            </el-form-item>
            <el-form-item :label="$t('admin.cacheBilling')">
              <el-switch v-model="apiSettings.enableCacheBilling" />
              <span style="margin-left: 10px; color: #909399">
                开启后，命中缓存的请求将享受折扣价格
              </span>
            </el-form-item>
            <el-form-item v-if="apiSettings.enableCacheBilling" :label="$t('admin.cacheDiscount')">
              <el-input-number
                v-model="apiSettings.cacheDiscount"
                :min="0"
                :max="1"
                :step="0.1"
                :precision="1"
              />
              <span style="margin-left: 10px">倍（如0.5表示5折）</span>
            </el-form-item>
            <el-form-item :label="$t('admin.tokenPrefix')">
              <el-input v-model="apiSettings.tokenPrefix" />
            </el-form-item>
            <el-form-item :label="$t('admin.allowedOrigins')">
              <el-input
                v-model="apiSettings.allowedOrigins"
                type="textarea"
                :rows="3"
                placeholder="用逗号分隔，例如: https://example.com,https://app.example.com"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveApiSettings">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全设置 -->
        <el-tab-pane :label="$t('admin.securitySettings')" name="security">
          <el-form label-width="150px">
            <el-form-item :label="$t('admin.jwtSecret')">
              <el-input v-model="securitySettings.jwtSecret" type="password" show-password />
              <el-button type="text" @click="regenerateJwtSecret" style="margin-left: 10px">
                {{ $t('admin.regenerate') }}
              </el-button>
            </el-form-item>
            <el-form-item :label="$t('admin.jwtExpireMinutes')">
              <el-input-number v-model="securitySettings.jwtExpireMinutes" :min="1" />
            </el-form-item>
            <el-form-item :label="$t('admin.refreshTokenExpire')">
              <el-input-number v-model="securitySettings.refreshTokenExpire" :min="1" />
              <span style="margin-left: 10px">天</span>
            </el-form-item>
            <el-form-item :label="$t('admin.allowRegister')">
              <el-switch v-model="securitySettings.allowRegister" />
            </el-form-item>
            <el-form-item :label="$t('admin.requireEmailVerify')">
              <el-switch v-model="securitySettings.requireEmailVerify" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSecuritySettings">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getSystemSettings, updateSystemSettings } from '@/api/admin'

export default {
  name: 'AdminSystemSettings',
  data() {
    return {
      activeTab: 'basic',
      basicForm: {
        siteName: 'API Platform',
        siteUrl: 'http://localhost:8080',
        defaultLanguage: 'zh-CN',
        defaultTheme: 'light'
      },
      basicRules: {
        siteName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
        siteUrl: [{ required: true, message: '请输入网站地址', trigger: 'blur' }]
      },
      rechargeSettings: {
        minAmount: 10,
        enableBonus: true,
        bonusRules: '[{"minAmount":100,"bonusPercent":0},{"minAmount":500,"bonusPercent":4},{"minAmount":1000,"bonusPercent":10}]',
        enabledMethods: ['alipay', 'wechat', 'stripe', 'trc20'],
        orderExpireMinutes: 30
      },
      apiSettings: {
        defaultRateLimit: 60,
        dailyRequestLimit: 10000,
        enableCacheBilling: true,
        cacheDiscount: 0.5,
        tokenPrefix: 'sk',
        allowedOrigins: '*'
      },
      securitySettings: {
        jwtSecret: '',
        jwtExpireMinutes: 60,
        refreshTokenExpire: 7,
        allowRegister: true,
        requireEmailVerify: false
      }
    }
  },
  created() {
    this.loadSettings()
  },
  methods: {
    async loadSettings() {
      try {
        const res = await getSystemSettings()
        if (res.data) {
          this.basicForm = { ...this.basicForm, ...res.data.basic }
          this.rechargeSettings = { ...this.rechargeSettings, ...res.data.recharge }
          this.apiSettings = { ...this.apiSettings, ...res.data.api }
          this.securitySettings = { ...this.securitySettings, ...res.data.security }
        }
      } catch (error) {
        this.$message.error(this.$t('admin.loadFailed'))
      }
    },
    async saveBasicSettings() {
      try {
        await this.$refs.basicFormRef.validate()
        await updateSystemSettings({ type: 'basic', data: this.basicForm })
        this.$message.success(this.$t('admin.saveSuccess'))
      } catch (error) {
        if (error !== false) {
          this.$message.error(this.$t('admin.operationFailed'))
        }
      }
    },
    async saveRechargeSettings() {
      try {
        await updateSystemSettings({ type: 'recharge', data: this.rechargeSettings })
        this.$message.success(this.$t('admin.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    async saveApiSettings() {
      try {
        await updateSystemSettings({ type: 'api', data: this.apiSettings })
        this.$message.success(this.$t('admin.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    async saveSecuritySettings() {
      try {
        await updateSystemSettings({ type: 'security', data: this.securitySettings })
        this.$message.success(this.$t('admin.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    regenerateJwtSecret() {
      this.securitySettings.jwtSecret = 'sk_' + Math.random().toString(36).substring(2, 34)
      this.$message.info('JWT密钥已重新生成')
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-settings {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }
}
</style>
