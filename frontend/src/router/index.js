import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/Home.vue') },
      { path: 'ranking', name: 'Ranking', component: () => import('../views/Ranking.vue') },
      { path: 'events', name: 'Events', component: () => import('../views/Events.vue') },
      { path: 'events/:id', name: 'EventDetail', component: () => import('../views/EventDetail.vue') },
      { path: 'clubs', name: 'Clubs', component: () => import('../views/Clubs.vue') },
      { path: 'clubs/:id', name: 'ClubDetail', component: () => import('../views/ClubDetail.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') }
    ]
  },
  { path: '/admin/login', name: 'AdminLogin', component: () => import('../views/admin/AdminLogin.vue') },
  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/admin/approvals' },
      { path: 'events/create', name: 'AdminCreateEvent', component: () => import('../views/admin/CreateEvent.vue') },
      { path: 'events', name: 'AdminEventList', component: () => import('../views/admin/EventList.vue') },
      { path: 'clubs/create', name: 'AdminCreateClub', component: () => import('../views/admin/CreateClub.vue') },
      { path: 'clubs', name: 'AdminClubList', component: () => import('../views/admin/ClubList.vue') },
      { path: 'approvals', name: 'AdminApprovals', component: () => import('../views/admin/Approvals.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const adminToken = localStorage.getItem('adminToken')

  if (to.path.startsWith('/admin') && to.path !== '/admin/login') {
    if (!adminToken) {
      next('/admin/login')
      return
    }
  } else if (to.path !== '/login' && to.path !== '/register' && to.path !== '/admin/login'
             && !to.path.startsWith('/admin')) {
    if (!token && to.path !== '/') {
      next('/login')
      return
    }
  }
  next()
})

export default router