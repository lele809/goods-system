import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { createPinia } from 'pinia'
// 优化：按需引入ElementPlus核心组件，减少打包体积
import {
  ElButton, ElInput, ElForm, ElFormItem, ElCard, ElRow, ElCol, ElTable, ElTableColumn,
  ElPagination, ElDialog, ElMessage, ElMessageBox, ElSelect, ElOption, ElDatePicker,
  ElRadioGroup, ElRadio, ElInputNumber, ElTag, ElDropdown, ElDropdownMenu,
  ElDropdownItem, ElContainer, ElHeader, ElAside, ElMain, ElMenu, ElMenuItem,
  ElIcon, ElResult, ElLoading
} from 'element-plus'
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

// 优化：按需注册ElementPlus组件
const components = [
  ElButton, ElInput, ElForm, ElFormItem, ElCard, ElRow, ElCol, ElTable, ElTableColumn,
  ElPagination, ElDialog, ElSelect, ElOption, ElDatePicker,
  ElRadioGroup, ElRadio, ElInputNumber, ElTag, ElDropdown, ElDropdownMenu,
  ElDropdownItem, ElContainer, ElHeader, ElAside, ElMain, ElMenu, ElMenuItem,
  ElIcon, ElResult, ElLoading
]

components.forEach(component => {
  app.component(component.name, component)
})

// 注册全局属性
app.config.globalProperties.$message = ElMessage
app.config.globalProperties.$messageBox = ElMessageBox
app.config.globalProperties.$loading = ElLoading.service

app.use(createPinia())
app.use(router)

app.mount('#app')
