<template>
  <div class="admin-oauth-settings">
    <h2 class="page-title">{{ $t('admin.oauthSettings') }}</h2>

    <el-card>
      <!-- LinuxDO 设置 -->
      <el-collapse>
        <el-collapse-item title="LinuxDO OAuth" name="linuxdo">
          <template slot="title">
            <div class="oauth-header">
              <span class="oauth-title">LinuxDO OAuth</span>
              <el-tag :type="providers.linuxdo?.enabled ? 'success' : 'info'" size="small">
                {{ providers.linuxdo?.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
          </template>
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="providers.linuxdo.enabled" />
            </el-form-item>
            <el-form-item label="Client ID">
              <el-input v-model="providers.linuxdo.clientId" placeholder="OAuth应用客户端ID" />
            </el-form-item>
            <el-form-item label="Client Secret">
              <el-input
                v-model="providers.linuxdo.clientSecret"
                type="password"
                show-password
                placeholder="OAuth应用客户端密钥"
              />
            </el-form-item>
            <el-form-item label="授权地址">
              <el-input v-model="providers.linuxdo.authUrl" placeholder="https://connect.devian.org/oauth2/token" />
            </el-form-item>
            <el-form-item label="用户信息URL">
              <el-input v-model="providers.linuxdo.userInfoUrl" placeholder="https://connect.devian.org/api/v3/me" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="saveProvider('linuxdo')">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>

        <!-- Telegram 设置 -->
        <el-collapse-item title="Telegram OAuth" name="telegram">
          <template slot="title">
            <div class="oauth-header">
              <span class="oauth-title">Telegram OAuth</span>
              <el-tag :type="providers.telegram?.enabled ? 'success' : 'info'" size="small">
                {{ providers.telegram?.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
          </template>
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="providers.telegram.enabled" />
            </el-form-item>
            <el-form-item label="Bot Token">
              <el-input
                v-model="providers.telegram.botToken"
                type="password"
                show-password
                placeholder="从 @BotFather 获取的Token"
              />
            </el-form-item>
            <el-form-item label="使用Mini App">
              <el-switch v-model="providers.telegram.useMiniApp" />
              <span style="margin-left: 10px; color: #909399">
                启用后使用Telegram Mini App登录方式
              </span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="saveProvider('telegram')">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>

        <!-- OIDC 设置 -->
        <el-collapse-item title="OIDC/OAuth2.0 通用" name="oidc">
          <template slot="title">
            <div class="oauth-header">
              <span class="oauth-title">OIDC/OAuth2.0 通用</span>
              <el-tag :type="providers.oidc?.enabled ? 'success' : 'info'" size="small">
                {{ providers.oidc?.enabled ? '已启用' : '已禁用' }}
              </el-tag>
            </div>
          </template>
          <el-form label-width="120px">
            <el-form-item label="启用">
              <el-switch v-model="providers.oidc.enabled" />
            </el-form-item>
            <el-form-item label="提供商名称">
              <el-input v-model="providers.oidc.name" placeholder="例如: GitHub, Google" />
            </el-form-item>
            <el-form-item label="Client ID">
              <el-input v-model="providers.oidc.clientId" placeholder="OAuth应用客户端ID" />
            </el-form-item>
            <el-form-item label="Client Secret">
              <el-input
                v-model="providers.oidc.clientSecret"
                type="password"
                show-password
                placeholder="OAuth应用客户端密钥"
              />
            </el-form-item>
            <el-form-item label="授权URL">
              <el-input v-model="providers.oidc.authUrl" placeholder="https://example.com/oauth/authorize" />
            </el-form-item>
            <el-form-item label="Token URL">
              <el-input v-model="providers.oidc.tokenUrl" placeholder="https://example.com/oauth/token" />
            </el-form-item>
            <el-form-item label="用户信息URL">
              <el-input v-model="providers.oidc.userInfoUrl" placeholder="https://example.com/oauth/userinfo" />
            </el-form-item>
            <el-form-item label="Scope">
              <el-input v-model="providers.oidc.scope" placeholder="openid profile email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small" @click="saveProvider('oidc')">
                {{ $t('admin.save') }}
              </el-button>
            </el-form-item>
          </el-form>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- OAuth绑定统计 -->
    <el-card style="margin-top: 20px">
      <div slot="header">
        <span>{{ $t('admin.oauthBindings') }}</span>
      </div>
      <el-table :data="bindings" style="width: 100%">
        <el-table-column prop="provider" :label="$t('admin.provider')" width="150">
          <template slot-scope="{ row }">
            {{ getProviderName(row.provider) }}
          </template>
        </el-table-column>
        <el-table-column prop="providerUsername" :label="$t('admin.boundUsername')" />
        <el-table-column prop="userCount" :label="$t('admin.bindingCount')" width="150" align="center" />
        <el-table-column prop="boundAt" :label="$t('admin.latestBinding')" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { getOAuthProviders, getOAuthBindings } from '@/api/oauth'

export default {
  name: 'AdminOAuthSettings',
  data() {
    return {
      providers: {
        linuxdo: {
          enabled: false,
          clientId: '',
          clientSecret: '',
          authUrl: 'https://connect.devian.org/oauth2/token',
          userInfoUrl: 'https://connect.devian.org/api/v3/me'
        },
        telegram: {
          enabled: false,
          botToken: '',
          useMiniApp: true
        },
        oidc: {
          enabled: false,
          name: '',
          clientId: '',
          clientSecret: '',
          authUrl: '',
          tokenUrl: '',
          userInfoUrl: '',
          scope: 'openid profile email'
        }
      },
      bindings: []
    }
  },
  created() {
    this.loadProviders()
    this.loadBindings()
  },
  methods: {
    async loadProviders() {
      try {
        const res = await getOAuthProviders()
        if (res.data) {
          // 更新提供商启用状态
          res.data.forEach(p => {
            if (this.providers[p.type]) {
              this.providers[p.type].enabled = true
            }
          })
        }
      } catch (error) {
        console.error('Failed to load providers:', error)
      }
    },
    async loadBindings() {
      try {
        const res = await getOAuthBindings()
        this.bindings = res.data || []
      } catch (error) {
        console.error('Failed to load bindings:', error)
      }
    },
    async saveProvider(provider) {
      try {
        await this.$http.put(`/admin/oauth/providers/${provider}`, this.providers[provider])
        this.$message.success(this.$t('admin.saveSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.operationFailed'))
      }
    },
    getProviderName(provider) {
      const names = {
        linuxdo: 'LinuxDO',
        telegram: 'Telegram',
        oidc: 'OIDC'
      }
      return names[provider] || provider
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-oauth-settings {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 600;
  }

  .oauth-header {
    display: flex;
    align-items: center;
    gap: 10px;

    .oauth-title {
      font-weight: 600;
    }
  }
}
</style>
