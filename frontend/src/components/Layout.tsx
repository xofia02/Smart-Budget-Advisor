import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useState } from 'react'

export function Layout() {
  const { usuario, sair } = useAuth()
  const [saindo, setSaindo] = useState(false)

  async function encerrar() {
    setSaindo(true)
    await sair()
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <strong>Smart Budget Advisor</strong>
        <nav className="topbar-nav">
          {usuario?.tipo === 'ADMIN' ? (
            <NavLink to="/admin">Admin</NavLink>
          ) : (
            <>
              <NavLink to="/" end>
                Início
              </NavLink>
              <NavLink to="/viagens" end>
                Viagens
              </NavLink>
              <NavLink to="/viagens/nova">Nova viagem</NavLink>
              <NavLink to="/perfil">Perfil</NavLink>
            </>
          )}
        </nav>
        <div className="topbar-user">
          <span>
            {usuario?.nome} · {usuario?.tipo}
          </span>
          <button type="button" className="ghost" onClick={encerrar} disabled={saindo}>
            {saindo ? 'Saindo…' : 'Sair'}
          </button>
        </div>
      </header>
      <Outlet />
    </div>
  )
}
