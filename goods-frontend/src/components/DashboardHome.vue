<template>
  <div class="dashboard-home">
    <!-- 顶部欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h2>欢迎回来，{{ userStore.currentUser?.adminName }}！</h2>
      <p>最后登录时间：{{ formatLastLogin() }}</p>
        </div>
        <div class="welcome-actions">
          <el-button 
            type="primary" 
            :icon="RefreshRight" 
            @click="refreshAllData"
            :loading="isLoading"
            size="large">
            刷新数据
          </el-button>
          <el-button 
            type="" 
            :icon="QuestionFilled" 
            @click="showHelpDialog"
            size="large"
            circle>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 系统状态栏 -->
    <el-card class="status-card" v-if="systemStatus">
      <div class="status-content">
        <div class="status-item">
          <el-icon class="status-icon online"><CircleCheck /></el-icon>
          <span>系统正常</span>
        </div>
        <div class="status-item">
          <el-icon class="status-icon"><Calendar /></el-icon>
          <span>{{ currentDate }}</span>
        </div>
        <div class="status-item">
          <el-icon class="status-icon"><Timer /></el-icon>
          <span>运行时间: {{ formatUptime() }}</span>
        </div>
        <div class="status-item" v-if="lowStockProducts.length > 0">
          <el-icon class="status-icon warning"><WarningFilled /></el-icon>
          <span>{{ lowStockProducts.length }} 件商品库存不足</span>
        </div>
      </div>
    </el-card>

    <!-- 快速搜索 -->
    <el-card class="search-card">
      <template #header>
        <div class="card-header">
          <span>快速搜索</span>
        </div>
      </template>
      <el-input
        v-model="searchQuery"
        placeholder="输入商品名称或规格快速搜索..."
        clearable
        size="large"
        @input="onSearchInput"
        class="search-input">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div v-if="searchResults.length > 0" class="search-results">
        <div v-for="product in searchResults" :key="product.id" class="search-item" @click="selectProduct(product)">
          <div class="product-info">
            <strong>{{ product.name }}</strong>
            <span v-if="product.spec">{{ product.spec }}</span>
          </div>
          <div class="product-stock">
            库存: {{ product.remainingQuantity }}
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" :xs="12" :sm="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon product-icon">
              <el-icon><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalProducts }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12" :sm="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon stock-icon">
              <el-icon><Box /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.totalStock }}</div>
              <div class="stat-label">库存总量</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12" :sm="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon outbound-icon">
              <el-icon><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ stats.todayOutbound }}</div>
              <div class="stat-label">今日出库</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6" :xs="12" :sm="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon value-icon">
              <el-icon><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">¥{{ stats.stockValue.toLocaleString() }}</div>
              <div class="stat-label">库存价值</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧列 -->
      <el-col :span="16" :xs="24" :sm="16">
        <!-- 库存预警 -->
        <el-card class="warning-card" v-if="lowStockProducts.length > 0">
          <template #header>
            <div class="card-header warning-header">
              <el-icon class="warning-icon"><Warning /></el-icon>
              <span>库存预警</span>
              <el-tag type="danger" size="small">{{ lowStockProducts.length }} 件商品</el-tag>
            </div>
          </template>
          <div class="warning-content">
            <div v-for="product in lowStockProducts.slice(0, 5)" :key="product.id" class="warning-item">
              <div class="product-details">
                <div class="product-name">{{ product.name }} {{ product.spec }}</div>
                <div class="product-unit">单位：{{ product.unit }}</div>
              </div>
              <el-tag type="danger" size="small">
                剩余 {{ product.remainingQuantity }}
              </el-tag>
            </div>
            <el-button v-if="lowStockProducts.length > 5" 
              type="text" 
              @click="goToProducts('lowStock')"
              class="view-all-btn">
              查看全部 {{ lowStockProducts.length }} 件
            </el-button>
          </div>
        </el-card>

        <!-- 库存预警详情 -->
        <el-card class="warning-detail-card">
          <template #header>
            <div class="card-header">
              <span>库存预警详情</span>
              <div class="warning-summary">
                <el-tag v-if="outOfStockProducts.length > 0" type="danger" size="small">
                  {{ outOfStockProducts.length }} 件缺货
                </el-tag>
                <el-tag v-if="lowStockProductsDetail.length > 0" type="warning" size="small">
                  {{ lowStockProductsDetail.length }} 件库存不足
                </el-tag>
                <el-tag v-if="outOfStockProducts.length === 0 && lowStockProductsDetail.length === 0" type="success" size="small">
                  库存状态良好
                </el-tag>
              </div>
            </div>
          </template>
          <div class="warning-details">
            <!-- 缺货商品 -->
            <div v-if="outOfStockProducts.length > 0" class="warning-section">
              <div class="section-title danger">
                <el-icon><WarningFilled /></el-icon>
                <span>缺货商品 ({{ outOfStockProducts.length }})</span>
              </div>
              <div class="warning-list">
                <div v-for="product in outOfStockProducts.slice(0, 3)" :key="'out-' + product.id" class="warning-product-item danger">
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-spec">{{ product.spec || '无规格' }} / {{ product.unit }}</div>
                  </div>
                  <div class="stock-status">
                    <el-tag type="danger" size="small">缺货</el-tag>
                  </div>
                </div>
              </div>
              <el-button v-if="outOfStockProducts.length > 3" 
                type="text" 
                size="small"
                @click="goToProducts('outOfStock')"
                class="view-more-btn">
                查看全部 {{ outOfStockProducts.length }} 件缺货商品
              </el-button>
            </div>
            
            <!-- 库存不足商品 -->
            <div v-if="lowStockProductsDetail.length > 0" class="warning-section">
              <div class="section-title warning">
                <el-icon><Warning /></el-icon>
                <span>库存不足商品 ({{ lowStockProductsDetail.length }})</span>
              </div>
              <div class="warning-list">
                <div v-for="product in lowStockProductsDetail.slice(0, 3)" :key="'low-' + product.id" class="warning-product-item warning">
                  <div class="product-info">
                    <div class="product-name">{{ product.name }}</div>
                    <div class="product-spec">{{ product.spec || '无规格' }} / {{ product.unit }}</div>
                  </div>
                  <div class="stock-status">
                    <el-tag type="warning" size="small">剩余 {{ product.remainingQuantity }}</el-tag>
                  </div>
                </div>
              </div>
              <el-button v-if="lowStockProductsDetail.length > 3" 
                type="text" 
                size="small"
                @click="goToProducts('lowStock')"
                class="view-more-btn">
                查看全部 {{ lowStockProductsDetail.length }} 件库存不足商品
              </el-button>
            </div>
            
            <!-- 状态良好 -->
            <div v-if="outOfStockProducts.length === 0 && lowStockProductsDetail.length === 0" class="no-warnings">
              <el-result 
                icon="success" 
                title="库存状态良好"
                sub-title="所有商品库存充足，无需预警"
              />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧列 -->
      <el-col :span="8" :xs="24" :sm="8">
    <!-- 快捷操作 -->
        <el-card class="action-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="actions-grid">
            <el-button type="primary" size="large" class="action-button" @click="goToProducts('add')">
            <el-icon><Plus /></el-icon>
              <span>添加商品</span>
          </el-button>
            <el-button type="success" size="large" class="action-button" @click="showOutboundDialog">
            <el-icon><Remove /></el-icon>
              <span>商品出库</span>
          </el-button>
          <el-button type="info" size="large" class="action-button" @click="goToProducts">
            <el-icon><Search /></el-icon>
              <span>查看库存</span>
          </el-button>
            <el-button type="warning" size="large" class="action-button" @click="showExportDialog">
            <el-icon><Download /></el-icon>
              <span>导出报表</span>
            </el-button>
            <el-button type="" size="large" class="action-button" @click="goToOutboundRecords">
              <el-icon><List /></el-icon>
              <span>出库记录</span>
            </el-button>
            <el-button type="danger" size="large" class="action-button" @click="goToInbound">
              <el-icon><Upload /></el-icon>
              <span>入库管理</span>
          </el-button>
          </div>
        </el-card>

        <!-- 最近活动 -->
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <div v-if="recentActivities.length === 0" class="no-activity">
            暂无最近活动
          </div>
          <div v-else class="activity-list">
            <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
              <div class="activity-icon">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.description }}</div>
                <div class="activity-time">{{ formatActivityTime(activity.createdAt) }}</div>
              </div>
            </div>
          </div>
        </el-card>
        </el-col>
      </el-row>

    <!-- 快捷出库对话框 -->
    <el-dialog v-model="outboundDialogVisible" title="快速出库" width="400px">
      <el-form :model="outboundForm" label-width="80px">
        <el-form-item label="选择商品">
          <el-select 
            v-model="outboundForm.productId" 
            placeholder="请选择商品"
            filterable
            style="width: 100%">
            <el-option
              v-for="product in allProducts"
              :key="product.id"
              :label="`${product.name} ${product.spec || ''}`"
              :value="product.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量">
          <el-input-number 
            v-model="outboundForm.quantity"
            :min="1"
            style="width: 100%">
          </el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="outboundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOutbound" :loading="submitLoading">确认出库</el-button>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出报表" width="400px">
      <el-form :model="exportForm" label-width="80px">
        <el-form-item label="导出类型">
          <el-radio-group v-model="exportForm.type">
            <el-radio value="stock">库存报表</el-radio>
            <el-radio value="outbound">出库记录</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="导出日期">
          <el-date-picker
            v-model="exportForm.date"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExport" :loading="exportLoading">导出</el-button>
      </template>
    </el-dialog>

    <!-- 帮助对话框 -->
    <el-dialog v-model="helpDialogVisible" title="帮助" width="400px">
      <p>欢迎使用库存管理系统！</p>
      <p>1. 在顶部欢迎区域查看系统状态和最后登录时间。</p>
      <p>2. 使用快速搜索输入框搜索商品，支持名称和规格。</p>
      <p>3. 点击统计卡片了解库存、出库和价值情况。</p>
      <p>4. 在左侧查看库存预警和出库趋势图表。</p>
      <p>5. 通过快捷操作快速添加商品、出库或查看报表。</p>
      <p>6. 点击右上角帮助按钮了解更多功能。</p>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import api, { productAPI, outboundRecordAPI, stockAPI } from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshRight, Search, Plus, Remove, Download, List, Upload, Warning, Clock, Goods, Box, Van, Coin, CircleCheck, Calendar, Timer, WarningFilled, QuestionFilled } from '@element-plus/icons-vue'


