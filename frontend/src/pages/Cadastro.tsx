import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { MapPin, Phone, User } from 'lucide-react'
import { authApi, SENHA_MIN } from '../api/auth'
import { REGIOES } from '../api/regioes'
import { ApiError } from '../api/client'
import { termosApi, type TermoUso } from '../api/termos'
import { useAuth } from '../auth/AuthContext'
import { AuthCard } from '../components/AuthCard'
import { CampoEmail, CampoSenha, CampoTexto } from '../components/AuthFields'
import { Modal } from '../components/Modal'
import { MENSAGEM_TELEFONE, formatarTelefone, somenteDigitos, telefoneValido } from '../utils/telefone'

export function Cadastro() {
  const { sessao, entrar } = useAuth()
  const navigate = useNavigate()
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [regiao, setRegiao] = useState('')
  const [telefone, setTelefone] = useState('')
  const [aceitou, setAceitou] = useState(false)
  const [termo, setTermo] = useState<TermoUso | null>(null)
  const [lendoTermo, setLendoTermo] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const divergente = confirmacao !== '' && confirmacao !== senha

  useEffect(() => {

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
    if (!telefoneValido(telefone)) {
      setErro(MENSAGEM_TELEFONE)
      return
    }
    setEnviando(true)
    try {
      await authApi.cadastrar({ nome: nome.trim(), email: email.trim(), senha, regiao, telefone: somenteDigitos(telefone), aceitouTermos: aceitou })
    } catch (err) {
      setErro(err instanceof ApiError ? err.message : 'Falha ao criar a conta.')
      setEnviando(false)
      return
    }
    try {
      await entrar(email.trim(), senha)
    } catch {
      navigate('/login', { replace: true })
    }
  }

  return (
    <AuthCard titulo="Crie sua conta" subtitulo="Cadastro rápido para gerar ou coletar resíduos.">
      <form onSubmit={enviar} className="auth-stack">
        <CampoTexto rotulo="Nome completo" icone={User} obrigatorio placeholder="Seu nome completo" autoComplete="name"
          value={nome} onChange={(e) => setNome(e.target.value)} />
        <CampoEmail rotulo="E-mail" obrigatorio value={email} onChange={(e) => setEmail(e.target.value)} />
        <div>
          <CampoTexto rotulo="Telefone" icone={Phone} obrigatorio type="tel" inputMode="tel" autoComplete="tel-national"
            placeholder="(85) 99999-9999" maxLength={15} value={telefone}
            onChange={(e) => setTelefone(formatarTelefone(e.target.value))} />
          <small className="muted">Usado pelo dono do resíduo para combinar a coleta quando você reservar.</small>
        </div>
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
