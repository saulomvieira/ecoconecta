
import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import {
    listarMinhasReservas,
    cancelarReserva,
    type Reserva,
    type StatusReserva,
} from '../api/reservas'
import { useAuth } from '../auth/AuthContext'
import './MinhasReservas.css'

type FiltroReserva = 'TODAS' | StatusReserva

const filtros: { valor: FiltroReserva; titulo: string }[] = [
    { valor: 'TODAS', titulo: 'Todas' },
    { valor: 'RESERVADA', titulo: 'Ativas' },
    { valor: 'COLETADA', titulo: 'Concluídas' },
    { valor: 'CANCELADA', titulo: 'Canceladas' },
]

const nomesStatus: Record<StatusReserva, string> = {
    RESERVADA: 'Ativa',
    COLETADA: 'Concluída',
    CANCELADA: 'Cancelada',
}

const mensagensVazias: Record<FiltroReserva, string> = {
    TODAS: 'Você ainda não realizou nenhuma reserva.',
    RESERVADA: 'Você não possui reservas ativas.',
    COLETADA: 'Você ainda não possui reservas concluídas.',
    CANCELADA: 'Você não possui reservas canceladas.',
}

export function MinhasReservas() {
    const { sessao } = useAuth()

    const [reservas, setReservas] = useState<Reserva[]>([])
    const [filtro, setFiltro] = useState<FiltroReserva>('TODAS')
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState('')
    const [cancelandoId, setCancelandoId] = useState<number | null>(null)

    useEffect(() => {
        if (!sessao) return

        let ativo = true

        async function carregarReservas() {
            setCarregando(true)
            setErro('')

            try {
                const dados = await listarMinhasReservas()

                if (ativo) {
                    setReservas(dados)
                }
            } catch {
                if (ativo) {
                    setErro('Não foi possível carregar suas reservas.')
                }
            } finally {
                if (ativo) {
                    setCarregando(false)
                }
            }
        }

        carregarReservas()

        return () => {
            ativo = false
        }
    }, [sessao])

    async function handleCancelar(id: number) {
        if (!window.confirm('Deseja realmente cancelar esta reserva?')) {
            return
        }

        setCancelandoId(id)
        setErro('')

        try {
            await cancelarReserva(id)

            setReservas((atual) =>
                atual.map((reserva) =>
                    reserva.id === id
                        ? { ...reserva, status: 'CANCELADA' }
                        : reserva
                )
            )
        } catch (error) {
            setErro(
                error instanceof Error
                    ? error.message
                    : 'Não foi possível cancelar a reserva.'
            )
        } finally {
            setCancelandoId(null)
        }
    }

    if (!sessao) {
        return (
            <Navigate
                to="/login"
                replace
                state={{ from: '/minhas-reservas' }}
            />
        )
    }

    const contadores: Record<FiltroReserva, number> = {
        TODAS: reservas.length,
        RESERVADA: reservas.filter(
            (reserva) => reserva.status === 'RESERVADA'
        ).length,
        COLETADA: reservas.filter(
            (reserva) => reserva.status === 'COLETADA'
        ).length,
        CANCELADA: reservas.filter(
            (reserva) => reserva.status === 'CANCELADA'
        ).length,
    }

    const reservasFiltradas = reservas
        .filter(
            (reserva) =>
                filtro === 'TODAS' || reserva.status === filtro
        )
        .sort(
            (a, b) =>
                new Date(b.dataReserva).getTime() -
                new Date(a.dataReserva).getTime()
        )

    return (
        <main className="minhas-reservas-container">
            <Link to="/feed" className="minhas-reservas-voltar">
                ← Voltar ao feed
            </Link>

            <h1>Minhas Reservas</h1>

            <p className="minhas-reservas-subtitulo">
                Acompanhe suas reservas ativas e consulte seu histórico
                de coletas e cancelamentos.
            </p>

            {erro && (
                <p className="minhas-reservas-erro" role="alert">
                    {erro}
                </p>
            )}

            {carregando ? (
                <p role="status">Carregando reservas...</p>
            ) : erro && reservas.length === 0 ? (
                <div className="minhas-reservas-vazio">
                    <h2>Não foi possível consultar suas reservas</h2>
                    <p>Tente atualizar a página novamente.</p>
                </div>
            ) : (
                <>
                    <div
                        className="minhas-reservas-filtros"
                        aria-label="Filtrar reservas por status"
                    >
                        {filtros.map((item) => (
                            <button
                                key={item.valor}
                                type="button"
                                className={
                                    `minhas-reservas-filtro ${
                                        filtro === item.valor ? 'ativo' : ''
                                    }`
                                }
                                aria-pressed={filtro === item.valor}
                                onClick={() => setFiltro(item.valor)}
                            >
                                <span>{item.titulo}</span>
                                <span className="minhas-reservas-contador">
                                    {contadores[item.valor]}
                                </span>
                            </button>
                        ))}
                    </div>

                    {reservasFiltradas.length === 0 ? (
                        <div className="minhas-reservas-vazio">
                            <h2>Nenhuma reserva encontrada</h2>
                            <p>{mensagensVazias[filtro]}</p>

                            {filtro === 'TODAS' ? (
                                <Link to="/feed">
                                    Explorar resíduos
                                </Link>
                            ) : (
                                <button
                                    type="button"
                                    className="minhas-reservas-limpar"
                                    onClick={() => setFiltro('TODAS')}
                                >
                                    Ver todas as reservas
                                </button>
                            )}
                        </div>
                    ) : (
                        <div className="minhas-reservas-lista">
                            {reservasFiltradas.map((reserva) => (
                                <article
                                    key={reserva.id}
                                    className="minhas-reservas-card"
                                >
                                    <div className="minhas-reservas-card-topo">
                                        <h2>{reserva.residuo.nome}</h2>

                                        <span
                                            className={
                                                `minhas-reservas-status status-${
                                                    reserva.status.toLowerCase()
                                                }`
                                            }
                                        >
                                            {nomesStatus[reserva.status]}
                                        </span>
                                    </div>

                                    <p>
                                        Categoria: {reserva.residuo.categoria}
                                    </p>

                                    <p>
                                        Região: {reserva.residuo.regiao}
                                    </p>

                                    <p>
                                        Data da reserva:{' '}
                                        {new Date(
                                            reserva.dataReserva
                                        ).toLocaleDateString('pt-BR')}
                                    </p>

                                    <div className="minhas-reservas-acoes">
                                        <Link
                                            to={`/residuos/${reserva.residuo.id}`}
                                        >
                                            Ver resíduo
                                        </Link>

                                        {reserva.status === 'RESERVADA' && (
                                            <button
                                                type="button"
                                                onClick={() =>
                                                    handleCancelar(reserva.id)
                                                }
                                                disabled={cancelandoId !== null}
                                                className="minhas-reservas-cancelar"
                                            >
                                                {cancelandoId === reserva.id
                                                    ? 'Cancelando...'
                                                    : 'Cancelar reserva'}
                                            </button>
                                        )}
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </>
            )}
        </main>
    )
}
