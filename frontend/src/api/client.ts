import { ApiError } from './types'

function resolveApiPath(path: string): string {
  if (path.startsWith('/api')) {
    return path
  }
  if (path.startsWith('/')) {
    return `/api${path}`
  }
  return `/api/${path}`
}

export async function apiFetch(path: string, init?: RequestInit): Promise<Response> {
  const url = resolveApiPath(path)
  return fetch(url, {
    credentials: 'include',
    ...init,
    headers: {
      ...init?.headers,
    },
  })
}

export async function apiJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await apiFetch(path, init)
  if (!response.ok) {
    throw await ApiError.fromResponse(response)
  }
  if (response.status === 204) {
    return undefined as T
  }
  const text = await response.text()
  if (text.length === 0) {
    return undefined as T
  }
  return JSON.parse(text) as T
}
