import type { CategoriaGasto, EstiloViagem, TipoAlerta, TipoViagem } from '../api/tipos'

export function centavos(valor: number | string | undefined): number {
  const n = Number(String(valor ?? '').replace(',', '.'))
  return Number.isFinite(n) ? Math.round(n * 100) : 0
}

export function reais(valor: number | string): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(centavos(valor) / 100)
}

export function dataBr(iso: string): string {
  const [ano, mes, dia] = iso.split('-')
  if (!ano || !mes || !dia) return iso
  return `${dia}/${mes}/${ano}`
}

export const ROTULO_TIPO: Record<TipoViagem, string> = {
  INDIVIDUAL: 'Individual',
  CASAL: 'Casal',
  FAMILIA: 'Família',
}

export const ROTULO_CATEGORIA: Record<CategoriaGasto, string> = {
  HOSPEDAGEM: 'Hospedagem',
  ALIMENTACAO: 'Alimentação',
  TRANSPORTE: 'Transporte',
  PASSEIOS: 'Passeios',
  COMPRAS: 'Compras',
  OUTROS: 'Outros',
}

export const CATEGORIAS = Object.keys(ROTULO_CATEGORIA) as CategoriaGasto[]

export const ROTULO_ALERTA: Record<TipoAlerta, string> = {
  ATENCAO_80: 'Atenção (80%)',
  EXCEDIDO_100: 'Excedido (100%)',
}

export const ROTULO_ESTILO: Record<EstiloViagem, string> = {
  ECONOMICO: 'Econômico',
  CONFORTAVEL: 'Confortável',
  LUXO: 'Luxo',
}

export const ESTILOS = Object.keys(ROTULO_ESTILO) as EstiloViagem[]

export function destinoInicial(tipo?: string): string {
  return tipo === 'ADMIN' ? '/admin' : '/viagens'
}
