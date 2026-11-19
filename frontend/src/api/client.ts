import { lerToken, salvarToken } from './storage'
import type { ErroApi } from './tipos'

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  status: number
  mensagens: string[]

  constructor(status: number, mensagens: string[], erro?: string) {
    super(mensagens[0] ?? erro ?? 'Não foi possível concluir o pedido.')
    this.name = 'ApiError'
    this.status = status
    this.mensagens = mensagens.length > 0 ? mensagens : [this.message]
  }
}

type Opcoes = {
  method?: string
  body?: unknown
  autenticado?: boolean
}

export async function api<T>(caminho: string, opcoes: Opcoes = {}): Promise<T> {
  const { method = 'GET', body, autenticado = true } = opcoes
  const cabecalhos = new Headers()

  if (body !== undefined) {
    cabecalhos.set('Content-Type', 'application/json')
  }

  if (autenticado) {
    const token = lerToken()
    if (token) {
      cabecalhos.set('Authorization', `Bearer ${token}`)
    }
  }

  let resposta: Response
  try {
    resposta = await fetch(`${BASE}${caminho}`, {
      method,
      headers: cabecalhos,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, [
      'Não foi possível conectar à API. Confira se o backend está em http://localhost:8080.',
    ])
  }

  const renovado = resposta.headers.get('X-Token-Renovado')
  if (renovado) {
    salvarToken(renovado)
  }

  if (resposta.status === 204) {
    return undefined as T
  }

  const texto = await resposta.text()
  const dados = texto ? (JSON.parse(texto) as unknown) : null

  if (!resposta.ok) {
    const erro = dados as ErroApi | null
    throw new ApiError(
      resposta.status,
      erro?.mensagens?.length ? erro.mensagens : ['Não foi possível concluir o pedido.'],
      erro?.erro,
    )
  }

  return dados as T
}

export function msgErro(e: unknown): string {
  return e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível concluir o pedido.'
}
