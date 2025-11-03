import http from './http'

// The log in API
export const signin = (data: { username: string; password: string }) => http.post('/auth/login', data);

// The sign up api
export const signup = (data: { username: string; password: string; email: string;  }) => http.post('/auth/signup', data);

// The refresh token api
export const refreshAccessToken = () => http.post('/auth/refresh')

// The verify email api
export const verifyEmail = (token: string) => http.get('/auth/verify?token=${encodeURIComponent(token)}')