<template>
  <div class="stock-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon class="header-icon"><House /></el-icon>
          <div>
            <h2>库存管理</h2>
            <p>查询和导出指定日期的商品库存信息</p>
          </div>
        </div>
        <div class="header-stats" v-if="stockList.length">
          <div class="stat-item">
            <el-icon><DataAnalysis /></el-icon>
            <div>
              <div class="stat-value">{{ stockList.length }}</div>
              <div class="stat-label">商品总数</div>
            </div>
          </div>
        </div>
      </div>
      <el-alert 
        v-if="dateRange.minDate"
        :title="`可查询日期范围：${dateRange.minDate} 至 ${dateRange.maxDate} （今天：${new Date().toISOString().split('T')[0]}）`"
        type="info"
        :closable="false"
        class="date-alert"
      />
    </div>

    <!-- 查询表单 -->
    <el-card class="search-card">
      <div class="search-form">
        <!-- 第一行：日期选择和主要操作 -->
        <el-row :gutter="20" align="middle" style="margin-bottom: 15px">
          <el-col :span="8">
            <div class="form-item">
              <label>选择日期：</label>
              <el-date-picker
                v-model="selectedDate"
                type="date"
                placeholder="请选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="(time) => {
                  // 获取选择日期的字符串格式 YYYY-MM-DD
                  const selectedDateStr = time.getFullYear() + '-' + 
                    String(time.getMonth() + 1).padStart(2, '0') + '-' + 
                    String(time.getDate()).padStart(2, '0')
                  
                  // 获取今天的日期字符串格式
                  const today = new Date()
                  const todayStr = today.getFullYear() + '-' + 
                    String(today.getMonth() + 1).padStart(2, '0') + '-' + 
                    String(today.getDate()).padStart(2, '0')
                  
                  // 获取最小日期
                  const minDateStr = dateRange.minDate || ''
                  
                  // 禁用未来日期和早于最小日期的日期
                  const isDisabled = selectedDateStr > todayStr || (minDateStr && selectedDateStr < minDateStr)
                  
                  // 调试信息（仅在禁用时输出）
                  if (isDisabled && selectedDateStr >= todayStr) {
                    console.log(`日期 ${selectedDateStr} 被禁用（未来日期）, 今天: ${todayStr}`)
                  }
                  
                  return isDisabled
                }"
                @change="handleDateChange"
              />
            </div>
          </el-col>
          <el-col :span="8">
            <el-button type="primary" @click="queryStock" :loading="loading">
              <el-icon><Search /></el-icon>
              查询库存
            </el-button>
            <el-button 
              type="success" 
              @click="exportStock" 
              :disabled="!filteredStockList.length"
              :loading="exportLoading"
            >
              <el-icon><Download /></el-icon>
              导出全部筛选结果
            </el-button>
            <el-button 
              type="primary" 
              @click="exportSelected" 
              :disabled="!selectedRows.length"
              :loading="exportLoading"
            >
              <el-icon><Download /></el-icon>
              导出选中 ({{ selectedRows.length }})
            </el-button>
          </el-col>
          <el-col :span="8">
            <div style="text-align: right">
              <el-button 
                type="info" 
                plain 
                @click="toggleFilter"
                size="small"
              >
                <el-icon><Filter /></el-icon>
                {{ showFilter ? '隐藏筛选' : '显示筛选' }}
              </el-button>
            </div>
          </el-col>
        </el-row>
        
        <!-- 第二行：筛选条件（可折叠） -->
        <el-collapse-transition>
          <div v-show="showFilter" class="filter-section">
            <el-divider content-position="left">筛选条件</el-divider>
            <el-row :gutter="20" align="middle">
              <el-col :span="6">
                <div class="form-item">
                  <label>商品名称：</label>
                  <el-input
                    v-model="filterForm.productName"
                    placeholder="输入商品名称"
                    clearable
                    @input="applyFilter"
                  />
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>库存状态：</label>
                  <el-select
                    v-model="filterForm.stockStatus"
                    placeholder="选择库存状态"
                    clearable
                    @change="applyFilter"
                    style="width: 100%"
                  >
                    <el-option label="全部" value="" />
                    <el-option label="正常" value="normal" />
                    <el-option label="库存不足" value="low" />
                    <el-option label="缺货" value="out" />
                  </el-select>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label>库存数量：</label>
                  <el-input-number
                    v-model="filterForm.minStock"
                    placeholder="最小库存"
                    :min="0"
                    controls-position="right"
                    @change="applyFilter"
                    style="width: 100%"
                  />
                </div>
              </el-col>
              <el-col :span="6">
                <div class="form-item">
                  <label style="opacity: 0;">占位</label>
                  <el-button type="warning" plain @click="clearFilter">
                    <el-icon><RefreshLeft /></el-icon>
                    清空筛选
                  </el-button>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-collapse-transition>
      </div>
    </el-card>

    <!-- 库存信息展示 -->
    <el-card class="result-card">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <el-icon><House /></el-icon>
            <span>库存信息</span>
          </div>
          <div class="header-info">
            <span v-if="selectedDate" class="date-info">
              <el-icon><DataAnalysis /></el-icon>
              统计日期：{{ selectedDate }}
            </span>
            <span v-if="selectedRows.length" class="selected-info">
              已选择：{{ selectedRows.length }} 项
            </span>
          </div>
        </div>
      </template>

      <!-- 库存表格 -->
      <div class="table-toolbar" v-if="filteredStockList.length">
        <div class="table-info">
          <span>共 {{ totalItems }} 条记录，当前显示第 {{ (currentPage - 1) * pageSize + 1 }}-{{ Math.min(currentPage * pageSize, totalItems) }} 条</span>
          <span v-if="selectedRows.length" style="margin-left: 16px; color: #409EFF">
            已选择 {{ selectedRows.length }} 条
          </span>
        </div>
        <div class="table-actions">
          <el-button 
            size="small" 
            @click="selectAllOnPage"
            :disabled="!paginatedStockList.length"
          >
            全选当前页
          </el-button>
          <el-button 
            size="small" 
            @click="selectNone"
            :disabled="!selectedRows.length"
          >
            取消全选
          </el-button>
          <el-button 
            size="small" 
            @click="selectInvertOnPage"
            :disabled="!paginatedStockList.length"
          >
            反选当前页
          </el-button>
        </div>
      </div>
      
      <el-table
        ref="tableRef"
        :data="paginatedStockList"
        style="width: 100%"
        v-loading="loading"
        stripe
        border
        row-key="productId"
        @selection-change="handleSelectionChange"
        :row-class-name="getRowClassName"
        class="beautiful-table"
        :default-sort="{ prop: 'productId', order: 'ascending' }"
      >
        <el-table-column 
          type="selection" 
          width="50" 
          :reserve-selection="true"
        />
        <el-table-column prop="productId" label="ID" width="100" align="center" sortable :sort-orders="['ascending', 'descending']" :sort-method="(a, b) => a.productId - b.productId">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain" style="color: black; border-color: black;">#{{ row.productId }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品图片" width="200" align="center">
          <template #default="{ row }">
            <div class="product-image-container">
              <el-image
                v-if="row.imageUrl"
                :src="row.imageUrl"
                :fit="'cover'"
                class="product-image"
                :preview-src-list="[row.imageUrl]"
                :initial-index="0"
                preview-teleported
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-placeholder">
                <el-icon><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" width="200" align="center">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-spec" v-if="row.spec">
                <el-tag size="small" type="success" effect="plain">{{ row.spec }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="200" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="dark">{{ row.unit }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存信息" width="250" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <div class="stock-item">
                <span class="stock-label">初始:</span>
                <span class="stock-value">{{ row.initialStock }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">入库:</span>
                <span class="stock-value inbound">{{ row.totalInbound || 0 }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">出库:</span>
                <span class="stock-value outbound">{{ row.totalOutbound }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="stockQuantity" label="当前库存" width="200" align="center">
          <template #default="scope">
            <div class="current-stock">
              <el-tag 
                :type="scope.row.stockQuantity <= 0 ? 'danger' : scope.row.stockQuantity < (scope.row.initialStock / 2) ? 'warning' : 'success'"
                size="large"
                effect="dark"
                class="stock-tag"
              >
                {{ scope.row.stockQuantity }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格信息" width="200" align="center">
          <template #default="{ row }">
            <div class="price-info">
              <div class="price-item">
                <span class="price-label">单价:</span>
                <span class="price-value">¥{{ row.unitPrice.toFixed(2) }}</span>
              </div>
              <div class="value-item">
                <span class="value-label">总值:</span>
                <span class="value-amount">¥{{ row.totalValue.toFixed(2) }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存状态" width="200" align="center">
          <template #default="scope">
            <div class="status-container">
              <el-tag 
                v-if="scope.row.stockQuantity <= 0" 
                type="danger"
                size="large"
                effect="dark"
                class="status-tag"
              >
                缺货
              </el-tag>
              <el-tag 
                v-else-if="scope.row.stockQuantity < (scope.row.initialStock / 2)" 
                type="warning"
                size="large"
                effect="dark"
                class="status-tag"
              >
                库存不足
              </el-tag>
              <el-tag 
                v-else 
                type="success"
                size="large"
                effect="dark"
                class="status-tag"
              >
                正常
              </el-tag>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-container" v-if="filteredStockList.length">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :total="totalItems"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <!-- 统计信息 -->
      <div class="summary-info" v-if="stockList.length">
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="6">
            <el-statistic 
              :title="filterForm.productName || filterForm.stockStatus || filterForm.minStock !== undefined ? '筛选结果数量' : '商品总数'" 
              :value="filteredStockList.length" 
            />
            <div style="font-size: 12px; color: #999; margin-top: 4px">
              {{ stockList.length !== filteredStockList.length ? `总计: ${stockList.length}` : '' }}
            </div>
          </el-col>
          <el-col :span="6">
            <el-statistic title="库存总价值" :value="filteredTotalStockValue" :precision="2" prefix="¥" />
            <div style="font-size: 12px; color: #999; margin-top: 4px">
              {{ stockList.length !== filteredStockList.length ? `全部: ¥${totalStockValue.toFixed(2)}` : '' }}
            </div>
          </el-col>
          <el-col :span="6">
            <el-statistic title="库存不足商品" :value="filteredLowStockCount" />
            <div style="font-size: 12px; color: #999; margin-top: 4px">
              {{ stockList.length !== filteredStockList.length ? `全部: ${lowStockCount}` : '' }}
            </div>
          </el-col>
          <el-col :span="6">
            <el-statistic title="缺货商品" :value="filteredOutOfStockCount" />
            <div style="font-size: 12px; color: #999; margin-top: 4px">
              {{ stockList.length !== filteredStockList.length ? `全部: ${outOfStockCount}` : '' }}
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-if="!filteredStockList.length && !loading" 
        :description="getEmptyDescription()"
        :image-size="100"
      >
        <template #description>
          <p v-if="stockList.length && !filteredStockList.length">
            筛选条件无匹配结果
          </p>
          <p v-else-if="selectedDate && !stockList.length">
            {{ selectedDate }} 当日暂无商品数据
          </p>
          <p v-else>
            请选择日期查询库存信息
          </p>
          <p style="color: #999; font-size: 12px; margin-top: 8px">
            库存计算公式：库存数量 = 初始库存 - 截止该日期的累计出库
          </p>
          <el-button 
            v-if="stockList.length && !filteredStockList.length" 
            type="primary" 
            plain 
            @click="clearFilter"
            style="margin-top: 16px"
          >
            清空筛选条件
          </el-button>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, Filter, RefreshLeft, Picture, House, DataAnalysis } from '@element-plus/icons-vue'
import { stockAPI } from '../api'

// 响应式数据
const selectedDate = ref('')
const stockList = ref<any[]>([])
const loading = ref(false)
const exportLoading = ref(false)
const showFilter = ref(false)
const selectedRows = ref<any[]>([]) // 选中的行数据
const tableRef = ref() // 表格引用
const dateRange = ref<{minDate: string, maxDate: string}>({
  minDate: '',
  maxDate: (() => {
    const today = new Date()
    return today.getFullYear() + '-' + 
      String(today.getMonth() + 1).padStart(2, '0') + '-' + 
      String(today.getDate()).padStart(2, '0')
  })()
})

// 分页相关数据
const currentPage = ref(1)
const pageSize = ref(10)
const pageSizes = [10, 20, 50, 100]

// 筛选表单
const filterForm = ref({
  productName: '',
  stockStatus: '',
  minStock: undefined as number | undefined
})

// 计算属性
const totalStockValue = computed(() => {
  return stockList.value.reduce((sum, item) => sum + item.totalValue, 0)
})

const lowStockCount = computed(() => {
  return stockList.value.filter(item => item.stockQuantity > 0 && item.stockQuantity < (item.initialStock / 2)).length
})

const outOfStockCount = computed(() => {
  return stockList.value.filter(item => item.stockQuantity <= 0).length
})

// 筛选后的库存列表
const filteredStockList = computed(() => {
  let result = stockList.value
  
  // 按商品名称筛选
  if (filterForm.value.productName) {
    result = result.filter(item => 
      item.productName.toLowerCase().includes(filterForm.value.productName.toLowerCase())
    )
  }
  
  // 按库存状态筛选
  if (filterForm.value.stockStatus) {
    result = result.filter(item => {
      switch (filterForm.value.stockStatus) {
        case 'normal':
          return item.stockQuantity >= (item.initialStock / 2)
        case 'low':
          return item.stockQuantity > 0 && item.stockQuantity < (item.initialStock / 2)
        case 'out':
          return item.stockQuantity <= 0
        default:
          return true
      }
    })
  }
  
  // 按最小库存数量筛选
  if (filterForm.value.minStock !== undefined && filterForm.value.minStock !== null) {
    result = result.filter(item => item.stockQuantity >= filterForm.value.minStock!)
  }
  
  return result
})

// 分页相关计算属性
const totalItems = computed(() => filteredStockList.value.length)
const totalPages = computed(() => Math.ceil(totalItems.value / pageSize.value))

// 当前页的数据
const paginatedStockList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStockList.value.slice(start, end)
})

// 基于筛选结果的统计
const filteredTotalStockValue = computed(() => {
  return filteredStockList.value.reduce((sum, item) => sum + item.totalValue, 0)
})

const filteredLowStockCount = computed(() => {
  return filteredStockList.value.filter(item => item.stockQuantity > 0 && item.stockQuantity < (item.initialStock / 2)).length
})

const filteredOutOfStockCount = computed(() => {
  return filteredStockList.value.filter(item => item.stockQuantity <= 0).length
})

// 获取日期范围
const fetchDateRange = async () => {
  try {
    const response = await stockAPI.getDateRange()
    if ((response as any).success) {
      const data = (response as any).data
      // 确保最大日期不超过今天
      const today = new Date()
      const todayStr = today.getFullYear() + '-' + 
        String(today.getMonth() + 1).padStart(2, '0') + '-' + 
        String(today.getDate()).padStart(2, '0')
      
      dateRange.value = {
        minDate: data.minDate,
        maxDate: todayStr  // 强制设置为今天
      }
      console.log('可选日期范围:', dateRange.value)
    }
  } catch (error) {
    console.error('获取日期范围失败:', error)
  }
}



// 筛选相关方法
const toggleFilter = () => {
  showFilter.value = !showFilter.value
}

const applyFilter = () => {
  // 筛选是通过计算属性自动应用的，这里可以添加额外逻辑
  console.log('应用筛选:', filterForm.value)
  // 重置到第一页
  currentPage.value = 1
  // 清空选中状态
  selectedRows.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const clearFilter = () => {
  filterForm.value = {
    productName: '',
    stockStatus: '',
    minStock: undefined
  }
  // 重置到第一页
  currentPage.value = 1
  // 清空选中状态
  selectedRows.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const getEmptyDescription = () => {
  if (stockList.value.length && !filteredStockList.value.length) {
    return '筛选条件无匹配结果'
  } else if (selectedDate.value && !stockList.value.length) {
    return `${selectedDate.value} 当日暂无商品数据`
  } else {
    return '请选择日期查询库存信息'
  }
}

// 表格选择相关方法
const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

// 分页选择方法
const selectAllOnPage = () => {
  paginatedStockList.value.forEach((row: any) => {
    tableRef.value.toggleRowSelection(row, true)
  })
}

const selectNone = () => {
  tableRef.value.clearSelection()
}

const selectInvertOnPage = () => {
  paginatedStockList.value.forEach((row: any) => {
    tableRef.value.toggleRowSelection(row)
  })
}

// 分页事件处理方法
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  // 清空选中状态
  selectedRows.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  // 清空选中状态
  selectedRows.value = []
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
}

// 日期变化处理方法
const handleDateChange = (value: string) => {
  selectedDate.value = value
  console.log('日期已更改:', value)
  // 可以在这里添加其他日期变化时需要执行的逻辑
}

// 方法
const queryStock = async () => {
  loading.value = true
  try {
    console.log('开始查询当天库存，日期:', selectedDate.value)
    const response = await stockAPI.getHistoryStock(selectedDate.value)
    if ((response as any).success) {
      stockList.value = (response as any).data || []
      
      // 重置分页和选中状态
      currentPage.value = 1
      selectedRows.value = []
      if (tableRef.value) {
        tableRef.value.clearSelection()
      }
      
      if (stockList.value.length === 0) {
        ElMessage.info(`${selectedDate.value} 当日暂无商品库存记录`)
      } else {
        // 计算实际有库存的商品数量
        const hasStockCount = stockList.value.filter(item => item.stockQuantity > 0).length
        ElMessage.success(`查询成功！共${stockList.value.length}个商品，其中${hasStockCount}个有库存`)
      }
    }
  } catch (error) {
    console.error('查询库存失败:', error)
    stockList.value = []
    ElMessage.error('查询失败，请检查网络连接和服务状态')
  } finally {
    loading.value = false
  }
}

const exportStock = async () => {
  if (!selectedDate.value) {
    ElMessage.warning('请先选择日期')
    return
  }

  if (!filteredStockList.value.length) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  exportLoading.value = true
  try {
    console.log('开始导出，日期:', selectedDate.value)
    console.log('筛选条件:', filterForm.value)
    console.log('导出数据量:', filteredStockList.value.length)
    
    // 如果有筛选条件，则导出筛选后的数据
    if (filteredStockList.value.length < stockList.value.length) {
      // 客户端生成筛选后的CSV
      exportFilteredData()
    } else {
      // 使用后端API导出全部数据
      const response = await stockAPI.exportStock(selectedDate.value)
      console.log('导出响应:', response)
      
      // 创建下载链接
      const blob = new Blob([response.data], { 
        type: 'text/csv; charset=utf-8' 
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `库存导出_${selectedDate.value}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    }
    
    ElMessage.success('导出成功')
  } catch (error: any) {
    console.error('导出失败:', error)
    let errorMessage = '导出失败，请重试'
    if (error.response) {
      errorMessage = `导出失败: ${error.response.status} ${error.response.statusText}`
    } else if (error.message) {
      errorMessage = `导出失败: ${error.message}`
    }
    ElMessage.error(errorMessage)
  } finally {
    exportLoading.value = false
  }
}

// 导出筛选后的数据
const exportFilteredData = () => {
  // 生成CSV内容
  let csvContent = '\uFEFF' // BOM for UTF-8
  csvContent += '商品名称,规格,单位,初始库存,累计入库,累计出库,库存数量,单价,库存总价值,统计日期\n'
  
  filteredStockList.value.forEach(item => {
    csvContent += `${item.productName || ''},${item.spec || ''},${item.unit || ''},${item.initialStock},${item.totalInbound || 0},${item.totalOutbound},${item.stockQuantity},${item.unitPrice.toFixed(2)},${item.totalValue.toFixed(2)},${item.stockDate}\n`
  })
  
  // 创建下载
  const blob = new Blob([csvContent], { type: 'text/csv; charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  
  // 生成文件名
  let filename = `库存导出_${selectedDate.value}`
  if (filterForm.value.productName) filename += `_${filterForm.value.productName}`
  if (filterForm.value.stockStatus) filename += `_${filterForm.value.stockStatus}`
  filename += '.csv'
  
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

// 导出选中的数据
const exportSelected = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要导出的数据')
    return
  }

  exportLoading.value = true
  try {
    console.log('导出选中数据，数量:', selectedRows.value.length)
    
    // 生成CSV内容
    let csvContent = '\uFEFF' // BOM for UTF-8
    csvContent += '商品名称,规格,单位,初始库存,累计入库,累计出库,库存数量,单价,库存总价值,统计日期\n'
    
    selectedRows.value.forEach(item => {
      csvContent += `${item.productName || ''},${item.spec || ''},${item.unit || ''},${item.initialStock},${item.totalInbound || 0},${item.totalOutbound},${item.stockQuantity},${item.unitPrice.toFixed(2)},${item.totalValue.toFixed(2)},${item.stockDate}\n`
    })
    
    // 创建下载
    const blob = new Blob([csvContent], { type: 'text/csv; charset=utf-8' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    let filename = `库存导出_选中${selectedRows.value.length}项_${selectedDate.value}`
    if (filterForm.value.productName) filename += `_${filterForm.value.productName}`
    if (filterForm.value.stockStatus) filename += `_${filterForm.value.stockStatus}`
    filename += '.csv'
    
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success(`成功导出 ${selectedRows.value.length} 条选中数据`)
  } catch (error) {
    console.error('导出选中数据失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

// 测试API连接
const testApiConnection = async () => {
  try {
    const response = await fetch('http://localhost:8084/api/health')
    console.log('API健康检查:', response.status, response.statusText)
  } catch (error) {
    console.error('API连接失败:', error)
    ElMessage.warning('无法连接到后端服务，请确保服务已启动')
  }
}

// 表格行样式类名
const getRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 初始化
onMounted(async () => {
  // 测试API连接
  testApiConnection()
  
  // 获取日期范围
  await fetchDateRange()
  
  // 设置默认日期为今天，确保不会选择未来日期
  const today = new Date()
  const todayStr = today.getFullYear() + '-' + 
    String(today.getMonth() + 1).padStart(2, '0') + '-' + 
    String(today.getDate()).padStart(2, '0')
  
  selectedDate.value = todayStr
  console.log('初始化默认日期:', todayStr)
  
  queryStock()
})
</script>

<style scoped>
.stock-management {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
  border-radius: 12px;
}

.page-header {
  margin-bottom: 30px;
  padding: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  font-size: 48px;
  color: #ffd700;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: white;
  font-size: 32px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.page-header p {
  margin: 0;
  color: rgba(255, 255, 255, 0.9);
  font-size: 16px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.header-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.stat-item .el-icon {
  font-size: 28px;
  color: #ffd700;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.date-alert {
  margin-top: 15px;
  border-radius: 12px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.search-card {
  margin-bottom: 25px;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: rgba(255, 255, 255, 0.9);
}

.search-card :deep(.el-card__body) {
  padding: 25px;
}

.search-form {
  padding: 10px 0;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-item label {
  font-weight: 600;
  white-space: nowrap;
  min-width: 80px;
  color: #2c3e50;
}

.result-card {
  min-height: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(15px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-bottom: none;
  color: white;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
  font-size: 20px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.table-title .el-icon {
  font-size: 28px;
  color: #ffd700;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.date-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.date-info .el-icon {
  font-size: 16px;
  color: #ffd700;
}

.beautiful-table {
  border-radius: 0 0 16px 16px;
  overflow: hidden;
}

.beautiful-table :deep(.el-table__header-wrapper th) {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #495057;
  font-weight: 700;
  font-size: 15px;
  padding: 20px 12px;
  border-bottom: 2px solid #dee2e6;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.beautiful-table :deep(.el-table__row) {
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.beautiful-table :deep(.el-table__row:hover) {
  background: linear-gradient(135deg, #e3f2fd 0%, #f3e5f5 100%);
  transform: scale(1.01);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.beautiful-table :deep(.el-table__row.even-row) {
  background: rgba(248, 249, 250, 0.5);
}

.beautiful-table :deep(.el-table__row.odd-row) {
  background: rgba(255, 255, 255, 0.8);
}

.beautiful-table :deep(.el-table td) {
  padding: 25px 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.beautiful-table :deep(.el-table th) {
  padding: 28px 24px;
}

.product-image-container {
  position: relative;
  width: 90px;
  height: 90px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.8);
  margin: 0 auto;
}

.product-image-container:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.product-image:hover {
  transform: scale(1.1);
}

.image-error {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  color: #6c757d;
  font-size: 28px;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6c757d;
  font-size: 12px;
  font-weight: 500;
}

.product-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 8px 0;
}

.product-name {
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  transition: color 0.3s ease;
}

.product-name:hover {
  color: #667eea;
}

.product-spec .el-tag {
  font-size: 11px;
  font-weight: 600;
  border-radius: 12px;
  padding: 4px 8px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.stock-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.stock-label {
  font-size: 12px;
  color: #6c757d;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.stock-value {
  font-size: 16px;
  font-weight: 700;
  color: #2c3e50;
  padding: 2px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
}

.stock-value.inbound {
  color: #27ae60;
  background: rgba(39, 174, 96, 0.1);
}

.stock-value.outbound {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
}

.current-stock {
  display: flex;
  justify-content: center;
  align-items: center;
}

.stock-tag {
  font-size: 16px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  border: none;
}

.price-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(255, 193, 7, 0.2);
  border: 1px solid rgba(255, 193, 7, 0.3);
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  margin-bottom: 4px;
}

.price-label {
  font-size: 12px;
  color: #8d6e63;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.price-value {
  font-size: 16px;
  font-weight: 700;
  color: #ff8f00;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.value-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding-top: 4px;
  border-top: 1px solid rgba(255, 193, 7, 0.3);
}

.value-label {
  font-size: 12px;
  color: #8d6e63;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.value-amount {
  font-size: 18px;
  font-weight: 700;
  color: #f57c00;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.status-container {
  display: flex;
  justify-content: center;
  align-items: center;
}

.status-tag {
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.summary-info {
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  padding: 25px;
  border-radius: 16px;
  margin-top: 25px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.filter-section {
  background: linear-gradient(135deg, #fafbfc 0%, #f1f3f4 100%);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.filter-section .el-divider {
  margin: 0 0 15px 0;
}

.filter-section .form-item label {
  color: #495057;
  font-size: 14px;
  font-weight: 600;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 2px solid #ebeef5;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  padding: 15px 20px;
}

.table-info {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #495057;
  font-weight: 600;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.table-actions .el-button {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.table-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.header-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.selected-info {
  background: linear-gradient(135deg, #e1f3d8 0%, #c8e6c9 100%);
  color: #2e7d32;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
  border: 1px solid rgba(76, 175, 80, 0.2);
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 25px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.pagination :deep(.el-pagination) {
  gap: 8px;
}

.pagination :deep(.el-pagination__total) {
  font-weight: 600;
  color: #495057;
  font-size: 14px;
}

.pagination :deep(.el-pagination__sizes) {
  margin-right: 16px;
}

.pagination :deep(.el-pagination__sizes .el-select .el-input) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination :deep(.el-pager li:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.pagination :deep(.el-pagination__jump) {
  font-weight: 600;
  color: #495057;
}

.pagination :deep(.el-pagination__jump .el-input) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination :deep(.el-pagination__jump .el-input:focus-within) {
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .stock-management {
    padding: 10px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
    text-align: center;
  }
  
  .page-header h2 {
    font-size: 20px;
    margin-bottom: 10px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }
  
  .header-actions .el-button {
    padding: 8px 16px;
    font-size: 12px;
    min-width: auto;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 15px;
    margin-bottom: 20px;
  }
  
  .stat-card {
    padding: 15px;
  }
  
  .stat-card h3 {
    font-size: 14px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .search-card :deep(.el-card__body) {
    padding: 15px;
  }
  
  .search-card .el-form {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  
  .search-card .el-form-item {
    margin-bottom: 10px;
    margin-right: 0;
  }
  
  .search-card .el-form-item__content {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .search-card .el-input,
  .search-card .el-select,
  .search-card .el-date-picker {
    width: 100% !important;
  }
  
  .filter-actions {
    flex-direction: column;
    gap: 8px;
  }
  
  .filter-actions .el-button {
    width: 100%;
  }
  
  .table-card :deep(.el-card__body) {
    padding: 10px;
  }
  
  .table-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }
  
  .table-title {
    font-size: 16px;
  }
  
  .header-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .table-actions {
    flex-wrap: wrap;
    justify-content: center;
    gap: 6px;
  }
  
  .table-actions .el-button {
    padding: 6px 12px;
    font-size: 11px;
  }
  
  /* 移动端表格优化 */
  .beautiful-table {
    font-size: 12px;
  }
  
  .beautiful-table :deep(.el-table__header) {
    font-size: 11px;
  }
  
  .beautiful-table :deep(.el-table__body) {
    font-size: 11px;
  }
  
  .beautiful-table :deep(.el-table-column--selection) {
    width: 40px !important;
  }
  
  .beautiful-table :deep(.el-table__cell) {
    padding: 8px 4px;
  }
  
  .product-image {
    width: 40px !important;
    height: 40px !important;
  }
  
  .image-placeholder {
    width: 40px;
    height: 40px;
    font-size: 10px;
  }
  
  .product-info {
    padding: 4px 0;
  }
  
  .product-name {
    font-size: 12px;
    margin-bottom: 4px;
  }
  
  .product-spec .el-tag {
    font-size: 9px;
    padding: 2px 4px;
  }
  
  .stock-info {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .stock-item {
    gap: 4px;
  }
  
  .stock-label {
    font-size: 9px;
  }
  
  .stock-value {
    font-size: 11px;
    padding: 1px 4px;
  }
  
  .stock-status {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .status-indicator {
    width: 6px;
    height: 6px;
  }
  
  .status-text {
    font-size: 10px;
  }
  
  .last-updated {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .last-updated .time-icon {
    font-size: 12px;
  }
  
  .last-updated span {
    font-size: 9px;
  }
  
  .pagination-container {
    padding: 15px 10px;
    margin: 15px 0;
  }
  
  .pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 4px;
  }
  
  .pagination :deep(.el-pagination__total),
  .pagination :deep(.el-pagination__jump) {
    order: 3;
    margin-top: 10px;
    font-size: 12px;
  }
  
  .pagination :deep(.el-pagination__sizes) {
    margin-right: 8px;
    margin-bottom: 8px;
  }
  
  .pagination :deep(.el-pager li) {
    width: 32px;
    height: 32px;
    font-size: 12px;
    margin: 0 1px;
  }
  
  .pagination :deep(.btn-prev),
  .pagination :deep(.btn-next) {
    width: 32px;
    height: 32px;
    font-size: 12px;
  }
}

/* 平板端样式 */
@media (max-width: 1024px) and (min-width: 769px) {
  .stock-management {
    padding: 15px;
  }
  
  .page-header {
    padding: 18px;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
  
  .header-actions .el-button {
    padding: 10px 20px;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .beautiful-table {
    font-size: 13px;
  }
  
  .product-image {
    width: 60px !important;
    height: 60px !important;
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .page-header h2 {
    font-size: 18px;
  }
  
  .header-actions {
    width: 100%;
  }
  
  .header-actions .el-button {
    flex: 1;
    min-width: 0;
    padding: 6px 8px;
    font-size: 10px;
  }
  
  .stat-card {
    padding: 12px;
  }
  
  .stat-card h3 {
    font-size: 12px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .beautiful-table :deep(.el-table__cell) {
    padding: 6px 2px;
  }
  
  .product-image {
    width: 30px !important;
    height: 30px !important;
  }
  
  .table-actions .el-button {
    padding: 4px 8px;
    font-size: 10px;
  }
}
</style>