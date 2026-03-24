// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

import Tests from '@/views/Tests.vue'
import Home from '@/views/Home.vue'

import Signin from '@/views/auth/sign-in.vue'
import Signup from '@/views/auth/sign-up.vue'
import Verify from '@/views/auth/verify.vue'

import Dashboard from '@/views/dashboard/dashboard.vue'

import Upload from '@/views/upload/upload.vue'

import Issue from '@/views/feedback/issue.vue'
import IssuePost from '@/views/feedback/issue-post.vue'
import IssueDetails from '@/views/feedback/issue-details.vue'

import AdminLogin from '@/views/admin/admin-login.vue'
import AdminHome from '@/views/admin/admin-home.vue'
import AdminIssues from '@/views/admin/admin-issues.vue'

import { useAuth } from '@/stores/auth'
import { useAdminAuth } from '@/stores/auth-admin'

import AppAdmin from '@/app-admin.vue'
import AppUser from '@/app-user.vue'

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
