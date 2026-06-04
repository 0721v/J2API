<template>
  <div class="docs-container">
    <el-row :gutter="20">
      <el-col :span="4">
        <el-card class="sidebar">
          <el-menu :default-active="activeDoc" @select="handleSelect">
            <el-menu-item index="overview">概述</el-menu-item>
            <el-menu-item index="authentication">认证</el-menu-item>
            <el-menu-item index="chat">聊天补全</el-menu-item>
            <el-menu-item index="embeddings">文本嵌入</el-menu-item>
            <el-menu-item index="models">模型列表</el-menu-item>
            <el-menu-item index="errors">错误码</el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <el-col :span="20">
        <el-card>
          <template #header>
            <span>{{ docTitle }}</span>
          </template>
          
          <div v-if="activeDoc === 'overview'" class="doc-content">
            <h2>API概述</h2>
            <p>欢迎使用API Platform API。我们提供OpenAI兼容的API接口，方便您快速接入。</p>
            
            <h3>基础URL</h3>
            <pre><code>https://api.example.com/v1</code></pre>
            
            <h3>请求格式</h3>
            <p>所有请求应包含以下Header：</p>
            <pre><code>Content-Type: application/json
Authorization: Bearer YOUR_API_KEY</code></pre>
          </div>
          
          <div v-if="activeDoc === 'authentication'" class="doc-content">
            <h2>认证</h2>
            <p>API使用Bearer Token进行认证。您可以在个人中心的API密钥页面获取API密钥。</p>
            
            <h3>获取API密钥</h3>
            <ol>
              <li>登录API Platform</li>
              <li>进入API密钥页面</li>
              <li>点击"创建密钥"按钮</li>
              <li>复制生成的API密钥</li>
            </ol>
            
            <h3>使用密钥</h3>
            <p>在每个请求的Header中包含您的API密钥：</p>
            <pre><code>Authorization: Bearer sk-xxxxxxxxxxxx</code></pre>
            
            <el-alert type="warning" :closable="false">
              请妥善保管您的API密钥，不要泄露给他人！
            </el-alert>
          </div>
          
          <div v-if="activeDoc === 'chat'" class="doc-content">
            <h2>聊天补全</h2>
            <p>创建聊天补全请求，类似OpenAI的Chat API。</p>
            
            <h3>端点</h3>
            <pre><code>POST /v1/chat/completions</code></pre>
            
            <h3>请求示例</h3>
            <pre><code>{
  "model": "gpt-3.5-turbo",
  "messages": [
    {"role": "user", "content": "你好"}
  ],
  "stream": false
}</code></pre>
            
            <h3>响应示例</h3>
            <pre><code>{
  "id": "chatcmpl-123",
  "object": "chat.completion",
  "created": 1677652288,
  "model": "gpt-3.5-turbo",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "你好！有什么可以帮助你的吗？"
    },
    "finish_reason": "stop"
  }],
  "usage": {
    "prompt_tokens": 9,
    "completion_tokens": 12,
    "total_tokens": 21
  }
}</code></pre>
          </div>
          
          <div v-if="activeDoc === 'embeddings'" class="doc-content">
            <h2>文本嵌入</h2>
            <p>获取文本的向量表示。</p>
            
            <h3>端点</h3>
            <pre><code>POST /v1/embeddings</code></pre>
            
            <h3>请求示例</h3>
            <pre><code>{
  "model": "text-embedding-3-small",
  "input": "The quick brown fox jumps over the lazy dog"
}</code></pre>
          </div>
          
          <div v-if="activeDoc === 'models'" class="doc-content">
            <h2>模型列表</h2>
            <p>获取所有可用模型。</p>
            
            <h3>端点</h3>
            <pre><code>GET /v1/models</code></pre>
            
            <h3>可用模型</h3>
            <el-table :data="models" stripe>
              <el-table-column prop="id" label="模型ID" />
              <el-table-column prop="name" label="名称" />
            </el-table>
          </div>
          
          <div v-if="activeDoc === 'errors'" class="doc-content">
            <h2>错误码</h2>
            <p>API可能返回以下错误码：</p>
            
            <el-table :data="errorCodes" stripe>
              <el-table-column prop="code" label="错误码" width="100" />
              <el-table-column prop="message" label="说明" />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getModels } from '@/api/model'

const activeDoc = ref('overview')
const models = ref([])

const docTitles = {
  overview: 'API概述',
  authentication: '认证',
  chat: '聊天补全',
  embeddings: '文本嵌入',
  models: '模型列表',
  errors: '错误码'
}

const errorCodes = [
  { code: 401, message: '未授权，API密钥无效或缺失' },
  { code: 403, message: '禁止访问，权限不足' },
  { code: 404, message: '资源不存在' },
  { code: 429, message: '请求过于频繁，已被限流' },
  { code: 500, message: '服务器内部错误' },
  { code: 503, message: '服务暂时不可用' }
]

const docTitle = ref('API概述')

function handleSelect(index) {
  activeDoc.value = index
  docTitle.value = docTitles[index]
  if (index === 'models') {
    fetchModels()
  }
}

async function fetchModels() {
  try {
    const res = await getModels()
    models.value = res.data.map(m => ({
      id: m.modelId,
      name: m.name
    }))
  } catch (error) {
    console.error('Failed to fetch models:', error)
  }
}
</script>

<style lang="scss" scoped>
.docs-container {
  padding: 20px;
}

.sidebar {
  position: sticky;
  top: 20px;
}

.doc-content {
  h2 {
    margin-top: 0;
    padding-bottom: 10px;
    border-bottom: 2px solid #409eff;
  }
  
  h3 {
    margin-top: 25px;
    color: #409eff;
  }
  
  p {
    line-height: 1.8;
    color: #606266;
  }
  
  pre {
    background: #f5f7fa;
    padding: 15px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 15px 0;
    
    code {
      font-family: 'Monaco', 'Menlo', monospace;
      font-size: 13px;
    }
  }
  
  ol, ul {
    padding-left: 25px;
    color: #606266;
    
    li {
      margin: 8px 0;
      line-height: 1.8;
    }
  }
}
</style>
