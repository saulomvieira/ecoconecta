import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { AdminLayout } from './components/AdminLayout'
import { DashboardPage } from './pages/Dashboard'
import { Login } from './pages/Login'
import { PlanosPage } from './pages/Planos'
import { ResiduosPage } from './pages/Residuos'
import { TermosUsoPage } from './pages/TermosUso'
import { UsuariosPage } from './pages/Usuarios'

function RequireAdmin({ children }: { children: React.ReactNode }) {
  const { sessao } = useAuth()
  const location = useLocation()
  if (!sessao) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  if (sessao.perfil !== 'ADMIN') return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <AdminLayout />
              </RequireAdmin>
            }
          >
            <Route index element={<Navigate to="dashboard" replace />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="usuarios" element={<UsuariosPage />} />
            <Route path="residuos" element={<ResiduosPage />} />
            <Route path="planos" element={<PlanosPage />} />
            <Route path="termos" element={<TermosUsoPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
