<template>
  <div class="outbound-record">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>出库记录</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加出库记录
        </el-button>
        <el-button 
          type="danger" 
          @click="handleBatchDelete"
          :disabled="selectedRecords.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedRecords.length }})
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品搜索：">
          <el-input
            v-model="searchForm.productName"
            placeholder="输入商品名称搜索"
            clearable
            style="width: 200px"
            @input="onProductSearchInput"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入姓名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="付款状态">
          <el-select
            v-model="searchForm.paymentStatus"
            placeholder="请选择付款状态"
            clearable
            style="width: 150px"
          >
            <el-option label="未付款" :value="0" />
            <el-option label="已付款" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库日期">
          <el-date-picker
            v-model="searchForm.outDate"
            type="date"
            placeholder="请选择出库日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 出库记录列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <el-icon><Van /></el-icon>
            <span>出库记录列表</span>
          </div>
          <div class="table-stats">
            <el-statistic title="总记录数" :value="pagination.total" />
          </div>
        </div>
      </template>
      
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="outboundList"
        stripe
        style="width: 100%"
        :row-class-name="getRowClassName"
        class="beautiful-table"
        @selection-change="handleSelectionChange"
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
        <el-table-column label="商品信息" width="200" align="center">
          <template #default="{ row }">
            <div class="product-info">
              <div class="product-name">{{ row.productName }}</div>
              <div class="product-spec" v-if="row.productSpec">
                <el-tag size="small" type="success" effect="plain">{{ row.productSpec }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="出库数量" width="120" align="center">
          <template #default="{ row }">
            <div class="quantity-info">
              <el-tag size="large" type="primary" effect="dark">{{ row.quantity }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="姓名" width="120" align="center">
          <template #default="{ row }">
            <div class="name-info">
              <el-tag size="small" type="warning" effect="plain">{{ row.name }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="付款状态" width="180" align="center">
          <template #default="{ row }">
            <div class="payment-status">
              <el-tag 
                :type="row.paymentStatus === 1 ? 'success' : 'warning'"
                size="large"
                effect="dark"
                class="status-tag"
              >
                {{ row.paymentStatus === 1 ? '已付款' : '未付款' }}
              </el-tag>
              <el-switch
                :model-value="row.paymentStatus === 1"
                @change="(value) => handlePaymentStatusChange(row, value)"
                :loading="paymentStatusLoading === row.id"
                class="payment-switch"
                style="--el-switch-on-color: #67c23a; --el-switch-off-color: #f56c6c"
              />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="出库日期" width="160" align="center">
          <template #default="{ row }">
            <div class="out-date">
              <el-icon class="date-icon"><Clock /></el-icon>
              <span>{{ row.outDate }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <div class="create-time">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span>{{ formatDateTime(row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                size="small"
                @click="handleEdit(row)"
                :icon="Edit"
                circle
                class="action-btn"
                title="编辑"
              />
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
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
      <div class="pagination-container">
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
    </el-card>

    <!-- 添加/编辑出库记录对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑出库记录' : '添加出库记录'"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="outboundFormRef"
        :model="outboundForm"
        :rules="outboundRules"
        label-width="100px"
      >
        <el-form-item label="商品" prop="productId">
          <el-select
            v-model="outboundForm.productId"
            placeholder="请选择商品"
            style="width: 100%"
            filterable
            remote
            reserve-keyword
            :remote-method="searchProducts"
            :loading="productSearchLoading"
            clearable
          >
            <el-option
              v-for="product in filteredProductOptions"
              :key="product.id"
              :label="`${product.name} (${product.spec || '无规格'})`"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number
            v-model="outboundForm.quantity"
            :min="1"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="outboundForm.name"
            placeholder="请输入姓名"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="付款状态" prop="paymentStatus">
          <el-select
            v-model="outboundForm.paymentStatus"
            placeholder="请选择付款状态"
            style="width: 100%"
          >
            <el-option label="未付款" :value="0" />
            <el-option label="已付款" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="出库日期" prop="outDate">
          <el-date-picker
            v-model="outboundForm.outDate"
            type="date"
            placeholder="请选择出库日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            :disabled-date="(time) => {
              const today = new Date()
              return time > today
            }"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            {{ isEdit ? '更新' : '确定' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Clock, Picture, Van } from '@element-plus/icons-vue'
import { outboundRecordAPI, productAPI } from '../api'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const outboundFormRef = ref<FormInstance>()
const paymentStatusLoading = ref<number | null>(null)
const selectedRecords = ref<any[]>([]) // 选中的出库记录列表
const tableRef = ref() // 表格引用
const productSearchLoading = ref(false) // 商品搜索加载状态
const filteredProductOptions = ref([]) // 筛选后的商品选项

// 搜索表单
const searchForm = reactive({
  productName: '' as string,
  name: '' as string,
  paymentStatus: undefined as number | undefined,
  outDate: '' as string
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 出库记录列表
const outboundList = ref([])

// 商品选项
const productOptions = ref([])
// 初始化时显示所有商品
const initFilteredProducts = () => {
  filteredProductOptions.value = productOptions.value
}

// 出库记录表单
const outboundForm = reactive({
  productId: undefined as number | undefined,
  quantity: 1,
  name: '' as string,
  paymentStatus: 0 as number,
  outDate: new Date().toISOString().split('T')[0] // 默认今天
})

// 表单验证规则
const outboundRules: FormRules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入出库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '出库数量必须大于0', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 1, max: 50, message: '姓名长度在1到50个字符', trigger: 'blur' }
  ],
  paymentStatus: [
    { required: true, message: '请选择付款状态', trigger: 'change' }
  ],
  outDate: [
    { required: true, message: '请选择出库日期', trigger: 'change' }
  ]
}

// 方法
const loadOutboundRecords = async () => {
  loading.value = true
  try {
    const params = {
      productName: searchForm.productName || undefined,
      name: searchForm.name || undefined,
      paymentStatus: searchForm.paymentStatus !== undefined ? searchForm.paymentStatus : undefined,
      startDate: searchForm.outDate || undefined,
      endDate: searchForm.outDate || undefined,
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      sort: 'id',
      direction: 'asc'
    }
    
    console.log('出库记录查询参数:', params)
    
    const response = await outboundRecordAPI.getOutboundRecords(params)
    if ((response as any).success) {
      outboundList.value = (response as any).data.content
      pagination.total = (response as any).data.totalElements
      
      // 如果有查询条件但没有结果，显示提示
      if (searchForm.productName && outboundList.value.length === 0) {
        ElMessage.info(`未找到包含 "${searchForm.productName}" 的出库记录`)
      }
    }
  } catch (error) {
    console.error('加载出库记录失败:', error)
  } finally {
    loading.value = false
  }
}

const loadProductOptions = async () => {
  try {
    const response = await productAPI.getAllProducts()
    if ((response as any).success) {
      productOptions.value = (response as any).data
      // 初始化筛选选项
      initFilteredProducts()
    }
  } catch (error) {
    console.error('加载商品选项失败:', error)
  }
}

// 搜索商品方法（对话框中使用）
const searchProducts = (query: string) => {
  if (query) {
    productSearchLoading.value = true
    // 模拟异步搜索，实际可以调用API
    setTimeout(() => {
      filteredProductOptions.value = productOptions.value.filter((product: any) => {
        const searchText = query.toLowerCase()
        return product.name.toLowerCase().includes(searchText) || 
               (product.spec && product.spec.toLowerCase().includes(searchText))
      })
      productSearchLoading.value = false
    }, 200)
  } else {
    // 如果搜索为空，显示所有商品
    filteredProductOptions.value = productOptions.value
  }
}

// 商品搜索输入处理（主搜索表单中使用）
let searchTimer: number | null = null
const onProductSearchInput = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 延迟300ms搜索，避免频繁请求
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300) as unknown as number
}

const handleSearch = () => {
  pagination.page = 1
  loadOutboundRecords()
}

const handleReset = () => {
  searchForm.productName = ''
  searchForm.name = ''
  searchForm.paymentStatus = undefined
  searchForm.outDate = ''
  pagination.page = 1
  loadOutboundRecords()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1
  loadOutboundRecords()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadOutboundRecords()
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  editingId.value = row.id
  outboundForm.productId = row.productId || row.productDTO?.id
  outboundForm.quantity = row.quantity
  outboundForm.name = row.name || ''
  outboundForm.paymentStatus = row.paymentStatus !== undefined ? row.paymentStatus : 0
  outboundForm.outDate = row.outDate
  dialogVisible.value = true
  console.log('编辑出库记录:', row, '表单数据:', outboundForm)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除这条出库记录吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await outboundRecordAPI.deleteOutboundRecord(row.id)
    if ((response as any).success) {
      ElMessage.success('删除成功')
      loadOutboundRecords()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除出库记录失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!outboundFormRef.value) return
  
  try {
    await outboundFormRef.value.validate()
    
    submitLoading.value = true
    let response
    
    if (isEdit.value && editingId.value) {
      // 编辑模式 - 更新出库记录
      response = await outboundRecordAPI.updateOutboundRecord(editingId.value, outboundForm)
    } else {
      // 新增模式 - 创建出库记录
      response = await outboundRecordAPI.createOutboundRecord(outboundForm)
    }
    
    if ((response as any).success) {
      ElMessage.success((response as any).message || (isEdit.value ? '更新成功' : '添加成功'))
      dialogVisible.value = false
      loadOutboundRecords()
    }
  } catch (error) {
    console.error('保存出库记录失败:', error)
    ElMessage.error('请填写姓名!')
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  if (outboundFormRef.value) {
    outboundFormRef.value.clearValidate()
  }
  isEdit.value = false
  editingId.value = null
  resetForm()
}

const resetForm = () => {
  outboundForm.productId = undefined
  outboundForm.quantity = 1
  outboundForm.name = ''
  outboundForm.paymentStatus = 0
  outboundForm.outDate = new Date().toISOString().split('T')[0]
}

const formatDateTime = (dateString: string) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN')
  } catch (error) {
    return dateString
  }
}

// 表格行样式类名
const getRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedRecords.value = selection
}

// 批量删除处理
const handleBatchDelete = async () => {
  if (selectedRecords.value.length === 0) {
    ElMessage.warning('请先选择要删除的出库记录')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRecords.value.length} 条出库记录吗？此操作不可撤销。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    let successCount = 0
    let failureCount = 0

    // 逐个删除选中的出库记录
    for (const record of selectedRecords.value) {
      try {
        const response = await outboundRecordAPI.deleteOutboundRecord(record.id)
        if ((response as any).success) {
          successCount++
        } else {
          failureCount++
        }
      } catch (error) {
        console.error(`删除出库记录 ID:${record.id} 失败:`, error)
        failureCount++
      }
    }

    // 显示结果消息
    if (successCount > 0) {
      ElMessage.success(`成功删除 ${successCount} 条出库记录${failureCount > 0 ? `，${failureCount} 条失败` : ''}`)
      // 清空选中状态
      selectedRecords.value = []
      if (tableRef.value) {
        tableRef.value.clearSelection()
      }
      // 重新加载数据
      loadOutboundRecords()
    } else {
      ElMessage.error('批量删除失败，请稍后再试')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败，请稍后再试')
    }
  } finally {
    loading.value = false
  }
}

const handlePaymentStatusChange = async (row: any, value: boolean) => {
  try {
    const newStatus = value ? 1 : 0
    await ElMessageBox.confirm(
      `确定要将付款状态改为${newStatus === 1 ? '已付款' : '未付款'}吗？`,
      '修改付款状态',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    paymentStatusLoading.value = row.id
    
    // 构造更新数据，只更新付款状态
    const updateData = {
      productId: row.productId || row.productDTO?.id,
      quantity: row.quantity,
      name: row.name || '',
      paymentStatus: newStatus,
      outDate: row.outDate
    }
    
    const response = await outboundRecordAPI.updateOutboundRecord(row.id, updateData)
    if ((response as any).success) {
      ElMessage.success('付款状态更新成功')
      loadOutboundRecords()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新付款状态失败:', error)
      ElMessage.error('更新付款状态失败')
      // 如果更新失败，刷新数据以恢复开关状态
      loadOutboundRecords()
    }
  } finally {
    paymentStatusLoading.value = null
  }
}

// 生命周期
onMounted(() => {
  loadProductOptions()
  loadOutboundRecords()
})
</script>

<style scoped>
.outbound-record {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px;
  border-radius: 12px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.page-header h2 {
  margin: 0;
  color: white;
  font-size: 28px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  letter-spacing: 1px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.header-actions .el-button {
  border-radius: 25px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.header-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
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

.table-card {
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

.table-stats {
  display: flex;
  align-items: center;
  gap: 20px;
}

.table-stats .el-statistic {
  color: white;
}

.table-stats :deep(.el-statistic__head) {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.table-stats :deep(.el-statistic__content) {
  color: white;
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
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

.quantity-info {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-info .el-tag {
  font-size: 16px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
  border: none;
}

.name-info {
  display: flex;
  justify-content: center;
  align-items: center;
}

.name-info .el-tag {
  font-size: 13px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: none;
  background-color: #f5f5f5;
  color: #000000;
}

.payment-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.status-tag {
  font-size: 13px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.payment-switch {
  transform: scale(1.2);
}

.out-date {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.2);
}

.out-date .date-icon {
  font-size: 18px;
  color: #2e7d32;
}

.out-date span {
  font-size: 13px;
  color: #1b5e20;
  font-weight: 600;
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
  margin-top: 30px;
  padding: 25px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.pagination {
  display: flex;
  justify-content: center;
}

.pagination :deep(.el-pagination) {
  gap: 8px;
}

.pagination :deep(.el-pager li) {
  border-radius: 8px;
  margin: 0 2px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.pagination :deep(.el-pager li:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.pagination :deep(.btn-prev),
.pagination :deep(.btn-next) {
  border-radius: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.pagination :deep(.btn-prev:hover),
.pagination :deep(.btn-next:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .outbound-record {
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
  .search-card .el-select {
    width: 100% !important;
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
  
  .outbound-info {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .outbound-item {
    gap: 4px;
  }
  
  .outbound-label {
    font-size: 9px;
  }
  
  .outbound-value {
    font-size: 11px;
    padding: 1px 4px;
  }
  
  .create-time {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .create-time .time-icon {
    font-size: 12px;
  }
  
  .create-time span {
    font-size: 9px;
  }
  
  .action-buttons {
    gap: 6px;
  }
  
  .action-btn {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .pagination-container {
    padding: 15px;
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
  }
  
  /* 对话框移动端优化 */
  .el-dialog {
    width: 95% !important;
    margin: 5vh auto !important;
  }
  
  .el-dialog :deep(.el-dialog__body) {
    padding: 15px;
  }
  
  .dialog-footer {
    flex-direction: column;
    gap: 8px;
  }
  
  .dialog-footer .el-button {
    width: 100%;
  }
}

/* 平板端样式 */
@media (max-width: 1024px) and (min-width: 769px) {
  .outbound-record {
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
  
  .beautiful-table {
    font-size: 13px;
  }
  
  .product-image {
    width: 60px !important;
    height: 60px !important;
  }
  
  .action-btn {
    width: 35px;
    height: 35px;
    font-size: 14px;
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
  
  .beautiful-table :deep(.el-table__cell) {
    padding: 6px 2px;
  }
  
  .product-image {
    width: 30px !important;
    height: 30px !important;
  }
  
  .action-btn {
    width: 24px;
    height: 24px;
    font-size: 10px;
  }
}
</style>