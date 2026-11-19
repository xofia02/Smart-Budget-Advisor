import { useEffect, useState, type FormEvent } from 'react'
import { excluirMinhaConta } from '../api/auth'
import { msgErro } from '../api/client'
import {
  atualizarAnterior,
  atualizarEstilo,
  buscarPerfil,
  criarAnterior,
  excluirAnterior,
  listarAnteriores,
} from '../api/perfil'
import type { EstiloViagem, Perfil, ViagemAnterior } from '../api/tipos'
import { useAuth } from '../auth/AuthContext'
import { dataBr, ESTILOS, reais, ROTULO_ESTILO } from '../lib/formatar'

const vazio = { destino: '', dataInicio: '', dataFim: '', valorTotal: '' }

export function PaginaPerfil() {
  const { sair } = useAuth()
  const [perfil, setPerfil] = useState<Perfil | null>(null)
  const [anteriores, setAnteriores] = useState<ViagemAnterior[]>([])
  const [form, setForm] = useState(vazio)
  const [editando, setEditando] = useState<number | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function carregar() {
    const [p, h] = await Promise.all([buscarPerfil(), listarAnteriores()])
    setPerfil(p)
    setAnteriores(h)
  }

  useEffect(() => {
    carregar().catch((e) => setErro(msgErro(e)))
  }, [])

  async function salvarEstilo(estilo: EstiloViagem) {
    setErro(null)
    setBusy(true)
    try {
      setPerfil(await atualizarEstilo(estilo))
      setAviso('Estilo atualizado.')
    } catch (e) {
      setErro(msgErro(e))
    } finally {
      setBusy(false)
    }
  }

  async function salvarAnterior(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    setAviso(null)
    const dados = {
      destino: form.destino.trim(),
      dataInicio: form.dataInicio,
      dataFim: form.dataFim,
      valorTotal: Number(form.valorTotal.replace(',', '.')),
    }
    setBusy(true)
    try {
      if (editando) await atualizarAnterior(editando, dados)
      else await criarAnterior(dados)
      setForm(vazio)
      setEditando(null)
      await carregar()
      setAviso('Histórico salvo. Com 2 viagens anteriores a previsão da IA fica personalizada.')
    } catch (e) {
      setErro(msgErro(e))
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="painel">
      <h1>Perfil</h1>
      <p className="muted">{perfil?.email}</p>
      {erro ? <p className="alerta erro">{erro}</p> : null}
      {aviso ? <p className="alerta sucesso">{aviso}</p> : null}

      <section className="cartao">
        <h2>Estilo de viagem</h2>
        <div className="duas-colunas">
          {ESTILOS.map((estilo) => (
            <button
              key={estilo}
              type="button"
              className={perfil?.estilo === estilo ? undefined : 'ghost'}
              disabled={busy}
              onClick={() => void salvarEstilo(estilo)}
            >
              {ROTULO_ESTILO[estilo]}
            </button>
          ))}
        </div>
      </section>

      <section className="cartao">
        <h2>Viagens anteriores ({anteriores.length})</h2>
        <p className="muted">Cadastre ao menos 2, já encerradas, para previsão personalizada.</p>
        <ul>
          {anteriores.map((v) => (
            <li key={v.id}>
              <span>
                {v.destino} · {dataBr(v.dataInicio)} — {dataBr(v.dataFim)}
              </span>
              <strong>
                {reais(v.valorTotal)}{' '}
                <button
                  type="button"
                  className="link"
                  onClick={() => {
                    setEditando(v.id)
                    setForm({
                      destino: v.destino,
                      dataInicio: v.dataInicio,
                      dataFim: v.dataFim,
                      valorTotal: String(v.valorTotal),
                    })
                  }}
                >
                  editar
                </button>{' '}
                <button
                  type="button"
                  className="link"
                  disabled={busy}
                  onClick={() => {
                    if (!window.confirm(`Excluir ${v.destino}?`)) return
                    void excluirAnterior(v.id)
                      .then(carregar)
                      .catch((e) => setErro(msgErro(e)))
                  }}
                >
                  excluir
                </button>
              </strong>
            </li>
          ))}
        </ul>

        <form className="form" onSubmit={salvarAnterior}>
          <h3>{editando ? 'Editar viagem' : 'Nova viagem anterior'}</h3>
          <label>
            Destino
            <input required maxLength={120} value={form.destino} onChange={(e) => setForm({ ...form, destino: e.target.value })} />
          </label>
          <div className="duas-colunas">
            <label>
              Início
              <input type="date" required value={form.dataInicio} onChange={(e) => setForm({ ...form, dataInicio: e.target.value })} />
            </label>
            <label>
              Término
              <input type="date" required value={form.dataFim} onChange={(e) => setForm({ ...form, dataFim: e.target.value })} />
            </label>
          </div>
          <label>
            Valor gasto (R$)
            <input type="number" min={0.01} step="0.01" required value={form.valorTotal} onChange={(e) => setForm({ ...form, valorTotal: e.target.value })} />
          </label>
          <button type="submit" disabled={busy}>{busy ? 'Salvando…' : 'Salvar'}</button>
          {editando ? (
            <button type="button" className="ghost" onClick={() => { setEditando(null); setForm(vazio) }}>
              Cancelar
            </button>
          ) : null}
        </form>
      </section>

      <section className="cartao">
        <h2>Excluir conta</h2>
        <p className="muted">Apaga a conta e todos os dados (LGPD). Não dá para desfazer.</p>
        <button
          type="button"
          className="ghost perigo"
          disabled={busy}
          onClick={() => {
            if (!window.confirm('Excluir sua conta e todos os dados?')) return
            setBusy(true)
            void excluirMinhaConta()
              .then(() => sair())
              .catch((e) => {
                setErro(msgErro(e))
                setBusy(false)
              })
          }}
        >
          Excluir minha conta
        </button>
      </section>
    </main>
  )
}
