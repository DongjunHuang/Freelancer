<template>
    <div class="min-h-screen bg-gray-50 text-slate-900">
      <!-- 内容区 -->
      <main class="max-w-7xl mx-auto px-4 py-6 grid gap-6 lg:grid-cols-3">
        <!-- 左列（主内容） -->
        <section class="lg:col-span-2 space-y-6">
          <!-- 横幅卡片 -->
          <div class="rounded-2xl border bg-white p-6">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-2xl font-bold leading-snug">Use YourBrand anywhere.</h3>
                <p class="text-slate-600 mt-1">Online. In stores. Just about anywhere.</p>
                <button class="mt-4 px-4 py-2 rounded-full bg-[#0B2E78] text-white hover:bg-[#0a2766]">Learn More</button>
              </div>
              <div class="hidden md:block w-40 h-24 rounded-xl bg-gradient-to-br from-blue-100 to-blue-200"></div>
            </div>
          </div>
  
          <!-- Rewards -->
          <div class="rounded-2xl border bg-white">
            <div class="p-6">
              <h4 class="text-sm font-semibold text-slate-700">YourBrand Rewards</h4>
              <div class="mt-2 text-5xl font-bold">0 <span class="text-xl font-medium align-top">points</span></div>
              <p class="text-slate-500 mt-1">$0.00 cash back value</p>
              <p class="text-slate-500">Available balance</p>
              <button class="mt-4 text-[#0B2E78] font-semibold hover:underline">Ways to earn</button>
            </div>
          </div>
  
          <!-- Benefit 提示 -->
          <div class="rounded-2xl border bg-white p-5 flex items-center justify-between">
            <div>
              <div class="font-semibold">Unlock new benefits</div>
              <p class="text-sm text-slate-600">Get a Balance Account</p>
            </div>
            <button class="px-3 py-1.5 rounded-full border hover:bg-slate-50">Learn more</button>
          </div>
  
          <!-- 最近活动 -->
          <div class="rounded-2xl border bg-white">
            <div class="p-5 border-b">
              <div class="font-semibold">Recent activity</div>
              <p class="text-sm text-slate-600">See when money comes in and when it goes out.</p>
            </div>
            <ul>
              <li v-for="(item, i) in recent" :key="i" class="px-5 py-4 flex items-center justify-between border-b last:border-b-0">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-full bg-slate-100 grid place-items-center">💸</div>
                  <div>
                    <div class="font-medium">{{ item.title }}</div>
                    <div class="text-xs text-slate-500">{{ item.date }}</div>
                  </div>
                </div>
                <div class="text-right">
                  <div :class="item.amount >= 0 ? 'text-emerald-600' : 'text-slate-800'">
                    {{ item.amount >= 0 ? '+' : '' }}{{ item.amount.toFixed(2) }}
                  </div>
                  <div class="text-xs text-slate-500">{{ item.currency }}</div>
                </div>
              </li>
            </ul>
            <div class="p-5">
              <button class="text-[#0B2E78] font-semibold hover:underline">Show all</button>
            </div>
          </div>
        </section>
  
        <aside class="space-y-6">
          <div class="rounded-2xl border bg-white p-5">
            <div class="grid grid-cols-4 gap-3 text-center">
              <button v-for="q in quickActions" :key="q.label"
                class="group">
                <div class="w-12 h-12 mx-auto rounded-full bg-slate-100 grid place-items-center group-hover:bg-slate-200"> {{ q.icon }} </div>
                <div class="mt-2 text-xs text-slate-700">{{ q.label }}</div>
              </button>
            </div>
          </div>
  
          <div class="rounded-2xl border bg-white p-5">
            <div class="font-semibold mb-3">Send again</div>
            <button class="w-full flex items-center gap-3 px-3 py-2 rounded-xl border hover:bg-slate-50">
              <span class="w-9 h-9 rounded-full bg-blue-100 grid place-items-center">🔎</span>
              <span class="text-sm">Search</span>
            </button>
          </div>
  
          <div class="rounded-2xl border bg-white p-5">
            <div class="font-semibold mb-3">Banks and cards</div>
            <ul class="space-y-3">
              <li v-for="b in banks" :key="b.id" class="flex items-center justify-between">
                <div class="flex items-center gap-3">
                  <div class="w-9 h-9 rounded-lg bg-slate-100 grid place-items-center">🏦</div>
                  <div>
                    <div class="text-sm font-medium">{{ b.name }}</div>
                    <div class="text-xs text-slate-500">{{ b.mask }}</div>
                  </div>
                </div>
                <button class="text-slate-500 hover:text-slate-700">⋯</button>
              </li>
            </ul>
            <button class="mt-4 text-[#0B2E78] font-semibold hover:underline">Link a Card or Bank</button>
          </div>
  
          <div class="rounded-2xl border bg-white p-5">
            <div class="font-semibold mb-2">Your favorite charities</div>
            <p class="text-sm text-slate-600">Make giving simple.</p>
            <button class="mt-3 text-[#0B2E78] font-semibold hover:underline">Add your first charity</button>
          </div>
        </aside>
      </main>
    </div>
  </template>
  
  <script setup lang="ts">
  import { RouterLink } from 'vue-router'
  
  const topNav = [
    { label: 'Home', to: '/' },
    { label: 'Finances', to: '/finances' },
    { label: 'Send and Request', to: '/send' },
    { label: 'Rewards', to: '/rewards' },
    { label: 'Wallet', to: '/wallet' },
    { label: 'Activity', to: '/activity' },
    { label: 'Help', to: '/help' },
  ]
  
  const recent = [
    { title: 'Payment to Grocery', date: 'Oct 25, 2025', amount: -36.8, currency: 'USD' },
    { title: 'Refund from Vendor',  date: 'Oct 21, 2025', amount: +12.0, currency: 'USD' },
    { title: 'Transfer to Bank',    date: 'Oct 20, 2025', amount: -150.0, currency: 'USD' },
  ]
  
  const quickActions = [
    { icon: '↔️', label: 'Send / Request' },
    { icon: '💳', label: 'Savings' },
    { icon: '🏆', label: 'Rewards' },
    { icon: '🧾', label: 'Pay bills' },
  ]
  
  const banks = [
    { id: 1, name: 'JPMORGAN CHASE BANK, NA', mask: 'Checking ••••18' },
  ]
  </script>