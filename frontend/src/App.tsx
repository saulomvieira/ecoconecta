import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { AdminLayout } from './components/AdminLayout'
import { EmBreve } from './pages/EmBreve'
import { Login } from './pages/Login'
import { TermosUsoPage } from './pages/TermosUso'

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
            <Route index element={<Navigate to="termos" replace />} />
            <Route path="dashboard" element={<EmBreve titulo="Dashboard" />} />
            <Route path="usuarios" element={<EmBreve titulo="Usuários" />} />
            <Route path="residuos" element={<EmBreve titulo="Resíduos" />} />
            <Route path="planos" element={<EmBreve titulo="Planos" />} />
            <Route path="termos" element={<TermosUsoPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/admin/termos" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
