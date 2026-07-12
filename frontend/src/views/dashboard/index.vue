<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Monitor, Bell, Warning, CircleCheckFilled, Refresh, TrendCharts,
} from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import HeroVitals from '@/components/dashboard/HeroVitals.vue'
import BaseChart from '@/components/charts/BaseChart.vue'
import WarnHandleDialog from '@/components/warn/WarnHandleDialog.vue'
import { getDeviceList, type DeviceInfo } from '@/api/device'
import { getWarnList, type WarnInfo } from '@/api/warn/handle'
import { useThemeStore } from '@/stores/theme'
import { WARN_LEVEL, enumTag, enumLabel } from '@/constants/enums'

const router = useRouter()
const themeStore = useThemeStore()

/* —— 图表配色（随主题切换，light/dark 各一套） —— */
const chartTokens = computed(() => themeStore.resolved === 'dark'
  ? {
      ink: '#EAF1F6', axisLine: '#33424F', axisLabel: '#7E8FA0', split: '#212E3B',
      legend: '#B4C2CE', pieBorder: '#16212E',
      primary: '#2DB6B6', area1: 'rgba(45,182,182,.26)', area2: 'rgba(45,182,182,.02)',
      success: '#2FB56A', warning: '#E7A93E', danger: '#F26468',
      tooltipBg: '#1E2B3A', tooltipBorder: '#33424F', tooltipText: '#EAF1F6',
    }
  : {
      ink: '#1B2A38', axisLine: '#E4EBF0', axisLabel: '#8798A8', split: '#EEF3F6',
      legend: '#46586A', pieBorder: '#FFFFFF',
      primary: '#0FA3A3', area1: 'rgba(15,163,163,.28)', area2: 'rgba(15,163,163,.02)',
      success: '#16A34A', warning: '#E6A23C', danger: '#E5484D',
      tooltipBg: '#FFFFFF', tooltipBorder: '#E4EBF0', tooltipText: '#1B2A38',
    })

/** 主题感知的 tooltip 样式，供各图表复用 */
const themedTooltip = computed(() => ({
  backgroundColor: chartTokens.value.tooltipBg,
  borderColor: chartTokens.value.tooltipBorder,
  textStyle: { color: chartTokens.value.tooltipText },
}))

const loading = ref(false)
const devices = ref<DeviceInfo[]>([])
const warns = ref<WarnInfo[]>([])

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric', month: 'long', day: 'numeric', weekday: 'long',
})

async function loadData() {
  loading.value = true
  const [dRes, wRes] = await Promise.allSettled([
    getDeviceList({ page: 1, pageSize: 1000 }),
    getWarnList({ page: 1, pageSize: 1000 }),
  ])
  devices.value = dRes.status === 'fulfilled'
    ? (dRes.value.records || dRes.value.list || []) : []
  warns.value = wRes.status === 'fulfilled'
    ? (wRes.value.records || wRes.value.list || []) : []
  loading.value = false
}

onMounted(loadData)

/* —— 派生指标 —— */
const deviceTotal = computed(() => devices.value.length)
const statusCount = computed(() => {
  const c = { NORMAL: 0, WARN: 0, FAULT: 0 } as Record<string, number>
  devices.value.forEach(d => { c[d.deviceStatus] = (c[d.deviceStatus] || 0) + 1 })
  return c
})
const pendingWarns = computed(() =>
  warns.value.filter(w => w.handleStatus === 'UNHANDLED').length)
const faultCount = computed(() => statusCount.value.FAULT || 0)
const normalRate = computed(() => {
  if (!deviceTotal.value) return 0
  return Math.round(((statusCount.value.NORMAL || 0) / deviceTotal.value) * 100)
})

