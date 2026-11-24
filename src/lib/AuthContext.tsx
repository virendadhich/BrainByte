import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { ApiError, getCurrentAccount, loginAccount, logoutAccount, registerAccount, type Account } from './api'

interface AuthContextValue {
  account: Account | null
  loading: boolean
  error: string | null
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [account, setAccount] = useState<Account | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    try {
      const response = await getCurrentAccount()
      setAccount(response.account)
      setError(null)
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setAccount(null)
        setError(null)
      } else {
        setAccount(null)
        setError(
          err instanceof ApiError
            ? err.message
            : 'Cannot reach the account API. Start it with `npm run api` and check the local MySQL settings.',
        )
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const login = useCallback(async (email: string, password: string) => {
    const response = await loginAccount(email, password)
    setAccount(response.account)
    setError(null)
  }, [])

  const register = useCallback(async (email: string, password: string) => {
    const response = await registerAccount(email, password)
    setAccount(response.account)
    setError(null)
  }, [])

  const logout = useCallback(async () => {
    await logoutAccount()
    setAccount(null)
  }, [])

  const value = useMemo(
    () => ({ account, loading, error, login, register, logout }),
    [account, loading, error, login, register, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
