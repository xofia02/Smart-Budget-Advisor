import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { excluirConta, indicadores, listarContas } from '../api/admin'
import { msgErro } from '../api/client'
import type { Indicadores, UsuarioAdmin } from '../api/tipos'
import { useAuth } from '../auth/AuthContext'

const ROTULOS: { chave: keyof Indicadores; label: string }[] = [
  { chave: 'totalUsuarios', label: 'Usuários' },
  { chave: 'totalViajantes', label: 'Viajantes' },
  { chave: 'totalViagens', label: 'Viagens' },
  { chave: 'viagensEmAndamento', label: 'Em andamento' },
  { chave: 'previsoesGeradas', label: 'Previsões' },
  { chave: 'mensagensNoChat', label: 'Msgs no chat' },
]

export function Admin() {
  const { usuario } = useAuth()
  const [dados, setDados] = useState<Indicadores | null>(null)
  const [contas, setContas] = useState<UsuarioAdmin[]>([])
  const [erro, setErro] = useState<string | null>(null)

  async function carregar() {
    const [i, c] = await Promise.all([indicadores(), listarContas()])
    setDados(i)
    setContas(c)
  }

  useEffect(() => {
    if (usuario?.tipo !== 'ADMIN') return
    carregar().catch((e) => setErro(msgErro(e)))
  }, [usuario])

  if (usuario?.tipo !== 'ADMIN') {
    return <Navigate to="/viagens" replace />
  }

  return (
    <main className="painel largo">
      <h1>Administração</h1>
      <p className="muted">Indicadores anônimos e contas. Dados financeiros dos viajantes não aparecem aqui.</p>
      {erro ? <p className="alerta erro">{erro}</p> : null}

      {dados ? (
        <div className="grade-viagens">
          {ROTULOS.map((r) => (
            <section key={r.chave} className="cartao">
              <h2>{r.label}</h2>
              <p className="numero">{dados[r.chave]}</p>
            </section>
          ))}
        </div>
      ) : null}

      <section className="cartao">
        <h2>Contas</h2>
        <ul>
          {contas.map((c) => (
            <li key={c.id}>
              <span>
                {c.nome} · {c.email} · {c.tipo}
              </span>
              {c.tipo === 'VIAJANTE' ? (
                <button
                  type="button"
                  className="link"
                  onClick={() => {
                    if (!window.confirm(`Excluir a conta de ${c.nome}?`)) return
                    void excluirConta(c.id)
                      .then(carregar)
                      .catch((e) => setErro(msgErro(e)))
                  }}
                >
                  excluir
                </button>
              ) : (
                <strong>admin</strong>
              )}
            </li>
          ))}
        </ul>
      </section>
    </main>
  )
}