const stats = computed(() => [
  { key: 'total', label: '设备总数', value: deviceTotal.value, unit: '台',
    icon: Monitor, tone: 'teal', hint: '纳入管理的全部设备' },
  { key: 'pending', label: '待处理预警', value: pendingWarns.value, unit: '条',
    icon: Bell, tone: 'amber', hint: '等待人工处理' },
  { key: 'fault', label: '故障设备', value: faultCount.value, unit: '台',
    icon: Warning, tone: 'red', hint: '需尽快检修' },
  { key: 'rate', label: '设备正常率', value: normalRate.value, unit: '%',
    icon: CircleCheckFilled, tone: 'green', hint: '正常运行占比' },
])

/* —— 图表配置 —— */
const levelMeta: Record<string, { name: string; color: string }> = {
  LOW: { name: '低', color: '#5FBF8B' },
  MEDIUM: { name: '中', color: '#E6A23C' },
  HIGH: { name: '高', color: '#EF7C4B' },
  URGENT: { name: '紧急', color: '#E5484D' },
}

const trendOption = computed(() => {
  // 近 6 个月预警数量趋势（按 warnTime 月份聚合）
  const now = new Date()
  const buckets: { label: string; key: string }[] = []
  for (let i = 5; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    buckets.push({
      label: `${d.getMonth() + 1}月`,
      key: `${d.getFullYear()}-${d.getMonth() + 1}`,
    })
  }
  const counts = buckets.map(b => warns.value.filter(w => {
    if (!w.warnTime) return false
    const t = new Date(w.warnTime)
    return `${t.getFullYear()}-${t.getMonth() + 1}` === b.key
  }).length)

  const t = chartTokens.value
  return {
    grid: { top: 24, right: 18, bottom: 30, left: 40 },
    tooltip: { trigger: 'axis', ...themedTooltip.value },
    xAxis: {
      type: 'category', data: buckets.map(b => b.label),
      boundaryGap: false,
      axisLine: { lineStyle: { color: t.axisLine } },
      axisLabel: { color: t.axisLabel },
    },
    yAxis: {
      type: 'value', minInterval: 1,
      splitLine: { lineStyle: { color: t.split } },
      axisLabel: { color: t.axisLabel },
    },
    series: [{
      type: 'line', data: counts, smooth: true, symbolSize: 7,
      lineStyle: { width: 3, color: t.primary },
      itemStyle: { color: t.primary, borderColor: t.pieBorder, borderWidth: 2 },
      areaStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: t.area1 },
            { offset: 1, color: t.area2 },
          ],
        },
      },
    }],
  }
})

const statusOption = computed(() => {
  const c = statusCount.value
  const t = chartTokens.value
  const data = [
    { value: c.NORMAL || 0, name: '正常', itemStyle: { color: t.success } },
    { value: c.WARN || 0, name: '预警', itemStyle: { color: t.warning } },
    { value: c.FAULT || 0, name: '故障', itemStyle: { color: t.danger } },
  ]
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} 台 ({d}%)', ...themedTooltip.value },
    legend: { bottom: 0, icon: 'circle', textStyle: { color: t.legend } },
    // 环心显示设备总数
    title: {
      text: String(deviceTotal.value),
      subtext: '设备总数',
      left: 'center', top: '34%',
      textStyle: { fontSize: 30, fontWeight: 700, color: t.ink },
      subtextStyle: { fontSize: 12, color: t.axisLabel },
    },
    series: [{
      type: 'pie', radius: ['52%', '72%'], center: ['50%', '44%'],
      avoidLabelOverlap: false,
      itemStyle: { borderColor: t.pieBorder, borderWidth: 3, borderRadius: 6 },
      label: { show: false },
      data,
    }],
  }
})

const levelOption = computed(() => {
  const order = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
  const counts = order.map(k => warns.value.filter(w => w.warnLevel === k).length)
  const t = chartTokens.value
  return {
    grid: { top: 24, right: 18, bottom: 30, left: 40 },
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, ...themedTooltip.value },
    xAxis: {
      type: 'category', data: order.map(k => levelMeta[k]?.name ?? k),
      axisLine: { lineStyle: { color: t.axisLine } },
      axisLabel: { color: t.axisLabel },
    },
    yAxis: {
      type: 'value', minInterval: 1,
      splitLine: { lineStyle: { color: t.split } },
      axisLabel: { color: t.axisLabel },
    },
    series: [{
      type: 'bar', data: counts.map((v, i) => ({
        value: v,
        itemStyle: { color: levelMeta[order[i] as string]?.color ?? '#0FA3A3', borderRadius: [6, 6, 0, 0] },
      })),
      barWidth: 30,
    }],
  }
})

