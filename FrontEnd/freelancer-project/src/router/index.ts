// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Testing from '../views/Testing.vue'
import Data from '../views/Data.vue'

const BASE = import.meta.env.BASE_URL
console.log('[Router BASE]', BASE)

export default createRouter({
  history: createWebHistory(BASE),
  routes: [
    { path: '/', name: 'home', component: Home },
    { path: '/testing', name: 'testing', component: Testing },
    { path: '/data', name: 'data', component: Data },
  ],
})