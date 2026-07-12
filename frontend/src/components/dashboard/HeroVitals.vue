<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  deviceTotal: number
  normalRate: number
  faultCount: number
  pendingWarns: number
}>()

/**
 * 综合运行体征：以故障优先、其次待处理预警，判定整体健康度。
 * 三档对应显示语气与脉搏节奏（越紧张越快）。
 */
const vitals = computed(() => {
  if (props.faultCount > 0) {
    return { level: 'alert', headline: '存在故障设备', pace: '1.15s',
      note: `${props.faultCount} 台设备故障，请尽快检修` }
  }
  if (props.pendingWarns > 0) {
    return { level: 'watch', headline: '运行中 · 待处理预警', pace: '1.7s',
      note: `${props.pendingWarns} 条预警等待处理` }
  }
  return { level: 'steady', headline: '运行平稳', pace: '2.4s',
    note: '全部设备状态正常，无待处理预警' }
})
</script>

<template>
  <section class="hero" :class="'is-' + vitals.level">
    <div class="hero-lead">
      <span class="hero-eyebrow">
        <span class="hero-dot" />系统运行体征
      </span>
      <h2 class="hero-headline">{{ vitals.headline }}</h2>
      <p class="hero-note">{{ vitals.note }}</p>
    </div>

    <!-- 监护仪式脉搏线：品牌盾牌内 ECG 笔触的延伸 -->
    <div class="hero-pulse" :style="{ '--pace': vitals.pace }" aria-hidden="true">
      <svg viewBox="0 0 600 48" preserveAspectRatio="none" class="pulse-svg">
        <path class="pulse-base" pathLength="100"
          d="M0,24 H118 l8,-2 l6,5 l6,-19 l6,31 l7,-16 l9,0 H300 l8,-2 l6,5 l6,-19 l6,31 l7,-16 l9,0 H600" />
        <path class="pulse-glow" pathLength="100"
          d="M0,24 H118 l8,-2 l6,5 l6,-19 l6,31 l7,-16 l9,0 H300 l8,-2 l6,5 l6,-19 l6,31 l7,-16 l9,0 H600" />
      </svg>
    </div>

    <div class="hero-total">
      <span class="hero-total-num">{{ deviceTotal }}</span>
      <span class="hero-total-label">在管设备</span>
    </div>
  </section>
</template>

<style scoped>
.hero {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--mg-sp-5);
  padding: var(--mg-sp-5) var(--mg-sp-6);
  margin-bottom: var(--mg-sp-4);
  border: 1px solid var(--mg-border);
  border-radius: var(--mg-radius-lg);
  background:
    radial-gradient(120% 140% at 100% 0%, var(--mg-primary-soft) 0%, transparent 46%),
    var(--mg-surface);
  overflow: hidden;
}

.hero-lead { flex-shrink: 0; z-index: 1; }
.hero-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  font-size: var(--mg-fs-caption);
  font-weight: 600;
  letter-spacing: .12em;
  color: var(--mg-muted);
  text-transform: uppercase;
}
.hero-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--mg-primary);
  box-shadow: 0 0 0 4px var(--mg-primary-soft);
}
.is-watch .hero-dot { background: var(--mg-warning); box-shadow: 0 0 0 4px var(--mg-tone-amber-bg); }
.is-alert .hero-dot { background: var(--mg-danger);  box-shadow: 0 0 0 4px var(--mg-tone-red-bg); }

.hero-headline {
  margin: 6px 0 2px;
  font-size: var(--mg-fs-2xl);
  font-weight: 700;
  color: var(--mg-ink);
  letter-spacing: .5px;
}
.hero-note {
  margin: 0;
  font-size: var(--mg-fs-sm);
  color: var(--mg-muted);
}

/* —— 脉搏线 —— */
.hero-pulse {
  flex: 1;
  min-width: 120px;
  height: 48px;
  align-self: center;
  color: var(--mg-primary);
}
.pulse-svg { width: 100%; height: 100%; display: block; }
.pulse-base {
  fill: none;
  stroke: currentColor;
  stroke-width: 1.6;
  stroke-linecap: round;
  stroke-linejoin: round;
  opacity: .16;
}
.pulse-glow {
  fill: none;
  stroke: currentColor;
  stroke-width: 2.2;
  stroke-linecap: round;
  stroke-linejoin: round;
  /* 短亮段 + 长空白：形成一段掠过的高亮，像监护仪扫描 */
  stroke-dasharray: 16 84;
  filter: drop-shadow(0 0 3px currentColor);
  animation: pulse-sweep var(--pace, 2.4s) linear infinite;
}
@keyframes pulse-sweep {
  from { stroke-dashoffset: 100; }
  to   { stroke-dashoffset: 0; }
}

.hero-total {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding-left: var(--mg-sp-5);
  border-left: 1px solid var(--mg-border);
  z-index: 1;
}
.hero-total-num {
  font-size: var(--mg-fs-display);
  font-weight: 700;
  line-height: 1;
  color: var(--mg-primary-strong);
  font-variant-numeric: tabular-nums;
}
.hero-total-label {
  margin-top: 3px;
  font-size: var(--mg-fs-caption);
  color: var(--mg-muted);
}

@media (max-width: 720px) {
  .hero-pulse { display: none; }
  .hero { gap: var(--mg-sp-4); }
}

@media (prefers-reduced-motion: reduce) {
  .pulse-glow { animation: none; opacity: .5; stroke-dasharray: none; }
}
</style>
