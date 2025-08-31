import axios from 'axios'
import { ElMessage } from 'element-plus'

// 重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000, // 1秒
  retryCondition: (error: any) => {
    // 只对网络错误和超时错误进行重试
    return error.code === 'ECONNABORTED' || 
           error.message.includes('timeout') ||
           (error.response && [502, 503, 504].includes(error.response.status))
  }
}

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8084/api',
  timeout: 30000, // 增加到30秒，处理Render免费版冷启动
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    // 添加重试标记
    config.metadata = { retryCount: 0 }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 重试函数
const retryRequest = (error: any) => {
  const { config } = error
  
  if (!config || !config.metadata) {
    config.metadata = { retryCount: 0 }
  }
  
  config.metadata.retryCount += 1
  
  if (config.metadata.retryCount <= RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
    return new Promise(resolve => {
      setTimeout(() => {
        console.log(`重试请求 ${config.url}，第 ${config.metadata.retryCount} 次`)
        resolve(api(config))
      }, RETRY_CONFIG.retryDelay * config.metadata.retryCount)
    })
  }
  
  return Promise.reject(error)
}

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
  async error => {
    // 先尝试重试
    if (RETRY_CONFIG.retryCondition(error)) {
      try {
        return await retryRequest(error)
      } catch (retryError) {
        error = retryError
      }
    }
    
    // 重试失败或不需要重试，显示错误信息
    let message = '网络错误'
    
    if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
      const retryCount = error.config?.metadata?.retryCount || 0
      if (retryCount > 0) {
        message = `请求超时，已重试 ${retryCount} 次仍然失败。服务可能正在启动中，请稍后再试...`
      } else {
        message = '请求超时，请稍后重试。服务可能正在启动中...'
      }
    } else if (error.response) {
      const status = error.response.status
      switch (status) {
        case 404:
          message = '接口不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
        case 503:
          message = '服务暂时不可用，请稍后重试'
          break
        default:
          message = error.response.data?.message || `请求失败 ${status}`
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络或稍后重试'
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

// 健康检查API
export const healthAPI = {
  // 检查服务健康状态
  checkHealth: () => {
    return api.get('/health', { timeout: 5000 }) // 较短的超时时间
  },
  
  // 预热服务（对于冷启动场景）
  warmup: async () => {
    try {
      console.log('正在预热服务...')
      await api.get('/health', { timeout: 45000 }) // 更长的超时时间给冷启动
      console.log('服务预热完成')
      return true
    } catch (error) {
      console.warn('服务预热失败:', error)
      return false
    }
  }
}

export default api
