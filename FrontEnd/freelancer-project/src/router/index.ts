// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Tests from '@/views/Tests.vue'
import Signin from '@/views/Signin.vue';
import Signup from '@/views/Signup.vue';
import Dashboard from '@/views/Dashboard.vue';
import Verify from '@/views/Verify.vue'
import Upload from '@/views/Upload.vue'
import { useAuth } from '@/stores/auth'

const BASE = import.meta.env.BASE_URL
const router =  createRouter({
  history: createWebHistory(BASE),
  routes: [
    // testing only
    { path: "/signin", component: Signin, meta: { guestOnly: true } },
    { path: "/signup", component: Signup, meta: { guestOnly: true } },
    { path: "/verify", component: Verify, meta: { guestOnly: true }},
    { path: '/tests', component: Tests, meta: { guestOnly: true } },
    
    // require loggin first
    { path: "/dashboard", component: Dashboard},
    { path: '/upload', component: Upload },
  ],
})

router.beforeEach((to, from, next) => {
  const { isLoggedIn } = useAuth()
  if (to.meta.requiresAuth && !isLoggedIn) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }
  if (to.meta.guestOnly && isLoggedIn) {
    return next('/tests')
  }
  next()
})

export default router