/**
 * 业务枚举 → 文案 / 标签类型 的统一映射
 * 列表页与仪表盘共用，避免各处重复定义导致口径不一致。
 */

export type TagType = 'success' | 'warning' | 'danger' | 'info' | 'primary' | ''

export interface EnumMeta {
  /** 中文文案 */
  label: string
  /** Element Plus el-tag 类型 */
  tag: TagType
}

export type EnumMap = Record<string, EnumMeta>

/** 设备状态 */
export const DEVICE_STATUS: EnumMap = {
  NORMAL: { label: '正常', tag: 'success' },
  WARN: { label: '预警', tag: 'warning' },
  FAULT: { label: '故障', tag: 'danger' },
}

/** 预警级别（口径以「预警处理」页为准：低=info） */
export const WARN_LEVEL: EnumMap = {
  LOW: { label: '低', tag: 'info' },
  MEDIUM: { label: '中', tag: 'warning' },
  HIGH: { label: '高', tag: 'danger' },
  URGENT: { label: '紧急', tag: 'danger' },
}

/** 预警处理状态 */
export const HANDLE_STATUS: EnumMap = {
  UNHANDLED: { label: '未处理', tag: 'danger' },
  PROCESSED: { label: '已处理', tag: 'success' },
  IGNORED: { label: '已忽略', tag: 'info' },
}

/** 检修计划状态 */
export const PLAN_STATUS: EnumMap = {
  PENDING: { label: '待执行', tag: 'warning' },
  COMPLETED: { label: '已完成', tag: 'success' },
}

/** 检修结果 */
export const INSPECT_RESULT: EnumMap = {
  PASS: { label: '合格', tag: 'success' },
  PARTIAL: { label: '部分合格', tag: 'warning' },
  FAIL: { label: '不合格', tag: 'danger' },
}

/** 用户账号状态（数值键） */
export const USER_STATUS: EnumMap = {
  1: { label: '正常', tag: 'success' },
  0: { label: '停用', tag: 'danger' },
}

/** 取文案，缺省返回占位符 */
export function enumLabel(map: EnumMap, key: string | number | undefined | null, fallback = '—'): string {
  if (key == null) return fallback
  return map[String(key)]?.label ?? fallback
}

/** 取标签类型，缺省返回 info */
export function enumTag(map: EnumMap, key: string | number | undefined | null): TagType {
  if (key == null) return 'info'
  return map[String(key)]?.tag ?? 'info'
}
