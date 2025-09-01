// Element Plus 类型定义补充
import type { FormInstance as ELFormInstance, FormRules as ELFormRules } from 'element-plus'

declare global {
  type FormInstance = ELFormInstance
  type FormRules = ELFormRules
}

export type { ELFormInstance as FormInstance, ELFormRules as FormRules }