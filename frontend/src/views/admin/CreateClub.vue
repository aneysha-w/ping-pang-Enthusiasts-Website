<template>
  <div class="pp-card">
    <div class="pp-card-header"><h3>👥 创建俱乐部</h3></div>
    <div class="pp-card-body">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="俱乐部名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入俱乐部名称（2-30个字符）" />
        </el-form-item>
        <el-form-item label="所在城市" prop="city">
          <el-select v-model="form.city" placeholder="请选择城市" style="width: 100%;">
            <el-option label="北京" value="北京" />
            <el-option label="上海" value="上海" />
            <el-option label="广州" value="广州" />
            <el-option label="深圳" value="深圳" />
            <el-option label="成都" value="成都" />
            <el-option label="杭州" value="杭州" />
          </el-select>
        </el-form-item>
        <el-form-item label="俱乐部简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入俱乐部简介（选填）" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large" round>创建俱乐部</el-button>
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
const form = ref({ name: '', city: '', description: '' })

const rules = {
  name: [
    { required: true, message: '请输入俱乐部名称', trigger: 'blur' },
    { min: 2, max: 30, message: '名称长度2-30个字符', trigger: 'blur' },
  ],
  city: [{ required: true, message: '请选择城市', trigger: 'change' }],
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch { return }

  submitting.value = true
  try {
    const res = await api.post('/admin/clubs', form.value)
    if (res.code === 0) {
      ElMessage.success('俱乐部创建成功！')
      router.push('/admin/clubs')
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