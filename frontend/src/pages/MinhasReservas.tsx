import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { mensagemErro } from '../api/admin'
import { cancelarReserva, listarMinhasReservas, type Reserva, type StatusReserva } from '../api/reservas'
import { IconeCategoria } from '../components/IconeCategoria'
import { Modal } from '../components/Modal'

type Filtro = 'TODAS' | StatusReserva

const FILTROS: { valor: Filtro; titulo: string }[] = [
  { valor: 'TODAS', titulo: 'Todas' },
  { valor: 'RESERVADA', titulo: 'Ativas' },
  { valor: 'COLETADA', titulo: 'Concluídas' },
  { valor: 'CANCELADA', titulo: 'Canceladas' },
]

const STATUS: Record<StatusReserva, { rotulo: string; classe: string }> = {
  RESERVADA: { rotulo: 'Ativa', classe: 'disponivel' },
  COLETADA: { rotulo: 'Concluída', classe: 'coletado' },
  CANCELADA: { rotulo: 'Cancelada', classe: 'cancelado' },
}

const VAZIO: Record<Filtro, string> = {
  TODAS: 'Você ainda não realizou nenhuma reserva.',
  RESERVADA: 'Você não possui reservas ativas.',
  COLETADA: 'Você ainda não possui reservas concluídas.',
  CANCELADA: 'Você não possui reservas canceladas.',
}

function dataCurta(iso: string): string {
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' })
}

export function MinhasReservas({ onContagem }: { onContagem?: (n: number) => void }) {
  const [reservas, setReservas] = useState<Reserva[]>([])
  const [filtro, setFiltro] = useState<Filtro>('TODAS')
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [cancelando, setCancelando] = useState<Reserva | null>(null)
  const [processando, setProcessando] = useState(false)
  const [erroCancelar, setErroCancelar] = useState('')

  useEffect(() => {
    let ativo = true
    listarMinhasReservas()
      .then((dados) => {
        if (!ativo) return
        setReservas(dados)
        onContagem?.(dados.length)
      })
      .catch(() => ativo && setErro('Não foi possível carregar suas reservas.'))
      .finally(() => ativo && setCarregando(false))
    return () => { ativo = false }
  }, [onContagem])

  async function confirmarCancelamento() {
    if (!cancelando) return
    setProcessando(true)
    setErroCancelar('')
    try {
      await cancelarReserva(cancelando.id)
      setReservas((atual) => atual.map((r) => (r.id === cancelando.id ? { ...r, status: 'CANCELADA' } : r)))
      setCancelando(null)
    } catch (e) {
      setErroCancelar(mensagemErro(e, 'Não foi possível cancelar a reserva.'))
    } finally {
      setProcessando(false)
    }
  }

  const contagem = (f: Filtro) => (f === 'TODAS' ? reservas.length : reservas.filter((r) => r.status === f).length)
  const visiveis = reservas
    .filter((r) => filtro === 'TODAS' || r.status === filtro)
    .sort((a, b) => new Date(b.dataReserva).getTime() - new Date(a.dataReserva).getTime())

  if (carregando) return <p className="muted">Carregando…</p>
  if (erro) return <p className="error" role="alert">{erro}</p>

  return (
    <>
      <div className="mr-filtros" role="group" aria-label="Filtrar reservas por status">
        {FILTROS.map(({ valor, titulo }) => (
          <button key={valor} type="button" className={`mr-filtro${filtro === valor ? ' ativo' : ''}`} aria-pressed={filtro === valor} onClick={() => setFiltro(valor)}>
            {titulo} ({contagem(valor)})
          </button>
        ))}
      </div>

      {visiveis.length === 0 ? (
        <p className="meus-residuos-vazio">
          {VAZIO[filtro]} {filtro === 'TODAS' && <Link to="/feed">Explorar resíduos</Link>}
        </p>
      ) : (
        <ul className="meus-residuos-lista">
          {visiveis.map((reserva) => {
            const { residuo } = reserva
            const info = STATUS[reserva.status]
            return (
              <li key={reserva.id} className="meus-residuos-item">
                <div className="mr-principal">
                  <span className={`mr-icone${reserva.status === 'RESERVADA' ? '' : ' cinza'}`}><IconeCategoria categoria={residuo.categoria} /></span>
                  <div className="mr-texto">
                    <strong>{residuo.nome} - {residuo.quantidade} {residuo.unidadeMedida}</strong>
                    <span>{residuo.enderecoColeta}</span>
                  </div>
                </div>
                <span className="mr-coluna mr-regiao">{residuo.regiao}</span>
                <span className="mr-coluna"><span className={`mr-status ${info.classe}`}>{info.rotulo}</span></span>
                <span className="mr-coluna mr-data">{dataCurta(reserva.dataReserva)}</span>
                <div className="mr-acoes mr-acoes-reserva">
                  <Link to={`/residuos/${residuo.id}`} className="mr-link">Ver resíduo</Link>
                  {reserva.status === 'RESERVADA' && (
                    <button type="button" className="mr-cancelar" onClick={() => { setErroCancelar(''); setCancelando(reserva) }}>Cancelar</button>
                  )}
                </div>
              </li>
            )
          })}
        </ul>
      )}

      {cancelando && (
        <Modal titulo="Cancelar reserva" onClose={() => !processando && setCancelando(null)}>
          <p>Deseja realmente cancelar a reserva de “{cancelando.residuo.nome}”?</p>
          {erroCancelar && <p className="error" role="alert">{erroCancelar}</p>}
          <div className="actions end">
            <button type="button" className="btn" onClick={() => setCancelando(null)} disabled={processando}>Voltar</button>
            <button type="button" className="btn danger" onClick={confirmarCancelamento} disabled={processando}>{processando ? 'Cancelando…' : 'Cancelar reserva'}</button>
          </div>
        </Modal>
      )}
    </>
  )
}
