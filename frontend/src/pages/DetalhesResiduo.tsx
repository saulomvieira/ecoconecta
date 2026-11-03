import { useEffect, useState } from 'react'
import { Archive, MapPin, User, X } from 'lucide-react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { perfilApi, type MeuPerfil } from '../api/perfil'
import { solicitarReserva } from '../api/reservas'
import type { ResiduoDisponibilidade } from '../api/residuos'
import { useAuth } from '../auth/AuthContext'
import { IconeCategoria } from '../components/IconeCategoria'
import { LimiteColetas, type TipoLimite } from '../components/LimiteColetas'
import { STATUS_RESIDUO, urlImagem } from '../utils/residuo'
import { tempoDecorrido } from '../utils/tempo'
import './DetalhesResiduo.css'

const NOMES_PLANO: Record<string, string> = { FREE: 'Gratuito' }

function avisoDoPlano(perfil: MeuPerfil | null): { texto: string; limite: boolean } | null {
  if (!perfil || perfil.limiteMensal == null) return null
  const plano = NOMES_PLANO[perfil.plano ?? ''] ?? perfil.plano ?? ''
  if ((perfil.restantesMes ?? 1) <= 0) {
    return {
      texto: `Você atingiu o limite de ${perfil.limiteMensal} coletas do plano ${plano} neste mês.`,
      limite: true,
    }
  }
  const numero = (perfil.coletasMes ?? 0) + 1
  return {
    texto: `Será sua ${numero}ª coleta no mês - o plano ${plano} permite ${perfil.limiteMensal}.`,
    limite: false,
  }
}

interface Props {
  residuos: ResiduoDisponibilidade[]
  carregando: boolean
  erro: string
  onReservado: (residuoId: number) => void
}

