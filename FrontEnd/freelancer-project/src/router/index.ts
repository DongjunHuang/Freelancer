// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Testing from '@/views/Metrics.vue'
import Signin from '@/views/Signin.vue';
import Signup from '@/views/Signup.vue';
import Dashboard from '@/views/Dashboard.vue';

const BASE = import.meta.env.BASE_URL
console.log('[Router BASE]', BASE)
const router =  createRouter({
  history: createWebHistory(BASE),
  routes: [
    { path: '/metrics', component: Testing },
    { path: "/signin", component: Signin },
    { path: "/signup", component: Signup },
    { path: "/dashboard", component: Dashboard }
  ],
})
export default router