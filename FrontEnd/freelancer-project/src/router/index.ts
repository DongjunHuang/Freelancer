// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Tests from '@/views/Tests.vue'
import Signin from '@/views/Signin.vue';
import Signup from '@/views/Signup.vue';
import Dashboard from '@/views/Dashboard.vue';
import Verify from '@/views/Verify.vue'
import { useAuth } from '@/stores/auth'

const BASE = import.meta.env.BASE_URL
const router =  createRouter({
  history: createWebHistory(BASE),
  routes: [
    { path: "/signin", component: Signin, meta: { guestOnly: true } },
    { path: "/signup", component: Signup, meta: { guestOnly: true } },
    { path: "/verify", component: Verify, meta: { guestOnly: true }},
    { path: "/dashboard", component: Dashboard},
    { path: '/tests', component: Tests, meta: { guestOnly: true } }
  ],
})

router.beforeEach((to, from, next) => {
  const { isLoggedIn } = useAuth()
  if (to.meta.requiresAuth && !isLoggedIn.value) {
    return next({ path: '/signin', query: { redirect: to.fullPath } })
  }
  if (to.meta.guestOnly && isLoggedIn.value) {
    return next('/tests')
  }
  next()
})

export default router