import http from './http'

// The log in API
export const signin = (data: { username: string; password: string }) => http.post(`/auth/signin`, data);

// The sign up api
export const signup = (data: { username: string; password: string; email: string}) => http.post(`/auth/signup`, data);

// The sign out api
export const signout = () => http.post(`/auth/signout`);

// The verify email api
export const verifyEmail = (token: string) => http.get(`/auth/verify?token=${encodeURIComponent(token)}`)

// To resend email
export const resendEmail = (data: {username: string}) => http.post(`/auth/resendEmail`, data)

