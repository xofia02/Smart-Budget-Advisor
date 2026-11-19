import { createContext, useContext, useMemo, useState, type ReactNode } from 'react'
import { login as loginApi, logout as logoutApi } from '../api/auth'
import { ApiError } from '../api/client'
import { limparSessao, lerUsuario, salvarToken, salvarUsuario } from '../api/storage'
import type { Usuario } from '../api/tipos'

type AuthContextValue = {
  usuario: Usuario | null
  entrar: (email: string, senha: string) => Promise<Usuario>
  sair: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(() => lerUsuario())

  const valor = useMemo<AuthContextValue>(
    () => ({
      usuario,
      async entrar(email, senha) {
        const resposta = await loginApi(email, senha)
        salvarToken(resposta.token)
        salvarUsuario(resposta.usuario)
        setUsuario(resposta.usuario)
        return resposta.usuario
      },
      async sair() {
        try {
          await logoutApi()
        } catch (erro) {
          if (!(erro instanceof ApiError)) {
            throw erro
          }
        } finally {
          limparSessao()
          setUsuario(null)
        }
      },
    }),
    [usuario],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth precisa estar dentro de AuthProvider')
  }
  return ctx
}
