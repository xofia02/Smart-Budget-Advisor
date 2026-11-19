import { api } from './client'
import type { EstiloViagem, Perfil, ViagemAnterior } from './tipos'

export const buscarPerfil = () => api<Perfil>('/api/perfil')
export const atualizarEstilo = (estilo: EstiloViagem) =>
  api<Perfil>('/api/perfil', { method: 'PUT', body: { estilo } })

export const listarAnteriores = () => api<ViagemAnterior[]>('/api/perfil/viagens-anteriores')

type DadosAnterior = Omit<ViagemAnterior, 'id'>

export const criarAnterior = (dados: DadosAnterior) =>
  api<ViagemAnterior>('/api/perfil/viagens-anteriores', { method: 'POST', body: dados })
export const atualizarAnterior = (id: number, dados: DadosAnterior) =>
  api<ViagemAnterior>(`/api/perfil/viagens-anteriores/${id}`, { method: 'PUT', body: dados })
export const excluirAnterior = (id: number) =>
  api<void>(`/api/perfil/viagens-anteriores/${id}`, { method: 'DELETE' })
