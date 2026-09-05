<template>
  <div class="auth-page">
    <div class="auth-left">
      <div class="auth-brand">
        <span class="brand-icon">🏓</span>
        <h1>加入我们</h1>
        <p>注册账号，开始您的乒乓球之旅</p>
      </div>
    </div>
    <div class="auth-right">
      <div class="auth-form">
        <h2>注册</h2>
        <p class="form-subtitle">创建一个新账号</p>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" prefix-icon="Phone" />
          </el-form-item>
          <el-form-item label="验证码">
            <div style="display: flex; gap: 12px; width: 100%">
              <el-input v-model="form.smsCode" placeholder="请输入验证码" />
              <el-button @click="sendCode" :disabled="countdown > 0" style="width: 130px;">
                {{ countdown > 0 ? `${countdown}s后重发` : '发送验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%; height: 48px; font-size: 16px;">注册</el-button>
        </el-form>
        <div class="auth-links">
          <span @click="$router.push('/login')">已有账号？去登录</span>
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
const countdown = ref(0)
const form = ref({ phone: '', smsCode: '', password: '' })

const sendCode = async () => {
  if (!form.value.phone) return ElMessage.warning('请输入手机号')
  try {
    const res = await api.post('/users/sms-code', { phone: form.value.phone })
    if (res.code === 0) {
      ElMessage({ type: 'success', message: `验证码: ${res.data.code}`, duration: 10000 })
      countdown.value = 60
      const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
    } else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '发送失败') }
}

const handleRegister = async () => {
  if (!form.value.phone || !form.value.smsCode || !form.value.password) return ElMessage.warning('请填写完整信息')
  loading.value = true
  try {
    const res = await api.post('/users/register', form.value)
    if (res.code === 0) {
      localStorage.setItem('token', res.data.token)
      ElMessage.success('注册成功')
      router.push('/')
    } else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '注册失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.auth-page { display: flex; min-height: 100vh; }
.auth-left { flex: 1; background: var(--pp-gradient); display: flex; align-items: center; justify-content: center; }
.auth-brand { text-align: center; color: #fff; }
.brand-icon { font-size: 80px; display: block; margin-bottom: 24px; }
.auth-brand h1 { font-size: 32px; font-weight: 700; margin-bottom: 12px; }
.auth-brand p { font-size: 16px; opacity: 0.9; }
.auth-right { width: 480px; background: #fff; display: flex; align-items: center; justify-content: center; }
.auth-form { width: 360px; }
.auth-form h2 { font-size: 28px; font-weight: 700; color: var(--pp-text); margin-bottom: 8px; }
.form-subtitle { font-size: 14px; color: var(--pp-text-secondary); margin-bottom: 32px; }
.auth-links { margin-top: 24px; text-align: center; }
.auth-links span { font-size: 14px; color: var(--pp-primary); cursor: pointer; }
.auth-links span:hover { text-decoration: underline; }
</style>