const router = useRouter()
const userStore = useUserStore()

// 响应式状态
const isLoading = ref(false)
const searchQuery = ref('')
const searchResults = ref([])


const systemStatus = ref(true)
const currentDate = ref('')
const systemStartTime = ref(new Date())
const helpDialogVisible = ref(false)
let autoRefreshTimer = null


// 计算属性：缺货商品（库存 <= 0）
const outOfStockProducts = computed(() => {
  return allProducts.value.filter(product => {
    const currentStock = product.remainingQuantity || 0
    return currentStock <= 0
  }).map(product => ({
    id: product.id,
    name: product.name,
    spec: product.spec,
    unit: product.unit,
    remainingQuantity: product.remainingQuantity,
    initialStock: product.initialStock
  }))
})

// 计算属性：库存不足商品（库存 > 0 且 < 初始库存/2）
const lowStockProductsDetail = computed(() => {
  return allProducts.value.filter(product => {
    const initialStock = product.initialStock || 0
    const currentStock = product.remainingQuantity || 0
    return currentStock > 0 && currentStock < (initialStock / 2)
  }).map(product => ({
    id: product.id,
    name: product.name,
    spec: product.spec,
    unit: product.unit,
    remainingQuantity: product.remainingQuantity,
    initialStock: product.initialStock
  }))
})

