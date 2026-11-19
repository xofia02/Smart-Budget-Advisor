import type { Usuario } from './tipos'

const TOKEN = 'sba_token'
const USUARIO = 'sba_usuario'

export function lerToken(): string | null {
  return localStorage.getItem(TOKEN)
}

export function salvarToken(token: string): void {
  localStorage.setItem(TOKEN, token)
}

export function lerUsuario(): Usuario | null {
  const bruto = localStorage.getItem(USUARIO)
  if (!bruto) return null
  try {
    return JSON.parse(bruto) as Usuario
  } catch {
    return null
  }
}

export function salvarUsuario(usuario: Usuario): void {
  localStorage.setItem(USUARIO, JSON.stringify(usuario))
}

export function limparSessao(): void {
  localStorage.removeItem(TOKEN)
  localStorage.removeItem(USUARIO)
}
