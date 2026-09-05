<template>
  <div class="pp-page" v-if="club">
    <div class="back-bar">
      <el-button @click="$router.back()" round>← 返回俱乐部列表</el-button>
    </div>

    <div class="hero">
      <div class="hero-avatar">{{ club.name?.[0] }}</div>
      <div class="hero-info">
        <h1>{{ club.name }}</h1>
        <div class="hero-meta">
          <span>📍 {{ club.city || '未知城市' }}</span>
          <span>👥 {{ club.memberCount || 0 }} 名成员</span>
        </div>
        <p class="hero-desc">{{ club.description || '暂无简介' }}</p>
      </div>
      <div class="hero-action">
        <el-button type="primary" @click="handleJoin" size="large" round>申请加入</el-button>
      </div>
    </div>

    <div class="detail-grid">
      <div class="pp-card">
        <div class="pp-card-header">
          <h3>💬 俱乐部帖子</h3>
          <el-button v-if="isMember" type="primary" size="small" @click="showPostDialog = true" round>✏️ 发帖</el-button>
        </div>
        <div class="pp-card-body">
          <div v-if="posts.length" class="post-list">
            <div v-for="p in posts" :key="p.id" class="post-item">
              <div class="post-icon">📝</div>
              <div class="post-content">
                <h4>{{ p.title }}</h4>
                <div class="post-meta">
                  <span>作者: {{ p.authorId }}</span>
                  <span>💬 {{ p.commentCount || 0 }} 评论</span>
                  <span>📅 {{ formatDateTime(p.publishTime) }}</span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="pp-empty" style="padding: 40px;">暂无帖子，快来发第一篇吧！</div>
        </div>
      </div>

      <div class="pp-card">
        <div class="pp-card-header"><h3>👥 俱乐部成员</h3></div>
        <div class="pp-card-body">
          <div v-if="members.length" class="member-list">
            <div v-for="m in members" :key="m.userId" class="member-item">
              <div class="member-avatar">{{ (m.userId || '?').toString()[0] }}</div>
              <div class="member-info">
                <span class="member-name">{{ m.userId }}</span>
                <span class="member-role pp-badge" :class="roleBadgeClass(m.role)">{{ roleText(m.role) }}</span>
              </div>
              <span class="member-time">{{ formatDate(m.joinTime) }}</span>
            </div>
          </div>
          <div v-else class="pp-empty" style="padding: 40px;">暂无成员</div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showPostDialog" title="发布帖子" width="500px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="postForm.content" type="textarea" :rows="6" placeholder="请输入帖子内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPostDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePost">发布</el-button>
      </template>
    </el-dialog>
  </div>
  <div v-else class="pp-page pp-empty" style="padding: 120px;">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../utils/api'

const route = useRoute()
const club = ref(null)
const members = ref([])
const posts = ref([])
const isMember = ref(false)
const showPostDialog = ref(false)
const postForm = ref({ title: '', content: '' })

const roleText = (r) => ({ OWNER: '会长', ADMIN: '管理员', MEMBER: '成员' }[r] || r || '成员')
const roleBadgeClass = (r) => ({ OWNER: 'pp-badge-warning', ADMIN: 'pp-badge-primary', MEMBER: 'pp-badge-success' }[r] || 'pp-badge-success')
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatDateTime = (d) => d ? new Date(d).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) : ''

const loadData = async () => {
  const clubId = route.params.id
  try {
    const [clubRes, membersRes, postsRes] = await Promise.all([
      api.get(`/clubs/${clubId}`),
      api.get(`/clubs/${clubId}/members`),
      api.get(`/clubs/${clubId}/posts`)
    ])
    if (clubRes.code === 0) club.value = clubRes.data
    if (membersRes.code === 0) members.value = membersRes.data
    if (postsRes.code === 0) posts.value = postsRes.data
  } catch (e) { console.error(e) }
}

const handleJoin = async () => {
  try {
    const res = await api.post(`/clubs/${route.params.id}/join-requests`)
    if (res.code === 0) {
      ElMessage.success('申请已提交，等待管理员审批')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error(e.message || '申请失败') }
}

const handlePost = async () => {
  if (!postForm.value.title || !postForm.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  try {
    const res = await api.post(`/clubs/${route.params.id}/posts`, postForm.value)
    if (res.code === 0) {
      ElMessage.success('发布成功')
      showPostDialog.value = false
      postForm.value = { title: '', content: '' }
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error(e.message || '发布失败') }
}

onMounted(loadData)
</script>

<style scoped>
.back-bar { margin-bottom: 20px; }

.hero {
  background: #fff;
  border-radius: var(--pp-radius);
  box-shadow: var(--pp-shadow);
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}
.hero-avatar {
  width: 80px; height: 80px; border-radius: var(--pp-radius);
  background: var(--pp-gradient); color: #fff;
  font-size: 36px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.hero-info { flex: 1; }
.hero h1 { font-size: 28px; font-weight: 700; color: var(--pp-text); margin-bottom: 12px; }
.hero-meta { display: flex; gap: 20px; font-size: 14px; color: var(--pp-text-secondary); margin-bottom: 8px; }
.hero-desc { font-size: 14px; color: var(--pp-text-secondary); line-height: 1.6; }
.hero-action { flex-shrink: 0; }

.detail-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; }

.post-list { display: flex; flex-direction: column; gap: 12px; }
.post-item {
  display: flex; gap: 16px; padding: 16px;
  background: #f8fafc; border-radius: var(--pp-radius-sm);
  transition: all 0.2s; cursor: pointer;
}
.post-item:hover { background: #f1f5f9; transform: translateX(4px); }
.post-icon { font-size: 24px; flex-shrink: 0; }
.post-content { flex: 1; }
.post-content h4 { font-size: 16px; font-weight: 600; color: var(--pp-text); margin-bottom: 8px; }
.post-meta { display: flex; gap: 16px; font-size: 13px; color: var(--pp-text-secondary); }

.member-list { display: flex; flex-direction: column; gap: 10px; }
.member-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; background: #f8fafc; border-radius: var(--pp-radius-sm);
  transition: all 0.2s;
}
.member-item:hover { background: #f1f5f9; }
.member-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: var(--pp-gradient-cool); color: #fff;
  font-size: 16px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.member-info { flex: 1; display: flex; align-items: center; gap: 8px; }
.member-name { font-size: 14px; font-weight: 500; color: var(--pp-text); }
.member-time { font-size: 12px; color: var(--pp-text-light); }

@media (max-width: 768px) {
  .hero { flex-direction: column; text-align: center; }
  .detail-grid { grid-template-columns: 1fr; }
}
</style>
