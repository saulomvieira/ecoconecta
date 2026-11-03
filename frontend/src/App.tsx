import { PlanosUsuario } from './pages/PlanosUsuario'

import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext'
import { AdminLayout } from './components/AdminLayout'
import { UserNavbar } from './components/UserNavbar'
import { DashboardPage } from './pages/Dashboard'
import { Cadastro } from './pages/Cadastro'
import { EsqueciSenha } from './pages/EsqueciSenha'
import { Login } from './pages/Login'
import { PlanosPage } from './pages/Planos'
import { RedefinirSenha } from './pages/RedefinirSenha'
import { ResiduosPage } from './pages/Residuos'
import { TermosUsoPage } from './pages/TermosUso'
import { UsuariosPage } from './pages/Usuarios'
import { FeedComDetalhes } from './pages/FeedComDetalhes'
import { MeusResiduos } from './pages/MeusResiduos'
import { Perfil } from './pages/Perfil'

function RequireAdmin({ children }: { children: React.ReactNode }) {
  const { sessao } = useAuth()
  const location = useLocation()

  if (!sessao) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (sessao.perfil !== 'ADMIN') {
    return <Navigate to="/feed" replace />
  }

  return children
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/cadastro" element={<Cadastro />} />
          <Route path="/esqueci-senha" element={<EsqueciSenha />} />
          <Route path="/redefinir-senha" element={<RedefinirSenha />} />

          {/* Área dos usuários */}
          <Route element={<UserNavbar />}>
            <Route path="/perfil" element={<Perfil />} />
            <Route path="/planos" element={<PlanosUsuario />} />
            <Route path="/feed" element={<FeedComDetalhes />} />
            <Route path="/meus-residuos" element={<MeusResiduos />} />
            <Route path="/residuos/:id" element={<FeedComDetalhes />} />
            <Route path="/minhas-reservas" element={<Navigate to="/meus-residuos?aba=reservas" replace />} />
          </Route>

          {/* Área administrativa */}
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

          <Route path="*" element={<Navigate to="/feed" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
