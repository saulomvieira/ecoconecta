
import { api } from './client'
import type { Residuo } from './residuos'

export type StatusReserva = 'RESERVADA' | 'COLETADA' | 'CANCELADA' | 'EXPIRADA'

export interface Reserva {
  id: number
  status: StatusReserva
  dataReserva: string
  // Fim do prazo: 7 dias após a reserva, ampliável pelo dono do resíduo
  dataExpiracao: string | null
  residuo: Residuo
}

interface NovaReservaRequest {
  usuarioId: number
  residuoId: number
}

export async function solicitarReserva(
  usuarioId: number,
  residuoId: number
): Promise<Reserva> {
  return api<Reserva>('/reservas', {
    method: 'POST',
    body: JSON.stringify({
      usuarioId,
      residuoId,
    } satisfies NovaReservaRequest),
  })
}

export async function listarMinhasReservas(): Promise<Reserva[]> {
  return api<Reserva[]>('/reservas/minhas')
}

export async function cancelarReserva(id: number): Promise<void> {
  return api<void>(`/reservas/${id}`, {
    method: 'DELETE',
  })
}

export interface ReservaRecebida {
  id: number
  status: StatusReserva
  dataReserva: string
  dataExpiracao: string
  residuoId: number
  reservadoPor: string
  // Contato de quem reservou (só dígitos, com DDD)
  telefone: string | null
}

export async function listarReservasRecebidas(): Promise<ReservaRecebida[]> {
  return api<ReservaRecebida[]>('/reservas/recebidas')
}

export async function confirmarColeta(id: number): Promise<Reserva> {
  return api<Reserva>(`/reservas/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ status: 'COLETADA' }),
  })
}

// Dono do resíduo amplia o prazo da reserva por mais 7 dias (sem limite de vezes)
export async function estenderReserva(id: number): Promise<ReservaRecebida> {
  return api<ReservaRecebida>(`/reservas/${id}/estender`, {
    method: 'PATCH',
  })
}
