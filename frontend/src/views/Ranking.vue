<template>
  <div class="pp-page">
    <div class="pp-card">
      <div class="pp-card-header">
        <h3>🏆 积分排行榜</h3>
        <el-radio-group v-model="scope" @change="loadRanking">
          <el-radio-button label="GLOBAL">全站排行</el-radio-button>
          <el-radio-button label="GROUPED">按等级分组</el-radio-button>
        </el-radio-group>
      </div>
      <div class="pp-card-body">
        <div v-if="scope === 'GLOBAL'">
          <div class="filter-bar">
            <span class="filter-label">技术水平：</span>
            <el-select v-model="skillLevel" placeholder="全部等级" clearable @change="loadRanking" style="width: 160px;">
              <el-option label="业余初级" value="BEGINNER" />
              <el-option label="业余中级" value="INTERMEDIATE" />
              <el-option label="业余高级" value="ADVANCED" />
              <el-option label="专业级" value="PROFESSIONAL" />
            </el-select>
          </div>
          <div v-if="ranking.length" class="rank-table">
            <div v-for="(u, i) in ranking" :key="u.id" class="rank-row">
              <span class="rank-num" :class="{top3: i < 3}">{{ i + 1 }}</span>
              <el-avatar :size="40" :src="u.avatarUrl">{{ u.nickname?.[0] }}</el-avatar>
              <div class="rank-user">
                <span class="rank-name">{{ u.nickname }}</span>
                <span class="rank-meta">{{ u.city || '未知' }} · {{ levelText(u.skillLevel) }}</span>
              </div>
              <span class="rank-score">{{ u.currentScore }}</span>
              <span class="rank-count">{{ u.matchCount || 0 }}场</span>
            </div>
          </div>
          <div v-else class="pp-empty">暂无排名数据</div>
        </div>
        <div v-else>
          <el-tabs v-model="activeLevel" @tab-change="loadGroupedRanking">
            <el-tab-pane label="业余初级" name="BEGINNER" />
            <el-tab-pane label="业余中级" name="INTERMEDIATE" />
            <el-tab-pane label="业余高级" name="ADVANCED" />
            <el-tab-pane label="专业级" name="PROFESSIONAL" />
          </el-tabs>
          <div v-if="groupedRanking.length" class="rank-table">
            <div v-for="(u, i) in groupedRanking" :key="u.id" class="rank-row">
              <span class="rank-num" :class="{top3: i < 3}">{{ i + 1 }}</span>
              <el-avatar :size="40" :src="u.avatarUrl">{{ u.nickname?.[0] }}</el-avatar>
              <div class="rank-user">
                <span class="rank-name">{{ u.nickname }}</span>
                <span class="rank-meta">{{ u.city || '未知' }}</span>
              </div>
              <span class="rank-score">{{ u.currentScore }}</span>
              <span class="rank-count">{{ u.matchCount || 0 }}场</span>
            </div>
          </div>
          <div v-else class="pp-empty">暂无数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const scope = ref('GLOBAL')
const skillLevel = ref('')
const ranking = ref([])
const activeLevel = ref('BEGINNER')
const groupedRanking = ref([])

const levelText = (l) => ({ BEGINNER: '业余初级', INTERMEDIATE: '业余中级', ADVANCED: '业余高级', PROFESSIONAL: '专业级' }[l] || '未设置')

const loadRanking = async () => {
  try {
    const res = skillLevel.value
      ? await api.get('/scores/ranking/by-level', { params: { skillLevel: skillLevel.value, scope: 'GLOBAL' } })
      : await api.get('/scores/ranking/global')
    if (res.code === 0) ranking.value = res.data
  } catch (e) {}
}
const loadGroupedRanking = async () => {
  try {
    const res = await api.get('/scores/ranking/by-level', { params: { skillLevel: activeLevel.value, scope: 'GLOBAL' } })
    if (res.code === 0) groupedRanking.value = res.data
  } catch (e) {}
}
onMounted(() => { loadRanking(); loadGroupedRanking() })
</script>

<style scoped>
.filter-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.filter-label { font-size: 14px; color: var(--pp-text-secondary); }
.rank-table { display: flex; flex-direction: column; gap: 8px; }
.rank-row {
  display: flex; align-items: center; gap: 16px; padding: 12px 16px;
  background: #f8fafc; border-radius: var(--pp-radius-sm); transition: all 0.2s;
}
.rank-row:hover { background: #eff6ff; }
.rank-num { width: 32px; height: 32px; border-radius: 50%; background: #e2e8f0; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; color: var(--pp-text-secondary); flex-shrink: 0; }
.rank-num.top3 { background: var(--pp-gradient); color: #fff; }
.rank-user { flex: 1; display: flex; flex-direction: column; }
.rank-name { font-size: 15px; font-weight: 500; color: var(--pp-text); }
.rank-meta { font-size: 12px; color: var(--pp-text-secondary); }
.rank-score { font-size: 18px; font-weight: 700; color: var(--pp-primary); min-width: 60px; text-align: right; }
.rank-count { font-size: 13px; color: var(--pp-text-secondary); min-width: 50px; text-align: right; }
</style>
