
import { api } from './client'
import type { Residuo } from './residuos'

export type StatusReserva = 'RESERVADA' | 'COLETADA' | 'CANCELADA'

export interface Reserva {
  id: number
  status: StatusReserva
  dataReserva: string
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
  residuoId: number
  reservadoPor: string
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
