// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

import Tests from '@/views/tests-main.vue'
import Home from '@/views/home-main.vue'

import Signin from '@/views/auth/sign-in.vue'
import Signup from '@/views/auth/sign-up.vue'
import Verify from '@/views/auth/verify-email.vue'

import Dashboard from '@/views/dashboard/dashboard-main.vue'
import Upload from '@/views/upload/upload-main.vue'

import Issue from '@/views/feedback/issue-main.vue'
import IssuePost from '@/views/feedback/issue-post.vue'
import IssueDetails from '@/views/feedback/issue-details.vue'

import AdminLogin from '@/views/admin/admin-login.vue'
import AdminHome from '@/views/admin/admin-home.vue'
import AdminIssues from '@/views/admin/admin-issues.vue'

import AppAdmin from '@/app-admin.vue'
import AppUser from '@/app-user.vue'

import { useAuth } from '@/stores/auth'
import { useAdminAuth } from '@/stores/auth-admin'
import { http } from '@/api/http-user'
import axios from 'axios'

function parseJwtPayload(token: string): Record<string, any> | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null

    const base64 = parts[1]
    const normalized = base64.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(
      normalized.length + ((4 - (normalized.length % 4 || 4)) % 4),
      '=',
    )

    const json = atob(padded)
    return JSON.parse(json)
  } catch {
    return null
  }
}

function isTokenExpired(token: string, bufferSeconds = 30): boolean {
  const payload = parseJwtPayload(token)
  if (!payload?.exp) return true

  const now = Math.floor(Date.now() / 1000)
  return payload.exp <= now + bufferSeconds
}

async function tryRefreshUser(): Promise<boolean> {
  const auth = useAuth()

  try {
    const res = await axios.post(
      `${import.meta.env.VITE_API_BASE}/auth/refresh`,
      {},
      {
        withCredentials: true,
        timeout: 10000,
      },
    )

    auth.setToken(res.data.accessToken)
    return true
  } catch {
    auth.clear()
    return false
  }
}

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

router.beforeEach(async (to) => {
  const auth = useAuth()
  const adminAuth = useAdminAuth()

  // The path to admin path
  if (to.meta.requiresAdmin) {
    if (!adminAuth.isLoggedIn) {
      return {
        path: '/admin/login',
        query: { redirect: to.fullPath },
      }
    }

    return true
  }

  // The path to user auth
  if (to.meta.requiresAuth) {
    const token = auth.accessToken

    if (!token || isTokenExpired(token, 30)) {
      const ok = await refreshAccessToken()
      if (!ok) {
        return {
          path: '/signin',
          query: { redirect: to.fullPath },
        }
      }
    }
    return true
  }

  return true
})

async function refreshAccessToken(): Promise<string> {
  const res = await http.post('/auth/refresh')
  return res.data.accessToken
}

export default router