// 统计数据
const stats = ref({
  totalProducts: 0,
  totalStock: 0,
  todayOutbound: 0,
  stockValue: 0
})

// 库存预警商品
const lowStockProducts = ref([])

// 最近活动
const recentActivities = ref([])

// 所有商品列表（用于出库选择）
const allProducts = ref([])

// 出库对话框
const outboundDialogVisible = ref(false)
const outboundForm = ref({
  productId: null,
  quantity: 1
})
const submitLoading = ref(false)

// 导出对话框
const exportDialogVisible = ref(false)
const exportForm = ref({
  type: 'stock',
  date: new Date().toISOString().split('T')[0]
})
const exportLoading = ref(false)

// 搜索防抖定时器
let searchTimer = null

// 格式化最后登录时间
const formatLastLogin = () => {
  const lastLogin = userStore.currentUser?.lastLogin
  if (!lastLogin) return '首次登录'
  
  try {
    const date = new Date(lastLogin)
    return date.toLocaleString('zh-CN')
  } catch (error) {
    return lastLogin
  }
}

// 格式化活动时间
const formatActivityTime = (timestamp: string) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60 * 1000) {
    return '刚刚'
  } else if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  } else if (diff < 24 * 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 60 * 1000))}小时前`
  } else {
    return date.toLocaleDateString('zh-CN')
  }
}

// 格式化系统运行时间
const formatUptime = () => {
  const now = new Date()
  const diff = now.getTime() - systemStartTime.value.getTime()
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${hours}小时${minutes}分钟`
}

