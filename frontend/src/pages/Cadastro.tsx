import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { MapPin, User } from 'lucide-react'
import { authApi, REGIOES, SENHA_MIN } from '../api/auth'
import { ApiError } from '../api/client'
import { termosApi, type TermoUso } from '../api/termos'
import { useAuth } from '../auth/AuthContext'
import { AuthCard } from '../components/AuthCard'
import { CampoEmail, CampoSenha, CampoTexto } from '../components/AuthFields'
import { Modal } from '../components/Modal'

export function Cadastro() {
  const { sessao, entrar } = useAuth()
  const navigate = useNavigate()
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [regiao, setRegiao] = useState('')
  const [aceitou, setAceitou] = useState(false)
  const [termo, setTermo] = useState<TermoUso | null>(null)
  const [lendoTermo, setLendoTermo] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const divergente = confirmacao !== '' && confirmacao !== senha

  useEffect(() => {
    // 404 = ainda não há termo publicado: o backend não exige aceite nesse caso.
    termosApi.atual().then(setTermo).catch(() => setTermo(null))
  }, [])

  if (sessao) return <Navigate to={sessao.perfil === 'ADMIN' ? '/admin/dashboard' : '/feed'} replace />

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setErro(null)
    if (senha.length < SENHA_MIN) {
      setErro(`A senha deve ter no mínimo ${SENHA_MIN} caracteres.`)
      return
    }
    if (senha !== confirmacao) {
      setErro('As senhas não conferem.')
      return
    }
    setEnviando(true)
    try {
      await authApi.cadastrar({ nome: nome.trim(), email: email.trim(), senha, regiao, aceitouTermos: aceitou })
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Falha ao criar a conta.')
      setEnviando(false)
      return
    }
    try {
      await entrar(email.trim(), senha) // a navegação ao feed acontece pelo redirecionamento acima
    } catch {
      navigate('/login', { replace: true }) // conta criada, mas o login automático falhou
    }
  }

  return (
    <AuthCard titulo="Crie sua conta" subtitulo="Cadastro rápido para gerar ou coletar resíduos.">
      <form onSubmit={enviar} className="auth-stack">
        <CampoTexto rotulo="Nome completo" icone={User} obrigatorio placeholder="Seu nome completo" autoComplete="name"
          value={nome} onChange={(e) => setNome(e.target.value)} />
        <CampoEmail rotulo="E-mail" obrigatorio value={email} onChange={(e) => setEmail(e.target.value)} />
        <CampoSenha rotulo="Senha" obrigatorio autoComplete="new-password" placeholder={`Mín. ${SENHA_MIN} caracteres`}
          minLength={SENHA_MIN} value={senha} onChange={(e) => setSenha(e.target.value)} />
        <div>
          <CampoSenha rotulo="Repetir senha" obrigatorio autoComplete="new-password" placeholder="Repita a senha"
            aria-invalid={divergente} value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} />
          {divergente && <p className="auth-error" role="alert">As senhas não conferem.</p>}
        </div>

        <label className="auth-field">
          <span className="auth-label">Região<b aria-hidden="true"> *</b></span>
          <span className="auth-input">
            <MapPin />
            <select required value={regiao} onChange={(e) => setRegiao(e.target.value)}>
              <option value="" disabled>Selecione</option>
              {REGIOES.map((r) => <option key={r} value={r}>Fortaleza - {r}</option>)}
            </select>
          </span>
          <small className="muted">Usada como pré-seleção do seletor de região no feed.</small>
        </label>

        {termo && (
          <label className="terms">
            <input type="checkbox" checked={aceitou} onChange={(e) => setAceitou(e.target.checked)} required />
            <span>
              Li e aceito os{' '}
              <button type="button" className="auth-link strong" onClick={() => setLendoTermo(true)}>Termos de Uso</button>{' '}
              do EcoConecta.
            </span>
          </label>
        )}

        {erro && <p className="error" role="alert">{erro}</p>}
        <button type="submit" className="btn primary auth-submit" disabled={enviando}>
          {enviando ? 'Criando…' : 'Criar conta'}
        </button>
        <p className="auth-foot">Já tem conta? <Link to="/login" className="auth-link strong">Entrar</Link></p>
      </form>

      {lendoTermo && termo && (
        <Modal largo titulo={`Termos de Uso · v${termo.versao}`} onClose={() => setLendoTermo(false)}>
          {/* Conteúdo HTML publicado por um administrador no editor de termos. */}
          <div className="terms-content" dangerouslySetInnerHTML={{ __html: termo.conteudo }} />
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setLendoTermo(false)}>Fechar</button>
            <button type="button" className="btn primary" onClick={() => { setAceitou(true); setLendoTermo(false) }}>
              Li e aceito
            </button>
          </div>
        </Modal>
      )}
    </AuthCard>
  )
}
