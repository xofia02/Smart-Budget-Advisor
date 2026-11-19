import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { destinoInicial } from '../lib/formatar'

export function Login() {
  const { usuario, entrar } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const aviso = (location.state as { aviso?: string } | null)?.aviso

  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) {
    return <Navigate to={destinoInicial(usuario.tipo)} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      const logado = await entrar(email.trim(), senha)
      navigate(destinoInicial(logado.tipo), { replace: true })
    } catch (e) {
      setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível entrar.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-page">
      <aside className="auth-brand">
        <p className="eyebrow">UCSAL · Tópicos Avançados</p>
        <h1>Smart Budget Advisor</h1>
        <p>
          Planeje a viagem, divida o orçamento e acompanhe os gastos com apoio de
          inteligência artificial.
        </p>
      </aside>

      <main className="auth-card">
        <h2>Entrar</h2>
        <p className="muted">Use a conta demo ou a que você cadastrou.</p>

        {aviso ? <p className="alerta sucesso">{aviso}</p> : null}
        {erro ? <p className="alerta erro">{erro}</p> : null}

        <form onSubmit={enviar} className="form">
          <label>
            E-mail
            <input
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>
          <label>
            Senha
            <input
              type="password"
              autoComplete="current-password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <p className="dica">
          Demo viajante: <code>ana@demo.com</code> / <code>demo12345</code>
          <br />
          Admin: <code>admin@smartbudget.com</code> / <code>admin12345</code>
        </p>

        <p className="rodape-auth">
          Ainda não tem conta? <Link to="/cadastro">Criar conta</Link>
        </p>
      </main>
    </div>
  )
}
