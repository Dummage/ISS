import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import * as authApi from '../api/auth'
import type { LoginRequest, RegisterRequest, UserResponse } from '../api/types'

type AuthContextValue = {
  user: UserResponse | null
  isLoading: boolean
  login: (body: LoginRequest) => Promise<void>
  logout: () => Promise<void>
  register: (body: RegisterRequest) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    let cancelled = false

    async function loadSession() {
      setIsLoading(true)
      try {
        const me = await authApi.fetchMe()
        if (!cancelled) {
          setUser(me)
        }
      } catch {
        if (!cancelled) {
          setUser(null)
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadSession()

    return () => {
      cancelled = true
    }
  }, [])

  const login = useCallback(async (body: LoginRequest) => {
    const nextUser = await authApi.login(body)
    setUser(nextUser)
  }, [])

  const logout = useCallback(async () => {
    await authApi.logout()
    setUser(null)
  }, [])

  const register = useCallback(async (body: RegisterRequest) => {
    await authApi.registerUser(body)
    const nextUser = await authApi.login({
      email: body.email,
      password: body.password,
    })
    setUser(nextUser)
  }, [])

  const value = useMemo(
    () => ({
      user,
      isLoading,
      login,
      logout,
      register,
    }),
    [user, isLoading, login, logout, register],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
