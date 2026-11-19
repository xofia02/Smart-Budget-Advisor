import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { buscarStatus } from '../api/auth'
import { ApiError } from '../api/client'
import type { StatusApi } from '../api/tipos'
import { useAuth } from '../auth/AuthContext'

export function Inicio() {
  const { usuario } = useAuth()
  const [status, setStatus] = useState<StatusApi | null>(null)
  const [erroStatus, setErroStatus] = useState<string | null>(null)

  useEffect(() => {
    buscarStatus()
      .then(setStatus)
      .catch((e) => {
        setErroStatus(e instanceof ApiError ? e.mensagens.join(' ') : 'API indisponível.')
      })
  }, [])

  if (usuario?.tipo === 'ADMIN') {
    return <Navigate to="/admin" replace />
  }

  const nome = usuario?.nome.split(' ')[0]

  return (
    <main className="painel">
      <h1>Olá, {nome}</h1>
      <p className="muted">Planeje a viagem, divida o orçamento e acompanhe os gastos com apoio da IA.</p>
      <p className="acoes-inicio">
        <Link to="/viagens" className="botao">
          Minhas viagens
        </Link>
        <Link to="/viagens/nova" className="ghost">
          Nova viagem
        </Link>
        <Link to="/perfil" className="ghost">
          Perfil
        </Link>
      </p>

      <section className="cartao">
        <h2>Sistema</h2>
        {erroStatus ? <p className="alerta erro">{erroStatus}</p> : null}
        {status ? (
          <>
            <ul>
              <li>
                <span>API</span>
                <strong>{status.api}</strong>
              </li>
              <li>
                <span>Banco de dados</span>
                <strong>{status.banco}</strong>
              </li>
              <li>
                <span>Assistente de IA</span>
                <strong>{status.iaConfigurada ? 'Disponível' : 'Indisponível'}</strong>
              </li>
            </ul>
            {status.iaConfigurada && status.modeloIa ? (
              <p className="muted">Modelo: {status.modeloIa}</p>
            ) : null}
          </>
        ) : !erroStatus ? (
          <p className="muted">Consultando…</p>
        ) : null}
      </section>
    </main>
  )
}
