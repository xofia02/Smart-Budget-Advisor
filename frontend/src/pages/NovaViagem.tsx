import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ApiError } from '../api/client'
import type { TipoViagem } from '../api/tipos'
import { atualizarViagem, buscarViagem, criarViagem } from '../api/viagens'

function diasEntre(inicio: string, fim: string): number {
  const a = new Date(`${inicio}T00:00:00`)
  const b = new Date(`${fim}T00:00:00`)
  return Math.round((b.getTime() - a.getTime()) / 86_400_000) + 1
}

export function NovaViagem() {
  const navigate = useNavigate()
  const id = Number(useParams().id)
  const editando = Number.isFinite(id) && id > 0

  const [destino, setDestino] = useState('')
  const [dataInicio, setDataInicio] = useState('')
  const [dataFim, setDataFim] = useState('')
  const [orcamento, setOrcamento] = useState('500.00')
  const [tipo, setTipo] = useState<TipoViagem>('INDIVIDUAL')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const [carregando, setCarregando] = useState(editando)

  useEffect(() => {
    if (!editando) return
    buscarViagem(id)
      .then((v) => {
        setDestino(v.destino)
        setDataInicio(v.dataInicio)
        setDataFim(v.dataFim)
        setOrcamento(String(v.orcamentoTotal))
        setTipo(v.tipo)
      })
      .catch((e) => setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível carregar a viagem.'))
      .finally(() => setCarregando(false))
  }, [editando, id])

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)

    const total = Number(orcamento.replace(',', '.'))
    if (Number.isNaN(total) || total < 500) {
      setErro('O orçamento total deve ser de pelo menos R$ 500,00.')
      return
    }
    if (!dataInicio || !dataFim) {
      setErro('Informe a data de início e a data de término.')
      return
    }
    const duracao = diasEntre(dataInicio, dataFim)
    if (duracao < 2 || duracao > 365) {
      setErro('A viagem deve durar entre 2 e 365 dias.')
      return
    }

    const dados = {
      destino: destino.trim(),
      dataInicio,
      dataFim,
      orcamentoTotal: total,
      tipo,
    }

    setEnviando(true)
    try {
      const salva = editando ? await atualizarViagem(id, dados) : await criarViagem(dados)
      navigate(`/viagens/${salva.id}`, { replace: true })
    } catch (e) {
      setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível salvar a viagem.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className="painel">
      <p className="voltar">
        <Link to={editando ? `/viagens/${id}` : '/viagens'}>
          {editando ? '← Voltar à viagem' : '← Minhas viagens'}
        </Link>
      </p>
      <h1>{editando ? 'Editar viagem' : 'Nova viagem'}</h1>
      <p className="muted">Orçamento mínimo de R$ 500,00 e duração entre 2 e 365 dias.</p>

      {erro ? <p className="alerta erro">{erro}</p> : null}
      {carregando ? <p className="muted">Carregando…</p> : null}

      {!carregando ? (
        <form onSubmit={enviar} className="form cartao">
          <label>
            Destino
            <input
              type="text"
              maxLength={120}
              required
              value={destino}
              onChange={(e) => setDestino(e.target.value)}
            />
          </label>
          <div className="duas-colunas">
            <label>
              Início
              <input type="date" required value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
            </label>
            <label>
              Término
              <input type="date" required value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
            </label>
          </div>
          <label>
            Orçamento total (R$)
            <input
              type="number"
              min={500}
              step="0.01"
              required
              value={orcamento}
              onChange={(e) => setOrcamento(e.target.value)}
            />
          </label>
          <label>
            Tipo
            <select value={tipo} onChange={(e) => setTipo(e.target.value as TipoViagem)}>
              <option value="INDIVIDUAL">Individual</option>
              <option value="CASAL">Casal</option>
              <option value="FAMILIA">Família</option>
            </select>
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Criar viagem'}
          </button>
        </form>
      ) : null}
    </main>
  )
}
