<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { handleWarn, type WarnInfo } from '@/api/warn/handle'
import { useUserStore } from '@/stores/users'
import { WARN_LEVEL, enumTag } from '@/constants/enums'

const props = defineProps<{
  /** 弹窗显隐（v-model） */
  modelValue: boolean
  /** 当前处理的预警 */
  warn: WarnInfo | null
  /** 仅查看（已处理/已忽略的记录） */
  viewOnly?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  /** 处理提交成功后通知父级刷新数据 */
  handled: []
}>()

const userStore = useUserStore()
const submitLoading = ref(false)

const form = reactive({
  handleStatus: 'PROCESSED' as 'PROCESSED' | 'IGNORED',
  handleUser: '',
  handleRemark: '',
})

/** el-alert 的 type 不接受 danger，需映射为 error */
const alertType = (level?: string) => {
  const t = enumTag(WARN_LEVEL, level || 'LOW')
  return t === 'danger' ? 'error' : (t as 'success' | 'warning' | 'info')
}

// 打开弹窗时依据 warn 初始化表单
watch(
  () => props.modelValue,
  (open) => {
    if (!open || !props.warn) return
    if (props.viewOnly) {
      form.handleStatus = (props.warn.handleStatus as 'PROCESSED' | 'IGNORED') || 'PROCESSED'
      form.handleUser = props.warn.handleUser || ''
      form.handleRemark = props.warn.handleRemark || ''
    } else {
      form.handleStatus = 'PROCESSED'
      // 预设填入当前登录者名称
      form.handleUser = userStore.userInfo?.realName || userStore.userInfo?.username || ''
      form.handleRemark = ''
    }
  },
)

function close() {
  emit('update:modelValue', false)
}

async function submit() {
  if (!props.warn?.id) return
  if (!form.handleUser) {
    ElMessage.warning('请填写处理人')
    return
  }
  if (form.handleStatus === 'PROCESSED' && !form.handleRemark) {
    ElMessage.warning('标记为「已处理」时，请填写处理备注')
    return
  }

  submitLoading.value = true
  try {
    await handleWarn(props.warn.id, {
      handleStatus: form.handleStatus,
      handleUser: form.handleUser,
      handleRemark: form.handleRemark,
    })
    ElMessage.success('处理提交成功')
    emit('update:modelValue', false)
    emit('handled')
  } catch (error) {
    console.error('处理失败:', error)
  } finally {
    submitLoading.value = false
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="close"
    :title="viewOnly ? '预警处理详情' : '处理预警'"
    width="500px"
    destroy-on-close
  >
    <el-form :model="form" label-width="90px">
      <el-form-item label="预警内容">
        <el-alert :title="warn?.warnContent" :type="alertType(warn?.warnLevel)" :closable="false" />
      </el-form-item>
      <el-form-item label="预警时间">
        <span>{{ warn?.warnTime }}</span>
      </el-form-item>

      <el-divider border-style="dashed" />

      <el-form-item label="处理结果" required>
        <el-radio-group v-model="form.handleStatus" :disabled="viewOnly">
          <el-radio value="PROCESSED">已处理</el-radio>
          <el-radio value="IGNORED">忽略警报</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="处理人" required>
        <el-input v-model="form.handleUser" :disabled="viewOnly" placeholder="请输入处理人姓名" />
      </el-form-item>
      <el-form-item label="处理备注" :required="form.handleStatus === 'PROCESSED'">
        <el-input
          v-model="form.handleRemark"
          type="textarea"
          :rows="3"
          :disabled="viewOnly"
          placeholder="请填写具体的排查或处理措施"
        />
      </el-form-item>
    </el-form>
    <template #footer v-if="!viewOnly">
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="submit" :loading="submitLoading">提交处理</el-button>
    </template>
  </el-dialog>
</template>
