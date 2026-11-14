import { get, post, type PageQuery, type PageResult } from './request'

export interface LoginPayload {
  username: string
  password: string
  scope: 'platform' | 'tenant'
}

export interface AuthUser {
  id: number
  username: string
  nickname: string
  scope: 'platform' | 'tenant'
  tenantId?: number | null
  tenantName?: string
  orgName?: string
}

export interface LoginResult {
  token: string
  user: AuthUser
  roles: string[]
  menus: any[]
  buttons: string[]
}

export const login = (data: LoginPayload) => post<LoginResult>('/auth/login', data)

export const getMe = () => get<LoginResult>('/auth/me')

export const logout = () => post<boolean>('/auth/logout')

export const changePassword = (data: { oldPassword: string; newPassword: string }) =>
  post<boolean>('/auth/password', data)

export type { PageQuery, PageResult }
