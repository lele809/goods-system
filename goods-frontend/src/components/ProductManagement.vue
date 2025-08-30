<template>
  <div class="product-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品管理</h2>
      <div class="header-actions">
                  <el-button type="info" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
          <el-button type="success" @click="handleImport">
            <el-icon><Upload /></el-icon>
            导入商品
          </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
        <el-button 
          type="danger" 
          @click="handleBatchDelete"
          :disabled="selectedProducts.length === 0"
        >
          <el-icon><Delete /></el-icon>
          批量删除 ({{ selectedProducts.length }})
        </el-button>
      </div>
    </div>

    <!-- 搜索筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="商品名称">
          <el-input
            v-model="searchForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="规格">
          <el-input
            v-model="searchForm.spec"
            placeholder="请输入规格"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
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

    <!-- 商品列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="table-header">
          <div class="table-title">
            <el-icon><Goods /></el-icon>
            <span>商品列表</span>
          </div>
          <div class="table-stats">
            <el-statistic title="总商品数" :value="pagination.total" />
          </div>
        </div>
      </template>
      
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="productList"
        stripe
        style="width: 100%"
        :row-class-name="getRowClassName"
        class="beautiful-table"
        @selection-change="handleSelectionChange"
        row-key="id"
      >
        <el-table-column 
          type="selection" 
          width="55" 
          :reserve-selection="true"
        />
        <el-table-column prop="id" label="ID" width="150" align="center">
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
              <div class="product-name">{{ row.name }}</div>
              <div class="product-spec" v-if="row.spec">
                <el-tag size="small" type="success" effect="plain">{{ row.spec }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="unit" label="单位" width="150" align="center">
          <template #default="{ row }">
            <el-tag size="small" effect="dark">{{ row.unit }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="库存信息" width="150" align="center">
          <template #default="{ row }">
            <div class="stock-info">
              <div class="stock-item">
                <span class="stock-label">初始:</span>
                <span class="stock-value">{{ row.initialStock }}</span>
              </div>
              <div class="stock-item">
                <span class="stock-label">当前:</span>
                <span class="stock-value" :class="getStockClass(row.currentStock, row.initialStock)">
                  {{ row.currentStock }}
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
                <span class="price-value">¥{{ row.price }}</span>
              </div>
              <div class="amount-item">
                <span class="amount-label">总额:</span>
                <span class="amount-value">¥{{ row.amount }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="剩余数量" width="150" align="center">
          <template #default="{ row }">
            <div class="remaining-quantity">
              <el-progress
                :percentage="getStockPercentage(row.remainingQuantity, row.initialStock)"
                :color="getProgressColor(row.remainingQuantity, row.initialStock)"
                :stroke-width="6"
                class="stock-progress"
              />
              <span class="quantity-text">{{ row.remainingQuantity }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="150" align="center">
          <template #default="{ row }">
            <div class="create-time">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span>{{ formatDate(row.createdAt) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="220" fixed="right" align="center">
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

    <!-- 添加/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="productFormRef"
        :model="productForm"
        :rules="productRules"
        label-width="100px"
      >
        <el-form-item label="商品图片">
          <div class="image-upload-container">
            <!-- 图片预览 -->
            <div v-if="productForm.imageUrl" class="image-preview">
              <el-image
                :src="productForm.imageUrl"
                style="width: 100px; height: 100px; border-radius: 6px;"
                fit="cover"
                :preview-src-list="[productForm.imageUrl]"
                preview-teleported
              />
              <div class="image-actions">
                <el-button
                  type="danger"
                  size="small"
                  @click="removeImage"
                  circle
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            
            <!-- 上传组件 -->
            <el-upload
              v-else
              class="image-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleImageChange"
              accept="image/*"
              drag
            >
              <div class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
                <div class="upload-text">点击或拖拽上传图片</div>
                <div class="upload-hint">支持 jpg、png、gif 格式，文件大小不超过 2MB</div>
              </div>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input
            v-model="productForm.name"
            placeholder="请输入商品名称"
          />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input
            v-model="productForm.spec"
            placeholder="请输入规格（可选）"
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input
            v-model="productForm.unit"
            placeholder="请输入单位"
          />
        </el-form-item>
        <el-form-item label="初始库存" prop="initialStock">
          <el-input-number
            v-model="productForm.initialStock"
            :min="0"
            :precision="0"
            style="width: 100%"
            @change="updateRemainingQuantity"
          />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number
            v-model="productForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
            @change="calculateAmount"
          />
        </el-form-item>
        <el-form-item label="金额/元" prop="amount">
          <el-input-number
            v-model="productForm.amount"
            :min="0"
            :precision="2"
            style="width: 100%"
            :disabled="true"
          />
        </el-form-item>
        <el-form-item label="剩余数量" prop="remainingQuantity">
          <el-input-number
            v-model="productForm.remainingQuantity"
            :min="0"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 导入商品对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="导入商品数据"
      width="600px"
      @close="handleImportDialogClose"
    >
      <div class="import-content">
        <!-- 文件上传 -->
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          :auto-upload="false"
          :accept="'.xlsx,.xls'"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :file-list="fileList"
          :limit="1"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将Excel文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传xlsx/xls文件，且不超过2MB
            </div>
          </template>
        </el-upload>

        <!-- 导入说明 -->
        <el-alert
          title="导入说明"
          type="info"
          :closable="false"
          style="margin-top: 15px;"
        >
          <template #default>
            <div>
              <p>1. Excel文件应包含以下列（顺序可调整）：</p>
              <p>   商品名称、规格、单位、初始库存、剩余数量、单价、金额</p>
              <p>2. 如果金额为空，系统会自动计算（初始库存 × 单价）</p>
              <p>3. 如果存在相同的商品（名称+规格相同），将跳过导入</p>
              <p>4. 第一行应为表头，数据从第二行开始</p>
              <p>5. 规格字段可以为空</p>
            </div>
          </template>
        </el-alert>

        <!-- 预览数据 -->
        <div v-if="previewData.length > 0" class="preview-section">
          <h4>数据预览（全部 {{ previewData.length }} 条）：</h4>
          <el-table :data="previewData" border style="width: 100%; max-height: 400px;" class="preview-table">
            <el-table-column prop="name" label="商品名称" width="120" />
            <el-table-column prop="spec" label="规格" width="100" />
            <el-table-column prop="unit" label="单位" width="80" />
            <el-table-column prop="initialStock" label="初始库存" width="100" />
            <el-table-column prop="remainingQuantity" label="剩余数量" width="100" />
            <el-table-column prop="price" label="单价" width="80" />
            <el-table-column prop="amount" label="金额" width="100" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'duplicate' ? 'warning' : 'success'">
                  {{ row.status === 'duplicate' ? '重复跳过' : '将导入' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="preview-summary">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="总数据" :value="previewData.length" />
              </el-col>
              <el-col :span="8">
                <el-statistic 
                  title="将导入" 
                  :value="previewData.filter(item => item.status === 'valid').length" 
                  :value-style="{ color: '#67c23a' }"
                />
              </el-col>
              <el-col :span="8">
                <el-statistic 
                  title="重复跳过" 
                  :value="previewData.filter(item => item.status === 'duplicate').length"
                  :value-style="{ color: '#e6a23c' }"
                />
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="handleImportSubmit" 
            :loading="importLoading"
            :disabled="previewData.length === 0 || previewData.filter(item => item.status !== 'duplicate').length === 0"
          >
            确认导入
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Upload, UploadFilled, Download, Search, Refresh, Plus, Delete, Goods, Picture, Clock, Edit } from '@element-plus/icons-vue'
import { productAPI } from '../api'
import * as XLSX from 'xlsx'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const productFormRef = ref<FormInstance>()
const selectedProducts = ref<any[]>([]) // 选中的商品列表
const tableRef = ref() // 表格引用

// 搜索表单
const searchForm = reactive({
  name: '',
  spec: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})



// 商品列表
const productList = ref([])

// 商品表单
const productForm = reactive({
  id: undefined as number | undefined,
  name: '',
  spec: '',
  unit: '',
  initialStock: 0,
  price: 0,
  amount: 0,
  remainingQuantity: 0,
  imageUrl: ''
})

// 导入相关
const importDialogVisible = ref(false)
const uploadRef = ref<any>()
const fileList = ref<any[]>([])
const importLoading = ref(false)
const previewData = ref<any[]>([])

// 监听初始库存和单价变化，自动计算金额
const calculateAmount = () => {
  productForm.amount = Number((productForm.initialStock * productForm.price).toFixed(2))
}

// 监听初始库存变化，自动设置剩余数量
const updateRemainingQuantity = () => {
  productForm.remainingQuantity = productForm.initialStock
  calculateAmount()
}

// 表单验证规则
const productRules: FormRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { min: 1, max: 100, message: '商品名称长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' },
    { min: 1, max: 20, message: '单位长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  initialStock: [
    { required: true, message: '请输入初始库存', trigger: 'blur' },
    { type: 'number', min: 0, message: '初始库存不能小于0', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能小于0', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0, message: '金额不能小于0', trigger: 'blur' }
  ],
  remainingQuantity: [
    { required: true, message: '请输入剩余数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '剩余数量不能小于0', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = ref('')

// 方法
const loadProducts = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name || undefined,
      spec: searchForm.spec || undefined,
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size,
      sort: 'id',
      direction: 'asc'
    }
    
    const response = await productAPI.getProducts(params)
    if ((response as any).success) {
      productList.value = (response as any).data.content
      pagination.total = (response as any).data.totalElements
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadProducts()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.spec = ''
  pagination.page = 1
  loadProducts()
}

const handleSizeChange = (val: number) => {
  pagination.size = val
  pagination.page = 1
  loadProducts()
}

const handleCurrentChange = (val: number) => {
  pagination.page = val
  loadProducts()
}



const handleAdd = () => {
  dialogTitle.value = '添加商品'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑商品'
  Object.assign(productForm, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除商品"${row.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await productAPI.deleteProduct(row.id)
    if ((response as any).success) {
      ElMessage.success('删除成功')
      loadProducts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!productFormRef.value) return
  
  try {
    await productFormRef.value.validate()
    
    submitLoading.value = true
    const response = await productAPI.saveProduct(productForm)
    
    if ((response as any).success) {
      ElMessage.success((response as any).message)
      dialogVisible.value = false
      loadProducts()
    }
  } catch (error) {
    console.error('保存商品失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  if (productFormRef.value) {
    productFormRef.value.clearValidate()
  }
  resetForm()
}

const resetForm = () => {
  productForm.id = undefined
  productForm.name = ''
  productForm.spec = ''
  productForm.unit = ''
  productForm.initialStock = 0
  productForm.price = 0
  productForm.amount = 0
  productForm.remainingQuantity = 0
  productForm.imageUrl = ''
}

// 图片相关方法
const handleImageChange = (file: any) => {
  if (!file.raw) return
  
  // 检查文件大小 (2MB限制)
  const maxSize = 2 * 1024 * 1024
  if (file.raw.size > maxSize) {
    ElMessage.error('图片大小不能超过2MB')
    return
  }
  
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.error('只支持jpg、png、gif格式的图片')
    return
  }
  
  // 创建预览URL
  const reader = new FileReader()
  reader.onload = (e) => {
    productForm.imageUrl = e.target?.result as string
    ElMessage.success('图片上传成功')
  }
  reader.readAsDataURL(file.raw)
}

const removeImage = () => {
  productForm.imageUrl = ''
  ElMessage.success('图片已移除')
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

// 导入相关方法
const handleImport = () => {
  importDialogVisible.value = true
  fileList.value = []
  previewData.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const handleImportDialogClose = () => {
  fileList.value = []
  previewData.value = []
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

// 下载Excel模板
const downloadTemplate = () => {
  const templateData = [
    {
      '商品名称': '养生汤料包',
      '规格': '1袋',
      '单位': '袋',
      '初始库存': 9,
      '剩余数量': 9,
      '单价': 80,
      '金额': 720
    },
    {
      '商品名称': '养生汤料包',
      '规格': '2袋',
      '单位': '袋',
      '初始库存': 6,
      '剩余数量': 6,
      '单价': 100,
      '金额': 600
    }
  ]
  
  const ws = XLSX.utils.json_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '商品导入模板')
  
  XLSX.writeFile(wb, '商品导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

const parseExcelFile = async (file: File) => {
  try {
    // 检查文件大小 (2MB限制)
    const maxSize = 2 * 1024 * 1024
    if (file.size > maxSize) {
      ElMessage.error('文件大小不能超过2MB')
      return
    }
    
    // 获取现有商品列表用于重复检测
    const existingProducts = await getExistingProducts()
    
    const fileReader = new FileReader()
    fileReader.onload = (e) => {
      try {
        const data = e.target?.result
        const workbook = XLSX.read(data, { type: 'binary' })
        const sheetName = workbook.SheetNames[0]
        const worksheet = workbook.Sheets[sheetName]
        const jsonData = XLSX.utils.sheet_to_json(worksheet)

        if (jsonData.length === 0) {
          ElMessage.warning('Excel文件中没有数据')
          return
        }
        
        console.log('Excel原始数据:', jsonData)
        
        // 过滤掉无效行（标题行、空行等）
        const validRows = jsonData.filter((row: any, index: number) => {
          // 检查是否是表头行 - 更严格的表头检测
          const keys = Object.keys(row)
          const values = Object.values(row).map(v => String(v || '').trim())
          
          // 如果包含这些值，很可能是表头行
          const headerIndicators = ['初始数量', '单价/元', '金额/元', '剩余数量', '商品名称', '规格', '单位', '序号']
          const isHeaderRow = values.some(value => headerIndicators.includes(value)) ||
                             keys.some(key => key.includes('__EMPTY') && values.includes(''))
          
          if (isHeaderRow) {
            console.log(`跳过第${index + 1}行（表头行）:`, row)
            return false
          }
          
          // 检查是否是有效数据行 - 必须有商品名称
          const productName = row['商品名称'] || row['name'] || row['商品'] || row['产品名称']
          const hasProductName = productName && String(productName).trim() !== '' && 
                                String(productName).trim() !== '商品名称'
          
          if (!hasProductName) {
            console.log(`跳过第${index + 1}行（无商品名称）:`, row)
            return false
          }
          
          return true
        })
        
        console.log('过滤后的有效数据:', validRows)
        
        if (validRows.length === 0) {
          ElMessage.warning('Excel文件中没有有效的商品数据，请检查文件格式')
          return
        }
        
        // 解析和验证数据
        const parsedData = validRows.map((row: any, index: number) => {
          // 智能字段检测
          const findFieldValue = (keywords: string[]) => {
            const keys = Object.keys(row)
            
            // 精确匹配字段名
            for (const keyword of keywords) {
              if (row[keyword] !== undefined && row[keyword] !== null && String(row[keyword]).trim() !== '') {
                console.log(`精确匹配字段 "${keyword}":`, row[keyword])
                return row[keyword]
              }
            }
            
            // 模糊匹配字段名
            for (const keyword of keywords) {
              const matchedKey = keys.find(key => 
                key.toLowerCase().includes(keyword.toLowerCase()) || 
                keyword.toLowerCase().includes(key.toLowerCase())
              )
              if (matchedKey && row[matchedKey] !== undefined && row[matchedKey] !== null && String(row[matchedKey]).trim() !== '') {
                console.log(`模糊匹配字段 "${keyword}" -> "${matchedKey}":`, row[matchedKey])
                return row[matchedKey]
              }
            }
            
            console.log(`未找到字段 ${keywords.join('/')}，可用字段:`, keys)
            return ''
          }
          
          // 安全获取字符串值
          const getName = () => {
            const name = findFieldValue(['商品名称', 'name', '商品', '名称', '产品名称', '产品'])
            return typeof name === 'string' ? name.trim() : String(name || '').trim()
          }
          
          const getSpec = () => {
            const spec = findFieldValue(['规格', 'spec', '包装', '型号', '规格型号'])
            return typeof spec === 'string' ? spec.trim() : String(spec || '').trim()
          }
          
          const getUnit = () => {
            const unit = findFieldValue(['单位', 'unit', '计量单位'])
            return typeof unit === 'string' ? unit.trim() : String(unit || '').trim()
          }
          
          // 安全获取数字值
          const getNumber = (value: any) => {
            if (value === null || value === undefined || value === '') return 0
            const num = Number(value)
            return isNaN(num) ? 0 : Math.max(0, num) // 确保非负数
          }
          
          // 获取初始库存 - 支持更多字段名
          const getInitialStock = () => {
            return getNumber(findFieldValue([
              '初始库存', 'initialStock', '初始数量', '库存', '入库数量', 
              '货架商品数', '商品数', '数量', '总数量', '库存数量'
            ]))
          }
          
          // 获取剩余数量 - 如果没有剩余数量，默认等于初始库存
          const getRemainingQuantity = (initialStock: number) => {
            const remaining = getNumber(findFieldValue([
              '剩余数量', 'remainingQuantity', '剩余库存', '当前库存', 
              '现有库存', '剩余', '余量', '现存数量'
            ]))
            // 如果Excel中没有剩余数量字段，默认等于初始库存
            return remaining > 0 ? remaining : initialStock
          }
          
          const initialStock = getInitialStock()
          const remainingQuantity = getRemainingQuantity(initialStock)
          
          // 处理合并单元格的Excel - 通过位置获取价格和金额
          const getValueByPosition = () => {
            const keys = Object.keys(row)
            const values = Object.values(row)
            
            // 根据Excel结构，价格和金额通常在后面的位置
            // 序号(0), 商品名称(1), 规格(2), 单位(3), 初始数量(4), 单价(5), 金额(6), 剩余数量(7)
            let price = 0
            let amount = 0
            
            // 尝试通过位置获取
            if (values.length >= 6) {
              price = getNumber(values[5]) // 第6列通常是单价
            }
            if (values.length >= 7) {
              amount = getNumber(values[6]) // 第7列通常是金额
            }
            
            // 如果位置获取失败，尝试通过__EMPTY键获取
            if (price === 0) {
              price = getNumber(row['__EMPTY'] || row['__EMPTY_1'] || row['__EMPTY_2'])
            }
            if (amount === 0) {
              amount = getNumber(row['__EMPTY_1'] || row['__EMPTY_2'] || row['__EMPTY_3'])
            }
            
            return { price, amount }
          }
          
          const { price, amount } = getValueByPosition()
          
          const item = {
            name: getName(),
            spec: getSpec(),
            unit: getUnit(),
            initialStock: initialStock,
            remainingQuantity: remainingQuantity,
            price: price,
            amount: amount,
            status: 'valid' as 'valid' | 'duplicate' | 'invalid'
          }
          
          // 显示完整的字段映射信息
          console.log(`=== 第${index + 1}行数据完整解析 ===`)
          console.log('原始数据:', row)
          console.log('所有字段和值:')
          Object.entries(row).forEach(([key, value], idx) => {
            console.log(`  [${idx}] "${key}": ${value}`)
          })
          console.log('位置解析:', {
            '位置5(单价)': Object.values(row)[5],
            '位置6(金额)': Object.values(row)[6],
            '__EMPTY': row['__EMPTY'],
            '__EMPTY_1': row['__EMPTY_1'], 
            '__EMPTY_2': row['__EMPTY_2']
          })
          console.log('解析结果:', item)
          console.log('=========================')

          // 如果金额为空或0，自动计算金额
          if (item.amount === 0) {
            item.amount = Number((item.initialStock * item.price).toFixed(2))
          }

          // 数据验证
          const errors = []
          if (!item.name || item.name.trim() === '') {
            errors.push('商品名称不能为空')
          }
          if (!item.unit || item.unit.trim() === '') {
            errors.push('单位不能为空')
          }
          if (isNaN(item.initialStock) || item.initialStock < 0) {
            errors.push('初始库存必须是非负数字')
          }
          if (isNaN(item.price) || item.price < 0) {
            errors.push('单价必须是非负数字')
          }
          if (isNaN(item.remainingQuantity) || item.remainingQuantity < 0) {
            errors.push('剩余数量必须是非负数字')
          }
          
          if (errors.length > 0) {
             item.status = 'invalid'
             const errorMsg = `第${index + 2}行数据错误: ${errors.join(', ')}`
             ElMessage.warning(errorMsg)
             console.error(`第${index + 2}行数据验证失败:`, {
               错误列表: errors,
               原始数据: row,
               解析结果: item,
               错误消息: errorMsg
             })
             return item
           }

          // 检测重复（名称+规格相同）
          const isDuplicate = existingProducts.some((existing: any) => 
            existing.name === item.name && existing.spec === item.spec
          )
          
          if (isDuplicate) {
            item.status = 'duplicate'
          }

          return item
        })

        // 过滤掉无效数据
        const validData = parsedData.filter(item => item.status !== 'invalid')
        previewData.value = validData

        if (validData.length === 0) {
          ElMessage.warning('没有有效的数据可以导入')
        } else {
          const duplicateCount = validData.filter(item => item.status === 'duplicate').length
          const validCount = validData.filter(item => item.status === 'valid').length
          ElMessage.success(`解析完成：共${validData.length}条数据，${validCount}条可导入，${duplicateCount}条重复`)
        }
      } catch (error) {
        console.error('解析Excel文件失败:', error)
        ElMessage.error('Excel文件解析失败，请检查文件格式')
      }
    }
    fileReader.readAsBinaryString(file)
  } catch (error) {
    console.error('读取文件失败:', error)
    ElMessage.error('文件读取失败')
  }
}

const getExistingProducts = async () => {
  try {
    const response = await productAPI.getAllProducts()
    return (response as any).data || []
  } catch (error) {
    console.error('获取现有商品列表失败:', error)
    return []
  }
}

const handleFileChange = async (file: any, uploadFileList: any) => {
  // Element Plus的onChange回调参数: (file, fileList)
  fileList.value = uploadFileList || []
  
  if (uploadFileList && uploadFileList.length > 0) {
    // 使用最新上传的文件
    const currentFile = file
    if (currentFile && currentFile.raw) {
      await parseExcelFile(currentFile.raw)
    }
  } else {
    previewData.value = []
  }
}

const handleFileRemove = () => {
  previewData.value = []
  fileList.value = []
}

const handleImportSubmit = async () => {
  if (previewData.value.length === 0) {
    ElMessage.warning('没有可导入的数据')
    return
  }

  // 过滤出可以导入的数据（非重复且有效）
  const validImportData = previewData.value.filter(item => item.status === 'valid')
  
  if (validImportData.length === 0) {
    ElMessage.warning('没有可导入的有效数据')
    return
  }

  importLoading.value = true
  try {
    // 批量保存商品
    let successCount = 0
    let failureCount = 0
    
    for (const item of validImportData) {
      try {
        const productData = {
          name: item.name,
          spec: item.spec,
          unit: item.unit,
          initialStock: item.initialStock,
          remainingQuantity: item.remainingQuantity,
          price: item.price,
          amount: item.amount
        }
        await productAPI.saveProduct(productData)
        successCount++
      } catch (error) {
        console.error(`保存商品失败: ${item.name}`, error)
        failureCount++
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(`成功导入 ${successCount} 条商品数据${failureCount > 0 ? `，${failureCount} 条失败` : ''}`)
      importDialogVisible.value = false
      loadProducts()
    } else {
      ElMessage.error('导入失败，请稍后再试')
    }
  } catch (error) {
    console.error('导入商品失败:', error)
    ElMessage.error('导入失败，请稍后再试')
  } finally {
    importLoading.value = false
  }
}

// 表格行样式类名
const getRowClassName = ({ rowIndex }: { rowIndex: number }) => {
  return rowIndex % 2 === 0 ? 'even-row' : 'odd-row'
}

// 处理表格选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedProducts.value = selection
}

// 批量删除处理
const handleBatchDelete = async () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedProducts.value.length} 个商品吗？此操作不可撤销。`,
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

    // 逐个删除选中的商品
    for (const product of selectedProducts.value) {
      try {
        const response = await productAPI.deleteProduct(product.id)
        if ((response as any).success) {
          successCount++
        } else {
          failureCount++
        }
      } catch (error) {
        console.error(`删除商品 ${product.name} 失败:`, error)
        failureCount++
      }
    }

    // 显示结果消息
    if (successCount > 0) {
      ElMessage.success(`成功删除 ${successCount} 个商品${failureCount > 0 ? `，${failureCount} 个失败` : ''}`)
      // 清空选中状态
      selectedProducts.value = []
      if (tableRef.value) {
        tableRef.value.clearSelection()
      }
      // 重新加载数据
      loadProducts()
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

// 库存状态样式类名
const getStockClass = (currentStock: number, initialStock: number) => {
  const percentage = (currentStock / initialStock) * 100
  if (percentage <= 20) return 'low'
  if (percentage <= 50) return 'medium'
  return 'high'
}

// 计算库存百分比
const getStockPercentage = (remainingQuantity: number, initialStock: number) => {
  if (initialStock === 0) return 0
  return Math.round((remainingQuantity / initialStock) * 100)
}

// 获取进度条颜色
const getProgressColor = (remainingQuantity: number, initialStock: number) => {
  const percentage = getStockPercentage(remainingQuantity, initialStock)
  if (percentage <= 20) return '#f56c6c'
  if (percentage <= 50) return '#e6a23c'
  return '#67c23a'
}

// 生命周期
onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-management {
  /* padding已在Dashboard布局中提供 */
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

.import-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.preview-section {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background-color: #f9fafc;
}

.preview-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.preview-table {
  max-height: 400px;
  overflow-y: auto;
}

.preview-table :deep(.el-table__body-wrapper) {
  max-height: 350px;
  overflow-y: auto;
}

.preview-table :deep(.el-table__header-wrapper) {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #fafafa;
}

.preview-summary {
  margin-top: 15px;
  padding: 15px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.preview-summary :deep(.el-statistic) {
  text-align: center;
}

.preview-summary :deep(.el-statistic__head) {
  color: #6c757d;
  font-weight: 600;
  margin-bottom: 8px;
}

.preview-summary :deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 700;
}

/* 图片上传相关样式 */
.image-upload-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-actions {
  position: absolute;
  top: -8px;
  right: -8px;
  z-index: 10;
}

.image-uploader {
  width: 100%;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
}

.image-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.image-uploader :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c939d;
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 8px;
  color: #8c939d;
}

.upload-text {
  font-size: 14px;
  margin-bottom: 4px;
}

.upload-hint {
  font-size: 12px;
  color: #a8abb2;
}

/* 新增的商品列表样式 */
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

.beautiful-table :deep(.el-table) {
  border-spacing: 0 12px;
  border-collapse: separate;
}

.beautiful-table :deep(.el-table__body tr) {
  margin-bottom: 12px;
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

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding-top: 4px;
  border-top: 1px solid rgba(255, 193, 7, 0.3);
}

.amount-label {
  font-size: 12px;
  color: #8d6e63;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.amount-value {
  font-size: 18px;
  font-weight: 700;
  color: #f57c00;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.remaining-quantity {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: linear-gradient(135deg, #e8f5e8 0%, #c8e6c9 100%);
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.3);
}

.quantity-text {
  font-weight: 700;
  font-size: 18px;
  color: #2e7d32;
  text-align: center;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.stock-progress {
  width: 100%;
}

.stock-progress :deep(.el-progress-bar__outer) {
  border-radius: 10px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stock-progress :deep(.el-progress-bar__inner) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
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

/* 移动端响应式样式 */
@media (max-width: 768px) {
  .product-management {
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
  
  .search-card .el-input {
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
  
  .price-info {
    padding: 4px 6px;
    gap: 4px;
  }
  
  .price-item, .amount-item {
    gap: 4px;
  }
  
  .price-label, .amount-label {
    font-size: 9px;
  }
  
  .price-value, .amount-value {
    font-size: 11px;
  }
  
  .remaining-quantity {
    padding: 6px;
    gap: 4px;
  }
  
  .quantity-text {
    font-size: 12px;
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
  
  .image-upload-container {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .image-uploader :deep(.el-upload-dragger) {
    width: 100%;
    height: 100px;
  }
  
  .upload-icon {
    font-size: 20px;
  }
  
  .upload-text {
    font-size: 12px;
  }
  
  .upload-hint {
    font-size: 10px;
  }
}

/* 平板端样式 */
@media (max-width: 1024px) and (min-width: 769px) {
  .product-management {
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