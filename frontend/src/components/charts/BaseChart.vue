<script setup lang="ts">
import { ref, shallowRef, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([
  LineChart, PieChart, BarChart,
  GridComponent, TooltipComponent, LegendComponent,
  CanvasRenderer,
])

const props = withDefaults(defineProps<{
  option: echarts.EChartsCoreOption
  height?: string
}>(), {
  height: '280px',
})

const el = ref<HTMLElement | null>(null)
const chart = shallowRef<echarts.ECharts | null>(null)
let ro: ResizeObserver | null = null

function render() {
  if (!chart.value) return
  chart.value.setOption(props.option, true)
}

onMounted(async () => {
  await nextTick()
  if (!el.value) return
  chart.value = echarts.init(el.value)
  render()
  ro = new ResizeObserver(() => chart.value?.resize())
  ro.observe(el.value)
})

watch(() => props.option, render, { deep: true })

onBeforeUnmount(() => {
  ro?.disconnect()
  chart.value?.dispose()
  chart.value = null
})
</script>

<template>
  <div ref="el" class="base-chart" :style="{ height }" />
</template>

<style scoped>
.base-chart {
  width: 100%;
}
</style>
