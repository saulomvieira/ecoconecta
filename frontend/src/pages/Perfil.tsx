import { useEffect, useState, type FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { LogOut } from 'lucide-react'
import { mensagemErro } from '../api/admin'
import { iniciais, perfilApi, type MeuPerfil } from '../api/perfil'
import { REGIOES } from '../api/regioes'
import { useAuth } from '../auth/AuthContext'
import './Perfil.css'

const SENHA_MIN = 8

function Barra({ rotulo, usadas, limite }: { rotulo: string; usadas: number; limite: number | null | undefined }) {
  const ilimitado = limite == null
  const pct = ilimitado || limite <= 0 ? 0 : Math.min(100, (usadas / limite) * 100)
  return (
    <div className="perfil-uso">
      <div className="perfil-uso-topo">
        <span>{rotulo}</span>
        <strong>{ilimitado ? `${usadas} (ilimitado)` : `${usadas} / ${limite}`}</strong>
      </div>
      <div className="perfil-barra" role="progressbar" aria-valuemin={0} aria-valuemax={limite ?? usadas} aria-valuenow={usadas}>
        <span style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

export function Perfil() {
  const { sessao, sair } = useAuth()
  const [perfil, setPerfil] = useState<MeuPerfil | null>(null)
  const [erroCarga, setErroCarga] = useState('')
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [regiao, setRegiao] = useState('')
  const [salvando, setSalvando] = useState(false)
  const [msgDados, setMsgDados] = useState<{ ok: boolean; texto: string } | null>(null)

  const [atual, setAtual] = useState('')
  const [nova, setNova] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [trocando, setTrocando] = useState(false)
  const [msgSenha, setMsgSenha] = useState<{ ok: boolean; texto: string } | null>(null)

  function preencher(p: MeuPerfil) {
    setPerfil(p)
    setNome(p.nome)
    setEmail(p.email)
    setRegiao(p.regiao ?? '')
  }

  useEffect(() => {
    if (!sessao) return
    let ativo = true
    perfilApi.meu()
      .then((p) => ativo && preencher(p))
      .catch((e) => ativo && setErroCarga(mensagemErro(e, 'Não foi possível carregar seu perfil.')))
    return () => { ativo = false }
  }, [sessao])

  if (!sessao) return <Navigate to="/login" replace state={{ from: '/perfil' }} />

  async function salvarDados(e: FormEvent) {
    e.preventDefault()
    setSalvando(true)
    setMsgDados(null)
    try {
      preencher(await perfilApi.atualizar({ nome: nome.trim(), email: email.trim(), regiao }))
      setMsgDados({ ok: true, texto: 'Dados atualizados com sucesso.' })
    } catch (err) {
      setMsgDados({ ok: false, texto: mensagemErro(err, 'Não foi possível salvar os dados.') })
    } finally {
      setSalvando(false)
    }
  }

  const divergente = confirmacao.length > 0 && nova !== confirmacao

  async function trocarSenha(e: FormEvent) {
    e.preventDefault()
    if (!sessao || nova !== confirmacao) return
    setTrocando(true)
    setMsgSenha(null)
    try {
      await perfilApi.alterarSenha(sessao.id, atual, nova)
      setAtual(''); setNova(''); setConfirmacao('')
      setMsgSenha({ ok: true, texto: 'Senha alterada com sucesso.' })
    } catch (err) {
      setMsgSenha({ ok: false, texto: mensagemErro(err, 'Não foi possível alterar a senha.') })
    } finally {
      setTrocando(false)
    }
  }

  return (
    <main className="perfil">
      <h1>Meu perfil</h1>
      {erroCarga && <p className="error" role="alert">{erroCarga}</p>}
      {!perfil && !erroCarga && <p className="muted">Carregando…</p>}

      {perfil && (
        <div className="perfil-grade">
          <section className="perfil-card">
            <div className="perfil-cabecalho">
              <span className="perfil-avatar">{iniciais(perfil.nome)}</span>
              <div><strong>{perfil.nome}</strong><span>{perfil.email}</span></div>
            </div>
            <form onSubmit={salvarDados} className="perfil-form">
              <h2>Dados pessoais</h2>
              <label>Nome completo
                <input required value={nome} onChange={(e) => setNome(e.target.value)} autoComplete="name" />
              </label>
              <label>E-mail
                <input required type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
              </label>
              <label>Região
                <select value={regiao} onChange={(e) => setRegiao(e.target.value)}>
                  <option value="">Selecione</option>
                  {regiao && !REGIOES.includes(regiao) && <option value={regiao}>{regiao}</option>}
                  {REGIOES.map((r) => <option key={r} value={r}>Fortaleza - {r}</option>)}
                </select>
              </label>
              {msgDados && <p className={msgDados.ok ? 'perfil-ok' : 'error'} role="alert">{msgDados.texto}</p>}
              <button type="submit" className="btn primary" disabled={salvando}>{salvando ? 'Salvando…' : 'Salvar alterações'}</button>
            </form>
          </section>

          <div className="perfil-coluna">
            <section className="perfil-card">
              <h2>Plano atual</h2>
              <p className="perfil-plano">{perfil.plano ?? 'Gratuito'}</p>
              <Barra rotulo="Coletas do mês" usadas={perfil.coletasMes ?? 0} limite={perfil.plano ? perfil.limiteMensal : 4} />
              <Barra rotulo="Coletas da semana" usadas={perfil.coletasSemana ?? 0} limite={perfil.plano ? perfil.limiteSemanal : 1} />
              <Link to="/planos" className="btn">Gerenciar plano</Link>
            </section>

            <section className="perfil-card">
              <form onSubmit={trocarSenha} className="perfil-form">
                <h2>Alterar senha</h2>
                <label>Senha atual
                  <input required type="password" value={atual} onChange={(e) => setAtual(e.target.value)} autoComplete="current-password" />
                </label>
                <label>Nova senha
                  <input required type="password" minLength={SENHA_MIN} placeholder={`Mín. ${SENHA_MIN} caracteres`} value={nova} onChange={(e) => setNova(e.target.value)} autoComplete="new-password" />
                </label>
                <label>Repetir nova senha
                  <input required type="password" value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} autoComplete="new-password" aria-invalid={divergente} />
                </label>
                {divergente && <p className="error" role="alert">As senhas não conferem.</p>}
                {msgSenha && <p className={msgSenha.ok ? 'perfil-ok' : 'error'} role="alert">{msgSenha.texto}</p>}
                <button type="submit" className="btn primary" disabled={trocando || divergente}>{trocando ? 'Alterando…' : 'Alterar senha'}</button>
              </form>
            </section>

            <button type="button" className="btn perfil-sair" onClick={sair}><LogOut /> Sair da conta</button>
          </div>
        </div>
      )}
    </main>
  )
}
