<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="auth-brand">
        <span class="brand-icon">🏓</span>
        <h1>乒乓交流平台</h1>
        <p>与附近球友交流，组织比赛，提升技艺</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="auth-form">
        <h2>登录</h2>
        <p class="form-subtitle">欢迎回来，请输入您的账号信息</p>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Phone" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%; height: 48px; font-size: 16px;">登录</el-button>
        </el-form>
        <div class="auth-links">
          <span @click="$router.push('/register')">没有账号？去注册</span>
          <span @click="$router.push('/admin/login')">管理后台登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const router = useRouter()
const loading = ref(false)
const form = ref({ phone: '', password: '' })

const handleLogin = async () => {
  if (!form.value.phone || !form.value.password) return ElMessage.warning('请填写完整信息')
  loading.value = true
  try {
    const res = await api.post('/users/login', form.value)
    if (res.code === 0) {
      localStorage.setItem('token', res.data.token)
      ElMessage.success('登录成功')
      router.push('/')
    } else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '登录失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display: flex; min-height: 100vh; }
.auth-left {
  flex: 1; background: var(--pp-gradient);
  display: flex; align-items: center; justify-content: center;
}
.auth-brand { text-align: center; color: #fff; }
.brand-icon { font-size: 80px; display: block; margin-bottom: 24px; }
.auth-brand h1 { font-size: 32px; font-weight: 700; margin-bottom: 12px; }
.auth-brand p { font-size: 16px; opacity: 0.9; }
.auth-right {
  width: 480px; background: #fff;
  display: flex; align-items: center; justify-content: center;
}
.auth-form { width: 360px; }
.auth-form h2 { font-size: 28px; font-weight: 700; color: var(--pp-text); margin-bottom: 8px; }
.form-subtitle { font-size: 14px; color: var(--pp-text-secondary); margin-bottom: 32px; }
.auth-links { display: flex; justify-content: space-between; margin-top: 24px; }
.auth-links span { font-size: 14px; color: var(--pp-primary); cursor: pointer; }
.auth-links span:hover { text-decoration: underline; }
</style>