/* —— 最近预警 —— */
const deviceNameMap = computed(() => {
  const m: Record<number, string> = {}
  devices.value.forEach(d => { if (d.id != null) m[d.id] = d.deviceName })
  return m
})
const recentWarns = computed(() =>
  [...warns.value]
    .sort((a, b) => (b.warnTime || '').localeCompare(a.warnTime || ''))
    .slice(0, 6))

function fmtTime(t?: string) {
  return t ? t.replace('T', ' ').slice(5, 16) : '—'
}

/* —— 一键处理 —— */
const handleDialogVisible = ref(false)
const handlingWarn = ref<WarnInfo | null>(null)
function openHandle(w: WarnInfo) {
  handlingWarn.value = w
  handleDialogVisible.value = true
}
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <PageHeader title="数据概览" :subtitle="today">
      <template #actions>
        <el-button :icon="Refresh" @click="loadData" :loading="loading">刷新</el-button>
      </template>
    </PageHeader>

    <!-- 运行体征（监护仪式脉搏 · 页面签名元素） -->
    <HeroVitals
      class="mg-rise"
      :device-total="deviceTotal"
      :normal-rate="normalRate"
      :fault-count="faultCount"
      :pending-warns="pendingWarns"
    />

    <!-- 指标卡 -->
    <section class="stat-grid">
      <div
        v-for="(s, i) in stats" :key="s.key"
        class="stat-card mg-card mg-hoverable mg-rise"
        :class="'tone-card-' + s.tone"
        :style="{ animationDelay: 70 * i + 'ms' }"
      >
        <span class="stat-icon" :class="'tone-' + s.tone">
          <el-icon><component :is="s.icon" /></el-icon>
        </span>
        <div class="stat-body">
          <span class="stat-label">{{ s.label }}</span>
          <span class="stat-value">
            {{ s.value }}<i>{{ s.unit }}</i>
          </span>
          <span class="stat-hint">{{ s.hint }}</span>
        </div>
      </div>
    </section>

    <!-- 图表区 -->
    <section class="chart-grid">
      <div class="panel mg-card mg-rise" style="animation-delay:.28s">
        <div class="panel-head">
          <h3><el-icon><TrendCharts /></el-icon> 预警趋势</h3>
          <span class="panel-sub">近 6 个月</span>
        </div>
        <BaseChart :option="trendOption" height="270px" />
      </div>

      <div class="panel mg-card mg-rise" style="animation-delay:.34s">
        <div class="panel-head"><h3>设备状态分布</h3></div>
        <BaseChart :option="statusOption" height="270px" />
      </div>

      <div class="panel mg-card mg-rise" style="animation-delay:.4s">
        <div class="panel-head"><h3>预警级别分布</h3></div>
        <BaseChart :option="levelOption" height="270px" />
      </div>

      <div class="panel mg-card mg-rise" style="animation-delay:.46s">
        <div class="panel-head">
          <h3>最近预警</h3>
          <el-button link type="primary" @click="router.push('/warn/handle')">查看全部</el-button>
        </div>
        <div v-if="recentWarns.length" class="recent-list">
          <div v-for="w in recentWarns" :key="w.id" class="recent-item">
            <el-tag :type="enumTag(WARN_LEVEL, w.warnLevel)" size="small" effect="light" round>
              {{ enumLabel(WARN_LEVEL, w.warnLevel) }}
            </el-tag>
            <div class="recent-main">
              <span class="recent-title">{{ w.warnContent }}</span>
              <span class="recent-meta">
                {{ deviceNameMap[w.deviceId] || '设备#' + w.deviceId }} · {{ fmtTime(w.warnTime) }}
              </span>
            </div>
            <el-button
              v-if="w.handleStatus === 'UNHANDLED'"
              class="recent-handle" link type="primary" size="small"
              @click="openHandle(w)"
            >去处理</el-button>
            <span v-else class="recent-status">已处理</span>
          </div>
        </div>
        <el-empty v-else description="暂无预警记录" :image-size="90" />
      </div>
    </section>

    <!-- 一键处理：复用预警处理页的同款弹窗 -->
    <WarnHandleDialog v-model="handleDialogVisible" :warn="handlingWarn" @handled="loadData" />
  </div>
