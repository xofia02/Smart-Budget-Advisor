import { api } from './client'
import type {
  Alerta,
  CategoriaGasto,
  Despesa,
  Previsao,
  Dica,
  Mensagem,
  Relatorio,
  ViagemDetalhe,
  ViagemRequest,
  ViagemResumo,
} from './tipos'

const v = (id: number) => `/api/viagens/${id}`

export const listarViagens = () => api<ViagemResumo[]>('/api/viagens')
export const buscarViagem = (id: number) => api<ViagemDetalhe>(v(id))
export const criarViagem = (dados: ViagemRequest) =>
  api<ViagemDetalhe>('/api/viagens', { method: 'POST', body: dados })
export const atualizarViagem = (id: number, dados: ViagemRequest) =>
  api<ViagemDetalhe>(v(id), { method: 'PUT', body: dados })
export const excluirViagem = (id: number) => api<void>(v(id), { method: 'DELETE' })

export const gerarPrevisao = (id: number) => api<Previsao>(`${v(id)}/previsao`, { method: 'POST' })
export const buscarPrevisao = (id: number) => api<Previsao>(`${v(id)}/previsao`)

export const distribuirOrcamento = (id: number) =>
  api<ViagemDetalhe>(`${v(id)}/orcamento/distribuir`, { method: 'POST' })
export const ajustarOrcamento = (id: number, categorias: { categoria: CategoriaGasto; valorAlocado: number }[]) =>
  api<ViagemDetalhe>(`${v(id)}/orcamento`, { method: 'PUT', body: { categorias } })

type DespesaPayload = { valor: number; categoria: CategoriaGasto; data: string; descricao?: string }

export function listarDespesas(
  id: number,
  filtro?: { categoria?: CategoriaGasto; inicio?: string; fim?: string },
) {
  const q = new URLSearchParams()
  if (filtro?.categoria) q.set('categoria', filtro.categoria)
  if (filtro?.inicio) q.set('inicio', filtro.inicio)
  if (filtro?.fim) q.set('fim', filtro.fim)
  const qs = q.toString()
  return api<Despesa[]>(`${v(id)}/despesas${qs ? `?${qs}` : ''}`)
}
export const criarDespesa = (id: number, dados: DespesaPayload) =>
  api<Despesa>(`${v(id)}/despesas`, { method: 'POST', body: dados })
export const atualizarDespesa = (id: number, despesaId: number, dados: DespesaPayload) =>
  api<Despesa>(`${v(id)}/despesas/${despesaId}`, { method: 'PUT', body: dados })
export const excluirDespesa = (id: number, despesaId: number) =>
  api<void>(`${v(id)}/despesas/${despesaId}`, { method: 'DELETE' })

export const listarAlertas = (id: number) => api<Alerta[]>(`${v(id)}/alertas`)
export const buscarRelatorio = (id: number) => api<Relatorio>(`${v(id)}/relatorio`)

export const listarDicas = (id: number) => api<Dica[]>(`${v(id)}/dicas`)
export const gerarDicas = (id: number) => api<Dica[]>(`${v(id)}/dicas`, { method: 'POST' })
export const historicoChat = (id: number) => api<Mensagem[]>(`${v(id)}/chat`)
export const perguntarChat = (id: number, pergunta: string) =>
  api<{ pergunta: Mensagem; resposta: Mensagem }>(`${v(id)}/chat`, { method: 'POST', body: { pergunta } })
