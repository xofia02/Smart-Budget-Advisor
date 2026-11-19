import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function Protegida({ children }: { children: ReactNode }) {
  const { usuario } = useAuth()
  if (!usuario) {
    return <Navigate to="/login" replace />
  }
  return children
}
