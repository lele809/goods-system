import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface CacheItem {
  data: any
  timestamp: number
  ttl: number
}

export const useCacheStore = defineStore('cache', () => {
  const cache = ref<Map<string, CacheItem>>(new Map())
  
  // 缓存配置
  const DEFAULT_TTL = 5 * 60 * 1000 // 5分钟
  const PRODUCT_OPTIONS_TTL = 10 * 60 * 1000 // 商品选项缓存10分钟
  
  const set = (key: string, data: any, ttl: number = DEFAULT_TTL) => {
    cache.value.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }
  
  const get = (key: string) => {
    const item = cache.value.get(key)
    if (!item) return null
    
    if (Date.now() - item.timestamp > item.ttl) {
      cache.value.delete(key)
      return null
    }
    
    return item.data
  }
  
  const clear = (key?: string) => {
    if (key) {
      cache.value.delete(key)
    } else {
      cache.value.clear()
    }
  }
  
  const has = (key: string) => {
    return get(key) !== null
  }
  
  // 专用的商品选项缓存
  const getProductOptions = () => get('product-options')
  const setProductOptions = (data: any) => set('product-options', data, PRODUCT_OPTIONS_TTL)
  
  return {
    set,
    get,
    clear,
    has,
    getProductOptions,
    setProductOptions
  }
})