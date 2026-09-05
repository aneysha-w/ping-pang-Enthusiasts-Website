<template>
  <div class="pp-card">
    <div class="pp-card-header"><h3>🏘️ 俱乐部列表</h3></div>
    <div class="pp-card-body" style="padding: 0;">
      <div v-if="clubs.length" class="club-table">
        <div class="table-header">
          <span class="col-name">俱乐部名称</span>
          <span class="col-city">城市</span>
          <span class="col-members">成员数</span>
          <span class="col-status">状态</span>
          <span class="col-time">创建时间</span>
        </div>
        <div v-for="c in clubs" :key="c.id" class="table-row">
          <span class="col-name">
            <span class="club-avatar">{{ c.name?.[0] }}</span>
            {{ c.name }}
          </span>
          <span class="col-city">{{ c.city }}</span>
          <span class="col-members">{{ c.memberCount }}人</span>
          <span class="col-status">
            <span class="pp-badge pp-badge-success">{{ c.status === 'ACTIVE' ? '正常' : c.status }}</span>
          </span>
          <span class="col-time">{{ formatDate(c.createTime) }}</span>
        </div>
      </div>
      <div v-else class="pp-empty" style="padding: 60px;">暂无俱乐部</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../../utils/api'

const clubs = ref([])

const formatDate = (d) => d ? new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }) : ''

const loadClubs = async () => {
  try {
    const res = await api.get('/admin/clubs')
    if (res.code === 0) clubs.value = res.data
  } catch (e) { ElMessage.error(e.message || '加载失败') }
}
onMounted(loadClubs)
</script>

<style scoped>
.club-table { font-size: 14px; }
.table-header, .table-row {
  display: grid; grid-template-columns: 2fr 1fr 1fr 1fr 1.5fr;
  gap: 8px; padding: 12px 24px; align-items: center;
}
.table-header { background: #f8fafc; font-weight: 600; color: var(--pp-text-secondary); font-size: 13px; border-bottom: 1px solid var(--pp-border); }
.table-row { border-bottom: 1px solid #f5f5f5; transition: background 0.2s; }
.table-row:hover { background: #f8fafc; }
.col-name { display: flex; align-items: center; gap: 10px; font-weight: 500; color: var(--pp-text); }
.club-avatar {
  width: 32px; height: 32px; border-radius: 8px;
  background: var(--pp-gradient); color: #fff;
  font-size: 14px; font-weight: 600;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.col-time { font-size: 13px; color: var(--pp-text-secondary); }
</style>