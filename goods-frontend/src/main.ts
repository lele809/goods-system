import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import Login from './components/Login.vue'
import Dashboard from './components/Dashboard.vue'
import DashboardHome from './components/DashboardHome.vue'
import ProductManagement from './components/ProductManagement.vue'

// 创建路由
const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: Login },
  { 
    path: '/dashboard', 
    name: 'Dashboard', 
    component: Dashboard,
    redirect: '/dashboard/home',
    children: [
      { path: 'home', name: 'DashboardHome', component: DashboardHome },
      { path: 'products', name: 'ProductManagement', component: ProductManagement },
      { path: 'inbound', name: 'InboundManagement', component: () => import('./components/InboundManagement.vue') },
      { path: 'outbound', name: 'OutboundRecord', component: () => import('./components/OutboundRecord.vue') },
      { path: 'stock', name: 'StockManagement', component: () => import('./components/StockManagement.vue') },
      { path: 'reports', name: 'Reports', component: () => import('./components/Reports.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 创建应用
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, {
  locale: zhCn,
})

app.mount('#app')
