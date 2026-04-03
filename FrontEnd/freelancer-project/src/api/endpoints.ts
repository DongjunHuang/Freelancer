import { UserType } from '@/types/user'
import http from '@/api/http-user'
import httpa from '@/api/http-admin'
import { markThreadAsRead } from './issue'

/**
 * Get corresponding http client for user/admin.
 *
 * @param userType admin or user.
 * @returns the corresponding client.
 */
export const getApiClient = (userType: UserType = UserType.USER) => {
  return userType === UserType.ADMIN ? httpa : http
}

/**
 * The prefix url.
 * @param userType admin or user.
 * @returns the prefix.
 */
function getPrefix(userType: UserType) {
  return userType === UserType.ADMIN ? '/admin' : ''
}

export const API_ENDPOINTS = {
  auth: {
    signin: (userType: UserType) => `${getPrefix(userType)}/auth/signin`,
    signout: (userType: UserType) => `${getPrefix(userType)}/auth/signout`,
    signup: '/auth/signup',
    resendEmail: '/auth/resendEmail',
    verifyEmail: (token: string) => `/auth/verify?token=${encodeURIComponent(token)}`,
  },
  upload: {
    fetchDatasets: '/dashboard/fetchDatasets',
    uploadCsv: '/upload/uploadCsv',
    deleteDataset: (datasetName: string) => `/upload/dataset/${datasetName}`,
  },
  dashboard: {
    fetchDatasets: '/dashboard/fetchDatasets',
    fetchDatapoints: (datasetName: string) =>
      `/dashboard/datasets/${encodeURIComponent(datasetName)}/datapoints/query`,
  },
  issue: {
    createThread: '/issues/createThread',
    postMessage: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}/postMessage`,
    getMessages: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}/getMessages`,
    getThread: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}`,
    updateThreadStatus: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}/status`,
    getThreads: (userType: UserType) => `${getPrefix(userType)}/issues/getThreads`,
    getThreadStats: (userType: UserType) => `${getPrefix(userType)}/issues/thread-stats`,
    getLatestMessages: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}/messages/latest`,
    markThreadAsRead: (userType: UserType, threadId: number) =>
      `${getPrefix(userType)}/issues/${threadId}/markAsRead`,
  },
  tests: {
    insertMetricMysql: '/tests/insert',
    fetchCountMysql: '/tests/getNumber',
    insertMetricNosql: '/tests/insertNosql',
    fetchCountNosql: '/tests/getNumberNosql',
    sendEmailTest: '/tests/sendEmailTest',
  },
} as const
