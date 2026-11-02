
import { useEffect, useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import {
    listarMinhasReservas,
    cancelarReserva,
    type Reserva,
} from '../api/reservas'
import { useAuth } from '../auth/AuthContext'
import './MinhasReservas.css'

export function MinhasReservas() {
    const { sessao } = useAuth()

    const [reservas, setReservas] = useState<Reserva[]>([])
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState('')
    const [cancelandoId, setCancelandoId] = useState<number | null>(null)

    useEffect(() => {
        if (!sessao) return

        let ativo = true

        async function carregarReservas() {
            try {
                const dados = await listarMinhasReservas()
                if (ativo) setReservas(dados)
            } catch {
                if (ativo) setErro('Não foi possível carregar suas reservas.')
            } finally {
                if (ativo) setCarregando(false)
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
        return <Navigate to="/login" replace state={{ from: '/minhas-reservas' }} />
    }

    return (
        <main className="minhas-reservas-container">
            <Link to="/feed" className="minhas-reservas-voltar">
                ← Voltar ao feed
            </Link>

            <h1>Minhas Reservas</h1>
            <p className="minhas-reservas-subtitulo">
                Acompanhe e gerencie suas reservas de resíduos.
            </p>

            {erro && <p className="minhas-reservas-erro" role="alert">{erro}</p>}

            {carregando ? (
                <p>Carregando reservas...</p>
            ) : reservas.length === 0 ? (
                <div className="minhas-reservas-vazio">
                    <h2>Nenhuma reserva encontrada</h2>
                    <p>Explore o feed e encontre resíduos disponíveis.</p>
                    <Link to="/feed">Explorar resíduos</Link>
                </div>
            ) : (
                <div className="minhas-reservas-lista">
                    {reservas.map((reserva) => (
                        <article key={reserva.id} className="minhas-reservas-card">
                            <div className="minhas-reservas-card-topo">
                                <h2>{reserva.residuo.nome}</h2>
                                <span className={`minhas-reservas-status status-${reserva.status.toLowerCase()}`}>
                                    {reserva.status}
                                </span>
                            </div>

                            <p>Categoria: {reserva.residuo.categoria}</p>
                            <p>Região: {reserva.residuo.regiao}</p>
                            <p>
                                Data da reserva:{' '}
                                {new Date(reserva.dataReserva).toLocaleDateString('pt-BR')}
                            </p>

                            <div className="minhas-reservas-acoes">
                                <Link to={`/residuos/${reserva.residuo.id}`}>
                                    Ver resíduo
                                </Link>

                                {reserva.status === 'RESERVADA' && (
                                    <button
                                        type="button"
                                        onClick={() => handleCancelar(reserva.id)}
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
        </main>
    )
}
