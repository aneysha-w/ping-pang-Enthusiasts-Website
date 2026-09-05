<template>
  <div class="pp-page" v-if="event">
    <div class="back-bar">
      <el-button @click="$router.back()" round>← 返回赛事列表</el-button>
    </div>

    <div class="hero" :style="{background: levelGradient(event.level)}">
      <div class="hero-content">
        <div class="hero-badges">
          <span class="hero-badge">{{ levelText(event.level) }}</span>
          <span class="hero-badge">{{ statusText(event.status) }}</span>
          <span class="hero-badge">{{ event.format === 'KNOCKOUT' ? '淘汰赛' : '循环赛' }}</span>
        </div>
        <h1>{{ event.name }}</h1>
        <div class="hero-meta">
          <span>📅 {{ formatDate(event.startTime) }}</span>
          <span>📍 {{ event.location }}</span>
        </div>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <div class="hero-stat-value">{{ event.enrolledCount }}</div>
          <div class="hero-stat-label">已报名</div>
        </div>
        <div class="hero-stat">
          <div class="hero-stat-value">{{ event.maxPlayers }}</div>
          <div class="hero-stat-label">人数上限</div>
        </div>
      </div>
    </div>

    <div class="detail-grid">
      <div class="pp-card info-card">
        <div class="pp-card-header"><h3>📋 赛事信息</h3></div>
        <div class="pp-card-body">
          <div class="info-row">
            <span class="info-label">赛事级别</span>
            <span class="info-value">{{ levelText(event.level) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">比赛赛制</span>
            <span class="info-value">{{ event.format === 'KNOCKOUT' ? '淘汰赛' : '循环赛' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">开始时间</span>
            <span class="info-value">{{ formatDateTime(event.startTime) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">报名截止</span>
            <span class="info-value">{{ formatDateTime(event.enrollDeadline) }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">比赛地点</span>
            <span class="info-value">{{ event.location }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">当前状态</span>
            <span class="info-value">
              <span class="pp-badge" :class="statusBadgeClass(event.status)">{{ statusText(event.status) }}</span>
            </span>
          </div>
        </div>
      </div>

      <div class="pp-card action-card">
        <div class="pp-card-header"><h3>🎯 报名操作</h3></div>
        <div class="pp-card-body">
          <div class="enroll-progress">
            <div class="enroll-progress-bar">
              <div class="enroll-progress-fill" :style="{width: enrollPercent + '%'}"></div>
            </div>
            <span class="enroll-progress-text">{{ event.enrolledCount }} / {{ event.maxPlayers }} 人</span>
          </div>
          <div class="action-buttons">
            <el-button type="primary" @click="handleEnroll" :disabled="event.status !== 'PENDING'" size="large" round>
              报名参赛
            </el-button>
            <el-button @click="handleCancelEnroll" :disabled="event.status !== 'PENDING'" size="large" round>
              取消报名
            </el-button>
          </div>
          <p class="action-hint" v-if="event.status !== 'PENDING'">赛事已开始或已结束，无法报名</p>
        </div>
      </div>
    </div>

    <div class="pp-card matches-card" v-if="matches.length > 0">
      <div class="pp-card-header">
        <h3>🏆 赛程对阵</h3>
        <span class="match-count">共 {{ matches.length }} 场</span>
      </div>
      <div class="pp-card-body">
        <div class="match-list">
          <div v-for="m in matches" :key="m.id" class="match-item">
            <div class="match-round">第{{ m.round }}轮</div>
            <div class="match-players">
              <div class="match-player" :class="{winner: m.winnerId === m.playerAId}">
                {{ m.playerA || '待定' }}
              </div>
              <div class="match-vs">VS</div>
              <div class="match-player" :class="{winner: m.winnerId === m.playerBId}">
                {{ m.playerB || '待定' }}
              </div>
            </div>
            <div class="match-status">
              <span class="pp-badge" :class="matchStatusClass(m.status)">{{ matchStatusText(m.status) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div v-else class="pp-page pp-empty" style="padding: 120px;">加载中...</div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const route = useRoute()
const event = ref(null)
const matches = ref([])

const levelText = (level) => ({ CHAMPIONSHIP: '锦标赛', OPEN: '公开赛', CLUB: '俱乐部赛', FRIENDLY: '友谊赛' }[level] || level)
const statusText = (status) => ({ PENDING: '待开始', IN_PROGRESS: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[status] || status)
const levelGradient = (l) => ({ CHAMPIONSHIP: 'var(--pp-gradient)', OPEN: 'var(--pp-gradient-warm)', CLUB: 'var(--pp-gradient-cool)', FRIENDLY: 'var(--pp-gradient-success)' }[l] || 'var(--pp-gradient)')
const statusBadgeClass = (s) => ({ PENDING: 'pp-badge-primary', IN_PROGRESS: 'pp-badge-warning', FINISHED: 'pp-badge-success', CANCELLED: 'pp-badge-danger' }[s] || 'pp-badge-primary')
const matchStatusText = (s) => ({ PENDING: '未开始', IN_PROGRESS: '进行中', FINISHED: '已结束' }[s] || s)
const matchStatusClass = (s) => ({ PENDING: 'pp-badge-primary', IN_PROGRESS: 'pp-badge-warning', FINISHED: 'pp-badge-success' }[s] || 'pp-badge-primary')

const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatDateTime = (d) => d ? new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const enrollPercent = computed(() => {
  if (!event.value || !event.value.maxPlayers) return 0
  return Math.min(100, (event.value.enrolledCount / event.value.maxPlayers) * 100)
})

const loadData = async () => {
  try {
    const [eventRes, matchesRes] = await Promise.all([
      api.get(`/events/${route.params.id}`),
      api.get(`/events/${route.params.id}/matches`)
    ])
    if (eventRes.code === 0) event.value = eventRes.data
    if (matchesRes.code === 0) matches.value = matchesRes.data
  } catch (e) { console.error(e) }
}

const handleEnroll = async () => {
  try {
    const res = await api.post(`/events/${route.params.id}/enroll`)
    if (res.code === 0) {
      ElMessage.success('报名成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error(e.message || '报名失败') }
}

const handleCancelEnroll = async () => {
  try {
    const res = await api.delete(`/events/${route.params.id}/enroll`)
    if (res.code === 0) {
      ElMessage.success('已取消报名')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error(e.message || '取消失败') }
}

onMounted(loadData)
</script>

<style scoped>
.back-bar { margin-bottom: 20px; }

.hero {
  border-radius: var(--pp-radius);
  padding: 40px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  box-shadow: var(--pp-shadow);
}
.hero-content { flex: 1; }
.hero-badges { display: flex; gap: 8px; margin-bottom: 12px; }
.hero-badge {
  background: rgba(255,255,255,0.25);
  padding: 4px 14px;
  border-radius: 20px;
  font-size: 13px;
  backdrop-filter: blur(4px);
}
.hero h1 { font-size: 32px; font-weight: 700; margin-bottom: 16px; }
.hero-meta { display: flex; gap: 24px; font-size: 15px; opacity: 0.9; }
.hero-stats { display: flex; gap: 32px; }
.hero-stat { text-align: center; }
.hero-stat-value { font-size: 36px; font-weight: 700; }
.hero-stat-label { font-size: 13px; opacity: 0.85; margin-top: 4px; }

.detail-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px; }

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--pp-border);
}
.info-row:last-child { border-bottom: none; }
.info-label { font-size: 14px; color: var(--pp-text-secondary); }
.info-value { font-size: 14px; font-weight: 500; color: var(--pp-text); }

.enroll-progress { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
.enroll-progress-bar { flex: 1; height: 8px; background: #f0f2f5; border-radius: 4px; overflow: hidden; }
.enroll-progress-fill { height: 100%; background: var(--pp-gradient); border-radius: 4px; transition: width 0.3s; }
.enroll-progress-text { font-size: 14px; color: var(--pp-text-secondary); white-space: nowrap; }

.action-buttons { display: flex; flex-direction: column; gap: 12px; }
.action-buttons .el-button { width: 100%; }
.action-hint { font-size: 13px; color: var(--pp-text-light); text-align: center; margin-top: 16px; }

.match-count { font-size: 13px; color: var(--pp-text-secondary); }
.match-list { display: flex; flex-direction: column; gap: 12px; }
.match-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: #f8fafc;
  border-radius: var(--pp-radius-sm);
  transition: all 0.2s;
}
.match-item:hover { background: #f1f5f9; }
.match-round {
  font-size: 13px;
  font-weight: 600;
  color: var(--pp-primary);
  background: #dbeafe;
  padding: 6px 12px;
  border-radius: 8px;
  flex-shrink: 0;
}
.match-players { flex: 1; display: flex; align-items: center; gap: 16px; }
.match-player { font-size: 15px; font-weight: 500; color: var(--pp-text); flex: 1; text-align: center; }
.match-player.winner { color: var(--pp-success); font-weight: 700; }
.match-vs { font-size: 13px; color: var(--pp-text-light); font-weight: 600; }
.match-status { flex-shrink: 0; }

@media (max-width: 768px) {
  .hero { flex-direction: column; text-align: center; gap: 20px; }
  .hero-meta { justify-content: center; }
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
