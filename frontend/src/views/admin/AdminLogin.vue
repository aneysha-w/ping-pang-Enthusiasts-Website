<template>
  <div class="admin-login-page">
    <div class="admin-login-card">
      <div class="admin-logo">🔧</div>
      <h2>管理后台</h2>
      <p class="subtitle">乒乓球交流平台管理系统</p>
      <el-form :model="form" label-position="top" size="large">
        <el-form-item label="管理员账号">
          <el-input v-model="form.account" placeholder="请输入账号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%; height: 48px; font-size: 16px;">登录</el-button>
      </el-form>
      <div class="admin-tip">
        <span>默认账号: admin / admin123</span>
        <span @click="$router.push('/login')" class="back-link">← 返回前台</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../../utils/api'

const router = useRouter()
const loading = ref(false)
const form = ref({ account: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    const res = await api.post('/admin/login', form.value)
    if (res.code === 0) {
      localStorage.setItem('adminToken', res.data.token)
      ElMessage.success('登录成功')
      router.push('/admin')
    } else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '登录失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
}
.admin-login-card {
  background: #fff; padding: 48px 40px; border-radius: var(--pp-radius);
  width: 400px; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.admin-logo { font-size: 48px; text-align: center; margin-bottom: 16px; }
.admin-login-card h2 { text-align: center; font-size: 24px; font-weight: 700; color: var(--pp-text); }
.subtitle { text-align: center; font-size: 14px; color: var(--pp-text-secondary); margin-bottom: 32px; }
.admin-tip { display: flex; justify-content: space-between; margin-top: 24px; font-size: 13px; color: var(--pp-text-secondary); }
.back-link { color: var(--pp-primary); cursor: pointer; }
.back-link:hover { text-decoration: underline; }
</style>
