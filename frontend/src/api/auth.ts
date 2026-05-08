import { apiFetch, apiJson } from './client'
import { ApiError, type LoginRequest, type RegisterRequest, type UserResponse } from './types'

export async function registerUser(body: RegisterRequest): Promise<UserResponse> {
  return apiJson<UserResponse>('/api/users', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function login(body: LoginRequest): Promise<UserResponse> {
  return apiJson<UserResponse>('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
}

export async function logout(): Promise<void> {
  await apiJson<void>('/api/auth/logout', {
    method: 'POST',
  })
}

export async function fetchMe(): Promise<UserResponse | null> {
  const response = await apiFetch('/api/auth/me', { method: 'GET' })
  if (response.status === 401) {
    return null
  }
  if (!response.ok) {
    throw await ApiError.fromResponse(response)
  }
  return response.json() as Promise<UserResponse>
}
