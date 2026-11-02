import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { CampoEmail, CampoSenha } from '../components/AuthFields'
import { LogoIcone } from '../components/LogoIcone'

export function Login() {
  const { sessao, entrar } = useAuth()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [lembrar, setLembrar] = useState(true)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  if (sessao) {
    const destino =
      (location.state as { from?: string } | null)?.from ??
      (sessao.perfil === 'ADMIN' ? '/admin/dashboard' : '/feed')
    return <Navigate to={destino} replace />
  }

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await entrar(email.trim(), senha, lembrar)
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Falha ao entrar.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-split">
        <aside className="auth-hero">
          <LogoIcone strokeWidth={1.5} className="hero-leaf" />
          <h2>Dê um destino útil ao que seria descartado.</h2>
          <p>Conectamos geradores e coletores de resíduos reaproveitáveis.</p>
          <small>EcoConecta © Sustentabilidade Inteligente</small>
        </aside>

        <form className="auth-form" onSubmit={enviar}>
          <div className="auth-brand"><LogoIcone /> EcoConecta</div>
          <h1>Entrar na sua conta</h1>
          <p className="auth-sub">Bem-vindo de volta! Informe seus dados.</p>

          <CampoEmail rotulo="E-mail" value={email} onChange={(e) => setEmail(e.target.value)} obrigatorio autoFocus />
          <CampoSenha rotulo="Senha" placeholder="••••••••" value={senha} onChange={(e) => setSenha(e.target.value)} obrigatorio />

          <div className="auth-row">
            <label className="check">
              <input type="checkbox" checked={lembrar} onChange={(e) => setLembrar(e.target.checked)} />
              Lembrar de mim
            </label>
            <Link to="/esqueci-senha" className="auth-link">Esqueci a senha</Link>
          </div>

          {erro && <p className="error" role="alert">{erro}</p>}
          <button type="submit" className="btn primary auth-submit" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>

          <div className="auth-or"><span>OU</span></div>
          <p className="auth-foot">Ainda não tem conta? <Link to="/cadastro" className="auth-link strong">Cadastre-se</Link></p>
        </form>
      </div>
    </div>
  )
}
