import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8084/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 如果是blob响应（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const data = response.data
    if (data.code === 200) {
      // 为了保持前端逻辑一致，添加success字段
      return { ...data, success: true }
    } else {
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
  },
  error => {
    let message = '网络错误'
    if (error.response) {
      message = error.response.data?.message || `请求失败 ${error.response.status}`
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// API接口定义
export const adminAPI = {
  // 管理员登录
  login: (data: { adminName: string; password: string }) => {
    return api.post('/admin/login', data)
  },
  
  // 检查管理员是否存在
  checkExists: (adminName: string) => {
    return api.get(`/admin/check/${adminName}`)
  },
  
  // 获取管理员信息
  getAdmin: (adminName: string) => {
    return api.get(`/admin/${adminName}`)
  }
}

// 商品相关API接口
export const productAPI = {
  // 分页查询商品列表
  getProducts: (params: {
    name?: string;
    spec?: string;
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }) => {
    return api.get('/products', { params })
  },
  
  // 获取所有商品（不分页）
  getAllProducts: () => {
    return api.get('/products/all')
  },
  
  // 根据ID获取商品详情
  getProductById: (id: number) => {
    return api.get(`/product/${id}`)
  },
  
  // 添加或更新商品
  saveProduct: (data: {
    id?: number;
    name: string;
    spec?: string;
    unit: string;
    initialStock: number;
    price: number;
    amount: number;
    remainingQuantity: number;
    imageUrl?: string;
  }) => {
    return api.post('/product', data)
  },
  
  // 删除商品
  deleteProduct: (id: number) => {
    return api.delete(`/product/${id}`)
  },
  
  // 获取库存不足的商品
  getLowStockProducts: (threshold: number = 10) => {
    return api.get('/products/low-stock', { params: { threshold } })
  },
  
  // 批量导入商品
  importProducts: (formData: FormData) => {
    return api.post('/products/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 出库记录相关API接口
export const outboundRecordAPI = {
  // 分页查询出库记录
  getOutboundRecords: (params: {
    productId?: number;
    productName?: string;
    name?: string;
    paymentStatus?: number;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }) => {
    return api.get('/outbounds', { params })
  },
  
  // 创建出库记录
  createOutboundRecord: (data: {
    productId: number;
    quantity: number;
    outDate?: string;
  }) => {
    return api.post('/outbound', data)
  },
  
  // 更新出库记录
  updateOutboundRecord: (id: number, data: {
    productId: number;
    quantity: number;
    outDate?: string;
  }) => {
    return api.put(`/outbound/${id}`, data)
  },
  
  // 根据ID获取出库记录
  getOutboundRecordById: (id: number) => {
    return api.get(`/outbound/${id}`)
  },
  
  // 删除出库记录
  deleteOutboundRecord: (id: number) => {
    return api.delete(`/outbound/${id}`)
  },
  
  // 获取出库趋势数据
  getOutboundTrendData: (days: number = 30) => {
    return api.get('/outbounds/trend', { params: { days } })
  },
  
  // 统计出库总量
  getTotalOutboundQuantity: (startDate: string, endDate: string) => {
    return api.get('/outbounds/total', { params: { startDate, endDate } })
  },
  
  // 导出出库记录
  exportOutboundRecords: (startDate: string, endDate: string) => {
    return api.get('/outbounds/export', { params: { startDate, endDate } })
  }
}

// 入库记录相关API接口
export const inboundRecordAPI = {
  // 分页查询入库记录
  getInboundRecords: (params: {
    productId?: number;
    productName?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sort?: string;
    direction?: string;
  }) => {
    return api.get('/inbounds', { params })
  },
  
  // 创建入库记录
  createInboundRecord: (data: {
    productId: number;
    quantity: number;
    inDate?: string;
  }) => {
    return api.post('/inbound', data)
  },
  
  // 更新入库记录
  updateInboundRecord: (id: number, data: {
    productId: number;
    quantity: number;
    inDate?: string;
  }) => {
    return api.put(`/inbound/${id}`, data)
  },
  
  // 根据ID获取入库记录
  getInboundRecordById: (id: number) => {
    return api.get(`/inbound/${id}`)
  },
  
  // 删除入库记录
  deleteInboundRecord: (id: number) => {
    return api.delete(`/inbound/${id}`)
  },
  
  // 获取入库趋势数据
  getInboundTrendData: (days: number = 30) => {
    return api.get('/inbounds/trend', { params: { days } })
  },
  
  // 统计入库总量
  getTotalInboundQuantity: (startDate: string, endDate: string) => {
    return api.get('/inbounds/total', { params: { startDate, endDate } })
  },
  
  // 导出入库记录
  exportInboundRecords: (startDate: string, endDate: string) => {
    return api.get('/inbounds/export', { params: { startDate, endDate } })
  }
}

// 库存相关API接口
export const stockAPI = {
  // 获取可查询的日期范围
  getDateRange: () => {
    return api.get('/stock/date-range')
  },
  
  // 查询指定日期的历史库存
  getHistoryStock: (date: string) => {
    return api.get('/stock/history', { params: { date } })
  },
  
  // 查询指定商品在指定日期的历史库存
  getProductHistoryStock: (productId: number, date: string) => {
    return api.get(`/stock/history/${productId}`, { params: { date } })
  },
  
  // 导出指定日期的库存Excel
  exportStock: (date: string) => {
    return api.get('/stock/export', { 
      params: { date },
      responseType: 'blob'  // 下载文件需要blob类型
    })
  }
}

export default api
