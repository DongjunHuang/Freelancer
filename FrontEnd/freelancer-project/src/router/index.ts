// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

import Tests from '@/views/Tests.vue'
import Home from '@/views/Home.vue'

import Signin from '@/views/auth/Signin.vue'
import Signup from '@/views/auth/Signup.vue'
import Verify from '@/views/auth/Verify.vue'

import Dashboard from '@/views/dashboard/Dashboard.vue'

import Upload from '@/views/upload/Upload.vue'

import Issue from '@/views/feedback/Issue.vue'
import IssuePost from '@/views/feedback/IssuePost.vue'
import IssueDetails from '@/views/feedback/IssueDetails.vue'

import AdminLogin from '@/views/admin/AdminLogin.vue'
import AdminHome from '@/views/admin/AdminHome.vue'
import AdminIssues from '@/views/admin/AdminIssues.vue'

import { useAuth } from '@/stores/auth'
import { useAdminAuth } from '@/stores/authAdmin'

import AppAdmin from '@/AppAdmin.vue'
import AppUser from '@/AppUser.vue'

const BASE = import.meta.env.BASE_URL

const router = createRouter({
  history: createWebHistory(BASE),
  routes: [
    {
      path: '/',
      component: AppUser,
      children: [
        {
          path: '',
          component: Home,
        },
        {
          path: 'dashboard',
          component: Dashboard,
          meta: { requiresAuth: true },
        },
        {
          path: 'signin',
          component: Signin,
        },
        {
          path: 'signup',
          component: Signup,
        },
        {
          path: 'verify',
          component: Verify,
        },
        {
          path: 'upload',
          component: Upload,
          meta: { requiresAuth: true },
        },
        {
          path: 'tests',
          component: Tests,
        },
        {
          path: 'issue',
          component: Issue,
          meta: { requiresAuth: true },
        },
        {
          path: 'issue/new',
          component: IssuePost,
          meta: { requiresAuth: true },
        },
        {
          path: 'issue/details/:id',
          component: IssueDetails,
          meta: { requiresAuth: true },
        },
      ],
    },
    {
      path: '/admin',
      component: AppAdmin,
      children: [
        {
          path: '',
          component: AdminHome,
          meta: { requiresAdmin: true },
        },
        {
          path: 'issues',
          component: AdminIssues,
          meta: { requiresAdmin: true },
        },
      ],
    },
    {
      path: '/admin/login',
      component: AdminLogin,
    },
  ],
})

router.beforeEach((to, from, next) => {
  const auth = useAuth()
  const adminAuth = useAdminAuth()

  if (to.meta.requiresAdmin) {
    if (!adminAuth.isLoggedIn) {
      next({
        path: '/admin/login',
        query: { redirect: to.fullPath },
      })
      return
    }

    next()
    return
  }

  if (to.meta.requiresAuth) {
    if (!auth.isLoggedIn) {
      next({
        path: '/signin',
        query: { redirect: to.fullPath },
      })
      return
    }
  }

  next()
})

export default router