</template>

<style scoped>
.dashboard { max-width: 1320px; }

/* 指标卡 */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--mg-sp-4);
  margin-bottom: var(--mg-sp-4);
}
.stat-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--mg-sp-4);
  padding: var(--mg-sp-5);
  overflow: hidden;
}
/* 顶部语气细线：读作监护读数条 */
.stat-card::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 3px;
  background: var(--mg-accent-line, var(--mg-primary));
  opacity: .85;
}
.stat-card.tone-card-teal  { --mg-accent-line: var(--mg-tone-teal-fg); }
.stat-card.tone-card-amber { --mg-accent-line: var(--mg-tone-amber-fg); }
.stat-card.tone-card-red   { --mg-accent-line: var(--mg-tone-red-fg); }
.stat-card.tone-card-green { --mg-accent-line: var(--mg-tone-green-fg); }
.stat-icon {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  font-size: 25px;
  flex-shrink: 0;
}
.tone-teal  { background: var(--mg-tone-teal-bg);  color: var(--mg-tone-teal-fg); }
.tone-amber { background: var(--mg-tone-amber-bg); color: var(--mg-tone-amber-fg); }
.tone-red   { background: var(--mg-tone-red-bg);   color: var(--mg-tone-red-fg); }
.tone-green { background: var(--mg-tone-green-bg); color: var(--mg-tone-green-fg); }
.stat-body { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.stat-label { font-size: var(--mg-fs-sm); color: var(--mg-muted); }
.stat-value {
  font-size: var(--mg-fs-display);
  font-weight: 700;
  color: var(--mg-ink);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.stat-value i { font-size: var(--mg-fs-body); font-weight: 500; color: var(--mg-muted); margin-left: 3px; font-style: normal; }
.stat-hint { font-size: var(--mg-fs-caption); color: var(--mg-muted); }

/* 图表面板 */
.chart-grid {
  display: grid;
  grid-template-columns: 1.6fr 1fr;
  gap: var(--mg-sp-4);
}
.panel { padding: var(--mg-sp-5) var(--mg-sp-5); }
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--mg-sp-2);
}
.panel-head h3 {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  font-size: var(--mg-fs-md);
  font-weight: 600;
  color: var(--mg-ink);
}
.panel-head h3 .el-icon { color: var(--mg-primary); }
.panel-sub { font-size: var(--mg-fs-caption); color: var(--mg-muted); }

/* 最近预警列表 */
.recent-list { display: flex; flex-direction: column; }
.recent-item {
  display: flex;
  align-items: center;
  gap: var(--mg-sp-3);
  padding: 11px var(--mg-sp-2);
  margin: 0 calc(var(--mg-sp-2) * -1);
  border-radius: var(--mg-radius-sm);
  border-bottom: 1px solid var(--mg-border);
  transition: background-color var(--mg-dur-fast) var(--mg-ease);
}
.recent-item:hover { background: var(--mg-surface-2); }
.recent-item:last-child { border-bottom: none; }
.recent-main { display: flex; flex-direction: column; gap: 2px; min-width: 0; flex: 1; }
.recent-title {
  font-size: var(--mg-fs-sm);
  color: var(--mg-ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-meta { font-size: var(--mg-fs-caption); color: var(--mg-muted); }
.recent-status { font-size: var(--mg-fs-caption); color: var(--mg-muted); flex-shrink: 0; }
.recent-status.is-pending { color: var(--mg-warning); font-weight: 600; }

@media (max-width: 1080px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