// 更新当前日期
const updateCurrentDate = () => {
  const now = new Date()
  const options: Intl.DateTimeFormatOptions = { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
}

// 键盘快捷键处理
const handleKeypress = (event: KeyboardEvent) => {
  if (event.ctrlKey || event.metaKey) {
    switch (event.key) {
      case 'r':
      case 'R':
        event.preventDefault()
        refreshAllData()
        break
      case 'f':
      case 'F':
        event.preventDefault()
        const searchInput = document.querySelector('.search-input input') as HTMLInputElement
        searchInput?.focus()
        break
      case 'n':
      case 'N':
        event.preventDefault()
        goToProducts('add')
        break
    }
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    // 并行获取所有统计数据
    const [productsResponse, stockResponse, outboundResponse, valueResponse] = await Promise.all([
      api.get('/products/count'),
      api.get('/products/stock/total'), 
      api.get('/outbound/today'),
      api.get('/products/stock/value')
    ])
    
    // 更新统计数据
    stats.value.totalProducts = productsResponse.data || 0
    stats.value.totalStock = stockResponse.data || 0
    stats.value.todayOutbound = outboundResponse.data || 0
    stats.value.stockValue = valueResponse.data || 0
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取所有商品数据（同时用于库存预警和出库选择）
const fetchAllProducts = async () => {
  try {
    const response = await productAPI.getAllProducts()
    allProducts.value = response.data || []
    
    // 同时计算库存预警商品（库存 < 初始库存 / 2）
    lowStockProducts.value = allProducts.value.filter(product => {
      const initialStock = product.initialStock || 0
      const currentStock = product.remainingQuantity || 0
      return currentStock > 0 && currentStock < (initialStock / 2)
    }).map(product => ({
      id: product.id,
      name: product.name,
      spec: product.spec,
      unit: product.unit,
      remainingQuantity: product.remainingQuantity,
      initialStock: product.initialStock
    }))
  } catch (error) {
    console.error('获取商品数据失败:', error)
  }
}

// 获取库存预警商品（现在直接使用fetchAllProducts的结果）
const fetchLowStockProducts = async () => {
  // 如果allProducts为空，先获取所有商品数据
  if (allProducts.value.length === 0) {
    await fetchAllProducts()
  } else {
    // 直接基于现有数据计算库存预警
    lowStockProducts.value = allProducts.value.filter(product => {
      const initialStock = product.initialStock || 0
      const currentStock = product.remainingQuantity || 0
      return currentStock > 0 && currentStock < (initialStock / 2)
    }).map(product => ({
      id: product.id,
      name: product.name,
      spec: product.spec,
      unit: product.unit,
      remainingQuantity: product.remainingQuantity,
      initialStock: product.initialStock
    }))
  }
}

// 获取最近活动（模拟数据，可以后续连接真实API）
const fetchRecentActivities = async () => {
  try {
    // 获取最近几条出库记录作为活动
    const response = await outboundRecordAPI.getOutboundRecords({
      page: 0,
      size: 5,
      sort: 'createdAt',
      direction: 'desc'
    })
    
    if (response.data && response.data.content) {
      recentActivities.value = response.data.content.map(record => ({
        id: record.id,
        description: `${record.productName} 出库 ${record.quantity} ${record.unit}`,
        createdAt: record.createdAt
      }))
    }
  } catch (error) {
    console.error('获取最近活动失败:', error)
  }
}

// 刷新库存预警数据
const fetchTrendData = async () => {
  // 直接调用fetchAllProducts，计算属性会自动更新预警数据
  await fetchAllProducts()
}

// 搜索商品
const onSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  searchTimer = setTimeout(async () => {
    if (!searchQuery.value.trim()) {
      searchResults.value = []
      return
    }
    
    try {
      const response = await productAPI.getProducts({
        name: searchQuery.value,
        size: 5
      })
      
      searchResults.value = response.data?.content || []
    } catch (error) {
      console.error('搜索商品失败:', error)
    }
  }, 300)
}

// 选择搜索结果商品
const selectProduct = (product) => {
  searchQuery.value = ''
  searchResults.value = []
  router.push(`/dashboard/products?id=${product.id}`)
}

// 显示出库对话框
const showOutboundDialog = () => {
  outboundDialogVisible.value = true
  // 如果商品列表为空，先获取数据
  if (allProducts.value.length === 0) {
    fetchAllProducts()
  }
}

// 提交出库
const submitOutbound = async () => {
  if (!outboundForm.value.productId || !outboundForm.value.quantity) {
    ElMessage.warning('请完善出库信息')
    return
  }
  
  submitLoading.value = true
  try {
    await outboundRecordAPI.createOutboundRecord({
      productId: outboundForm.value.productId,
      quantity: outboundForm.value.quantity
    })
    
    ElMessage.success('出库成功')
    outboundDialogVisible.value = false
    outboundForm.value = { productId: null, quantity: 1 }
    
    // 刷新数据
    refreshAllData()
  } catch (error) {
    console.error('出库失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 显示导出对话框
const showExportDialog = () => {
  exportDialogVisible.value = true
}

// 处理导出
const handleExport = async () => {
  exportLoading.value = true
  try {
    if (exportForm.value.type === 'stock') {
      const response = await stockAPI.exportStock(exportForm.value.date)
      // 处理文件下载
      const blob = new Blob([response.data], { type: 'text/csv' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `库存导出_${exportForm.value.date}.csv`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
    } else {
      const response = await outboundRecordAPI.exportOutboundRecords(
        exportForm.value.date,
        exportForm.value.date
      )
      console.log('导出出库记录:', response)
    }
    
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

// 刷新所有数据
const refreshAllData = async () => {
  isLoading.value = true
  try {
    await Promise.all([
      fetchStats(),
      fetchAllProducts(), // 这个函数现在同时处理库存预警计算
      fetchRecentActivities(),
      fetchTrendData()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    isLoading.value = false
  }
}

// 路由跳转方法
const goToProducts = (mode = '') => {
  if (mode === 'add') {
    router.push('/dashboard/products?action=add')
  } else if (mode === 'lowStock') {
    router.push('/dashboard/products?filter=lowStock')
  } else if (mode === 'outOfStock') {
    router.push('/dashboard/products?filter=outOfStock')
  } else {
    router.push('/dashboard/products')
  }
}

const goToOutboundRecords = () => {
  router.push('/dashboard/outbound')
}

const goToInbound = () => {
  router.push('/dashboard/inbound')
}

// 显示帮助对话框
const showHelpDialog = () => {
  helpDialogVisible.value = true
}

// 启动自动刷新
const startAutoRefresh = () => {
  autoRefreshTimer = setInterval(async () => {
    try {
      await Promise.all([
        fetchStats(),
        fetchAllProducts() // 这个函数现在同时处理库存预警计算，fetchTrendData会自动通过计算属性更新
      ])
    } catch (error) {
      console.error('自动刷新失败:', error)
    }
  }, 15000) // 每15秒刷新一次，提高数据实时性
}

// 组件挂载时获取数据
onMounted(async () => {
  await refreshAllData()
  // 启动定时器更新日期
  updateCurrentDate()
  setInterval(updateCurrentDate, 1000)
  // 添加键盘快捷键监听
  document.addEventListener('keypress', handleKeypress)
  startAutoRefresh() // 启动自动刷新
})

// 组件销毁时清理资源
onUnmounted(() => {
  // 清理定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
  
  // 移除事件监听器
  document.removeEventListener('keypress', handleKeypress)
})
</script>

<style scoped>
.dashboard-home {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 80px);
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 20px;
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 搜索卡片 */
.search-card {
  margin-bottom: 20px;
}

.search-input {
  margin-bottom: 10px;
}

.search-results {
  max-height: 200px;
  overflow-y: auto;
}

.search-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.2s;
}

.search-item:hover {
  background-color: #f5f7fa;
  border-color: #409eff;
}

.search-item:last-child {
  margin-bottom: 0;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-info span {
  color: #909399;
  font-size: 12px;
}

.product-stock {
  color: #67c23a;
  font-weight: 600;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.product-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stock-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.outbound-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.value-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  line-height: 1;
}

/* 主要内容区域 */
.main-content {
  margin-bottom: 20px;
}

/* 库存预警卡片 */
.warning-card {
  margin-bottom: 20px;
  border-left: 4px solid #f56c6c;
}

.warning-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.warning-icon {
  color: #f56c6c;
}

.warning-content {
  max-height: 300px;
  overflow-y: auto;
}

.warning-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #fde2e2;
  border-radius: 6px;
  margin-bottom: 8px;
  background-color: #fef0f0;
}

.warning-item:last-child {
  margin-bottom: 0;
}

.product-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-weight: 600;
  color: #333;
}

.product-unit {
  font-size: 12px;
  color: #909399;
}

.view-all-btn {
  width: 100%;
  margin-top: 12px;
}

/* 图表卡片 */
.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 库存预警详情样式 */
.warning-detail-card {
  margin-bottom: 20px;
  border-left: 4px solid #f56c6c;
}

.warning-summary {
  display: flex;
  gap: 8px;
  align-items: center;
}

.warning-details {
  padding: 20px;
  min-height: 260px;
}

.warning-section {
  margin-bottom: 25px;
}

.warning-section:last-child {
  margin-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 16px;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid transparent;
}

.section-title.danger {
  color: #f56c6c;
  border-bottom-color: #f56c6c;
}

.section-title.warning {
  color: #e6a23c;
  border-bottom-color: #e6a23c;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.warning-product-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid;
  transition: all 0.2s;
}

.warning-product-item.danger {
  background-color: #fef0f0;
  border-color: #fde2e2;
}

.warning-product-item.danger:hover {
  background-color: #fde2e2;
  transform: translateX(4px);
}

.warning-product-item.warning {
  background-color: #fdf6ec;
  border-color: #f5dab1;
}

.warning-product-item.warning:hover {
  background-color: #f5dab1;
  transform: translateX(4px);
}

.warning-product-item .product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.warning-product-item .product-name {
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.warning-product-item .product-spec {
  font-size: 12px;
  color: #7f8c8d;
}

.stock-status {
  display: flex;
  align-items: center;
}

.view-more-btn {
  margin-top: 8px;
  width: 100%;
  color: #409eff;
  font-weight: 500;
}

.no-warnings {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 220px;
}

.no-warnings :deep(.el-result__title) {
  color: #67c23a;
  font-size: 18px;
}

.no-warnings :deep(.el-result__subtitle) {
  color: #909399;
  font-size: 14px;
}

/* 快捷操作 */
.actions-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.action-button {
  height: 60px;
  font-size: 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.2s;
}

.action-button:hover {
  transform: translateY(-1px);
}

.action-button .el-icon {
  font-size: 18px;
}

/* 最近活动 */
.activity-card {
  margin-top: 20px;
}

.no-activity {
  text-align: center;
  color: #909399;
  padding: 20px;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 32px;
  height: 32px;
  background-color: #f5f7fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  flex-shrink: 0;
}

.activity-content {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-home {
    padding: 15px;
  }
  
  .welcome-content {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .stats-row .el-col {
    margin-bottom: 15px;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .main-content .el-col:first-child {
    margin-bottom: 20px;
  }
}

@media (max-width: 480px) {
  .welcome-section {
    padding: 20px;
  }
  
  .welcome-text h2 {
  font-size: 20px;
  }
  
  .stat-number {
    font-size: 24px;
  }
  
  .action-button {
    height: 50px;
    font-size: 12px;
  }
}

/* 卡片标题样式 */
.card-header {
  font-weight: 600;
  color: #333;
}

/* 过渡动画 */
.el-card {
  transition: all 0.3s ease;
}

/* 系统状态栏 */
.status-card {
  margin-bottom: 20px;
  background: linear-gradient(90deg, #f8f9fa 0%, #ffffff 100%);
}

.status-content {
  display: flex;
  justify-content: space-around;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  font-size: 14px;
}

.status-icon {
  font-size: 16px;
}

.status-icon.online {
  color: #67c23a;
}

.status-icon.warning {
  color: #f56c6c;
}

/* 帮助对话框样式 */
.help-dialog .el-message-box__title {
  color: #409eff;
}

.help-dialog .el-message-box__content {
  color: #606266;
  font-size: 14px;
}

.help-dialog .el-message-box__btns {
  justify-content: center;
}
</style>