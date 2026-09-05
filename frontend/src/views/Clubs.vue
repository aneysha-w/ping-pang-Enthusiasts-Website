<template>
  <div class="pp-page">
    <div class="page-header">
      <h2>👥 附近俱乐部</h2>
      <el-input v-model="city" placeholder="按城市筛选" clearable @change="loadClubs" style="width: 200px;" prefix-icon="Search" />
    </div>
    <div v-if="clubs.length" class="club-grid">
      <div v-for="club in clubs" :key="club.id" class="club-card" @click="$router.push(`/clubs/${club.id}`)">
        <div class="club-avatar">{{ club.name?.[0] }}</div>
        <div class="club-info">
          <h3>{{ club.name }}</h3>
          <p class="club-desc">{{ club.description || '暂无简介' }}</p>
          <div class="club-meta">
            <span>📍 {{ club.city || '未知' }}</span>
            <span>👥 {{ club.memberCount }}人</span>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="pp-empty" style="padding: 80px;">暂无俱乐部</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const clubs = ref([])
const city = ref('')

const loadClubs = async () => {
  try {
    const res = await api.get('/clubs/nearby', { params: { city: city.value } })
    if (res.code === 0) clubs.value = res.data
  } catch (e) {}
}
onMounted(loadClubs)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 700; color: var(--pp-text); }
.club-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.club-card {
  background: #fff; border-radius: var(--pp-radius); box-shadow: var(--pp-shadow);
  cursor: pointer; transition: all 0.3s; padding: 24px; display: flex; gap: 16px;
}
.club-card:hover { box-shadow: var(--pp-shadow-hover); transform: translateY(-4px); }
.club-avatar {
  width: 56px; height: 56px; border-radius: var(--pp-radius-sm);
  background: var(--pp-gradient); color: #fff; font-size: 24px; font-weight: 700;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.club-info { flex: 1; min-width: 0; }
.club-info h3 { font-size: 18px; font-weight: 600; color: var(--pp-text); margin-bottom: 6px; }
.club-desc { font-size: 13px; color: var(--pp-text-secondary); margin-bottom: 12px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.club-meta { display: flex; gap: 16px; font-size: 13px; color: var(--pp-text-secondary); }
</style>
