import { api } from './client'
import type { Indicadores, UsuarioAdmin } from './tipos'

export const indicadores = () => api<Indicadores>('/api/admin/indicadores')
export const listarContas = () => api<UsuarioAdmin[]>('/api/admin/usuarios')
export const excluirConta = (id: number) => api<void>(`/api/admin/usuarios/${id}`, { method: 'DELETE' })