export function DetalhesResiduo({ residuos, carregando, erro, onReservado }: Props) {
  const { id } = useParams<{ id: string }>()
  const { sessao } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [perfil, setPerfil] = useState<MeuPerfil | null>(null)
  const [erroReserva, setErroReserva] = useState('')
  const [reservando, setReservando] = useState(false)
  const [reservaRealizada, setReservaRealizada] = useState(false)
  const [imagemFalhou, setImagemFalhou] = useState(false)
  const [limiteAtingido, setLimiteAtingido] = useState<TipoLimite | null>(null)

  const fechar = () => navigate('/feed')
  const item = residuos.find(({ residuo }) => String(residuo.id) === id) ?? null

  const logado = sessao != null
  useEffect(() => {
    if (!logado) return
    let ativo = true
    perfilApi
      .meu()
      .then((dados) => ativo && setPerfil(dados))
      .catch(() => {})
    return () => {
      ativo = false
    }
  }, [logado, reservaRealizada])

  useEffect(() => {
    const aoPressionar = (evento: KeyboardEvent) => {
      if (evento.key === 'Escape') navigate('/feed')
    }
    const overflowAnterior = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    document.addEventListener('keydown', aoPressionar)
    return () => {
      document.body.style.overflow = overflowAnterior
      document.removeEventListener('keydown', aoPressionar)
    }
  }, [navigate])

  async function handleSolicitarReserva() {
    if (!item || reservando || reservaRealizada) return

    if (!sessao) {
      navigate('/login', { state: { from: location.pathname } })
      return
    }

    if (item.status !== 'DISPONIVEL') return

    if (perfil && (perfil.restantesMes ?? 1) <= 0) return setLimiteAtingido('MENSAL')
    if (perfil && (perfil.restantesSemana ?? 1) <= 0) return setLimiteAtingido('SEMANAL')

    setReservando(true)
    setErroReserva('')

    try {
      await solicitarReserva(sessao.id, item.residuo.id)
      setReservaRealizada(true)
      onReservado(item.residuo.id)
    } catch (error) {
      const mensagem = error instanceof Error ? error.message : ''
      const tipoLimite = /limite mensal/i.test(mensagem) ? 'MENSAL' : /limite semanal/i.test(mensagem) ? 'SEMANAL' : null
      if (tipoLimite) {
        perfilApi.meu().then(setPerfil).catch(() => {})
        setLimiteAtingido(tipoLimite)
        return
      }
      setErroReserva(
        error instanceof Error
          ? error.message
          : 'Não foi possível solicitar a reserva. Tente novamente.',
      )
    } finally {
      setReservando(false)
    }
  }

  const residuo = item?.residuo
  const status = item?.status ?? ''
  const situacao = STATUS_RESIDUO[status] ?? { rotulo: status, classe: 'coletado' }
  const disponivel = status === 'DISPONIVEL'
  const imagemUrl = residuo && !imagemFalhou ? urlImagem(residuo) : null
  const aviso = disponivel ? avisoDoPlano(perfil) : null

  return (
    <div className="detalhe-fundo" onClick={fechar}>
      <div
        className="detalhe-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="detalhe-titulo"
        onClick={(evento) => evento.stopPropagation()}
      >
        {limiteAtingido && (
          <LimiteColetas
            tipo={limiteAtingido}
            plano={NOMES_PLANO[perfil?.plano ?? ''] ?? perfil?.plano ?? 'atual'}
            limite={limiteAtingido === 'MENSAL' ? (perfil?.limiteMensal ?? null) : (perfil?.limiteSemanal ?? null)}
            usadas={limiteAtingido === 'MENSAL' ? (perfil?.coletasMes ?? null) : (perfil?.coletasSemana ?? null)}
            onVoltar={fechar}
            onVerPlanos={() => navigate('/planos')}
          />
        )}

        {!limiteAtingido && carregando && <p className="detalhe-mensagem">Carregando detalhes...</p>}

        {!limiteAtingido && !carregando && erro && (
          <div className="detalhe-mensagem">
            <p role="alert">{erro}</p>
            <button type="button" className="btn" onClick={fechar}>Voltar ao feed</button>
          </div>
        )}

        {!limiteAtingido && !carregando && !erro && !item && (
          <div className="detalhe-mensagem">
            <h1 id="detalhe-titulo">Resíduo não encontrado</h1>
            <p>O resíduo solicitado não está disponível no feed.</p>
            <button type="button" className="btn" onClick={fechar}>Voltar ao feed</button>
          </div>
        )}

        {!limiteAtingido && !carregando && !erro && item && residuo && (
          <>
            <div className="detalhe-topo">
              {imagemUrl ? (
                <img src={imagemUrl} alt={residuo.nome} onError={() => setImagemFalhou(true)} />
              ) : (
                <IconeCategoria categoria={residuo.categoria} className="detalhe-icone" />
              )}
              <div className="detalhe-chips">
                <span className="detalhe-chip categoria">{residuo.categoria}</span>
                <div className="detalhe-chips-direita">
                  <span className={`detalhe-chip status ${situacao.classe}`}>{situacao.rotulo}</span>
                  <button type="button" className="detalhe-fechar" onClick={fechar} aria-label="Fechar">
                    <X aria-hidden="true" />
                  </button>
                </div>
              </div>
            </div>

            <div className="detalhe-corpo">
              <h1 id="detalhe-titulo">{residuo.nome}</h1>
              <p className="detalhe-descricao">{residuo.descricao}</p>

              <ul className="detalhe-info">
                <li>
                  <MapPin aria-hidden="true" />
                  {residuo.enderecoColeta} - {residuo.regiao}
                </li>
                <li>
                  <User aria-hidden="true" />
                  Publicado por {residuo.usuario?.nome ?? 'usuário do EcoConecta'} · {tempoDecorrido(residuo.dataCadastro)}
                </li>
                <li>
                  <Archive aria-hidden="true" />
                  {residuo.quantidade} {residuo.unidadeMedida}
                </li>
              </ul>

              <div className="detalhe-rodape">
                {reservaRealizada ? (
                  <p className="detalhe-sucesso" role="status">
                    Reserva solicitada com sucesso! Acompanhe em{' '}
                    <Link to="/minhas-reservas">Minhas Reservas</Link>.
                  </p>
                ) : !disponivel ? (
                  <p className="detalhe-nota">Este resíduo não está disponível para reserva.</p>
                ) : aviso ? (
                  <p className={`detalhe-nota${aviso.limite ? ' limite' : ''}`}>{aviso.texto}</p>
                ) : null}

                {erroReserva && <p className="detalhe-erro" role="alert">{erroReserva}</p>}

                <div className="detalhe-acoes">
                  <button type="button" className="detalhe-botao secundario" onClick={fechar}>
                    Voltar
                  </button>
                  {!reservaRealizada && (
                    <button
                      type="button"
                      className="detalhe-botao"
                      onClick={handleSolicitarReserva}
                      disabled={!disponivel || reservando}
                    >
                      <Archive aria-hidden="true" />
                      {reservando ? 'Reservando...' : 'Reservar coleta'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
