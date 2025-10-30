// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'
import Testing from '../views/Testing.vue'
import Data from '../views/Data.vue'
import Login from '../views/Login.vue';
import Signup from '../views/Signup.vue';

const BASE = import.meta.env.BASE_URL
console.log('[Router BASE]', BASE)

export default createRouter({
  history: createWebHistory(BASE),
  routes: [
    { path: '/', component: Home },
    { path: '/testing', component: Testing },
    { path: '/data', component: Data },
    { path: "/login", component: Login },
    { path: "/signup", component: Signup },
  ],
})