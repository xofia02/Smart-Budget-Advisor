import { api } from './client'
import type { LoginResponse, StatusApi, Usuario } from './tipos'

export function cadastrar(dados: {
  nome: string
  email: string
  senha: string
  consentimentoLGPD: boolean
}): Promise<Usuario> {
  return api<Usuario>('/api/auth/cadastro', {
    method: 'POST',
    body: dados,
    autenticado: false,
  })
}

export function login(email: string, senha: string): Promise<LoginResponse> {
  return api<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: { email, senha },
    autenticado: false,
  })
}

export function logout(): Promise<void> {
  return api<void>('/api/auth/logout', { method: 'POST' })
}

export function buscarStatus(): Promise<StatusApi> {
  return api<StatusApi>('/api/status', { autenticado: false })
}

export function excluirMinhaConta(): Promise<void> {
  return api<void>('/api/usuarios/me', { method: 'DELETE' })
}
