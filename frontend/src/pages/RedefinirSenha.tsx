import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { authApi, SENHA_MIN } from '../api/auth'
import { ApiError } from '../api/client'
import { AuthCard } from '../components/AuthCard'
import { CampoSenha } from '../components/AuthFields'

export function RedefinirSenha() {
  const [params] = useSearchParams()
  const token = params.get('token')
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [concluido, setConcluido] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    if (senha.length < SENHA_MIN) return setErro(`A senha deve ter no mínimo ${SENHA_MIN} caracteres.`)
    if (senha !== confirmacao) return setErro('As senhas não conferem.')
    setEnviando(true)
    try {
      await authApi.redefinirSenha(token!, senha)
      setConcluido(true)
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Falha ao redefinir a senha.')
    } finally {
      setEnviando(false)
    }
  }

  if (!token) {
    return (
      <AuthCard titulo="Link inválido">
        <div className="auth-stack">
          <p className="error">Este link de recuperação está incompleto. Solicite um novo.</p>
          <Link to="/esqueci-senha" className="btn primary auth-submit">Solicitar novo link</Link>
        </div>
      </AuthCard>
    )
  }

  return (
    <AuthCard titulo="Nova senha" subtitulo="Escolha uma nova senha para acessar sua conta.">
      {concluido ? (
        <div className="auth-stack">
          <p className="success" role="status">Senha redefinida com sucesso.</p>
          <Link to="/login" className="btn primary auth-submit">Entrar</Link>
        </div>
      ) : (
        <form onSubmit={enviar} className="auth-stack">
          <CampoSenha rotulo="Nova senha" obrigatorio autoComplete="new-password" autoFocus placeholder={`Mín. ${SENHA_MIN} caracteres`}
            minLength={SENHA_MIN} value={senha} onChange={(e) => setSenha(e.target.value)} />
          <CampoSenha rotulo="Confirmar nova senha" obrigatorio autoComplete="new-password" placeholder="Repita a senha"
            value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} />
          {erro && <p className="error" role="alert">{erro}</p>}
          <button type="submit" className="btn primary auth-submit" disabled={enviando}>
            {enviando ? 'Salvando…' : 'Redefinir senha'}
          </button>
          <p className="auth-foot"><Link to="/login" className="auth-link strong">Voltar ao login</Link></p>
        </form>
      )}
    </AuthCard>
  )
}
