<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <span class="brand-icon">🏓</span>
        <span class="brand-text">乒乓管理后台</span>
      </div>
      <nav class="sidebar-nav">
        <router-link to="/admin/events/create" class="nav-item" active-class="active">
          <span class="nav-icon">🏆</span><span>创建赛事</span>
        </router-link>
        <router-link to="/admin/events" class="nav-item" active-class="active" exact>
          <span class="nav-icon">📋</span><span>赛事列表</span>
        </router-link>
        <router-link to="/admin/clubs/create" class="nav-item" active-class="active">
          <span class="nav-icon">👥</span><span>创建俱乐部</span>
        </router-link>
        <router-link to="/admin/clubs" class="nav-item" active-class="active" exact>
          <span class="nav-icon">🏘️</span><span>俱乐部列表</span>
        </router-link>
        <router-link to="/admin/approvals" class="nav-item" active-class="active">
          <span class="nav-icon">✅</span><span>审批管理</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <el-button @click="logout" plain size="small" style="width: 100%;">退出登录</el-button>
      </div>
    </aside>
    <div class="main-area">
      <header class="topbar">
        <div class="topbar-title">{{ pageTitle }}</div>
        <div class="topbar-user">
          <span class="user-avatar">A</span>
          <span class="user-name">管理员</span>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const pageTitle = computed(() => {
  const map = {
    '/admin': '审批管理',
    '/admin/events/create': '创建赛事',
    '/admin/events': '赛事列表',
    '/admin/clubs/create': '创建俱乐部',
    '/admin/clubs': '俱乐部列表',
    '/admin/approvals': '审批管理',
  }
  return map[route.path] || '管理后台'
})

const logout = () => {
  localStorage.removeItem('adminToken')
  router.push('/admin/login')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; background: #f0f2f5; }

.sidebar {
  width: 220px; background: #1e293b; color: #fff;
  display: flex; flex-direction: column; flex-shrink: 0;
  position: fixed; top: 0; left: 0; bottom: 0; z-index: 100;
}
.sidebar-brand {
  padding: 20px 24px; display: flex; align-items: center; gap: 10px;
  border-bottom: 1px solid #334155;
}
.brand-icon { font-size: 24px; }
.brand-text { font-size: 16px; font-weight: 600; }

.sidebar-nav { flex: 1; padding: 16px 0; overflow-y: auto; }
.nav-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 24px; color: #94a3b8; text-decoration: none;
  font-size: 14px; transition: all 0.2s; border-left: 3px solid transparent;
}
.nav-item:hover { background: #334155; color: #fff; }
.nav-item.active { background: #334155; color: #fff; border-left-color: var(--pp-primary); }
.nav-icon { font-size: 18px; }

.sidebar-footer { padding: 16px 20px; border-top: 1px solid #334155; }

.main-area { flex: 1; margin-left: 220px; display: flex; flex-direction: column; min-height: 100vh; }
.topbar {
  height: 56px; background: #fff; display: flex;
  align-items: center; justify-content: space-between;
  padding: 0 24px; box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky; top: 0; z-index: 50;
}
.topbar-title { font-size: 18px; font-weight: 600; color: var(--pp-text); }
.topbar-user { display: flex; align-items: center; gap: 8px; }
.user-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: var(--pp-gradient); color: #fff;
  font-size: 14px; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
}
.user-name { font-size: 14px; color: var(--pp-text-secondary); }

.content { flex: 1; padding: 24px; max-width: 1100px; width: 100%; margin: 0 auto; }
</style>
