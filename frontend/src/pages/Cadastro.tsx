import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { cadastrar } from '../api/auth'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { destinoInicial } from '../lib/formatar'

export function Cadastro() {
  const { usuario } = useAuth()
  const navigate = useNavigate()

  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [lgpd, setLgpd] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (usuario) {
    return <Navigate to={destinoInicial(usuario.tipo)} replace />
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro(null)

    if (!lgpd) {
      setErro('É preciso aceitar os termos de uso de dados (LGPD) para criar a conta.')
      return
    }
    if (senha.length < 8) {
      setErro('A senha deve ter entre 8 e 72 caracteres.')
      return
    }

    setEnviando(true)
    try {
      await cadastrar({
        nome: nome.trim(),
        email: email.trim(),
        senha,
        consentimentoLGPD: true,
      })
      navigate('/login', {
        replace: true,
        state: { aviso: 'Conta criada. Faça login para continuar.' },
      })
    } catch (e) {
      setErro(e instanceof ApiError ? e.mensagens.join(' ') : 'Não foi possível cadastrar.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-page">
      <aside className="auth-brand">
        <p className="eyebrow">Nova conta</p>
        <h1>Comece pelo cadastro</h1>
        <p>
          O cadastro cria um perfil de viajante. Depois do login você poderá informar o
          estilo de viagem e montar o orçamento.
        </p>
      </aside>

      <main className="auth-card">
        <h2>Criar conta</h2>
        {erro ? <p className="alerta erro">{erro}</p> : null}

        <form onSubmit={enviar} className="form">
          <label>
            Nome
            <input
              type="text"
              maxLength={120}
              autoComplete="name"
              required
              value={nome}
              onChange={(e) => setNome(e.target.value)}
            />
          </label>
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
              autoComplete="new-password"
              minLength={8}
              maxLength={72}
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </label>
          <label className="check">
            <input
              type="checkbox"
              checked={lgpd}
              onChange={(e) => setLgpd(e.target.checked)}
            />
            Aceito os termos de uso dos meus dados (LGPD).
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? 'Criando…' : 'Cadastrar'}
          </button>
        </form>

        <p className="rodape-auth">
          Já tem conta? <Link to="/login">Entrar</Link>
        </p>
      </main>
    </div>
  )
}
