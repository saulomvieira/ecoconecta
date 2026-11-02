import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { api, tokenStorage, UNAUTHORIZED_EVENT } from '../api/client'

interface Sessao {
  perfil: 'ADMIN' | 'USUARIO'
}

interface AuthContextValue {
  sessao: Sessao | null
  entrar: (email: string, senha: string) => Promise<void>
  sair: () => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

// Lê as claims do JWT só para a UI; quem valida de verdade é o backend.
function lerSessao(token: string | null): Sessao | null {
  if (!token) return null
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    if (payload.exp * 1000 < Date.now()) return null
    if (payload.perfil !== 'ADMIN' && payload.perfil !== 'USUARIO') return null
    return { perfil: payload.perfil }
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [sessao, setSessao] = useState<Sessao | null>(() => lerSessao(tokenStorage.get()))

  const sair = useCallback(() => {
    tokenStorage.clear()
    setSessao(null)
  }, [])

  const entrar = useCallback(async (email: string, senha: string) => {
    const { token } = await api<{ token: string }>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha }),
    })
    tokenStorage.set(token)
    setSessao(lerSessao(token))
  }, [])

  useEffect(() => {
    window.addEventListener(UNAUTHORIZED_EVENT, sair)
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, sair)
  }, [sair])

  const value = useMemo(() => ({ sessao, entrar, sair }), [sessao, entrar, sair])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return ctx
}
