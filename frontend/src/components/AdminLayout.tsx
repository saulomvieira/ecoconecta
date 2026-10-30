import { useEffect, useState } from 'react'
import { CreditCard, FileText, LayoutDashboard, LogOut, Sprout, Trash2, Users } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { iniciais, nomeAbreviado, perfilApi, type MeuPerfil } from '../api/perfil'
import { useAuth } from '../auth/AuthContext'

const itens = [
  { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/admin/usuarios', label: 'Usuários', icon: Users },
  { to: '/admin/residuos', label: 'Resíduos', icon: Trash2 },
  { to: '/admin/planos', label: 'Planos', icon: CreditCard },
  { to: '/admin/termos', label: 'Termos de Uso', icon: FileText },
]

export function AdminLayout() {
  const { sair } = useAuth()
  const [perfil, setPerfil] = useState<MeuPerfil | null>(null)

  useEffect(() => {
    perfilApi.meu().then(setPerfil).catch(() => {})
  }, [])

  return (
    <div className="admin">
      <aside className="sidebar">
        <div>
          <div className="brand">
            <Sprout strokeWidth={1.75} />
            EcoConecta
          </div>
          <span className="badge-admin">ADMIN</span>
          <nav>
            {itens.map(({ to, label, icon: Icon }) => (
              <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'active' : '')}>
                <Icon />
                {label}
              </NavLink>
            ))}
          </nav>
        </div>
        <div className="sidebar-user">
          <div className="avatar">{perfil ? iniciais(perfil.nome) : '·'}</div>
          <div>
            <strong>{perfil ? nomeAbreviado(perfil.nome) : 'Administrador'}</strong>
            <span>Administrador</span>
          </div>
          <button type="button" className="logout" onClick={sair} aria-label="Sair" title="Sair">
            <LogOut />
          </button>
        </div>
      </aside>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}
