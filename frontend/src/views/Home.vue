<template>
  <div class="home">
    <div class="hero-section">
      <h1>欢迎回来，{{ homeData.profile?.nickname || '球友' }}</h1>
      <p>探索乒乓球的世界，与球友交流、切磋技艺</p>
    </div>

    <div class="entry-cards">
      <div class="entry-card" style="--card-gradient: var(--pp-gradient)" @click="$router.push('/ranking')">
        <div class="entry-icon">🏆</div>
        <div class="entry-info">
          <h3>积分排名</h3>
          <p>查看球友积分排行</p>
        </div>
        <div class="entry-stat">第{{ homeData.myRank > 0 ? homeData.myRank : '--' }}名</div>
      </div>
      <div class="entry-card" style="--card-gradient: var(--pp-gradient-warm)" @click="$router.push('/events')">
        <div class="entry-icon">🎯</div>
        <div class="entry-info">
          <h3>赛事中心</h3>
          <p>查看并报名参赛</p>
        </div>
        <div class="entry-stat">{{ homeData.recentEvents?.length || 0 }}场赛事</div>
      </div>
      <div class="entry-card" style="--card-gradient: var(--pp-gradient-cool)" @click="$router.push('/clubs')">
        <div class="entry-icon">👥</div>
        <div class="entry-info">
          <h3>俱乐部</h3>
          <p>加入俱乐部交流</p>
        </div>
        <div class="entry-stat">{{ homeData.nearbyClubs?.length || 0 }}个俱乐部</div>
      </div>
      <div class="entry-card" style="--card-gradient: var(--pp-gradient-success)" @click="$router.push('/profile')">
        <div class="entry-icon">👤</div>
        <div class="entry-info">
          <h3>个人中心</h3>
          <p>管理个人资料</p>
        </div>
        <div class="entry-stat">{{ homeData.profile?.currentScore || 1000 }}积分</div>
      </div>
    </div>

    <div class="content-grid">
      <div class="pp-card">
        <div class="pp-card-header"><h3>🏆 积分排名 TOP 10</h3><span class="link" @click="$router.push('/ranking')">查看全部 →</span></div>
        <div class="pp-card-body" style="padding: 0;">
          <div v-if="homeData.rankingPreview?.length" class="rank-list">
            <div v-for="(u, i) in homeData.rankingPreview" :key="u.userId" class="rank-item">
              <span class="rank-num" :class="{top3: i < 3}">{{ i + 1 }}</span>
              <span class="rank-name">{{ u.nickname }}</span>
              <span class="rank-score">{{ u.currentScore }}</span>
            </div>
          </div>
          <div v-else class="pp-empty">暂无排名数据</div>
        </div>
      </div>

      <div class="pp-card">
        <div class="pp-card-header"><h3>🎯 近期赛事</h3><span class="link" @click="$router.push('/events')">查看全部 →</span></div>
        <div class="pp-card-body" style="padding: 0;">
          <div v-if="homeData.recentEvents?.length" class="event-list">
            <div v-for="e in homeData.recentEvents" :key="e.eventId" class="event-item" @click="$router.push(`/events/${e.eventId}`)">
              <div class="event-info">
                <span class="event-name">{{ e.name }}</span>
                <span class="pp-badge" :class="statusClass(e.status)">{{ statusText(e.status) }}</span>
              </div>
              <span class="event-level">{{ levelText(e.level) }}</span>
            </div>
          </div>
          <div v-else class="pp-empty">暂无赛事</div>
        </div>
      </div>

      <div class="pp-card">
        <div class="pp-card-header"><h3>👥 附近俱乐部</h3><span class="link" @click="$router.push('/clubs')">查看全部 →</span></div>
        <div class="pp-card-body" style="padding: 0;">
          <div v-if="homeData.nearbyClubs?.length" class="club-list">
            <div v-for="c in homeData.nearbyClubs" :key="c.clubId" class="club-item" @click="$router.push(`/clubs/${c.clubId}`)">
              <span class="club-name">{{ c.name }}</span>
              <span class="club-meta">{{ c.city || '未知' }} · {{ c.memberCount }}人</span>
            </div>
          </div>
          <div v-else class="pp-empty">暂无俱乐部</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../utils/api'

const homeData = ref({})

const levelText = (l) => ({ CHAMPIONSHIP: '锦标赛', OPEN: '公开赛', CLUB: '俱乐部赛', FRIENDLY: '友谊赛' }[l] || l)
const statusText = (s) => ({ PENDING: '待开始', IN_PROGRESS: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }[s] || s)
const statusClass = (s) => ({ PENDING: 'pp-badge-primary', IN_PROGRESS: 'pp-badge-warning', FINISHED: 'pp-badge-success', CANCELLED: 'pp-badge-danger' }[s] || 'pp-badge-primary')

onMounted(async () => {
  try {
    const res = await api.get('/home')
    if (res.code === 0) homeData.value = res.data
  } catch (e) {}
})
</script>

<style scoped>
.home { padding: 24px; max-width: 1200px; margin: 0 auto; }
.hero-section { text-align: center; padding: 40px 0 32px; }
.hero-section h1 { font-size: 28px; font-weight: 700; color: var(--pp-text); margin-bottom: 8px; }
.hero-section p { font-size: 15px; color: var(--pp-text-secondary); }

.entry-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
.entry-card {
  background: #fff; border-radius: var(--pp-radius); padding: 24px;
  box-shadow: var(--pp-shadow); cursor: pointer; transition: all 0.3s;
  display: flex; align-items: center; gap: 16px; position: relative; overflow: hidden;
}
.entry-card::before {
  content: ''; position: absolute; top: 0; left: 0; right: 0; height: 4px;
  background: var(--card-gradient);
}
.entry-card:hover { box-shadow: var(--pp-shadow-hover); transform: translateY(-4px); }
.entry-icon { font-size: 36px; }
.entry-info h3 { font-size: 16px; font-weight: 600; color: var(--pp-text); }
.entry-info p { font-size: 13px; color: var(--pp-text-secondary); margin-top: 2px; }
.entry-stat { margin-left: auto; font-size: 13px; font-weight: 600; color: var(--pp-primary); }

.content-grid { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 16px; }
.link { color: var(--pp-primary); font-size: 14px; cursor: pointer; }

.rank-list, .event-list, .club-list { padding: 8px 0; }
.rank-item {
  display: flex; align-items: center; padding: 10px 24px; gap: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.rank-item:last-child { border-bottom: none; }
.rank-num { width: 28px; height: 28px; border-radius: 50%; background: #f0f2f5; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; color: var(--pp-text-secondary); }
.rank-num.top3 { background: var(--pp-gradient); color: #fff; }
.rank-name { flex: 1; font-size: 14px; color: var(--pp-text); }
.rank-score { font-size: 14px; font-weight: 600; color: var(--pp-primary); }

.event-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 24px; border-bottom: 1px solid #f5f5f5; cursor: pointer; }
.event-item:last-child { border-bottom: none; }
.event-item:hover { background: #f8fafc; }
.event-info { display: flex; align-items: center; gap: 8px; }
.event-name { font-size: 14px; color: var(--pp-text); }
.event-level { font-size: 12px; color: var(--pp-text-secondary); }

.club-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 24px; border-bottom: 1px solid #f5f5f5; cursor: pointer; }
.club-item:last-child { border-bottom: none; }
.club-item:hover { background: #f8fafc; }
.club-name { font-size: 14px; color: var(--pp-text); }
.club-meta { font-size: 12px; color: var(--pp-text-secondary); }
</style>
