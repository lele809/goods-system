<template>
  <div class="inbound-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <el-icon class="header-icon"><Box /></el-icon>
          <div>
            <h2>入库管理</h2>
            <p>管理商品入库记录，查看入库历史和统计信息</p>
          </div>
        </div>
        <div class="header-stats" v-if="inboundList.length">
          <div class="stat-item">
            <el-icon><DataAnalysis /></el-icon>
            <div>
              <div class="stat-value">{{ inboundList.length }}</div>
              <div class="stat-label">入库记录数</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询和操作表单 -->
    <el-card class="search-card">
      <div class="search-form">
        <!-- 第一行：主要查询条件 -->
        <el-row :gutter="20" align="middle" style="margin-bottom: 15px">
          <el-col :span="6">
            <div class="form-item">
              <label>商品搜索：</label>
              <el-input
                v-model="searchForm.productName"
                placeholder="输入商品名称搜索"
                clearable
                style="width: 100%"
                @input="onProductSearchInput"
                @clear="loadInboundRecords"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="form-item">
              <label>入库日期：</label>
              <el-date-picker
                v-model="searchForm.inDate"
                type="date"
                placeholder="请选择入库日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled-date="disabledDate"
                @change="loadInboundRecords"
              />
            </div>
          </el-col>
          <el-col :span="6">
            <!-- 空列，保持布局 -->
          </el-col>
          <el-col :span="6">
            <div class="form-item">
              <label style="opacity: 0;">操作</label>
              <div class="button-group">
                <el-button type="primary" @click="showAddDialog = true">
                  <el-icon><Plus /></el-icon>
                  新增入库
                </el-button>
                <el-button type="success" plain @click="loadInboundRecords">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 入库记录表格 -->
    <el-card class="result-card">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <el-icon><Box /></el-icon>
            <span>入库记录</span>
          </div>
          <div class="header-info">
            <span v-if="selectedRows.length" class="selected-info">
              已选择：{{ selectedRows.length }} 项
            </span>
          </div>
        </div>
      </template>

      <!-- 表格工具栏 -->
      <div class="table-toolbar" v-if="inboundList.length">
        <div class="table-info">
          <span>共 {{ pagination.total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button 
            type="danger" 
            plain 
            size="small" 
            :disabled="!selectedRows.length"
            @click="batchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button type="success" plain size="small" @click="exportRecords">
            <el-icon><Download /></el-icon>
            导出记录
          </el-button>
        </div>
      </div>

      <!-- 入库记录表格 -->
      <el-table
        ref="tableRef"
        :data="inboundList"
        v-loading="loading"
        class="beautiful-table"
        :row-class-name="getRowClassName"
        @selection-change="handleSelectionChange"
        empty-text="暂无入库记录"
        row-key="id"
        :default-sort="{ prop: 'id', order: 'ascending' }"
      >
        <el-table-column 
          type="selection" 
          width="55" 
          :reserve-selection="true"
        />
        <el-table-column prop="id" label="ID" width="150" align="center" sortable :sort-orders="['ascending', 'descending']">
          <template #default="{ row }">
            <el-tag type="info" size="small" effect="plain" style="color: black; border-color: black;">#{{ row.id }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品图片" width="150" align="center">
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
        
        <el-table-column label="商品信息" width="150" align="center">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-spec" v-if="row.productSpec">
                <el-tag size="small" type="success" effect="plain">{{ row.productSpec }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="productUnit" label="单位" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="dark">{{ row.productUnit }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="入库数量" width="150" align="center">
          <template #default="scope">
            <div class="quantity-info">
              <el-tag 
                type="primary"
                size="large"
                effect="dark"
                class="quantity-tag"
              >
                {{ scope.row.quantity }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="库存信息" width="150" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <div class="stock-item">
                <span class="stock-label">初始:</span>
                <span class="stock-value">{{ getProductStock(row.productId)?.initialStock || 0 }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">当前:</span>
                <span class="stock-value" :class="getStockClass(getProductStock(row.productId)?.remainingQuantity || 0, getProductStock(row.productId)?.initialStock || 0)">
                  {{ getProductStock(row.productId)?.remainingQuantity || 0 }}
                </span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格信息" width="150" align="center">
          <template #default="{ row }">
            <div class="price-info">
              <div class="price-item">
                <span class="price-label">单价:</span>
                <span class="price-value">¥{{ row.productPrice?.toFixed(2) || '0.00' }}</span>
              </div>
              <div class="value-item">
                <span class="value-label">总值:</span>
                <span class="value-amount">¥{{ row.totalAmount?.toFixed(2) || '0.00' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="inDate" label="入库日期" width="150" align="center">
          <template #default="{ row }">
            <div class="create-time">
              <el-icon class="time-icon"><Calendar /></el-icon>
              <span>{{ row.inDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="150" align="center">
          <template #default="scope">
            <div class="create-time">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span>{{ formatDate(scope.row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="editRecord(scope.row)"
                :icon="Edit"
                circle
                class="action-btn"
                title="编辑"
              />
              <el-button
                type="danger"
                size="small"
                @click="deleteRecord(scope.row.id)"
                :icon="Delete"
                circle
                class="action-btn"
                title="删除"
              />
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container" v-if="pagination.total > 0">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          class="pagination"
        />
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && !inboundList.length"
        description="暂无入库记录"
        :image-size="100"
      >
        <template #description>
          <p>暂无入库记录数据</p>
          <p style="color: #999; font-size: 12px; margin-top: 8px">
            点击"新增入库"按钮添加入库记录
          </p>
        </template>
      </el-empty>
    </el-card>

    <!-- 新增/编辑入库记录对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="editingRecord ? '编辑入库记录' : '新增入库记录'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="inboundForm"
        :rules="inboundRules"
        label-width="100px"
      >
        <el-form-item label="商品" prop="productId">
          <el-select
            v-model="inboundForm.productId"
            placeholder="请选择商品"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="product in productOptions"
              :key="product.id"
              :label="`${product.name} (${product.spec || '无规格'})`"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number
            v-model="inboundForm.quantity"
            :min="1"
            :max="99999"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="入库日期" prop="inDate">
          <el-date-picker
            v-model="inboundForm.inDate"
            type="date"
            placeholder="请选择入库日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" @click="saveRecord" :loading="saveLoading">
            {{ editingRecord ? '更新' : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Download, Plus, Delete, Edit, Picture, Box, DataAnalysis, Calendar, Clock } from '@element-plus/icons-vue'
import { inboundRecordAPI, productAPI } from '../api'
import type { FormRules, FormInstance } from 'element-plus'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const showAddDialog = ref(false)
const editingRecord = ref<any>(null)
const selectedRows = ref<any[]>([])
const tableRef = ref()
const formRef = ref<FormInstance>()

// 搜索表单
const searchForm = reactive({
  productName: '' as string,
  inDate: '' as string
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 入库记录列表
const inboundList = ref([])

// 商品选项
const productOptions = ref([])

// 入库记录表单
const inboundForm = reactive({
  productId: undefined as number | undefined,
  quantity: 1,
  inDate: new Date().toISOString().split('T')[0] // 默认今天
})

// 表单验证规则
const inboundRules: FormRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入入库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '入库数量必须大于0', trigger: 'blur' }
  ],
  inDate: [
    { required: true, message: '请选择入库日期', trigger: 'change' }
  ]
}

// 方法
const loadInboundRecords = async () => {
  loading.value = true
  try {
    const params = {
      productName: searchForm.productName || undefined,
      startDate: searchForm.inDate || undefined,
      endDate: searchForm.inDate || undefined,
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      sort: 'id',
      direction: 'asc'
    }
    
    console.log('入库记录查询参数:', params)
    
    const response = await inboundRecordAPI.getInboundRecords(params)
    
    if ((response as any).success) {
      inboundList.value = (response as any).data.content
      pagination.total = (response as any).data.totalElements
      
      // 如果有查询条件但没有结果，显示提示
      if (searchForm.productName && inboundList.value.length === 0) {
        ElMessage.info(`未找到包含 "${searchForm.productName}" 的入库记录`)
      }
      if (searchForm.inDate && inboundList.value.length === 0) {
        ElMessage.info(`${searchForm.inDate} 当天无入库信息`)
      }
    }
  } catch (error) {
    console.error('加载入库记录失败:', error)
    ElMessage.error('加载入库记录失败')
  } finally {
    loading.value = false
  }
}

const loadProductOptions = async () => {
  try {
    const response = await productAPI.getAllProducts()
    if ((response as any).success) {
      productOptions.value = (response as any).data
    }
  } catch (error) {
    console.error('加载商品选项失败:', error)
    ElMessage.error('加载商品选项失败')
  }
}

const editRecord = (record: any) => {
  editingRecord.value = record
  inboundForm.productId = record.productId
  inboundForm.quantity = record.quantity
  inboundForm.inDate = record.inDate
  showAddDialog.value = true
}

const cancelEdit = () => {
  showAddDialog.value = false
  editingRecord.value = null
  resetForm()
}

const resetForm = () => {
  inboundForm.productId = undefined
  inboundForm.quantity = 1
  inboundForm.inDate = new Date().toISOString().split('T')[0]
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const saveRecord = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saveLoading.value = true
    
    const data = {
      productId: inboundForm.productId!,
      quantity: inboundForm.quantity,
      inDate: inboundForm.inDate
    }
    
    if (editingRecord.value) {
      // 更新记录
      await inboundRecordAPI.updateInboundRecord(editingRecord.value.id, data)
      ElMessage.success('入库记录更新成功')
    } else {
      // 新增记录
      await inboundRecordAPI.createInboundRecord(data)
      ElMessage.success('入库记录创建成功')
    }
    
    showAddDialog.value = false
    editingRecord.value = null
    resetForm()
    loadInboundRecords()
  } catch (error) {
    console.error('保存入库记录失败:', error)
    ElMessage.error('保存入库记录失败')
  } finally {
    saveLoading.value = false
  }
}

const deleteRecord = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条入库记录吗？删除后将无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await inboundRecordAPI.deleteInboundRecord(id)
    ElMessage.success('入库记录删除成功')
    loadInboundRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除入库记录失败:', error)
      ElMessage.error('删除入库记录失败')
    }
  }
}

const batchDelete = async () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请先选择要删除的记录')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条入库记录吗？删除后将无法恢复。`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 批量删除
    const deletePromises = selectedRows.value.map(row => 
      inboundRecordAPI.deleteInboundRecord(row.id)
    )
    
    await Promise.all(deletePromises)
    ElMessage.success(`成功删除 ${selectedRows.value.length} 条入库记录`)
    selectedRows.value = []
    loadInboundRecords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const exportRecords = async () => {
  try {
    const exportDate = searchForm.inDate || new Date().toISOString().split('T')[0]
    
    await inboundRecordAPI.exportInboundRecords(exportDate, exportDate)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1
  loadInboundRecords()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadInboundRecords()
}

const getRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch (error) {
    return dateString
  }
}

// 获取商品库存信息
const getProductStock = (productId: number) => {
  return productOptions.value.find((product: any) => product.id === productId)
}

// 库存状态样式类名
const getStockClass = (currentStock: number, initialStock: number) => {
  if (initialStock === 0) return 'high'
  const percentage = (currentStock / initialStock) * 100
  if (percentage <= 20) return 'low'
  if (percentage <= 50) return 'medium'
  return 'high'
}

// 禁用未来日期
const disabledDate = (time: Date) => {
  return time.getTime() > Date.now()
}

// 商品搜索输入处理
let searchTimer: number | null = null
const onProductSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 延迟300ms搜索，避免频繁请求
  searchTimer = setTimeout(() => {
    loadInboundRecords()
  }, 300) as unknown as number
}

// 初始化
onMounted(async () => {
  await loadProductOptions()
  await loadInboundRecords()
})
</script>

<style scoped>
.inbound-management {
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

.button-group {
  display: flex;
  gap: 8px;
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

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 2px solid #ebeef5;
  margin-bottom: 15px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
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
}

.beautiful-table :deep(.el-table__row:hover) {
  background-color: rgba(102, 126, 234, 0.05) !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.beautiful-table :deep(.even-row) {
  background-color: rgba(248, 249, 250, 0.8);
}

.beautiful-table :deep(.odd-row) {
  background-color: rgba(255, 255, 255, 0.9);
}

.product-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
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

.image-placeholder .el-icon {
  font-size: 28px;
  color: #6c757d;
  margin-bottom: 4px;
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

.quantity-info {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-tag {
  font-size: 16px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  border: none;
}

.stock-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  padding: 2px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stock-value.low {
  color: white;
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.4);
}

.stock-value.medium {
  color: white;
  background: linear-gradient(135deg, #e6a23c 0%, #edb563 100%);
  box-shadow: 0 2px 8px rgba(230, 162, 60, 0.4);
}

.stock-value.high {
  color: white;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.4);
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

.create-time {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f3e5f5 0%, #e1bee7 100%);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(156, 39, 176, 0.2);
  border: 1px solid rgba(156, 39, 176, 0.2);
}

.create-time .time-icon {
  font-size: 18px;
  color: #7b1fa2;
}

.create-time span {
  font-size: 13px;
  color: #4a148c;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.action-btn {
  font-size: 16px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.action-btn:hover {
  transform: translateY(-3px) scale(1.1);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

.action-btn.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #66b3ff 100%);
}

.action-btn.el-button--danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 0 0 16px 16px;
}

.pagination :deep(.el-pagination) {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .inbound-management {
    padding: 10px;
  }
  
  .page-header {
    padding: 15px;
    text-align: center;
  }
  
  .page-header h2 {
    font-size: 20px;
    margin-bottom: 10px;
  }
  
  .search-card :deep(.el-card__body) {
    padding: 15px;
  }
  
  .form-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .form-item label {
    min-width: auto;
  }
  
  .button-group {
    width: 100%;
    justify-content: space-between;
  }
  
  .table-toolbar {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .table-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .product-container {
    flex-direction: column;
    text-align: center;
  }
  
  .product-image {
    width: 40px;
    height: 40px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
}
</style>