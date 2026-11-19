import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { Layout } from './components/Layout'
import { destinoInicial } from './lib/formatar'
import { Cadastro } from './pages/Cadastro'
import { Inicio } from './pages/Inicio'
import { Login } from './pages/Login'
import { Admin } from './pages/Admin'
import { PaginaPerfil } from './pages/Perfil'
import { NovaViagem } from './pages/NovaViagem'
import { Viagem } from './pages/Viagem'
import { Viagens } from './pages/Viagens'
import { Protegida } from './rotas/Protegida'

function LoginOuRedireciona() {
  const { usuario } = useAuth()
  if (usuario) {
    return <Navigate to={destinoInicial(usuario.tipo)} replace />
  }
  return <Login />
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginOuRedireciona />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route
            element={
              <Protegida>
                <Layout />
              </Protegida>
            }
          >
            <Route path="/" element={<Inicio />} />
            <Route path="/viagens" element={<Viagens />} />
            <Route path="/viagens/nova" element={<NovaViagem />} />
            <Route path="/viagens/:id/editar" element={<NovaViagem />} />
            <Route path="/viagens/:id" element={<Viagem />} />
            <Route path="/perfil" element={<PaginaPerfil />} />
            <Route path="/admin" element={<Admin />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
