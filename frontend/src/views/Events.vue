<template>
  <div class="pp-page">
    <div class="page-header">
      <h2>🎯 赛事中心</h2>
      <div class="filters">
        <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="loadEvents" style="width: 120px;">
          <el-option label="待开始" value="PENDING" />
          <el-option label="进行中" value="IN_PROGRESS" />
          <el-option label="已结束" value="FINISHED" />
        </el-select>
        <el-select v-model="filterLevel" placeholder="全部级别" clearable @change="loadEvents" style="width: 120px;">
          <el-option label="锦标赛" value="CHAMPIONSHIP" />
          <el-option label="公开赛" value="OPEN" />
          <el-option label="俱乐部赛" value="CLUB" />
          <el-option label="友谊赛" value="FRIENDLY" />
        </el-select>
      </div>
    </div>
    <div v-if="events.length" class="event-grid">
      <div v-for="e in events" :key="e.id" class="event-card" @click="$router.push(`/events/${e.id}`)">
        <div class="event-card-top" :style="{background: levelGradient(e.level)}">
          <span class="event-badge">{{ levelText(e.level) }}</span>
          <span class="event-badge">{{ statusText(e.status) }}</span>
        </div>
        <div class="event-card-body">
          <h3>{{ e.name }}</h3>
          <div class="event-meta">
            <span>📅 {{ formatDate(e.startTime) }}</span>
            <span>📍 {{ e.location }}</span>
          </div>
          <div class="event-progress">
            <div class="progress-bar">
              <div class="progress-fill" :style="{width: (e.enrolledCount / e.maxPlayers * 100) + '%'}"></div>
            </div>
            <span class="progress-text">{{ e.enrolledCount }}/{{ e.maxPlayers }}人</span>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="pp-empty" style="padding: 80px;">暂无赛事，快去创建一个吧！</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const events = ref([])
const filterStatus = ref('')
const filterLevel = ref('')

const levelText = (l) => ({ CHAMPIONSHIP: '锦标赛', OPEN: '公开赛', CLUB: '俱乐部赛', FRIENDLY: '友谊赛' }[l] || l)
const statusText = (s) => ({ PENDING: '待开始', IN_PROGRESS: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)
const levelGradient = (l) => ({ CHAMPIONSHIP: 'var(--pp-gradient)', OPEN: 'var(--pp-gradient-warm)', CLUB: 'var(--pp-gradient-cool)', FRIENDLY: 'var(--pp-gradient-success)' }[l] || 'var(--pp-gradient)')
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''

const loadEvents = async () => {
  try {
    const res = await api.get('/events', { params: { status: filterStatus.value, level: filterLevel.value } })
    if (res.code === 0) events.value = res.data
  } catch (e) {}
}
onMounted(loadEvents)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 700; color: var(--pp-text); }
.filters { display: flex; gap: 12px; }
.event-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.event-card { background: #fff; border-radius: var(--pp-radius); box-shadow: var(--pp-shadow); cursor: pointer; transition: all 0.3s; overflow: hidden; }
.event-card:hover { box-shadow: var(--pp-shadow-hover); transform: translateY(-4px); }
.event-card-top { padding: 20px; display: flex; justify-content: space-between; align-items: center; }
.event-badge { background: rgba(255,255,255,0.2); color: #fff; padding: 2px 10px; border-radius: 20px; font-size: 12px; }
.event-card-body { padding: 20px; }
.event-card-body h3 { font-size: 18px; font-weight: 600; color: var(--pp-text); margin-bottom: 12px; }
.event-meta { display: flex; flex-direction: column; gap: 6px; font-size: 13px; color: var(--pp-text-secondary); margin-bottom: 16px; }
.event-progress { display: flex; align-items: center; gap: 12px; }
.progress-bar { flex: 1; height: 6px; background: #f0f2f5; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: var(--pp-gradient); border-radius: 3px; transition: width 0.3s; }
.progress-text { font-size: 13px; color: var(--pp-text-secondary); white-space: nowrap; }
</style>
