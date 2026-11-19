export type Usuario = {
  id: number
  nome: string
  email: string
  tipo: 'VIAJANTE' | 'ADMIN' | string
}

export type LoginResponse = {
  token: string
  expiraEmMinutos: number
  usuario: Usuario
}

export type ErroApi = {
  status: number
  erro: string
  mensagens: string[]
  horario?: string
}

export type StatusApi = {
  api: string
  banco: string
  iaConfigurada: boolean
  modeloIa?: string
}

export type TipoViagem = 'INDIVIDUAL' | 'CASAL' | 'FAMILIA'

export type CategoriaGasto =
  | 'HOSPEDAGEM'
  | 'ALIMENTACAO'
  | 'TRANSPORTE'
  | 'PASSEIOS'
  | 'COMPRAS'
  | 'OUTROS'

export type TipoAlerta = 'ATENCAO_80' | 'EXCEDIDO_100'

export type ViagemResumo = {
  id: number
  destino: string
  dataInicio: string
  dataFim: string
  tipo: TipoViagem
  orcamentoTotal: number
  totalGasto: number
  percentualConsumido: number
  bloqueada: boolean
}

export type CategoriaViagem = {
  id: number
  categoria: CategoriaGasto
  valorAlocado: number
  totalGasto: number
  percentualUsado: number
  valorExcedido: number
  alerta: TipoAlerta | null
}

export type ViagemDetalhe = ViagemResumo & {
  duracaoEmDias: number
  totalAlocado: number
  categorias: CategoriaViagem[]
}

export type ViagemRequest = {
  destino: string
  dataInicio: string
  dataFim: string
  orcamentoTotal: number
  tipo: TipoViagem
}

export type ItemPrevisao = {
  categoria: CategoriaGasto
  minimo: number
  recomendado: number
  maximo: number
}

export type Previsao = {
  id: number
  personalizada: boolean
  geradaEm: string
  aviso: string | null
  itens: ItemPrevisao[]
}

export type Despesa = {
  id: number
  valor: number
  categoria: CategoriaGasto
  data: string
  descricao: string | null
  alertaGerado: Alerta | null
}

export type Alerta = {
  id: number
  tipo: TipoAlerta
  categoria: CategoriaGasto
  valorExcedido: number
  geradoEm: string
}

export type EstiloViagem = 'ECONOMICO' | 'CONFORTAVEL' | 'LUXO'

export type Perfil = {
  id: number
  nome: string
  email: string
  estilo: EstiloViagem | null
  totalViagensAnteriores: number
}

export type ViagemAnterior = {
  id: number
  destino: string
  dataInicio: string
  dataFim: string
  valorTotal: number
}

export type Dica = { id: number; texto: string; alertaTurista: boolean }

export type Mensagem = { id: number; papel: string; conteudo: string; enviadaEm: string }

export type Indicadores = {
  totalUsuarios: number
  totalViajantes: number
  totalViagens: number
  viagensEmAndamento: number
  previsoesGeradas: number
  mensagensNoChat: number
}

export type UsuarioAdmin = {
  id: number
  nome: string
  email: string
  tipo: string
  criadoEm: string
}

export type Relatorio = {
  totalPlanejado: number
  totalGasto: number
  percentualDesvio: number
  viagemEncerrada: boolean
  geradoEm: string
  categorias: { categoria: CategoriaGasto; planejado: number; gasto: number; percentualDesvio: number }[]
}
