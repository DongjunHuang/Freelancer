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
    { 
      path: "/signin", 
      component: Signin, 
      meta: { requiresAuth: false } 
    },
    { 
      path: "/signup", 
      component: Signup, 
      meta: { requiresAuth: false } 
    },
    { 
      path: "/verify", 
      component: Verify, 
      meta: { requiresAuth: false }
    },
    
    // require loggin first
    { 
      path: "/dashboard", 
      component: Dashboard,
      meta: { requiresAuth: true }
    },
    { 
      path: '/upload', 
      component: Upload, 
      meta: { requiresAuth: true }
    },
    { 
      path: '/tests', 
      component: Tests,
      meta: { requiresAuth: false }
    }, 
  ]
})

router.beforeEach((to, from, next) => {
  const auth = useAuth()

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else {
    next()
  }
})

export default router