import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/auth'
import { ApiError } from '../api/client'
import { AuthCard } from '../components/AuthCard'
import { CampoEmail } from '../components/AuthFields'

export function EsqueciSenha() {
  const [email, setEmail] = useState('')
  const [enviado, setEnviado] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    setEnviando(true)
    try {
      await authApi.esqueciSenha(email.trim())
      setEnviado(true)
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Falha ao enviar as instruções.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <AuthCard titulo="Recuperar senha" subtitulo="Informe seu e-mail e enviaremos um link para criar uma nova senha.">
      {enviado ? (
        <div className="auth-stack">
          <p className="success" role="status">
            Se existir uma conta com esse e-mail, enviamos as instruções de recuperação. O link expira em 30 minutos.
          </p>
          <Link to="/login" className="btn primary auth-submit">Voltar ao login</Link>
        </div>
      ) : (
        <form onSubmit={enviar} className="auth-stack">
          <CampoEmail rotulo="E-mail" obrigatorio autoFocus value={email} onChange={(e) => setEmail(e.target.value)} />
          {erro && <p className="error" role="alert">{erro}</p>}
          <button type="submit" className="btn primary auth-submit" disabled={enviando}>
            {enviando ? 'Enviando…' : 'Enviar link de recuperação'}
          </button>
          <p className="auth-foot"><Link to="/login" className="auth-link strong">Voltar ao login</Link></p>
        </form>
      )}
    </AuthCard>
  )
}
