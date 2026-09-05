<template>
  <div class="pp-card">
    <div class="pp-card-header">
      <h3>📋 赛事列表</h3>
      <el-select v-model="filterStatus" placeholder="全部状态" clearable @change="loadEvents" style="width: 140px;">
        <el-option label="待开始" value="PENDING" />
        <el-option label="进行中" value="IN_PROGRESS" />
        <el-option label="已结束" value="FINISHED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    <div class="pp-card-body" style="padding: 0;">
      <div v-if="events.length" class="event-table">
        <div class="table-header">
          <span class="col-name">赛事名称</span>
          <span class="col-level">级别</span>
          <span class="col-format">赛制</span>
          <span class="col-count">报名</span>
          <span class="col-status">状态</span>
          <span class="col-time">开始时间</span>
          <span class="col-loc">地点</span>
        </div>
        <div v-for="e in events" :key="e.id" class="table-row">
          <span class="col-name">{{ e.name }}</span>
          <span class="col-level">{{ levelText(e.level) }}</span>
          <span class="col-format">{{ e.format === 'KNOCKOUT' ? '淘汰赛' : '循环赛' }}</span>
          <span class="col-count">{{ e.enrolledCount }}/{{ e.maxPlayers }}</span>
          <span class="col-status">
            <span class="pp-badge" :class="statusClass(e.status)">{{ statusText(e.status) }}</span>
          </span>
          <span class="col-time">{{ formatDate(e.startTime) }}</span>
          <span class="col-loc">{{ e.location }}</span>
        </div>
      </div>
      <div v-else class="pp-empty" style="padding: 60px;">暂无赛事</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api'

const events = ref([])
const filterStatus = ref('')

const levelText = (l) => ({ CHAMPIONSHIP: '锦标赛', OPEN: '公开赛', CLUB: '俱乐部赛', FRIENDLY: '友谊赛' }[l] || l)
const statusText = (s) => ({ PENDING: '待开始', IN_PROGRESS: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)
const statusClass = (s) => ({ PENDING: 'pp-badge-primary', IN_PROGRESS: 'pp-badge-warning', FINISHED: 'pp-badge-success', CANCELLED: 'pp-badge-danger' }[s] || 'pp-badge-primary')
const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const loadEvents = async () => {
  try {
    const res = await api.get('/admin/events', { params: { status: filterStatus.value } })
    if (res.code === 0) events.value = res.data
  } catch (e) { ElMessage.error(e.message || '加载失败') }
}
onMounted(loadEvents)
</script>

<style scoped>
.event-table { font-size: 14px; }
.table-header, .table-row {
  display: grid; grid-template-columns: 2fr 1fr 1fr 0.8fr 0.8fr 1.5fr 1fr;
  gap: 8px; padding: 12px 24px; align-items: center;
}
.table-header { background: #f8fafc; font-weight: 600; color: var(--pp-text-secondary); font-size: 13px; border-bottom: 1px solid var(--pp-border); }
.table-row { border-bottom: 1px solid #f5f5f5; transition: background 0.2s; }
.table-row:hover { background: #f8fafc; }
.col-name { font-weight: 500; color: var(--pp-text); }
.col-time { font-size: 13px; color: var(--pp-text-secondary); }
.col-loc { font-size: 13px; color: var(--pp-text-secondary); }
</style>