import http from './httpAdmin'

// The log in API
export const adminSignin = (data: { username: string; password: string }) => http.post(`/admin/auth/signin`, data);

// The sign out api
export const adminSignout = () => http.post(`/admin/auth/signout`);