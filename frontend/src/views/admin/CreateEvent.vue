<template>
  <div class="pp-card">
    <div class="pp-card-header"><h3>🏆 创建赛事</h3></div>
    <div class="pp-card-body">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="赛事名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入赛事名称" />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="赛事级别" prop="level">
            <el-select v-model="form.level" placeholder="请选择" style="width: 100%;">
              <el-option label="友谊赛" value="FRIENDLY" />
              <el-option label="俱乐部赛" value="CLUB" />
              <el-option label="公开赛" value="OPEN" />
              <el-option label="锦标赛" value="CHAMPIONSHIP" />
            </el-select>
          </el-form-item>
          <el-form-item label="比赛赛制" prop="format">
            <el-select v-model="form.format" placeholder="请选择" style="width: 100%;">
              <el-option label="淘汰赛" value="KNOCKOUT" />
              <el-option label="循环赛" value="ROUND_ROBIN" />
            </el-select>
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="人数上限" prop="maxPlayers">
            <el-input-number v-model="form.maxPlayers" :min="4" :max="256" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="比赛地点" prop="location">
            <el-input v-model="form.location" placeholder="请输入比赛地点" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="报名截止时间" prop="enrollDeadline">
            <el-date-picker v-model="form.enrollDeadline" type="datetime" placeholder="选择报名截止时间" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%;" />
          </el-form-item>
        </div>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large" round>创建赛事</el-button>
          <el-button @click="resetForm" size="large" round>重置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../../utils/api'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)
const form = ref({
  name: '', level: '', format: '', maxPlayers: 8,
  location: '', startTime: '', enrollDeadline: ''
})

const rules = {
  name: [{ required: true, message: '请输入赛事名称', trigger: 'blur' }],
  level: [{ required: true, message: '请选择赛事级别', trigger: 'change' }],
  format: [{ required: true, message: '请选择比赛赛制', trigger: 'change' }],
  maxPlayers: [{ required: true, message: '请输入人数上限', trigger: 'blur' }],
  location: [{ required: true, message: '请输入比赛地点', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  enrollDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }

  if (form.value.format === 'KNOCKOUT' && (form.value.maxPlayers & (form.value.maxPlayers - 1)) !== 0) {
    ElMessage.error('淘汰赛人数需为2的幂次方（4/8/16/32...）')
    return
  }
  if (new Date(form.value.enrollDeadline) >= new Date(form.value.startTime)) {
    ElMessage.error('报名截止时间需早于开始时间')
    return
  }

  submitting.value = true
  try {
    const res = await api.post('/admin/events', {
      ...form.value,
      startTime: new Date(form.value.startTime).toISOString(),
      enrollDeadline: new Date(form.value.enrollDeadline).toISOString(),
    })
    if (res.code === 0) {
      ElMessage.success('赛事创建成功！')
      router.push('/admin/events')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error(e.message || '创建失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  formRef.value.resetFields()
}
</script>

<style scoped>
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
@media (max-width: 768px) { .form-row { grid-template-columns: 1fr; } }
</style>