<template>
  <div class="dashboard-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <!-- 移动端菜单按钮 -->
        <el-button 
          class="mobile-menu-btn" 
          @click="toggleSidebar" 
          :icon="Menu" 
          circle 
          size="small"
        />
        <h2 class="system-title">货架商品管理系统</h2>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            <el-icon><User /></el-icon>
            <span class="username">{{ userStore.currentUser?.adminName }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人信息
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><Switch /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主内容区域 -->
    <el-container class="main-container">
      <!-- 侧边导航 -->
      <el-aside 
        class="sidebar" 
        :class="{ 'sidebar-collapsed': isSidebarCollapsed }"
        :width="isSidebarCollapsed ? '0px' : '250px'"
      >
        <div class="sidebar-content" :class="{ 'hidden': isSidebarCollapsed }">
          <el-menu
            :default-active="currentMenuIndex"
            class="sidebar-menu"
            :collapse="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/dashboard/home">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/products">
              <el-icon><Goods /></el-icon>
              <span>商品管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/inbound">
              <el-icon><Box /></el-icon>
              <span>入库管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/outbound">
              <el-icon><Van /></el-icon>
              <span>出库管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/stock">
              <el-icon><Box /></el-icon>
              <span>库存管理</span>
            </el-menu-item>
            <el-menu-item index="/dashboard/reports">
              <el-icon><DataLine /></el-icon>
              <span>统计报表</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-aside>

      <!-- 移动端遮罩层 -->
      <div 
        v-if="!isSidebarCollapsed && isMobile" 
        class="sidebar-overlay" 
        @click="closeSidebar"
      ></div>

      <!-- 主内容 -->
      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed, ref, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Menu } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 响应式状态
const isSidebarCollapsed = ref(false)
const isMobile = ref(false)

// 检测屏幕尺寸
const checkScreenSize = () => {
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value) {
    isSidebarCollapsed.value = true
  } else {
    isSidebarCollapsed.value = false
  }
}

// 切换侧边栏
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

// 关闭侧边栏（移动端点击遮罩层时）
const closeSidebar = () => {
  if (isMobile.value) {
    isSidebarCollapsed.value = true
  }
}

// 当前菜单索引
const currentMenuIndex = computed(() => {
  return route.path
})

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  router.push(index)
}

// 处理下拉菜单命令
const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中...')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理登出
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}

// 初始化
onMounted(() => {
  userStore.initUser()
  // 如果未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
  
  // 初始化屏幕尺寸检测
  checkScreenSize()
  window.addEventListener('resize', checkScreenSize)
})

// 清理监听器
onUnmounted(() => {
  window.removeEventListener('resize', checkScreenSize)
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  position: relative;
  z-index: 1001;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mobile-menu-btn {
  display: none;
}

.system-title {
  color: #333;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f5f5;
}

.username {
  white-space: nowrap;
}

.main-container {
  flex: 1;
  height: calc(100vh - 60px);
  position: relative;
}

.sidebar {
  background: #fff;
  border-right: 1px solid #e6e6e6;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  z-index: 1000;
}

.sidebar-content {
  width: 250px;
  height: 100%;
  transition: opacity 0.3s ease;
}

.sidebar-content.hidden {
  opacity: 0;
}

.sidebar-collapsed {
  width: 0 !important;
  border-right: none;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-overlay {
  position: fixed;
  top: 60px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: none;
}

.content {
  background: #f5f7fa;
  padding: 20px;
  transition: margin-left 0.3s ease;
}

/* 移动端样式 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }
  
  .mobile-menu-btn {
    display: inline-flex;
  }
  
  .system-title {
    font-size: 16px;
  }
  
  .username {
    display: none;
  }
  
  .sidebar {
    position: fixed;
    top: 60px;
    left: 0;
    height: calc(100vh - 60px);
    z-index: 1000;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  }
  
  .sidebar-overlay {
    display: block;
  }
  
  .content {
    padding: 15px;
    margin-left: 0 !important;
  }
}

/* 平板样式 */
@media (max-width: 1024px) and (min-width: 769px) {
  .content {
    padding: 18px;
  }
}

/* 大屏幕优化 */
@media (min-width: 1200px) {
  .content {
    padding: 24px;
  }
}
</style>