<template>
  <div class="layout">
    <header class="layout-header">
      <div class="header-inner">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">🏓</span>
          <span class="logo-text">乒乓交流平台</span>
        </div>
        <nav class="nav-menu">
          <router-link to="/" class="nav-item" :class="{active: $route.path === '/'}">首页</router-link>
          <router-link to="/ranking" class="nav-item" :class="{active: $route.path === '/ranking'}">积分排名</router-link>
          <router-link to="/events" class="nav-item" :class="{active: $route.path.startsWith('/events')}">赛事中心</router-link>
          <router-link to="/clubs" class="nav-item" :class="{active: $route.path.startsWith('/clubs')}">俱乐部</router-link>
          <router-link to="/profile" class="nav-item" :class="{active: $route.path === '/profile'}">个人中心</router-link>
        </nav>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user?.avatarUrl">{{ user?.nickname?.[0] || 'U' }}</el-avatar>
              <span class="user-name">{{ user?.nickname || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="admin" divided>管理后台</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>
    <main class="layout-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'

const router = useRouter()
const user = ref(null)

onMounted(async () => {
  try {
    const res = await api.get('/users/me')
    if (res.code === 0) user.value = res.data
  } catch (e) {}
})

const handleCommand = (cmd) => {
  if (cmd === 'profile') router.push('/profile')
  else if (cmd === 'admin') router.push('/admin/login')
  else if (cmd === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { min-height: 100vh; background: var(--pp-bg); }
.layout-header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  position: sticky; top: 0; z-index: 100;
}
.header-inner {
  max-width: 1200px; margin: 0 auto;
  height: 64px; display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
}
.logo { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 20px; font-weight: 700; color: var(--pp-text); }
.nav-menu { display: flex; gap: 8px; }
.nav-item {
  padding: 8px 16px; border-radius: 8px;
  color: var(--pp-text-secondary); font-size: 15px; font-weight: 500;
  text-decoration: none; transition: all 0.2s;
}
.nav-item:hover { color: var(--pp-primary); background: #eff6ff; }
.nav-item.active { color: var(--pp-primary); background: #eff6ff; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { font-size: 14px; color: var(--pp-text); }
.layout-main { min-height: calc(100vh - 64px); }
</style>
