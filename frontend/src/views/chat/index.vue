<template>
  <div class="chat-container">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <span>AI Chat</span>
          <div class="header-actions">
            <el-select v-model="selectedModel" placeholder="选择模型" size="small">
              <el-option label="GPT-3.5" value="gpt-3.5-turbo" />
              <el-option label="GPT-4" value="gpt-4" />
              <el-option label="Claude" value="claude-3-5-sonnet-20241022" />
            </el-select>
          </div>
        </div>
      </template>
      
      <div ref="messagesContainer" class="messages-container">
        <div v-for="(msg, index) in messages" :key="index" 
             class="message" :class="msg.role">
          <div class="message-avatar">
            {{ msg.role === 'user' ? 'U' : 'AI' }}
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ msg.time }}</div>
          </div>
        </div>
        
        <div v-if="loading" class="message assistant">
          <div class="message-avatar">AI</div>
          <div class="message-content">
            <div class="message-text typing">
              <span class="dot"></span>
              <span class="dot"></span>
              <span class="dot"></span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入消息... (Shift+Enter换行，Enter发送)"
          @keydown.enter.exact.prevent="handleSend"
        />
        <div class="input-actions">
          <el-button @click="clearChat">清空对话</el-button>
          <el-button type="primary" :loading="loading" @click="handleSend">
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const messages = ref([
  {
    role: 'assistant',
    content: '你好！我是AI助手，有什么可以帮助你的吗？',
    time: new Date().toLocaleTimeString()
  }
])

const inputMessage = ref('')
const loading = ref(false)
const selectedModel = ref('gpt-3.5-turbo')
const messagesContainer = ref(null)

function handleSend() {
  if (!inputMessage.value.trim()) {
    return
  }
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString()
  })
  
  const userMessage = inputMessage.value
  inputMessage.value = ''
  
  // 滚动到底部
  scrollToBottom()
  
  // 模拟AI响应
  loading.value = true
  setTimeout(() => {
    loading.value = false
    messages.value.push({
      role: 'assistant',
      content: `这是对你消息"${userMessage}"的模拟回复。在实际使用中，这里会显示真实的AI响应。`,
      time: new Date().toLocaleTimeString()
    })
    scrollToBottom()
  }, 1500)
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

function clearChat() {
  messages.value = [{
    role: 'assistant',
    content: '对话已清空，有什么可以帮助你的吗？',
    time: new Date().toLocaleTimeString()
  }]
}
</script>

<style lang="scss" scoped>
.chat-container {
  padding: 20px;
  height: calc(100vh - 100px);
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .el-card__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    padding: 0;
  }
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  
  &.user {
    flex-direction: row-reverse;
    
    .message-content {
      align-items: flex-end;
      
      .message-text {
        background: #409eff;
        color: white;
      }
    }
  }
  
  &.assistant {
    .message-text {
      background: #f5f7fa;
    }
  }
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
  margin: 0 10px;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-break: break-word;
  
  &.typing {
    display: flex;
    gap: 4px;
    padding: 16px 20px;
    
    .dot {
      width: 8px;
      height: 8px;
      background: #909399;
      border-radius: 50%;
      animation: bounce 1.4s infinite ease-in-out;
      
      &:nth-child(1) { animation-delay: -0.32s; }
      &:nth-child(2) { animation-delay: -0.16s; }
    }
  }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.input-area {
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
  
  .input-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 10px;
  }
}
</style>
