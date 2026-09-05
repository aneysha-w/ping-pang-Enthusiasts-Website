<template>
  <div class="pp-page">
    <div class="profile-header">
      <el-avatar :size="80" :src="user.avatarUrl">{{ user.nickname?.[0] }}</el-avatar>
      <div class="profile-name">
        <h2>{{ user.nickname || '用户' }}</h2>
        <p>{{ user.phone }} · {{ levelText(user.skillLevel) }}</p>
      </div>
    </div>

    <div class="stat-cards">
      <div class="pp-card stat-card">
        <div class="pp-stat"><div class="pp-stat-value">{{ user.currentScore || 0 }}</div><div class="pp-stat-label">当前积分</div></div>
      </div>
      <div class="pp-card stat-card">
        <div class="pp-stat"><div class="pp-stat-value">{{ user.eventScore || 0 }}</div><div class="pp-stat-label">赛事积分</div></div>
      </div>
      <div class="pp-card stat-card">
        <div class="pp-stat"><div class="pp-stat-value">{{ user.friendlyScore || 0 }}</div><div class="pp-stat-label">友谊赛积分</div></div>
      </div>
      <div class="pp-card stat-card">
        <div class="pp-stat"><div class="pp-stat-value">{{ user.matchCount || 0 }}</div><div class="pp-stat-label">参赛次数</div></div>
      </div>
    </div>

    <div class="pp-card" style="margin-bottom: 20px;">
      <div class="pp-card-header"><h3>📝 个人资料</h3></div>
      <div class="pp-card-body">
        <el-form :model="form" label-position="top">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="性别">
                <el-radio-group v-model="form.gender">
                  <el-radio label="MALE">男</el-radio>
                  <el-radio label="FEMALE">女</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="城市"><el-input v-model="form.city" /></el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="技术水平">
                <el-select v-model="form.skillLevel" placeholder="请选择" style="width: 100%;">
                  <el-option label="业余初级" value="BEGINNER" />
                  <el-option label="业余中级" value="INTERMEDIATE" />
                  <el-option label="业余高级" value="ADVANCED" />
                  <el-option label="专业级" value="PROFESSIONAL" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-button type="primary" @click="handleSave">保存资料</el-button>
        </el-form>
      </div>
    </div>

    <div class="pp-card">
      <div class="pp-card-header"><h3>📊 积分变动历史</h3></div>
      <div class="pp-card-body">
        <div v-if="history.length" class="history-list">
          <div v-for="h in history" :key="h.id" class="history-item">
            <span class="history-change" :class="{positive: h.scoreChange > 0, negative: h.scoreChange < 0}">
              {{ h.scoreChange > 0 ? '+' : '' }}{{ h.scoreChange }}
            </span>
            <span class="history-range">{{ h.scoreBefore }} → {{ h.scoreAfter }}</span>
            <span class="history-type">{{ h.changeType === 'EVENT' ? '赛事' : '友谊赛' }}</span>
            <span class="history-time">{{ formatDate(h.createTime) }}</span>
          </div>
        </div>
        <div v-else class="pp-empty">暂无积分变动记录</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const user = ref({})
const history = ref([])
const form = ref({ nickname: '', realName: '', gender: '', city: '', skillLevel: '', avatarUrl: '' })

const levelText = (l) => ({ BEGINNER: '业余初级', INTERMEDIATE: '业余中级', ADVANCED: '业余高级', PROFESSIONAL: '专业级' }[l] || '未设置')
const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN') : ''

const loadUser = async () => {
  try {
    const res = await api.get('/users/me')
    if (res.code === 0) {
      user.value = res.data
      form.value = { nickname: res.data.nickname || '', realName: res.data.realName || '', gender: res.data.gender || '', city: res.data.city || '', skillLevel: res.data.skillLevel || '', avatarUrl: res.data.avatarUrl || '' }
      const histRes = await api.get(`/scores/${res.data.id}/history`)
      if (histRes.code === 0) history.value = histRes.data
    }
  } catch (e) {}
}
const handleSave = async () => {
  try {
    const res = await api.put(`/users/${user.value.id}/profile`, form.value)
    if (res.code === 0) { ElMessage.success('保存成功'); loadUser() }
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error(e.message || '保存失败') }
}
onMounted(loadUser)
</script>

<style scoped>
.profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 24px; }
.profile-name h2 { font-size: 24px; font-weight: 700; color: var(--pp-text); }
.profile-name p { font-size: 14px; color: var(--pp-text-secondary); margin-top: 4px; }
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { padding: 24px; }
.history-list { display: flex; flex-direction: column; gap: 8px; }
.history-item { display: flex; align-items: center; gap: 16px; padding: 12px 16px; background: #f8fafc; border-radius: var(--pp-radius-sm); }
.history-change { font-size: 18px; font-weight: 700; min-width: 60px; }
.history-change.positive { color: var(--pp-success); }
.history-change.negative { color: var(--pp-danger); }
.history-range { font-size: 14px; color: var(--pp-text-secondary); flex: 1; }
.history-type { font-size: 12px; color: var(--pp-primary); background: #eff6ff; padding: 2px 8px; border-radius: 4px; }
.history-time { font-size: 13px; color: var(--pp-text-light); }
</style>
