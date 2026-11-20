import { useCallback, useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError, msgErro } from '../api/client'
import type { Alerta, CategoriaGasto, Despesa, Dica, Mensagem, Previsao, Relatorio, ViagemDetalhe } from '../api/tipos'
import {
  ajustarOrcamento,
  buscarPrevisao,
  buscarViagem,
  atualizarDespesa,
  criarDespesa,
  distribuirOrcamento,
  excluirDespesa,
  gerarPrevisao,
  buscarRelatorio,
  gerarDicas,
  historicoChat,
  listarAlertas,
  listarDicas,
  listarDespesas,
  perguntarChat,
} from '../api/viagens'
import { CATEGORIAS, centavos, dataBr, reais, ROTULO_ALERTA, ROTULO_CATEGORIA, ROTULO_TIPO } from '../lib/formatar'

export function Viagem() {
  const id = Number(useParams().id)
  const [viagem, setViagem] = useState<ViagemDetalhe | null>(null)
  const [previsao, setPrevisao] = useState<Previsao | null>(null)
  const [despesas, setDespesas] = useState<Despesa[]>([])
  const [alertas, setAlertas] = useState<Alerta[]>([])
  const [relatorio, setRelatorio] = useState<Relatorio | null>(null)
  const [dicas, setDicas] = useState<Dica[]>([])
  const [chat, setChat] = useState<Mensagem[]>([])
  const [pergunta, setPergunta] = useState('')
  const [alocacao, setAlocacao] = useState<Record<CategoriaGasto, string>>({} as Record<CategoriaGasto, string>)
  const [erro, setErro] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [busy, setBusy] = useState<string | null>(null)

  const [valor, setValor] = useState('')
  const [categoria, setCategoria] = useState<CategoriaGasto>('ALIMENTACAO')
  const [data, setData] = useState('')
  const [descricao, setDescricao] = useState('')
  const [editandoDespesa, setEditandoDespesa] = useState<number | null>(null)
  const [filtroCategoria, setFiltroCategoria] = useState<CategoriaGasto | ''>('')
  const [filtroInicio, setFiltroInicio] = useState('')
  const [filtroFim, setFiltroFim] = useState('')
  const [filtroAtivo, setFiltroAtivo] = useState<{ categoria?: CategoriaGasto; inicio?: string; fim?: string }>({})

  const carregar = useCallback(async () => {
    if (!id) return
    const [v, d, a, r, di, ch] = await Promise.all([
      buscarViagem(id),
      listarDespesas(id, filtroAtivo),
      listarAlertas(id),
      buscarRelatorio(id),
      listarDicas(id),
      historicoChat(id),
    ])
    setViagem(v)
    setDespesas(d)
    setAlertas(a)
    setRelatorio(r)
    setDicas(di)
    setChat(ch)
    setData((atual) => atual || v.dataInicio)
    setAlocacao(
      Object.fromEntries(
        v.categorias.map((c) => [c.categoria, (centavos(c.valorAlocado) / 100).toFixed(2)]),
      ) as Record<CategoriaGasto, string>,
    )
    try {
      setPrevisao(await buscarPrevisao(id))
    } catch (e) {
      if (!(e instanceof ApiError && e.status === 404)) throw e
      setPrevisao(null)
    }
  }, [id, filtroAtivo])

  useEffect(() => {
    carregar().catch((e) => setErro(msgErro(e)))
  }, [carregar])

  const somaCentavos = useMemo(
    () => CATEGORIAS.reduce((s, c) => s + centavos(alocacao[c]), 0),
    [alocacao],
  )

  async function agir(nome: string, fn: () => Promise<void>) {
    setErro(null)
    setAviso(null)
    setBusy(nome)
    try {
      await fn()
      await carregar()
    } catch (e) {
      setErro(msgErro(e))
    } finally {
      setBusy(null)
    }
  }

  if (!viagem && !erro) return <main className="painel"><p className="muted">Carregando…</p></main>
  if (!viagem) {
    return (
      <main className="painel">
        <p className="voltar"><Link to="/viagens">← Minhas viagens</Link></p>
        <p className="alerta erro">{erro}</p>
      </main>
    )
  }

  return (
    <main className="painel">
      <p className="voltar"><Link to="/viagens">← Minhas viagens</Link></p>
      <div className="titulo-acao">
        <div>
          <h1>{viagem.destino}</h1>
          <p className="muted">
            {dataBr(viagem.dataInicio)} — {dataBr(viagem.dataFim)} · {viagem.duracaoEmDias} dias ·{' '}
            {ROTULO_TIPO[viagem.tipo]}
          </p>
        </div>
        <div>
          <Link to={`/viagens/${viagem.id}/editar`} className="ghost">Editar</Link>
          {viagem.bloqueada ? <span className="badge perigo">Bloqueada</span> : null}
        </div>
      </div>

      {erro ? <p className="alerta erro">{erro}</p> : null}
      {aviso ? <p className="alerta sucesso">{aviso}</p> : null}

      <section className="cartao">
        <h2>Resumo</h2>
        <ul>
          <li><span>Orçamento</span><strong>{reais(viagem.orcamentoTotal)}</strong></li>
          <li><span>Alocado</span><strong>{reais(viagem.totalAlocado)}</strong></li>
          <li><span>Gasto</span><strong>{reais(viagem.totalGasto)}</strong></li>
          <li><span>Consumido</span><strong>{Number(viagem.percentualConsumido).toFixed(0)}%</strong></li>
        </ul>
      </section>

      <section className="cartao">
        <div className="titulo-acao">
          <h2>Previsão da IA</h2>
          <button type="button" disabled={!!busy} onClick={() => agir('previsao', async () => {
            const p = await gerarPrevisao(id)
            setPrevisao(p)
            if (p.aviso) setAviso(p.aviso)
          })}>
            {busy === 'previsao' ? 'Gerando…' : 'Gerar previsão'}
          </button>
        </div>
        {previsao ? (
          <>
            {!previsao.personalizada && previsao.aviso ? <p className="muted">{previsao.aviso}</p> : null}
            <ul>
              {previsao.itens.map((i) => (
                <li key={i.categoria}>
                  <span>{ROTULO_CATEGORIA[i.categoria]}</span>
                  <strong>{reais(i.minimo)} · {reais(i.recomendado)} · {reais(i.maximo)}</strong>
                </li>
              ))}
            </ul>
          </>
        ) : (
          <p className="muted">Ainda não há previsão. Gere uma para sugerir a divisão do orçamento.</p>
        )}
      </section>

      <section className="cartao">
        <div className="titulo-acao">
          <h2>Orçamento</h2>
          <button type="button" className="ghost" disabled={!!busy} onClick={() => agir('dist', async () => {
            await distribuirOrcamento(id)
            setAviso('Orçamento distribuído.')
          })}>
            {busy === 'dist' ? 'Distribuindo…' : 'Distribuir automaticamente'}
          </button>
        </div>
        <form
          className="form"
          onSubmit={(e: FormEvent) => {
            e.preventDefault()
            void agir('salvar', async () => {
              await ajustarOrcamento(
                id,
                CATEGORIAS.map((c) => ({ categoria: c, valorAlocado: centavos(alocacao[c]) / 100 })),
              )
              setAviso('Divisão salva.')
            })
          }}
        >
          {CATEGORIAS.map((c) => (
            <label key={c}>
              {ROTULO_CATEGORIA[c]}
              <input
                type="number"
                min={0}
                step="0.01"
                value={alocacao[c] ?? '0'}
                onChange={(e) => setAlocacao((a) => ({ ...a, [c]: e.target.value }))}
              />
            </label>
          ))}
          <p className={somaCentavos > centavos(viagem.orcamentoTotal) ? 'alerta erro' : 'muted'}>
            Soma {reais(somaCentavos / 100)} / total {reais(viagem.orcamentoTotal)}
          </p>
          <button type="submit" disabled={!!busy || somaCentavos > centavos(viagem.orcamentoTotal)}>
            {busy === 'salvar' ? 'Salvando…' : 'Salvar divisão'}
          </button>
        </form>
      </section>

      <section className="cartao">
        <h2>Despesas</h2>
        {viagem.bloqueada ? (
          <p className="alerta erro">
            Gastos passaram de 120% do orçamento. Revise a divisão, edite um lançamento ou aumente o total para lançar novas despesas.
          </p>
        ) : null}
        <form
          className="form"
          onSubmit={(e: FormEvent) => {
            e.preventDefault()
            void agir('despesa', async () => {
              const dados = {
                valor: Number(valor.replace(',', '.')),
                categoria,
                data,
                descricao: descricao.trim() || undefined,
              }
              const salva = editandoDespesa
                ? await atualizarDespesa(id, editandoDespesa, dados)
                : await criarDespesa(id, dados)
              setValor('')
              setDescricao('')
              setEditandoDespesa(null)
              if (salva.alertaGerado) {
                setAviso(`${ROTULO_ALERTA[salva.alertaGerado.tipo]} em ${ROTULO_CATEGORIA[salva.alertaGerado.categoria]}.`)
              } else if (editandoDespesa) {
                setAviso('Despesa atualizada.')
              }
            })
          }}
        >
          <div className="duas-colunas">
            <label>
              Valor
              <input type="number" min={0.01} step="0.01" required value={valor} onChange={(e) => setValor(e.target.value)} />
            </label>
            <label>
              Data
              <input type="date" required min={viagem.dataInicio} max={viagem.dataFim} value={data} onChange={(e) => setData(e.target.value)} />
            </label>
          </div>
          <label>
            Categoria
            <select value={categoria} onChange={(e) => setCategoria(e.target.value as CategoriaGasto)}>
              {CATEGORIAS.map((c) => (
                <option key={c} value={c}>{ROTULO_CATEGORIA[c]}</option>
              ))}
            </select>
          </label>
          <label>
            Descrição
            <input type="text" maxLength={255} value={descricao} onChange={(e) => setDescricao(e.target.value)} />
          </label>
          <button type="submit" disabled={!!busy || (viagem.bloqueada && !editandoDespesa)}>
            {busy === 'despesa' ? 'Salvando…' : editandoDespesa ? 'Salvar despesa' : 'Lançar despesa'}
          </button>
          {editandoDespesa ? (
            <button
              type="button"
              className="ghost"
              disabled={!!busy}
              onClick={() => {
                setEditandoDespesa(null)
                setValor('')
                setDescricao('')
              }}
            >
              Cancelar
            </button>
          ) : null}
        </form>
        <form
          className="form"
          onSubmit={(e: FormEvent) => {
            e.preventDefault()
            setFiltroAtivo({
              categoria: filtroCategoria || undefined,
              inicio: filtroInicio || undefined,
              fim: filtroFim || undefined,
            })
          }}
        >
          <div className="duas-colunas">
            <label>
              Filtrar categoria
              <select
                value={filtroCategoria}
                onChange={(e) => setFiltroCategoria(e.target.value as CategoriaGasto | '')}
              >
                <option value="">Todas</option>
                {CATEGORIAS.map((c) => (
                  <option key={c} value={c}>{ROTULO_CATEGORIA[c]}</option>
                ))}
              </select>
            </label>
            <label>
              De
              <input
                type="date"
                min={viagem.dataInicio}
                max={viagem.dataFim}
                value={filtroInicio}
                onChange={(e) => setFiltroInicio(e.target.value)}
              />
            </label>
            <label>
              Até
              <input
                type="date"
                min={viagem.dataInicio}
                max={viagem.dataFim}
                value={filtroFim}
                onChange={(e) => setFiltroFim(e.target.value)}
              />
            </label>
          </div>
          <button type="submit" disabled={!!busy}>Filtrar</button>
          <button
            type="button"
            className="ghost"
            disabled={!!busy}
            onClick={() => {
              setFiltroCategoria('')
              setFiltroInicio('')
              setFiltroFim('')
              setFiltroAtivo({})
            }}
          >
            Limpar
          </button>
        </form>
        <ul>
          {despesas.map((d) => (
            <li key={d.id}>
              <span>{dataBr(d.data)} · {ROTULO_CATEGORIA[d.categoria]}{d.descricao ? ` · ${d.descricao}` : ''}</span>
              <strong>
                {reais(d.valor)}{' '}
                <button
                  type="button"
                  className="link"
                  disabled={!!busy}
                  onClick={() => {
                    setEditandoDespesa(d.id)
                    setValor(String(d.valor))
                    setCategoria(d.categoria)
                    setData(d.data)
                    setDescricao(d.descricao ?? '')
                  }}
                >
                  editar
                </button>
                {' '}
                <button type="button" className="link" disabled={!!busy} onClick={() => agir('exc', () => excluirDespesa(id, d.id))}>
                  excluir
                </button>
              </strong>
            </li>
          ))}
        </ul>
        {despesas.length === 0 ? (
          <p className="muted">
            {filtroAtivo.categoria || filtroAtivo.inicio || filtroAtivo.fim
              ? 'Nenhuma despesa neste filtro.'
              : 'Nenhuma despesa ainda.'}
          </p>
        ) : null}
      </section>

      <section className="cartao">
        <h2>Alertas</h2>
        {alertas.length === 0 ? <p className="muted">Nenhum alerta.</p> : (
          <ul>
            {alertas.map((a) => (
              <li key={a.id}>
                <span>{ROTULO_ALERTA[a.tipo]} · {ROTULO_CATEGORIA[a.categoria]}</span>
                <strong>{a.tipo === 'EXCEDIDO_100' ? `+ ${reais(a.valorExcedido)}` : '80% usado'}</strong>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="cartao">
        <div className="titulo-acao">
          <h2>Dicas de economia</h2>
          <button type="button" disabled={!!busy} onClick={() => agir('dicas', async () => {
            setDicas(await gerarDicas(id))
          })}>
            {busy === 'dicas' ? 'Gerando…' : 'Gerar dicas'}
          </button>
        </div>
        {dicas.length === 0 ? <p className="muted">Gere dicas para o destino desta viagem.</p> : (
          <ul className="dicas">
            {dicas.map((d) => (
              <li key={d.id} className={d.alertaTurista ? 'turista' : undefined}>{d.texto}</li>
            ))}
          </ul>
        )}
      </section>

      <section className="cartao">
        <h2>Chat</h2>
        <div className="chat">
          {chat.length === 0 ? <p className="muted">Pergunte sobre gastos neste destino.</p> : null}
          {chat.map((m) => (
            <p key={m.id} className={m.papel === 'USUARIO' ? 'eu' : 'ia'}>{m.conteudo.replace(/\*\*(.+?)\*\*/g, '$1')}</p>
          ))}
        </div>
        <form
          className="form"
          onSubmit={(e: FormEvent) => {
            e.preventDefault()
            const texto = pergunta.trim()
            if (!texto) return
            void agir('chat', async () => {
              const par = await perguntarChat(id, texto)
              setChat((atual) => [...atual, par.pergunta, par.resposta])
              setPergunta('')
            })
          }}
        >
          <label>
            Pergunta
            <input maxLength={1000} value={pergunta} onChange={(e) => setPergunta(e.target.value)} required />
          </label>
          <button type="submit" disabled={!!busy}>{busy === 'chat' ? 'Pensando…' : 'Enviar'}</button>
        </form>
      </section>

      {relatorio ? <RelatorioBloco relatorio={relatorio} /> : null}
    </main>
  )
}

function RelatorioBloco({ relatorio }: { relatorio: Relatorio }) {
  const teto = Math.max(...relatorio.categorias.flatMap((c) => [Number(c.planejado), Number(c.gasto)]), 1)
  return (
    <section className="cartao">
      <h2>Relatório</h2>
      <ul>
        <li><span>Planejado</span><strong>{reais(relatorio.totalPlanejado)}</strong></li>
        <li><span>Gasto</span><strong>{reais(relatorio.totalGasto)}</strong></li>
        <li><span>Desvio</span><strong>{Number(relatorio.percentualDesvio).toFixed(1)}%</strong></li>
        <li><span>Status</span><strong>{relatorio.viagemEncerrada ? 'Encerrada' : 'Em andamento'}</strong></li>
      </ul>
      <div className="grafico">
        {relatorio.categorias.map((c) => (
          <div key={c.categoria} className="grafico-item">
            <span>{ROTULO_CATEGORIA[c.categoria]}</span>
            <div className="trilho">
              <i className="planejado" style={{ width: `${(Number(c.planejado) / teto) * 100}%` }} />
            </div>
            <div className="trilho">
              <i className="gasto" style={{ width: `${(Number(c.gasto) / teto) * 100}%` }} />
            </div>
            <small>
              {reais(c.planejado)} planejado · {reais(c.gasto)} gasto
            </small>
          </div>
        ))}
      </div>
      <p className="legenda"><i className="planejado" /> planejado · <i className="gasto" /> gasto</p>
    </section>
  )
}
