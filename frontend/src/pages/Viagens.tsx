import { useEffect, useState, type MouseEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import type { ViagemResumo } from '../api/tipos'
import { excluirViagem, listarViagens } from '../api/viagens'
import { dataBr, reais, ROTULO_TIPO } from '../lib/formatar'

export function Viagens() {
  const navigate = useNavigate()
  const [lista, setLista] = useState<ViagemResumo[]>([])
  const [erro, setErro] = useState<string | null>(null)
  const [carregando, setCarregando] = useState(true)

  async function carregar() {
    setErro(null)
    setCarregando(true)
    try {
      setLista(await listarViagens())
    } catch (e) {
      setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível listar as viagens.')
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    void carregar()
  }, [])

  async function remover(evento: MouseEvent, viagem: ViagemResumo) {
    evento.preventDefault()
    evento.stopPropagation()
    if (!window.confirm(`Excluir a viagem para ${viagem.destino}?`)) {
      return
    }
    try {
      await excluirViagem(viagem.id)
      setLista((atual) => atual.filter((v) => v.id !== viagem.id))
    } catch (e) {
      setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível excluir.')
    }
  }

  return (
    <main className="painel largo">
      <div className="titulo-acao">
        <div>
          <h1>Minhas viagens</h1>
          <p className="muted">Crie uma viagem para gerar previsão e controlar os gastos.</p>
        </div>
        <Link to="/viagens/nova" className="botao">
          Nova viagem
        </Link>
      </div>

      {erro ? <p className="alerta erro">{erro}</p> : null}
      {carregando ? <p className="muted">Carregando…</p> : null}

      {!carregando && lista.length === 0 && !erro ? (
        <section className="cartao">
          <p>Você ainda não cadastrou nenhuma viagem.</p>
        </section>
      ) : null}

      <div className="grade-viagens">
        {lista.map((viagem) => (
          <article
            key={viagem.id}
            className="cartao viagem-card"
            role="link"
            tabIndex={0}
            onClick={() => navigate(`/viagens/${viagem.id}`)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') navigate(`/viagens/${viagem.id}`)
            }}
          >
            <div className="viagem-card-topo">
              <h2>{viagem.destino}</h2>
              {viagem.bloqueada ? <span className="badge perigo">Bloqueada</span> : null}
            </div>
            <p className="muted">
              {dataBr(viagem.dataInicio)} — {dataBr(viagem.dataFim)} · {ROTULO_TIPO[viagem.tipo]}
            </p>
            <ul>
              <li>
                <span>Orçamento</span>
                <strong>{reais(viagem.orcamentoTotal)}</strong>
              </li>
              <li>
                <span>Gasto</span>
                <strong>{reais(viagem.totalGasto)}</strong>
              </li>
              <li>
                <span>Consumido</span>
                <strong>{Number(viagem.percentualConsumido).toFixed(0)}%</strong>
              </li>
            </ul>
            <button
              type="button"
              className="ghost"
              onClick={(e) => {
                e.preventDefault()
                e.stopPropagation()
                navigate(`/viagens/${viagem.id}/editar`)
              }}
            >
              Editar
            </button>
            <button type="button" className="ghost perigo" onClick={(e) => void remover(e, viagem)}>
              Excluir
            </button>
          </article>
        ))}
      </div>
    </main>
  )
}
