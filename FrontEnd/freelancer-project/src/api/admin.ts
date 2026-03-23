import httpa from '@/api/httpadmin'

// The log in API
export const adminSignin = (data: { username: string; password: string }) =>
  httpa.post(`/admin/auth/signin`, data)

// The sign out api
export const adminSignout = () => httpa.post(`/admin/auth/signout`)
