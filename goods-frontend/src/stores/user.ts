import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminAPI } from '../api'

export interface Admin {
  id: number
  adminName: string
  lastLogin?: string
}

export const useUserStore = defineStore('user', () => {
  // 状态
  const isLoggedIn = ref(false)
  const currentUser = ref<Admin | null>(null)
  const loading = ref(false)

  // 登录方法
  const login = async (adminName: string, password: string) => {
    loading.value = true
    try {
      const response: any = await adminAPI.login({ adminName, password })
      
      if (response.success) {
        isLoggedIn.value = true
        currentUser.value = response.data
        
        // 保存到localStorage
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('currentUser', JSON.stringify(response.data))
        
        return { success: true, message: response.message }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error: any) {
      return { 
        success: false, 
        message: error.message || '登录失败，请检查网络连接' 
      }
    } finally {
      loading.value = false
    }
  }

  // 登出方法
  const logout = () => {
    isLoggedIn.value = false
    currentUser.value = null
    localStorage.removeItem('isLoggedIn')
    localStorage.removeItem('currentUser')
  }

  // 初始化用户状态（从localStorage恢复）
  const initUser = () => {
    const savedLoginStatus = localStorage.getItem('isLoggedIn')
    const savedUser = localStorage.getItem('currentUser')
    
    if (savedLoginStatus === 'true' && savedUser) {
      try {
        isLoggedIn.value = true
        currentUser.value = JSON.parse(savedUser)
      } catch (error) {
        // 如果解析失败，清除错误数据
        logout()
      }
    }
  }

  return {
    isLoggedIn,
    currentUser,
    loading,
    login,
    logout,
    initUser
  }
}) 