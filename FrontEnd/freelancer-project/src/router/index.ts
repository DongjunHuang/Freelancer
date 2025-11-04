// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Tests from '@/views/Tests.vue'
import Signin from '@/views/Signin.vue';
import Signup from '@/views/Signup.vue';
import Dashboard from '@/views/Dashboard.vue';
import Verify from '@/views/Verify.vue'

const BASE = import.meta.env.BASE_URL
console.log('[Router BASE]', BASE)
const router =  createRouter({
  history: createWebHistory(BASE),
  routes: [
    { path: '/tests', component: Tests },
    { path: "/signin", component: Signin },
    { path: "/signup", component: Signup },
    { path: "/dashboard", component: Dashboard },
    { path: "/verify", component: Verify }
  ],
})
export default router