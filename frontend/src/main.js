import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import en from 'element-plus/dist/locale/en.mjs'
import 'element-plus/dist/index.css'
import router from './router'
import App from './App.vue'
import './assets/styles/index.scss'

const app = createApp(App)

// Pinia状态管理
app.use(createPinia())

// 路由
app.use(router)

// Element Plus
const localeMap = {
  'zh-CN': zhCn,
  'en-US': en,
  'ja-JP': zhCn // TODO: 添加日文locale
}

const savedLocale = localStorage.getItem('locale') || 'zh-CN'
app.use(ElementPlus, { locale: localeMap[savedLocale] || zhCn })

// 全局组件
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
