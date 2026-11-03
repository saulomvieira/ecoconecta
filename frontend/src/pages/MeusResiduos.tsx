import { useEffect, useState } from 'react'
import { Navigate, useSearchParams } from 'react-router-dom'
import { Lock, Pencil, Plus, Trash2 } from 'lucide-react'
import { mensagemErro } from '../api/admin'
import { confirmarColeta, listarMinhasReservas, listarReservasRecebidas, type ReservaRecebida } from '../api/reservas'
import { excluirResiduo, listarFeed, type ResiduoDisponibilidade } from '../api/residuos'
import { useAuth } from '../auth/AuthContext'
import { IconeCategoria } from '../components/IconeCategoria'
import { Modal } from '../components/Modal'
import { STATUS_RESIDUO } from '../utils/residuo'
import { CadastrarResiduo } from './CadastrarResiduo'
import { MinhasReservas } from './MinhasReservas'
import './MeusResiduos.css'

function dataCurta(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

export function MeusResiduos() {
  const { sessao } = useAuth()
  const usuarioId = sessao?.id
  const [params, setParams] = useSearchParams()
  const aba = params.get('aba') === 'reservas' ? 'reservas' : 'publicados'
  const [itens, setItens] = useState<ResiduoDisponibilidade[]>([])
  const [reservas, setReservas] = useState(0)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [formulario, setFormulario] = useState<{ item?: ResiduoDisponibilidade } | null>(null)
  const [excluindo, setExcluindo] = useState<ResiduoDisponibilidade | null>(null)
  const [erroExcluir, setErroExcluir] = useState('')
  const [apagando, setApagando] = useState(false)
  const [recebidas, setRecebidas] = useState<Record<number, ReservaRecebida>>({})
  const [confirmando, setConfirmando] = useState<ReservaRecebida | null>(null)
  const [erroConfirmar, setErroConfirmar] = useState('')
  const [coletando, setColetando] = useState(false)

  const [versao, setVersao] = useState(0)
  const recarregar = () => setVersao((v) => v + 1)

  useEffect(() => {
    let ativo = true
    Promise.all([listarFeed(), listarMinhasReservas().catch(() => []), listarReservasRecebidas().catch(() => [])])
      .then(([feed, minhas, recebidasApi]) => {
        if (!ativo) return
        setRecebidas(Object.fromEntries(recebidasApi.map((r) => [r.residuoId, r])))
        setItens(feed.filter((i) => i.residuo.usuario?.id === usuarioId))
        setReservas(minhas.length)
        setErro('')
      })
      .catch((e) => ativo && setErro(mensagemErro(e, 'Não foi possível carregar seus resíduos.')))
      .finally(() => ativo && setCarregando(false))
    return () => { ativo = false }
  }, [usuarioId, versao])

  async function confirmarExclusao() {
    if (!excluindo) return
    setApagando(true)
    setErroExcluir('')
    try {
      await excluirResiduo(excluindo.residuo.id)
      setExcluindo(null)
      recarregar()
    } catch (e) {
      setErroExcluir(mensagemErro(e, 'Não foi possível excluir o resíduo.'))
    } finally {
      setApagando(false)
    }
  }

  async function confirmarRecebimento() {
    if (!confirmando) return
    setColetando(true)
    setErroConfirmar('')
    try {
      await confirmarColeta(confirmando.id)
      setConfirmando(null)
      recarregar()
    } catch (e) {
      setErroConfirmar(mensagemErro(e, 'Não foi possível confirmar a coleta.'))
    } finally {
      setColetando(false)
    }
  }

  function aoSalvar() {
    setFormulario(null)
    recarregar()
  }

  if (!sessao) return <Navigate to="/login" replace state={{ from: '/meus-residuos' }} />

  return (
    <main className="meus-residuos">
      <div className="meus-residuos-topo">
        <h1>Meus resíduos</h1>
        <button type="button" className="meus-residuos-novo" onClick={() => { setParams({}); setFormulario({}) }}>
          <Plus /> Novo resíduo
        </button>
      </div>

      <div className="meus-residuos-abas" role="tablist">
        <button type="button" role="tab" aria-selected={aba === 'publicados'} className={`aba${aba === 'publicados' ? ' ativa' : ''}`} onClick={() => setParams({})}>Publicados por mim ({itens.length})</button>
        <button type="button" role="tab" aria-selected={aba === 'reservas'} className={`aba${aba === 'reservas' ? ' ativa' : ''}`} onClick={() => setParams({ aba: 'reservas' })}>Minhas reservas ({reservas})</button>
      </div>

      {aba === 'reservas' ? (
        <MinhasReservas onContagem={setReservas} />
      ) : (
        <>
      {erro && <p className="error" role="alert">{erro}</p>}
      {carregando && !erro && <p className="muted">Carregando…</p>}
      {!carregando && !erro && itens.length === 0 && (
        <p className="meus-residuos-vazio">Você ainda não publicou nenhum resíduo. Clique em “Novo resíduo” para começar.</p>
      )}

      <ul className="meus-residuos-lista">
        {itens.map(({ residuo, status }) => {
          const info = STATUS_RESIDUO[status] ?? STATUS_RESIDUO.DISPONIVEL
          const finalizado = status === 'COLETADO'
          const reserva = status === 'RESERVADO' ? recebidas[residuo.id] : undefined
          return (
            <li key={residuo.id} className="meus-residuos-item">
              <div className="mr-principal">
                <span className={`mr-icone${finalizado ? ' cinza' : ''}`}><IconeCategoria categoria={residuo.categoria} /></span>
                <div className="mr-texto">
                  <strong>{residuo.nome} - {residuo.quantidade} {residuo.unidadeMedida}</strong>
                  <span>{residuo.enderecoColeta}</span>
                </div>
              </div>
              <span className="mr-coluna mr-regiao">{residuo.regiao}</span>
              <span className="mr-coluna"><span className={`mr-status ${info.classe}`}>{info.rotulo}</span></span>
              <span className="mr-coluna mr-data">{dataCurta(residuo.dataCadastro)}</span>
              <div className="mr-acoes">
                {reserva && (
                  <button type="button" className="mr-confirmar" onClick={() => { setErroConfirmar(''); setConfirmando(reserva) }}>Confirmar coleta</button>
                )}
                {finalizado ? (
                  <span className="mr-finalizado">Finalizado <Lock /></span>
                ) : (
                  <>
                    <button type="button" className="mr-icone-btn" aria-label={`Editar ${residuo.nome}`} onClick={() => setFormulario({ item: { residuo, status } })}><Pencil /></button>
                    <button type="button" className="mr-icone-btn excluir" aria-label={`Excluir ${residuo.nome}`} onClick={() => { setErroExcluir(''); setExcluindo({ residuo, status }) }}><Trash2 /></button>
                  </>
                )}
              </div>
            </li>
          )
        })}
      </ul>

        </>
      )}

      {formulario && <CadastrarResiduo residuo={formulario.item?.residuo} onClose={() => setFormulario(null)} onSalvo={aoSalvar} />}

      {confirmando && (
        <Modal titulo="Confirmar coleta" onClose={() => !coletando && setConfirmando(null)}>
          <p>Confirmar que {confirmando.reservadoPor} coletou este resíduo? Esta ação não pode ser desfeita.</p>
          {erroConfirmar && <p className="error" role="alert">{erroConfirmar}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setConfirmando(null)} disabled={coletando}>Cancelar</button>
            <button type="button" className="btn primary" onClick={confirmarRecebimento} disabled={coletando}>{coletando ? 'Confirmando…' : 'Confirmar coleta'}</button>
          </div>
        </Modal>
      )}

      {excluindo && (
        <Modal titulo="Excluir resíduo" onClose={() => !apagando && setExcluindo(null)}>
          <p>Deseja excluir “{excluindo.residuo.nome}”? Esta ação não pode ser desfeita.</p>
          {erroExcluir && <p className="error" role="alert">{erroExcluir}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setExcluindo(null)} disabled={apagando}>Cancelar</button>
            <button type="button" className="btn danger" onClick={confirmarExclusao} disabled={apagando}>{apagando ? 'Excluindo…' : 'Excluir'}</button>
          </div>
        </Modal>
      )}
    </main>
  )
}
