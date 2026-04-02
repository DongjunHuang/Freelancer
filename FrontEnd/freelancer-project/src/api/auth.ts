import { getApiClient, API_ENDPOINTS } from '@/api/endpoints'
import { UserType } from '@/types/user'

// USER/ADMIN API to login
export const signin = (userType: UserType, username: string, password: string) => {
  return getApiClient(userType).post(API_ENDPOINTS.auth.signin(userType), {
    username,
    password,
  })
}

// USER/ADMIN API to sign out
export const signout = (userType: UserType) =>
  getApiClient(userType).post(API_ENDPOINTS.auth.signout(userType))

// User only sign up api
export const signup = (data: { username: string; password: string; email: string }) =>
  getApiClient().post(API_ENDPOINTS.auth.signup, data)

// User only resend email api
export const resendEmail = (data: { username: string }) =>
  getApiClient().post(API_ENDPOINTS.auth.resendEmail, data)

// User only verify email api
export const verifyEmail = (token: string) =>
  getApiClient().post(API_ENDPOINTS.auth.verifyEmail(token))
