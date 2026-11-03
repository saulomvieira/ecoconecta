import { useEffect, useRef, useState } from 'react'
import { LayoutDashboard, LogOut } from 'lucide-react'
import { Link, NavLink, Outlet } from 'react-router-dom'
import { iniciais, nomeAbreviado, perfilApi, type MeuPerfil } from '../api/perfil'
import { useAuth } from '../auth/AuthContext'
import { LogoIcone } from './LogoIcone'

const links = [
  { to: '/feed', label: 'Feed' },
  { to: '/meus-residuos', label: 'Meus resíduos' },
  { to: '/planos', label: 'Planos' },
]

function MenuUsuario({ perfil, admin }: { perfil: MeuPerfil | null; admin: boolean }) {
  const { sair } = useAuth()
  const [aberto, setAberto] = useState(false)
  const raiz = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (!aberto) return
    const fora = (e: MouseEvent) => !raiz.current?.contains(e.target as Node) && setAberto(false)
    const esc = (e: KeyboardEvent) => e.key === 'Escape' && setAberto(false)
    document.addEventListener('mousedown', fora)
    document.addEventListener('keydown', esc)
    return () => {
      document.removeEventListener('mousedown', fora)
      document.removeEventListener('keydown', esc)
    }
  }, [aberto])

  return (
    <div className="navbar-user" ref={raiz}>
      {perfil && <span className="nome">{nomeAbreviado(perfil.nome)}</span>}
      <button type="button" className="avatar" onClick={() => setAberto((v) => !v)}
        aria-haspopup="menu" aria-expanded={aberto} aria-label="Menu da conta">
        {perfil ? iniciais(perfil.nome) : '·'}
      </button>
      {aberto && (
        <div className="card user-menu" role="menu">
          {perfil && (
            <div className="menu-info"><strong>{perfil.nome}</strong><span>{perfil.email}</span></div>
          )}
          {admin && <Link to="/admin/dashboard" role="menuitem"><LayoutDashboard /> Painel admin</Link>}
          <button type="button" className="sair" role="menuitem" onClick={sair}><LogOut /> Sair</button>
        </div>
      )}
    </div>
  )
}

export function UserNavbar() {
  const { sessao } = useAuth()
  const [perfil, setPerfil] = useState<MeuPerfil | null>(null)
  const id = sessao?.id

  useEffect(() => {
    if (id == null) return
    let ativo = true
    perfilApi.meu().then((p) => ativo && setPerfil(p)).catch(() => { })
    return () => { ativo = false }
  }, [id])

  return (
    <div className="user-layout">
      <header className="navbar">
        <Link to="/feed" className="brand"><LogoIcone /><span>EcoConecta</span></Link>
        <nav className="navbar-links" aria-label="Principal">
          {links.map(({ to, label }) => (
            <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'active' : '')}>{label}</NavLink>
          ))}
        </nav>
        {sessao ? (
          <MenuUsuario perfil={perfil} admin={sessao.perfil === 'ADMIN'} />
        ) : (
          <div className="navbar-user">
            <Link to="/login" className="btn">Entrar</Link>
            <Link to="/cadastro" className="btn primary">Criar conta</Link>
          </div>
        )}
      </header>
      <Outlet />
    </div>
  )
}
